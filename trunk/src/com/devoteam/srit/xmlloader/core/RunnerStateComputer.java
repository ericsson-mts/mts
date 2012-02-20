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
package com.devoteam.srit.xmlloader.core;

import com.devoteam.srit.xmlloader.core.RunnerState.State;

/**
 *
 * @author gege
 */
public class RunnerStateComputer
{

    private State[] states;
    private int[] progressions;
    private int progression;
    private RunnerStateSequence state;
    private RunnerStateSequence realState;
    private boolean shouldCompute;
    private boolean canBeFinal;

    private long[] executionsCurrentArray;
    private long[] executionsEndArray;
    private long executionsCurrent;
    private long executionsEnd;

    private long timeBegin;
    private long timeCurrent;
    private long timeEnd;


    public RunnerStateComputer(int size)
    {
        this.canBeFinal = true;
        this.states = new State[size];
        this.progressions = new int[size];
        this.executionsCurrentArray = new long[size];
        this.executionsEndArray = new long[size];
        this.state = new RunnerStateSequence();
        this.realState = new RunnerStateSequence();
        this.reset();
    }

    public void setCanBeFinal(boolean canBeFinal)
    {
        this.canBeFinal = canBeFinal;
        this.shouldCompute = true;
    }
    
    public void reset()
    {
        this.reset(this.states.length);
    }
    
    synchronized public void reset(int size)
    {
        if(size != this.states.length)
        {
            this.states = new State[size];
            this.progressions = new int[size];
            this.executionsCurrentArray = new long[size];
            this.executionsEndArray = new long[size];
        }

        this.state.reset();
        this.realState.reset();
        
        for (int i = 0; i < this.states.length; i++)
        {
            this.states[i] = State.UNINITIALIZED;
        }

        for (int i = 0; i < this.progressions.length; i++)
        {
            this.progressions[i] = 0;
            this.executionsCurrentArray[i] = 0;
            this.executionsEndArray[i] = 0;
        }

        this.progression = 0;
        this.shouldCompute = true;

        this.executionsCurrent = 0;
        this.executionsEnd = 0;

        this.timeBegin = 0;
        this.timeCurrent = 0;
        this.timeEnd = 0;
    }

    synchronized public void update(RunnerState runnerState)
    {
        int index = runnerState.getIndex();
        this.progression -= this.progressions[index];
        this.progressions[index] = runnerState.getProgression();
        this.progression += this.progressions[index];

        executionsCurrent -= executionsCurrentArray[index];
        executionsCurrentArray[index] = runnerState.getExecutionsCurrent();
        executionsCurrent += executionsCurrentArray[index];
        
        executionsEnd -= executionsEndArray[index];
        executionsEndArray[index] = runnerState.getExecutionsEnd();
        executionsEnd += executionsEndArray[index];

        if(runnerState.getTimeEnd() > 0 && runnerState.getTimeEnd() >= timeEnd )
        {
            timeBegin = runnerState.getTimeBegin();
            timeCurrent = runnerState.getTimeCurrent();
            timeEnd = runnerState.getTimeEnd();
        }

        if (this.states[index] != (this.states[index] = runnerState.getState()))
        {
            this.shouldCompute = true;
        }
    }

    synchronized public RunnerState getComputedState()
    {
        if (this.shouldCompute)
        {
            // compute state
            State currentState = State.UNINITIALIZED;
            for (State inputState : this.states)
            {
                switch (currentState)
                {
                    case UNINITIALIZED:
                        currentState = inputState;
                        break;
                    case OPENING:
                        break;
                    case OPEN_SUCCEEDED:
                        switch (inputState)
                        {
                            case UNINITIALIZED:
                                currentState = State.OPENING;
                                break;
                            default:
                                currentState = inputState;
                                break;
                        }
                        break;
                    case OPEN_FAILED:
                        switch (inputState)
                        {
                            case UNINITIALIZED:
                                currentState = State.OPENING;
                                break;
                            case OPEN_SUCCEEDED:
                                break;
                            default:
                                currentState = inputState;
                                break;
                        }
                        break;
                    case RUNNING:
                        switch (inputState)
                        {
                            case FAILING:
                            case INTERRUPTING:
                                currentState = inputState;
                                break;
                        }
                        break;
                    case FAILING:
                        switch (inputState)
                        {
                            case INTERRUPTING:
                                currentState = inputState;
                                break;
                        }
                        break;
                    case INTERRUPTING:
                        break;
                    case SUCCEEDED:
                        switch (inputState)
                        {
                            case SUCCEEDED:
                            case RUNNING:
                            case FAILED:
                            case FAILING:
                            case INTERRUPTED:
                            case INTERRUPTING:
                                currentState = inputState;
                                break;
                            default:
                                currentState = State.RUNNING;
                                break;
                        }
                        break;
                    case FAILED:
                        switch (inputState)
                        {
                            case SUCCEEDED:
                                break;
                            case FAILED:
                            case FAILING:
                            case INTERRUPTED:
                            case INTERRUPTING:
                                currentState = inputState;
                                break;
                            default:
                                currentState = State.FAILING;
                                break;
                        }
                        break;
                    case INTERRUPTED:
                        switch (inputState)
                        {
                            case SUCCEEDED:
                            case FAILED:
                                break;
                            case INTERRUPTED:
                            case INTERRUPTING:
                                currentState = inputState;
                                break;
                            default:
                                currentState = State.INTERRUPTING;
                                break;
                        }

                        break;
                }
            }

            this.realState.updateState(currentState);

            if(!canBeFinal)
            {
                switch(currentState)
                {
                    case INTERRUPTED:
                        currentState = State.INTERRUPTING;
                        break;
                    case SUCCEEDED:
                        currentState = State.RUNNING;
                        break;
                    case FAILED:
                        currentState = State.FAILING;
                        break;
                }
            }
            
            
            this.state.updateState(currentState);
            this.shouldCompute = false;
        }
        
        RunnerState currentRunnerState = new RunnerState();

        // compute progress
        currentRunnerState.setTimeBegin(timeBegin);
        currentRunnerState.setTimeEnd(timeEnd);
        currentRunnerState.setTimeCurrent(timeCurrent);

        currentRunnerState.setExecutionsEnd(executionsEnd);
        currentRunnerState.setExecutionsCurrent(executionsCurrent);

        currentRunnerState.setState(state.getState());
        return currentRunnerState;
    }
    
    synchronized public RunnerState getRealComputedState()
    {
        this.getComputedState();
        RunnerState currentRunnerState = new RunnerState();

        // compute progress
        currentRunnerState.setTimeBegin(timeBegin);
        currentRunnerState.setTimeEnd(timeEnd);
        currentRunnerState.setTimeCurrent(timeCurrent);

        currentRunnerState.setExecutionsEnd(executionsEnd);
        currentRunnerState.setExecutionsCurrent(executionsCurrent);

        currentRunnerState.setState(realState.getState());
        return currentRunnerState;
    }

    public boolean realComputedStateChanged()
    {
        return this.realState.changed();
    }
    
    public boolean computedStateChanged()
    {
        return this.state.changed();
    }
    
    @Override
    public String toString()
    {
        String ret = "";
        
        for(State aState:this.states)
        {
            ret += aState + " - ";
        }
        
        ret += "======>" + this.getComputedState().getState();
        
        return ret;
    }
}
