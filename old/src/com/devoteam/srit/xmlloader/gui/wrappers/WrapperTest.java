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
package com.devoteam.srit.xmlloader.gui.wrappers;

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.RunnerState;
import com.devoteam.srit.xmlloader.core.RunnerState.State;
import com.devoteam.srit.xmlloader.core.Test;
import com.devoteam.srit.xmlloader.core.TestRunner;
import com.devoteam.srit.xmlloader.core.TestRunnerLoad;
import com.devoteam.srit.xmlloader.core.TestRunnerSequential;
import com.devoteam.srit.xmlloader.core.TestRunnerSingle;
import com.devoteam.srit.xmlloader.core.Testcase;
import com.devoteam.srit.xmlloader.core.TestcaseRunner;
import com.devoteam.srit.xmlloader.core.Tester;
import com.devoteam.srit.xmlloader.core.ThreadPool;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.hierarchy.DefaultHierarchyMember;
import com.devoteam.srit.xmlloader.core.utils.hierarchy.HierarchyMember;
import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import com.devoteam.srit.xmlloader.gui.TesterGui;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author gpasquiers
 */
public class WrapperTest implements HierarchyMember<Object, WrapperTestcase>
{

    private DefaultHierarchyMember<Object, WrapperTestcase> defaultHierarchyMember;
    
    public Object getParent()
    {
        return this.defaultHierarchyMember.getParent();
    }

    public List<WrapperTestcase> getChildren()
    {
        return this.defaultHierarchyMember.getChildren();
    }

    public void setParent(Object parent)
    {
        this.defaultHierarchyMember.setParent(parent);
    }

    public void addChild(WrapperTestcase child)
    {
        this.defaultHierarchyMember.addChild(child);
    }

    public void removeChild(WrapperTestcase child)
    {
        this.defaultHierarchyMember.removeChild(child);
    }
    
    private Test test;
    private boolean running;
    private TestRunner testRunner;
    private boolean wasActiveForSingle;
    
    public WrapperTest()
    {
        this.defaultHierarchyMember = new DefaultHierarchyMember<Object, WrapperTestcase>();
    }

    public WrapperTest(Test test, List<WrapperTestcase> wrapperTestcases)
    {
        this();
        this.test = test;
        this.testRunner = null;
        for(WrapperTestcase wrapperTestcase:wrapperTestcases)
        {
            this.addChild(wrapperTestcase);
            wrapperTestcase.setParent(this);
        }
    }

    public WrapperTest(Test test, WrapperTestcase wrapperTestcase)
    {
        this();
        this.test = test;
        this.testRunner = null;
        this.addChild(wrapperTestcase);
        wrapperTestcase.setParent(this);
    }
    
    public void startSequential()
    {
        List<Testcase> testcasesList = null;
        try
        {
            this.setRunning(true, true);
            testcasesList = new LinkedList<Testcase>();
            List<Integer> numbersToRun = new LinkedList<Integer>();
            
            for (WrapperTestcase wrapperTestcase : this.getChildren())
            {
                testcasesList.add(wrapperTestcase.getTestcase());
                // save active status before start
                numbersToRun.add(wrapperTestcase.getNumber());
            }

            this.testRunner = new TestRunnerSequential(this.test, testcasesList, numbersToRun);
            this.registerToTestRunner();

            Iterator<WrapperTestcase> iterator = this.getChildren().iterator();
            for(TestcaseRunner testcaseRunner:this.testRunner.getChildren()) testcaseRunner.addListener(iterator.next());
            
            
            StatPool.getInstance().reset();
            
            this.report_generate();
            
            testRunner.start();
                        
            this.setRunning(true, false);
        }
        catch (Exception e)
        {
            setRunning(false, false);
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, e, "Exception while starting runner");
            JOptionPane.showMessageDialog(null, "An error occured while starting Runner\nPlease check logs for more informations", "Error", JOptionPane.ERROR_MESSAGE);
            if(null != testRunner)
            {
                for(TestcaseRunner testcaseRunner:this.testRunner.getChildren())
                {
                    switch(testcaseRunner.getState().getState())
                    {
                        case OPEN_FAILED:
                            testcaseRunner.getState().setState(State.FAILED);
                            break;
                        case OPEN_SUCCEEDED:
                            testcaseRunner.getState().setState(State.INTERRUPTED);
                            break;
                    }
                    testcaseRunner.tryNotifyAll();
                }
            }
        }
    }
    
    public void startSingle()
    {
        try
        {
            this.wasActiveForSingle = this.getChildren().get(0).getActive();
            this.setRunning(true, true);
            this.testRunner = new TestRunnerSingle(this.test, this.getChildren().get(0).getTestcase(), this.getChildren().get(0).getNumber());
            this.registerToTestRunner();
            this.testRunner.getChildren().get(0).addListener(this.getChildren().get(0));
            this.testRunner.start();
            this.setRunning(true, false);
        }
        catch (Exception e)
        {
            setRunning(false, false);
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, e, "Exception while starting runner");
            JOptionPane.showMessageDialog(null, "An error occured while starting Runner\nPlease check logs for more informations", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void startLoad()
    {
        List<Testcase> testcasesList = null;
        try
        {
            this.setRunning(true, true);
            testcasesList = new LinkedList<Testcase>();
            for (WrapperTestcase wrapperTestcase : this.getChildren())
            {
                testcasesList.add(wrapperTestcase.getTestcase());
                
                // save active status before start
            }
            
            this.testRunner = new TestRunnerLoad(Tester.getInstance().getTest(), testcasesList);
            this.registerToTestRunner();

            
            Iterator<WrapperTestcase> iterator = this.getChildren().iterator();
            for(TestcaseRunner testcaseRunner:this.testRunner.getChildren()) testcaseRunner.addListener(iterator.next());
            for(TestcaseRunner testcaseRunner:this.testRunner.getChildren()) testcaseRunner.init(true);
            
            StatPool.getInstance().reset();

            this.report_generate();
            
            this.testRunner.start();
            this.setRunning(true, false);
        }
        catch(Exception e)
        {
            this.setRunning(false, false);
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, e, "Exception while starting runner");
            JOptionPane.showMessageDialog(null, "An error occured while starting Runner\nPlease check logs for more informations", "Error" ,JOptionPane.ERROR_MESSAGE);
            if(null != testRunner)
            {
                for(TestcaseRunner testcaseRunner:this.testRunner.getChildren())
                {
                    switch(testcaseRunner.getState().getState())
                    {
                        case OPEN_FAILED:
                            testcaseRunner.getState().setState(State.FAILED);
                            break;
                        case OPEN_SUCCEEDED:
                            testcaseRunner.getState().setState(State.INTERRUPTED);
                            break;
                    }
                    testcaseRunner.tryNotifyAll();
                }
            }
        }
    }

    private void report_generate()
    {
    	final TestRunner _testRunner = this.testRunner;
    
    	this.testRunner.addListener(new NotificationListener<Notification<String,RunnerState>>(){

    		boolean alreadyManage = false;
			public void notificationReceived(Notification<String, RunnerState> notification) {
				switch(notification.getData().getState())
				{
					case SUCCEEDED:
					case FAILED:
					case INTERRUPTED:
						if (!alreadyManage)
						{
				        	boolean automaticGenerate  = Config.getConfigByName("tester.properties").getBoolean("stats.AUTOMATIC_GENERATE", false);
				        	if (automaticGenerate)
				        	{
				        		_testRunner.getTest().report_generate();
				        	}
				        	alreadyManage = true;
				        	
						}
						break;
					default :
				}
			}
    	
	    });
    }

    
    private void setRunning(boolean running, boolean opening)
    {
        this.running = running;
        if (this.running)
        {
            TesterGui.instance().startWrapperTest(this);
            for (WrapperTestcase wrapperTestcase : this.getChildren())
            {
                wrapperTestcase.setActive(false);
                wrapperTestcase.setEnabled(false);
            }
        }
        else
        {
            TesterGui.instance().stopWrapperTest(this);
            if(testRunner instanceof TestRunnerSingle)
            {
                this.getChildren().get(0).setActive(this.wasActiveForSingle);
                this.getChildren().get(0).setEnabled(true);
            }
            else
            {
                for (WrapperTestcase wrapperTestcase : this.getChildren())
                {
                    wrapperTestcase.setActive(true);
                    wrapperTestcase.setEnabled(true);
                }
            }
        }

        TesterGui.instance().updateMenuState(opening);
    }

    public boolean getRunning()
    {
        return this.running;
    }
    
    public Runner getRunner()
    {
        return this.testRunner;
    }

    public int getTestcaseNumber()
    {
        return this.getChildren().size();
    }
    
    public void registerToTestRunner()
    {
        this.testRunner.addListener(new NotificationListener<Notification<String, RunnerState>>(){
            public void notificationReceived(Notification<String, RunnerState> notification){
                RunnerState state = notification.getData();
                switch (state.getState())
                {
                    case OPENING:
                    case OPEN_SUCCEEDED:
                    case INTERRUPTING:
                    case RUNNING:
                    case FAILING:
                        break;
                    case SUCCEEDED:
                    case FAILED:
                    case INTERRUPTED:
                        try
                        {
                            ThreadPool.reserve().start(new Runnable(){
                                public void run(){
                                    setRunning(false, false);
                                }
                            });                            
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        break;
                        
                }
            }
        });
    }
}
