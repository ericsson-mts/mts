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

import java.net.InetAddress;

import gp.utils.arrays.Array;
import gp.utils.arrays.SupArray;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;

import org.dom4j.Element;


/**
 *
 * @author Fabien Henry
 */
public class IPV4AddressField extends Field
{
	
    public IPV4AddressField(Element rootXML) 
    {
        super(rootXML);
        this._length = 4 * 8;
    }

    @Override
    public Array setValue(String value, int offset, SupArray array) throws Exception 
    {
    	this._offset = offset;
    	InetAddress inetAddr = InetAddress.getByName(value);
    	byte[] bytes = inetAddr.getAddress();
    	for (int i = 0; i < 4; i++)
    	{
    		int pos = offset / 8 + i;
    		array.set(pos, bytes[i] & 0xff);
    	}
    	return null;
    }
    
    @Override
    public String getValue(Array array) throws Exception 
    {
    	int pos = this._offset / 8;
    	byte[] bytes = array.getBytes(pos, 4);
    	InetAddress inetAddr = InetAddress.getByAddress(bytes);
    	return inetAddr.getHostAddress();
    }
    
    
   
}
