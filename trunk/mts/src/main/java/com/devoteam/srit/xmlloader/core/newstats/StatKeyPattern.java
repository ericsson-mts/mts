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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used as a key in the StatPool for indexing the {@link com.devoteam.srit.xmlloader.core.newstats.IStatCounter}
 * hash key is based on the attributes passed in the constructor.
 *
 * @author mjagodzinski
 */
public final class StatKeyPattern
{
    private String[] attributes;
    private Pattern[] patterns;

    /**
     *@param attribute array of attributes that form the key
     *
     */

    public StatKeyPattern(StatKey key)
    {
        this(key.getAllAttributes());
    }

    /**
     *
     * This is just a conveniance construcor, slower that the constructor with the array. Instead of an array takes a string
     * character with the attributes separated by the '/'. Remember to start with the '/' character ex. /aa/bb/cc is correct aaa/vvv/bbb is not correct
     * @param pathLikeAttributes attributes represented in a path like line, ex. /aaa/bbb/ccc becomes evetually {"aaa","bbb","ccc"}
     */
    
    public StatKeyPattern(String... attributes)
    {
        this.attributes = attributes;
        this.patterns = new Pattern[attributes.length];
        
        for(int i=0; i<attributes.length; i++)
        {
            patterns[i] = Pattern.compile(attributes[i]);
        }
    }

    /**
     *Return the attributs in the path like format
     */
    @Override
    public String toString()
    {
        StringBuilder StringBuilder = new StringBuilder();
        for(String attribute:attributes)
        {
            StringBuilder.append("/").append(attribute);
        }
        return StringBuilder.toString();
    }
    
    public boolean matchesStrict(StatKey key)
    {
        String[] keyAttributes = key.getAllAttributes();
        
        if(this.attributes.length != keyAttributes.length)
        {
            return false;
        }
        
        return this.matchesLax(key);
    }

    public boolean matchesLax(StatKey key)
    {
        String[] keyAttributes = key.getAllAttributes();
        
        if(this.attributes.length > keyAttributes.length)
        {
            return false;
        }
        
        for(int i=0; i<patterns.length; i++)
        {
        	Matcher matcherI = null;
        	if (patterns[i] != null && keyAttributes[i] != null)
        	{
        		matcherI = patterns[i].matcher(keyAttributes[i]);
        		if (matcherI != null)
        		{
        			if (!matcherI.matches())
        			{
        				return false;
        			}
        		}
        		else
        		{
        			System.out.println("NullPointerException : key = " + key.toString());
        		}
        	}
        	else
        	{
        		System.out.println("NullPointerException : key = " + key.toString());
        	}
        }
        
        return true;
    }
}
