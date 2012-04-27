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

package com.devoteam.srit.xmlloader.cmd;

import com.devoteam.srit.xmlloader.core.RunnerState;
import com.devoteam.srit.xmlloader.core.ThreadPool;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.report.ReportGenerator;
import com.devoteam.srit.xmlloader.core.report.ReportStatus;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.URIFactory;
import com.devoteam.srit.xmlloader.core.utils.XMLDocument;
import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationSender;
import com.devoteam.srit.xmlloader.master.Master;
import com.devoteam.srit.xmlloader.master.master.utils.DataMaster;
import com.devoteam.srit.xmlloader.master.master.utils.DataTest;
import java.net.URI;
import java.util.concurrent.Semaphore;

/**
 *
 * @author
 * gpasquiers
 */
public class MasterTextTester {

    private Semaphore _semaphoreIntern;
    private final MasterTextTester _this = this;
    private String _mode;
    private DataMaster _dataMaster;

    public MasterTextTester(URI uri, String mode) throws Exception {
        try {
            XMLDocument xmlDocument = new XMLDocument();
            xmlDocument.setXMLFile(uri);
            xmlDocument.setXMLSchema(URIFactory.newURI("../conf/schemas/master.xsd"));
            xmlDocument.parse();

            _dataMaster = new DataMaster(xmlDocument, URIFactory.resolve(uri, "."));

            _semaphoreIntern = new Semaphore(0);
            _mode = mode;
        }
        catch (Exception e) {
            throw e;
        }
    }

    public void run() throws Exception {
        Master.init();

        try {
            if (_mode.equalsIgnoreCase("sequential")) {
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

                for (DataTest dataTest : _dataMaster.getDataTests()) {
                    dataTest.getControlerTest().open(true);
                    dataTest.getControlerTest().start();
                    final Semaphore semaphoreSeq = new Semaphore(0);
                    final NotificationSender<RunnerState> sender = dataTest.getControlerTest().getNotificationSenderForRunnerState();
                    sender.addListener(new NotificationListener<RunnerState>() {

                        boolean _couldBeFinished = false;

                        @Override
                        public void notificationReceived(RunnerState notification) {
                            System.out.println(notification);
                            if (_couldBeFinished && (notification.isFinished() || notification.couldNotStart())) {
                                sender.removeListener(this);
                                semaphoreSeq.release();
                            }
                            else {
                                _couldBeFinished = true;
                            }
                        }
                    });
                    try {
                        // wait for the test to end
                        semaphoreSeq.acquire();
                        _semaphoreIntern.release();
                        Thread.sleep(500);
                    }
                    catch (Exception e) {
                        throw e;
                    }
                }
            }
            else { //default: simultaneous
                try {
                    for (DataTest dataTest : _dataMaster.getDataTests()) {
                        dataTest.getControlerTest().connect();
                    }

                    for (DataTest dataTest : _dataMaster.getDataTests()) {
                        dataTest.getControlerTest().open(true);
                    }

                    long timestamp = System.currentTimeMillis();
                    for (DataTest dataTest : _dataMaster.getDataTests()) {
                        dataTest.getControlerTest().resetStats(timestamp);

                        // add a listener that will give a token to the semaphore at each test's end
                        final NotificationSender<RunnerState> sender = dataTest.getControlerTest().getNotificationSenderForRunnerState();
                        sender.addListener(new NotificationListener<RunnerState>() {

                            boolean _couldBeFinished = false;

                            @Override
                            public void notificationReceived(RunnerState notification) {
                                System.out.println(notification);
                                if (_couldBeFinished && (notification.isFinished() || notification.couldNotStart())) {
                                    sender.removeListener(this);
                                    _semaphoreIntern.release();
                                }
                                else {
                                    _couldBeFinished = true;
                                }
                            }
                        });
                    }

                    for (DataTest dataTest : _dataMaster.getDataTests()) {
                        dataTest.getControlerTest().start();
                    }
                }
                catch (Exception e) {
                    throw e;
                }
            }

            _semaphoreIntern.acquireUninterruptibly(_dataMaster.getDataTests().size());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("run ended");
    }

    public void report() throws Exception {
        System.out.println("Start generating report.");
        // check slaves are synched
        long timestamp = Long.MIN_VALUE;
        long earliest = 0;
        long latest = 0;
        for (DataTest dataTest : _dataMaster.getDataTests()) {
            if (earliest == 0) {
                earliest = dataTest.getControlerTest().getStartTimestamp();
                latest = earliest;
            }
            else {
                earliest = Math.min(earliest, dataTest.getControlerTest().getStartTimestamp());
                latest = Math.max(latest, dataTest.getControlerTest().getStartTimestamp());
            }
        }

        if (latest - earliest > 1000) {
            System.out.println("The slaves test pools were initialized up to " + (latest - earliest) / 1000 + "s apart.\nNo merged report will be generated");
            return;
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
        final Semaphore semaphore = new Semaphore(0);
        reportGenerator.addListener(new NotificationListener<Notification<String, ReportStatus>>() {

            @Override
            public void notificationReceived(Notification<String, ReportStatus> notification) {
                System.out.println(notification.getData().getProgress());
                if (100 == notification.getData().getProgress()) {
                    semaphore.release();
                }
            }
        });
        semaphore.acquire();
        System.out.println("Report generated.");
    }
    
    public RunnerState getGlobalState(){
        RunnerState finalState = null;
        for (DataTest dataTest : _dataMaster.getDataTests()) {
            RunnerState state = dataTest.getControlerTest().getNotificationSenderForRunnerState().getLastNotification();
            if(finalState == null){
                finalState = state.clone();
            }
            else{
                finalState.merge(state);
            }
        }
        
        return finalState;
    }
}
