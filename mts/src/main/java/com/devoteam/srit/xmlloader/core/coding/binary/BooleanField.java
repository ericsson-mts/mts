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

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.utils.Utils;


/**
 *
 * @author indiaye
 */
public class BooleanField extends FieldAbstract
{

	public BooleanField()
    {
    }
	
    public BooleanField(Element rootXML) 
    {
        super(rootXML);
        this.length = 1;
    }

    @Override
    public void setValue(String value, int offset, SupArray array) throws Exception 
    {
    	this.offset = offset;
    	boolean bool = Utils.parseBoolean(value, this.name);
    	if (bool)
    	{
    		array.setBit(offset, 1);
    	}
    	else
    	{
    		array.setBit(offset, 0);
    	}
    }

    @Override
    public String getValue(Array array) throws Exception 
    {
        return Integer.toString(array.getBits(this.offset, this.length));
    }
    
    @Override
    public FieldAbstract clone()
    {
    	BooleanField newField = new BooleanField(); 
    	newField.copyToClone(this);
    	return newField;
    }
}
