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

package com.devoteam.srit.xmlloader.pcp;


import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.Runner;
import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.core.utils.XMLElementTextMsgParser;

import com.devoteam.srit.xmlloader.tcp.ChannelTcp;
import com.portal.pcm.EBufException;
import com.portal.pcm.FList;
import com.portal.pcm.PCPServerOperation;
import com.portal.pcm.XMLToFlist;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.xml.sax.InputSource;

public class StackPcp extends Stack {

    public StackPcp() throws Exception {
		super();

        // initiate a default listenpoint if port is not empty or null
        int port = getConfig().getInteger("listenpoint.LOCAL_PORT", 0);
        if (port > 0)
        {
        	Listenpoint listenpoint = new ListenpointPcp(this);
            createListenpoint(listenpoint, StackFactory.PROTOCOL_PCP);
        }
	}

    /** Creates a Listenpoint specific to each Stack */
    @Override
    public Listenpoint parseListenpointFromXml(Element root) throws Exception 
    { 
        Listenpoint listenpoint = new ListenpointPcp(this, root);
        return listenpoint;        
    }

    /** Creates a Channel specific to each Stack */
    @Override
	public synchronized Channel parseChannelFromXml(Element root, String protocol) throws Exception {
		String name = root.attributeValue("name");
		String localHost = root.attributeValue("localHost");
		String localPort = root.attributeValue("localPort");
        String remoteHost = root.attributeValue("remoteHost");
		String remotePort = root.attributeValue("remotePort");
        String infranetConnection = root.attributeValue("infranetConnection");
        String loginType = root.attributeValue("loginType");

        if( ((infranetConnection != null) && (loginType == null))
           || ((infranetConnection == null) && (loginType != null)) )
            throw new Exception("infranetConnection and loginType must be set both or not set.");

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
            return new ChannelPcp(name, localHost, localPort, remoteHost, remotePort, protocol, infranetConnection, loginType);
        }
    }
    
	/** Creates a specific Msg */
	public Msg parseMsgFromXml(Boolean request, Element root, Runner runner) throws Exception {
        XMLToFlist xmltoflist = XMLToFlist.getInstance();
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + root.element("flist").asXML();

        InputSource src = new InputSource(new ByteArrayInputStream(xml.getBytes()));
        xmltoflist.convert(src);
        
        return new MsgPcp(xmltoflist.getFList());
	}

	/** Returns the Config object to access the protocol config file*/
	public Config getConfig() throws Exception {
		return Config.getConfigByName("pcp.properties");
	}

	/** Returns the replacer used to parse sendMsg Operations */
	public XMLElementReplacer getElementReplacer() {
		return XMLElementTextMsgParser.instance();
	}

    /**
     * Creates a Msg specific to each Channel type
     * should become ABSTRACT later
     */
    @Override
    public Msg readFromStream(InputStream inputStream, Channel channel) throws Exception
    {
        FList flist = null;
        if(((ChannelTcp)channel).getListenpointTcp() != null)//server 
        {
            try {
                PCPServerOperation servOp = (PCPServerOperation)((ChannelTcp)channel).getListenpointTcp().getAttachment();
                if(servOp == null) {
                       servOp = new PCPServerOperation(((ChannelTcp)channel).getSocketTcp().getSocket());
                       ((ChannelTcp)channel).getListenpointTcp().setAttachment(servOp);
                }
                synchronized (inputStream)
                {
                    flist = servOp.receive();
                }
            } catch (EBufException ex) {
                ex.printStackTrace();
                System.out.println("exception catched in receive in server case");
                throw new Exception("End of stream detected");
            }
        }
        else//client
        {
            System.out.println("receive in client case");
        }
        return new MsgPcp(flist);
    }

}
