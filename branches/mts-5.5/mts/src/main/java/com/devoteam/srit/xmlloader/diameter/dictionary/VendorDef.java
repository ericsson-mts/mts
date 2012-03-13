/*
 * VendorDef.java
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
