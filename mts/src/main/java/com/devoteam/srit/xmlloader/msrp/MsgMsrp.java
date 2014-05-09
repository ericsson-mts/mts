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

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.operations.basic.operators.PluggableParameterOperatorSetFromURI;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.Trans;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.coding.text.MsgParser;
import com.devoteam.srit.xmlloader.msrp.data.MSRPFirstLine;
import com.devoteam.srit.xmlloader.msrp.data.MSRPTextMessage;

public class MsgMsrp extends Msg
{	
	private MSRPTextMessage message;
    private String  msgRemoteHost = null;
    private int     msgRemotePort = -1;
    
    private String type = null;

    // --- constructer --- //
    public MsgMsrp(String text) throws Exception {
        message = new MSRPTextMessage(getProtocol());
    	message.parse(text);
	}
 
    // --- heritage methods --- //
    public String getProtocol(){
        return StackFactory.PROTOCOL_MSRP;
    }

    public String getType() {
        if(null == type)
        {
            if (isRequest()){
                MSRPFirstLine firstline = this.message.getFirstline();
                type = firstline.getMethod();
            }
            else {               
                try {
                    //get transaction with transactionId associated to the request
                    Msg msgTemp = null;
                    Trans tr = StackFactory.getStack(StackFactory.PROTOCOL_MSRP).getOutTransaction(getTransactionId());
                    if(tr != null)
                        msgTemp = tr.getBeginMsg();
                    else
                        msgTemp = StackFactory.getStack(StackFactory.PROTOCOL_MSRP).getInTransaction(getTransactionId()).getBeginMsg();    
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
        	MSRPFirstLine firstline = this.message.getFirstline();
        	return firstline.getStatusCode();
        }
        return null;
    }
    public boolean isRequest() {
    	return this.message.getFirstline().isRequest();
	}
	    		
	// --- get Parameters --- //   //TODO
    @Override
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
        	MSRPFirstLine firstline = this.message.getFirstline();
            //---------------------------------------------------------------------- firstline -
            if (params.length == 1)
            {
                var.add(firstline.getLine());
            }
            //---------------------------------------------------------------------- firstline:Method -
        	else if (params.length == 2 && params[1].equalsIgnoreCase("Method"))
            {
            	var.add(firstline.getMethod());
            }
            //---------------------------------------------------------------------- firstline:TransactionId -
        	else if (params.length >= 2 && params[1].equalsIgnoreCase("Transaction-ID"))
            {
                var.add(firstline.getTransID());
            }
            //---------------------------------------------------------------------- firstline:StatusCode -
        	else if (params.length == 2 && params[1].equalsIgnoreCase("StatusCode"))
            {
				var.add(firstline.getStatusCode());
            }
            //----------------------------------------------------------------------- firstline:Comment -
        	else if (params.length == 2 && params[1].equalsIgnoreCase("Comment"))
			{
				var.add(firstline.getComment());
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
        {//ex. header:Status:namespace
        	String str = message.getHeader(params[1]).getHeader(0);
        	String[] values = null;
        	
        	if(params[1].equalsIgnoreCase("status")){
        		values = Utils.splitNoRegex(str, " ");
        		if(params[2].equalsIgnoreCase("Namespace") && 2 < values.length){
        			var.add(values[0]);
        		}
        		else if(params[2].equalsIgnoreCase("StatusCode") && 2 < values.length){
        			var.add(values[1]);
        		}
        		else if(params[2].equalsIgnoreCase("ReasonPhrase") && 2 < values.length){
        			var.add(values[2]);
        		}
        	}
        	else if(params[1].equalsIgnoreCase("to-path") || params[1].equalsIgnoreCase("from-path")){
        		var.add(PluggableParameterOperatorSetFromURI.setFromUri(str, params[2]));
        	}
        	else if(params[1].equalsIgnoreCase("byte-range"))
        	{
        		str = str.trim();
        		if(!str.contains("/") && !str.contains("-")){
        			return var;
        		}
        		values = Utils.splitNoRegex(str, "/");
        		String[] rangeTmp = Utils.splitNoRegex(values[0], "-"); 
        		
        		if(1 > values.length || 1 > rangeTmp.length){
        			return var;
        		}
        		
        		if(params[2].equalsIgnoreCase("range-start") && Utils.isInteger(rangeTmp[0]))
        		{
        			var.add(rangeTmp[0]);
        		}
        		else if(params[2].equalsIgnoreCase("range-end") && (Utils.isInteger(rangeTmp[1]) || rangeTmp[1].equals("*")))
        		{
        			var.add(rangeTmp[1]);
        		}
        		else if(params[2].equalsIgnoreCase("total") && (Utils.isInteger(values[1]) || values[1].equals("*")))
        		{
        			var.add(values[1]);
        		}       		
        	}
        	else{    			
                MsgParser parser = new MsgParser();
        		if (parser.getHeader(params[2]) != null)
        		{
        			var.add(parser.getHeader(params[2]).getHeader(0));
        		}
        	}
		}
		//---------------------------------------------------------------------- content(X): -
        else if (params[0].startsWith("content"))
		{
            message.addContentParameter(var, params, path); //like in SIP
		}
        //---------------------------------------------------------------------- ContinuationFlag: -
        else if (params[0].equalsIgnoreCase("continuationFlag")){
			var.add(message.getContinuationFlag());
		}
        else
        {
        	Parameter.throwBadPathKeywordException(path);
        }

		return var;
	}

	// --- get/set method --- //
	public MSRPTextMessage getMessage(){
		return this.message;
	}

    public String getMsgRemoteHost() {
        return msgRemoteHost;
    }

    public int getMsgRemotePort() {
        return msgRemotePort;
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
        ret += this.message.getFirstline().getLine();
        return ret;
	}

    /** Get the XML representation of the message; for the genscript module. */
    @Override
    public String toXml() throws Exception {
        return message.getMessage().toString();
    }

}