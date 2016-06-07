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

package com.devoteam.srit.xmlloader.core.coding.binary.coap;

import com.devoteam.srit.xmlloader.core.coding.binary.Dictionary;
import com.devoteam.srit.xmlloader.core.coding.binary.ElementAbstract;
import com.devoteam.srit.xmlloader.core.coding.binary.StringField;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.Integer16Array;
import gp.utils.arrays.SupArray;


/**
 *
 * @author Fabien Henry
 */
public class ElementCOAPOption extends ElementAbstract
{

    public ElementCOAPOption()
    {
    	
    }
    
	@Override
    public int decodeFromArray(Array array, Dictionary dictionary) throws Exception
	{
		if (array.length <= 0)
		{
			return 0;
		}
		Array deltaLengthArray = array.subArray(0, 1);
	    int deltaLength = new Integer08Array(deltaLengthArray).getValue();
	    
	    this.tag = deltaLength / 16; 
	    int length = deltaLength % 16;
	    
	    Array dataArray = array.subArray(1, length);
	    	
	    decodeFieldsTagElementsFromArray(dataArray, dictionary);
	    return length + 1;
    }

	@Override    
    public SupArray encodeToArray(Dictionary dictionary) throws Exception
	{
		// encode the sub-element
		this.subelementsArray = super.encodeToArray(dictionary);

        SupArray sup = new SupArray();
        
    	int length = this.fieldsArray.length + this.subelementsArray.length;    	
    	int deltaLength = this.tag*16 + length;
        Integer08Array idArray = new Integer08Array(deltaLength);
        sup.addLast(idArray);
	    sup.addLast(this.fieldsArray);
	    sup.addLast(this.subelementsArray);
	    
        return sup;
    }

	/*
	 * get padding array
	 */
    protected static byte[] getPaddingBytes(int lengthPadding)
	{
	    byte[] bytes = new byte[lengthPadding];
	    for (int i = 0; i < lengthPadding; i++)
	    {
	    	bytes[i] = 0;
	    }
	    return bytes;
	}
	
	/*
	 * remove padding data
	 */
    protected static int removePaddingBytes(Array data)
    {
	    int i = data.length - 1;
	    int lengthPadding = 0;
	    if (data.length > 0)
	    {
		    while (i >= 0 && data.get(i) == 0)
		    {
		    	lengthPadding++;
		    	i--;
		    }
		    if (i == 0)
		    {
		    	return 0;
		    }
	    }
	    return lengthPadding;
    }
}
