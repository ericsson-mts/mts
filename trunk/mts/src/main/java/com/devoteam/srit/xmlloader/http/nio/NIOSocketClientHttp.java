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
import org.apache.http.impl.DefaultHttpClientConnection;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;

import com.devoteam.srit.xmlloader.core.hybridnio.HybridSocketInputHandler;
import com.devoteam.srit.xmlloader.http.ChannelHttp;
import com.devoteam.srit.xmlloader.http.MsgHttp;
import com.devoteam.srit.xmlloader.http.SocketClientHttp;
import org.apache.http.HttpResponse;

/**
 *
 * @author gpasquiers
 */
public class NIOSocketClientHttp extends SocketClientHttp implements HybridSocketInputHandler
{
    private boolean init = false;
    
    /** Creates a new instance of SocketClientReceiver */
    public NIOSocketClientHttp()
    {}

    public void init(DefaultHttpClientConnection aClientConnection, ChannelHttp connHttp)
    {
        init = true;
        this.clientConnection = aClientConnection;
        this.connHttp = connHttp;
    }

    @Override
    public boolean handle(HybridSocket hybridSocket)
    {
        if(false == init) return true;
        
        try
        {
            if(clientConnection.isOpen())
            {
                HttpResponse response = clientConnection.receiveResponseHeader();
                clientConnection.receiveResponseEntity(response);

                MsgHttp msgResponse = new MsgHttp(response);

                //
                // Get corresponding msgRequest to read transactionId
                //
                if(isValid)
                {
                    MsgHttp msgRequest = requestsSent.take();
                    msgResponse.setTransactionId(msgRequest.getTransactionId());
                    msgResponse.setChannel(this.connHttp);
                    msgResponse.setType(msgRequest.getType());
                }

                //
                // Callback vers la Stack generic
                //
                StackFactory.getStack(StackFactory.PROTOCOL_HTTP).receiveMessage(msgResponse);
            }
            else
            {
                synchronized(this.connHttp)
                {
                    restoreConnection();
                }
            }
        }
        catch(Exception e)
        {
            if(requestsSent.isEmpty())
            {
                GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Exception in SocketClientHttp without pending messages");
            }
            else
            {
                GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Exception in SocketClientHttp with pending messages");
            }
            synchronized(this.connHttp)
            {
                try
                {
                    restoreConnection();
                }
                catch(Exception ignore) {}
            }
        }

        return !isShutdown;
    }

    public void init(HybridSocket hybridSocket)
    {
        // not used
    }
}
