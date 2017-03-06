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
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.coding.text.MsgParser;

import java.util.Vector;

import org.dom4j.Element;

public class MsgPop extends Msg
{
    private String          dataRaw = "";//just the message to read/write on socket
    private Vector<String>  arguments = null;

    /** Creates a new instance */
    public MsgPop(Stack stack) throws Exception
    {
        super(stack);
    }

    /** Creates a new instance */
    public MsgPop(Stack stack, String someData, Channel channel) throws Exception 
    {
    	this(stack);
       decode(someData.getBytes());
       setChannel(channel);
    }
    
    /** 
     * Return true if the message is a request else return false
     */
	@Override
    public boolean isRequest() 
	{
        String[] msgSplit = Utils.splitNoRegex(dataRaw.trim(), " ");
        if(msgSplit[0].contains("+") || msgSplit[0].contains("-"))
        {
            return false;
        }
        return true;
    }
    
    /** 
     * Get the type of the message
     * Used for message filtering with "type" attribute and for statistic counters 
     */
	@Override
    public String getType() 
    {
        if(isRequest())//to not try to get command on a response
        {
            String[] msgSplit = Utils.splitNoRegex(dataRaw.trim(), " ");
            type = msgSplit[0];
        }
        else//refer to the transaction request in case of response to get command
        {
            try
            {
                if(isSend())
                    type = this.stack.getInTransaction(getTransactionId()).getBeginMsg().getType();
                else
                    type = this.stack.getOutTransaction(getTransactionId()).getBeginMsg().getType();
            }
            catch(Exception e) 
            {
            	
            }
        }
        return type.toUpperCase();
	}

    /** 
     * Get the result of the message (null if request)
     * Used for message filtering with "result" attribute and for statistic counters 
     */
	@Override
    public String getResult()
    {
        if (!isRequest())
        {        	
            String[] msgSplit1 = Utils.splitNoRegex(dataRaw.trim(), "\r\n");
            String[] msgSplit2 = Utils.splitNoRegex(msgSplit1[0], " ");
            if(msgSplit2[0].startsWith("+") || msgSplit2[0].startsWith("-"))
            {
                msgSplit2[0] = msgSplit2[0].substring(1);
            }
            result = msgSplit2[0];
        }
        return result.toUpperCase();
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

    //-------------------------------------------------
    // methods for the encoding / decoding of the message
    //-------------------------------------------------
    
    /** 
     * encode the message to binary data 
     */    
    @Override
    public byte[] encode() throws Exception
    {
        return this.dataRaw.getBytes();
    }
    
    /** 
     * decode the message from binary data 
     */
    public void decode(byte[] data) throws Exception
    {
    	String text = new String(data);
        text = text.replace("\r\n", "\n");
        this.dataRaw = text.replace("\n", "\r\n");

        if(!this.dataRaw.endsWith("\r\n"))
        {
            this.dataRaw += "\r\n";
        }    	
    }

    
    //---------------------------------------------------------------------
    // methods for the XML display / parsing of the message
    //---------------------------------------------------------------------

    /** Returns a short description of the message. Used for logging as INFO level */
    /** This methods HAS TO be quick to execute for performance reason */
    @Override
	public String toShortString() throws Exception 
    {
    	String ret = super.toShortString();
    	ret += "\n";
        ret += new String(dataRaw.getBytes(), 0, Math.min(dataRaw.length(), 100), "UTF8");
		return ret;
    }
    
    /** 
     * Convert the message to XML document 
     */
    @Override
    public String toXml() throws Exception 
    {
        return dataRaw;
    }
 
    /** 
     * Parse the message from XML element 
     */
    @Override
    public void parseFromXml(ParseFromXmlContext context, Element root, Runner runner) throws Exception
    {
    	super.parseFromXml(context,root,runner);

    	String text = root.getText().trim();
    	decode(text.getBytes());
    }

    /** Set the message from text */
    public String getMessageText() 
    {
        String[] msgSplit = Utils.splitNoRegex(dataRaw.trim(), " ");
        return  dataRaw.substring(dataRaw.indexOf(msgSplit[1]));
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