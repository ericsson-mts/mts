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
import gp.utils.arrays.SupArray;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import org.dom4j.Element;


/**
 *
 * @author indiaye
 */
public class IntegerField extends FieldAbstract
{
	
	public IntegerField() 
    {
		this.length = 4 * 8;
    }
	
	@Override
    public void parseFromXML(Element rootXML, boolean parseDico) 
    {
        super.parseFromXML(rootXML, parseDico);
    
        if (this.length < 0)
        {
        	this.length = 4 * 8;
        }
    }

    @Override
    public String getValue(Array array) throws Exception 
    {
    	long valueLong = 0;
    	if (this.length <= array.length * 8)
    	{
    		valueLong = array.getBitsL(this.offset, this.length);
    	}
    	else
    	{
    		valueLong = array.getBitsL(this.offset, array.length * 8);
    	}
    	
    	return Long.toString(valueLong);
    }

    @Override
    public void setValue(String value, int offset, SupArray array) throws Exception 
    {
    	this.offset = offset;
        try
        {
        	array.setBitsL(offset, this.length, Long.parseLong(value));
	    }
        catch(Exception e)
        {
        	throw new ExecutionException("The value \"" + value + "\" for the integer field : \"" + this.name + "\" is not valid.", e);            	            	
        }
    }

    @Override
    public void initValue(int index, int offset, SupArray array) throws Exception 
    {
    	Double max = Math.pow((double) 2, (double) this.length) - 1;
    	Long integer = Utils.randomLong(0, max.longValue());
    	this.setValue(integer.toString(), offset, array);
    }

    @Override
    public FieldAbstract clone()
    {
    	IntegerField newField = new IntegerField(); 
    	newField.copyToClone(this);
    	return newField;
    }
}
