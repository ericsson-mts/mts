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
        	if (this.attributes[i] != null)
        	{
        		this.attributes[i] = this.attributes[i].trim();
        	}
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
