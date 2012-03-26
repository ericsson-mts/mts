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

package com.devoteam.srit.xmlloader.core.newstats;

import com.devoteam.srit.xmlloader.core.RunnerState;
import com.devoteam.srit.xmlloader.core.TestRunner;
import com.devoteam.srit.xmlloader.core.TestRunnerSingle;
import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import java.util.HashSet;

/**
 *
 * @author gpasquiers
 */
public class StatPoolReset
{
    //static part
    private static final StatPoolReset instance = new StatPoolReset();

    public static StatPoolReset instance()
    {
        return instance;
    }


    //instanciated part
    private HashSet<TestRunner> runners;
    private boolean _enabled;

    public StatPoolReset()
    {
        _enabled =  false;
        runners = new HashSet();
    }

    synchronized public void trigger(final TestRunner testRunner)
    {
        if(testRunner instanceof TestRunnerSingle) return;

        if(runners.contains(testRunner)) return;

        if(_enabled && runners.size() ==0) StatPool.getInstance().reset();

        runners.add(testRunner);

        testRunner.addListener(new NotificationListener<Notification<String, RunnerState>>(){
            public void notificationReceived(Notification<String, RunnerState> notification)
            {
                if(notification.getData().isFinished())
                {
                    synchronized(instance)
                    {
                        instance.runners.remove(testRunner);
                    }
                }
            }
        });
    }

    synchronized public void enable(){
        _enabled = true;
    }

    synchronized public void disable(){
        _enabled = false;
    }

}
