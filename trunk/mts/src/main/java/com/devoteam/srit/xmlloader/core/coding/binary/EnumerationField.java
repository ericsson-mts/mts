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
import com.devoteam.srit.xmlloader.core.utils.maps.LinkedHashMap;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.Integer32Array;
import gp.utils.arrays.SupArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;


/**
 *
 * @author indiaye
 */
public class EnumerationField extends IntegerField
{
	
    private Map<Integer, String> labelsMapByValue = new HashMap<Integer, String>();
    private Map<String, Integer> valuesMapByLabel = new HashMap<String, Integer>();

	
    public EnumerationField(Element rootXML) 
    {
        super(rootXML);
        
        List<Element> list = rootXML.elements("enum");
        for (Element elemEnum : list) 
        {
        	byte[] valueBytes = Utils.parseBinaryString(elemEnum.attributeValue("value"));
        	int value = (int) valueBytes[0] & 0xFF;        	
            this.valuesMapByLabel.put(elemEnum.attributeValue("name"), value);
            this.labelsMapByValue.put(value, elemEnum.attributeValue("name"));
        }

    }

    @Override
    public void setValue(String value, int offset, SupArray array) throws Exception 
    {
    	this._offset = offset;
        Integer integerValue = this.getEnumValue(value);
        array.setBits(getOffset(), getLength(), integerValue.byteValue() & 0xff);
    }
    
    @Override
    public String getValue(Array array) throws Exception 
    {
        String value = super.getValue(array);
    	String name = this.labelsMapByValue.get(new Integer(value));
    	String ret = "";
    	if (name != null)
    	{
    		ret = name + ":";
    	}
    	ret += value;
    	return ret;
    }
    
    public Integer getValuesMapByName(String name) 
    {
        return this.valuesMapByLabel.get(name);
    }

    public String getNamesMapByValue(Integer value) 
    {
        return this.labelsMapByValue.get(value);
    }
    
    public Integer getEnumValue(String text) throws Exception
    {
    	int iPos = text.indexOf(":");
    	String value= text;
    	if (iPos >= 0)
    	{
    		String label = text.substring(0, iPos);
    		value = text.substring(iPos + 1);
   			if (!label.equalsIgnoreCase(this.labelsMapByValue.get(value)))
   			{
   				GlobalLogger.instance().getApplicationLogger().warn(Topic.PROTOCOL, "For the enumeration field \"" + getName() + "\", the value \"" + value + "\"  does not match the label \"" + label + "\"");
   			}
    	}
    	try
    	{
    		int val = Integer.parseInt(value);
    		return (Integer) val;
    	}
    	catch (NumberFormatException e)
    	{
    		Integer val = this.valuesMapByLabel.get(value);
	        if (val == null)
	        {
	        	throw new ExecutionException("For the enumeration field \"" + getName() + "\", the value \"" + value + "\" is not numeric or valid according to the dictionary.");
	        }

    		return val;
    	}
    }
}
