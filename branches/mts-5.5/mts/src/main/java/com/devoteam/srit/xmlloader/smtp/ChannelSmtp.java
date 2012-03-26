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

package com.devoteam.srit.xmlloader.smtp;

//import java.net.InetSocketAddress;
//import java.net.Socket;

import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.TransactionId;
import com.devoteam.srit.xmlloader.tcp.ChannelTcp;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ChannelSmtp extends Channel
{
    private ChannelTcp channel = null;
    private TransactionId transID;
    private String typeSent;

    /** Creates a new instance of ChannelSmtp */
    public ChannelSmtp(String name, String aLocalHost, String aLocalPort,
            String aRemoteHost, String aRemotePort, String aProtocol) throws Exception
    {
        super(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        channel = new ChannelTcp(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        transID = null;
        typeSent = null;
    }

    public ChannelSmtp(String name, Listenpoint listenpoint, Socket socket) throws Exception
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
        transID = null;
        typeSent = null;
    }

    /** Send a Msg to Channel */
    public boolean sendMessage(Msg msg) throws Exception
    {
        if (null == channel)
        {
            throw new ExecutionException(
                    "Channel is null, has the connection been opened ?");
        }
        if (msg.getChannel() == null)
            msg.setChannel(this);

        if (msg.isRequest())
        {
            this.transID = msg.getTransactionId();
            this.typeSent = msg.getType();
        }
        channel.sendMessage((MsgSmtp) msg);
        return true;
    }

    /** receive a Msg from Channel */
    @Override
    public boolean receiveMessage(Msg msg) throws Exception
    {
        if (typeSent != null)
        {
            ((MsgSmtp)msg).setType(typeSent);
        }
        if (transID != null)
        {
            msg.setTransactionId(transID);
        }
        return super.receiveMessage(msg);
    }

    public boolean open() throws Exception
    {
        boolean result = channel.open();

        if(result && isServer())
        {
            String welcomeMsg = StackFactory.getStack(getProtocol()).getConfig().getString("server.WELCOME_MESSAGE");
            if(!welcomeMsg.equalsIgnoreCase(""))
            {
                //send welcome message
                MsgSmtp msg = new MsgSmtp(welcomeMsg);
                channel.sendMessage(msg);
            }
        }
        return result;
    }

    public boolean close()
    {
        try
        {
            channel.close();
        }
        catch (Exception e)
        {
            // Nothing to do
        }
        channel = null;
        return true;
    }
    
    /** Get the transport protocol of this message */
    public String getTransport() 
    {
    	return StackFactory.PROTOCOL_TCP;
    }

    public boolean isServer(){
        return (channel.getListenpointTcp() != null);
    }
}
