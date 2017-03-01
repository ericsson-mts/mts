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
*//*
 * MsgHttp.java
 *
 * Created on 6 avril 2007, 16:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.sip.light;

import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPResponse;

import java.util.HashMap;
import java.util.HashSet;

import javax.sip.SipFactory;
import javax.sip.header.ContentTypeHeader;
import javax.sip.message.MessageFactory;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.operations.basic.operators.PluggableParameterOperatorSetFromAddress;
import com.devoteam.srit.xmlloader.core.operations.basic.operators.PluggableParameterOperatorSetFromURI;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.coding.text.FirstLine;
import com.devoteam.srit.xmlloader.core.coding.text.MsgParser;
import com.devoteam.srit.xmlloader.core.coding.text.Header;
import com.devoteam.srit.xmlloader.core.coding.text.TextMessage;
import com.devoteam.srit.xmlloader.rtsp.StackRtsp;
import com.devoteam.srit.xmlloader.sip.MsgSipCommon;
import com.devoteam.srit.xmlloader.sip.StackSipCommon;

/**
 *
 * @author fhenry
 */
public class MsgSip extends MsgSipCommon
{    
	private TextMessage message = null;
    	
	private static HashSet<String> multiHeader = new HashSet<String>();	
	private static HashMap<String, String> compressedHeader = new HashMap<String, String>();
	
	static 
	{
		// all headers except : Authorization,Call-ID,Content-Encoding,Content-Length,Content-Type,
		// CSeq,Date,expires,From,Max-Forwards,MIME-Version,Min-Expires,Organization,Priority,
		// Proxy-Authenticate,Proxy-Authorization,Reply-To,Retry-After,Server,Subject,Timestamp,To,
		// User-Agent,WWW-Authenticate, Authentication-Info <generic>
		multiHeader.add("accept");multiHeader.add("accept-encoding");multiHeader.add("accept-language");
		multiHeader.add("alert-Info");multiHeader.add("allow");multiHeader.add("call-info");
		multiHeader.add("contact");multiHeader.add("m");multiHeader.add("content-encoding");
		multiHeader.add("e");multiHeader.add("content-language");multiHeader.add("error-info");
		multiHeader.add("in-reply-to");multiHeader.add("proxy-require");multiHeader.add("record-route");
		multiHeader.add("require");multiHeader.add("route");multiHeader.add("supported");multiHeader.add("k");
		multiHeader.add("Unsupported");multiHeader.add("via");multiHeader.add("v");multiHeader.add("warning");
		
		compressedHeader.put("a", "accept-contact");		// rfc 3841
		compressedHeader.put("c", "content-type");			// rfc 3261
		compressedHeader.put("e", "content-encoding");		// rfc 3261
		compressedHeader.put("f", "from");					// rfc 3261
		compressedHeader.put("i", "call-id");				// rfc 3261
		compressedHeader.put("j", "reject-contact");		// rfc 3841
		compressedHeader.put("k", "supported");				// rfc 3261
		compressedHeader.put("l", "content-length");		// rfc 3261
		compressedHeader.put("m", "contact");				// rfc 3261
		compressedHeader.put("r", "refer-to");				// rfc 3515
		compressedHeader.put("s", "subject");				// rfc 3261
		compressedHeader.put("t", "To");					// rfc 3261
		compressedHeader.put("v", "via");					// rfc 3261
	}
    
    /** Creates a new instance */
    public MsgSip(Stack stack) 
    {
        super(stack);
    }

    /** 
     * Return true if the message is a request else return false
     */
    @Override
    public boolean isRequest() 
    {
		return ((FirstLine)(this.message.getGenericfirstline())).isRequest();
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
         return message.getMessage().getBytes();
    }

    /** 
     * decode the message from binary data 
     */
    @Override 
    public void decode(byte[] data) throws Exception
    {
    	StackSip stackSip = (StackSip) stack;
        this.message = new TextMessage(getProtocol(), false, 0, stackSip.contentBinaryTypes);
        this.message.setCompressedHeader(compressedHeader);
        this.message.setMultiHeader(multiHeader);
        String text = new String(data);
        this.message.parse(text);
        this.message.setGenericfirstline(new FirstLine(this.message.getFirstLineString(),getProtocol()));
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
        ret += ((FirstLine)(this.message.getGenericfirstline())).getLine();
        ret += "\n";
        String transId = getTransactionId().toString();
        ret += "<MESSAGE transactionId=\"" + transId + "\""; 
        String dialogId = getDialogId();
        ret+= " dialogId=\"" + dialogId + "\"";
        ret+= "/>";
        return ret;
    }
    
    /** 
     * Convert the message to XML document 
     */
    @Override
    public String toXml() throws Exception 
    {        
        return message.getMessage().toString();
    }
    
    /** 
     * Parse the message from XML element 
     */
    @Override
    public void parseFromXml(Boolean request, Element root, Runner runner) throws Exception
    {
    	super.parseFromXml(request,root,runner);

    	String text = root.getText();
        StackSip stackSip = (StackSip) stack;
        this.message = new TextMessage(getProtocol(), true, stackSip.addCRLFContent, stackSip.contentBinaryTypes);
        this.message.setCompressedHeader(compressedHeader);
        this.message.setMultiHeader(multiHeader);
        this.message.parse(text);
        this.message.setGenericfirstline(new FirstLine(this.message.getFirstLineString(),getProtocol()));
    }

    
    //------------------------------------------------------
    // method for the "setFromMessage" <parameter> operation
    //------------------------------------------------------

    /** 
     * Get a parameter from the message
     */
    public Parameter getParameter(String path) throws Exception
    {
        Parameter var = super.getParameter(path);
        if (var != null)
        {
            return var;
        }
    	
    	var = new Parameter();
    	// bidouille pour importSIPP pour générer des path keyword de la forme header.Xxxxx:
    	path = path.trim();
    	if (path.endsWith(":"))
    	{
    		path = path.substring(0, path.length() - 1);
    	}
    	
        String[] params = Utils.splitPath(path);
        
        if (params.length >= 1 && params[0].equalsIgnoreCase("firstline"))
        {
    		FirstLine firstline = ((FirstLine)(this.message.getGenericfirstline()));
    		if (params.length == 1)
            {
                //---------------------------------------------------------------------- firstline -
                var.add(firstline.getLine());
                return var;
            }
            //---------------------------------------------------------------------- firstline:Version -
        	if (params.length == 2 && params[1].equalsIgnoreCase("Version"))
            {
                var.add(firstline.getVersion());
                return var;
            }
            //---------------------------------------------------------------------- firstline:Method -
        	if (params.length == 2 && params[1].equalsIgnoreCase("Method"))
            {
            	var.add(firstline.getMethod());
                return var;
            }
            //---------------------------------------------------------------------- firstline:URI -
        	if (params.length >= 2 && params[1].equalsIgnoreCase("URI"))
            {
            	Header header = new Header(null);
            	header.addHeader(firstline.getUri());
            	addSIPURIHeader(var, params, header);
                return var;
            }
            //---------------------------------------------------------------------- firstline:StatusCode -
        	if (params.length == 2 && params[1].equalsIgnoreCase("StatusCode"))            
            {
				var.add(firstline.getStatusCode());
	            return var;
            }
            //----------------------------------------------------------------------- firstline:ReasonPhrase -
        	if (params.length == 2 && params[1].equalsIgnoreCase("ReasonPhrase")) 
			{
				var.add(firstline.getReasonPhrase());
	            return var;
			}
            else
            {
            	Parameter.throwBadPathKeywordException(path);
            }
        }
        else if (params.length >= 1 && params[0].equalsIgnoreCase("header"))
        {
            //---------------------------------------------------------------------- header -
            if (params.length == 1)
            {
    			message.addHeaderIntoParameter(var);
                return var;
            }
            //---------------------------------------------------------------------- header:Others -
            else if (params.length == 2 && params[1].equalsIgnoreCase("Others"))
            {
                message.addOtherIntoParameter(var);
                return var;
            }
            //---------------------------------------------------------------------- header:Authorization -
            //---------------------------------------------------------------------- header:WWW-Authenticate -
            //---------------------------------------------------------------------- header:Proxy-Authorization -
            //---------------------------------------------------------------------- header:Proxy-Authenticate -
            else if (params.length >= 2 && 
               (params[1].equalsIgnoreCase("Authorization") ||
            	params[1].equalsIgnoreCase("WWW-Authenticate") ||
            	params[1].equalsIgnoreCase("Proxy-Authorization") ||
            	params[1].equalsIgnoreCase("Proxy-Authenticate") ||
            	params[1].equalsIgnoreCase("Authentication-Info")))
            {
            	if (addSIPHeaderAuthentication(var, params, message.getHeader(params[1])))
            	{
            		return var;
            	}
                // DEPRECATED value
                //---------------------------------------------------------------------- header:Yyyyy:URI -        
                if (params.length >= 3 && params[2].equalsIgnoreCase("URI"))
                {
                	String newPath = "header:" + params[1] + ":Attribute:uri";
                	var = getParameter(newPath);
                	GlobalLogger.instance().logDeprecatedMessage("setFromMessage value=" + path, "setFromMessage value=" + newPath);
        	        return var;
                }        
                //---------------------------------------------------------------------- header:Yyyyy:Parameter:Zzzzz -        
                if (params.length >= 4 && params[2].equalsIgnoreCase("Parameter"))
                {
                	String newPath = "header:" + params[1] + ":Attribute:" + params[3];
                	var = getParameter(newPath);
                	GlobalLogger.instance().logDeprecatedMessage("setFromMessage value=" + path, "setFromMessage value=" + newPath);
        	        return var;
                }
                else
                {
                	Parameter.throwBadPathKeywordException(path);
                }
            }
            //---------------------------------------------------------------------- header:CSeq:* -
            else if (params.length >= 2 && params[1].toLowerCase().startsWith("cseq"))
            {
            	Header header = message.getHeader("CSeq");
                if (addSIPHeaderCSeq(var, params, header))
                {
                	return var;
                }
                // DEPRECATED value
        	    if (params.length == 2 && params[1].equalsIgnoreCase("CSeqNumber"))
        	    {
                	String newPath = "header:CSeq:Number";
                	var = getParameter(newPath);
                	GlobalLogger.instance().logDeprecatedMessage("setFromMessage value=" + path, "setFromMessage value=" + newPath);
        	        return var;
        	    }
                // DEPRECATED value
        	    if (params.length == 2 && params[1].equalsIgnoreCase("CSeqMethod"))
        	    {
                	String newPath = "header:CSeq:Method";
                	var = getParameter(newPath);
                	GlobalLogger.instance().logDeprecatedMessage("setFromMessage value=" + path, "setFromMessage value=" + newPath);
        	        return var;
        	    }
                else
                {
                	Parameter.throwBadPathKeywordException(path);
                }
            }
            //---------------------------------------------------------------------- header:RSeq -
            else if (params.length >= 2 && params[1].equalsIgnoreCase("RSeq"))
            {
            	Header header = message.getHeader("rseq");
                var.addHeaderInteger(header);
           		return var;
          	}
            //---------------------------------------------------------------------- header:RAck:* -
            else if (params.length >= 2 && params[1].equalsIgnoreCase("RAck"))
            {
            	Header header = message.getHeader("rack");
                if (addSIPHeaderRAck(var, params, header))
                {
                	return var;
                }
            }
            //---------------------------------------------------------------------- header:TopMostVia:* -
            else if (params.length >= 2 && params[1].equalsIgnoreCase("TopmostVia"))
            {
            	String topmostVia = message.getHeader("Via").getHeader(0);
            	Header header = new Header("Via");
            	header.addHeader(topmostVia); 
                if (addSIPHeaderVia(var, params, header))
                {
                	return var;
                }
            }
            //---------------------------------------------------------------------- header:Via:* -
            else if (params.length >= 2 && params[1].equalsIgnoreCase("Via"))
            {
            	Header header = message.getHeader("Via");
                if (addSIPHeaderVia(var, params, header))
                {
                	return var;
                }
            }
            //---------------------------------------------------------------------- header:From -
            else if (params.length == 2 && params[1].equalsIgnoreCase("DialogId"))
            {
            	var.add(getDialogId().toString());
                return var;
            }            
            //---------------------------------------------------------------------- header:Xxxxx -
            else if (addSIPHeaderGenericXXX(var, params, message.getHeader(params[1])))
        	{
        		return var;
        	}
        }
        else if (params[0].toLowerCase().startsWith("content"))
        {
        	message.addContentParameter(var, params, path);
            return var;
        }
        // DEPRECATED value
        else if ("header:Contact:QValue".equalsIgnoreCase(path))
        {
        	String newPath = "header:Contact:Parameter:q";
        	var = getParameter(newPath);
        	GlobalLogger.instance().logDeprecatedMessage("setFromMessage value=" + path, "setFromMessage value=" + newPath);
            return var;
        }
        else if ("header:To:Tag".equalsIgnoreCase(path))
        {
        	String newPath = "header:To:Parameter:tag";
        	var = getParameter(newPath);
        	GlobalLogger.instance().logDeprecatedMessage("setFromMessage value=" + path, "setFromMessage value=" + newPath);
            return var;
        }
        else if ("header:From:Tag".equalsIgnoreCase(path))
        {
        	String newPath = "header:From:Parameter:tag";
        	var = getParameter(newPath);
        	GlobalLogger.instance().logDeprecatedMessage("setFromMessage value=" + path, "setFromMessage value=" + newPath);
            return var;
        }
        else if ("header:Call-Id".equalsIgnoreCase(path))
        {
        	String newPath = "header:Call-ID";
        	var = getParameter(newPath);
        	GlobalLogger.instance().logDeprecatedMessage("setFromMessage value=" + path, "setFromMessage value=" + newPath);
            return var;
        }
        else
        {
        	Parameter.throwBadPathKeywordException(path);
        }

        return var;
    }
          
    /** Get the message Address of this message */
    private void addSIPAddressHeader(Parameter var, String[] params, Header header) throws Exception
    {
        if (params.length == 3)
        {
            var.addHeader(header);
        }
        else
        {
            String operandeAddress = "";
            for (int i = 3; i < params.length; i++)
            {
                operandeAddress += params[i] + ".";
            }
            operandeAddress = operandeAddress.substring(0, operandeAddress.length() - 1);
            for (int i = 0; i < header.getSize(); i++)            
            {
            	var.add(PluggableParameterOperatorSetFromAddress.setFromAddress(header.getHeader(i), operandeAddress));
            }
        }
    }

    /** Get the message Uri of this message */
    private void addSIPURIHeader(Parameter var, String[] params, Header header) throws Exception
    {
        if (params.length == 2)
        {
            var.addHeader(header);
        }
        else
        {
            String operandeAddress = "";
            for (int i = 2; i < params.length; i++)
            {
                operandeAddress += params[i] + ".";
            }
            operandeAddress = operandeAddress.substring(0, operandeAddress.length() - 1);
            for (int i = 0; i < header.getSize(); i++)            
            {
                var.add(PluggableParameterOperatorSetFromURI.setFromUri(header.getHeader(i), operandeAddress));
            }
            
        }
    }

    /** Get the elements of CSeq header of this message */
    private boolean addSIPHeaderCSeq(Parameter var, String[] params, Header header) throws Exception
    {
	    //---------------------------------------------------------------------- header:CSeq -
	    if (params.length == 2 && params[1].equalsIgnoreCase("CSeq"))
	    {
            var.addHeader(header);
	        return true;
	    }	    
	    //---------------------------------------------------------------------- header:CSeq:Namevalue -
        if (params.length == 3 && params[2].equalsIgnoreCase("Namevalue"))
	    {
            var.addHeaderNamevalue(header);
	        return true;
	    }	    	    
	    //---------------------------------------------------------------------- header:CSeq:Number -
	    if (params.length == 3 && params[2].equalsIgnoreCase("Number"))
	    {
			MsgParser parser = new MsgParser(); 
			parser.splitHeader(header, " ");
            var.addHeaderInteger(parser.getHeader(0));
	        return true;
	    }
	    //---------------------------------------------------------------------- header:CSeq:Method -
	    if (params.length == 3 && params[2].equalsIgnoreCase("Method"))
	    {
			MsgParser parser = new MsgParser(); 
			parser.splitHeader(header, " ");
            var.addHeader(parser.getHeader(1));
	        return true;
	    }
	    return false;
    }
    
    /** Get the elements of RAck header of this message */
    private boolean addSIPHeaderRAck(Parameter var, String[] params, Header header) throws Exception
    {
	    //---------------------------------------------------------------------- header:RAck -
	    if (params.length == 2)
	    {
            var.addHeader(header);
	        return true;
	    }
	    //---------------------------------------------------------------------- header:RAck:Namevalue -
        if (params[2].equalsIgnoreCase("Namevalue"))
	    {
            var.addHeaderNamevalue(header);
	        return true;
	    }	    
	    //---------------------------------------------------------------------- header:RAck:Number -
	    if (params.length == 3 && params[2].equalsIgnoreCase("Number"))
	    {
			MsgParser parser = new MsgParser(); 
			parser.splitHeader(header, " ");
            var.addHeaderInteger(parser.getHeader(0));
	        return true;
	    }
	    //---------------------------------------------------------------------- header:RAck:CSeq -
	    if (params.length == 3 && params[2].equalsIgnoreCase("CSeqNumber"))
	    {
			MsgParser parser = new MsgParser(); 
			parser.splitHeader(header, " ");
            var.addHeaderInteger(parser.getHeader(1));
	        return true;
	    }
	    //---------------------------------------------------------------------- header:RAck:Method -
	    if (params.length == 3 && params[2].equalsIgnoreCase("Method"))
	    {
			MsgParser parser = new MsgParser(); 
			parser.splitHeader(header, " ");
            var.addHeader(parser.getHeader(2));
	        return true;
	    }
	    return false;
    }

    /** Get the elements of Authentication headers of this message */
    private boolean addSIPHeaderAuthentication(Parameter var, String[] params, Header header) throws Exception
    {
        //---------------------------------------------------------------------- header:Yyyyy -
        if (params.length == 2)
        {
            var.addHeader(header);
            return true;
        }
        //---------------------------------------------------------------------- header:Yyyyy:Namevalue -
        if (params[2].equalsIgnoreCase("Namevalue"))
        {
            var.addHeaderNamevalue(header);
            return true;
        }                
        //---------------------------------------------------------------------- header:Yyyyy:Scheme -
        if (params.length == 3 && params[2].equalsIgnoreCase("Scheme"))
        {
        	String value = header.getHeader(0);
        	// case Authentication-Info pas de Scheme Digest devant (authentication HTTP)
        	if ("Authentication-Info".equalsIgnoreCase(params[1]))
        	{
        		value = " ," + value;
        	}
    		MsgParser parser = new MsgParser(); 
    		parser.parse(value, " ,", '=', "\"\"");
   			var.addHeader(parser.getHeader(null));
            return true;
        }
        //---------------------------------------------------------------------- header:Yyyyy:Attribute:Zzzzz -
        if (params.length == 4 && params[2].equalsIgnoreCase("Attribute"))
        {
        	String value = header.getHeader(0);
        	// case Authentication-Info pas de Scheme Digest devant (authentication HTTP)
        	if ("Authentication-Info".equalsIgnoreCase(params[1]))
        	{
        		value = " ," + value;
        	}
        	MsgParser parser = new MsgParser(); 
    		parser.parse(value, " ,", '=', "\"\"");
   			var.addHeader(parser.getHeader(params[3]));
            return true;
        }
        return false;
    }
        
    /** Get the elements of Via header of this message */
    private boolean addSIPHeaderVia(Parameter var, String[] params, Header header) throws Exception
    {
       	MsgParser parser = new MsgParser(); 
		parser.parseHeader(header, ";", '=', "<>", "\"\"");    
    	Header via = parser.getHeader(null);

        //---------------------------------------------------------------------- header:TopMostVia or header:Via-
        if (params.length == 2)
        {
            var.addHeader(header);
        	return true;
        }
        //---------------------------------------------------------------------- header:TopMostVia:Namevalue or header:Via:Namevalue-
        if (params[2].equalsIgnoreCase("Namevalue"))
        {
            var.addHeaderNamevalue(header);
        	return true;
        }        
        //---------------------------------------------------------------------- header:TopMostVia:Protocol or header:Via:Protocol-
        if (params.length == 3 && params[2].equalsIgnoreCase("Protocol"))
        {
    		MsgParser parser1 = new MsgParser(); 
    		parser1.splitHeader(via, " ");
    		MsgParser parser2 = new MsgParser(); 
    		parser2.splitHeader(parser1.getHeader(0), "/");
    		addSIPViaTransport(var, parser2);
    		return true;
        }
        //---------------------------------------------------------------------- header:TopMostVia:Transport or header:Via:Transport-
        if (params.length == 3 && params[2].equalsIgnoreCase("Transport"))
        {
    		MsgParser parser1 = new MsgParser(); 
    		parser1.splitHeader(via, " ");
    		MsgParser parser2 = new MsgParser(); 
    		parser2.splitHeader(parser1.getHeader(0), "/");
            var.addHeader(parser2.getHeader(2));
    		return true;
        }
        //---------------------------------------------------------------------- header:TopMostVia:Host or header:Via:Host-
        if (params.length == 3 && params[2].equalsIgnoreCase("Host"))
        {
    		MsgParser parser1 = new MsgParser(); 
    		parser1.splitHeader(via, " ");
    		Header hdr = parser1.getHeader(1);
            for (int 	i = 0; i < hdr.getSize(); i++) 
            {
            	String strVia = hdr.getHeader(i);
            	String hostVia;
           		int pos = strVia.indexOf(']');
           		// case IPV4
           		if (pos < 0)
            	{
					int pos1 = strVia.lastIndexOf(':');
					hostVia = strVia; 
					if (pos1 >= 0)
					{
						hostVia = strVia.substring(0, pos1);
					}
            	}
           		// case IPV6
           		else
           		{
					hostVia = strVia.substring(0, pos + 1);
           		}
		        var.add(hostVia);
            }
    		return true;
        }
        //---------------------------------------------------------------------- header:TopMostVia:Port or header:Via:Port-
        if (params.length == 3 && params[2].equalsIgnoreCase("Port"))
        {
    		MsgParser parser1 = new MsgParser(); 
    		parser1.splitHeader(via, " ");
    		Header hdr = parser1.getHeader(1);
            for (int i = 0; i < hdr.getSize(); i++) 
            {
            	String strVia = hdr.getHeader(i);
            	String portVia;
           		int pos = strVia.indexOf(']');
           		// case IPV4
           		if (pos < 0)
            	{
					int pos1 = strVia.lastIndexOf(':');
					portVia = strVia; 
					if (pos1 >= 0)
					{
						portVia = strVia.substring(pos1 + 1);
					}
					else
					{
						portVia = "5060";
					}
            	}
           		// case IPV6
           		else
           		{
           			if (pos < strVia.length() - 1)
           			{
           				portVia = strVia.substring(pos + 2);
           			}
           			else
           			{
           				portVia = "5060";
           			}
           		}
		        var.add(portVia);
            }
    		return true;
        }
        //---------------------------------------------------------------------- header:TopmostVia:Parameter:Xxxx or header:Via:Parameter:Xxxx-
        if (params.length == 4 && params[2].equalsIgnoreCase("Parameter"))
        {
    		boolean noMagic = false;
    		if (params[3].equalsIgnoreCase("branchNoMagic"))
    		{
    			params[3] = "branch";
    			noMagic = true;
    		}
            var.addHeader(parser.getHeader(params[3]), noMagic);
    		return true;
        }
		return false;
    }
    
    /** Get the elements of  header of this message */
    private void addSIPViaTransport(Parameter var, MsgParser parser) throws Exception
    {
    	Header header0 = parser.getHeader(0);
    	Header header1 = parser.getHeader(1);
    	int num0 = header0.getSize();
    	int num1 = header1.getSize();
    	int numMax = num0;
    	if (num1 > num0) numMax = num1;
    	String value;
    	for (int i = 0; i < numMax; i++)
    	{
    		value = header0.getHeader(i) + "/" + header1.getHeader(i);
    		var.add(value);
    	}
    }
    
    /** Get the elements of XXX header of this message */
    private boolean addSIPHeaderGenericXXX(Parameter var, String[] params, Header header) throws Exception
    {    	
        //---------------------------------------------------------------------- header:Xxxx -
        if (params.length == 2)
        {
            var.addHeader(header);
       		return true;
      	}
        //---------------------------------------------------------------------- header:Xxxx:Namevalue -
        if (params[2].equalsIgnoreCase("Namevalue"))
        {
            var.addHeaderNamevalue(header);
       		return true;
      	}        
        //---------------------------------------------------------------------- header:Xxxx:URI:... -
        if (params[2].equalsIgnoreCase("URI"))
        {            	            
        	Header token = header.parseParameter(null, ";", '=', "<>", "\"\"");
        	addSIPURIHeader(var, params, token);
       		return true;
        } 
        //---------------------------------------------------------------------- header:Xxxx:Address:... -
        if (params[2].equalsIgnoreCase("Address"))
        {
        	Header token = header.parseParameter(null, ";", '=', "<>", "\"\"");
            addSIPAddressHeader(var, params, token);
       		return true;
        }        
        //---------------------------------------------------------------------- header:Xxxx:Parameter:Yyyy -
        else if (params[2].equalsIgnoreCase("Parameter"))
        {
        	if (params.length == 3)
        	{
	        	Header token = header.parseParameter(null, ";", '=', "<>", "\"\"");
	            var.addHeader(token);
	       		return true;
        	} 
        	else
        	{	
	        	Header token = header.parseParameter(params[3], ";", '=', "<>", "\"\"");
	            var.addHeader(token);
	       		return true;
        	}
        }
        //---------------------------------------------------------------------- header:Xxxx:Argument:Yyyy -
        else if (params[2].equalsIgnoreCase("Attribute"))
        {
        	if (params.length == 3)
        	{
	        	Header token = header.parseParameter(null, ",", '=', "<>", "\"\"");
	            var.addHeader(token);
	       		return true;
        	}
        	else
        	{	
	        	Header token = header.parseParameter(params[3], ",", '=', "<>", "\"\"");
	            var.addHeader(token);
	       		return true;
        	}
        }
        //---------------------------------------------------------------------- header:Xxxx:Argument:Yyyy -
        else if (params[2].equalsIgnoreCase("Argument"))
        {
        	MsgParser parser = new MsgParser(); 
			parser.splitHeader(header, " ");
            var.addHeader(parser.getHeader(Integer.parseInt(params[3])));       		
            return true;
        }
        //---------------------------------------------------------------------- header:Xxxx:Argument:Yyyy -
    
   		return false;
   	}

    /*
     * Complete the Via header with Received and RPort paramter if needed
     * 
     */
    public void completeViaTopmostHeader() throws Exception
    {
    	Header via = message.getHeader("Via");
		Parameter viaHostParam = getParameter("header.TopmostVia.Host");
		if (viaHostParam.length() > 0)
		{		
			String viaHost = viaHostParam.get(0).toString();
			if ("no".equalsIgnoreCase(viaHost))
			{
				String received = getChannel().getRemoteHost(); 
				int rPort = getChannel().getRemotePort();
				String topmostVia = via.getHeader(0).toString();
				via.setHeader(0, topmostVia + ";Received=" + received + ";RPort=" + rPort);
			}
		}
    }

}