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
		super();
		this.length = 1;
    }
	
	@Override
    public void parseFromXML(Element rootXML, boolean parseDico) 
    {
        super.parseFromXML(rootXML, parseDico);
        if (this.length < 0)
        {
        	this.length = 1;
        }
    }

    @Override
    public String getValue(Array array) throws Exception 
    {
    	int val = array.getBits(this.offset, this.length);
    	if (val == 0)
    	{
    		return "False:0";
    	}
    	else
    	{
    		return "True:1";
    	}
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
    public void initValue(int index, int offset, SupArray array) throws Exception
    {
    	Boolean bool = Utils.randomBoolean();	
        this.setValue(bool.toString(), offset, array);
    }
    
    @Override
    public FieldAbstract clone()
    {
    	BooleanField newField = new BooleanField(); 
    	newField.copyToClone(this);
    	return newField;
    }
}
