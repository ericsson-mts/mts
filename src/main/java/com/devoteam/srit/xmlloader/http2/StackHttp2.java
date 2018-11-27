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

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.http2.nio.ChannelHttp2NIO;
import com.devoteam.srit.xmlloader.core.protocol.Msg.ParseFromXmlContext;

/**
 *
 * @author gpasquiers
 */
public class StackHttp2 extends Stack
{
    
    /** Constructor */
    public StackHttp2() throws Exception
    {
        super();
    }
    
    /** Creates a Channel specific to each Stack */
    @Override
    public Channel parseChannelFromXml(Element root, Runner runner, String protocol) throws Exception
    {
    	Channel ch = new ChannelHttp2NIO(this);
    	System.out.println("root = " +root.asXML());

    	String name = root.valueOf("@name");
    	String remoteURL = root.valueOf("@remoteURL");
    	String[] tab =  remoteURL.split("//");
    	int size = remoteURL.split("//").length;
    	if(size > 1) {
    		String[] tab2 = tab[1].split(":");
    		ch.setRemoteHost(tab2[0]);
    		ch.setRemotePort(Integer.parseInt(tab2[1]));
    		System.out.println("host = " +tab2[0] + ", port = " + tab2[1]);
    	}
    	else {
    		throw new Exception("");
    	}
    	    	
    	System.out.println("StackHttp2.parseChannelFromXml()");
        return ch;
    }

	/** Creates a specific Msg */
    @Override
    public Msg parseMsgFromXml(ParseFromXmlContext context, Element root, Runner runner) throws Exception
    {
    	Msg msg = new MsgHttp2(this);
    	System.out.println("StackHttp2.parseMsgFromXml()");
    	return msg;
    }
     
    public synchronized boolean sendMessage(Msg msg) throws Exception
    {
    	return super.sendMessage(msg);        
    }
    
    /** Receive a message */
    @Override
    public boolean receiveMessage(Msg msg) throws Exception{
    	
    	return super.receiveMessage(msg);
    }


}
