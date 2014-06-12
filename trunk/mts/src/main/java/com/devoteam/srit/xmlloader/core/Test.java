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
import com.devoteam.srit.xmlloader.core.report.ReportGenerator;
import com.devoteam.srit.xmlloader.core.utils.URIRegistry;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.newstats.StatKey;

import java.util.List;

import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.operations.basic.OperationParameter;
import com.devoteam.srit.xmlloader.core.parameters.EditableParameterProvider;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLDocument;
import com.devoteam.srit.xmlloader.core.utils.XMLElementDefaultParser;
import com.devoteam.srit.xmlloader.core.utils.XMLTree;

import com.devoteam.srit.xmlloader.core.utils.hierarchy.DefaultHierarchyMember;
import com.devoteam.srit.xmlloader.core.utils.hierarchy.HierarchyMember;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.DefaultElementInterface;

/**
 * Object representing the test XML file
 */
public class Test implements Serializable, HierarchyMember<Object, Testcase> {

    private String _name;
    private String _description;
    private long beginTime;
    private Element _root;
    private XMLDocument _xmlDocument;
    private EditableParameterProvider _editableParameterProvider;
    private transient ParameterPool _parameters;

    private RunProfile _runProfile;

    /**
     * parse the test file
     *  1 - init misc attributes
     *  2 - update the editable parameters values
     *  3 - work on the DOM tree
     *    3.1 - unfold the "for" loops
     *    3.2 - replace paramters in testcase names
     *    3.3 - replace parameters in runprofile (and parse runprofile)
     *  4 - create testcase objects
     */
    public Test(XMLDocument xmlDocument, EditableParameterProvider editableParameterProvider) throws Exception {
        URIRegistry.MTS_TEST_HOME = xmlDocument.getXMLFile();

        // init
        _editableParameterProvider = editableParameterProvider;
        _parameters = new ParameterPool(null, ParameterPool.Level.test, null);
        _xmlDocument = xmlDocument;
        _root = this._xmlDocument.getDocument().getRootElement();
        _name = _root.attributeValue("name");
        _name = Utils.replaceFileName(this._name);
        _description = _root.attributeValue("description");
        defaultHierarchyMember = new DefaultHierarchyMember<Object, Testcase>();
        this.setParent(null);

        if (null != _root.attributeValue("home") && !_root.attributeValue("home").endsWith("/")) {
            _root.attribute("home").setValue(_root.attributeValue("home") + "/");
        }

        String home = this.attributeValue("home");
        if (null == home) {
            URIRegistry.MTS_CONFIG_HOME = URIRegistry.MTS_TEST_HOME;
        }
        else {
            if (!home.endsWith("/")) {
                home += "/";
            }
            URIRegistry.MTS_CONFIG_HOME = URIRegistry.MTS_TEST_HOME.resolve(home);
        }

        // 2 - update the editable parameters values
        initEditableParameters();

        // 3.1, 3.2, 3.3 - in one go because they need a "runner" context
        initForLoopsTestcasesRunProfile();

        // 4 - parse testcases
        initTestcases();
    }

    /**
     * process the editable <parameter> and set their value with
     * the one given by the EditableParameterProvider
     */
    private void initEditableParameters() throws ParsingException {
        // parameters - editable
        //   throw an error if one editable=yes parameter that has an operation attribute that is neither set or list.set
        List invalid = _root.selectNodes("./parameter[@editable='true'][@operation][not(@operation='set')][not(@operation='list.set')]");
        if (invalid.size() > 0) {
            throw new ParsingException("Only operations 'set' should have 'editable=true' attribute\n" + ((Element) invalid.get(0)).asXML());
        }

        // parameters - editable
        //   get all editable=yes parameters (./parameter[@editable='true'])
        List<Element> elementsParameterEditable = getEditableParameters();

        // parameters - editable
        //   check unicity of all parameters names
        HashSet<String> hashSet = new HashSet();
        for (Element element : elementsParameterEditable) {
            String nameAttribute = (String) element.selectObject("string(./@name)");
            /* Suppression d'un controle inutile
            if (hashSet.contains(nameAttribute)) {
                // throw new ParsingException("The name of the 'editable' parameters should be unique\n" + element.asXML());
            }
            */
            hashSet.add(nameAttribute);
        }

        // parameters - editable
        //   change the values of each editable parameter by the one provided by _editableParameterProvider
        if (null != _editableParameterProvider) {
            for (Element element : elementsParameterEditable) {
                String value = _editableParameterProvider.getParameterValue(element.attributeValue("name"));
                if (null != value) {
                    element.attribute("value").setValue(value);
                }
            }
        }
    }

    /**
     * process the <for> tag : copy of the <testcase> tag contained in the
     * <for> tag as many time as required and delete the corresponding <for> tag
     */
    private void initForLoopsTestcasesRunProfile() throws Exception {
        Runner runner = new Runner(_name);

        runner.setParameterPool(_parameters);

        // set the hardcoded parameters [testName] and [testId]
        runner.getParameterPool().createSimple("[testName]", this.getName());
        runner.getParameterPool().createSimple("[testId]", this.getName());

        // execute the parameters operations
        this.executeParameterOperations(runner);

        // unfold for loops
        List<Element> elementsFor = _root.elements("for");
        for (Element element : elementsFor) {
            List<Element> testcases = unfoldFor(element, runner);
            for (Element testcase : testcases) {
                DefaultElementInterface.insertNode((DefaultElement) _root, element, testcase);
            }
            _root.remove(element);
        }

        // testcases (replace the parameters by their values in DOM structure)
        List<Element> elementsTestcase = _root.elements("testcase");
        for (Element element : elementsTestcase) {
            DefaultElement testcase = (DefaultElement) replaceParameterInTestcase(element, runner);
            DefaultElementInterface.insertNode((DefaultElement) _root, element, testcase);
            _root.remove(element);
        }

        // runprofile (replace the parameters by their values in DOM structure)
        Element runProfileElement = _root.element("runProfile");
        if (null == runProfileElement) {
            runProfileElement = new DefaultElement("runProfile");
        }
        else {
            XMLTree xmlTree = new XMLTree(runProfileElement, true, Parameter.EXPRESSION, true);

            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "Test RunProfile before parsing\n", xmlTree);

            xmlTree.replace(XMLElementDefaultParser.instance(), runner.getParameterPool());

            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "Test RunProfile after parsing\n", xmlTree);

            runProfileElement = xmlTree.getTreeRoot();
        }
        _runProfile = new RunProfile(runProfileElement);
    }

    /**
     * execute the <parameter> tag : build the parameters pool
     */
    public void executeParameterOperations(Runner runner) throws Exception {
        // set the hardcoded parameters [testName] and [testId]
        runner.getParameterPool().createSimple("[testName]", this.getName());
        runner.getParameterPool().createSimple("[testId]", this.getName());

        List<Element> elementsParameter = (List<Element>) _xmlDocument.getDocument().selectNodes("/test/parameter");
        for (Element element : elementsParameter) {
            OperationParameter operationParameter = new OperationParameter(element);
            operationParameter.executeAndStat(runner);
        }
    }

    private List<Element> unfoldFor(Element element, Runner runner) throws Exception {
        // do parameter replacement in <for> element (not recursive)
        XMLTree xmlTree;
        xmlTree = new XMLTree(element, true, Parameter.EXPRESSION, false);
        xmlTree.replace(XMLElementDefaultParser.instance(), runner.getParameterPool());
        element = xmlTree.getTreeRoot();

        String index = element.attributeValue("index");
        String from = element.attributeValue("from");
        String to = element.attributeValue("to");
        String step = element.attributeValue("step");

        // Default step value
        if (null == step) {
            step = "1";
        }

        try {
            from = runner.getParameterPool().parse(from).getFirst();
            to = runner.getParameterPool().parse(to).getFirst();
            step = runner.getParameterPool().parse(step).getFirst();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        LinkedList<Element> elements = new LinkedList<Element>();

        // if there is a parameter with the same name than index, save it
        Parameter oldIndex = null;
        if (runner.getParameterPool().exists(index)) {
            oldIndex = runner.getParameterPool().get(index);
        }

        // read the attributes from XML
        int indexLength = Math.max(from.length(), to.length());
        int indexInt = Integer.parseInt(from);
        int fromInt = Integer.parseInt(from);
        int toInt = Integer.parseInt(to);
        int stepInt = Integer.parseInt(step);

        // now really unfold the loop, do replacement in <testcase>
        while (((stepInt > 0) && (indexInt >= fromInt) && (indexInt <= toInt)) ||
        	  ((stepInt < 0) && (indexInt <= fromInt) && (indexInt >= toInt)))
        {
            String indexStr = String.valueOf(indexInt);
            indexStr = Utils.padInteger(indexStr, indexLength);

            runner.getParameterPool().createSimple(index, indexStr);

            List<Element> testcases = element.elements("testcase");
            for (Element testcase : testcases) {
                // do parameter replacement in <testcase> element (not recursive)
                // must duplicate the element !
                xmlTree = new XMLTree(testcase, true, Parameter.EXPRESSION, false);
                xmlTree.replace(XMLElementDefaultParser.instance(), runner.getParameterPool());

                testcase = xmlTree.getTreeRoot();

                // index in testcase pool : create a parameter operation that sets the index value value
                DefaultElement parameter = new DefaultElement("parameter");
                parameter.addAttribute("name", index);
                parameter.addAttribute("value", indexStr);

                // index in testcase pool : very dirty "insert first" : remove all children, add parameter, re-add children
                List<Element> children = testcase.elements();
                for(Element child:children){
                    testcase.remove(child);
                }
                testcase.add(parameter);
                for(Element child:children){
                    testcase.add(child);
                }

                elements.addLast(xmlTree.getTreeRoot());
            }
            indexInt += stepInt;
        }

        runner.getParameterPool().delete(index);
        
        // restore the parameter with the same name as index, if there was one
        if (null != oldIndex) {
            runner.getParameterPool().set(index, oldIndex);
        }

        return elements;
    }

    private Element replaceParameterInTestcase(Element testcase, Runner runner) throws Exception {
        // Parser for testcase
        XMLTree xmlTree;
        xmlTree = new XMLTree(testcase);
        xmlTree.compute(Parameter.EXPRESSION, false);
        xmlTree.replace(XMLElementDefaultParser.instance(), runner.getParameterPool());

        Element newTestcase = xmlTree.getTreeRoot();

        for (Object object : newTestcase.elements()) {
            Element innerElement = (Element) object;

            if (!innerElement.getName().equals("parameter")) {
                XMLTree innerXmlTree;
                innerXmlTree = new XMLTree(innerElement, false);
                innerXmlTree.compute(Parameter.EXPRESSION, false);

                innerXmlTree.replace(XMLElementDefaultParser.instance(), runner.getParameterPool());
            }
        }

        return xmlTree.getTreeRoot();
    }

    /**
     * process the <testcase> tag : create testcase objects
     */
    private void initTestcases() throws Exception {
        this.getChildren().clear();

        // parse each testcase
        List testcases = _root.selectNodes("./testcase");
        int index = 0;
        for (Element element : (List<Element>) testcases) {
            Testcase testcase = new Testcase(this, element, index);
            index++;
            this.addChild(testcase);
        }

        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, this.getChildren().size(), " testcases read from file.");
    }

    public XMLDocument getXMLDocument() {
        return this._xmlDocument;
    }

    public List<Testcase> getTestcaseList() {
        return this.getChildren();
    }

    public ParameterPool getParameterPool() {
        return _parameters;
    }

    public String attributeValue(String name) {
        return _root.attributeValue(name);
    }
    
    public List<Element> getEditableParameters() {
        return (List<Element>) _root.selectNodes("./parameter[@editable='true']");
    }

    public RunProfile getProfile() throws Exception {
        return _runProfile;
    }
    private DefaultHierarchyMember<Object, Testcase> defaultHierarchyMember;

    public Object getParent() {
        return this.defaultHierarchyMember.getParent();
    }

    public List<Testcase> getChildren() {
        return this.defaultHierarchyMember.getChildren();
    }

    public void setParent(Object parent) {
        this.defaultHierarchyMember.setParent(parent);
    }

    public void addChild(Testcase child) {
        this.defaultHierarchyMember.addChild(child);
    }

    public void removeChild(Testcase child) {
        this.defaultHierarchyMember.removeChild(child);
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._root.attribute("name").setValue(name);
        this._name = name;
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(String descr) {
        this._root.attribute("description").setValue(descr);
        this._description = descr;
    }

    public long getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(long beginTime) {
		this.beginTime = beginTime;
	}

	public Testcase getTestcase(String testcaseName) {
        List<Testcase> testcaseList = this.getTestcaseList();

        for (Testcase testcase : testcaseList) {
            if (testcase.getName().equalsIgnoreCase(testcaseName)) {
                return testcase;
            }
        }
        return null;
    }

    public void report_generate() {
        String reportDir = Config.getConfigByName("tester.properties").getString("stats.REPORT_DIRECTORY", "../reports/");
        try {
            ReportGenerator reportGenerator = new ReportGenerator(reportDir + "/" + "TEST_" + getName());

            reportGenerator.generateReport(StatPool.getInstance().clone(), StatPool.getInstance().getZeroTimestamp());
        }
        catch (Exception e) {
            GlobalLogger.instance().getApplicationLogger().error(Topic.CORE, e, "Error occured while generating the statistic report");
            e.printStackTrace();
        }

        boolean automaticShow = Config.getConfigByName("tester.properties").getBoolean("stats.AUTOMATIC_SHOW", false);
        if (automaticShow) {
            report_show();
        }
    }

    public static void report_show() {
        ThreadPool.reserve().start(new Runnable() {

            public void run() {
                try {
                    // find then open the latest report in the directory
                    String reportFileName = Config.getConfigByName("tester.properties").getString("stats.REPORT_DIRECTORY", "../reports/");
                    String[] files = new File(reportFileName).list();
                    File latest = null;
                    if (files != null)
                    {
	                    for (String file : files) {
	                        File aFile = new File(reportFileName + "/" + file);
	                        if (aFile.isDirectory()) {
	                            if (null == latest || aFile.lastModified() > latest.lastModified()) {
	                                latest = aFile;
	                            }
	                        }
	                    }
	                    GlobalLogger.instance().getApplicationLogger().info(Topic.CORE, "Opening latest report: ", latest.toURI().resolve("_report.html"));
	                    ReportGenerator.showReport(latest.toURI().resolve("_report.html"));	                    
                    }                }
                catch (Exception e) {
                    GlobalLogger.instance().getApplicationLogger().error(Topic.CORE, e, "Error occured while opening latest report");
                    e.printStackTrace();
                }
            }
        });
    }

    public void generateTestplan() {
        String nameTestCase;
        String description;
        PrintWriter pw = null;
        Boolean statFailed;
        Boolean statComplete;
        String etatTestcase;
        try {
            File csvFile = new File("../logs/testPlan.csv");
            pw = new PrintWriter(new FileWriter(csvFile, true));
            if (csvFile.length() == 0) {
                pw.println("Name" + ";" + "Description" + ";" + "Test Status");
            }
            if (_root.attributeValue("description") != null) {
                pw.println(_root.attributeValue("name") + ";" + _root.attributeValue("description"));
            }
            else {
                pw.println(_root.attributeValue("name") + ";" + "");
            }
            List<Element> elementsTestcase = _root.elements("testcase");
            for (Element element : elementsTestcase) {
                nameTestCase = element.attributeValue("name");
                description = element.attributeValue("description");
                statFailed = StatPool.getInstance().exists(new StatKey(StatPool.PREFIX_TESTCASE, element.attributeValue("name"), "_failedNumber"));
                statComplete = StatPool.getInstance().exists(new StatKey(StatPool.PREFIX_TESTCASE, element.attributeValue("name"), "_completeNumber"));
                if (statComplete) {
                    if (statFailed) {
                        etatTestcase = "Failed";
                    }
                    else {
                        etatTestcase = "OK";
                    }
                }
                else {
                    etatTestcase = "?";
                }
                if (description == null) {
                    description = "";
                }
                pw.println("            " + nameTestCase + ";" + description + ";" + etatTestcase);
            }
            pw.flush();
            pw.close();
        }
        catch (IOException ex) {
            GlobalLogger.instance().getApplicationLogger().error(Topic.CORE, ex, "error generate Test plan " );
            pw.close();
        }
    }
}
