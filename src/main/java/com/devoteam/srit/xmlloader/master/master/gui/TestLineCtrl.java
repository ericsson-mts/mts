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

package com.devoteam.srit.xmlloader.master.master.gui;

import com.devoteam.srit.xmlloader.core.RunnerState;
import com.devoteam.srit.xmlloader.core.ThreadPool;
import com.devoteam.srit.xmlloader.core.report.ReportGenerator;
import com.devoteam.srit.xmlloader.core.report.ReportStatus;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.exceptionhandler.ExceptionHandlerSingleton;
import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import com.devoteam.srit.xmlloader.gui.frames.JPanelReporting;
import com.devoteam.srit.xmlloader.master.master.utils.DataTest;
import com.devoteam.srit.xmlloader.master.master.utils.states.ConnectionState;
import com.devoteam.srit.xmlloader.master.master.utils.states.DeploymentState;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author Gwenhael
 */
public class TestLineCtrl {
    private TestLineView _view;
    private DataTest _dataTest;
    private TestcasePanelCtrl _testcasePanelCtrl;
    // event when deployment state changes
    private NotificationListener<DeploymentState> _deploymentStateListener = new NotificationListener<DeploymentState>() {
        @Override
        public void notificationReceived(final DeploymentState state) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    switch (state.getValue()) {
                        case DeploymentState.UNDEPLOYED:
                            break;
                        case DeploymentState.STARTED:
                            JFrameMasterCtrl.getInstance().somethingActivated();
                            break;
                        case DeploymentState.SUCCEEDED:
                        case DeploymentState.FAILED:
                            JFrameMasterCtrl.getInstance().somethingDeactivated();
                            break;
                    }

                    final boolean _deploying = (state.getValue() == DeploymentState.STARTED);
                    final boolean _deployed = (state.getValue() == DeploymentState.SUCCEEDED);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            _view.jProgressBar.setIndeterminate(_deploying);
                            _view.jToggleButtonExpand.setEnabled(_deployed);
                            _view.jButtonDeploy.setEnabled(!_deploying);
                            _view.jButtonRun.setEnabled(!_deploying);
                            _view.jToggleButtonConnect.setEnabled(!_deploying);
                        }
                    });
                }
            });
        }
    };
    // event when connection state changes
    private NotificationListener<ConnectionState> _connectionStateListener = new NotificationListener<ConnectionState>() {
        @Override
        public void notificationReceived(ConnectionState state) {
            final boolean _connected = state.getValue() == ConnectionState.CONNECTED;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    _view.jTextFieldSlave.setEnabled(!_connected);
                    _view.jToggleButtonConnect.setSelected(_connected);
                }
            });
        }
    };
    // event when runner state changes
    private NotificationListener<RunnerState> _runnerStateListener = new NotificationListener<RunnerState>() {
        boolean _lastStateActive = false;

        @Override
        public void notificationReceived(final RunnerState state) {

            // next lines of code and variables are to check and report to the gui activity change (test started, test stopped)
            boolean stateActive;
            if (state.isUninitialized() || (state.isOpened() && !state.isStarted()) || state.isFinished()) {
                stateActive = false;
            }
            else {
                stateActive = true;
            }

            if (stateActive && !_lastStateActive) {
                JFrameMasterCtrl.getInstance().somethingActivated();
            }
            else if (!stateActive && _lastStateActive) {
                JFrameMasterCtrl.getInstance().somethingDeactivated();
            }
            _lastStateActive = stateActive;

            final boolean _stateActive = stateActive;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    _view.jProgressBar.setValue(state._progression);
                    _view.jProgressBar.setToolTipText(state.toPopupHTMLString());
                    _view.jToggleButtonConnect.setEnabled(!_stateActive);
                    _view.jButtonDeploy.setEnabled(!_stateActive);
                    _view.jButtonStop.setEnabled(_stateActive);
                    _view.jButtonStop.setVisible(_stateActive);
                    _view.jButtonRun.setEnabled(!_stateActive);
                    _view.jButtonRun.setVisible(!_stateActive);
                    _view.jComboBoxRunner.setEnabled(!_stateActive);
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
    };

    public TestLineCtrl(TestLineView view, DataTest dataTest) {
        _view = view;
        _dataTest = dataTest;

        _dataTest.getControlerTest().getNotificationSenderForDeploymentState().addListener(_deploymentStateListener);
        _dataTest.getControlerTest().getNotificationSenderForConnectionState().addListener(_connectionStateListener);
        _dataTest.getControlerTest().getNotificationSenderForRunnerState().addListener(_runnerStateListener);

        _view.jTextFieldSlave.setText(_dataTest.getSlave());
        _view.jLabelTestName.setText(_dataTest.getName());
        // action for deploying
        _view.jButtonDeploy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (null != _testcasePanelCtrl) {
                    _testcasePanelCtrl.dispose();
                }
                if (_view.jToggleButtonExpand.isSelected()) {
                    _view.jToggleButtonExpand.doClick(0);
                }
                else {
                    _testcasePanelCtrl = null;
                }
                ThreadPool.reserve().start(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            _dataTest.getControlerTest().open(true);
                        }
                        catch (Exception ex) {
                            ExceptionHandlerSingleton.instance().display(ex, _view);
                        }
                    }
                });
            }
        });
        // action for running
        _view.jButtonRun.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (null != _testcasePanelCtrl) {
                    _testcasePanelCtrl.dispose();
                }
                if (_view.jToggleButtonExpand.isSelected()) {
                    _view.jToggleButtonExpand.doClick(0);
                }
                else {
                    _testcasePanelCtrl = null;
                }
                
                try {
                    _dataTest.getControlerTest().resetStats(System.currentTimeMillis());
                }
                catch (Exception ex) {
                    ExceptionHandlerSingleton.instance().display(ex, _view);
                }

                ThreadPool.reserve().start(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            _dataTest.getControlerTest().open(false);
                            _dataTest.getControlerTest().start();
                        }
                        catch (Exception ex) {
                            ExceptionHandlerSingleton.instance().display(ex, _view);
                        }
                    }
                });
            }
        });
        // action for stopping
        _view.jButtonStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ThreadPool.reserve().start(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            _dataTest.getControlerTest().stop();
                        }
                        catch (Exception ex) {
                            ExceptionHandlerSingleton.instance().display(ex, _view);
                        }
                    }
                });
            }
        });
        // action for editing
        _view.jButtonXML.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ThreadPool.reserve().start(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Utils.openEditor(_dataTest.getPath());
                        }
                        catch (Exception ex) {
                            ExceptionHandlerSingleton.instance().display(ex, _view);
                        }
                    }
                });
            }
        });
        // change runner class
        _view.jComboBoxRunner.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _dataTest.setRunner(_view.jComboBoxRunner.getSelectedItem().toString());
            }
        });
        // change slave address
        _view.jTextFieldSlave.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                _dataTest.setSlave(_view.jTextFieldSlave.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                _dataTest.setSlave(_view.jTextFieldSlave.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                _dataTest.setSlave(_view.jTextFieldSlave.getText());
            }
        });
        // action for report
        _view.jButtonReport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ThreadPool.reserve().start(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ReportGenerator reportGenerator = _dataTest.getControlerTest().report();

                            JPanelReporting jPanelReporting = new JPanelReporting(reportGenerator, reportGenerator.getReportDirectory());
                            JFrameTasksView.instance().addTask(jPanelReporting);
                            JFrameTasksView.instance().setVisible(false);
                            JFrameTasksView.instance().setVisible(true);
                            _view.jButtonReport.setEnabled(false);
                            reportGenerator.addListener(new NotificationListener<Notification<String, ReportStatus>>() {
                                public void notificationReceived(Notification<String, ReportStatus> notification) {
                                    if (100 == notification.getData().getProgress()) {
                                        SwingUtilities.invokeLater(new Runnable() {
                                            public void run() {
                                                _view.jButtonReport.setEnabled(true);
                                            }
                                        });
                                    }
                                }
                            });
                        }
                        catch (Exception ex) {
                            ExceptionHandlerSingleton.instance().display(ex, _view);
                        }
                    }
                });
            }
        });
        // action for connecting
        _view.jToggleButtonConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ThreadPool.reserve().start(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (_view.jToggleButtonConnect.isSelected()) {
                                _dataTest.getControlerTest().connect();
                            }
                            else {
                                _dataTest.getControlerTest().disconnect();
                            }
                        }
                        catch (Exception ex) {
                            ExceptionHandlerSingleton.instance().display(ex, _view);
                        }
                    }
                });
            }
        });
        // action for expanding
        _view.jToggleButtonExpand.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ThreadPool.reserve().start(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (_view.jToggleButtonExpand.isSelected()) {
                                GridBagLayout layout = (GridBagLayout) _view.getParent().getLayout();
                                GridBagConstraints constraints = (GridBagConstraints) layout.getConstraints(_view).clone();
                                constraints.gridy++;

                                if (_testcasePanelCtrl == null) {
                                    TestcasePanelView jPanelTestcasePanelView = new TestcasePanelView();
                                    _testcasePanelCtrl = new TestcasePanelCtrl(jPanelTestcasePanelView, _dataTest.getControlerTest());
                                }

                                _testcasePanelCtrl.getView().setBackground(_view.getBackground());
                                _view.getParent().add(_testcasePanelCtrl.getView(), constraints);
                                _view.getParent().getParent().validate();
                                _view.getParent().getParent().repaint();
                            }
                            else if (!_view.jToggleButtonExpand.isSelected()) {
                                _view.getParent().remove(_testcasePanelCtrl.getView());
                                _view.getParent().getParent().validate();
                                _view.getParent().getParent().repaint();
                                if (_testcasePanelCtrl.disposed()) {
                                    _testcasePanelCtrl = null;
                                }
                            }
                        }
                        catch (Exception ex) {
                            ExceptionHandlerSingleton.instance().display(ex, _view);
                        }
                    }
                });
            }
        });
    }

    public void free() {
        _dataTest.getControlerTest().getNotificationSenderForDeploymentState().removeListener(_deploymentStateListener);
        _dataTest.getControlerTest().getNotificationSenderForConnectionState().removeListener(_connectionStateListener);
        _dataTest.getControlerTest().getNotificationSenderForRunnerState().removeListener(_runnerStateListener);
        try {
            _dataTest.getControlerTest().disconnect();
        }
        catch (Exception e) {
            ExceptionHandlerSingleton.instance().display(e, _view);
        }
    }

    public TestLineView getView() {
        return _view;
    }

    public DataTest getDataTest() {
        return _dataTest;
    }
}
