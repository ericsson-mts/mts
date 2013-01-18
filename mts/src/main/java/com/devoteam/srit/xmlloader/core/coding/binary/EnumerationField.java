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
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.maps.LinkedHashMap;

import gp.utils.arrays.Array;
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
	
    private Map<Integer, String> namesMapByValue = new HashMap<Integer, String>();
    private Map<String, Integer> valuesMapByName = new HashMap<String, Integer>();

	
    public EnumerationField(Element rootXML) 
    {
        super(rootXML);
        
        List<Element> list = rootXML.elements("enum");
        for (Element elemEnum : list) 
        {
        	byte[] valueBytes = Utils.parseBinaryString(elemEnum.attributeValue("value"));
        	int value = (int) valueBytes[0] & 0xFF;        	
            this.valuesMapByName.put(elemEnum.attributeValue("name"), value);
            this.namesMapByValue.put(value, elemEnum.attributeValue("name"));
        }

    }

    @Override
    public void setValue(String value, int offset, SupArray array) throws Exception {
    	this._offset = offset;
	    try
	    {
	    	array.setBits(getOffset(), getLength(), Integer.parseInt(value));
	    }
	    catch(Exception e)
	    {
	        Integer integerValue = this.valuesMapByName.get(value);
	        if (integerValue == null)
	        {
	        	throw new ExecutionException("The value \"" + value + "\" for the ISDN enumeration field : \"" + getName() + "\" is not present in the dictionnary.");            	            	
	        }
	        array.setBits(getOffset(), getLength(), integerValue.byteValue() & 0xff);
	    }
    }
    
    @Override
    public String getValue(Array array) throws Exception {
        String value = super.getValue(array);
    	String name = this.namesMapByValue.get(new Integer(value));
    	String ret = "";
    	if (name != null)
    	{
    		ret = name + ":";
    	}
    	ret += value;
    	return ret;
    }
    
    public Integer getValuesMapByName(String name) {
        return this.valuesMapByName.get(name);
    }

    public String getNamesMapByValue(Integer value) {
        return this.namesMapByValue.get(value);
    }
}
