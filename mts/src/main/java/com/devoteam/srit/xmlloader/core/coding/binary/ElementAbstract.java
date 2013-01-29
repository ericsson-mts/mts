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
import com.devoteam.srit.xmlloader.core.coding.binary.q931.ElementQ931;
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
    protected String coding;
	
    protected int tag;

    protected String label;

	// protected int spare;
	protected int instances;

    protected LinkedHashMap<String, FieldAbstract> _hashMapFields = new LinkedHashMap<String, FieldAbstract>();
    
    protected List<ElementAbstract> elements = new ArrayList<ElementAbstract>();
    
    protected SupArray _fields;
    protected SupArray _elements;

    public static ElementAbstract buildFactory(String coding) throws Exception
    {
    	ElementAbstract newElement = null;
		if ("TLIV".equals(coding))
		{
			newElement = new ElementTLIV();
		}
		else if ("TLV".equals(coding))
		{
			newElement = new ElementTLV();
		}
		else if ("TV".equals(coding))
		{
			newElement = new ElementTV();
		} 
		else if ("Q931".equals(coding))
		{
			newElement = new ElementQ931();
		}
		else
		{
     		throw new ExecutionException("ERROR : The coding attribute for the element is mandatory because the element is not present in the dictionary.");
		}
		newElement.coding = coding;
		return newElement;
    }
    
    public void parseFromXML(Element elementRoot, Dictionary dictionary, ElementAbstract elemDico) throws Exception 
    {
    	if (elemDico == null)
    	{
            //si elem dans dico on prend dico sinon on envoie ce qu'il y a dans le fichier xml
            String tag = elementRoot.attributeValue("identifier");
            if (tag == null)
            {
            	tag = elementRoot.attributeValue("tag");
            }
    		tag = tag.trim();
        	int iPos = tag.indexOf(":");
        	String label = tag;
        	String value = tag;
        	if (iPos >= 0)
        	{
        		label = tag.substring(0, iPos);
        		value = tag.substring(iPos + 1);
        	}
    		
        	int tagInt = getTagValueFromBinary(value);
        	this.tag = tagInt;
        	this.label = label;	
    	}
    	
    	// for Q931 protocols
        String labelTag = elementRoot.attributeValue("name");
        if (labelTag != null)
        {
        	this.label = labelTag;
        }
        
        String instances = elementRoot.attributeValue("instances");
        if (instances != null)
        {
        	this.instances = Integer.parseInt(instances);
        }

        List<Element> listField = elementRoot.elements("field");
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
             	if (type ==  null) 
             	{
             		throw new ExecutionException("ERROR : The type attribute for the field \"" + name + "\" is mandatory because the element tag \"" + tag + "\" is not present in the dictionary.");
             	}
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
	            	throw new ExecutionException("ERROR : The field type \"" + type + "\" is not supported in the element tag : \"" + tag + "\"");
	            }
            }
            this._hashMapFields.put(elemField.attributeValue("name"), field);
        }
        
        // initiate the Array containing the fields
        this._fields = new SupArray();
        Array emptyArray = new DefaultArray(getLengthElem() / 8);
       	this._fields.addFirst(emptyArray);
       	this._elements = new SupArray();
       	
        // set the value for each fields
        listField = elementRoot.elements("field");
        //boucle pour setter tous les field de elemV
        int offset = 0;
        for (Iterator<Element> it = listField.iterator(); it.hasNext();) 
        {
            Element element1 = it.next();
            String fieldName = element1.attributeValue("name");
            FieldAbstract field = this._hashMapFields.get(fieldName);
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
                throw new ExecutionException("The field \"" + fieldName + "\" is not found in the element : \"" + this.tag + "\"");
            }
        }
        
        //parse the sub-elements
        List<Element> listElement = elementRoot.elements("element");
        ElementAbstract subElemDico = null;
        ElementAbstract elem = null;
        for (Iterator<Element> it = listElement.iterator(); it.hasNext();) 
        {
            Element elemElement = it.next();
            if (dictionary  != null)
            {
            	subElemDico = dictionary.getElementFromXML(elemElement);
            	elem = (ElementAbstract) subElemDico.cloneAttribute();
            	elem.parseFromXML(elemElement, dictionary, subElemDico);
            	this.elements.add(elem);
            }
        }
        
    }

    public int getLengthElem() 
    {
        int length = 0;
        for (Entry<String, FieldAbstract> field : _hashMapFields.entrySet()) 
        {
            length += field.getValue()._length;
        }
        return length;
    }

	/**
	 * Return an integer value for the tag from a binary string 
	 * using 'b' 'd' 'h' character to specify the value in respectively 
	 * boolean décimal (default) or hexadecimal 
	 * @param tag
	 * @return
	 * @throws Exception
	 */
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

	public static List<ElementAbstract> decodeElementsFromArray(Array data, Dictionary dictionary) throws Exception 
	{
		List<ElementAbstract> elements = new ArrayList<ElementAbstract>();
	    int offset = 0;
	    while (offset < data.length) 
	    {
	        int tag = new Integer08Array(data.subArray(offset, 1)).getValue();
	        ElementAbstract elemDico = dictionary.getElementByTag(tag);
	        if (elemDico == null)
	        {
				throw new ExecutionException("The element tag \"" + tag + "\" can not be decoded because it is not present in the dictionary.");
	        }

	        ElementAbstract elemNew = null;
	        if (elemDico != null)
	        {
	        	elemNew = (ElementAbstract) elemDico.clone();
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
    
    public ElementAbstract cloneAttribute() throws Exception
    {
		ElementAbstract newElement = buildFactory(this.coding);
		
		newElement.coding = this.coding;
		newElement.tag = this.tag;
		newElement.label = this.label;
		newElement.instances = this.instances;
		
    	return newElement;
    }
    
    protected void copyToCLone(ElementAbstract source) throws Exception
    {
    	this.tag = source.tag;
    	this.label = source.label;
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
    
    public void getParameter(Parameter var, String[] params, String path, int offset, Dictionary dictionary) throws Exception 
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
        	List<ElementAbstract> list = ElementAbstract.getElementsFromTag(this.elements, params[offset + 2], dictionary);
        	 Iterator<ElementAbstract> iter = list.iterator();
 		    while (iter.hasNext())
 		    {
 		    	ElementAbstract elem = (ElementAbstract) iter.next();
 		    	elem.getParameter(var, params, params[offset + 2], offset + 1, dictionary);
 		    }
        }    	
    }

    /** Get all the element from a given list which match a 
     * given tag according to the dictionary
     * @param elements
     * @param tag
     * @param dictionary
     * @return
     * @throws Exception
     */
	public static List<ElementAbstract> getElementsFromTag(List<ElementAbstract> elements, String tag, Dictionary dictionary) throws Exception 
	{
		// case when the instances is specified
    	int iPos = tag.indexOf(',');
    	String tagStr = tag;
    	Integer instances = null;
    	if (iPos >= 0)
    	{
    		tagStr = tag.substring(0, iPos);
    		String instancesStr = tag.substring(iPos + 1);
    		instances = Integer.parseInt(instancesStr);
    	}		
		
		Integer tagInt = dictionary.getElementFromTag(tagStr).getTag();
		
		List<ElementAbstract> list = new ArrayList<ElementAbstract>();
		
	    Iterator<ElementAbstract> iter = elements.iterator();
	    while (iter.hasNext())
	    {
	    	ElementAbstract elem = (ElementAbstract) iter.next();
	        if ((tagInt == elem.getTag()) && ((instances == null) || (instances.equals(elem.instances))))
	        {
	        	list.add(elem);
	        }
	    }
	    return list;
	}

    public String toString() 
    {
        StringBuilder elemString = new StringBuilder();
        elemString.append("<element ");
        elemString.append("tag=\"");
    	if (this.label != null)
    	{
    		elemString.append(this.label + ":");
    	}
    	elemString.append(this.tag);
    	elemString.append("\"");
    	elemString.append(" instances=\"");
   		elemString.append(this.instances);
    	elemString.append("\"");
    	elemString.append(" coding=\"");
   		elemString.append(this.coding);
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

    public LinkedHashMap<String, FieldAbstract> getHashMapFields() {
        return _hashMapFields;
    }

    public int getTag() {
        return this.tag;
    }

	public String getLabel() {
		return this.label;
	}

}
