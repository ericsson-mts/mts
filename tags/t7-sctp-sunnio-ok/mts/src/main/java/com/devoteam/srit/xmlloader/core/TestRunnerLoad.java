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

import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import java.util.HashMap;

/**
 *
 * @author gpasquiers
 */
public class TestRunnerLoad extends TestRunner implements Runnable, NotificationListener<Notification<String, RunnerState>> {

    private ThreadRunner _thread;
    final TestRunnerLoad _this = this;

    /** Creates a new instance of TestRunnerSequential */
    public TestRunnerLoad(Test test) throws Exception {
        super(test.attributeValue("name"), test);
        Cache.reset();

        _thread = null;

        try {
            for (Testcase testcase : test.getChildren()) {
                TestcaseRunner testcaseRunner = testcase.getTestcaseRunner();
                if (testcase.getState() && testcaseRunner.attach(this)) {
                    testcaseRunner.reset();
                    testcaseRunner.addListener(this);
                    addChild(testcaseRunner);
                }
            }

            if (getChildren().isEmpty()) {
                throw new Exception("No testcase to run");
            }
        }
        catch (Exception e) {
            this.stop();
            throw new Exception("Error while creating a testcase runner", e);
        }

        addListener(new NotificationListener<Notification<String, RunnerState>>() {
            @Override
            public void notificationReceived(Notification<String, RunnerState> notification) {
                if (notification.getData().isFinished() || notification.getData().couldNotStart()) {
                    for (TestcaseRunner runner : getChildren()) {
                        try {
                            runner.removeListener(_this);
                            runner.detach(_this);
                        }
                        catch (Exception e) {
                            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, e, "Error in load runner : ", e);
                        }
                    }

                    GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "TestRunnerLoad ended");

                    _this.removeListener(this);
                }
            }
        });
    }

    /**
     * Starts the thread of the TestRunnerSequential
     */
    public void start() {
        if (null == _thread) {
            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "TestRunnerLoad started");
            _thread = ThreadPool.reserve().start(this);
        }
        else {
            throw new RuntimeException("Thread already started");
        }
    }

    /**
     * Stops the thread of the TestRunnerLoad
     */
    @Override
    public void stop() {
        // stop TestcaseRunners
        for (TestcaseRunner runner : getChildren()) {
            if (null != runner) {
                runner.stop();
            }
        }
    }

    /**
     * Method executed by the thread
     */
    @Override
    public void run() {
        Cache.reset();
        // start TestcaseRunners
        for (TestcaseRunner runner : getChildren()) {
            try {
                runner.init();
            }
            catch (Exception e) {
                StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TEST, this.getTest().getName(), "_failedNumber"), 1);
                GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, e, "Error in load runner : ", e);
            }
        }
        
        for (TestcaseRunner runner : getChildren()) {
            try {
                runner.startLoad();
            }
            catch (Exception e) {
                StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TEST, this.getTest().getName(), "_failedNumber"), 1);
                GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, e, "Error in load runner : ", e);
            }
        }
    }
    
    private HashMap<String, RunnerState> _states = new HashMap();

    @Override
    synchronized public void notificationReceived(Notification<String, RunnerState> notification) {
        RunnerState old = getState().clone();
        _states.put(notification.getSource(), notification.getData());
        RunnerState compiled = null;

        RunnerState lowestAdvancement = null;

        for (RunnerState runnerState:_states.values()) {
            if (null == compiled) {
                compiled = runnerState.clone();
            }
            else {
                compiled.merge(runnerState);
            }

            if (null == lowestAdvancement) {
                lowestAdvancement = runnerState;
            }
            else {
                if (runnerState._progression < lowestAdvancement._progression) {
                    lowestAdvancement = runnerState;
                }
            }
        }

        compiled._executionsCurrent = lowestAdvancement._executionsCurrent;
        compiled._executionsEnd = lowestAdvancement._executionsEnd;
        compiled._timeBegin = lowestAdvancement._timeBegin;
        compiled._timeCurrent = lowestAdvancement._timeCurrent;
        compiled._timeEnd = lowestAdvancement._timeEnd;
        compiled._progression = lowestAdvancement._progression;

        setState(compiled);
        
        if(!old.sameValuesAs(getState())){
            doNotifyAll();
        }
    }
}
