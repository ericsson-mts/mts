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
public class TypeDef {
    
    private String _type_name ;
    
    private TypeDef _type_parent ;
    
    /** Creates a new instance of TypeDef */
    public TypeDef(String type_name, TypeDef type_parent) {
        _type_name = type_name;
        _type_parent = type_parent;
    }
    
    public String get_type_name() { return _type_name ; }
    
    public TypeDef get_type_parent() { return _type_parent ; }
}
