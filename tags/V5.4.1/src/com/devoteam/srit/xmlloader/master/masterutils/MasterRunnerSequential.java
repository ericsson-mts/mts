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
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author gpasquiers
 */
public class MasterRunnerSequential extends MasterRunner
{
    final MasterRunnerSequential _this = this;
    
    
    public MasterRunnerSequential(Master master, List<TestData> testDatas) throws Exception
    {
        super(master, testDatas);
    }
    
    @Override
    public void start() throws Exception
    {
        this.init(false);
        
        this.addListener(new NotificationListener<Notification<String, RunnerState>>()
        {
            public void notificationReceived(Notification<String, RunnerState> notification)
            {
                switch(notification.getData().getState())
                {
                    case OPEN_SUCCEEDED:
                        try
                        {
                            long timestamp = System.currentTimeMillis();
                            
                            for(RemoteTesterRunner tester:_this.getChildren())
                            {
                                tester.resetStats(timestamp);
                            }
                            
                            startNext();
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
    
    Iterator<RemoteTesterRunner> iterator = this.getChildren().iterator();
    RemoteTesterRunner currentRemoteTesterRunner = null;
    
    private void startNext() throws Exception
    {
        if(iterator.hasNext())
        {
            final RemoteTesterRunner remoteTesterRunner = iterator.next();
            this.currentRemoteTesterRunner = remoteTesterRunner;
            remoteTesterRunner.start();
            remoteTesterRunner.addListener(new NotificationListener<Notification<String, RunnerState>>()
            {

                public void notificationReceived(Notification<String, RunnerState> notification)
                {
                    switch(notification.getData().getState())
                    {
                        case INTERRUPTED:
                        case FAILED:
                        case SUCCEEDED:
                            try
                            {
                                startNext();
                                remoteTesterRunner.removeListener(this);
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
        else
        {
            this.currentRemoteTesterRunner = null;
        }
    }
    
    @Override
    public void stop() throws Exception
    {
        if(null != this.currentRemoteTesterRunner)
        {
            this.currentRemoteTesterRunner.stop();
        }
    }

}
