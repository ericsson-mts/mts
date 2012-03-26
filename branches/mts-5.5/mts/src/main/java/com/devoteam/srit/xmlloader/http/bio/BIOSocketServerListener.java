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
import com.devoteam.srit.xmlloader.core.ThreadPool;
import org.apache.http.impl.DefaultHttpServerConnection;
import java.net.ServerSocket;
import java.net.Socket;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.http.SocketServerListener;
import com.devoteam.srit.xmlloader.http.StackHttp;
import java.net.InetSocketAddress;
import org.apache.http.params.HttpParams;
import org.apache.http.params.BasicHttpParams;

/**
 *
 * @author sngom
 */
public class BIOSocketServerListener extends SocketServerListener implements Runnable
{
    private ServerSocket serverSocket;

    private long startTimestamp = 0;
    
    /** Creates a new instance of SocketServerHttpListener */
    public BIOSocketServerListener(int port, boolean secure) throws ExecutionException
    {
        super(secure);
        
    	if (secure)
    	{
    		StatPool.beginStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_TLS, StackFactory.PROTOCOL_HTTP);
    	}
    	else
    	{
    		StatPool.beginStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_TCP, StackFactory.PROTOCOL_HTTP);
    	}
		this.startTimestamp = System.currentTimeMillis();
        
        try
        {
            if(secure) serverSocket = StackHttp.context.getServerSocketFactory().createServerSocket(port);
            else  serverSocket = new ServerSocket(port);

            ThreadPool.reserve().start(this);
        }
        catch(Exception e)
        {
            throw new ExecutionException("Can't instantiate the HTTP SocketServerListener secure="+secure, e);
        }
    }
    
    public void run()
    {
        try
        {
            while (true)
            {
                //
                // Set up HTTP connection
                //
                GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "SocketServerListener secure=", secure, "waiting for a connection on socket");
                Socket socket = serverSocket.accept();
                GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "SocketServerListener secure=", secure, "got a connection");
                
                DefaultHttpServerConnection serverConnection = new DefaultHttpServerConnection();
                HttpParams params = new BasicHttpParams();
                serverConnection.bind(socket, params);
                
                InetSocketAddress remoteInetSocketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
                InetSocketAddress localInetSocketAddress = (InetSocketAddress) socket.getLocalSocketAddress();
                
                String connectionName = "HTTPServerConnection" + Stack.nextTransactionId();
                String remoteHost = remoteInetSocketAddress.getAddress().getHostAddress();
                String remotePort = Integer.toString(remoteInetSocketAddress.getPort());
                String localHost = localInetSocketAddress.getAddress().getHostAddress();
                String localPort = Integer.toString(localInetSocketAddress.getPort());
                
                BIOChannelHttp connHttp = new BIOChannelHttp(connectionName, localHost, localPort, remoteHost, remotePort, StackFactory.PROTOCOL_HTTP, secure);
                
                //
                // Start Server thread
                //
                BIOSocketServerHttp socketServerHttp = new BIOSocketServerHttp(serverConnection, connHttp);

                connHttp.setSocketServerHttp(socketServerHttp);
                StackFactory.getStack(StackFactory.PROTOCOL_HTTP).openChannel(connHttp);
            }
        }
        catch(Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, e, "Exception in SocketServerListener secure=" + secure);
        }
        
        GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "SocketServerListener secure=", secure, "stopped");
    }
    
    @Override
    public void shutdown()
    {
    	if (secure)
    	{
    		StatPool.endStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_TLS, StackFactory.PROTOCOL_HTTP, startTimestamp);
    	}
    	else
    	{
    		StatPool.endStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_TCP, StackFactory.PROTOCOL_HTTP, startTimestamp);
    	}
    	
        //
        // Stop listening
        //
        try
        {
            serverSocket.close();
        }
        catch(Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Error while closing SocketServerListener's socket secure=" + secure);
        }
    }
}
