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

package com.devoteam.srit.xmlloader.tls;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.TransportStack;

import java.io.InputStream;

/**
 *
 * @author fvandecasteele openChannel
 */
public class StackTls extends TransportStack
{

    /** Constructor */
    public StackTls() throws Exception
    {
        super();
    }
    
    /** 
     * Creates a Msg specific to each Stack
     * should become ABSTRACT later  
     */
    @Override    
    public Msg readFromStream(InputStream  inputStream, Channel channel) throws Exception
    {
    	byte[] buffer = new byte[1500]; // MTU
    	MsgTls msgtls = null;
    		
    	int length = inputStream.read(buffer);    
	    if(length > 0)
	    {
	        byte[] data = new byte[length];
	
	        for(int i=0; i<length; i++)
	        {
	            data[i] = buffer[i];
	        }
	
	        msgtls = new MsgTls(this);
	        msgtls.decode(data);
	    }
        else
        {
            throw new Exception("End of stream detected");
        }
    	return msgtls;
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
				MsgTls msgTls = new MsgTls(this);
		        msgTls.decode(bytes);
				msgTls.setType(type);
				msgTls.setChannel(channel);
				msgTls.setListenpoint(listenpoint);
				// dispatch it to the generic stack			
				receiveMessage(msgTls);
    		}
        }
        catch (Exception e)
        {
			GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Exception : Empty message creation for transport action on channel : ", channel);
        }
	
    }

}
