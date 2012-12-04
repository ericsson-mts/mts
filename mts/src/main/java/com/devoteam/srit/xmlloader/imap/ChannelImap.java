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

package com.devoteam.srit.xmlloader.imap;

import java.net.InetSocketAddress;
import java.net.Socket;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.TransactionId;
import com.devoteam.srit.xmlloader.tcp.ChannelTcp;
import com.devoteam.srit.xmlloader.tcp.ListenpointTcp;
import com.devoteam.srit.xmlloader.tcp.bio.ListenpointTcpBIO;
import com.devoteam.srit.xmlloader.tcp.nio.ListenpointTcpNIO;
import com.devoteam.srit.xmlloader.tls.ChannelTls;
import com.devoteam.srit.xmlloader.tls.ListenpointTls;

public class ChannelImap extends Channel
{
    private Channel channel = null;
        
    private TransactionId transId = null;
    private boolean transactionInProgress = false;
    
    private MsgImap welcomeMsg = null;
    private int nextNbCharToRead = 0;
    private boolean incompleteMessage = false;
    private String transport = null;

    private boolean isAuthenticateMecanismActually = false;
    
    /** Creates a new instance of Channel */
    public ChannelImap(String name, String aLocalHost, String aLocalPort,
            String aRemoteHost, String aRemotePort, String aProtocol, String aTransport) throws Exception
    {
        super(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        if(aTransport.equalsIgnoreCase("tcp"))
            channel = new ChannelTcp(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        else if(aTransport.equalsIgnoreCase("tls"))
            channel = new ChannelTls(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        transport = aTransport;
    }

    public ChannelImap(String name, Listenpoint listenpoint, Socket socket) throws Exception
    {
        super(name, 
            ((InetSocketAddress)socket.getLocalSocketAddress()).getAddress().getHostAddress(),
            Integer.toString(((InetSocketAddress)socket.getLocalSocketAddress()).getPort()),
            ((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress().getHostAddress(),
            Integer.toString(((InetSocketAddress)socket.getRemoteSocketAddress()).getPort()),
            listenpoint.getProtocol());

        if(listenpoint instanceof ListenpointTcp || listenpoint instanceof ListenpointTcpBIO || listenpoint instanceof ListenpointTcpNIO)
        {
            channel = new ChannelTcp(name, listenpoint, socket);
            transport = "tcp";
        }
        else if(listenpoint instanceof ListenpointTls)
        {
            channel = new ChannelTls(name, listenpoint, socket);
            transport = "tls";
        }
    }
    
    /** Send a Msg to Channel */
    public boolean sendMessage(Msg msg) throws Exception
    {
        if (null == channel)
            throw new ExecutionException("The ChannelTcp is null, has the channel been opened ?");
        
        if (msg.getChannel() == null)
            msg.setChannel(this);
        
        if(isServer())//pour un server (envoi d'une reponse)
        {
            if(isAuthenticateMecanismActually() && !((MsgImap)msg).getResult().equalsIgnoreCase(""))
                setIsAuthenticateMecanismActually(false);
        }
        else//pour un client (envoi d'une requete)
        {
            if(((MsgImap)msg).getType().equalsIgnoreCase("authenticate"))
                setIsAuthenticateMecanismActually(true);
        }

        channel.sendMessage((MsgImap) msg);
        return true;
    }

    /** receive a Msg from Channel */
    public boolean receiveMessage(Msg msg) throws Exception
    {
        if(isServer())//pour un server (reception d'une requete)
        {
            msg = checkTransationRequest(msg, channel.getRemoteHost() + channel.getRemotePort());
            if(((MsgImap)msg).getType().equalsIgnoreCase("authenticate"))
                setIsAuthenticateMecanismActually(true);
        }
        else//pour un client (reception d'une reponse)
        {
            //// part used for the welcome message ////
            if(transId == null)//first request has not occured, save welcome message
            {
                welcomeMsg = (MsgImap)msg;
                return false;//to not call receiveMessage and checkTransaction because it is not a transaction, exit
            }
            else if(welcomeMsg != null)//if a request has occured and first time we check the welcomeMsg
            {
                welcomeMsg.setTransactionId(transId);
                super.receiveMessage(welcomeMsg);
                welcomeMsg = null;
            }
            checkTransationResponse(msg, channel.getLocalHost() + channel.getLocalPort());
            if(isAuthenticateMecanismActually() && !((MsgImap)msg).getResult().equalsIgnoreCase(""))
                setIsAuthenticateMecanismActually(false);
        }
        return super.receiveMessage(msg);
    }

    public boolean open() throws Exception
    {
        boolean result = channel.open();
        
        if(result && isServer())
        {
            //send welcome message
            MsgImap msg = new MsgImap("* OK Welcome to M.T.S. (Multiprotocol Test Suite)", this);
            this.sendMessage(msg);
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
    	return transport;
    }

    public Channel getChannel() 
    {
        return channel;
    }

    public TransactionId getTransactionId() 
    {
        return transId;
    }

    protected Msg checkTransationRequest(Msg msg, String ipPort) throws Exception
    {
        if(incompleteMessage && (getTransactionId() != null))//if last message receive or sent was incomplete
        {
            //get last message, add this message to the old and sent just part of the new message
            //for the moment just set old tag to new message, which will be reused for transactionId
            MsgImap msgTmp;
            if(isServer())
            {
                msgTmp = (MsgImap) StackFactory.getStack(StackFactory.PROTOCOL_IMAP).getInTransaction(getTransactionId()).getBeginMsg();
            }
            else{
                msgTmp = (MsgImap) StackFactory.getStack(StackFactory.PROTOCOL_IMAP).getOutTransaction(getTransactionId()).getBeginMsg();
            }

            msgTmp.addNewMessage(((MsgImap)msg).getDataRaw());
            msg = msgTmp;
        }
        else
        {
            if(!((MsgImap)msg).getTag().equalsIgnoreCase(""))//if request and not provisionnal request
            {
                transId = new TransactionId(((MsgImap)msg).getTag() + ipPort);
                if(transactionInProgress)
                {
                    throw new ExecutionException("A transaction is already in progress, don't do several transactions simultaneously");
                }
            }
        }
        
        setIncompleteMessage(((MsgImap)msg).isIncompleteMessage());

        msg.setTransactionId(transId);
        transactionInProgress = true;
        return msg;
    }
    
    protected void checkTransationResponse(Msg msg, String ipPort) throws Exception
    {
        if(transId != null)
        {
            msg.setTransactionId(transId);
            String str = ((MsgImap)msg).getTag() + ipPort;
            if(str.equalsIgnoreCase(transId.toString()))
            {
                transactionInProgress = false;
            }
        }
    }

    public boolean isServer(){
        if(transport.equalsIgnoreCase("tcp"))
            return (((ChannelTcp)channel).getListenpointTcp() != null);
        else
            return (((ChannelTls)channel).getListenpointTLS() != null);
    }

    public synchronized int getAndResetNextNbCharToRead() {
        int value = nextNbCharToRead;
        nextNbCharToRead = 0;            
        return value;
    }

    public synchronized void setNextNbCharToRead(int nextNbCharToRead) {
        this.nextNbCharToRead = nextNbCharToRead;
    }

    public boolean isIncompleteMessage() {
        return incompleteMessage;
    }

    public void setIncompleteMessage(boolean requestIncomplete) {
        this.incompleteMessage = requestIncomplete;
    }

    public boolean isAuthenticateMecanismActually() {
        return isAuthenticateMecanismActually;
    }

    public void setIsAuthenticateMecanismActually(boolean isAuthenticateMecanismActually) {
        this.isAuthenticateMecanismActually = isAuthenticateMecanismActually;
    }
}
