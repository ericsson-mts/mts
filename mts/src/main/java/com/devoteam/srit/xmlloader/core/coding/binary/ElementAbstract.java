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

import com.devoteam.srit.xmlloader.asn1.ASNToXMLConverter;
import com.devoteam.srit.xmlloader.asn1.data.ElementDigitV;
import com.devoteam.srit.xmlloader.asn1.data.ElementLengthV;
import com.devoteam.srit.xmlloader.asn1.data.ElementValue;
import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.coding.binary.coap.ElementCOAPOption;
import com.devoteam.srit.xmlloader.core.coding.binary.coap.ElementCOAPMessage;
import com.devoteam.srit.xmlloader.core.coding.binary.eap.ElementEAP;
import com.devoteam.srit.xmlloader.core.coding.binary.eap.ElementEAPLength;
import com.devoteam.srit.xmlloader.core.coding.binary.eap.ElementEAPLengthBit;
import com.devoteam.srit.xmlloader.core.coding.binary.q931.ElementQ931;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.gtp.data.ElementTL1V;
import com.devoteam.srit.xmlloader.gtp.data.ElementTLIV;
import com.devoteam.srit.xmlloader.gtp.data.ElementTLV;
import com.devoteam.srit.xmlloader.gtp.data.ElementTV;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.SupArray;

import java.math.BigInteger;
import java.nio.ByteBuffer;
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
	
	protected int tag = Integer.MIN_VALUE;

    protected String label;
    
    protected String description;

	protected int instances;

    protected LinkedHashMap<String, FieldAbstract> fieldsByName = new LinkedHashMap<String, FieldAbstract>();
    protected List<FieldAbstract> fields = new ArrayList<FieldAbstract>();
    
    protected List<ElementAbstract> elements = new ArrayList<ElementAbstract>();
    
    protected SupArray fieldsArray;
    
	protected SupArray subelementsArray;
	
	protected ElementAbstract parentElement;

	public ElementAbstract(ElementAbstract parent)
	{
		parentElement = parent;
	}
	
    public static ElementAbstract buildFactory(String coding, ElementAbstract parent) throws Exception
    {
    	ElementAbstract newElement = null;
    	if (coding == null)
		{
    		GlobalLogger.instance().logDeprecatedMessage("element identifier=\"...\"",
            "element identifier=\"...\" coding=\"Q931\"");
			newElement = new ElementQ931(parent);
		}    	
    	else if ("TLIV".equals(coding))
		{
			newElement = new ElementTLIV(parent);
		}
		else if ("TLV".equals(coding))
		{
			newElement = new ElementTLV(parent);
		}
		else if ("TL1V".equals(coding))
		{
			newElement = new ElementTL1V(parent);
		}
		else if ("TV".equals(coding))
		{
			newElement = new ElementTV(parent);
		} 
		else if ("Q931".equals(coding))
		{
			newElement = new ElementQ931(parent);
		}
		else if ("V".equals(coding))
		{
			newElement = new ElementValue(parent);
		}
		else if ("LV".equals(coding))
		{
			newElement = new ElementLengthV(parent);
		}
		else if ("DV".equals(coding))
		{
			newElement = new ElementDigitV(parent);
		}
		else if ("EAP".equals(coding))
		{
			newElement = new ElementEAP(parent);
		}
		else if ("EAPLength".equals(coding))
		{
			newElement = new ElementEAPLength(parent);
		}
		else if ("EAPLengthBit".equals(coding))
		{
			newElement = new ElementEAPLengthBit(parent);
		}		
		else if ("Message".equals(coding))
		{
			newElement = new ElementMessage(parent);
		}
		else if ("COAPOption".equals(coding))
		{
			newElement = new ElementCOAPOption(parent);
		}
		else if ("COAPOption".equals(coding))
		{
			newElement = new ElementCOAPOption(parent);
		}    	
		else if ("COAPMessage".equals(coding))
		{
			newElement = new ElementCOAPMessage(parent);
		}    	    	
		else
		{
     		throw new ExecutionException("ERROR : For the element the coding attribute \"" + coding + "\" value is unknown.");
		}
		newElement.coding = coding;
		return newElement;
    }
    
    public void parseFromXML(Element elementRoot, Dictionary dictionary, ElementAbstract elemDico, boolean parseDico) throws Exception 
    {
    	// for Q931 protocols only
        String tagStr = elementRoot.attributeValue("identifier");
        if (tagStr == null)
        {
        	tagStr = elementRoot.attributeValue("tag");
        }
        if (tagStr != null)
        {
    		tagStr = tagStr.trim();
        	int iPos = tagStr.indexOf(":");
        	String label = null;
        	String value = tagStr;
        	if (iPos >= 0)
        	{
        		label = tagStr.substring(0, iPos);
        		value = tagStr.substring(iPos + 1);
        	}
        	if (value != null)
        	{
        		Integer intTag = getTagValueFromBinary(value);
        		if (intTag !=  null)
        		{
        			this.tag = intTag;
        			this.label = label;
        		}
        		else
        		{
        			this.label = value;
        		}
        	}
        }
    	
    	// for Q931 protocols only
        String labelTag = elementRoot.attributeValue("name");
        if (labelTag != null)
        {
        	this.label = labelTag;
        }
        labelTag = elementRoot.attributeValue("label");
        if (labelTag != null)
        {
        	this.label = labelTag;
        }
        
        this.description = elementRoot.attributeValue("description");
        
        String instances = elementRoot.attributeValue("instances");
        if (instances != null)
        {
        	this.instances = Integer.parseInt(instances);
        }

        List<Element> listField = elementRoot.elements("field");
        for (Iterator<Element> it = listField.iterator(); it.hasNext();) 
        {
            Element fieldRoot = it.next();
            String name = fieldRoot.attributeValue("name");
            
            FieldAbstract fieldDico = null;
            // Case if field is present in the dico
            if (elemDico != null)
            {
            	fieldDico = elemDico.getFieldsByName(name);
            }
            
            FieldAbstract newField;
            String type = fieldRoot.attributeValue("type");
            // Case if "type" attribute is specified in the scenario
            if (type != null)
            {
                newField = FieldAbstract.buildFactory(fieldRoot);
                // Case if the element exits in the dictionary with the same "type" as in the scenario
                if (fieldDico != null && fieldDico.getClass().equals(newField.getClass()))
                {
                	newField = fieldDico.clone();
                }            	
            }
            // Case if "type" attribute is not specified by the user 
            else
            {
            	newField = fieldDico;
            }
            if (newField == null)
            {
            	newField = FieldAbstract.buildFactory(fieldRoot);
            }
            newField.parseFromXML(fieldRoot, parseDico);
            this.fieldsByName.put(name, newField);
            this.fields.add(newField);
        }
        
        // initiate the Array containing the fields
        SupArray tempArray = new SupArray();
        
        int length = getLengthElem() / 8;
        Array emptyArray = new DefaultArray(length);
        tempArray.addFirst(emptyArray);
       	this.fieldsArray = tempArray;
       	
       	this.subelementsArray = new SupArray();
       	
        // set the value for each fields
        listField = elementRoot.elements("field");
        int offset = 0;
        for (Iterator<Element> it = listField.iterator(); it.hasNext();) 
        {
            Element element1 = it.next();
            String fieldName = element1.attributeValue("name");
            FieldAbstract field = this.fieldsByName.get(fieldName);
            if (field != null) 
            {
            	String value = element1.attributeValue("value");
            	if (value == null)
            	{
            		value = element1.elementText("value");
            	}
            	if (value != null)
            	{
                    // replace escape XML character
            		value = Utils.unescapeXMLEntities(value);
			        field.setValue(value, offset, this.fieldsArray);
            	}
            	else
            	{
            		field.setOffset(offset);
            	}
		        length = field.getLength();
		        /// TODO revoir ce truc bizzaroide
		        if (length > 0)
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
        	if (dictionary != null)
        	{
        		subElemDico = dictionary.getElementFromXML(elemElement);
        		if (subElemDico != null)
        		{
        			elem = (ElementAbstract) subElemDico.cloneAttribute();
        		}
        		//elem = ElementAbstract.buildFactory(subElemDico.coding);
        		//elem.copyToClone(subElemDico);
        	}
        	//else
        	String coding = elemElement.attributeValue("coding");
    		if (coding !=  null)
        	{
        		elem = ElementAbstract.buildFactory(coding, this);
        		if (subElemDico == null)
        		{
        			elem = (ElementAbstract) subElemDico.cloneAttribute();
        		}
        	}
        	elem.parseFromXML(elemElement, dictionary, subElemDico, parseDico);
        	this.elements.add(elem);
        	// case when we parse the dictionary itself
        	if (parseDico && dictionary !=  null)
        	{
        		dictionary.addElement(elem);
        	}
        }
        
    }

    public void initValue(int index, Dictionary dictionary) throws Exception 
    {
    	List<ElementAbstract> newElements = new ArrayList<ElementAbstract>();
        for (Iterator<ElementAbstract> it = elements.iterator(); it.hasNext();) 
        {
			ElementAbstract elemInfo = (ElementAbstract) it.next();
			ElementAbstract elemDico = dictionary.getElementByLabel(elemInfo.getLabel());
			elemInfo = ElementAbstract.buildFactory(elemInfo.coding, this);
			if (elemDico != null)
			{
				elemInfo.copyToClone(elemDico);
			}
	        elemInfo.initValue(index, dictionary);
	        newElements.add(elemInfo);
        }
        this.elements = newElements;
        
        // initiate the Array containing the fields
        SupArray tempArray = new SupArray();
        
        int length = getLengthElem() / 8;
        Array emptyArray = new DefaultArray(length);
        tempArray.addFirst(emptyArray);
       	this.fieldsArray = tempArray;
       	
       	this.subelementsArray = new SupArray();
       	
        // set the value for each fields
        int offset = 0;
        for (Iterator<FieldAbstract> it = fields.iterator(); it.hasNext();) 
        {
            FieldAbstract field = it.next();
             field.initValue(index, offset, this.fieldsArray);
	        length = field.getLength();
	        /// TODO revoir ce truc bizzaroide
	        if (length > 0)
	        {
		        offset += length;
	        }
	        else
	        {
		        offset = this.fieldsArray.length * 8;
	        }
        }
                
    }

    /**
     * get FieldAbsctract for generic element/field structure
     * with a description that contains a given keyword (between [] character)
     * @return
     */
    public FieldAbstract getFieldByDescriptionKeyword(String keyword) 
    {
    	keyword = keyword.toLowerCase();
    	if (elements.size() > 0)
    	{
    		return elements.get(0).getFieldByDescriptionKeyword(keyword);
    	}
		Iterator<FieldAbstract> iterF = this.fields.iterator();
		while (iterF.hasNext())
		{
			FieldAbstract field = (FieldAbstract) iterF.next();
			if (field.description !=  null && field.description.toLowerCase().contains("[" + keyword + "]"))
			{
				return field;
			}            
        }
        return null;
    }

    public int getLengthElem() 
    {
        int length = 0;
		Iterator<FieldAbstract> iterF = this.fields.iterator();
		while (iterF.hasNext())
		{
			FieldAbstract field = (FieldAbstract) iterF.next();
			int fieldLength = field.getLength();
            length += fieldLength;
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
    		// case the tab starts with 's|S' character
	    	if (tag.charAt(0) == 's' || tag.charAt(0) == 'S')
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
	    	// convert to string
	    	String idStr = Utils.toHexaString(idBytes, 0, -1, "");
	    	// convert to unsigned int
	    	Long idLong = Long.parseLong(idStr, 16);
	        return (int) (idLong & 0xffffffff);
    	}
    	return null;
    }

    /*
     * Decode the sub-element for element starting with the tag (ElementT* : tag encoded sur 1 octet)
     */
	public static List<ElementAbstract> decodeTag1OctetElementsFromArray(Array data, Dictionary dictionary) throws Exception 
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

	        ElementAbstract elemNew = (ElementAbstract) elemDico.clone();	
	        int length = elemNew.decodeFromArray(data.subArray(offset), dictionary);
	        offset += length;
	        
	        elements.add(elemNew);
	    }
	    return elements;
	
	}

    /*
     * Decode the sub-element for element starting with the tag defined in the dictionary
     */
	public static List<ElementAbstract> decodeTagElementsIntoDicoFromArray(Array data, Dictionary dictionary) throws Exception 
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

	        ElementAbstract elemNew = (ElementAbstract) elemDico.clone();	
	        int length = elemNew.decodeFromArray(data.subArray(offset), dictionary);
	        offset += length;
	        
	        elements.add(elemNew);
	    }
	    return elements;
	
	}

	/*
     * Decode the fields and sub-element for elements starting with the tag (ElementT*)
     */
	public void decodeFieldsTagElementsFromArray(Array data, Dictionary dictionary) throws Exception 
	{
	    // cas when there are some sub fields
	    if (!this.fieldsByName.isEmpty())
	    {
	        this.fieldsArray = new SupArray();
	        this.fieldsArray.addFirst(data);
	    }
	    // cas when there are some sub elements
	    else if (!this.elements.isEmpty())
	    {
	        this.subelementsArray = new SupArray();
	    	this.subelementsArray.addFirst(data);
	    	this.elements = ElementAbstract.decodeTag1OctetElementsFromArray(this.subelementsArray, dictionary);
	    }
	}

	/*
     * Decode the fields and sub-element for elements not starting with the tag
     */
    public int decodeFieldsNotTagElementsFromArray(Array array, Dictionary dictionary) throws Exception
	{
		if (!this.elements.isEmpty())
		{
			int length = this.decodeNotTagElementsFromArray(array, dictionary);
			
			this.subelementsArray = new SupArray();
	        this.subelementsArray.addFirst(array.subArray(0, length));
	        return length;
		}
		if (!this.fieldsByName.isEmpty())
		{
			FieldAbstract lastField = this.fields.get(this.fields.size() - 1);
			// cas ou le dernier field est statique donc longueur non nulle  
			if (lastField.getLength() > 0)
			{
		        this.fieldsArray = new SupArray();
		        int elementLength = getLengthElem() / 8;
		        //cas il n'y a pas suffisamment de données pour remplir tous les fields
		        if (elementLength > array.length)
		        {
		        	elementLength = array.length; 
		        }
		        Array subArray = array.subArray(0, elementLength);
		        this.fieldsArray.addFirst(subArray);
		        return elementLength;
			}
			else
			{
				int length = array.length;
		        this.fieldsArray = new SupArray();
		        Array subArray = array.subArray(0, length);
		        this.fieldsArray.addFirst(subArray);
		        return length;				
			}
		}
		return 0;
	}
    
    public int decodeNotTagElementsFromArray(Array array, Dictionary dictionary) throws Exception
    {
		// encode the sub-element
		Iterator<ElementAbstract> iter = this.elements.iterator();
		int index = 0;
		List<ElementAbstract> newElements = new ArrayList<ElementAbstract>();
		while (iter.hasNext())
		{
			ElementAbstract elemInfo = (ElementAbstract) iter.next();
			ElementAbstract elemDico = dictionary.getElementByLabel(elemInfo.getLabel());
			//ElementAbstract elemDico = dictionary.getElementByTag(elemInfo.getTag());
			elemInfo = ElementAbstract.buildFactory(elemInfo.coding, this);
			if (elemDico != null)
			{
				elemInfo.copyToClone(elemDico);
			}
			int length = elemInfo.getLengthElem() / 8;
			// S'il n'y a plus de data à décoder ou si l'element est dynamique (longueur nulle) 
			// (cas d'un element dynamique ex MAP TP-DA element)
			if (index < array.length || length == 0)
			{
				length = array.length - index;			
				Array subArray = array.subArray(index, length);
				index += elemInfo.decodeFromArray(subArray, dictionary);
				newElements.add(elemInfo);
			}
		}
		this.elements = newElements;
		return index;
    }
    
    public abstract int decodeFromArray(Array array, Dictionary dictionary) throws Exception;
    
    public SupArray encodeToArray(Dictionary dictionary) throws Exception
    {
    	SupArray sup = new SupArray();
		// encode the sub-element
		Iterator<ElementAbstract> iter = this.elements.iterator();
		while (iter.hasNext())
		{
			ElementAbstract elemInfo = (ElementAbstract) iter.next();
			Array array = elemInfo.encodeToArray(dictionary);
			sup.addLast(array);
		}
		return sup;
    }
    
    public String getFieldValue(String fieldName) throws Exception
    {
    	FieldAbstract field = this.getFieldsByName(fieldName);
    	if (field != null && field.offset / 8 < this.fieldsArray.length)
    	{
    		return field.getValue(this.fieldsArray);
    	}
		return null;
    }
    
    public ElementAbstract cloneAttribute() throws Exception
    {
		ElementAbstract newElement = buildFactory(this.coding, this.parentElement);
		
		newElement.coding = this.coding;
		newElement.tag = this.tag;
		newElement.label = this.label;
		newElement.instances = this.instances;
		
    	return newElement;
    }
    
    public void copyToClone(ElementAbstract source) throws Exception
    {
    	this.tag = source.tag;
    	this.label = source.label;
    	this.fieldsArray = null;
    	this.subelementsArray = null;
		// encode the sub-element
		Iterator<ElementAbstract> iter = source.elements.iterator();
		while (iter.hasNext())
		{
			ElementAbstract elemOld = (ElementAbstract) iter.next();
			ElementAbstract elemNew = (ElementAbstract) elemOld.clone();
			this.elements.add(elemNew);
		}
		Iterator<FieldAbstract> iterField = source.fields.iterator();
		while (iterField.hasNext())
		{
			FieldAbstract field = (FieldAbstract) iterField.next();
			this.fieldsByName.put(field.name, field);
			this.fields.add(field);
		}
    }
    
    public void getParameter(Parameter var, String[] params, String path, int offset, Dictionary dictionary) throws Exception 
    {
    	if (params.length == offset + 2) 
        {
    		Array array = this.encodeToArray(dictionary);
    		var.add(Array.toHexString(array));
        }
    	/*
        else if (params.length == offset + 4 && (params[offset + 2].equalsIgnoreCase("field"))) 
        {
        	FieldAbstract field = this.fieldsByName.get(params[offset + 3]);
        	if (field != null)
        	{	
                String strVal = null;
                try
                {
                	strVal = field.getValue(this.fieldsArray);
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
        }
        */
        else if (params.length >= offset + 3 && (params[offset + 2].equalsIgnoreCase("field"))) 
        {
        	FieldAbstract field = this.fieldsByName.get(params[offset + 3]);
        	if (field != null)
        	{	
        		field.getParameter(var, params, path, offset + 4, dictionary, this.fieldsArray);
        	}
        }    	
        else 
        {
        	List<ElementAbstract> list = ElementAbstract.getElementsFromTag(this.elements, params[offset + 2], dictionary);
        	 Iterator<ElementAbstract> iter = list.iterator();
 		    while (iter.hasNext())
 		    {
 		    	ElementAbstract elem = (ElementAbstract) iter.next();
 		    	elem.getParameter(var, params, path, offset + 1, dictionary);
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
		
		List<ElementAbstract> list = new ArrayList<ElementAbstract>();		
    	ElementAbstract elementAbstr = dictionary.getElementFromTag(tagStr);
    	if (elementAbstr !=  null)
    	{
			Integer tagInt = dictionary.getElementFromTag(tagStr).getTag();
			
		    Iterator<ElementAbstract> iter = elements.iterator();
		    while (iter.hasNext())
		    {
		    	ElementAbstract elem = (ElementAbstract) iter.next();
		        if ((tagInt == elem.getTag() || tag.equalsIgnoreCase(elem.getLabel())) && (instances == null || instances.equals(elem.instances)))
		        {
		        	list.add(elem);
		        }
		    }
    	}
	    return list;
	}

    public String toXml(int indent) 
    {
        StringBuilder elemString = new StringBuilder();
        elemString.append(ASNToXMLConverter.indent(indent));
        elemString.append("<element ");
        if (this.tag != Integer.MIN_VALUE)
        {
        	if (!(this instanceof ElementQ931))
        	{
        		elemString.append("tag=\"");
        	}
        	else
        	{
        		elemString.append("identifier=\"");
        	}
	    	if (this.label != null)
	    	{
	    		elemString.append(this.label + ":");
	    	}
	    	elemString.append(this.tag);
	    	elemString.append("\"");
        }
        else
        {
	        elemString.append("label=\"");
	    	elemString.append(this.label);
	    	elemString.append("\"");        	
        }
        if (this.instances != 0)
        {
	    	elemString.append(" instances=\"");
	   		elemString.append(this.instances);
	    	elemString.append("\"");
        }
    	elemString.append(" coding=\"");
   		elemString.append(this.coding);
    	elemString.append("\"");
    	if (fieldsArray != null && (fields.size() > 1 || 
    	   (fields.size() == 1 && !(fields.get(0) instanceof BinaryField))))
        {
    		elemString.append(" binary=\"");
            elemString.append(Array.toHexString(fieldsArray));
            elemString.append("\"");
        }
    	elemString.append(">");
        
        elemString.append(fieldsElementsToXml(indent + ASNToXMLConverter.NUMBER_SPACE_TABULATION));
        
        elemString.append(ASNToXMLConverter.indent(indent));
        elemString.append("</element>\n");
        
        return elemString.toString();
    }

    public String fieldsElementsToXml(int indent) 
    {
        StringBuilder elemString = new StringBuilder();
        elemString.append("\n");
		Iterator<FieldAbstract> iterField = this.fields.iterator();
		while (iterField.hasNext())
		{
			FieldAbstract field = (FieldAbstract) iterField.next();
            elemString.append(field.toXml(this.fieldsArray, indent));
        }
        
        Iterator<ElementAbstract> iterElem = this.elements.iterator();
		while (iterElem.hasNext())
		{
			ElementAbstract elemInfo = (ElementAbstract) iterElem.next();
            elemString.append(elemInfo.toXml(indent));
        }
        
        return elemString.toString();
    }

    public String toString() 
    {
    	return toXml(4);
    }
    public FieldAbstract getFieldsByName(String name) 
    {
        return fieldsByName.get(name);
    }

    public FieldAbstract getField(int index) 
    {
    	if (index < this.fields.size())
    	{
    		return fields.get(index);
    	}
        return null;
    }
    
    public ElementAbstract getElement(int index) 
    {
    	if (index < this.elements.size())
    	{
    		return elements.get(index);
    	}
        return null;
    }
    
    public String getCoding() 
    {
		return coding;
	}
    
    public int getTag() 
    {
        return this.tag;
    }

	public String getLabel() {
		return this.label;
	}

	// do not use experimental for development
	public void setLabel(String label) {
		this.label = label;
	}
	
	// do not use experimental for development
	public void addField(FieldAbstract field) {
		fields.add(field);
		fieldsByName.put(label, field);
	}

    public SupArray getFieldsArray() 
    {
		return fieldsArray;
	}
    
    public boolean isEmpty() 
    {
		return fields.isEmpty() && elements.isEmpty();
	}
    

}
