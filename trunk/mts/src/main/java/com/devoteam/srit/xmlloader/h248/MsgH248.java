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

package com.devoteam.srit.xmlloader.h248;

import java.util.HashSet;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.Trans;
import com.devoteam.srit.xmlloader.core.protocol.TransactionId;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.h248.data.Header;
import com.devoteam.srit.xmlloader.h248.data.ABNFParser;
import com.devoteam.srit.xmlloader.h248.data.AuthHeader;
import com.devoteam.srit.xmlloader.h248.data.Descriptor;
import com.devoteam.srit.xmlloader.h248.data.Dictionary;

/**
 *
 * @author fhenry
 */
public class MsgH248 extends Msg
{

	private String message;
	
	private AuthHeader authHeader;
	private Header header;
	
	private Descriptor descr = null;

	// List of descriptor name for the type of message
	private static HashSet<String> typeDescriptor = new HashSet<String>();

    private TransactionId responseTransactionId;
    private boolean isResponseTransactionIsSet;    
	
    /** Creates a new instance of MsgSip */
    public MsgH248() 
    {
        super();       
        
        typeDescriptor.add("A");typeDescriptor.add("MF");typeDescriptor.add("S");typeDescriptor.add("MV");
        typeDescriptor.add("AV");typeDescriptor.add("AC");typeDescriptor.add("N");typeDescriptor.add("SC");
    }
    
    /** Creates a new instance of MsgSip */
    public MsgH248(String text) throws Exception
    {
    	this();
        this.message = text;
        
        // remove the comment block : from ";" char to the end of line
        text = text.replaceAll(";[^\r\n]*", " ");
        
        // check the MEGACO token
		int index = ABNFParser.indexOfKWDictionary(text, ABNFParser.MEGACOP_TOKEN, 0);
		if (index >= 0)			
		{
	        // parse the authentication header
			String authHeader = text.substring(0, index).trim();
			if (authHeader.length() > 0)
			{
		        this.authHeader = new AuthHeader();
		        this.authHeader.parseHeader(authHeader + " ", 0);			
			}
	        
	        // parse the message header
	        this.header = new Header();
	        index = this.header.parseHeader(text, index);
	        index = index + 1;
		}
		else
		{
			index = 0;
		}
	        
		if (index >=0)
		{
	        // parse the message descriptors list
	        String descriptors = text.substring(index, text.length()).trim();
	        descriptors = "descr{" + descriptors + "}"; 
	        this.descr = new Descriptor();
	        this.descr.parseDescriptors(descriptors, 0, false);
		}
    }

    /** Get the protocol of this message */
    public String getProtocol()
    {
        return StackFactory.PROTOCOL_H248;
    }

    /** Get the command code of this message */
    public String getType() throws Exception
   {
    	// case Transaction or Reply
    	Parameter param = getParameter("descr.*.C.*.n");
    	String type = getType(param);
    	if (type != null)
    	{
    		return type;
    	}
    	// case TransactionResponseAck
    	param = getParameter("descr.K.n");
        if (param.length() > 0)
        {        	
    		return "TransactionResponseAck";
    	}
    	// case Segment
    	param = getParameter("descr.SM.n");
        if (param.length() > 0)
        {        	
    		return "Segment";
    	}
    	// case TransactionPending or other
    	if (!isRequest())
    	{
        	Trans trans = getTransaction(); 
        	if (trans == null)
        	{
        		return "null";
        	}
	    	Msg request = trans.getBeginMsg();
	    	if (request == null)
	    	{
	    		return "null";
	    	}
	    	return request.getType();
    	}
		return "null";
    }

    /** Get the command code of this message */
    private String getType(Parameter var) throws Exception
    {
    	for (int i = 0; i < var.length(); i++)
    	{
    		String type = (String) var.get(i);
        	// delete a prefix
    		int pos = type.indexOf(ABNFParser.MINUS);
    		if (pos >= 0)
    		{
    			type = type.substring(pos + 1);
    		}
    		String resolvedType = Dictionary.getInstance().getLongToken(type);
    		if (typeDescriptor.contains(type))
    		{
    			return resolvedType;
    		}
    	}
    	return null;	
    }

    /** Get the complete type (with dictionary conversion) of this message */
    public String getTypeComplete() throws Exception
    {
    	String type = getType();
		String resolvedType = Dictionary.getInstance().getShortToken(type);
		String complete = type;
		if (!resolvedType.equals(type))
		{
			complete = type + ":" + resolvedType;
		}
		return complete;
    }

    /** Get the result of this answer (null if request) */
    public String getResult() throws Exception
    {
    	// case Error
    	Parameter trans = getParameter("descr.ER.value");
        if (trans.length() > 0)
        {        	
    		return (String) trans.get(0);
    	}
    	// Reply/Error
    	trans = getParameter("descr.P.ER.value");
        if (trans.length() > 0)
        {        	
    		return (String) trans.get(0);
    	}
    	// case Reply/Context/Error
    	trans = getParameter("descr.P.C.ER.value");
        if (trans.length() > 0)
        {        	
    		return (String) trans.get(0);
    	}
    	// case Reply/Context/<command>/Error
    	trans = getParameter("descr.P.C.*.ER.value");
        if (trans.length() > 0)
        {        	
    		return (String) trans.get(0);
    	}
        // case transactionPending
    	trans = getParameter("descr.PN.value");
        if (trans.length() > 0)
        {        	
    		return "Pending";
    	}
       	return "Reply";
    }

    /** Get the complete result (with dictionary conversion) of this message */
    public String getResultComplete() throws Exception
    {
    	String result = getResult();
		String resolvedResult = Dictionary.getInstance().getShortToken(result);
		String complete = result;
		if (!resolvedResult.equals(result))
		{
			complete = result + ":" + resolvedResult;
		}
		return complete;
    }

    /** Return true if the message is a request else return false*/
    public boolean isRequest() throws Exception
    {	
    	// errorDescriptor
    	Parameter param = getParameter("descr.*.n");
        if (param.length() <= 0)
        {   
    		return true;
    	}
        String descr = (String) param.get(0);
    	// errorDescriptor
        if ("ER".equals(descr))
        {
        	return false;
        }
    	// ReplyDescriptor
        if ("P".equals(descr))
        {
        	return false;
        }
    	// PendingDescriptor
        if ("PN".equals(descr))
        {
        	return false;
        }
        return true;
	}

    /** Get the transaction Identifier of this message */
    @Override
    public TransactionId getTransactionId() throws Exception
    {
        if (!this.isTransactionIdSet)
        {
	    	// transactionRequest, transactionReply, transactionPending, segmentReply
	    	String transID = "";
	    	Parameter trans = getParameter("descr.*.value");
	        if (trans.length() <= 0)
	        {        	
	        	// transactionResponseAck
	        	trans = getParameter("descr.*.parameters");
	    	}
	        if (trans.length() > 0)
	        {
	        	transID = trans.get(0).toString();
	        	// transactionReply token
	        	// remove the [SLASH segmentNumber [SLASH SegmentationCompleteToken]] string (
	        	int pos = transID.indexOf(ABNFParser.SLASH);
	        	if (pos >= 0)
	        	{
	        		transID = transID.substring(0, pos).trim();
	        	}
	        	// transactionResponseAck token
	        	// remove the *(COMMA transactionAck) string (
	        	pos = transID.indexOf(ABNFParser.COMMA);
	        	if (pos >= 0)
	        	{
	        		transID = transID.substring(0, pos).trim();
	        	}
	        	// transactionAck token
	        	// remove / (TransactionID MINUS TransactionID) 
	        	pos = transID.indexOf(ABNFParser.MINUS);
	        	if (pos >= 0)
	        	{
	        		transID = transID.substring(0, pos).trim();
	        	}
	
	        }
            this.transactionId = new TransactionId(transID);
            this.isTransactionIdSet = true;
        }

        return this.transactionId;
    }

    /** Get a parameter from the message */
    public Parameter getParameter(String path) throws Exception
    {
        Parameter var = super.getParameter(path);
        if (var != null)
        {
            return var;
        }

    	var = new Parameter();
        path = path.trim();
        String[] params = Utils.splitPath(path);

        if (params[0].equalsIgnoreCase("header"))
        {
            if (params.length == 1)
            {
            	var.add(header.getLine());
            }
            else 
            {
            	header.addParameter(var, params[1]);
            }
        }
        else if (params[0].equalsIgnoreCase("authHeader"))
        {
            if (params.length == 1)
            {
            	var.add(authHeader.getLine());
            }
            else 
            {
            	authHeader.addParameter(var, params[1]);
            }
        }
        else if (params[0].equalsIgnoreCase("descr"))
        {
    	    if (params[1].equalsIgnoreCase("segmentNumber"))
    	    {
    	    	var.add(this.getSegmentNumber());
    	    }
    	    else if (params[1].equalsIgnoreCase("segmentComplete"))
    	    {
    	    	var.add(this.getSegmentComplete());
    	    }
    	    else
    	    {
	        	Descriptor des = this.descr;
	        	String [] p = Utils.splitNoRegex(params[0], "=");
	        	if ((des != null) && (des.getNameValue() != null) && (des.getNameValue().equalsParameter(p)))
	        	{
	        		if (!des.findAddParameters(var, params, 1))
	        		{
	        			Parameter.throwBadPathKeywordException(path);
	        		}	
	        	}
    	    }
        }
        else
        {
        	Parameter.throwBadPathKeywordException(path);
        }
        
        return var;
    }

    /** Get the segment number of this message */
    public String getSegmentNumber() throws Exception
    {
    	Parameter transID = getParameter("descr.*.value");
    	String trans = "";
    	if (transID.length() > 0)
    	{
    		trans = (String) transID.get(0);
    	} 
    	int pos = trans.indexOf(ABNFParser.SLASH);
    	if (pos >= 0)
    	{
        	int pos1 = trans.indexOf(ABNFParser.SLASH, pos + 1);
        	if (pos1 < 0)
        	{
        		pos1 = trans.length();
        	}
        	return  trans.substring(pos + 1, pos1);
    	}
    	return null;
    }

    /** Get the segmentation complete of this message */
    public String getSegmentComplete() throws Exception
    {
    	Parameter transID = getParameter("descr.*.value");
    	String trans = "";
    	if (transID.length() > 0)
    	{
    		trans = (String) transID.get(0);
    	}
    	int pos = trans.indexOf(ABNFParser.SLASH);
    	if (pos >= 0)
    	{
        	int pos1 = trans.indexOf(ABNFParser.SLASH, pos + 1);
        	if (pos1 >= 0)
        	{
        		String ret = trans.substring(pos1 + 1); 
        		String resolvedRet = Dictionary.getInstance().getLongToken(ret); 
            	return resolvedRet;
        	}
    	}
    	return null;    	
    }

    /** Get the transaction Identifier of this message */
    public TransactionId getResponseTransactionId() throws Exception
    {
        if(!this.isResponseTransactionIsSet)
        {
           	// transactionRequest, transactionReply, transactionPending, segmentReply
        	Parameter trans = getParameter("descr.*.n");
            if (trans.length() > 0)
            {
            	String name = (String) trans.get(0);            	
            	if ("K".equals(name) || "SM".equals(name) || ("P".equals(name)))
            	{
            		this.responseTransactionId = new TransactionId(this.getTransactionId() + "|" +  this.getSegmentNumber());
            	}
            }
            this.isResponseTransactionIsSet = true;
        }
        return this.responseTransactionId;
    }

    /**
     *  Tell whether the message shall be retransmitted or not 
     * (= true by default) 
     */
    public boolean shallBeRetransmitted() throws Exception
    {	
        String type = getType();
        if (isRequest())
        {
            if ("TransactionResponseAck".equals(type) )
            {
                return false;
            }
            if ("Segment".equals(type))
            {
                return false;
            }
        }
        else
        {
            String result = getResult();
            if ("Pending".equals(result))
            {
                return false;
            }
        }
        return true;
    }

    /**
     *  Tell whether the message shall be stop the automatic 
     *  retransmission mechanism or not 
     * (= true by default) 
     */
    public boolean shallStopRetransmit() throws Exception
    {
        String type = getType();
        if (isRequest())
        {
            if ("TransactionResponseAck".equals(type) )
            {
                return true;
            }
            if ("Segment".equals(type) )
            {
                return true;
            }
            return false;
        }
        else
        {
            return true;
        }
    }
 
    /**
     *  Tell whether the request begins the transaction or not 
     * (= true by default) 
     */
    public boolean beginTransaction() throws Exception
    {
		String status = getType();
		if ("TransactionResponseAck".equals(status))
		{
			return false;
		}
		if ("Segment".equals(status))
		{
			return false;
		}
        return true;
    }

    /**
     *  Tell whether the response ends the transaction or not 
     * (= true by default) 
     */
    public boolean endTransaction() throws Exception
    {
    	String method = getType();
    	if ("TransactionResponseAck".equals(method))
    	{
    		return true;
    	}
		String status = getResult();
    	if ("Reply".equals(status))
    	{
    		return true;
    	}
        return false;
    }

    /** Get the data (as binary) of this message */
    @Override
    public byte[] getBytesData()
    {
        return message.getBytes();
    }

    /** Returns a short description of the message. Used for logging as INFO level */
    /** This methods HAS TO be quick to execute for performance reason */
    @Override
    public String toShortString() throws Exception {
    	String ret = super.toShortString();
    	Parameter param = getParameter("descr.*.name");
    	if (param.length() > 0)
    	{
    		ret += (String) param.get(0) + "=";
    	}		
    	ret += getTransactionId();
		String segnum = getSegmentNumber();
		if (segnum != null)
		{
			ret += "/" + segnum;
		}
		String segcomp = getSegmentComplete();
		if (segcomp != null)
		{
			ret += "/" + segcomp;
		}
		param = getParameter("descr.*.Context.value");
    	if (param.length() > 0)
    	{
    		ret += " C=" + (String) param.get(0);
    	}
		String action = getType();
		ret += " " + action;
		param = getParameter("descr.*.*." + action + ".value");
		if (param.length() > 0)
    	{
			ret += "=" + param.get(0);
    	}
 	    return ret;
    }

    /** Get the XML representation of the message; for the genscript module. */
    @Override
    public String toXml() throws Exception {
    	String ret = null;
       	if (this.authHeader != null)
    	{
    		ret += this.authHeader.toString() + "\n";
    	}
       	if (this.header != null)
    	{
       		ret += this.header.toString() + "\n";
    	}
       	if (this.descr!= null)
    	{
       		ret += this.descr.toString();
    	}
       	return ret;
    }
}