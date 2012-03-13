/*
 * SocketServerHttp.java
 *
 * Created on 21 juin 2007, 10:50
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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

    public void run()
    {
        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "ServerSocketHttp started");
        
        try
        {
            while (true)
            {
                GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "ServerSocketHttp waiting for header");
                
                HttpRequest request = defaultHttpServerConnection.receiveRequestHeader();
                
                if(request instanceof HttpEntityEnclosingRequest)
                {
                    GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "ServerSocketHttp receiving entity");
                    defaultHttpServerConnection.receiveRequestEntity((HttpEntityEnclosingRequest)request);
                }
                
                MsgHttp msgRequest = new MsgHttp(request);                
                //
                // Set the channel attached to the msg
                //
                msgRequest.setChannel(this.connHttp);
                
                synchronized(messagesReceived)
                {
                    messagesReceived.addLast(msgRequest);
                }                
                
                //
                // Call back to the generic stack
                //
                StackFactory.getStack(StackFactory.PROTOCOL_HTTP).receiveMessage(msgRequest);
            }
        }
        catch(Exception e)
        {
            if(messagesReceived.isEmpty())
            {
                GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, e, "Exception in ServerSocketHttp without pending messages");
            }
            else
            {
                GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, e, "Exception in ServerSocketHttp with pending messages");
            }
        }
        
        //
        // try to close itself
        //
        try
        {
            if(defaultHttpServerConnection.isOpen())
            {
                StackFactory.getStack(StackFactory.PROTOCOL_HTTP).closeChannel(this.connHttp.getName());
            }
        }
        catch(Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, e, "Error while closing connection ", this.connHttp);
        }
        
        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "ServerSocketHttp ended");
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
