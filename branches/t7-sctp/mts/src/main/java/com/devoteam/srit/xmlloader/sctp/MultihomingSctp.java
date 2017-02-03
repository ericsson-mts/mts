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

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

/**
 * @author emicpou
 *
 */

public class MultihomingSctp {

    /**
     * multi-homing addresses
     */
	protected final Set<InetAddress> addresses = new HashSet<InetAddress>();
	
	public MultihomingSctp(){
	}
	
	/**
	 * @return multihoming addresses (should be unmutable)
	 */
	public Set<InetAddress> getAddresses(){
		return this.addresses;
	}

}
