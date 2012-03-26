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