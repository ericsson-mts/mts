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
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.protocol.*;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import java.util.List;

import org.dom4j.Element;

/**
 *
 * @author nghezzaz
 */

//  channel is called association in SCTP 
public abstract class ChannelSctp extends Channel
{
	/**
	 * 
	 */
    protected ListenpointSctp listenpointSctp;
    
    /**
     * the channel initialization parameters
     * to use when opening transport layer
     */
    protected ChannelConfigSctp configSctp;
    
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
     * @author emicpou
     * @param name
     * @param listenpointSctp
     * @throws Exception
     */
    protected ChannelSctp(String name,ListenpointSctp listenpointSctp) throws Exception
    {
        super(name);

        this.localHost = listenpointSctp.getHost();
        this.localPort = listenpointSctp.getPort();

        this.protocol = listenpointSctp.getProtocol();
        this.stack = listenpointSctp.getStack();

        this.listenpointSctp = listenpointSctp;
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

    	Config stackConfig = this.stack.getConfig();
    	
       	@SuppressWarnings("unchecked")
    	List<Element> sctpElements = root.elements("sctp");

       	this.configSctp = new ChannelConfigSctp();
    	this.configSctp.setFromStackConfig( stackConfig );
    	this.configSctp.setFromXml( sctpElements );
		
		// log datas
		GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "ChannelConfigSctp.num_ostreams=", this.configSctp.num_ostreams);			
		GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "ChannelConfigSctp.max_instreams=", this.configSctp.max_instreams);
		GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "ChannelConfigSctp.max_attempts=", this.configSctp.max_attempts);
		GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "ChannelConfigSctp.max_init_timeo=", this.configSctp.max_init_timeo);


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
        ret += this.toString_attributes();
        ret += ">\n";
        if( this.configSctp!=null ) {
        	ret += this.configSctp.toString();
        }
        return ret;
    }
    
    /**
     * should be overriden
     * @return stringified attributes
     */
    protected String toString_attributes()
    {
    	return "";
    }
   
    /**
     *  @param associationSctp optional association id (required by lksctp)
     */
    public abstract String toXml_PeerAddresses(AssociationSctp associationSctp) throws Exception;
        
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
