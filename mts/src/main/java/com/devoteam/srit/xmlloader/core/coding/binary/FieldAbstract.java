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

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;

import org.dom4j.Element;


/**
 *
 * @author indiaye
 */
public abstract class FieldAbstract 
{

	public void setOffset(int offset) {
		this.offset = offset;
	}

	protected String name;
    protected int length;

	protected int offset;

    public FieldAbstract() 
    {
    	this.name = "";
    	this.length = 0;
    	this.offset = 0;
	}

    public FieldAbstract(Element rootXML) 
    {
        name = rootXML.attributeValue("name");
        this.length = 0;
        String lengthBit = rootXML.attributeValue("lengthBit");
        if (lengthBit != null) 
        {
            this.length = Integer.parseInt(lengthBit);
        }
    }

    public static FieldAbstract parseFromXML(Element fieldRoot) throws Exception
    {
     	String type = fieldRoot.attributeValue("type");
     	String name = fieldRoot.attributeValue("name");
     	FieldAbstract newField = null;
    	if (type ==  null) 
     	{
     		throw new ExecutionException("ERROR : The type attribute for the field \"" + name + "\" is mandatory because the element he belongs to is not present in the dictionary.");
     	}
     	if (type.equalsIgnoreCase("integer")) 
        {
            newField = new IntegerField(fieldRoot);
        } 
        else if (type.equalsIgnoreCase("boolean")) 
        {
        	newField = new BooleanField(fieldRoot);
        } 
        else if (type.equalsIgnoreCase("enumeration")) 
        {
        	newField = new EnumerationField(fieldRoot);
        } 
        else if (type.equalsIgnoreCase("string")) 
        {	
        	newField = new StringField(fieldRoot);	
        }
        else if (type.equalsIgnoreCase("length_string")) 
        {
        	newField = new LengthStringField(fieldRoot);	
        }
        else if (type.equalsIgnoreCase("length2_string")) 
        {
        	newField = new Length2StringField(fieldRoot);	
        }	            
        else if (type.equalsIgnoreCase("binary")) 
        {
        	newField = new BinaryField(fieldRoot);
        }
        else if (type.equalsIgnoreCase("number_bcd")) 
        {
        	newField = new NumberBCDField(fieldRoot);
        }
        else if (type.equalsIgnoreCase("number_mmc")) 
        {
        	newField = new NumberMMCField(fieldRoot);
        }	            
        else if (type.equalsIgnoreCase("ipv4_address")) 
        {
        	newField = new IPV4AddressField(fieldRoot);
        }
        else if (type.equalsIgnoreCase("ipv6_address")) 
        {
        	newField = new IPV6AddressField(fieldRoot);
        }	            	            
        else
        {
        	throw new ExecutionException("ERROR : The field type \"" + type + "\" is not supported in the field \"" + name + "\"");
        }
    return newField;
    }

    public abstract String getValue(Array array)throws Exception;

    public abstract void setValue(String value, int offset, SupArray array) throws Exception;
    
    public abstract FieldAbstract clone();
    
    protected void copyToClone(FieldAbstract source) 
    {
    	this.name = source.name;
    	this.length = source.length;
    	this.offset = source.offset;
    }
    
    public String toXml(Array array) {

        StringBuilder elemString = new StringBuilder();
        elemString.append("    <field ");
        elemString.append("name=\"" + this.name + "\" ");
        try
        {
        	elemString.append("value=\"" + this.getValue(array) + "\" ");
        }
        catch (Exception e)
        {
        	// GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.CORE, e, "Exception in toString() method for field " + this._name);
        	// nothing to do 
        }
        elemString.append("type=\"" + this.getClass().getSimpleName().split("Field")[0] + "\" ");
        elemString.append("lengthBit=\"" + this.length + "\" ");
        elemString.append("/>\n");
        return elemString.toString();
    }
    
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
		if (length == 0)
		{
			array.addLast(valueArray);
		}
		else
		{
			byte[] bytes = valueArray.getBytes();
	    	// for (int i = 0 ; i < this.length; i++)
		    for (int i = bytes.length - 1; i >= 0; i--)
	    	{
	    		int pos = (offset + length) / 8 - bytes.length + i;
	    		array.set(pos, bytes[i] & 0xff);
	    	}	
		}
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

    public String toString() 
    {
    	Array array = new DefaultArray(0);
    	return toXml(array);
    }

}
