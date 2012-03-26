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
 */

package com.devoteam.srit.xmlloader.core;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;

/**
 *
 * @author gpasquiers
 */
public class RunnerState implements Serializable {

    public static final int F_FAILED        = Integer.parseInt("00000000000000001", 2);
    public static final int F_INTERRUPTED   = Integer.parseInt("00000000000000010", 2);
    public static final int F_STARTED       = Integer.parseInt("00000000000000100", 2);
    
    // flags before (right) this limit : merging with OR
    public static final int F_LOWER_LIMIT   = Integer.parseInt("00000000100000000", 2);
    // flags after (left) this limit : merging with AND
    
    public static final int F_OPENED        = Integer.parseInt("10000000000000000", 2);
    public static final int F_FINISHED      = Integer.parseInt("00100000000000000", 2);

    private int _flags;

    public int _progression;

    public long _timeBegin;
    public long _timeCurrent;
    public long _timeEnd;
    
    public long _executionsCurrent;
    public long _executionsEnd;

    /** Creates a new instance of RunnerState */
    public RunnerState() {
        _flags = Integer.parseInt("00000000000000000", 2);
        _timeBegin = 0;
        _timeCurrent = 0;
        _timeEnd = 0;
        _executionsCurrent = 0;
        _executionsEnd = 0;
        _progression = 0;
    }

    public RunnerState(RunnerState other) {
        _flags = other._flags;
        _timeBegin = other._timeBegin;
        _timeCurrent = other._timeCurrent;
        _timeEnd = other._timeEnd;
        _executionsCurrent = other._executionsCurrent;
        _executionsEnd = other._executionsEnd;
        _progression = other._progression;
    }

    public void setFlag(int flag, boolean value){
        if(value){
            _flags |= flag;
        }
        else{
            flag ^= 0xffff;
            _flags &= flag;
        }
    }

    public boolean getFlag(int flag){
        return (_flags & flag) == flag;
    }

    public int getFlags(){
        return _flags;
    }

    public void setFlags(int value){
        _flags = value;
    }

    public void merge(RunnerState other){
        _flags |= (other._flags & 0x00ff);
        _flags &= (other._flags | 0x00ff);

        if(_timeEnd <= other._timeEnd){
            _timeBegin = other._timeBegin;
            _timeCurrent = other._timeCurrent;
            _timeEnd = other._timeEnd;
        }

        if(other._progression <= _progression){
            _progression = other._progression;
        }

        _executionsCurrent = 0;
        _executionsEnd = 0;
    }

    @Override
    public RunnerState clone(){
        return new RunnerState(this);
    }

    public boolean sameValuesAs(RunnerState other){
        if(!(other._flags == _flags)){
            return false;
        }
        // only use progression when comparing: limit notification frequency to 100 per life cycle
        if(!(other._progression == _progression)){
            return false;
        }
        
        /*if(!(other._executionsCurrent == _executionsCurrent)){
            return false;
        }
        if(!(other._executionsEnd == _executionsEnd)){
            return false;
        }
        if(!(other._timeBegin == _timeBegin)){
            return false;
        }
        if(!(other._timeCurrent == _timeCurrent)){
            return false;
        }
        if(!(other._timeEnd == _timeEnd)){
            return false;
        }*/

        return true;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        
        if(getFlag(F_OPENED)){
            stringBuilder.append("OPENED.");
        }
        if(getFlag(F_STARTED)){
            stringBuilder.append("STARTED.");
        }
        if(getFlag(F_FINISHED)){
            stringBuilder.append("FINISHED.");
        }
        if(getFlag(F_FAILED)){
            stringBuilder.append("FAILED.");
        }
        if(getFlag(F_INTERRUPTED)){
            stringBuilder.append("INTERRUPTED.");
        }

        stringBuilder.append("@").append(_progression).append("%");
        return stringBuilder.toString();
    }

    public String toPopupHTMLString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<html>").append(toLegacyStatus()).append(" (").append(_progression).append("%) ").append("<br/>");

        if (this._timeEnd > 0) {
            String timeStrStart = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date(_timeBegin));
            String timeStrCurrent = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date(_timeCurrent));
            String timeStrEnd = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date(_timeEnd));

            stringBuilder.append("start : ").append(timeStrStart).append("<br/>");
            stringBuilder.append("current : ").append(timeStrCurrent).append("<br/>");
            stringBuilder.append("end : ").append(timeStrEnd).append("<br/>");
        }

        if (this._executionsEnd > 0) {
            stringBuilder.append("executions: ").append(_executionsCurrent).append("/").append(_executionsEnd).append("<br/>");
        }

        stringBuilder.append("</html>");

        return stringBuilder.toString();
    }

    // convenience methods
    public boolean isUninitialized(){ return _flags == 0;}
    public boolean isOpened(){ return getFlag(F_OPENED);}
    public boolean isStarted(){ return getFlag(F_STARTED);}
    public boolean isFinished(){ return getFlag(F_FINISHED);}
    public boolean isFailed(){ return getFlag(F_FAILED);}
    public boolean isInterrupted(){ return getFlag(F_INTERRUPTED);}
    public boolean couldNotStart(){ return (isFailed() || isInterrupted()) && (!isOpened() && !isStarted());}
    
    public String toLegacyStatus(){
        String ret;
        
        if(isUninitialized()){
            ret = "OPENING";
        }
        else if(isOpened() && !isFinished() && (!isFailed() && !isInterrupted())){
            ret = "OPENED";
        }
        else if(couldNotStart()){
            ret = "OPEN_FAILED";
        }
        else if(isStarted() && !isFinished() && (!isFailed() && !isInterrupted())){
            ret = "RUNNING";
        }
        else if(isStarted() && !isFinished() && isFailed()){
            ret = "FAILING";
        }
        else if(isStarted() && !isFinished() && isInterrupted()){
            ret = "INTERRUPTING";
        }
        else if(isFinished() && (!isInterrupted() && !isFailed())){
            ret = "SUCCEEDED";
        }
        else if(isFinished() && isInterrupted()){
            ret = "INTERRUPTED";
        }
        else if(isFinished() && isFailed()){
            ret = "FAILED";
        }
        else{
            ret = "UNKNOWN";
        }
        
        return ret;
    }
}
