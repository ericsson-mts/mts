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

import com.devoteam.srit.xmlloader.core.protocol.*;

import com.devoteam.srit.xmlloader.sctp.*;

import java.net.Socket;

/**
 * @author emicpou
 *
 * @see <a href="http://www.oracle.com/technetwork/articles/javase/index-139946.html">Stream Control Transport Protocol (SCTP) in Java</a>
 * @see <a href="http://docs.oracle.com/javase/8/docs/jre/api/nio/sctp/spec/com/sun/nio/sctp/package-summary.html">Package com.sun.nio.sctp</a>
 * 
 */
public class StackSunNioSctp extends StackSctp
{
	//a change
	
	/** Creates a new instance */
	public StackSunNioSctp() throws Exception
	{
		super();
	}
    
    /**
     * 
     * @return a new ListenpointSctp instance
     */
    @Override
    public ListenpointSctp createListenpointSctp() throws Exception {
    	return new ListenpointSunNioSctp(this);	
    }
    
    /**
     * 
     * @return a new ListenpointSctp instance
     */
    @Override
    public ListenpointSctp createListenpointSctp( Stack stack ) throws Exception {
    	return new ListenpointSunNioSctp(stack);	
    }
	
    /**
     * @return a new ChannelSctp instance
     */
    @Override
    public ChannelSctp createChannelSctp( Stack stack ) throws Exception {
    	return new ChannelSunNioSctp(stack);
    }
    
    /**
     * 
     * @return a new ChannelSctp instance
     */
    @Override
    public ChannelSctp createChannelSctp(String name, Listenpoint aListenpoint, Socket aSocket) throws Exception {
    	return new ChannelSunNioSctp(name,aListenpoint,aSocket);
    }

    /**
     * @return a new MsgSctp instance
     */
    @Override
    public MsgSctp createMsgSctp() throws Exception {
    	return new MsgSunNioSctp(this);
    }

    /**
     * @return a new MsgSctp instance
     */
    @Override
    public MsgSctp createMsgSctp(DataSctp chunk) throws Exception {
    	return new MsgSunNioSctp(this,chunk);
    }
   
}
