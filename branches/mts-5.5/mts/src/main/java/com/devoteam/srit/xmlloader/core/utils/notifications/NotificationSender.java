/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.utils.notifications;

/**
 *
 * @author gpasquiers
 */
public interface NotificationSender<N>
{
    public void addListener(NotificationListener<N> listener);
    
    public void removeListener(NotificationListener<N> listener);

    public void notifyAll(N notification);
}
