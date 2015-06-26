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

import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.protocol.*;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.core.utils.XMLElementTextMsgParser;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.sip.ListenpointSipCommon;

import java.io.InputStream;
import org.dom4j.Element;

public class StackSmtp extends Stack
{
	
	/** Creates a new instance */
    public StackSmtp() throws Exception
    {
        super();
    }

    /*
     * Get the info for MsgSMTP from xml doc
     */
    public Msg parseMsgFromXml(Boolean request, Element root, Runner runner) throws Exception
    {
    	MsgSmtp msg = (MsgSmtp) super.parseMsgFromXml(request, root, runner);
    	
        String transactionIdStr = root.attributeValue("transactionId");
        if (transactionIdStr != null)
        {
            TransactionId transactionId = new TransactionId(transactionIdStr);
            msg.setTransactionId(transactionId);
            Msg requestSmtp = getInTransaction(transactionId).getBeginMsg();
            msg.setType(requestSmtp.getType());
        }
        return msg;
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
     * Creates a Msg specific to each Channel type
     * should become ABSTRACT later
     */
    @Override
    public Msg readFromStream(InputStream inputStream, Channel channel) throws Exception
    {
        boolean isLastLine = false;
        boolean canBeRequest = true;
        boolean canBeResponse = true;
        boolean exit = false;
        String line = null;
        StringBuilder message = new StringBuilder();
        MsgSmtp msg = null;

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
                    msg = new MsgSmtp(this, message.toString());
                    exit = true;
                }
            }
            else
            {
                exit = true;
            }
        }
        return msg;
    }

}
