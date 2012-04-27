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

import com.devoteam.srit.xmlloader.core.protocol.Channel;
import java.net.InetSocketAddress;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.utils.Utils;

public class ListenpointUdpNIO extends Listenpoint
{

    // --- attributs --- //
    private SocketUdpNIO socketUdp;

    /** Creates a new instance of Listenpoint */
    public ListenpointUdpNIO(Stack stack) throws Exception
    {
        super(stack);
    }

	/** Creates a Listenpoint specific from XML tree*/
	public ListenpointUdpNIO(Stack stack, Element root) throws Exception	
	{
		super(stack, root);
	}

    /** Creates a new instance of Listenpoint */
    public ListenpointUdpNIO(Stack stack, String name, String host, int port) throws Exception
    {
        super(stack, name, host, port);
    }

    /** Create a listenpoint to each Stack */
    @Override
    public boolean create(String protocol) throws Exception
    {
        if (!super.create(protocol))
        {
            return false;
        }

        if (socketUdp != null)
        {
            return true;
        }

        InetSocketAddress localDatagramSocketAddress;

        if (getHost() != null)
        {
            localDatagramSocketAddress = new InetSocketAddress(getHost(), getPort());
        }
        else
        {
            localDatagramSocketAddress = new InetSocketAddress(getPort());
        }

        this.setPort(localDatagramSocketAddress.getPort());

        socketUdp = new SocketUdpNIO();
        socketUdp.setListenpointUdp(this);

        DatagramReactor.instance().open(localDatagramSocketAddress, socketUdp);

        return true;
    }

    @Override
    public synchronized Channel prepareChannel(Msg msg, String remoteHost, int remotePort, String transport) throws Exception
    {
        ChannelUdpNIO channel;

        String nameChannel = remoteHost + ":" + remotePort;

        if (!this.existsChannel(nameChannel))
        {
			String host = getHost();
			if ("0.0.0.0".equals(host))
			{
				host = Utils.getLocalAddress().getHostAddress();
			}
        	channel = new ChannelUdpNIO(socketUdp, host, getPort(), remoteHost, remotePort, this.getProtocol());
			this.putChannel(nameChannel, channel);
        }
        else
        {
        	channel = (ChannelUdpNIO) this.getChannel(nameChannel);
        }

        return channel;
    }

    @Override
    public synchronized boolean sendMessage(Msg msg, String remoteHost, int remotePort, String transport) throws Exception
    {
        return prepareChannel(msg, remoteHost, remotePort, transport).sendMessage(msg);
    }

    public boolean remove()
    {
        super.remove();

        if (this.socketUdp != null)
        {
            this.socketUdp.close();
            this.socketUdp = null;
        }

        return true;
    }
}
