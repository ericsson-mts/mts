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
import com.devoteam.srit.xmlloader.core.Testcase;
import com.devoteam.srit.xmlloader.core.report.ReportGenerator;
import com.devoteam.srit.xmlloader.core.report.ReportStatus;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.exceptionhandler.ExceptionHandlerSingleton;
import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import com.devoteam.srit.xmlloader.gui.frames.JPanelReporting;
import com.devoteam.srit.xmlloader.master.masterutils.MasterRunner;
import com.devoteam.srit.xmlloader.master.masterutils.MasterRunnerDeployer;
import com.devoteam.srit.xmlloader.master.masterutils.MasterRunnerRegistry;
import com.devoteam.srit.xmlloader.master.masterutils.MasterRunnerSingleTest;
import com.devoteam.srit.xmlloader.master.masterutils.RemoteTesterRunner;
import com.devoteam.srit.xmlloader.master.masterutils.TestData;
import java.awt.Color;
import java.util.LinkedList;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author  gege
 */
public class JPanelTest extends javax.swing.JPanel implements NotificationListener<Notification<String, RunnerState>>
{
    private MasterRunner masterRunner;
    private RemoteTesterRunner remoteTesterRunner;

    public void setMasterRunner(MasterRunner masterRunner) 
    {
        if(null == masterRunner) return;
        
        if(null != this.remoteTesterRunner) this.remoteTesterRunner.removeListener(this);

        if(null != masterRunner)
        {
            for(RemoteTesterRunner child:masterRunner.getChildren())
            {
                if(child.getTestData() == this.testData)
                {
                    this.masterRunner = masterRunner;
                    this.remoteTesterRunner = child;
                }
            }

        }
        else
        {
            this.remoteTesterRunner = null;
            this.masterRunner = null;
        }

        if(null != this.remoteTesterRunner) this.remoteTesterRunner.addListener(this);
    }

    public MasterRunner getMasterRunner()
    {
        return this.masterRunner;
    }
    
    
    
    
    private TestData testData;
    
    private boolean isRunning;
    private boolean isDeploying;
    private JPanelMaster jPanelMaster;
    private final JPanelTest _this = this;

    private LinkedList<JPanelTestcase> jPanelTestcases;
    
    /** Creates new form JPanelTest */
    public JPanelTest(JPanelMaster jPanelMaster, TestData testData)
    {
        this.jPanelMaster = jPanelMaster;
        this.isRunning = false;
        this.testData = testData;
        initComponents();
        this.jTextFieldSlave.setText(this.testData.attributeValue("slave"));
        this.jComboBoxRunner.setSelectedItem(this.testData.attributeValue("runner"));
        this.jLabelTestName.setText(this.testData.attributeValue("name"));
        this.jLabelTestName.setToolTipText(this.testData.attributeValue("description"));
        this.jPanelTestcases = new LinkedList();
        
        this.jTextFieldSlave.getDocument().addDocumentListener(new DocumentListener(){

            public void insertUpdate(DocumentEvent e){
                _this.testData.setSlave(_this.jTextFieldSlave.getText());
            }

            public void removeUpdate(DocumentEvent e){
                _this.testData.setSlave(_this.jTextFieldSlave.getText());
            }

            public void changedUpdate(DocumentEvent e){
                // nothing.
            }
        });
        
        
        // TRY to get the same runner as our own master
        this.setMasterRunner(this.jPanelMaster.getMasterRunner());
        
        // if not, try to get it from the registry
        if(null == this.getMasterRunner())
        {
            String name = "";
            name += this.jPanelMaster.getMaster().attributeValue("name");
            name += this.testData.attributeValue("name");
            this.setMasterRunner(MasterRunnerRegistry.get(name));
        }
    }
    
    public void setBackgroundColor(Color color)
    {
        this.setBackground(color);
        this.jLabelTestName.setBackground(color);
        this.jPanel4.setBackground(color);
    }
    
    private void setIsRunning(boolean isRunning)
    {
        this.isRunning = isRunning;
        this.udpateComponents();
    }

    private void setIsDeploying(boolean isDeploying)
    {
        this.isDeploying = isDeploying;
        this.udpateComponents();
    }
    
    private void udpateComponents()
    {
        if((this.isRunning && (this.jPanelMaster.getMasterRunner() == this.getMasterRunner())) || this.isDeploying)
        {
            this.jButtonRun.setEnabled(false);
            this.jButtonDeploy.setEnabled(false);
            this.jComboBoxRunner.setEnabled(false);
            this.jTextFieldSlave.setEnabled(false);
        }
        else
        {
            this.jButtonRun.setEnabled(true);
            this.jButtonDeploy.setEnabled(!this.isRunning);
            this.jComboBoxRunner.setEnabled(!isRunning);
            this.jTextFieldSlave.setEnabled(!isRunning);
        }

        if (this.isRunning) this.jButtonRun.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/process-stop.png"))); // NOI18N
        else                this.jButtonRun.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/media-playback-start.png"))); // NOI18N

    }
    
    public TestData getTestData()
    {
        return this.testData;
    }

    /**
     * Update the display of the state of the test upon reception of a Notification.
     *  - Update the progress bar
     *    - It's color.
     *    - It's progression.
     *  - Update the isRunning/isDeploying status
     * @param notification
     */
    public void notificationReceived(Notification<String, RunnerState> notification)
    {
        RunnerState runnerState = notification.getData();

        // handle progression
        this.jProgressBar.setValue(runnerState.getProgression());
        this.jProgressBar.setToolTipText(runnerState.toPopupHTMLString());
        
        // handle state display
        State state = notification.getData().getState();
        jProgressBar.setString(notification.getData().getState().toString().toLowerCase());

                // handle the running state
        switch (state)
        {
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
                break;
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
        
        // handle state
        switch(state)
        {
            case UNINITIALIZED:
            case OPEN_SUCCEEDED:
            case OPEN_FAILED:
            case INTERRUPTED:
            case FAILED:
            case SUCCEEDED:
                this.setIsDeploying(false);
                this.setIsRunning(false);
                break;
            case OPENING:
                this.setIsDeploying(true);
                this.setIsRunning(false);
                break;
            case RUNNING:
            case FAILING:
            case INTERRUPTING:
                this.setIsDeploying(false);
                this.setIsRunning(true);
                break;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel4 = new javax.swing.JPanel();
        jLabelTestName = new javax.swing.JLabel();
        jButtonReport = new javax.swing.JButton();
        jToggleButtonExpand = new javax.swing.JToggleButton();
        jButtonRun = new javax.swing.JButton();
        jProgressBar = new javax.swing.JProgressBar();
        jComboBoxRunner = new javax.swing.JComboBox();
        jButtonDeploy = new javax.swing.JButton();
        jButtonXML = new javax.swing.JButton();
        jTextFieldSlave = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jPanelPadding = new javax.swing.JPanel();
        jPanelConstantContainer = new javax.swing.JPanel();
        jPanelContainer = new javax.swing.JPanel();

        setMinimumSize(new java.awt.Dimension(700, 24));
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        jPanel4.setMaximumSize(new java.awt.Dimension(2147483647, 24));
        jPanel4.setMinimumSize(new java.awt.Dimension(650, 24));
        jPanel4.setPreferredSize(new java.awt.Dimension(650, 24));
        jPanel4.setLayout(new java.awt.GridBagLayout());

        jLabelTestName.setText("test_name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel4.add(jLabelTestName, gridBagConstraints);

        jButtonReport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/text-html.png"))); // NOI18N
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
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel4.add(jButtonReport, gridBagConstraints);

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
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel4.add(jToggleButtonExpand, gridBagConstraints);

        jButtonRun.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/media-playback-start.png"))); // NOI18N
        jButtonRun.setMaximumSize(new java.awt.Dimension(32, 20));
        jButtonRun.setMinimumSize(new java.awt.Dimension(32, 20));
        jButtonRun.setPreferredSize(new java.awt.Dimension(32, 20));
        jButtonRun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRunActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel4.add(jButtonRun, gridBagConstraints);

        jProgressBar.setMaximumSize(new java.awt.Dimension(32767, 12));
        jProgressBar.setMinimumSize(new java.awt.Dimension(148, 12));
        jProgressBar.setPreferredSize(new java.awt.Dimension(148, 12));
        jProgressBar.setString("uninitialized");
        jProgressBar.setStringPainted(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel4.add(jProgressBar, gridBagConstraints);

        jComboBoxRunner.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "sequential", "load" }));
        jComboBoxRunner.setMinimumSize(new java.awt.Dimension(100, 20));
        jComboBoxRunner.setPreferredSize(new java.awt.Dimension(100, 20));
        jComboBoxRunner.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxRunnerActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel4.add(jComboBoxRunner, gridBagConstraints);

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
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel4.add(jButtonDeploy, gridBagConstraints);

        jButtonXML.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/document-open.png"))); // NOI18N
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
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel4.add(jButtonXML, gridBagConstraints);

        jTextFieldSlave.setText("127.0.0.1:2099");
        jTextFieldSlave.setMargin(new java.awt.Insets(0, 4, 0, 0));
        jTextFieldSlave.setMinimumSize(new java.awt.Dimension(150, 20));
        jTextFieldSlave.setPreferredSize(new java.awt.Dimension(130, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel4.add(jTextFieldSlave, gridBagConstraints);

        jLabel1.setText("Slave :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 5);
        jPanel4.add(jLabel1, gridBagConstraints);

        add(jPanel4);

        jPanelPadding.setLayout(new java.awt.GridBagLayout());

        jPanelConstantContainer.setLayout(new javax.swing.BoxLayout(jPanelConstantContainer, javax.swing.BoxLayout.Y_AXIS));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanelPadding.add(jPanelConstantContainer, gridBagConstraints);

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
            if(null == this.remoteTesterRunner)
            {
                Throwable throwable = new Throwable("Cannot expand an undeployed test");
                ExceptionHandlerSingleton.instance().display(throwable, this);
                this.jToggleButtonExpand.setSelected(false);
                return;
            }

            if(null == this.remoteTesterRunner.getTest())
            {
                Throwable throwable = new Throwable("Cannot expand an undeployed test");
                ExceptionHandlerSingleton.instance().display(throwable, this);
                this.jToggleButtonExpand.setSelected(false);
                return;
            }

            Color[] colors = {this.getParent().getBackground().brighter(), this.getParent().getBackground()};
            int i = 0;

            for(Testcase testcase:this.remoteTesterRunner.getTest().getChildren())
            {
                
                JPanelTestcase jPanelTestcase = new JPanelTestcase(this, testcase);
                jPanelTestcase.setBackgroundColor(colors[i++ % 2]);
                this.jPanelTestcases.add(jPanelTestcase);
                this.jPanelContainer.add(jPanelTestcase);
            }

            this.jPanelContainer.revalidate();
            jToggleButtonExpand.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/list-remove.png")));
        }
        else
        {
            for (JPanelTestcase jPanelTestcase : this.jPanelTestcases)
            {
                try
                {
                    jPanelTestcase.setMasterRunner(null);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            this.jPanelTestcases.clear();
            this.jPanelContainer.removeAll();
            this.jPanelContainer.revalidate();
            jToggleButtonExpand.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/list-add.png")));
        }
}//GEN-LAST:event_jToggleButtonExpandActionPerformed

    private void jButtonRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRunActionPerformed
        if(isRunning)
        {
            try
            {
                this.getMasterRunner().stop();
            }
            catch (Exception e)
            {
                Utils.showError(this, "Error stopping test " + this.testData.attributeValue("name"), e);
            }
        }
        else
        {
            try
            {
                if (this.isRunning || this.isDeploying)
                {
                    throw new Exception("error message to fill");
                }
                else
                {
                    LinkedList<TestData> dummyList = new LinkedList();
                    dummyList.add(this.testData);
                    final MasterRunner runner = new MasterRunnerSingleTest(this.jPanelMaster.getMaster(), dummyList);
                    runner.start();
                    this.setMasterRunner(runner);
                    for(JPanelTestcase child:jPanelTestcases)
                    {
                        child.setMasterRunner(null);
                    }

                    runner.addListener(new NotificationListener<Notification<String, RunnerState>>(){
                        public void notificationReceived(Notification<String, RunnerState> notification)
                        {
                            switch(notification.getData().getState())
                            {
                                case UNINITIALIZED:
                                case OPENING:
                                    break;
                                default:
                                    for(JPanelTestcase child:jPanelTestcases)
                                    {
                                        child.setMasterRunner(masterRunner);
                                    }
                                    runner.removeListener(this);
                            }
                        }
                    });
                    MasterRunnerRegistry.register(this.getMasterRunner().getMaster().attributeValue("name") + this.testData.attributeValue("name"), masterRunner);


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
            }
            catch(Exception e)
            {
                this.setIsDeploying(false);
                this.setIsRunning(false);

                try
                {
                    this.setMasterRunner(null);
                }
                catch (Exception ee)
                {
                    Utils.showError(this, "Error freeing slave", ee);
                }
                Utils.showError(this, "Error starting master test", e);
            }
        }
}//GEN-LAST:event_jButtonRunActionPerformed

private void jComboBoxRunnerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxRunnerActionPerformed
    this.testData.setRunner(this.jComboBoxRunner.getSelectedItem().toString());
}//GEN-LAST:event_jComboBoxRunnerActionPerformed

private void jButtonReportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonReportActionPerformed
    try
    {
        final ReportGenerator reportGenerator = this.remoteTesterRunner.report();

        JPanelReporting jPanelReporting = new JPanelReporting(reportGenerator, reportGenerator.getReportDirectory());
        JFrameTasks.instance().addTask(jPanelReporting);
        JFrameTasks.instance().setVisible(false);
        JFrameTasks.instance().setVisible(true);
        this.jButtonReport.setEnabled(false);
        final JPanelTest jPanelTest = this;
        reportGenerator.addListener(new NotificationListener<Notification<String, ReportStatus>>() {
            public void notificationReceived(Notification<String, ReportStatus> notification) {
                if (100 == notification.getData().getProgress()) jPanelTest.jButtonReport.setEnabled(true);
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
        LinkedList<TestData> dummyList = new LinkedList();
        dummyList.add(this.testData);
        this.setMasterRunner(new MasterRunnerDeployer(this.jPanelMaster.getMaster(), dummyList));
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

        try
        {
            this.setMasterRunner(null);
        }
        catch (Exception ee)
        {
            Utils.showError(this, "Error freeing all slaves", ee);
        }
        Utils.showError(this, "Error deploying master test", e);
    }
}//GEN-LAST:event_jButtonDeployActionPerformed

private void jButtonXMLActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonXMLActionPerformed
{//GEN-HEADEREND:event_jButtonXMLActionPerformed
    //.master.getXMLDocument().getXMLFile()
    Utils.openEditor(this.testData.getPath());
}//GEN-LAST:event_jButtonXMLActionPerformed

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonDeploy;
    private javax.swing.JButton jButtonReport;
    private javax.swing.JButton jButtonRun;
    private javax.swing.JButton jButtonXML;
    private javax.swing.JComboBox jComboBoxRunner;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelTestName;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanelConstantContainer;
    private javax.swing.JPanel jPanelContainer;
    private javax.swing.JPanel jPanelPadding;
    private javax.swing.JProgressBar jProgressBar;
    private javax.swing.JTextField jTextFieldSlave;
    private javax.swing.JToggleButton jToggleButtonExpand;
    // End of variables declaration//GEN-END:variables
    
}
