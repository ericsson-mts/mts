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

import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.msrp.MsgMsrp;
import com.devoteam.srit.xmlloader.sctp.ChannelSctp;
import com.devoteam.srit.xmlloader.tcp.ChannelTcp;
import com.devoteam.srit.xmlloader.tls.ChannelTls;
import com.devoteam.srit.xmlloader.udp.ChannelUdp;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.dom4j.Element;

public class ChannelSigtran extends Channel
{
    private Channel channel = null;
    private String transport = null;
    
    /** Creates a new instance of Channel*/
    public ChannelSigtran(Stack stack)
    {
        super(stack);
    }

    // --- basic methods --- //
    public boolean open() throws Exception 
    {
        return channel.open();
    }
    
    /** Send a Msg to Channel */
    public boolean sendMessage(Msg msg) throws Exception{ 
        if (null == channel)
            throw new Exception("Channel is null, has one channel been opened ?");

        if (msg.getChannel() == null)
            msg.setChannel(this);

        channel.sendMessage(msg);

        return true;
    }
    
    /** 
     * Parse the channel from XML element 
     */
    public void parseChannelFromXml(Element root, String protocol) throws Exception
    {
    	super.parseChannelFromXml(root, protocol);
    	
        String transport = root.attributeValue("transport");
        if (transport == null)
        {
        	transport = stack.getConfig().getString("listenpoint.TRANSPORT");
        }
        this.transport = transport.toUpperCase(); 
        if (transport.equalsIgnoreCase(StackFactory.PROTOCOL_TCP))
        {
        	channel = new ChannelTcp(this.name, this.localHost, new Integer(this.localPort).toString(), this.remoteHost, new Integer(this.remotePort).toString(), this.protocol);
        }
        else if (transport.equalsIgnoreCase(StackFactory.PROTOCOL_TLS))
        {
        	channel = new ChannelTls(name, this.localHost, new Integer(this.localPort).toString(), this.remoteHost, new Integer(this.remotePort).toString(), this.protocol);
        }
        else if (transport.equalsIgnoreCase(StackFactory.PROTOCOL_SCTP))
        {
        	channel = new ChannelSctp(name, this.localHost, new Integer(this.localPort).toString(), this.remoteHost, new Integer(this.remotePort).toString(), this.protocol);
        }
        else if (transport.equalsIgnoreCase(StackFactory.PROTOCOL_UDP))
        {
        	channel = new ChannelUdp(name, this.localHost, new Integer(this.localPort).toString(), this.remoteHost, new Integer(this.remotePort).toString(), this.protocol, true);
        }
        else
        {
        	throw new Exception("openChannelSIGTRAN operation : Bad transport value for " + transport);
        }
    }

    public boolean close(){
        try 
        {
        	channel.close();
        } 
        catch (Exception e) 
        {
            // nothing to do
        }
        channel = null;
        return true;
    }
    
    /** Get the transport protocol of this message */
    public String getTransport() 
    {
        return transport;
    }
    
}
