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
