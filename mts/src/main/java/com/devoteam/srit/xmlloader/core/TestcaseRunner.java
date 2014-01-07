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
 */

package com.devoteam.srit.xmlloader.core;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/**
 *
 * @author gpasquiers
 */
public class TestcaseRunner extends Runner implements Task,
        HierarchyMember<TestRunner, ScenarioRunner>,
        NotificationSender<Notification<String, RunnerState>>,
        NotificationListener<Notification<String, RunnerState>> {

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
    private DefaultNotificationSender<Notification<String, RunnerState>> defaultNotificationSender;

    public void addListener(NotificationListener<Notification<String, RunnerState>> listener) {
        this.defaultNotificationSender.addListener(listener);
        listener.notificationReceived(new Notification<String, RunnerState>(this.getName(), getState().clone()));
    }

    public void removeListener(NotificationListener listener) {
        this.defaultNotificationSender.removeListener(listener);
    }

    public void notifyAll(Notification<String, RunnerState> notification) {
        this.defaultNotificationSender.notifyAll(notification);
    }

    public void doNotifyAll() {
        this.defaultNotificationSender.notifyAll(new Notification<String, RunnerState>(this.getName(), getState().clone()));
    }
    private HashMap<String, RunnerState> _states = new HashMap();

    synchronized public void notificationReceived(Notification<String, RunnerState> notification) {
        RunnerState old = getState().clone();

        _states.put(notification.getSource(), notification.getData());

        RunnerState compiled = null;
        for (Entry<String, RunnerState> entry : _states.entrySet()) {
            if (null == compiled) {
                compiled = entry.getValue().clone();
            }
            else {
                compiled.merge(entry.getValue());
            }
        }

        if (compiled.isFailed()) {
            getState().setFlag(RunnerState.F_FAILED, true);
        }

        if (compiled.isInterrupted()) {
            getState().setFlag(RunnerState.F_INTERRUPTED, true);
        }

        if (compiled.isOpened()) {
            getState().setFlag(RunnerState.F_OPENED, true);
        }

        if (compiled.isStarted()) {
            getState().setFlag(RunnerState.F_STARTED, true);
        }

        if (!_scheduled && (compiled.isFinished() || compiled.couldNotStart())) {
            // do not copy finished state, onScenarioEnd will if it's finished in the testcase's point of view
            onScenariosEnd();
        }

        if (!old.sameValuesAs(getState())) {
            doNotifyAll();
        }
    }
    private Testcase _testcase;
    private double theoricalTimestamp;
    private double max_latency;
    private boolean _stopped;
    private boolean _startedSingle;
    private boolean _startedLoad;
    private boolean _scheduled;
    private long start_timestamp_stats;
    private RunProfile _profile;
    private RunProfileContext _context;
    // legacy semaphores pool
    private Semaphores semaphores;

    /**
     * Creates a new instance of TestcaseRunner
     */
    public TestcaseRunner(Testcase testcase) throws Exception {
        super(testcase.getName());

        this.defaultHierarchyMember = new DefaultHierarchyMember<TestRunner, ScenarioRunner>();
        this.defaultNotificationSender = new DefaultNotificationSender<Notification<String, RunnerState>>();

        this._testcase = testcase;

        this.setParameterPool(testcase.getParameterPool());

        this.max_latency = Config.getConfigByName("tester.properties").getDouble("loadRunner.MAX_LATENCY", 1) * 1000;
        this.semaphores = new Semaphores();
        this.theoricalTimestamp = 0;

        _stopped = false;
        _startedSingle = false;
        _startedLoad = false;
        _scheduled = false;

        // create scenario runners and add listeners
        for (ScenarioReference scenario : testcase.getChildren()) {
            ScenarioRunner runner = new ScenarioRunner(this, scenario);
            scenario.setScenarioRunner(runner);
            runner.addListener(this);
            addChild(runner);
        }

        final TestcaseRunner _this = this;

        addListener(new NotificationListener<Notification<String, RunnerState>>() {

            private boolean startedOld = false;
            private boolean finishedOld = false;

            @Override
            public void notificationReceived(Notification<String, RunnerState> notification) {
                if (!startedOld && notification.getData().isStarted()) {
                    if (null != _this.getParent()) {
                        StatPoolReset.instance().trigger(_this.getParent());
                    }
                    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TESTCASE, _this._testcase.getName(), "_currentNumber"), 1);
                }

                if (!finishedOld && notification.getData().isFinished()) {
                    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TESTCASE, _this._testcase.getName(), "_currentNumber"), -1);
                }

                startedOld = notification.getData().isStarted();
                finishedOld = notification.getData().isFinished();
            }
        });
    }

    public boolean detach(TestRunner testRunner) {
        synchronized (defaultHierarchyMember) {
            if (getParent() == testRunner) {
                setParent(null);
                return true;
            }
            return false;
        }
    }

    public synchronized boolean attach(TestRunner testRunner) {
        synchronized (defaultHierarchyMember) {
            if (getParent() == null) {
                setParent(testRunner);
                return true;
            }
            return false;
        }
    }

    /**
     * Parse all scenarios.
     * @throws java.lang.Exception
     */
    @Override
    public void init() throws Exception {
        try {
            _testcase.parseScenarios();
        }
        catch (Exception e) {
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, e, "Error parsing scenarios in TestcaseRunner");
            getState().setFlag(RunnerState.F_STARTED, true);
            getState().setFlag(RunnerState.F_FAILED, true);
            getState().setFlag(RunnerState.F_FINISHED, true);
            doNotifyAll();
            throw e;
        }
    }



    public void reset() {
        _stopped = false;
        _states.clear();
        resetState();
        for (ScenarioRunner runner : getChildren()) {
            runner.reset();
        }
    }

    /**
     * Starts the TestcaseRunner in "single" mode, using the number
     */
    public void startSingle() {
        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "TestcaseRunner started");

        try {
            /**
             * Compute the profile here because it must be done after we initialized
             * all  (testcase/test) parameter pools.
             */
            _profile = _testcase.getProfile();
            _context = _profile.createContext();

            getState()._executionsCurrent = 0;
            getState()._executionsEnd = Math.max(1, _testcase.getNumber());

            getState()._timeBegin = System.currentTimeMillis();
            getState()._timeCurrent = getState()._timeBegin;
            getState()._timeEnd = 0;
            _startedLoad = false;
            _startedSingle = true;
            doNotifyAll();
            init();
            synchronized (this) {
                _scheduled = true;
                _scheduler.execute(this, false);
            }
        }
        catch (Exception e) {
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, e, "Error starting TestcaseRunner");
            getState().setFlag(RunnerState.F_STARTED, true);
            getState().setFlag(RunnerState.F_FAILED, true);
            getState().setFlag(RunnerState.F_FINISHED, true);
            doNotifyAll();
        }
    }

    /**
     * Starts the TestcaseRunner using the run profile
     */
    public void startLoad() {
        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "TestcaseRunner started");

        try {
            /**
             * Compute the profile here because it must be done after we initialized
             * all  (testcase/test) parameter pools.
             */
            _profile = _testcase.getProfile();
            _context = _profile.createContext();
            init();

            getState()._executionsCurrent = 0;
            getState()._executionsEnd = this._profile.getExecutions();

            getState()._timeBegin = this._profile.getStartTime(this._context);
            getState()._timeCurrent = getState()._timeBegin;

            // if time based
            if (this._profile.getEndTime(this._context) > 0) {
                getState()._timeEnd = this._profile.getEndTime(this._context);
            }

            theoricalTimestamp = _profile.getStartTime(this._context);

            _startedLoad = true;
            _startedSingle = false;
            if (this._profile.nothingToDo()) {
                getState()._progression = 100;
                getState().setFlag(RunnerState.F_STARTED, true);
                getState().setFlag(RunnerState.F_FINISHED, true);
                doNotifyAll();
            }
            else {
                doNotifyAll();
                synchronized (this) {
                    _scheduled = true;
                    _scheduler.scheduleAt(this, (long) Math.round(theoricalTimestamp));
                }
            }
        }
        catch (Exception e) {
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, e, "Error starting TestcaseRunner");
            getState().setFlag(RunnerState.F_STARTED, true);
            getState().setFlag(RunnerState.F_FAILED, true);
            getState().setFlag(RunnerState.F_FINISHED, true);
            doNotifyAll();
        }
    }

    /**
     * Start all children scenarios.
     */
    @Override
    public synchronized void execute() {
        _scheduled = false;
        _testcase.incRunId();
        start_timestamp_stats = System.currentTimeMillis();

        // legacy: reset the semaphore pool
        semaphores.reset();

        for (ScenarioRunner runner : getChildren()) {
            runner.resetToOpened();
        }

        // Register all ScenarioRunners into the message dispatcher
        // for ScenarioName routing.
        for (ScenarioRunner runner : getChildren()) {
            DispatcherMsg.registerScenario(runner);
        }

        // Start all ScenarioRunners
        // The start method does not throws Exceptions and updates
        // the scenario RunnerState's State if an error occured
        for (ScenarioRunner runner : getChildren()) {
            runner.start();
        }

        StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TESTCASE, this._testcase.getName(), "_name"), this._testcase.getName());
        StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TESTCASE, this._testcase.getName(), "_description"), this._testcase.attributeValue("description"));
        StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TESTCASE, this._testcase.getName(), "_startNumber"), 1);
    }

    /**
     * Call stop on each ScenarioRunner
     */
    public synchronized void stop() {
        if (_testcase.isInterruptible() && !getState().isFinished() && !getState().couldNotStart()) {
            _stopped = true;

            for (ScenarioRunner scenarioRunner : this.getChildren()) {
                scenarioRunner.stop();
            }

            if (_startedLoad) {
                // if necessary, unschedule
                // in any case, schedule to NOW for the scenario to stop as INTERUPTED
                if (_scheduled) {
                    _scheduler.unschedule(this);
                    _scheduled = true;
                    _scheduler.execute(this, false);
                }
            }
        }
    }

    /**
     * Method called each time an execution of this testcase ended.
     *
     * This method will either schedule this testcase again or detect
     * this testcase ended and finalize the state and stop re-scheduling.
     */
    private void onScenariosEnd() {
        // Unregister all ScenarioRunners from the Stack
        for (ScenarioRunner runner : getChildren()) {
            DispatcherMsg.unregisterScenario(runner);
        }

        // Handle some statistics
        if (!getState().isInterrupted()) {
            double duration_stats = (System.currentTimeMillis() - start_timestamp_stats) / 1000.0;
            StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TESTCASE, _testcase.getName(), "_durationTime"), duration_stats);
            GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "Terminate a testcase (time = ", duration_stats, " s) for the transaction : ", toString());
            StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TESTCASE, _testcase.getName(), "_completeNumber"), 1);

            Iterator iter = _testcase.getScenarioPathByNameMap().entrySet().iterator();
            // while (iter.hasNext())
            {
            	// ScenarioReference scenario = (ScenarioReference) iter.next();
            	// if (scenario.)
            }

            for (Entry<String, RunnerState> entry : _states.entrySet()) 
            {
            	RunnerState scenarioState = entry.getValue();
            	if (scenarioState.isFailed()) 
            	{
            		StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TESTCASE, _testcase.getName(), "_failedNumber"), 1);
            		break;
            	}
            }
            // if (getState().isFailed()) {
            //    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TESTCASE, _testcase.getName(), "_failedNumber"), 1);
            //}
        }

        // Test, based on time, number of runs, or current state, if the
        // testcase has to end now or not (boolean ended).
        boolean ended = false;
        long current_time = System.currentTimeMillis();

        getState()._timeCurrent = current_time;
        getState()._executionsCurrent++;


        // compute progression
        //getState()._progression = 0;

        if (getState()._timeEnd > 0) {
            long timeProgression = (getState()._timeCurrent - getState()._timeBegin) * 100 / (getState()._timeEnd - getState()._timeBegin);
            getState()._progression = (int) Math.max(timeProgression, getState()._progression);
        }

        if (getState()._executionsEnd > 0) {
            long executionProgression = getState()._executionsCurrent * 100 / getState()._executionsEnd;
            getState()._progression = (int) Math.max(executionProgression, getState()._progression);
        }

        // check if testcase ended
        if (getState()._progression >= 100) {
            ended = true;
        }

        if (getState().isInterrupted()) {
            ended = true;
        }

        if (_stopped) {
            ended = true;
        }


        // schedule the next start of this testcase.
        if (!ended && _startedLoad) {
            // if started load mode, compute next run date according to run profile
            long now = System.currentTimeMillis();

            if (max_latency != 0 && this.theoricalTimestamp < now - max_latency) {
                this.theoricalTimestamp = now - max_latency;
            }


            double nextTheoricalTimestamp = this._profile.getNextDate(this.theoricalTimestamp, _context);

            if (RunProfile.PROFILE_INF == nextTheoricalTimestamp) {
                theoricalTimestamp = now;
            }
            else {
                theoricalTimestamp = nextTheoricalTimestamp;
            }

            // FIX: Check if the next start date is past the end date, if it is the case, end the test
            if (this._profile.getEndTime(_context) > 0 && theoricalTimestamp > this._profile.getEndTime(_context)) {
                ended = true;
            }
            else {
                synchronized (this) {
                    _scheduled = true;
                    if (theoricalTimestamp > now) {
                        _scheduler.scheduleAt(this, (long) Math.round(theoricalTimestamp));
                    }
                    else {
                        _scheduler.execute(this, false);
                    }
                }
            }
        }
        else if (!ended && _startedSingle) {
            // if started single, ignore run profile
            synchronized (this) {
                _scheduled = true;
                _scheduler.execute(this, false);
            }
        }

        // If the test ended (is not to be started again)
        if (ended) {
            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "TestcaseRunner ended");

            // Legacy: release the semaphore to free thread that could be waiting this testcase to finish.
            getState().setFlag(RunnerState.F_FINISHED, true);
        }
    }

    public Testcase getTestcase() {
        return _testcase;
    }

    public Semaphores getSemaphores() {
        return semaphores;
    }

    public String getRunId() {
        return _testcase.getName() + "_" + _testcase.getRunId();
    }
}
