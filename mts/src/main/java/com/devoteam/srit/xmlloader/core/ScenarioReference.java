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



import com.devoteam.srit.xmlloader.core.utils.URIFactory;
import com.devoteam.srit.xmlloader.core.utils.URIRegistry;
import com.devoteam.srit.xmlloader.core.utils.XMLDocument;
import org.dom4j.Element;

import java.io.Serializable;

/**
 * Scenario to be played
 * @author JM. Auffret
 */
public class ScenarioReference implements Serializable {

    /** Name of the scenario */
    private String _name;
    private String _routingName;
    private String _description;
    private boolean _state = true;
    private String _filename;
    private Scenario _scenario;
    private Testcase _testcase;
    private boolean _parsed = false;
    private transient ScenarioRunner _scenarioRunner;
    
    /** Constructor */
    public ScenarioReference(String name) {
        _name = name;
    }

    public ScenarioReference(Element elements, Testcase testcase) {
        _name = elements.attributeValue("name");
        _routingName = elements.attributeValue("routingName");
        _description = elements.attributeValue("description");
        String strState = elements.attributeValue("state");
        if (strState != null)
        {
        	_state = Boolean.parseBoolean(strState);
        }
        _filename = elements.attributeValue("file");
        if(null == _filename)
        {
            _filename = elements.getStringValue();
        }
        _filename = _filename.trim();
        _testcase = testcase;
    }

    public void setScenarioRunner(ScenarioRunner value){
        _scenarioRunner = value;
    }

    public Testcase getTestcase() {
        return _testcase;
    }

    public void free() {
        _scenario = null;
        _parsed = false;
    }

    public Scenario getScenario(){
        return _scenario;
    }
    
    public boolean isParsed() {
        return _parsed;
    }

    public void parse() throws Exception {
        //get scenario from cache
        _parsed = true;
        XMLDocument document = Cache.getXMLDocument(URIRegistry.MTS_TEST_HOME.resolve(getFilename()), URIFactory.newURI("../conf/schemas/scenario.xsd"));
        _scenario = Cache.getScenario(document);
        
        _scenarioRunner.getState().setFlag(RunnerState.F_OPENED, true);
        _scenarioRunner.doNotifyAll();
    }

    public String getName() {
        return _name;
    }
    
    public String getRoutingName() {
        if(null != _routingName){
            return _routingName;
        }
        else{
            return getName();
        }
    }

    public String getDescription() {
        return _description;
    }

    public boolean getState() {
        return _state;
    }
    
    public String getFilename() {
        return _filename;
    }

    public String getId() {
        return _testcase.getId() + getName();
    }

    public String toString() {
        return _name;
    }
}
