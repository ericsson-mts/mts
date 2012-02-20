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
 * Created on Oct 11, 2004
 */
package com.devoteam.srit.xmlloader.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Cursor;
import java.io.File;
import java.io.PrintStream;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.devoteam.srit.xmlloader.core.Test;
import com.devoteam.srit.xmlloader.core.TestRunnerSingle;
import com.devoteam.srit.xmlloader.core.Testcase;
import com.devoteam.srit.xmlloader.core.Tester;
import com.devoteam.srit.xmlloader.core.licence.Licence;
import com.devoteam.srit.xmlloader.core.licence.UtilsLicence;
import com.devoteam.srit.xmlloader.core.log.FileTextListenerProvider;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.log.TextListenerProviderRegistry;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.BetterFileChooser;
import com.devoteam.srit.xmlloader.core.utils.JDialogError;
import com.devoteam.srit.xmlloader.core.utils.URIRegistry;
import com.devoteam.srit.xmlloader.core.utils.filesystem.LocalFSInterface;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import com.devoteam.srit.xmlloader.gui.components.ComponentRenderer;
import com.devoteam.srit.xmlloader.gui.components.MouseOverJTable;
import com.devoteam.srit.xmlloader.gui.frames.JFrameEditableParameters;
import com.devoteam.srit.xmlloader.gui.logs.GUITextListenerProvider;
import com.devoteam.srit.xmlloader.gui.conf.JFrameConf;

import com.devoteam.srit.xmlloader.gui.wrappers.WrapperTest;
import com.devoteam.srit.xmlloader.gui.wrappers.WrapperTestcase;
import java.awt.Color;
import java.awt.Font;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javolution.util.FastList;

/**
 * @author pn007888 A class to implement Tester class GUI.
 */
public class TesterGui {

    private final static String DEFAULT_TITLE = "IMSLoader";
    private final static String[] NAMES = {"Testcase", "N.", "Run", "%", "Logs", "Status", "Action", "Profile"};
    private static TesterGui instance = null;
    private Tester tester;
    private JTable jTableTestcases;
    private JScrollPane jTableTestcasesScrollPane;
    private DefaultTableModel defaultTableModel;
    private GUIMenuHelper guiMenuHelper;
    private TesterGuiHelper testerGuiHelper;
    private JFrameEditableParameters jFrameEditableParameters;
    private JFrame jFrame;
    private File openFileDirectory;

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
        testerGuiHelper = new TesterGuiHelper(this);
        guiMenuHelper = new GUIMenuHelper(testerGuiHelper);

    }

    /**
     * Create and set up the window.
     */
    public void realize() {
        this.jTableTestcasesScrollPane = new JScrollPane(setUpTable());
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.getContentPane().add(guiMenuHelper.getJMenuBar(), BorderLayout.NORTH);
        jFrame.getContentPane().add(jTableTestcasesScrollPane, BorderLayout.CENTER);
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

    private JTable setUpTable() {
        defaultTableModel = new DefaultTableModel(NAMES, 0);
        jTableTestcases = new MouseOverJTable(defaultTableModel);
        jTableTestcases.setRowHeight(25);
        for (int i = 0; i < jTableTestcases.getColumnCount(); i++) {
            TableColumn column = jTableTestcases.getColumnModel().getColumn(i);

            column.setCellRenderer(new ComponentRenderer());
            column.setCellEditor(new ComponentRenderer());

            switch (i) {
                // testcase name + active checkbox
                case 0:
                    column.setPreferredWidth(150);
                    break;
                // spinner    
                case 1:
                    column.setMaxWidth(60);
                    column.setMinWidth(60);
                    column.setPreferredWidth(60);
                    break;
                // run button
                case 2:
                    column.setMaxWidth(80);
                    column.setMinWidth(80);
                    column.setPreferredWidth(80);
                    break;
                // progress bar
                case 3:
                    column.setMaxWidth(100);
                    column.setMinWidth(100);
                    column.setPreferredWidth(100);
                    break;
                // logs checkbox
                case 4:
                    column.setMaxWidth(60);
                    column.setMinWidth(60);
                    column.setPreferredWidth(60);
                    break;
                // status icon
                case 5:
                    column.setMaxWidth(40);
                    column.setMinWidth(40);
                    column.setPreferredWidth(40);
                    break;
                // show button
                case 6:
                    column.setMaxWidth(80);
                    column.setMinWidth(80);
                    column.setPreferredWidth(80);
                    break;
                case 7:
                    column.setMaxWidth(80);
                    column.setMinWidth(80);
                    column.setPreferredWidth(80);
                    break;
            }
        }
        jTableTestcases.setPreferredScrollableViewportSize(new Dimension(800, 480));
        return jTableTestcases;
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
                jFileChooser.setCurrentDirectory(new File(URIRegistry.IMSLOADER_BIN.resolve("..")));
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
        else {
            guiMenuHelper.updateMenuStates(false, false, false, false);
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
        this.jFrame.setTitle("IMSLoader : " + tester.getTest().getXMLDocument().getXMLFile());
        guiMenuHelper.updateLogStorageFromConfig();
        guiMenuHelper.updateLogLevelFromConfig();
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

        testcaseToWrapper.clear();
        wrapperTestcases.clear();

        try {
            if (null != this.jTableTestcases.getCellEditor()) {
                this.jTableTestcases.getCellEditor().cancelCellEditing();
            }
        }
        catch (Exception e) {/* ignore */

        }


        defaultTableModel.setRowCount(0);

        guiMenuHelper.updateMenuStates(false, false, false, false);
    }

    protected void selected(final boolean selected) {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                for (WrapperTestcase wrapperTestcase : wrapperTestcases) {
                    if (null == wrapperTestcase.getParent() || !wrapperTestcase.getParent().getRunning()) {
                        wrapperTestcase.setActive(selected);
                    }
                }
            }
        });
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
        //
        // sets the default font for all Swing components.
        //
        java.util.Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, new javax.swing.plaf.FontUIResource("Sans", Font.PLAIN, 12));
            }
        }

        /*
         * Set the FSInterface to LocalFS.
         */
        SingletonFSInterface.setInstance(new LocalFSInterface());

        /* Remove the licence control
        try {
            Licence.instance().isComplete();
        }
        catch (Exception ex) {
            UtilsLicence.logMessage("EXCEPTION TesterGui isComplete() : ", ex);
            displayErrorLicencePanel();
            System.exit(-1);
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date currentDate = new Date();
            Date licenceDate = sdf.parse(Licence.instance().getDate());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(licenceDate);

            if ((Licence.instance().getValidity().length() != 0)
                    && (Integer.parseInt(Licence.instance().getValidity()) != 0))//for non infinite period
            {
                calendar.add(Calendar.DAY_OF_YEAR, Integer.parseInt(Licence.instance().getValidity()));
                Date licenceDateExpiration = calendar.getTime();

                if (currentDate.after(licenceDateExpiration)) {
                    UtilsLicence.logMessage("EXCEPTION Your licence has expired : ", new Exception());
                    JDialogError dialog = new JDialogError((JFrame) null, true);
                    dialog.setTitle("Licence error");
                    dialog.setMessage("Your licence has expired");
                    dialog.setDetails(Licence.getExpiredLicenceMessage());
                    dialog.setVisible(true);
                    System.exit(-1);
                }
            }
        }
        catch (Exception ex) {
            UtilsLicence.logMessage("EXCEPTION : checklicense : ", ex);
            displayErrorLicencePanel();
            System.exit(-1);
        }

        if (Licence.instance().isTrial()) {
            JOptionPane.showMessageDialog(null, Licence.getTrialMessage(),
                    "Trail version", JOptionPane.WARNING_MESSAGE);
        }

        if (!Licence.instance().versionMatches()) {
            UtilsLicence.logMessage("EXCEPTION Your Licence does not support the current version of IMSloader : ", new Exception());
            JDialogError dialog = new JDialogError((JFrame) null, true);
            dialog.setTitle("Licence version error");
            dialog.setMessage("Your Licence does not support the current version of IMSloader");
            dialog.setDetails(Licence.getBadVersionLicenceMessage());
            dialog.setVisible(true);
            System.exit(-1);
        }
		*/
		
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

    private static void displayErrorLicencePanel() {
        String computerID = "?";
        try {
            computerID = UtilsLicence.getNewComputeID();
        }
        catch (Exception ee) {
        }

        JDialogError dialog = new JDialogError((JFrame) null, true);
        dialog.setTitle("Licence error");
        dialog.setMessage("Invalid licence file");
        dialog.setDetails(Licence.getProcedure(computerID));
        dialog.setVisible(true);
        System.exit(-1);
    }

    public JFrameEditableParameters getJFrameEditableParameters() {
        return jFrameEditableParameters;
    }

    public void showJFrameConf() {
        new JFrameConf(this.jFrame, true).setVisible(true);
    }

    // Handle JTable
    public void fireTableRowsUpdated(int firstRow, int lastRow) {
        this.defaultTableModel.fireTableRowsUpdated(firstRow, lastRow);
    }
    // <editor-fold desc="New TestcaseGui">
    private LinkedHashMap<Testcase, WrapperTestcase> testcaseToWrapper = new LinkedHashMap<Testcase, WrapperTestcase>();
    private FastList<WrapperTestcase> wrapperTestcases = new FastList<WrapperTestcase>();

    private void initialize(Test test) {
        testcaseToWrapper.clear();
        wrapperTestcases.clear();

        try {
            if (null != this.jTableTestcases.getCellEditor()) {
                this.jTableTestcases.getCellEditor().cancelCellEditing();
            }
        }
        catch (Exception e) {
            //ignore
        }

        defaultTableModel.setRowCount(0);

        int i = 0;
        for (Testcase testcase : test.getTestcaseList()) {
            WrapperTestcase wrapperTestcase = new WrapperTestcase(testcase, i++);
            testcaseToWrapper.put(testcase, wrapperTestcase);
            wrapperTestcases.add(wrapperTestcase);
            defaultTableModel.addRow(wrapperTestcase.getComponents());
        }
    }
    private FastList<WrapperTest> wrapperTests = new FastList<WrapperTest>();

    synchronized public void startTestcase(Testcase testcase) {
        WrapperTest wrapperTest = new WrapperTest(Tester.getInstance().getTest(), testcaseToWrapper.get(testcase));
        wrapperTest.startSingle();
    }

    synchronized public void startTestcasesLoad() {
        LinkedList<WrapperTestcase> wrapperTestcasesToStart = new LinkedList<WrapperTestcase>();

        for (WrapperTestcase wrapperTestcase : wrapperTestcases) {
            if (wrapperTestcase.getActive()) {
                wrapperTestcasesToStart.add(wrapperTestcase);
            }
        }

        WrapperTest wrapperTest = new WrapperTest(Tester.getInstance().getTest(), wrapperTestcasesToStart);
        wrapperTest.startLoad();
    }

    synchronized public void startTestcasesSequential() {
        LinkedList<WrapperTestcase> wrapperTestcasesToStart = new LinkedList<WrapperTestcase>();

        for (WrapperTestcase wrapperTestcase : wrapperTestcases) {
            if (wrapperTestcase.getActive()) {
                wrapperTestcasesToStart.add(wrapperTestcase);
            }
        }

        WrapperTest wrapperTest = new WrapperTest(Tester.getInstance().getTest(), wrapperTestcasesToStart);
        wrapperTest.startSequential();
    }

    synchronized public void startWrapperTest(WrapperTest wrapperTest) {
        if(!wrapperTests.contains(wrapperTest)){
            this.wrapperTests.add(wrapperTest);
        }
    }

    synchronized public void stopWrapperTest(WrapperTest wrapperTest) {
        this.wrapperTests.remove(wrapperTest);
    }

    synchronized public void updateMenuState(boolean opening) {
        boolean testRunning = false;
        boolean testcaseRunning = false;
        for (WrapperTest wrapperTest : wrapperTests) {
            if (wrapperTest.getRunning()) {
                if (wrapperTest.getRunner() instanceof TestRunnerSingle) {
                    testcaseRunning = true;
                }
                else {
                    testRunning = true;
                }
            }
        }
        this.guiMenuHelper.updateMenuStates(true, opening, testRunning, testcaseRunning);
    }

    public void stopAll() {
        for (WrapperTest wrapperTest : wrapperTests) {
            try {
                wrapperTest.getRunner().stop();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void stopRun() {
        for (WrapperTest wrapperTest : wrapperTests) {
            if (wrapperTest.getRunning()) {
                if (wrapperTest.getTestcaseNumber() > 1) {
                    try {
                        wrapperTest.getRunner().stop();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
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
            jFileChooser.setCurrentDirectory(new File(URIRegistry.IMSLOADER_BIN.resolve("..")));
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
