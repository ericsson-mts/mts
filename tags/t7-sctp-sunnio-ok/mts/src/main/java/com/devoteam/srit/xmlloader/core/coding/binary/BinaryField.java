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

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
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
		super();
    }

	@Override
    public String getValue(Array array) 
    {
		Array arrayValue = null;
		if (this.length > 0)
		{
			arrayValue = array.subArray(this.offset / 8, this.length / 8);
		}
		else
		{
			arrayValue = array.subArray(this.offset / 8);
		}
    	return Array.toHexString(arrayValue);
    }
	
    @Override
    public void setValue(String value, int offset, SupArray array) 
    {
        Array valueArray = Array.fromHexString(value);	
        super.setValueFromArray( valueArray, offset, array);
    }

    @Override
    public void initValue(int index, int offset, SupArray array) throws Exception
    {
    	int numByte = (int) Utils.randomLong(1, 10L);
    	byte[] bytes = Utils.randomBytes(numByte);
        Array valueArray = new DefaultArray(bytes);	
        super.setValueFromArray( valueArray, offset, array);
    }

    @Override
    public FieldAbstract clone()
    {
    	BinaryField newField = new BinaryField(); 
    	newField.copyToClone(this);
    	return newField;
    }
}
