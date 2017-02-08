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

package com.devoteam.srit.xmlloader.tcp;

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.tcp.bio.ChannelTcpBIO;
import com.devoteam.srit.xmlloader.tcp.bio.SocketTcpBIO;
import com.devoteam.srit.xmlloader.tcp.nio.ChannelTcpNIO;

import java.net.InetSocketAddress;
import java.net.Socket;

import org.dom4j.Element;

/**
 *
 * @author gpasquiers
 */
public class ChannelTcp extends Channel {

    private boolean nio = Config.getConfigByName("tcp.properties").getBoolean("USE_NIO", false);

    /** Creates a new instance of Channel*/
    public ChannelTcp(Stack stack)
    {
    	super(stack);
        if (nio) 
        {
            channel = new ChannelTcpNIO(stack);
        }
        else 
        {
            channel = new ChannelTcpBIO(stack);
        }
    }

    /** Creates a new instance of ChannelTcp */
    // for IMAP protocol : to delete ?
    public ChannelTcp(String name, String aLocalHost, String aLocalPort, String aRemoteHost, String aRemotePort, String aProtocol) throws Exception 
    {
        super(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        if (nio) 
        {
            channel = new ChannelTcpNIO(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        }
        else 
        {
            channel = new ChannelTcpBIO(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        }
    }

    /** Creates a new instance of ChannelTcp */
    public ChannelTcp(String name, Listenpoint listenpoint, Socket socket) throws Exception 
    {
        super(name,
                ((InetSocketAddress) socket.getLocalSocketAddress()).getAddress().getHostAddress(),
                Integer.toString(((InetSocketAddress) socket.getLocalSocketAddress()).getPort()),
                ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().getHostAddress(),
                Integer.toString(((InetSocketAddress) socket.getRemoteSocketAddress()).getPort()),
                listenpoint.getProtocol());
        if (nio) 
        {
            channel = new ChannelTcpNIO(name, listenpoint, socket);
        }
        else 
        {
            channel = new ChannelTcpBIO(name, listenpoint, socket);
        }
    }

    
    //---------------------------------------------------------------------
    // methods for the transport
    //---------------------------------------------------------------------

    /** Open a channel */
    @Override
    public boolean open() throws Exception {
        return channel.open();
    }

    /** Close a channel */
    @Override
    public boolean close() {
        return channel.close();
    }

    /** Send a Msg to Channel */
    @Override
    public synchronized boolean sendMessage(Msg msg) throws Exception {
        return channel.sendMessage(msg);
    }

    /** Get the transport protocol */
    @Override
    public String getTransport() 
    {
        return StackFactory.PROTOCOL_TCP;
    }

    public Listenpoint getListenpointTcp() 
    {
        if(nio)
        {
            return ((ChannelTcpNIO) channel).getListenpointTcp();
        }
        else
        {
            return ((ChannelTcpBIO) channel).getListenpointTcp();
        }
    }

    public SocketTcpBIO getSocketTcp() 
    {
        if(nio)
        {
            throw new RuntimeException("probably not supported with NIO");
        }
        else
        {
            return ((ChannelTcpBIO) channel).getSocketTcp();
        }
    }
    
    
    //---------------------------------------------------------------------
    // methods for the XML display / parsing
    //---------------------------------------------------------------------

    /** 
     * Returns the string description of the message. Used for logging as DEBUG level 
     */
    @Override
    public String toString()
    {
        return channel.toString();
    }

    /** 
     * Parse the channel from XML element 
     */
    @Override
    public void parseFromXml(Element root, Runner runner, String protocol) throws Exception
    {
    	super.parseFromXml(root, runner, StackFactory.PROTOCOL_TCP);
    	this.channel.parseFromXml(root, runner, StackFactory.PROTOCOL_TCP);
    }

    
    /** clone method */
    @Override
    public void clone(Channel channel)
    {
        this.channel.clone(channel);
    }
    
    /** equals method */
    @Override
    public boolean equals(Channel channel)
    {
        return this.channel.equals(channel);
    }

}
