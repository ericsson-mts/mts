/*
 * CommandDef.java
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
