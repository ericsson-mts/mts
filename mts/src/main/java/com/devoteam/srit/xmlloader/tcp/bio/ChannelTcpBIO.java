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
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 *
 * @author gpasquiers
 */
public class ChannelTcpBIO extends Channel
{
    private SocketTcpBIO socketTcp;
    
    private Listenpoint listenpoint;    

    private long startTimestamp = 0;
    
    /** Creates a new instance of ChannelTcp */
    public ChannelTcpBIO(String name, String aLocalHost, String aLocalPort, String aRemoteHost, String aRemotePort, String aProtocol) throws Exception
    {
        super(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        socketTcp = null;
        listenpoint = null;
    }
    
    /** Creates a new instance of ChannelTcp */
    public ChannelTcpBIO(String name, Listenpoint listenpoint, Socket socket) throws Exception
    {
        super(
        		name, 
        		((InetSocketAddress)socket.getLocalSocketAddress()).getAddress().getHostAddress(), 
        		Integer.toString(((InetSocketAddress)socket.getLocalSocketAddress()).getPort()), 
        		((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress().getHostAddress(), 
        		Integer.toString(((InetSocketAddress)socket.getRemoteSocketAddress()).getPort()),
        		listenpoint.getProtocol()
        );
        
		StatPool.beginStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_TCP, getProtocol());
		this.startTimestamp = System.currentTimeMillis();

        socketTcp = new SocketTcpBIO(socket);
        socketTcp.setChannelTcp(this);
        this.listenpoint = listenpoint;
    }

    /** Creates a new instance of ChannelTcp */
    public ChannelTcpBIO(Listenpoint listenpointTcp, String localHost, int localPort, String remoteHost, int remotePort, String aProtocol)
    {
    	super(localHost, localPort, remoteHost, remotePort, aProtocol);
        socketTcp = null;    	    
        this.listenpoint = listenpointTcp;        
    }

    /** Send a Msg to Channel */
    public synchronized boolean sendMessage(Msg msg) throws Exception
    {
        if(null == socketTcp)
        {
            throw new ExecutionException("SocketTcp is null, has the connection been opened ?");
        }
        
        msg.setChannel(this);
        socketTcp.send(msg);
        return true;
    }
    
    public boolean open() throws Exception
    {
    	if (socketTcp == null) 
    	{
    		StatPool.beginStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_TCP, getProtocol());
    		this.startTimestamp = System.currentTimeMillis();
        	
    		InetAddress localAddr = InetAddress.getByName(getLocalHost());
    		Socket socket = new Socket(getRemoteHost(), getRemotePort(), localAddr, getLocalPort());
    		
    		boolean keepAlive = Config.getConfigByName("tcp.properties").getBoolean("socket.KEEP_ALIVE", false);
    		socket.setKeepAlive(keepAlive);
       		boolean oobInline = Config.getConfigByName("tcp.properties").getBoolean("socket.OOB_INLINE", false);
    		socket.setOOBInline(oobInline);
    		int receiveBuffer = Config.getConfigByName("tcp.properties").getInteger("socket.RECEIVE_BUFFER", 8192);
    		socket.setReceiveBufferSize(receiveBuffer);
    		boolean reuseAddress = Config.getConfigByName("tcp.properties").getBoolean("socket.REUSE_ADDRESS", false);
    		socket.setReuseAddress(reuseAddress);
    		int sendBuffer = Config.getConfigByName("tcp.properties").getInteger("socket.SEND_BUFFER", 8192);
    		socket.setSendBufferSize(sendBuffer);
    		int lingerTimeout = Config.getConfigByName("tcp.properties").getInteger("socket.LINGER_TIMEOUT", -1);
    		if (lingerTimeout >= 0)
    			socket.setSoLinger(true, lingerTimeout);
    		else
    			socket.setSoLinger(false, 0);
    		int timeout = Config.getConfigByName("tcp.properties").getInteger("socket.TIMEOUT", 0);
    		socket.setSoTimeout(timeout);
       		boolean tcpNoDelay = Config.getConfigByName("tcp.properties").getBoolean("socket.TCP_NO_DELAY", false);
    		socket.setTcpNoDelay(tcpNoDelay);
       		int trafficClass = Config.getConfigByName("tcp.properties").getInteger("socket.TRAFFIC_CLASS", 0);
    		socket.setTrafficClass(trafficClass);
    		
            this.setLocalPort(socket.getLocalPort());
            this.setLocalHost(socket.getLocalAddress().getHostAddress());
            
            socketTcp = new SocketTcpBIO(socket);
    	}
		
        socketTcp.setChannelTcp(this);
        socketTcp.setDaemon(true);
        socketTcp.start();
        
        return true;
    }
    
    public boolean close()
    {
        SocketTcpBIO tmp = socketTcp;
    	if (tmp != null){
    		StatPool.endStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_TCP, getProtocol(), startTimestamp);
    		tmp.close();
            socketTcp = null;
    	}
        return true;
    }
    
    /** Get the transport protocol of this message */
    public String getTransport() 
    {
    	return StackFactory.PROTOCOL_TCP;
    }

	public Listenpoint getListenpointTcp() {
		return listenpoint;
	}

    public SocketTcpBIO getSocketTcp() {
        return socketTcp;
    }
}
