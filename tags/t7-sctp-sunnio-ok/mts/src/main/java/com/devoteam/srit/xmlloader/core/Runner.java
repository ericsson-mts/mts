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

import com.devoteam.srit.xmlloader.core.log.TextListenerKey;

import gp.utils.scheduler.Scheduler;

/**
 *
 * @author gpasquiers
 */
public class Runner implements TextListenerKey {

    static protected Scheduler _scheduler = new Scheduler(3);
    private RunnerState _state;
    private ParameterPool _variables;
    private String _name;

    public Runner(String name) {
        resetState();
        _name = name;
    }

    public void init() throws Exception {
    }

    final public String getName() {
        return _name;
    }

    final public RunnerState getState() {
        return _state;
    }

    final public void setState(RunnerState value) {
        _state = value;
    }

    final public void resetState() {
        _state = new RunnerState();
    }

    final public ParameterPool getParameterPool() {
        return _variables;
    }

    final public void setParameterPool(ParameterPool variables) {
        _variables = variables;
    }
}
