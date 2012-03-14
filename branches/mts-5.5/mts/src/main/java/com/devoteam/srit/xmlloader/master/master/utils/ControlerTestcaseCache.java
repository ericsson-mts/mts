/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.master.master.utils;

import com.devoteam.srit.xmlloader.core.RunnerState;
import com.devoteam.srit.xmlloader.core.utils.notifications.DefaultNotificationSenderWithCache;
import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;

/**
 *
 * @author Gwenhael
 */
public class ControlerTestcaseCache extends DefaultNotificationSenderWithCache<RunnerState> implements NotificationListener<Notification<String, RunnerState>> {
    @Override
    public void notificationReceived(Notification<String, RunnerState> notification) {
        notifyAll(notification.getData());
    }
}
