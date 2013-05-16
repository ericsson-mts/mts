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
public class NumberMMCField extends FieldAbstract
{
	public NumberMMCField() 
    {
    }
	
    public NumberMMCField(Element rootXML) 
    {
        super(rootXML);
        this.length = 3 * 8;
    }

    @Override
    public void setValue(String value, int offset, SupArray array) throws Exception 
    {
    	this.offset = offset;

    	int pos = value.indexOf(",");
    	if (pos < 0)
    	{
    		throw new ExecutionException("The value \"" + value + "\" for the mumber MCC/MNC field : \"" + this.name + "\" is not valid : format = [MCC],[MNC]");
    	}

    	// process the MCC digits
    	String mcc = value.substring(0, pos).trim();
    	if (mcc.length() != 3)
    	{
    		throw new ExecutionException("The value \"" + value + "\" for the mumber MCC field : \"" + this.name + "\" is not valid : [MCC] should have [3..3] characters");
    	}    	
    	// process the MNC digits
    	String mnc = value.substring(pos + 1).trim();
    	if (mnc.length() < 2 || mnc.length() > 3)
    	{
    		throw new ExecutionException("The value \"" + value + "\" for the mumber MNC field : \"" +this.name + "\" is not valid : [MNC] should have [2..3] characters");
    	}    		
    	
    	String mmc; 
    	if (mnc.length() == 2)
    	{
    		mmc = mcc + "f" + mnc.charAt(0) + mnc.charAt(1);
    	}
    	else
    	{
    		mmc = mcc + mnc.charAt(2) + mnc.charAt(0) + mnc.charAt(1); 
    	}

    	// permute the octet 2 a 2
    	byte[] bytes = mmc.getBytes();
    	permuteByte(bytes);
    	String mmcPermute = new String(bytes);
    	Array valueArray = Array.fromHexString(mmcPermute);
    	super.setValueFromArray( valueArray, offset, array);
    }
    
    @Override
    public String getValue(Array array) throws Exception 
    {
    	// get the bits
    	Array arrayValue = array.subArray(this.offset / 8, 3);
    	String string = Array.toHexString(arrayValue);
    	
    	// permute the octet 2 a 2
    	byte[] bytes = string.getBytes();
    	permuteByte(bytes);
    	String value = new String(bytes);
    	
    	String mmc = value.substring(0, 3);
    	String mnc;
    	String temp = value.substring(3, 6);
    	
    	if (temp.charAt(0) == 'f')
    	{
    		mnc = ""+ temp.charAt(1) + temp.charAt(2);
    	}
    	else
    	{
    		mnc = ""+ temp.charAt(1) + temp.charAt(2) + temp.charAt(0);
    	}
    	return mmc + ',' + mnc;
    }
    
    @Override
    public FieldAbstract clone()
    {
    	NumberMMCField newField = new NumberMMCField(); 
    	newField.copyToClone(this);
    	return newField;
    }
}
