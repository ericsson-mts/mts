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


import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.tcp.bio.ChannelTcpBIO;
import com.devoteam.srit.xmlloader.tcp.nio.ChannelTcpNIO;
import com.devoteam.srit.xmlloader.udp.bio.ChannelUdpBIO;
import com.devoteam.srit.xmlloader.udp.nio.ChannelUdpNIO;

public class ChannelUdp extends Channel
{
    private boolean nio = Config.getConfigByName("udp.properties").getBoolean("USE_NIO", false);
    
    /** Creates a new instance of Channel*/
    public ChannelUdp(Stack stack)
    {
    	super(stack);
        if (nio) 
        {
            channel = new ChannelUdpNIO(stack);
        }
        else 
        {
            channel = new ChannelUdpBIO(stack);
        }
    }

    public String getName()
    {
        return channel.getName();
    }

    public String getUID()
    {
        return channel.getUID();
    }

    public int getLocalPort()
    {
        return channel.getLocalPort();
    }

    public String getLocalHost()
    {
        return channel.getLocalHost();
    }

    public int getRemotePort()
    {
        return channel.getRemotePort();
    }

    public String getRemoteHost()
    {
        return channel.getRemoteHost();
    }

    public String getProtocol()
    {
        return channel.getProtocol();
    }

    
    //---------------------------------------------------------------------
    // methods for the transport
    //---------------------------------------------------------------------

    /** Open a channel */
    @Override
    public boolean open() throws Exception
    {
        return channel.open();
    }

    /** Close a channel */
    @Override
    public boolean close()
    {
        return channel.close();
    }

    /** Send a Msg through the channel */
    @Override
    public boolean sendMessage(Msg msg) throws Exception
    {
        return channel.sendMessage(msg);
    }

    /** Receive a Msg from the channel */
    @Override
    public boolean receiveMessageNIO(Msg msg) throws Exception
    {
        return channel.receiveMessageNIO(msg);
    }

    /** Receive a Msg from the channel */
    @Override
    public boolean receiveMessage(Msg msg) throws Exception
    {
        return channel.receiveMessage(msg);
    }

    /** Get the transport protocol */
    @Override
    public String getTransport()
    {
        return channel.getTransport();
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
    public void parseFromXml(Element root, Runner runner, String protocool) throws Exception
    {
    	super.parseFromXml(root, runner, StackFactory.PROTOCOL_UDP);
    	this.channel.parseFromXml(root, runner, StackFactory.PROTOCOL_UDP);
    }

    
    //------------------------------------------------------
    // method for the "setFromMessage" <parameter> operation
    //------------------------------------------------------
    
    /** 
     * Get a parameter from the message 
     */
    @Override
    public Parameter getParameter(String path) throws Exception
    {
        return channel.getParameter(path);
    }

    /** clone method */
    @Override
    public void clone(Channel channel)
    {
    	super.clone(channel);
        this.channel.clone(channel);
    }

    /** equals method */
    @Override
    public boolean equals(Channel channel)
    {
        return this.channel.equals(channel);
    }

}
