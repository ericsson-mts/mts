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

package com.devoteam.srit.xmlloader.core.utils.notifications;

import javolution.util.FastList;

/**
 *
 * @author gpasquiers
 */
public class DefaultNotificationSender<N> implements NotificationSender<N>
{
    private FastList<NotificationListener<N>> listeners = new FastList<NotificationListener<N>>();
    
    public void addListener(NotificationListener<N> listener)
    {
        if(!this.listeners.contains(listener)) this.listeners.add(listener);
    }
    
    public void removeListener(NotificationListener<N> listener)
    {
        while(listeners.remove(listener));
    }

    public void notifyAll(N notification)
    {
        for(NotificationListener listener:listeners)
        {
            if(null != listener)
            {
                listener.notificationReceived(notification);
            }
        }
    }
}
