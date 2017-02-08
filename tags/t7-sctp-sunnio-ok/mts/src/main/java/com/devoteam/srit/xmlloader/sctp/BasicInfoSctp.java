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

import java.util.List;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.utils.Config;

/**
 * An implementation of InfoSctp which stores informations in memory
 *
 * @author emicpou
 */
public class BasicInfoSctp extends InfoSctp {
	
	protected short streamId = -1;
	protected short ssn = -1;
	protected short flags = -1;
	protected int ppid = -1;
	protected int context = -1;
	protected int timetolive = -1;
	protected int tsn = -1;
	protected int cumtsn = -1;
	protected BasicAssociationSctp association = new BasicAssociationSctp(-1);
	
	/**
	 */
	public BasicInfoSctp(){
	}
	
	/**
	 * @return the stream id value
	 */
	@Override
	public short getStreamId(){
		return this.streamId;
	}
	
	/**
	 * @param the stream id value
	 */
	public void setStreamId( short streamId ) throws Exception{
		this.streamId = streamId;
	}
	
	/**
	 * @return the ssn value
	 */
	@Override
	public short getSsn(){
		return this.ssn;
	}
	
	/**
	 * @param the ssn value
	 */
	@Override
	public void setSsn( short ssn ) throws Exception{
		this.ssn = ssn;
	}
	
	/**
	 * @return the flags value
	 */
	@Override
	public short getFlags(){
		return this.flags;
	}
	
	/**
	 * @param the flags value
	 */
	@Override
	public void setFlags( short flags ) throws Exception{
		this.flags = flags;
	}
	
	/**
	 * @return the ppid value (little endian)
	 */
	@Override
	public int getPpid(){
		return this.ppid;
	}
	
	/**
	 * @param the ppid value
	 */
	@Override
	public void setPpid( int ppid ) throws Exception{
		this.ppid = ppid;
	}
	
	/**
	 * @return the context value
	 */
	@Override
	public int getContext(){
		return this.context;
	}
	
	/**
	 * @param the context value
	 */
	public void setContext( int context ) throws Exception{
		this.context = context;
	}
	
	/**
	 * @return the ttl value
	 */
	@Override
	public int getTtl(){
		return this.timetolive;
	}
		
	/**
	 * @param the ttl value
	 */
	@Override
	public void setTtl( int ttl ) throws Exception{
		this.timetolive = ttl;
	}

	/**
	 * @return the tsn value
	 */
	@Override
	public int getTsn(){
		return this.tsn;
	}
	
	
	/**
	 * @param the tsn value
	 */
	@Override
	public void setTsn( int tsn ) throws Exception{
		this.tsn = tsn;
	}

	/**
	 * @return the tsn value
	 */
	@Override
	public int getCumtsn(){
		return this.cumtsn;
	}

	/**
	 * @param the cumtsn value
	 */
	@Override
	public void setCumtsn( int cumtsn ) throws Exception{
		this.cumtsn = cumtsn;
	}	
	
	/**
	 * @return the AssociationSctp value
	 */
	@Override
	public AssociationSctp getAssociation(){
		return this.association;
	}
	
	
	/**
	 * @param associationId the AssociationSctp value
	 */
	@Override
	public void setAssociationId( int associationId ){
		this.association.setId(associationId);
	}
	
    /*
     * 
     */
    public void setFromStackConfig( Config stackConfig ) throws Exception
    {
    	this.streamId = (short) stackConfig.getInteger("client.DEFAULT_STREAM", 1);
    	this.ssn = (short) stackConfig.getInteger("client.DEFAULT_SSN", 0);
    	this.flags = (short) stackConfig.getInteger("client.DEFAULT_FLAGS", 0);
    	this.ppid = stackConfig.getInteger("client.DEFAULT_PPID", 0);
    	this.context = stackConfig.getInteger("client.DEFAULT_CONTEXT", 0);
    	this.timetolive = stackConfig.getInteger("client.DEFAULT_TTL", 0);
    	this.tsn = stackConfig.getInteger("client.DEFAULT_TSN", 0);
    	this.cumtsn = stackConfig.getInteger("client.DEFAULT_CUMTSN", 0);
    	this.association.setId( stackConfig.getInteger("client.DEFAULT_AID", 0) );
    }

    /*
     * 
     */
    public void setFromXml(List<Element> sctpElements) throws Exception
    {
		// TODO check unsigned->signed conversion and overflows

    	if (sctpElements != null && sctpElements.size() > 0)
		{
			Element sctpElement = sctpElements.get(0);
	        
			String stream = sctpElement.attributeValue("stream");
			if (stream != null)
			{
				this.streamId = (short) Integer.parseUnsignedInt(stream);
			}
			
			String ssn = sctpElement.attributeValue("ssn");
			if (ssn != null)
			{
				this.ssn = (short) Integer.parseUnsignedInt(ssn);
			}
			
			String flags = sctpElement.attributeValue("flags");
			if (flags != null)
			{
				this.flags = (short) Integer.parseUnsignedInt(flags);
			}
			
			String ppid = sctpElement.attributeValue("ppid");
			if (ppid != null)
			{
				this.ppid = Integer.parseUnsignedInt(ppid);
			}
			
			String context = sctpElement.attributeValue("context");
			if (context != null)
			{
				this.context = Integer.parseUnsignedInt(context);
			}
			
			String ttl = sctpElement.attributeValue("ttl");
			if (ttl != null)
			{
				this.timetolive = Integer.parseUnsignedInt(ttl);
			}
			
			String tsnString = sctpElement.attributeValue("tsn");
			if (tsnString != null)
			{
				this.tsn = Integer.parseUnsignedInt(tsnString);
			}
			
			String cumtsnString = sctpElement.attributeValue("cumtsn");
			if (cumtsnString != null)
			{
				this.cumtsn = Integer.parseUnsignedInt(cumtsnString);
			}

			String aidString = sctpElement.attributeValue("aid");
			if (aidString != null)
			{
				this.association.setId( Integer.parseUnsignedInt(aidString) );
			}
		}
    }

}
