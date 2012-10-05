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

package com.devoteam.srit.xmlloader.diameter;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;

import dk.i1.diameter.node.ConnectionKey;
import dk.i1.diameter.node.Peer;

/**
 *
 * @author gpasquiers
 */
public class ChannelDiameter extends Channel
{
	
	private Peer peer = null;

	private ConnectionKey connKey;
	
	private String transport = null;
	
    public ChannelDiameter(String aLocalHost, String aLocalPort, String aRemoteHost, String aRemotePort, String aProtocol, String aTransport) throws Exception
    {
        super(null, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        this.transport = aTransport.toUpperCase();
    }

    public ChannelDiameter(String anUrl, String aProtocol, String aTransport) throws Exception
    {
        super(null, anUrl, aProtocol);
        this.transport = aTransport.toUpperCase();
    }

    public ChannelDiameter(ConnectionKey connKey, Peer peer) throws Exception
    {
        super(null, 0, peer.host(), peer.port(), (Peer.TransportProtocol.sctp).equals(peer.transportProtocol()) ? StackFactory.PROTOCOL_SCTP : StackFactory.PROTOCOL_TCP);
        this.peer = peer;
        this.connKey = connKey; 
        this.transport = peer.transportProtocol().name().toUpperCase();
    }

    /** Open a Channel*/
    public boolean open() {
        return true;
    }

    /** Close a Channel */
    public boolean close() {
        return true;
    }
    
    /** Send a Msg to Channel */
    public synchronized boolean sendMessage(Msg msg) throws ExecutionException
    {
        return true;        
    }

    /** Get the transport protocol of a Channel */
    public String getTransport() 
    {
    	return this.transport;
    }
    
    /** Get the diameter peer of a Channel */
    public Peer getPeer() 
    {
    	return this.peer;
    }

    /** Get the diameter connection key of a Channel */
    public ConnectionKey getConnectionKey() 
    {
    	return this.connKey;
    }
    
}
