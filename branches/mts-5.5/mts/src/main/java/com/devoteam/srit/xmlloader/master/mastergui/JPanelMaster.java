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
 * JPanelTest.java
 *
 * Created on 16 avril 2008, 19:26
 */
package com.devoteam.srit.xmlloader.master.mastergui;

import com.devoteam.srit.xmlloader.core.RunnerState;
import com.devoteam.srit.xmlloader.core.RunnerState.State;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import com.devoteam.srit.xmlloader.master.masterutils.Master;
import com.devoteam.srit.xmlloader.master.masterutils.TestData;
import com.devoteam.srit.xmlloader.core.report.ReportStatus;
import com.devoteam.srit.xmlloader.core.report.ReportGenerator;
import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.gui.frames.JPanelReporting;

import com.devoteam.srit.xmlloader.master.masterutils.MasterRunner;
import com.devoteam.srit.xmlloader.master.masterutils.MasterRunnerDeployer;
import com.devoteam.srit.xmlloader.master.masterutils.MasterRunnerRegistry;
import com.devoteam.srit.xmlloader.master.masterutils.MasterRunnerSequential;
import com.devoteam.srit.xmlloader.master.masterutils.MasterRunnerSimultaneous;
import java.awt.Color;
import java.lang.String;
import java.util.LinkedList;

/**
 *
 * @author  gege
 */
public class JPanelMaster extends javax.swing.JPanel implements NotificationListener<Notification<String, RunnerState>>
{
    final JPanelMaster _this = this;
    
    private MasterRunner masterRunner;
    
    public void setMasterRunner(MasterRunner masterRunner) throws Exception
    {
        if(null == masterRunner) return;
        if(masterRunner == this.masterRunner) return;
        
        if(null != this.masterRunner)
        {
            this.masterRunner.removeListener(this);
        }

        for(JPanelTest jPanelTest:this.jPanelTests)
        {
            MasterRunner runner = jPanelTest.getMasterRunner();
            if(null == runner || (null != runner && runner.finished()))
            {
                jPanelTest.setMasterRunner(masterRunner);
            }
            else
            {
                throw new RuntimeException("this case should not happen runner=" + runner );
            }
        }
        
        this.masterRunner = masterRunner;


        
        if(null != this.masterRunner)
        {
            this.masterRunner.addListener(this);
        }
    }

    public MasterRunner getMasterRunner()
    {
        return this.masterRunner;
    }
    
    private Master master;
    private LinkedList<JPanelTest> jPanelTests;

    private boolean isRunning;
    private boolean isDeploying;
            
    /** Creates new form JPanelTest */
    public JPanelMaster(Master masterXMLFile)
    {
        this.master = masterXMLFile;
        this.jPanelTests = new LinkedList<JPanelTest>();
        initComponents();

        this.jLabel1.setText(this.master.attributeValue("name"));
        this.jLabel1.setToolTipText(this.master.attributeValue("description"));
    }

    public Master getMaster()
    {
        return this.master;
    }
    
    public void setBackgroundColor(Color color)
    {
        this.setBackground(color);
        this.jPanel4.setBackground(color);
        this.jPanelPadding.setBackground(color);
        this.jPanelContainer.setBackground(color);
    }

    private void setIsRunning(boolean isRunning)
    {
        if(isRunning && this.isDeploying) throw new RuntimeException("Cannot be running while still deploying");
        
        if (isRunning != this.isRunning)
        {
            this.isRunning = isRunning;

            if (isRunning)
            {
                this.jButtonRun.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/process-stop.png"))); // NOI18N
                this.jButtonDeploy.setEnabled(false);
                this.jComboBoxRunner.setEnabled(false);
            }
            else
            {
                this.jButtonRun.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/media-playback-start.png"))); // NOI18N
                this.jButtonDeploy.setEnabled(true);
                this.jComboBoxRunner.setEnabled(true);
            }
        }
    }

    public boolean isRunning()
    {
        return this.isRunning;
    }
    
    private void setIsDeploying(boolean isDeploying)
    {
        if(isDeploying && this.isRunning) throw new RuntimeException("Cannot be deploying while running");

        if(isDeploying != this.isDeploying)
        {
            this.isDeploying = isDeploying;

            this.jButtonRun.setEnabled(!this.isDeploying);
            this.jButtonDeploy.setEnabled(!this.isDeploying);
        }
    }

    public boolean isDeploying()
    {
        return this.isDeploying;
    }
    
    synchronized public void notificationReceived(Notification<String, RunnerState> notification)
    {
        // we should only receive notifications from a TestRunner !
        RunnerState runnerState = notification.getData();

        // handle progression
        int currentProgression = this.jProgressBar.getValue();
        this.jProgressBar.setValue(runnerState.getProgression());
        this.jProgressBar.setToolTipText(runnerState.toPopupHTMLString());

        // handle state display
        State state = runnerState.getState();
        jProgressBar.setString(runnerState.getState().toString().toLowerCase());

        // handle state color
        switch (state)
        {
            case UNINITIALIZED:
            case OPENING:
            case RUNNING:
                this.jProgressBar.setForeground(JFrameMaster.STATE_COLOR_DEFAULT);
                break;
            case OPEN_FAILED:
            case FAILED:
            case FAILING:
                this.jProgressBar.setForeground(JFrameMaster.STATE_COLOR_FAILURE);
                break;
            case INTERRUPTED:
            case INTERRUPTING:
                this.jProgressBar.setForeground(JFrameMaster.STATE_COLOR_INTERRUPTED);
                break;
            case OPEN_SUCCEEDED:
            case SUCCEEDED:
                this.jProgressBar.setForeground(JFrameMaster.STATE_COLOR_SUCCESS);
                break;
        }
        
        // handle the running state
        switch (state)
        {
            case UNINITIALIZED:
                this.setIsDeploying(false);
                this.setIsRunning(false);
                break;
            case OPENING:
                this.setIsRunning(false);
                this.setIsDeploying(true);
                break;
            case RUNNING:
            case FAILING:
            case INTERRUPTING:
                this.setIsDeploying(false);
                this.setIsRunning(true);
                break;
            case OPEN_SUCCEEDED:
            case OPEN_FAILED:
                this.setIsDeploying(false);
                this.setIsRunning(false);
                break;
            case INTERRUPTED:
            case FAILED:
            case SUCCEEDED:
                try
                {
                    this.setMasterRunner(null);
                }
                catch(Exception e)
                {
                    // should not happen
                }
                
                this.setIsDeploying(false);
                this.setIsRunning(false);
                break;
        }
        
        // handle the running state
        switch (state)
        {
            case OPEN_SUCCEEDED:
            case OPEN_FAILED:
                if(this.getMasterRunner() instanceof MasterRunnerDeployer)
                {
                    try
                    {
                        this.setMasterRunner(null);
                    }
                    catch(Exception e)
                    {
                        // should not happen
                    }
                }
                break;
        }
    }

    /** 
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel4 = new javax.swing.JPanel();
        jToggleButtonExpand = new javax.swing.JToggleButton();
        jButtonRun = new javax.swing.JButton();
        jProgressBar = new javax.swing.JProgressBar();
        jButtonXML = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jButtonReport = new javax.swing.JButton();
        jButtonDeploy = new javax.swing.JButton();
        jComboBoxRunner = new javax.swing.JComboBox();
        jPanelPadding = new javax.swing.JPanel();
        jPanelContainer = new javax.swing.JPanel();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        jPanel4.setMaximumSize(new java.awt.Dimension(2147483647, 24));
        jPanel4.setMinimumSize(new java.awt.Dimension(438, 24));
        jPanel4.setPreferredSize(new java.awt.Dimension(600, 24));
        jPanel4.setLayout(new java.awt.GridBagLayout());

        jToggleButtonExpand.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/list-add.png"))); // NOI18N
        jToggleButtonExpand.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jToggleButtonExpand.setMaximumSize(new java.awt.Dimension(20, 20));
        jToggleButtonExpand.setMinimumSize(new java.awt.Dimension(20, 20));
        jToggleButtonExpand.setPreferredSize(new java.awt.Dimension(20, 20));
        jToggleButtonExpand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonExpandActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel4.add(jToggleButtonExpand, gridBagConstraints);

        jButtonRun.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/media-playback-start.png"))); // NOI18N
        jButtonRun.setToolTipText("Start all tests");
        jButtonRun.setMaximumSize(new java.awt.Dimension(32, 20));
        jButtonRun.setMinimumSize(new java.awt.Dimension(32, 20));
        jButtonRun.setPreferredSize(new java.awt.Dimension(32, 20));
        jButtonRun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRunActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        jPanel4.add(jButtonRun, gridBagConstraints);

        jProgressBar.setMaximumSize(new java.awt.Dimension(32767, 12));
        jProgressBar.setMinimumSize(new java.awt.Dimension(148, 12));
        jProgressBar.setPreferredSize(new java.awt.Dimension(148, 12));
        jProgressBar.setString("uninitialized");
        jProgressBar.setStringPainted(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel4.add(jProgressBar, gridBagConstraints);

        jButtonXML.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/document-open.png"))); // NOI18N
        jButtonXML.setToolTipText("View master file");
        jButtonXML.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButtonXML.setMaximumSize(new java.awt.Dimension(20, 20));
        jButtonXML.setMinimumSize(new java.awt.Dimension(20, 20));
        jButtonXML.setPreferredSize(new java.awt.Dimension(20, 20));
        jButtonXML.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonXMLActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel4.add(jButtonXML, gridBagConstraints);

        jLabel1.setText("master_test_name");
        jLabel1.setMaximumSize(new java.awt.Dimension(200, 14));
        jLabel1.setMinimumSize(new java.awt.Dimension(200, 14));
        jLabel1.setPreferredSize(new java.awt.Dimension(200, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanel4.add(jLabel1, gridBagConstraints);

        jButtonReport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/text-html.png"))); // NOI18N
        jButtonReport.setToolTipText("Generate global report");
        jButtonReport.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButtonReport.setMaximumSize(new java.awt.Dimension(20, 20));
        jButtonReport.setMinimumSize(new java.awt.Dimension(20, 20));
        jButtonReport.setPreferredSize(new java.awt.Dimension(20, 20));
        jButtonReport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonReportActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel4.add(jButtonReport, gridBagConstraints);

        jButtonDeploy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/view-refresh.png"))); // NOI18N
        jButtonDeploy.setToolTipText("Deploy all tests");
        jButtonDeploy.setMaximumSize(new java.awt.Dimension(32, 20));
        jButtonDeploy.setMinimumSize(new java.awt.Dimension(32, 20));
        jButtonDeploy.setPreferredSize(new java.awt.Dimension(32, 20));
        jButtonDeploy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeployActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel4.add(jButtonDeploy, gridBagConstraints);

        jComboBoxRunner.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "parallel", "sequential" }));
        jComboBoxRunner.setMaximumSize(new java.awt.Dimension(32767, 20));
        jComboBoxRunner.setMinimumSize(new java.awt.Dimension(120, 20));
        jComboBoxRunner.setPreferredSize(new java.awt.Dimension(120, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel4.add(jComboBoxRunner, gridBagConstraints);

        add(jPanel4);

        jPanelPadding.setLayout(new java.awt.GridBagLayout());

        jPanelContainer.setLayout(new javax.swing.BoxLayout(jPanelContainer, javax.swing.BoxLayout.Y_AXIS));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 24, 0, 0);
        jPanelPadding.add(jPanelContainer, gridBagConstraints);

        add(jPanelPadding);
    }// </editor-fold>//GEN-END:initComponents

    private void jToggleButtonExpandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonExpandActionPerformed

        if (this.jToggleButtonExpand.isSelected())
        {
            Color[] colors = new Color[2];
            colors[0] = this.getBackground().brighter();
            colors[1] = this.getBackground();
            int i = 0;

            for (TestData testData : this.master.getTestDatas())
            {
                JPanelTest jPanelTest = new JPanelTest(this, testData);
                jPanelTest.setBackgroundColor(colors[i++ % 2]);
                this.jPanelTests.add(jPanelTest);
                this.jPanelContainer.add(jPanelTest);
                
            }

            this.jPanelContainer.revalidate();
            jToggleButtonExpand.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/list-remove.png")));
        }
        else
        {
            for (JPanelTest jPanelTest : this.jPanelTests)
            {
                try
                {
                    jPanelTest.setMasterRunner(null);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            this.jPanelTests.clear();
            this.jPanelContainer.removeAll();
            this.jPanelContainer.revalidate();
            jToggleButtonExpand.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/list-add.png")));
        }
}//GEN-LAST:event_jToggleButtonExpandActionPerformed

    private void jButtonRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRunActionPerformed
        if (!isRunning)
        {
            try
            {
                // Check that there is "TestData" already running
                // because for now a Master test starts ALL it's
                // "TestDatas".
                
                // If anything is running then we can't start the master test.
                for(MasterRunner aRunner:MasterRunnerRegistry.getRunners())
                {
                    if(!aRunner.finished()) throw new Exception("Cannot start the test. There are other tests running.");
                }
                
                if ((null != this.getMasterRunner() && !this.getMasterRunner().finished()) || this.isRunning || this.isDeploying)
                {
                    throw new Exception("Cannot start the test again. It should already be running.");
                }
                else if (this.jComboBoxRunner.getSelectedItem().equals("parallel"))
                {
                    this.setMasterRunner(new MasterRunnerSimultaneous(this.master, this.master.getTestDatas()));
                    this.getMasterRunner().start();
                }
                else if (this.jComboBoxRunner.getSelectedItem().equals("sequential"))
                {
                    this.setMasterRunner(new MasterRunnerSequential(this.master, this.master.getTestDatas()));
                    this.getMasterRunner().start();
                }
                
                MasterRunnerRegistry.register(masterRunner.getMaster().attributeValue("name"), masterRunner);

                this.getMasterRunner().addListener(new NotificationListener<Notification<String, RunnerState>>(){
                    RunnerState lastState = null;;
                    public void notificationReceived(Notification<String, RunnerState> notification){
                        if(null == lastState)
                        {
                            lastState = notification.getData();
                            JFrameMaster.instance().updateRunning();
                        }
                        else
                        {
                            lastState.setState(notification.getData().getState());
                            if(lastState.changed()) JFrameMaster.instance().updateRunning();
                        }
                    }
                });
                
            }
            catch (Exception e)
            {
                this.setIsDeploying(false);
                this.setIsRunning(false);

                try
                {
                    this.setMasterRunner(null);
                }
                catch (Exception ee)
                {
                    Utils.showError(this, "Error freeing all slaves", ee);
                }
                Utils.showError(this, "Error starting master test", e);
            }
        }
        else
        {
            try
            {
                this.getMasterRunner().stop();
            }
            catch (Exception e)
            {
                Utils.showError(this, "Error starting master test", e);
            }
        }
}//GEN-LAST:event_jButtonRunActionPerformed

private void jButtonReportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonReportActionPerformed
    try
    {
        final ReportGenerator reportGenerator = this.getMasterRunner().report();

        JPanelReporting jPanelReporting = new JPanelReporting(reportGenerator, reportGenerator.getReportDirectory());
        JFrameTasks.instance().addTask(jPanelReporting);
        JFrameTasks.instance().setVisible(false);
        JFrameTasks.instance().setVisible(true);
        this.jButtonReport.setEnabled(false);
        reportGenerator.addListener(new NotificationListener<Notification<String, ReportStatus>>(){
            public void notificationReceived(Notification<String, ReportStatus> notification){
                if (100 == notification.getData().getProgress()) _this.jButtonReport.setEnabled(true);
            }
        });
    }
    catch (Exception e)
    {
        Utils.showError(this, e);
    }
}//GEN-LAST:event_jButtonReportActionPerformed

private void jButtonDeployActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeployActionPerformed

    try
    {
        this.setMasterRunner(new MasterRunnerDeployer(this.master, this.master.getTestDatas()));
        this.getMasterRunner().init(true);
        
        MasterRunnerRegistry.register(masterRunner.getMaster().attributeValue("name"), masterRunner);

        this.getMasterRunner().addListener(new NotificationListener<Notification<String, RunnerState>>(){
            RunnerState lastState = null;;
            public void notificationReceived(Notification<String, RunnerState> notification){
                if(null == lastState)
                {
                    lastState = notification.getData();
                    JFrameMaster.instance().updateRunning();
                }
                else
                {
                    lastState.setState(notification.getData().getState());
                    if(lastState.changed()) JFrameMaster.instance().updateRunning();
                }
            }
        });
    }
    catch (Exception e)
    {
        this.setIsDeploying(false);
        this.setIsRunning(false);
        Utils.showError(this, "Error deploying master test", e);
    }
 
}//GEN-LAST:event_jButtonDeployActionPerformed

private void jButtonXMLActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonXMLActionPerformed
{//GEN-HEADEREND:event_jButtonXMLActionPerformed
    Utils.openEditor(this.master.getXMLDocument().getXMLFile());
}//GEN-LAST:event_jButtonXMLActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonDeploy;
    private javax.swing.JButton jButtonReport;
    private javax.swing.JButton jButtonRun;
    private javax.swing.JButton jButtonXML;
    private javax.swing.JComboBox jComboBoxRunner;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanelContainer;
    private javax.swing.JPanel jPanelPadding;
    private javax.swing.JProgressBar jProgressBar;
    private javax.swing.JToggleButton jToggleButtonExpand;
    // End of variables declaration//GEN-END:variables
}
