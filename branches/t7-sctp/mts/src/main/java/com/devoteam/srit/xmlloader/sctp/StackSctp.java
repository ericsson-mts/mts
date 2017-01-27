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

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.*;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.sctp.ChannelSctp;

import java.net.Socket;

import org.dom4j.Element;

public abstract class StackSctp extends Stack
{
	
	/** Creates a new instance */
	public StackSctp() throws Exception
	{
		super();
	}

    /** Get the protocol of this message */
    @Override
	public String getProtocol() {
		return StackFactory.PROTOCOL_SCTP;  
	}

    /** 
     * Returns the Config object to access the protocol config file 
     */
    @Override
    public Config getConfig() throws Exception
    {
        return Config.getConfigByName("sctp.properties");
    }
	
	/**
	 * Creates a Msg specific to each Stack
	 * 
	 * useful to set breakpoints
	 */
	@Override
    public Msg parseMsgFromXml(Boolean request, Element root, Runner runner) throws Exception
    {
		Msg msg = super.parseMsgFromXml(request, root, runner);
		assert( msg instanceof MsgSctp );
    	return msg;
    }

    /**
     * Creates a Msg specific to each Stack
     * Used for SCTP like protocol : to build incoming message
     */
    public Msg readFromSCTPData(DataSctp chunk) throws Exception    
    {
	  return this.createMsgSctp(chunk);
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
				MsgSctp msg = this.createMsgSctp();
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
    
    /**
     * 
     * @return a new ListenpointSctp instance
     */
    public abstract ListenpointSctp createListenpointSctp() throws Exception;
    /**
     * 
     * @return a new ListenpointSctp instance
     */
    public abstract ListenpointSctp createListenpointSctp(Stack stack) throws Exception;
    
    /**
     * 
     * @return a new ChannelSctp instance
     */
    public abstract ChannelSctp createChannelSctp(Stack stack) throws Exception;
    
    /**
     * 
     * @return a new ChannelSctp instance
     */
    public abstract ChannelSctp createChannelSctp(String name, Listenpoint aListenpoint, Socket aSocket) throws Exception;
    
    /**
     * 
     * @return a new MsgSctp instance
     */
    public abstract MsgSctp createMsgSctp() throws Exception;

    /**
     * 
     * @return a new MsgSctp instance
     */
    public abstract MsgSctp createMsgSctp(DataSctp chunk) throws Exception;

}
