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

package com.devoteam.srit.xmlloader.gtpp.data;

import java.io.InputStream;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.coding.binary.Dictionary;
import com.devoteam.srit.xmlloader.core.coding.binary.EnumerationField;
import com.devoteam.srit.xmlloader.core.coding.binary.HeaderAbstract;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.Integer16Array;
import gp.utils.arrays.Integer32Array;
import gp.utils.arrays.SupArray;

/**
 *
 * @author Fabien Henry
 */
public class HeaderGTPPrime extends HeaderAbstract
{
    private int version;
    private int protocolType;    
    private int messageType;
	private String name;
    private int sequenceNumber;
    
    public HeaderGTPPrime() 
    {
    	this.syntax = "GTPPrime";
    	this.protocolType = 0; 
    	this.version = 0; 
	}
    public HeaderGTPPrime(DefaultArray flagArray) 
    {
    	this();
        this.version = flagArray.getBits(0,3);
        this.protocolType = flagArray.getBits(3,1);
	}
    
    @Override
    public boolean isRequest() 
    {
    	if ((this.name != null) && (!this.name.contains("Request")))
    	{
    		return false;   		
    	}
    	return true;
    }
	
    @Override
    public String getType() 
    {  
	    return this.name + ":" + messageType;
    }

    @Override
    public void parseFromXML(Element header, Dictionary dictionary) throws Exception
    {
		this.dictionary = dictionary;
		
        String strName = header.attributeValue("name");
        String strType = header.attributeValue("type");

        if ((strType != null) && (strName != null))
            throw new Exception("Type and name of the message " + this.name + " must not be set both");

        if ((strType == null) && (strName == null))
            throw new Exception("One of the parameter type or name of the message header must be set");

        if (strName != null)
        {
            this.name = strName;
            EnumerationField field = (EnumerationField) dictionary.getMapHeader().get("Message Type");
            this.messageType = field._hashMapEnumByName.get(this.name);
        }
        else if(strType != null)
        {	
        	this.messageType = Integer.parseInt(strType);
        	EnumerationField field = (EnumerationField) dictionary.getMapHeader().get("Message Type");
    	    this.name = field._hashMapEnumByValue.get(this.messageType);
        }
        
        String attribute;
        String attrFlag;
                
        attribute = header.attributeValue("sequenceNumber");
        if (attribute != null)
        {
        	this.sequenceNumber = Integer.parseInt(attribute);
        }
    }

	@Override
    public String toXML()
    {
        String str = "<headerPrime ";
        str += " messageType=\"" + this.name + ":" + this.messageType + "\""; 
        str += " sequenceNumber=\"" + this.sequenceNumber + "\"";
        str += " length=\"" + this.length + "\"";
        str += " version=\"" + this.version + "\"";        
        str += " protocolType=\"" + this.protocolType + "\"";
        str += "/>";
        return str;
    }
	
    public Array encodeToArray()
    {
        //manage header data
        SupArray supArray = new SupArray();

        DefaultArray firstByte = new DefaultArray(1);//first byte data
        firstByte.setBits(0, 3, version);
        firstByte.setBits(3, 1, protocolType);
        firstByte.setBits(4, 4, 15);//=> 1111 in bits
        supArray.addFirst(firstByte);

        supArray.addLast(new Integer08Array(messageType));
        supArray.addLast(new Integer16Array(length));
        supArray.addLast(new Integer16Array(sequenceNumber));

        return supArray;
    }

	@Override
	public int calculateHeaderSize()
    {
		int size = 0;
		size += 2;
		return size;
    }
	
	@Override
	public void decodeFromArray(Array data, String syntax, Dictionary dictionary) throws Exception
	{
		// throw new Exception("Method is not implemented !");
		// Nothing to do
	}
	
	@Override
	public void decodeFromStream(InputStream stream, Dictionary dictionary) throws Exception
    {	
		this.dictionary = dictionary;
		
		byte[] header = new byte[1];
        stream.read(header, 0, 1);
        Array array = new DefaultArray(header); 
        this.messageType = (new Integer08Array(array).getValue());
    	EnumerationField field = (EnumerationField) dictionary.getMapHeader().get("Message Type");
	    this.name = field._hashMapEnumByValue.get(messageType);
        
	    header = new byte[2];
        stream.read(header, 0, 2);
        array = new DefaultArray(header); 
        this.length = (new Integer16Array(array).getValue());
                
    	header = new byte[2];
    	stream.read(header, 0, 2);
	    array = new DefaultArray(header); 
	    this.sequenceNumber = (new Integer16Array(array).getValue()); 
    }
 
    @Override
    public void getParameter(Parameter var, String param) throws Exception
    {
    	// TODO
    }
    
}
