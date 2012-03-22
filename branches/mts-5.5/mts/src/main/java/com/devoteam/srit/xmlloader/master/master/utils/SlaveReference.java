/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
        _slaveIntf = (SlaveIntf) LocateRegistry.getRegistry(host, port).lookup("imsloader/slave");

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
