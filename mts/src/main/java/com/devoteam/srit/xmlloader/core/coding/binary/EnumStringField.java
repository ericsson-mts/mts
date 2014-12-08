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
import gp.utils.arrays.DefaultArray;
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
public class EnumStringField extends IntegerField
{
	
    private Map<String, String> labelsByValue = new HashMap<String, String>();
    private Map<String, String> valuesByLabel = new HashMap<String, String>();

    private List<EnumRange> ranges = new ArrayList<EnumRange>();
	
	public EnumStringField() 
    {
    }

    public EnumStringField(Element rootXML) 
    {
        super(rootXML);
        
        List<Element> list = rootXML.elements("enum");
        for (Element elemEnum : list) 
        {
        	String valueStr = elemEnum.attributeValue("value");
        	String nameStr = elemEnum.attributeValue("name");
            this.valuesByLabel.put(nameStr, valueStr);
            this.labelsByValue.put(valueStr, nameStr);
        }

    }

    @Override
    public void setValue(String value, int offset, SupArray array) throws Exception 
    {
    	this.offset = offset;
    	Array valueArray = new DefaultArray(value.getBytes());
    	super.setValueFromArray( valueArray, offset, array);
    }
    
    @Override
    public String getValue(Array array) throws Exception 
    {
        String value = super.getValue(array);
    	String name = getEnumLabelByValue(new String(value));
    	String ret = "";
    	if (name != null)
    	{
    		ret = name + ":";
    	}
    	ret += value;
    	return ret;
    }
    
    public String getEnumValueByLabel(String name) 
    {
        return this.valuesByLabel.get(name);
    }

    public String getEnumLabelByValue(String value) 
    {
    	String found = this.labelsByValue.get(value);
    	return found;
    }
    
    public String getEnumString(String text) throws Exception
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
    		String val = value;
   			if (!label.equalsIgnoreCase(getEnumLabelByValue(val)) && !label.equals(text))
   			{
   				GlobalLogger.instance().getApplicationLogger().warn(Topic.PROTOCOL, "For the enumeration field \"" + this.name + "\", the value \"" + value + "\"  does not match the label \"" + label + "\"");
   			}    		
    		return val;
    	}
    	catch (NumberFormatException e)
    	{
    		String val = this.valuesByLabel.get(value);
	        if (val == null)
	        {
	        	throw new ExecutionException("For the enumeration field \"" + this.name + "\", the value \"" + value + "\" is not numeric or valid according to the dictionary.");
	        }

    		return val;
    	}
    }
    
    public String getEnumValue(String value) throws Exception 
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

}
