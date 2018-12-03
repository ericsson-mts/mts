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

package com.devoteam.srit.xmlloader.http2;

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import org.apache.hc.core5.http.HttpMessage;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.dom4j.Element;

/**
 *
 * @author qqin
 */
public class MsgHttp2 extends Msg {
	private HttpMessage message;
	private String messageContent;

	/** Creates a new instance */
	public MsgHttp2(Stack stack) {
		super(stack);
	}

	/** Creates a new instance */
	public MsgHttp2(Stack stack, HttpMessage aMessage) throws Exception {
		this(stack);
		message = aMessage;
	}

	@Override
	public boolean isRequest() throws Exception {
		System.out.println("MsgHttp2.isRequest()");
		if (message instanceof HttpRequest) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String getResult() throws Exception {
		System.out.println("MsgHttp2.getResult()");
		return "mon resultat";
	}

	@Override
	public void decode(byte[] data) throws Exception {
		System.out.println("MsgHttp2.decode()");
	}

	@Override
	public String toXml() throws Exception {
		System.out.println("MsgHttp2.toXml()");
		return "mon message";
	}

	@Override
	public byte[] encode() throws Exception {
		System.out.println("MsgHttp2.encode()");
		return "msg http2".getBytes();
	}

	@Override
	public void parseFromXml(ParseFromXmlContext request, Element root, Runner runner) throws Exception {
		System.out.println("MsgHttp2.parseFromXml()");
		super.parseFromXml(request, root, runner);

		message = getHttpMessage(request, root);
	}

	/**
	 * Get the type of the message Used for message filtering with "type" attribute
	 * and for statistic counters
	 */
	@Override
	public String getType() {
		if (null == type) {
			if (message instanceof HttpRequest) {
				type = ((HttpRequest) message).getMethod();
			}
		}
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public HttpMessage getMessage() {
		return message;
	}

	public void setMessage(HttpMessage message) {
		this.message = message;
	}

	public String getMessageContent() {
		return messageContent;
	}

	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}

	private HttpMessage getHttpMessage(ParseFromXmlContext request1, Element root) throws ExecutionException {
		String text = root.getText();
    	
    	BasicClassicHttpResponse responseMessage = null;
        BasicClassicHttpRequest requestMessage = null;

        HttpMessage currentMessage = null;
        
        int endOfLine;
        String line;

        //
        // Clean beginning of first line (XML-related control characters)
        //
        while (text.startsWith("\n") || text.startsWith("\r") || text.startsWith(" ") || text.startsWith("\t"))
        {
            text = text.substring(1);
        }

        while (text.endsWith("\n") || text.endsWith("\r") || text.endsWith(" ") || text.endsWith("\t"))
        {
        	text = text.substring(0, text.length() - 1);
        }

        String headers = text.split("[\\r]?[\\n][\\r]?[\\n]")[0];
        String datas = text.substring(headers.length());
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

            responseMessage = new BasicClassicHttpResponse(Integer.parseInt(parts[1]), phrase);
            currentMessage = responseMessage;
            currentMessage.setVersion(httpVersion);
        } // Message is a request
        else
        {
            String[] parts = line.split(" ");
            HttpVersion httpVersion = HttpVersion.HTTP_1_1;
            
            if ((parts.length == 3) &&(parts[2].endsWith("HTTP/1.0")))
            {
                httpVersion = HttpVersion.HTTP_1_0;
            }

            requestMessage = new BasicClassicHttpRequest(parts[0], parts[1]);
            requestMessage.setPath(parts[1]);
            currentMessage = requestMessage;
            currentMessage.setVersion(httpVersion);
        }

        //
        // Parse headers
        //
        endOfLine = headers.indexOf("\n");
        if (endOfLine != -1)
        {
            line = headers.substring(0, endOfLine);
        }
        boolean contentLengthPresent = false;
        while (endOfLine != -1 && line.length() > 0)
        {
            // Remove line from data since we parse it here
            headers = headers.substring(endOfLine + 1, headers.length());

            // Add header to message
            int index = line.indexOf(":");

            if (index == -1)
            {
                throw new ExecutionException("Invalid header ':' not found " + line);
            }

            String name =  line.substring(0, index).trim();
            String value =  line.substring(index + 1, line.length()).trim();
            if (!name.equals("Content-Length") || Utils.isInteger(value))
            {
            	currentMessage.addHeader(name, value);
            }
            else
            {
            	contentLengthPresent = true;
            }

            // Read next line
            endOfLine = headers.indexOf("\n");
            if (endOfLine != -1)
            {
                line = headers.substring(0, endOfLine);
            }
        }

        return currentMessage;
	}
}
