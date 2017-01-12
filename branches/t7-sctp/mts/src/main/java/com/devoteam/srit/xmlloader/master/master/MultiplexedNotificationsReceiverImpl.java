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

package com.devoteam.srit.xmlloader.master.master;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import com.devoteam.srit.xmlloader.master.common.MultiplexedNotification;
import com.devoteam.srit.xmlloader.master.slave.SlaveIntf;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class, as it received "multiplexed events" is a demultiplexer ;-)
 * @author Gwenhael
 */
public class MultiplexedNotificationsReceiverImpl extends UnicastRemoteObject implements MultiplexedNotificationsReceiverIntf {

    private SlaveIntf _slaveIntf;
    private Map<String, NotificationListener> _listeners;
    private Map<NotificationListener, String> _channels;

    public MultiplexedNotificationsReceiverImpl(SlaveIntf slaveIntf) throws RemoteException {
        _slaveIntf = slaveIntf;
        _listeners = Collections.synchronizedMap(new HashMap());
        _channels = Collections.synchronizedMap(new HashMap());
    }

    public void addMultiplexedListener(NotificationListener listener, String testName, String testcaseName, String scenarioName) throws Exception {
        String channelUID = Utils.newUID();
        GlobalLogger.instance().getApplicationLogger().debug(Topic.CORE, "NotificationDemultiplexer: register notification listener for channelUID ", channelUID);

        try {
            if (_channels.containsKey(listener)) {
                GlobalLogger.instance().getApplicationLogger().error(Topic.CORE, "unregister listener with UID ", _channels.get(listener), " before registering it with UID ", channelUID);
                this.removeMultiplexedListener(listener);
            }

            _listeners.put(channelUID, listener);
            _channels.put(listener, channelUID);

            _slaveIntf.addMultiplexedListener(channelUID, testName, testcaseName, scenarioName);
        }
        catch (Exception e) {
            GlobalLogger.instance().getApplicationLogger().error(Topic.CORE, e, "error while registering listener in demultiplexer");
            _listeners.remove(channelUID);
            _channels.remove(listener);
        }
    }

    public void removeMultiplexedListener(NotificationListener listener) throws Exception {
        String channelUID = _channels.remove(listener);

        if (null == channelUID) {
            return;
        }

        if (null == _slaveIntf) {
            return;
        }

        _listeners.remove(channelUID);
        _slaveIntf.removeMultiplexedListener(channelUID);
        GlobalLogger.instance().getApplicationLogger().debug(Topic.CORE, "NotificationDemultiplexer: unregistered notification listener for channel ", channelUID);
    }

    public void free() {
        while (!_listeners.isEmpty()) {
            try {
                Entry<String, NotificationListener> entry = _listeners.entrySet().iterator().next();
                _listeners.remove(entry.getKey());
                _channels.remove(entry.getValue());
                _slaveIntf.removeMultiplexedListener(entry.getKey());
            }
            catch (Exception e) {
                // ignore
            }
        }
    }

    @Override
    public void notificationReceived(MultiplexedNotification notification) throws RemoteException {
        GlobalLogger.instance().getApplicationLogger().debug(Topic.CORE, "NotificationDemultiplexer: receive notification for channelUID ", notification.getChannelUID());
        String channelUID = notification.getChannelUID();
        NotificationListener notificationListener = _listeners.get(channelUID);
        if (null == notificationListener) {
            GlobalLogger.instance().getApplicationLogger().warn(Topic.CORE, "NotificationDemultiplexer: could not handle notification for channelUID", channelUID, ": no listener");
        }
        else {
            notificationListener.notificationReceived(notification.getNotification());
        }
    }
}
