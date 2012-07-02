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

package com.devoteam.srit.xmlloader.master.masterutils;

import com.devoteam.srit.xmlloader.core.utils.notifications.DefaultNotificationSender;
import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationSender;
import com.devoteam.srit.xmlloader.master.MasterImplementation;
import com.devoteam.srit.xmlloader.master.node.MasterNode;
import com.devoteam.srit.xmlloader.master.node.MasterNodeUpdate;
import com.devoteam.srit.xmlloader.master.node.NodeIdentifier;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author gpasquiers
 */
public class SlavesStatusSingleton implements NotificationListener<Notification<MasterNode, MasterNodeUpdate>>,
                                              NotificationSender<Notification<SlavesStatusSingleton, SlavesStatusUpdate>>
{
    static private SlavesStatusSingleton instance = null;
    
    static public SlavesStatusSingleton instance()
    {
        if(null == instance)
        {
            instance = new SlavesStatusSingleton();
        }
        
        return instance;
    }
    
    private HashSet<NodeIdentifier> freeNodes;
    private HashSet<NodeIdentifier> reservedNodes;
    
    private SlavesStatusSingleton()
    {
        this.freeNodes = new HashSet<NodeIdentifier>();
        this.reservedNodes = new HashSet<NodeIdentifier>();
        this.defaultNotificationSender = new DefaultNotificationSender<Notification<SlavesStatusSingleton, SlavesStatusUpdate>>();
        MasterImplementation.instance().getMasterNode().addListener(this);
    }

    public void notificationReceived(Notification<MasterNode, MasterNodeUpdate> notification)
    {
        NodeIdentifier nodeIdentifier = notification.getData().getNodeIdentifier();
        
        switch (notification.getData().getType())
        {
            case MasterNodeUpdate.SLAVE_CONNECTED:
                synchronized (this)
                {
                    this.freeNodes.add(nodeIdentifier);
                }
                sendNotification(SlavesStatusUpdate.SLAVE_CONNECTED, nodeIdentifier);
                break;
            case MasterNodeUpdate.SLAVE_DISCONNECTED:
                synchronized (this)
                {
                    if(this.freeNodes.remove(nodeIdentifier)) { }
                    else if(!this.reservedNodes.remove(nodeIdentifier)) { }
                    else
                    {
                        //some error
                    }
                }
                sendNotification(SlavesStatusUpdate.SLAVE_DISCONNECTED, nodeIdentifier);
                break;
            case MasterNodeUpdate.SLAVE_UPDATED:
                sendNotification(SlavesStatusUpdate.SLAVE_UPDATED, nodeIdentifier);
                break;
        }
    }

    public Iterator<NodeIdentifier> getFreeSlaves()
    {
        return this.freeNodes.iterator();
    }
    
    public void reserveSlave(NodeIdentifier nodeIdentifier) throws Exception
    {
        synchronized(this)
        {
            if(this.freeNodes.remove(nodeIdentifier))
            {
                this.reservedNodes.add(nodeIdentifier);
            }
            else
            {
                throw new Exception("could not reserve slave " + nodeIdentifier + "not in free list");
            }
        }
        sendNotification(SlavesStatusUpdate.SLAVE_RESERVED, nodeIdentifier);
    }
    
    synchronized public void freeSlave(NodeIdentifier nodeIdentifier) throws Exception
    {
        synchronized(this)
        {
            if(this.reservedNodes.remove(nodeIdentifier))
            {
                this.freeNodes.add(nodeIdentifier);
            }
            else
            {
                throw new Exception("could not free slave " + nodeIdentifier + ": not in reserved list");
            }
        }
        sendNotification(SlavesStatusUpdate.SLAVE_RELEASED, nodeIdentifier);
    }

    synchronized public void freeAllSlaves() throws Exception
    {
        while(!this.reservedNodes.isEmpty())
        {
            this.freeSlave(this.reservedNodes.iterator().next());
        }
    }
    
    public boolean isReserved(NodeIdentifier nodeIdentifier)
    {
        synchronized(this)
        {
            return this.reservedNodes.contains(nodeIdentifier);
        }
    }
    
    public boolean isFree(NodeIdentifier nodeIdentifier)
    {
        synchronized(this)
        {
            return this.freeNodes.contains(nodeIdentifier);
        }
    }
    
    private void sendNotification(int event, NodeIdentifier nodeIdentifier)
    {
        SlavesStatusUpdate slavesStatusUpdate = new SlavesStatusUpdate(event, nodeIdentifier);
        Notification<SlavesStatusSingleton,SlavesStatusUpdate> notification;
        notification = new Notification<SlavesStatusSingleton,SlavesStatusUpdate>(this, slavesStatusUpdate);
        this.notifyAll(notification);
    }
    
    // <editor-fold desc="NotificationSender Implementation">
    private DefaultNotificationSender<Notification<SlavesStatusSingleton, SlavesStatusUpdate>> defaultNotificationSender;

    public void addListener(NotificationListener listener)
    {
        this.defaultNotificationSender.addListener(listener);

        synchronized(this)
        {
            for(NodeIdentifier nodeIdentifier:this.freeNodes)
            {
                sendNotification(SlavesStatusUpdate.SLAVE_CONNECTED, nodeIdentifier);
            }
        }
    }

    public void removeListener(NotificationListener listener)
    {
        this.defaultNotificationSender.removeListener(listener);
    }

    public void notifyAll(Notification<SlavesStatusSingleton, SlavesStatusUpdate> notification)
    {
        this.defaultNotificationSender.notifyAll(notification);
    }
    // </editor-fold>
}
