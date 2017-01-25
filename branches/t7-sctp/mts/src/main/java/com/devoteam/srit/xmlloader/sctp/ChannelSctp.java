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

package com.devoteam.srit.xmlloader.sctp;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.tcp.bio.ChannelTcpBIO;
import com.devoteam.srit.xmlloader.tcp.nio.ChannelTcpNIO;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.List;

import java.net.Socket;

import org.dom4j.Element;

/**
 *
 * @author nghezzaz
 */

//  channel is called association in SCTP 
public abstract class ChannelSctp extends Channel
{
    protected ListenpointSctp listenpointSctp;
    
    /** Creates a new instance of Channel*/
    public ChannelSctp(Stack stack)
    {
    	super(stack);
    }

    /** Creates a new instance of Channel*/
    public ChannelSctp(String name)
    {
    	super(name);
    	assert(false):"this code path is not tested";
    }

    /** Creates a new instance of Channel */
    public ChannelSctp(String localHost, int localPort, String remoteHost, int remotePort, String aProtocol) throws Exception
    {
    	super(localHost, localPort, remoteHost, remotePort, aProtocol);
    	assert(false):"this code path is not tested";
    }

    /** Creates a new instance of Channel */
    public ChannelSctp(String name, String localHost, String localPort, String remoteHost, String remotePort, String aProtocol) throws Exception
    {
    	super(name, localHost, localPort, remoteHost, remotePort, aProtocol);
    }

    public ChannelSctp(Listenpoint aListenpoint, String aLocalHost, int aLocalPort, String aRemoteHost, int aRemotePort, String aProtocol) throws Exception
    {
        super(aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        this.listenpointSctp = (ListenpointSctp)aListenpoint;
    }

    public ChannelSctp(Listenpoint aListenpoint, String name, String localHost, String localPort, String remoteHost, String remotePort, String aProtocol) throws Exception
    {
        super(name, localHost, localPort, remoteHost, remotePort, aProtocol);
        this.listenpointSctp = (ListenpointSctp)aListenpoint;
    }

    /**
     * Open a Channel
     * should be overriden
     */
    @Override
    public boolean open() throws Exception
    {
    	//does not call super.open intentionally

    	//common sctp code here...

    	return true;
    }
    
    /**
     * Close a Channel
     * should be overriden
     */
    @Override
    public boolean close()
    {	
    	//does not call super.close intentionally
    	
    	//common sctp code here...

    	return true;
    }
    
    /**
     * Send a Msg to Channel
     * should be overriden
     */
    @Override
    public boolean sendMessage(Msg msg) throws Exception {
    	//does not call super.sendMessage intentionally

    	//common sctp code here...

    	return true;
    }
    
    /** Get the transport protocol */
    @Override
    public String getTransport() 
    {
    	return StackFactory.PROTOCOL_SCTP;
    }
   
    public Listenpoint getListenpointSctp() {
      return this.listenpointSctp;
    }
	
    /** 
     * Parse the message from XML element 
     * should be overriden
     */
    @Override
    public void parseFromXml(Element root, Runner runner, String protocol) throws Exception
    {
    	super.parseFromXml(root, runner, protocol);

    	//common sctp code here...
    }
    
    //---------------------------------------------------------------------
    // methods for the XML display / parsing
    //---------------------------------------------------------------------

    /** 
     * Returns the string description of the message. Used for logging as DEBUG level 
     */
    @Override
    public String toString()
    {
        String ret = super.toString();

    	//common sctp code here...

        return ret;
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
		Parameter var = super.getParameter(path);
		if (var != null)
		{
			return var;
		}
       //common sctp code here...

       return null;
    }

}



