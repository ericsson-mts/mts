/*
 * TypeDef.java
 *
 * Created on 26 mars 2007, 14:09
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
