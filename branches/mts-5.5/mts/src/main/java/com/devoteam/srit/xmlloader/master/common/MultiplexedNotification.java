/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.master.common;

import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import java.io.Serializable;

/**
 *
 * @author gpasquiers
 */
public class MultiplexedNotification implements Serializable
{
    private Notification notification;
    
    private String channelUID;
    
    public MultiplexedNotification(Notification notification, String channelUID)
    {
        this.notification = notification;
        this.channelUID = channelUID;
    }
    
    public String getChannelUID()
    {
        return this.channelUID;
    }

    public Notification getNotification()
    {
        return this.notification;
    }
}
