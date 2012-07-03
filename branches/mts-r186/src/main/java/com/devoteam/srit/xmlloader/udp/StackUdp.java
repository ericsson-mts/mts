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

package com.devoteam.srit.xmlloader.udp;

import com.devoteam.srit.xmlloader.core.ParameterPool;

import com.devoteam.srit.xmlloader.core.Runner;
import org.dom4j.Element;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.core.utils.XMLElementTextMsgParser;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author 
 */
public class StackUdp extends Stack
{
    /** Constructor */
    public StackUdp() throws Exception
    {
        super();
        
        // initiate a default listenpoint if port is not empty or null
        int port = getConfig().getInteger("listenpoint.LOCAL_PORT", 0);
        if (port > 0)
        {
        	Listenpoint listenpoint = new ListenpointUdp(this);
            createListenpoint(listenpoint, StackFactory.PROTOCOL_UDP);
        }
    }


	/** Creates a Listenpoint specific to each Stack */
    @Override
	public synchronized Listenpoint parseListenpointFromXml(Element root) throws Exception 
	{
        Listenpoint listenpoint = new ListenpointUdp(this, root);
        return listenpoint;        
	}

    /** Creates a Channel specific to each Stack */
    // deprecated part //
    @Override
    public Channel parseChannelFromXml(Element root, String protocol) throws Exception
    {
        String name = root.attributeValue("socketName");
        String localHost = root.attributeValue("localHost");
        String localPort = root.attributeValue("localPort");
        String remoteHost = root.attributeValue("remoteHost");
        String remotePort = root.attributeValue("remotePort");
        String connected = root.attributeValue("connected");

        if (existsChannel(name))
        {
            return getChannel(name);
        }
        else
        {
            return new ChannelUdp(name, localHost, localPort, remoteHost, remotePort, protocol, Boolean.parseBoolean(connected));
        }
    }
    // deprecated part //

    
	/** Creates a specific Msg */
    @Override
    public Msg parseMsgFromXml(Boolean request, Element root, Runner runner) throws Exception
    {
        //
        // Parse all <data ... /> tags
        //
        List<Element> elements = root.elements("data");
        List<byte[]> datas = new LinkedList<byte[]>();

        try
        {
            for (Element element : elements)
            {
                if (element.attributeValue("format").equalsIgnoreCase("text"))
                {
                    String text = element.getText();
                    // change the \n caractère to \r\n caracteres because the dom librairy return only \n.
                    // this could make some trouble when the length is calculated in the scenario
                    text = Utils.replaceNoRegex(text, "\r\n","\n");                    
                    text = Utils.replaceNoRegex(text, "\n","\r\n");                    
                    datas.add(text.getBytes("UTF8"));
                }
                else if (element.attributeValue("format").equalsIgnoreCase("binary"))
                {
                    String text = element.getTextTrim();
                    datas.add(Utils.parseBinaryString(text));
                }
            }
        }
        catch (Exception e)
        {
            throw new ExecutionException("StackUDP: Error while parsing data", e);
        }

        //
        // Compute total length
        //
        int dataLength = 0;
        for (byte[] data : datas)
        {
            dataLength += data.length;
        }

        byte[] data = new byte[dataLength];
        int i = 0;
        for (byte[] aData : datas)
        {
            for (int j = 0; j < aData.length; j++)
            {
                data[i] = aData[j];
                i++;
            }
        }

        String length = root.attributeValue("length");
        if (length != null)
        {
        	dataLength = Integer.parseInt(length);
            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "fixed length of the datagramPacket to be sent:  ", dataLength);
            if (data.length != dataLength)
            {
                GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "data.length different from chosen fixed length");
            }
        }

        MsgUdp msgUdp = new MsgUdp(data, dataLength);

        String remoteHost = root.attributeValue("remoteHost");
        String remotePort = root.attributeValue("remotePort");

        // deprecated part //
        String name = root.attributeValue("socketName");
        if(name != null)
        {
        	Channel channel = getChannel(name);
            if (channel == null)
            {
                throw new ExecutionException("StackUDP: The connection <name=" + name + "> does not exist");
            }

            if (remoteHost != null)
            {
                channel.setRemoteHost(remoteHost);
            }
            if (remotePort != null)
            {
                channel.setRemotePort(new Integer(remotePort).intValue());
            }
            msgUdp.setChannel(channel);
        }// deprecated part //
        else
        {
            name = root.attributeValue("listenpoint");
            Listenpoint listenpoint = getListenpoint(name);
            if (listenpoint == null)
            {
                throw new ExecutionException("StackUDP: The listenpoint <name=" + name + "> does not exist");
            }

            if (remoteHost != null)
            {
                msgUdp.setRemoteHost(remoteHost);
            }
            if (remotePort != null) 
            {
                msgUdp.setRemotePort(new Integer(remotePort).intValue());
            }
            msgUdp.setListenpoint(listenpoint);
        }

        return msgUdp;
    }

    /** Returns the Config object to access the protocol config file*/
    public Config getConfig() throws Exception
    {
        return Config.getConfigByName("udp.properties");
    }

    /** Returns the replacer used to parse sendMsg Operations */
    public XMLElementReplacer getElementReplacer()
    {
        return XMLElementTextMsgParser.instance();
    }
    
    /** 
     * Creates a Msg specific to each Stack
     * should become ABSTRACT later  
     */
    @Override    
    public Msg readFromDatas(byte[] datas, int length) throws Exception
    {
    	MsgUdp msg = new MsgUdp(datas, length);    		
    	return msg;
    }

}
