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
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.URIFactory;
import com.devoteam.srit.xmlloader.core.utils.URIRegistry;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLDocument;
import com.devoteam.srit.xmlloader.diameter.dictionary.CommandDef;
import com.devoteam.srit.xmlloader.diameter.dictionary.Dictionary;
import java.io.Serializable;
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
        //------------------------------------------------------------------------ aaa operations -
        // deprecated part
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
        // deprecated part
        else if (rootName.equals("createListenpointDIAMETER")) {
            ope = new OperationCreateListenpoint(StackFactory.PROTOCOL_DIAMETER, root);
        }
        else if (rootName.equals("removeListenpointDIAMETER")) {
            ope = new OperationRemoveListenpoint(StackFactory.PROTOCOL_DIAMETER, root);
        }
        else if (rootName.equals("openChannelDIAMETER")) {
            ope = new OperationOpenChannel(StackFactory.PROTOCOL_DIAMETER, root);
        }
        else if (rootName.equals("closeChannelDIAMETER")) {
            ope = new OperationCloseChannel(StackFactory.PROTOCOL_DIAMETER, root);
        }
        else if (rootName.equals("createProbeDIAMETER")) {
            ope = new OperationCreateProbe(StackFactory.PROTOCOL_DIAMETER, root);
        }
        else if (rootName.equals("removeProbeDIAMETER")) {
            ope = new OperationRemoveProbe(StackFactory.PROTOCOL_DIAMETER, root);
        }
        else if (rootName.equals("sendMessageDIAMETER")) {
            ope = new OperationSendMessage(StackFactory.PROTOCOL_DIAMETER, root);
        }
        else if (rootName.equals("receiveMessageDIAMETER")) {
            ope = new OperationReceiveMessage(StackFactory.PROTOCOL_DIAMETER, root, this);
        }
        //------------------------------------------------------------------------ sip operations -
        // deprecated part
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
        // deprecated part
        else if (rootName.equals("createListenpointSIP")) {
            ope = new OperationCreateListenpoint(StackFactory.PROTOCOL_SIP, root);
        }
        else if (rootName.equals("removeListenpointSIP")) {
            ope = new OperationRemoveListenpoint(StackFactory.PROTOCOL_SIP, root);
        }
        else if (rootName.equals("createProbeSIP")) {
            ope = new OperationCreateProbe(StackFactory.PROTOCOL_SIP, root);
        }
        else if (rootName.equals("removeProbeSIP")) {
            ope = new OperationRemoveProbe(StackFactory.PROTOCOL_SIP, root);
        }
        else if (rootName.equals("sendMessageSIP")) {
            ope = new OperationSendMessage(StackFactory.PROTOCOL_SIP, root);
        }
        else if (rootName.equals("receiveMessageSIP")) {
            ope = new OperationReceiveMessage(StackFactory.PROTOCOL_SIP, root, this);
        }
        //----------------------------------------------------------------------------------- RTSP
        else if (rootName.equals("openChannelRTSP")) {
            ope = new OperationOpenChannel(StackFactory.PROTOCOL_RTSP, root);
        }
        else if (rootName.equals("closeChannelRTSP")) {
            ope = new OperationCloseChannel(StackFactory.PROTOCOL_RTSP, root);
        }
        else if (rootName.equals("createListenpointRTSP")) {
            ope = new OperationCreateListenpoint(StackFactory.PROTOCOL_RTSP, root);
        }
        else if (rootName.equals("removeListenpointRTSP")) {
            ope = new OperationRemoveListenpoint(StackFactory.PROTOCOL_RTSP, root);
        }
        else if (rootName.equals("sendMessageRTSP")) {
            ope = new OperationSendMessage(StackFactory.PROTOCOL_RTSP, root);
        }
        else if (rootName.equals("receiveMessageRTSP")) {
            ope = new OperationReceiveMessage(StackFactory.PROTOCOL_RTSP, root, this);
        }
        //------------------------------------------------------------------------ http operations -
        // deprecated part
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
        // deprecated part
        else if (rootName.equals("openChannelHTTP")) {
            ope = new OperationOpenChannel(StackFactory.PROTOCOL_HTTP, root);
        }
        else if (rootName.equals("closeChannelHTTP")) {
            ope = new OperationCloseChannel(StackFactory.PROTOCOL_HTTP, root);
        }
        else if (rootName.equals("sendMessageHTTP")) {
            ope = new OperationSendMessage(StackFactory.PROTOCOL_HTTP, root);
        }
        else if (rootName.equals("receiveMessageHTTP")) {
            ope = new OperationReceiveMessage(StackFactory.PROTOCOL_HTTP, root, this);
        }
        //------------------------------------------------------------------------ radius operations -
        // deprecated part //
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
        // deprecated part //
        else if (rootName.equals("createListenpointRADIUS")) {
            ope = new OperationCreateListenpoint(StackFactory.PROTOCOL_RADIUS, root);
        }
        else if (rootName.equals("removeListenpointRADIUS")) {
            ope = new OperationRemoveListenpoint(StackFactory.PROTOCOL_RADIUS, root);
        }
        else if (rootName.equals("sendMessageRADIUS")) {
            ope = new OperationSendMessage(StackFactory.PROTOCOL_RADIUS, root);
        }
        else if (rootName.equals("receiveMessageRADIUS")) {
            ope = new OperationReceiveMessage(StackFactory.PROTOCOL_RADIUS, root, this);
        }
        //--------------------------------------------------------------------------------- RTP -        
        // deprecated part //
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
        // deprecated part //
        else if (rootName.equals("createListenpointRTP")) {
            ope = new OperationCreateListenpoint(StackFactory.PROTOCOL_RTP, root);
        }
        else if (rootName.equals("removeListenpointRTP")) {
            ope = new OperationRemoveListenpoint(StackFactory.PROTOCOL_RTP, root);
        }
        else if (rootName.equals("createProbeRTP")) {
            ope = new OperationCreateProbe(StackFactory.PROTOCOL_RTP, root);
        }
        else if (rootName.equals("removeProbeRTP")) {
            ope = new OperationRemoveProbe(StackFactory.PROTOCOL_RTP, root);
        }
        else if (rootName.equals("sendMessageRTP")) {
            ope = new OperationSendMessage(StackFactory.PROTOCOL_RTP, root);
        }
        else if (rootName.equals("receiveMessageRTP")) {
            ope = new OperationReceiveMessage(StackFactory.PROTOCOL_RTP, root, this);
        }
        else if (rootName.equals("createListenpointRTPFLOW")) {
            ope = new OperationCreateListenpoint(StackFactory.PROTOCOL_RTPFLOW, root);
        }
        else if (rootName.equals("removeListenpointRTPFLOW")) {
            ope = new OperationRemoveListenpoint(StackFactory.PROTOCOL_RTPFLOW, root);
        }
        else if (rootName.equals("createProbeRTPFLOW")) {
            ope = new OperationCreateProbe(StackFactory.PROTOCOL_RTPFLOW, root);
        }
        else if (rootName.equals("removeProbeRTPFLOW")) {
            ope = new OperationRemoveProbe(StackFactory.PROTOCOL_RTPFLOW, root);
        }
        else if (rootName.equals("sendMessageRTPFLOW")) {
            ope = new OperationSendMessage(StackFactory.PROTOCOL_RTPFLOW, root);
        }
        else if (rootName.equals("receiveMessageRTPFLOW")) {
            ope = new OperationReceiveMessage(StackFactory.PROTOCOL_RTPFLOW, root, this);
        }
        //--------------------------------------------------------------------------------- TCP -        
        // deprecated part //
        else if (rootName.equals("openConnectionTCP")) {
            ope = new OperationOpenChannel(StackFactory.PROTOCOL_TCP, root);
        }
        else if (rootName.equals("closeConnectionTCP")) {
            ope = new OperationCloseChannel(StackFactory.PROTOCOL_TCP, root);
        }
        else if (rootName.equals("sendDataTCP")) {
            ope = new OperationSendMsg(StackFactory.PROTOCOL_TCP, false, root);
        }
        else if (rootName.equals("receiveDataTCP")) {
            ope = parseReceiveTCP(StackFactory.PROTOCOL_TCP, root);
        }
        // deprecated part //
        else if (rootName.equals("openChannelTCP")) {
            ope = new OperationOpenChannel(StackFactory.PROTOCOL_TCP, root);
        }
        else if (rootName.equals("closeChannelTCP")) {
            ope = new OperationCloseChannel(StackFactory.PROTOCOL_TCP, root);
        }
        else if (rootName.equals("sendMessageTCP")) {
            ope = new OperationSendMessage(StackFactory.PROTOCOL_TCP, root);
        }
        else if (rootName.equals("receiveMessageTCP")) {
            ope = new OperationReceiveMessage(StackFactory.PROTOCOL_TCP, root, this);
        }
        else if (rootName.equals("createListenpointTCP")) {
            ope = new OperationCreateListenpoint(StackFactory.PROTOCOL_TCP, root);
        }
        else if (rootName.equals("removeListenpointTCP")) {
            ope = new OperationRemoveListenpoint(StackFactory.PROTOCOL_TCP, root);
        }
        //--------------------------------------------------------------------------------- SMTP -
        // deprecated part
        else if (rootName.equals("openSessionSMTP")) {
            ope = new OperationOpenChannel(StackFactory.PROTOCOL_SMTP, root);
        }
        else if (rootName.equals("closeSessionSMTP")) {
            ope = new OperationCloseChannel(StackFactory.PROTOCOL_SMTP, root);
        }
        else if (rootName.equals("sendRequestSMTP")) {
            ope = new OperationSendMsg(StackFactory.PROTOCOL_SMTP, true, root);
        }
        else if (rootName.equals("receiveRequestSMTP")) {
            ope = parseReceiveSMTP(StackFactory.PROTOCOL_SMTP, true, root);
        }
        else if (rootName.equals("sendResponseSMTP")) {
            ope = new OperationSendMsg(StackFactory.PROTOCOL_SMTP, false, root);
        }
        else if (rootName.equals("receiveResponseSMTP")) {
            ope = parseReceiveSMTP(StackFactory.PROTOCOL_SMTP, false, root);
        }
        // deprecated part
        else if (rootName.equals("openChannelSMTP")) {
            ope = new OperationOpenChannel(StackFactory.PROTOCOL_SMTP, root);
        }
        else if (rootName.equals("closeChannelSMTP")) {
            ope = new OperationCloseChannel(StackFactory.PROTOCOL_SMTP, root);
        }
        else if (rootName.equals("createListenpointSMTP")) {
            ope = new OperationCreateListenpoint(StackFactory.PROTOCOL_SMTP, root);
        }
        else if (rootName.equals("removeListenpointSMTP")) {
            ope = new OperationRemoveListenpoint(StackFactory.PROTOCOL_SMTP, root);
        }
        else if (rootName.equals("sendMessageSMTP")) {
            ope = new OperationSendMessage(StackFactory.PROTOCOL_SMTP, root);
        }
        else if (rootName.equals("receiveMessageSMTP")) {
            ope = new OperationReceiveMessage(StackFactory.PROTOCOL_SMTP, root, this);
        }
        //--------------------------------------------------------------------------------- UDP -        
        // deprecated part //
        else if (rootName.equals("openSocketUDP")) {
            ope = new OperationOpenChannel(StackFactory.PROTOCOL_UDP, root);
        }
        else if (rootName.equals("closeSocketUDP")) {
            ope = new OperationCloseChannel(StackFactory.PROTOCOL_UDP, root);
        }
        else if (rootName.equals("sendDataUDP")) {
            ope = new OperationSendMsg(StackFactory.PROTOCOL_UDP, false, root);
        }
        else if (rootName.equals("receiveDataUDP")) {
            ope = parseReceiveUDP(StackFactory.PROTOCOL_UDP, root);
        }
        // deprecated part //
        else if (rootName.equals("createListenpointUDP")) {
            ope = new OperationCreateListenpoint(StackFactory.PROTOCOL_UDP, root);
        }
        else if (rootName.equals("removeListenpointUDP")) {
            ope = new OperationRemoveListenpoint(StackFactory.PROTOCOL_UDP, root);
        }
        else if (rootName.equals("sendMessageUDP")) {
            ope = new OperationSendMessage(StackFactory.PROTOCOL_UDP, root);
        }
        else if (rootName.equals("receiveMessageUDP")) {
            ope = new OperationReceiveMessage(StackFactory.PROTOCOL_UDP, root, this);
        }
        //--------------------------------------------------------------------------------- SCTP -        
        // deprecated part //
        else if (rootName.equals("openConnectionSCTP")) {
            ope = new OperationOpenChannel(StackFactory.PROTOCOL_SCTP, root);
        }
        else if (rootName.equals("closeConnectionSCTP")) {
            ope = new OperationCloseChannel(StackFactory.PROTOCOL_SCTP, root);
        }
        else if (rootName.equals("sendDataSCTP")) {
            ope = new OperationSendMsg(StackFactory.PROTOCOL_SCTP, false, root);
        }
        else if (rootName.equals("receiveDataSCTP")) {
            ope = parseReceiveSCTP(StackFactory.PROTOCOL_SCTP, root);
        }
        // deprecated part //
        else if (rootName.equals("openChannelSCTP")) {
            ope = new OperationOpenChannel(StackFactory.PROTOCOL_SCTP, root);
        }
        else if (rootName.equals("closeChannelSCTP")) {
            ope = new OperationCloseChannel(StackFactory.PROTOCOL_SCTP, root);
        }
        else if (rootName.equals("createListenpointSCTP")) {
            ope = new OperationCreateListenpoint(StackFactory.PROTOCOL_SCTP, root);
        }
        else if (rootName.equals("removeListenpointSCTP")) {
            ope = new OperationRemoveListenpoint(StackFactory.PROTOCOL_SCTP, root);
        }
        else if (rootName.equals("sendMessageSCTP")) {
            ope = new OperationSendMessage(StackFactory.PROTOCOL_SCTP, root);
        }
        else if (rootName.equals("receiveMessageSCTP")) {
            ope = new OperationReceiveMessage(StackFactory.PROTOCOL_SCTP, root, this);
        }
        //--------------------------------------------------------------------------------- IMAP -
        else if (rootName.equals("openChannelIMAP")) {
            ope = new OperationOpenChannel(StackFactory.PROTOCOL_IMAP, root);
        }
        else if (rootName.equals("closeChannelIMAP")) {
            ope = new OperationCloseChannel(StackFactory.PROTOCOL_IMAP, root);
        }
        else if (rootName.equals("createListenpointIMAP")) {
            ope = new OperationCreateListenpoint(StackFactory.PROTOCOL_IMAP, root);
        }
        else if (rootName.equals("removeListenpointIMAP")) {
            ope = new OperationRemoveListenpoint(StackFactory.PROTOCOL_IMAP, root);
        }
        else if (rootName.equals("sendMessageIMAP")) {
            ope = new OperationSendMessage(StackFactory.PROTOCOL_IMAP, root);
        }
        else if (rootName.equals("receiveMessageIMAP")) {
            ope = new OperationReceiveMessage(StackFactory.PROTOCOL_IMAP, root, this);
        }
        //--------------------------------------------------------------------------------- POP -
        else if (rootName.equals("openChannelPOP")) {
            ope = new OperationOpenChannel(StackFactory.PROTOCOL_POP, root);
        }
        else if (rootName.equals("closeChannelPOP")) {
            ope = new OperationCloseChannel(StackFactory.PROTOCOL_POP, root);
        }
        else if (rootName.equals("createListenpointPOP")) {
            ope = new OperationCreateListenpoint(StackFactory.PROTOCOL_POP, root);
        }
        else if (rootName.equals("removeListenpointPOP")) {
            ope = new OperationRemoveListenpoint(StackFactory.PROTOCOL_POP, root);
        }
        else if (rootName.equals("sendMessagePOP")) {
            ope = new OperationSendMessage(StackFactory.PROTOCOL_POP, root);
        }
        else if (rootName.equals("receiveMessagePOP")) {
            ope = new OperationReceiveMessage(StackFactory.PROTOCOL_POP, root, this);
        }
        //--------------------------------------------------------------------------------- SMPP -
        else if (rootName.equals("openChannelSMPP")) {
            ope = new OperationOpenChannel(StackFactory.PROTOCOL_SMPP, root);
        }
        else if (rootName.equals("closeChannelSMPP")) {
            ope = new OperationCloseChannel(StackFactory.PROTOCOL_SMPP, root);
        }
        else if (rootName.equals("createListenpointSMPP")) {
            ope = new OperationCreateListenpoint(StackFactory.PROTOCOL_SMPP, root);
        }
        else if (rootName.equals("removeListenpointSMPP")) {
            ope = new OperationRemoveListenpoint(StackFactory.PROTOCOL_SMPP, root);
        }
        else if (rootName.equals("sendMessageSMPP")) {
            ope = new OperationSendMessage(StackFactory.PROTOCOL_SMPP, root);
        }
        else if (rootName.equals("receiveMessageSMPP")) {
            ope = new OperationReceiveMessage(StackFactory.PROTOCOL_SMPP, root, this);
        }
        //--------------------------------------------------------------------------------- UCP -
        else if (rootName.equals("openChannelUCP")) {
            ope = new OperationOpenChannel(StackFactory.PROTOCOL_UCP, root);
        }
        else if (rootName.equals("closeChannelUCP")) {
            ope = new OperationCloseChannel(StackFactory.PROTOCOL_UCP, root);
        }
        else if (rootName.equals("createListenpointUCP")) {
            ope = new OperationCreateListenpoint(StackFactory.PROTOCOL_UCP, root);
        }
        else if (rootName.equals("removeListenpointUCP")) {
            ope = new OperationRemoveListenpoint(StackFactory.PROTOCOL_UCP, root);
        }
        else if (rootName.equals("sendMessageUCP")) {
            ope = new OperationSendMessage(StackFactory.PROTOCOL_UCP, root);
        }
        else if (rootName.equals("receiveMessageUCP")) {
            ope = new OperationReceiveMessage(StackFactory.PROTOCOL_UCP, root, this);
        }
        //--------------------------------------------------------------------------------- SIGTRAN -
        else if (rootName.equals("openChannelSIGTRAN")) {
            ope = new OperationOpenChannel(StackFactory.PROTOCOL_SIGTRAN, root);
        }
        else if (rootName.equals("closeChannelSIGTRAN")) {
            ope = new OperationCloseChannel(StackFactory.PROTOCOL_SIGTRAN, root);
        }
        else if (rootName.equals("createListenpointSIGTRAN")) {
            ope = new OperationCreateListenpoint(StackFactory.PROTOCOL_SIGTRAN, root);
        }
        else if (rootName.equals("removeListenpointSIGTRAN")) {
            ope = new OperationRemoveListenpoint(StackFactory.PROTOCOL_SIGTRAN, root);
        }
        else if (rootName.equals("sendMessageSIGTRAN")) {
            ope = new OperationSendMessage(StackFactory.PROTOCOL_SIGTRAN, root);
        }
        else if (rootName.equals("receiveMessageSIGTRAN")) {
            ope = new OperationReceiveMessage(StackFactory.PROTOCOL_SIGTRAN, root, this);
        }
        //--------------------------------------------------------------------------------- TLS -
        else if (rootName.equals("openChannelTLS")) {
            ope = new OperationOpenChannel(StackFactory.PROTOCOL_TLS, root);
        }
        else if (rootName.equals("closeChannelTLS")) {
            ope = new OperationCloseChannel(StackFactory.PROTOCOL_TLS, root);
        }
        else if (rootName.equals("sendMessageTLS")) {
            ope = new OperationSendMessage(StackFactory.PROTOCOL_TLS, root);
        }
        else if (rootName.equals("receiveMessageTLS")) {
            ope = new OperationReceiveMessage(StackFactory.PROTOCOL_TLS, root, this);
        }
        else if (rootName.equals("createListenpointTLS")) {
            ope = new OperationCreateListenpoint(StackFactory.PROTOCOL_TLS, root);
        }
        else if (rootName.equals("removeListenpointTLS")) {
            ope = new OperationRemoveListenpoint(StackFactory.PROTOCOL_TLS, root);
        }
        //--------------------------------------------------------------------------------- H248 -
        else if (rootName.equals("openChannelH248")) {
            ope = new OperationOpenChannel(StackFactory.PROTOCOL_H248, root);
        }
        else if (rootName.equals("closeChannelH248")) {
            ope = new OperationCloseChannel(StackFactory.PROTOCOL_H248, root);
        }
        else if (rootName.equals("sendMessageH248")) {
            ope = new OperationSendMessage(StackFactory.PROTOCOL_H248, root);
        }
        else if (rootName.equals("receiveMessageH248")) {
            ope = new OperationReceiveMessage(StackFactory.PROTOCOL_H248, root, this);
        }
        else if (rootName.equals("createListenpointH248")) {
            ope = new OperationCreateListenpoint(StackFactory.PROTOCOL_H248, root);
        }
        else if (rootName.equals("removeListenpointH248")) {
            ope = new OperationRemoveListenpoint(StackFactory.PROTOCOL_H248, root);
        }
        //--------------------------------------------------------------------------------- PCP -
        else if (rootName.equals("openChannelPCP")) {
            ope = new OperationOpenChannel(StackFactory.PROTOCOL_PCP, root);
        }
        else if (rootName.equals("closeChannelPCP")) {
            ope = new OperationCloseChannel(StackFactory.PROTOCOL_PCP, root);
        }
        else if (rootName.equals("sendMessagePCP")) {
            ope = new OperationSendMessage(StackFactory.PROTOCOL_PCP, root);
        }
        else if (rootName.equals("receiveMessagePCP")) {
            ope = new OperationReceiveMessage(StackFactory.PROTOCOL_PCP, root, this);
        }
        else if (rootName.equals("createListenpointPCP")) {
            ope = new OperationCreateListenpoint(StackFactory.PROTOCOL_PCP, root);
        }
        else if (rootName.equals("removeListenpointPCP")) {
            ope = new OperationRemoveListenpoint(StackFactory.PROTOCOL_PCP, root);
        }
        //--------------------------------------------------------------------------------- MSRP -
        else if (rootName.equals("openChannelMSRP")) {
            ope = new OperationOpenChannel(StackFactory.PROTOCOL_MSRP, root);
        }
        else if (rootName.equals("closeChannelMSRP")) {
            ope = new OperationCloseChannel(StackFactory.PROTOCOL_MSRP, root);
        }
        else if (rootName.equals("sendMessageMSRP")) {
            ope = new OperationSendMessage(StackFactory.PROTOCOL_MSRP, root);
        }
        else if (rootName.equals("receiveMessageMSRP")) {
            ope = new OperationReceiveMessage(StackFactory.PROTOCOL_MSRP, root, this);
        }
        else if (rootName.equals("createListenpointMSRP")) {
            ope = new OperationCreateListenpoint(StackFactory.PROTOCOL_MSRP, root);
        }
        else if (rootName.equals("removeListenpointMSRP")) {
            ope = new OperationRemoveListenpoint(StackFactory.PROTOCOL_MSRP, root);
        }
        //--------------------------------------------------------------------------------- GTPP -
        else if (rootName.equals("openChannelGTP")) {
            ope = new OperationOpenChannel(StackFactory.PROTOCOL_GTP, root);
        }
        else if (rootName.equals("closeChannelGTP")) {
            ope = new OperationCloseChannel(StackFactory.PROTOCOL_GTP, root);
        }
        else if (rootName.equals("sendMessageGTP")) {
            ope = new OperationSendMessage(StackFactory.PROTOCOL_GTP, root);
        }
        else if (rootName.equals("receiveMessageGTP")) {
            ope = new OperationReceiveMessage(StackFactory.PROTOCOL_GTP, root, this);
        }
        else if (rootName.equals("createListenpointGTP")) {
            ope = new OperationCreateListenpoint(StackFactory.PROTOCOL_GTP, root);
        }
        else if (rootName.equals("removeListenpointGTP")) {
            ope = new OperationRemoveListenpoint(StackFactory.PROTOCOL_GTP, root);
        }
        else if (rootName.equals("createProbeGTP")) {
            ope = new OperationCreateProbe(StackFactory.PROTOCOL_GTP, root);
        }
        else if (rootName.equals("removeProbeGTP")) {
            ope = new OperationRemoveProbe(StackFactory.PROTOCOL_GTP, root);
        }
        //--------------------------------------------------------------------------------- ETHERNET  -
        else if (rootName.equals("sendMessageETHERNET")) {
        	ope = new OperationSendMessage(StackFactory.PROTOCOL_ETHERNET, root);
        }
        else if (rootName.equals("receiveMessageETHERNET")) {
        	ope = new OperationReceiveMessage(StackFactory.PROTOCOL_ETHERNET, root, this);
        }
        else if (rootName.equals("createProbeETHERNET")) {
            ope = new OperationCreateProbe(StackFactory.PROTOCOL_ETHERNET, root);
        }
        else if (rootName.equals("removeProbeETHERNET")) {
            ope = new OperationRemoveProbe(StackFactory.PROTOCOL_ETHERNET, root);
        }
        //--------------------------------------------------------------------------------- SNMP -
        else if (rootName.equals("sendMessageSNMP")) {
            ope = new OperationSendMessage(StackFactory.PROTOCOL_SNMP, root);
        }
        else if (rootName.equals("receiveMessageSNMP")) {
            ope = new OperationReceiveMessage(StackFactory.PROTOCOL_SNMP, root, this);
        }
        else if (rootName.equals("createListenpointSNMP")) {
            ope = new OperationCreateListenpoint(StackFactory.PROTOCOL_SNMP, root);
        }
        else if (rootName.equals("removeListenpointSNMP")) {
            ope = new OperationRemoveListenpoint(StackFactory.PROTOCOL_SNMP, root);
        }
        //--------------------------------------------------------------------------------- MGCP -
        else if (rootName.equals("sendMessageMGCP")) {
            ope = new OperationSendMessage(StackFactory.PROTOCOL_MGCP, root);
        }
        else if (rootName.equals("receiveMessageMGCP")) {
            ope = new OperationReceiveMessage(StackFactory.PROTOCOL_MGCP, root, this);
        }
        else if (rootName.equals("createListenpointMGCP")) {
            ope = new OperationCreateListenpoint(StackFactory.PROTOCOL_MGCP, root);
        }
        else if (rootName.equals("removeListenpointMGCP")) {
            ope = new OperationRemoveListenpoint(StackFactory.PROTOCOL_MGCP, root);
        }
        //--------------------------------------------------------------------------------- STUN -
        else if (rootName.equals("sendMessageSTUN")) {
            ope = new OperationSendMessage(StackFactory.PROTOCOL_STUN, root);
        }
        else if (rootName.equals("receiveMessageSTUN")) {
            ope = new OperationReceiveMessage(StackFactory.PROTOCOL_STUN, root, this);
        }
        else if (rootName.equals("createListenpointSTUN")) {
            ope = new OperationCreateListenpoint(StackFactory.PROTOCOL_STUN, root);
        }
        else if (rootName.equals("removeListenpointSTUN")) {
            ope = new OperationRemoveListenpoint(StackFactory.PROTOCOL_STUN, root);
        }
        //--------------------------------------------------------------------------------- Q931-
        else if (rootName.equals("sendMessageH225CS")) {
            ope = new OperationSendMessage(StackFactory.PROTOCOL_H225CS, root);
        }
        else if (rootName.equals("receiveMessageH225CS")) {
            ope = new OperationReceiveMessage(StackFactory.PROTOCOL_H225CS, root, this);
        }
        else if (rootName.equals("createListenpointH225CS")) {
            ope = new OperationCreateListenpoint(StackFactory.PROTOCOL_H225CS, root);
        }
        else if (rootName.equals("removeListenpointH225CS")) {
            ope = new OperationRemoveListenpoint(StackFactory.PROTOCOL_H225CS, root);
        }
        //--------------------------------------------------------------------------------- core
        else if (rootName.equals("label")) {
            ope = new OperationLabel(root);
        }
        else if (rootName.equals("exit")) {
            ope = new OperationExit(root);
        }
        else if (rootName.equals("parameter")) {
            ope = new OperationParameter(root);
        }
        else if (rootName.equals("pause")) {
            ope = new OperationPause(root);
        }
        else if (rootName.equals("semaphore")) {
            ope = new OperationSemaphore(root);
        }
        else if (rootName.equals("goto")) {
            ope = new OperationGoto(root);
        }
        else if (rootName.equals("if")) {
            ope = new OperationIf(root, this);
        }
        else if (rootName.equals("switch")) {
            ope = new OperationSwitch(root, this);
        }
        else if (rootName.equals("while")) {
            ope = new OperationWhile(root, this);
        }
        else if (rootName.equals("try")) {
            ope = new OperationTry(root, this);
        }
        else if (rootName.equals("system")) {
            ope = new OperationSystem(root);
        }
        else if (rootName.equals("test")) {
            ope = new OperationTest(root);
        }
        else if (rootName.equals("and")) {
            ope = new OperationTestAnd(root, this);
        }
        else if (rootName.equals("not")) {
            ope = new OperationTestNot(root, this);
        }
        else if (rootName.equals("or")) {
            ope = new OperationTestOr(root, this);
        }
        else if (rootName.equals("log")) {
            ope = new OperationLog(root);
        }
        else if (rootName.equals("stats")) {
            ope = new OperationStats(root);
        }
        else if (rootName.equals("for")) {
            ope = new OperationFor(root, this);
        }
        else if (rootName.equals("call")) {
            ope = new OperationCall(root);
        }
        else if (rootName.equals("function")) {
            ope = new OperationFunction(root);
        }
        else {
            throw new ParsingException("Unknown operation " + rootName);
        }

        return ope;
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
