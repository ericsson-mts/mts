/*
 * IStatCounter.java
 *
 * Created on 23 janvier 2008, 11:53
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.core.newstats;

import java.io.Serializable;

/**
 * Interface that must be implemented by all counters held by the StatPool class
 * @author mjagodzinski
 */
public interface IStatCounter extends Serializable, Cloneable
{
    /**
     * Adds a value to the counter
     * @param value
     */
    public void addValue(Object value);

    /**
     * Adds a value to the counter with the possibility of specifying the timestamp
     * @param value
     */
    public void addValue(Object value, long timestamp);

    /**
     * Sums this counter values with values of another counter
     * @param operand
     */
    public void sum(IStatCounter operand);

    

    public IStatCounter clone();
}
