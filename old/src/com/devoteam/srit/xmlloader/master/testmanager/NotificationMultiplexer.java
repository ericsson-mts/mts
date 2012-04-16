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

package com.devoteam.srit.xmlloader.master.testmanager;

import com.devoteam.srit.xmlloader.core.ScenarioRunner;
import com.devoteam.srit.xmlloader.core.TestRunner;
import com.devoteam.srit.xmlloader.core.TestcaseRunner;
import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationSender;
import com.devoteam.srit.xmlloader.master.MasterInterface;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 *
 * @author gpasquiers
 */
public class NotificationMultiplexer {
    private MasterInterface masterInterface;
    
    private HashMap<String, NotificationListener> listeners;
    private HashMap<String, NotificationSender> senders;
    
    public NotificationMultiplexer(MasterInterface masterInterface)
    {
        this.masterInterface = masterInterface;
        this.listeners = new HashMap();
        this.senders = new HashMap();
    }
    
    public void removeMultiplexedListener(String channelUID) throws RemoteException
    {
        NotificationSender sender = this.senders.remove(channelUID);
        NotificationListener listener = this.listeners.remove(channelUID);
        
        if(null == sender || null == listener)
        {
            throw new RemoteException("Error: sender=" + sender +" and listener=" + listener + " for ChannelUID " + channelUID);
        }
        else
        {
            sender.removeListener(listener);
        }
    }
        
    public void addMultiplexedListener(TestRunner runner, final String channelUID, String testName, String testcaseName, String scenarioName) throws RemoteException
    {
        if(null == runner)
        {
            throw new RemoteException("Could not register to notification sender, null runner");
        }

        NotificationSender notificationSender = null;
        
        if (null != testName)
        {
            if (runner.getName().equals(testName))
            {
                notificationSender = runner;
                if (null != testcaseName)
                {
                    notificationSender = null;
                    for (TestcaseRunner testcaseRunner : runner.getChildren())
                    {
                        if (testcaseRunner.getName().equals(testcaseName))
                        {
                            notificationSender = testcaseRunner;
                            if (null != scenarioName)
                            {
                                notificationSender = null;
                                for (ScenarioRunner scenarioRunner : testcaseRunner.getChildren())
                                {
                                    if (scenarioRunner.getName().equals(scenarioName))
                                    {
                                        notificationSender = scenarioRunner;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if(null == notificationSender)
        {
            throw new RemoteException("Could not register to notification sender " + testName + "/" + testcaseName + "/" + scenarioName);
        }
        
        NotificationListener listener = new NotificationListener<Notification>(){
            public void notificationReceived(Notification notification){
                try{
                    masterInterface.notificationReceived(new MultiplexedNotification(notification, channelUID));
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        };
        this.listeners.put(channelUID, listener);
        this.senders.put(channelUID, notificationSender);
        notificationSender.addListener(listener);
    }
    
    public void reset() throws RemoteException
    {
        LinkedList<String> linkedList = new LinkedList();
        linkedList.addAll(listeners.keySet());
        while(!linkedList.isEmpty()) this.removeMultiplexedListener(linkedList.removeFirst());
    }
}
