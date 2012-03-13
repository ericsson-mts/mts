/*
 * StatKey.java
 *
 * Created on 28 janvier 2008, 14:07
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.core.newstats;

import java.io.Serializable;

/**
 * Used as a key in the StatPool for indexing the {@link com.devoteam.srit.xmlloader.core.newstats.IStatCounter}
 * hash key is based on the attributes passed in the constructor.
 *
 * @author mjagodzinski
 */

public final class StatKey implements Comparable<StatKey>, Serializable
{
    private int hashCode;
    private boolean hashCodeComputed;
    
    private String[] attributes;

    public StatKey(String... attributes)
    {
        this.hashCodeComputed = false;
        this.attributes = attributes;
        for (int i = 0; i < this.attributes.length; i++)
        {
        	this.attributes[i] = this.attributes[i].trim(); 
        }
    }

    public String getAttribute(int index)
    {
        if (index >= attributes.length)
        {
            return null;
        }
        return attributes[index];
    }

    public String[] getAllAttributes()
    {
        return attributes;
    }

    public int getAttributesLength()
    {
        return attributes.length;
    }

    public String getLastAttribute()
    {
        return attributes[attributes.length - 1];
    }
   
    @Override
    public int hashCode()
    {
        if(!hashCodeComputed)
        {
            StringBuilder StringBuilder = new StringBuilder();
            for(String attribute:attributes)
            {
                StringBuilder.append(attribute);
            }
            this.hashCode = StringBuilder.toString().hashCode();
            this.hashCodeComputed = true;
        }
        
        return hashCode;
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof StatKey)
        {
            return this.hashCode() == object.hashCode();
        }
        else
        {
            return false;
        }
    }

    @Override
    public String toString()
    {
        StringBuilder StringBuilder = new StringBuilder();
        for(String attribute:attributes)
        {
            StringBuilder.append('/').append(attribute);
        }
        return StringBuilder.toString();
    }

    public int compareTo(StatKey comparedStatKey)
    {
        //if the hash codes are the same - they must be equal
        if (this.equals(comparedStatKey))
        {
            return 0;
        }
        
        int comparedStatKeyLength = comparedStatKey.getAttributesLength();
        for (int i = 0; i < attributes.length; i++)
        {
            if (i >= comparedStatKeyLength)
            {
                return 1;
            }
            int attrRes = this.attributes[i].compareTo(comparedStatKey.getAttribute(i));
            if (attrRes != 0)
            {
                return attrRes;
            }
        }
        
        //this should never happen
        throw new RuntimeException("Two statKey doesn't have equal hash codes but all of their attributes are equal");
    }
}
