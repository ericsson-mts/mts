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

import java.io.Serializable;
import java.util.List;
import org.dom4j.Element;

/**
 * Generic replacer to replace only the attributes and the content of the XML tag
 *
 * @author gpasquiers
 */
public class XMLElementTextMsgParser implements XMLElementReplacer, Serializable
{
    static private XMLElementReplacer instance = null;

    static public XMLElementReplacer instance() {
        if (null == instance) {
            instance = new XMLElementTextMsgParser();
        }
        return instance;
    }
    private XMLElementReplacer xmlElementDefaultParser;
    private XMLElementReplacer xmlElementTextOnlyParser;

    protected XMLElementTextMsgParser() {
        xmlElementDefaultParser = XMLElementDefaultParser.instance();
        xmlElementTextOnlyParser = XMLElementTextOnlyParser.instance();
    }

    public List<Element> replace(Element element, ParameterPool variables) throws Exception {
        List<Element> result;

        result = xmlElementDefaultParser.replace(element, variables);
        element = result.get(0);
        result = xmlElementTextOnlyParser.replace(element, variables);
        return result;
    }
}
