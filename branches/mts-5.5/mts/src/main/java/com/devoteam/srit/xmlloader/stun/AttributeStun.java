/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.stun;

import com.devoteam.srit.xmlloader.core.Parameter;
import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.Integer16Array;
import gp.utils.arrays.SupArray;

/**
 *
 * @author indiaye
 */
public abstract class AttributeStun {

    private Array type;
    protected Integer16Array length = new Integer16Array(0);

    public AttributeStun(Array type) {
        this.type = type;
    }

    public Array getArray() {
        SupArray data = new SupArray();
        data.addLast(type);
        data.addLast(length);
        data.addLast(getValue());
        return data;
    }
    public int getType(){
        return Integer.valueOf(Array.toHexString(this.type),16).intValue();
    }

    public abstract Array getValue();

    public int getPaddedLength(){
        int len = this.length.getValue();
        len += (4 - len % 4) % 4;
       
        return len;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<attribute ");
        stringBuilder.append("type=\"" + (String)DictionnaryStun.readProperties().get(Array.toHexString(type)) + "\">\n");
        stringBuilder.append(getStringAttribute());
        stringBuilder.append("\n");
        stringBuilder.append("<attribute/>\n");


        return stringBuilder.toString();
    }

    public abstract String getStringAttribute();

    public abstract Parameter getParameterAtt(String param);
}
