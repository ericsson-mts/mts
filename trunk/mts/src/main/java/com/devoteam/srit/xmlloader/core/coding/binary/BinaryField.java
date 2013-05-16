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

import gp.utils.arrays.Array;
import gp.utils.arrays.SupArray;

import org.dom4j.Element;


/**
 *
 * @author indiaye
 */
public class BinaryField extends FieldAbstract
{
	
	public BinaryField()
    {
    }
	
    public BinaryField(Element rootXML) throws Exception 
    {
        super(rootXML);
        if (this.length % 8 != 0) {
            throw new ExecutionException("Wrong length for binary field : \"" + this.name + "\"");
        }
    }

    @Override
    public void setValue(String value, int offset, SupArray array) 
    {
    	this.offset = offset;    	
        Array valueArray = Array.fromHexString(value);	
        super.setValueFromArray( valueArray, offset, array);
    }

    @Override
    public String getValue(Array array) 
    {
    	Array arrayValue = array.subArray(this.offset / 8);
    	return Array.toHexString(arrayValue);
    }
    
    @Override
    public FieldAbstract clone()
    {
    	BinaryField newField = new BinaryField(); 
    	newField.copyToClone(this);
    	return newField;
    }
}
