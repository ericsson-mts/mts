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

package com.devoteam.srit.xmlloader.tcp;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.TransportStack;

import java.io.InputStream;


/**
 *
 * @author gpasquiers
 */
public class StackTcp extends TransportStack
{
 	
	/** Creates a new instance */
    public StackTcp() throws Exception
    {
        super();
    }
    
    /** 
     * Creates a Msg specific to each Stack
     * Used for TCP/TLS like protocol : to build incoming message
     */
    @Override    
    public byte[] readMessageFromStream(InputStream  inputStream) throws Exception
    {
    	byte[] buffer = new byte[1500]; // MTU
    		
    	int length = inputStream.read(buffer);    
	    if(length > 0)
	    {
	        byte[] data = new byte[length];
	
	        for(int i=0; i<length; i++)
	        {
	            data[i] = buffer[i];
	        }
	        return data;
	    } 
	    
        throw new Exception("End of stream detected");
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
				MsgTcp msg = new MsgTcp(this);
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