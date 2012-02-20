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

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.h323.h225cs.StackH225cs;
import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.Integer16Array;
import gp.utils.arrays.SupArray;
import java.util.Map.Entry;
import org.dom4j.Element;

/**
 *
 * @author indiaye
 */
public class HeaderQ931 {

    private Integer08Array _discrimArray;
    private Array _callReferenceArray;

	private Array _layer3AddressArray;
    private Integer08Array _typeArray;
    private Dictionary dictionary; 

    private int _length;
    
    public HeaderQ931(Element header, Dictionary dictionary) throws ExecutionException {
        if (header.attributeValue("type").startsWith("b")) {
            _typeArray = new Integer08Array((Utils.parseBinaryString(header.attributeValue("type")))[0]);
        } else {
            _typeArray = new Integer08Array(dictionary.getMapHeader().get("Message type").getHashMapEnumByName().get(header.attributeValue("type")));
        }

    	String discriminator = header.attributeValue("discriminator");
        try {
            _discrimArray = new Integer08Array((Utils.parseBinaryString(discriminator))[0]);
        } catch (Exception e) {
            _discrimArray = new Integer08Array(dictionary.getMapHeader().get("Protocol discriminator").getHashMapEnumByName().get(discriminator));
        }
        String callReference = header.attributeValue("callReference");
        if (callReference != null)
        {
        	_callReferenceArray = new DefaultArray(Utils.parseBinaryString(callReference));
        	if (_callReferenceArray.length > 2)
        	{
        		throw new ExecutionException("ISDN layer : The \"callReference\" attribute value for the header is too long [0...32767]");        		
        	}
        }
        String layer3Address = header.attributeValue("layer3Address");
        if (layer3Address != null)
        {
        	_layer3AddressArray = new DefaultArray(Utils.parseBinaryString(layer3Address));
        }
        this.dictionary = dictionary; 
    }

    public HeaderQ931(Array data, String syntax, Dictionary dictionary) {
    	this.dictionary = dictionary; 
        _discrimArray = new Integer08Array(data.subArray(0, 1));
        if (syntax.contains("q931"))
        {
	        Integer08Array length = new Integer08Array(data.subArray(1, 1));
	        _callReferenceArray = data.subArray(2, length.getValue());
	        _typeArray = new Integer08Array(data.subArray(2 + length.getValue(), 1));
	        _length = 3 + length.getValue();
        }
        if (syntax.contains("v5x"))
        {        
        	_layer3AddressArray = data.subArray(1, 2);
        	_typeArray = new Integer08Array(data.subArray(3, 1));
	        _length = 4;
        }
    }

    public int getTypeArray() {
        return _typeArray.getValue();
    }

    public int getLength() {
		return _length;
	}

	public Array getCallReferenceArray() {
		return _callReferenceArray;
	}

	public long getCallReference() {
		if (_callReferenceArray.length == 1)
		{
			Integer08Array callRef = new Integer08Array(_callReferenceArray);
			return callRef.getValue();
		} 
		else if (_callReferenceArray.length == 2)
		{
			Integer16Array callRef = new Integer16Array(_callReferenceArray);
			return callRef.getValue();
		}		
		return -1;
	}

    public Array getValue() {
        SupArray arrheader = new SupArray();
        arrheader.addLast(_discrimArray);
        if (_callReferenceArray != null)
        {
        	arrheader.addLast(new Integer08Array(_callReferenceArray.length));        
        	arrheader.addLast(_callReferenceArray);
        }
        if (_layer3AddressArray != null)
        {
        	arrheader.addLast(_layer3AddressArray);
        }
        arrheader.addLast(_typeArray);
        return arrheader;
    }

    public String getType() {
	    EnumerationField field = (EnumerationField) dictionary.getMapHeader().get("Message type");
	    String name = field._hashMapEnumByValue.get(_typeArray.getValue());        
	    return name + ":" + _typeArray.getValue();
    }
    
    @Override
    public String toString() {
        StringBuilder headerString = new StringBuilder();
        headerString.append("<header");
       	headerString.append(" discriminator=\"" + Array.toHexString(_discrimArray) + "\"");
        headerString.append(" type=\"" + getType() + "\"");
        if (_callReferenceArray != null)
        {
        	headerString.append(" callReference=\"" + getCallReference() + "\"");
        }
        if (_layer3AddressArray != null)
        {
        	headerString.append(" layer3Address=\"" + Array.toHexString(_layer3AddressArray) + "\"");
        }        
        headerString.append("/>\n");
        return headerString.toString();
    }

    public void getParameter(Parameter var, String param) {
        if (param.equalsIgnoreCase("type")) {
            var.add(_typeArray.getValue());
        }
        else if (param.equalsIgnoreCase("callReference")) {
        	if (_callReferenceArray != null)
        	{
        		var.add(getCallReference());
        	}
        }
        else if (param.equalsIgnoreCase("layer3Address")) {
        	if (_layer3AddressArray != null)
        	{
        		var.add(Array.toHexString(_layer3AddressArray));
        	}
        }
        else if (param.equalsIgnoreCase("discriminator")) {
            var.add(_discrimArray.getValue());
        }
    }
}
