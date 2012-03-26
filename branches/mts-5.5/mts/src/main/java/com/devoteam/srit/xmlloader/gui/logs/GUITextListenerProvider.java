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

package com.devoteam.srit.xmlloader.gui.logs;

import com.devoteam.srit.xmlloader.core.ScenarioRunner;
import com.devoteam.srit.xmlloader.core.Testcase;
import com.devoteam.srit.xmlloader.core.TestcaseRunner;
import com.devoteam.srit.xmlloader.core.Test;
import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextListener;
import com.devoteam.srit.xmlloader.core.log.TextListenerKey;
import com.devoteam.srit.xmlloader.core.log.TextListenerProvider;
import com.devoteam.srit.xmlloader.gui.frames.JFrameLogsSession;
import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author gpasquiers
 */
public class GUITextListenerProvider implements TextListenerProvider {

    static private GUITextListenerProvider instance;

    static public synchronized GUITextListenerProvider instance() {
        if (null == instance) {
            instance = new GUITextListenerProvider();
        }
        return instance;
    }
    private HashMap<Testcase, JFrameLogsSession> testcaseFrames;
    private JFrameLogsSession jFrameLogsApplication;
    //private JFrameLogsSession jFrameLogsSessionUnique;
    //private Testcase testcaseUnique;

    private GUITextListenerProvider() {
        testcaseFrames = new HashMap<Testcase, JFrameLogsSession>();
        jFrameLogsApplication = new JFrameLogsSession(true);
        //jFrameLogsSessionUnique = null;
        //testcaseUnique = null;
    }

    public TextListener provideTextListenerForSession(ScenarioRunner runner) {
        TestcaseRunner testcaseRunner = runner.getParent();
        if (null == testcaseRunner) {
            return null;
        }

        Testcase testcase = testcaseRunner.getTestcase();
        LoggingSet loggingSet = getJFrameLogsSession(testcase).getLoggingSet(runner.getParent());
        return loggingSet.getScenarioLogs(runner.getName());
    }

    public TextListener provideTextListenerForTestcase(TestcaseRunner runner) {
        Testcase testcase = runner.getTestcase();
        LoggingSet loggingSet = getJFrameLogsSession(testcase).getLoggingSet(null);
        return loggingSet.getTestcaseLogs();
    }

    public TextListener provideTextListenerForApplication() {
        //return jFrameLogsSessionUnique.getLogTable();
        return jFrameLogsApplication.getLogTable();
    }

    public JFrameLogsSession getJFrameLogsSession(Testcase testcase) {
        if (!testcaseFrames.containsKey(testcase)) {
            JFrameLogsSession jFrameLogsSession = new JFrameLogsSession(false);
            testcaseFrames.put(testcase, jFrameLogsSession);
        }
        return testcaseFrames.get(testcase);
    }

    public JFrameLogsSession getJFrameLogsApplication() {
        return jFrameLogsApplication;
    }

    /**public JFrameLogsSession getJFrameLogsSessionUnique(Testcase testcase){
    if (this.jFrameLogsSessionUnique == null){
    this.jFrameLogsSessionUnique = new JFrameLogsSession(false);
    }
    this.setTestcase(testcase);
    this.jFrameLogsSessionUnique.setLogsTab(getJFrameLogsSession(testcase).getLogTable());
    return this.jFrameLogsSessionUnique;
    }*/
    /**public void setJFrameLogsSessionUnique (JFrameLogsSession jFrameLogsSessionUnique){
    this.jFrameLogsSessionUnique = jFrameLogsSessionUnique;
    }
    
    public JFrameLogsSession getJFrameLogsApplication(){
    return this.jFrameLogsApplication;
    }*/
    public void reset() {
        testcaseFrames.clear();
    }

    public void clearLogs() {
        Set<Entry<Testcase, JFrameLogsSession>> entrySet = this.testcaseFrames.entrySet();
        Iterator<Entry<Testcase, JFrameLogsSession>> it = entrySet.iterator();
        Entry<Testcase, JFrameLogsSession> o;
        while (it.hasNext()) {
            o = it.next();
            o.getValue().clearLogs();
        }
    }

    public void save(URI logApplicationPathName) {
        /* Set<Entry<Testcase, JFrameLogsSession>> entrySet = this.testcaseFrames.entrySet();
        Iterator<Entry<Testcase, JFrameLogsSession>> it = entrySet.iterator();
        Entry<Testcase, JFrameLogsSession> o;
        while (it.hasNext()) {
        o = it.next();
        o.getValue().saveLogs();
        }*/
        for (Entry<Testcase, JFrameLogsSession> entry : this.testcaseFrames.entrySet()) {
            JFrameLogsSession jFrameLogsSession = entry.getValue();
            jFrameLogsSession.saveLogs(logApplicationPathName);
        }
    }

    public void openLogs(Test test, URI logApplicationPathName) throws Exception {

        // Application log
        this.jFrameLogsApplication.clearLogs();
        this.jFrameLogsApplication.openLogs(null, null, logApplicationPathName);


        // Scenario Log
        int idx, idxTestCase;
        File logApplicationPath = new File(logApplicationPathName);
        String[] listFile;
        listFile = logApplicationPath.list();
        String testCaseName = "";
        String testCaseExist = "";
        int testCaseID = 0;

        for (idx = 0; idx < listFile.length; idx++) {
            File testCaseDirectory = new File(logApplicationPathName.getPath() + listFile[idx]);
            if (testCaseDirectory.isDirectory()) {
                idxTestCase = listFile[idx].lastIndexOf("_");

                if (idxTestCase >= 0) {
                    testCaseName = listFile[idx].substring(0, idxTestCase);
                    if (test.getTestcaseList().contains(test.getTestcase(testCaseName))) {
                        Testcase testCase = test.getTestcase(testCaseName);
                        if (!(testCaseName.equalsIgnoreCase(testCaseExist))) {
                            testCaseExist = testCaseName;
                            JFrameLogsSession jFrameLogsSession = getJFrameLogsSession(testCase);
                            jFrameLogsSession.clearLogs();
                            this.testcaseFrames.put(testCase, jFrameLogsSession);
                            jFrameLogsSession.openLogs(testCase, listFile[idx], logApplicationPathName);
                        }
                        int testCaseIDTemp = Integer.parseInt(listFile[idx].substring(idxTestCase + 1));
                        //test what is the maximum ID because listFile[] is in alphabetical order (1, 11, 12, 2, 3, ...)
                        if (testCaseIDTemp > testCaseID) {
                            testCaseID = testCaseIDTemp;
                        }
                        testCase.setRunId(testCaseID);
                    }
                }
            }
        }
    }

    public TextListener provide(TextListenerKey key) {
        if (GlobalLogger.instance().getLogStorage() != GlobalLogger.LOG_STORAGE_MEMORY) {
            return null;
        }

        if (null == key) {
            return this.provideTextListenerForApplication();
        }

        if (key instanceof ScenarioRunner) {
            return this.provideTextListenerForSession((ScenarioRunner) key);
        }

        return null;
    }

    public void dispose(TextListenerKey key) {
        if (testcaseFrames.containsKey(key)) {
            TextListener textListener = provide(key);
            if (null != textListener) {
                textListener.dispose();
            }
            this.testcaseFrames.remove(key);
        }
    }
    /**public void setTestcase (Testcase testcase){
    if (this.testcaseUnique == null){
    //this.testcaseUnique = new Testcase(testcase.getParent());
    }
    else{
    this.testcaseUnique = testcase;
    }
    } */
}
