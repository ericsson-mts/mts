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

import com.devoteam.srit.xmlloader.core.ThreadPool;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.Config;
import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.LinkedList;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 * @author pn007888
 */
public class GUIMenuHelper {
    // File jMenu action commands

    public static final String FILE_OPEN = "FILE_OPEN";
    public static final String FILE_CLOSE = "FILE_CLOSE";
    public static final String FILE_RELOAD = "FILE_RELOAD";
    public static final String FILE_QUIT = "FILE_QUIT";
    public static final String TEST_PLAN = "TEST_PLAN";
    // Test jMenu action commands
    public static final String TEST_RUN_SEQUENTIAL = "TEST_RUN_SEQUENTIAL";
    public static final String TEST_RUN_LOAD = "TEST_RUN_LOAD";
    public static final String TEST_STOP = "TEST_STOP";
    public static final String TEST_STOP_ALL = "TEST_STOP_ALL";
    public static final String TEST_SELECT_ALL = "TEST_SELECT_ALL";
    public static final String TEST_UNSELECT_ALL = "TEST_UNSELECT_ALL";
    // Log jMenu action commands
    public static final String LOG_MAIN_LOG = "LOG_MAIN_LOG";
    public static final String LOG_LEVEL_DEBUG = "LOG_LEVEL_DEBUG";
    public static final String LOG_LEVEL_INFO = "LOG_LEVEL_INFO";
    public static final String LOG_LEVEL_WARNING = "LOG_LEVEL_WARNING";
    public static final String LOG_LEVEL_ERROR = "LOG_LEVEL_ERROR";
    public static final String LOG_SAVE = "SAVE_LOG";
    public static final String LOG_OPEN = "OPEN_LOG";
    public static final String LOG_CLEAR_LOG = "CLEAR_LOG";
    public static final String LOG_MODE_GUI = "LOG_MODE_GUI";
    public static final String LOG_MODE_FILE = "LOG_MODE_FILE";
    public static final String LOG_MODE_NONE = "LOG_MODE_NONE";
    // Windows jMenu action commands
    public static final String WINDOWS_TEST_PROFILE = "WINDOWS_TEST_PROFILE";
    public static final String WINDOWS_EDIT_CONF = "WINDWS_EDIT_CONF";
    public static final String WINDOWS_PARAMETERS = "WINDOWS_PARAMETERS";
    public static final String WINDOWS_OPEN_TEST_FILE = "WINDOWS_OPEN_TEST_FILE";
    // Report jMenu action commands
    public static final String REPORT_GENERATE = "REPORT_GENERATE";
    public static final String REPORT_VIEW = "REPORT_VIEW";
    //-----------------------------------------
    public static final String STATS_SHOW_RT_STATS = "STATS_SHOW_RT_STATS";
    //-----------------------------------------
    public static final String STATS_AUTOMATIC_GENERATE = "STATS_AUTOMATIC_GENERATE";
    public static final String STATS_AUTOMATIC_SHOW = "STATS_AUTOMATIC_SHOW";
    public static final String STATS_GENERATE_CHARTS_PICTURES = "STATS_GENERATE_CHARTS_PICTURES";
    public static final String STATS_GENERATE_CHARTS_CSVS = "STATS_GENERATE_CHARTS_CSVS";
    public static final String STATS_ACTIVATE_COUNTERS = "STATS_ACTIVATE_COUNTERS";
    //-----------------------------------------
    public static final String RESET_STATS = "RESET_STATS";
    // Help jMenu action commands
    //-----------------------------------------
    public static final String HELP_DOCUMENTATION = "HELP_DOCUMENTATION";
    public static final String XML_GRAMMAR = "HELP_GRAMMAR";
    public static final String HELP_WEBSITE = "HELP_WEBSITE";
    public static final String GOOGLE_CODE = "GOOGLE_CODE";
    public static final String HELP_ABOUT = "HELP_ABOUT";
    
    private JMenuBar jMenuBar;
    private JMenu jMenuFile;
    private JMenuItem jMenuItemOpen;
    private JMenuItem jMenuItemClose;
    private JMenuItem jMenuItemReload;
    private JMenuItem jMenuItemTestPlan;
    private JMenu jMenuRecents;
    private JMenuItem jMenuRecentsClear;
    private JMenuItem jMenuItemEmpty;
    //private RecentFiles recentFiles;
    private JMenu jMenuTest;
    private JMenuItem jMenuItemRunSequential;
    private JMenuItem jMenuItemRunParallel;
    private JMenuItem jMenuItemStop;
    private JMenuItem jMenuItemStopAll;
    private JMenuItem jMenuItemSelectAll;
    private JMenuItem jMenuItemUnselectAll;
    private JMenu jMenuLog;
    private ButtonGroup buttonGroupLogLevel;
    private JRadioButtonMenuItem jRadioButtonMenuItemDebugLevel;
    private JRadioButtonMenuItem jRadioButtonMenuItemInfoLevel;
    private JRadioButtonMenuItem jRadioButtonMenuItemWarningLevel;
    private JRadioButtonMenuItem jRadioButtonMenuItemErrorLevel;
    private ButtonGroup buttonGroupLogMode;
    private JRadioButtonMenuItem jRadioButtonMenuItemLogsModeGui;
    private JRadioButtonMenuItem jRadioButtonMenuItemLogsModeFile;
    private JRadioButtonMenuItem jRadioButtonMenuItemLogsModeNone;
    private JMenuItem jMenuParameters;
    private JMenuItem jMenuConf;
    private JMenuItem jMenuRunProfile;
    private JMenuItem jMenuOpenTestFile;
    private JMenu jMenuReport;
    public static JCheckBoxMenuItem jCheckBoxMenuItemStatsAutomaticGenerate;
    public static JCheckBoxMenuItem jCheckBoxMenuItemStatsAutomaticShow;
    public static JCheckBoxMenuItem jCheckBoxMenuItemStatsGenerateChartsPictures;
    public static JCheckBoxMenuItem jCheckBoxMenuItemStatsGenerateChartsCsvs;
    public static JCheckBoxMenuItem jCheckBoxMenuItemStatsActivateCounters;
    private JMenu jMenuHelp;
    // <editor-fold desc="Recents file menu" defaultState="collapsed">
    private static LinkedList<String> recents = new LinkedList<String>();

    public static String getFirstRecent() {
        if (recents.size() > 0) {
            return recents.getFirst();
        }
        return null;
    }

    public void addToRecents(String path) {
        if (recents.contains(path)) {
            recents.remove(path);
        }
        recents.addFirst(path);
        this.updateRecents();
    }

    private void updateRecents() {
        this.jMenuRecents.removeAll();

        if (this.recents.isEmpty()) {
            this.jMenuRecents.add(this.jMenuItemEmpty);
        }
        else {
            for (String path : this.recents) {
                javax.swing.JMenuItem jMenuItem = new javax.swing.JMenuItem();
                jMenuItem.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        jMenuItemRecentsItemActionPerformed(evt);
                    }
                });

                jMenuItem.setText(path);

                jMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/text-x-generic.png")));

                this.jMenuRecents.add(jMenuItem);
            }
        }

        this.jMenuRecents.add(new javax.swing.JSeparator());
        this.jMenuRecents.add(this.jMenuRecentsClear);
        this.jMenuRecents.revalidate();

        this.dumpRecentsToFile();
    }

    private void dumpRecentsToFile() {
        try {
            PrintStream printStream = new PrintStream(new FileOutputStream("../conf/tester.recents"));
            for (String path : this.recents) {
                printStream.println(path);
            }
            printStream.close();
        }
        catch (IOException e) {
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, "Unable to save recent files");
        }
    }

    public static void readRecentsFromFile() {
        recents.clear();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("../conf/tester.recents"));
            String line;
            while (null != (line = bufferedReader.readLine())) {
                if (line.length() != 0) {
                    recents.add(line);
                }
            }
            bufferedReader.close();
        }
        catch (FileNotFoundException e) {
            // this is not an error
        }
        catch (IOException e) {
            e.printStackTrace();
            //Utils.showError(null, "Error in readRecentsFromFile()", e);
        }
    }

    private void jMenuItemRecentsItemActionPerformed(final java.awt.event.ActionEvent evt) {
        ThreadPool.reserve().start(new Runnable() {

            public void run() {
                JMenuItem jMenuItem = (JMenuItem) evt.getSource();
                try {
                    TesterGui.instance().open(new URI(jMenuItem.getText()));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // </editor-fold>
    public JMenuBar getJMenuBar() {
        return jMenuBar;
    }

//    public RecentFiles getRecentFiles()
//    {
//        return recentFiles;
//    }
    private JMenuItem createJMenuItem(ActionListener actionListener, String name, String actionCommand) {
        JMenuItem jMenuItem = new JMenuItem(name);
        jMenuItem.addActionListener(actionListener);
        jMenuItem.setActionCommand(actionCommand);
        return jMenuItem;
    }

    private JRadioButtonMenuItem createJRadioButtonMenuItem(ActionListener actionListener, ButtonGroup buttonGroup, String name, String actionCommand) {
        JRadioButtonMenuItem jRadioButtonMenuItem = new JRadioButtonMenuItem(name);
        jRadioButtonMenuItem.addActionListener(actionListener);
        jRadioButtonMenuItem.setActionCommand(actionCommand);

        buttonGroup.add(jRadioButtonMenuItem);

        return jRadioButtonMenuItem;
    }

    private JCheckBoxMenuItem createJCheckBoxMenuItem(ActionListener actionListener, String name, String actionCommand) {
        JCheckBoxMenuItem jCheckBoxMenuItem = new JCheckBoxMenuItem(name);
        jCheckBoxMenuItem.addActionListener(actionListener);
        jCheckBoxMenuItem.setActionCommand(actionCommand);

        return jCheckBoxMenuItem;
    }

    private JMenu createJMenuFile(ActionListener actionListener) {
        JMenu jMenu = new JMenu("File");
        jMenu.add(jMenuItemOpen = createJMenuItem(actionListener, "Open", FILE_OPEN));
        // initialize recents menu

        this.jMenuRecents = new javax.swing.JMenu();
        this.jMenuRecents.setText("Open");
        jMenu.add(this.jMenuRecents);


        this.jMenuItemEmpty = new javax.swing.JMenuItem();
        this.jMenuItemEmpty.setText("...");
        this.jMenuItemEmpty.setEnabled(false);
        this.jMenuRecents.add(this.jMenuItemEmpty);

        this.jMenuRecents.add(new javax.swing.JSeparator());

        this.jMenuRecentsClear = new javax.swing.JMenuItem();
        this.jMenuRecentsClear.setText("Clear history");
        this.jMenuRecentsClear.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemClearActionPerformed(evt);
            }
        });
        this.jMenuRecents.add(this.jMenuRecentsClear);

        GUIMenuHelper.readRecentsFromFile();
        this.updateRecents();

        jMenu.add(new JSeparator());
        jMenu.add(jMenuItemReload = createJMenuItem(actionListener, "Reload", FILE_RELOAD));
        jMenu.add(jMenuItemClose = createJMenuItem(actionListener, "Close", FILE_CLOSE));



        jMenu.add(new JSeparator());
        jMenu.add(jMenuOpenTestFile = createJMenuItem(actionListener, "Edit", WINDOWS_OPEN_TEST_FILE));
        jMenu.add(jMenuItemTestPlan = createJMenuItem(actionListener, "Generate Test Plan", TEST_PLAN));
        jMenu.add(new JSeparator());
        jMenu.add(createJMenuItem(actionListener, "Quit", FILE_QUIT));
        return jMenu;
    }

    private JMenu createJMenuTest(ActionListener actionListener) {
        JMenu jMenu = new JMenu("Test");
        jMenu.add(jMenuItemRunSequential = createJMenuItem(actionListener, "Run: sequential", TEST_RUN_SEQUENTIAL));
        jMenu.add(jMenuItemRunParallel = createJMenuItem(actionListener, "Run: parallel", TEST_RUN_LOAD));

        jMenu.add(new JSeparator());

        jMenu.add(jMenuItemStop = createJMenuItem(actionListener, "Stop run", TEST_STOP));
        jMenu.add(jMenuItemStopAll = createJMenuItem(actionListener, "Stop all", TEST_STOP_ALL));

        jMenu.add(new JSeparator());

        jMenu.add(jMenuItemSelectAll = createJMenuItem(actionListener, "Select all", TEST_SELECT_ALL));
        jMenu.add(jMenuItemUnselectAll = createJMenuItem(actionListener, "Unselect all", TEST_UNSELECT_ALL));

        jMenu.add(new JSeparator());

        jMenu.add(jMenuParameters = createJMenuItem(actionListener, "Edit parameters", WINDOWS_PARAMETERS));
        jMenu.add(jMenuConf = createJMenuItem(actionListener, "Edit configuration", WINDOWS_EDIT_CONF));

        jMenu.add(jMenuRunProfile = createJMenuItem(actionListener, "Edit run profile", WINDOWS_TEST_PROFILE));

        return jMenu;
    }

    private JMenu createJMenuLog(ActionListener actionListener) {
        JMenu jMenu = new JMenu("Log");
        jMenu.addMenuListener(new MenuListener() {

            public void menuSelected(MenuEvent e) {
                updateLogLevelFromConfig();
                updateLogStorageFromConfig();
            }

            public void menuDeselected(MenuEvent e) {
            }

            public void menuCanceled(MenuEvent e) {
            }
        });
        jMenu.add(createJMenuItem(actionListener, "Application", LOG_MAIN_LOG));



        /*
         * Log storage (disable(0) / file(1) / gui(2))
         */
        jMenu.add(new JSeparator());

        buttonGroupLogMode = new ButtonGroup();
        jMenu.add(jRadioButtonMenuItemLogsModeGui = createJRadioButtonMenuItem(actionListener, buttonGroupLogMode, "Logging in GUI", LOG_MODE_GUI));
        jMenu.add(jRadioButtonMenuItemLogsModeFile = createJRadioButtonMenuItem(actionListener, buttonGroupLogMode, "Logging in Files", LOG_MODE_FILE));
        jMenu.add(jRadioButtonMenuItemLogsModeNone = createJRadioButtonMenuItem(actionListener, buttonGroupLogMode, "Logging disabled", LOG_MODE_NONE));
        updateLogStorageFromConfig();

        /*
         * Log level (ERROR(0) / WARN(1) / INFO(2) / DEBUG(3) 
         */

        jMenu.add(new JSeparator());

        buttonGroupLogLevel = new ButtonGroup();
        jMenu.add(jRadioButtonMenuItemDebugLevel = createJRadioButtonMenuItem(actionListener, buttonGroupLogLevel, "Debug level", LOG_LEVEL_DEBUG));
        jMenu.add(jRadioButtonMenuItemInfoLevel = createJRadioButtonMenuItem(actionListener, buttonGroupLogLevel, "Info level", LOG_LEVEL_INFO));
        jMenu.add(jRadioButtonMenuItemWarningLevel = createJRadioButtonMenuItem(actionListener, buttonGroupLogLevel, "Warning level", LOG_LEVEL_WARNING));
        jMenu.add(jRadioButtonMenuItemErrorLevel = createJRadioButtonMenuItem(actionListener, buttonGroupLogLevel, "Error level", LOG_LEVEL_ERROR));
        updateLogLevelFromConfig();

        jMenu.add(new JSeparator());

        jMenu.add(createJMenuItem(actionListener, "Save", LOG_SAVE));
        jMenu.add(createJMenuItem(actionListener, "Open", LOG_OPEN));

        jMenu.add(new JSeparator());

        jMenu.add(createJMenuItem(actionListener, "Clear all", LOG_CLEAR_LOG));

        return jMenu;
    }

    public void updateLogLevelFromConfig() {
        switch (GlobalLogger.instance().getLogLevel()) {
            case TextEvent.DEBUG: {
                buttonGroupLogLevel.setSelected(jRadioButtonMenuItemDebugLevel.getModel(), true);
                break;
            }
            case TextEvent.INFO: {
                buttonGroupLogLevel.setSelected(jRadioButtonMenuItemInfoLevel.getModel(), true);
                break;
            }
            case TextEvent.WARN: {
                buttonGroupLogLevel.setSelected(jRadioButtonMenuItemWarningLevel.getModel(), true);
                break;
            }
            case TextEvent.ERROR: {
                buttonGroupLogLevel.setSelected(jRadioButtonMenuItemErrorLevel.getModel(), true);
                break;
            }
        }
    }

    public void updateLogStorageFromConfig() {
        int logStorage = GlobalLogger.instance().getLogStorage();
        switch (logStorage) {
            case 0: {
                buttonGroupLogMode.setSelected(jRadioButtonMenuItemLogsModeNone.getModel(), true);
                break;
            }
            case 1: {
                buttonGroupLogMode.setSelected(jRadioButtonMenuItemLogsModeFile.getModel(), true);
                break;
            }
            case 2: {
                buttonGroupLogMode.setSelected(jRadioButtonMenuItemLogsModeGui.getModel(), true);
                break;
            }
        }
    }

    private JMenu createJMenuReport(ActionListener actionListener) {
        JMenu jMenu = new JMenu("Stats");
        jMenu.addMenuListener(new MenuListener() {

            public void menuSelected(MenuEvent e) {
                updateStatsStorageFromConfig();
            }

            public void menuDeselected(MenuEvent e) {
            }

            public void menuCanceled(MenuEvent e) {
            }
        });
        jMenu.add(createJMenuItem(actionListener, "Show real-time", STATS_SHOW_RT_STATS));
        jMenu.add(createJMenuItem(actionListener, "Generate report HTML", REPORT_GENERATE));
        jMenu.add(createJMenuItem(actionListener, "Show last report", REPORT_VIEW));
        jMenu.add(new javax.swing.JSeparator());
        jMenu.add(jCheckBoxMenuItemStatsAutomaticGenerate = createJCheckBoxMenuItem(actionListener, "Automatic generate", STATS_AUTOMATIC_GENERATE));
        jMenu.add(jCheckBoxMenuItemStatsAutomaticShow = createJCheckBoxMenuItem(actionListener, "Automatic show", STATS_AUTOMATIC_SHOW));
        jMenu.add(jCheckBoxMenuItemStatsGenerateChartsPictures = createJCheckBoxMenuItem(actionListener, "Generate charts pictures", STATS_GENERATE_CHARTS_PICTURES));
        jMenu.add(jCheckBoxMenuItemStatsGenerateChartsCsvs = createJCheckBoxMenuItem(actionListener, "Generate charts CSVS", STATS_GENERATE_CHARTS_CSVS));
        jMenu.add(jCheckBoxMenuItemStatsActivateCounters = createJCheckBoxMenuItem(actionListener, "Activate counters", STATS_ACTIVATE_COUNTERS));
        jCheckBoxMenuItemStatsActivateCounters.setVisible(false);
        jMenu.add(new javax.swing.JSeparator());
        jMenu.add(createJMenuItem(actionListener, "Reset all", RESET_STATS));

        updateStatsStorageFromConfig();

        return jMenu;
    }

    public void updateStatsStorageFromConfig() {
        String AUTOMATIC_GENERATE = Config.getConfigByName("tester.properties").getString("stats.AUTOMATIC_GENERATE", "");
        String AUTOMATIC_SHOW = Config.getConfigByName("tester.properties").getString("stats.AUTOMATIC_SHOW", "");
        String GENERATE_CHARTS_PICTURES = Config.getConfigByName("tester.properties").getString("stats.GENERATE_CHARTS_PICTURES", "");
        String GENERATE_CHARTS_CSVS = Config.getConfigByName("tester.properties").getString("stats.GENERATE_CHARTS_CSVS", "");
        String ACTIVATE_COUNTERS = Config.getConfigByName("tester.properties").getString("stats.ACTIVATE_COUNTERS", "");

        jCheckBoxMenuItemStatsAutomaticGenerate.setSelected(AUTOMATIC_GENERATE.equals("true"));
        jCheckBoxMenuItemStatsAutomaticShow.setSelected(AUTOMATIC_SHOW.equals("true"));
        jCheckBoxMenuItemStatsGenerateChartsPictures.setSelected(GENERATE_CHARTS_PICTURES.equals("true"));
        jCheckBoxMenuItemStatsGenerateChartsCsvs.setSelected(GENERATE_CHARTS_CSVS.equals("true"));
        jCheckBoxMenuItemStatsActivateCounters.setSelected(ACTIVATE_COUNTERS.equals("true"));
    }

    private JMenu createJMenuHelp(ActionListener actionListener) {
        JMenu jMenu = new JMenu("Help");
        jMenu.add(createJMenuItem(actionListener, "Documentation", HELP_DOCUMENTATION));
        jMenu.add(createJMenuItem(actionListener, "XML Grammar", XML_GRAMMAR));
        jMenu.add(new javax.swing.JSeparator());
        jMenu.add(createJMenuItem(actionListener, "Web Site", HELP_WEBSITE));
        jMenu.add(createJMenuItem(actionListener, "Google Code", GOOGLE_CODE));
        jMenu.add(new javax.swing.JSeparator());
        jMenu.add(createJMenuItem(actionListener, "About", HELP_ABOUT));
        
        return jMenu;
    }

    /**
     * Builds a class to help menu construction.
     * @param actionListener the listener for all menu actions
     */
    public GUIMenuHelper(ActionListener actionListener) {
        jMenuBar = new JMenuBar();

        jMenuFile = createJMenuFile(actionListener);
        jMenuFile.setEnabled(true);
        jMenuBar.add(jMenuFile);

        jMenuItemReload.setEnabled(false);

        jMenuTest = createJMenuTest(actionListener);
        jMenuTest.setEnabled(false);
        jMenuBar.add(jMenuTest);

        jMenuLog = createJMenuLog(actionListener);
        jMenuLog.setEnabled(true);
        jMenuBar.add(jMenuLog);

        jMenuReport = createJMenuReport(actionListener);
        jMenuReport.setEnabled(true);
        jMenuBar.add(jMenuReport);

        jMenuHelp = createJMenuHelp(actionListener);
        jMenuHelp.setEnabled(true);
        jMenuBar.add(jMenuHelp);

        jMenuFile.revalidate();
    }

    private void jMenuItemClearActionPerformed(java.awt.event.ActionEvent evt) {
        this.recents.clear();
        this.updateRecents();
        this.dumpRecentsToFile();
    }

    private boolean _file = false;
    private boolean _opening = false;
    private boolean _test = false;
    private boolean _testcase = false;
    
    public void updateMenuStatesFile(final boolean file) {
        _file = file;
        updateMenuStates(_file, _opening, _test, _testcase);
    }
    public void updateMenuStatesOpening(final boolean opening) {
        _opening = opening;
        updateMenuStates(_file, _opening, _test, _testcase);
    }
    public void updateMenuStatesTest(final boolean test) {
        _test = test;
        updateMenuStates(_file, _opening, _test, _testcase);
    }
    public void updateMenuStatesTestcase(final boolean testcase) {
        _testcase = testcase;
        updateMenuStates(_file, _opening, _test, _testcase);
    }
    
    
    /* file:     true if a file is opened
     * opening:  true if a test is being opened
     * test:     true if a test is running
     * testcase: true if at least a testcase is running
     */
    public void updateMenuStates(final boolean file, final boolean opening, final boolean test, final boolean testcase) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                jMenuItemOpen.setEnabled(!opening && !test && !testcase);
                jMenuRecents.setEnabled(!opening && !test && !testcase);

                jMenuItemClose.setEnabled(file && !opening && !test && !testcase);
                jMenuItemReload.setEnabled(file && !opening && !test && !testcase);
                jMenuItemTestPlan.setEnabled(file && !opening && !test && !testcase);

                jMenuTest.setEnabled(file);

                jMenuItemRunSequential.setEnabled(file && !opening && !test);
                jMenuItemRunParallel.setEnabled(file && !opening && !test);

                jMenuItemStop.setEnabled(file && !opening && test);
                jMenuItemStopAll.setEnabled(file && !opening && (test || testcase));
                jMenuItemSelectAll.setEnabled(file);
                jMenuItemUnselectAll.setEnabled(file);

                jMenuParameters.setEnabled(file);
                jMenuConf.setEnabled(file);
                TesterGui.instance().getJFrameEditableParameters().setReadOnly(!test && !testcase);
                jMenuOpenTestFile.setEnabled(file);

                jRadioButtonMenuItemDebugLevel.setEnabled(file && !opening);
                jRadioButtonMenuItemInfoLevel.setEnabled(file && !opening);
                jRadioButtonMenuItemWarningLevel.setEnabled(file && !opening);
                jRadioButtonMenuItemErrorLevel.setEnabled(file && !opening);

                jRadioButtonMenuItemLogsModeGui.setEnabled(file && !opening);
                jRadioButtonMenuItemLogsModeFile.setEnabled(file && !opening);
                jRadioButtonMenuItemLogsModeNone.setEnabled(file && !opening);
            }
        });
    }
}
