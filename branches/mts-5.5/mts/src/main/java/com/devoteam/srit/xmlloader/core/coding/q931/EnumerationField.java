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
*//*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.coding.q931;

import java.util.List;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.maps.LinkedHashMap;

/**
 *
 * @author indiaye
 */
public class EnumerationField extends IntegerField{
	
    protected LinkedHashMap<Integer, String> _hashMapEnumByValue = new LinkedHashMap<Integer, String>();
    protected LinkedHashMap<String, Integer> _hashMapEnumByName = new LinkedHashMap<String, Integer>();

	
    public EnumerationField(Element elemField, ElementInformationQ931 elem, Dictionary dictionary) {
        super(elemField, elem, dictionary);
        
        List<Element> list = elemField.elements("enum");
        for (Element elemEnum : list) {
            _hashMapEnumByName.put(elemEnum.attributeValue("name"), (int) (Utils.parseBinaryString(elemEnum.attributeValue("value")))[0]);

            _hashMapEnumByValue.put((int) Utils.parseBinaryString(elemEnum.attributeValue("value"))[0], elemEnum.attributeValue("name"));

        }

    }

    @Override
    public String getValue(ElementInformationQ931V elemV) throws Exception {
        String value = super.getValue(elemV);
    	String name = _hashMapEnumByValue.get(new Integer(value));
    	String ret = "";
    	if (name != null)
    	{
    		ret = name + ":";
    	}
    	ret += value;
    	return ret;
    }

    @Override
    public void setValue(String value, int offset, ElementInformationQ931V elemV) throws Exception {
    	_offset = offset;
	    try{
	    	elemV.getFieldsArray().setBits(getOffset(), getLength(), Integer.parseInt(value));
	    }catch(Exception e){
	        Integer integerValue = this.getHashMapEnumByName().get(value);
	        if (integerValue == null)
	        {
	        	throw new ExecutionException("The value \"" + value + "\" for the ISDN enumeration field : \"" + getName() + "\" is not present in the dictionnary.");            	            	
	        }
	        elemV.getFieldsArray().setBits(getOffset(), getLength(),integerValue.byteValue() & 0xff);
	    }
    }
    
    @Override
    public LinkedHashMap<String, Integer> getHashMapEnumByName() {
        return _hashMapEnumByName;
    }
    
}
