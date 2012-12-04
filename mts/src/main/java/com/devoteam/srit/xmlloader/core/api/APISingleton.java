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

package com.devoteam.srit.xmlloader.core.api;

import com.devoteam.srit.xmlloader.core.Test;
import com.devoteam.srit.xmlloader.core.TestRunner;
import com.devoteam.srit.xmlloader.core.TestRunnerLoad;
import com.devoteam.srit.xmlloader.core.TestRunnerSequential;
import com.devoteam.srit.xmlloader.core.Testcase;
import com.devoteam.srit.xmlloader.core.TestcaseRunner;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.pluggable.ParameterOperatorRegistry;
import com.devoteam.srit.xmlloader.core.pluggable.ParameterTestRegistry;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.report.ReportGenerator;
import com.devoteam.srit.xmlloader.core.utils.XMLDocument;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author gpasquiers
 */
public class APISingleton {

    static private HashMap<String, APISingleton> instances = new HashMap();

    synchronized static public APISingleton instance() {
        return APISingleton.instance("default_instance");
    }

    synchronized static public APISingleton instance(String name) {
        if (null == APISingleton.instances.get(name)) {
            APISingleton.instances.put(name, new APISingleton());
        }

        return APISingleton.instances.get(name);
    }

    /**
     * Generates an MTS report.
     * This report will be written to the directory stats.REPORT_DIRECTORY setted
     * in the tester.properties conf file.
     */
    static public ReportGenerator getReportGenerator(String reportDir) throws Exception {
        return new ReportGenerator(reportDir);
    }

    synchronized static public void reset() {
        StackFactory.reset();
        StatPool.getInstance().reset();
    }
    private Test test;
    private TestRunner testRunner;

    /**
     * Class constructor. Initializes all necessary variables for MTS.
     * The FSInterface HAS TO be initialized prior to this constructor.
     */
    private APISingleton() {

        /*
         * Initialize the pluggable components
         */
        if (APISingleton.instances.size() == 0) {
            ParameterOperatorRegistry.initialize();
            ParameterTestRegistry.initialize();
        }

        /*
         * Initialize class attributes
         */
        this.test = null;
    }

    /**
     * Prepares the test for execution (opening and creating runner).
     * @param testURI URI to the test.xml file in the HashMapFileSystem
     * @param runnerClass TestRunnerLoad.class or TestRunnerSequential.class
     */
    public void openTest(URI testURI, Class runnerClass) throws Exception {
        if (null != this.testRunner) {

            if(testRunner.getState().isFinished()){
                this.testRunner = null;
            }
            else{
                throw new Exception("Cannot open a test if one is already running");
            }
        }

        if (APISingleton.instances.size() == 1) {
            APISingleton.reset();
        }

        this.test = null;

        XMLDocument x = new XMLDocument();
        x.setXMLFile(testURI);
        x.setXMLSchema(new URI("../conf/schemas/test.xsd"));
        x.parse();

        this.test = new Test(x, null);

        List<Testcase> testcaseList = test.getTestcaseList();
        List<Testcase> enabledTestcaseList = new LinkedList<Testcase>();
        List<Integer> numberToRunList = new LinkedList<Integer>();
        for (Testcase testcase : testcaseList) {
            if (null == testcase.attributeValue("state") || testcase.attributeValue("state").equalsIgnoreCase("true")) {
                enabledTestcaseList.add(testcase);
                String number = testcase.attributeValue("number");
                if (number == null) {
                    number = "1";
                }
                numberToRunList.add(new Integer(number));
            }
        }

        if (runnerClass.equals(TestRunnerSequential.class)) {
            this.testRunner = new TestRunnerSequential(test);
        }
        else if (runnerClass.equals(TestRunnerLoad.class)) {
            this.testRunner = new TestRunnerLoad(test);
        }
        else {
            throw new Exception("unknown runner class " + runnerClass);
        }

        for (TestcaseRunner testcaseRunner : this.testRunner.getChildren()) {
            testcaseRunner.init();
        }
    }

    /**
     * Returns the TestRunner. This allows to register listeners in order
     * to be notified of the progression (and ending!) of the test.
     * @return the current TestRunner
     */
    public TestRunner getTestRunner() {
        return this.testRunner;
    }
}
