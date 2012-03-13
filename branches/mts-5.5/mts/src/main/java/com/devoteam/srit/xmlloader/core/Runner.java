/*
 * Runner.java
 *
 * Created on 31 mai 2007, 16:22
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
