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

import com.devoteam.srit.xmlloader.sctp.AssociationSctp;

import com.sun.nio.sctp.*;

/**
 * @author emicpou
 * 
 * Association implementation object adapter 
 *  
 * @see <a href="http://docs.oracle.com/javase/8/docs/jre/api/nio/sctp/spec/com/sun/nio/sctp/Association.html">Class MessageInfo</a>
 * 
 */
public class AssociationSunNioSctp implements AssociationSctp{
	
	/**
	 * reference on the implementation object
	 */
	protected Association association;
	
	/**
	 * @param sinfo_assoc_id reference on the implementation object
	 */
	public AssociationSunNioSctp( Association association ){
		this.association = association;
	}
		
	/**
	 * 
	 */
	@Override
	public String toString(){
		return Integer.toString( this.association.associationID() );
	}

	/**
	 * @return the local handle to the SCTP association
	 */
	@Override
	public long getId(){
		return (int)this.association.associationID();
	}
	
}
