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
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.report.ReportGenerator;
import com.devoteam.srit.xmlloader.core.report.ReportStatus;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.URIFactory;
import com.devoteam.srit.xmlloader.core.utils.URIRegistry;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLDocument;
import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationSender;
import com.devoteam.srit.xmlloader.gui.frames.JFrameAbout;
import com.devoteam.srit.xmlloader.gui.frames.JPanelReporting;
import com.devoteam.srit.xmlloader.gui.logs.GUITextListenerProvider;
import com.devoteam.srit.xmlloader.master.master.utils.DataMaster;
import com.devoteam.srit.xmlloader.master.master.utils.DataTest;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.util.concurrent.Semaphore;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author Gwenhael
 */
public class JFrameMasterCtrl {
    // static
    private static JFrameMasterCtrl _instance;

    public static JFrameMasterCtrl getInstance() {
        return _instance;
    }
    // instanciated
    private JFrameMasterView _view;
    private TestPanelCtrl _testPanelCtrl;
    private JFrameMasterCtrlMenuRecents _jMenuRecentsCtrl;
    private int _numberActive;
    private boolean _somethingActive;
    private boolean _somethingOpened;
    private URI _lastOpenedFile;
    private DataMaster _dataMaster;

    public JFrameMasterCtrl(JFrameMasterView view) {
        _view = view;
        _testPanelCtrl = null;
        _lastOpenedFile = null;
        _dataMaster = null;
        _numberActive = 0;
        _somethingOpened = false;
        _somethingActive = false;

        _view.jScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        _jMenuRecentsCtrl = new JFrameMasterCtrlMenuRecents(_view.jMenuRecents, _view.jMenuItemClear, _view.jMenuItemClear, _view.jSeparatorRecents, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                open(URIFactory.create(((javax.swing.JMenuItem) e.getSource()).getText()));
            }
        });

        // open a master file
        _view.jMenuItemOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                if (null != _lastOpenedFile) {
                    jFileChooser.setCurrentDirectory(new File(_lastOpenedFile));
                }
                else {
                    jFileChooser.setCurrentDirectory(new File(URIRegistry.MTS_BIN_HOME));
                }
                int returnVal = jFileChooser.showOpenDialog(_view);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    URI file = jFileChooser.getSelectedFile().toURI();

                    open(file);
                }
            }
        });

        // close current master file
        _view.jMenuItemClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });

        // reload current master file
        _view.jMenuItemReload.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
                open(_lastOpenedFile);
            }
        });

        // show logs
        _view.jMenuItemMasterLogs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GUITextListenerProvider.instance().getJFrameLogsApplication().setVisible(false);
                GUITextListenerProvider.instance().getJFrameLogsApplication().setVisible(true);
            }
        });

        // show tasks
        _view.jMenuItemTasks.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrameTasksView.instance().setVisible(false);
                JFrameTasksView.instance().setVisible(true);
            }
        });

        // show about
        _view.jMenuItemAbout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrameAbout jFrameAbout = new JFrameAbout();
                jFrameAbout.setVisible(true);
            }
        });

        // deploy all tests
        _view.jMenuItemDeploy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (TestLineCtrl testLineCtrl : _testPanelCtrl.getTestLineCtrls()) {
                    testLineCtrl.getView().jButtonDeploy.doClick(0);
                }
            }
        });

        // start sequentially
        _view.jMenuItemStartSequential.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ThreadPool.reserve().start(new Runnable() {
                    @Override
                    public void run() {

                        // try to connect all slave
                        try {
                            for (DataTest dataTest : _dataMaster.getDataTests()) {
                                dataTest.getControlerTest().connect();
                            }

                            long timestamp = System.currentTimeMillis();
                            for (DataTest dataTest : _dataMaster.getDataTests()) {
                                dataTest.getControlerTest().setResetInhibited(false);
                                dataTest.getControlerTest().resetStats(timestamp);
                                dataTest.getControlerTest().setResetInhibited(true);
                            }
                        }
                        catch (Exception e) {
                            // nothing to do, could not connect to all slave, the stats will not be synched
                        }

                        for (TestLineCtrl testLineCtrl : _testPanelCtrl.getTestLineCtrls()) {
                            testLineCtrl.getView().jButtonRun.doClick(0);
                            final Semaphore semaphore = new Semaphore(0);
                            final NotificationSender<RunnerState> sender = testLineCtrl.getDataTest().getControlerTest().getNotificationSenderForRunnerState();
                            sender.addListener(new NotificationListener<RunnerState>() {
                                boolean _couldBeFinished = false;

                                @Override
                                public void notificationReceived(RunnerState notification) {
                                    if (_couldBeFinished && (notification.isFinished() || notification.couldNotStart())) {
                                        sender.removeListener(this);
                                        semaphore.release();
                                    }
                                    else {
                                        _couldBeFinished = true;
                                    }
                                }
                            });
                            try {
                                // wait for the test to end
                                semaphore.acquire();
                                Thread.sleep(500);
                            }
                            catch (Exception e) {
                                // should not happen
                                e.printStackTrace();
                            }
                        }
                        for (DataTest dataTest : _dataMaster.getDataTests()) {
                            dataTest.getControlerTest().setResetInhibited(false);
                        }
                    }
                });
            }
        });

        // start all
        _view.jMenuItemStartSimultaneous.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ThreadPool.reserve().start(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            for (DataTest dataTest : _dataMaster.getDataTests()) {
                                dataTest.getControlerTest().connect();
                            }

                            long timestamp = System.currentTimeMillis();
                            for (DataTest dataTest : _dataMaster.getDataTests()) {
                                dataTest.getControlerTest().resetStats(timestamp);
                            }

                            for (TestLineCtrl testLineCtrl : _testPanelCtrl.getTestLineCtrls()) {
                                testLineCtrl.getView().jButtonRun.doClick(0);
                            }
                        }
                        catch (Exception e) {
                            Utils.showError(_view, e);
                        }
                    }
                });
            }
        });

        // report all
        _view.jMenuItemReport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ThreadPool.reserve().start(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // check slaves are synched
                            long timestamp = Long.MIN_VALUE;
                            long earliest = 0;
                            long latest = 0;
                            for (DataTest dataTest : _dataMaster.getDataTests()) {
                                if (earliest == 0) {
                                    earliest = dataTest.getControlerTest().getStartTimestamp();
                                    latest = earliest;
                                }
                                else{
                                    earliest = Math.min(earliest, dataTest.getControlerTest().getStartTimestamp());
                                    latest = Math.max(latest, dataTest.getControlerTest().getStartTimestamp());
                                }
                            }
                            
                            if(latest - earliest > 1000){
                                int res = JOptionPane.showConfirmDialog(_view, "The slaves test pools were initialized up to " + (latest - earliest)/1000 + "s apart.\nMerge stat pools anyway ?", "Merge stat pools anyway ?", JOptionPane.YES_NO_OPTION);
                                if(res == JOptionPane.NO_OPTION){
                                    return;
                                }
                            }
                                    

                            // compute merges stat pool
                            StatPool statPool = null;
                            for (DataTest dataTest : _dataMaster.getDataTests()) {
                                if (null == statPool) {
                                    statPool = dataTest.getControlerTest().getStatPool().clone();
                                }
                                else {
                                    statPool.merge(dataTest.getControlerTest().getStatPool());
                                }
                            }

                            final StatPool finalStatPool = statPool;
                            final long finalTimestamp = timestamp;
                            // generate stat report
                            String dirName = Config.getConfigByName("tester.properties").getString("stats.REPORT_DIRECTORY", "../reports/");
                            String fileName = dirName + "/MASTER_TEST";
                            final ReportGenerator reportGenerator = new ReportGenerator(fileName);

                            ThreadPool.reserve().start(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        reportGenerator.generateReport(finalStatPool, finalTimestamp);
                                    }
                                    catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            JPanelReporting jPanelReporting = new JPanelReporting(reportGenerator, reportGenerator.getReportDirectory());
                            JFrameTasksView.instance().addTask(jPanelReporting);
                            JFrameTasksView.instance().setVisible(false);
                            JFrameTasksView.instance().setVisible(true);
                            _view.jMenuItemReport.setEnabled(false);
                            reportGenerator.addListener(new NotificationListener<Notification<String, ReportStatus>>() {
                                @Override
                                public void notificationReceived(Notification<String, ReportStatus> notification) {
                                    if (100 == notification.getData().getProgress()) {
                                        SwingUtilities.invokeLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                _view.jMenuItemReport.setEnabled(true);
                                            }
                                        });
                                    }
                                }
                            });

                        }
                        catch (Exception e) {
                            Utils.showError(_view, e);
                        }
                    }
                });
            }
        });

        _view.setPreferredSize(new Dimension(800, 540));
        _view.setSize(new Dimension(800, 540));
        _view.setVisible(true);

        _instance = this;

        _jMenuRecentsCtrl.openLatest();
    }

    public void open(URI file) {
        close();

        Config.reset();

        _jMenuRecentsCtrl.addToRecents(file.toString());

        try {
            XMLDocument xmlDocument = new XMLDocument();
            xmlDocument.setXMLFile(file);
            xmlDocument.setXMLSchema(URIFactory.newURI("../conf/schemas/master.xsd"));
            xmlDocument.parse();
            _dataMaster = new DataMaster(xmlDocument, URIFactory.resolve(file, "."));
            TestPanelView jPanelTestPanelView = new TestPanelView();
            _testPanelCtrl = new TestPanelCtrl(jPanelTestPanelView, _dataMaster);

            _view.jScrollPane.setViewportView(jPanelTestPanelView);
            _lastOpenedFile = file;
            setSomethingOpened(true);
        }
        catch (Exception e) {
            Utils.showError(_view, "Error while parsing xml file", e);
            return;
        }

    }

    public void close() {
        if (null != _testPanelCtrl) {
            _testPanelCtrl.free();
            _testPanelCtrl = null;
            _view.jScrollPane.setViewportView(null);
            setSomethingOpened(false);
        }
    }

    public synchronized void somethingActivated() {
        _numberActive++;
        _somethingActive = _numberActive > 0;
        updateMenus();
    }

    public synchronized void somethingDeactivated() {
        _numberActive--;
        _somethingActive = _numberActive > 0;
        updateMenus();
    }

    public void setSomethingOpened(boolean somethingOpened) {
        _somethingOpened = somethingOpened;
        updateMenus();
    }

    public void doClickReport(){
        _view.jMenuItemReport.doClick(0);
    }

    private void updateMenus() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                _view.jMenuMaster.setEnabled(_somethingOpened);
                _view.jMenuItemDeploy.setEnabled(!_somethingActive);
                _view.jMenuItemStartSequential.setEnabled(!_somethingActive);
                _view.jMenuItemStartSimultaneous.setEnabled(!_somethingActive);

                _view.jMenuItemClose.setEnabled(!_somethingActive && _somethingOpened);
                _view.jMenuItemReload.setEnabled(!_somethingActive && _somethingOpened);

                _view.jMenuItemOpen.setEnabled(!_somethingActive);
                _view.jMenuRecents.setEnabled(!_somethingActive);
            }
        });
    }
}
