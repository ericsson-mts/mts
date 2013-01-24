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
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.gtp.data.ElementTLIV;
import com.devoteam.srit.xmlloader.gtp.data.ElementTLV;
import com.devoteam.srit.xmlloader.gtp.data.ElementTV;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.SupArray;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import org.dom4j.Element;


/**
 *
 * @author Fabien Henry
 */
public abstract class ElementAbstract implements Cloneable
{

    protected int id;

    protected String name;

	// protected int spare;
	protected int instances;

    protected LinkedHashMap<String, FieldAbstract> _hashMapFields = new LinkedHashMap<String, FieldAbstract>();
    
    protected List<ElementAbstract> elements = new ArrayList<ElementAbstract>();
    
    protected SupArray _fields;
    protected SupArray _elements;

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
        String tag = element.attributeValue("identifier");
        if (tag != null)
        {
        	tag = tag.trim();
        }
        if (tag == null)
        {
        	tag = element.attributeValue("tag");
            if (tag != null)
            {
            	tag = tag.trim();
            }
        }

        ElementAbstract elemDico = this.parseTagFromDictionary(tag, dictionary);
        String nameTag = element.attributeValue("name");
        if (nameTag != null)
        {
        	this.name = nameTag;
        }
        
        String instances = element.attributeValue("instances");
        if (instances != null)
        {
        	this.instances = Integer.parseInt(instances);
        }

        List<Element> listField = element.elements("field");
        for (Iterator<Element> it = listField.iterator(); it.hasNext();) {
            Element elemField = it.next();
            String name = elemField.attributeValue("name");
            FieldAbstract field = null;
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
	            	throw new ExecutionException("ERROR : The field type \"" + type + "\" is not supported : " + tag);    
	            }
            }

            String length = elemField.attributeValue("lengthBit");
            if (length != null)
            {
            	// BUG dans Sigtran 105_Q931_DISCONNECT et autres : incohrence entre le dictionnaire et le script
            	// field.setLength(Integer.parseInt(length));
            }

            this._hashMapFields.put(elemField.attributeValue("name"), field);
        }
        
        // initiate the Array containing the fields
        this._fields = new SupArray();
        Array emptyArray = new DefaultArray(getLengthElem() / 8);
       	this._fields.addFirst(emptyArray);
       	this._elements = new SupArray();
       	
        // set the value for each fields
        listField = element.elements("field");
        //boucle pour setter tous les field de elemV
        int offset = 0;
        for (Iterator<Element> it = listField.iterator(); it.hasNext();) 
        {
            Element element1 = it.next();
            FieldAbstract field = this._hashMapFields.get(element1.attributeValue("name"));
            if (field != null) 
            {
            	String value = element1.attributeValue("value");
            	if (value != null)
            	{
			        field.setValue(element1.attributeValue("value"), offset, this._fields);
			        int length = field._length;
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
        
        List<Element> listElement = element.elements("element");
        ElementAbstract elemInfo = null;
        for (Iterator<Element> it = listElement.iterator(); it.hasNext();) 
        {
            Element elemElement = it.next();
            elemInfo = ElementAbstract.buildFactory(elemElement);
	        elemInfo.parseFromXML(elemElement, dictionary);
	        
	        this.elements.add(elemInfo);
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
    	elemString.append("\"");
    	elemString.append(" instances=\"");
   		elemString.append(this.instances);
    	elemString.append("\"");   	
        if (_fields != null)
        {
            elemString.append(" value=\"" + Array.toHexString(_fields));
        }
        elemString.append("\">");
        
        elemString.append("\n");
        
        
        Iterator<FieldAbstract> iterField = this._hashMapFields.values().iterator();
		while (iterField.hasNext())
		{
			FieldAbstract field = (FieldAbstract) iterField.next();
            elemString.append(field.toString(this._fields));
        }
        
        Iterator<ElementAbstract> iterElem = this.elements.iterator();
		while (iterElem.hasNext())
		{
			ElementAbstract elemInfo = (ElementAbstract) iterElem.next();
            elemString.append("    ");
            elemString.append(elemInfo.toString());
        }
        
        elemString.append("</element>");
        elemString.append("\n");
        return elemString.toString();
    }
    
	public static List<ElementAbstract> decodeElementsFromArray(Array data, Dictionary dictionary) throws Exception 
	{
		List<ElementAbstract> elements = new ArrayList<ElementAbstract>();
	    int offset = 0;
	    while (offset < data.length) 
	    {
	        int id = new Integer08Array(data.subArray(offset, 1)).getValue();
	        ElementAbstract elemDico = dictionary.getMapElementById().get(id);
	        
	        ElementAbstract elemNew = null;
	        if (elemDico != null)
	        {
	        	elemNew = (ElementAbstract) elemDico.clone();
	        }
	        else
	        {
	        	elemNew =  new ElementTLIV();
	        }
	        
	
	        int length = elemNew.decodeFromArray(data.subArray(offset), dictionary);
	        offset += length;
	
	        elements.add(elemNew);
	    }
	    return elements;
	
	}
    
    public abstract int decodeFromArray(Array array, Dictionary dictionary) throws Exception;
    
    public SupArray encodeToArray()
    {
    	SupArray sup = new SupArray();
		// encode the sub-element
		Iterator<ElementAbstract> iter = this.elements.iterator();
		while (iter.hasNext())
		{
			ElementAbstract elemInfo = (ElementAbstract) iter.next();
			Array array = elemInfo.encodeToArray();
			sup.addLast(array);
		}
		return sup;

    }
    
    protected void copyToCLone(ElementAbstract source) throws Exception
    {
    	this.id = source.id;
    	this.name = source.name;
    	this._fields = null;
		// encode the sub-element
		Iterator<ElementAbstract> iter = source.elements.iterator();
		while (iter.hasNext())
		{
			ElementAbstract elemOld = (ElementAbstract) iter.next();
			ElementAbstract elemNew = (ElementAbstract) elemOld.clone();
			this.elements.add(elemNew);
		}
        Iterator<FieldAbstract> iterField = source._hashMapFields.values().iterator();
		while (iterField.hasNext())
		{
			FieldAbstract fieldOld = (FieldAbstract) iterField.next();
			FieldAbstract fieldNew = fieldOld.clone();
			this._hashMapFields.put(fieldNew._name, fieldNew);
		}
    }
    
    public void getParameter(Parameter var, String[] params, String path, int offset) throws Exception 
    {
    	if (params.length == offset + 2) 
        {
    		String value = "";
        	if (this._fields != null)
        	{
        		value = Array.toHexString(this._fields);
        	}
        	if (this._elements != null)
        	{
        		value += Array.toHexString(this._elements);
        	}
    		var.add(value);
        }
        else if (params.length >= offset + 4 && (params[offset + 2].equalsIgnoreCase("field"))) 
        {
        	FieldAbstract field = this._hashMapFields.get(params[offset + 3]);
        	if (field != null)
        	{	
        		var.add(field.getValue(this._fields));
        	}
        }
        else 
        {
        	List<ElementAbstract> list = ElementAbstract.getElements(this.elements, Integer.parseInt(params[offset + 2]));
        	 Iterator<ElementAbstract> iter = list.iterator();
 		    while (iter.hasNext())
 		    {
 		    	ElementAbstract elem = (ElementAbstract) iter.next();
 		    	elem.getParameter(var, params, params[offset + 2], offset + 1);
 		    }
        }    	
    }
    
	public static List<ElementAbstract> getElements(List<ElementAbstract> elements, int id) throws Exception 
	{
		List<ElementAbstract> list = new ArrayList<ElementAbstract>();
		
	    Iterator<ElementAbstract> iter = elements.iterator();
	    while (iter.hasNext())
	    {
	    	ElementAbstract elem = (ElementAbstract) iter.next();
	        if (id == elem.getId())
	        {
	        	list.add(elem);
	        }
	    }
	    return list;
	}

    private ElementAbstract parseTagFromDictionary(String tag, Dictionary dictionary) throws Exception
    {
    	int iPos = tag.indexOf(":");
    	String label = tag;
    	String value = tag;
    	if (iPos >= 0)
    	{
    		label = tag.substring(0, iPos);
    		value = tag.substring(iPos + 1);
    	}
    	// case if the tag is set by the value as a binary vale (with 'b' 's' 'd' prefix)
    	Integer valueInt = getTagValueFromBinary(value);
    	// check the consistency between value and label
    	ElementAbstract elemById = dictionary.getMapElementById().get(valueInt);
		if (elemById != null && !label.equalsIgnoreCase(elemById.getName()))
		{
			GlobalLogger.instance().getApplicationLogger().warn(Topic.PROTOCOL, "The element label \"" + label + "\" does not match the tag/id \"" + value + "\" in the dictionary.");
		}
		// return first by the tag name
		ElementAbstract elemByName = dictionary.getMapElementByName().get(label);
    	if (elemByName != null)
    	{
    		this.id = elemByName.id;
    		this.name = label;
    		return elemByName;
    	}
    	// return first by the tag value
    	if (elemById != null)
    	{
        	this.id = valueInt;
    		this.name = elemById.name;
    		return elemById;
    	}
    	this.id = valueInt;
    	this.name = label;
    	return null;
    }

    public static Integer getTagValueFromBinary(String tag) throws Exception
    {
    	if (tag.length() > 0)
    	{
	    	if (tag.charAt(0) == 's')
	        {
				return null;
	        }
			byte[] idBytes = new byte[]{};
	    	try
	    	{
	    		idBytes = Utils.parseBinaryString(tag);
	    	}
	    	catch (Exception e)
	    	{
	    		return null;
	    	}
	    	if (idBytes.length != 1)
	        {
	    		return null;
	        }
	    	return idBytes[0] & 0xff;
    	}
    	return null;
    }

    public int getLengthElem() {
        int length = 0;
        for (Entry<String, FieldAbstract> field : _hashMapFields.entrySet()) {
            length += field.getValue()._length;
        }
        return length;
    }

    public LinkedHashMap<String, FieldAbstract> getHashMapFields() {
        return _hashMapFields;
    }

    public int getId() {
        return this.id;
    }

	public String getName() {
		return this.name;
	}
    
}
