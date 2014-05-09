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

package com.devoteam.srit.xmlloader.sip.jain;

import gov.nist.javax.sip.header.CSeq;
import gov.nist.javax.sip.header.CallID;
import gov.nist.javax.sip.header.Contact;
import gov.nist.javax.sip.header.ContactList;
import gov.nist.javax.sip.header.From;
import gov.nist.javax.sip.header.ParametersHeader;
import gov.nist.javax.sip.header.RecordRoute;
import gov.nist.javax.sip.header.RecordRouteList;
import gov.nist.javax.sip.header.Route;
import gov.nist.javax.sip.header.RouteList;
import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.To;
import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.header.ViaList;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;

import java.util.Iterator;
import java.util.ListIterator;

import javax.sip.SipFactory;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.Header;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.operations.basic.operators.PluggableParameterOperatorSetFromAddress;
import com.devoteam.srit.xmlloader.core.operations.basic.operators.PluggableParameterOperatorSetFromURI;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.coding.text.ContentParser;
import com.devoteam.srit.xmlloader.sip.MsgSip;

/**
 *
 * @author gpasquiers
 */
public class MsgSipJain extends MsgSip
{
    private SIPMessage sipMessage;
    
    /** Creates a new instance of MsgSip */
    public MsgSipJain() 
    {
        super();
    }
    
    /** Creates a new instance of MsgSip */
    public MsgSipJain(String text, int addCRLFContent) throws Exception
    {
    	super();

        try
        {
            MessageFactory messageFactory = SipFactory.getInstance().createMessageFactory();

            text = text.trim();
            text = text.replace("\r\n", "\n");
            text = text.replace("\n", "\r\n");

            if (text.startsWith("SIP/"))
            {
                sipMessage = (SIPResponse) messageFactory.createResponse(text + "\r\n\r\n");
            }
            else
            {
                sipMessage = (SIPMessage) messageFactory.createRequest(text + "\r\n\r\n");
            }

            int posContent = text.indexOf("\r\n\r\n");
            if (posContent >= 0)
            {
                String contentString = text.substring(posContent).trim();
                
                // bug NSN equipment : add a CRLF at the end of the Content
                if (addCRLFContent == 1)
                {
                	contentString = contentString + "\r\n"; 
                }
                
                ContentTypeHeader contentType = sipMessage.getContentTypeHeader();
                sipMessage.setContent(contentString, contentType);
            }
        }
        catch (Exception e)
        {
        	throw new ExecutionException("Can't parse the SIP message : ", e);
        }
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

        try
        {
	        if (params.length == 1 && params[0].equalsIgnoreCase("firstline"))
	        {
	            //---------------------------------------------------------------------- firstline -
	            var.add(sipMessage.getFirstLine().trim());
	            return var;
	        }
	        else if (params.length > 1 && params[0].equalsIgnoreCase("firstline"))
	        {
	            //---------------------------------------------------------------------- firstline:Version -
	            if (params[1].equalsIgnoreCase("Version"))
	            {
	            	if (sipMessage instanceof SIPRequest)
	            	{
	            		var.add(((SIPRequest) sipMessage).getSIPVersion());
	            	}
	            	if (sipMessage instanceof SIPResponse)
	            	{
	            		var.add(((SIPResponse) sipMessage).getSIPVersion());
	            	}
	                return var;
	            }
	            //---------------------------------------------------------------------- firstline:Method -
	            else if (params[1].equalsIgnoreCase("Method"))
	            {
	            	if (sipMessage instanceof SIPRequest)
	            	{
	            		var.add(((SIPRequest) sipMessage).getMethod());
	            	}
	                return var;
	            }
	            //---------------------------------------------------------------------- firstline:URI -
	            else if (params[1].equalsIgnoreCase("URI"))
	            {
	            	if (sipMessage instanceof SIPRequest)
	            	{
	            		addSIPURIHeader(var, params, ((SIPRequest) sipMessage).getRequestURI().toString());
	            	}
	                return var;
	            }
	            //---------------------------------------------------------------------- firstline:StatusCode -
	            else if (params[1].equalsIgnoreCase("StatusCode"))
	            {
	            	if (sipMessage instanceof SIPResponse)
	            	{
	            		var.add(Integer.toString(((SIPResponse) sipMessage).getStatusCode()));
	            		return var;
	            	}
	            }
	            //----------------------------------------------------------------------- firstline:ReasonPhrase -
	            else if (params[1].equalsIgnoreCase("ReasonPhrase"))
	            {
	            	if (sipMessage instanceof SIPResponse)
	            	{
	            		var.add(((SIPResponse) sipMessage).getReasonPhrase());
	            	}
	                return var;
	            }
	            else
	            {
	            	Parameter.throwBadPathKeywordException(path);
	            }
	        }
	        else if (params.length == 1 && params[0].equalsIgnoreCase("header"))
	        {
	            Iterator iter = sipMessage.getHeaders();
	            while (iter.hasNext())
	            {
	                var.add(iter.next().toString().trim());
	            }
	            return var;
	        }
	        else if (params.length > 1 && params[0].equalsIgnoreCase("header"))
	        {
	            //---------------------------------------------------------------------- header:Yyyyy -
	            if (params.length >= 2 && (params[1].equals("Authorization")))
	            {
	                if (params.length == 2)
	                {
	                    var.add(sipMessage.getAuthorization().getValue());
	                    return var;
	                }
	                else
	                {
	                    addSIPHeaderAuthorization(var, params);
	                    return var;
	                }
	            }
	            //---------------------------------------------------------------------- header:To -
	            else if (params[1].equals("To"))
	            {
	                if (params.length == 2)
	                {
	                    addSIPHeader(var, sipMessage.getTo().toString());
	                    return var;
	                }
	                else
	                {
	                    addSIPHeaderTo(var, params);
	                    return var;
	                }
	            }
	            //---------------------------------------------------------------------- header:From -
	            else if (params[1].equals("From"))
	            {
	                if (params.length == 2)
	                {
	                    addSIPHeader(var, sipMessage.getFrom().toString());
	                    return var;
	                }
	                else
	                {
	                    addSIPHeaderFrom(var, params);
	                    return var;
	                }
	            }
	            //---------------------------------------------------------------------- header:ContentType -
	            else if (params[1].equals("ContentType"))
	            {
	                if (params.length == 2)
	                {
	                    var.add(sipMessage.getContentTypeHeader().getValue());
	                    return var;
	                }
	                else
	                {
	                    addSIPHeaderContentType(var, params);
	                    return var;
	                }
	            }
	            //---------------------------------------------------------------------- header:Contact -
	            else if (params[1].equals("Contact"))
	            {
	                if (params.length == 2)
	                {
	                	if (sipMessage.getContactHeaders() != null)
	                	{
	                		addSIPHeaderFromTab(var, sipMessage.getContactHeaders().toArray());                		
	                	}
	            		return var;
	                }
	                else
	                {
	                    addSIPHeaderContact(var, params, sipMessage.getContactHeaders());
	                    return var;
	                }
	            }
	            //---------------------------------------------------------------------- header:CSeq -
	            else if (params[1].equals("CSeq"))
	            {            	
	            	addSIPHeaderCSeq(var, params, (CSeq) sipMessage.getCSeq());                
	            }
	            //---------------------------------------------------------------------- header:CSeqNumber -
	            else if (params[1].equals("CSeqNumber"))
	            {
	                var.add(Long.valueOf(sipMessage.getCSeq().getSeqNumber()).toString());
	            	GlobalLogger.instance().logDeprecatedMessage("setFromMessage value=header:CSeqNumber", "setFromMessage value=header:CSeq:Number");
	                return var;
	            }
	            //---------------------------------------------------------------------- header:CSeqMethod -
	            else if (params[1].equals("CSeqMethod"))                	
	            {
	                var.add(sipMessage.getCSeq().getMethod().toString());
	            	GlobalLogger.instance().logDeprecatedMessage("setFromMessage value=header:CSeqMethod", "setFromMessage value=header:CSeq:Method");
	                return var;
	            }
	
	            //---------------------------------------------------------------------- header:Route -
	            else if (params[1].equals("Route"))
	            {
	            	if (sipMessage.getRouteHeaders() != null)
	            	{
		                Iterator iter = sipMessage.getRouteHeaders().iterator();
		                while (iter.hasNext())
		                {
		                    addSIPHeaderRoute(var, params, (Route) iter.next());
		                }
	            	}
	                return var;
	            }
	            //---------------------------------------------------------------------- header:RecordRoute -
	            else if (params[1].equals("RecordRoute"))
	            {
	                Iterator iter = sipMessage.getRecordRouteHeaders().iterator();
	                while (iter.hasNext())
	                {
	                    addSIPHeaderRecordRoute(var, params, (RecordRoute) iter.next());
	                }
	                return var;
	            }
	            //---------------------------------------------------------------------- header:TopMostVia -
	            else if (params[1].equals("TopmostVia"))
	            {
	                addSIPHeaderVia(var, params, sipMessage.getTopmostVia());
	                return var;
	            }
	            //---------------------------------------------------------------------- header:Via -
	            else if (params[1].equals("Via"))
	            {
	                Iterator iter = sipMessage.getViaHeaders().iterator();
	                while (iter.hasNext())
	                {
	                    addSIPHeaderVia(var, params, (Via) iter.next());
	                }
	                return var;
	            }
	            //---------------------------------------------------------------------- header:From -
	            else if (params[1].equalsIgnoreCase("DialogId"))
	            {
	                addSIPHeader(var, getDialogId().toString());
	                return var;
	            }            
	            //---------------------------------------------------------------------- header:Others -
	            else if (params[1].equalsIgnoreCase("Others"))
	            {
	                Iterator iter = sipMessage.getHeaders();
	                while (iter.hasNext())
	                {
	                	Header header = (Header)iter.next();
	                    if (!(header instanceof To) &&
	                        !(header instanceof From) &&
	                        !(header instanceof ContactList) &&
	                        !(header instanceof CSeq) &&
	                        !(header instanceof CallID) &&
	                        !(header instanceof RouteList) &&
	                        !(header instanceof RecordRouteList) &&
	                        !(header instanceof ViaList))
	                    {
	                    	var.add(header.toString().trim());
	                    }
	                }
	                return var;
	            }
	            //---------------------------------------------------------------------- header:Xxxx -
	            else
	            {
	                if (params.length == 2)
	                {
	                    addIterator(var, sipMessage.getHeaders(params[1]));
	                    return var;
	                }
	                else
	                {
	                    addSIPHeaderXXX(var, params, sipMessage.getHeaders(params[1]));
	                    return var;
	                }
	            }
	        }
	        //---------------------------------------------------------------------- content(X): -
	        if (params[0].toLowerCase().startsWith("content"))
	        {
	        	String content = sipMessage.getMessageContent();
	        	String contentBoundary = sipMessage.getContentTypeHeader().getParameter("boundary");
	    		ContentParser contentParser = new ContentParser("SIP",content, contentBoundary);
	        	contentParser.addContentParameter(var, params,path);
	            return var;
	        }
        }
        catch (Exception e)
        {
        	var = new Parameter();
        }
        return var;
    }

    /** Get the message Identifier of this message */
    private void addIterator(Parameter var, Iterator iter) throws Exception
    {
        while (iter.hasNext())
        {
            addSIPHeader(var, iter.next().toString());
        }
    }

    /** Get the message Identifier of this message */
    private void addSIPHeader(Parameter var, String header) throws Exception
    {
        int iPos = header.indexOf(":");
        header = header.substring(iPos + 1).trim();
        var.add(header);
    }

    /** Get the message Address of this message */
    private void addSIPAddressHeader(Parameter var, String[] params, String address) throws Exception
    {
        if (params.length == 3)
        {
            var.add(address);
        }
        else
        {
            String operandeAddress = "";
            for (int i = 3; i < params.length; i++)
            {
                operandeAddress += params[i] + ".";
            }
            operandeAddress = operandeAddress.substring(0, operandeAddress.length() - 1);
            var.add(PluggableParameterOperatorSetFromAddress.setFromAddress(address, operandeAddress));
        }
    }

    /** Get the message Uri of this message */
    private void addSIPURIHeader(Parameter var, String[] params, String uri) throws Exception
    {
        if (params.length == 2)
        {
            var.add(uri);
        }
        else
        {
            String operandeAddress = "";
            for (int i = 2; i < params.length; i++)
            {
                operandeAddress += params[i] + ".";
            }
            operandeAddress = operandeAddress.substring(0, operandeAddress.length() - 1);
            var.add(PluggableParameterOperatorSetFromURI.setFromUri(uri, operandeAddress));
        }
    }

    /** Get the elements of To or From header of this message */
    private void addSIPHeaderAuthorization(Parameter var, String[] params) throws Exception
    {
        //---------------------------------------------------------------------- header:Yyyyyy:Scheme -
        if (((params.length == 3) && params[2].equalsIgnoreCase("URI")))
        {
            var.add(sipMessage.getAuthorization().getURI().toString());
        	GlobalLogger.instance().logDeprecatedMessage("setFromMessage value=header:" + params[1] + ":URI", "setFromMessage value=header:" + params[1] + ":Attribute:URI");            
        }
        //---------------------------------------------------------------------- header:Yyyyyy:Scheme -
        if (((params.length == 3) && params[2].equalsIgnoreCase("Scheme")))
        {
            var.add(sipMessage.getAuthorization().getScheme().toString());
        }
        //---------------------------------------------------------------------- header:Yyyyyy:Parameter:Zzzzzz -
        else if ((params.length == 4) && params[2].equalsIgnoreCase("Parameter"))
        {
            var.add(sipMessage.getAuthorization().getParameter(params[3]));
        	GlobalLogger.instance().logDeprecatedMessage("setFromMessage value=header:" + params[1] + ":Parameter:" + params[3], "setFromMessage value=header:" + params[1] + ":Attribute:" + params[3]);
        }
        //---------------------------------------------------------------------- header:Yyyyyy:Attribute:Zzzzzz -
        else if ((params.length == 4) && params[2].equalsIgnoreCase("Attribute"))
        {
        	if (sipMessage.getAuthorization() != null)
        	{
        		var.add(sipMessage.getAuthorization().getParameter(params[3]));
        	}
        }
    }

    /** Get the elements of Content-Type header of this message */
    private void addSIPHeaderContentType(Parameter var, String[] params) throws Exception
    {
        //---------------------------------------------------------------------- header:ContentType:Parameter:Xxxx -
        if (params[2].equalsIgnoreCase("Parameter"))
        {
            var.add(sipMessage.getContentTypeHeader().getParameter(params[3]));
        }
    }

    /** Get the elements of From header of this message */
    private void addSIPHeaderFrom(Parameter var, String[] params) throws Exception
    {
        //---------------------------------------------------------------------- header:To|From:Tag -

        if (params[2].equals("Tag"))
        {
            var.add(sipMessage.getFrom().getTag().toString());
        	GlobalLogger.instance().logDeprecatedMessage("setFromMessage value=header:From:Tag", "setFromMessage value=header:From:Parameter:tag");            
        }
        //---------------------------------------------------------------------- header:To|From:Address -
        else if (params[2].equals("Address"))
        {
            String address = sipMessage.getFrom().getAddress().toString();
            addSIPAddressHeader(var, params, address);
        }
        else if ((params.length == 4) && params[2].equalsIgnoreCase("Parameter"))
        {
            //---------------------------------------------------------------------- header:To|From:Parameter:Xxxx -
            var.add(sipMessage.getFrom().getParameter(params[3]));
        }
        else
        {
            throw new ExecutionException("Error in addSIPHeaderFrom() function : unknown format operand_2 ");
        }
    }

    /** Get the elements of To header of this message */
    private void addSIPHeaderTo(Parameter var, String[] params) throws Exception
    {
        //---------------------------------------------------------------------- header:To|From:Tag -

        if (params[2].equals("Tag"))
        {
            var.add(sipMessage.getTo().getTag());
        	GlobalLogger.instance().logDeprecatedMessage("setFromMessage value=header:To:Tag", "setFromMessage value=header:To:Parameter:tag");            
        }
        //---------------------------------------------------------------------- header:To|From:Address -
        else if (params[2].equals("Address"))
        {
            String address = sipMessage.getTo().getAddress().toString();
            addSIPAddressHeader(var, params, address);
        }
        else if ((params.length == 4) && params[2].equalsIgnoreCase("Parameter"))
        {
            //---------------------------------------------------------------------- header:To|From:Parameter:Xxxx -
            var.add(sipMessage.getTo().getParameter(params[3]));
        }
        else
        {
            throw new ExecutionException("Error in addSIPHeaderTo() function : unknown format operand_2 ");
        }
    }

    /** Get the elements of CSeq header of this message */
    private void addSIPHeaderCSeq(Parameter var, String[] params, CSeq header) throws Exception
    {
        if (params.length == 2)
        {
            var.add(header.getValue());
        }
        //---------------------------------------------------------------------- header:CSeq:Number -
        else if (params[2].equals("Number"))
        {
            var.add(Long.valueOf(header.getSeqNumber()).toString());
        }
        //---------------------------------------------------------------------- header:CSeq:Method -
        else if (params[2].equals("Method"))
        {
            var.add(header.getMethod().toString());
        }
    }

    /** Get the elements of Via header of this message */
    private void addSIPHeaderVia(Parameter var, String[] params, Via header) throws Exception
    {
        if (params.length == 2)
        {
            var.add(header.getValue());
        }
        //---------------------------------------------------------------------- header:TopMostVia:Protocol or header:Via:Protocol-
        else if (params[2].equals("Protocol"))
        {
            var.add(header.getProtocol());
        }
        //---------------------------------------------------------------------- header:TopMostVia:Transport or header:Via:Transport-
        else if (params[2].equals("Transport"))
        {
            var.add(header.getTransport());
        }
        //---------------------------------------------------------------------- header:TopMostVia:Host or header:Via:Host-
        else if (params[2].equals("Host"))
        {
            var.add(header.getHost());
        }
        //---------------------------------------------------------------------- header:TopMostVia:Port or header:Via:Port-
        else if (params[2].equals("Port"))
        {
            var.add(Integer.toString(header.getPort()));
        }
        //---------------------------------------------------------------------- header:TopmostVia:Parameter:Xxxx or header:Via:Parameter:Xxxx-
        else if ((params.length == 4) && (params[2].equalsIgnoreCase("Parameter")))
        {
            var.add(header.getParameter(params[3]));
        }
        else
        {
            new ExecutionException("Error in Via function() : unknown format operand_2 ");
        }
    }

    /** Get the elements of Route header of this message */
    private void addSIPHeaderRoute(Parameter var, String[] params, Route header) throws Exception
    {
        if (params.length == 2)
        {
            var.add(header.getValue());
        }
        //---------------------------------------------------------------------- header:Route:Address -
        else if (params[2].equals("Address"))
        {
            String address = header.getAddress().toString();
            addSIPAddressHeader(var, params, address);
        //---------------------------------------------------------------------- header:Route:Parameter:Xxxx -
        }
        else if (params[2].equalsIgnoreCase("Parameter"))
        {
            var.add(header.getParameter(params[3]));
        }
    }

    /** Get the elements of RecordRoute header of this message */
    private void addSIPHeaderRecordRoute(Parameter var, String[] params, RecordRoute header) throws Exception
    {
        if (params.length == 2)
        {
            var.add(header.getValue());
        }
        //---------------------------------------------------------------------- header:Route:Address -
        else if (params[2].equals("Address"))
        {
            String address = header.getAddress().toString();
            addSIPAddressHeader(var, params, address);
        }
        //---------------------------------------------------------------------- header:Route:Parameter:Xxxx -
        else if (params[2].equalsIgnoreCase("Parameter"))
        {
            var.add(header.getParameter(params[3]));
        }
    }

    /** Get the elements of Contact header of this message */
    private void addSIPHeaderContact(Parameter var, String[] params, ContactList listContacts) throws Exception
    {

        Iterator iterator = listContacts.iterator();
        while (iterator.hasNext())
        {
            Contact currentContact = (Contact) iterator.next();
            //---------------------------------------------------------------------- header:Contact:Address -
            if (params[2].equals("Address"))
            {
                String address = currentContact.getAddress().toString();
                addSIPAddressHeader(var, params, address);
            }
            // ---------------------------------------------------------------------- header:Contact:QValue -
            else if (params[2].equals("QValue"))
            {
                var.add(Float.toString(currentContact.getQValue()));
            	GlobalLogger.instance().logDeprecatedMessage("setFromMessage value=header:Contact:QValue", "setFromMessage value=header:Contact:Parameter:q");
            }
            //---------------------------------------------------------------------- header:Contact:Parameter:Xxxx -
            else if ((params.length == 4) && (params[2].equalsIgnoreCase("Parameter")))
            {
                var.add(currentContact.getParameter(params[3]));
            }
            else
            {
                new ExecutionException("Error in addSIPHeaderContact() function : unknown format operand_2 ");
            }
        }
    }

    /** Get the elements of XXX header of this message */
    private void addSIPHeaderXXX(Parameter var, String[] params, ListIterator list) throws Exception
    {

        while (list.hasNext())
        {
            ParametersHeader currentHeader = (ParametersHeader) list.next();
            //---------------------------------------------------------------------- header:Xxxx:Parameter:Yyyy -
            if (params[2].equalsIgnoreCase("Parameter"))
            {
                var.add(currentHeader.getParameter(params[3]));
            }
            else if (params[2].equalsIgnoreCase("Attribute"))
            {
                var.add(currentHeader.getParameter(params[3]));
            }
            else
            {
                new ExecutionException("Error in addSIPHeaderXXX() function : unknown format operand_2 ");
            }
        }
    }

    /** Get the message Identifier of this message */
    private void addSIPHeaderFromTab(Parameter var, Object[] tabHeader) throws Exception
    {
        for (int i = 0; i < tabHeader.length; i++)
        {
            String header = ((SIPHeader) tabHeader[i]).getValue();
            var.add(header);
        }
    }

    /*
     * Complete the Via header with Received and RPort paramter if needed
     * 
     */
    public void completeViaTopmostHeader() throws Exception
    {
    	ViaList viaList = sipMessage.getViaHeaders();
		Parameter viaHostParam = getParameter("header.TopmostVia.Host");
		if ((viaHostParam != null) && (viaHostParam.length() > 0))
		{		
			String viaHost = viaHostParam.get(0).toString();
			if ("no".equalsIgnoreCase(viaHost))
			{
				String received = getChannel().getRemoteHost(); 
				int rPort = getChannel().getRemotePort();				
				ViaHeader topmostVia = (ViaHeader) viaList.getFirst();
				topmostVia.setReceived(received);
				topmostVia.setParameter("RPort", new Integer(rPort).toString());
			}
		}
    }

    /** Return true if the message is a request else return false*/
    public boolean isRequest()
    {
        return sipMessage instanceof SIPRequest;
    }
    
    public SIPMessage getSipMessage()
    {
        return sipMessage;
    }

    /** Get the data (as binary) of this message */
    @Override
    public byte[] getBytesData()
    {
        return sipMessage.encodeAsBytes();
    }

    /** Returns a short description of the message. Used for logging as INFO level */
    /** This methods HAS TO be quick to execute for performance reason */
    @Override
    public String toShortString() throws Exception
    {
    	String ret = super.toShortString();
		String firstline = sipMessage.getFirstLine();
		firstline = firstline.substring(0, firstline.length() - 2);
		ret += firstline;
        String transId = getTransactionId().toString();
        ret += "<transactionId=\"" + transId + "\">"; 
        String dialogId = getDialogId();
        ret+= "<DialogId=\"" + dialogId + "\">";
        return ret;
    }

    /** Get the XML representation of the message; for the genscript module. */
    @Override
    public String toXml() throws Exception {
    	return sipMessage.toString();
    }
    
}