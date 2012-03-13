/*
 * StatKey.java
 *
 * Created on 28 janvier 2008, 14:07
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.core.newstats;

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
            if(!patterns[i].matcher(keyAttributes[i]).matches())
            {
                return false;
            }
        }
        
        return true;
    }
}
