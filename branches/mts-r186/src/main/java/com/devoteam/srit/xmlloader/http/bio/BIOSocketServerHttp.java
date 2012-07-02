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
