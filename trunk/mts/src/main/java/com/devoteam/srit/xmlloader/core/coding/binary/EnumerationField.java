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

package com.devoteam.srit.xmlloader.core.coding.binary;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import gp.utils.arrays.Array;
import gp.utils.arrays.SupArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;


/**
 *
 * @author indiaye
 */
public class EnumerationField extends IntegerField
{
	
    private Map<Integer, String> labelsByValue = new HashMap<Integer, String>();
    private Map<String, Integer> valuesByLabel = new HashMap<String, Integer>();

    private List<EnumRange> ranges = new ArrayList<EnumRange>();
	
    public EnumerationField(Element rootXML) 
    {
        super(rootXML);
        
        List<Element> list = rootXML.elements("enum");
        for (Element elemEnum : list) 
        {
        	String valueStr = elemEnum.attributeValue("value");
        	String nameStr = elemEnum.attributeValue("name");
        	int iPos = valueStr.indexOf('-');
        	if (iPos >= 0)
        	{
        		String beginStr = valueStr.substring(0, iPos);
        		String endStr = valueStr.substring(iPos + 1);
        		EnumRange range = new EnumRange(beginStr, endStr, nameStr);
        		ranges.add(range);
	            this.valuesByLabel.put(nameStr, range.getBegin());
        	}
        	else
        	{
	        	byte[] valueBytes = Utils.parseBinaryString(valueStr);
	        	int value = (int) valueBytes[0] & 0xFF;
	            this.valuesByLabel.put(nameStr, value);
	            this.labelsByValue.put(value, nameStr);
        	}
        }

    }

    @Override
    public void setValue(String value, int offset, SupArray array) throws Exception 
    {
    	this.offset = offset;
        Integer integerValue = this.getEnumValue(value);
        array.setBits(offset, this.length, integerValue.byteValue() & 0xff);
    }
    
    @Override
    public String getValue(Array array) throws Exception 
    {
        String value = super.getValue(array);
    	String name = getEnumLabelByValue(new Integer(value));
    	String ret = "";
    	if (name != null)
    	{
    		ret = name + ":";
    	}
    	ret += value;
    	return ret;
    }
    
    public Integer getEnumValueByLabel(String name) 
    {
        return this.valuesByLabel.get(name);
    }

    public String getEnumLabelByValue(Integer value) 
    {
    	String found = this.labelsByValue.get(value);
    	if (found != null)
    	{
    		return found;
    	}
		Iterator<EnumRange> iter = ranges.iterator();
	    while (iter.hasNext())
	    {
	    	EnumRange range = (EnumRange) iter.next();
	    	if (range.isEnclosedInto(value))
	    	{
	    		return range.getName();
	    	}
	    }
        return null;
    }
    
    public Integer getEnumValue(String text) throws Exception
    {
    	text = text.trim();
    	int iPos = text.indexOf(":");
    	String label = text;
    	String value= text;
    	
    	if (iPos >= 0)
    	{
    		label = text.substring(0, iPos);
    		value = text.substring(iPos + 1);
    	}
    	try
    	{
    		int val = Integer.parseInt(value);
   			if (!label.equalsIgnoreCase(getEnumLabelByValue(val)) && !label.equals(text))
   			{
   				GlobalLogger.instance().getApplicationLogger().warn(Topic.PROTOCOL, "For the enumeration field \"" + this.name + "\", the value \"" + value + "\"  does not match the label \"" + label + "\"");
   			}    		
    		return (Integer) val;
    	}
    	catch (NumberFormatException e)
    	{
    		Integer val = this.valuesByLabel.get(value);
	        if (val == null)
	        {
	        	throw new ExecutionException("For the enumeration field \"" + this.name + "\", the value \"" + value + "\" is not numeric or valid according to the dictionary.");
	        }

    		return val;
    	}
    }
}
