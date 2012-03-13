/*
 * SocketClientReceiver.java
 *
 * Created on 26 juin 2007, 10:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.http;

import org.apache.http.impl.DefaultHttpClientConnection;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;

import java.util.concurrent.LinkedBlockingQueue;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;

/**
 *
 * @author gpasquiers
 */
public class SocketClientHttp
{
    protected LinkedBlockingQueue<MsgHttp> requestsSent;
    protected DefaultHttpClientConnection clientConnection;

    protected ChannelHttp connHttp;

    protected boolean isValid = true;
    protected boolean isReconnected = false;
    protected boolean isShutdown = false;
    
    /** Creates a new instance of SocketClientReceiver */
    public SocketClientHttp()
    {
        this.requestsSent = new LinkedBlockingQueue();
    }

    /**
     * Restore the connection if necessary (pending requests) and send those requests again.
     */
    protected void restoreConnection() throws Exception
    {
        if(isReconnected || isShutdown) return;

        isValid = false;
        try
        {
            clientConnection.close();
        }
        catch(Exception ignore){}

        if(!requestsSent.isEmpty())
        {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "Restoring connection and re-sending pending requests");
            try
            {
                this.connHttp.close();
                Thread.currentThread().interrupted();
                this.connHttp.open();
                isReconnected = true;
                while(!this.requestsSent.isEmpty())
                {
                    MsgHttp msg = this.requestsSent.poll();
                    this.connHttp.sendMessage(msg);
                }
            }
            catch(Exception e)
            {
                GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, e, "Exception reconnecting SocketClientHttp with pending messages");
                StackFactory.getStack(StackFactory.PROTOCOL_HTTP).closeChannel(this.connHttp.getName());
                throw e;
            }
        }
    }

    public void sendMessage(MsgHttp msg) throws Exception
    {
        this.requestsSent.offer(msg);

        try
        {
            if (msg.getMessage() instanceof HttpEntityEnclosingRequest)
            {
                HttpEntityEnclosingRequest httpEntityEnclosingRequest = (HttpEntityEnclosingRequest) msg.getMessage();
                clientConnection.sendRequestHeader(httpEntityEnclosingRequest);
                clientConnection.sendRequestEntity(httpEntityEnclosingRequest);
                if (httpEntityEnclosingRequest.getEntity() == null)
                {
                    clientConnection.flush();
                }
            }
            else
            {
                clientConnection.sendRequestHeader((HttpRequest) msg.getMessage());
                clientConnection.flush();
            }
        }
        catch(Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Error while sending message");

            synchronized(this.connHttp)
            {
                this.restoreConnection();
            }
        }
    }

    public void shutdown() throws Exception
    {
        isShutdown = true;
        clientConnection.close();
    }

}
