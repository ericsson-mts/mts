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

package com.devoteam.srit.xmlloader.diameter.dk;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.Trans;
import com.devoteam.srit.xmlloader.diameter.dk.ChannelDiameter;
import com.devoteam.srit.xmlloader.diameter.dk.DiameterNodeManager;
import com.devoteam.srit.xmlloader.diameter.ListenpointDiamCommon;

import dk.i1.diameter.node.Peer;
import dk.i1.diameter.node.Peer.TransportProtocol;

/**
 *
 * @author fhenry
 */
public class ListenpointDiameter extends ListenpointDiamCommon
{
    private long startTimestamp = 0;
    
    private DiameterNodeManager diameterNode = null;

    /** Creates a new instance of Listenpoint */
    public ListenpointDiameter(Stack stack) throws Exception
    {
        super(stack);
    }

    
    //---------------------------------------------------------------------
    // methods for the transport
    //---------------------------------------------------------------------
    
    /** create a listenpoint  */
    @Override
    public boolean create(String protocol) throws Exception    
    {
    	if (this.getListenTCP())
    	{
    		StatPool.beginStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_TCP, protocol);
    	}
    	else if (this.getListenSCTP())
    	{
    		StatPool.beginStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_SCTP, protocol);
    	}
    	this.protocol = protocol;
    	this.startTimestamp = System.currentTimeMillis();
    	this.diameterNode = new DiameterNodeManager(node_settings, this);
    	return true;
    }
	
    /** Remove a listenpoint */
    @Override
    public boolean remove()
    {
    	if (this.getListenTCP())
    	{
    		StatPool.endStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_TCP, getProtocol(), startTimestamp);
    	}
    	else if (this.getListenSCTP())
    	{
    		StatPool.endStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_SCTP, getProtocol(), startTimestamp);
    	}
    	
    	try 
    	{
    		if(null != diameterNode) 
    		{
    			diameterNode.reset();
            }
    	}
        catch (Exception e ) 
        {
        	GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "Exception in diameterNode.reset() ", e);
        	// nothing to do
        }
    	diameterNode = null;
        return true;
    }
    
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
	    		ChannelDiameter channel = (ChannelDiameter) request.getChannel();
	    		msg.setChannel(channel);

	    		// send the DIAMETER answer
	    		diameterNode.sendAnswer(((MsgDiameter) msg).getMessage(), channel.getConnectionKey());
	    		return true;
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

		// default transport is TCP (RFC3588)	
        TransportProtocol protocol = Peer.TransportProtocol.tcp; 
        if (StackFactory.PROTOCOL_SCTP.equalsIgnoreCase(transport)) {
            protocol = Peer.TransportProtocol.sctp;
        }
       
        // set peers list
        Peer peer = new Peer(remoteHost, remotePort, protocol);
        peer.capabilities = node_settings.capabilities();

		diameterNode.sendRequest((MsgDiameter) msg, peer);
        return true;
    }	
    
}