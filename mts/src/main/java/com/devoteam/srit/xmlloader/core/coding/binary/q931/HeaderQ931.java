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

package com.devoteam.srit.xmlloader.core.coding.binary.q931;

import java.io.InputStream;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.coding.binary.Dictionary;
import com.devoteam.srit.xmlloader.core.coding.binary.EnumerationField;
import com.devoteam.srit.xmlloader.core.coding.binary.HeaderAbstract;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.Integer16Array;
import gp.utils.arrays.SupArray;

import org.dom4j.Element;


/**
 *
 * @author indiaye
 */
public class HeaderQ931 extends HeaderAbstract {

    private Integer08Array _discrimArray;
    private Array _callReferenceArray;

	private Array _layer3AddressArray;
    private Integer08Array _typeArray;
    
    public HeaderQ931()
    {
    }
    
    @Override
    public boolean isRequest() {
        if (_callReferenceArray.length == 1) {

            return (_callReferenceArray.getBit(7) == 0);
        }
        else if (_callReferenceArray.length == 2) {
            return (_callReferenceArray.getBit(15) == 0);
        }
        else {
            return (_callReferenceArray.getBit(23) == 0);
        }
    }
    
    @Override
    public String getType() 
    {
	    EnumerationField field = (EnumerationField) dictionary.getHeaderFieldByName("Message type");
	    String name = field.getEnumLabelByValue(_typeArray.getValue());
	    return name + ":" + _typeArray.getValue();
    }
    
    @Override
    public String getSyntax() 
    {
	    return "Q931";
    }
    
    @Override
    public void parseFromXML(Element header, Dictionary dictionary) throws Exception {
        this.dictionary = dictionary; 
        
        if (header.attributeValue("type").startsWith("b")) 
        {
            _typeArray = new Integer08Array((Utils.parseBinaryString(header.attributeValue("type")))[0]);
        } else 
        {
        	EnumerationField field = (EnumerationField) dictionary.getHeaderFieldByName("Message type");
            _typeArray = new Integer08Array(field.getEnumValueByLabel(header.attributeValue("type")));
        }

    	String discriminator = header.attributeValue("discriminator");
        try 
        {
            _discrimArray = new Integer08Array((Utils.parseBinaryString(discriminator))[0]);
        } catch (Exception e) 
        {
        	EnumerationField field = (EnumerationField) dictionary.getHeaderFieldByName("Protocol discriminator");
            _discrimArray = new Integer08Array(field.getEnumValueByLabel(discriminator));
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
    }

    @Override
    public String toXml() {
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

    @Override
    public int decodeFromArray(Array data, String syntax, Dictionary dictionary) {
    	this.dictionary = dictionary; 
    	
        _discrimArray = new Integer08Array(data.subArray(0, 1));
        if (syntax.contains("q931"))
        {
	        Integer08Array length = new Integer08Array(data.subArray(1, 1));
	        _callReferenceArray = data.subArray(2, length.getValue());
	        _typeArray = new Integer08Array(data.subArray(2 + length.getValue(), 1));
	        this.length = 3 + length.getValue();
        }
        if (syntax.contains("v5x"))
        {        
        	_layer3AddressArray = data.subArray(1, 2);
        	_typeArray = new Integer08Array(data.subArray(3, 1));
	        this.length = 4;
        }
        return 0;
    }
	
    @Override
    public Array encodeToArray() {
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

    @Override
    public int calculateHeaderSize() {
        return 0;
    }
    
    @Override
    public void getParameter(Parameter var, String param) 
    {
        if (param.equalsIgnoreCase("type")) 
        {
            var.add(_typeArray.getValue());
        }
        else if (param.equalsIgnoreCase("callReference")) 
        {
        	if (_callReferenceArray != null)
        	{
        		var.add(getCallReference());
        	}
        }
        else if (param.equalsIgnoreCase("layer3Address")) 
        {
        	if (_layer3AddressArray != null)
        	{
        		var.add(Array.toHexString(_layer3AddressArray));
        	}
        }
        else if (param.equalsIgnoreCase("discriminator")) 
        {
            var.add(_discrimArray.getValue());
        }
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
    
}
