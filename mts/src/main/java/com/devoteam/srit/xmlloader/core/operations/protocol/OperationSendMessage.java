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

import com.devoteam.srit.xmlloader.diameter.MsgDiameterParser;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.ScenarioRunner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.Operation;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Probe;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;


import com.devoteam.srit.xmlloader.core.protocol.TransactionId;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.utils.Config;

import org.dom4j.Element;

/**
 * TODO : statistics; request or answer ;
 *
 * @author gpasquiers
 */
public class OperationSendMessage extends Operation {

    private String protocol;

    /**
     * Creates a new instance
     */
    public OperationSendMessage(String aProtocol, Element rootNode) throws Exception {
        super(rootNode, null);
        protocol = aProtocol;
    }

    /**
     * Executes the operation
     */
    public Operation execute(Runner aRunner) throws Exception {
    	Stack stack = StackFactory.getStack(protocol);

    	setReplacer(stack.getElementReplacer());

        ScenarioRunner runner = (ScenarioRunner) aRunner;

        GlobalLogger.instance().getSessionLogger().info(aRunner, TextEvent.Topic.PROTOCOL, this);

        String request;
        String listenpointName;
        String channelName;
        String remoteHost;
        String remotePort;
        String remoteUrl;
        String transport;
        String transactionIdAttribute;
        String destScenarioAttribute;
        String strDelay;
        String answerHandlerAttribute;
        String probe;
        Msg msg;
        Boolean req = null;

        try {
            lockAndReplace(runner);
            GlobalLogger.instance().getSessionLogger().debug(runner, TextEvent.Topic.PROTOCOL, "Operation after pre-parsing \n", this);
            Element root = getRootElement();
            // deprecated 
            
            request = getAttribute("request");
            listenpointName = getAttribute("listenpoint");
            channelName = getAttribute("channel");
            remoteHost = getAttribute("remoteHost");
            remotePort = getAttribute("remotePort");
            remoteUrl = getAttribute("remoteURL");
            transport = getAttribute("transport");
            transactionIdAttribute = getAttribute("transactionId");
            destScenarioAttribute = getAttribute("destScenario");
            strDelay = getAttribute("delay");
            answerHandlerAttribute = getAttribute("answerHandler");
            probe = getAttribute("probe");
            
            if (null != request) {
                if (StackFactory.PROTOCOL_DIAMETER.equalsIgnoreCase(protocol)) {
                    GlobalLogger.instance().logDeprecatedMessage(root.getName() + " request=\"xxx\" .../", "sendMessage" + protocol + " .../><header request=\"xxx\" .../");
                }
                else if (StackFactory.PROTOCOL_RADIUS.equalsIgnoreCase(protocol)) {
                    GlobalLogger.instance().logDeprecatedMessage(root.getName() + " request=\"xxx\" .../", "sendMessage" + protocol + " .../");
                }
                req = Boolean.valueOf(request);
            }

            if (protocol.equals(StackFactory.PROTOCOL_DIAMETER)) {
            	String applicationId = root.element("header").attributeValue("applicationId");
            	MsgDiameterParser.getInstance().doDictionnary(this.getRootElement(), applicationId, true);
            }

            // instanciates the msg
            msg = stack.parseMsgFromXml(req, root, runner);
            msg.setSend(true);
        }
        finally {
            unlockAndRestore();
        }
        if ((null != listenpointName) && (null != channelName)) {
            throw new Exception("There must be just a listenpoint or a channel to send message, not both");
        }
        if (((null != remoteHost) || (null != remoteHost) || (null != remoteUrl))
                && (null != channelName)) {
            throw new Exception("RemoteHost and remotePort cannot be set with the channel");
        }

        if (null != channelName) {
            Channel channel = stack.getChannel(channelName);
            if (channel == null) {
                throw new ExecutionException("The channel <name=" + channelName + "> does not exist");
            }
            msg.setChannel(channel);
        }
        Probe p = stack.getProbe(probe);
        if (probe != null && p == null)
        	throw new ExecutionException("The probe <name=" + listenpointName + "> does not exist");
        msg.setProbe(p);
        
        Listenpoint listenpoint = stack.getListenpoint(listenpointName);
        if (listenpointName != null && listenpoint == null) {
            throw new ExecutionException("The listenpoint <name=" + listenpointName + "> does not exist");
        }
        msg.setListenpoint(listenpoint);

        if (null != remoteHost) {
            msg.setRemoteHost(remoteHost);
        }
        if (null != remotePort) {
            msg.setRemotePort(Integer.parseInt(remotePort));
        }
        if (null != remoteUrl) {
            msg.setRemoteUrl(remoteUrl);
        }
        if (null != transport) {
            msg.setTransport(transport);
        }

        // Not a generic mechanism : USED only for HTTP and IMAP
        // Should be copy to StackHttp and StackImap in the parseMsgFromXml() method
        if (null != transactionIdAttribute) {
            msg.setTransactionId(new TransactionId(transactionIdAttribute));
        }

        
        Runner destRunner = null;
        if (null != destScenarioAttribute) {
            ScenarioRunner scenarioRunner = (ScenarioRunner) runner;

            for (ScenarioRunner aScenarioRunner : scenarioRunner.getParent().getChildren()) {
                if (aScenarioRunner.getName().equals(destScenarioAttribute)) {
                    destRunner = aScenarioRunner;
                }
            }

            if (null == destRunner) {
                GlobalLogger.instance().getSessionLogger().error(runner, TextEvent.Topic.PROTOCOL, "Unknown destination scenario ", destScenarioAttribute);
                throw new ExecutionException("The destination scenario '" + destScenarioAttribute + "' mentioned in the scenario is not defined");
            }
        }

        if (null != strDelay) {
            long lDelay = (long) (Float.parseFloat(strDelay) * 1000);
            msg.setTimestamp(System.currentTimeMillis() + lDelay);
        }

        Runner answerHandler = null;
        if (null == answerHandlerAttribute) {
            answerHandler = runner;
        }
        else {
            ScenarioRunner scenarioRunner = (ScenarioRunner) runner;

            for (ScenarioRunner aScenarioRunner : scenarioRunner.getParent().getChildren()) {
                if (aScenarioRunner.getName().equals(answerHandlerAttribute)) {
                    answerHandler = aScenarioRunner;
                }
            }

            if (null == answerHandlerAttribute) {
                GlobalLogger.instance().getSessionLogger().error(runner, TextEvent.Topic.PROTOCOL, "Unknown answerHandler scenario ", destScenarioAttribute);
                throw new ExecutionException("The answerHandler scenario '" + answerHandlerAttribute + "' mentioned in the scenario is not defined");
            }


        }
        // send the request via the stack
        StackFactory.getStack(protocol).sendMessage(msg, (ScenarioRunner) runner, (ScenarioRunner) destRunner, (ScenarioRunner) answerHandler);

        if (protocol.equals(StackFactory.PROTOCOL_RTPFLOW)) {
            if (((com.devoteam.srit.xmlloader.rtp.flow.MsgRtpFlow) msg).isSynchronous())//wait for the end of the flow
            {
                //take a mutex which will be given by the end of the flow
                ((com.devoteam.srit.xmlloader.rtp.flow.MsgRtpFlow) msg).getSynchronousSemaphore().acquire();
            }
        }

        // set the implicit message into the runner (for setFromMessage operation)
        runner.setCurrentMsg(msg);

        return null;
    }
}
