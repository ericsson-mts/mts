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

package com.devoteam.srit.xmlloader.core.utils;

import java.io.Serializable;

/**
 * Creates and instantiates an unsigned 32-bit integer object. The
 * CIMDataType class uses this class to instantiate valid CIM
 * data types. 
 *
 * @author	Sun Microsystems, Inc.
 * @since       WBEM 1.0
 */
public class UnsignedInt32 extends UnsignedInt implements Serializable {

    final static long serialVersionUID = 200;

    /**
     * the maximum value this long can have
     */
    public final static long MAX_VALUE = 0xffffffffL;
    
    /**
     * the minimum value this long can have
     */   
    public final static long MIN_VALUE = 0;

    /**
     * Constructor creates an unsigned 32-bit integer object for 
     * the specified long value. Only the bottom 32 bits are 
     * considered. 
     *
     * @param value the long to be represented as an unsigned 32-bit integer
     *                object
     */
    public UnsignedInt32(long value) {
	if ((value < MIN_VALUE) || (value > MAX_VALUE)) {
	    throw new NumberFormatException();
	}
	this.value = new Long(value);
    }

    /**
     * Constructor creates an unsigned 32-bit integer object for the specified
     * string. Only the bottom 32 bits are considered.
     * 
     * @param value the string to be represented as an unsigned 32-bit integer
     * @throws NumberFormatException if the number is out of range
     */
    public UnsignedInt32(String value) throws NumberFormatException {
	Long temp = new Long(value);
	long longValue = temp.longValue();
	if ((longValue < MIN_VALUE) || (longValue > MAX_VALUE)) {
	    throw new NumberFormatException();
	}
	this.value = temp;
    }

    /**
     * Compares this unsigned 32-bit integer object with the specified object
     * for equality
     * 
     * @param o the object to compare
     * @return boolen true if the specified object is an unsigned 32-bit
     *         integer. Otherwise, false.
     */
    public boolean equals(Object o) {
	if (!(o instanceof UnsignedInt32)) {
	    return false;
        }
	return (((UnsignedInt32)o).value.equals(this.value));
    }
}
