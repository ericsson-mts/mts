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

package com.devoteam.srit.xmlloader.diameter.light;

import java.util.Iterator;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.Trans;


/**
 *
 * @author fhenry
 */
public class ListenpointDiameter extends Listenpoint
{
	
    /** Creates a new instance of Listenpoint */
    public ListenpointDiameter(Stack stack) throws Exception
    {
        super(stack);
    }
    
    
    //---------------------------------------------------------------------
    // methods for the transport
    //---------------------------------------------------------------------

    /** Send a Msg to a given destination with a given transport protocol */
    @Override
    public synchronized boolean sendMessage(Msg msg, String remoteHost, int remotePort, String transport) throws Exception
    {
		if (remoteHost == null || !msg.isRequest())
		{
	    	// RFC 3588 : Hop-by-Hop Id is used instead of Destination-Host or Destination-Realm AVP
	    	Trans trans = msg.getTransaction();
	    	if (trans != null)
	    	{
	    		Msg request = trans.getBeginMsg();
	    		Channel channel = (Channel) request.getChannel();
	    		msg.setChannel(channel);

	    		// send the DIAMETER answer
	    		return stack.sendMessage(msg);
	    	}
		}
    	if (remoteHost == null)
		{        	
    		// RFC 3588 : If peer identity == Destination-Host AVP then send that peer, otherwise
    		Parameter destHost = msg.getParameter("avp.293.value");
    		if ((destHost != null) && (destHost.length() > 0)) 
    		{
    			remoteHost = destHost.get(0).toString();	    			
    		}
    		else 
    		{
	    		// RFC 3588 : Lookup realm table with Destination-Realm and AppId
	    		Parameter destRealm = msg.getParameter("avp.283.value");
	    		if ((destRealm != null) && (destRealm.length() > 0)) 
	    		{
	    			remoteHost = destRealm.get(0).toString();	    			
	    		}	    			
    		}	    		
		}
		if (remoteHost == null)
		{
			throw new ExecutionException("Could not determine the remote destination from the message : " + msg);
		}

		// default port is 3868 (RFC3588) 
		if (remotePort <=0)
		{
			remotePort = 3868; 
		}

		// default port is 3868 (RFC3588) 
		if (transport == null)
		{
			transport = this.transport; 
		}
       	return super.sendMessage(msg, remoteHost, remotePort, transport);
    }	
 
}