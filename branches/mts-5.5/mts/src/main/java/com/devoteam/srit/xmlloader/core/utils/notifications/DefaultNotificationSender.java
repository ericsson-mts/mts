/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.core.utils.notifications;

import javolution.util.FastList;

/**
 *
 * @author gpasquiers
 */
public class DefaultNotificationSender<N> implements NotificationSender<N> {

    private FastList<NotificationListener<N>> listeners = new FastList<NotificationListener<N>>();

    @Override
    public void addListener(NotificationListener<N> listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    @Override
    public void removeListener(NotificationListener<N> listener) {
        listeners.remove(listener);
    }

    @Override
    public void notifyAll(N notification) {
        for (NotificationListener listener : listeners) {
            if (null != listener) {
                listener.notificationReceived(notification);
            }
        }
    }
}
