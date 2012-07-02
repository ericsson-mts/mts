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

package com.devoteam.srit.xmlloader.cmd;

import com.devoteam.srit.xmlloader.core.RunnerState;
import com.devoteam.srit.xmlloader.core.Test;
import com.devoteam.srit.xmlloader.core.Testcase;
import com.devoteam.srit.xmlloader.core.report.ReportGenerator;
import com.devoteam.srit.xmlloader.core.report.ReportStatus;
import com.devoteam.srit.xmlloader.core.utils.URIFactory;
import com.devoteam.srit.xmlloader.core.utils.XMLDocument;
import com.devoteam.srit.xmlloader.core.utils.exceptionhandler.ExceptionHandlerSingleton;
import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import com.devoteam.srit.xmlloader.master.MasterImplementation;
import com.devoteam.srit.xmlloader.master.masterutils.Master;
import com.devoteam.srit.xmlloader.master.masterutils.MasterRunner;
import com.devoteam.srit.xmlloader.master.masterutils.MasterRunnerSequential;
import com.devoteam.srit.xmlloader.master.masterutils.MasterRunnerSimultaneous;
import com.devoteam.srit.xmlloader.master.masterutils.RemoteTesterRunner;
import java.net.URI;
import java.util.concurrent.Semaphore;

/**
 *
 * @author gpasquiers
 */
public class MasterTextTester
{
    private Master master;
    private Semaphore semaphore;
    private final MasterTextTester _this = this;
    private String runnerName;
    private MasterRunner masterRunner;
    

    public MasterTextTester(URI uri, String runnerName) throws Exception
    {
        try
        {
            XMLDocument xmlDocument = new XMLDocument();
            xmlDocument.setXMLFile(uri);
            xmlDocument.setXMLSchema(URIFactory.newURI("../conf/schemas/master.xsd"));
            xmlDocument.parse();
            this.master = new Master(xmlDocument, URIFactory.resolve(uri, "."));
            this.semaphore = new Semaphore(0);
            this.runnerName = runnerName;
        }
        catch (Exception e)
        {
            throw e;
        }
    }
    
    public void acquire() throws InterruptedException
    {
        this.semaphore.acquire();
    }
    
    public void start()
    {
        MasterImplementation.init();
        try
        {
            if(runnerName.equalsIgnoreCase("simultaneous"))
            {
                masterRunner = new MasterRunnerSimultaneous(master, master.getTestDatas());
            }
            else if(runnerName.equalsIgnoreCase("sequential"))
            {
                masterRunner = new MasterRunnerSequential(master, master.getTestDatas());
            }
            else //default: simultaneous
            {
                masterRunner = new MasterRunnerSimultaneous(master, master.getTestDatas());
            }

            masterRunner.start();
            
            for(final RemoteTesterRunner remoteTesterRunner:this.masterRunner.getChildren())
            {
                final String testName = remoteTesterRunner.getTestData().attributeValue("name");
                remoteTesterRunner.addListener(new NotificationListener<Notification<String, RunnerState>>(){
                    boolean didRegister = false;
                    public void notificationReceived(Notification<String, RunnerState> notification){
                        if(this.didRegister) return;
                        
                        switch(notification.getData().getState())
                        {
                            case RUNNING:
                            case FAILING:
                            case INTERRUPTING:
                            case INTERRUPTED:
                            case FAILED:
                            case SUCCEEDED:
                                this.didRegister = true;
                                Test test = remoteTesterRunner.getTest();
                                for(Testcase testcase:test.getChildren())
                                {
                                    if(testcase.attributeValue("state") == null || testcase.attributeValue("state").equalsIgnoreCase("true"))
                                    try
                                    {
                                        final String testcaseName = testcase.attributeValue("name");
                                        MasterImplementation.instance().addMultiplexedListener(new NotificationListener<Notification<String, RunnerState>>(){
                                            public void notificationReceived(Notification<String, RunnerState> notification){
                                                switch(notification.getData().getState())
                                                {
                                                    case FAILING:
                                                    case INTERRUPTING:
                                                    case FAILED:
                                                    case INTERRUPTED:
                                                        System.out.println(notification.getData().getState() + ": " + master.attributeValue("name") + " / " + testName + " / " + testcaseName);
                                                        try
                                                        {
                                                            MasterImplementation.instance().removeMultiplexedListener(this);
                                                        }
                                                        catch(Exception e)
                                                        {
                                                            // ignore
                                                        }
                                                }
                                            }
                                        }, remoteTesterRunner.getRemoteTester(), testName, testcaseName, null);
                                    }
                                    catch(Exception e)
                                    {
                                        ExceptionHandlerSingleton.instance().display(e, null);
                                    }
                                }
                        }
                    };
                });
            }
            
            masterRunner.addListener(new NotificationListener<Notification<String, RunnerState>>(){
                public void notificationReceived(Notification<String, RunnerState> notification){
                    switch(notification.getData().getState())
                    {
                        case OPEN_FAILED:
                        case SUCCEEDED:
                        case FAILED:
                        case INTERRUPTED:
                            _this.semaphore.release();
                            break;
                    }
                }
            });
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void report() throws Exception
    {
        try
        {
            ReportGenerator reportGenerator = masterRunner.report();
            reportGenerator.addListener(new NotificationListener<Notification<String, ReportStatus>>(){
                public void notificationReceived(Notification<String, ReportStatus> notification){
                    if(notification.getData().getProgress() == 100) _this.semaphore.release();
                }
            });
        }
        catch(Exception e)
        {
            this.semaphore.release();
            ExceptionHandlerSingleton.instance().display(e, null);
        }
    }

	public Master getMaster() {
		return master;
	}

	public MasterRunner getMasterRunner() {
		return masterRunner;
	}
}
