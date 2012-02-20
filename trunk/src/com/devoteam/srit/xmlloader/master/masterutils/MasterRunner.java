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
import com.devoteam.srit.xmlloader.core.RunnerStateComputer;
import com.devoteam.srit.xmlloader.core.Tester;
import com.devoteam.srit.xmlloader.core.ThreadPool;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.report.ReportGenerator;
import com.devoteam.srit.xmlloader.core.report.ReportStatus;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.hierarchy.DefaultHierarchyMember;
import com.devoteam.srit.xmlloader.core.utils.hierarchy.HierarchyMember;
import com.devoteam.srit.xmlloader.core.utils.notifications.DefaultNotificationSender;
import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationSender;
import com.devoteam.srit.xmlloader.master.mastergui.JFrameMaster;
import java.util.List;

/**
 *
 * @author gpasquiers
 */
abstract public class MasterRunner implements HierarchyMember<Object, RemoteTesterRunner>, NotificationSender<Notification<String, RunnerState>>, NotificationListener<Notification<String, RunnerState>>
{
    // <editor-fold desc="Hierarchy implementation" defaultstate="collapsed">
    private DefaultHierarchyMember<Object, RemoteTesterRunner> defaultHierarchyMember;
    
    public Object getParent()
    {
        throw new RuntimeException("This HierarchyMember CANNOT have any parent");
    }

    public List<RemoteTesterRunner> getChildren()
    {
        return this.defaultHierarchyMember.getChildren();
    }

    public void setParent(Object parent)
    {
        throw new RuntimeException("This HierarchyMember CANNOT have any parent");
    }

    public void addChild(RemoteTesterRunner child)
    {
        this.defaultHierarchyMember.addChild(child);
    }

    public void removeChild(RemoteTesterRunner child)
    {
        this.defaultHierarchyMember.removeChild(child);
    }
    // </editor-fold>

    // <editor-fold desc="NotificationSender Implementation" defaultstate="collapsed">>
    private DefaultNotificationSender<Notification<String, RunnerState>> defaultNotificationSender;

    public void addListener(NotificationListener<Notification<String, RunnerState>> listener)
    {
        this.defaultNotificationSender.addListener(listener);
        
        listener.notificationReceived(new Notification<String, RunnerState>(this.master.attributeValue("name"), runnerStateComputer.getComputedState()));
    }

    public void removeListener(NotificationListener listener)
    {
        this.defaultNotificationSender.removeListener(listener);
    }

    public void notifyAll(Notification<String, RunnerState> notification)
    {
        this.defaultNotificationSender.notifyAll(notification);
    }

    public void notifyAll(RunnerState runnerState)
    {
        this.defaultNotificationSender.notifyAll(new Notification<String, RunnerState>(this.master.attributeValue("name"), runnerState));
    }
    // </editor-fold>
  
    // <editor-fold desc="NotificationListener Implementation" defaultstate="collapsed">>
    public void notificationReceived(Notification<String, RunnerState> notification)
    {
        this.runnerStateComputer.update(notification.getData());
        RunnerState runnerState = this.runnerStateComputer.getComputedState();
        this.notifyAll(runnerState);
    }
    // </editor-fold>
    
    public RunnerState getRunnerState()
    {
        return this.runnerStateComputer.getComputedState();
    }
    
    private final MasterRunner _this = this;
    
    private RunnerStateComputer runnerStateComputer;

    private Master master;
    
    public MasterRunner(Master master, List<TestData> testDatas) throws Exception
    {
        this.defaultHierarchyMember = new DefaultHierarchyMember();
        this.defaultNotificationSender = new DefaultNotificationSender();
        this.master = master;
        
        this.runnerStateComputer = new RunnerStateComputer(testDatas.size());
        
        int i=0;
        for(TestData testData:testDatas)
        {
            RemoteTesterRunner remoteTesterRunner = new RemoteTesterRunner(testData, i++);
            this.addChild(remoteTesterRunner);
            remoteTesterRunner.addListener(this);
        }
        
        this.addListener(new NotificationListener<Notification<String, RunnerState>>() {
            public void notificationReceived(Notification<String, RunnerState> notification) {
                switch(notification.getData().getState())
                {
                    case OPEN_SUCCEEDED:
                        if(_this instanceof MasterRunnerDeployer)
                        {
                            try
                            {
                                dispose();
                            }
                            catch(Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case OPEN_FAILED:
                    case FAILED:
                    case SUCCEEDED:
                    case INTERRUPTED:
                        try
                        {
                            dispose();
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        });
        
    }
    
    protected RunnerStateComputer getRunnerStateComputer()
    {
        return this.runnerStateComputer;
    }
    
    public void init(boolean force) throws Exception
    {
        for(RemoteTesterRunner remoteTesterRunner:this.getChildren())
        {
            remoteTesterRunner.init(force);
        }
    }

    public ReportGenerator report() throws Exception
    {
        // create StatPool
        final StatPool statPool;
        final long zeroTimestamp;
        
        StatPool tmpStatPool = null;
        long tmpZeroTimestamp = -1;
        for(RemoteTesterRunner remoteTesterRunner:this.getChildren())
        {
            if(-1 == tmpZeroTimestamp)
            {
            	tmpZeroTimestamp = remoteTesterRunner.getStartTimestamp();
            }
            else
            {
                if(tmpZeroTimestamp != remoteTesterRunner.getStartTimestamp())
                {
                    throw new Exception("Not all tests have the same zeroTimestamp. Can't generate the report.");
                }
            }
            
            if(null == tmpStatPool)
            {
            	tmpStatPool = remoteTesterRunner.getStatPool().clone();
            }
            else
            {
            	tmpStatPool.merge(remoteTesterRunner.getStatPool());
            }
            
        }
        statPool = tmpStatPool;
        zeroTimestamp = tmpZeroTimestamp;
    	
        // generate stat report
    	String dirName = Config.getConfigByName("tester.properties").getString("stats.REPORT_DIRECTORY","../reports/");
    	String fileName = dirName + "/MASTER_" + this.master.attributeValue("name");
        final ReportGenerator reportGenerator = new ReportGenerator(fileName);
        
        ThreadPool.reserve().start(new Runnable(){
            public void run(){
                try
                {
                    reportGenerator.generateReport(statPool, zeroTimestamp);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
        
        return reportGenerator;
    }

    public long getZeroTimestamp() throws Exception
    {
        long zeroTimestamp = -1;
        for(RemoteTesterRunner remoteTesterRunner:this.getChildren())
        {
            if(-1 == zeroTimestamp)
            {
                zeroTimestamp = remoteTesterRunner.getStartTimestamp();
            }
            else
            {
                if(zeroTimestamp != remoteTesterRunner.getStartTimestamp())
                {
                    throw new Exception("Not all tests have the same zeroTimestamp. Can't generate the report.");
                }
            }
        }
        return zeroTimestamp;
    }

    public StatPool getAndResetStatPool() throws Exception
    {
        StatPool statPool = null;
        for(RemoteTesterRunner remoteTesterRunner:this.getChildren())
        {
            if(null == statPool)
            {
                statPool = remoteTesterRunner.getStatPool().clone();
            }
            else
            {
                statPool.merge(remoteTesterRunner.getStatPool());
            }
            remoteTesterRunner.getStatPool().reset();
        }
        return statPool;
    }

    public Master getMaster()
    {
        return this.master;
    }
    
    public void dispose() throws Exception
    {
        MasterRunnerRegistry.remove(this);

        for(RemoteTesterRunner remoteTesterRunner:this.getChildren())
        {
            remoteTesterRunner.removeListener(this);
            remoteTesterRunner.dispose();
        }
    }

    public boolean finished()
    {
        switch(this.runnerStateComputer.getComputedState().getState())
        {
            case SUCCEEDED:
            case FAILED:
            case INTERRUPTED:
            case OPEN_FAILED:
                return true;
            case OPEN_SUCCEEDED:
                return this instanceof MasterRunnerDeployer;
            default :
                return false;
        }
    }
    
    abstract public void start() throws Exception;
    
    abstract public void stop() throws Exception;
}
