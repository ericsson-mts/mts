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

import com.devoteam.srit.xmlloader.sctp.SndrcvinfoSctp;
import com.devoteam.srit.xmlloader.sctp.AssociationIdSctp;

import dk.i1.sctp.AssociationId;
import dk.i1.sctp.sctp_sndrcvinfo;

/**
 * @author emicpou
 * sctp_sndrcvinfo implementation object adapter 
 */
public class SndrcvinfoLksctp implements SndrcvinfoSctp {
	
	/**
	 * reference on the implementation object
	 */
	sctp_sndrcvinfo sndrcvinfo;
	
	/**
	 * @param sndrcvinfo reference on the implementation object
	 */
	SndrcvinfoLksctp( sctp_sndrcvinfo sndrcvinfo ){
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
	public void setStreamId( short streamId ){
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
	public void setSsn( short ssn ){
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
	public void setFlags( short flags ){
		this.sndrcvinfo.sinfo_flags = flags;
	}
	
	/**
	 * @return the ppid value (little endian)
	 */
	@Override
	public int getPpid(){
		return this.sndrcvinfo.sinfo_ppid;
	}
	
	/**
	 * @param the ppid value
	 */
	@Override
	public void setPpid( int ppid ){
		this.sndrcvinfo.sinfo_ppid = ppid;
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
	public void setContext( int context ){
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
	public void setTtl( int ttl ){
		this.sndrcvinfo.sinfo_timetolive = ttl;
	}

	/**
	 * @return the tsn value
	 */
	@Override
	public int getTsn(){
		return this.sndrcvinfo.sinfo_tsn;
	}
	
	
	/**
	 * @param the tsn value
	 */
	@Override
	public void setTsn( int tsn ){
		this.sndrcvinfo.sinfo_tsn = tsn;
	}

	/**
	 * @return the tsn value
	 */
	@Override
	public int getCumtsn(){
		return this.sndrcvinfo.sinfo_cumtsn;
	}

	/**
	 * @param the cumtsn value
	 */
	@Override
	public void setCumtsn( int cumtsn ){
		this.sndrcvinfo.sinfo_cumtsn = cumtsn;
	}	
	
	/**
	 * @return the AssociationIdSctp value
	 */
	@Override
	public AssociationIdSctp getAssociationId(){
		return new AssociationIdLksctp(this.sndrcvinfo.sinfo_assoc_id);
	}
	
	
	/**
	 * @param associationId the AssociationIdSctp value
	 */
	@Override
	public void setAssociationId( long associationId ){
		this.sndrcvinfo.sinfo_assoc_id = new AssociationId(associationId);
	}
	
	/**
	 * @param associationId the AssociationIdSctp value
	 */
	@Override
	public void setAssociationId( AssociationIdSctp associationId ){
	}
	
}
