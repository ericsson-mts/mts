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

package com.devoteam.srit.xmlloader.master.slave;

import com.devoteam.srit.xmlloader.core.utils.filesystem.FSInterface;
import com.devoteam.srit.xmlloader.core.Test;
import com.devoteam.srit.xmlloader.core.TestRunner;
import com.devoteam.srit.xmlloader.core.TestRunnerLoad;
import com.devoteam.srit.xmlloader.core.TestRunnerSequential;
import com.devoteam.srit.xmlloader.core.Testcase;
import com.devoteam.srit.xmlloader.core.TestcaseRunner;
import com.devoteam.srit.xmlloader.core.Tester;
import com.devoteam.srit.xmlloader.core.Cache;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.parameters.EditableParameterProviderHashMap;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.URIFactory;
import com.devoteam.srit.xmlloader.core.utils.URIRegistry;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import com.devoteam.srit.xmlloader.master.master.HeartbeatCheckerIntf;
import com.devoteam.srit.xmlloader.master.master.MultiplexedNotificationsReceiverIntf;
import java.net.URI;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author gpasquiers
 */
public class SlaveImpl extends UnicastRemoteObject implements SlaveIntf {
    private final SlaveImpl _this = this;
    private MultiplexedNotificationsReceiverIntf _multiplexedNotificationsReceiver;
    private HashMap<String, String> _oldInitialParametersValues;
    private HeartbeatCheckerIntf _heartbeatChecker;
    private NotificationMultiplexer _notificationMultiplexer;
    private TestRunner _runner;
    private Timer _timer;
    private TimerTask _hearbeatTask = new TimerTask() {
        @Override
        public void run() {
            synchronized (_this) {
                try {
                    if (null != _heartbeatChecker) {
                        _heartbeatChecker.beat();
                    }
                }
                catch (Exception e) {
                    _multiplexedNotificationsReceiver = null;
                    _heartbeatChecker = null;
                    _notificationMultiplexer = null;
                }
            }
        }
    };

    public SlaveImpl() throws RemoteException {
        _multiplexedNotificationsReceiver = null;
        _oldInitialParametersValues = null;
        _heartbeatChecker = null;

        _runner = null;
        _notificationMultiplexer = null;

        _timer = new Timer();
        _timer.schedule(_hearbeatTask, 0, 1000);
    }

    @Override
    public void connect(FSInterface fsInterface, HeartbeatCheckerIntf heartbeatChecker, MultiplexedNotificationsReceiverIntf multiplexedNotificationsReceiverIntf) throws RemoteException {
        // check if there is already a heartbeat checker (assume it's alive because the heartbeat thread did not delete it)
        synchronized (this) {
            _hearbeatTask.run();

            if (null != _heartbeatChecker) {
                // master is still alive and did not invalidate the connection
                throw new RemoteException("Slave already used by a master.");
            }
            else {
                // we can accept a new connection
                SingletonFSInterface.setInstance(new FSInterfaceAdapter(fsInterface));
                _heartbeatChecker = heartbeatChecker;
                _multiplexedNotificationsReceiver = multiplexedNotificationsReceiverIntf;
                _notificationMultiplexer = new NotificationMultiplexer(_multiplexedNotificationsReceiver);
            }
        }
    }

    @Override
    public void beat() throws RemoteException {
    }

    @Override
    public void disconnect() throws RemoteException {
        _hearbeatTask.run();
    }

    @Override
    public Test openTest(URI path, URI MTS_BIN_HOME, String name, String home, HashMap<String, String> initialParametersValues, boolean force) throws RemoteException {
        try {
            URIRegistry.MTS_BIN_HOME = MTS_BIN_HOME;
            _notificationMultiplexer.reset();

            if (null != _runner) {
                _runner.stop();
                _runner.stop();
            }

            _runner = null;

            StackFactory.reset();
            Config.reset();

            if (false == force
                    && null != Tester.getInstance()
                    && null != Tester.getInstance().getTest()
                    && null != Tester.instance().getTestXMLDocument()
                    && Tester.getInstance().getTestXMLDocument().getXMLFile().equals(path)
                    && null != _oldInitialParametersValues
                    && _oldInitialParametersValues.equals(initialParametersValues)) {
                return Tester.instance().getTest();
            }
            else {
                Cache.reset();
                Tester.cleanInstance();
                Tester.buildInstance();
                Tester.instance().open_openFile(path, new EditableParameterProviderHashMap(initialParametersValues));

                Test test = Tester.instance().getTest();

                test.setName(name);

                // override "mts.resources.home"
                if (null != home) {
                    URIRegistry.MTS_CONFIG_HOME = URIFactory.resolve(URIRegistry.MTS_TEST_HOME, home);
                }

                for (Testcase testcase : test.getChildren()) {
                    if (null == testcase.attributeValue("state") || testcase.attributeValue("state").equals("true")) {
                        testcase.parseScenarios();
                    }
                    testcase.getTestcaseRunner().reset();
                }
                _oldInitialParametersValues = initialParametersValues;
                return test;
            }
        }
        catch (Exception e) {
            throw new RemoteException("exception while opening test", e);
        }
    }

    @Override
    public void startTest(Class runnerClass) throws RemoteException {
        try {
            Test test = Tester.instance().getTest();

            if (null == test) {
                throw new RemoteException("Test not set");
            }

            if (runnerClass.equals(TestRunnerSequential.class)) {
                _runner = new TestRunnerSequential(test);
            }
            else if (runnerClass.equals(TestRunnerLoad.class)) {
                _runner = new TestRunnerLoad(test);
            }
            else {
                throw new RemoteException("Unhandled runner class");
            }

            for (TestcaseRunner testcaseRunner : _runner.getChildren()) {
                testcaseRunner.init();
            }

            _runner.start();
        }
        catch (Exception e) {
            throw new RemoteException("Exception while starting TestRunner", e);
        }
    }

    @Override
    public void addMultiplexedListener(final String channelUID, String testName, String testcaseName, String scenarioName) throws RemoteException {
        _notificationMultiplexer.addMultiplexedListener(_runner, channelUID, testName, testcaseName, scenarioName);
    }

    @Override
    public void removeMultiplexedListener(final String channelUID) throws RemoteException {
        _notificationMultiplexer.removeMultiplexedListener(channelUID);
    }

    @Override
    public void stopTest() throws RemoteException {
        try {
            if (null != _runner) {
                _runner.stop();
            }
            else {
                throw new RemoteException("There is no runner, how could you call stop !?");
            }
        }
        catch (Exception e) {
            throw new RemoteException("Error stopping test", e);
        }
    }

    @Override
    public StatPool getStatPool() throws RemoteException {
        return StatPool.getInstance().clone();
    }

    @Override
    public void resetStatPool() throws RemoteException {
        StatPool.getInstance().reset();
    }
}
