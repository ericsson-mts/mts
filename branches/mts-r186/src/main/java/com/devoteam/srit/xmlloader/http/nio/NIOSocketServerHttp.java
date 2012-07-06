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

package com.devoteam.srit.xmlloader.http.nio;

import com.devoteam.srit.xmlloader.core.hybridnio.HybridSocket;
import org.apache.http.impl.DefaultHttpServerConnection;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.hybridnio.HybridSocketInputHandler;
import com.devoteam.srit.xmlloader.http.MsgHttp;
import com.devoteam.srit.xmlloader.http.SocketServerHttp;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;


/***
 * 
 * @author gpasquiers
 */
public class NIOSocketServerHttp extends SocketServerHttp implements HybridSocketInputHandler
{            
    private boolean init = false;
    private boolean _continue = true;

    public NIOSocketServerHttp()
    {}

    public void init(DefaultHttpServerConnection aDefaultHttpServerConnection, NIOChannelHttp connHttp)
    {
        this.defaultHttpServerConnection = aDefaultHttpServerConnection;
        this.connHttp = connHttp;
        this.init = true;
    }

    @Override
    public void shutdown() throws Exception
    {
        _continue = false;
        defaultHttpServerConnection.close();
    }

    @Override
    public boolean handle(HybridSocket hybridSocket)
    {
        if(false == init) return true;
        
        try
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

            //
            // try to close itself
            //
            try
            {
                synchronized (this)
                {
                    StackFactory.getStack(StackFactory.PROTOCOL_HTTP).closeChannel(this.connHttp.getName());
                }
            }
            catch(Exception ee)
            {
                GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, ee, "Error while closing connection ", this.connHttp);
            }

            GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "ServerSocketHttp ended");
        }

        return _continue;
    }

    public void init(HybridSocket hybridSocket)
    {
        // not used
    }
}
