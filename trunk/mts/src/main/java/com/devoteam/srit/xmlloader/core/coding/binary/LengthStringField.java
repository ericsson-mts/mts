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
import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.SupArray;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.utils.Utils;

/**
 *
 * @author indiaye
 */
public class LengthStringField extends Field 
{

    public LengthStringField(Element rootXML) 
    {
        super(rootXML);      
    }

    @Override
    public Array setValue(String value, int offset, Array array) throws Exception 
    {
    	this._offset = offset;
    	// this._length = (value.length() + 1) * 8;
        SupArray suparray = new SupArray();		
        suparray.addLast(array);
        suparray.addLast(new Integer08Array(value.length()));
        Array arrayValue = new DefaultArray(value.getBytes());
        suparray.addLast(arrayValue);
        return suparray;
    }

    @Override
    public String getValue(Array array) throws Exception 
    {
    	int length = array.getBits(this._offset, 8);
    	Array arrayValue = array.subArray(this._offset / 8 + 1, length);
        return new String(arrayValue.getBytes());
    }
}
