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

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import dk.i1.sctp.AssociationId;
import dk.i1.sctp.SCTPData;

public class StackSctp extends Stack
{
	
	/** Creates a new instance */
	public StackSctp() throws Exception
	{
		super();
	}

    /**
     * Creates a Msg specific to each Stack
     * Used for SCTP like protocol : to build incoming message
     */
    public SCTPData getConfigSCTPData() throws Exception    
    {
    	SCTPData sctpData = new SCTPData();
    	
        Config config = getConfig();
        sctpData.sndrcvinfo.sinfo_stream = (short) config.getInteger("client.DEFAULT_STREAM", 1);
		sctpData.sndrcvinfo.sinfo_ssn = (short) config.getInteger("client.DEFAULT_SSN", 0);
		int ppid = config.getInteger("client.DEFAULT_PPID", 0);
		sctpData.sndrcvinfo.sinfo_ppid = Utils.convertLittleBigIndian(ppid);
		sctpData.sndrcvinfo.sinfo_flags = (short) config.getInteger("client.DEFAULT_FLAGS", 0);		
		sctpData.sndrcvinfo.sinfo_context = config.getInteger("client.DEFAULT_CONTEXT", 0);
		sctpData.sndrcvinfo.sinfo_timetolive = config.getInteger("client.DEFAULT_TTL", 0);
		sctpData.sndrcvinfo.sinfo_tsn = config.getInteger("client.DEFAULT_TSN", 0);
		sctpData.sndrcvinfo.sinfo_cumtsn = config.getInteger("client.DEFAULT_CUMTSN", 0);
		long assocId = (long) config.getInteger("client.DEFAULT_AID", 0);
		sctpData.sndrcvinfo.sinfo_assoc_id = new AssociationId(assocId);

    	return sctpData;
    }

    /**
     * Creates a Msg specific to each Stack
     * Used for SCTP like protocol : to build incoming message
     */
    public Msg readFromSCTPData(SCTPData chunk) throws Exception    
    {
    	return new MsgSctp(this, chunk);
    }

	/** 
     * Create an empty message for transport connection actions (open or close) 
     * and on server side and dispatch it to the generic stack 
     **/
    public void receiveTransportMessage(String type, Channel channel, Listenpoint listenpoint)
    {
    	try 
    	{
    		boolean generateTransportMessage = getConfig().getBoolean("GENERATE_TRANSPORT_MESSAGE", false);
    		if (generateTransportMessage)
    		{
				// create an empty message
				byte[] bytes = new byte[0];
				MsgSctp msg = new MsgSctp(this);
				msg.decode(bytes);
				msg.setType(type);
				msg.setChannel(channel);
				msg.setListenpoint(listenpoint);
				// dispatch it to the generic stack			
				receiveMessage(msg);
    		}
        }
        catch (Exception e)
        {
			GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Exception : Empty message creation for transport action on channel : ", channel);
        }
	
    }
    
}
