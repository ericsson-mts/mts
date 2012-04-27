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

package com.devoteam.srit.xmlloader.sctp;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;

public class ListenpointSctp extends Listenpoint {
	
	// --- attributs --- //
	private SocketServerSctpListener  socketListenerSctp;

    private long startTimestamp = 0;
	
    /** Creates a new instance of Listenpoint */
    public ListenpointSctp(Stack stack) throws Exception
    {
    	super(stack);
    }

	/** Creates a Listenpoint specific from XML tree*/
	public ListenpointSctp(Stack stack, Element root) throws Exception	
	{
		super(stack, root);
	}

    /** Creates a new instance of Listenpoint */
    public ListenpointSctp(Stack stack, String name, String host, int port) throws Exception
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
		
    	try
    	{
    		socketListenerSctp = new SocketServerSctpListener(this);
    		socketListenerSctp.setDaemon(true);
    		socketListenerSctp.start();
    	}
    	catch (NoClassDefFoundError e)
    	{
    		// nothing to do
    		// we are on Windows and have not any SCTP library
    		return false;
    	}
    	catch (UnsatisfiedLinkError e)
    	{
    		// nothing to do
    		// we are on Windows and have not any SCTP library
    		return false;
    	}

		StatPool.beginStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_SCTP, protocol);
		this.startTimestamp = System.currentTimeMillis();

		return true;
	}
	
	public synchronized boolean sendMessage(Msg msg, String remoteHost, int remotePort, String transport) throws Exception
	{			
		ChannelSctp channel;
		
		String keySocket = remoteHost + ":" + remotePort;
		
		if(!this.existsChannel(keySocket))
		{
			channel = new ChannelSctp(this, getHost(), 0, remoteHost, remotePort, this.getProtocol());
			this.openChannel(channel);
		}
		else
		{
			channel = (ChannelSctp) this.getChannel(keySocket);
		}			
				
		channel.sendMessage(msg);
		
		return true;
	}
		
    @Override
	public boolean remove()
    {    	
		super.remove();
	
    	if(this.socketListenerSctp != null)
    	{
    		StatPool.endStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_SCTP, getProtocol(), startTimestamp);
    		
    		this.socketListenerSctp.close();
    		this.socketListenerSctp = null;
    	}
    	
        return true;
    }
	
}
