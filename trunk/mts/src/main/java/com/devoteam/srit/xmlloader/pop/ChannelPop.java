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

import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;

import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.TransactionId;
import com.devoteam.srit.xmlloader.tcp.ChannelTcp;
import java.net.InetSocketAddress;
import java.net.Socket;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;

public class ChannelPop extends Channel
{
    private ChannelTcp channel = null;

    private TransactionId transId = null;
    private boolean transactionInProgress = false;
    private boolean waitWelcomeMessage = false;
    private boolean nextReadMultiLine = false;
    
    // --- constructeur --- //
    public ChannelPop(String name, String aLocalHost, String aLocalPort, String aRemoteHost, String aRemotePort, String aProtocol) throws Exception {
    	super(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        channel = new ChannelTcp(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        waitWelcomeMessage = StackFactory.getStack(getProtocol()).getConfig().getBoolean("client.WAIT_WELCOME_MESSAGE");
    }

    public ChannelPop(String name, Listenpoint listenpoint, Socket socket) throws Exception
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
        waitWelcomeMessage = StackFactory.getStack(getProtocol()).getConfig().getBoolean("client.WAIT_WELCOME_MESSAGE");
    }

    // --- basic methods --- //
    public boolean open() throws Exception {
        boolean result = channel.open();

        if(result && isServer())
        {
            String welcomeMsg = StackFactory.getStack(getProtocol()).getConfig().getString("server.WELCOME_MESSAGE");
            if(!welcomeMsg.equalsIgnoreCase(""))
            {
                //send welcome message
                MsgPop msg = new MsgPop(welcomeMsg, this);
                channel.sendMessage(msg);
            }
        }
        return result;
    }
    
    /** Send a Msg to Channel */
    public boolean sendMessage(Msg msg) throws Exception{ 
        if (null == channel)
            throw new Exception("Channel is null, has one channel been opened ?");

            if(isServer())
            {
            }
            else
            {
                setNextReadMultiLine(((MsgPop)msg).shouldResponseBeMultiLine());
            }
        
        channel.sendMessage((MsgPop) msg);
        return true;
    }

    /** receive a Msg from Channel */
    @Override
    public boolean receiveMessage(Msg msg) throws Exception
    {
        if(isServer())//pour un server (reception d'une requete)
        {
            checkTransactionRequest(msg);
        }
        else//pour un client (reception d'une reponse)
        {
            //// part used for the welcome message ////
            if(waitWelcomeMessage)//first request has not occured
            {
                waitWelcomeMessage = false;
                return true;
            }            
            checkTransactionResponse(msg);
        }
        return super.receiveMessage(msg);
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

    public boolean isNextReadMultiLine() {
        return nextReadMultiLine;
    }
    
    public void setNextReadMultiLine(boolean value) {
        nextReadMultiLine = value;
    }

    public void checkTransactionRequest(Msg msg) throws Exception
    {
        if(!transactionInProgress)
        {
            transId = msg.getTransactionId();
            transactionInProgress = true;
        }
        else
        {
            throw new ExecutionException("A transaction is already in progress, don't do several transactions simultaneously");
        }
    }

    public void checkTransactionResponse(Msg msg) throws Exception
    {
        if(transactionInProgress)
        {
            msg.setTransactionId(transId);
            transactionInProgress = false;
        }
        else
        {
            throw new ExecutionException("No transaction are started");
        }
    }
}
