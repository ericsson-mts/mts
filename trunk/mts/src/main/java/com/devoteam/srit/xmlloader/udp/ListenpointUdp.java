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

package com.devoteam.srit.xmlloader.udp;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.udp.bio.ListenpointUdpBIO;
import com.devoteam.srit.xmlloader.udp.nio.ListenpointUdpNIO;

import org.dom4j.Element;

public class ListenpointUdp extends Listenpoint
{
    private boolean nio = Config.getConfigByName("udp.properties").getBoolean("USE_NIO", false);
    private Listenpoint  listenpoint;
    private long startTimestamp = 0;
    
    /** Creates a new instance of Listenpoint */
    public ListenpointUdp(Stack stack) throws Exception
    {
        super(stack);
        if(nio) listenpoint = new ListenpointUdpNIO(stack);
        else listenpoint = new ListenpointUdpBIO(stack);
    }

	/** Creates a Listenpoint specific from XML tree*/
	public ListenpointUdp(Stack stack, Element root) throws Exception	
	{
		super(stack, root);
        if(nio) listenpoint = new ListenpointUdpNIO(stack, root);
        else listenpoint = new ListenpointUdpBIO(stack, root);
	}

    /** Creates a new instance of Listenpoint */
    public ListenpointUdp(Stack stack, String name, String host, int port) throws Exception
    {
        super(stack, name, host, port);
        if(nio) listenpoint = new ListenpointUdpNIO(stack, name, host, port);
        else listenpoint = new ListenpointUdpBIO(stack, name, host, port);
    }

    public int hashCode()
    {
        return listenpoint.hashCode();
    }

    public boolean equals(Object obj)
    {
        return listenpoint.equals(obj);
    }

    public String toString()
    {
        return listenpoint.toString();
    }

    public void setPort(int port)
    {
        listenpoint.setPort(port);
    }

    @Override
    public synchronized Channel prepareChannel(Msg msg, String remoteHost, int remotePort, String transport) throws Exception
    {
        return listenpoint.prepareChannel(msg, remoteHost, remotePort, transport);
    }

    @Override
    public boolean sendMessage(Msg msg, String remoteHost, int remotePort, String transport) throws Exception
    {
        return listenpoint.sendMessage(msg, remoteHost, remotePort, transport);
    }

    public synchronized boolean removeConnection(Channel channel)
    {
        return listenpoint.removeChannel(channel);
    }

    public boolean remove()
    {
    	if (nio)
    	{
    		StatPool.endStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_UDP, getProtocol(), startTimestamp);
    	}
    	else
    	{
    		StatPool.endStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_UDP, getProtocol(), startTimestamp);
    	}

		
        return listenpoint.remove();
    }

    public boolean openConnection(Channel channel) throws Exception
    {
        return listenpoint.openChannel(channel);
    }

    public String getProtocol()
    {
        return listenpoint.getProtocol();
    }

    public int getPort()
    {
        return listenpoint.getPort();
    }

    public Parameter getParameter(String path) throws Exception
    {
        return listenpoint.getParameter(path);
    }

    public String getHost()
    {
        return listenpoint.getHost();
    }

    public Channel getChannel(String name) throws Exception
    {
        return listenpoint.getChannel(name);
    }

    public boolean existsChannel(String name) throws Exception
    {
        return listenpoint.existsChannel(name);
    }

    public boolean equals(Listenpoint listenpoint)
    {
        return this.listenpoint.equals(listenpoint);
    }

    public boolean create(String protocol) throws Exception
    {
    	if (nio)
    	{
    		StatPool.beginStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_UDP, protocol);
    	}
    	else
    	{
    		StatPool.beginStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_UDP, protocol);
    	}
		this.startTimestamp = System.currentTimeMillis();
		
        return listenpoint.create(protocol);
    }

    public Object getAttachment() {
        return listenpoint.getAttachment();
    }

    public void setAttachment(Object attachment) {
        listenpoint.setAttachment(attachment);
    }
}
