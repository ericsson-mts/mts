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

package com.devoteam.srit.xmlloader.gui;

import com.devoteam.srit.xmlloader.core.RunProfile;
import com.devoteam.srit.xmlloader.core.Test;
import com.devoteam.srit.xmlloader.core.Tester;
import com.devoteam.srit.xmlloader.core.ThreadPool;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.gui.frames.JFrameAbout;
import com.devoteam.srit.xmlloader.gui.frames.JFrameLogsSession;
import com.devoteam.srit.xmlloader.gui.frames.JFrameRTStats;
import com.devoteam.srit.xmlloader.gui.frames.JFrameRunProfile;
import com.devoteam.srit.xmlloader.gui.logs.GUITextListenerProvider;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

/**
 * @author pn007888
 */
class TesterGuiHelper implements ActionListener {

    private final TesterGui testerGui;

    /**
     * Builds an helper to help tgui
     * @param tgui the gui to help.
     */
    public TesterGuiHelper(TesterGui tgui) {
        testerGui = tgui;
    }

    /**
     * Button callbacks
     * @param e the event
     */
    public void actionPerformed(ActionEvent e) {
        final String actionCommand = e.getActionCommand();

        if (actionCommand.equals(GUIMenuHelper.FILE_OPEN)) {
            //clear main log

            Tester.getInstance().close();

            ThreadPool.reserve().start(new Runnable() {

                public void run() {
                    testerGui.open(null);
                }
            });
        }
        else if (actionCommand.equals(GUIMenuHelper.FILE_CLOSE)) {

            EventQueue.invokeLater(new Runnable() {

                public void run() {
                   
                    if (Config.getConfigByName("tester.properties").getBoolean("logs.SAVE_BEFORE_PURGE", false)) {
                        // save application log
                        // URI logPath = URI.create(Config.getConfigByName("tester.properties").getString("logs.STORAGE_DIRECTORY", "../logs/"));
                         JFrameLogsSession jFrameLogsApplication = GUITextListenerProvider.instance().getJFrameLogsApplication();
                        // jFrameLogsApplication.saveLogs(logPath);
                        // save scenario log
                        // GUITextListenerProvider.instance().save(logPath);
                        
                        //clear application log
                        jFrameLogsApplication.clearLogs();
                  
                    }
                     testerGui.close_closeFile();
                    testerGui.close_closeGui();
                }
            });
        }
        else if (actionCommand.equals(GUIMenuHelper.FILE_RELOAD)) {
         
            ThreadPool.reserve().start(new Runnable() {

                public void run() {
                    testerGui.reload(true);
                }
            });
        }
        else if (actionCommand.equals(GUIMenuHelper.TEST_PLAN)) {
            testerGui.getTester().getTest().generateTestplan();
        }
        else if (actionCommand.equals(GUIMenuHelper.FILE_QUIT)) {
            System.exit(0);
        }
        else if (actionCommand.equals(GUIMenuHelper.TEST_RUN_SEQUENTIAL)) {
            ThreadPool.reserve().start(new Runnable() {

                public void run() {
                    TesterGui.instance().startTestcasesSequential();
                }
            });
        }
        else if (actionCommand.equals(GUIMenuHelper.TEST_RUN_LOAD)) {
            ThreadPool.reserve().start(new Runnable() {

                public void run() {
                    StatPool.getInstance().reset();
                    TesterGui.instance().startTestcasesLoad();
                }
            });
        }
        else if (actionCommand.equals(GUIMenuHelper.TEST_STOP)) {
            ThreadPool.reserve().start(new Runnable() {

                public void run() {
                    TesterGui.instance().stopRun();
                }
            });
        }
        else if (actionCommand.equals(GUIMenuHelper.TEST_STOP_ALL)) {
            ThreadPool.reserve().start(new Runnable() {

                public void run() {
                    TesterGui.instance().stopAll();
                }
            });
        }
        else if (actionCommand.equals(GUIMenuHelper.TEST_SELECT_ALL)) {
            testerGui.selected(true);
        }
        else if (actionCommand.equals(GUIMenuHelper.TEST_UNSELECT_ALL)) {
            testerGui.selected(false);
        }
        else if (actionCommand.equals(GUIMenuHelper.LOG_MAIN_LOG)) {
            JFrameLogsSession jFrameLogsApplication = GUITextListenerProvider.instance().getJFrameLogsApplication();
            jFrameLogsApplication.setVisible(!jFrameLogsApplication.isVisible());
            if (jFrameLogsApplication.isValid()) {
                jFrameLogsApplication.gotoBottom();
            }
        }
        else if (actionCommand.equals(GUIMenuHelper.LOG_SAVE)) {
            URI logApplicationPathName = testerGui.openDirectory();
            // logApplication equals null if cancel button is clicked
            if (logApplicationPathName != null) {
                //application log
                JFrameLogsSession jFrameLogsApplication = GUITextListenerProvider.instance().getJFrameLogsApplication();
                jFrameLogsApplication.saveLogs(logApplicationPathName);
                jFrameLogsApplication.clearLogs();
                //scenario log
                GUITextListenerProvider.instance().save(logApplicationPathName);
                GUITextListenerProvider.instance().clearLogs();
            }
        }
        else if (actionCommand.equals(GUIMenuHelper.LOG_OPEN)) {
            URI logApplicationPathName = testerGui.openDirectory();

            if (logApplicationPathName != null) {
                try {
                    GUITextListenerProvider.instance().openLogs(testerGui.instance().getTester().getTest(), logApplicationPathName);
                }
                catch (Exception ex) {
                    GlobalLogger.instance().getApplicationLogger().error(Topic.CORE, ex, "Open log directory : ", logApplicationPathName.getPath());
                }
            }
        }
        else if (actionCommand.equals(GUIMenuHelper.LOG_CLEAR_LOG)) {
            //scenario logs
            GUITextListenerProvider.instance().clearLogs();
            //main log
            JFrameLogsSession jFrameLogsApplication = GUITextListenerProvider.instance().getJFrameLogsApplication();
            jFrameLogsApplication.clearLogs();
        }
        else if (actionCommand.equals(GUIMenuHelper.LOG_LEVEL_DEBUG)) {
            Config.getConfigByName("tester.properties").setParameter("logs.MAXIMUM_LEVEL", "DEBUG");
        }
        else if (actionCommand.equals(GUIMenuHelper.LOG_LEVEL_INFO)) {
            Config.getConfigByName("tester.properties").setParameter("logs.MAXIMUM_LEVEL", "INFO");
        }
        else if (actionCommand.equals(GUIMenuHelper.LOG_LEVEL_WARNING)) {
            Config.getConfigByName("tester.properties").setParameter("logs.MAXIMUM_LEVEL", "WARN");
        }
        else if (actionCommand.equals(GUIMenuHelper.LOG_LEVEL_ERROR)) {
            Config.getConfigByName("tester.properties").setParameter("logs.MAXIMUM_LEVEL", "ERROR");
        }
        else if (actionCommand.equals(GUIMenuHelper.LOG_MODE_GUI)) {
            Config.getConfigByName("tester.properties").setParameter("logs.STORAGE_LOCATION", "MEMORY");
        }
        else if (actionCommand.equals(GUIMenuHelper.LOG_MODE_FILE)) {
            Config.getConfigByName("tester.properties").setParameter("logs.STORAGE_LOCATION", "FILE");
        }
        else if (actionCommand.equals(GUIMenuHelper.LOG_MODE_NONE)) {
            Config.getConfigByName("tester.properties").setParameter("logs.STORAGE_LOCATION", "DISABLE");
        }
        else if (actionCommand.equals(GUIMenuHelper.STATS_SHOW_RT_STATS)) {
            JFrameRTStats.instance().setVisible(false);
            JFrameRTStats.instance().setVisible(true);
        }
        else if (actionCommand.equals(GUIMenuHelper.RESET_STATS)) {
            StatPool.getInstance().reset();
        }
        else if (actionCommand.equals(GUIMenuHelper.REPORT_VIEW)) {
            if (null != TesterGui.instance().getTester()) {
                Test.report_show();
            }
        }
        else if (actionCommand.equals(GUIMenuHelper.REPORT_GENERATE)) {
            ThreadPool.reserve().start(new Runnable() {

                public void run() {
                    try {
                        testerGui.setCursor(true);
                        Tester.instance().getTest().report_generate();
                        testerGui.setCursor(false);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        else if (actionCommand.equals(GUIMenuHelper.STATS_AUTOMATIC_GENERATE)) {
            if (GUIMenuHelper.jCheckBoxMenuItemStatsAutomaticGenerate.isSelected()) {
                Config.getConfigByName("tester.properties").setParameter("stats.AUTOMATIC_GENERATE", "true");
            }
            else {
                Config.getConfigByName("tester.properties").setParameter("stats.AUTOMATIC_GENERATE", "false");
            }
        }
        else if (actionCommand.equals(GUIMenuHelper.STATS_AUTOMATIC_SHOW)) {
            if (GUIMenuHelper.jCheckBoxMenuItemStatsAutomaticShow.isSelected()) {
                Config.getConfigByName("tester.properties").setParameter("stats.AUTOMATIC_SHOW", "true");
            }
            else {
                Config.getConfigByName("tester.properties").setParameter("stats.AUTOMATIC_SHOW", "false");
            }
        }
        else if (actionCommand.equals(GUIMenuHelper.STATS_GENERATE_CHARTS_PICTURES)) {
            if (GUIMenuHelper.jCheckBoxMenuItemStatsGenerateChartsPictures.isSelected()) {
                Config.getConfigByName("tester.properties").setParameter("stats.GENERATE_CHARTS_PICTURES", "true");
            }
            else {
                Config.getConfigByName("tester.properties").setParameter("stats.GENERATE_CHARTS_PICTURES", "false");
            }
        }
        else if (actionCommand.equals(GUIMenuHelper.STATS_GENERATE_CHARTS_CSVS)) {
            if (GUIMenuHelper.jCheckBoxMenuItemStatsGenerateChartsCsvs.isSelected()) {
                Config.getConfigByName("tester.properties").setParameter("stats.GENERATE_CHARTS_CSVS", "true");
            }
            else {
                Config.getConfigByName("tester.properties").setParameter("stats.GENERATE_CHARTS_CSVS", "false");
            }
        }
        else if (actionCommand.equals(GUIMenuHelper.STATS_ACTIVATE_COUNTERS)) {
            if (GUIMenuHelper.jCheckBoxMenuItemStatsActivateCounters.isSelected()) {
                Config.getConfigByName("tester.properties").setParameter("stats.ACTIVATE_COUNTERS", "true");
            }
            else {
                Config.getConfigByName("tester.properties").setParameter("stats.ACTIVATE_COUNTERS", "false");
            }
        }
        else if (actionCommand.equals(GUIMenuHelper.WINDOWS_TEST_PROFILE)) {
            try {
                RunProfile profile = Tester.instance().getTest().getProfile();

                JFrameRunProfile jFrameRunProfile = JFrameRunProfile.getJFrame(profile);
                String title = "Edit run profile of test " + Tester.instance().getTest().getName();
                jFrameRunProfile.setTitle(title);
                jFrameRunProfile.setVisible(true);
            }
            catch (Exception ee) {
                ee.printStackTrace();
            }
        }
        else if (actionCommand.equals(GUIMenuHelper.WINDOWS_EDIT_CONF)) {
            testerGui.showJFrameConf();
        }
        else if (actionCommand.equals(GUIMenuHelper.WINDOWS_PARAMETERS)) {
            testerGui.getJFrameEditableParameters().setVisible(!testerGui.getJFrameEditableParameters().isVisible());
        }
        else if (actionCommand.equals(GUIMenuHelper.WINDOWS_OPEN_TEST_FILE)) {
            Utils.openEditor(testerGui.getTester().getTestXMLDocument().getXMLFile());
        }
        else if (actionCommand.equals(GUIMenuHelper.HELP_ABOUT)) {
            new JFrameAbout().setVisible(true);
        }
        else if (actionCommand.equals(GUIMenuHelper.HELP_DOCUMENTATION)) {
            try {
                String browser = Config.getConfigByName("tester.properties").getString("stats.BROWSER_PATH");
                browser = Utils.normalizePath(browser);
                if (browser != null)
            	{
	                String urlName = "../doc/MTS_doc_index.htm";
	                URI absURI = new File(urlName).toURI();
                    GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "Opening web browser with command :\n", browser, " ", absURI);
                    Runtime.getRuntime().exec(browser + " " + absURI);
            	}
            }
            catch (Exception ex) {
                GlobalLogger.instance().getApplicationLogger().error(Topic.CORE, ex, "Error occured while opening latest report");
                ex.printStackTrace();
            }
        }
        else if (actionCommand.equals(GUIMenuHelper.XML_GRAMMAR)) {
            try {
                String browser = Config.getConfigByName("tester.properties").getString("stats.BROWSER_PATH");
                browser = Utils.normalizePath(browser);
                if (browser != null)
            	{
                	URI absURI = new File("../doc/schemas/scenario.html").toURI();
	                Runtime.getRuntime().exec(browser + " " + absURI); // le lien est dans urlName
            	}
            }
            catch (Exception ex) {
                GlobalLogger.instance().getApplicationLogger().error(Topic.CORE, ex, "Error occured while opening latest report");
                ex.printStackTrace();
            }
        }
        else if (actionCommand.equals(GUIMenuHelper.HELP_WEBSITE)) {
            try {
                String browser = Config.getConfigByName("tester.properties").getString("stats.BROWSER_PATH");
                browser = Utils.normalizePath(browser);
                if (browser != null)
            	{
	                String urlName = "http://mts.arm-tool.com/";
	                Runtime.getRuntime().exec(browser + " " + urlName); // le lien est dans urlName
            	}
            }
            catch (Exception ex) {
                GlobalLogger.instance().getApplicationLogger().error(Topic.CORE, ex, "Error occured while opening latest report");
                ex.printStackTrace();
            }
        }
        else if (actionCommand.equals(GUIMenuHelper.GOOGLE_CODE)) {
            try {
                String browser = Config.getConfigByName("tester.properties").getString("stats.BROWSER_PATH");
                browser = Utils.normalizePath(browser);
                if (browser != null)
            	{
	                String urlName = "http://code.google.com/p/mts-project/";
	                Runtime.getRuntime().exec(browser + " " + urlName); // le lien est dans urlName
            	}
            }
            catch (Exception ex) {
                GlobalLogger.instance().getApplicationLogger().error(Topic.CORE, ex, "Error occured while opening latest report");
                ex.printStackTrace();
            }
        }
    }
}
