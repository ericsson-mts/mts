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

package com.devoteam.srit.xmlloader.gui.better;

import com.devoteam.srit.xmlloader.core.*;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import com.devoteam.srit.xmlloader.gui.frames.JFrameLogsSession;
import com.devoteam.srit.xmlloader.gui.frames.JFrameRunProfile;
import com.devoteam.srit.xmlloader.gui.logs.GUITextListenerProvider;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map.Entry;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Gwenhael
 */
public class TestcaseLineCtrl implements NotificationListener<Notification<String, RunnerState>> {

    private TestcaseLineView _view;
    private Testcase _testcase;
    private TestcaseLineCtrl _this = this;

    public TestcaseLineCtrl(TestcaseLineView view, Testcase testcase) {
        _view = view;
        _testcase = testcase;

        _view.jCheckBoxSelected.setSelected(testcase.getState());
        _view.jCheckBoxSelected.setText(testcase.getName());

        // set the jSpinner value AND behavior
        _view.jSpinnerNumber.setModel(new SpinnerNumberModel(testcase.getNumber(), 1, Integer.MAX_VALUE, 1));

        // report the modifications of the jspinner on the testcase object
        _view.jSpinnerNumber.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                _testcase.setNumber((Integer) ((JSpinner) e.getSource()).getValue());
            }
        });

        // event on click, start or stop the testcase runner, depending on the situation
        _view.jButtonAction.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                ThreadPool.reserve().start(new Runnable() {
                    @Override
                    public void run() {
                        if ("Stop".equals(e.getActionCommand())) {
                            _testcase.getTestcaseRunner().stop();
                        }
                        else {
                            try {
                                TestRunnerSingle testRunner = new TestRunnerSingle(_testcase.getParent(), _testcase);
                                RegistryTestRunner.getInstance().registerTestRunner(testRunner);
                                testRunner.start();
                            }
                            catch (Exception ee) {
                                ee.printStackTrace();
                            }
                        }
                    }
                });
            }
        });

        // action when checking the state checkbox
        _view.jCheckBoxSelected.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                _testcase.setState(((JCheckBox) e.getSource()).isSelected());
            }
        });
        
        // action when checking the logs checkbox
        _view.jCheckBoxLogs.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFrameLogsSession jFrameLogsApplication = GUITextListenerProvider.instance().getJFrameLogsSession(_testcase);
                jFrameLogsApplication.setVisible(!jFrameLogsApplication.isVisible());
                if (jFrameLogsApplication.isValid()) {
                    jFrameLogsApplication.gotoBottom();
                }
                JFrameLogsSession jFrameLogsSession = GUITextListenerProvider.instance().getJFrameLogsSession(_testcase);
                jFrameLogsSession.setTestcaseLineCtrl(_this);
                jFrameLogsSession.setVisible(_view.jCheckBoxLogs.isSelected());
            }
        });

        // action when clicking on the edit button
        _view.jButtonEdit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    for (Entry<String, ScenarioReference> scenario : _testcase.getScenarioPathByNameMap().entrySet()) {
                        Utils.openEditor(_testcase.getParent().getXMLDocument().getXMLFile().resolve(scenario.getValue().getFilename()));
                    }
                }
                catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
        });



        // action when clicking on the profile button
        _view.jButtonProfile.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    JFrameRunProfile jFrameRunProfile = JFrameRunProfile.getJFrame(_testcase.getProfile());
                    String title = "Edit run profile of testcase " + _testcase.getParent().getName() + "/" + _testcase.getName();
                    jFrameRunProfile.setTitle(title);
                    jFrameRunProfile.setVisible(true);
                }
                catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
        });

        _testcase.getTestcaseRunner().addListener(this);
    }

    public void setTestcaseSelected(final boolean value) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if(value != _view.jCheckBoxSelected.isSelected()){
                    _view.jCheckBoxSelected.doClick(0);
                }
            }
        });
    }

    public void setLogSelected(final boolean value) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                _view.jCheckBoxLogs.setSelected(value);
            }
        });
    }

    // this methods does the necessary modifications on swing components when receiving a state change from the testcase runner
    @Override
    public synchronized void notificationReceived(final Notification<String, RunnerState> notification) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                RunnerState state = notification.getData().clone();
                _view.jProgressBar.setValue(state._progression);
                _view.jProgressBar.setToolTipText(state.toPopupHTMLString());

                // the action button
                if (state.isStarted() && !state.isFinished()) {
                    _view.jButtonAction.setText("Stop");
                    _view.jButtonAction.setActionCommand("Stop");
                    _view.jButtonAction.setEnabled(_testcase.isInterruptible());
                }
                else if (state.isOpened() && !state.isFinished() && !state.isFailed() && !state.isInterrupted()) {
                    _view.jButtonAction.setEnabled(false);
                }
                else {
                    _view.jButtonAction.setText("Start");
                    _view.jButtonAction.setActionCommand(null);
                    _view.jButtonAction.setEnabled(true);
                }

                if (state.isFinished() && state.isFailed()) {
                    _view.jLabelIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/silk/cross.png"))); // NOI18N
                }
                else if (state.isFinished() && !state.isInterrupted()) {
                    _view.jLabelIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/silk/tick.png"))); // NOI18N
                }
                else if (!state.isFinished() && state.isFailed()) {
                    _view.jLabelIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/silk/hourglass_delete.png"))); // NOI18N
                }
                else if (state.isStarted()) {
                    _view.jLabelIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/silk/hourglass.png"))); // NOI18N
                }
                else {
                    _view.jLabelIcon.setIcon(null);
                }
            }
        });
    }
}
