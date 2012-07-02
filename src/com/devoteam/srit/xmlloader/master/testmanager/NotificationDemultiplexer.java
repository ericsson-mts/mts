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

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import com.devoteam.srit.xmlloader.master.mastergui.JPanelTestcase;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author gpasquiers
 */
public class NotificationDemultiplexer
{
    private Map<String, NotificationListener> listeners;
    private Map<String, RemoteTester> testManagers;
    private Map<NotificationListener, String> channels;
    
    public NotificationDemultiplexer()
    {
        this.listeners = Collections.synchronizedMap(new HashMap());
        this.testManagers = Collections.synchronizedMap(new HashMap());
        this.channels = Collections.synchronizedMap(new HashMap());
    }
    
    public void addMultiplexedListener(NotificationListener listener, RemoteTester testManager, String testName, String testcaseName, String scenarioName) throws Exception
    {
        String channelUID = Utils.newUID();
        GlobalLogger.instance().getApplicationLogger().debug(Topic.CORE, "NotificationDemultiplexer: register notification listener for channelUID ", channelUID);

        try
        {
            if(this.channels.containsKey(listener))
            {
                GlobalLogger.instance().getApplicationLogger().error(Topic.CORE,"unregister listener with UID ", this.channels.get(listener), " before registering it with UID ", channelUID);
                this.removeMultiplexedListener(listener);
            }

            this.listeners.put(channelUID, listener);
            this.channels.put(listener, channelUID);
            this.testManagers.put(channelUID, testManager);

            testManager.addMultiplexedListener(channelUID, testName, testcaseName, scenarioName);
        }
        catch(Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().debug(Topic.CORE, e, "errir while registering listener in demultiplexer");
            this.listeners.remove(channelUID);
            this.channels.remove(listener);
            this.testManagers.remove(channelUID);
        }
    }
    
    public void removeMultiplexedListener(NotificationListener listener) throws Exception
    {
        String channelUID = this.channels.remove(listener);

        if(null == channelUID) return;

        RemoteTester testManager = this.testManagers.remove(channelUID);

        if(null == testManager) return;
        
        this.listeners.remove(channelUID);
        
        
        testManager.removeMultiplexedListener(channelUID);
        
        
        GlobalLogger.instance().getApplicationLogger().debug(Topic.CORE, "NotificationDemultiplexer: unregistered notification listener for channel ", channelUID);
    }
    
    public void notificationReceived(MultiplexedNotification notification) throws Exception
    {
        GlobalLogger.instance().getApplicationLogger().debug(Topic.CORE, "NotificationDemultiplexer: receive notification for channelUID ", notification.getChannelUID());
        String channelUID = notification.getChannelUID();
        NotificationListener notificationListener = listeners.get(channelUID);
        if(null == notificationListener)
        {
            GlobalLogger.instance().getApplicationLogger().warn(Topic.CORE, "NotificationDemultiplexer: could not handle notification for channelUID", channelUID, ": no listener");
        }
        else
        {
            notificationListener.notificationReceived(notification.getNotification());
        }
    }
}
