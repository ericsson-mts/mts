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
 * ScenarioRunner.java
 *
 * Created on 30 mai 2007, 10:40
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.core;

import com.devoteam.srit.xmlloader.core.RunnerState.State;
import com.devoteam.srit.xmlloader.core.exception.InterruptedExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.protocol.BufferMsg;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.log.TextListenerKey;
import com.devoteam.srit.xmlloader.core.log.TextListenerProviderRegistry;
import com.devoteam.srit.xmlloader.core.newstats.StatKey;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;

import com.devoteam.srit.xmlloader.core.utils.hierarchy.DefaultHierarchyMember;
import com.devoteam.srit.xmlloader.core.utils.hierarchy.HierarchyMember;
import com.devoteam.srit.xmlloader.core.utils.notifications.DefaultNotificationSender;
import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationSender;
import java.util.List;

/**
 *
 * @author gpasquiers
 */
public class ScenarioRunner extends Runner
        implements TextListenerKey, Runnable,
        HierarchyMember<TestcaseRunner, Object>,
        NotificationSender<Notification<String, RunnerState>>
{
	
    private DefaultHierarchyMember<TestcaseRunner, Object> defaultHierarchyMember;

    
    // <editor-fold defaultstate="collapsed" desc="DefaultHierarchyMember Implementation">
    public TestcaseRunner getParent()
    {
        return this.defaultHierarchyMember.getParent();
    }

    public List<Object> getChildren()
    {
        throw new RuntimeException("This HierarchyMember CANNOT have any children");
    }

    public void setParent(TestcaseRunner parent)
    {
        this.defaultHierarchyMember.setParent(parent);
    }

    public void addChild(Object child)
    {
        throw new RuntimeException("This HierarchyMember CANNOT have any children");
    }

    public void removeChild(Object child)
    {
        throw new RuntimeException("This HierarchyMember CANNOT have any children");
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="NotificationSender Implementation">
    private DefaultNotificationSender<Notification<String, RunnerState>> defaultNotificationSender;

    public void addListener(NotificationListener<Notification<String, RunnerState>> listener)
    {
        this.defaultNotificationSender.addListener(listener);

        listener.notificationReceived(new Notification<String, RunnerState>(this.getName(), this.getState()));
    }

    public void removeListener(NotificationListener listener)
    {
        this.defaultNotificationSender.removeListener(listener);
    }

    public void notifyAll(Notification<String, RunnerState> notification)
    {
        this.defaultNotificationSender.notifyAll(notification);
    }

    public void tryNotifyAll()
    {
        if(this.getState().changed())
        {
            this.defaultNotificationSender.notifyAll(new Notification<String, RunnerState>(this.getName(), this.getState()));
        }
    }
    // </editor-fold>

    @Override
    public void changeState(RunnerState.State aState)
    {
        synchronized (getState())
        {
            switch (getState().getState())
            {
                case INTERRUPTED:
                case INTERRUPTING:
                    if (aState == State.INTERRUPTED) this.getState().setState(State.INTERRUPTED);
                    break;
                default:
                    this.getState().setState(aState);
                    break;
            }
        }

        this.tryNotifyAll();
    }
    
    private Scenario scenario;
    private ThreadRunner thread;
    private TestcaseRunner testcaseRunner;
    private BufferMsg bufferMsg;
    private long startTimestamp;
    private boolean stopping;
    private int finallyCount;
    
    /** Creates a new instance of ScenarioRunner */
    public ScenarioRunner(TestcaseRunner aTestcaseRunner, Scenario scenario)
    {
        super(scenario.getName());

        this.defaultHierarchyMember = new DefaultHierarchyMember<TestcaseRunner, Object>();
        this.defaultNotificationSender = new DefaultNotificationSender<Notification<String, RunnerState>>();

        this.defaultHierarchyMember.setParent(aTestcaseRunner);

        this.setParameterPool(new ParameterPool(this, ParameterPool.Level.scenario, this.getParent().getParameterPool()));

        this.stopping = false;
        this.getState().setExecutionsEnd(1);
        changeState(State.OPEN_SUCCEEDED);
        this.bufferMsg = new BufferMsg();
        this.thread = null;
        this.scenario = scenario;
        testcaseRunner = aTestcaseRunner;
    }

    public void assertIsNotInterrupting() throws InterruptedExecutionException
    {
        this.assertIsNotInterrupting(0);
    }

    public void assertIsNotInterrupting(int stopCount) throws InterruptedExecutionException
    {
        if (this.getState().getState() == State.INTERRUPTED || this.getState().getState() == State.INTERRUPTING)
        {
            if(this.stopping && !isInFinally())
            {
                throw new InterruptedExecutionException("scenario runner is currently stopping");
            }
        }
    }

    /**
     * Starts the thread of the ScenarioRunner
     */
    synchronized public void start()
    {
        try
        {
            this.assertIsNotInterrupting();

            GlobalLogger.instance().getSessionLogger().debug(this, TextEvent.Topic.CORE, "ScenarioRunner started");

            changeState(RunnerState.State.RUNNING);
            this.getState().setExecutionsCurrent(0);
            this.getState().setProgression(0);
            getParameterPool().clear();

            Parameter parameter = new Parameter();
            parameter.add(getScenario().getName());
            getParameterPool().set("[scenarioName]", parameter);

            parameter = new Parameter();
            parameter.add(getScenario().getId());
            getParameterPool().set("[scenarioId]", parameter);

            this.tryNotifyAll();

            this.thread = ThreadPool.reserve().start(this);
        }
        catch (InterruptedExecutionException e)
        {
            GlobalLogger.instance().getSessionLogger().error(this, TextEvent.Topic.CORE, e, "ScenarioRunner exception");
            this.changeState(State.INTERRUPTED);
            this.release();
        }
        catch (Exception e)
        {
            GlobalLogger.instance().getSessionLogger().error(this, TextEvent.Topic.CORE, e, "ScenarioRunner exception");
            this.changeState(State.FAILED);
            this.release();
        }
    }

    /**
     * Stops the thread of the ScenarioRunner
     */
    synchronized public void stop()
    {
        synchronized(getState())
        {
            switch (getState().getState())
            {
                case SUCCEEDED:
                case FAILED:
                case INTERRUPTED:
                    changeState(State.INTERRUPTED);
                    break;
                default:
                    changeState(State.INTERRUPTING);
                    break;
            }
        }


        this.stopping = true;
        if (!isInFinally())
        {
            if (this.thread !=null) this.thread.interrupt();
        }

    }

    synchronized public boolean isInFinally(){
        return finallyCount > 0;
    }

    synchronized public void finallyEnter(){
        finallyCount++;
        
        // consume interrupted flag
        Thread.interrupted();
    }

    synchronized public void finallyExit(){
        finallyCount--;
    }

    public void free(){
        setParent(null);
        scenario = null;
    }

    private boolean interruptible = false;

    public boolean isInterruptible()
    {
        return this.interruptible;
    }

    synchronized public void setInterruptible(boolean interruptible)
    {
        this.interruptible = interruptible;
        Thread.interrupted();
    }

    /**
     * Method executed by the thread
     */
    public void run()
    {
        /**
         * Update logs and statistics: a new scenario is currently running
         */
        StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_SCENARIO, scenario.getName(), "_name"), scenario.getName());
        StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_SCENARIO, scenario.getName(), "_description"), scenario.getDescription());
        StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_SCENARIO, scenario.getName(), "_startNumber"), 1);
        StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_SCENARIO, scenario.getName(), "_currentNumber"), 1);
        GlobalLogger.instance().getSessionLogger().info(this, TextEvent.Topic.CORE, "Scenario running");

        /**
         * Backup the start time of the scenario
         */
        startTimestamp = System.currentTimeMillis();

        /**
         * Then execute the operations of the scenario
         */
        try
        {
            scenario.executeScenario(this);
        }
        catch (Exception e)
        {
            GlobalLogger.instance().getSessionLogger().error(this, TextEvent.Topic.CORE, e, "Exception in ScenarioRunner\n");

            /**
             * If any Exception happen while executing the operations then the
             * test go in FAILING state if he is not already in INTERRUPTING state
             * because of the stop() method.
             */
            changeState(RunnerState.State.FAILING);
        }

        Thread.interrupted();

        /**
         * Then execute the operations of the finally
         */
        try
        {
            scenario.executeFinally(this);
        }
        catch (Exception e)
        {
            
            GlobalLogger.instance().getSessionLogger().error(this, TextEvent.Topic.CORE, e, "Exception in finally in ScenarioRunner\n");

            /**
             * If any Exception happen while executing the operations then the
             * test go in FAILING state if he is not already in INTERRUPTING state
             * because of the stop() method.
             */
            changeState(RunnerState.State.FAILING);
        }

        this.thread = null;

        /**
         * Update the statistics : a scenario ended
         */
        StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_SCENARIO, scenario.getName(), "_currentNumber"), -1);
        StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_SCENARIO, scenario.getName(), "_completeNumber"), 1);

        /**
         * Add the duration of the execution to the statistics and to the logs
         */
        long endTimestamp = System.currentTimeMillis();
        float duration_stats = ((float) (endTimestamp - startTimestamp) / 1000);
        StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_SCENARIO, scenario.getName(), "_durationTime"), duration_stats);

        
        
        /**
         * Finalize and clean up things.
         */
        this.bufferMsg.clear();

        GlobalLogger.instance().getSessionLogger().debug(this, TextEvent.Topic.CORE, "ScenarioRunner ended: notify TestcaseRunner");

        /**
         * Compute the final state of the scenario and increments counters
         * accordingly to that state.
         * Putting the scenario state in a final test should be the last thing to do.
         */
        synchronized(getState())
        {
            this.getState().setExecutionsCurrent(1);
            switch (getState().getState())
            {
                case RUNNING:
                    GlobalLogger.instance().getSessionLogger().info(this, TextEvent.Topic.CORE, "ScenarioRunner OK (duration=", duration_stats, "s)");
                    changeState(RunnerState.State.SUCCEEDED);
                    break;
                case INTERRUPTING:
                    GlobalLogger.instance().getSessionLogger().error(this, TextEvent.Topic.CORE, "ScenarioRunner interrupted (duration=", duration_stats, "s)");
                    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_SCENARIO, scenario.getName(), "_failedNumber"), 1);
                    changeState(RunnerState.State.INTERRUPTED);
                    break;
                case FAILING:
                    GlobalLogger.instance().getSessionLogger().error(this, TextEvent.Topic.CORE, "ScenarioRunner KO (duration=", duration_stats, "s)");
                    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_SCENARIO, scenario.getName(), "_failedNumber"), 1);
                    changeState(RunnerState.State.FAILED);
                    break;
            }
        }

        TextListenerProviderRegistry.instance().dispose(this);

        // legacy: not used anymore
        release();
    }

    public Scenario getScenario()
    {
        return scenario;
    }

    public TestcaseRunner getTestcaseRunner()
    {
        return testcaseRunner;
    }

    /** adds a message to the stack and notify the Thread waiting for it */
    public void dispatchMessage(Msg msg)
    {
        bufferMsg.dispatchMessage(msg);
    }

    public BufferMsg getBufferMsg()
    {
        return bufferMsg;
    }
    
    public void stackFunctionParameterPool() {
        ParameterPool parameterPool = new ParameterPool(this, ParameterPool.Level.function, getParameterPool());
        setParameterPool(parameterPool);
    }

    public void unstackFunctionParameterPool() {
        if (getParameterPool().level == ParameterPool.Level.function) {
            setParameterPool(getParameterPool().getParent());
        }
    }

    /** Implicit message for setFromMessage operation */
    private Msg currentMsg = null;

    public Msg getCurrentMsg() {
		return currentMsg;
	}

	public void setCurrentMsg(Msg currentMsg) {
		this.currentMsg = currentMsg;
	}
	
	public String toString() {
		return scenario.toString();
	}
	
}
