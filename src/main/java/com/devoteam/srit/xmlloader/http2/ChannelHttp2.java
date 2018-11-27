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

package com.devoteam.srit.xmlloader.http2;

import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;

/**
 *
 * @author qqin
 */

public class ChannelHttp2 extends Channel
{   
	 /** Creates a new instance of Channel */
    public ChannelHttp2(Stack stack) throws Exception
    {
        super(stack);
    }
    
  //---------------------------------------------------------------------
    // methods for the transport
    //---------------------------------------------------------------------

    @Override
    public boolean open() throws Exception {
    	this.transport = getTransport() ;
        return true;
    }

    /** Close a channel */
    @Override
    public boolean close() {
        return true;
    }

    
    /** Send a Msg to Channel */
    @Override
    public boolean sendMessage(Msg msg) throws Exception
    {
    	System.out.println("ChannelHttp2.sendMessage()");
    	return super.sendMessage(msg);
    }

    /** receive a Msg from Channel */
    @Override
    public boolean receiveMessage(Msg msg) throws Exception
    {
    	System.out.println("ChannelHttp2.receiveMessage()");
        return super.receiveMessage(msg);
    }
    
    /** Get the transport protocol */
    @Override
    public String getTransport() 
    {
    	System.out.println("ChannelHttp2.getTransport()");
    	return StackFactory.PROTOCOL_TCP;
    }
}
