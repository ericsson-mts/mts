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
 * @author gpasquiers
 */
public class RunnerStateSequence
{
    private State state;
    
    private boolean changed;
    
    public RunnerStateSequence()
    {
        this.reset();
    }

    public void reset()
    {
        this.state = State.UNINITIALIZED;
        this.changed = true;
    }

    public boolean changed()
    {
        boolean oldChanged = this.changed;
        this.changed = false;
        return oldChanged;
    }
    
    public State getState()
    {
        return this.state;
    }
    
    public void updateState(State state)
    {
        State oldState = this.state;
        
        switch(this.state){
            case UNINITIALIZED:
                switch(state){
                    default:
                        this.state=state;
                        break;
                    case UNINITIALIZED:
                }
                break;
            case OPENING:
                switch(state){
                    default:
                        this.state=state;
                        break;
                    case UNINITIALIZED:
                    case OPENING:
                }
                break;
            case OPEN_SUCCEEDED:
                switch(state){
                    default:
                        this.state=state;
                        break;
                    case UNINITIALIZED:
                    case OPENING:
                    case OPEN_SUCCEEDED:
                }
                break;
            case OPEN_FAILED:
                switch(state){
                    default:
                        this.state=state;
                        break;
                    case UNINITIALIZED:
                    case OPENING:
                    case OPEN_SUCCEEDED:
                    case OPEN_FAILED:
                }
                break;
            case RUNNING:
                switch(state){
                    default:
                        this.state=state;
                        break;
                    case UNINITIALIZED:
                    case OPENING:
                    case OPEN_SUCCEEDED:
                    case RUNNING:
                }
                break;
            case FAILING:
                switch(state){
                    default:
                        this.state=state;
                        break;
                    case SUCCEEDED:
                        this.state = State.FAILED;
                        break;
                    case UNINITIALIZED:
                    case OPENING:
                    case OPEN_SUCCEEDED:
                    case RUNNING:
                    case FAILING:
                }
                break;
            case INTERRUPTING:
                switch(state){
                    default:
                        this.state=state;
                        break;
                    case SUCCEEDED:
                    case FAILED:
                        this.state = State.INTERRUPTED;
                        break;
                    case UNINITIALIZED:
                    case OPENING:
                    case OPEN_SUCCEEDED:
                    case RUNNING:
                    case FAILING:
                    case INTERRUPTING:
                }
                break;
            case SUCCEEDED:
                switch(state){
                    default:
                        this.state=state;
                        break;
                    case UNINITIALIZED:
                    case OPENING:
                    case OPEN_SUCCEEDED:
                    case RUNNING:
                    case FAILING:
                    case INTERRUPTING:
                    case SUCCEEDED:
                }
                break;
            case FAILED:
                switch(state){
                    default:
                        this.state=state;
                        break;
                    case UNINITIALIZED:
                    case OPENING:
                    case OPEN_SUCCEEDED:
                    case RUNNING:
                    case FAILING:
                    case INTERRUPTING:
                    case SUCCEEDED:
                    case FAILED:
                }
                break;
            case INTERRUPTED:
                break;                
        }
        
        if(this.state != oldState)
        {
            this.changed = true;
        }
    }
    
}
