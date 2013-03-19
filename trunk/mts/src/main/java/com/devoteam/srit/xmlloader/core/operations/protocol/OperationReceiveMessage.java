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
package com.devoteam.srit.xmlloader.core.operations.protocol;

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.Scenario;
import com.devoteam.srit.xmlloader.core.ScenarioRunner;
import com.devoteam.srit.xmlloader.core.exception.AssertException;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.Operation;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.XMLElementDefaultParser;
import com.devoteam.srit.xmlloader.core.utils.XMLTree;
import com.devoteam.srit.xmlloader.ethernet.StackEthernet;

import java.util.LinkedList;
import java.util.List;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.DefaultElementInterface;

/**
 *
 * @author gpasquiers
 */
public class OperationReceiveMessage extends Operation {

    /**
     * Parent scenario
     */
    private Scenario scenario;
    private String protocol;
    private boolean failedOnTimeout;
    private boolean failedOnReceive;
    /**
     * List of filters
     */
    private List<Operation> operations;

    /**
     * Creates a new instance of ReceiveMsgOperation
     */
    public OperationReceiveMessage(String protocol, Element rootNode, Scenario aScenario) throws Exception {
        super(rootNode, XMLElementDefaultParser.instance(), false);
        this.protocol = protocol;
        operations = new LinkedList<Operation>();
        scenario = aScenario;

        //part for deprecated grammar
        String connexion = rootNode.attributeValue("connexionName");
        if (null != connexion) {
            addParameterTestTag(rootNode, "string.equals", "channel.name", connexion);
        }
        //part for deprecated grammar

        String request = rootNode.attributeValue("request");
        String listenpoint = rootNode.attributeValue("listenpoint");
        String channel = rootNode.attributeValue("channel");
        String type = rootNode.attributeValue("type");
        String result = rootNode.attributeValue("result");
        String probe = rootNode.attributeValue("probe");
        
        
        //for DIAMETER Protocol
        /*
         * if ((protocol == StackFactory.PROTOCOL_DIAMETER) && (type != null) && (!Utils.isInteger(type))) { // use ApplicationID "base" but will search in all Applications anyway
         * com.devoteam.srit.xmlloader.diameter.dictionary.CommandDef commandDef = com.devoteam.srit.xmlloader.diameter.dictionary.Dictionary.getInstance().getCommandDefByName(type, "base"); if (null
         * != commandDef) { type = Integer.toString(commandDef.get_code()); } }
         */
        if (null != protocol) {
            addParameterTestTag(rootNode, "string.startsWith", "message.protocol", protocol);
        }
        if (null != request) {
            addParameterTestTag(rootNode, "string.equals", "message.request", new Boolean(request).toString());
        }

        if (null != channel) {
            addParameterTestTag(rootNode, "string.equals", "channel.name", channel);
        }

        if (null != listenpoint) {
            addParameterTestTag(rootNode, "string.equals", "listenpoint.name", listenpoint);
        }

        if (null != type) {
            addParameterTestTag(rootNode, "string.contains", "message.typeComparison", ":" + type + ":");
        }

        if (null != result) {
            addParameterTestTag(rootNode, "string.contains", "message.resultComparison", result);
        }
        
        if (null != probe) {
        	addParameterTestTag(rootNode, "string.equals", "probe.name", probe);
        }
        
        parse(rootNode);
    }

    /**
     * Executes the operation (retrieve and check message)
     */
    public Operation execute(Runner aRunner) throws Exception {    	
        //for ETHERNET Protocol -- Start listening to wire right now
    	//                         getting only one ethernet frame according to capture filter
    	//                         set by user in xml scenario file        
    	ScenarioRunner runner = (ScenarioRunner) aRunner;

        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.PROTOCOL, this);

        String failedOnTimeoutStr;
        String failedOnReceiveStr;
        String timeoutStr;

        try {
            lockAndReplace(runner);
            GlobalLogger.instance().getSessionLogger().debug(runner, TextEvent.Topic.PROTOCOL, "Operation after pre-parsing \n", this);
            failedOnTimeoutStr = getAttribute("failedOnTimeout");
            failedOnReceiveStr = getAttribute("failedOnReceive");
            timeoutStr = getAttribute("timeout");
        }
        finally {
            unlockAndRestore();
        }

        if (null == failedOnTimeoutStr) {
            failedOnTimeoutStr = "true";
        }

        if (failedOnTimeoutStr.equalsIgnoreCase("false")) {
            this.failedOnTimeout = false;
        }
        else if (failedOnTimeoutStr.equalsIgnoreCase("true")) {
            this.failedOnTimeout = true;
        }
        else {
            throw new ExecutionException("failedOnTimeout (" + failedOnTimeoutStr + ") should be a boolean");
        }

        if (null == failedOnReceiveStr) {
            failedOnReceiveStr = "false";
        }

        if (failedOnReceiveStr.equalsIgnoreCase("false")) {
            this.failedOnReceive = false;
        }
        else if (failedOnReceiveStr.equalsIgnoreCase("true")) {
            this.failedOnReceive = true;
        }
        else {
            throw new ExecutionException("failedOnReceive (" + failedOnReceiveStr + ") should be a boolean");
        }


        // instantiate the factory
        if (null != protocol) {
            StackFactory.getStack(protocol);
        }
        else {
            StackFactory.getAllStacks();
        }

        Msg msg;

        long timeout;
        if (timeoutStr != null) {
            timeout = (long) (Float.parseFloat(timeoutStr) * 1000);
        }
        else if (protocol != null) {
            timeout = StackFactory.getStack(protocol).receiveTimeout;
        }
        else {
            timeout = (long) (Config.getConfigByName("tester.properties").getDouble("operations.RECEIVE_TIMEOUT", 30) * 1000);
        }

        if (timeout <= 0) {
            timeout = Long.MAX_VALUE;
        }

        long timestamp = System.currentTimeMillis();

        Exception error = null;
        try {
            while (null != (msg = runner.getBufferMsg().readMessageFromStack(timeout - (System.currentTimeMillis() - timestamp)))) {
                GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.PROTOCOL, "Testing message\n", msg);
                // set the implicit message into the runner (for setFromMessage operation)
                runner.setCurrentMsg(msg);

                boolean isValid = true;
                try {
                    for (Operation o : operations) {
                        o.executeAndStat(runner);
                    }
                }
                catch (AssertException e) {
                    GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.PROTOCOL, "</test> (KO)\n", e.getMessage());
                    isValid = false;
                }

                if (isValid) {
                    runner.getBufferMsg().removeMsgFromStack(msg);
                    break;
                }
                else {
                    GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.PROTOCOL, "The message does not match the conditions");
                }
            }
        }
        catch (Exception e) {
            GlobalLogger.instance().getSessionLogger().error(runner, TextEvent.Topic.PROTOCOL, e, "Error in <", this.getName(), ">");
            throw new ExecutionException("Error in OperationReceiveMessage", e);
        }
        finally {
            runner.getBufferMsg().resetMsgStackFlag();
        }

        if (this.failedOnReceive) {
            if (null == msg) {
                GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.PROTOCOL, "<", this.getName(), "> OK (no message)");
            }
            else {
                GlobalLogger.instance().getSessionLogger().error(runner, TextEvent.Topic.PROTOCOL, "<", this.getName(), " KO : ", msg.getProtocol(), " message ", msg);
                throw new ExecutionException("Received a valid message (failedOnReceive=true)" + this, error);
            }
        }
        else if (this.failedOnTimeout) {
            if (null == msg) {
                GlobalLogger.instance().getSessionLogger().error(runner, TextEvent.Topic.PROTOCOL, "<", this.getName(), "> TIMEOUT (no message)");
                throw new ExecutionException("Error occured while waiting for a Msg (failedOnTimeout=true)\n" + this, error);
            }
            else {
                GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.PROTOCOL, "<", this.getName(), "> OK : ", msg.getProtocol(), " message ", msg.toShortString());
                GlobalLogger.instance().getSessionLogger().debug(runner, TextEvent.Topic.PROTOCOL, "<", this.getName(), "> OK : ", msg.getProtocol(), " message ", msg);
            }
        }
        else {
            if (null == msg) {
                GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.PROTOCOL, "<", this.getName(), " TIMEOUT (no message)\n", this);
            }
            else {
                GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.PROTOCOL, "<", this.getName(), "> OK : ", msg.getProtocol(), " message ", msg);
            }
        }

        return null;
    }

    /**
     * Parses the operation from a root element
     */
    private void addParameterTestTag(Element root, String operation, String path, String value) throws Exception {
        DefaultElement filter;

        filter = new DefaultElement("parameter");
        filter.addAttribute("name", "[reserved_param]");
        filter.addAttribute("operation", "protocol.setFromMessage");
        filter.addAttribute("value", path);

        DefaultElementInterface.addNode((DefaultElement) root, filter, 0);

        filter = new DefaultElement("test");
        filter.addAttribute("parameter", "[reserved_param]");
        filter.addAttribute("condition", operation);
        filter.addAttribute("value", value);

        DefaultElementInterface.addNode((DefaultElement) root, filter, 1);
    }

    /**
     * Parses the operation from a root element
     */
    private void parse(Element root) throws Exception {
        List<Element> list = root.elements();

        for (Element element : list) {
            operations.add(scenario.parseOperation(element));
        }
    }
}
