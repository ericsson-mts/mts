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

import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.basic.OperationParameter;


import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLElementDefaultParser;
import com.devoteam.srit.xmlloader.core.utils.XMLElementTextMsgParser;
import com.devoteam.srit.xmlloader.core.utils.XMLTree;
import com.devoteam.srit.xmlloader.core.utils.hierarchy.DefaultHierarchyMember;
import com.devoteam.srit.xmlloader.core.utils.hierarchy.HierarchyMember;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import org.dom4j.Element;

/**
 * Testcase
 */
public class Testcase implements HierarchyMember<Test, ScenarioReference>, Serializable {

    private String _name;
    private int _index;
    private boolean _state;
    private int _number;
    private LinkedHashMap<String, ScenarioReference> scenarioByName;
    private LinkedHashMap<String, List<OperationParameter>> parametersByScenarioName;
    private Element _root;
    private int _runId = 0;
    private boolean _interruptible;
    private RunProfile _runProfile;
    private transient ParameterPool _parameters;
    private transient TestcaseRunner _testcaseRunner;
    /**
     * create the testcase object
     *  1 - init parameter pool: execute the "parameter" operations
     *  2 - init the runprofile: execute a replacer on the <runprofile>
     *  3 - init the scenario: execut a replace on <scenario>
     */
    public Testcase(Test test, Element root, int index) throws Exception {
        parametersByScenarioName = new LinkedHashMap<String, List<OperationParameter>>();
        defaultHierarchyMember = new DefaultHierarchyMember<Test, ScenarioReference>();
        defaultHierarchyMember.setParent(test);
        _root = root;
        _parameters = new ParameterPool(null, ParameterPool.Level.testcase, test.getParameterPool());
        _interruptible = Boolean.valueOf(_root.attributeValue("interruptible", "true"));
        _name = _root.attributeValue("name");
        _index = index;
        String strNumber= _root.attributeValue("number");
        _number = 1;
        if (strNumber != null)
        {
        	_number = Integer.parseInt(strNumber);
    	}
        String strState = _root.attributeValue("state");
        if (strState != null)
        {
        	_state = Boolean.parseBoolean(strState);
        }
        _name = Utils.replaceFileName(this._name);

        // assert the name is not empty (can cause problems with log files and stats)
        if (_name == null || _name.trim().isEmpty()) {
            throw new ParsingException("testcase name should not be empty " + _root.asXML());
        }

        // do 1, 2, 3 in one go because of runner
        initParametersRunProfileScenarios();
        
        _testcaseRunner = new TestcaseRunner(this);
    }

    /**
     * Parse a testcase
     *
     * @param node Parser node
     * @return Testcase
     * @throws ParsingException
     */
    private void initParametersRunProfileScenarios() throws Exception {
        Runner runner = new Runner(getId());

        runner.setParameterPool(_parameters);

        // set the hardcoded parameters [testName] and [testId]
        runner.getParameterPool().createSimple("[testcaseName]", this._name);
        runner.getParameterPool().createSimple("[testcaseId]", this.getId());
        runner.getParameterPool().createSimple("[testcaseIndex]", this._index);

        // create and execute parameter operation to init testcase pool
        List<Element> elementsParameter = (List<Element>) _root.selectNodes("./parameter");
        for (Element element : elementsParameter) {
            OperationParameter operationParameter = new OperationParameter(element);
            operationParameter.executeAndStat(runner);
        }

        // apply replacer on <runProfile> (recursive) and parse runprofile
        Element runProfileElement = (Element) _root.selectSingleNode("./runProfile");
        if (null == runProfileElement) {
            this._runProfile = this.getParent().getProfile();
        }
        else {
            XMLTree xmlTree = new XMLTree(runProfileElement, false, Parameter.EXPRESSION, true);

            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "Testcase RunProfile before parsing\n", xmlTree);

            xmlTree.replace(XMLElementDefaultParser.instance(), runner.getParameterPool());

            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "Testcase RunProfile after parsing\n", xmlTree);

            this._runProfile = new RunProfile(xmlTree.getTreeRoot());
        }

        // apply replacer on <scenario> (recursive) and parse runprofile
        for (Element scenario : (List<Element>) _root.selectNodes("./scenario")) {
            // apply replacer everything on everything or just the scenario element depending on presence of @file or not
            if(scenario.selectNodes("./@file").isEmpty()){
                XMLTree xmlTree = new XMLTree(scenario, false, Parameter.EXPRESSION, true); // do not duplicate, use the same root
                GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.CORE, "Putting the path as text is deprecated, please use attribute \"file\"");
                GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "Scenario before parsing\n", xmlTree);
                xmlTree.replace(XMLElementTextMsgParser.instance(), runner.getParameterPool());
                GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "Scenario after parsing\n", xmlTree);
            }
            else{
                XMLTree xmlTree = new XMLTree(scenario, false, Parameter.EXPRESSION, false); // do not duplicate, use the same root
                GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "Scenario before parsing\n", xmlTree);
                xmlTree.replace(XMLElementDefaultParser.instance(), runner.getParameterPool());
                GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "Scenario after parsing\n", xmlTree);
            }
        }

        // fill a map with "scenario name" => "scenario object"
        scenarioByName = new LinkedHashMap<String, ScenarioReference>();
        for (Element element : (List<Element>) _root.selectNodes("./scenario")) {
            ScenarioReference scenario = new ScenarioReference(element, this);
            String name = scenario.getName();
            if (null != name && scenarioByName.containsKey(name)) {
                throw new ParsingException("Duplicate scenario identifier (name) : " + name + "; the scenario identifier must be unique because it is used for message routing between scenarios.");
            }

            addChild(scenario);

            scenarioByName.put(name, scenario);
            
            // fill the list of parameter operation per scenario
            List<OperationParameter> parameters;
            if(!parametersByScenarioName.containsKey(name)){
                parameters = new LinkedList<OperationParameter>();
                parametersByScenarioName.put(name, parameters);
            }
            else{
                parameters = parametersByScenarioName.get(name);
            }
            
            for (Element parameterElement : (List<Element>) element.selectNodes("./parameter")) {
                parameters.add(new OperationParameter(parameterElement));
            }
        }
    }

    public boolean parsedScenarios() throws Exception {
        for (Entry<String, ScenarioReference> entry : scenarioByName.entrySet()) {
            if(!entry.getValue().isParsed()){
                return false;
            }
        }
        return true;
    }

    public void parseScenarios() throws Exception {
        for (Entry<String, ScenarioReference> entry : scenarioByName.entrySet()) {
            entry.getValue().parse();
        }
    }

    public void free() {
        for (Entry<String, ScenarioReference> entry : scenarioByName.entrySet()) {
            entry.getValue().free();
        }
    }

    public TestcaseRunner getTestcaseRunner(){
        return _testcaseRunner;
    }

    public ParameterPool getParameterPool() {
        return _parameters;
    }

    public String attributeValue(String name) {
        return _root.attributeValue(name);
    }

    public LinkedHashMap<String, ScenarioReference> getScenarioPathByNameMap() {
        return scenarioByName;
    }

    public String getId() {
        return this.getParent().attributeValue("name") + this._name;
    }

    public int getRunId() {
        return _runId;
    }

    public String getName() {
        return this._name;
    }

    public int incRunId() {
        return _runId++;
    }

    public void setRunId(int runId) {
        _runId = runId;
    }

    public synchronized void setNumber(int value){
        _number = value;
    }

    public synchronized int getNumber(){
        return _number;
    }

    public synchronized void setState(boolean value){
        _state = value;
    }

    public synchronized boolean getState(){
        return _state;
    }

    public boolean isInterruptible() {
        return this._interruptible;
    }

    public RunProfile getProfile() throws Exception {
        return this._runProfile;
    }
    
    public List<OperationParameter> getParametersByScenarioName(String scenarioName){
        return this.parametersByScenarioName.get(scenarioName);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Hierarchy implementation">
    private DefaultHierarchyMember<Test, ScenarioReference> defaultHierarchyMember;

    public Test getParent() {
        return this.defaultHierarchyMember.getParent();
    }

    public List<ScenarioReference> getChildren() {
        return this.defaultHierarchyMember.getChildren();
    }

    public void setParent(Test parent) {
        this.defaultHierarchyMember.setParent(parent);
    }

    public void addChild(ScenarioReference child) {
        this.defaultHierarchyMember.addChild(child);
    }

    public void removeChild(ScenarioReference child) {
        this.defaultHierarchyMember.removeChild(child);
    }
    // </editor-fold>
}
