/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.master.master.utils;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.ThreadPool;
import com.devoteam.srit.xmlloader.core.XMLDocumentCache;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.operations.basic.OperationParameter;
import com.devoteam.srit.xmlloader.core.report.ReportGenerator;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.URIRegistry;
import com.devoteam.srit.xmlloader.core.utils.XMLDocument;
import com.devoteam.srit.xmlloader.core.utils.XMLElementDefaultParser;
import com.devoteam.srit.xmlloader.core.utils.XMLTree;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.DefaultElementInterface;

/**
 *
 * @author gpasquiers
 */
public class DataMaster {

    private Element root;
    private XMLDocument xmlDocument;
    private String name;
    private LinkedList<DataTest> testDatas;

    public DataMaster(XMLDocument xmlDocument, URI path) throws Exception {
        URIRegistry.IMSLOADER_TEST_HOME = path;
        this.xmlDocument = xmlDocument;
        this.root = xmlDocument.getDocument().getRootElement();
        this.name = root.attributeValue("name");

        XMLDocumentCache.reset();
        Runner runner = new Runner(name);
        runner.setParameterPool(new ParameterPool(runner, ParameterPool.Level.standalone, null));
        preparse(runner);
        parse();
    }

    private void preparse(Runner runner) throws Exception {
        List<Element> elementsParameter = (List<Element>) root.elements("parameter");
        for (Element element : elementsParameter) {
            OperationParameter operationParameter = new OperationParameter(element);
            operationParameter.executeAndStat(runner);
        }


        List<Element> elementsTest = (List<Element>) root.elements("test");
        for (Element test : elementsTest) {
            XMLElementDefaultParser xmlElementDefaultParser;
            xmlElementDefaultParser = new XMLElementDefaultParser(runner.getParameterPool());

            XMLTree xmlTree;
            xmlTree = new XMLTree(test);
            xmlTree.compute(Parameter.EXPRESSION, false);
            xmlTree.replace(xmlElementDefaultParser);
            DefaultElementInterface.insertNode((DefaultElement) test.getParent(), test, xmlTree.getTreeRoot());
            root.remove(test);
        }
    }

    private void parse() throws Exception {
        this.testDatas = new LinkedList<DataTest>();

        List<Element> parametersMaster = (List<Element>) root.elements("parameter");

        /* Duplicate the parameter elements from the <master> level and copy
         * them at the begining of the <test> element, before the <test>
         * element's own parameter elements.
         */
        List<Element> tests = (List<Element>) root.elements("test");
        for (Element test : tests) {
            List<Element> parametersTest = (List<Element>) test.elements("parameter");
            for (Element parameter : parametersTest) {
                test.remove(parameter);
            }

            for (Element parameter : parametersMaster) {
                test.add(parameter.createCopy());
            }

            for (Element parameter : parametersTest) {
                test.add(parameter);
            }

            testDatas.add(new DataTest(test, this.xmlDocument.getXMLFile()));
        }

        //
        // Test for TestData names uniqueness
        HashSet hashSet = new HashSet();
        for (DataTest testData : testDatas) {
            if (hashSet.contains(testData.getName())) {
                throw new Exception("Test name should be unique (" + testData.getName() + ").");
            }
            else {
                hashSet.add(testData.getName());
            }
        }
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

            @Override
            public void run() {
                try {
                    // find then open the latest report in the directory
                    String reportFileName = Config.getConfigByName("tester.properties").getString("stats.REPORT_DIRECTORY", "../reports/");
                    String[] files = new File(reportFileName).list();
                    File latest = null;
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
                }
                catch (Exception e) {
                    GlobalLogger.instance().getApplicationLogger().error(Topic.CORE, e, "Error occured while opening latest report");
                    e.printStackTrace();
                }
            }
        });
    }

    public LinkedList<DataTest> getDataTests() {
        return this.testDatas;
    }

    public String attributeValue(String name) {
        return root.attributeValue(name);
    }

    public XMLDocument getXMLDocument() {
        return this.xmlDocument;
    }

    public String getName() {
        return name;
    }
}
