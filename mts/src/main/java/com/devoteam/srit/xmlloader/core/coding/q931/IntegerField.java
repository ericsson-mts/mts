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

package com.devoteam.srit.xmlloader.core.coding.q931;

import gp.utils.arrays.Array;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;

import org.dom4j.Element;


/**
 *
 * @author indiaye
 */
public class IntegerField extends Field{
    public IntegerField(Element rootXML) {
        super(rootXML);
    }

    @Override
    public Array setValue(String value, int offset, Array array) throws Exception {
    	_offset = offset;
        try
        {
        	array.setBits(offset, getLength(), (long) Long.parseLong(value) & 0xffffffffl);
	    }
        catch(Exception e)
        {
        	throw new ExecutionException("ISDN layer : The value \"" + value + "\" for the integer field : \"" + getName() + "\" is not valid.", e);            	            	
        }
        return null;
    }
    
    @Override
    public String getValue(Array array) throws Exception {
    	long valueLong = (long) array.getBits(getOffset(), getLength());
    	return Long.toString(valueLong);
    }
   
}
