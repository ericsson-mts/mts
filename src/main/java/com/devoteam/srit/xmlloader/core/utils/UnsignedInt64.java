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
import java.math.BigInteger;

/**
 * Creates and instantiates an unsigned 64-bit integer object. The
 * CIMDataType class uses this class to instantiate valid CIM
 * data types. 
 *
 * @author	Sun Microsystems, Inc.
 * @since       WBEM 1.0
 */
public class UnsignedInt64 extends UnsignedInt implements Serializable, Comparable {

    final static long serialVersionUID = 200;
 
    /**
     * the maximum value this BigInteger can have
     */
    public final static BigInteger MAX_VALUE = new 
			BigInteger("18446744073709551615");
					 
    /**
     * the minimum value this BigInteger can have
     */
    public final static BigInteger MIN_VALUE = new BigInteger("0");

    /**
     * Constructor creates an unsigned 64-bit integer object for the specified
     * string. Only the bottom 64 bits are considered.
     * 
     * @param sval the String to be represented as an unsigned 64-bit integer
     *                object
     * @throws NumberFormatException if the number is out of range
     */
    public UnsignedInt64(String sval) throws NumberFormatException {
	BigInteger bigInt = new BigInteger(sval);
	if ((bigInt.compareTo(MIN_VALUE) < 0) || 
            (bigInt.compareTo(MAX_VALUE) > 0)) {
	    throw new NumberFormatException();
	}
	value = bigInt;
    }
    
    /**
     * Constructor creates an unsigned 64-bit integer object for the specified
     * array of bytes. Only the bottom 64 bits are considered.
     * 
     * @param bval the byte array to be represented as an unsigned 64-bit
     *                integer object
     * @throws NumberFormatException if the number is out of range
     */
    public UnsignedInt64(byte[] bval) throws NumberFormatException {
        BigInteger bigInt = new BigInteger(bval);
	if ((bigInt.compareTo(MIN_VALUE) < 0) || 
            (bigInt.compareTo(MAX_VALUE) > 0)) {
	    throw new NumberFormatException();
	}
	value = bigInt;
    }

    /**
     * Constructor creates an unsigned 64-bit integer object for the specified
     * BigInteger. Only the bottom 64 bits are considered.
     * 
     * @param input the BigInteger to be represented as an unsigned 64-bit
     *                integer object
     */
    public UnsignedInt64(BigInteger input) {
        BigInteger bigInt = new BigInteger(input.toString());
	if ((bigInt.compareTo(MIN_VALUE) < 0) || 
	    (bigInt.compareTo(MAX_VALUE) > 0)) {
	    throw new NumberFormatException();
	}
	value = bigInt;
    }

 
    /**
     * Compares this unsigned 64-bit integer object with the specified object
     * for equality
     * 
     * @param o the object to compare
     * @return true if the specified object is an unsigned 64-bit integer
     *         object. Otherwise, false.
     */ 
    public boolean equals(Object o) {
        if (!(o instanceof UnsignedInt64)) {
            return false;
        }
        return (((UnsignedInt64)o).value.equals(this.value));
    }

    /**
     * Gets the value as a <code>BigInteger</code>
     * 
     * @return the BigInteger representation of this object
     */
    public BigInteger bigIntValue() {
	return (BigInteger)value;
    }

    /**
     * Compares this UnsignedInt64 with the specified UnsignedInt64.  
     * This method is provided in preference to individual methods for each of 
     * the six boolean comparison operators 
     * (&lt;, ==, &gt;, &gt;=, !=, &lt;=).  The
     * suggested idiom for performing these comparisons is:
     * <tt>(x.compareTo(y)</tt> &lt;<i>op</i>&gt; <tt>0)</tt>,
     * where &lt;<i>op</i>&gt; is one of the six comparison operators.
     *
     * @param  val Object to which this UnsignedInt64 is to be compared. Throws
     *             a ClassCastException if the input object is not an
     *		   UnsignedInt64.
     * @return -1, 0 or 1 as this UnsignedInt64 is numerically less than, equal
     *         to, or greater than <tt>val</tt>.
     */
    public int compareTo(Object val) {
        
	return ((BigInteger)value).compareTo((BigInteger)((UnsignedInt64)val).value);
    }
}
