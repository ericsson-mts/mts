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

import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;

import com.devoteam.srit.xmlloader.sctp.*;

import dk.i1.sctp.*;

import java.net.Socket;

public class StackLksctp extends StackSctp
{
	//a change
	
	/** Creates a new instance */
	public StackLksctp() throws Exception
	{
		super();
	}
	
    /**
     * Creates a Msg specific to each Stack
     * Used for SCTP like protocol : to build incoming message
     */
    public Msg readFromSCTPData(SCTPData chunk) throws Exception    
    {
    	return new MsgLksctp(this, chunk);
    }
    
    /**
     * 
     * @return a new ListenpointSctp instance
     */
    @Override
    public ListenpointSctp createListenpointSctp() throws Exception {
    	return new ListenpointLksctp(this);	
    }
    
    /**
     * 
     * @return a new ListenpointSctp instance
     */
    @Override
    public ListenpointSctp createListenpointSctp( Stack stack ) throws Exception {
    	return new ListenpointLksctp(stack);	
    }
	
    /**
     * @return a new ChannelSctp instance
     */
    @Override
    public ChannelSctp createChannelSctp( Stack stack ) throws Exception {
    	return new ChannelLksctp(stack);
    }
    
    /**
     * 
     * @return a new ChannelSctp instance
     */
    @Override
    public ChannelSctp createChannelSctp(String name, Listenpoint aListenpoint, Socket aSocket) throws Exception {
    	return new ChannelLksctp(name,aListenpoint,aSocket);
    }

    /**
     * @return a new MsgSctp instance
     */
    @Override
    public MsgSctp createMsgSctp() throws Exception {
    	return new MsgLksctp(this);
    }

    /**
     * @return a new MsgSctp instance
     */
    @Override
    public MsgSctp createMsgSctp(DataSctp chunk) throws Exception {
    	return new MsgLksctp(this,chunk);
    }
	
	public static void configSctp2initMsg( ChannelConfigSctp configSctp,sctp_initmsg initMsg  ){
		initMsg.sinit_num_ostreams = configSctp.num_ostreams;
		initMsg.sinit_max_instreams = configSctp.max_instreams;
		initMsg.sinit_max_attempts = configSctp.max_attempts;
		initMsg.sinit_max_init_timeo= configSctp.max_init_timeo;
	}
}
