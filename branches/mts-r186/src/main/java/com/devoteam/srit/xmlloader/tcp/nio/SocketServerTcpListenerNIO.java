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

package com.devoteam.srit.xmlloader.tcp.nio;

import java.net.InetAddress;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.hybridnio.IOHandler;
import com.devoteam.srit.xmlloader.core.hybridnio.IOReactor;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.tcp.StackTcp;

import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 *
 * @author sngom
 */
public class SocketServerTcpListenerNIO implements IOHandler
{

    private ServerSocketChannel serverChannel;
    private ListenpointTcpNIO listenpoint;
    private Stack stack;
    
    /**
     * Creates a new instance of SocketServerTcpListener
     */
    public SocketServerTcpListenerNIO(ListenpointTcpNIO listenpoint) throws ExecutionException
    {
        int port = 0;
        InetAddress localInetAddr = null;

        // Set up Tcp channel
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
            InetSocketAddress local = new InetSocketAddress(localInetAddr, port);
            //this.serverSocket = new ServerSocket(port, 0, localInetAddr);
            
            this.listenpoint = listenpoint;

            IOReactor.instance().openTCPServer(local, this);
        }
        catch (Exception e)
        {
            throw new ExecutionException("Can't instantiate the Tcp SocketServerTcpListener on " + localInetAddr + ":" + port, e);
        }
    }

    public void close()
    {
        try
        {
            serverChannel.close();
        }
        catch (Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Error while closing Tcp listener's socket");
        }
    }

    public void init(SelectionKey selectionKey, SelectableChannel channel)
    {
        this.serverChannel = (ServerSocketChannel) channel;
    }

    public void inputReady()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void outputReady()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void connectReady()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void acceptReady()
    {
        try
        {
            SocketChannel socketChannel = serverChannel.accept();

            if (listenpoint != null)
            {
                if (null == this.stack)
                {
                	this.stack = StackFactory.getStack(listenpoint.getProtocol());
                }

                Channel channel = stack.buildChannelFromSocket(listenpoint, socketChannel.socket());
                listenpoint.openChannel(channel);
                
				// Create an empty message for transport connection actions (open or close) 
				// and on server side and dispatch it to the generic stack 
                ((StackTcp) StackFactory.getStack(StackFactory.PROTOCOL_TCP)).receiveTransportMessage("SYN-ACK", channel, listenpoint);
            }
        }
        catch (Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, e, "Exception in SocketServerTcpListener");
        }

        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "SocketServerTcpListener got a channel");
    }
}
