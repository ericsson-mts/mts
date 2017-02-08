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
 * @author ma007141
 *
*/
class Option {
	
	private int id;
	
	private byte[] value;
	
	public Option(int i, byte[] v) {
		id = i;
		value = new byte[v.length];
		System.arraycopy(v, 0,value,0,v.length);
	}
	
	public int getId() {
		return id;
	}
	
	public byte[] getValue() {
		return value;
	}
	
	public int getLength() {
		return value.length+2;
	}
	
}
