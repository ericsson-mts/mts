/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.master.master.utils.states;

/**
 *
 * @author Gwenhael
 */
public class ConnectionState {
    public static final int CONNECTED = 0;
    public static final int DISCONNECTED = 1;
    public static final int CONNECTION_FAILURE = 2;
        
    private int _value;
    
    public ConnectionState(int value){
        _value = value;
    }
    
    public int getValue(){
        return _value;
    }    
}
