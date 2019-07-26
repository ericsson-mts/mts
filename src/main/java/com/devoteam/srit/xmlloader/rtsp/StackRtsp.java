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

package com.devoteam.srit.xmlloader.rtsp;


import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.Tester;
import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Msg.ParseFromXmlContext;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.Trans;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.core.utils.XMLElementTextMsgParser;

import java.io.InputStream;

public class StackRtsp extends Stack 
{

    public int addCRLFContent = 0;

    
    /** Creates a new instance */
	public StackRtsp() throws Exception 
	{
		super();

        if(getConfig().getBoolean("equipment.ADD_CRLF_CONTENT", false))
        {
            this.addCRLFContent++;
        }
        this.addCRLFContent += getConfig().getInteger("protocol.ADD_SPECIFIC_CONTENT_CRLF", 0);
	}
    
	/** Creates a specific Msg */
	@Override
	public Msg parseMsgFromXml(ParseFromXmlContext context, Element root, Runner runner) throws Exception 
	{
        Msg msg = super.parseMsgFromXml(context, root, runner);
        
        String remoteHostAttr = root.attributeValue("remoteHost");
        if(remoteHostAttr != null)
        {
            msg.setRemoteHost(remoteHostAttr);
        }

        String remotePortAttr = root.attributeValue("remotePort");
        if(remotePortAttr != null)
        {
            msg.setRemotePort(Integer.parseInt(remotePortAttr));
        }
        
        String listenpointAttr = root.attributeValue("listenpoint");
        if(listenpointAttr != null)
        {
            Listenpoint listenpoint = getListenpoint(listenpointAttr);
            msg.setListenpoint(listenpoint);
            
            msg.setChannel(listenpoint.prepareChannel(msg, msg.getRemoteHost(), msg.getRemotePort(), msg.getTransport()));
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
     * Read the message data from the stream
     * Use for TCP/TLS like protocol : to build incoming message  
     */
    @Override
    public byte[] readMessageFromStream(InputStream inputStream) throws Exception
    {
        String text = null;
    	synchronized (inputStream)
    	{
			text = this.reader(inputStream);
    	}
    	
    	return text.getBytes();
    }

    protected String reader(InputStream inputStream) throws Exception {
		int lengthContent = 0;
        int pos = 0;
        StringBuilder message = new StringBuilder();
        String line = "1";

        while (line.length() > 0) //while line is not just \r\n
        {
            line = Utils.readLineFromInputStream(inputStream).trim();
            if (line.contains("Content-Length"))
            {
                pos = line.indexOf(":");
                lengthContent = Integer.valueOf(line.substring(pos + 1).trim());
            }
            message.append(line).append("\r\n");
        }

        if(lengthContent > 0){
            byte[] ch = new byte[lengthContent];
            inputStream.read(ch, 0, lengthContent);
            String s = new String(ch).trim();
            message.append(s);
        }
        
        return message.toString();
    }
}
