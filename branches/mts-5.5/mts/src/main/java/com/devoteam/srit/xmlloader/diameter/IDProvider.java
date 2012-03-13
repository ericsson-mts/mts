/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.diameter;

/**
 *
 * @author gpasquiers
 */
public class IDProvider {
    private static int id = (int) (Math.random() * Integer.MAX_VALUE + Math.random() * Integer.MIN_VALUE);

    public synchronized static int nextId(){
        return id++;
    }
}
