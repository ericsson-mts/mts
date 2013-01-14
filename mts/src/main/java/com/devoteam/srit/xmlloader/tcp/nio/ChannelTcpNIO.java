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

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.hybridnio.HybridSocket;
import com.devoteam.srit.xmlloader.core.hybridnio.IOReactor;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

/**
 *
 * @author gpasquiers
 */
public class ChannelTcpNIO extends Channel
{

    private SocketTcpNIO socketTcp;
    
    private Listenpoint listenpoint;

    private long startTimestamp = 0;
    
    /** Creates a new instance of ChannelTcp */
    public ChannelTcpNIO(String name, String aLocalHost, String aLocalPort, String aRemoteHost, String aRemotePort, String aProtocol) throws Exception
    {
        super(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        socketTcp = null;
        listenpoint = null;
    }

    /** Creates a new instance of ChannelTcp */
    public ChannelTcpNIO(String name, Listenpoint listenpoint, Socket socket) throws Exception
    {
        super(  name,
                ((InetSocketAddress) socket.getLocalSocketAddress()).getAddress().getHostAddress(),
                Integer.toString(((InetSocketAddress) socket.getLocalSocketAddress()).getPort()),
                ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().getHostAddress(),
                Integer.toString(((InetSocketAddress) socket.getRemoteSocketAddress()).getPort()),
                listenpoint.getProtocol());

        SocketChannel socketChannel = socket.getChannel();

		StatPool.beginStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_TCP, getProtocol());
		this.startTimestamp = System.currentTimeMillis();
        
        this.socketTcp = new SocketTcpNIO();
        this.socketTcp.setChannelTcp(this);

        HybridSocket hybridSocket = new HybridSocket(this.socketTcp);

        IOReactor.instance().openTCP(socketChannel, hybridSocket);

        this.listenpoint = listenpoint;
    }

    /** Creates a new instance of ChannelTcp */
    public ChannelTcpNIO(Listenpoint listenpointTcp, String localHost, int localPort, String remoteHost, int remotePort, String aProtocol)
    {
        super(localHost, localPort, remoteHost, remotePort, aProtocol);
        this.socketTcp = null;
        this.listenpoint = listenpointTcp;
    }

    /** Send a Msg to Channel */
    public synchronized boolean sendMessage(Msg msg) throws Exception
    {
        if (null == socketTcp)
        {
            throw new ExecutionException("SocketTcp is null, has the channel been opened ?");
        }

        msg.setChannel(this);
        socketTcp.send(msg);
        return true;
    }

    @Override
    public boolean open() throws Exception
    {
        if (socketTcp == null)
        {
    		StatPool.beginStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_TCP, getProtocol());
    		this.startTimestamp = System.currentTimeMillis();
        	
            InetAddress localAddr = InetAddress.getByName(getLocalHost());

            InetSocketAddress local  = new InetSocketAddress(localAddr, getLocalPort());
            InetSocketAddress remote = new InetSocketAddress(getRemoteHost(), getRemotePort());
            
            socketTcp = new SocketTcpNIO();
            HybridSocket hybridSocket = new HybridSocket(socketTcp);
            socketTcp.init(hybridSocket);

            socketTcp.setChannelTcp(this);
            IOReactor.instance().openTCP(local, remote, hybridSocket);

    		// read all properties for the TCP socket 
    		Config.getConfigForTCPSocket(hybridSocket, false);

            this.setLocalPort(hybridSocket.getLocalPort());
            this.setLocalHost(hybridSocket.getLocalAddress().getHostAddress());
        }


        return true;
    }

    public boolean close()
    {
        if (socketTcp != null)
        {
    		StatPool.endStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_TCP, getProtocol(), startTimestamp);
    		
            socketTcp.close();
            socketTcp = null;
        }

        return true;
    }

    /** Get the transport protocol of this message */
    public String getTransport()
    {
        return StackFactory.PROTOCOL_TCP;
    }

    public Listenpoint getListenpointTcp()
    {
        return listenpoint;
    }
}
