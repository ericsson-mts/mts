/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.core.utils.expireshashmap;

public class ExpirableValue<V extends Removable>
{

    private long expirationTimestamp;
    private V value;

    public ExpirableValue(V value, long timestamp)
    {
        this.expirationTimestamp = timestamp;
        this.value = value;
    }

    public long getTimestamp()
    {
        return this.expirationTimestamp;
    }

    public boolean isExpired()
    {
        return this.expirationTimestamp < System.currentTimeMillis();
    }

    public V getValue()
    {
        return this.value;
    }
    
    @Override
    public String toString()
    {
        return this.value.toString();
    }
    
    @Override
    public int hashCode()
    {
        return this.value.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        return this.value.equals(obj);
    }
}