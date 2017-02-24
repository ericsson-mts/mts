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
import com.devoteam.srit.xmlloader.core.Runner;
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

    private long startTimestamp = 0;
    
    /** Creates a new instance of Listenpoint */
    public ListenpointUdp(Stack stack) throws Exception
    {
        super(stack);
        if(nio) 
    	{
        	listenpointUdp = new ListenpointUdpNIO(stack);
    	}
        else
        {
        	listenpointUdp = new ListenpointUdpBIO(stack);
        }
        listenpointUdp.clone(this);
    }

    /** Creates a new instance of Listenpoint */
    // Not used except for test unit
    public ListenpointUdp(Stack stack, String name, String host, int port) throws Exception
    {
        super(stack, name, host, port);
    }

    @Override
    public synchronized Channel prepareChannel(Msg msg, String remoteHost, int remotePort, String transport) throws Exception
    {
        return listenpointUdp.prepareChannel(msg, remoteHost, remotePort, transport);
    }

    @Override
    public boolean sendMessage(Msg msg, String remoteHost, int remotePort, String transport) throws Exception
    {
        return listenpointUdp.sendMessage(msg, remoteHost, remotePort, transport);
    }

    public synchronized boolean removeConnection(Channel channel)
    {
        return listenpointUdp.removeChannel(channel);
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

		
        return listenpointUdp.remove();
    }

    public boolean openConnection(Channel channel) throws Exception
    {
        return listenpointUdp.openChannel(channel);
    }

    public String getProtocol()
    {
        return listenpointUdp.getProtocol();
    }

    public int getPort()
    {
        return listenpointUdp.getPort();
    }

    public String getHost()
    {
        return listenpointUdp.getHost();
    }

    public Channel getChannel(String name) throws Exception
    {
        return listenpointUdp.getChannel(name);
    }

    public boolean existsChannel(String name) throws Exception
    {
        return listenpointUdp.existsChannel(name);
    }

    @Override
    public boolean create(String protocol) throws Exception
    {
        if(nio)
        {
    		StatPool.beginStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_UDP, protocol);
        }
        else
        {
    		StatPool.beginStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_UDP, protocol);
        }

		this.startTimestamp = System.currentTimeMillis();
        return listenpointUdp.create(protocol);
    }

    
    //---------------------------------------------------------------------
    // methods for the XML display / parsing 
    //---------------------------------------------------------------------

    /** 
     * Convert the channel to XML document 
     */
    @Override
    public String toXml()
    {
    	return listenpointUdp.toXml();
    }

    /** 
     * Parse the listenpoint from XML element 
     */
    @Override
    public void parseFromXml(Element root, Runner runner) throws Exception
    {
    	super.parseFromXml(root, runner);
    	listenpointUdp.parseFromXml(root, runner);
    }

    
    //------------------------------------------------------
    // method for the "setFromMessage" <parameter> operation
    //------------------------------------------------------

    /** 
     * Get a parameter from the message 
     */
    @Override
    public Parameter getParameter(String path) throws Exception
    {
        return listenpointUdp.getParameter(path);
    }

    
    /** clone method */
    //@Override
    public void clone(Listenpoint listenpoint)
    {
    	super.clone(listenpoint);
        this.listenpointUdp.clone(listenpoint);
    }

    /** equals method */
    @Override
    public boolean equals(Object obj)
    {
        return listenpointUdp.equals(obj);
    }

    public Object getAttachment() 
    {
        return listenpointUdp.getAttachment();
    }

    public void setAttachment(Object attachment) 
    {
        listenpointUdp.setAttachment(attachment);
    }
}
