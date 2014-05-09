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

package com.devoteam.srit.xmlloader.imap;

import java.util.Vector;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.coding.text.MsgParser;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.MessageId;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.tcp.ChannelTcp;
import com.devoteam.srit.xmlloader.tcp.bio.ChannelTcpBIO;
import com.devoteam.srit.xmlloader.tcp.nio.ChannelTcpNIO;
import com.devoteam.srit.xmlloader.tls.ChannelTls;

public class MsgImap extends Msg {
	
    private String          dataRaw = "";//just the message to read/write on socket
    private String          dataComplete = "";//concatenation of all messages of provisionnal request/response
    private String          tag = "";
    private String          command = "";
    private Vector<String>  arguments = null;
    private String          result = "";
    private String          text = null;
    private Boolean         isRequest = null;
    
    private Vector<String> messages = null;

    public MsgImap(String someData) throws Exception {
        super();

        someData = someData.replace("\r\n", "\n");
        dataRaw = someData.replace("\n", "\r\n");

        if(!dataRaw.endsWith("\r\n"))
            dataRaw += "\r\n";

        dataComplete = dataRaw;
        messages = new Vector<String>();

        String[] msgSplit = Utils.splitNoRegex(dataRaw, "\r\n");
        for(int i = 0; i < (msgSplit.length - 1); i++)//msgSplit.length - 1 because the last will be empty with a \r\n
        {
            messages.add(msgSplit[i] + "\r\n");
        }

        checkLiteral();

        //no need to do another processing because this messages is
        //ready to be sent and no operation to get data on it will be done
        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "Msg Imap is: ", toString());
    }

    public MsgImap(String someData, Channel channel) throws Exception {
        this(someData);
        
        setChannel(channel);
        setTransactionId(((ChannelImap)channel).getTransactionId());
    }

	public MsgImap(Vector<String> someData) throws Exception {
		super();
		for(int i = 0; i < someData.size(); i++)
		{
		    dataRaw += someData.get(i);
		}
		dataRaw = dataRaw.replace("\r\n", "\n");
		dataRaw = dataRaw.replace("\n", "\r\n");

		if(!dataRaw.endsWith("\r\n"))
            dataRaw += "\r\n";
        
        dataComplete = dataRaw;
		messages = someData;

        checkLiteral();
        
		GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "Msg Imap is: ", toString());
	}
	
	/*
	 * Get parameters from the command/reply lines
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
		
		if (params[0].equalsIgnoreCase("request"))
        {
            if (params[1].equalsIgnoreCase("tag")) 
            {
            	var.add(this.getTag());
            }
            else if (params[1].equalsIgnoreCase("command")) 
            {
            	var.add(this.getType());
            }
            else if (params[1].equalsIgnoreCase("arguments")) 
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
    		if (params[1].equalsIgnoreCase("done")) 
    		{
                if (params[2].equalsIgnoreCase("tag")) 
                {
                	var.add(this.getTag());
                }
                else if (params[2].equalsIgnoreCase("command")) 
                {
                	var.add(this.getType());
                }
                else if (params[2].equalsIgnoreCase("result")) 
                {
                	var.add(this.getResult());
                }
                else if (params[2].equalsIgnoreCase("text")) 
                {
                	var.add(this.getText());
                }
                else 
                {
                	Parameter.throwBadPathKeywordException(path);
                }
    		}
    		else if (params[1].equalsIgnoreCase("continue")) 
    		{
                if (params[2].equalsIgnoreCase("tag"))
                {
                	var.add(this.getTag());
                }
                else if (params[2].equalsIgnoreCase("text"))
                {
                	var.add(this.getText());
                }
                else 
                {
                	Parameter.throwBadPathKeywordException(path);
                }
    		}
    		else if (params[1].equalsIgnoreCase("data")) 
    		{
                //search for param[2] passed in argument
                for(int i = 0; i < messages.size(); i++)
                {
                    if(messages.elementAt(i).contains(params[2]))
                    {
                    	var.add(messages.elementAt(i).trim());
                    }
                }
    		}
            else 
            {
            	Parameter.throwBadPathKeywordException(path);
            }
		}
		else if (params[0].equalsIgnoreCase("data"))
        {
			var.add(dataComplete.replace("\0", ""));
        }
        else 
        {
        	Parameter.throwBadPathKeywordException(path);
        }
    	
		return var;
	}

	/** Return true if the messages is a request else return false */
	public boolean isRequest() {
        if(isRequest == null)
        {
            if(isSend())
            {
                if(((ChannelImap)this.getChannel()).isServer())
                    isRequest = false;
                else
                    isRequest = true;
            }
            else
            {
                if(getChannel().getTransport().equalsIgnoreCase(StackFactory.PROTOCOL_TCP))
                {
                    if(getChannel() instanceof ChannelTcp && ((ChannelTcp) getChannel()).getListenpointTcp() != null)
                        isRequest = true;
                    else if(getChannel() instanceof ChannelTcpBIO && ((ChannelTcpBIO) getChannel()).getListenpointTcp() != null)
                        isRequest = true;
                    else if(getChannel() instanceof ChannelTcpNIO && ((ChannelTcpNIO) getChannel()).getListenpointTcp() != null)
                        isRequest = true;
                    else
                        isRequest = false;
                }
                else
                {
                    if(((ChannelTls)this.getChannel()).getListenpointTLS() != null)
                        isRequest = true;
                    else
                        isRequest = false;
                }
            }
        }
        return isRequest;
	}

	/** Return the transport of the messages */
    @Override
	public String getTransport() {
		return getChannel().getTransport();
	}
	
    /** Get the protocol of this messages */
    public String getProtocol() {
        return StackFactory.PROTOCOL_IMAP;
    }
    
    public String getType() {//cmd
        if(command.equalsIgnoreCase(""))
        {
            if(isRequest())//to not try to get command on a response
            {
                String[] msgSplit = Utils.splitNoRegex(messages.firstElement().trim(), " ");
                if(msgSplit[0].matches("\\p{Alnum}{1,4}"))
                {
                    tag = msgSplit[0];
                    command = msgSplit[1];
                }
            }
            else//refer to the transaction request in case of response to get command
            {
                setTypeFromPreviousRequest();
            }
        }
        return command.toUpperCase();
    }

    private void setTypeFromPreviousRequest()
    {
        try{
            MsgImap msg;
            //vérifier si server ou client pour savoir quelle liste de transaction utiliser
            if(isSend())
            {
                if(((ChannelImap)this.getChannel()).isServer())
                    msg = (MsgImap) StackFactory.getStack(StackFactory.PROTOCOL_IMAP).getInTransaction(getTransactionId()).getBeginMsg();
                else
                    msg = (MsgImap) StackFactory.getStack(StackFactory.PROTOCOL_IMAP).getOutTransaction(getTransactionId()).getBeginMsg();
            }
            else
            {
                if(getChannel().getTransport().equalsIgnoreCase(StackFactory.PROTOCOL_TCP))
                {
                    if(getChannel() instanceof ChannelTcp && ((ChannelTcp) getChannel()).getListenpointTcp() != null)
                        msg = (MsgImap) StackFactory.getStack(StackFactory.PROTOCOL_IMAP).getInTransaction(getTransactionId()).getBeginMsg();
                    else if(getChannel() instanceof ChannelTcpBIO && ((ChannelTcpBIO) getChannel()).getListenpointTcp() != null)
                        msg = (MsgImap) StackFactory.getStack(StackFactory.PROTOCOL_IMAP).getInTransaction(getTransactionId()).getBeginMsg();
                    else if(getChannel() instanceof ChannelTcpNIO && ((ChannelTcpNIO) getChannel()).getListenpointTcp() != null)
                        msg = (MsgImap) StackFactory.getStack(StackFactory.PROTOCOL_IMAP).getInTransaction(getTransactionId()).getBeginMsg();
                    else
                        msg = (MsgImap) StackFactory.getStack(StackFactory.PROTOCOL_IMAP).getOutTransaction(getTransactionId()).getBeginMsg();
                }
                else
                {
                    if(((ChannelTls)this.getChannel()).getListenpointTLS() != null)
                        msg = (MsgImap) StackFactory.getStack(StackFactory.PROTOCOL_IMAP).getInTransaction(getTransactionId()).getBeginMsg();
                    else
                        msg = (MsgImap) StackFactory.getStack(StackFactory.PROTOCOL_IMAP).getOutTransaction(getTransactionId()).getBeginMsg();
                }
            }
            command = msg.getType();
        }
        catch(Exception e) {
//            System.out.println("exception catched while searching type in previous request");
        }
    }

    public String getResult() throws Exception {//response
        if(!isRequest())//to not try to get result on a request
        {
            if(result.equalsIgnoreCase(""))
            {
                String[] msgSplit = Utils.splitNoRegex(messages.lastElement().trim(), " ");
                if(msgSplit[0].matches("\\p{Alnum}{1,4}"))
                {
                    tag = msgSplit[0];
                    result = msgSplit[1];
                    if((msgSplit.length > 2) && msgSplit[2].startsWith("[") && msgSplit[2].endsWith("]"))//resultCode is present
                        result += " " + msgSplit[2];
                }
            }
        }
        return result.toUpperCase();
    }

    public String getTag() {
        if(tag.equalsIgnoreCase(""))
        {
            //dernière ligne(si requete, c'est aussi la première, si reponse, c'est la dernière)
            String[] msgSplit = Utils.splitNoRegex(messages.lastElement().trim(), " ");

            if(msgSplit[0].matches("\\p{Alnum}{1,4}") || msgSplit[0].equals("+"))
            {
                tag = msgSplit[0];
            }
        }
        return tag;
    }
    
    public Vector<String> getArguments() {
        if(isRequest())
        {
            arguments = new Vector<String>();
            MsgParser.split(arguments,
                            dataComplete.substring(dataComplete.indexOf(getType()) + getType().length() + 1),
                            " ", true, "()", "\0\0");
        }
        return arguments;        
    }
    
    public String getText() {
        if(text ==  null)
        {
            //uniquement pour les reponse continue et les reponse done => derniere ligne
            String[] msgSplit = Utils.splitNoRegex(messages.lastElement().trim(), " ");
            if(msgSplit[0].equalsIgnoreCase("+"))
            {
            	if (msgSplit.length > 1)
            	{
            		text = dataRaw.substring(dataRaw.indexOf(msgSplit[1])).trim();
            	}
            }
            else
            {
            	if (msgSplit.length > 2)
            	{
            		text = dataRaw.substring(dataRaw.indexOf(msgSplit[2])).trim();
            	}
            }
        }
        return text;
    }
    
    @Override
    public MessageId getMessageId() throws Exception {
        return null;
    }

    /** Get the messages Identifier of this messages */
    private void addVector(Parameter var, Vector vect) throws Exception
    {
        for (int i = 0; i < vect.size(); i++)
        {
            var.add(vect.get(i).toString());
        }
    }

    public boolean isIncompleteMessage()
    {
        boolean res = false;
        //always check the last message present in the vector
        String[] msgSplit = Utils.splitNoRegex(messages.lastElement(), " ");
        if(msgSplit[msgSplit.length-1].matches("\\{\\d*\\}\r\n"))
            res = true;
        return res;
    }

    public void addNewMessage(String message)
    {
        messages.add(message);
        dataComplete += message;
        dataRaw = message;

        checkLiteral();
    }

    public String getDataRaw(){
        return dataRaw;
    }

    public String getDataComplete(){
        return dataComplete;
    }

    private void checkLiteral(){
        String[] msgSplit;
        String tmp;
        String firstTag = " {";
        String secondTag = "}\r\n";

        //modification of data to integrate literal in request or response
        //get literal
        int index = dataComplete.indexOf(firstTag);
        int index2 = dataComplete.indexOf(secondTag);
        int nbChar = 0;
        if((index != -1) && (index2 != -1))
        {
            nbChar = Integer.parseInt(dataComplete.substring(index + 2, index2));
            if((index2 + secondTag.length() + nbChar) <= dataComplete.length())//check that nb indicate is not > to length or data
            {
                tmp = dataComplete.substring(index2 + secondTag.length(), index2 + secondTag.length() + nbChar);
                //replace {xx} by \0literal\0 and remove literal from dataComplete
                dataComplete = dataComplete.replace(tmp, "").replace(firstTag + dataComplete.substring(index + 2, index2) + secondTag,
                                                                         "\r\n\0" + tmp + "\0");
            }
        }
        
        //vector parsing to integrate modification to it
        for(int i = 0; i < messages.size(); i++)
        {
            tmp = messages.elementAt(i);
            msgSplit = Utils.splitNoRegex(tmp, " ");

            if(msgSplit[msgSplit.length - 1].matches("\\{\\d*\\}\r\n"))
            {
                index = tmp.indexOf(firstTag);
                index2 = tmp.indexOf(secondTag);
                nbChar = Integer.parseInt(tmp.substring(index + 2, index2));
                if((i + 1) < messages.size())
                {
                    tmp = tmp.replace(firstTag + tmp.substring(index + 2, index2) + secondTag,
                                      "\r\n\0" + messages.elementAt(i+1) + "\0");
                    messages.remove(i);
                    messages.insertElementAt(tmp, i);
                    messages.remove(i+1);
                    break;
                }
            }
        }
    }

    /** Get the data (as binary) of this message */
    @Override
    public byte[] getBytesData(){
        return this.dataRaw.getBytes();
    }

    /** Returns a short description of the message. Used for logging as INFO level */
    /** This methods HAS TO be quick to execute for performance reason */
    @Override
	public String toShortString() throws Exception {
    	String ret = super.toShortString();
        if(messages != null)
            ret += new String(messages.lastElement().getBytes(), 0, Math.min(messages.lastElement().length(), 100), "UTF8");
        else
            ret += new String(dataRaw.getBytes(), 0, Math.min(dataRaw.length(), 100), "UTF8");
        ret = ret.replace("\0", "");
		return ret;
	}

    /** Get the XML representation of the message; for the genscript module. */
    @Override
    public String toXml() throws Exception {
        String xml = dataComplete.replace("\0", "");        
        return xml;
    }
    
    public boolean isSTARTTLS_request()
    {
    	for (String s : this.messages)
    	{
    		if (!s.startsWith("*") && s.toLowerCase().contains("STARTTLS".toLowerCase()))
    			return true;    	
    	}
    	return false;
    }
    
    public boolean isSTARTTLS_answer()
    {
    	for (String s : this.messages)
    	{
    		if (!s.startsWith("*") && s.toLowerCase().contains("OK Begin TLS negotiation now".toLowerCase()))
    			return true;    	
    	}
    	return false;
    }
}
