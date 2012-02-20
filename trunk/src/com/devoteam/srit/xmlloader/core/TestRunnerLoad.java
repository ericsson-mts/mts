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
 * TestRunnerSequential.java
 *
 * Created on 30 mai 2007, 11:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.core;

import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.newstats.StatKey;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;

import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 *
 * @author gpasquiers
 */
public class TestRunnerLoad extends TestRunner implements Runnable, NotificationListener<Notification<String, RunnerState>> {

    private ThreadRunner thread;
    private RunnerStateComputer runnerStateComputer;
    Semaphore semaphore;

    /** Creates a new instance of TestRunnerSequential */
    public TestRunnerLoad(Test test, List<Testcase> testcases) throws Exception {
        super(test.attributeValue("name"), test);

        if (testcases.isEmpty()) {
            throw new ParsingException("A test needs at least one selected testcase");
        }

        XMLDocumentCache.reset();

        this.thread = null;
        this.runnerStateComputer = new RunnerStateComputer(testcases.size());
        this.semaphore = new Semaphore(0);

        try {
            int i = 0;
            for (Testcase testcase : testcases) {
                TestcaseRunner testcaseRunner = new TestcaseRunner(this, testcase, 0, 0, 0, 0, false);
                testcaseRunner.getState().setIndex(i++);
                testcaseRunner.addListener(this);

                addChild(testcaseRunner);
            }
        }
        catch (Exception e) {
            this.stop();
            throw new Exception("Error while creating a testcase runner", e);
        }
    }

    /**
     * Starts the thread of the TestRunnerSequential
     */
    public void start() {
        if (null == thread) {
            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "TestRunnerLoad started");
            this.semaphore.drainPermits();
            this.thread = ThreadPool.reserve().start(this);
        }
        else {
            throw new RuntimeException("Thread already started");
        }
    }

    /**
     * Stops the thread of the TestRunnerLoad
     */
    public void stop() {
        // stop TestcaseRunners
        for (Runner runner : getChildren()) {
            if (null != runner) {
                runner.stop();
            }
        }

        this.semaphore.release(this.getChildren().size());
    }

    /**
     * Method executed by the thread
     */
    public void run() {
        // start TestcaseRunners
        for (Runner runner : getChildren()) {
            try {
                runner.start();
            }
            catch (Exception e) {
                StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TEST, this.getTest().getName(), "_failedNumber"), 1);
                GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, e, "Error in load runner : ", e);
            }
        }

        // wait for the end of TestcaseRunners
        for (Runner runner : getChildren()) {
            try {
                runner.acquire();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "TestRunnerLoad ended");

        // Notify it finished
        release();
    }

    public void notificationReceived(Notification<String, RunnerState> notification) {
        runnerStateComputer.update(notification.getData());
        RunnerState computedState = runnerStateComputer.getComputedState();
        this.getState().set(computedState);

        if (this.getState().changed()) {
            this.notifyAll(this.getState());
        }
    }
}
