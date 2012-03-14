/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.master.master.utils.states;

/**
 *
 * @author Gwenhael
 */
public class DeploymentState {
    public static final int UNDEPLOYED = 0;
    public static final int STARTED = 1;
    public static final int SUCCEEDED = 2;
    public static final int FAILED = 3;
        
    private int _value;
    
    public DeploymentState(int value){
        _value = value;
    }
    
    public int getValue(){
        return _value;
    }
}
