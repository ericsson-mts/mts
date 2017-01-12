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

public class StackPcp extends Stack 
{

	/** Creates a new instance */
    public StackPcp() throws Exception 
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
            return new ChannelPcp(this, name, localHost, localPort, remoteHost, remotePort, protocol, infranetConnection, loginType);
        }
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
        MsgPcp msg = new MsgPcp(this);
        msg.setFList(flist);
        return msg;
    }

}
