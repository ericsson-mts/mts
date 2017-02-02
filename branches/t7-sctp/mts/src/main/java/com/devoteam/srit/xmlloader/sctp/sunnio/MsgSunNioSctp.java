/* 
 * Copyright 2017 Ericsson http://www.ericsson.com
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

package com.devoteam.srit.xmlloader.sctp.sunnio;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.protocol.*;
import com.devoteam.srit.xmlloader.sctp.*;

import java.nio.*;

/**
 * @author emicpou
 *
 */
public class MsgSunNioSctp extends MsgSctp{
	
	/*
	 * 
	 */
	protected DataSunNioSctp dataSunNioSctp;

	/** Creates a new instance */
    public MsgSunNioSctp(Stack stack) throws Exception
    {
        super(stack);
		this.dataSunNioSctp = new DataSunNioSctp();
    }
     
    /** Creates a new instance */
	public MsgSunNioSctp(Stack stack, DataSctp chunk) throws Exception{
		super(stack);		
		this.setType("DATA");
		//would be safer to deep clone
		if( chunk instanceof DataSunNioSctp) {
			this.dataSunNioSctp = (DataSunNioSctp)chunk;
		}
		else{
			this.dataSunNioSctp = new DataSunNioSctp(chunk);
		}
	}
	
	/**
	 * 
	 * @return the associated DataSctp abstraction
	 */
	@Override
	public DataSctp getDataSctp(){
		assert(this.dataSunNioSctp!=null);
		return this.dataSunNioSctp;		
	}
	
	/**
	 * 
	 * @return the associated DataSctp abstraction
	 */
	public DataSunNioSctp getDataSunNioSctp(){
		assert(this.dataSunNioSctp!=null);
		return this.dataSunNioSctp;		
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
    	/*
    	ChannelSunIoSctp connSctp =((ChannelSunIoSctp) getChannel());
		SocketSunIoSctp socketSunIoSctp = connSctp.getSocketSunIoSctp();
		if (socketSunIoSctp != null){
			SCTPSocket sctpSocket = socketSunIoSctp.getSCTPSocket();
			SCTPData sctpData = this.dataSunNioSctp.getSCTPData();
			Collection<InetAddress> col = sctpSocket.getPeerInetAddresses(sctpData.sndrcvinfo.sinfo_assoc_id);
			for (InetAddress ia : col)
			{	
				var.add(ia.getHostAddress());						
			}
		}
		*/
		return var;
    }
    
    /** 
     * Get a parameter from the message
     */
    @Override
    protected Parameter getParameterPeerPort() throws Exception {
    	Parameter var = new Parameter();
    	/*
    	ChannelSunIoSctp connSctp =((ChannelSunIoSctp) getChannel());
		SocketSunIoSctp socketSunIoSctp = connSctp.getSocketSunIoSctp();
		if (socketSunIoSctp != null){
			SCTPSocket sctpSocket = socketSunIoSctp.getSCTPSocket();
			SCTPData sctpData = this.dataSunNioSctp.getSCTPData();
      	  	int port = sctpSocket.getPeerInetPort(sctpData.sndrcvinfo.sinfo_assoc_id);
    	    var.add(Integer.toString(port));
		}
		*/
		return var;
    }

}
