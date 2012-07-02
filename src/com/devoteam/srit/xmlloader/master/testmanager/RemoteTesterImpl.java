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
package com.devoteam.srit.xmlloader.master.testmanager;

import com.devoteam.srit.xmlloader.core.Test;
import com.devoteam.srit.xmlloader.core.TestRunner;
import com.devoteam.srit.xmlloader.core.TestRunnerLoad;
import com.devoteam.srit.xmlloader.core.TestRunnerSequential;
import com.devoteam.srit.xmlloader.core.Testcase;
import com.devoteam.srit.xmlloader.core.TestcaseRunner;
import com.devoteam.srit.xmlloader.core.Tester;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.parameters.EditableParameterProviderHashMap;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.URIFactory;
import com.devoteam.srit.xmlloader.core.utils.URIRegistry;
import com.devoteam.srit.xmlloader.master.SlaveImplementation;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author gpasquiers
 */
public class RemoteTesterImpl implements RemoteTester
{
    private TestRunner runner;
    
    public RemoteTesterImpl()
    {
         this.notificationMultiplexer = null;
    }
    
    public Test openTest(URI path, URI IMSLOADER_BIN, String name, String home, HashMap<String, String> initialParametersValues, boolean force) throws RemoteException
    {
        try
        {
            URIRegistry.IMSLOADER_BIN = IMSLOADER_BIN;
            if(null != this.notificationMultiplexer)
            {
                this.notificationMultiplexer.reset();
            }
            
            if(null != this.runner)
            {
                this.runner.stop();
                this.runner.stop();
            }
            this.runner = null;
            this.notificationMultiplexer = null;
            
            StackFactory.reset();
            Config.reset();

            if(false == force &&
               null != Tester.getInstance() &&
               null != Tester.getInstance().getTest() &&
               null != Tester.instance().getTestXMLDocument() &&
               Tester.getInstance().getTestXMLDocument().getXMLFile().equals(path))
            {
                return Tester.instance().getTest();
            }
            
            Tester.cleanInstance();
            Tester.buildInstance();
            Tester.instance().open_openFile(path, new EditableParameterProviderHashMap(initialParametersValues));
            
            Test test = Tester.instance().getTest();
            
            test.setName(name);
            
            // override "imsloader.resources.home"
            if(null != home){
                URIRegistry.IMSLOADER_RESOURCES_HOME = URIFactory.resolve(URIRegistry.IMSLOADER_TEST_HOME, home);
            }
            
            for (Testcase testcase : test.getChildren())
            {
                if(null == testcase.attributeValue("state") || testcase.attributeValue("state").equals("true"))
                {
                    testcase.parseScenarios();
                }
            }
            
            return test;
        }
        catch (Exception e)
        {
            throw new RemoteException("exception while opening test", e);
        }
    }
    
    public void startTest(Class runnerClass) throws RemoteException
    {
        try
        {
            Test test = Tester.instance().getTest();
            if(null == test)
            {
                throw new RemoteException("Test not set");
            }

            List<Testcase> enabledTestcaseList = new LinkedList<Testcase>();
            List<Integer> numberToRunList = new LinkedList<Integer>();

            for(Testcase testcase:test.getTestcaseList())
            {
                if(null == testcase.attributeValue("state") || testcase.attributeValue("state").equalsIgnoreCase("true"))
                {
                    enabledTestcaseList.add(testcase);

                    String number = testcase.attributeValue("number");
                    if (number == null) number="1";
                    numberToRunList.add(new Integer(number));
                }
            }

            if(runnerClass.equals(TestRunnerSequential.class))
            {
                runner = new TestRunnerSequential(test, enabledTestcaseList, numberToRunList);
            }
            else if(runnerClass.equals(TestRunnerLoad.class))
            {
                runner = new TestRunnerLoad(test, enabledTestcaseList);
            }
            else
            {
                throw new RemoteException("Unhandled runner class");
            }

            for(TestcaseRunner testcaseRunner:runner.getChildren())
            {
                testcaseRunner.init();
            }
            
            runner.getState().setIndex(0);
            runner.start();
        }
        catch(Exception e)
        {
            throw new RemoteException("Exception while starting TestRunner", e);
        }
    }

    private NotificationMultiplexer notificationMultiplexer;
    
    public void addMultiplexedListener(final String channelUID, String testName, String testcaseName, String scenarioName) throws RemoteException
    {
        if(null == this.notificationMultiplexer) this.notificationMultiplexer = new NotificationMultiplexer(SlaveImplementation.instance().getSlaveNode().getStub());

        this.notificationMultiplexer.addMultiplexedListener(runner, channelUID, testName, testcaseName, scenarioName);
    }

    public void removeMultiplexedListener(final String channelUID) throws RemoteException
    {

        this.notificationMultiplexer.removeMultiplexedListener(channelUID);
    }

    public void stopTest() throws RemoteException
    {
        try
        {
            if(null != this.runner)
            {
                this.runner.stop();
            }
            else
            {
                throw new RemoteException("There is no runner, how could you call stop !?");
            }
        }
        catch(Exception e)
        {
            throw new RemoteException("Error stopping test", e);
        }
    }
    
    public StatPool getStatPool() throws RemoteException
    {
        return StatPool.getInstance().clone();
    }

    public void resetStatPool() throws RemoteException
    {
        StatPool.getInstance().reset();
    }
}
