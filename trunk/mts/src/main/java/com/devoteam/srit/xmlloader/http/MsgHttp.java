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

package com.devoteam.srit.xmlloader.http;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.Trans;
import com.devoteam.srit.xmlloader.core.protocol.TransactionId;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLLoaderEntityResolver;

import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.SupArray;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpMessage;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpVersion;
import org.apache.http.entity.ByteArrayEntity;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultAttribute;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.DefaultText;

/**
 *
 * @author gpasquiers
 */
public class MsgHttp extends Msg
{

    private HttpMessage message;
    private String messageContent;
    private boolean ignoreContents ;
    
    private String type = null;
    
    
    /** Creates a new instance of MsgHttp from a message */
    public MsgHttp(HttpMessage aMessage) throws Exception
    {
        super();
        this.ignoreContents = Config.getConfigByName("http.properties").getBoolean("message.IGNORE_RECEIVED_CONTENTS", false);

        message = aMessage;

        HttpEntity entity;

        //
        // Get the entity
        //
        if (message instanceof HttpResponse)
        {
            entity = ((HttpResponse) message).getEntity();
        }
        else if (message instanceof HttpEntityEnclosingRequest)
        {
            entity = ((HttpEntityEnclosingRequest) message).getEntity();
        }
        else
        {
            entity = null;
        }

        //
        // Convert the entity to a String
        //
        if (null != entity && (entity.getContentLength() != -1 || entity.isChunked()))
        {
            if (this.ignoreContents)
            {
                entity.consumeContent();
                messageContent = "";
            }
            else
            {
                InputStream inputStream = entity.getContent();
                int l;
                byte[] tmp = new byte[2048];
                SupArray array = new SupArray();

                while ((l = inputStream.read(tmp)) != -1)
                {
                    byte[] bytes = new byte[l];
                    for(int i=0; i<l; i++) bytes[i] = tmp[i];
                    array.addLast(new DefaultArray(bytes));

                }
                inputStream.close();
                messageContent = Utils.newString(array.getBytes());
            }
        }
        else
        {
            messageContent = null;
        }
    }

    /** Creates a new instance of MsgHttp from data scenario */
    public MsgHttp(String data) throws Exception
    {
    	data = data.trim();
        BasicHttpResponse response = null;
        BasicHttpEntityEnclosingRequest request = null;

        HttpMessage currentMessage = null;

        int endOfLine;
        String line;



        //
        // Clean beginning of first line (XML-related control characters)
        //
        while (data.startsWith("\n") || data.startsWith("\r") || data.startsWith(" ") || data.startsWith("\t"))
        {
            data = data.substring(1);
        }

        while (data.endsWith("\n") || data.endsWith("\r") || data.endsWith(" ") || data.endsWith("\t"))
        {
            data = data.substring(0, data.length() - 1);
        }

        String headers = data.split("[\\r]?[\\n][\\r]?[\\n]")[0];
        String datas = data.substring(headers.length());
        if(datas.startsWith("\r")) datas = datas.substring(1);
        if(datas.startsWith("\n")) datas = datas.substring(1);
        if(datas.startsWith("\r")) datas = datas.substring(1);
        if(datas.startsWith("\n")) datas = datas.substring(1);

        //
        // Use only \n: remove \r
        //
        headers = headers.replace("\r", "");



        //
        // Whatever the message is, it seems it should end with a new line
        //
        headers += "\n";

        //
        // Get first line
        //
        endOfLine = headers.indexOf("\n");
        line = headers.substring(0, endOfLine);

        // Remove first line from data
        headers = headers.substring(endOfLine + 1, headers.length());

        // Message is a response
        if (line.startsWith("HTTP"))
        {
            String[] parts = line.split(" ");

            HttpVersion httpVersion = HttpVersion.HTTP_1_1;
            if (parts[0].endsWith("HTTP/1.0"))
            {
                httpVersion = HttpVersion.HTTP_1_0;
            }

            String phrase = "";
            if (parts.length > 2)
            {
                phrase = parts[2];
            }

            response = new BasicHttpResponse(httpVersion, Integer.parseInt(parts[1]), phrase);
            currentMessage = response;
        } // Message is a request
        else
        {
            String[] parts = line.split(" ");
            HttpVersion httpVersion = HttpVersion.HTTP_1_1;
            
            if ((parts.length == 3) &&(parts[2].endsWith("HTTP/1.0")))
            {
                httpVersion = HttpVersion.HTTP_1_0;
            }

            request = new BasicHttpEntityEnclosingRequest(parts[0], parts[1], httpVersion);
            currentMessage = request;
        }

        //
        // Parse headers
        //
        endOfLine = headers.indexOf("\n");
        if (endOfLine != -1)
        {
            line = headers.substring(0, endOfLine);
        }
        while (endOfLine != -1 && line.length() > 0)
        {
            // Remove line from data since we parse it here
            headers = headers.substring(endOfLine + 1, headers.length());

            // Add header to message
            int index = line.indexOf(":");

            if (index == -1)
            {
                throw new ExecutionException("Invalid header -> " + line);
            }

            String name =  line.substring(0, index).trim();
            String value =  line.substring(index + 1, line.length()).trim();
            if (!name.equals("Content-Length") || Utils.isInteger(value))
            {
            	currentMessage.addHeader(name, value);
            }

            // Read next line
            endOfLine = headers.indexOf("\n");
            if (endOfLine != -1)
            {
                line = headers.substring(0, endOfLine);
            }
        }

        //
        // Register parsed message into the class variable
        //
        message = currentMessage;

        //
        // Set entity into the message
        //
        if (datas.length() > 0)
        {
            //
            // Check if we are allowed to have an entity in this message
            //
            if (null != request)
            {
                if (request.getRequestLine().getMethod().toLowerCase().equals("get") ||
                        request.getRequestLine().getMethod().toLowerCase().equals("head"))
                {
                    throw new ExecutionException("Request " + request.getRequestLine().getMethod() + " is not allowed to contain an entity");
                }
            }

            if (null != response || null != request)
            {
                messageContent = datas;
            }

            //
            // Set the Content-Length header if the transfer-encoding is not "chunked"
            //
            Header[] contentEncoding = currentMessage.getHeaders("Transfer-Encoding");
            Header[] contentType = currentMessage.getHeaders("Content-Type");
            Header[] contentLength = currentMessage.getHeaders("Content-Length");
            if(contentType.length > 0 && contentType[0].getValue().toLowerCase().contains("multipart"))
            {
                // do nothing, we should not add a CRLF at the end of the data when multipart
            }
            else
            {
                messageContent += "\r\n"; // it seems necessary to end the data by a CRLF (???)
            }

            if(contentEncoding.length == 0 || !contentEncoding[0].getValue().contains("chunked"))
            {
            	if (contentLength.length == 0)
            	{
	                if (null != messageContent)
	                {
	                    currentMessage.addHeader("Content-Length", Integer.toString(messageContent.length()));
	                }
	                else
	                {
	                    currentMessage.addHeader("Content-Length", Integer.toString(0));
	                }
            	}
            }
            else
            {
                messageContent += "\r\n"; // when chunked, there must be a double CRLF after the last chunk size
            }

            HttpEntity entity = new ByteArrayEntity(messageContent.getBytes());
            if (null != response)
            {
                response.setEntity(entity);
            }
            else if (null != request)
            {
                request.setEntity(entity);
            }
            else
            {
                entity = null;
            }


        }
    }

    /** Returns the HTTP message without entity */
    public HttpMessage getMessage()
    {
        return message;
    }

    /** Returns the entity of a HTTP message */
    public String getMessageContent()
    {
        return messageContent;
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
        
        if (params.length > 1 && params[0].equalsIgnoreCase("header"))
        {
            //------------------------------------------------------------------------ header:xxx -
            Header[] listhead = getMessage().getHeaders(params[1]);
            for (int i = 0; i < listhead.length; i++)
            {
            	var.add(listhead[i].getValue());
            }
        }
        else if (params.length == 1 && params[0].equalsIgnoreCase("firstline"))
        {
            //---------------------------------------------------------------------- firstline -
            if (message instanceof HttpRequest)
            {
            	var.add(((HttpRequest) message).getRequestLine().toString());
            }
            else
            {
            	var.add(((HttpResponse) message).getStatusLine().toString());
            }
        }
        else if (params.length > 1 && params[0].equalsIgnoreCase("firstline"))
        {
            //---------------------------------------------------------------------- firstline:Version -
            if (params[1].equalsIgnoreCase("version"))
            {
            	var.add(message.getProtocolVersion().toString());
            }
            //---------------------------------------------------------------------- firstline:Method -
            else if (params[1].equalsIgnoreCase("method"))
            {
                if (message instanceof HttpRequest)
                {
                	var.add(((HttpRequest) message).getRequestLine().getMethod());
                }
            }
            //---------------------------------------------------------------------- firstline:URI -
            else if (params[1].equalsIgnoreCase("uri"))
            {
                if (message instanceof HttpRequest)
                {
                	var.add(((HttpRequest) message).getRequestLine().getUri());
                }
            }
            else if (params[1].equalsIgnoreCase("reasonPhrase"))
            {
                if (message instanceof HttpResponse)
                {
                	var.add(((HttpResponse) message).getStatusLine().getReasonPhrase());
                }
            }
            else if (params[1].equalsIgnoreCase("statuscode"))
            {
                if (message instanceof HttpResponse)
                {
                	var.add(Integer.toString(((HttpResponse) message).getStatusLine().getStatusCode()));
                }
            }
            else
            {
            	Parameter.throwBadPathKeywordException(path);
            }
        }
        else if (params.length >= 1 && params[0].equalsIgnoreCase("content"))
        {
        	if (params.length == 1)
        	{
        		var.add(getMessageContent());
        	}
        	else if ((params.length > 1) && (params[1].equalsIgnoreCase("xml")))
          	{
        		if ((params.length > 3) && (params[2].equalsIgnoreCase("xpath")))
        		{
		            String strXpath = params[3];
		            for (int i = 4; i < params.length; i++)
		            {
		            	strXpath += "." + params[i];
		            }
		    		var.applyXPath(getMessageContent(), strXpath, true);
        		}
                else
                {
                	Parameter.throwBadPathKeywordException(path);
                }
          	}
        	// not documented features
	        else if (path.equalsIgnoreCase("content:xml:operation"))
	        {
	            //
	            // Read name of method
	            //
	            String content = messageContent;
	
	            //
	            // Read and delete the 3 first tags
	            //
	            for (int i = 0; i < 3; i++)
	            {
	                content = content.substring(content.indexOf(">") + 1);
	            }
	
	            //
	            // Read the first tag of the remaining content
	            //
	            String tag = content.substring(content.indexOf("<"), content.indexOf(">"));
	
	            //
	            // Read tag name
	            //
	            String name = tag.substring(1);
	            name.trim();
	            int endOfName;
	            name = name.replace("\r", "");
	            endOfName = name.indexOf(" ");
	            if (endOfName == -1 || name.indexOf("\n") != -1 && name.indexOf("\n") < endOfName)
	            {
	                endOfName = name.indexOf("\n");
	            }
	
	            if (endOfName == -1)
	            {
	                endOfName = name.length();
	            }
	
	            name = name.substring(0, endOfName);
	
	            //
	            // Remove namespace
	            //
	            if (name.indexOf(":") != -1)
	            {
	                name = name.substring(name.indexOf(":") + 1);
	            }
	
	            var.add(name);
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

    /** Get the protocol of this message */
    public String getProtocol()
    {
        return StackFactory.PROTOCOL_HTTP;
    }

    /** Return true if the message is a request else return false*/
    public boolean isRequest()
    {
        if (message instanceof HttpRequest)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public void setTransactionId(TransactionId transactionId)
    {
        super.setTransactionId(transactionId);
        
        if(isRequest() && null != getType()) return;
        
        try
        {
            Trans trans = StackFactory.getStack(StackFactory.PROTOCOL_HTTP).getInTransaction(transactionId);
            if (trans != null)
            {
	            Msg req = trans.getBeginMsg();
	            this.setType(req.getType());
            }
        }
        catch(Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "Could not find the request matching this answer", this, "\n and thus, couldn't set the Type of the answer");
        }
    }
    /** Get the command code of this message */
    public String getType()
    {
        if(null == type)
        {
            if (message instanceof HttpRequest)
            {
                type = ((HttpRequest) message).getRequestLine().getMethod();
            }
        }
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }
    
    /** Get the result of this answer (null if request) */
    public String getResult()
    {
        if (message instanceof HttpResponse)
        {
            return Integer.toString(((HttpResponse) message).getStatusLine().getStatusCode());
        }
        else
        {
            return null;
        }
    }
    
    /** Return the transport of the message*/
    public String getTransport() {
    	return StackFactory.PROTOCOL_TCP;
    }

    private String getTextMessage()
    {
        // get the first line
    	String ret = getFirstLine();            
        ret += "\r\n";

        // get the headers list
        Header[] header = message.getAllHeaders();
        for (int i = 0; i < header.length; i++)
        {
            ret += header[i] + "\r\n";
        }
        ret += "\r\n";
        
        // get the message content
        if (messageContent != null)
        {
        	ret += messageContent;
        }
        
        return ret;
    }

    private String getFirstLine()
    {
        if (this.message instanceof HttpResponse)
        {
            return ((HttpResponse) message).getStatusLine().toString();
        }
        else
        {
            return ((HttpRequest) message).getRequestLine().toString();
        }
    }

    /** Get the data (as binary) of this message */
    @Override
    public byte[] getBytesData()
    {
    	return getTextMessage().getBytes();
    }

    /** Returns a short description of the message. Used for logging as INFO level */
    /** This methods HAS TO be quick to execute for performance reason */
    @Override
    public String toShortString() throws Exception {
    	String ret = super.toShortString();
  		ret += getFirstLine();
   		ret += "<transactionId =\"" + getTransactionId() + "\">";    		
    	return ret;
    }

    /** Get the XML representation of the message; for the genscript module. */
    @Override
    public String toXml() throws Exception {
		return getTextMessage();
    }
}
