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

package com.devoteam.srit.xmlloader.pop;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.coding.text.MsgParser;

import java.util.Vector;

import org.dom4j.Element;

public class MsgPop extends Msg
{
    private String          type = "";
    private String          result = "";
    private String          dataRaw = "";//just the message to read/write on socket
    private Boolean         isRequest = null;
    private Vector<String>  arguments = null;
    private String          text = "";

    /** Creates a new instance */
    public MsgPop() throws Exception
    {
        super();
    }

    /** Creates a new instance */
    public MsgPop(String someData, Channel channel) throws Exception {
       setMessageText(someData);
       setChannel(channel);
    }
    
    // --- heritage methods --- //
    public String getProtocol(){
        return StackFactory.PROTOCOL_POP;
    }

    public String getType() {
        if(type.equalsIgnoreCase(""))
        {
            if(isRequest())//to not try to get command on a response
            {
                String[] msgSplit = Utils.splitNoRegex(dataRaw.trim(), " ");
                type = msgSplit[0];
            }
            else//refer to the transaction request in case of response to get command
            {
                try{
                    if(isSend())
                        type = StackFactory.getStack(getProtocol()).getInTransaction(getTransactionId()).getBeginMsg().getType();
                    else
                        type = StackFactory.getStack(getProtocol()).getOutTransaction(getTransactionId()).getBeginMsg().getType();
                }
                catch(Exception e) {}
            }
        }
        return type.toUpperCase();
	}

    public String getResult(){
        if (!isRequest()){        	
        	if(result.equalsIgnoreCase(""))
            {
                String[] msgSplit1 = Utils.splitNoRegex(dataRaw.trim(), "\r\n");
                String[] msgSplit2 = Utils.splitNoRegex(msgSplit1[0], " ");
                if(msgSplit2[0].startsWith("+") || msgSplit2[0].startsWith("-"))
                {
                    msgSplit2[0] = msgSplit2[0].substring(1);
                }
                result = msgSplit2[0];
            }
        }
        return result.toUpperCase();
    }
    
    public boolean isRequest() {
        if(isRequest == null)
        {
            String[] msgSplit = Utils.splitNoRegex(dataRaw.trim(), " ");
            if(msgSplit[0].contains("+") || msgSplit[0].contains("-"))
            {
                isRequest = false;
            }
            else
            {
                isRequest = true;
            }
        }
        return isRequest;
    }
	
    public Vector<String> getArguments() {
        if(isRequest())
        {
            arguments = new Vector<String>();
            MsgParser.split(arguments,
                            dataRaw.substring(dataRaw.indexOf(getType()) + getType().length()).trim(),
                            " ",
                            true);
        }
        return arguments;
    }

    public boolean shouldResponseBeMultiLine()
    {
        boolean res = false;
        if(getType().equalsIgnoreCase("RETR")
           || getType().equalsIgnoreCase("TOP")
           ||(getType().equalsIgnoreCase("LIST") && getArguments().isEmpty())
           || (getType().equalsIgnoreCase("UIDL") && getArguments().isEmpty()))
        {
            res = true;
        }
        return res;
    }

    /** Get the data (as binary) of this message */    
    @Override
    public byte[] encode(){
        return this.dataRaw.getBytes();
    }

    /** Returns a short description of the message. Used for logging as INFO level */
    /** This methods HAS TO be quick to execute for performance reason */
    @Override
	public String toShortString() throws Exception {
    	String ret = super.toShortString();
    	ret += "\n";
        ret += new String(dataRaw.getBytes(), 0, Math.min(dataRaw.length(), 100), "UTF8");
		return ret;
    }
    
    /** Get the XML representation of the message; for the genscript module. */
    @Override
    public String toXml() throws Exception {
        return dataRaw;
    }
 
    /** 
     * Parse the message from XML element 
     */
    @Override
    public void parseFromXml(Boolean request, Element root, Runner runner) throws Exception
    {
    	String text = root.getText().trim();
    	setMessageText(text);
    }

    /** Set the message from text */
    public String getMessageText() {
        if(text.equalsIgnoreCase(""))
        {
            String[] msgSplit = Utils.splitNoRegex(dataRaw.trim(), " ");
            text = dataRaw.substring(dataRaw.indexOf(msgSplit[1]));
        }
        return text;
    }
    
    /** Set the message from text */
    public void setMessageText(String text) throws Exception
    {
        text = text.replace("\r\n", "\n");
        this.dataRaw = text.replace("\n", "\r\n");

        if(!this.dataRaw.endsWith("\r\n"))
        {
            this.dataRaw += "\r\n";
        }
    }

	// ------------------------------------------------------
    // method for the "setFromMessage" <parameter> operation
    //------------------------------------------------------

    /** 
     * Get a parameter from the message 
     */
    @Override
	public synchronized Parameter getParameter(String path) throws Exception 
	{
		Parameter var = super.getParameter(path);
		if (var != null) 
		{
			return var;
		}

		var = new Parameter();
        path = path.trim();
		String[] params = Utils.splitPath(path);
		
        if (params[0].equalsIgnoreCase("request"))
		{
            if(params[1].equalsIgnoreCase("command") && (params.length == 2))
            {
                var.add(getType());
            }
            else if(params[1].equalsIgnoreCase("arguments") && (params.length == 2))
            {
                this.addVector(var, this.getArguments());                
            }
            else
            {
            	Parameter.throwBadPathKeywordException(path);
            }
		}
        else if (params[0].equalsIgnoreCase("response"))
		{
            if(params[1].equalsIgnoreCase("result") && (params.length == 2))
            {
                var.add(getResult());
            }
            else if(params[1].equalsIgnoreCase("text") && (params.length == 2))
            {
                var.add(getMessageText());
            }
            else
            {
            	Parameter.throwBadPathKeywordException(path);
            }
		}
		//---------------------------------------------------------------------- content(X): -
        else if (params[0].equalsIgnoreCase("content"))
		{
            var.add(dataRaw);
		}
        else
        {
        	Parameter.throwBadPathKeywordException(path);
        }

		return var;
	}

    /** Get the messages Identifier of this messages */
    private void addVector(Parameter var, Vector vect) throws Exception
    {
        for (int i = 0; i < vect.size(); i++)
        {
            var.add(vect.get(i).toString());
        }
    }

    public boolean isSTARTTLS_request()
    {
   		if (dataRaw.toLowerCase().contains("STLS".toLowerCase()))
   			return true;    	
    	return false;
    }
    
    public boolean isSTARTTLS_answer()
    {
   		if (dataRaw.toLowerCase().contains("+OK Begin TLS negotiation".toLowerCase()))
   			return true;    	
    	return false;
    }
}