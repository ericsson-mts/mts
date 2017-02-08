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

package com.devoteam.srit.xmlloader.master.master.utils;

import com.devoteam.srit.xmlloader.core.RunnerState;
import com.devoteam.srit.xmlloader.core.Test;
import com.devoteam.srit.xmlloader.core.TestRunnerLoad;
import com.devoteam.srit.xmlloader.core.TestRunnerSequential;
import com.devoteam.srit.xmlloader.core.Testcase;
import com.devoteam.srit.xmlloader.core.ThreadPool;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.report.ReportGenerator;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.URIRegistry;
import com.devoteam.srit.xmlloader.core.utils.notifications.DefaultNotificationSender;
import com.devoteam.srit.xmlloader.core.utils.notifications.DefaultNotificationSenderWithCache;
import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import com.devoteam.srit.xmlloader.master.master.utils.states.ConnectionState;
import com.devoteam.srit.xmlloader.master.master.utils.states.DeploymentState;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author gpasquiers
 */
public class ControlerTest {
    // Object representing a test inside of a master and containing informations such as slave, path, and some parameter operations.
    private DataTest _dataTest;
    // Reference to various interfaces related to the slave. This object is null if this controler is not connected to the slave.
    private SlaveReference _slaveReference;
    // Test object as opened on the RemoteTester.
    private Test _test;
    // Map of listeners that act like cache and forwarders
    private HashMap<String, ControlerTestcaseCache> _testcaseStateCache;
    // Timestamp of the last start. This is used with the reporting because all StatPool values are relative to their start date.
    private long _startTimestamp;
    private StatPool _statPoolCache;
    private boolean _resetInhibited;
    private DefaultNotificationSenderWithCache<RunnerState> _notificationSenderForRunnerState;
    private DefaultNotificationSenderWithCache<DeploymentState> _notificationSenderForDeploymentState;
    private DefaultNotificationSenderWithCache<ConnectionState> _notificationSenderForConnectionState;
    private NotificationListener<Notification<String, RunnerState>> _notificationForwarderForRunnerState = new NotificationListener<Notification<String, RunnerState>>() {
        @Override
        public void notificationReceived(Notification<String, RunnerState> notification) {
            _notificationSenderForRunnerState.notifyAll(notification.getData());
        }
    };
    private Timer _beatTimer;
    private TimerTask _beatTimerTask = new TimerTask(){
        @Override
        public void run() {
            synchronized(_this){
                try{
                    if(isConnected()){
                        _slaveReference.getSlaveIntf().beat();
                    }
                }
                catch(Exception e){
                    try{
                        disconnect();
                    }
                    catch(Exception ignore){
                        
                    }
                }
            }
        }
    };
    private ControlerTest _this = this;
    public ControlerTest(DataTest testData) throws Exception {
        _test = null;
        _beatTimer = new Timer();
        _beatTimer.schedule(_beatTimerTask, 0, 1000);
        _slaveReference = null;
        _resetInhibited = false;
        _notificationSenderForRunnerState = new DefaultNotificationSenderWithCache<RunnerState>();
        _notificationSenderForDeploymentState = new DefaultNotificationSenderWithCache<DeploymentState>();
        _notificationSenderForConnectionState = new DefaultNotificationSenderWithCache<ConnectionState>();

        // just init the notification for the listeners that will register before the first notification is ever sent
        _notificationSenderForDeploymentState.notifyAll(new DeploymentState(DeploymentState.UNDEPLOYED));
        _notificationSenderForConnectionState.notifyAll(new ConnectionState(ConnectionState.DISCONNECTED));
        _notificationSenderForRunnerState.notifyAll(new RunnerState());

        _testcaseStateCache = new HashMap<String, ControlerTestcaseCache>();
        _dataTest = testData;
    }

    public DefaultNotificationSenderWithCache<ConnectionState> getNotificationSenderForConnectionState() {
        return _notificationSenderForConnectionState;
    }

    public DefaultNotificationSenderWithCache<DeploymentState> getNotificationSenderForDeploymentState() {
        return _notificationSenderForDeploymentState;
    }

    public DefaultNotificationSenderWithCache<RunnerState> getNotificationSenderForRunnerState() {
        return _notificationSenderForRunnerState;
    }

    public DataTest getDataTest() {
        return _dataTest;
    }

    public synchronized void connect() throws Exception {
        try {
            try {
                if (isConnected()) {
                    _slaveReference.getSlaveIntf().beat();
                }
            }
            catch (Exception e) {
                _slaveReference = null;
            }

            if (!isConnected()) {
                _slaveReference = new SlaveReference(_dataTest.getSlave());
                _notificationSenderForConnectionState.notifyAll(new ConnectionState(ConnectionState.CONNECTED));
            }
        }
        catch (Exception e) {
            _notificationSenderForConnectionState.notifyAll(new ConnectionState(ConnectionState.CONNECTION_FAILURE));
            throw e;
        }
    }

    public synchronized boolean isConnected() throws Exception {
        return _slaveReference != null;
    }

    public synchronized void disconnect() throws Exception {
        if (null != _slaveReference) {
            try {
                _slaveReference.free();
            }
            catch (Exception e) {
                _slaveReference = null;
            }

            _notificationSenderForConnectionState.notifyAll(new ConnectionState(ConnectionState.DISCONNECTED));
        }
    }

    public void open(boolean force) throws Exception {
        try {
            _notificationSenderForDeploymentState.notifyAll(new DeploymentState(DeploymentState.STARTED));
            connect();
            _test = _slaveReference.getSlaveIntf().openTest(_dataTest.getPath(), URIRegistry.MTS_BIN_HOME, _dataTest.getName(), _dataTest.getHome(), _dataTest.getInitialValues(), force);
            _notificationSenderForDeploymentState.notifyAll(new DeploymentState(DeploymentState.SUCCEEDED));
            connectCache();
            resetCache();
            disconnectCache();
        }
        catch (Exception e) {
            _notificationSenderForDeploymentState.notifyAll(new DeploymentState(DeploymentState.FAILED));
            throw e;
        }
    }

    public Test getTest() {
        return _test;
    }

    public synchronized void resetCache() {
        if (null != _test) {
            for (Testcase testcase : _test.getChildren()) {
                String testcaseName = testcase.getName();
                if (_testcaseStateCache.containsKey(testcaseName)) {
                    _testcaseStateCache.get(testcaseName).notifyAll(new RunnerState());
                }
            }
        }
    }

    public synchronized void connectCache() throws Exception {
        connect();
        for (Testcase testcase : _test.getChildren()) {
            String testcaseName = testcase.getName();
            if (!_testcaseStateCache.containsKey(testcaseName)) {
                ControlerTestcaseCache cache = new ControlerTestcaseCache();
                cache.notifyAll(new RunnerState());
                _testcaseStateCache.put(testcase.getName(), cache);
            }
            _slaveReference.getMultiplexedNotificationsReceiverImpl().addMultiplexedListener(_testcaseStateCache.get(testcaseName), _test.getName(), testcaseName, null);
        }
    }

    public synchronized void disconnectCache() throws Exception {
        for (Testcase testcase : _test.getChildren()) {
            String testcaseName = testcase.getName();
            if (_testcaseStateCache.containsKey(testcaseName)) {
                _slaveReference.getMultiplexedNotificationsReceiverImpl().removeMultiplexedListener(_testcaseStateCache.get(testcaseName));
            }
        }
    }

    public synchronized void registerToCache(String name, NotificationListener<RunnerState> listener) {
        if (!_testcaseStateCache.containsKey(name)) {
            ControlerTestcaseCache cache = new ControlerTestcaseCache();
            cache.notifyAll(new RunnerState());
            _testcaseStateCache.put(name, cache);
        }
        _testcaseStateCache.get(name).addListener(listener);
    }

    public synchronized void unregisterFromCache(String name, NotificationListener<RunnerState> listener) {
        if (_testcaseStateCache.containsKey(name)) {
            _testcaseStateCache.get(name).removeListener(listener);
        }
    }

    public long getStartTimestamp() {
        return _startTimestamp;
    }

    /**
     * This method returns the statpool of the remote slave or an exception if
     * there is no slave yet since it means the test was never deployed.
     * If the test stopped then the statpool is cached in this object for further
     * calls to this method.
     * If this method is called during the test execution, the statpool is returned
     * "as is" from the slave through RMI.
     * @return The statpool of the remote slave.
     * @throws java.rmi.RemoteException
     */
    public StatPool getStatPool() throws Exception {
        if (null == _statPoolCache && isConnected()) {
            StatPool pool = _slaveReference.getSlaveIntf().getStatPool();
            pool.setUpdateLastTimestamp(false);
            return pool;
        }
        else if (null == _statPoolCache && !isConnected()) {
            throw new Exception("did not get statpool and not connected to slave so it's impossible to get statpool");
        }
        else {
            return _statPoolCache;
        }
    }

    /**
     * Do all the reporting in a separate thread.
     * This method is asynchronous and returns the ReportGenerator for the caller
     * to register it's own listeners and get updates on the progression.
     * This method already adds a listener that calls the MasterFrame for it to 
     * refresh it's state (open/close menus allowed or not).
     * @return the report generator
     * @throws java.lang.Exception
     */
    public ReportGenerator report() throws Exception {
        final StatPool statpool = this.getStatPool();
        final long zeroTimestamp = _startTimestamp;
        // generate stat report
        String dirName = Config.getConfigByName("tester.properties").getString("stats.REPORT_DIRECTORY", "../reports/");
        String fileName = dirName + "/MASTER_TEST_" + _dataTest.getName();
        final ReportGenerator reportGenerator = new ReportGenerator(fileName);

        ThreadPool.reserve().start(new Runnable() {
            @Override
            public void run() {
                try {
                    reportGenerator.generateReport(statpool, zeroTimestamp);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return reportGenerator;
    }

    public void resetStats(long timestamp) throws Exception {
        if (!_resetInhibited) {
            connect();
            _slaveReference.getSlaveIntf().resetStatPool();
            _startTimestamp = timestamp;
        }
    }

    public void setResetInhibited(boolean value) {
        _resetInhibited = value;
    }

    public void start() throws Exception {
        connect();
        _statPoolCache = null;
        if ("sequential".equalsIgnoreCase(_dataTest.getRunner())) {
            _slaveReference.getSlaveIntf().startTest(TestRunnerSequential.class);
        }
        else if ("load".equalsIgnoreCase(_dataTest.getRunner())) {
            _slaveReference.getSlaveIntf().startTest(TestRunnerLoad.class);
        }
        else {
            throw new RemoteException("Invalid runner name: " + _dataTest.getRunner());
        }

        _slaveReference.getMultiplexedNotificationsReceiverImpl().addMultiplexedListener(_notificationForwarderForRunnerState, _test.getName(), null, null);
        connectCache();

        // plannify cache disconnection
        _slaveReference.getMultiplexedNotificationsReceiverImpl().addMultiplexedListener(new NotificationListener<Notification<String, RunnerState>>() {
            @Override
            public void notificationReceived(Notification<String, RunnerState> notification) {
                if (notification.getData().couldNotStart() || notification.getData().isFinished()) {
                    try {
                        _statPoolCache = _slaveReference.getSlaveIntf().getStatPool();
                        disconnectCache();
                        disconnect();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, _test.getName(), null, null);

    }

    public void stop() throws RemoteException {
        _slaveReference.getSlaveIntf().stopTest();
    }
}
