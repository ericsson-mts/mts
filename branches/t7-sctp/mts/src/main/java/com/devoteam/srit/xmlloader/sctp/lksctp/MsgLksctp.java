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

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;

import java.net.InetAddress;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.sctp.ChannelSctp;
import com.devoteam.srit.xmlloader.sctp.DataSctp;
import com.devoteam.srit.xmlloader.sctp.MsgSctp;

import dk.i1.sctp.AssociationId;
import dk.i1.sctp.SCTPData;
import dk.i1.sctp.SCTPSocket;


public class MsgLksctp extends MsgSctp{
	
	private enum DataType
	{
		text,
		binary
	}

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

	/** 
     * Convert the message sub elements to XML document
     * @see toXml
     */
	@Override
    protected String toXml_SubElements() throws Exception 
    {
    	String xml = "";
    	ChannelLksctp channelLksctp = (ChannelLksctp) getChannel();
		if (channelLksctp != null)
		{
			SocketLksctp socketLksctp = channelLksctp.getSocketLksctp();
			if (socketLksctp != null)
			{
				SCTPSocket sctpSocket = socketLksctp.getSCTPSocket();
				if (sctpSocket != null)
				{
					SCTPData sctpData = this.dataLksctp.getSCTPData();
					AssociationId assoId = sctpData.sndrcvinfo.sinfo_assoc_id;
					if (assoId != null && assoId.hashCode() != 0)
					{
						//System.out.println("assoId" + assoId);
						int port= sctpSocket.getPeerInetPort(assoId);
						//int port=  0;
						Collection<InetAddress> col = sctpSocket.getPeerInetAddresses(assoId);
						if (col != null)
						{
							for (InetAddress ia : col)
							{	
								xml += "    <peer ";
								xml += "address=\"" + ia.getHostAddress() + "\" ";
								xml += "port=\"" + port + "\" ";
								xml += "/>\n";
							}
						}
					}
				}
			}	
		}
    	return xml;
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
