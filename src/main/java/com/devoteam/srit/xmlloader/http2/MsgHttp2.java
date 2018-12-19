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

import org.apache.hc.core5.http.HttpMessage;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.nio.AsyncServerRequestHandler.ResponseTrigger;
import org.apache.hc.core5.http.protocol.HttpContext;

import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.http.MsgHttp;

/**
 *
 * @author qqin
 */
public class MsgHttp2 extends MsgHttp {
	
	private ResponseTrigger responseTrigger;
	private HttpContext context = null;
	
	/** Creates a new instance */
	public MsgHttp2(Stack stack) {
		super(stack);
	}

	/** Creates a new instance */
	public MsgHttp2(Stack stack, HttpMessage aMessage) throws Exception {
		super(stack, aMessage);
		
	}

    @Override
    public HttpVersion retrieveHttpVersion(String[] parts, boolean response) throws ParsingException {
    	HttpVersion httpVersion = HttpVersion.HTTP_2_0; 	

    	// Message is a response
    	if (response)
    	{
    		if(parts[0].endsWith("HTTP/1.0") || parts[0].endsWith("HTTP/1.1"))
    			throw new ParsingException("Bad HTTP Version in message (should be HTTP/2.0) : \"version:" + parts[0]);
    	} // Message is a request
    	else
    	{
    		if ((parts.length == 3) && (parts[2].endsWith("HTTP/1.0") || parts[2].endsWith("HTTP/1.1") ) )
    		{
    			throw new ParsingException("Bad HTTP Version in message (should be HTTP/2.0) : \"version:" + parts[2]);
    		}
    	}
    	return httpVersion;
    }
    
	public ResponseTrigger getResponseTrigger() {
		return responseTrigger;
	}

	public void setResponseTrigger(ResponseTrigger responseTrigger) {
		this.responseTrigger = responseTrigger;
	}

	public HttpContext getContext() {
		return context;
	}

	public void setContext(HttpContext context) {
		this.context = context;
	}
	

	@Override
	public boolean getConfig() {
    	return Config.getConfigByName("http2.properties").getBoolean("message.IGNORE_RECEIVED_CONTENTS", false);
    }

}
