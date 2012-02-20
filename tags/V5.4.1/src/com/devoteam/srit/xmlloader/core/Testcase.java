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
*/package com.devoteam.srit.xmlloader.core;

import com.devoteam.srit.xmlloader.core.utils.URIRegistry;
import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.basic.OperationParameter;


import com.devoteam.srit.xmlloader.core.utils.URIFactory;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLDocument;
import com.devoteam.srit.xmlloader.core.utils.XMLElementDefaultParser;
import com.devoteam.srit.xmlloader.core.utils.XMLElementTextMsgParser;
import com.devoteam.srit.xmlloader.core.utils.XMLTree;
import com.devoteam.srit.xmlloader.core.utils.hierarchy.DefaultHierarchyMember;
import com.devoteam.srit.xmlloader.core.utils.hierarchy.HierarchyMember;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import org.dom4j.Element;

/**
 * Testcase
 */
public class Testcase implements HierarchyMember<Test, Scenario>, Serializable {

    private String _name;
    private LinkedHashMap<String, Scenario> scenarioPathByName;
    private Element _root;
    private int _runId = 0;
    private boolean _parsedScenarios;
    private boolean _interruptible;
    private RunProfile _runProfile;
    private transient ParameterPool _parameters;

    /**
     * create the testcase object
     *  1 - init parameter pool: execute the "parameter" operations
     *  2 - init the runprofile: execute a replacer on the <runprofile>
     *  3 - init the scenario: execut a replace on <scenario>
     */
    public Testcase(Test test, Element root) throws Exception {
        defaultHierarchyMember = new DefaultHierarchyMember<Test, Scenario>();
        defaultHierarchyMember.setParent(test);
        _root = root;
        _parsedScenarios = false;
        _parameters = new ParameterPool(null, ParameterPool.Level.testcase, test.getParameterPool());
        _interruptible = Boolean.valueOf(_root.attributeValue("interruptible", "true"));
        _name = _root.attributeValue("name");
        _name = Utils.replaceFileName(this._name);

        // assert the name is not empty (can cause problems with log files and stats)

        if (_name == null || _name.trim().isEmpty()) {
            throw new ParsingException("testcase name should not be empty " + _root.asXML());
        }

        // do 1, 2, 3 in one go because of runner
        initParametersRunProfileScenarios();

    }

    /**
     * Parse a testcase
     *
     * @param node Parser node
     * @return Testcase
     * @throws ParsingException
     */
    private void initParametersRunProfileScenarios() throws Exception {
        Runner runner = new Runner(getId()) {

            @Override
            public void start() {
            }

            @Override
            public void stop() {
            }
        };

        runner.setParameterPool(_parameters);

        // set the hardcoded parameters [testName] and [testId]
        runner.getParameterPool().createSimple("[testcaseName]", this._name);
        runner.getParameterPool().createSimple("[testcaseId]", this.getId());

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

            xmlTree.replace(new XMLElementDefaultParser(runner.getParameterPool()));

            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "Testcase RunProfile after parsing\n", xmlTree);

            this._runProfile = new RunProfile(xmlTree.getTreeRoot());
        }

        // apply replacer on <scenario> (recursive) and parse runprofile
        for (Element scenario : (List<Element>) _root.selectNodes("./scenario")) {
            XMLTree xmlTree = new XMLTree(scenario, false, Parameter.EXPRESSION, true); // do not duplicate, use the same root

            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "Scenario before parsing\n", xmlTree);

            xmlTree.replace(new XMLElementTextMsgParser(runner.getParameterPool()));

            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "Scenario RunProfile after parsing\n", xmlTree);
            scenario = xmlTree.getTreeRoot();
        }

        // fill a map with scenario name <=> file path
        scenarioPathByName = new LinkedHashMap<String, Scenario>();
        for (Element elements : (List<Element>) _root.selectNodes("./scenario")) {
            Scenario scenario = new Scenario(elements, this);

            String name = scenario.getName();
            if (null != name && scenarioPathByName.containsKey(name)) {
                throw new ParsingException("Duplicate scenario identifier (name) : " + name + "; the scenario identifier must be unique because it is used for message routing between scenarios.");
            }

            scenarioPathByName.put(name, scenario);
        }
    }

    public boolean parsedScenarios() throws Exception {
        return this._parsedScenarios;
    }

    public void parseScenarios() throws Exception {
        reset();

        // get scenarios ids and files path
        Iterator iter = scenarioPathByName.values().iterator();
        while (iter.hasNext()) {
            Scenario scenario = (Scenario) iter.next();
            ;

            String relativePath = scenario.getFilename();

            XMLDocument scenarioDocument = XMLDocumentCache.get(URIRegistry.IMSLOADER_TEST_HOME.resolve(relativePath), URIFactory.newURI("../conf/schemas/scenario.xsd"));

            scenario.parse(scenarioDocument.getDocument().getRootElement());
            this.addChild(scenario);
        }

        this._parsedScenarios = true;
    }

    public void reset() {
        _parsedScenarios = false;
        getChildren().clear();
    }

    public ParameterPool getParameterPool() {
        return _parameters;
    }

    public String attributeValue(String name) {
        return _root.attributeValue(name);
    }

    public LinkedHashMap<String, Scenario> getScenarioPathByNameMap() {
        return scenarioPathByName;
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

    public boolean interruptible() {
        return this._interruptible;
    }

    public RunProfile getProfile() throws Exception {
        return this._runProfile;
    }
    // <editor-fold defaultstate="collapsed" desc="Hierarchy implementation">
    private DefaultHierarchyMember<Test, Scenario> defaultHierarchyMember;

    public Test getParent() {
        return this.defaultHierarchyMember.getParent();
    }

    public List<Scenario> getChildren() {
        return this.defaultHierarchyMember.getChildren();
    }

    public void setParent(Test parent) {
        this.defaultHierarchyMember.setParent(parent);
    }

    public void addChild(Scenario child) {
        this.defaultHierarchyMember.addChild(child);
    }

    public void removeChild(Scenario child) {
        this.defaultHierarchyMember.removeChild(child);
    }
    // </editor-fold>
}
