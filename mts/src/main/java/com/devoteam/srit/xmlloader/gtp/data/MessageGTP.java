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

package com.devoteam.srit.xmlloader.gtp.data;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.coding.binary.Dictionary;
import com.devoteam.srit.xmlloader.core.coding.binary.ElementAbstract;
import com.devoteam.srit.xmlloader.core.coding.binary.EnumerationField;
import com.devoteam.srit.xmlloader.core.coding.binary.HeaderAbstract;
import com.devoteam.srit.xmlloader.core.coding.binary.XMLDoc;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.SupArray;

import java.io.InputStream;
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
 * @author Fabien Henry
 */
public class MessageGTP 
{
   
	public static HashMap<String, Dictionary> dictionaries = new  HashMap<String, Dictionary>();
	
	private String syntax;
	
	private Dictionary dictionary;
		
	private HeaderAbstract header;
	
	private List<ElementAbstract> elements;
	
	public MessageGTP()
	{
		
	}
   
	public MessageGTP(Element root) throws Exception 
	{              
	    Element elementHeader;
	    elementHeader = root.element("headerPrime");
	    if (elementHeader != null)
	    {
	        this.header = new HeaderGTPPrime();
	        this.syntax = header.getSyntax();
	        initDictionary(this.syntax); 
	        this.header.parseFromXML(elementHeader, dictionary);
	    }
	    elementHeader = root.element("headerV1");
	    if (elementHeader!= null)
	    {
	        this.header = new HeaderGTPV1();
	        this.syntax = header.getSyntax();
	        initDictionary(this.syntax); 
	        this.header.parseFromXML(elementHeader, dictionary);
	    }
	    elementHeader = root.element("headerV2");
	    if (elementHeader!= null)
	    {
	        this.header = new HeaderGTPV2();
	        this.syntax = header.getSyntax();
	        initDictionary(this.syntax); 
	        this.header.parseFromXML(elementHeader, dictionary);
	    }        
	    
	    this.elements = new ArrayList<ElementAbstract>();
	    List<Element> elementsInf = root.elements("element");
	    ElementAbstract elemInfo = null;
	    for (Element element : elementsInf) 
	    {
	        elemInfo = ElementAbstract.buildFactory(element);
	        elemInfo.parseFromXML(element, dictionaries.get(this.syntax));
	        
	        this.elements.add(elemInfo);
	
	    }
	}

    /** Return true if the message is a request else return false*/
    public boolean isRequest()
    {
        return this.header.isRequest();
    }
	
    /** Get the type of this message */
	public String getType() {
	    return header.getType();
	}

    /** Get the result of this answer (null if request) */
    public String getResult()
    {
    	return "result";
        // return message.getHeader().getResult();
    }
	    
	public void decodeFromStream(InputStream inputStream) throws Exception
	{
		byte[] begin = new byte[4];
		//read the header
		int nbCharRead= inputStream.read(begin, 0, 4);
		if(nbCharRead == -1){
			throw new Exception("End of stream detected");
		}
		else if (nbCharRead < 1) {
			throw new Exception("Not enough char read");
		}
		 
		SupArray array = new SupArray();
		array.addFirst(new DefaultArray(begin));
		
		int version = array.getBits(0, 3);
		// int messageType = beginArray.getBits(8, 8);
		if (version == 0)
		{
			 this.header = new HeaderGTPPrime(array);
		}
		else if (version == 1)
		{
			 this.header = new HeaderGTPV1(array);
		}
		else if (version == 2)
		{
			 this.header = new HeaderGTPV2(array);
		}
		 
		this.syntax = this.header.getSyntax();
		initDictionary(this.syntax);
		
		byte[] fieldBuffer = new byte[this.header.getLength()];
		int nbCharToRead = this.header.getLength();
		//read the staying message's data
		nbCharRead = inputStream.read(fieldBuffer, 0, nbCharToRead);
		if(nbCharRead == -1)
			 throw new Exception("End of stream detected");
		else if(nbCharRead < nbCharToRead)
		    throw new Exception("Not enough char read");
		
		array.addLast(new DefaultArray(fieldBuffer));
		int offset = this.header.decodeFromArray((Array) array, "", dictionary);
		int fieldLength = this.header.getLength() - offset + 4;
		
		Array fieldArray = new DefaultArray(0);
		if (fieldLength > 0)
		{
			fieldArray = array.subArray(offset, fieldLength);
		}
		this.elements = ElementAbstract.decodeElementsFromArray(fieldArray, this.dictionary);
	}

	public void decodeFromBytes(byte[] data) throws Exception
	{
		Array array = new DefaultArray(data);
		 
		int version = array.getBits(0, 3);
		// int messageType = array.getBits(8, 8);		
		 
		if (version == 0)
		{
			 this.header = new HeaderGTPPrime(array);
		}
		else if (version == 1)
		{
			 this.header = new HeaderGTPV1(array);
		}
		else if (version == 2)
		{
			 this.header = new HeaderGTPV2(array);
		}
		 
		this.syntax = this.header.getSyntax();
		initDictionary(this.syntax);
		
		int offset = this.header.decodeFromArray(array, "", dictionary);
		int fieldLength = this.header.getLength() - offset + 4; 		
		
		Array fieldArray = new DefaultArray(0);
		if (fieldLength > 0)
		{
			fieldArray = array.subArray(offset, fieldLength);
		}
		this.elements = ElementAbstract.decodeElementsFromArray(fieldArray, this.dictionary);
	}

	/** Get a parameter from the message */
	public void getParameter(Parameter var, String[] params, String path) throws Exception 
	{
	    if (params.length >= 2 && params[0].equalsIgnoreCase("header")) 
	    {
	        this.header.getParameter(var, params[1]);
	    }
	    else if ((params[0].equalsIgnoreCase("element") && params.length >= 2)) 
	    {
	    	int id = 0;
	    	try 
	    	{
	            byte[] idBytes = Utils.parseBinaryString(params[1]);
	            if (idBytes.length > 1)
	            {
	            	throw new ExecutionException("Reading the element Id for path keyword : value is too long " + params[1]);
	            }                
	            id = idBytes[0] & 0xff;
	    	}
	    	catch (Exception e) 
	    	{
	    		ElementAbstract elem = dictionaries.get(this.syntax).getMapElementByName().get(params[1]);
	    		if (elem != null)
	    		{
	    			id = elem.getId();
	    		}
	    	}        	
	    	List<ElementAbstract> list = this.getElement(id);
		    Iterator<ElementAbstract> iter = list.iterator();
		    while (iter.hasNext())
		    {
		    	ElementAbstract elem = (ElementAbstract) iter.next();
	    		elem.getParameter(var, params, path, 0);
	    	}
	    }
	    else
	    {
	       	Parameter.throwBadPathKeywordException(path);
	    }
	}
	
	public Array encodeToArray() 
	{
	    SupArray array = new SupArray();
	    Iterator<ElementAbstract> iter = this.elements.iterator();
	    while (iter.hasNext())
	    {
	    	ElementAbstract elem = (ElementAbstract) iter.next();
	        array.addLast(elem.encodeToArray());	    	
	    }
	    header.setLength(array.length + header.calculateHeaderSize());
	    array.addFirst(header.encodeToArray());
	    return array;
	}
	
	@Override
	public String toString() 
	{
	    StringBuilder messageToString = new StringBuilder();
	    messageToString.append(header.toString());
	    messageToString.append("\n");
	
	    Iterator<ElementAbstract> iter = this.elements.iterator();
	    while (iter.hasNext())
	    {
	    	ElementAbstract elem = (ElementAbstract) iter.next();
	    	messageToString.append(elem.toString());
	    }
	    return messageToString.toString();
	
	}
	
	public int getLength() {
	
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
	
	public void initDictionary(String syntax) throws Exception 
	{
		this.dictionary = MessageGTP.dictionaries.get(syntax);
		if (this.dictionary == null)
		{
	        XMLDoc xml = new XMLDoc();
	        String file = "../conf/gtpp/dictionary_" + header.getSyntax() + ".xml";
	        xml.setXMLFile(new URI(file));
	        xml.parse();
	        Element rootDico = xml.getDocument().getRootElement();
	        this.dictionary = new Dictionary(rootDico, header.getSyntax());
	        MessageGTP.dictionaries.put(syntax, dictionary);
		}
	}
	
	public List<ElementAbstract> getElement(int id) throws Exception 
	{
		List<ElementAbstract> list = new ArrayList<ElementAbstract>();
		
	    Iterator<ElementAbstract> iter = this.elements.iterator();
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

}
