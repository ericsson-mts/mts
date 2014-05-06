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

package com.devoteam.srit.xmlloader.diameter;

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLElementAVPParser;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;

import dk.i1.diameter.Message;
import dk.i1.diameter.node.Node;
import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.SupArray;

import java.io.InputStream;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dom4j.Element;

/**
 *
 * @author gpasquiers
 */
public class StackDiameter extends Stack {
    
    /** this instance  */
    private final static String configFile = "diameter.properties";

    protected static Listenpoint listenpoint = null;
    
    /** Creates or returns the instance of this stack */
    public StackDiameter() throws Exception {
        super();

        // initiate a default listenpoint if port is not empty or null
        int port = getConfig().getInteger("listenpoint.LOCAL_PORT", 0);
        if (port > 0)
        {
        	Listenpoint listenpoint = new ListenpointDiameter(this);
            createListenpoint(listenpoint, StackFactory.PROTOCOL_DIAMETER);
        }
        
        // configure stack trace parameters
        FileHandler fh = new FileHandler("../logs/diameterStack.log");
        // logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        Node.logger.addHandler(fh);
        String stringLevel = getConfig().getString("TRACE_LEVEL");
        Level traceLevel = Level.parse(stringLevel);
        Node.logger.setLevel(traceLevel);
        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "traceLevel : ", traceLevel);
        Node.logger.warning("traceLevel = " + traceLevel);
    }
    
    /** Creates a Listenpoint specific to each Stack */
    @Override
    public Listenpoint parseListenpointFromXml(Element root) throws Exception 
    { 
        Listenpoint listenpoint = new ListenpointDiameter(this, root);
        return listenpoint;        
    }
    
    /** Creates a channel specific to each Stack */
    @Override
    public Channel parseChannelFromXml(Element root, String protocol) throws Exception
    {
        return null;
    }
    
    /** Creates a specific Msg */
    @Override
    public Msg parseMsgFromXml(Boolean request, Element root, Runner runner) throws Exception {
        MsgDiameter msg= MsgDiameterParser.getInstance().parseMsgFromXml(request, root);
                
        return msg;
    }
    
    /** 
     * Creates a Msg specific to each Stack
     * Use for TCP like protocol : to build incoming message
     * should become ABSTRACT later  
     */
    public Msg readFromStream(InputStream inputStream, Channel channel) throws Exception
    {
    	// read the header
        byte[] tab = new byte[20];
        int done = Utils.readFromSocketStream(inputStream, tab);
        
        // get the length from the header
        Array header = new DefaultArray(tab,0,done);
        Array lengthArray = header.subArray(1,3);       
        int length = 0;
        length += (lengthArray.get(0) & 0xFF) << 16;
        length += (lengthArray.get(1) & 0xFF) << 8;
        length += lengthArray.get(2) & 0xFF;
        length -= 20;
        
        // read the message payload
        tab = new byte[length];
        done = Utils.readFromSocketStream(inputStream, tab);
        Array payload = new DefaultArray(tab,0,done);
        
        // concat the header and the payload
        SupArray arrayMsg = new SupArray();
        arrayMsg.addFirst(header);
        arrayMsg.addLast(payload);
               
        // build a stack Diameter message
        Message message = new Message();
        message.decode(arrayMsg.getBytes());
        
        MsgDiameter msg = new MsgDiameter(message);
        return msg;
    }
    
    /** Returns the Config object to access the protocol config file*/
    public Config getConfig() throws Exception {
        return Config.getConfigByName(configFile);
    }
    
    public XMLElementReplacer getElementReplacer() {
        return XMLElementAVPParser.instance();
    }
    
}
