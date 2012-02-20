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
*//*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.master.masterutils;

import com.devoteam.srit.xmlloader.core.RunnerState;
import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import java.util.List;

/**
 *
 * @author gpasquiers
 */
public class MasterRunnerSimultaneous extends MasterRunner
{

    public MasterRunnerSimultaneous(Master master, List<TestData> testDatas) throws Exception
    {
        super(master, testDatas);
    }
    
    @Override
    public void start() throws Exception
    {
        this.init(false);
        final long timestamp = System.currentTimeMillis();
        this.addListener(new NotificationListener<Notification<String, RunnerState>>()
        {

            public void notificationReceived(Notification<String, RunnerState> notification)
            {
                switch(notification.getData().getState())
                {
                    case OPEN_SUCCEEDED:
                        try
                        {
                            for (RemoteTesterRunner remoteTesterRunner : getChildren())
                            {
                                remoteTesterRunner.resetStats(timestamp);
                                remoteTesterRunner.start();
                            }
                        }
                        catch(Exception e)
                        {
                            System.err.println("Exception to handle");
                            e.printStackTrace();
                        }
                }
            }
        });
        

    }

    @Override
    public void stop() throws Exception
    {
        for(RemoteTesterRunner remoteTesterRunner:this.getChildren())
        {
            remoteTesterRunner.stop();
        }
    }

}
