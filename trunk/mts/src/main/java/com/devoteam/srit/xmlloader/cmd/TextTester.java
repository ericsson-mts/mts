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

package com.devoteam.srit.xmlloader.cmd;

import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.RunnerState;
import com.devoteam.srit.xmlloader.core.Test;
import com.devoteam.srit.xmlloader.core.TestRunner;
import com.devoteam.srit.xmlloader.core.TestRunnerLoad;
import com.devoteam.srit.xmlloader.core.TestRunnerSequential;
import com.devoteam.srit.xmlloader.core.TestRunnerSingle;
import com.devoteam.srit.xmlloader.core.Testcase;
import com.devoteam.srit.xmlloader.core.TestcaseRunner;
import com.devoteam.srit.xmlloader.core.Tester;
import com.devoteam.srit.xmlloader.core.ThreadPool;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.parameters.EditableParameterProvider;
import com.devoteam.srit.xmlloader.core.parameters.EditableParameterProviderHashMap;

import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import com.devoteam.srit.xmlloader.gui.model.ModelEditableParameters;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.dom4j.Element;

public class TextTester {

    private Tester tester;
    private TestRunner runner;
    private Semaphore semaphore;

    public TextTester(URI testFilename, String runnerName, List<String> parameterEditable) throws Exception {
        this.semaphore = new Semaphore(0);

        // Instanciate and start the tester
        this.tester = Tester.buildInstance();

        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "Starting MTS in text mode");

        // Opens the test file
        tester.open_reset();
        tester.open_openFile(testFilename, convertCmdParameter(parameterEditable));

        // Apply editable parameters on the command-line
        checkCmdParameter(parameterEditable);

        // Create the runner
        if ("-seq".equals(runnerName)) {
            runner = new TestRunnerSequential(tester.getTest());
        }
        else if ("-par".equals(runnerName)) {
            runner = new TestRunnerLoad(tester.getTest());
        }
        else if ("-testplan".equals(runnerName)) {
            tester.getTest().generateTestplan();
            System.exit(0);
        }
        else {
            Test test = tester.getTest();
            Testcase testcase = test.getTestcase(runnerName);
            // case with a bad testcase name parameter
            if (testcase == null) {
                TextImplementation.usage(
                        "The test \"" + testFilename
                        + "\" does not contains the testcase \""
                        + runnerName + "\"");
            }

            runner = new TestRunnerSingle(test, testcase);
        }

        for (TestcaseRunner testcaseRunner : runner.getChildren()) {
            testcaseRunner.init();
        }

        // Display editable parameters on the standard console
        displayEditableParameters(tester.getTest().getEditableParameters());

    }

    private EditableParameterProvider convertCmdParameter(List<String> params) {
        HashMap<String, String> map = new HashMap();
        for (String param : params) {
            String[] splitted1 = Utils.splitNoRegex(param, "+");
            splitted1[0] = ParameterPool.bracket(splitted1[0]);
            map.put(splitted1[0], splitted1[1]);
        }
        return new EditableParameterProviderHashMap(map);
    }

    private void checkCmdParameter(List<String> params) {
        for (String param : params) {
            String[] splitted1 = Utils.splitNoRegex(param, "+");
            String name = splitted1[0];
            String value = splitted1[1];

            name = ParameterPool.bracket(name);

            boolean find = false;
            for (Element element : tester.getTest().getEditableParameters()) {
                if (element.attributeValue("name").equals(name)) {
                    find = true;
                }
            }

            if (!find) {
                TextImplementation.usage("The editable parameter \"" + name + "\" does not exist in the test pool. Please check that point.");
            }
        }
    }

    private void displayEditableParameters(List<Element> elements) {
        String ret = "";
        for (Element element : elements) {
            ret += "\r\n";
            ret += element.attributeValue("name");
            ret += "=";
            ret += element.attribute("value").getValue();
        }
        System.out.println(ret);
    }

    public void start() throws Exception {
        // Start the input handler
        ThreadPool.reserve().start(new InputHandler(this));
        this.runner.init();
        
        for (Testcase testcase : this.runner.getTest().getChildren()) {
            final TestcaseRunner testcaseRunner = testcase.getTestcaseRunner();
          
            // register listener for FAILED or INTERRUPTED state
            testcaseRunner.addListener(new NotificationListener<Notification<String, RunnerState>>() {

                public void notificationReceived(Notification<String, RunnerState> notification) {
                    if ((notification.getData().isFailed() || notification.getData().isInterrupted()) && notification.getData().isFinished()) {
                    	if (testcaseRunner.getParent() != null) {
                    		System.out.println(notification.getData().toLegacyStatus() + ": " + testcaseRunner.getParent().getName() + " / " + testcaseRunner.getName());
                    	}
                        try {
                            testcaseRunner.removeListener(this);
                        }
                        catch (Exception e) {
                            // ignore
                        }
                    }
                }
            });
        }

        // Start the runner
        this.runner.start();

        // Wait for the end of the runner and release the semaphore
        runner.addListener(new NotificationListener<Notification<String, RunnerState>>() {

            boolean _couldBeFinished = false;

            @Override
            public void notificationReceived(Notification<String, RunnerState> notification) {
                if (_couldBeFinished && (notification.getData().isFinished() || notification.getData().couldNotStart())) {
                    runner.removeListener(this);
                    semaphore.release();
                }
                else {
                    _couldBeFinished = true;
                }
            }
        });
    }

    public void stop() throws Exception {
        this.runner.stop();
    }

    public void acquire() throws Exception {
        semaphore.acquire();
    }

    public void release() {
        semaphore.release();
    }

    public TestRunner getRunner() {
        return runner;
    }
}
