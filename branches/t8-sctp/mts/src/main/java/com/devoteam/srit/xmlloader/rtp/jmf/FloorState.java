/* 
 * Copyright 2012 Devoteam http://www.devoteam.com
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

package com.devoteam.srit.xmlloader.rtp.jmf;

/**
 * @author pn007888
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FloorState {
    
    int state;
    private FloorState(int i) {
        state=i;
    }
    
    public boolean equals(Object o) {
        return (o instanceof FloorState) && (((FloorState)o).state == state);
    }
	/**
	 * State "has no floor"
	 */
	public final static FloorState HAS_NO_FLOOR = new FloorState(0);

	/**
	 * State "request pending"
	 */
	public final static FloorState REQ_PENDING = new FloorState(1);

	/**
	 * State "has floor"
	 */
	public final static FloorState HAS_FLOOR = new FloorState(2);

	/**
	 * State "release pending"
	 */
	public final static FloorState REL_PENDING = new FloorState(3);
}
