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

package com.devoteam.srit.xmlloader.diameter.dictionary;

/**
 *
 * @author gpasquiers
 */
public class VendorDef {
    // required
    private int    _code ;
    private String _vendor_id;
 
    // optional
    private String _name;

    /** Creates a new instance of VendorDef */
    public VendorDef(int code, String vendor_id, String name) {
        _code = code ;
        _vendor_id = vendor_id ;
        _name = name ;
    }
    
    public int get_code(){ return _code ; }
    
    public String get_vendor_id(){ return _vendor_id ; }
    
    public String get_name(){ return _name ; }
}
