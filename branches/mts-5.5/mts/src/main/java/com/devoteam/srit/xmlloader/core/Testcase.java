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
import java.util.List;
import java.util.Map.Entry;
import org.dom4j.Element;

/**
 * Testcase
 */
public class Testcase implements HierarchyMember<Test, Scenario>, Serializable {

    private String _name;
    private boolean _state;
    private int _number;
    private LinkedHashMap<String, Scenario> scenarioByName;
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
    public Testcase(Test test, Element root) throws Exception {
        defaultHierarchyMember = new DefaultHierarchyMember<Test, Scenario>();
        defaultHierarchyMember.setParent(test);
        _root = root;
        _parameters = new ParameterPool(null, ParameterPool.Level.testcase, test.getParameterPool());
        _interruptible = Boolean.valueOf(_root.attributeValue("interruptible", "true"));
        _name = _root.attributeValue("name");
        _number = 1;
        _state = Boolean.parseBoolean(_root.attributeValue("state"));
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

        // fill a map with "scenario name" => "scenario object"
        scenarioByName = new LinkedHashMap<String, Scenario>();
        for (Element element : (List<Element>) _root.selectNodes("./scenario")) {
            Scenario scenario = new Scenario(element, this);

            String name = scenario.getName();
            if (null != name && scenarioByName.containsKey(name)) {
                throw new ParsingException("Duplicate scenario identifier (name) : " + name + "; the scenario identifier must be unique because it is used for message routing between scenarios.");
            }

            addChild(scenario);

            scenarioByName.put(name, scenario);
        }
    }

    public boolean parsedScenarios() throws Exception {
        for (Entry<String, Scenario> entry : scenarioByName.entrySet()) {
            if(!entry.getValue().isParsed()){
                return false;
            }
        }
        return true;
    }

    public void parseScenarios() throws Exception {
        for (Entry<String, Scenario> entry : scenarioByName.entrySet()) {
            entry.getValue().parse();
        }
    }

    public void free() {
        for (Entry<String, Scenario> entry : scenarioByName.entrySet()) {
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

    public LinkedHashMap<String, Scenario> getScenarioPathByNameMap() {
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
