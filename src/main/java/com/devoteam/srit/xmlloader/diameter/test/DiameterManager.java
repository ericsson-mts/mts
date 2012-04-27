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

package com.devoteam.srit.xmlloader.diameter.test;

import com.devoteam.srit.xmlloader.diameter.MsgDiameter;
import com.devoteam.srit.xmlloader.diameter.test.IMSDiameterConstants;

import com.devoteam.srit.xmlloader.core.log.GenericLogger;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Stack;

import dk.i1.diameter.AVP;
import dk.i1.diameter.AVP_Grouped;
import dk.i1.diameter.AVP_Integer32;
import dk.i1.diameter.AVP_UTF8String;
import dk.i1.diameter.AVP_Unsigned32;
import dk.i1.diameter.Message;
import dk.i1.diameter.ProtocolConstants;
import dk.i1.diameter.Utils;
import dk.i1.diameter.node.ConnectionKey;
import dk.i1.diameter.node.NodeManager;
import dk.i1.diameter.node.NodeSettings;
import dk.i1.diameter.node.NotARequestException;
import dk.i1.diameter.node.NotRoutableException;
import dk.i1.diameter.node.Peer;
import dk.i1.diameter.node.StaleConnectionException;

public class DiameterManager extends NodeManager {
                        
    
    private GenericLogger logger;
    
    private NodeSettings node_settings = null;      

    // peers list
    private Peer peers[] = null;
    
    /**
     * create the diameter Header.
     * @param    request The request to send
     * @return The answer to the request. Null if there is no answer (all peers down, or other error)
     */
    public Message prepareDiameterMessage(int commandCode, boolean request)throws Exception  {
        Message msg = new Message();
        
        // set the header
        msg.hdr.setRequest(request);
        msg.hdr.setProxiable(true);
        msg.hdr.application_id = IMSDiameterConstants.DIAMETER_APPLICATION_CXDX;        
        msg.hdr.command_code = commandCode;
        
        // set the avp list
        AVP avp = new AVP_UTF8String(ProtocolConstants.DI_ORIGIN_HOST, node_settings.hostId());
        msg.add(avp);
        
        avp = new AVP_UTF8String(ProtocolConstants.DI_ORIGIN_REALM, node_settings.realm());                   
        msg.add(avp);
        
        avp = new AVP_UTF8String(ProtocolConstants.DI_DESTINATION_HOST, node_settings.hostId());        
        msg.add(avp);
        
        avp = new AVP_UTF8String(ProtocolConstants.DI_DESTINATION_REALM, node_settings.realm());
        msg.add(avp);
        
        int supportedVendor = IMSDiameterConstants.DIAMETER_3GPP_VENDOR_ID;       
        int capabilityAuthApplication = IMSDiameterConstants.DIAMETER_APPLICATION_CXDX;
        AVP vendorId = new AVP_Unsigned32(ProtocolConstants.DI_VENDOR_ID, supportedVendor);
        AVP authAppId = new AVP_Unsigned32(ProtocolConstants.DI_AUTH_APPLICATION_ID, capabilityAuthApplication);
        AVP[] vendorSpecific = {vendorId, authAppId};
        msg.add(new AVP_Grouped(ProtocolConstants.DI_VENDOR_SPECIFIC_APPLICATION_ID, vendorSpecific));
        
        Utils.setMandatory_RFC3588(msg);
        
        return msg;
    }

    /**
     * init the diameter Client.
     * @param    peer : the peer to connect to
     * @return 
     */
    public DiameterManager(NodeSettings node_settings, Peer[] peers ) throws Exception {
        super(node_settings);
        this.peers = peers;
        for (int i = 0; i < peers.length; i++) {
            peers[i].capabilities = node_settings.capabilities();
        }
        logger = GlobalLogger.instance().getApplicationLogger();
        this.node_settings = node_settings;
        // start the client             
        try {
            start();            
            for(Peer p : peers) {
               node().initiateConnection(p, true);
            }      
            waitForConnection();
        } catch (Exception e) {
            logger.error(TextEvent.Topic.PROTOCOL, e, "ERROR : Start and connecting the DIAMETER node: ", e);
            e.printStackTrace();
        }
    }  

    
    /**
     * Send a diameter request.
     * @param    request The request to send
     * @return The answer to the request. Null if there is no answer (all peers down, or other error)
     */
    public Message sendDiameterRequest(MsgDiameter msg) throws Exception {
        logger.debug(TextEvent.Topic.PROTOCOL, Stack.SEND, msg);
       
        SyncCall sc = new SyncCall();
        sc.answer_ready = false;
        sc.answer=null;
                            
        try {              
            this.sendRequest(msg.getMessage(), peers, sc);
            //ok, sent
            synchronized(sc) {
                while(!sc.answer_ready)
                    sc.wait();
            }            
        } catch (Exception e) {
            logger.error(TextEvent.Topic.PROTOCOL, e, "ERROR : Sending DIAMETER request : ", e);
            e.printStackTrace();
        }        
        return sc.answer;
    }
    
    /**
     * Method to close properly the ressources
     */    
    public boolean close() {
        this.stop(0);        
        // noting to do
        return true;
    }

    private static class SyncCall {
        boolean answer_ready;
        Message answer;
    }

    /**
     * Dispatches an answer to threads waiting for it.
     */    
    protected void handleAnswer(Message answer, ConnectionKey answer_connkey, Object state) {
        MsgDiameter msg = new MsgDiameter(answer);
        try
        {
            logger.debug(TextEvent.Topic.PROTOCOL, Stack.RECEIVE, msg);
        }
        catch (Exception e)
        {
            logger.warn(TextEvent.Topic.PROTOCOL, e, "An error occured while logging the diameter answer : ", answer);
        }
        
        SyncCall sc = (SyncCall)state;
        synchronized(sc) {
            sc.answer = answer;
            sc.answer_ready = true;
            sc.notify();
        }
    }
    
    /**
     * Handle a request.
     * This method is called when a request arrives. It is meant to be
     * overridden by a subclass. This implementation rejects all requests.
     * <p>
     * Please note that the handleRequest() method is called by the
     * networking thread and messages from other peers cannot be received
     * until the method returns. If the handleRequest() method needs to do
     * any lengthy processing then it should implement a message queue, put
     * the message into the queue, and return. The requests can then be
     * processed by a worker thread pool without stalling the networking layer.
     * @param request The incoming request.
     * @param connkey The connection from where the request came.
     * @param peer The peer that sent the request. This is not the originating peer but the peer directly connected to us that sent us the request.
     */
    protected void handleRequest(Message request, ConnectionKey connkey, Peer peer) {
        MsgDiameter msg = new MsgDiameter(request);
        try
        {
            logger.debug(TextEvent.Topic.PROTOCOL, Stack.RECEIVE, msg);
        }
        catch (Exception e)
        {
            logger.warn(TextEvent.Topic.PROTOCOL, e, "An error occured while logging the diameter request : ", request);
        }    
       
        Message response = null;
        try {
            response = prepareDiameterMessage(IMSDiameterConstants.MAR, false);
            response.prepareAnswer(request);
            response.add(new AVP_Integer32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_SUCCESS));

            answer(response,connkey);
        } catch(Exception e) { 
            logger.error(TextEvent.Topic.PROTOCOL, e, "ERROR sending the diameter answer : ", response);
            return;
        }
        
        try {
            logger.debug(TextEvent.Topic.PROTOCOL, "SEND the DIAMETER response : ", new MsgDiameter(response));
        } catch (Exception e) {
            logger.warn(TextEvent.Topic.PROTOCOL, e, "An error occured while logging the diameter response : ", response);
        }    
                
        return ;
    }
        
    
}
