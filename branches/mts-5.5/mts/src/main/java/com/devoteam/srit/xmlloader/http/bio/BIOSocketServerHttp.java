/*
 * SocketServerHttp.java
 *
 * Created on 21 juin 2007, 10:50
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.http.bio;
import org.apache.http.impl.DefaultHttpServerConnection;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;

import com.devoteam.srit.xmlloader.http.MsgHttp;
import com.devoteam.srit.xmlloader.http.SocketServerHttp;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
/**
 *
 * @author sngom
 */

public class BIOSocketServerHttp extends SocketServerHttp implements Runnable
{        
    public BIOSocketServerHttp(DefaultHttpServerConnection aDefaultHttpServerConnection, BIOChannelHttp connHttp)
    {
        this.defaultHttpServerConnection = aDefaultHttpServerConnection;
        this.connHttp = connHttp;
    }
    
    public void run()
    {
        GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "ServerSocketHttp started");
        
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
                // Set the connection attached to the msg
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
                GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Exception in ServerSocketHttp without pending messages");
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
        
        GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "ServerSocketHttp ended");
    }
    
}
