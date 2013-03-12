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

import com.devoteam.srit.xmlloader.core.*;
import com.devoteam.srit.xmlloader.core.log.FileTextListenerProvider;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.log.TextListenerProviderRegistry;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.BetterFileChooser;
import com.devoteam.srit.xmlloader.core.utils.URIRegistry;
import com.devoteam.srit.xmlloader.core.utils.filesystem.LocalFSInterface;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import com.devoteam.srit.xmlloader.gui.better.RegistryTestRunner;
import com.devoteam.srit.xmlloader.gui.better.TestPanelCtrl;
import com.devoteam.srit.xmlloader.gui.better.TestPanelView;
import com.devoteam.srit.xmlloader.gui.conf.JFrameConf;
import com.devoteam.srit.xmlloader.gui.frames.JFrameEditableParameters;
import com.devoteam.srit.xmlloader.gui.logs.GUITextListenerProvider;
import java.awt.*;
import java.io.File;
import java.io.PrintStream;
import java.net.URI;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

/**
 * @author pn007888 A class to implement Tester class GUI.
 */
public class TesterGui {

    private final static String DEFAULT_TITLE = "MTS";
    private static TesterGui instance = null;
    private Tester tester;
    private JScrollPane _jScrollPane;
    private GUIMenuHelper guiMenuHelper;
    private TesterGuiHelper testerGuiHelper;
    private JFrameEditableParameters jFrameEditableParameters;
    private JFrame jFrame;
    private File openFileDirectory;
    private TestPanelCtrl _testPanelCtrl;
    
    static public TesterGui instance() {
        if (instance == null) {
            instance = new TesterGui(Tester.getInstance());
        }

        return instance;
    }

    static public boolean instanciated() {
        return instance != null;
    }

    /**
     * private constructor
     * @param t the concerned tester
     */
    private TesterGui(Tester aTester) {
        this.jFrameEditableParameters = new JFrameEditableParameters(this.jFrame, true);

        tester = aTester;
        jFrame = new JFrame(DEFAULT_TITLE);
        jFrame.setLocationByPlatform(true);
        jFrame.setPreferredSize(new Dimension(800, 540));
        jFrame.setSize(new Dimension(800, 540));
        jFrame.setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icon.png")));
        testerGuiHelper = new TesterGuiHelper(this);
        guiMenuHelper = new GUIMenuHelper(testerGuiHelper);
    }

    /**
     * Create and set up the window.
     */
    public void realize() {
        _jScrollPane = new JScrollPane();
        _jScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        jFrame.setJMenuBar(guiMenuHelper.getJMenuBar());
        jFrame.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(4, 4, 4, 4);
        constraints.weightx = 1;
        constraints.weighty = 1;
        
        jFrame.getContentPane().add(_jScrollPane, constraints);
        //jFrame.getContentPane().add(getButtonPanel(), BorderLayout.EAST);

        jFrame.pack();
        jFrame.setVisible(true);

        guiMenuHelper.updateMenuStates(false, false, false, false);
    }

    public Tester getTester() {
        return this.tester;
    }

    public GUIMenuHelper getGUIMenuHelper() {
        return this.guiMenuHelper;
    }

    /**
     * 'Open file' selected.
     */
    public void open(URI filename) {
        if (filename == null) {

            JFileChooser jFileChooser = new BetterFileChooser();

            // Add a filter to select only "testcases.xml files
            jFileChooser.addChoosableFileFilter(new FileFilter() {

                public boolean accept(File file) {
                    return file.getName().endsWith(".xml") || file.isDirectory();
                }

                public String getDescription() {
                    return "'.xml' files";
                }
            });

            if (openFileDirectory != null) {
                jFileChooser.setSelectedFile(openFileDirectory);
            }
            else {
                jFileChooser.setCurrentDirectory(new File(URIRegistry.MTS_BIN_HOME.resolve("..")));
            }

            int ret = jFileChooser.showOpenDialog(null);

            if (ret == JFileChooser.APPROVE_OPTION) {
                filename = jFileChooser.getSelectedFile().toURI();
                openFileDirectory = new File(filename);
            }
            else {
                filename = null;
            }
        }
        
        if (null != filename) {
            try {
                close_closeGui();
                close_closeFile();

                this.guiMenuHelper.addToRecents(filename.toString());
                
                this.getJFrameEditableParameters().clear();
                
                this.open_openFile(filename);
                this.open_openGui();

                this.getJFrameEditableParameters().fill(tester.getTest().getEditableParameters());
                guiMenuHelper.updateMenuStates(true, false, false, false);
            }
            catch (Exception e) {
                e.printStackTrace();
                GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, e, "Unable to open file ", filename);
                JOptionPane.showMessageDialog(null, "Unable to open file " + filename + "\n" + e, "Erreur", JOptionPane.ERROR_MESSAGE);
                guiMenuHelper.updateMenuStates(false, false, false, false);
            }
        }
    }

    public void reload(boolean resetEditableParameters) {
        URI filename = tester.getTest().getXMLDocument().getXMLFile();

        this.close_closeGui();
        this.close_closeFile();

        if (resetEditableParameters) {
            this.getJFrameEditableParameters().clear();
        }

        try {
            this.open_openFile(filename);
            this.open_openGui();

            if (resetEditableParameters) {
                this.getJFrameEditableParameters().fill(tester.getTest().getEditableParameters());
            }

            guiMenuHelper.updateMenuStates(true, false, false, false);
        }
        catch (Exception e) {

            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, e, "Unable to open file ", filename);
            JOptionPane.showMessageDialog(null, "Unable to open file " + filename + "\n" + e, "Erreur", JOptionPane.ERROR_MESSAGE);
            guiMenuHelper.updateMenuStates(false, false, false, false);
        }
    }

    public void open_openFile(URI path) throws Exception {
        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "Opening test ", path);

        tester.open_reset();
        tester.open_openFile(path, this.getJFrameEditableParameters());

        StatPool.getInstance().reset();
    }

    public void open_openGui() throws Exception {
        this.initialize(Tester.getInstance().getTest());
        this.jFrame.setTitle("M.T.S. : " + tester.getTest().getXMLDocument().getXMLFile());
        guiMenuHelper.updateLogStorageFromConfig();
        guiMenuHelper.updateLogLevelFromConfig();
        guiMenuHelper.updateMenuStatesFile(true);
    }

    /**
     * Close the testcase file
     */
    public void close_closeFile() {
        tester.close();

        // reset all the protocol stacks using the factory
        StackFactory.reset();
    }

    public void close_closeGui() {
        jFrame.setTitle(DEFAULT_TITLE);
        _jScrollPane.setViewportView(null);
        guiMenuHelper.updateMenuStatesFile(false);
    }

    protected void selected(final boolean selected) {
        _testPanelCtrl.setAllTestLineSelected(selected);
    }

    /**
     * Main entry
     * @param args the arguments
     */
    public static void main(String[] args) {
        //
        // redirect the output in a file
        //
        try {
            File file = new File("../logs/stdout.log");
            PrintStream print = new PrintStream(file);
            System.setOut(print);
            System.setErr(print);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        /*
         * Set the FSInterface to LocalFS.
         */
        SingletonFSInterface.setInstance(new LocalFSInterface());

        //
        // Some SWING ToolTip configuration
        //
        ToolTipManager.sharedInstance().setDismissDelay(60000);
        UIManager.put("ToolTip.background", new Color(184, 207, 229));
        UIManager.put("ToolTip.foreground", new Color(51, 51, 51));

        /*
         * Register the GUI logger provider
         */
        TextListenerProviderRegistry.instance().register(GUITextListenerProvider.instance());

        /*
         * Register the File logger provider
         */
        TextListenerProviderRegistry.instance().register(new FileTextListenerProvider());

        //
        // Instanciate the tester
        //
        try {
            Tester.buildInstance();
            Tester.mode = "GUI";

            TesterGui.instance().realize();
            String openFileName = null;
            // the user specifies as parameter on the command-line the file to open
            if (args.length > 0) {
                openFileName = args[0];
            }
            // by default we open the first file in the recents file
            else {
                GUIMenuHelper.readRecentsFromFile();
                openFileName = GUIMenuHelper.getFirstRecent();
            }
            // opens the corresponding file
            if (openFileName != null) {
                if (openFileName.startsWith("file:")) {
                    TesterGui.instance().open(new URI(openFileName));
                }
                else {
                    File fileName = new File(openFileName);
                    TesterGui.instance().open(fileName.toURI());
                }
            }

            //
            // Initialize the Statistics to automatically generate periodically statistics report
            //
            StatPool.initialize("standalone");

            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "Debug level enabled");
            GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "Info level enabled");
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.CORE, "Warn level enabled");
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, "Error level enabled");
        }
        catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public JFrameEditableParameters getJFrameEditableParameters() {
        return jFrameEditableParameters;
    }

    public void showJFrameConf() {
        new JFrameConf(this.jFrame, true).setVisible(true);
    }

    // <editor-fold desc="New TestcaseGui">
    private void initialize(Test test) {
        TestPanelView testPanelView = new TestPanelView();
        _testPanelCtrl = new TestPanelCtrl(testPanelView, test);
        testPanelView.revalidate();
        _jScrollPane.setViewportView(testPanelView);
        _jScrollPane.revalidate();
        testPanelView.revalidate();

    }

    synchronized public void startTestcasesLoad() {
        try {
            TestRunnerLoad testRunnerLoad = new TestRunnerLoad(Tester.getInstance().getTest());
            RegistryTestRunner.getInstance().registerTestRunner(testRunnerLoad);
            testRunnerLoad.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    synchronized public void startTestcasesSequential() {
        try {
            TestRunnerSequential testRunnerSequential = new TestRunnerSequential(Tester.getInstance().getTest());
            RegistryTestRunner.getInstance().registerTestRunner(testRunnerSequential);
            testRunnerSequential.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopAll() {
        for(TestRunner testRunner : RegistryTestRunner.getInstance().getActiveTestRunners()){
            testRunner.stop();
        }
    }

    public void stopRun() {
        for(TestRunner testRunner : RegistryTestRunner.getInstance().getActiveTestRunners()){
            if(testRunner instanceof TestRunnerLoad || testRunner instanceof TestRunnerSequential){
                testRunner.stop();
            }
        }
    }

    public void setCursor(boolean wait) {
        if (wait) {
            jFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
        else {
            jFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }
    // </editor-fold>

    /**
     * 'Open file' selected.
     */
    public URI openDirectory() {
        JFileChooser jFileChooser = new BetterFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jFileChooser.addChoosableFileFilter(new FileFilter() {

            public boolean accept(File file) {
                return file.isDirectory();
            }

            public String getDescription() {
                return "Directory";
            }
        });

        if (openFileDirectory != null) {
            jFileChooser.setSelectedFile(openFileDirectory);
        }
        else {
            jFileChooser.setCurrentDirectory(new File(URIRegistry.MTS_BIN_HOME.resolve("..")));
        }

        int ret = jFileChooser.showOpenDialog(null);

        if (ret == JFileChooser.APPROVE_OPTION) {
            return jFileChooser.getSelectedFile().toURI();
            //openFileDirectory = new File(filename);
        }
        else {
            return null;
            // filename = null;
        }
    }
}
