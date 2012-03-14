/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.master.master;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 *
 * @author Gwenhael
 */
public class HeartbeatCheckerImpl extends UnicastRemoteObject implements HeartbeatCheckerIntf {
    private boolean _invalidated;
    
    public HeartbeatCheckerImpl() throws RemoteException {
        _invalidated = false;
    }

    public void invalidate(){
        _invalidated = true;
    }
    
    @Override
    public void beat() throws RemoteException {
        if(_invalidated){
            throw new RemoteException("heartbeat checked was manually invalidated");
        }
    }
}
