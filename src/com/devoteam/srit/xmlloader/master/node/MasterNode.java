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
*//*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.master.node;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.notifications.DefaultNotificationSender;
import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationSender;
import com.devoteam.srit.xmlloader.master.SlaveInterface;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author gpasquiers
 */
public class MasterNode implements NodeInterface, NotificationSender<Notification<MasterNode, MasterNodeUpdate>>
{
    private HashMap<NodeIdentifier, NodeParameters> slavesNodesParameters;
    private HashMap<NodeIdentifier, SlaveInterface> slavesNodesStubs;
    
    private NodeParameters localNodeParameters;
    
    private Timer timer;
    
    public MasterNode(NodeParameters localNodeParameters)
    {
        this.localNodeParameters = localNodeParameters;
        this.slavesNodesStubs = new HashMap<NodeIdentifier, SlaveInterface>();
        this.slavesNodesParameters = new HashMap<NodeIdentifier, NodeParameters>();
        this.defaultNotificationSender = new DefaultNotificationSender<Notification<MasterNode, MasterNodeUpdate>>();
        //this.unfinalizedConnections = new HashSet<NodeIdentifier>();
        
        this.timer = new Timer(true);
        this.timer.schedule(new TimerTask(){

            @Override
            public void run()
            {
                try
                {
                    for(NodeIdentifier nodeIdentifier:slavesNodesParameters.keySet())
                    {
                        try
                        {
                            SlaveInterface slaveInterface = slavesNodesStubs.get(nodeIdentifier);
                            slaveInterface.isAlive();
                        }
                        catch(Throwable t)
                        {
                            // there was an exception so the slave is not connected anymore
                            closeConnection(nodeIdentifier, false);
                        }
                    }
                }
                catch (Throwable t)
                {
                    System.out.println("timertask:" + t.toString());
                }
            }
        }, 1000, 10000);
    }
    
    public void connectToSlave(NodeParameters slaveNodeParameters) throws RemoteException, NotBoundException
    {
        this.getSlaveStub(slaveNodeParameters).initConnection(slaveNodeParameters, this.localNodeParameters);
    }
    
    private SlaveInterface getSlaveStub(NodeParameters slaveNodeParameters) throws RemoteException, NotBoundException
    {
        Registry registry = LocateRegistry.getRegistry(slaveNodeParameters.getHost(), slaveNodeParameters.getPort());
        return (SlaveInterface) registry.lookup(slaveNodeParameters.getStub());
    }

    public void initConnection(NodeParameters slaveNodeParameters, NodeParameters masterNodeParameters) throws RemoteException
    {
        throw new RemoteException("Not supported. No one inits something with the master, the master inits everything.");
    }

    public void finalizeConnection(NodeParameters slaveNodeParameters) throws RemoteException
    {
        try
        {
            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "Slave ", slaveNodeParameters, " is finalizing the connection");

            //TODO: use a hashSet of NodeIdentifier to verify that an authroized slave call finalizeConnection()
            SlaveInterface slaveInterface = this.getSlaveStub(slaveNodeParameters);

            GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "Slave ", slaveNodeParameters, " is now aboard");
            
            synchronized (this)
            {
                this.slavesNodesParameters.put(slaveNodeParameters.getNodeIdentifier(), slaveNodeParameters);
                this.slavesNodesStubs.put(slaveNodeParameters.getNodeIdentifier(), slaveInterface);

                //
                // Update listeners of a change in the hashmap
                //
                MasterNodeUpdate masterNodeUpdate = new MasterNodeUpdate(MasterNodeUpdate.SLAVE_CONNECTED, slaveNodeParameters.getNodeIdentifier());
                this.notifyAll(new Notification<MasterNode, MasterNodeUpdate>(this, masterNodeUpdate));
            }
        }
        catch (Exception e)
        {
            throw new RemoteException("exception while finalizing connection", e);
        }
    }

    public void closeConnection(NodeIdentifier disconnectedNodeIdentifier) throws RemoteException
    {
        this.closeConnection(disconnectedNodeIdentifier, true);
    }
    
    public void closeConnection(NodeIdentifier disconnectedNodeIdentifier, boolean notifySlave) throws RemoteException
    {
        synchronized (this)
        {
            if(this.slavesNodesParameters.containsKey(disconnectedNodeIdentifier))
            {
                NodeParameters disconnectedNodeParameters = this.slavesNodesParameters.remove(disconnectedNodeIdentifier);
                SlaveInterface slaveInterface = this.slavesNodesStubs.remove(disconnectedNodeIdentifier);
                
                //
                // Update listeners of a change in the hashmap
                //
                MasterNodeUpdate masterNodeUpdate = new MasterNodeUpdate(MasterNodeUpdate.SLAVE_DISCONNECTED, disconnectedNodeIdentifier);
                this.notifyAll(new Notification<MasterNode, MasterNodeUpdate>(this, masterNodeUpdate));
    
                GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "Slave ", disconnectedNodeParameters, " is now disconnected, notified=", notifySlave);

                if(notifySlave)
                {
                    slaveInterface.closeConnection(this.localNodeParameters.getNodeIdentifier());
                }
            }
        }
    }
    
    public boolean isAlive() throws RemoteException
    {
        return true;
    }
    
    public NodeParameters getSlaveNodesParameters(NodeIdentifier nodeIdentifier)
    {
        synchronized(this)
        {
            return slavesNodesParameters.get(nodeIdentifier);
        }
    }

    public SlaveInterface getSlaveInterface(NodeIdentifier nodeIdentifier)
    {
        synchronized(this)
        {
            return slavesNodesStubs.get(nodeIdentifier);
        }
    }

    public NodeParameters getLocalNodeParameters()
    {
        return localNodeParameters;
    }

    // <editor-fold desc="NotificationSender Implementation" defaultstate="collapsed">>
    private DefaultNotificationSender<Notification<MasterNode, MasterNodeUpdate>> defaultNotificationSender;

    public void addListener(NotificationListener listener)
    {
        this.defaultNotificationSender.addListener(listener);

        for(NodeIdentifier nodeIdentifier:this.slavesNodesParameters.keySet())
        {
            MasterNodeUpdate masterNodeUpdate = new MasterNodeUpdate(MasterNodeUpdate.SLAVE_CONNECTED, nodeIdentifier);
            listener.notificationReceived(new Notification<MasterNode, MasterNodeUpdate>(this, masterNodeUpdate));
        }
    }

    public void removeListener(NotificationListener listener)
    {
        this.defaultNotificationSender.removeListener(listener);
    }

    public void notifyAll(Notification<MasterNode, MasterNodeUpdate> notification)
    {
        this.defaultNotificationSender.notifyAll(notification);
    }
    // </editor-fold>
}
