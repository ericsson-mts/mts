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

package com.devoteam.srit.xmlloader.sctp.sunnio;

import com.devoteam.srit.xmlloader.sctp.InfoSctp;
import com.devoteam.srit.xmlloader.sctp.AssociationSctp;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import com.sun.nio.sctp.*;


/**
 * @author emicpou
 * 
 * MessageInfo implementation object adapter
 * wrapper/adapter design pattern
 *
 * @see <a href="http://docs.oracle.com/javase/8/docs/jre/api/nio/sctp/spec/com/sun/nio/sctp/MessageInfo.html">Class MessageInfo</a>
 * 
 */
public class InfoSunNioSctp extends InfoSctp {
	
	/**
	 * adaptee reference
	 */
    protected MessageInfo messageInfo;
	
	/**
	 * @param messageInfo reference on the implementation object
	 */
	public InfoSunNioSctp( MessageInfo messageInfo ){
		this.messageInfo = messageInfo;
		assert(this.messageInfo!=null);
	}
	
	/**
	 * @return the stream id value
	 */
	@Override
	public short getStreamId(){
		int streamNumber = this.messageInfo.streamNumber();
		// TODO check and throw InvalidVAlueException
		//assert( (Short.MIN_VALUE<=streamNumber)&&(streamNumber<=Short.MAX_VALUE) );
		short result = (short)streamNumber;
		return result;
	}
	
	/**
	 * @param the stream id value
	 */
	@Override
	public void setStreamId( short streamId ) throws Exception{
		int streamNumber = Short.toUnsignedInt(streamId);
		this.messageInfo.streamNumber(streamNumber);
	}
	
	/**
	 * @return the ssn value
	 */
	@Override
	public short getSsn(){
		//not available!
		return 0;
	}
	
	/**
	 * @param the ssn value
	 */
	@Override
	public void setSsn( short ssn ) throws Exception{
	}
	
	/**
	 * @return the flags value
	 */
	@Override
	public short getFlags(){
		short flags = 0;
		if( this.messageInfo.isUnordered() ){
		  flags |= FLAG_UNORDERED;
		}
		return flags;
	}
	
	/**
	 * @param the flags value
	 */
	@Override
	public void setFlags( short flags ) throws Exception{
		boolean unordered = ((flags&FLAG_UNORDERED)!=0);
		this.messageInfo.unordered( unordered );
	}
	
	/**
	 * @return the ppid value (little endian)
	 */
	@Override
	public int getPpid(){
		return this.messageInfo.payloadProtocolID();
	}
	
	/**
	 * @param the ppid value
	 */
	@Override
	public void setPpid( int ppid ) throws Exception{
		this.messageInfo.payloadProtocolID( ppid );
	}
	
	/**
	 * @return the context value
	 */
	@Override
	public int getContext(){
		//not available!
		return 0;
	}
	
	/**
	 * @param the context value
	 */
	@Override
	public void setContext( int context ) throws Exception{
	}
	
	/**
	 * @return the ttl value
	 */
	@Override
	public int getTtl(){
		long timeToLive = this.messageInfo.timeToLive();
		// TODO check and throw InvalidVAlueException
		//assert( (Integer.MIN_VALUE<=timeToLive)&&(timeToLive<=Integer.MAX_VALUE) );
		int result = (int)timeToLive;
		return result;
	}
		
	/**
	 * @param the ttl value
	 */
	@Override
	public void setTtl( int ttl ) throws Exception{
		long timeToLive = Integer.toUnsignedLong(ttl);
		this.messageInfo.timeToLive( timeToLive );
	}

	/**
	 * @return the tsn value
	 */
	@Override
	public int getTsn(){
		return this.messageInfo.streamNumber();
	}
	
	
	/**
	 * @param the tsn value
	 */
	@Override
	public void setTsn( int tsn ) throws Exception{
		this.messageInfo.streamNumber( tsn );
	}

	/**
	 * @return the cumtsn value
	 */
	@Override
	public int getCumtsn(){
		//not available!
		return 0;
	}

	/**
	 * @param the cumtsn value
	 */
	@Override
	public void setCumtsn( int cumtsn ) throws Exception{
	}	
	
	/**
	 * @return the AssociationSctp value
	 */
	@Override
	public AssociationSctp getAssociation(){
		return new AssociationSunNioSctp(this.messageInfo.association());
	}
	
	/**
	 * @param associationId the AssociationSctp value
	 */
	@Override
	public void setAssociationId( int associationId ) throws Exception{
		//the association id is immutable!
		//it is a parameter of the static factory MessageInfo.createOutgoing(Association association, SocketAddress address, int streamNumber)
		assert( this.messageInfo.association().associationID() == associationId );
	}
	
	/**
	 * 
	 */
	protected boolean hasExtensions(){
		return true;
	}

	/**
	 * 
	 */
	protected String toXml_ExtensionsElements(){
		String xml = "";
		if( this.messageInfo.address()!=null ){
    		SocketAddress socketAddress = this.messageInfo.address();
    		if( (socketAddress!=null) && (socketAddress instanceof InetSocketAddress) ){
    			InetSocketAddress inetSocketAddress = (InetSocketAddress)socketAddress;
    			xml += "<address ";
    			xml += "host=\""+inetSocketAddress.getAddress().getHostAddress()+"\" ";
    			xml += "port=\""+inetSocketAddress.getPort()+"\" ";
    			xml += "/>";
    			xml += System.lineSeparator();
    		}
		}
		return xml;
	}

}
