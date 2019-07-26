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
