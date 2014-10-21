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

import java.util.List;

import gp.utils.arrays.Array;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.asn1.dictionary.ASNDictionary;
import com.devoteam.srit.xmlloader.asn1.dictionary.Embedded;
import com.devoteam.srit.xmlloader.asn1.dictionary.EmbeddedMap;


/**
 *
 * @author fhenry
 */
public abstract class ASNMessage 
{
	// Class name for the root object
	protected String className; 

	// list of embedded objects
	private EmbeddedMap embeddedList;

	public ASNMessage()
	{
		this.embeddedList = new EmbeddedMap();
	}
	
	public ASNMessage(String className)
	{
		this();
		this.className = className;
    }

    public String getClassName()
    {
    	return this.className;
    }

	public void setClassName(String className) 
	{
		this.className = className;
	}

    public abstract Array encode() throws Exception; 

    public abstract void decode(Array array) throws Exception;

    public abstract void parseFromXML(Element root) throws Exception;
    
    public abstract String toXML();
    
    public Embedded getEmbeddedByInitial(String initial) 
 	{
     	Embedded init = this.embeddedList.getEmbeddedByInitial(initial);
     	if (init ==  null)
     	{
     		init = ASNDictionary.getInstance().getEmbeddedByInitial(initial);
     	}
     	if (init != null && init.getCondition() == null)
     	{
     		return init;
     	}
     	return null;
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
}