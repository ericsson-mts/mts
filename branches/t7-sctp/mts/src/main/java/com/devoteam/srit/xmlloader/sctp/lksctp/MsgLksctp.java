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

package com.devoteam.srit.xmlloader.sctp.lksctp;

import java.net.InetAddress;
import java.util.Collection;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.protocol.Stack;

import com.devoteam.srit.xmlloader.sctp.*;

import dk.i1.sctp.*;


public class MsgLksctp extends MsgSctp{

	protected DataLksctp dataLksctp;

	/** Creates a new instance */
    public MsgLksctp(Stack stack) throws Exception
    {
        super(stack);
		this.dataLksctp = new DataLksctp();
    }
    
    /** Creates a new instance */
	public MsgLksctp(Stack stack, SCTPData chunk) throws Exception{
		super(stack);
		this.setType("DATA");
		this.dataLksctp = new DataLksctp(chunk);		
	}
    
    /** Creates a new instance */
	public MsgLksctp(Stack stack, DataSctp chunk) throws Exception{
		super(stack);		
		this.setType("DATA");
		this.dataLksctp = new DataLksctp(chunk);		
	}
	
	/**
	 * 
	 * @return the associated DataSctp abstraction
	 */
	@Override
	public DataSctp getDataSctp(){
		assert(this.dataLksctp!=null);
		return this.dataLksctp;		
	}

	/**
	 * 
	 * @return the associated DataSctp implementation object
	 */
	public SCTPData getSCTPData() {
		return this.dataLksctp.getSCTPData();
	}

	/// a utiliser
	public void setAidFromMsg()
	{
		SCTPData sctpData = this.dataLksctp.getSCTPData();
		((ChannelLksctp) getChannel()).setAssociationId(sctpData.sndrcvinfo.sinfo_assoc_id);
	}

    //------------------------------------------------------
    // method for the "setFromMessage" <parameter> operation
    //------------------------------------------------------

    /** 
     * Get a parameter from the message
     */
    @Override
    protected Parameter getParameterPeerHosts() throws Exception {
    	Parameter var = new Parameter();
    	ChannelLksctp connSctp =((ChannelLksctp) getChannel());
		SocketLksctp socketLksctp = connSctp.getSocketLksctp();
		if (socketLksctp != null){
			SCTPSocket sctpSocket = socketLksctp.getSCTPSocket();
			SCTPData sctpData = this.dataLksctp.getSCTPData();
			Collection<InetAddress> col = sctpSocket.getPeerInetAddresses(sctpData.sndrcvinfo.sinfo_assoc_id);
			for (InetAddress ia : col)
			{	
				var.add(ia.getHostAddress());						
			}
		}
		return var;
    }
    
    /** 
     * Get a parameter from the message
     */
    @Override
    protected Parameter getParameterPeerPort() throws Exception {
    	Parameter var = new Parameter();
    	ChannelLksctp connSctp =((ChannelLksctp) getChannel());
		SocketLksctp socketLksctp = connSctp.getSocketLksctp();
		if (socketLksctp != null){
			SCTPSocket sctpSocket = socketLksctp.getSCTPSocket();
			SCTPData sctpData = this.dataLksctp.getSCTPData();
      	  	int port = sctpSocket.getPeerInetPort(sctpData.sndrcvinfo.sinfo_assoc_id);
    	    var.add(Integer.toString(port));
		}
		return var;
    }

}
