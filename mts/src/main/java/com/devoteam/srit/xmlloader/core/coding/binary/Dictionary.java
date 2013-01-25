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

import com.devoteam.srit.xmlloader.core.coding.binary.q931.ElementQ931;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;


/**
 *
 * @author indiaye
 */
public class Dictionary {
	
    private Map<String,ElementAbstract> elementsMapByLabel = new HashMap<String, ElementAbstract>();
    private Map<Integer,ElementAbstract> elementsMapByTag = new HashMap<Integer, ElementAbstract>();
    private Map<String,FieldAbstract> fieldsMapHeader = new HashMap<String, FieldAbstract>();
    
    public Dictionary(Element root, String syntax) throws Exception 
    {
     
        List<Element> listElem=root.element("header").elements("field");
        for (Element element : listElem) {
        	fieldsMapHeader.put(element.attributeValue("name"), new EnumerationField(element));
        }
        
        List<Element> list=root.elements("element");
        for (Element elem : list) {
            ElementAbstract elemInfo = null;
            if ("Q931".equalsIgnoreCase(syntax))
            {
            	elemInfo = new ElementQ931();
            }
            else if (syntax.contains("GTP"))
            {
            	String coding = elem.attributeValue("coding");
            	elemInfo = ElementAbstract.buildFactory(coding);
            }
            
            elemInfo.parseFromXML(elem, this);
            
            elementsMapByLabel.put(elemInfo.getLabel(), elemInfo);
            elementsMapByTag.put(elemInfo.getTag(), elemInfo);
        }

    }
    public ElementAbstract getElementByTag(Integer tag) 
    {
        return elementsMapByTag.get(tag);
    }

    public ElementAbstract getElementByLabel(String label) 
    {
        return elementsMapByLabel.get(label);
    }

    public FieldAbstract getHeaderFieldByName(String name) 
    {
        return fieldsMapHeader.get(name);
    }

}
