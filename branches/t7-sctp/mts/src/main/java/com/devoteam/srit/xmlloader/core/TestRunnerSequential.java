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
import java.util.Iterator;
import java.util.Map.Entry;

/**
 *
 * @author gpasquiers
 */
public class TestRunnerSequential extends TestRunner implements Runnable, NotificationListener<Notification<String, RunnerState>> {

    private ThreadRunner _threadRunner;
    private TestcaseRunner _currentTestcaseRunner;
    private boolean _stopped;
    private HashMap<String, RunnerState> _states = new HashMap();
    private Iterator<Testcase> _iterator;
    private int _testcaseDone;

    @Override
    public synchronized void notificationReceived(Notification<String, RunnerState> notification) {
        RunnerState old = getState().clone();
        _states.put(notification.getSource(), notification.getData());
        if ((notification.getData().isFinished() || notification.getData().couldNotStart())) {
            _currentTestcaseRunner.removeListener(this);
            _currentTestcaseRunner.detach(this);
            _currentTestcaseRunner = null;
            _testcaseDone++;
            ThreadPool.reserve().start(new Runnable() {

                @Override
                public void run() {
                    startNextTestcase();
                }
            });
        }
        compileStates();
        if(!old.sameValuesAs(getState())){
            doNotifyAll();
        }
    }

    public TestRunnerSequential(Test test) throws Exception {
        super(test.attributeValue("name"), test);
        _stopped = false;
        Cache.reset();
        getState()._progression = 0;
        _threadRunner = null;
        _currentTestcaseRunner = null;
        _iterator = getTest().getChildren().iterator();
        _testcaseDone = 0;
    }

    private synchronized void compileStates() {

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

        if (compiled.isFinished()) {
            getState().setFlag(RunnerState.F_FINISHED, true);
        }

        getState()._progression = _testcaseDone * 100 / _states.size();
    }

    /**
     * Starts the thread of the TestRunnerSequential
     */
    public void start() {
        if (null == _threadRunner) {
            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "TestRunnerSequential started");
            this._threadRunner = ThreadPool.reserve().start(this);
        }
        else {
            throw new RuntimeException("Thread already started");
        }
    }

    /**
     * Stops the thread of the TestRunnerSequential
     */
    public void stop() {
        _stopped = true;
        TestcaseRunner currentTestcaseRunner = _currentTestcaseRunner;
        if (null != currentTestcaseRunner) {
            currentTestcaseRunner.stop();
        }
    }

    /**
     * Method executes by the thread
     */
    public void run() {
        // Start TestcaseRunner
        for (Testcase testcase : getTest().getChildren()) {
            _states.put(testcase.getName(), new RunnerState());
        }

        compileStates();
        doNotifyAll();

        startNextTestcase();

        // prepare for running the tests

    }

    private void startNextTestcase() {
        if (_iterator.hasNext()) {
            Testcase testcase = _iterator.next();
            try {
                _currentTestcaseRunner = testcase.getTestcaseRunner();
                if (!_stopped && testcase.getState() && _currentTestcaseRunner.attach(this)) {
                    _currentTestcaseRunner.reset();
                    _currentTestcaseRunner.addListener(this);
                    _currentTestcaseRunner.init();
                    _currentTestcaseRunner.startSingle();
                }
                else {
                    _currentTestcaseRunner = null;
                    synchronized(this){
                        _states.remove(testcase.getName());
                    }
                    compileStates();
                    doNotifyAll();
                    startNextTestcase();
                }
            }
            catch (Exception e) {
                StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TEST, this.getTest().getName(), "_failedNumber"), 1);
                GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, e, "Error in sequential runner : ", e);
            }
        }
        else {
            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "TestRunnerSequential ended");
        }
    }
}
