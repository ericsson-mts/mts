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
 * Runner.java
 *
 * Created on 31 mai 2007, 16:22
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core;


import com.devoteam.srit.xmlloader.core.log.TextListenerKey;
import java.util.concurrent.Semaphore;

import gp.utils.scheduler.Scheduler;

/**
 *
 * @author gpasquiers
 */
public abstract class Runner implements TextListenerKey
{
    static protected Scheduler scheduler = new Scheduler(3);

    private Semaphore semaphore;
    
    private RunnerState state;
    
    private ParameterPool variables;
    
    private String name;
    
    public Runner(String name)
    {
        this.state = new RunnerState();
        this.semaphore = new Semaphore(0);
        this.name = name;
    }

    public void init() throws Exception
    {
        
    }

    final public String getName()
    {
        return name;
    }
    
    final public RunnerState getState()
    {
        return state;
    }
    
    public void changeState(RunnerState.State aState)
    {
        if(state.getState() != aState)
        {
            state.setState(aState);
        }
    }
    
    final public void acquire() throws InterruptedException
    {
        semaphore.acquire();
    }
    
    final public void release()
    {
        semaphore.release();
    }

    final public ParameterPool getParameterPool()
    {
        return variables;
    }

    final public void setParameterPool(ParameterPool variables)
    {
        this.variables = variables;
    }
    
    public abstract void start();

    public abstract void stop();
}
