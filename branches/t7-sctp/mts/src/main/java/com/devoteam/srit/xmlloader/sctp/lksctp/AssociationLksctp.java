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

import com.devoteam.srit.xmlloader.sctp.AssociationSctp;

import dk.i1.sctp.*;

/**
 * @author emicpou
 * sctp_sndrcvinfo implementation object adapter 
 */
public class AssociationLksctp implements AssociationSctp{
	
	/**
	 * reference on the implementation object
	 */
	protected AssociationId sinfo_assoc_id;
	
	/**
	 * @param sinfo_assoc_id reference on the implementation object
	 */
	public AssociationLksctp( AssociationId sinfo_assoc_id ){
		this.sinfo_assoc_id = sinfo_assoc_id;
	}
		
	/**
	 * 
	 */
	@Override
	public String toString(){
		return this.sinfo_assoc_id.toString();
	}

	/**
	 * @return the local handle to the SCTP association
	 */
	@Override
	public long getId(){
		// hashCode implemented as a return value
		//return this.sinfo_assoc_id.id;
		return this.sinfo_assoc_id.hashCode();
	}
	
}
