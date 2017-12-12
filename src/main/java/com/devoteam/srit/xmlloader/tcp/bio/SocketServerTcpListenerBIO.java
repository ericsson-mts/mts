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

package com.devoteam.srit.xmlloader.tcp.bio;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Stack;

import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.tcp.StackTcp;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 *
 * @author sngom
 */
public class SocketServerTcpListenerBIO extends Thread
{
    private ServerSocket serverSocket;
    
    private ListenpointTcpBIO listenpoint;
    
    /**
     * Creates a new instance of SocketServerTcpListener
     */
    public SocketServerTcpListenerBIO(ListenpointTcpBIO listenpoint) throws ExecutionException
    {
    	int port = 0;
    	InetAddress localInetAddr = null;
    	
        // Set up Tcp connection
        try
        {
            port = listenpoint.getPort();
            
            if (null != listenpoint.getHost())
            {
                localInetAddr = InetAddress.getByName(listenpoint.getHost());
            }
            else
            {
                localInetAddr = InetAddress.getByName("0.0.0.0");
            }
            this.serverSocket = new ServerSocket(port, 0, localInetAddr);
            this.listenpoint = listenpoint;
        }
        catch(Exception e)
        {
            throw new ExecutionException("Can't instantiate the Tcp SocketServerTcpListener on " + localInetAddr + ":" + port, e);
        }
    }
    
    public void run()
    {        
		Stack stack = null; 
		try
		{
			stack = StackFactory.getStack(listenpoint.getProtocol());			
		}
		catch (Exception e)
		{
			GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, e, "Exception in SocketServerTcpListener");
		}

        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "SocketServerTcpListener started");
    	
		boolean exception = false;    	
        while (!exception)
        {
            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "SocketServerTcpListener waiting for a connection on socket");
             
            try
            {
            	Socket socket = serverSocket.accept();
            	if (listenpoint != null) 
				{
            		Channel channel= stack.buildChannelFromSocket(listenpoint, socket);
            		listenpoint.openChannel(channel);
            		
					// Create an empty message for transport connection actions (open or close) 
					// and on server side and dispatch it to the generic stack 
					((StackTcp) StackFactory.getStack(StackFactory.PROTOCOL_TCP)).receiveTransportMessage("SYN-ACK", channel, listenpoint);
				}
            }
	        catch(SocketException e)
	        {
				if (e.getMessage().contains("closed"))
				{
					exception = true;
				}
				else
				{
					GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, e, "Exception in SocketServerTcpListener");
				}
	        }
	        catch(Exception e)
	        {
	            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, e, "Exception in SocketServerTcpListener");
	        }
            
            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "SocketServerTcpListener got a connection");
        }
    	
        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "SocketServerTcpListener stopped");
    }
    
    public void close()
    {
        try
        {
            serverSocket.close();
        }
        catch(Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Error while closing Tcp listener's socket");
        }
    }
}
