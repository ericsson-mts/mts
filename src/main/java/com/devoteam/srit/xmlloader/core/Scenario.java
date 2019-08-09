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

import com.devoteam.srit.xmlloader.core.exception.ExitExecutionException;
import com.devoteam.srit.xmlloader.core.exception.GotoExecutionException;
import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.operations.Operation;
import com.devoteam.srit.xmlloader.core.operations.basic.*;
import com.devoteam.srit.xmlloader.core.operations.protocol.*;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.URIFactory;
import com.devoteam.srit.xmlloader.core.utils.URIRegistry;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLDocument;
import com.devoteam.srit.xmlloader.diameter.dictionary.CommandDef;
import com.devoteam.srit.xmlloader.diameter.dictionary.Dictionary;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.List;

import org.dom4j.Element;

/**
 * Scenario to be played
 *
 * @author JM. Auffret
 */
public class Scenario implements Serializable {

    private OperationSequence _operationSequenceScenario;
    private OperationSequence _operationSequenceFinally;

    public Scenario(XMLDocument xmlDocument) throws Exception {
        _operationSequenceScenario = null;
        _operationSequenceFinally = null;
        parse(xmlDocument);
    }

    public void executeScenario(ScenarioRunner runner) throws Exception {
        try {
            GlobalLogger.instance().getSessionLogger().info(runner, Topic.CORE, "<scenario>");
            this._operationSequenceScenario.execute(runner);
            GlobalLogger.instance().getSessionLogger().info(runner, Topic.CORE, "</scenario>");
        }
        catch (ExitExecutionException e) {
            if (e.getFailed()) {
                throw e;
            }
        }
        catch (GotoExecutionException e) {
            throw new Exception("Could not find label.", e);
        }
    }

    public void executeFinally(ScenarioRunner runner) throws Exception {
        if (null != this._operationSequenceFinally) {
            try {
                runner.finallyEnter();
                GlobalLogger.instance().getSessionLogger().info(runner, Topic.CORE, "<finally>");
                this._operationSequenceFinally.execute(runner);
                GlobalLogger.instance().getSessionLogger().info(runner, Topic.CORE, "</finally>");
            }
            catch (ExitExecutionException e) {
                if (e.getFailed()) {
                    throw e;
                }
            }
            catch (GotoExecutionException e) {
                throw new Exception("Could not find label.", e);
            }
            finally {
                runner.finallyExit();
            }
        }
    }

    public void free() {
        _operationSequenceFinally = null;
        _operationSequenceScenario = null;
    }

    /**
     * Parse the scenario
     */
    private void parse(XMLDocument scenarioDocument) throws Exception {
        //String relativePath = getFilename();

        //XMLDocument scenarioDocument = XMLDocumentCache.getXMLDocument(URIRegistry.MTS_TEST_HOME.resolve(relativePath), URIFactory.newURI("../conf/schemas/scenario.xsd"));

        Element root = scenarioDocument.getDocument().getRootElement();


        /**
         * Check the position of the finally tag
         */
        boolean isFinallyLast = false;
        int finallyInstances = 0;
        List<Element> elements = root.elements();
        for (Element element : elements) {
            isFinallyLast = false;
            if (element.getName().equals("finally")) {
                isFinallyLast = true;
                finallyInstances++;
            }
        }

        if (finallyInstances == 1 && !isFinallyLast) {
            throw new ParsingException("Finally must be the last operation of the scenario.");
        }
        else if (finallyInstances > 1) {
            throw new ParsingException("There must be at most one finally operation.");
        }

        /**
         * Create the finally operations sequence. If there is a finally then the sequence is created and then the finally tag is removed from the scenario. To allow the creation of the scenario
         * operations sequence.
         */
        if (finallyInstances == 1) {
            Element finallyRoot = root.element("finally");
            root.remove(finallyRoot);
            this._operationSequenceFinally = new OperationSequence(finallyRoot, this);
        }

        _operationSequenceScenario = new OperationSequence(root, this);
    }

    /**
     * Parse an operation
     */
    public Operation parseOperation(Element root) throws Exception {
        Operation ope = null;
        String rootName = root.getName();

        if (rootName.equals("receiveMessage")) {
            ope = new OperationReceiveMessage(null, root, this);
        }
        //------------------------------------------------------------------------ DIAMETER operations -
        // DEPRECATED begin
        else if (rootName.equals("sendAnswerAAA")) {
            ope = new OperationSendMsg(StackFactory.PROTOCOL_DIAMETER, false, root);
        }
        else if (rootName.equals("sendRequestAAA")) {
            ope = new OperationSendMsg(StackFactory.PROTOCOL_DIAMETER, true, root);
        }
        else if (rootName.equals("receiveAnswerAAA")) {
            ope = parseReceiveAAA(StackFactory.PROTOCOL_DIAMETER, false, root);
        }
        else if (rootName.equals("receiveRequestAAA")) {
            ope = parseReceiveAAA(StackFactory.PROTOCOL_DIAMETER, true, root);
        }
        // DEPRECATED end
        //------------------------------------------------------------------------ SIP operations -
        // DEPRECATED begin
        else if (rootName.equals("openProviderSIP")) {
            ope = new OperationCreateListenpoint(StackFactory.PROTOCOL_SIP, root);
        }
        else if (rootName.equals("closeProviderSIP")) {
            ope = new OperationRemoveListenpoint(StackFactory.PROTOCOL_SIP, root);
        }
        else if (rootName.equals("sendResponseSIP")) {
            ope = new OperationSendMsg(StackFactory.PROTOCOL_SIP, false, root);
        }
        else if (rootName.equals("sendRequestSIP")) {
            ope = new OperationSendMsg(StackFactory.PROTOCOL_SIP, true, root);
        }
        else if (rootName.equals("receiveResponseSIP")) {
            ope = parseReceiveSIP(StackFactory.PROTOCOL_SIP, false, root);
        }
        else if (rootName.equals("receiveRequestSIP")) {
            ope = parseReceiveSIP(StackFactory.PROTOCOL_SIP, true, root);
        }
        // DEPRECATED end
        //------------------------------------------------------------------------ http operations -
        // DEPRECATED begin
        else if (rootName.equals("openConnectionHTTP")) {
            ope = new OperationOpenChannel(StackFactory.PROTOCOL_HTTP, root);
        }
        else if (rootName.equals("closeConnectionHTTP")) {
            ope = new OperationCloseChannel(StackFactory.PROTOCOL_HTTP, root);
        }
        else if (rootName.equals("sendResponseHTTP")) {
            ope = new OperationSendMsg(StackFactory.PROTOCOL_HTTP, false, root);
        }
        else if (rootName.equals("sendRequestHTTP")) {
            ope = new OperationSendMsg(StackFactory.PROTOCOL_HTTP, true, root);
        }
        else if (rootName.equals("receiveResponseHTTP")) {
            ope = parseReceiveHTTP(StackFactory.PROTOCOL_HTTP, false, root);
        }
        else if (rootName.equals("receiveRequestHTTP")) {
            ope = parseReceiveHTTP(StackFactory.PROTOCOL_HTTP, true, root);
        }
        // DEPRECATED end
        //------------------------------------------------------------------------ radius operations -
        // DEPRECATED begin //
        else if (rootName.equals("openSocketRadius")) {
            ope = new OperationOpenChannel(StackFactory.PROTOCOL_RADIUS, root);
        }
        else if (rootName.equals("closeSocketRadius")) {
            ope = new OperationCloseChannel(StackFactory.PROTOCOL_RADIUS, root);
        }
        else if (rootName.equals("sendResponseRadius")) {
            ope = new OperationSendMsg(StackFactory.PROTOCOL_RADIUS, false, root);
        }
        else if (rootName.equals("sendRequestRadius")) {
            ope = new OperationSendMsg(StackFactory.PROTOCOL_RADIUS, true, root);
        }
        else if (rootName.equals("receiveResponseRadius")) {
            ope = parseReceiveRadius(StackFactory.PROTOCOL_RADIUS, false, root);
        }
        else if (rootName.equals("receiveRequestRadius")) {
            ope = parseReceiveRadius(StackFactory.PROTOCOL_RADIUS, true, root);
        }
        // DEPRECATED end //
        //--------------------------------------------------------------------------------- RTP -        
        // DEPRECATED begin //
        else if (rootName.equals("openConnectionRTP")) {
            ope = new OperationOpenChannel(StackFactory.PROTOCOL_RTP, root);
        }
        else if (rootName.equals("closeConnectionRTP")) {
            ope = new OperationCloseChannel(StackFactory.PROTOCOL_RTP, root);
        }
        else if (rootName.equals("sendPacketRTP")) {
            ope = new OperationSendMsg(StackFactory.PROTOCOL_RTP, false, root);
        }
        else if (rootName.equals("receivePacketRTP")) {
            ope = parseReceiveRTP(StackFactory.PROTOCOL_RTP, root);
        }
        // DEPRECATED end //
        //--------------------------------------------------------------------------------- core
        else if (rootName.equals("label")) 
        {
            ope = new OperationLabel(root);
        }
        else if (rootName.equals("exit")) 
        {
            ope = new OperationExit(root);
        }
        else if (rootName.equals("parameter")) 
        {
            ope = new OperationParameter(root);
        }
        else if (rootName.equals("pause")) 
        {
            ope = new OperationPause(root);
        }
        else if (rootName.equals("semaphore")) 
        {
            ope = new OperationSemaphore(root);
        }
        else if (rootName.equals("goto")) 
        {
            ope = new OperationGoto(root);
        }
        else if (rootName.equals("if")) 
        {
            ope = new OperationIf(root, this);
        }
        else if (rootName.equals("switch")) 
        {
            ope = new OperationSwitch(root, this);
        }
        else if (rootName.equals("while")) 
        {
            ope = new OperationWhile(root, this);
        }
        else if (rootName.equals("try")) 
        {
            ope = new OperationTry(root, this);
        }
        else if (rootName.equals("system")) 
        {
            ope = new OperationSystem(root);
        }
        else if (rootName.equals("groovy")) 
        {
            ope = new OperationGroovy(root);
        }
        else if (rootName.equals("test")) 
        {
            ope = new OperationTest(root);
        }
        else if (rootName.equals("and")) 
        {
            ope = new OperationTestAnd(root, this);
        }
        else if (rootName.equals("not")) 
        {
            ope = new OperationTestNot(root, this);
        }
        else if (rootName.equals("or")) 
        {
            ope = new OperationTestOr(root, this);
        }
        else if (rootName.equals("log")) 
        {
            ope = new OperationLog(root);
        }
        else if (rootName.equals("stats")) 
        {
            ope = new OperationStats(root);
        }
        else if (rootName.equals("for")) 
        {
            ope = new OperationFor(root, this);
        }
        else if (rootName.equals("call")) 
        {
            ope = new OperationCall(root);
        }
        else if (rootName.equals("function")) 
        {
            ope = new OperationFunction(root);
        }
        else 
        {
        	ope = getClassFromProtocol("sendMessage", rootName, root);
        	if (ope != null)
        	{
        		return ope;
        	}
        	ope = getClassFromProtocol("receiveMessage", rootName, root);
        	if (ope != null)
        	{
        		return ope;
        	}
        	ope = getClassFromProtocol("createListenpoint", rootName, root);
        	if (ope != null)
        	{
        		return ope;
        	}
        	ope = getClassFromProtocol("removeListenpoint", rootName, root);
        	if (ope != null)
        	{
        		return ope;
        	}
        	ope = getClassFromProtocol("openChannel", rootName, root);
        	if (ope != null)
        	{
        		return ope;
        	}
        	ope = getClassFromProtocol("closeChannel", rootName, root);
        	if (ope != null)
        	{
        		return ope;
        	}
        	ope = getClassFromProtocol("createProbe", rootName, root);
        	if (ope != null)
        	{
        		return ope;
        	}
        	ope = getClassFromProtocol("removeProbe", rootName, root);
        	if (ope != null)
        	{
        		return ope;
        	}
        	throw new ParsingException("Unknown operation " + rootName);       
        }

        return ope;
    }

    /** Get the operation object for the type (sendMessage, receiveMessage, createListenpoint,
     * removeListenpoint, openChannel, closeChannel, createProbe, removeProbe) depending on the 
     * root name XML element
     */
    public Operation getClassFromProtocol(String type, String rootName, Element root) throws Exception
    {
    	if (rootName.startsWith(type))
    	{
    		int length = type.length();
			String rootNameFirstUpper = rootName.substring(0, length);
			rootNameFirstUpper = rootNameFirstUpper.substring(0, 1).toUpperCase() + rootNameFirstUpper.substring(1); 
			String operationClassname = StackFactory.ROOT_PACKAGE + ".core.operations.protocol.Operation" + rootNameFirstUpper;
			String protocol = rootName.substring(length);
			Class<?> classObject = ClassLoader.getSystemClassLoader().loadClass(operationClassname);
			Constructor<?> constr = null;
			if ("receiveMessage".startsWith(type))
			{
				constr = classObject.getConstructor(String.class, Element.class, Scenario.class);
				return (Operation) constr.newInstance(protocol, root, this);
			}
			else
			{
				constr = classObject.getConstructor(String.class, Element.class);
				return (Operation) constr.newInstance(protocol, root);
			}
    	}
    	return null;
    }
    
    /**
     * Parse a ReceiveXXXXXAAA operation
     */
    @Deprecated
    private Operation parseReceiveAAA(String protocol, boolean request, Element node) throws Exception {
        String type = node.attributeValue("command");
        // go read the value of the command in dictionnary
        if ((type != null) && (!Utils.isInteger(type))) {
            // use ApplicationID "base" but will search in all Applications anyway
            CommandDef commandDef = Dictionary.getInstance().getCommandDefByName(type, "0");
            if (null != commandDef) {
                type = Integer.toString(commandDef.get_code());
            }
        }
        String result = node.attributeValue("result");

        return new OperationReceiveMsg(StackFactory.PROTOCOL_DIAMETER, request, null, null, type, result, node, this);
    }

    /**
     * Parse a ReceiveXXXXXAAA operation
     */
    @Deprecated
    private Operation parseReceiveRadius(String protocol, boolean request, Element node) throws Exception {
        String type = node.attributeValue("type");

        String result = node.attributeValue("result");

        String channel = node.attributeValue("socketName");

        return new OperationReceiveMsg(StackFactory.PROTOCOL_RADIUS, request, channel, null, type, result, node, this);
    }

    /**
     * Parse a ReceiveXXXXXSIP operation
     */
    @Deprecated
    private Operation parseReceiveSIP(String protocol, boolean request, Element node) throws Exception {
        String provider = node.attributeValue("providerName");
        String type = node.attributeValue("method");
        String result = node.attributeValue("result");

        return new OperationReceiveMsg(StackFactory.PROTOCOL_SIP, request, null, provider, type, result, node, this);
    }

    /**
     * Parse a ReceiveXXXXXHTTP operation
     */
    @Deprecated
    private Operation parseReceiveHTTP(String protocol, boolean request, Element node) throws Exception {
        String type = node.attributeValue("method");
        String result = node.attributeValue("result");

        return new OperationReceiveMsg(StackFactory.PROTOCOL_HTTP, request, null, null, type, result, node, this);
    }

    /**
     * Parse a ReceivePacketRTP operation
     */
    @Deprecated
    private Operation parseReceiveRTP(String protocol, Element node) throws Exception {
        String channel = node.attributeValue("sessionName");
        String type = node.attributeValue("payloadType");

        return new OperationReceiveMsg(StackFactory.PROTOCOL_RTP, true, channel, null, type, null, node, this);
    }

    /**
     * Parse a ReceivePacketTCP operation
     */
    @Deprecated
    private Operation parseReceiveTCP(String protocol, Element node) throws Exception {
        String channel = node.attributeValue("connexionName");
        return new OperationReceiveMsg(StackFactory.PROTOCOL_TCP, true, channel, null, null, null, node, this);
    }

    /**
     * Parse a ReceivePacketSMTP operation
     */
    @Deprecated
    private Operation parseReceiveSMTP(String protocol, boolean request, Element node) throws Exception {
        String channel = node.attributeValue("sessionName");
        String type = node.attributeValue("commandName");
        String result = node.attributeValue("replyCode");
        return new OperationReceiveMsg(StackFactory.PROTOCOL_SMTP, request, channel, null, type, result, node, this);
    }

    /**
     * Parse a ReceivePacketMGCP operation
     */
    @Deprecated
    private Operation parseReceiveMGCP(String protocol, boolean request, Element node) throws Exception {
        String channel = node.attributeValue("sessionName");
        String type = node.attributeValue("commandName");
        String result = node.attributeValue("replyCode");
        return new OperationReceiveMsg(StackFactory.PROTOCOL_MGCP, request, channel, null, type, result, node, this);
    }

    @Deprecated
    private Operation parseReceiveSTUN(String protocol, boolean request, Element node) throws Exception {
        String channel = node.attributeValue("sessionName");
        String type = node.attributeValue("commandName");
        String result = node.attributeValue("replyCode");
        return new OperationReceiveMsg(StackFactory.PROTOCOL_STUN, request, channel, null, type, result, node, this);
    }

    @Deprecated
    private Operation parseReceiveH225CS(String protocol, boolean request, Element node) throws Exception {
        String channel = node.attributeValue("sessionName");
        String type = node.attributeValue("commandName");
        String result = node.attributeValue("replyCode");
        return new OperationReceiveMsg(StackFactory.PROTOCOL_H225CS, request, channel, null, type, result, node, this);
    }

    /**
     * Parse a ReceivePacketUDP operation
     */
    @Deprecated
    private Operation parseReceiveUDP(String protocol, Element node) throws Exception {
        String channel = node.attributeValue("connexionName");
        return new OperationReceiveMsg(StackFactory.PROTOCOL_UDP, true, channel, null, null, null, node, this);
    }

    /**
     * Parse a ReceivePacketSCTP operation
     */
    @Deprecated
    private Operation parseReceiveSCTP(String protocol, Element node) throws Exception {
        String channel = node.attributeValue("connexionName");
        return new OperationReceiveMsg(StackFactory.PROTOCOL_SCTP, true, channel, null, null, null, node, this);
    }
}
