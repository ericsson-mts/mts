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

package com.devoteam.srit.xmlloader.ucp;

import com.devoteam.srit.xmlloader.core.ParameterPool;

import com.devoteam.srit.xmlloader.core.Runner;
import org.dom4j.Element;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.core.utils.XMLElementTextMsgParser;

import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import com.devoteam.srit.xmlloader.ucp.data.*;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.SupArray;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.util.List;
import java.util.Vector;


/**
 *
 * @author bbouvier
 */
public class StackUcp extends Stack
{
	
    private static final byte STX = 2;
    private static final byte ETX = 3;
    private static final byte SEP = 47;//47 in decimal is 2f in hexa
    private UcpDictionary ucpDictionary;

    /** Constructor */
    public StackUcp() throws Exception
    {
        super();
        ucpDictionary = new UcpDictionary(SingletonFSInterface.instance().getInputStream(new URI("../conf/ucp/dictionary.xml")));

        // initiate a default listenpoint if port is not empty or null
        int port = getConfig().getInteger("listenpoint.LOCAL_PORT", 0);
        if (port > 0)
        {
        	Listenpoint listenpoint = new ListenpointUcp(this);
            createListenpoint(listenpoint, StackFactory.PROTOCOL_UCP);
        }
    }

    /** Creates a Listenpoint specific to each Stack */
    @Override
    public Listenpoint parseListenpointFromXml(Element root) throws Exception 
    { 
        Listenpoint listenpoint = new ListenpointUcp(this, root);
        return listenpoint;        
    }

    /** Creates a Channel specific to each Stack */
    @Override
    public Channel parseChannelFromXml(Element root, String protocol) throws Exception
    {
        String name       = root.attributeValue("name");
        String localHost  = root.attributeValue("localHost");
        String localPort  = root.attributeValue("localPort");
        String remoteHost = root.attributeValue("remoteHost");
        String remotePort = root.attributeValue("remotePort");

        if (existsChannel(name))
        {
            return getChannel(name);
        }
        else
        {
            if(null != localHost) localHost = InetAddress.getByName(localHost).getHostAddress();
            else                  localHost = "0.0.0.0";

            if(null != remoteHost) remoteHost = InetAddress.getByName(remoteHost).getHostAddress();

            return new ChannelUcp(name, localHost, localPort, remoteHost, remotePort, protocol);
        }
    }

	/** Creates a specific Msg */
    @Override
    public Msg parseMsgFromXml(Boolean request, Element root, Runner runner) throws Exception
    {
        UcpMessage ucpMessage = new UcpMessage();

        // header
        Element header = root.element("header");
        String msgName = header.attributeValue("name");
        String msgOT = header.attributeValue("OT");

        if((msgOT != null) && (msgName != null))
            throw new Exception("OT and name of the message " + msgName + " must not be set both");

        if((msgOT == null) && (msgName == null))
            throw new Exception("One of the parameter OT and name of the message header must be set");

        if(msgName != null)
        {
            ucpMessage.setName(msgName);
            ucpMessage.setOperationType(ucpDictionary.getMessageOperationTypeFromName(msgName));
            if(ucpMessage.getOperationType() == null)
                throw new Exception("Message <" + msgName + "> is unknown in the dictionary");
        }

        if(msgOT != null)
        {
            ucpMessage.setName(ucpDictionary.getMessageNameFromOperationType(msgOT));
            if(ucpMessage.getName() == null)
                throw new Exception("Message with OperationType <" + msgOT + "> is unknown in the dictionary");
            ucpMessage.setOperationType(msgOT);
        }

        ucpMessage.setMessageType(header.attributeValue("MT"));
        ucpMessage.setTransactionNumber(header.attributeValue("TRN"));

        parseAttributes(root, ucpMessage);
        ucpMessage.calculLength();//calcul the length with attribute from the attribute
        return new MsgUcp(ucpMessage);
    }

    public void parseAttributes(Element root, UcpMessage msg) throws Exception
    {
        List<Element> attributes = root.elements("attribute");
        List<Element> imbricateAttributes = null;
        List<Element> xserAttributes = null;
        UcpAttribute att = null;
        UcpAttribute att2 = null;

        for(Element element:attributes)
        {
            att = new UcpAttribute();
            att.setName(element.attributeValue("name"));

            //check imbricate attribute + extra service(xser) to send
            imbricateAttributes = element.selectNodes("attribute");
            xserAttributes = element.selectNodes("xser");
            
            if(imbricateAttributes.size() != 0)
            {
                att.setValue(new Vector<UcpAttribute>());
                for(Element element2:imbricateAttributes)
                {
                    att2 = new UcpAttribute();
                    att2.setName(element2.attributeValue("name"));
                    att2.setValue(element2.attributeValue("value"));
                    ((Vector<UcpAttribute>)att.getValue()).add(att2);
                }
            }
            else if(xserAttributes.size() != 0)
            {
                parseXser(xserAttributes, att);
            }
            else
            {
                String encoding = element.attributeValue("encoding");
                if((encoding != null) && (encoding.equalsIgnoreCase("true")))
                {
                    att.setFormat("encodedString");
                }
                att.setValue(element.attributeValue("value"));
            }
            msg.addAttribute(att);
        }
    }

    public void parseXser(List<Element> list, UcpAttribute att) throws Exception
    {
        UcpXser ser = null;
        att.setValue(new Vector<UcpXser>());
        for(Element element:list)
        {
            ser = new UcpXser();
            ser.setType(element.attributeValue("type"));
            ser.setLength(Integer.parseInt(element.attributeValue("length")));
            ser.setValue(element.attributeValue("value").toUpperCase());
            ((Vector<UcpXser>)att.getValue()).add(ser);
        }
    }

    @Override
    public Msg readFromStream(InputStream inputStream, Channel channel) throws Exception
    {
        byte[] header = new byte[14];
        byte[] buf = null;
        int nbCharRead = 0;
        int msgLengthToRead = 0;
        String OT = null;
        String RR = null;//reponseResult
        SupArray msgArray = new SupArray();
        
        synchronized (inputStream)
        {
            //read of start character
            nbCharRead = inputStream.read(header, 0, 1);
            if(nbCharRead == -1)
                //TODO: empty buffer to restart to read on good base, TOSEE if its really useful
                throw new Exception("End of stream detected");
            else if(header[0] != STX)
                throw new Exception("STX character for start message incorrect");

            //read of the header "TRN/LEN/O-R/OT/"
            nbCharRead = inputStream.read(header, 0, 14);
            if(nbCharRead == -1)
                throw new Exception("End of stream detected");
            else if(nbCharRead < 14)
                throw new Exception("Not enough char read for header");

            //process header
            //check all separator
            if((header[2] != SEP) || (header[8] != SEP) || (header[10] != SEP) || (header[13] != SEP))
            {
                throw new Exception("Error while getting message from socket on separator");
                // read msg given by length by display warning or anticipate a pb on parsing message
            }

            //header from 3 to 5 is length field
            msgLengthToRead = Integer.parseInt(new String(header, 3, 5)) - 14 + 1;//-14 for header, +1 for etx character
            buf = new byte[msgLengthToRead];
            //read the staying message's data
            nbCharRead = inputStream.read(buf, 0, msgLengthToRead);
        }

        if(nbCharRead == -1)
            throw new Exception("End of stream detected");
        else if(nbCharRead < msgLengthToRead)
            throw new Exception("Not enough char read for message data");
        else if(buf[msgLengthToRead - 1] != ETX)
            throw new Exception("ETX character for end message incorrect");

        msgArray.addFirst(new DefaultArray(header));
        msgArray.addLast(new DefaultArray(buf, 0, msgLengthToRead - 1));//to not include ETX in msg

        OT = new String(header, 11, 2);

        if((char)header[9] == 'R')//response, header 9 is MT field
        {
            //get result of response, ACK or NACK before getting message in dictionary
            RR = new String(buf, 0, 1);
        }
        UcpMessage msg = ucpDictionary.getMessage(RR, OT);
        msg.parseArray(msgArray);

        return new MsgUcp(msg);
    }

    /** Returns the Config object to access the protocol config file*/
    public Config getConfig() throws Exception
    {
        return Config.getConfigByName("ucp.properties");
    }

    /** Returns the replacer used to parse sendMsg Operations */
    public XMLElementReplacer getElementReplacer()
    {
        return XMLElementTextMsgParser.instance();
    }

}