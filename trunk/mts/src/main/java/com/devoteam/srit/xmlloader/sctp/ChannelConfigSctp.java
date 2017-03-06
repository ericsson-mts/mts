/* 
 * Copyright 2017 Ericsson http://www.ericsson.com
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
import com.devoteam.srit.xmlloader.core.ParameterKey;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;

import java.util.Collection;

import javax.annotation.Nonnull;

import org.dom4j.Element;

/**
 * @author emicpou
 * TODO refactor/rename
 */
public class ChannelConfigSctp{
	   
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

	/**
	 * 
	 */
	public ChannelConfigSctp()
    {
    }
    
	/**
	 * 
	 */
    public void setFromStackConfig( Config stackConfig ) throws Exception
    {
		// initialize from the configuration file
    	this.num_ostreams = (short) stackConfig.getInteger("connect.NUM_OSTREAMS");
    	this.max_instreams = (short) stackConfig.getInteger("connect.MAX_INSTREAMS");
    	this.max_attempts = (short) stackConfig.getInteger("connect.MAX_ATTEMPTS");
    	this.max_init_timeo= (short) stackConfig.getInteger("connect.MAX_INIT_TIMEO");
    }

	/**
	 * 
	 */
	public void setFromSctpStackConfig() throws Exception {
	    Config sctpStackConfig = StackFactory.getStack(StackFactory.PROTOCOL_SCTP).getConfig();
	    this.setFromStackConfig( sctpStackConfig );
	}

    /**
     * 
     */
    public void setFromXml(Element sctpElement) throws Exception
    {
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
    
	/**
	 * 
	 */
	public void parseFromXml(Collection<Element> sctpElements) throws Exception {
	    this.setFromSctpStackConfig();
	    for( Element sctpElement:sctpElements ){
		    this.setFromXml(sctpElement);
	    }	    
	}    
	
	/**
	 * TODO refactor
	 */
	public static boolean isParameterHeadSubkeyValid(ParameterKey parameterKey){
		try{
			String headSubkey = parameterKey.getHeadSubkey();
			switch( headSubkey ){
			case "num_ostreams":
			case "max_instreams":
			case "max_attempts":
			case "max_init_timeo":
				return true;
			}
		}catch(Exception exception){
			//nothing special to do
		}
		return false;
	}
    
	/**
	 * 
	 */
	@Nonnull
	public Parameter getParameter(ParameterKey parameterKey) throws ParameterException {
		Parameter parameter = new Parameter();
		try{
			String headSubkey = parameterKey.getHeadSubkey();
			switch( headSubkey ){
			case "num_ostreams":
				parameter.add(  Long.toUnsignedString( Short.toUnsignedLong(this.num_ostreams) ) );
	    		break;
			case "max_instreams":
				parameter.add(  Long.toUnsignedString( Short.toUnsignedLong(this.max_instreams) ) );
	    		break;
			case "max_attempts":
				parameter.add(  Long.toUnsignedString( Short.toUnsignedLong(this.max_attempts) ) );
	    		break;
			case "max_init_timeo":
				parameter.add(  Long.toUnsignedString( Short.toUnsignedLong(this.max_init_timeo) ) );
	    		break;
	    	default:
	    		Parameter.throwBadPathKeywordException( parameterKey );
			}
		}catch(Exception exception){
			if( exception instanceof ParameterException ){
				throw exception;
			}
			else{
				throw new ParameterException( "",exception );
			}
		}
		return parameter;
	}

    /** 
     * Returns the string description of the message. Used for logging as DEBUG level 
     */
    public String toString()
    {
        String ret = "";

		ret += "<ChannelConfigSctp";
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
    
    /**
     * 
     */
    @Override
    public boolean equals( Object object )
    {
    	if( object==null ){
    		return false;
    	}
    	if( !(object instanceof ChannelConfigSctp) ){
    		return false;
    	}
    	ChannelConfigSctp channelConfigSctp = (ChannelConfigSctp)object;
    	
    	if( this.num_ostreams != channelConfigSctp.num_ostreams ){
    		return false;
    	}
    	if( this.max_instreams != channelConfigSctp.max_instreams ){
    		return false;
    	}
    	if( this.max_attempts != channelConfigSctp.max_attempts ){
    		return false;
    	}
    	if( this.max_init_timeo != channelConfigSctp.max_init_timeo ){
    		return false;
    	}
    	return true;
    }


}
