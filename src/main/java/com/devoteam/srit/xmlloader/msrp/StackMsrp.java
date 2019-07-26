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

package com.devoteam.srit.xmlloader.msrp;

import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.Trans;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.core.utils.XMLElementTextMsgParser;

import java.io.InputStream;

public class StackMsrp extends Stack 
{

	/** Creates a new instance */
	public StackMsrp() throws Exception 
	{
		super();
	}
    
    /** Send a Msg to Stack */
    @Override
    public synchronized boolean sendMessage(Msg msg) throws Exception {    	        
    	
    	// copy the channel from the request into the response using the transaction
        Trans trans = msg.getTransaction();       
        if (trans != null)
        {
        	Channel channel = trans.getBeginMsg().getChannel();
            msg.setChannel(channel);
        }
                
    	return super.sendMessage(msg);        
    }

    /** 
     * Read the message data from the stream
     * Use for TCP/TLS like protocol : to build incoming message  
     */
    @Override
    public byte[] readMessageFromStream(InputStream inputStream) throws Exception
    {
        String text = null;
    	synchronized (inputStream)
    	{
			text = this.reader(inputStream);
    	}

    	return text.getBytes(); 
    }

    private String reader(InputStream inputStream) throws Exception 
    {
        StringBuilder message = new StringBuilder();
        String line = "";

        while (!line.startsWith("-------") && (!line.endsWith("$") || !line.endsWith("+") || !line.endsWith("#")))
        {
            line = Utils.readLineFromInputStream(inputStream);
            message.append(line);
        }
        
        return message.toString();
    }
}
