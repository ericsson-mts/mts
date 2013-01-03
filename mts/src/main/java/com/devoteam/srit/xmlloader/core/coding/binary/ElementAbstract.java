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

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.maps.LinkedHashMap;
import com.devoteam.srit.xmlloader.gtp.data.ElementTLIV;
import com.devoteam.srit.xmlloader.gtp.data.ElementTLV;
import com.devoteam.srit.xmlloader.gtp.data.ElementTV;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.SupArray;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.dom4j.Element;


/**
 *
 * @author Fabien Henry
 */
public abstract class ElementAbstract
{

    protected int id;
    protected String name;
    
    protected LinkedHashMap<String, Field> _hashMapFields = new LinkedHashMap<String, Field>();
    
    protected SupArray _fields;
    
    protected boolean _bigLength;

    public static ElementAbstract buildFactory(Element root)
    {
    	String coding = root.attributeValue("coding");
		if ("TLIV".equals(coding))
		{
			return new ElementTLIV();
		}
		else if ("TLV".equals(coding))
		{
			return new ElementTLV();
		}
		else if ("TV".equals(coding))
		{
			return new ElementTV();
		}		
		return null;
    }
    
    public void parseFromXML(Element element, Dictionary dictionary) throws Exception 
    {
        //si elem dans dico on prend dico sinon on envoie ce qu'il y a dans le fichier xml
        String idStr = element.attributeValue("identifier").trim();
        ElementAbstract elemDico = null;
    	try 
    	{
    		byte[] idBytes = Utils.parseBinaryString(idStr);
    		this.id = idBytes[0] & 0xff;
            if (idBytes.length > 1)
            {
            	throw new ExecutionException("ERROR : Reading the element Id from XML file : value is too long " + idStr);
            }                
            if (dictionary != null)
            {
            	elemDico = dictionary.getMapElementById().get(this.id);
            }
    	}
    	catch (Exception e) 
    	{
    		if (dictionary != null)
    		{
    			elemDico = dictionary.getMapElementByName().get(idStr);
    		}
    		if (elemDico == null)
    		{
            	throw new ExecutionException("ERROR : The element \"" + idStr + "\" for the ISDN layer is not present in the dictionnary.");            	            	
            }        				
    		this.id = elemDico.getId();
    	}
    	
    	
        if (elemDico != null)
        {
        	this.name = elemDico.getName();
        }
        else
        {
        	this.name = element.attributeValue("name");
        }
        
        List<Element> listField = element.elements("field");
        for (Iterator<Element> it = listField.iterator(); it.hasNext();) {
            Element elemField = it.next();
            String name = elemField.attributeValue("name");
            Field field = null;
            if (elemDico != null)
            {
            	field = elemDico.getHashMapFields().get(name); 
            }
            if (field == null)
            {
             	String type = elemField.attributeValue("type");
	            if (type.equalsIgnoreCase("integer")) 
	            {
	                field = new IntegerField(elemField);
	            } 
	            else if (type.equalsIgnoreCase("boolean")) 
	            {
	                field = new BooleanField(elemField);
	            } 
	            else if (type.equalsIgnoreCase("enumeration")) 
	            {
	                field = new EnumerationField(elemField);
	            } 
	            else if (type.equalsIgnoreCase("string")) 
	            {
	                field = new StringField(elemField);	
	            }
	            else if (type.equalsIgnoreCase("length_string")) 
	            {
	                field = new LengthStringField(elemField);	
	            }
	            else if (type.equalsIgnoreCase("length2_string")) 
	            {
	                field = new Length2StringField(elemField);	
	            }	            
	            else if (type.equalsIgnoreCase("binary")) 
	            {
	                field = new BinaryField(elemField);
	
	            }
	            else if (type.equalsIgnoreCase("number_bcd")) 
	            {
	                field = new NumberBCDField(elemField);
	            }
	            else if (type.equalsIgnoreCase("number_mmc")) 
	            {
	                field = new NumberMMCField(elemField);
	            }	            
	            else if (type.equalsIgnoreCase("ipv4_address")) 
	            {
	                field = new IPV4AddressField(elemField);
	            }
	            else if (type.equalsIgnoreCase("ipv6_address")) 
	            {
	                field = new IPV6AddressField(elemField);
	            }	            	            
	            else
	            {
	            	throw new ExecutionException("ERROR : The field type \"" + type + "\" is not supported : " + idStr);    
	            }
            }
            else
            {
            	// int length = Integer.parseInt(elemField.attributeValue("lengthBit"));
            	// field.setLength(length);
            }
            this._hashMapFields.put(elemField.attributeValue("name"), field);
        }
        
        // initiate the Array containing the fields
        this._fields = new SupArray();
        Array emptyArray = new DefaultArray(getLengthElem() / 8);
        if (emptyArray.length > 0)
        {
        	this._fields.addFirst(emptyArray);
        }
        
        // set the value for each fields
        listField = element.elements("field");
        //boucle pour setter tous les field de elemV
        int offset = 0;
        for (Iterator<Element> it = listField.iterator(); it.hasNext();) 
        {
            Element element1 = it.next();
            Field field = this._hashMapFields.get(element1.attributeValue("name"));
            if (field != null) 
            {
            	String value = element1.attributeValue("value");
            	if (value != null)
            	{
			        Array result = field.setValue(element1.attributeValue("value"), offset, this._fields);
			        int length = field.getLength();
			        if (length != 0)
			        {
				        offset += length;
			        }
			        else
			        {
				        offset = this._fields.length * 8;
			        }
            	}
            }
            else 
            {
                throw new ExecutionException("The field " + element1.attributeValue("name") + " is not found in element : " + this.name + ":" + this.id);
            }
        }               
    }
    
    public String toString() {

        StringBuilder elemString = new StringBuilder();
        elemString.append("<element ");
        elemString.append("identifier=\"");
    	if (this.name != null)
    	{
    		elemString.append(this.name + ":");
    	}
    	elemString.append(this.id);
        if (_fields != null)
        {
            elemString.append(" value=\"" + Array.toHexString(_fields));
        }
        elemString.append("\">");
        elemString.append("\n");
        for (Entry<String, Field> e : this._hashMapFields.entrySet()) {
            elemString.append(e.getValue().toString(this._fields));
        }

        elemString.append("</element>");
        elemString.append("\n");
        return elemString.toString();
    }

    
    public abstract void decodeFromArray(Array array, boolean bigLength);
    
    public abstract Array encodeToArray();
    
    public void getParameter(Parameter var, String[] params, String path, int offset) throws Exception 
    {
    	if (params.length == offset + 2) 
        {
        	if (this._fields != null)
        	{
        		var.add(Array.toHexString(this._fields));
        	}
        }
        else if (params.length >= offset + 4 && (params[offset + 2].equalsIgnoreCase("field"))) 
        {
        	Field field = this._hashMapFields.get(params[offset + 3]);
        	if (field != null)
        	{	
        		var.add(field.getValue(this._fields));
        	}
        }
        else
        {
           	Parameter.throwBadPathKeywordException(path);
        }
    }
    
    public int getLengthElem() {
        int length = 0;
        for (Entry<String, Field> field : _hashMapFields.entrySet()) {
            length += field.getValue().getLength();
        }
        return length;
    }

    public LinkedHashMap<String, Field> getHashMapFields() {
        return _hashMapFields;
    }

    public int getId() {
        return this.id;
    }

	public String getName() {
		return this.name;
	}
    
}
