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

package com.devoteam.srit.xmlloader.gtp;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.coding.binary.Dictionary;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.Trans;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.gtp.data.MessageGTP;

/**
 *
 * @author Fabien Henry
 */
public class MsgGtp extends Msg
{
    // based on GTP encryption 
    private MessageGTP message;

    /** Creates a new instance */
    public MsgGtp(Stack stack) throws Exception
    {
        super(stack);
    }
    
    /** Creates a new instance */
    public MsgGtp(Stack stack, MessageGTP message) throws Exception
    {
    	this(stack);
        this.message = message;
    }

    /** 
     * Get the protocol acronym of the message 
     */
    @Override
    public String getProtocol()
    {
    	return StackFactory.PROTOCOL_GTP + StackFactory.SEP_SUB_INFORMATION + this.message.getSubProtocol();
    }

    /** 
     * Return true if the message is a request else return false
     */
    @Override
    public boolean isRequest() throws Exception
    {
        return this.message.isRequest();
    }

    /** 
     * Get the type of the message
     * Used for message filtering with "type" attribute and for statistic counters 
     */
    @Override
    public String getType() throws Exception
    {
    	if (isRequest())
    	{
    		return this.message.getType();
    	}
    	else
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
    }

    /** 
     * Get the result of the message (null if request)
     * Used for message filtering with "result" attribute and for statistic counters 
     */
	@Override
    public String getResult() throws Exception
    {
    	Parameter causeParam = null;
    	if (this.message.getSyntax().equals("V2"))
    	{
    		causeParam = this.getParameter("element.Cause:2.field.Cause value");
    	}
    	if (this.message.getSyntax().equals("V1"))
    	{
    		causeParam = this.getParameter("element.Cause:2.field.Cause value");
    	}
    	if (this.message.getSyntax().equals("Prime"))
    	{
    		causeParam = this.getParameter("element.Cause:1.field.cause");
    	}
    	
		if (causeParam.length() > 0 && causeParam.length() > 0)
        {
            return causeParam.get(0).toString();
        }
		// case of "Version Not Supported" message type
		else
		{
			return message.getType();
		}
    }
        
    
    //-------------------------------------------------
    // methods for the encoding / decoding of the message
    //-------------------------------------------------

    /** 
     * encode the message to binary data 
     */
    @Override    
    public byte[] encode()  throws Exception
    {
    	return message.encodeToArray().getBytes();
    }
    
    /** 
     * decode the message from binary data 
     */
    public void decode(byte[] data) throws Exception
    {
    	byte[] newData = data.clone();
        
    	MessageGTP message = new MessageGTP();
        message.decodeFromBytes(newData);
        
        this.message = message;
    }

    
    //---------------------------------------------------------------------
    // methods for the XML display / parsing of the message
    //---------------------------------------------------------------------

    /** 
     * Convert the message to XML document 
     */
    @Override
    public String toXml() throws Exception 
    {
        return message.toXml();
    }

    /** 
     * Parse the message from XML element 
     */
    @Override
    public void parseFromXml(ParseFromXmlContext context, Element root, Runner runner) throws Exception
    {
    	super.parseFromXml(context,root,runner);
    	MessageGTP messageGTP = new MessageGTP(root);
    	this.message = messageGTP;
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
    	String replacePath = Utils.replaceNoRegex(path, ":", "//"); 
        Parameter var = super.getParameter(replacePath);
        if (null != var)
        {
            return var;
        }
        var = new Parameter();
        path = path.trim();
        String[] params = Utils.splitNoRegex(path, ".");
        
        this.message.getParameter(var, params, path);
        
        return var;
    }
    
}
