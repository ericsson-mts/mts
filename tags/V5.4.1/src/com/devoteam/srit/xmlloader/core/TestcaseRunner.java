/*
* Copyright 2012 Devoteam http://www.devoteam.com
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
*
*
* This file is part of Multi-Protocol Test Suite (MTS).
*
* Multi-Protocol Test Suite (MTS) is free software: you can redistribute
* it and/or modify it under the terms of the GNU General Public License 
* as published by the Free Software Foundation, either version 3 of the 
* License.
* 
* Multi-Protocol Test Suite (MTS) is distributed in the hope that it will
* be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Multi-Protocol Test Suite (MTS).  
* If not, see <http://www.gnu.org/licenses/>. 
*
*//*
 * TestcaseRunner.java
 *
 * Created on 30 mai 2007, 10:40
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.core;

import com.devoteam.srit.xmlloader.core.RunnerState.State;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.newstats.StatKey;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.newstats.StatPoolReset;
import com.devoteam.srit.xmlloader.core.protocol.DispatcherMsg;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.hierarchy.DefaultHierarchyMember;
import com.devoteam.srit.xmlloader.core.utils.hierarchy.HierarchyMember;
import com.devoteam.srit.xmlloader.core.utils.notifications.DefaultNotificationSender;
import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationSender;
import gp.utils.scheduler.Task;

import java.util.Iterator;
import java.util.List;

/**
 *
 * @author gpasquiers
 */
public class TestcaseRunner extends Runner implements Task,
        HierarchyMember<TestRunner, ScenarioRunner>,
        NotificationSender<Notification<String, RunnerState>>,
        NotificationListener<Notification<String, RunnerState>> {

    // <editor-fold defaultstate="collapsed" desc="Hierarchy implementation">
    private DefaultHierarchyMember<TestRunner, ScenarioRunner> defaultHierarchyMember;

    public TestRunner getParent() {
        return this.defaultHierarchyMember.getParent();
    }

    public List<ScenarioRunner> getChildren() {
        return this.defaultHierarchyMember.getChildren();
    }

    public void setParent(TestRunner parent) {
        this.defaultHierarchyMember.setParent(parent);
    }

    public void addChild(ScenarioRunner child) {
        this.defaultHierarchyMember.addChild(child);
    }

    public void removeChild(ScenarioRunner child) {
        this.defaultHierarchyMember.removeChild(child);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="NotificationSender Implementation">
    private DefaultNotificationSender<Notification<String, RunnerState>> defaultNotificationSender;

    public void addListener(NotificationListener<Notification<String, RunnerState>> listener) {
        this.defaultNotificationSender.addListener(listener);

        listener.notificationReceived(new Notification<String, RunnerState>(this.getName(), this.getState()));
    }

    public void removeListener(NotificationListener listener) {
        this.defaultNotificationSender.removeListener(listener);
    }

    public void notifyAll(Notification<String, RunnerState> notification) {
        this.defaultNotificationSender.notifyAll(notification);
    }

    public void tryNotifyAll() {
        if (this.getState().changed()) {
            this.defaultNotificationSender.notifyAll(new Notification<String, RunnerState>(this.getName(), this.getState()));
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="NotificationListener Implementation">
    synchronized public void notificationReceived(Notification<String, RunnerState> notification) {
        this.currentRunnerStateComputer.update(notification.getData());
        this.globalRunnerStateComputer.update(notification.getData());
        RunnerState computedRunnerState = this.globalRunnerStateComputer.getComputedState();
        this.getState().setState(computedRunnerState.getState());
        this.tryNotifyAll();

        // trigger state computing in order to have a correct realComputedStatechanged() value
        this.currentRunnerStateComputer.getComputedState();

        if (this.currentRunnerStateComputer.realComputedStateChanged()) {
            switch (this.currentRunnerStateComputer.getRealComputedState().getState()) {
                case SUCCEEDED:
                case FAILED:
                case INTERRUPTED:
                    this.onScenariosEnd();
            }
        }
    }
    // </editor-fold>

    // override changeState temporarily
    @Override
    public void changeState(RunnerState.State aState) {
        synchronized (getState()) {
            switch (getState().getState()) {
                case INTERRUPTING:
                    if (aState == State.INTERRUPTED) {
                        this.getState().setState(State.INTERRUPTED);
                    }
                case INTERRUPTED:
                    break;
                default:
                    this.getState().setState(aState);
                    break;
            }
        }

        if (this.getState().changed()) {
            this.defaultNotificationSender.notifyAll(new Notification<String, RunnerState>(this.getName(), this.getState()));
        }
    }
    private Testcase testcase;
    private long executions;
    private double theoricalTimestamp;
    private double max_latency;
    private boolean softInterrupt;
    // Semaphores pool
    private Semaphores semaphores;
    private RunnerStateComputer globalRunnerStateComputer;
    private RunnerStateComputer currentRunnerStateComputer;
    private RunProfile profile;
    private RunProfileContext context;

    /**
     * Creates a new instance of TestcaseRunner
     */
    public TestcaseRunner(TestRunner testRunner, Testcase testcase, long executions, long duration, long start_timestamp /* not used ATM */, long period /* not used ATM */, boolean strictPeriod) throws Exception {
        super(testcase.getName());
        this.globalRunnerStateComputer = new RunnerStateComputer(testcase.getScenarioPathByNameMap().size());
        this.globalRunnerStateComputer.setCanBeFinal(false);

        this.currentRunnerStateComputer = new RunnerStateComputer(testcase.getScenarioPathByNameMap().size());
        this.currentRunnerStateComputer.setCanBeFinal(true);

        this.defaultHierarchyMember = new DefaultHierarchyMember<TestRunner, ScenarioRunner>();
        this.defaultNotificationSender = new DefaultNotificationSender<Notification<String, RunnerState>>();
        this.setParent(testRunner);

        this.testcase = testcase;

        this.setParameterPool(testcase.getParameterPool());

        this.tryNotifyAll();

        this.max_latency = Config.getConfigByName("tester.properties").getDouble("loadRunner.MAX_LATENCY", 1) * 1000;
        this.semaphores = new Semaphores();
        this.softInterrupt = false;
        this.theoricalTimestamp = 0;

        this.executions = executions;

        if (this.executions > 0) {
            getState().setExecutionsEnd(this.executions);
        }

        final TestcaseRunner _this = this;

        this.addListener(new NotificationListener<Notification<String, RunnerState>>() {

            private State lastState = null;

            public void notificationReceived(Notification<String, RunnerState> notification) {
                // starting of test
                if (lastState != State.RUNNING
                        && lastState != State.FAILING
                        && lastState != State.INTERRUPTING && (notification.getData().getState() == State.RUNNING
                        || notification.getData().getState() == State.FAILING
                        || notification.getData().getState() == State.INTERRUPTING)) {
                    StatPoolReset.instance().trigger(_this.getParent());
                    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TESTCASE, _this.testcase.getName(), "_currentNumber"), 1);
                }

                // ending of test
                if (lastState != State.SUCCEEDED
                        && lastState != State.FAILED
                        && lastState != State.INTERRUPTED && (notification.getData().getState() == State.SUCCEEDED
                        || notification.getData().getState() == State.FAILED
                        || notification.getData().getState() == State.INTERRUPTED)) {
                    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TESTCASE, _this.testcase.getName(), "_currentNumber"), -1);
                }
                lastState = notification.getData().getState();
            }
        });

    }

    @Override
    public void init() throws Exception {
        this.init(false);
    }

    /**
     * Parse all scenarios and create associated scenarioRunners.
     * @param force force the parsing of the scenario XML files, even if it has already been done.
     * @throws java.lang.Exception
     */
    public void init(boolean force) throws Exception {
        try {
            int i;

            this.getChildren().clear();

            i = 0;
            if (force || !testcase.parsedScenarios()) {
                RunnerState runnerState = new RunnerState();
                runnerState.setState(State.OPENING);
                runnerState.setIndex(i);
                Notification<String, RunnerState> notification = new Notification(null, runnerState);
                this.notificationReceived(notification);
                testcase.parseScenarios();

                i++;
            }

            i = 0;
            for (Scenario scenario : testcase.getChildren()) {
                // add scenario to runners;
                ScenarioRunner runner = new ScenarioRunner(this, scenario);
                runner.getState().setIndex(i);
                runner.addListener(this);
                this.addChild(runner);
                i++;
            }
        }
        catch (Exception e) {
            this.getState().setState(State.FAILED);
            this.tryNotifyAll();
            throw new Exception(e);
        }
    }

    public void reset(){
        // remove listeners
        for (ScenarioRunner runner : getChildren()) {
            runner.getScenario().free();
            runner.free();
            runner.removeListener(this);
        }

        // clear testcase runner children
        getChildren().clear();

        // clear testcase's children
        testcase.reset();
    }

    private long start_timestamp_stats;

    /**
     * Starts the TestcaseRunner: parameters operations as well as some parsing.
     */
    public void start() {
        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "TestcaseRunner started");

        try {
            /**
             * Compute the profile here because it must be done after we initialized
             * all  (testcase/test) parameter pools.
             */
            this.profile = this.testcase.getProfile();
            this.context = this.profile.createContext();

            if (0 == this.executions) {
                this.executions = this.profile.getExecutions();
            }
            if (this.executions > 0) {
                getState().setExecutionsEnd(this.executions);
            }

            getState().setTimeBegin(this.profile.getStartTime(this.context));
            getState().setTimeCurrent(getState().getTimeBegin());

            // if time based
            if (this.profile.getEndTime(this.context) > 0) {
                getState().setTimeEnd(this.profile.getEndTime(this.context));
            }

            this.theoricalTimestamp = this.profile.getStartTime(this.context);

            if (this.profile.nothingToDo()) {
                getState().setProgression(100);
                changeState(RunnerState.State.SUCCEEDED);
                release();
            }
            else {
                scheduler.scheduleAt(this, (long) Math.round(theoricalTimestamp));
            }
        }
        catch (Exception e) {
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, e, "Error starting TestcaseRunner");
            changeState(RunnerState.State.FAILED);
        }
    }

    /**
     * Call stop on each ScenarioRunner
     */
    @SuppressWarnings("empty-statement")
    public void stop() {
        if (!this.testcase.interruptible()) {
            softInterrupt = true;
            return;
        }

        switch (this.getState().getState()) {
            case SUCCEEDED:
            case FAILED:
            case INTERRUPTED:
                break;
            default:
                for (final ScenarioRunner runner : this.getChildren()) {
                    if (null != runner) {
                        /**
                         * Use the scheduler to trigger the stops in order to stop
                         * all testcases at once and not sequencially.
                         */
                        scheduler.execute(new Task() {

                            public void execute() {
                                runner.stop();
                            }
                        }, false);
                    }
                }

                // if the task was successfuly unscheduled then schedule it to NOW
                // in order to have the scenarios see the INTERRUPTING and go to
                // INTERRUPTED state
                while (scheduler.unschedule(this));
                break;
        }

    }

    /**
     * Start all children scenarios.
     */
    public void execute() {
        if (this.currentRunnerStateComputer.getRealComputedState().getState() != State.INTERRUPTED) {
            this.currentRunnerStateComputer.reset();
        }

        this.testcase.incRunId();
        start_timestamp_stats = System.currentTimeMillis();

        // legacy: reset the semaphore pool
        semaphores.reset();


        // Register all ScenarioRunners into the message dispatcher
        // for ScenarioName routing.
        for (Runner runner : getChildren()) {
            DispatcherMsg.registerScenario(runner);
        }

        // Start all ScenarioRunners
        // The start method does not throws Exceptions and updates
        // the scenario RunnerState's State if an error occured
        ScenarioRunner runner = null;
        Iterator<ScenarioRunner> iter = getChildren().iterator();
        while (iter.hasNext())
        {
        	runner = 	iter.next();
        	runner.start();
        }
        /*
        for (ScenarioRunner runner : getChildren()) {
            runner.start();
        }
		*/
        
        StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TESTCASE, this.testcase.getName(), "_name"), this.testcase.getName());
        StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TESTCASE, this.testcase.getName(), "_description"), this.testcase.attributeValue("description"));
        StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TESTCASE, this.testcase.getName(), "_startNumber"), 1);
    }

    /**
     * Method called each time an execution of this testcase ended.
     *
     * This method will either schedule this testcase again or detect
     * this testcase ended and finalize the state and stop re-scheduling.
     */
    private void onScenariosEnd() {
        // Unregister all ScenarioRunners from the Stack
        for (Runner runner : getChildren()) {
            DispatcherMsg.unregisterScenario(runner);
        }

        // Handle some statistics
        switch (this.currentRunnerStateComputer.getRealComputedState().getState()) {
            case SUCCEEDED:
                break;
            case FAILED:
                StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TESTCASE, testcase.getName(), "_failedNumber"), 1);
                break;
            case INTERRUPTED:
                break;
        }

        switch (this.currentRunnerStateComputer.getRealComputedState().getState()) {
            case SUCCEEDED:
            case FAILED:
                double duration_stats = (System.currentTimeMillis() - start_timestamp_stats) / 1000.0;
                StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TESTCASE, testcase.getName(), "_durationTime"), duration_stats);
                GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "Terminate a testcase (time = ", duration_stats, " s) for the transaction : ", toString());
                StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TESTCASE, testcase.getName(), "_completeNumber"), 1);
        }

        // Test, based on time, number of runs, or current state, if the
        // testcase has to end now or not (boolean ended).
        boolean ended = false;
        long current_time = System.currentTimeMillis();

        getState().setTimeCurrent(current_time);
        getState().setExecutionsCurrent(getState().getExecutionsCurrent() + 1);

        if (getState().getProgression() >= 100) {
            ended = true;
        }

        switch (this.globalRunnerStateComputer.getRealComputedState().getState()) {
            case INTERRUPTED:
                ended = true;
        }

        if (this.softInterrupt) {
            ended = true;
        }

        // Schedule the next start of this testcase.
        if (!ended) {
            switch (this.currentRunnerStateComputer.getRealComputedState().getState()) {
                case SUCCEEDED:
                case FAILED:

                    // Compute the next start date.
                    long now = System.currentTimeMillis();

                    if (max_latency != 0 && this.theoricalTimestamp < now - max_latency) {
                        this.theoricalTimestamp = now - max_latency;
                    }


                    double nextTheoricalTimestamp = this.profile.getNextDate(this.theoricalTimestamp, context);

                    if (RunProfile.PROFILE_INF == nextTheoricalTimestamp) {
                        theoricalTimestamp = now;
                    }
                    else {
                        theoricalTimestamp = nextTheoricalTimestamp;
                    }


                    // FIX: Check if the next start date is past the end date, if it is the case, end the test
                    if (this.profile.getEndTime(context) > 0 && theoricalTimestamp > this.profile.getEndTime(context)) {
                        ended = true;
                    }
                    else {
                        if (theoricalTimestamp > now) {
                            scheduler.scheduleAt(this, (long) Math.round(theoricalTimestamp));
                        }
                        else {
                            scheduler.execute(this, false);
                        }
                    }
                    break;
                case INTERRUPTED:
                    throw new RuntimeException("should not happen");
            }
        }

        // If the test ended (is not to be started again)
        if (ended) {
            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "TestcaseRunner ended");

            // Trigger a new update of the TestcaseRunner's state
            this.globalRunnerStateComputer.setCanBeFinal(true);
            this.getState().setState(this.globalRunnerStateComputer.getComputedState().getState());
            this.tryNotifyAll();

            // Legacy: release the semaphore to free thread that could be waiting this testcase to finish.
            release();
        }
    }

    public Testcase getTestcase() {
        return testcase;
    }

    public Semaphores getSemaphores() {
        return semaphores;
    }

    public String getRunId() {
        return testcase.getName() + "_" + testcase.getRunId();
    }
}
