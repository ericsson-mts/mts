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
package com.devoteam.srit.xmlloader.core.utils;

import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * Generic replacer to replace only the attributes of the XML tag
 *
 * @author Gege
 */
public class XMLElementDefaultParser implements XMLElementReplacer, Serializable
{

    static private XMLElementReplacer instance = null;

    static public XMLElementReplacer instance() {
        if (null == instance) {
            instance = new XMLElementDefaultParser();
        }
        return instance;
    }
    
    protected XMLElementDefaultParser(){
    }
    
    public List<Element> replace(Element element, ParameterPool parameterPool) throws Exception {
        List<Element> list = new LinkedList<Element>();

        Element newElement = element.createCopy();
        list.add(newElement);
        List<Attribute> attributes = newElement.attributes();


        for (Attribute attribute : attributes) {
            String value = attribute.getValue();

            LinkedList<String> parsedValue = parameterPool.parse(value);

            if (parsedValue.size() != 1) {
                throw new ExecutionException("Invalid size of variables in attribute " + value);
            }

            attribute.setValue(parsedValue.getFirst());
        }
        return list;
    }
}
