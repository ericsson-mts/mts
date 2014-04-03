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

import static java.lang.Integer.parseInt;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class ChannelUdpBIO extends Channel
{

    private SocketUdpBIO socketUdp;
	private InetSocketAddress remoteDatagramSocketAddress = null;
	
    // deprecated part //
    private boolean connected = false;
    // deprecated part //

    private long startTimestamp = 0;
    
    public ChannelUdpBIO(String name, String aLocalHost, String aLocalPort, String aRemoteHost, String aRemotePort, String aProtocol, boolean aConnected) throws Exception
    {
        super(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        this.socketUdp = null; 
        int remotePort = Integer.parseInt(aRemotePort);
		this.remoteDatagramSocketAddress = new InetSocketAddress(aRemoteHost, remotePort);
        // deprecated part //
        connected = aConnected;
        // deprecated part //
    }

    public ChannelUdpBIO(SocketUdpBIO socketUdp, String aLocalHost, int aLocalPort, String aRemoteHost, int aRemotePort, String aProtocol) throws Exception
    {
         super(aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        this.socketUdp = socketUdp;
		this.remoteDatagramSocketAddress = new InetSocketAddress(aRemoteHost, aRemotePort);				

        // deprecated part //
        this.connected = false;
        // deprecated part //
    }

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

            if (getLocalHost() != null)
            {
                localDatagramSocketAddress = new InetSocketAddress(getLocalHost(), getLocalPort());
                datagramSocket = new DatagramSocket(localDatagramSocketAddress);
            }
            else
            {
                datagramSocket = new DatagramSocket(getLocalPort());
            }
    		// read all properties for the UDP socket 
    		Config.getConfigForUDPSocket(datagramSocket);

            if (!datagramSocket.isBound())
            {
                GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "ChannelUdp: The datagramSocket is unbounded");
            }
            // deprecated part //
            // in the last grammar, we don't take account of the connected possibility
            if (getRemoteHost() != null && getRemotePort() != 0 && connected)
            {
                InetSocketAddress remoteDatagramSocketAddress = new InetSocketAddress(getRemoteHost(), getRemotePort());
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
            else if (connected && (getRemoteHost() == null | getRemotePort() == 0))
            {
                GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "ChannelUdp: connect=true with missing arg");
            }
            // deprecated part //
            
            this.setLocalPort(datagramSocket.getLocalPort());

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

    public boolean close()
    {
        if (socketUdp != null)
        {
    		// StatPool.endStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_UDP, getProtocol(), startTimestamp);
        	
        	socketUdp.close();
            socketUdp = null;        	
        }
        
        return true;
    }

    /** Get the transport protocol of this message */
    public String getTransport() 
    {
    	return StackFactory.PROTOCOL_UDP;
    }

}
