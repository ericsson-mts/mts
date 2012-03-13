/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.core.coding.q931;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.maps.LinkedHashMap;
import org.dom4j.Element;

/**
 *
 * @author indiaye
 */
public abstract class Field {

	private String _name;
    private int _length;
    protected int _offset;
    
    protected Dictionary dictionary; 
    
    protected ElementInformationQ931 _elem;

    public Field(Element elemField, ElementInformationQ931 elem, Dictionary dictionary) {

        _name = elemField.attributeValue("name");
        String lengthBit = elemField.attributeValue("lengthBit");
        if (lengthBit != null) {
            _length = Integer.parseInt(lengthBit);
        }
        else if(lengthBit == null && elemField.attributeValue("value") != null){
            _length = elemField.attributeValue("value").length() * 8;
        }
        else if((elemField.attributeValue("type")).equalsIgnoreCase("string"))
        {
            _length=0;
        }
        else if((elemField.attributeValue("type")).equalsIgnoreCase("binary"))
        {
            _length=0;
        }

        _elem = elem;
        this.dictionary = dictionary; 
    }

    public int getLength() {
        return _length;
    }

    public int getOffset() {
        return _offset;
    }

    public String getName() {
        return _name;
    }
     public void setLength(int _length) {
        this._length = _length;
    }

    public abstract String getValue(ElementInformationQ931V elemV)throws Exception;

    public abstract void setValue(String value, int offset, ElementInformationQ931V elemV) throws Exception;
    
    public String toString(ElementInformationQ931V elemV) {

        StringBuilder elemString = new StringBuilder();
        elemString.append("    <field ");
        elemString.append("name=\"" + getName() + "\" ");
        try
        {
        	elemString.append("value=\"" + this.getValue(elemV) + "\" ");
        }
        catch (Exception e)
        {
        	GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.CORE, e, "Exception in toString() method for field " + this._name);
        }
        elemString.append("type=\"" + this.getClass().getSimpleName().split("Field")[0] + "\" ");
        if (!this.getClass().getName().equalsIgnoreCase("String")) {
            elemString.append("lengthBit=\"" + getLength() + "\" ");
        }
        elemString.append("/>\n");
        return elemString.toString();


    }

    public LinkedHashMap<String, Integer> getHashMapEnumByName() {
        return null;
    }
}
