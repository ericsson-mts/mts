/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.master.master;

import com.devoteam.srit.xmlloader.master.common.MultiplexedNotification;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author Gwenhael
 */
public interface MultiplexedNotificationsReceiverIntf extends Remote {

    public void notificationReceived(MultiplexedNotification notification) throws RemoteException;
}
