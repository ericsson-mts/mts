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
 * RunnerState.java
 *
 * Created on 1 juin 2007, 10:16
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;

/**
 *
 * @author gpasquiers
 */
public class RunnerState implements Serializable
{
    public static enum State
    {
        UNINITIALIZED,
        OPENING,
        OPEN_SUCCEEDED,
        OPEN_FAILED,
        // Running states
        RUNNING,
        FAILING,
        INTERRUPTING,
        // Final states
        SUCCEEDED,
        FAILED,
        INTERRUPTED,
    }
    
    private int progression;
    
    private int index;
    
    private boolean changed;
    
    private State state;

    private long timeBegin;
    private long timeCurrent;
    private long timeCurrentBack;
    private long timeEnd;

    private long executionsCurrent;
    private long executionsEnd;

    /** Creates a new instance of RunnerState */
    public RunnerState()
    {
        this.timeBegin = 0;
        this.timeCurrent = 0;
        this.timeCurrentBack = 0;
        this.timeEnd = 0;
        this.executionsCurrent = 0;
        this.executionsEnd = 0;
        this.progression = 0;
        this.changed = true;
        this.index = -1;
        this.state = State.UNINITIALIZED;
    }
    
    public boolean changed()
    {
        if(this.changed)
        {
            this.changed = false;
            return true;
        }
        return false;
    }
    
    public void setIndex(int index)
    {
        this.index = index;
    }
    
    public int getIndex()
    {
        return this.index;
    }
    
    public void setState(State state)
    {
        if(this.state != state)
        {
            this.changed = true;
        }
        
        this.state = state;
    }
    
    public State getState()
    {
        return state;
    }
    
    public void setProgression(int progression)
    {
        if(this.progression != progression) this.changed = true;
        
        this.progression = progression;
    }
    
    public int getProgression()
    {
        return progression;
    }
    
    @Override
    public String toString()
    {
        return this.state + " / " + this.progression + " / " + this.index;
    }

    public String toPopupHTMLString()
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<html>").append(this.state).append(" (").append(this.getProgression()).append("%) ").append("<br/>");

        if(this.timeEnd > 0)
        {
            String timeStrStart = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date(this.getTimeBegin()));
            String timeStrCurrent = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date(this.getTimeCurrent()));
            String timeStrEnd = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date(this.getTimeEnd()));

            stringBuilder.append("start : ").append(timeStrStart).append("<br/>");
            stringBuilder.append("current : ").append(timeStrCurrent).append("<br/>");
            stringBuilder.append("end : ").append(timeStrEnd).append("<br/>");
        }
        
        if(this.executionsEnd > 0)
        {
            stringBuilder.append("executions: ").append(this.getExecutionsCurrent()).append("/").append(this.getExecutionsEnd()).append("<br/>");
        }

        stringBuilder.append("</html>");
        
        return stringBuilder.toString();
    }
    /**
     * @return the timeBegin
     */
    public long getTimeBegin()
    {
        return timeBegin;
    }

    /**
     * @param timeBegin the timeBegin to set
     */
    public void setTimeBegin(long timeBegin)
    {
        this.timeBegin = timeBegin;
    }

    /**
     * @return the timeCurrent
     */
    public long getTimeCurrent()
    {
        return timeCurrent;
    }

    /**
     * @param timeCurrent the timeCurrent to set
     */
    public void setTimeCurrent(long timeCurrent)
    {
        this.timeCurrent = timeCurrent;

        if(this.timeBegin > 0 && this.timeEnd > 0)
        {
            int timeProgression = (int) (100 * ((this.timeCurrent - this.timeBegin)) / (this.timeEnd - this.timeBegin));
            if(timeProgression > this.getProgression()) this.setProgression(timeProgression);
        }

        if(this.timeCurrent - this.timeCurrentBack> 1000)
        {
            this.timeCurrentBack = this.timeCurrent;
            this.changed = true;
        }
    }

    /**
     * @return the timeEnd
     */
    public long getTimeEnd()
    {
        return timeEnd;
    }

    /**
     * @param timeEnd the timeEnd to set
     */
    public void setTimeEnd(long timeEnd)
    {
        this.timeEnd = timeEnd;
    }

    /**
     * @return the executionsCurrent
     */
    public long getExecutionsCurrent()
    {
        return executionsCurrent;
    }

    /**
     * @param executionsCurrent the executionsCurrent to set
     */
    public void setExecutionsCurrent(long executionsCurrent)
    {
        this.executionsCurrent = executionsCurrent;

        if(this.executionsEnd > 0)
        {
            int timeProgression = (int) (100 * this.executionsCurrent / this.executionsEnd);
            if(timeProgression > this.getProgression()) this.setProgression(timeProgression);
        }
        
    }

    /**
     * @return the executionsEnd
     */
    public long getExecutionsEnd()
    {
        return executionsEnd;
    }

    /**
     * @param executionsEnd the executionsEnd to set
     */
    public void setExecutionsEnd(long executionsEnd)
    {
        this.executionsEnd = executionsEnd;
    }

    public void set(RunnerState runnerState)
    {
        this.setState(runnerState.getState());
        this.setTimeBegin(runnerState.getTimeBegin());
        this.setTimeCurrent(runnerState.getTimeCurrent());
        this.setTimeEnd(runnerState.getTimeEnd());
        this.setExecutionsCurrent(runnerState.getExecutionsCurrent());
        this.setExecutionsEnd(runnerState.getExecutionsEnd());
        this.setProgression(runnerState.getProgression());
    }
}
