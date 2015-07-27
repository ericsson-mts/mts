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

package com.devoteam.srit.xmlloader.smtp;

import com.devoteam.srit.xmlloader.core.protocol.*;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.protocol.Stack;

import java.io.InputStream;

public class StackSmtp extends Stack
{
	
	/** Creates a new instance */
    public StackSmtp() throws Exception
    {
        super();
    }

    /** Send a Msg to Stack */
    @Override
    public synchronized boolean sendMessage(Msg msg) throws Exception
    { 
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
    public byte[] readMessageFromStream(InputStream inputStream) throws Exception
    {
        boolean isLastLine = false;
        boolean canBeRequest = true;
        boolean canBeResponse = true;
        boolean exit = false;
        String line = null;
        StringBuilder message = new StringBuilder();

        while (!exit)
        {
            line = Utils.readLineFromInputStream(inputStream);
            if(line != null)
            {
                message.append(line);

                // is it a request ?
                if (canBeRequest && (line.length() >= 4) && (SmtpDictionary.instance().containsCommand(line.substring(0, 4)) || (line.length() >= 8 && SmtpDictionary.instance().containsCommand(line.substring(0, 8)))))
                {
                    isLastLine = true;
                }
                else
                {
                    canBeRequest = false;
                }

                // is it a response ?
                if (canBeResponse && (line.length() >= 3) && SmtpDictionary.instance().containsResult(line.substring(0, 3)) && (line.charAt(3) == ' '))
                {
                    isLastLine = true;
                }
                else if(canBeResponse && (line.length() >= 3) && !SmtpDictionary.instance().containsResult(line.substring(0, 3)))
                {
                    canBeResponse = false;
                }

                // is it data ? (received data with \r\n.\r\n at the end)
                if(message.toString().endsWith("\r\n.\r\n"))
                {
                    isLastLine = true;
                }
                
                if (isLastLine)
                {
                	return message.toString().getBytes();
                }
            }
            else
            {
                return null;
            }
        }
        return null;
    }

}
