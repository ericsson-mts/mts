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

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.coding.binary.q931.MessageQ931;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;


/**
 *
 * @author indiaye
 */
public class Dictionary 
{
	
	public static HashMap<String, Dictionary> dictionaries;
	static 
	{
		dictionaries = new  HashMap<String, Dictionary>();
	}
	
    private String _layer;
    
    // data for generic element/field structure
    private ElementAbstract elementForMessage = null;
    private String fieldNameForLength = null;
    private String fieldNameForType = null;
	
    private Map<String,ElementAbstract> elementsMapByLabel = new HashMap<String, ElementAbstract>();
    private Map<Integer,ElementAbstract> elementsMapByTag = new HashMap<Integer, ElementAbstract>();
    private Map<String,FieldAbstract> fieldsMapHeader = new HashMap<String, FieldAbstract>();
    
    public Dictionary() 
    { 
    }
    
    public static Dictionary getInstance(String file) throws Exception
    {
    	if (dictionaries == null)
    	{
    		dictionaries = new  HashMap<String, Dictionary>();
    	}
    	Dictionary instance = null;
    	synchronized (dictionaries)
    	{
	    	instance = dictionaries.get(file);
	    	if (instance == null)
	    	{
	    		instance = new Dictionary(file);
	    		dictionaries.put(file, instance);
	    	}
    	}
    	return instance;
    }
    
    public static void reset()
    {
    	if (dictionaries !=  null)
    	{
	    	dictionaries.clear();
	    	dictionaries = null;
    	}
    }
    
	public Dictionary(String file) throws Exception 
    {
    	this();    	
		XMLDoc xml = new XMLDoc();
		String path = "../conf/" + file;
	    xml.setXMLFile(new URI(path));
	    xml.parse();
	    Element rootDico = xml.getDocument().getRootElement();
	    parseFromXML(rootDico);
    }
    
    private void parseFromXML(Element rootDico) throws Exception
    {     
        _layer = rootDico.attributeValue("layer");
        
        Element headerElement = rootDico.element("header");
        if (headerElement != null)
        {
        	List<Element> listFieldsElement = headerElement.elements("field"); 
	        for (Element element : listFieldsElement) 
	        {
	        	FieldAbstract fieldHeader = FieldAbstract.buildFactory(element);
	        	fieldHeader.parseFromXML(element, true);
	        	fieldsMapHeader.put(element.attributeValue("name"), fieldHeader);
	        }
        }
        
        List<Element> list = rootDico.elements("element");
        int i = 0;
        for (Element elem : list) 
        {
        	String coding = elem.attributeValue("coding");
        	ElementAbstract elemInfo = ElementAbstract.buildFactory(coding, null);
            elemInfo.parseFromXML(elem, this, null, true);
            
            addElement(elemInfo);
                        
            if (i == 0)
            {
            	// data for generic element/field structure
            	this.elementForMessage = elemInfo;
            	FieldAbstract fieldForLength = elemInfo.getFieldByDescriptionKeyword("length");
            	if (fieldForLength != null)
            	{
            		this.fieldNameForLength = fieldForLength.name;
            	}
            	FieldAbstract fieldForType = elemInfo.getFieldByDescriptionKeyword("type");
            	if (fieldForType != null)
            	{
            		this.fieldNameForType = fieldForType.name;
            	}
            }            
            i++;
        }
    }
    
    public String get_layer() {
		return this._layer;
	}

    public ElementAbstract getElementByTag(Integer tag) 
    {
        return elementsMapByTag.get(tag);
    }

    public ElementAbstract getElementByLabel(String label) 
    {
        return elementsMapByLabel.get(label);
    }

    public void addElement(ElementAbstract elemInfo) 
    {
        String eltLabel = elemInfo.getLabel();
        if (eltLabel != null)
        {
			String[] initElt = eltLabel.split(",");
			for (int i=0; i < initElt.length; i++)
			{
				// cas pour l'element est défini plusieurs fois 
				// on l'ajoute s'il n'existe pas ou s'il n'est pas vide
				if (!elementsMapByLabel.containsKey(initElt[i]) || !elemInfo.isEmpty())
				{
					elementsMapByLabel.put(initElt[i], elemInfo);
				}
			}
			// cas pour l'element est défini plusieurs fois			
			// on l'ajoute s'il n'existe pas ou s'il n'est pas vide
			if (!elementsMapByTag.containsKey(elemInfo.getTag()) || !elemInfo.isEmpty())
			{
				elementsMapByTag.put(elemInfo.getTag(), elemInfo);
			}
        }
    }
    
    public FieldAbstract getHeaderFieldByName(String name) 
    {
        return fieldsMapHeader.get(name);
    }

    public ElementAbstract getElementFromXML(Element elementRoot) throws Exception
    {
    	// for Q931 protocols only
        String tag = elementRoot.attributeValue("identifier");
        if (tag == null)
        {
        	tag = elementRoot.attributeValue("tag");
        }
        if (tag != null)
        {    
        	ElementAbstract elemDico = getElementFromTag(tag);
	        // the element is present in the dictionary
	        if (elemDico != null)
	        {
	        	return elemDico;
	        }
        }

        // for Q931 protocols only
        String label = elementRoot.attributeValue("name");
        if (label == null)
        {
        	label = elementRoot.attributeValue("label");
        }
        if (label != null)
        {
        	ElementAbstract elemDico = getElementByLabel(label);
	        // the element is present in the dictionary
	        if (elemDico != null)
	        {
	        	return elemDico;
	        }
        }
        
        // the element is not present in the dictionary
        String coding = elementRoot.attributeValue("coding");
        ElementAbstract newElement = ElementAbstract.buildFactory(coding, null);
        return newElement;
    }
    
	/**
	 * get an element from the dictionary from a given tag 
	 * with the form of "LABEL:VALUE"
	 * if not found return an empty GTPV2 element containing the tag and label 
	 * @param tag
	 * @param dictionary
	 * @return
	 * @throws Exception
	 */
	public ElementAbstract getElementFromTag(String tag) throws Exception
    {	
		tag = tag.trim();
    	int iPos = tag.indexOf(":");
    	String label = tag;
    	String value = tag;
    	if (iPos >= 0)
    	{
    		label = tag.substring(0, iPos);
    		value = tag.substring(iPos + 1);
    	}
    	// case if the tag is set by the value as a binary vale (with 'b' 's' 'd' prefix)
    	Integer valueInt = ElementAbstract.getTagValueFromBinary(value);
    	// check the consistency between value and label
    	ElementAbstract elemById = getElementByTag(valueInt);
		if (elemById != null && !label.equalsIgnoreCase(elemById.getLabel()) && !label.equals(tag))
		{
			GlobalLogger.instance().getApplicationLogger().warn(Topic.PROTOCOL, "The element label \"" + label + "\" does not match the tag/id \"" + value + "\" in the dictionary.");
		}
		// return first by the tag name
		ElementAbstract elemByName = getElementByLabel(label);
    	if (elemByName != null)
    	{
    		return elemByName;
    	}
    	// return first by the tag value
    	if (elemById != null)
    	{
    		return elemById;
    	}
    	return null;
    }


	public ElementAbstract getElementForMessage() 
	{
		return elementForMessage;
	}

	public String getFieldNameForLength() 
	{
		return fieldNameForLength;
	}

	public String getFieldNameForType() 
	{
		return fieldNameForType;
	}

}
