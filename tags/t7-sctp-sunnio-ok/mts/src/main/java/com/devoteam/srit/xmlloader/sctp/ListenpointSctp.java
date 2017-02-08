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

import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;

public abstract class ListenpointSctp extends Listenpoint {
    
    /**
     * multi-homing addresses
     */
    protected final MultihomingSctp multihoming = new MultihomingSctp();
	
    /**
     * Creates a new instance of Listenpoint
     */
    public ListenpointSctp(Stack stack) throws Exception
    {
    	super(stack);
    }

    
    //---------------------------------------------------------------------
    // methods for the transport
    //---------------------------------------------------------------------

    
    /**
     * Create a listenpoint to each Stack
     * should be overriden
     */
    @Override
	public boolean create(String protocol) throws Exception
    {
		if (!super.create(protocol)) 
		{
			return false;
		}
		//put additional code here...
		return true;
    }
    
    /**
     * Prepare the channel
     */
    @Override
    public Channel prepareChannel(Msg msg, String remoteHost, int remotePort, String transport) throws Exception
    {
    	return super.prepareChannel(msg, remoteHost, remotePort, transport);
    }
	
    /**
     * Send a Msg to a given destination with a given transport protocol
     */
    @Override
    public synchronized boolean sendMessage(Msg msg, String remoteHost, int remotePort, String transport) throws Exception
    {	
    	//sctp won't rely on another transport layer 
    	
		ChannelSctp channel = null;
		String keySocket = remoteHost + ":" + remotePort;
		if(!this.existsChannel(keySocket)){
			String localHost = this.getHost();
			int localPort = 0;
			String protocol = this.getProtocol();
			channel = this.createChannelSctp( localHost, localPort, remoteHost, remotePort, protocol);
			assert(channel!=null);
			this.openChannel(channel);
		}
		else{
			Channel existingChannel = this.getChannel(keySocket);
			assert((existingChannel!=null) && (existingChannel instanceof ChannelSctp));
			channel = (ChannelSctp)existingChannel;
		}			
				
		channel.sendMessage(msg);
		
		return true;
    }
		
    @Override
    public boolean remove()
    { 
        if (!super.remove()) {
            return false;
        }
         
        //put additional code here...	
        return true;
    }
	
    /**
     * create a channel instance
     * @see sendMessage
     * @return a new ChannelSctp instance
     */
    protected abstract ChannelSctp createChannelSctp(String aLocalHost, int aLocalPort, String aRemoteHost, int aRemotePort, String aProtocol) throws Exception;

}
