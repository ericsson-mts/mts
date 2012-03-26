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
 * Creates and instantiates an unsigned integer object
 * 
 * @since WBEM 1.0
 */
abstract class UnsignedInt extends Number implements Serializable {

    final static long serialVersionUID = 200;

    /**
     * The value for this unsigned integer.
     * 
     * @serial
     */
    protected Number value;

    /**
     * Returns the value of this unsigned integer object as a byte
     * 
     * @return the byte value of this unsigned integer object
     */
    public byte byteValue() {
        return value.byteValue();
    }

    /**
     * Returns the value of this unsigned integer object as a short
     * 
     * @return value of this unsigned integer object as a short
     */
    public short shortValue() {
        return value.shortValue();
    }

    /**
     * Returns the value of this unsigned integer object as an int
     * 
     * @return value of this unsigned integer object as an int
     */
    public int intValue() {
        return value.intValue();
    }

    /**
     * Returns the value of this unsigned integer object as a long
     * 
     * @return value of this unsigned integer object as a long
     */
    public long longValue() {
        return value.longValue();
    }

    /**
     * Returns the value of this unsigned integer object as a float
     * 
     * @return value of this unsigned integer object as a float
     */
    public float floatValue() {
        return value.floatValue();
    }

    /**
     * Returns the value of this unsigned integer object as a double
     * 
     * @return value of this unsigned integer object as a double
     */
    public double doubleValue() {
        return value.doubleValue();
    }

    /**
     * Returns the text representation of this unsigned integer object
     * 
     * @return text representation of this unsigned integer
     */
    public String toString() {
        return value.toString();
    }

    /**
     * Computes the hash code for this unsigned integer object
     * 
     * @return the integer representing the hash code for this unsigned
     *         integer object
     */
    public int hashCode() {
        return value.hashCode();
    }

    /**
     * Compares this unsigned integer object with the specified object
     * for equality
     * 
     * @param o the object to compare
     * @return true if the specified object is an unsigned 8-bit
     *         integer object. Otherwise, false.
     */
    public boolean equals(Object o) {
        if (!(o instanceof UnsignedInt)) {
            return false;
        }
        return (((UnsignedInt) o).value.equals(this.value));
    }
}
