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

    	this.configSctp = new ChannelConfigSctp();
    	this.configSctp.setFromStackConfig( this.stack.getConfig() );
    	this.configSctp.setFromXml( root );
    	
		
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
     * Convert the message sub elements to XML document
     * @see toString
     */
    protected String toXml_SubElements() throws Exception 
    {
    	return "";
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
    
    protected static class ChannelConfigSctp{
    
    	/**This is an integer number representing the number of streams that the
    	 *application wishes to be able to send to.  This number is confirmed
    	 *in the SCTP_COMM_UP notification and must be verified since it is a
    	 *negotiated number with the remote endpoint.  The default value of 0
    	 *indicates to use the endpoint default value.
    	 */
    	public short num_ostreams = 0;
    	
    	/**This value represents the maximum number of inbound streams the
    	 *application is prepared to support.  This value is bounded by the
    	 *actual implementation.  In other words the user MAY be able to
    	 *support more streams than the Operating System.  In such a case, the
    	 *Operating System limit overrides the value requested by the user.
    	 *The default value of 0 indicates to use the endpoints default value.
    	*/
    	public short max_instreams = 0;
    	
    	/**This integer specifies how many attempts the SCTP endpoint should
    	 *make at resending the INIT.  This value overrides the system SCTP
    	 *'Max.Init.Retransmits' value.  The default value of 0 indicates to
    	 *use the endpoints default value.  This is normally set to the
    	 *system's default 'Max.Init.Retransmit' value.
    	*/
    	public short max_attempts = 0;
    	
    	/**This value represents the largest Time-Out or RTO value (in
    	 *milliseconds) to use in attempting an INIT.  Normally the 'RTO.Max'
    	 *is used to limit the doubling of the RTO upon timeout.  For the INIT
    	 *message this value MAY override 'RTO.Max'.  This value MUST NOT
    	 *influence 'RTO.Max' during data transmission and is only used to
    	 *bound the initial setup time.  A default value of 0 indicates to use
    	 *the endpoints default value.  This is normally set to the system's
    	 *'RTO.Max' value (60 seconds).
    	*/
    	public short max_init_timeo = 0;
	    	
	    public ChannelConfigSctp()
	    {
	    }
	    
	    public void setFromStackConfig( Config stackConfig ) throws Exception
	    {
			// initialize from the configuration file
	    	this.num_ostreams = (short) stackConfig.getInteger("connect.NUM_OSTREAMS");
	    	this.max_instreams = (short) stackConfig.getInteger("connect.MAX_INSTREAMS");
	    	this.max_attempts = (short) stackConfig.getInteger("connect.MAX_ATTEMPTS");
	    	this.max_init_timeo= (short) stackConfig.getInteger("connect.MAX_INIT_TIMEO");
	    }

        /*
         * 
         */
	    public void setFromXml(Element root) throws Exception
	    {
			// Parse the XML file
	    	@SuppressWarnings("unchecked")
			List<Element> sctpElements = root.elements("sctp");
			if (sctpElements != null && sctpElements.size() > 0)
			{
				Element sctpElement = sctpElements.get(0);
		        
				String num_ostreams = sctpElement.attributeValue("num_ostreams");
				if (num_ostreams != null)
				{
					this.num_ostreams = (short) Integer.parseInt(num_ostreams);	    	
				}
		
				String max_instreams = sctpElement.attributeValue("max_instreams");
		    	if (max_instreams != null)
		    	{
		    		this.max_instreams = (short) Integer.parseInt(max_instreams);
		    	}
				
		    	String max_attempts = sctpElement.attributeValue("max_attempts");
				if (max_attempts != null)
				{
					this.max_attempts = (short) Integer.parseInt(max_attempts);    			
				}
				
				String max_init_timeo = sctpElement.attributeValue("max_initTimeo");
				if (max_init_timeo != null)
				{
					this.max_init_timeo= (short) Integer.parseInt(max_init_timeo);    			
				}
			}
	    }
	    
       /** 
         * Returns the string description of the message. Used for logging as DEBUG level 
         */
        public String toString()
        {
            String ret = "";

			ret += "<sctp";
			int numOutstreams = this.num_ostreams & 0xffff;
			ret += " num_ostreams=\"" + numOutstreams + "\"";
			int maxInstreams = this.max_instreams & 0xffff;
			ret += " max_instreams=\"" + maxInstreams + "\"";
			int maxAttempts = this.max_attempts & 0xffff;
			ret += " max_attempts=\"" + maxAttempts+ "\"";
			int maxInitTimeo = this.max_init_timeo & 0xffff;
			ret += " max_initTimeo=\"" + maxInitTimeo+ "\"";
			ret += "/>\n";
			
			return ret;
		}	

    }

}



