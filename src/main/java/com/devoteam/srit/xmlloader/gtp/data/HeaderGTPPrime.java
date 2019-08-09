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
import gp.utils.arrays.SupArray;

/**
 *
 * @author Fabien Henry
 */
public class HeaderGTPPrime extends HeaderAbstract
{
    private int version;
    private int protocolType;    
    private int type;
	private String label;
    private int sequenceNumber;
    
    public HeaderGTPPrime() 
    {
    	this.syntax = "Prime";
    	this.protocolType = 0; 
    	this.version = 0; 
	}
    public HeaderGTPPrime(Array beginArray) 
    {
    	this();
        this.version = beginArray.getBits(0,3);
        this.protocolType = beginArray.getBits(3,1);
        
    	Array typeArray = beginArray.subArray(1, 1);
    	this.type= (new Integer08Array(typeArray).getValue());
    	
    	Array lengthArray = beginArray.subArray(2, 2);
    	this.length = (new Integer16Array(lengthArray).getValue());
	}
    
    @Override
    public boolean isRequest() 
    {
    	if ((this.label != null) && (!this.label.contains("Request")))
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
                
        attribute = header.attributeValue("sequenceNumber");
        if (attribute != null)
        {
        	this.sequenceNumber = Integer.parseInt(attribute);
        }
    }

	@Override
    public String toXml()
    {
        String str = "<headerPrime ";
        str += " type=\"";
        if (this.label != null)
        {
        	str += this.label + ":";
        }
        str += this.type + "\""; 
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

        supArray.addLast(new Integer08Array(type));
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
	public int decodeFromArray(Array array, String syntax, Dictionary dictionary) throws Exception
	{
		this.dictionary = dictionary;
		int offset = 4;
		
    	EnumLongField field = (EnumLongField) dictionary.getHeaderFieldByName("Message Type");
    	this.label = field.getEnumLabelByValue((long) this.type);
    	
    	Array seqnumArray = array.subArray(offset, 2); 	
    	this.sequenceNumber = (new Integer16Array(seqnumArray).getValue());
        offset = offset + 2;
        
        return offset;
    }
    
    @Override
    public void getParameter(Parameter var, String param) throws Exception
    {
    	if (param.equalsIgnoreCase("version"))
        {
            var.add(this.version);
        }
       	else if (param.equalsIgnoreCase("protocolType"))
        {
            var.add(this.protocolType);
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
        else if (param.equalsIgnoreCase("sequenceNumber"))
        {
            var.add(this.sequenceNumber);
        }      	
        else
        {
        	Parameter.throwBadPathKeywordException("header." + param);
        }
    }

}
