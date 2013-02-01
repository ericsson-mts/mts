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

package com.devoteam.srit.xmlloader.core.coding.binary.q931;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.coding.binary.Dictionary;
import com.devoteam.srit.xmlloader.core.coding.binary.ElementAbstract;
import com.devoteam.srit.xmlloader.core.coding.binary.HeaderAbstract;
import com.devoteam.srit.xmlloader.core.coding.binary.XMLDoc;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import gp.utils.arrays.Array;
import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.SupArray;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import org.dom4j.Element;


/**
 *
 * @author indiaye
 */
public class MessageQ931 
{
   
    public static HashMap<String, Dictionary> dictionaries = new  HashMap<String, Dictionary>();
	
	private String syntax;
	
    private Dictionary dictionary;
    	
    private HeaderAbstract header;
    
    private List<ElementAbstract> elements;

	public MessageQ931(Element root) throws Exception 
    {
        this.syntax = root.attributeValue("syntax");
        initDictionary(syntax);       
        
        this.header = new HeaderQ931();
        this.header.parseFromXML(root.element("header"), dictionary);
        
        this.elements = new ArrayList<ElementAbstract>();
        
        List<Element> elementsInf = root.elements("element");
        ElementAbstract elemInfo = null;
        ElementAbstract elem = null;
        for (Element element : elementsInf) 
        {
        	element.addAttribute("coding", "Q931");
            elemInfo = this.dictionary.getElementFromXML(element);
	        elem = (ElementQ931) elemInfo.cloneAttribute();
            // FH Manage a new Element like ElementQ931big for id = User-User:126
            elem.parseFromXML(element, this.dictionary, elemInfo);
            
            this.elements.add(elem);
        }
    }

    public MessageQ931(Array data, String syntax) throws Exception 
    {
    	this.syntax = syntax;
        initDictionary(syntax);
        

        if (syntax.contains("q931") || (syntax.contains("v5x")))
        {
	        this.header = new HeaderQ931();
        }

        this.header.decodeFromArray(data, syntax, dictionary);

        // does not work like with GTP why ?
        this.elements = ElementAbstract.decodeElementsFromArray(data.subArray(header.getLength()), this.dictionary);
    }

    /** Get a parameter from the message */
    public void getParameter(Parameter var, String[] params, String path) throws Exception 
    {
        if (params.length > 2 && params[1].equalsIgnoreCase("header")) 
        {
            this.header.getParameter(var, params[2]);
        }
        else if ((params[1].equalsIgnoreCase("element") && params.length > 2)) 
        {
        	int id = 0;
        	try 
        	{
                byte[] idBytes = Utils.parseBinaryString(params[2]);
                if (idBytes.length > 1)
                {
                	throw new ExecutionException("Reading the element Id for path keyword : value is too long " + params[2]);
                }                
                id = idBytes[0] & 0xff;
        	}
        	catch (Exception e) 
        	{
        		ElementAbstract elem = this.dictionary.getElementByLabel(params[2]);
        		if (elem != null)
        		{
        			id = elem.getTag();
        		}
        	}        	
	    	List<ElementAbstract> list = ElementAbstract.getElementsFromTag(this.elements, params[2], dictionary);
		    Iterator<ElementAbstract> iter = list.iterator();
		    while (iter.hasNext())
		    {
		    	ElementAbstract elem = (ElementAbstract) iter.next();
	    		elem.getParameter(var, params, path, 1, dictionary);
	    	}
        }
        else
        {
           	Parameter.throwBadPathKeywordException(path);
        }
    }

    public Array getValue() 
    {
        SupArray array = new SupArray();
        array.addLast(header.encodeToArray());
	    Iterator<ElementAbstract> iter = this.elements.iterator();
	    while (iter.hasNext())
	    {
	    	ElementAbstract elem = (ElementAbstract) iter.next();
            array.addLast(elem.encodeToArray());
        }
        return array;
    }

    public String getType() 
    {
	    return header.getType();
    }
    
    @Override
    public String toString() 
    {
        StringBuilder messageToString = new StringBuilder();
        messageToString.append("<ISDN>");
        messageToString.append(header.toXml());

	    Iterator<ElementAbstract> iter = this.elements.iterator();
	    while (iter.hasNext())
	    {
	    	ElementAbstract elem = (ElementAbstract) iter.next();
            messageToString.append(elem.toXml());
        }
        messageToString.append("</ISDN>");
        return messageToString.toString();

    }

    public HeaderAbstract getHeader() 
    {
        return header;
    }

    public int getLength() 
    {

        int msglength = 0;
        msglength = header.encodeToArray().length;
	    Iterator<ElementAbstract> iter = this.elements.iterator();
	    while (iter.hasNext())
	    {
	    	ElementAbstract elem = (ElementAbstract) iter.next();
            msglength += elem.encodeToArray().length;

        }
        return msglength;
    }

	public List<ElementAbstract> getElementsFromTag(String tag) throws Exception 
	{
	    return ElementAbstract.getElementsFromTag(this.elements, tag, this.dictionary);
	}
	
    public void initDictionary(String syntax) throws Exception 
    {
    	this.dictionary = MessageQ931.dictionaries.get(syntax);
    	if (this.dictionary == null)
    	{
	        XMLDoc xml = new XMLDoc();
	        xml.setXMLFile(new URI(syntax));
	        xml.parse();
	        Element rootDico = xml.getDocument().getRootElement();
	        this.dictionary = new Dictionary(rootDico, "Q931");
	        MessageQ931.dictionaries.put(syntax, dictionary);
    	}
    }

}