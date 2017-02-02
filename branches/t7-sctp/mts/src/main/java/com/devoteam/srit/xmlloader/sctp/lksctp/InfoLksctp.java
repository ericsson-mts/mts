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

package com.devoteam.srit.xmlloader.sctp.lksctp;

import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.sctp.*;

import dk.i1.sctp.*;

/**
 * @author emicpou
 * sctp_sndrcvinfo implementation object adapter 
 */
public class InfoLksctp extends InfoSctp {
	
	/**
	 * reference on the implementation object
	 */
	protected sctp_sndrcvinfo sndrcvinfo;
	
	/**
	 * @param sndrcvinfo reference on the implementation object
	 */
	public InfoLksctp( sctp_sndrcvinfo sndrcvinfo ){
		this.sndrcvinfo = sndrcvinfo;
	}
	
	/**
	 * @return the stream id value
	 */
	@Override
	public short getStreamId(){
		return this.sndrcvinfo.sinfo_stream;
	}
	
	/**
	 * @param the stream id value
	 */
	@Override
	public void setStreamId( short streamId ) throws Exception{
		this.sndrcvinfo.sinfo_stream = streamId;
	}
	
	/**
	 * @return the ssn value
	 */
	@Override
	public short getSsn(){
		return this.sndrcvinfo.sinfo_ssn;
	}
	
	/**
	 * @param the ssn value
	 */
	@Override
	public void setSsn( short ssn ) throws Exception{
		this.sndrcvinfo.sinfo_ssn = ssn;
	}
	
	/**
	 * @return the flags value
	 */
	@Override
	public short getFlags(){
		return this.sndrcvinfo.sinfo_flags;
	}
	
	/**
	 * @param the flags value
	 */
	@Override
	public void setFlags( short flags ) throws Exception{
		this.sndrcvinfo.sinfo_flags = flags;
	}
	
	/**
	 * @return the ppid value (little endian)
	 */
	@Override
	public int getPpid(){
		int ppid = Utils.convertLittleBigIndian(this.sndrcvinfo.sinfo_ppid);
		return ppid;
	}
	
	/**
	 * @param the ppid value
	 */
	@Override
	public void setPpid( int ppid ) throws Exception{
		this.sndrcvinfo.sinfo_ppid = Utils.convertLittleBigIndian(ppid);
	}
	
	/**
	 * @return the context value
	 */
	@Override
	public int getContext(){
		return this.sndrcvinfo.sinfo_context;
	}
	
	/**
	 * @param the context value
	 */
	public void setContext( int context ) throws Exception{
		this.sndrcvinfo.sinfo_context = context;
	}
	
	/**
	 * @return the ttl value
	 */
	@Override
	public int getTtl(){
		return this.sndrcvinfo.sinfo_timetolive;
	}
		
	/**
	 * @param the ttl value
	 */
	@Override
	public void setTtl( int ttl ) throws Exception{
		this.sndrcvinfo.sinfo_timetolive = ttl;
	}

	/**
	 * @return the tsn value
	 */
	@Override
	public int getTsn(){
		int tsn = Utils.convertLittleBigIndian(this.sndrcvinfo.sinfo_tsn);
		return tsn;
	}
	
	
	/**
	 * @param the tsn value
	 */
	@Override
	public void setTsn( int tsn ) throws Exception{
		this.sndrcvinfo.sinfo_tsn = Utils.convertLittleBigIndian(tsn);
	}

	/**
	 * @return the tsn value
	 */
	@Override
	public int getCumtsn(){
		int cumtsn = Utils.convertLittleBigIndian(this.sndrcvinfo.sinfo_cumtsn);
		return cumtsn;
	}

	/**
	 * @param the cumtsn value
	 */
	@Override
	public void setCumtsn( int cumtsn ){
		this.sndrcvinfo.sinfo_cumtsn = Utils.convertLittleBigIndian(cumtsn);
	}	
	
	/**
	 * @return the AssociationSctp value
	 */
	@Override
	public AssociationSctp getAssociation(){
		return new AssociationLksctp(this.sndrcvinfo.sinfo_assoc_id);
	}
	
	
	/**
	 * @param associationId the AssociationSctp value
	 */
	@Override
	public void setAssociationId( int associationId ){
		// TODO check conversion
		this.sndrcvinfo.sinfo_assoc_id = new AssociationId(associationId);
	}
	
}
