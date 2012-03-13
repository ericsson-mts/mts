/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.utils.notifications;

/**
 *
 * @author gpasquiers
 */
public interface NotificationListener<N>
{
    public void notificationReceived(N notification);
}
