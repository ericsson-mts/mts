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

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.SupArray;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import org.dom4j.Element;


/**
 *
 * @author Fabien Henry
 */
public class NumberBCDField extends FieldAbstract
{
	
	public NumberBCDField() throws Exception 
    {
    }
	
	public NumberBCDField(Element rootXML) 
    {
        super(rootXML);
    }

    @Override
    public void setValue(String value, int offset, SupArray array) throws Exception 
    {
    	this._offset = offset;   	
    	if (value.length() % 2 != 0)
    	{
    		value = value + "f";
    	}
    	byte[] bytes = value.getBytes();
    	permuteByte(bytes);
    	String string = new String(bytes);
    	Array valueArray = Array.fromHexString(string);   	
        array.addLast(valueArray);
    }
    
    @Override
    public String getValue(Array array) throws Exception 
    {
    	Array arrayValue = array.subArray(getOffset() / 8);
    	String string = Array.toHexString(arrayValue);
    	byte[] bytes = string.getBytes();     	
    	permuteByte(bytes);
    	String value = new String(bytes);
    	if (value.endsWith("f"))
    	{
    		value = value.substring(0, value.length() - 1);
    	}
    	return value;
    }

    private void permuteByte(byte[] bytes) throws Exception 
    {
		int i = 0;
		while (i < bytes.length - 1)
		{
			byte temp = bytes[i];
			bytes[i] = bytes[i + 1];
			bytes[i + 1] = temp;
			i = i + 2;
		}
    }
    
    @Override
    public FieldAbstract clone(FieldAbstract field) throws Exception
    {
    	NumberBCDField newField = new NumberBCDField(); 
    	super.copy(newField, field);
    	return newField;
    }
}
