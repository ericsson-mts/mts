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

import com.devoteam.srit.xmlloader.sctp.AssociationIdSctp;

/**
 * @author emicpou
 *
 */
public interface SndrcvinfoSctp {
	
	/**
	 * @return the stream id value
	 */
	public short getStreamId();
	
	/**
	 * @param the stream id value
	 */
	public void setStreamId( short streamId );
	
	/**
	 * @return the ssn value
	 */
	public short getSsn();
	
	/**
	 * @param the ssn value
	 */
	public void setSsn( short ssn );
	
	/**
	 * @return the flags value
	 */
	public short getFlags();
	
	/**
	 * @param the flags value
	 */
	public void setFlags( short flags );
	
	/**
	 * @return the ppid value (little endian)
	 */
	public int getPpid();
	
	/**
	 * @param the ppid value
	 */
	public void setPpid( int ppid );
	
	/**
	 * @return the context value
	 */
	public int getContext();
	
	/**
	 * @param the context value
	 */
	public void setContext( int context );
	
	/**
	 * @return the ttl value
	 */
	public int getTtl();
	
	/**
	 * @param the ttl value
	 */
	public void setTtl( int ttl );
	
	/**
	 * @return the tsn value
	 */
	public int getTsn();
	
	/**
	 * @param the tsn value
	 */
	public void setTsn( int tsn );

	/**
	 * @return the tsn value
	 */
	public int getCumtsn();
	
	/**
	 * @param the cumtsn value
	 */
	public void setCumtsn( int cumtsn );
	
	/**
	 * @return the AssociationIdSctp value (copy or immutable reference)
	 */
	public AssociationIdSctp getAssociationId();
	
	/**
	 * @param associationId the AssociationIdSctp value
	 */
	public void setAssociationId( long associationId );
	
	/**
	 * @param associationId the AssociationIdSctp value
	 */
	public void setAssociationId( AssociationIdSctp associationId );
	
}
