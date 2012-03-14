/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.master.master;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author Gwenhael
 */
public interface HeartbeatCheckerIntf extends Remote {
    public void beat() throws RemoteException;
}
