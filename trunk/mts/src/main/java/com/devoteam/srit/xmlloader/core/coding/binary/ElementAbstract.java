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
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.gtp.data.ElementTL1V;
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

    protected LinkedHashMap<String, FieldAbstract> fieldsByName = new LinkedHashMap<String, FieldAbstract>();
    protected List<FieldAbstract> fields = new ArrayList<FieldAbstract>();
    
    protected List<ElementAbstract> elements = new ArrayList<ElementAbstract>();
    
    protected SupArray fieldsArray;
    protected SupArray subelementsArray;

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
		else if ("TL1V".equals(coding))
		{
			newElement = new ElementTL1V();
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
        //si non present dans le dico on parse le fichier xml
    	if (elemDico == null)
    	{
            String tagStr = elementRoot.attributeValue("identifier");
            if (tagStr == null)
            {
            	tagStr = elementRoot.attributeValue("tag");
            }
    		tagStr = tagStr.trim();
        	int iPos = tagStr.indexOf(":");
        	String label = null;
        	String value = tagStr;
        	if (iPos >= 0)
        	{
        		label = tagStr.substring(0, iPos);
        		value = tagStr.substring(iPos + 1);
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
            Element fieldRoot = it.next();
            String name = fieldRoot.attributeValue("name");
            FieldAbstract field = null;
            // Case if field is present in the dico
            if (elemDico != null)
            {
            	field = elemDico.getFieldsByName(name); 
            }
            if (field == null)
            {
            	field = FieldAbstract.parseFromXML(fieldRoot);
            }
            this.fieldsByName.put(name, field);
            this.fields.add(field);
        }
        
        // initiate the Array containing the fields
        this.fieldsArray = new SupArray();
        Array emptyArray = new DefaultArray(getLengthElem() / 8);
       	this.fieldsArray.addFirst(emptyArray);
       	this.subelementsArray = new SupArray();
       	
        // set the value for each fields
        listField = elementRoot.elements("field");
        //boucle pour setter tous les field de elemV
        int offset = 0;
        for (Iterator<Element> it = listField.iterator(); it.hasNext();) 
        {
            Element element1 = it.next();
            String fieldName = element1.attributeValue("name");
            FieldAbstract field = this.fieldsByName.get(fieldName);
            if (field != null) 
            {
            	String value = element1.attributeValue("value");
            	if (value != null)
            	{
			        field.setValue(value, offset, this.fieldsArray);
            	}
            	else
            	{
            		field.setOffset(offset);
            	}
		        int length = field.length;
		        if (length != 0)
		        {
			        offset += length;
		        }
		        else
		        {
			        offset = this.fieldsArray.length * 8;
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
		Iterator<FieldAbstract> iter = this.fields.iterator();
		while (iter.hasNext())
		{
			FieldAbstract field = (FieldAbstract) iter.next();
            length += field.length;
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
    	this.fieldsArray = null;
		// encode the sub-element
		Iterator<ElementAbstract> iter = source.elements.iterator();
		while (iter.hasNext())
		{
			ElementAbstract elemOld = (ElementAbstract) iter.next();
			ElementAbstract elemNew = (ElementAbstract) elemOld.clone();
			this.elements.add(elemNew);
		}
		Iterator<FieldAbstract> iterField = source.fields.iterator();
		while (iter.hasNext())
		{
			FieldAbstract fieldOld = (FieldAbstract) iterField.next();
			FieldAbstract fieldNew = fieldOld.clone();
			this.fieldsByName.put(fieldNew.name, fieldNew);
			this.fields.add(fieldNew);
		}
    }
    
    public void getParameter(Parameter var, String[] params, String path, int offset, Dictionary dictionary) throws Exception 
    {
    	if (params.length == offset + 2) 
        {
    		String value = "";
        	if (this.fieldsArray != null)
        	{
        		value = Array.toHexString(this.fieldsArray);
        	}
        	if (this.subelementsArray != null)
        	{
        		value += Array.toHexString(this.subelementsArray);
        	}
    		var.add(value);
        }
        else if (params.length >= offset + 4 && (params[offset + 2].equalsIgnoreCase("field"))) 
        {
        	FieldAbstract field = this.fieldsByName.get(params[offset + 3]);
        	if (field != null)
        	{	
        		var.add(field.getValue(this.fieldsArray));
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

    public String toXml() 
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
    	if (fieldsArray != null)
        {
            elemString.append(" value=\"" + Array.toHexString(fieldsArray));
        }
        elemString.append("\">");
        
        elemString.append("\n");
        
		Iterator<FieldAbstract> iterField = this.fields.iterator();
		while (iterField.hasNext())
		{
			FieldAbstract field = (FieldAbstract) iterField.next();
            elemString.append(field.toXml(this.fieldsArray));
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

    public String toString() 
    {
    	return toXml();
    }
    public FieldAbstract getFieldsByName(String name) 
    {
        return fieldsByName.get(name);
    }

    public int getTag() 
    {
        return this.tag;
    }

	public String getLabel() {
		return this.label;
	}

}
