/*
 *EXHIBIT A - Sun Industry Standards Source License
 *
 *"The contents of this file are subject to the Sun Industry
 *Standards Source License Version 1.2 (the "License");
 *You may not use this file except in compliance with the
 *License. You may obtain a copy of the 
 *License at http://wbemservices.sourceforge.net/license.html
 *
 *Software distributed under the License is distributed on
 *an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either
 *express or implied. See the License for the specific
 *language governing rights and limitations under the License.
 *
 *The Original Code is WBEM Services.
 *
 *The Initial Developer of the Original Code is:
 *Sun Microsystems, Inc.
 *
 *Portions created by: Sun Microsystems, Inc.
 *are Copyright (c) 2001 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): Brian Schlosser
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
