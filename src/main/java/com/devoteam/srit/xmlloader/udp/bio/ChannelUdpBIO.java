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

package com.devoteam.srit.xmlloader.udp.bio;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class ChannelUdpBIO extends Channel
{

    private SocketUdpBIO socketUdp;
	private InetSocketAddress remoteDatagramSocketAddress = null;
	
    private long startTimestamp = 0;
    
    /** Creates a new instance of Channel*/
    public ChannelUdpBIO(Stack stack)
    {
    	super(stack);
    }
    
    public ChannelUdpBIO(SocketUdpBIO socketUdp, String aLocalHost, int aLocalPort, String aRemoteHost, int aRemotePort, String aProtocol) throws Exception
    {
        super(aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
               
        this.socketUdp = socketUdp;
		this.remoteDatagramSocketAddress = new InetSocketAddress(aRemoteHost, aRemotePort);				
    }

    //---------------------------------------------------------------------
    // methods for the transport
    //---------------------------------------------------------------------

    /** Open a channel */
    @Override
    public boolean open() throws Exception
    {
        if (socketUdp != null)
        {
            return true;
        }
        try
        {
    		StatPool.beginStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_UDP, getProtocol());
    		this.startTimestamp = System.currentTimeMillis();
        	
            InetSocketAddress localDatagramSocketAddress;

            DatagramSocket datagramSocket = null;

            String localHost = this.getLocalHost();
            if (localHost != null)
            {
                localDatagramSocketAddress = new InetSocketAddress(localHost, this.localPort);
                datagramSocket = new DatagramSocket(localDatagramSocketAddress);
            }
            else
            {
                datagramSocket = new DatagramSocket(this.localPort);
            }
    		// read all properties for the UDP socket 
    		Config.getConfigForUDPSocket(datagramSocket);

            if (!datagramSocket.isBound())
            {
                GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "ChannelUdp: The datagramSocket is unbounded");
            }
            // deprecated part //
            // in the last grammar, we don't take account of the connected possibility
            if (this.remoteHost != null && this.remotePort != 0)
            {
                this.remoteDatagramSocketAddress = new InetSocketAddress(getRemoteHost(), getRemotePort());
                datagramSocket.connect(remoteDatagramSocketAddress);
                if (datagramSocket.isConnected())
                {
                    GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "ChannelUdp: The datagramSocket is connected to ", getRemoteHost(), ":", getRemotePort());
                }
                else
                {
                    GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "ChannelUdp: The datagramSocket failed connecting to ", getRemoteHost(), ":", getRemotePort());
                }
            }
            
            this.localPort = datagramSocket.getLocalPort();

            socketUdp = new SocketUdpBIO(datagramSocket);
            socketUdp.setChannelUdp(this);
            socketUdp.setDaemon(true);
            socketUdp.start();

            return true;
        }
        catch (Exception e)
        {
            throw new ExecutionException("ChannelUdp: Error occured while creating socket", e);
        }
    }

    /** Close a channel */
    @Override
    public boolean close()
    {
        if (socketUdp != null)
        {
    		StatPool.endStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_UDP, getProtocol(), startTimestamp);
        	
        	socketUdp.close();
            socketUdp = null;        	
        }
        
        return true;
    }

    /** Send a Msg through the channel */
    @Override
    public synchronized boolean sendMessage(Msg msg) throws Exception
    {
        if (socketUdp == null)
        {
            throw new ExecutionException("SocketUdp is null, has the connection been opened ?");
        }

        msg.setChannel(this);
        socketUdp.send(msg, remoteDatagramSocketAddress);
        return true;
    }

    /** Get the transport protocol */
    @Override
    public String getTransport() 
    {
    	return StackFactory.PROTOCOL_UDP;
    }

}
