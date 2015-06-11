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

package com.devoteam.srit.xmlloader.sip.jain;

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
import com.devoteam.srit.xmlloader.sip.MsgSip;
import com.devoteam.srit.xmlloader.sip.StackSip;

import dk.i1.sctp.SCTPData;

/**
 *
 * @author gpasquiers
 */
public class StackSipJain extends StackSip
{

    /** Constructor */
    public StackSipJain() throws Exception
    {
        super();     
    }
        
    /** Receive a message */
    @Override
    public boolean receiveMessage(Msg msg) throws Exception
    {
		((MsgSipJain) msg).completeViaTopmostHeader();
		return super.receiveMessage(msg);
    }

    /** 
     * Creates a Msg specific to each Stack
     * Use for TCP like protocol : to build incoming message
     * should become ABSTRACT later  
     */
    @Override    
    public Msg readFromStream(InputStream  inputStream, Channel channel) throws Exception
    {
    	String text = null;
    	synchronized (inputStream)
    	{
    		text = this.reader(inputStream);
    	}
    	
		if (text != null && text.contains(StackFactory.PROTOCOL_SIP)) 
		{
			MsgSipJain msgSip = new MsgSipJain(this);
	    	msgSip.setMessageText(text, 0);
			return msgSip;
		}
		else
		{
			Tester.getGlobalLogger().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "Receive an incomplete message; we ignore it : ", text);			
		}


    	return null;
    }

    /** 
     * Creates a Msg specific to each Stack
     * Use for UDP like protocol : to build incoming message
     * should become ABSTRACT later  
     */
    @Override	
    public Msg readFromDatas(byte[] datas, int length) throws Exception
    {
    	String str = new String(datas);
    	str = str.substring(0, length);
    	MsgSipJain msgSip = new MsgSipJain(this);
    	msgSip.setMessageText(str, 0);
    	return msgSip;
    }

    /**
     * Creates a Msg specific to each Stack
     * Use for SCTP like protocol : to build incoming message
     */
    @Override
    public Msg readFromSCTPData(SCTPData chunk) throws Exception
    {
    	String str = new String(chunk.getData());
    	str = str.substring(0, chunk.getLength());
        MsgSipJain msgSip = new MsgSipJain(this);
    	msgSip.setMessageText(str, 0);
        return msgSip;            
    }

}
