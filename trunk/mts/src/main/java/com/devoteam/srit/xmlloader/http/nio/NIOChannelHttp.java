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
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.http.ChannelHttp;
import com.devoteam.srit.xmlloader.http.StackHttp;
import java.net.InetSocketAddress;
import java.net.Socket;
import org.apache.http.impl.DefaultHttpClientConnection;
import org.apache.http.params.BasicHttpParams;

/**
 *
 * @author gpasquiers
 */
public class NIOChannelHttp extends ChannelHttp
{
    
    public NIOChannelHttp(String name, String localHost, String localPort, String remoteHost, String remotePort, String aProtocol, boolean secure) throws Exception
    {
        super(name, localHost, localPort, remoteHost, remotePort, aProtocol, secure);
    }
    
    /** Open a connexion to each Stack */
    public synchronized boolean open() throws Exception
    {
    	if (this.secure)
    	{
    		StatPool.beginStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_TLS, StackFactory.PROTOCOL_HTTP);
    	}
    	else
    	{
    		StatPool.beginStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_TCP, StackFactory.PROTOCOL_HTTP);
    	}
    	
        if(null == this.socketServerHttp)
        {
            nbOpens++;

            if(nbOpens > 3)
            {
                throw new Exception("HTTP Channel already failed to open more than 3 times");
            }

            String host = this.getRemoteHost();
            int port = this.getRemotePort();
            
            DefaultHttpClientConnection defaultHttpClientConnection = new DefaultHttpClientConnection();

            //
            // Bind the socket to the local address
            //
            String localHost = this.getLocalHost();
            int localPort = initialLocalport;
            
            if(null == localHost) localHost = "0.0.0.0";

            InetSocketAddress localsocketAddress = new InetSocketAddress(localHost, localPort);
            InetSocketAddress remoteAddress = new InetSocketAddress(host, port);

            this.socketClientHttp = new NIOSocketClientHttp();
            Socket socket = new HybridSocket((NIOSocketClientHttp)this.socketClientHttp);

            if(secure) StackHttp.ioReactor.openTLS(localsocketAddress, remoteAddress, (HybridSocket) socket, StackHttp.context);
            else StackHttp.ioReactor.openTCP(localsocketAddress, remoteAddress, (HybridSocket) socket);

            defaultHttpClientConnection.bind(socket, new BasicHttpParams());
            ((NIOSocketClientHttp)this.socketClientHttp).init(defaultHttpClientConnection, this);
            
    		// read all properties for the TCP socket 
    		Config.getConfigForTCPSocket(socket, false);            
        }
        return true;
    }

    
    /** Close a connexion */
    @Override
    public boolean close()
    {
    	if (this.secure)
    	{
    		StatPool.endStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_TLS, StackFactory.PROTOCOL_HTTP, startTimestamp);
    	}
    	else
    	{
    		StatPool.endStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_TCP, StackFactory.PROTOCOL_HTTP, startTimestamp);
    	}
    	    	        	
    	return super.close();
    }
}
