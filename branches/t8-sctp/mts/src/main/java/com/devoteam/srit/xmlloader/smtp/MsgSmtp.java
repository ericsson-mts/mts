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

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.coding.text.FirstLine;
import com.devoteam.srit.xmlloader.core.coding.text.TextMessage;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.MessageId;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;

public class MsgSmtp extends Msg {
	
	private String data;
	private String type; // command name
	private String result; // result code

	public boolean typeIsSet = false;
	public boolean resultIsSet = false;

	
    /** Creates a new instance */
    public MsgSmtp(Stack stack) throws Exception 
    {
        super(stack);
    }
    
	/*
	 * Get the string following the commands
	 * getString is used for : - EHLO & HELO - VRFY - HELP - NOOP - EXPN
	 * and all the responses
	 *
	 */
	private String getString(boolean isRequest) throws Exception {
		if (this.data != null) {
			String str = new String(this.data);
			if (isRequest)
				return str.substring(5, str.length()-2);
			else {
				try {
					String res = "";
					String[] strs = str.split("\r\n");
					for(int i = 0; i< strs.length-1; i++){
						if (strs[i].charAt(3) == ('-')){
							strs[i]=strs[i].substring(4)+ " ";
						}
						res += strs[i];
					}
					res+=strs[strs.length-1].substring(4);
					return res;
				} catch (Exception ex) {
					throw new Exception();
				}
			}
		}
		return null;
	}

	/*
	 * Get the sender & receiver
	 */
	private String getFromTo() throws Exception {
		if (this.data != null) {
			String str = new String(this.data);
			if (str.contains("<") && str.contains(">"))
				return str.substring(str.indexOf("<") + 1, str.indexOf(">"));
			else {
				str = str.substring(str.indexOf(":") + 1);
				if (str.contains(" "))
					return str.substring(0, str.indexOf(" "));
				else
					return str.substring(0, str.indexOf("\r\n"));
			}
		}
		return null;
	}

	/*
	 * Get the parameter of command
	 */
	private String getParameterOfCommand() throws Exception {
		if (this.data != null) {
			String str = new String(this.data);
			str = str.substring(str.indexOf(' ', str.indexOf(":")) + 1);
			return str.substring(0, str.indexOf("\r\n"));
		}
		return null;
	}

	public synchronized void setType(String str) {
		this.type = str;
		this.typeIsSet = true;
	}

	public synchronized void setResult(String str) {
		this.result = str;
		this.resultIsSet = true;
	}

	public MessageId getMessageId() throws Exception {
		return null;
	}

    /** 
     * Return true if the message is a request else return false
     */
    @Override
	public boolean isRequest() 
    {
		String str = new String(this.data);
		if (str.endsWith("\r\n.\r\n"))
			return true;
		else
			return !Character.isDigit(this.data.charAt(0));
	}

    /** 
     * Get the type of the message
     * Used for message filtering with "type" attribute and for statistic counters 
     */
	@Override
	public String getType() 
	{
		if (this.typeIsSet)
			return this.type;
		else if (this.data == null) 
		{
			GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "***Error in MsgSmtp : Msg is vide !");
			return null;
		} 
		else 
		{
			String str = new String(this.data).trim();

			if (SmtpDictionary.instance().containsCommand(str.trim().substring(
					0,
					str.trim().contains(" ") ? str.trim().indexOf(" ") : str
							.trim().length()))) 
			{
				this.setType(str.trim().substring(
						0,
						str.trim().contains(" ") ? str.trim().indexOf(" ")
								: str.trim().length()));
				return this.getType();
			}

			try 
			{
				String st = this.data;

				if (st.endsWith("\r\n.\r\n")) 
				{
					this.setType(SmtpDictionary.DEFINI_CNTT);
					return this.type;
				}
			} 
			catch (Exception e) 
			{
				GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Exception in getType !");
			}
			this.typeIsSet = true;
			return this.getType();
		}
	}

    /** 
     * Get the result of the message (null if request)
     * Used for message filtering with "result" attribute and for statistic counters 
     */
	@Override
	public String getResult() 
	{
		if (this.resultIsSet)
			return this.result;
		else if (this.data == null) 
		{
			GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "Error in MsgSmtp : Msg is vide !");
			return null;
		} 
		else 
		{
			String str = new String(this.data).trim();

			if (SmtpDictionary.instance().containsResult(str.substring(0, 3))) 
			{
				this.setResult(str.substring(0, 3));
			}
			this.resultIsSet = true;
			return this.getResult();
		}
	}
	
	/** Return the transport of the message */
	public String getTransport() 
	{
		return StackFactory.PROTOCOL_TCP;
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
        return this.data.getBytes();
    }
    
    /** 
     * decode the message from binary data 
     */
    @Override 
    public void decode(byte[] data) throws Exception
    {
    	String str = new String(data); 
		str = Utils.replaceNoRegex(str, "\r\n", "\n");
		str = Utils.replaceNoRegex(str, "\n", "\r\n");
        if(!str.endsWith("\r\n")) str += "\r\n";
        
        this.data = str;
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
    	ret += new String(data.getBytes(), 0, Math.min(data.substring(0,data.indexOf("\n")).length(), 100 ), "UTF8");
		return ret;
	}

    /** 
     * Convert the message to XML document 
     */
    @Override
    public String toXml() throws Exception 
    {
    	return data;
    }

    /** 
     * Parse the message from XML element 
     */
    @Override
    public void parseFromXml(Boolean request, Element root, Runner runner) throws Exception
    {
    	super.parseFromXml(request,root,runner);

    	this.data = root.getText();
		this.data = Utils.replaceNoRegex(this.data, "\r\n", "\n");
		this.data = Utils.replaceNoRegex(this.data, "\n", "\r\n");
        if(!this.data.endsWith("\r\n"))
        {
        	this.data += "\r\n";
        }
    }
    
    //------------------------------------------------------
    // method for the "setFromMessage" <parameter> operation
    //------------------------------------------------------

    /** 
     * Get a parameter from the message
     */
	@Override
	public Parameter getParameter(String path) throws Exception 
	{	
		Parameter var = super.getParameter(path);
		if (null != var) 
		{
			return var;
		}

    	var = new Parameter();
        path = path.trim();
        String[] params = Utils.splitPath(path);

		if (params[0].equalsIgnoreCase("data")) 
		{
			if (params[1].equalsIgnoreCase("text")) 
			{
				var.add(this.data);
			} 
			else if (params[1].equalsIgnoreCase("binary")) 
			{			
				var.add(Array.toHexString(new DefaultArray(this.data.getBytes())));
			} 
			else 
			{
            	Parameter.throwBadPathKeywordException(path);
			}
		} 
		else if (params[0].equalsIgnoreCase("firstline")) 
		{
			if (params[1].equalsIgnoreCase("commandName")) 
			{
				var.add(this.type);
			} 
			else if (params[1].equalsIgnoreCase("string")) 
			{
				var.add(this.getString(true));
			} 
			else if (params[1].equalsIgnoreCase("from")) 
			{
				var.add(this.getFromTo());
			} 
			else if (params[1].equalsIgnoreCase("to")) 
			{
				var.add(this.getFromTo());
			} 
			else if (params[1].equalsIgnoreCase("parameter")) 
			{
				var.add(this.getParameterOfCommand());
			} 
			else if (params[1].equalsIgnoreCase("replyCode")) 
			{
				var.add(this.getResult());
			} 
			else if (params[1].equalsIgnoreCase("reasonPhrase")) 
			{
				var.add(this.getString(false));
			} 
			else 
			{
            	Parameter.throwBadPathKeywordException(path);
			}

		} 
		else 
		{
        	Parameter.throwBadPathKeywordException(path);
		}

		return var;
	}
    
    public boolean isSTARTTLS_request()
    {
   		if (data.toLowerCase().contains("STARTTLS".toLowerCase()))
   			return true;    	
    	return false;
    }
    
    public boolean isSTARTTLS_answer()
    {
   		if (data.toLowerCase().contains("220 Go ahead".toLowerCase()))
   			return true;    	
    	return false;
    }
}
