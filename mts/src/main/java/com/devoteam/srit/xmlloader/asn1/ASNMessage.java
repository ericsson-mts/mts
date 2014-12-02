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

package com.devoteam.srit.xmlloader.asn1;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

import gp.utils.arrays.Array;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.asn1.dictionary.ASNDictionary;
import com.devoteam.srit.xmlloader.asn1.dictionary.Embedded;
import com.devoteam.srit.xmlloader.asn1.dictionary.EmbeddedMap;
import com.devoteam.srit.xmlloader.core.coding.binary.ElementAbstract;
import com.devoteam.srit.xmlloader.core.coding.binary.XMLDoc;
import com.devoteam.srit.xmlloader.gtp.data.MessageGTP;


/**
 *
 * @author fhenry
 */
public abstract class ASNMessage 
{
	
	public static HashMap<String, ASNDictionary> dictionaries = new  HashMap<String, ASNDictionary>();
	
	protected ASNDictionary dictionary;
	
	// list of embedded objects
	private EmbeddedMap embeddedList;

	public ASNMessage()
	{
		this.embeddedList = new EmbeddedMap();
	}
	
	public ASNMessage(String dictionaryFile) throws Exception
	{
		this();
		initDictionary(dictionaryFile);
		
	}
	
    public String getClassName()
    {
    	return this.dictionary.getClassName();
    }

    public abstract Array encode() throws Exception; 

    public abstract void decode(Array array, String className) throws Exception;
    
    public void decode(Array array) throws Exception
    {
    	decode(array, this.dictionary.getClassName());
    }

    public abstract void parseFromXML(Element root, String className) throws Exception;
    
    public void parseFromXML(Element root) throws Exception
    {
    	parseFromXML(root, this.dictionary.getClassName());
    }
    
    public abstract String toXML();
    
    public Embedded getEmbeddedByInitial(String initial) 
 	{
     	Embedded init = this.embeddedList.getEmbeddedByInitial(initial);
     	if (init ==  null)
     	{
     		init = this.dictionary.getEmbeddedByInitial(initial);
     	}
     	if (init != null && init.getCondition() == null)
     	{
     		return init;
     	}
     	return null;
 	}
    
    public ElementAbstract getBinaryByLabel(String label)
    {
    	return this.dictionary.getBinaryByLabel(label);
    }
    
    public List<Embedded> getEmbeddedByCondition(String condition) 
	{
    	return this.dictionary.getEmbeddedByCondition(condition);
	}
    
    public void addConditionalEmbedded(List<Embedded> embeddeds) 
 	{
    	for( int i = 0; i < embeddeds.size(); i++)
    	{
    		Embedded embedded = embeddeds.get(i);
	    	this.embeddedList.addEmbedded(embedded);
	    	embedded.setCondition(null);
    	}
 	}
    
	public void initDictionary(String dictionaryFile) throws Exception 
	{
		this.dictionary = dictionaries.get(dictionaryFile);
		if (this.dictionary == null)
		{
	        this.dictionary = new ASNDictionary(dictionaryFile);
	        dictionaries.put(dictionaryFile, dictionary);
		}
	}
}