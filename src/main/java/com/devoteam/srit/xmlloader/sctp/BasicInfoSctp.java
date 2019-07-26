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

/**
 * An implementation of InfoSctp which stores informations in memory
 *
 * @author emicpou
 */
public class BasicInfoSctp extends InfoSctp {
	
	protected short streamId = 1;
	protected short ssn = 0;
	protected short flags = 0;
	protected int ppid = 0;
	protected int context = 0;
	protected int timetolive = 0;
	protected int tsn = 0;
	protected int cumtsn = 0;
	protected BasicAssociationSctp association = new BasicAssociationSctp(0);
	
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
	
}
