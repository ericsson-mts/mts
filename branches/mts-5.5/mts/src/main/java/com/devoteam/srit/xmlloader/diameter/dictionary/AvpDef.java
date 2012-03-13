/*
 * AvpDef.java
 *
 * Created on 26 mars 2007, 14:09
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.diameter.dictionary;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author gpasquiers
 */
public class AvpDef {
    
    // mandatory field
    String _name ;
    int _code ;
    TypeDef _type ;
    
    // optionnal fields
    String _description ;
    String _may_encrypt ;
    String _mandatory ;
    String _protected ;
    String _vendor_bit ;
    boolean _constrained ;
    
    // enum hashmap
    HashMap<String, String> enumNameByCode ;
    HashMap<String, String> enumCodeByName ;
    
    // names of grouped avps
    LinkedList<String> groupedAvpNames ;
    
    // grouped avp hashmap
    HashMap<String, AvpDef> groupedAvpByName ;
    HashMap<String, AvpDef> groupedAvpByCode ;
    
    VendorDef _vendor_id ;
    
    /** Creates a new instance of AvpDef */
    public AvpDef(String name, int code, String description, String may_encrypt,
            String protected_, String vendor_bit, String mandatory,
            boolean constrained, TypeDef type, VendorDef vendor_id) {
        _name = name ;
        _code = code ;
        _may_encrypt = may_encrypt ;
        _mandatory = mandatory ;
        _protected = protected_ ;
        _vendor_bit = vendor_bit ;
        _constrained = constrained ;
        _type = type ;
        _vendor_id = vendor_id ;
        
        groupedAvpByName = new HashMap();
        groupedAvpByCode = new HashMap();
        enumCodeByName = new HashMap();
        enumNameByCode = new HashMap();
        
        groupedAvpNames = new LinkedList();
    }
    
    public void addGroupedAvpName(String name){
        groupedAvpNames.add(name);
    }
    
    public LinkedList<String> getGroupedAvpNameList(){
        return groupedAvpNames ;
    }
    
    public void addEnum(String name, int code){
        enumNameByCode.put(Integer.toString(code), name) ;
        enumCodeByName.put(name, Integer.toString(code)) ;
    }
    
    public boolean isEnumerated(){
        if(enumCodeByName.size() > 0) return true ;
        return false;
    }
    
    public long getEnumCodeByName(String name){
        String codeStr = (String) enumCodeByName.get(name);
        if(codeStr == null) return -1 ;
        else                return Long.parseLong(codeStr);
    }
    
    public String getEnumNameByCode(String code){
        return enumNameByCode.get(code);
    }
    
    public void addGroupedAvpDef(AvpDef avpDef){
        groupedAvpByName.put(avpDef.get_name(), avpDef);
        groupedAvpByCode.put(Integer.toString(avpDef.get_code()), avpDef);
    }
    
    public AvpDef getGroupedAvpDefByName(String name){
        return groupedAvpByName.get(name);
    }
    
    public AvpDef getGroupedAvpDefByCode(int code){
        return groupedAvpByName.get(Integer.toString(code));
    }
    
    public int get_code(){
        return _code ;
    }
    
    public String get_name(){
        return _name ;
    }
    
    public String get_description(){
        return _description ;
    }
    
    public String get_may_encrypt(){
        return _may_encrypt ;
    }
    
    public String get_mandatory(){
        return _mandatory ;
    }
    
    public String get_protected(){
        return _protected ;
    }
    
    public String get_vendor_bit(){
        return _vendor_bit ;
    }
    
    public boolean get_constrained(){ 
        return _constrained ;
    }
    
    public VendorDef get_vendor_id(){
        return _vendor_id ;
    }
    
    public TypeDef get_type(){
        return _type ;
    }
}
