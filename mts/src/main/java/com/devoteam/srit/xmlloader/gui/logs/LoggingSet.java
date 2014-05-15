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

import com.devoteam.srit.xmlloader.core.ScenarioReference;
import com.devoteam.srit.xmlloader.core.Testcase;
import com.devoteam.srit.xmlloader.core.TestcaseRunner;
import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.gui.JTableLogs;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 *
 * @author gpasquiers
 */
public class LoggingSet {

    private HashMap<String, JTableLogs> jTableMap;

    public LoggingSet(TestcaseRunner testcaseRunner, String testCaseName) {
        this(testcaseRunner.getTestcase(), testCaseName);
    }

    public LoggingSet(Testcase testcase, String testCaseName) {
        jTableMap = new HashMap();

        /*
         * Initialize the testcase logtable
         */
        int size = testcase.getScenarioPathByNameMap().size();

        String[] titles = new String[size + 3];

        titles[0] = "Date";
        titles[1] = "Level";
        titles[2] = "Topic";

        int i = 3;
        for (ScenarioReference scenario : testcase.getChildren()) {
            titles[i++] = scenario.getName();
        }

        this.jTableMap.put(null, new JTableLogs("(callflow) " + testcase.getName(), titles, testCaseName));


        /*
         * Initialize each scenario logtable
         */
        for (ScenarioReference scenario : testcase.getChildren()) {
        	if (scenario.getState())
        	{
	            JTableLogs jTableLogs = new JTableLogs(scenario.getName(), null, testCaseName);
	            this.jTableMap.put(scenario.getName(), jTableLogs);
	            jTableLogs.setTestcaseTableLogs(this.jTableMap.get(null));
        	}
        }
    }

    public JTableLogs getTestcaseLogs() {
        return this.jTableMap.get(null);
    }

    public JTableLogs getScenarioLogs(String key) {
        return this.jTableMap.get(key);
    }

    public HashMap<String, JTableLogs> getJTableMap() {
        return this.jTableMap;
    }

    public void save(URI logApplicationPathName) {
        for (Entry<String, JTableLogs> entry : this.jTableMap.entrySet()) {
            JTableLogs jTableLogs = entry.getValue();
            if (!jTableLogs.getTitle().toString().contains("callflow")) {
                jTableLogs.save(logApplicationPathName);
            }
        }
    }

    public void open(String testcaseIteration, String testCaseName, URI logApplicationPathName) throws Exception {

        int idx, idxScenario;
        int idxMini = 0;

        String scenarioName;
        String scenario = null;
        File logApplicationPath = new File(logApplicationPathName.getPath() + testcaseIteration);
        String pathNameFile = null;
        String[] listFile;
        listFile = logApplicationPath.list();
        File[] testCaseFile = new File[listFile.length];
        BufferedReader[] bufferedFile = new BufferedReader[listFile.length];
        JTableLogs[] jTableLogs = new JTableLogs[listFile.length];
        boolean[] logFormatCSV = new boolean[listFile.length];
        String[] titlesjTableMap = new String[3 + listFile.length];
        TextEvent[] textEvent = new TextEvent[listFile.length];

        titlesjTableMap[0] = "Date";
        titlesjTableMap[1] = "Level";
        titlesjTableMap[2] = "Topic";

        //prepare buffered file of testcase and know number of scenario
        for (idx = 0; idx < listFile.length; idx++) {
            scenario = listFile[idx];
            pathNameFile = logApplicationPath.getPath() + "/" + scenario;
            testCaseFile[idx] = new File(pathNameFile);
            bufferedFile[idx] = new BufferedReader(new FileReader(testCaseFile[idx]));
            if (bufferedFile[idx] == null) {
                throw new ParsingException("File not found : " + testCaseFile[idx]);
            }
            if (testCaseFile[idx].getName().contains(".csv")) {
                logFormatCSV[idx] = true;
            }
            else {
                logFormatCSV[idx] = false;
            }
            idxScenario = scenario.lastIndexOf(".");
            scenarioName = scenario.substring(0, idxScenario);
            titlesjTableMap[3 + idx] = scenarioName;
        }

        //link (callflow) tabbed panel with scenario tabbed panels
        this.jTableMap.put(null, new JTableLogs("(callflow) " + testCaseName, titlesjTableMap, testcaseIteration));

        //prepare jtable of each scenario
        for (idx = 0; idx < listFile.length; idx++) {
            scenario = listFile[idx];
            idxScenario = scenario.lastIndexOf(".");
            scenarioName = scenario.substring(0, idxScenario);
            jTableLogs[idx] = this.jTableMap.get(scenario);

            if (jTableLogs[idx] == null) {
                String[] titles = new String[4];
                titles[0] = "Date";
                titles[1] = "Level";
                titles[2] = "Topic";
                titles[3] = "Message";
                jTableLogs[idx] = new JTableLogs(scenarioName, titles, testcaseIteration);
            }

            this.jTableMap.put(scenarioName, jTableLogs[idx]);
            jTableLogs[idx].setTestcaseTableLogs(this.jTableMap.get(null));
        }


        // initialise textEvent[] with each first Event of scenario
        for (idx = 0; idx < listFile.length; idx++) {
            if (!jTableLogs[idx].getTitle().toString().contains("callflow")) {
                textEvent[idx] = jTableLogs[idx].readFile(bufferedFile[idx], logFormatCSV[idx]);
            }
        }


        // complete tabbed panel
        int completeFiles = 0;

        while (completeFiles != listFile.length) {

            //find which Event have the minimum index
            for (idx = 0; idx < listFile.length; idx++) {
                if (textEvent[idx] == null) {
                    completeFiles++;
                }
                else {
                    // if previous event displayed was the last event of his scenario,
                    // get an idx mini different than null
                    if (textEvent[idxMini] == null) {
                        idxMini = idx;
                    }
                    else {
                        if (textEvent[idx].getIndex() < textEvent[idxMini].getIndex()) {
                            idxMini = idx;
                        }
                    }
                }
            }

            // display Event and get the next event of this scenario
            if (textEvent[idxMini] != null) {
                if (!jTableLogs[idxMini].getTitle().toString().contains("callflow")) {
                    jTableLogs[idxMini].printText(textEvent[idxMini], true);
                    textEvent[idxMini] = jTableLogs[idxMini].readFile(bufferedFile[idxMini], logFormatCSV[idxMini]);
                }
            }

            if (completeFiles != listFile.length) {
                completeFiles = 0;
            }
        }

        //close buffered files
        for (idx = 0; idx < listFile.length; idx++) {
            bufferedFile[idx].close();
        }

    }

    public int search(String search, String scenarioName, int idx) {
        for (Entry<String, JTableLogs> entry : this.jTableMap.entrySet()) {
            JTableLogs jTableLogs = entry.getValue();
            if (jTableLogs.getTitle().toString().equals(scenarioName)) {
                return jTableLogs.search(search, idx);
            }
        }
        return -2;
    }
}
