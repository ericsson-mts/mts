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
import java.util.HashMap;
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
    
    private LinkedHashMap<Integer, ElementAbstract> hashElements;

	public MessageQ931(Element root) throws Exception 
    {
        this.syntax = root.attributeValue("syntax");
        initDictionary(syntax);       
        
        this.header = new HeaderQ931();
        this.header.parseFromXML(root.element("header"), dictionary);
        
        this.hashElements = new LinkedHashMap<Integer, ElementAbstract>();
        
        List<Element> elementsInf = root.elements("element");
        ElementQ931 elemInfo = null;
        for (Element element : elementsInf) 
        {
            elemInfo = new ElementQ931();
            // FH Manage a new Element like ElementQ931big for id = User-User:126
            elemInfo.parseFromXML(element, dictionaries.get(this.syntax));
            
            this.hashElements.put(elemInfo.getId(), elemInfo);
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
        
        hashElements = new LinkedHashMap<Integer, ElementAbstract>();
        int offset = header.getLength();
        while (offset < data.length) 
        {
            int id = new Integer08Array(data.subArray(offset, 1)).getValue();         
            ElementAbstract elemInfo = dictionaries.get(syntax).getMapElementById().get(id);
        
            elemInfo.decodeFromArray(data.subArray(offset));
            offset += elemInfo.encodeToArray().length;
            
            hashElements.put(id, elemInfo);
        }

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
        		ElementAbstract elem = dictionaries.get(this.syntax).getMapElementByName().get(params[2]);
        		if (elem != null)
        		{
        			id = elem.getId();
        		}
        	}        	
        	ElementAbstract elem = hashElements.get(id);
        	if (elem != null)
        	{
        		elem.getParameter(var, params, path, 1);
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
        for (Entry<Integer, ElementAbstract> entry : hashElements.entrySet()) {
            array.addLast(entry.getValue().encodeToArray());
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
        messageToString.append(header.toString());

        for (Entry<Integer, ElementAbstract> entry : hashElements.entrySet()) {

            messageToString.append(entry.getValue().toString());
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
        for (Entry<Integer, ElementAbstract> entry : hashElements.entrySet()) {

            msglength += entry.getValue().encodeToArray().length;

        }
        return msglength;
    }

    public ElementAbstract getElementQ931(int id) 
    {
		return hashElements.get(id);
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