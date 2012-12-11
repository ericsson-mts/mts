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

package com.devoteam.srit.xmlloader.core.coding.q931;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.maps.LinkedHashMap;

import gp.utils.arrays.Array;
import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.Integer16Array;
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

    protected int _id;
    protected String _name;
    
    protected LinkedHashMap<String, Field> _hashMapFields = new LinkedHashMap<String, Field>();
    
	protected Array _value;
    protected Array _fields;
    
    protected boolean _bigLength;
    protected Integer08Array _idArray;
    
    public void parseFromXML(Element element, Dictionary dictionary) throws Exception 
    {
        //si elem ds dico on prend dico sinon on envoi ce qu'il y  ads le fichier xml
        String idStr = element.attributeValue("identifier").trim();
        ElementAbstract elemDico = null;
    	try 
    	{
    		byte[] idBytes = Utils.parseBinaryString(idStr);
    		_id = idBytes[0] & 0xff;
            if (idBytes.length > 1)
            {
            	throw new ExecutionException("ISDN layer : Reading the element Id from XML file : value is too long " + idStr);
            }                

    		if (dictionary != null)
    		{
    			elemDico = dictionary.getMapElementById().get(_id);
    		}
    	}
    	catch (Exception e) 
    	{
    		if (dictionary == null)
    		{
    			throw new ExecutionException("ISDN layer : The element identifier \"" + idStr + "\" is not valid : " + idStr);
    		}
    		elemDico = dictionary.getMapElementByName().get(idStr);    		
    		if (elemDico == null)
    		{
            	throw new ExecutionException("ISDN layer : The element \"" + idStr + "\" for the ISDN layer is not present in the dictionnary.");            	            	
            }        				
    		_id = elemDico.getId();
    	}        	    		
        if (elemDico != null)
        {
        	_name = elemDico.getName();
        }
        else
        {
        	_name = element.attributeValue("name");
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
	            if (type.equalsIgnoreCase("integer")) {
	                field = new IntegerField(elemField);
	
	            } else if (type.equalsIgnoreCase("boolean")) {
	
	                field = new BooleanField(elemField);
	
	            } else if (type.equalsIgnoreCase("enumeration")) {
	                field = new EnumerationField(elemField);
	
	            } else if (type.equalsIgnoreCase("string")) {
	                field = new StringField(elemField);	
	            } else if (type.equalsIgnoreCase("binary")) {
	                field = new BinaryField(elemField);
	
	            }else
	            {
	            	throw new ExecutionException("ISDN layer : The field type \"" + type + "\" is not supported : " + idStr);    
	            }
            }
            else
            {
            	// int length = Integer.parseInt(elemField.attributeValue("lengthBit"));
            	// field.setLength(length);
            }
            _hashMapFields.put(elemField.attributeValue("name"), field);
        }
       
    }
    
    public String toString() {

        StringBuilder elemString = new StringBuilder();
        elemString.append("<element ");
        elemString.append("identifier=\"");
    	if (_name != null)
    	{
    		elemString.append(_name + ":");
    	}
    	elemString.append(_idArray.getValue());
        if (_fields != null)
        {
            elemString.append(" value=\"" + Array.toHexString(_fields));
        }
        elemString.append("\">");
        elemString.append("\n");
        for (Entry<String, Field> e : getHashMapFields().entrySet()) {
            elemString.append(e.getValue().toString(this.getFieldsArray()));
        }

        elemString.append("</element>");
        elemString.append("\n");
        return elemString.toString();
    }

    
    public abstract void decodeFromArray(Array array, boolean bigLength, boolean fromdata);
    
    public abstract Array encodeToArray();
    
    public void getParameter(Parameter var, String[] params, String path) throws Exception {
        if (params.length ==3) 
        {
        	if (this._value != null)
        	{
        		var.add(Array.toHexString(this._value));
        	}
        }
        else if (params.length > 4 && (params[3].equalsIgnoreCase("field"))) 
        {
        	Field field = getHashMapFields().get(params[4]);
        	if (field != null)
        	{	
        		var.add(field.getValue(this.getFieldsArray()));
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
        return _id;
    }

	public String getName() {
		return _name;
	}
	
    public Array getFieldsArray() {
        return _fields;
    }

    public void setFields(Array _fields) {
        this._fields = _fields;
    }
    
}
