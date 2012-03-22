/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.core.utils.notifications;

/**
 *
 * @author Gwenhael
 */
public class DefaultNotificationSenderWithCache<N> extends DefaultNotificationSender<N> {
    private N _lastNotification = null;
    
    @Override
    synchronized public void notifyAll(N notification) {
        super.notifyAll(notification);
        _lastNotification = notification;
    }    
    
    @Override
    synchronized public void addListener(NotificationListener<N> listener) {
        super.addListener(listener);
        if(null != _lastNotification){
            listener.notificationReceived(_lastNotification);
        }
    }    
    
    synchronized public N getLastNotification() {
        return _lastNotification;
    }    
}
