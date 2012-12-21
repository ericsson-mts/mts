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

import java.util.List;
import org.dom4j.Element;


/**
 *
 * @author indiaye
 */
public class EnumerationField extends IntegerField
{
	
    public LinkedHashMap<Integer, String> _hashMapEnumByValue = new LinkedHashMap<Integer, String>();
    public LinkedHashMap<String, Integer> _hashMapEnumByName = new LinkedHashMap<String, Integer>();

	
    public EnumerationField(Element rootXML) 
    {
        super(rootXML);
        
        List<Element> list = rootXML.elements("enum");
        for (Element elemEnum : list) 
        {
        	byte[] valueBytes = Utils.parseBinaryString(elemEnum.attributeValue("value"));
        	int value = (int) valueBytes[0] & 0xFF;        	
            this._hashMapEnumByName.put(elemEnum.attributeValue("name"), value);
            this._hashMapEnumByValue.put(value, elemEnum.attributeValue("name"));
        }

    }

    @Override
    public Array setValue(String value, int offset, Array array) throws Exception {
    	this._offset = offset;
	    try
	    {
	    	array.setBits(getOffset(), getLength(), Integer.parseInt(value));
	    }
	    catch(Exception e)
	    {
	        Integer integerValue = this.getHashMapEnumByName().get(value);
	        if (integerValue == null)
	        {
	        	throw new ExecutionException("The value \"" + value + "\" for the ISDN enumeration field : \"" + getName() + "\" is not present in the dictionnary.");            	            	
	        }
	        array.setBits(getOffset(), getLength(), integerValue.byteValue() & 0xff);
	    }
	    return null;
    }
    
    @Override
    public String getValue(Array array) throws Exception {
        String value = super.getValue(array);
    	String name = this._hashMapEnumByValue.get(new Integer(value));
    	String ret = "";
    	if (name != null)
    	{
    		ret = name + ":";
    	}
    	ret += value;
    	return ret;
    }
    
    @Override
    public LinkedHashMap<String, Integer> getHashMapEnumByName() {
        return this._hashMapEnumByName;
    }
    
}
