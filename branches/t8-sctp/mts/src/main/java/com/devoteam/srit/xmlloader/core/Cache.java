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

    static private HashMap<String, XMLDocument> _xmlCache = new HashMap<String, XMLDocument>();
    static private HashMap<XMLDocument, Scenario> _scenarioCache = new HashMap<XMLDocument, Scenario>();
    static private boolean _enabled = true;

    static public void enable() {
        _enabled = true;
    }

    static public void disable() {
        _enabled = false;
    }

    static public XMLDocument getXMLDocument(URI pathXML, URI pathXSD) throws Exception {
        XMLDocument xmlDocument = null;

        if (_enabled) {
            xmlDocument = _xmlCache.get(pathXML.toString());
        }

        if (null == xmlDocument) {
            xmlDocument = new XMLDocument();
            xmlDocument.setXMLSchema(pathXSD);
            xmlDocument.setXMLFile(pathXML);
            xmlDocument.parse();
            if (_enabled) {
                _xmlCache.put(pathXML.toString(), xmlDocument);
            }
        }


        return xmlDocument;
    }

    static public Scenario getScenario(XMLDocument document) throws Exception {
        Scenario scenario = null;

        scenario = _scenarioCache.get(document);

        if (null == scenario) {
            scenario = new Scenario(document);
            if (_enabled) {
                _scenarioCache.put(document, scenario);
            }
        }

        return scenario;
    }

    static public void reset() {
        _xmlCache.clear();
        _scenarioCache.clear();
    }
}
