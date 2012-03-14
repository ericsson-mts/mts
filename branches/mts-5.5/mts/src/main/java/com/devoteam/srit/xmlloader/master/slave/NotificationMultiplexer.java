/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.master.slave;

import com.devoteam.srit.xmlloader.core.TestRunner;
import com.devoteam.srit.xmlloader.core.Testcase;
import com.devoteam.srit.xmlloader.core.Tester;
import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationSender;
import com.devoteam.srit.xmlloader.master.common.MultiplexedNotification;
import com.devoteam.srit.xmlloader.master.master.MultiplexedNotificationsReceiverIntf;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 *
 * @author gpasquiers
 */
public class NotificationMultiplexer {
    private MultiplexedNotificationsReceiverIntf _multiplexedNotificationsReceiverIntf;
    private HashMap<String, NotificationListener> _listeners;
    private HashMap<String, NotificationSender> _senders;

    public NotificationMultiplexer(MultiplexedNotificationsReceiverIntf multiplexedNotificationsReceiverIntf) {
        _multiplexedNotificationsReceiverIntf = multiplexedNotificationsReceiverIntf;
        _listeners = new HashMap();
        _senders = new HashMap();
    }

    public void removeMultiplexedListener(String channelUID) throws RemoteException {
        NotificationSender sender = _senders.remove(channelUID);
        NotificationListener listener = _listeners.remove(channelUID);

        if (null == sender || null == listener) {
            throw new RemoteException("Error: sender=" + sender + " and listener=" + listener + " for ChannelUID " + channelUID);
        }
        else {
            sender.removeListener(listener);
        }
    }

    public void addMultiplexedListener(TestRunner runner, final String channelUID, String testName, String testcaseName, String scenarioName) throws RemoteException {
        if (null == runner) {
            throw new RemoteException("Could not register to notification sender, null runner");
        }

        NotificationSender notificationSender = null;

        if (null != testName && null == testcaseName) {
            notificationSender = runner;
        }
        else if (null != testName && null != testcaseName && null == scenarioName) {
            if (Tester.getInstance().getTest().getName().equals(testName)) {
                for (Testcase testcase : runner.getTest().getChildren()) {
                    if (testcase.getName().equals(testcaseName)) {
                        notificationSender = testcase.getTestcaseRunner();
                    }
                }
            }
        }
        else if (null != testName && null != testcaseName && null != scenarioName) {
            throw new RemoteException("Listening scenarios state is not supported yet");
        }


        if (null == notificationSender) {
            throw new RemoteException("Could not register to notification sender " + testName + "/" + testcaseName + "/" + scenarioName);
        }

        NotificationListener listener = new NotificationListener<Notification>() {
            @Override
            public void notificationReceived(Notification notification) {
                try {
                    _multiplexedNotificationsReceiverIntf.notificationReceived(new MultiplexedNotification(notification, channelUID));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        _listeners.put(channelUID, listener);
        _senders.put(channelUID, notificationSender);
        notificationSender.addListener(listener);
    }

    public void reset() throws RemoteException {
        LinkedList<String> linkedList = new LinkedList();
        linkedList.addAll(_listeners.keySet());
        while (!linkedList.isEmpty()) {
            this.removeMultiplexedListener(linkedList.removeFirst());
        }
    }
}
