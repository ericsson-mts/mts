/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.master.slave;

import com.devoteam.srit.xmlloader.core.Test;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.utils.filesystem.FSInterface;
import com.devoteam.srit.xmlloader.master.master.HeartbeatCheckerIntf;
import com.devoteam.srit.xmlloader.master.master.MultiplexedNotificationsReceiverIntf;
import java.net.URI;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

/**
 *
 * @author gpasquiers
 */
public interface SlaveIntf extends Remote {
    
    public void connect(FSInterface fsInterface, HeartbeatCheckerIntf heartbeatChecker, MultiplexedNotificationsReceiverIntf multiplexedNotificationsReceiverIntf) throws RemoteException;

    public void beat() throws RemoteException;
    
    public void disconnect() throws RemoteException;
    
    public Test openTest(URI path, URI IMSLOADER_BIN, String name, String home, HashMap<String, String> initialParametersValues, boolean force) throws RemoteException;

    public void startTest(Class runnerClass) throws RemoteException;
    
    public void stopTest() throws RemoteException;

    public void resetStatPool() throws RemoteException;

    public StatPool getStatPool() throws RemoteException;
    
    public void addMultiplexedListener(final String channelUID, String testName, String testcaseName, String scenarioName) throws RemoteException;

    public void removeMultiplexedListener(String channelUID) throws RemoteException;
}
