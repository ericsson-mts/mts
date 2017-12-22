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
package com.devoteam.srit.xmlloader.core;

import com.devoteam.srit.xmlloader.core.utils.XMLDocument;
import java.net.URI;
import java.util.HashMap;

/**
 *
 * @author gpasquiers
 */
public class Cache {

    static private final HashMap<String, XMLDocument> XML_CACHE = new HashMap<String, XMLDocument>();
    static private final HashMap<XMLDocument, Scenario> SCENARIO_CACHE = new HashMap<XMLDocument, Scenario>();

    static public XMLDocument getXMLDocument(URI pathXML, URI pathXSD) throws Exception {
        XMLDocument xmlDocument = XML_CACHE.get(pathXML.toString());
        if (null == xmlDocument) {
            xmlDocument = new XMLDocument();
            xmlDocument.setXMLSchema(pathXSD);
            xmlDocument.setXMLFile(pathXML);
            xmlDocument.parse();
            XML_CACHE.put(pathXML.toString(), xmlDocument);
        }
        return xmlDocument;
    }

    static public Scenario getScenario(XMLDocument document) throws Exception {
        Scenario scenario = SCENARIO_CACHE.get(document);
        if (null == scenario) {
            scenario = new Scenario(document);
            SCENARIO_CACHE.put(document, scenario);
        }
        return scenario;
    }

    static public void reset() {
        XML_CACHE.clear();
        SCENARIO_CACHE.clear();
    }
}
