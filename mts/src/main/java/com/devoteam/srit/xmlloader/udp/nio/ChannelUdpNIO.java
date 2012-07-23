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

package com.devoteam.srit.xmlloader.udp.nio;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.udp.nio.DatagramReactor;
import java.net.InetSocketAddress;

public class ChannelUdpNIO extends Channel
{
    private SocketUdpNIO socketUdp;

    private long startTimestamp = 0;
    
    public ChannelUdpNIO(String name, String aLocalHost, String aLocalPort, String aRemoteHost, String aRemotePort, String aProtocol, boolean aConnected) throws Exception
    {
        super(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        socketUdp = null;
    }

    public ChannelUdpNIO(SocketUdpNIO socketUdp, String aLocalHost, int aLocalPort, String aRemoteHost, int aRemotePort, String aProtocol) throws Exception
    {
        super(aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        this.socketUdp = socketUdp;
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
    		StatPool.beginStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_UDP, getProtocol());
    		this.startTimestamp = System.currentTimeMillis();
        	
            InetSocketAddress localDatagramSocketAddress;


            if (getLocalHost() != null)
            {
                localDatagramSocketAddress = new InetSocketAddress(getLocalHost(), getLocalPort());
            }
            else
            {
                localDatagramSocketAddress = new InetSocketAddress(getLocalPort());
            }


            this.setLocalPort(localDatagramSocketAddress.getPort());

            socketUdp = new SocketUdpNIO();
            socketUdp.setChannelUdp(this);

            DatagramReactor.instance().open(localDatagramSocketAddress, socketUdp);

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
        socketUdp.send(msg);
        return true;
    }

    public boolean close()
    {
        if (socketUdp != null)
        {
    		// StatPool.endStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_UDP, getProtocol(), startTimestamp);
    		
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
