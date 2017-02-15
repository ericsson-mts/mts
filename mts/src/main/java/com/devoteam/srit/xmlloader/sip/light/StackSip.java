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

package com.devoteam.srit.xmlloader.sip.light;

import com.devoteam.srit.xmlloader.core.Runner;

import java.io.InputStream;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.Tester;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.sip.StackSipCommon;

/**
 *
 * @author gpasquiers
 */
public class StackSip extends StackSipCommon
{
	
	public String contentBinaryTypes = null;
        
    /** Constructor */
    public StackSip() throws Exception
    {
        super();        
        
        this.contentBinaryTypes = "," + getConfig().getString("content.BINARY_TYPES", "") + ",";
    }
    
    /** Receive a message */
    @Override
    public boolean receiveMessage(Msg msg) throws Exception
    {
		((MsgSip) msg).completeViaTopmostHeader();
		return super.receiveMessage(msg);
    }

    /** 
     * Creates a Msg specific to each Stack
     * Used for TCP/TLS like protocol : to build incoming message
     */
    @Override    
    public byte[] readMessageFromStream(InputStream  inputStream) throws Exception
    {
    	String text = null;
    	synchronized (inputStream)
    	{
			text = this.reader(inputStream);
    	}
    	
    	return text.getBytes();
    }
        
}
