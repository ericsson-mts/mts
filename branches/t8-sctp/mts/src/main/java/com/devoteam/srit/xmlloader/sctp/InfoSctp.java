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

import java.util.Collection;

import javax.annotation.Nonnull;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.ParameterKey;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;

/**
 * @author emicpou
 *
 */
public abstract class InfoSctp {
	
	/**
	 * @return the stream id value
	 */
	public abstract short getStreamId();
	
	/**
	 * @param the stream id value
	 */
	public abstract void setStreamId( short streamId ) throws Exception;
	
	/**
	 * @return the ssn value
	 */
	public abstract short getSsn();
	
	/**
	 * @param the ssn value
	 */
	public abstract void setSsn( short ssn ) throws Exception;
	
	/**
	 * flags values
	 */
	static public final short FLAG_UNORDERED = 1;
	static public final short FLAG_ADDR_OVER = 2;
	static public final short FLAG_ABORT = 4;
	static public final short FLAG_EOF = 8;
	static public final short FLAG_SENDALL = 16;
	
	/**
	 * @return the flags value
	 */
	public abstract short getFlags();
	
	/**
	 * @param the flags value
	 */
	public abstract void setFlags( short flags ) throws Exception;
	
	/**
	 * @return the ppid value (little endian)
	 */
	public abstract int getPpid();
	
	/**
	 * @param the ppid value
	 */
	public abstract void setPpid( int ppid ) throws Exception;
	
	/**
	 * @return the context value
	 */
	public abstract int getContext();
	
	/**
	 * @param the context value
	 */
	public abstract void setContext( int context ) throws Exception;
	
	/**
	 * @return the ttl value
	 */
	public abstract int getTtl();
	
	/**
	 * @param the ttl value
	 */
	public abstract void setTtl( int ttl ) throws Exception;
	
	/**
	 * @return the tsn value
	 */
	public abstract int getTsn();
	
	/**
	 * @param the tsn value
	 */
	public abstract void setTsn( int tsn ) throws Exception;

	/**
	 * @return the tsn value
	 */
	public abstract int getCumtsn();
	
	/**
	 * @param the cumtsn value
	 */
	public abstract void setCumtsn( int cumtsn ) throws Exception;
	
	/**
	 * @return the AssociationSctp value (copy or immutable reference)
	 */
	public abstract AssociationSctp getAssociation();
	
	/**
	 * @param associationId the AssociationSctp value
	 */
	public abstract void setAssociationId( int associationId ) throws Exception;
	
	/**
	 * @param src the source object
	 */
	public void set( InfoSctp src ) throws Exception {
		copy( src,this );
	}

	/**
	 * @param src the source object
	 * @return status
	 */
	public boolean trySet( InfoSctp src ){
		return tryCopy( src,this );
	}
	
	/**
	 * @param dst the stream id value
	 */
	public void get( InfoSctp dst ) throws Exception {
		copy( this,dst );
	}
	
	/**
	 * @param dst the stream id value
	 * @return status
	 */
	public boolean tryGet( InfoSctp dst ){
		return tryCopy( this,dst );
	}

	
	
	
	
    /*
     * 
     */
    public void setFromStackConfig( Config stackConfig ) throws Exception
    {
    	{
    		short streamId = (short) stackConfig.getInteger("client.DEFAULT_STREAM", 1);
    		this.setStreamId(streamId);
    	}
    	{
    		short ssn = (short) stackConfig.getInteger("client.DEFAULT_SSN", 0);
    		this.setSsn(ssn);
    	}
    	{
    		short flags = (short) stackConfig.getInteger("client.DEFAULT_FLAGS", 0);
    		this.setFlags(flags);
    	}
    	{
    		int ppid = stackConfig.getInteger("client.DEFAULT_PPID", 0);
    		this.setPpid(ppid);
    	}
    	{
    		int context = stackConfig.getInteger("client.DEFAULT_CONTEXT", 0);
    		this.setContext(context);
    	}
    	{
    		int ttl = stackConfig.getInteger("client.DEFAULT_TTL", 0);
    		this.setTtl(ttl);
    	}
    	{
    		int tsn = stackConfig.getInteger("client.DEFAULT_TSN", 0);
    		this.setTsn(tsn);
    	}
    	{
    		int cumtsn = stackConfig.getInteger("client.DEFAULT_CUMTSN", 0);
    		this.setCumtsn(cumtsn);
    	}
    	{
    		int aid = stackConfig.getInteger("client.DEFAULT_AID", 0);
    		this.setAssociationId(aid);
    	}
    }

	/**
	 * 
	 */
	public void setFromSctpStackConfig() throws Exception {
	    Config sctpStackConfig = StackFactory.getStack(StackFactory.PROTOCOL_SCTP).getConfig();
	    this.setFromStackConfig( sctpStackConfig );
	}

	/**
	 * TODO check unsigned->signed conversion and overflows
	 */
	public void setFromXml(Element sctpElement) throws Exception {
		// TODO check unsigned->signed conversion and overflows
			{
				String streamIdString = sctpElement.attributeValue("stream");
				if (streamIdString != null)
				{
					short streamId = (short) Integer.parseUnsignedInt(streamIdString);
					this.setStreamId(streamId);
				}
			}
			{
				String ssnString = sctpElement.attributeValue("ssn");
				if (ssnString != null)
				{
					short ssn = (short) Integer.parseUnsignedInt(ssnString);
					this.setSsn(ssn);
				}
			}
			{
				String flagsString = sctpElement.attributeValue("flags");
				if (flagsString != null)
				{
					short flags = (short) Integer.parseUnsignedInt(flagsString);
					this.setFlags(flags);
				}
			}
			{
				String ppidString = sctpElement.attributeValue("ppid");
				if (ppidString != null)
				{
					int ppid = Integer.parseUnsignedInt(ppidString);
					this.setPpid(ppid);
				}
			}
			{
				String contextString = sctpElement.attributeValue("context");
				if (contextString != null)
				{
					int context = Integer.parseUnsignedInt(contextString);
					this.setContext(context);
				}
			}
			{
				String ttlString = sctpElement.attributeValue("ttl");
				if (ttlString != null)
				{
					int ttl = Integer.parseUnsignedInt(ttlString);
					this.setTtl(ttl);
				}
			}
			{
				String tsnString = sctpElement.attributeValue("tsn");
				if (tsnString != null)
				{
					int tsn = Integer.parseUnsignedInt(tsnString);
					this.setTsn(tsn);
				}
			}
			{
				String cumtsnString = sctpElement.attributeValue("cumtsn");
				if (cumtsnString != null)
				{
					int cumtsn = Integer.parseUnsignedInt(cumtsnString);
					this.setCumtsn(cumtsn);
				}
			}
			{
				String aidString = sctpElement.attributeValue("aid");
				if (aidString != null)
				{
					int aid = Integer.parseUnsignedInt(aidString);
					this.setAssociationId(aid);
				}
			}
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
			case "stream":
			case "ssn":
			case "ppid":
			case "flags":
			case "context":
			case "ttl":
			case "tsn":
			case "cumtsn":
			case "aid":
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
			case "stream":
				parameter.add( InfoSctp.StringPrinter.getStreamId(this) );
	    		break;
			case "ssn":
				parameter.add( InfoSctp.StringPrinter.getSsn(this) );
	    		break;
			case "ppid":
				parameter.add( InfoSctp.StringPrinter.getPpid(this) );
	    		break;
			case "flags":
				parameter.add( InfoSctp.StringPrinter.getFlags(this) );
	    		break;
			case "context":
				parameter.add( InfoSctp.StringPrinter.getContext(this) );
	    		break;
			case "ttl":
				parameter.add( InfoSctp.StringPrinter.getTtl(this) );
	    		break;
			case "tsn":
				parameter.add( InfoSctp.StringPrinter.getTsn(this) );
	    		break;
			case "cumtsn":
				parameter.add( InfoSctp.StringPrinter.getCumtsn(this) );
	    		break;
			case "aid":
				parameter.add( InfoSctp.StringPrinter.getAssociationId(this) );
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
     */
    @Override
    public boolean equals( Object object )
    {
    	if( object==null ){
    		return false;
    	}
    	if( !(object instanceof InfoSctp) ){
    		return false;
    	}
    	InfoSctp infoSctp = (InfoSctp)object;
    	
    	if( this.getStreamId() != infoSctp.getStreamId() ){
    		return false;
    	}
    	if( this.getSsn() != infoSctp.getSsn() ){
    		return false;
    	}
    	if( this.getFlags() != infoSctp.getFlags() ){
    		return false;
    	}
    	if( this.getPpid() != infoSctp.getPpid() ){
    		return false;
    	}
    	if( this.getContext() != infoSctp.getContext() ){
    		return false;
    	}
    	if( this.getTtl() != infoSctp.getTtl() ){
    		return false;
    	}
    	if( this.getTsn() != infoSctp.getTsn() ){
    		return false;
    	}
    	if( this.getCumtsn() != infoSctp.getCumtsn() ){
    		return false;
    	}
    	if( this.getAssociation().getId() != infoSctp.getAssociation().getId() ){
    		return false;
    	}
    	return true;
    }
	
	
	
	/**
	 * @param dst the stream id value
	 * @return status
	 */
	protected static boolean tryCopy( InfoSctp src,InfoSctp dst ){
		boolean status = true;
		try{
			copy( src,dst );
		}catch(Exception e){
			status = false;
		}
		return status;
	}
	
	/**
	 * @param dst the stream id value
	 * @return status
	 */
	protected static void copy( InfoSctp src,InfoSctp dst )throws Exception{
		dst.setStreamId( src.getStreamId() );
		dst.setSsn( src.getSsn() );
		dst.setFlags( src.getFlags() );
		dst.setPpid( src.getPpid() );
		dst.setContext( src.getContext() );
		dst.setTtl( src.getTtl() );
		dst.setTsn( src.getTsn() );
		dst.setCumtsn( src.getCumtsn() );
		dst.setAssociationId( src.getAssociation().getId() );
	}
	
	/**
	 * 
	 */
	public String toString(){
		return this.toXml();
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public String toXml(){
		String xml = "";

		xml += "<InfoSctp>";
		xml += System.lineSeparator();
		
		xml += "<stream>" + StringPrinter.getStreamId( this ) + "</stream>";
		xml += System.lineSeparator();
		
		xml += "<ssn>" + StringPrinter.getSsn( this ) + "</ssn>";
		xml += System.lineSeparator();

		xml += "<ppid>" + StringPrinter.getPpid( this ) + "</ppid>";
		xml += System.lineSeparator();

		xml += "<flags>" + StringPrinter.getFlags( this ) + "</flags>";
		xml += System.lineSeparator();

		xml += "<context>" + StringPrinter.getContext( this ) + "</context>";
		xml += System.lineSeparator();

		xml += "<ttl>" + StringPrinter.getTtl( this ) + "</ttl>";
		xml += System.lineSeparator();

		xml += "<tsn>" + StringPrinter.getTsn( this ) + "</tsn>";
		xml += System.lineSeparator();

		xml += "<cumtsn>" + StringPrinter.getCumtsn( this ) + "</cumtsn>";
		xml += System.lineSeparator();

		xml += "<aid>" + StringPrinter.getAssociationId( this ) + "</aid>";
		xml += System.lineSeparator();
		
		if( this.hasExtensions() ){
			xml += "<extensions>";
			xml += System.lineSeparator();
			xml += this.toXml_ExtensionsElements();
			xml += "</extensions>";
			xml += System.lineSeparator();
		}
		
		xml += "</InfoSctp>";

		return xml;
	}
	
	/**
	 * 
	 */
	protected boolean hasExtensions(){
		return false;
	}

	/**
	 * 
	 */
	protected String toXml_ExtensionsElements(){
		return "";
	}
	
	/**
	 * helper class to convert unsigned values to strings
	 * 
	 * TODO implementation based on generic
	 */
	public static class StringPrinter{
		
		static String getStreamId( InfoSctp infoSctp ){
			short usValue = infoSctp.getStreamId();
			long ulValue = Short.toUnsignedLong( usValue );
			String strValue = Long.toUnsignedString( ulValue );
			return strValue;
		}
		static String getSsn( InfoSctp infoSctp ){
			short usValue = infoSctp.getSsn();
			long ulValue = Short.toUnsignedLong( usValue );
			String strValue = Long.toUnsignedString( ulValue );
			return strValue;
		}
		static String getFlags( InfoSctp infoSctp ){
			short usValue = infoSctp.getFlags();
			long ulValue = Short.toUnsignedLong( usValue );
			String strValue = Long.toUnsignedString( ulValue );
			return strValue;
		}
		static String getPpid( InfoSctp infoSctp ){
			int uiValue = infoSctp.getPpid();
			long ulValue = Integer.toUnsignedLong( uiValue );
			String strValue = Long.toUnsignedString( ulValue );
			return strValue;
		}
		static String getContext( InfoSctp infoSctp ){
			int uiValue = infoSctp.getContext();
			long ulValue = Integer.toUnsignedLong( uiValue );
			String strValue = Long.toUnsignedString( ulValue );
			return strValue;
		}
		static String getTtl( InfoSctp infoSctp ){
			int uiValue = infoSctp.getTtl();
			long ulValue = Integer.toUnsignedLong( uiValue );
			String strValue = Long.toUnsignedString( ulValue );
			return strValue;
		}
		static String getTsn( InfoSctp infoSctp ){
			int uiValue = infoSctp.getTsn();
			long ulValue = Integer.toUnsignedLong( uiValue );
			String strValue = Long.toUnsignedString( ulValue );
			return strValue;
		}
		static String getCumtsn( InfoSctp infoSctp ){
			int uiValue = infoSctp.getCumtsn();
			long ulValue = Integer.toUnsignedLong( uiValue );
			String strValue = Long.toUnsignedString( ulValue );
			return strValue;
		}
		static String getAssociationId( InfoSctp infoSctp ){
			int uiValue = infoSctp.getAssociation().getId();
			long ulValue = Integer.toUnsignedLong( uiValue );
			String strValue = Long.toUnsignedString( ulValue );
			return strValue;
		}

	}
}
