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

package com.devoteam.srit.xmlloader.rtsp;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.operations.basic.operators.PluggableParameterOperatorSetFromURI;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.Trans;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.coding.text.FirstLine;
import com.devoteam.srit.xmlloader.core.coding.text.MsgParser;
import com.devoteam.srit.xmlloader.core.coding.text.TextMessage;

public class MsgRtsp extends Msg
{	
	private TextMessage message;

    // --- constructer --- //
    public MsgRtsp(String text, boolean completeContentLength, int addCRLFContent) throws Exception {
        message = new TextMessage(getProtocol(), completeContentLength, addCRLFContent);
    	message.parse(text);
        this.message.setGenericfirstline(new FirstLine(this.message.getFirstLineString(),getProtocol()));
    	if (((FirstLine)(this.message.getGenericfirstline())).isRequest())
    	{
    		extractRemoteData();
    	}
	}
   
 
    // --- heritage methods --- //
    public String getProtocol(){
        return StackFactory.PROTOCOL_RTSP;
    }

    public String getType() {
        if(null == type)
        {
            if (isRequest()){
                FirstLine firstline = ((FirstLine)(this.message.getGenericfirstline()));
                type = firstline.getMethod();
            }
            else {               
                try {
                    //get transaction with transactionId associated to the request
                    Msg msgTemp = null;
                    Trans tr = StackFactory.getStack(StackFactory.PROTOCOL_RTSP).getOutTransaction(getTransactionId());
                    if(tr != null)
                        msgTemp = tr.getBeginMsg();
                    else
                        msgTemp = StackFactory.getStack(StackFactory.PROTOCOL_RTSP).getInTransaction(getTransactionId()).getBeginMsg();    
                    type = msgTemp.getType();
                }
                catch(Exception e)
                {}
            }
        }
        return type;
	}

    public String getResult(){
        if (!isRequest()){        	
        	FirstLine firstline = ((FirstLine)(this.message.getGenericfirstline()));
        	return firstline.getStatusCode();
        }
        return null;
    }
    public boolean isRequest() {
    	return ((FirstLine)(this.message.getGenericfirstline())).isRequest();
	}
	    		
	// --- get Parameters --- //
	public synchronized Parameter getParameter(String path) throws Exception 
	{
		Parameter var = super.getParameter(path);	
		if (var != null) {
			return var;
		}

		var = new Parameter();
        path = path.trim();
        String[] params = Utils.splitPath(path);
		
        if (params.length >= 1 && params[0].equalsIgnoreCase("firstline"))
        {
    		FirstLine firstline = ((FirstLine)(this.message.getGenericfirstline()));
            //---------------------------------------------------------------------- firstline -
            if (params.length == 1)
            {
                var.add(firstline.getLine());
            }
            //---------------------------------------------------------------------- firstline:Version -
        	else if (params.length == 2 && params[1].equalsIgnoreCase("Version"))
            {
                var.add(firstline.getVersion());
            }
            //---------------------------------------------------------------------- firstline:Method -
        	else if (params.length == 2 && params[1].equalsIgnoreCase("Method"))
            {
            	var.add(firstline.getMethod());
            }
            //---------------------------------------------------------------------- firstline:URI -
        	else if (params.length >= 2 && params[1].equalsIgnoreCase("URI"))
            {
                var.add(firstline.getUri());
            }
            //---------------------------------------------------------------------- firstline:StatusCode -
        	else if (params.length == 2 && params[1].equalsIgnoreCase("StatusCode"))
            {
				var.add(firstline.getStatusCode());
            }
            //----------------------------------------------------------------------- firstline:ReasonPhrase -
        	else if (params.length == 2 && params[1].equalsIgnoreCase("ReasonPhrase"))
			{
				var.add(firstline.getReasonPhrase());
			}
            else
            {
            	Parameter.throwBadPathKeywordException(path);
            }
        }
		// ----------------------------------------------------------------------header -
        else if (params.length == 1 && params[0].equalsIgnoreCase("header"))
        {
			var.add(message.getHeaders());
		}
		// ----------------------------------------------------------------------header:XXXXX -
        else if (params.length == 2 && params[0].equalsIgnoreCase("header")) 
        {
        	var.addHeader(message.getHeader(params[1]));
            // var.add(message.getHeader(params[1]).getHeader(0).trim());
		}
		// ----------------------------------------------------------------------header:XXXXX:YYYYY -
        else if (params.length == 3 && params[0].equalsIgnoreCase("header")) 
        {//ex. header:Setup:level
			String str = message.getHeader(params[1]).getHeader(0);
            MsgParser parser = new MsgParser();
    		if (parser.getHeader(params[2]) != null)
    		{
    			var.add(parser.getHeader(params[2]).getHeader(0));
    		}
		}
		//---------------------------------------------------------------------- content(X): -
        else if (params[0].startsWith("content"))
		{
            message.addContentParameter(var, params, path); //like in SIP
		}
        else
        {
        	Parameter.throwBadPathKeywordException(path);
        }

		return var;
	}

	// --- get/set method --- //
	public TextMessage getMessage(){
		return this.message;
	}

    private void extractRemoteData() throws Exception
    {
		String strURI = ((FirstLine)(this.message.getGenericfirstline())).getUri();
		String remoteHost = PluggableParameterOperatorSetFromURI.setFromUri(strURI, "host");
		setRemoteHost(remoteHost);
		String rPort = PluggableParameterOperatorSetFromURI.setFromUri(strURI, "port");
		int remotePort = Integer.parseInt(rPort);
		setRemotePort(remotePort);
		String scheme = PluggableParameterOperatorSetFromURI.setFromUri(strURI, "scheme");
		String transport;
		if ("rtspu".equalsIgnoreCase(scheme))
		{
			transport = StackFactory.PROTOCOL_UDP;
		}
		else if ("rtsp".equalsIgnoreCase(scheme))
		{
			transport = StackFactory.PROTOCOL_TCP;
		}
		else
		{
            throw new ExecutionException("Could not determine the transport from the message using the request URI : " + this);
		}
		setTransport(transport);
    }

    /**
     *  Tell whether the message shall be retransmitted or not
     * (= true by default)
     */
    public boolean shallBeRetransmitted() throws Exception
    {
        if(getTransport().equalsIgnoreCase(StackFactory.PROTOCOL_TCP))
            return false;
        else if(getTransport().equalsIgnoreCase(StackFactory.PROTOCOL_UDP))
            return true;
        else
            return true;
    }
    
    /** Get the data (as binary) of this message */
    @Override
    public byte[] getBytesData(){
        return this.message.getMessage().getBytes();
    }

    /** Returns a short description of the message. Used for logging as INFO level */
    /** This methods HAS TO be quick to execute for performance reason */
    @Override
	public String toShortString() throws Exception {
    	String ret = super.toShortString();
        ret += ((FirstLine)(this.message.getGenericfirstline())).getLine();
        return ret;
	}

    /** Get the XML representation of the message; for the genscript module. */
    @Override
    public String toXml() throws Exception {
    	return message.getMessage().toString();
    }

}