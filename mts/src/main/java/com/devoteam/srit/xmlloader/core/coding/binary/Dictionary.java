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

import java.util.LinkedHashMap;
import java.util.List;
import org.dom4j.Element;


/**
 *
 * @author indiaye
 */
public class Dictionary {
	
    private LinkedHashMap<String,ElementAbstract> mapElementByName=new LinkedHashMap<String, ElementAbstract>();
    private LinkedHashMap<Integer,ElementAbstract> mapElementById=new LinkedHashMap<Integer, ElementAbstract>();
    private LinkedHashMap<String,FieldAbstract> mapHeader= new LinkedHashMap<String, FieldAbstract>();
    
    public Dictionary(Element root, String syntax) throws Exception 
    {
     
        List<Element> listElem=root.element("header").elements("field");
        for (Element element : listElem) {
            mapHeader.put(element.attributeValue("name"), new EnumerationField(element));
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
            	elemInfo = ElementAbstract.buildFactory(elem);
            }
            
            elemInfo.parseFromXML(elem, this);
            mapElementByName.put(elem.attributeValue("name"), elemInfo);
            String tagId = elem.attributeValue("identifier");
            if (tagId != null)
            {
            	tagId = tagId.trim();
            }
            if (tagId == null)
            {
            	tagId = elem.attributeValue("tag");
                if (tagId != null)
                {
                	tagId = tagId.trim();
                }
            }
            mapElementById.put((int)(Utils.parseBinaryString(tagId)[0] & 0xff), elemInfo);
        }

    }
    public LinkedHashMap<Integer, ElementAbstract> getMapElementById() {
        return mapElementById;
    }

    public LinkedHashMap<String, ElementAbstract> getMapElementByName() {
        return mapElementByName;
    }

    public LinkedHashMap<String, FieldAbstract> getMapHeader() {
        return mapHeader;
    }

}
