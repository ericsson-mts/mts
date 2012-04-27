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

package com.devoteam.srit.xmlloader.genscript;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Main class of genscript module
 * @author bthou
 */
public class ScriptGenerator {

    File fileRoot;
    File pathRoot;
    String testcaseName = "000_generate";
    Test test;
    TestCase testcase;
    Scenario scenario;
    List<FiltreGenerator> listeFiltre;
    FiltreGenerator mainFiltre;

    // Getters et Setters
    public String getTestcaseName() {
        return testcaseName;
    }

    public void setTestcaseName(String tcn) {
        testcaseName = tcn;
    }
    // ------------------

    // Constructeur du générateur de scripts
    public ScriptGenerator(URI out) {
        fileRoot = new File(out);
        pathRoot = fileRoot.getParentFile();
        listeFiltre = new ArrayList<FiltreGenerator>();
    }

    // Method for generate all object for starting a generation
    public void generateTest() throws Exception {

        // Si le fichier n'existe pas déjà
        if (!fileRoot.exists()) {
            test = new Test("Genscript", "Script converted from capture");
        }
        // Si le fichier de sortie existe déjà
        else {
            // On ouvre le fichier existant
            File xml = fileRoot;
            Document testDoc;
            SAXReader reader = new SAXReader();
            testDoc = reader.read(xml);
            Element testExistant = testDoc.getRootElement();

            // On génére un nouvel objet test à partir de l'élément existant dans le fichier
            test = new Test(testExistant.attributeValue("name"), testExistant.attributeValue("description"));
            test.setTest(testExistant);
        }

        // On tente de récupérer le testcase à générer
        Element testcaseExistant = null;
        for (Iterator i = test.getTest().elements().iterator(); i.hasNext();) {
            Element elem = (Element) i.next();
            if (elem.getName().equals("testcase") && elem.attribute("name").getText().equals(testcaseName)) {
                testcaseExistant = elem;
            }
        }

        // On récupère les paramètres existants
        for (Iterator i = test.getTest().elements().iterator(); i.hasNext();) {
            Element elem = (Element) i.next();
            if (elem.getName().equals("parameter")) {
                Param p = new Param(elem.attributeValue("name"), "test", elem.attributeValue("operation") + "," + elem.attributeValue("value"), null, Param.TARGET_SENDCLIENT);
                p.setName(p.getFamily());
                p.setRemplacedValue(elem.attributeValue("value"));
                ParamGenerator.getInstance().recordParam(p);
            }
        }

        // Si le testcase n'existe pas encore
        if (testcaseExistant == null) {
            // On le créait
            testcase = new TestCase(testcaseName, "Testcase generate from capture", "true");
            test.getTest().add(testcase.getTestCase());
        }
        // Sinon on génére un testcase à partir de celui existant dans le fichier
        else {
            testcase = new TestCase(testcaseExistant.attributeValue("name"), testcaseExistant.attributeValue("description"), testcaseExistant.attributeValue("state"));
            testcase.setTestCase(testcaseExistant);

            // On récupère les paramètres existants
            for (Iterator i = testcase.getTestCase().elements().iterator(); i.hasNext();) {
                Element elem = (Element) i.next();
                if (elem.getName().equals("parameter")) {
                    Param p = new Param(elem.attributeValue("name"), "testcase", elem.attributeValue("operation") + "," + elem.attributeValue("value"), null, Param.TARGET_SENDCLIENT);
                    p.setName(p.getFamily());
                    p.setRemplacedValue(elem.attributeValue("value"));
                    ParamGenerator.getInstance().recordParam(p);
                }
            }
        }
        // On ajoute le testcase dans le test
        test.addTestCase(testcase);

        // On tente de récupérer le scenario
        Element scenarioExistant = null;
        // On enregistre les scenarios de ce testcase
        for (Iterator j = testcase.getTestCase().elements().iterator(); j.hasNext();) {
            Element elem = (Element) j.next();
            if (elem.getName().equals("scenario") && elem.getText().contains(listeFiltre.get(0).getHostPort().toString())) {
                scenarioExistant = elem;
            }
            else if (elem.getName().equals("scenario")) {
                Scenario sc = new Scenario(elem.attributeValue("name"), elem.getText(), listeFiltre);
                testcase.addScenario(sc);
            }
        }

        // Si le scenario n'existe pas encore
        if (scenarioExistant == null) {
            // On le créait
            scenario = new Scenario(getScenarioName(), getScenarioPath(), listeFiltre);
            testcase.getTestCase().add(scenario.toXmlElement());
        }
        else {
            scenario = new Scenario(scenarioExistant.attributeValue("name"), scenarioExistant.getText(), listeFiltre);
            scenario.setScenario(scenarioExistant);
        }

        // On ajoute ce scenario au testcase
        testcase.addScenario(scenario);

    }

    // Methode appelée à la capture d'un message à partir du fichier pcap
    public void generateMsg(Msg msg) throws Exception {
    	
    	if (StackFactory.PROTOCOL_DIAMETER.equalsIgnoreCase(msg.getProtocol()))
    	{
    		// case the msg is a CER/CEA DIAMETER message (Capabilities Exchange)
    		if (msg.getType().equals("257"))
    		{
    			return;
    		}
    		// case the msg is a DPR/DPA DIAMETER message (Disconnect Peer)
    		if (msg.getType().equals("282"))
    		{
    			return;
    		}
    		// case the msg is a DWR/DWA DIAMETER message (Device-Watchdog)
    		if (msg.getType().equals("280"))
    		{
    			return;
    		}
    	}
    	
        String srcIp = "";
        String dstIp = "";
        String srcPort = "";
        String dstPort = "";
        String protocole = "";
        String transport = "";
        String timestamp = "";
        String name = "";
        String type = null;
        String result = null;
        String request = null;

        Parameter protocoleParam = msg.getParameter("message.protocol");
        if (protocoleParam.length() > 0) {
            protocole = protocoleParam.get(0).toString();
        }

        Parameter transportParam = msg.getParameter("channel.transport");
        if (transportParam.length() > 0) {
            transport = transportParam.get(0).toString();
        }

        Parameter timestampParam = msg.getParameter("message.timestampCaptureFile");
        if (timestampParam.length() > 0) {
            timestamp = timestampParam.get(0).toString();
        }

        Parameter srcIpParam = msg.getParameter("channel.localHost");
        if (srcIpParam.length() > 0) {
            srcIp = srcIpParam.get(0).toString();
        }

        Parameter dstIpParam = msg.getParameter("channel.remoteHost");
        if (dstIpParam.length() > 0) {
            dstIp = dstIpParam.get(0).toString();
        }

        Parameter srcPortParam = msg.getParameter("channel.localPort");
        if (srcPortParam.length() > 0) {
            srcPort = srcPortParam.get(0).toString();
        }

        Parameter dstPortParam = msg.getParameter("channel.remotePort");
        if (dstPortParam.length() > 0) {
            dstPort = dstPortParam.get(0).toString();
        }

        Parameter typeParam = msg.getParameter("message.type");
        if (typeParam.length() > 0) {
            type = typeParam.get(0).toString();
            name += type + " ";
        }

        Parameter resultParam = msg.getParameter("message.result");
        if (resultParam.length() > 0) {
            result = resultParam.get(0).toString();
            name += result;
        }

        Parameter requestParam = msg.getParameter("message.request");
        if (requestParam.length() > 0) {
            request = requestParam.get(0).toString();
        }
               
        String msgTrans = transport + ":" + srcIp + ":" + srcPort + "->" + dstIp + ":" + dstPort;
        String msgSummary = msg.toShortString();
        int pos = msgSummary.indexOf("\n");
        if (pos > 0)
        {
        	msgSummary = msgSummary.substring(0, pos);
        }
        System.out.println(msgTrans + " => " + msgSummary);
        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, Stack.CAPTURE, msg.getProtocol(), " message :", msg.toShortString());        
        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, Stack.CAPTURE, msg.getProtocol(), " message :", msg);
        

        FiltreGenerator filtreMsgSrc = new FiltreGenerator(protocole, srcIp, Integer.parseInt(srcPort));
        FiltreGenerator filtreMsgDst = new FiltreGenerator(protocole, dstIp, Integer.parseInt(dstPort));

        Scenario scenarioSrc = null;
        Scenario scenarioDst = null;

        if (scenario.support(filtreMsgSrc) != null) {
            scenarioSrc = scenario;
        }
        if (scenario.support(filtreMsgDst) != null) {
            scenarioDst = scenario;
        }

        String xml = msg.toXml();
        String xmlCdata = xml;
        if (!xml.trim().startsWith("<"))
        {
        	xmlCdata = "<![CDATA[" + System.getProperty("line.separator");
        	xmlCdata = xmlCdata + xml;
        	xmlCdata += System.getProperty("line.separator") + "]]>";            
        }
        if (scenarioSrc != null) {
            Message message = new Message(scenario.support(filtreMsgSrc).getProtocole(), name);
            message.setSrcIp(srcIp);
            message.setSrcPort(srcPort);
            message.setDstIp(dstIp);
            message.setDstPort(dstPort);
            message.setMsg(xmlCdata);
            message.setMsgSrc(msg);
            message.setTimestamp(Long.parseLong(timestamp));
            message.setEnvoi(true);
            scenarioSrc.generateMsg(message);
            ParamGenerator.setLastMsg(message);
        }
        if (scenarioDst != null) {
            Message message = new Message(scenario.support(filtreMsgDst).getProtocole(), name);
            message.setSrcIp(srcIp);
            message.setSrcPort(srcPort);
            message.setDstIp(dstIp);
            message.setDstPort(dstPort);
            message.setMsg(xmlCdata);
            message.setMsgSrc(msg);
            message.setTimestamp(Long.parseLong(timestamp));
            if (type != null) {
                message.setType(type);
            }
            if (result != null) {
                message.setResult(result);
            }
            if (request != null) {
                message.setRequest(request);
            }
            ParamGenerator.setLastMsg(message);
            message.setEnvoi(false);
            scenarioDst.generateMsg(message);
        }
    }

    // Methode give scenario name
    private String getScenarioName() throws Exception {
        String scenarioName = "";
        for (FiltreGenerator f : listeFiltre) {
            if (!scenarioName.equals("")) {
                scenarioName += ",";
            }
            Param scenarioNameParam = new Param("[" + f.protocole + "LocalPort]", "test", "set", f.getHostPort().toString(), Param.TARGET_SENDCLIENT);
            scenarioNameParam.setRemplacedValue(f.getHostPort().toString());
            scenarioName += ParamGenerator.getInstance().recordParam(scenarioNameParam);
        }
        return scenarioName;
    }

    // Methode give scenario path
    private String getScenarioPath() {
        return testcaseName + "/" + mainFiltre.getHostPort().toString() + ".xml";
    }

    // Méthode pour ajouter un filtre
    public void addFiltre(FiltreGenerator f) {
        listeFiltre.add(f);
        if (mainFiltre == null) {
            mainFiltre = listeFiltre.get(0);
        }
    }

    // Methode pour la lancer la generation reelle des scripts
    public void closeTest() throws Exception {

        // GENERATION DU FICHIER DE SCENARIO
        // Définition du filtre principal
        FiltreGenerator filtre = listeFiltre.get(0);

        // Génération de l'arborescence du fichier de scénario
        URI scenarioURI = new URI(pathRoot.toURI().toString() + "/" + testcaseName);
        File scenarioPath = new File(scenarioURI);
        scenarioPath.mkdirs();
        File scenarioFile = new File(new URI(scenarioPath.toURI() + "/" + filtre.getHostPort() + ".xml"));
        System.out.println("Generate scenario file: " + testcaseName + "/" + filtre.getHostPort() + ".xml");
        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "Generate scenario file: " + testcaseName + "/" + filtre.getHostPort() + ".xml");
        
        String scenarioXml = scenario.toXml();
        generateXML(scenarioXml, scenarioFile);

        // GENERATION DU FICHIER DE TEST        
        // Creation de l'arborescence
        pathRoot.mkdirs();
        // On creait le fichier de test
        String testStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.getProperty("line.separator");
        testStr += test.toXmlElement().asXML();
        // Réalisation de l'indentation
        testStr = testStr.replaceAll("><", ">" + System.getProperty("line.separator") + "<");
        testStr = testStr.replaceAll(System.getProperty("line.separator") + "<testcase", System.getProperty("line.separator") + "  <testcase");
        testStr = testStr.replaceAll(System.getProperty("line.separator") + "<scenario", System.getProperty("line.separator") + "    <scenario");
        testStr = testStr.replaceAll(System.getProperty("line.separator") + "<parameter", System.getProperty("line.separator") + "  <parameter");
        testStr = testStr.replaceAll(System.getProperty("line.separator") + "  <scenario", System.getProperty("line.separator") + "    <scenario");
        testStr = testStr.replaceAll(System.getProperty("line.separator") + "</testcase>", System.getProperty("line.separator") + "  </testcase>");
        // Génération du fichier
        generateXML(testStr, fileRoot);
    }

    // Methode statique qui génére un fichier XML à partir d'un string
    public static void generateXML(String src, File fichier) {
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(fichier)));
            pw.print(src);
            pw.close();

        }
        catch (Exception e) {
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, e, "Error while generating an XML file from a string");
            e.printStackTrace();
            System.exit(-30);
        }
    }
}
