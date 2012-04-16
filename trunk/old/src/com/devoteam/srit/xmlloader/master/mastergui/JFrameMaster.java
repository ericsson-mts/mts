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
 * NewApplication.java
 *
 * Created on 15 avril 2008, 13:35
 */
package com.devoteam.srit.xmlloader.master.mastergui;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.BetterFileChooser;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.URIFactory;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLDocument;
import com.devoteam.srit.xmlloader.gui.frames.JFrameAbout;
import com.devoteam.srit.xmlloader.gui.frames.JFrameLogsSession;
import com.devoteam.srit.xmlloader.gui.logs.GUITextListenerProvider;
import com.devoteam.srit.xmlloader.master.masterutils.Master;
import com.devoteam.srit.xmlloader.master.masterutils.MasterRunner;
import com.devoteam.srit.xmlloader.master.masterutils.MasterRunnerPreparsing;
import com.devoteam.srit.xmlloader.master.masterutils.MasterRunnerRegistry;
import com.devoteam.srit.xmlloader.master.masterutils.SlavesStatusSingleton;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.LinkedList;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

/**
 *
 * @author  gpasquiers
 */
public class JFrameMaster extends javax.swing.JFrame
{
    // <editor-fold desc="Singleton" defaultstate="collapsed">
    static private JFrameMaster instance = null;

    static public JFrameMaster instance()
    {
        if (null == instance)
        {
            instance = new JFrameMaster();
        }
        
        return instance;
    }
    // </editor-fold>
    
    // <editor-fold desc="Color constants" defaultstate="collapsed">
    
    static public final Color STATE_COLOR_DEFAULT = javax.swing.UIManager.getDefaults().getColor("ProgressBar.foreground");
    static public final Color STATE_COLOR_FAILURE = new Color(204, 000, 000);
    static public final Color STATE_COLOR_SUCCESS = new Color(000, 153, 051);
    static public final Color STATE_COLOR_INTERRUPTED = new Color(255, 153, 000);
    
    // </editor-fold>

    private JFrameLogsSession jFrameMasterLogs;
    private Master masterXMLFile;
    private URI lastMasterFile;

    private JPanelMaster jPanelMaster;
    
    /** Creates new form NewApplication */
    private JFrameMaster()
    {
        initComponents();
        this.jFrameMasterLogs = GUITextListenerProvider.instance().getJFrameLogsApplication();
        this.jPanelMaster = null;
        readRecentsFromFile();
    }

    public JPanelMaster getJPanelMaster()
    {
        return jPanelMaster;
    }

    public void updateRunning()
    {
        boolean somethingIsRunning = false;
        
        for(MasterRunner masterRunner:MasterRunnerRegistry.getRunners())
        {
            if(!masterRunner.finished())
            {
                somethingIsRunning = true;
                break;
            }
        }
        
        if (somethingIsRunning)
        {
            jMenuItemOpen.setEnabled(false);
            jMenuRecents.setEnabled(false);
            jMenuItemReload.setEnabled(false);
            jMenuItemClose.setEnabled(false);
        }
        else
        {
            jMenuItemOpen.setEnabled(true);
            jMenuRecents.setEnabled(true);
            jMenuItemReload.setEnabled(true);
            jMenuItemClose.setEnabled(true);
        }
    }

    public void open(URI file)
    {
        Config.reset();
        addToRecents(file.toString());
        
        try
        {
            XMLDocument xmlDocument = new XMLDocument();
            xmlDocument.setXMLFile(file);
            xmlDocument.setXMLSchema(URIFactory.newURI("../conf/schemas/master.xsd"));
            xmlDocument.parse();
            masterXMLFile = new Master(xmlDocument, URIFactory.resolve(file,"."));
            new MasterRunnerPreparsing(masterXMLFile);
            lastMasterFile = file;
        }
        catch (Exception e)
        {
            Utils.showError(this, "Error while parsing xml file", e);
            masterXMLFile = null;
            lastMasterFile = null;
            return;
        }

        try
        {
            SlavesStatusSingleton.instance().freeAllSlaves();
        }
        catch (Exception e)
        {
            Utils.showError(this, "Could not free all slaves", e);
            masterXMLFile = null;
            lastMasterFile = null;
            return;
        }

        jMenuItemClose.setEnabled(true);
        jMenuItemReload.setEnabled(true);

        GridBagConstraints gridBagConstraintsTest = new GridBagConstraints();
        gridBagConstraintsTest.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraintsTest.gridx = 0;
        gridBagConstraintsTest.gridwidth = 1;
        gridBagConstraintsTest.weightx = 1;

        this.jPanelContainer.removeAll();

        if (null != masterXMLFile)
        {
            this.jPanelMaster = new JPanelMaster(masterXMLFile);
            this.jPanelContainer.add(jPanelMaster, gridBagConstraintsTest);
        }

        GridBagConstraints gridBagConstraintsPadding = new GridBagConstraints();
        gridBagConstraintsPadding.fill = GridBagConstraints.VERTICAL;
        gridBagConstraintsPadding.gridx = 0;
        gridBagConstraintsPadding.gridwidth = 1;
        gridBagConstraintsPadding.weighty = 1;

        JPanel jPanel = new JPanel();
        jPanel.setMinimumSize(new Dimension(0, 0));
        jPanel.setPreferredSize(new Dimension(0, 0));

        this.jPanelContainer.add(jPanel, gridBagConstraintsPadding);

        this.jPanelContainer.revalidate();
    }

    public void close()
    {
        this.masterXMLFile = null;
        this.jPanelContainer.removeAll();
        this.jPanelContainer.revalidate();
        this.jPanelContainer.repaint();
    }
   
    // <editor-fold desc="Recents file menu" defaultstate="collapsed">
    private LinkedList<String> recents = new LinkedList<String>();

    private void addToRecents(String path)
    {
        if (recents.contains(path))
        {
            recents.remove(path);
        }
        recents.addFirst(path);
        this.updateRecents();
    }
    
    private void updateRecents()
    {
        this.jMenuRecents.removeAll();

        if (this.recents.isEmpty())
        {
            this.jMenuRecents.add(this.jMenuItemEmpty);
        }
        else
        {
            for (String path : this.recents)
            {
                javax.swing.JMenuItem jMenuItem = new javax.swing.JMenuItem();
                jMenuItem.addActionListener(new java.awt.event.ActionListener(){

                    public void actionPerformed(java.awt.event.ActionEvent evt)
                    {
                        jMenuItemRecentsItemActionPerformed(evt);
                    }
                });

                jMenuItem.setText(path);

                jMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/text-x-generic.png")));

                this.jMenuRecents.add(jMenuItem);
            }
        }

        this.jMenuRecents.add(this.jSeparator2);
        this.jMenuRecents.add(this.jMenuItemClear);
        this.jMenuRecents.revalidate();

        this.dumpRecentsToFile();
    }
    
    private void dumpRecentsToFile()
    {
        try
        {
            PrintStream printStream = new PrintStream(new FileOutputStream("../conf/master.recents"));//BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("../conf/master.recents"));
            for (String path : this.recents)
            {
                printStream.println(path);
            }
            printStream.close();
        }
        catch (IOException e)
        {
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, "Unable to save recent files");
        }
    }
    
    private void readRecentsFromFile()
    {
        this.recents.clear();
        try
        {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("../conf/master.recents"));
            String line;
            while (null != (line = bufferedReader.readLine()))
            {
                if (line.length() != 0)
                {
                    this.recents.add(line);
                }
            }
            bufferedReader.close();
        }
        catch(FileNotFoundException e)
        {
            // this is not an error
        }
        catch (IOException e)
        {
            Utils.showError(this, "Error in readRecentsFromFile()", e);
        }
        this.updateRecents();
    }
    // </editor-fold>

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane = new javax.swing.JScrollPane();
        jPanelContainer = new javax.swing.JPanel();
        menuBar = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuItemOpen = new javax.swing.JMenuItem();
        jMenuRecents = new javax.swing.JMenu();
        jMenuItemEmpty = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        jMenuItemClear = new javax.swing.JMenuItem();
        jMenuItemReload = new javax.swing.JMenuItem();
        jMenuItemClose = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuItemExit = new javax.swing.JMenuItem();
        jMenuWindows = new javax.swing.JMenu();
        jMenuItemMasterLogs = new javax.swing.JMenuItem();
        jMenuItemTasks = new javax.swing.JMenuItem();
        jMenuHelp = new javax.swing.JMenu();
        jMenuItemAbout = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("IMSLoader - MASTER");
        setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/applications-system.png")));
        setLocationByPlatform(true);

        jScrollPane.setBorder(null);

        jPanelContainer.setMinimumSize(new java.awt.Dimension(600, 0));
        jPanelContainer.setLayout(new java.awt.GridBagLayout());
        jScrollPane.setViewportView(jPanelContainer);

        jMenuFile.setText("File");

        jMenuItemOpen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/document-open.png"))); // NOI18N
        jMenuItemOpen.setText("Open...");
        jMenuItemOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemOpenActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemOpen);

        jMenuRecents.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/document-open.png"))); // NOI18N
        jMenuRecents.setText("Open");

        jMenuItemEmpty.setText("...");
        jMenuItemEmpty.setEnabled(false);
        jMenuRecents.add(jMenuItemEmpty);
        jMenuRecents.add(jSeparator2);

        jMenuItemClear.setText("Clear history");
        jMenuItemClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemClearActionPerformed(evt);
            }
        });
        jMenuRecents.add(jMenuItemClear);

        jMenuFile.add(jMenuRecents);

        jMenuItemReload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/view-refresh.png"))); // NOI18N
        jMenuItemReload.setText("Reload");
        jMenuItemReload.setEnabled(false);
        jMenuItemReload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemReloadActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemReload);

        jMenuItemClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit-clear.png"))); // NOI18N
        jMenuItemClose.setText("Close");
        jMenuItemClose.setEnabled(false);
        jMenuItemClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCloseActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemClose);
        jMenuFile.add(jSeparator1);

        jMenuItemExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/system-log-out.png"))); // NOI18N
        jMenuItemExit.setText("Exit");
        jMenuItemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExitActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemExit);

        menuBar.add(jMenuFile);

        jMenuWindows.setText("Windows");

        jMenuItemMasterLogs.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/internet-group-chat.png"))); // NOI18N
        jMenuItemMasterLogs.setText("Logs");
        jMenuItemMasterLogs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemMasterLogsActionPerformed(evt);
            }
        });
        jMenuWindows.add(jMenuItemMasterLogs);

        jMenuItemTasks.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/applications-system.png"))); // NOI18N
        jMenuItemTasks.setText("Tasks");
        jMenuItemTasks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemTasksActionPerformed(evt);
            }
        });
        jMenuWindows.add(jMenuItemTasks);

        menuBar.add(jMenuWindows);

        jMenuHelp.setText("Help");

        jMenuItemAbout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help-browser.png"))); // NOI18N
        jMenuItemAbout.setText("About");
        jMenuItemAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAboutActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuItemAbout);

        menuBar.add(jMenuHelp);

        setJMenuBar(menuBar);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 716, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExitActionPerformed
        System.exit(0);
}//GEN-LAST:event_jMenuItemExitActionPerformed

private void jMenuItemMasterLogsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemMasterLogsActionPerformed
    this.jFrameMasterLogs.setVisible(!this.jFrameMasterLogs.isVisible());
}//GEN-LAST:event_jMenuItemMasterLogsActionPerformed

private void jMenuItemOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemOpenActionPerformed
    //Handle open button action.
    
    if (null != this.masterXMLFile)
    {
        this.close();
    }
    
    JFileChooser fc = new BetterFileChooser();
    fc.setCurrentDirectory(new File(".."));
    int returnVal = fc.showOpenDialog(this);

    if (returnVal != JFileChooser.APPROVE_OPTION)
    {
        return;
    }
    
    this.open(fc.getSelectedFile().toURI());
}//GEN-LAST:event_jMenuItemOpenActionPerformed

private void jMenuItemCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCloseActionPerformed
    this.close();
    this.jMenuItemClose.setEnabled(false);
    this.jMenuItemReload.setEnabled(false);
}//GEN-LAST:event_jMenuItemCloseActionPerformed

private void jMenuItemReloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemReloadActionPerformed
    this.close();
    this.open(this.lastMasterFile);
}//GEN-LAST:event_jMenuItemReloadActionPerformed

private void jMenuItemClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemClearActionPerformed
    this.recents.clear();
    this.updateRecents();
    this.dumpRecentsToFile();
}//GEN-LAST:event_jMenuItemClearActionPerformed

private void jMenuItemAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAboutActionPerformed
    new JFrameAbout().setVisible(true);
}//GEN-LAST:event_jMenuItemAboutActionPerformed

private void jMenuItemTasksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemTasksActionPerformed


    JFrameTasks.instance().setVisible(!JFrameTasks.instance().isVisible());//GEN-LAST:event_jMenuItemTasksActionPerformed

}

private void jMenuItemRecentsItemActionPerformed(java.awt.event.ActionEvent evt) {
    this.open(URIFactory.create(((javax.swing.JMenuItem)evt.getSource()).getText()));
}


    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenu jMenuHelp;
    private javax.swing.JMenuItem jMenuItemAbout;
    private javax.swing.JMenuItem jMenuItemClear;
    private javax.swing.JMenuItem jMenuItemClose;
    private javax.swing.JMenuItem jMenuItemEmpty;
    private javax.swing.JMenuItem jMenuItemExit;
    private javax.swing.JMenuItem jMenuItemMasterLogs;
    private javax.swing.JMenuItem jMenuItemOpen;
    private javax.swing.JMenuItem jMenuItemReload;
    private javax.swing.JMenuItem jMenuItemTasks;
    private javax.swing.JMenu jMenuRecents;
    private javax.swing.JMenu jMenuWindows;
    private javax.swing.JPanel jPanelContainer;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JMenuBar menuBar;
    // End of variables declaration//GEN-END:variables
    
}
