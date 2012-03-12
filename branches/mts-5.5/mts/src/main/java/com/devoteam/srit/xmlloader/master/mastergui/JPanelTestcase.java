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
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import com.devoteam.srit.xmlloader.master.MasterImplementation;
import com.devoteam.srit.xmlloader.master.masterutils.MasterRunner;
import com.devoteam.srit.xmlloader.master.masterutils.MasterRunnerDeployer;
import com.devoteam.srit.xmlloader.master.masterutils.RemoteTesterRunner;
import com.devoteam.srit.xmlloader.master.testmanager.RemoteTester;
import java.awt.Color;
import java.util.LinkedList;
import javax.swing.JPanel;

/**
 *
 * @author  gege
 */
public class JPanelTestcase extends javax.swing.JPanel implements NotificationListener<Notification<String, RunnerState>>
{
    private MasterRunner masterRunner;
    private RemoteTesterRunner remoteTesterRunner;

    public void setMasterRunner(MasterRunner masterRunner)
    {
        try
        {
            try
            {
                MasterImplementation.instance().removeMultiplexedListener(this);
            }
            catch(Exception e)
            {
                GlobalLogger.instance().getApplicationLogger().error(Topic.CORE, e, "error while unregistering listener");
            }

            if (null != masterRunner)
            {
                for (RemoteTesterRunner child : masterRunner.getChildren())
                {
                    if (child.getTestData() == jPanelTest.getTestData())
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

            if (null != this.remoteTesterRunner)
            {
                RemoteTester remoteTester = this.remoteTesterRunner.getRemoteTester();
                MasterImplementation.instance().addMultiplexedListener(this,
                        remoteTester,
                        this.jPanelTest.getTestData().attributeValue("name"),
                        this.testcase.attributeValue("name"),
                        null);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            this.remoteTesterRunner = null;
            this.masterRunner = null;
        }
    }

    public MasterRunner getMasterRunner()
    {
        return this.masterRunner;
    }
    
    
    
    
    //private TestData testData;
    
    private LinkedList<JPanel> jPanelsConstant;
    
    private boolean isRunning;
    private boolean isDeploying;
    private JPanelTest jPanelTest;
    private final JPanelTestcase _this = this;
    private Testcase testcase;
    /** Creates new form JPanelTest */
    public JPanelTestcase(JPanelTest jPanelTest, Testcase testcase)
    {
        this.jPanelsConstant = new LinkedList<JPanel>();
        this.jPanelTest = jPanelTest;
        this.isRunning = false;
        this.testcase=testcase;
        initComponents();
        this.jLabelTestName.setText(this.testcase.attributeValue("name"));
        this.jLabelTestName.setToolTipText(this.testcase.attributeValue("description"));

        this.setMasterRunner(jPanelTest.getMasterRunner());
        this.jToggleButtonExpand.setVisible(false);
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
        if((this.isRunning && (this.jPanelTest.getMasterRunner() == this.getMasterRunner())) || this.isDeploying)
        {
            this.jToggleButtonExpand.setEnabled(false);
            //this.jButtonRun.setEnabled(false);
        }
        else
        {
            //this.jButtonRun.setEnabled(true);
        }

//        if (this.isRunning) this.jButtonRun.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/process-stop.png"))); // NOI18N
//        else                this.jButtonRun.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/media-playback-start.png"))); // NOI18N
    }
    
    public Testcase getTestcase()
    {
        return this.testcase;
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
        jToggleButtonExpand = new javax.swing.JToggleButton();
        jProgressBar = new javax.swing.JProgressBar();
        jLabelTestName = new javax.swing.JLabel();
        jPanelPadding = new javax.swing.JPanel();
        jPanelConstantContainer = new javax.swing.JPanel();
        jPanelContainer = new javax.swing.JPanel();

        setMinimumSize(new java.awt.Dimension(700, 24));
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        jPanel4.setMaximumSize(new java.awt.Dimension(2147483647, 24));
        jPanel4.setMinimumSize(new java.awt.Dimension(650, 24));
        jPanel4.setPreferredSize(new java.awt.Dimension(650, 24));
        jPanel4.setLayout(new java.awt.GridBagLayout());

        jToggleButtonExpand.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/list-add.png"))); // NOI18N
        jToggleButtonExpand.setEnabled(false);
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
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel4.add(jToggleButtonExpand, gridBagConstraints);

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

        jLabelTestName.setText("test_name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel4.add(jLabelTestName, gridBagConstraints);

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
}//GEN-LAST:event_jToggleButtonExpandActionPerformed

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabelTestName;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanelConstantContainer;
    private javax.swing.JPanel jPanelContainer;
    private javax.swing.JPanel jPanelPadding;
    private javax.swing.JProgressBar jProgressBar;
    private javax.swing.JToggleButton jToggleButtonExpand;
    // End of variables declaration//GEN-END:variables
    
}
