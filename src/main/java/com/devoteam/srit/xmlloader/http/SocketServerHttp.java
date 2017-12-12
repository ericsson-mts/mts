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

package com.devoteam.srit.xmlloader.http;

import org.apache.http.impl.DefaultHttpServerConnection;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.TransactionId;

import java.util.HashMap;
import java.util.LinkedList;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;


/***
 * 
 * @author gpasquiers
 */
public class SocketServerHttp
{
    protected DefaultHttpServerConnection defaultHttpServerConnection;
    
    protected LinkedList<MsgHttp> messagesReceived;
    
    protected HashMap<TransactionId, MsgHttp> messagesToSend;
    
    protected ChannelHttp connHttp;

    public SocketServerHttp()
    {
        super();
        this.messagesReceived = new LinkedList<MsgHttp>();
        this.messagesToSend = new HashMap<TransactionId, MsgHttp>();
    }

    public synchronized void sendMessage(MsgHttp msgHttp) throws ExecutionException
    {
        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "SocketServerHttp: sendMessage() ", msgHttp);
        try
        {
            MsgHttp requestToBeAnswered = messagesReceived.getFirst();
            if(requestToBeAnswered.getTransactionId().equals(msgHttp.getTransactionId()))
            {
                synchronized(messagesReceived)
                {
                    messagesReceived.removeFirst();
                }
                doSendMessage(msgHttp);
                
                //
                // Check if we can now send other messages
                //
                if(messagesToSend.size() > 0)
                {
                    synchronized(messagesReceived)
                    {
                        requestToBeAnswered = messagesReceived.getFirst();
                    }
                    
                    while(messagesToSend.containsKey(requestToBeAnswered.getTransactionId()))
                    {
                        MsgHttp msg = messagesToSend.remove(requestToBeAnswered.getTransactionId());
                        synchronized(messagesReceived)
                        {
                            messagesReceived.removeFirst();
                        }
                        doSendMessage(msg);
                        
                        if(messagesReceived.size() > 0)
                        {
                            synchronized(messagesReceived)
                            {
                                requestToBeAnswered = messagesReceived.getFirst();
                            }
                        }
                    }
                }
            }
            else
            {
                messagesToSend.put(msgHttp.getTransactionId(), msgHttp);
                GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "SocketServerHttp: queued message for later send ", msgHttp);
            }
        }
        catch(Exception e)
        {
            //
            // try to close itself
            //
            try
            {
                StackFactory.getStack(StackFactory.PROTOCOL_HTTP).closeChannel(this.connHttp.getName());
            }
            catch(Exception ee)
            {
                GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, ee, "Error while closing connection ", this.connHttp);
            }

            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "ServerSocketHttp ended", e);

            throw new ExecutionException("Error in serversocketHttp while sending answer : " + msgHttp, e);
        }
    }
    
    protected void doSendMessage(MsgHttp msgHttp) throws Exception
    {
        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "SocketServerHttp: doSendMessage() ", msgHttp);

        synchronized(this)
        {
            HttpResponse response = (HttpResponse) msgHttp.getMessage();
            defaultHttpServerConnection.sendResponseHeader(response);
            defaultHttpServerConnection.sendResponseEntity(response);
            defaultHttpServerConnection.flush();
        }
    }

    public void shutdown() throws Exception
    {
        defaultHttpServerConnection.close();
    }

}
