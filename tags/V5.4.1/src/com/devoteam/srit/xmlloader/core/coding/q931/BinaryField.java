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
*//*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.core.coding.q931;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.SupArray;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.h323.h225cs.StackH225cs;

/**
 *
 * @author indiaye
 */
public class BinaryField extends Field {

    public BinaryField(Element field, ElementInformationQ931 elem, Dictionary dictionary) throws Exception {
        super(field, elem, dictionary);
        if (getLength() % 8 != 0) {
            throw new ExecutionException("Wrong length for binary field : \"" + getName() + "\"");
        }
    }

    @Override
    public void setValue(String value, int offset, ElementInformationQ931V elemV) {
    	_offset = offset;    	
        Array array = Array.fromHexString(value);

        if (this.dictionary.getMapElementById().get(elemV.getId()) == null) {


            for (int i = 0; i < array.length; i++) {
                elemV.getFieldsArray().set(i + getOffset() / 8, array.get(i));
            }
        }
        else {
            SupArray suparray = new SupArray();
            suparray.addLast(elemV.getFieldsArray());
            suparray.addLast(array);
            elemV.setFields(suparray);
        }
    }

    @Override
    public String getValue(ElementInformationQ931V elemV) {
        return Array.toHexString(elemV.getFieldsArray().subArray(getOffset() / 8, getLength() / 8));
    }
    
}
