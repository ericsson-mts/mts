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
import com.devoteam.srit.xmlloader.core.coding.q931.Dictionary;
import com.devoteam.srit.xmlloader.core.coding.q931.EnumerationField;
import com.devoteam.srit.xmlloader.core.coding.q931.HeaderAbstract;

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
public class HeaderGTPV2 extends HeaderAbstract 
{
	
	//Header composers 
	private int version;
	private int piggyFlag;
	private int teidFlag;
	private int messageType;
	private String name;
	private int tunnelEndpointId;
	private int sequenceNumber;
    
    public HeaderGTPV2()
	{
    	this.syntax = "GTPV2";
    	this.version = 2;
	}
	
    public HeaderGTPV2(Array flagArray)
	{
    	this.syntax = "GTPV2";
    	this.version = 2;
    	this.piggyFlag = flagArray.getBits(3, 1);
    	this.teidFlag = flagArray.getBits(4, 1);
	}

    @Override
    public boolean isRequest() {
    	if ((name != null) && (!name.contains("Request")))
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
	public String getSyntax() 
    {
		return this.syntax;
    }

	@Override
	public void parseFromXML(Element header, Dictionary dictionary) throws Exception
    {
		this.dictionary = dictionary;
		
        String strName = header.attributeValue("name");
        String strType = header.attributeValue("type");

        if((strType != null) && (strName != null))
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
        
        attribute = header.attributeValue("piggyFlag");
        if (attribute != null)
        {
        	this.piggyFlag = Integer.parseInt(attribute);
        }
        
        attrFlag = header.attributeValue("teidFlag");
        if (attrFlag != null)
        {
        	this.teidFlag = Integer.parseInt(attrFlag);
        }
        attribute = header.attributeValue("tunnelEndpointId");
        if (attribute != null)
        {
        	this.tunnelEndpointId = Integer.parseInt(attribute);
        	if (attrFlag ==  null)
        	{
        		this.teidFlag = 1;
        	}
        }
        else
        {
        	if (attrFlag ==  null)
        	{
        		this.teidFlag = 0;
        	}
        }

        attribute = header.attributeValue("sequenceNumber");
        if (attribute != null)
        {
        	this.sequenceNumber = Integer.parseInt(attribute);
        }    
    }

	@Override
    public String toXML()
    {
        String str = "<headerV2 ";
        str += " messageType=\"" + this.name + ":" + messageType + "\"";
        str += " tunnelEndpointId=\"" + this.tunnelEndpointId + "\"";
        str += " sequenceNumber=\"" + this.sequenceNumber + "\"";
        str += " length=\"" + this.length + "\""; 
        str += " piggyFlag=\"" + this.piggyFlag + "\""; 
        str += " teidFlag=" + this.teidFlag +  "\"";
        str += "/>";
        return str;
    }

	@Override
	public Array encodeToArray()
    {
        //manage header data
        SupArray supArray = new SupArray();

        DefaultArray firstByte = new DefaultArray(1);//first byte data
        firstByte.setBits(0, 3, this.version);
        firstByte.setBits(3, 1, this.piggyFlag);
        firstByte.setBits(4, 1, this.teidFlag);
        firstByte.setBits(5, 1, 0);
        firstByte.setBits(6, 1, 0);
        firstByte.setBits(7, 1, 0);
        supArray.addFirst(firstByte);

        supArray.addLast(new Integer08Array(this.messageType));
        
        supArray.addLast(new Integer16Array(this.length));
        
        if (this.teidFlag != 0)
        {
        	supArray.addLast(new Integer32Array(this.tunnelEndpointId));
        }
        
        Array sequenceNumberArray= new Integer32Array(this.sequenceNumber);
        supArray.addLast(sequenceNumberArray.subArray(1, 3));
        
        supArray.addLast(new Integer08Array(0));
        
        return supArray;
    }
	
	@Override
	public int calculateHeaderSize()
    {
		int size = 0;
        if (this.teidFlag != 0)
        {
        	size += 4;
        }
        size += 3;
        size +=1;
        return size;
    }
	@Override
	public void decodeFromArray(Array data, String syntax, Dictionary dictionary) throws Exception
	{
		// throw new Exception("Method is not implemented !");
		// Nothing to do
	}
	
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
        
        if (this.teidFlag != 0)
    	{
	        header = new byte[4];
	        stream.read(header, 0, 4);
	        array = new DefaultArray(header); 
	        this.tunnelEndpointId = (new Integer32Array(array).getValue());
    	}
        
    	header = new byte[4];
    	stream.read(header, 1, 3);
    	array = new DefaultArray(header); 
    	this.sequenceNumber = (new Integer32Array(array).getValue()); 

    	header = new byte[1];
    	stream.read(header, 0, 1);
    	// TODO champ spare2
    	
    }
	
    @Override
    public void getParameter(Parameter var, String param) 
    {
    	// TODO a compléter
    }
    

}


