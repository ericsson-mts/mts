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

public abstract class ListenpointSctp extends Listenpoint {
	
    /** Creates a new instance of Listenpoint */
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
	
    public synchronized boolean sendMessage(Msg msg, String remoteHost, int remotePort, String transport) throws Exception
    {			
		ChannelSctp channel;
		
		String keySocket = remoteHost + ":" + remotePort;
		
		if(!this.existsChannel(keySocket))
		{
			channel = this.createChannelSctp( getHost(), 0, remoteHost, remotePort, this.getProtocol());
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
