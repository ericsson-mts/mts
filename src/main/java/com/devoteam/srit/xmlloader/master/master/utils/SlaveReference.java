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

import com.devoteam.srit.xmlloader.master.master.FSInterfaceImpl;
import com.devoteam.srit.xmlloader.master.master.HeartbeatCheckerImpl;
import com.devoteam.srit.xmlloader.master.master.MultiplexedNotificationsReceiverImpl;
import com.devoteam.srit.xmlloader.master.slave.SlaveIntf;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 *
 * @author Gwenhael
 */
public class SlaveReference {

    private SlaveIntf _slaveIntf;
    private HeartbeatCheckerImpl _heartbeatCheckerImpl;
    private MultiplexedNotificationsReceiverImpl _multiplexedNotificationsReceiverImpl;
    private FSInterfaceImpl _fsInterfaceImpl;

    protected SlaveReference(String url) throws RemoteException, NotBoundException {
        int port = 2099;
        String host;
        if (url.contains(":")) {
            host = url.split(":")[0];
            port = Integer.parseInt(url.split(":")[1]);
        }
        else {
            host = url;
        }

        // get the slave interface
        _slaveIntf = (SlaveIntf) LocateRegistry.getRegistry(host, port).lookup("mts/slave");

        // try to initialize it
        _fsInterfaceImpl = new FSInterfaceImpl();
        _heartbeatCheckerImpl = new HeartbeatCheckerImpl();
        _multiplexedNotificationsReceiverImpl = new MultiplexedNotificationsReceiverImpl(_slaveIntf);
        _slaveIntf.connect(_fsInterfaceImpl, _heartbeatCheckerImpl, _multiplexedNotificationsReceiverImpl);
    }

    public SlaveIntf getSlaveIntf() {
        return _slaveIntf;
    }

    public MultiplexedNotificationsReceiverImpl getMultiplexedNotificationsReceiverImpl() {
        return _multiplexedNotificationsReceiverImpl;
    }

    public void free() {
        try {
            _multiplexedNotificationsReceiverImpl.free();
            _heartbeatCheckerImpl.invalidate();
            _slaveIntf.disconnect();
        }
        catch (Exception e) {
            // should happen ! ! !
            //e.printStackTrace();
        }
        finally {
            _slaveIntf = null;
            _heartbeatCheckerImpl = null;
            _multiplexedNotificationsReceiverImpl = null;
            _fsInterfaceImpl = null;
        }
    }
}
