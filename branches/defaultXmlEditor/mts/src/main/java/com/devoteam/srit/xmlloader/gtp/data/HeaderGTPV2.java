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

package com.devoteam.srit.xmlloader.gtp.data;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.coding.binary.Dictionary;
import com.devoteam.srit.xmlloader.core.coding.binary.EnumLongField;
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
public class HeaderGTPV2 extends HeaderAbstract 
{
	
	//Header composers 
	private int version;
	private int piggyFlag;
	private int teidFlag;
	private int type;
	private String label;
	private long tunnelEndpointId;
	private int sequenceNumber;
	private int spare;
	
    public HeaderGTPV2()
	{
    	this.syntax = "V2";
    	this.version = 2;
	}
	
    public HeaderGTPV2(Array beginArray)
	{
    	this();
        this.version = beginArray.getBits(0,3);
        
    	this.piggyFlag = beginArray.getBits(3, 1);
    	this.teidFlag = beginArray.getBits(4, 1);
    	
    	Array typeArray = beginArray.subArray(1, 1);
    	this.type= (new Integer08Array(typeArray).getValue());
    	
    	Array lengthArray = beginArray.subArray(2, 2);
    	this.length = (new Integer16Array(lengthArray).getValue());
	}

    @Override
    public boolean isRequest() 
    {
    	if (this.type ==3)
    	{
    		return false;   		
    	}
    	if ((this.label != null) && (this.label.contains("Response")))
    	{
    		return false;   		
    	}
    	return true;
    }
    
    @Override
    public String getType() 
    {  
	    return this.label + ":" + this.type;
    }

	@Override
	public void parseFromXML(Element header, Dictionary dictionary) throws Exception
    {
		this.dictionary = dictionary;
		
        String strType = header.attributeValue("type");
        if (strType != null)
        {
            EnumLongField field = (EnumLongField) dictionary.getHeaderFieldByName("Message Type");
            this.type = (int) field.getEnumLong(strType);
        }
        EnumLongField field = (EnumLongField) dictionary.getHeaderFieldByName("Message Type");
        this.label = field.getEnumLabelByValue((long) this.type);

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
        	this.tunnelEndpointId = Long.parseLong(attribute);
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
        
        attribute = header.attributeValue("spare");
        if (attribute != null)
        {
        	this.spare = Integer.parseInt(attribute);
        }
    }

	@Override
    public String toXml()
    {
        String str = "<headerV2 ";
        str += " type=\"";
        if (this.label != null)
        {
        	str += this.label + ":";
        }
        str += this.type + "\"";
        str += " tunnelEndpointId=\"" + this.tunnelEndpointId + "\"";
        str += " sequenceNumber=\"" + this.sequenceNumber + "\"";
        str += " spare=\"" + this.spare + "\"";
        str += " length=\"" + this.length + "\""; 
        str += " piggyFlag=\"" + this.piggyFlag + "\""; 
        str += " teidFlag=\"" + this.teidFlag + "\"";
        str += " version=\"" + this.version + "\"";
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

        supArray.addLast(new Integer08Array(this.type));
        
        supArray.addLast(new Integer16Array(this.length));
        
        if (this.teidFlag != 0)
        {
        	supArray.addLast(new Integer32Array((int) (this.tunnelEndpointId & 0xffffffffl)));
        }
        
        Array sequenceNumberArray= new Integer32Array(this.sequenceNumber);
        supArray.addLast(sequenceNumberArray.subArray(1, 3));
        
        supArray.addLast(new Integer08Array(this.spare));
        
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
	
	public int decodeFromArray(Array array, String syntax, Dictionary dictionary) throws Exception
    {
		this.dictionary = dictionary;
		int offset = 4;
		
    	EnumLongField field = (EnumLongField) dictionary.getHeaderFieldByName("Message Type");
	    this.label = field.getEnumLabelByValue((long) this.type);    	
        
        if (this.teidFlag != 0)
    	{
    	    Array teidArray = array.subArray(offset, 4); 
	        this.tunnelEndpointId = new Integer32Array(teidArray).getValue() & 0xffffffffl;
        	offset = offset + 4;	        
    	}
        
    	Array seqnumArray = array.subArray(offset, 3); 	
		Array zeroArray = new DefaultArray(new byte[]{0});
    	SupArray seqnumSup = new SupArray();		
    	seqnumSup.addFirst(zeroArray);
    	seqnumSup.addLast(seqnumArray);
    	this.sequenceNumber = (new Integer32Array(seqnumSup).getValue());
        offset = offset + 3;
        
        Array spareArray = array.subArray(offset, 1); 
        this.spare = new Integer08Array(spareArray).getValue();
    	offset = offset + 1;
    	return offset;
    }

	@Override
    public void getParameter(Parameter var, String param) throws Exception
    {
    	if (param.equalsIgnoreCase("version"))
        {
            var.add(this.version);
        }
    	else if (param.equalsIgnoreCase("piggyFlag"))
        {
            var.add(this.piggyFlag);
        }
    	else if (param.equalsIgnoreCase("teidFlag"))
        {
            var.add(this.teidFlag);
        }
    	else if (param.equalsIgnoreCase("type"))
        {
            var.add(this.type);
        }
    	else if (param.equalsIgnoreCase("label"))
        {
            var.add(this.label);
        }
    	else if (param.equalsIgnoreCase("name"))
        {
            var.add(this.label + ":" + this.type);
        }
        else if (param.equalsIgnoreCase("tunnelEndpointId"))
        {
            var.add(this.tunnelEndpointId);
        }
        else if (param.equalsIgnoreCase("sequenceNumber"))
        {
            var.add(this.sequenceNumber);
        }
        else if (param.equalsIgnoreCase("spare"))
        {
            var.add(this.spare);
        }      	
        else
        {
        	Parameter.throwBadPathKeywordException("header." + param);
        }
    }
    

}


