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

package com.devoteam.srit.xmlloader.sigtran;

import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.Integer32Array;
import gp.utils.arrays.SupArray;

import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.core.utils.XMLElementTextMsgParser;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import com.devoteam.srit.xmlloader.core.coding.binary.q931.MessageQ931;
import com.devoteam.srit.xmlloader.asn1.ASNMessage;
import com.devoteam.srit.xmlloader.sigtran.ap.BN_APMessage;
import com.devoteam.srit.xmlloader.sigtran.ap.BN_TCAPMessage;
import com.devoteam.srit.xmlloader.sigtran.fvo.FvoDictionary;
import com.devoteam.srit.xmlloader.sigtran.fvo.FvoMessage;
import com.devoteam.srit.xmlloader.sigtran.tlv.TlvDictionary;
import com.devoteam.srit.xmlloader.sigtran.tlv.TlvMessage;

import dk.i1.sctp.SCTPData;

public class StackSigtran extends Stack {

    private static StackSigtran instance = null;
    public static StackSigtran instance(){
        return instance;
    }

    private HashMap<String, TlvDictionary> tlvDictionaries;
    private HashMap<String, FvoDictionary> fvoDictionaries;

    private int defaultPayloadProtocolID = getConfig().getInteger("server.DEFAULT_PPID", 1);
        
    public StackSigtran() throws Exception {
        super();
        
        StackSigtran.instance = this;
        
        tlvDictionaries = new HashMap();
        fvoDictionaries = new HashMap();

        // initiate a default listenpoint if port is not empty or null
        int port = getConfig().getInteger("listenpoint.LOCAL_PORT", 0);
        if (port > 0) {
            Listenpoint listenpoint = new ListenpointSigtran(this);
            createListenpoint(listenpoint, StackFactory.PROTOCOL_SIGTRAN);
        }
    }

    public TlvDictionary getTlvDictionnary(String name) throws Exception{
        if(!tlvDictionaries.containsKey(name)){
            tlvDictionaries.put(name, new TlvDictionary(SingletonFSInterface.instance().getInputStream(new URI("../conf/sigtran/"+name)), this));
        }
        return tlvDictionaries.get(name);
    }

    
    public FvoDictionary getFvoDictionnary(String name) throws Exception{
        if(!fvoDictionaries.containsKey(name)){
            fvoDictionaries.put(name, new FvoDictionary(SingletonFSInterface.instance().getInputStream(new URI("../conf/sigtran/"+name))));
        }
        return fvoDictionaries.get(name);
    }

    /** Creates a Listenpoint specific to each Stack */
    @Override
    public Listenpoint parseListenpointFromXml(Element root) throws Exception {
        Listenpoint listenpoint = new ListenpointSigtran(this, root);
        return listenpoint;
    }

    
    /** Creates a specific Msg */
    @Override
    public Msg parseMsgFromXml(Boolean request, Element root, Runner runner) throws Exception 
    {
        MsgSigtran msgSigtran = new MsgSigtran();
        
        List<Element> listAps = root.elements("AP");
        Object[] tabAps = listAps.toArray();
        
        ASNMessage tcapMessage = null;
        if (tabAps.length >= 1)
        {
        	Element elementTCAP = (Element) tabAps[tabAps.length - 1];
        	tcapMessage = new BN_TCAPMessage();
        	tcapMessage.parseFromXML(elementTCAP);
        	String className = tcapMessage.getClassName(); 
            // TCAP layer (optional)
        	msgSigtran.setTCAPMessage((BN_TCAPMessage) tcapMessage);
        }

        if (tabAps.length >= 2)
        {
        	Element elementAP = (Element) tabAps[0];
        	ASNMessage apMessage = new BN_APMessage(tcapMessage);
        	apMessage.parseFromXML(elementAP);
        	String className = apMessage.getClassName(); 
            // AP layer (optional)
        	msgSigtran.setAPMessage((BN_APMessage) apMessage);
        }

        // ISDN layer (optional)
        Element ie = root.element("ISDN");
        if (ie != null) {
        	MessageQ931 ieMessage = new MessageQ931(ie);
            msgSigtran.setIeMessage(ieMessage);
        }
        
        // SS7 layer (optional)
        Element fvo = root.element("SS7");
        if (fvo != null) {
        	FvoMessage fvoMessage = new FvoMessage(msgSigtran, getFvoDictionnary(fvo.attributeValue("file")));
            msgSigtran.setFvoMessage(fvoMessage);
            fvoMessage.parseElement(fvo);
        }

        // UA layer (mandatory)
        Element tlv = root.element("UA");
        if (tlv != null) {
            TlvDictionary tlvDictionnary = getTlvDictionnary(tlv.attributeValue("file"));
            TlvMessage tlvMessage = new TlvMessage(msgSigtran, tlvDictionnary);
            tlvMessage.parseMsgFromXml(tlv);
            msgSigtran.setTlvMessage(tlvMessage);
            msgSigtran.setTlvProtocol(tlvDictionnary.getPpid());
        }
        else{
            // TODO throw some exception
        }
        

        return msgSigtran;
    }

    /** 
     * Creates a Msg specific to each Stack
     * Use for TCP like protocol : to build incoming message
     * should become ABSTRACT later  
     */
    @Override
    public Msg readFromStream(InputStream inputStream, Channel channel) throws Exception {
        byte[] header = new byte[4];
        byte[] lg = new byte[4];
        byte[] buf = null;
        int nbCharRead = 0;
        int msgLength = 0;
        Integer32Array headerArray = null;
        Integer32Array lgArray = null;
        SupArray msgArray = new SupArray();

        synchronized (inputStream) {
            //read the header
            nbCharRead = inputStream.read(header, 0, 4);
            if (nbCharRead == -1) {
                throw new Exception("End of stream detected");
            }
            else if (nbCharRead < 4) {
                throw new Exception("Not enough char read");
            }
            headerArray = new Integer32Array(new DefaultArray(header));

            //read the length
            nbCharRead = inputStream.read(lg, 0, 4);
            if (nbCharRead == -1) {
                throw new Exception("End of stream detected");
            }
            else if (nbCharRead < 4) {
                throw new Exception("Not enough char read");
            }

            lgArray = new Integer32Array(new DefaultArray(lg));
            msgLength = lgArray.getValue();
            buf = new byte[msgLength - 8];
            //read the staying message's data
            nbCharRead = inputStream.read(buf, 0, msgLength - 8);
        }

        if (nbCharRead == -1) {
            throw new Exception("End of stream detected");
        }
        else if (nbCharRead < (msgLength - 8)) {
            throw new Exception("Not enough char read");
        }

        msgArray.addFirst(headerArray);
        msgArray.addLast(lgArray);
        msgArray.addLast(new DefaultArray(buf));
        DefaultArray array = new DefaultArray(msgArray.getBytes());
        
        //create the message
        int ppidInt = defaultPayloadProtocolID;
        MsgSigtran msg = new MsgSigtran(array, ppidInt);
        return msg;

    }

    /** Returns the Config object to access the protocol config file*/
    public Config getConfig() throws Exception {
        return Config.getConfigByName("sigtran.properties");
    }

    /** Returns the replacer used to parse sendMsg Operations */
    public XMLElementReplacer getElementReplacer() {
        return XMLElementTextMsgParser.instance();
    }

    /**
     * Creates a Msg specific to each Stack
     * Use for SCTP like protocol : to build incoming message
     */
    @Override
    public Msg readFromSCTPData(SCTPData chunk) throws Exception {
        DefaultArray array = new DefaultArray(chunk.getData());
        int ppidInt = Utils.convertLittleBigIndian(chunk.sndrcvinfo.sinfo_ppid);
        // when the PPID is not present into the sctp layer
        if (ppidInt == 0)
        {
        	ppidInt = defaultPayloadProtocolID;
        }
        MsgSigtran msgSigtran = new MsgSigtran(array, ppidInt);
        return msgSigtran;
    }
}
