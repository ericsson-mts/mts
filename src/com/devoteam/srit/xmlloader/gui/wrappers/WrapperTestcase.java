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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.gui.wrappers;

import com.devoteam.srit.xmlloader.core.RunnerState;
import com.devoteam.srit.xmlloader.core.Scenario;
import com.devoteam.srit.xmlloader.core.TestRunnerSingle;
import com.devoteam.srit.xmlloader.core.Testcase;
import com.devoteam.srit.xmlloader.core.ThreadPool;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.hierarchy.DefaultHierarchyMember;
import com.devoteam.srit.xmlloader.core.utils.hierarchy.HierarchyMember;
import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import com.devoteam.srit.xmlloader.gui.GuiHelper;
import com.devoteam.srit.xmlloader.gui.TesterGui;
import com.devoteam.srit.xmlloader.gui.frames.JFrameLogsSession;
import com.devoteam.srit.xmlloader.gui.frames.JFrameRunProfile;
import com.devoteam.srit.xmlloader.gui.logs.GUITextListenerProvider;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JFrame;
import java.awt.Window;
import java.util.ArrayList;

/**
 *
 * @author gpasquiers
 */
public class WrapperTestcase implements NotificationListener<Notification<String, RunnerState>>,
                                        ActionListener,
                                        HierarchyMember<WrapperTest, Object>
{
    // <editor-fold desc="Hierarchy implementation" defaultstate="collapsed">
    private DefaultHierarchyMember<WrapperTest, Object> defaultHierarchyMember;
    //private ArrayList<JCheckBox> myJCheckBoxLogs = new ArrayList<JCheckBox>();

    public WrapperTest getParent()
    {
        return this.defaultHierarchyMember.getParent();
    }

    public List<Object> getChildren()
    {
        return this.defaultHierarchyMember.getChildren();
    }

    public void setParent(WrapperTest parent)
    {
        this.defaultHierarchyMember.setParent(parent);
    }

    public void addChild(Object child)
    {
        this.defaultHierarchyMember.addChild(child);
    }

    public void removeChild(Object child)
    {
        this.defaultHierarchyMember.removeChild(child);
    }
    // </editor-fold>
    
    private Testcase testcase;
    
    private int row;
    
    public WrapperTestcase()
    {
        //myJCheckBoxLogs.add(this.jCheckBoxLogs);
        this.defaultHierarchyMember = new DefaultHierarchyMember<WrapperTest, Object>();
    }
            
    public WrapperTestcase(Testcase testcase, int row)
    {        
        this();
        //myJCheckBoxLogs.add(this.jCheckBoxLogs);
        this.testcase = testcase;
        this.row = row;
        this.initComponents();
        if(null != testcase.attributeValue("state"))
        {
            this.setActive(Boolean.parseBoolean(testcase.attributeValue("state")));
        }
    }
    
    public Testcase getTestcase()
    {
        return this.testcase;
    }
    
    public void setLogs(final boolean status)
    {
        EventQueue.invokeLater( new Runnable()
        {
            public void run()
            {
                jCheckBoxLogs.setSelected(status);
                TesterGui.instance().fireTableRowsUpdated(row, row);
            }
        });
    }
    
    public void setActive(final boolean status)
    {
        EventQueue.invokeLater( new Runnable()
        {
            public void run()
            {
                jCheckBoxActive.setSelected(status);
                TesterGui.instance().fireTableRowsUpdated(row, row);
            }
        });
    }

    public boolean getActive()
    {
        return jCheckBoxActive.isSelected();
    }

    public void setEnabled(final boolean status)
    {
        EventQueue.invokeLater( new Runnable()
        {
            public void run()
            {
                jCheckBoxActive.setEnabled(status);
                if(false == status && getParent().getRunner() instanceof TestRunnerSingle)
                {
                    jButtonStart.setEnabled(true);
                }
                else
                {
                    jButtonStart.setEnabled(status);
                }
                jSpinnerNumber.setEnabled(status);
                jButtonProfile.setEnabled(!status);
                TesterGui.instance().fireTableRowsUpdated(row, row);
            }
        });
    }

    public int getNumber()
    {
        return ((Integer)this.jSpinnerNumber.getValue()).intValue();
    }
    
    // <editor-fold desc="Swing components" defaultstate="collapsed">
    private JCheckBox    jCheckBoxActive;
    private JButton      jButtonStart;
    private JButton      jButtonShow;
    private JButton      jButtonProfile;
    private JLabel       jLabelResult;
    private JCheckBox    jCheckBoxLogs;
    private JProgressBar jProgressBar;
    private JSpinner     jSpinnerNumber;

    private void initComponents()
    {
        // JCheckBox : select test (to run or not)
        jCheckBoxActive = new JCheckBox();
        jCheckBoxActive.setToolTipText("Select / unselect the testcase");
        jCheckBoxActive.setSelected(false);
        jCheckBoxActive.setActionCommand(Command.ACTIVE.toString());
        
        jCheckBoxActive.setText(testcase.getName());
        jCheckBoxActive.addActionListener(this);
        
        // JCheckbox: session logs
        jCheckBoxLogs = new JCheckBox("Logs");
        jCheckBoxLogs.setToolTipText("Display scenarios logs");
        jCheckBoxLogs.setActionCommand(Command.SHOW_TEST_LOGS.toString());
        jCheckBoxLogs.addActionListener(this);
        
        // JSpinner: number of runs to do for the testcase
        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1);
        jSpinnerNumber = new JSpinner(spinnerNumberModel);
        jSpinnerNumber.setToolTipText("Enter the iteration number");
        jSpinnerNumber.setValue(1);
        if(null != testcase.attributeValue("number"))
        {
            jSpinnerNumber.setValue(Integer.parseInt(testcase.attributeValue("number")));
        }
        
        // JButton: start the testcase
        jButtonStart = new JButton("Run");
        jButtonStart.setToolTipText("Run the testcase");
        jButtonStart.setActionCommand(Command.START_TEST.toString());
        jButtonStart.addActionListener(this);
        
        // JProgressBar: progression of the testcase
        jProgressBar = new JProgressBar(0,1);
        jProgressBar.setToolTipText("Progression of the testcase");
        jProgressBar.setStringPainted(true);
        jProgressBar.setMinimum(0);
        jProgressBar.setMaximum(100);
        
        // JLabel: Test status
        jLabelResult = new JLabel(GuiHelper.ICON_IDLE);
        jLabelResult.setToolTipText("Test result");
        
        // JButton: Show scenario files
        jButtonShow = new JButton("Edit");
        jButtonShow.setToolTipText("Edit the scenarios scripts");
        jButtonShow.setActionCommand(Command.SHOW_TEST_SOURCE.toString());
        jButtonShow.addActionListener(this);

        // JButton: edit the testcase profile
        jButtonProfile = new JButton("Profile");
        jButtonProfile.setToolTipText("Edit the run profile");
        jButtonProfile.setActionCommand(Command.EDIT_PROFILE.toString());
        jButtonProfile.addActionListener(this);
        jButtonProfile.setEnabled(false);
    }
    
    public Component[] getComponents()
    {
        Component[] components = {jCheckBoxActive, jSpinnerNumber, jButtonStart, jProgressBar, jCheckBoxLogs, jLabelResult, jButtonShow, jButtonProfile};
        return components;
    }
    // </editor-fold>
        
    // <editor-fold desc="Swing events" defaultstate="collapsed">
    public enum Command
    {
        IGNORE,
        ACTIVE,
        START_TEST,
        STOP_TEST,
        SHOW_TEST_SOURCE,
        SHOW_TEST_LOGS,
        EDIT_PROFILE
    }
    
    public void actionPerformed(ActionEvent e)
    {
        Command command = Command.valueOf(e.getActionCommand());

        switch(command)
        {
            case START_TEST:
                ThreadPool.reserve().start(new Runnable()
                {
                    public void run()
                    {
                        TesterGui.instance().startTestcase(testcase);
                    }
                });
                break;
            case STOP_TEST:
                ThreadPool.reserve().start(new Runnable()
                {
                    public void run()
                    {
                        try
                        {
                            try
                            {
                                getParent().getRunner().stop();
                            }
                            catch(Exception ee)
                            {
                                ee.printStackTrace();
                            }
                        }
                        catch(Exception ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                });
                break;
            case SHOW_TEST_SOURCE:
                for(Entry<String, Scenario> scenario:testcase.getScenarioPathByNameMap().entrySet())
                {
                    Utils.openEditor(testcase.getParent().getXMLDocument().getXMLFile().resolve(scenario.getValue().getFilename()));
                }
                break;
            case SHOW_TEST_LOGS:
                /**for (int i = 0; i<= myJCheckBoxLogs.size()-1; i++){
                    if (!myJCheckBoxLogs.get(i).equals(this.jCheckBoxLogs) && myJCheckBoxLogs.get(i).isSelected()){
                        myJCheckBoxLogs.get(i).setSelected(false);
                    }
                }
                Window[] frames = JFrame.getWindows();
                for (int i = 0; i <= frames.length-1; i++){
                    if (frames[i] instanceof JFrameLogsSession){
                        //this.jCheckBoxLogs.setSelected(!jCheckBoxLogs.isSelected());
                        frames[i].setVisible(false);
                    }
                }*/
                JFrameLogsSession jFrameLogsApplication = GUITextListenerProvider.instance().getJFrameLogsSession(this.testcase);
                jFrameLogsApplication.setVisible(!jFrameLogsApplication.isVisible());
                if(jFrameLogsApplication.isValid())
                {
                    jFrameLogsApplication.gotoBottom();
                }
                //GUITextListenerProvider.instance().setJFrameLogsSessionUnique(jFrameLogsSession);
                JFrameLogsSession jFrameLogsSession = GUITextListenerProvider.instance().getJFrameLogsSession(this.testcase);
                jFrameLogsSession.setWrapperTestcase(this);
                jFrameLogsSession.setVisible(jCheckBoxLogs.isSelected());
                //this.jCheckBoxLogs.setSelected(jCheckBoxLogs.isSelected());
                //GUITextListenerProvider.instance().getJFrameLogsSessionUnique(testcase).setVisible(jCheckBoxLogs.isSelected());
                break;
            case EDIT_PROFILE:
                try
                {
                    JFrameRunProfile jFrameRunProfile = JFrameRunProfile.getJFrame(testcase.getProfile());
                    String title = "Edit run profile of testcase " + this.testcase.getParent().getName() + "/" +  this.testcase.getName();
                    jFrameRunProfile.setTitle(title);
                    jFrameRunProfile.setVisible(true);

                }
                catch(Exception ee)
                {

                    ee.printStackTrace();
                }
                
                
                break;
        }
    }

    // </editor-fold>
    
    public void notificationReceived(Notification<String, RunnerState> notification)
    {
        final RunnerState runnerState = notification.getData();
        EventQueue.invokeLater(new Runnable()
        {

            public void run()
            {
                jProgressBar.setValue(runnerState.getProgression());
                jProgressBar.setString(runnerState.getProgression() + "%");
                jProgressBar.setToolTipText(runnerState.toPopupHTMLString());
                jButtonStart.setEnabled(true);
                switch (runnerState.getState())
                {
                    case OPENING:
                        jLabelResult.setIcon(GuiHelper.ICON_RUNNING);
                        jButtonStart.setActionCommand(Command.IGNORE.toString());
                        jButtonStart.setText("Opening");
                        jButtonStart.setToolTipText("Opening testcase");
                        break;
                    case OPEN_SUCCEEDED:
                        jLabelResult.setIcon(GuiHelper.ICON_RUNNING);
                        jButtonStart.setActionCommand(Command.IGNORE.toString());
                        jButtonStart.setText("Opened");
                        jButtonStart.setToolTipText("Testcase opened");
                        break;
                    case INTERRUPTING:
                        jButtonStart.setActionCommand(Command.STOP_TEST.toString());
                        jButtonStart.setText("Finally");
                        jButtonStart.setToolTipText("Test interrupting");
                        jButtonStart.setEnabled(false);
                        break;
                    case RUNNING:
                        jLabelResult.setIcon(GuiHelper.ICON_RUNNING);
                        jButtonStart.setActionCommand(Command.STOP_TEST.toString());
                        jButtonStart.setText("Stop");
                        jButtonStart.setToolTipText("Stop this test");
                        break;
                    case FAILING:
                        jLabelResult.setIcon(GuiHelper.ICON_KO);
                        break;
                    case SUCCEEDED:
                    case FAILED:
                    case INTERRUPTED:
                        jButtonStart.setActionCommand(Command.START_TEST.toString());
                        jButtonStart.setText("Run");
                        jButtonStart.setToolTipText("Run this test");

                        switch (runnerState.getState())
                        {
                            case SUCCEEDED:
                                jLabelResult.setIcon(GuiHelper.ICON_OK);
                                break;
                            case FAILED:
                                jLabelResult.setIcon(GuiHelper.ICON_KO);
                                break;
                        }

                        break;
                }
                TesterGui.instance().fireTableRowsUpdated(row, row);
            }
        });
    }

    
}
