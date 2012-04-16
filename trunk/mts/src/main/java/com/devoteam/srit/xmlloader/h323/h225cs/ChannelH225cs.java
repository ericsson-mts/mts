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

package com.devoteam.srit.xmlloader.h323.h225cs;

/**
 *
 * @author indiaye
 */
/*
 * ChannelUcp.java
 *
 */



import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;

import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.tcp.ChannelTcp;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ChannelH225cs extends Channel
{
    private ChannelTcp channel = null;

    // --- constructeur --- //
    public ChannelH225cs(String name, String aLocalHost, String aLocalPort, String aRemoteHost, String aRemotePort, String aProtocol) throws Exception {
    	super(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        channel = new ChannelTcp(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
    }

    public ChannelH225cs(String name, Listenpoint listenpoint, Socket socket) throws Exception
    {
        super(
                name,
                ((InetSocketAddress)socket.getLocalSocketAddress()).getAddress().getHostAddress(),
                Integer.toString(((InetSocketAddress)socket.getLocalSocketAddress()).getPort()),
                ((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress().getHostAddress(),
                Integer.toString(((InetSocketAddress)socket.getRemoteSocketAddress()).getPort()),
                listenpoint.getProtocol()
        );
        channel = new ChannelTcp(name, listenpoint, socket);
    }

    // --- basic methods --- //
    public boolean open() throws Exception {
        boolean result = channel.open();

        return result;
    }

    /** Send a Msg to Channel */
    public boolean sendMessage(Msg msg) throws Exception{
        if (null == channel)
            throw new Exception("Channel is null, has one channel been opened ?");

        if (msg.getChannel() == null)
            msg.setChannel(this);

        channel.sendMessage((MsgH225cs) msg);
        return true;
    }

    public boolean close(){
        try {
            channel.close();
        } catch (Exception e) {
            // nothing to do
        }
        channel = null;
        return true;
    }

    @Override
    public String getTransport() {
        return StackFactory.PROTOCOL_TCP;
    }

    public boolean isServer(){
        return (channel.getListenpointTcp() != null);
    }

}
