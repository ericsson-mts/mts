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

package com.devoteam.srit.xmlloader.asn1.dictionary;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.coding.binary.Dictionary;
import com.devoteam.srit.xmlloader.core.coding.binary.ElementAbstract;
import com.devoteam.srit.xmlloader.core.coding.binary.XMLDoc;


/**
 *
 * @author fhenry
 */
public class ASNDictionary extends Dictionary
{

	private static ASNDictionary _instance;
    
	private String layer;
	private String className;
	
	// list of embedded objects
	private EmbeddedMap embeddedList;

    public ASNDictionary()
    {
    	super();
		embeddedList = new EmbeddedMap();
    }
	
	public static ASNDictionary getInstance() throws Exception
    {
    	if (_instance == null)
    	{
    		_instance = new ASNDictionary("tcap/dictionary_TCAP.xml");
    	}
    	return _instance;
    }

    public String getLayer() {
		return layer;
	}

	public String getClassName() {
		return className;
	}

	public ASNDictionary(String file) throws Exception 
    {
    	this();
		XMLDoc xml = new XMLDoc();
		String path = "../conf/asn1/" + file;
	    xml.setXMLFile(new URI(path));
	    xml.parse();
	    Element rootDico = xml.getDocument().getRootElement();
	    parseFromXML(rootDico);
    }
	
    public void parseFromXML(Element root) throws Exception 
    {
    	this.layer = root.attributeValue("layer");
    	this.className = root.attributeValue("className");
    	
        List<Element> listElement = root.elements("element");
        for (Element elem : listElement) 
        {
        	String coding = elem.attributeValue("coding");
        	ElementAbstract elemInfo = ElementAbstract.buildFactory(coding, null);            
            elemInfo.parseFromXML(elem, this, null, true);
            addElement(elemInfo);
        }

        List<Element> listEmbedded = root.elements("embedded");
        for (Element elem : listEmbedded) 
        {
            Embedded embedded = new Embedded();            
            embedded.parseFromXML(elem, this);
            
            this.embeddedList.addEmbedded(embedded);
        }

    }
    
    public Embedded getEmbeddedByInitial(String initial) 
	{
		return embeddedList.getEmbeddedByInitial(initial);
	}

    public List<Embedded> getEmbeddedByCondition(String condition) 
	{
    	return embeddedList.getEmbeddedByCondition(condition);
	}


}
