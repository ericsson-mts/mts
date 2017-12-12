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
public class CommandDef {
    
    private int _code ;
    private String _name ;
    private VendorDef _vendor_id;
    
    /** Creates a new instance of CommandDef */
    public CommandDef(int code, String name, VendorDef vendor_id) {
        _code = code ;
        _name = name ;
        _vendor_id = vendor_id ;
    }
    
    
    public int get_code() { return _code ; }
    
    public String get_name() { return _name ; }
    
    public VendorDef get_vendor_id() { return _vendor_id ; }
}
