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
 * BUG les enumeration integer ne sont prévu que pour des entiers sur 1 octets (0-255)
 * 
 */
public class EnumLongField extends IntegerField
{
	
    private Map<Long, String> labelsByValue = new HashMap<Long, String>();
    private Map<String, Long> valuesByLabel = new HashMap<String, Long>();

    private List<EnumRange> ranges = new ArrayList<EnumRange>();
	
	public EnumLongField() 
    {
		super();
    }

	@Override
    public void parseFromXML(Element rootXML, boolean parseDico) 
    {
        super.parseFromXML(rootXML, parseDico);
        
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
	            this.valuesByLabel.put(nameStr, range.getBeginValue());
	            this.labelsByValue.put(range.getBeginValue(), nameStr);
        	}
        	else
        	{
	        	byte[] valueBytes = Utils.parseBinaryString(valueStr);
	        	long value = EnumRange.toLong(valueBytes);

	            this.valuesByLabel.put(nameStr, value);
	            this.labelsByValue.put(value, nameStr);
        	}
        }

    }
	
    @Override
    public String getValue(Array array) throws Exception 
    {
        String value = super.getValue(array);
    	String name = getEnumLabelByValue(new Long(value));
    	String ret = "";
    	if (name != null)
    	{
    		ret = name + ":";
    	}
    	ret += value;
    	return ret;
    }
    
    @Override
    public void setValue(String value, int offset, SupArray array) throws Exception 
    {
    	this.offset = offset;
        Long longValue = this.getEnumLong(value);
        array.setBits(offset, this.length, longValue.intValue());
    }
    
    @Override
    public void initValue(int index, int offset, SupArray array) throws Exception 
    {
    	if (ranges.size() > 0)
    	{
    		int indexRange = (int) Utils.randomLong(0, ranges.size() - 1);
    		EnumRange range = ranges.get(indexRange);
    		Long l = range.getRandomValue();
    		this.setValue(l.toString(), offset, array);
    		if (Utils.randomBoolean())
    		{
    			return;
    		}
    	}
    	if (valuesByLabel.size() > 0)
    	{
    		int indexLabel = (int) Utils.randomLong(0, valuesByLabel.size() - 1);
    		Long l = (Long) valuesByLabel.values().toArray()[indexLabel];
        	this.setValue(l.toString(), offset, array);
    		if (Utils.randomBoolean())
    		{
    			return;
    		}
    	}
    	super.initValue(index, offset, array);
    }

    public Long getLongValue(Array array) throws Exception 
    {
    	String value = super.getValue(array);
    	long l = Long.parseLong(value);
        return l;
    }

    public Long getEnumValueByLabel(String name) 
    {
    	Long found = this.valuesByLabel.get(name);
    	if (found != null)
    	{
    		return found;
    	}
    	
		Iterator<EnumRange> iter = ranges.iterator();
	    while (iter.hasNext())
	    {
	    	EnumRange range = (EnumRange) iter.next();
	    	Long value = range.getValueFromLabel(name);
	    	if (value != null)
	    	{
	    		return value;
	    	}
	    }
        return null;        
    }

    public String getEnumLabelByValue(Long value) 
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
	    	String label = range.getLabelFromValue(value);
	    	if (label != null)
	    	{
	    		return label;
	    	}
	    }
        return null;
    }
    
    public long getEnumLong(String text) throws Exception
    {
    	text = text.trim();
    	int iPos = text.lastIndexOf(":");
    	String label = text;
    	String value= text;
    	
    	if (iPos >= 0)
    	{
    		label = text.substring(0, iPos);
    		value = text.substring(iPos + 1);
    	}
    	try
    	{
    		long val = Long.parseLong(value);
   			if (!label.equalsIgnoreCase(getEnumLabelByValue(val)) && !label.equals(text))
   			{
   				GlobalLogger.instance().getApplicationLogger().warn(Topic.PROTOCOL, "For the enumeration field \"" + this.name + "\", the value \"" + value + "\"  does not match the label \"" + label + "\"");
   			}    		
    		return val;
    	}
    	catch (NumberFormatException e)
    	{
    		Long val = getEnumValueByLabel(value);
	        if (val == null)
	        {
	        	throw new ExecutionException("For the enumeration field \"" + this.name + "\", the value \"" + value + "\" is not numeric or valid according to the dictionary.");
	        }

    		return val;
    	}
    }
    
    public String getEnumValue(long value) throws Exception 
    {
    	String name = getEnumLabelByValue(value);
    	String ret = "";
    	if (name != null)
    	{
    		ret = name + ":";
    	}
    	ret += value;
    	return ret;
    }

    @Override
    public FieldAbstract clone()
    {
    	EnumLongField newField = new EnumLongField(); 
    	newField.copyToClone(this);
    	
    	newField.labelsByValue = this.labelsByValue;
    	newField.valuesByLabel = this.valuesByLabel; 
    	newField.ranges = this.ranges;
    	
    	return newField;
    }
}
