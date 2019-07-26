/* 
 * Copyright 2017 Orange http://www.orange.com
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
package com.devoteam.srit.xmlloader.core.groovy;

/**
 * This class is used to add optional attributes to a declared MTS parameter.
 * current supported attributes are :
 * <ul>
 * <li>editable</li>
 * </ul>
 *
 * @author mickael.jezequel@orange.com
 *
 */
public class TestParameter {

    Object value;

    boolean editable;

    /**
     * constructor
     *
     * @param value the MTS parameter value
     * @param editable flag for editable parameters
     */
    public TestParameter(Object value, boolean editable) {
        this.value = value;
        this.editable = editable;
    }

    public Object getValue() {
        return value;
    }

    public boolean isEditable() {
        return editable;
    }

}
