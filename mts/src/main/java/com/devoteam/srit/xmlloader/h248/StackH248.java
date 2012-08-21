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

package com.devoteam.srit.xmlloader.h248;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.ScenarioRunner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.Trans;
import com.devoteam.srit.xmlloader.core.protocol.TransactionId;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.expireshashmap.ExpireHashMap;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.core.utils.XMLElementTextMsgParser;

/**
 *
 * @author fhenry
 */
public class StackH248 extends Stack
{
	
	private ExpireHashMap<TransactionId, Trans> outTransactionsResponse;
	private ExpireHashMap<TransactionId, Trans> inTransactionsResponse;
    
    /**
     * Config parameter
     * which characters to add at the end of a line
     */
    public String endLineCharacters = "CRLF";

    /** Constructor */
    public StackH248() throws Exception
    {
        super();

        this.outTransactionsResponse = new ExpireHashMap<TransactionId, Trans>("outTransactionsResponse", this.msgLifeTime);
        this.inTransactionsResponse = new ExpireHashMap<TransactionId, Trans>("inTransactionsResponse", this.msgLifeTime);

        // initiate a default listenpoint if port is not empty or null
        int port = getConfig().getInteger("listenpoint.LOCAL_PORT", 0);
        if (port > 0)
        {
        	Listenpoint listenpoint = new ListenpointH248(this);
            createListenpoint(listenpoint, StackFactory.PROTOCOL_H248);
        }
        endLineCharacters = getConfig().getString("END_LINE_CHARACTERS", "CRLF");
    }

    /** Creates a Listenpoint specific to each Stack */
    @Override
    public Listenpoint parseListenpointFromXml(Element root) throws Exception 
    { 
        Listenpoint listenpoint = new ListenpointH248(this, root);
        return listenpoint;        
    }

    /** Creates a specific SIP Msg */
    @Override
    public Msg parseMsgFromXml(Boolean request, Element root, Runner runner) throws Exception
    {
        String text = root.getText().trim();
        if ("CRLF".equals(endLineCharacters))
        {
            text = Utils.replaceNoRegex(text, "\n", "\r\n");
        }        
        else if ("CR".equals(endLineCharacters))
        {
            text = Utils.replaceNoRegex(text, "\n", "\r");
        }        
        else if ("LF".equals(endLineCharacters))
        {
        }
        else
        {
        	throw new ExecutionException("The \"END_LINE_CHARACTERS\" configuration parameter should be a string from the list {CRLF, LF, CR}");
        }
        MsgH248 msgH248 = new MsgH248(text);
              
        return msgH248;
    }

    /** Send the message from the given scenario */
    @Override
    public synchronized boolean sendMessageException(Msg msg, ScenarioRunner srcRunner, ScenarioRunner destRunner, ScenarioRunner answerHandler) throws Exception {
        // is the message a retransmission ?
        boolean isRetransmission = false;
        if (msg.getRetransmissionId() != null) {
            if (!msg.isRequest()) {
                isRetransmission = outRetransResponses.get(msg.getRetransmissionId()) != null;
            }
            else{
                isRetransmission = outRetransRequests.get(msg.getRetransmissionId()) != null;
            }
        }

        // is the message part of an inverted transaction ?
        MsgH248 msgH248 = (MsgH248) msg;
        boolean isInvertedTransaction = true;
        TransactionId responseTransactionId = msgH248.getResponseTransactionId();
        if(null == responseTransactionId) {
            isInvertedTransaction = false;
            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "StackH248: responseTransactionId is null, nothing to do.");
        }

        // do things, if we have to
        if (!isRetransmission && isInvertedTransaction) {
            // Create a new Transaction in order to do timer-based restransmitions.
            // This transaction will have a response as a "begin message" ex:180 INVITE, 183 INVITE, 200 INVITE.
            if (!msg.isRequest()) {
                if (msg.shallBeRetransmitted()) {
                    // add the transaction into the list
                    Trans transaction = new Trans(this, msg);
                    transaction.setScenarioRunner(srcRunner);
                    GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "StackH248: create client transaction (for retrans) ", responseTransactionId, " with begin message: \n", msg);
                    outTransactionsResponse.put(responseTransactionId, transaction);
                    // send the retransmission
                    if (retransmitManagement) {
                        transaction.startAutomaticRetransmit();
                    }
                }
            }
            // Add the Request to it's transaction. That way, when receiving a retransmission
            // of the begin message of that transaction we will retransmit this request as well.
            else {
                Trans transaction = this.inTransactionsResponse.get(msgH248.getResponseTransactionId());
                if (transaction != null) {
                    GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "StackH248: add entry to server transaction (for retrans) ", responseTransactionId, ": \n", msg);
                    transaction.addEndMessage(msg);
                }
                else {
                    GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "StackH248: could not add message to any server transaction ", responseTransactionId, " (for retrans) : \n", msg);
                }
            }
        }

        // then really then the message
        super.sendMessageException(msg, srcRunner, destRunner, answerHandler);
        return true;
    }

    /** Receive a message */
    @Override
    public boolean doReceiveMessage(Msg msg) throws Exception {
        // is the message a retransmission ?
        boolean isRetransmission = false;
        if (msg.getRetransmissionId() != null) {
            if (!msg.isRequest()) {
                isRetransmission = inRetransResponses.get(msg.getRetransmissionId()) != null;
            }
            else{
                isRetransmission = inRetransRequests.get(msg.getRetransmissionId()) != null;
            }
        }

        // is the message part of an inverted transaction ?
        MsgH248 msgH248 = (MsgH248) msg;
        boolean isInvertedTransaction = true;
        TransactionId responseTransactionId = msgH248.getResponseTransactionId();
        if(null == responseTransactionId) {
            isInvertedTransaction = false;
            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "StackH248: responseTransactionId is null, nothing to do.");
        }

        // do things, if we have to
        if (isInvertedTransaction) {
            if (!msg.isRequest()) {
                // Reception of a response retransmission :
                //  - if not a retransmission : create a new transaction.
                //  - if is a retransmission : get the transaction and retransmit the subsequent messages.
                if (msg.shallBeRetransmitted()) {
                    // add the transaction into the list
                    if (isRetransmission) {
                        Trans transaction = this.inTransactionsResponse.get(responseTransactionId);

                        if (retransmitManagement) {
                            if (transaction != null) {
                                transaction.retransmitAutomaticResponses();
                            }
                            else {
                                GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "StackH248: could not do automatic retransmissions for response transaction ", responseTransactionId, " of message:\n", msg);
                            }
                        }
                    }
                    else {
                        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "StackH248: create server transaction (for retrans) ", responseTransactionId, " with begin message: \n", msg);
                        Trans transaction = new Trans(this, msg);
                        this.inTransactionsResponse.put(responseTransactionId, transaction);
                    }
                }
            }
            else {
                // Reception of a non-retransmitted request (ACK, CANCEL, PRACK) :
                //  - Add the request to the transaction in order to stop the timer
                //    based retransmissions of the associated response.
                Trans transaction = this.outTransactionsResponse.get(responseTransactionId);
                if (transaction != null) {
                    GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "StackH248: add entry to client transaction (for retrans) ", responseTransactionId, " : \n", msg);
                    transaction.addEndMessage(msg);
                }
                else {
                    GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "StackH248: could not add message to any client transaction ", responseTransactionId, " (for retrans) : \n", msg);
                }
            }
        }

        super.doReceiveMessage(msg);

        return true;
    }







    /** Returns the Config object to access the protocol config file*/
    public Config getConfig() throws Exception
    {
        return Config.getConfigByName("h248.properties");
    }

    /** Returns the replacer used to parse sendMsg Operations */
    public XMLElementReplacer getElementReplacer()
    {
        return XMLElementTextMsgParser.instance();
    }

    /** 
     * Creates a Msg specific to each Stack
     * Use for UDP like protocol : to build incoming message
     * should become ABSTRACT later  
     */
    @Override	
    public Msg readFromDatas(byte[] datas, int length) throws Exception
    {
    	String str = new String(datas);
    	str = str.substring(0, length);
    	MsgH248 msgH248 = new MsgH248(str);    	
    	return msgH248;
    }

    /*
     * Remove eldest entry if instructed, else grow capacity if appropriate
     * in all stack lists
     */
    @Override
    public void cleanStackLists(){
    	super.cleanStackLists();
    	this.outTransactionsResponse.cleanEldestEntries();
    	this.inTransactionsResponse.cleanEldestEntries();
    }
    
}
