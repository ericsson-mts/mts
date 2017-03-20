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
import gp.utils.arrays.SupArray;

import com.devoteam.srit.xmlloader.asn1.ASNToXMLConverter;
import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import org.dom4j.Element;


/**
 *
 * @author indiaye
 */
public abstract class FieldAbstract 
{
	
	protected String name;
    protected String description;
    
    protected int length = Integer.MIN_VALUE;

	protected int offset;

    public FieldAbstract() 
    {
    	this.name = "";
    	this.length = Integer.MIN_VALUE;
    	this.offset = 0;
	}

    public void parseFromXML(Element rootXML, boolean parseDico) 
    {
        this.name = rootXML.attributeValue("name");
        this.description = rootXML.attributeValue("description");
        String lengthBit = rootXML.attributeValue("lengthBit");
        if (lengthBit != null && (this.length >= 0 || parseDico)) 
        {
            this.length = Integer.parseInt(lengthBit);
        }
        String length = rootXML.attributeValue("length");
        if (length != null && (this.length >= 0 || parseDico)) 
        {
            this.length = Integer.parseInt(length) * 8;
        }
    }

    public static FieldAbstract buildFactory(Element fieldRoot) throws Exception
    {
     	String type = fieldRoot.attributeValue("type");
     	String name = fieldRoot.attributeValue("name");
     	FieldAbstract newField = null;
    	if (type ==  null) 
     	{
     		throw new ExecutionException("ERROR : The type attribute for the field \"" + name + "\" is mandatory because the element he belongs to is not present in the dictionary.");
     	}
    	else if (type.equalsIgnoreCase("boolean")) 
        {
        	newField = new BooleanField();
        } 
        else if (type.equalsIgnoreCase("integer")) 
        {
            newField = new IntegerField();
        } 
        else if (type.equalsIgnoreCase("enumlong")) 
        {
        	newField = new EnumLongField();
        }
        else if (type.equalsIgnoreCase("enumeration")) 
        {
        	newField = new EnumLongField();
        }
        else if (type.equalsIgnoreCase("enum_string")) 
        {
        	newField = new EnumStringField();
        }
        else if (type.equalsIgnoreCase("string")) 
        {	
        	newField = new StringField();	
        }
        else if (type.equalsIgnoreCase("length_string")) 
        {
        	newField = new LengthStringField();	
        }
        else if (type.equalsIgnoreCase("length2_string")) 
        {
        	newField = new Length2StringField();	
        }	            
        else if (type.equalsIgnoreCase("binary")) 
        {
        	newField = new BinaryField();
        }
        else if (type.equalsIgnoreCase("number_bcd") || type.equalsIgnoreCase("numberbcd")) 
        {
        	newField = new NumberBCDField();
        }
        else if (type.equalsIgnoreCase("number_mmc") || type.equalsIgnoreCase("numbermmc")) 
        {
        	newField = new NumberMMCField();
        }
        else if (type.equalsIgnoreCase("ipv4_address") || type.equalsIgnoreCase("ipv4address")) 
        {
        	newField = new IPV4AddressField();
        }
        else if (type.equalsIgnoreCase("ipv6_address") || type.equalsIgnoreCase("ipv6address")) 
        {
        	newField = new IPV6AddressField();
        }
        else
        {
        	throw new ExecutionException("ERROR : The field type \"" + type + "\" is not supported in the field \"" + name + "\"");
        }
    return newField;
    }

    public abstract String getValue(Array array)throws Exception;

    public abstract void setValue(String value, int offset, SupArray array) throws Exception;
    
    public abstract void initValue(int index, int offset, SupArray array) throws Exception;
    
    public abstract FieldAbstract clone();
    
    protected void copyToClone(FieldAbstract source) 
    {
    	this.name = source.name;
    	this.length = source.length;
    	this.offset = source.offset;
    }
    
    public String toXml(Array array, int indent) 
    {
        String strVal = null;
        try
        {
        	strVal = this.getValue(array);
        }
        catch (Exception e)
        {
        	// GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.CORE, e, "Exception in toString() method for field " + this._name);
        	// nothing to do 
        }
        StringBuilder elemString = new StringBuilder();
        elemString.append(ASNToXMLConverter.indent(indent));
        elemString.append("<field");
        elemString.append(" name=\"");
        elemString.append(this.name);
        elemString.append("\"");
        String type = this.getClass().getSimpleName().split("Field")[0];
        if ("NumberBCD".equalsIgnoreCase(type))
        {
        	type = "Number_BCD";
        }        
        if (strVal != null)
        {        
        	elemString.append(" value=\"");
        	// if there are non printable characters
        	if (!Utils.hasPrintableChar(strVal))
        	{
        		Array arrayVal = new DefaultArray(strVal.getBytes());
        		strVal = Array.toHexString(arrayVal);
        		type = "Binary";
        	}
    		// replace escape XML character
    		strVal = Utils.escapeXMLEntities(strVal);
        	elemString.append(strVal);        	
		    elemString.append("\"");
        }
        elemString.append(" type=\"");
        elemString.append(type);
        elemString.append("\"");	        
        int intLen = this.length;
        if (intLen < 0 && strVal != null)
        { 
        	if (type.toLowerCase().contains("binary"))
        	{
        		intLen = strVal.length() * 8 / 2;
        	}
        	else
        	{
        		intLen = strVal.length() * 8;
        	}
        }
        if (intLen >= 0)
        {
	        if (intLen % 8 == 0)
	        {
	        	elemString.append(" length=\"");
	        	elemString.append((int) (intLen / 8));
	        	elemString.append("\"");
	        }
	        else
	        {
	        	elemString.append(" lengthBit=\"");
	        	elemString.append( (int) intLen);
	        	elemString.append("\"");
	        }
        }
    	elemString.append("/>\n");
	    return elemString.toString();
    }

    /*
    private static boolean searchListChar(String str, String listChar)
    {
    	if (str == null)
    	{ 
    		return false;
    	}
    	for (int i = 0; i < listChar.length(); i++)
    	{
    		if (str.indexOf(listChar.charAt(i)) >= 0)
    		{ 
    			return true; 
    		}
    	}
    	return false;
    }
    */
    
    protected void permuteByte(byte[] bytes) throws Exception 
    {
		int i = 0;
		while (i < bytes.length - 1)
		{
			byte temp = bytes[i];
			bytes[i] = bytes[i + 1];
			bytes[i + 1] = temp;
			i = i + 2;
		}
    }

    protected void setValueFromArray(Array valueArray, int offset, SupArray array)
    {
    	if (this.length < 0)
		{
			array.addLast(valueArray);
		}
		else
		{
			byte[] bytes = valueArray.getBytes();
	    	//for (int i = 0 ; i < this.length; i++)
		    for (int i = bytes.length - 1; i >= 0; i--)
	    	{
	    		int pos = (offset + length) / 8 - bytes.length + i;
	    		if (pos >= 0 && pos < array.length)
	    		{
	    			array.set(pos, bytes[i] & 0xff);
	    		}
	    	}	
		}
    	this.offset = offset; 
    	//this.length = valueArray.length * 8;
    }
    
    protected void setValueFromBytes(byte[] values, int offset, SupArray array)
    {
		if (length == 0)
		{
			Array valueArray = new DefaultArray(values);
			array.addLast(valueArray);
		}
		else
		{
	    	// for (int i = 0 ; i < this.length; i++)
		    for (int i = values.length - 1; i >= 0; i--)
	    	{
	    		int pos = (offset + length) / 8 - values.length + i;
	    		array.set(pos, values[i] & 0xff);
	    	}	
		}
    }

    public void getParameter(Parameter var, String[] params, String path, int offset, Dictionary dictionary, Array fieldArray) throws Exception 
    {
    	if (params.length == offset) 
        {
            String strVal = null;
            try
            {
            	strVal = this.getValue(fieldArray);
            }
            catch (Exception e)
            {
            	// GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.CORE, e, "Exception in toString() method for field " + this._name);
            	// nothing to do 
            }

    		if (strVal !=  null)
    		{
    			var.add(strVal);
    		}
        }
        else
        {
           	Parameter.throwBadPathKeywordException(path);
        }    	
    }
    
    public String toString() 
    {
    	Array array = new DefaultArray(0);
    	return toXml(array, 4);
    }

	public int getLength() 
	{
		if (this.length >= 0)
		{
			return length;
		}
		return 0;
	}

    
	public void setOffset(int offset) 
	{
		this.offset = offset;
	}

}
