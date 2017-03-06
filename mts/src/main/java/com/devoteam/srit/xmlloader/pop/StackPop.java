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

package com.devoteam.srit.xmlloader.pop;


import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.Tester;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Msg.ParseFromXmlContext;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.Trans;
import com.devoteam.srit.xmlloader.core.protocol.TransactionId;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.core.utils.XMLElementTextMsgParser;

import org.dom4j.Element;
import java.io.InputStream;
import java.net.Socket;

public class StackPop extends Stack 
{
	
	public StackPop() throws Exception 
	{
		super();
	}
	
    /** Creates a Channel specific to each Stack */
    @Override
	public synchronized Channel parseChannelFromXml(Element root, Runner runner, String protocol) throws Exception {
		String name = root.attributeValue("name");
		String localHost = root.attributeValue("localHost");
		String localPort = root.attributeValue("localPort");
        String remoteHost = root.attributeValue("remoteHost");
		String remotePort = root.attributeValue("remotePort");

		if((remoteHost == null) || (remotePort == null))
        {
            throw new Exception("Missing one of the remoteHost or remotePort parameter to create channel.");
        }

        if(localHost == null)
        {
            localHost = Utils.getLocalAddress().getHostAddress();
        }
        
        if (existsChannel(name))
        {
            return getChannel(name);
        }
        else
        {
        	ChannelPop channelPop = new ChannelPop(this, name, localHost, localPort, remoteHost, remotePort, protocol);
            return channelPop;
        }
	}
    
	/** Creates a specific Msg */
    @Override
	public Msg parseMsgFromXml(ParseFromXmlContext context, Element root, Runner runner) throws Exception {

        Msg msg = super.parseMsgFromXml(context, root, runner);

        String channelName = root.attributeValue("channel");
        String transactionId = root.attributeValue("transactionId");
        if (null != channelName && existsChannel(channelName))
        {
            msg.setChannel(getChannel(channelName));
        }

        if(null != transactionId)
        {
            Trans trans = super.getInTransaction(new TransactionId(transactionId));
            msg.setChannel(trans.getBeginMsg().getChannel());

        }

        ChannelPop channel = (ChannelPop) msg.getChannel();

        if(channel.isServer())
        {
            channel.checkTransactionResponse(msg);
        }
        else
        {
            channel.checkTransactionRequest(msg);
        }

        return msg;
	}

    /** Send a Msg to Stack */
    @Override
    public synchronized boolean sendMessage(Msg msg) throws Exception {    	        
    	
    	// copy the channel from the request into the response using the transaction
        Trans trans = msg.getTransaction();       
        if (trans != null)
        {
        	Channel channel = trans.getBeginMsg().getChannel();
            msg.setChannel(channel);
        }
                
    	return super.sendMessage(msg);        
    }

    /**
     * Creates a Msg specific to each Channel type
     * should become ABSTRACT later
     */
    @Override
    public Msg readFromStream(InputStream inputStream, Channel channel) throws Exception
    {
        String text = null;
    	synchronized (inputStream)
    	{
			text = this.reader(inputStream, channel);
    	}
        
		if (text != null)
		{
			return new MsgPop(this, text, channel);
		}

        Tester.getGlobalLogger().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "Receive an incomplete message; we ignore it : ", text);
    	return null;
    }

    protected String reader(InputStream inputStream, Channel channel) throws Exception {
        StringBuilder buf = new StringBuilder();
        String tmp = null;
        boolean multiLine = false;

        tmp = Utils.readLineFromInputStream(inputStream);
        buf.append(tmp);
        multiLine = ((ChannelPop) channel).isNextReadMultiLine();
        
        if(!tmp.contains("+OK"))
        {
            multiLine = false;
        }

        while(multiLine)
        {
            tmp = Utils.readLineFromInputStream(inputStream);

            if(tmp.equalsIgnoreCase(".\r\n")) {
                multiLine = false;
            }
            else {
                if(tmp.equalsIgnoreCase("..\r\n"))
                {
                    tmp = tmp.substring(1);
                }
                buf.append(tmp);
            }
        }
        return buf.toString();
    }

//    @Override
    public Channel buildChannelFromSocket(Listenpoint listenpoint, Socket socket) throws Exception
    {
        ChannelPop channelPop = new ChannelPop(this, "Channel #" + Stack.nextTransactionId(), listenpoint, socket);
        return channelPop;
    }
}
