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

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;

import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;

/**
 *
 * @author gpasquiers
 */
public class TestRunnerSingle extends TestRunner implements Runnable, NotificationListener<Notification<String, RunnerState>> {

    private ThreadRunner thread;

    public TestRunnerSingle(Test test, String testcaseName, int numberToRun) throws Exception {
        this(test, (Testcase) test.getTestcase(testcaseName), numberToRun);
    }

    public TestRunnerSingle(Test test, Testcase testcase, int numberToRun) throws Exception {
        super(test.attributeValue("name"), test);

        XMLDocumentCache.reset();

        thread = null;

        try {
            TestcaseRunner testcaseRunner = new TestcaseRunner(this, testcase, numberToRun, -1, 0, 0, false);
            testcaseRunner.getState().setIndex(0);
            testcaseRunner.addListener(this);
            addChild(testcaseRunner);
            testcaseRunner.init(true);
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
        // increment statistics
        //statAddValue(/*0,*/ false);

        if (null == thread) {

            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "TestRunnerSingle started");

            thread = ThreadPool.reserve();
            thread.start(this);
        }
        else {
            throw new RuntimeException("Thread already started");
        }
    }

    /**
     * Stops the thread of the TestRunnerSequential
     */
    public void stop() {
        // stop TestcaseRunners
        for (Runner runner : getChildren()) {
            if (null != runner) {
                runner.stop();
            }
        }
    }

    /**
     * Method executes by the thread
     */
    public void run() {
        // Start TestcaseRunner
        this.getChildren().get(0).start();

        // Wait for the end of the runner
        try {
            this.getChildren().get(0).acquire();
        }
        catch (InterruptedException e) {
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, e, "Error in single runner. The thread go interrupted. This should not happen. Continuing anyway.");
        }

        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "TestRunnerSingle ended");

        // Notify it finished
        release();
    }

    public void notificationReceived(Notification<String, RunnerState> notification) {
        this.getState().set(notification.getData());
        if (this.getState().changed()) {
            this.notifyAll(this.getState());
        }
    }
}
