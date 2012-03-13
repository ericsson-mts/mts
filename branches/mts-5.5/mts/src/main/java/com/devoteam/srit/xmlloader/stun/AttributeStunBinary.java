/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.stun;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.Integer16Array;

/**
 *
 * @author indiaye
 */
public class AttributeStunBinary extends AttributeStun {

    private Array binary;

    public AttributeStunBinary(Array type, Array binary) {
        super(type);
        setBinary(binary);
    }

    AttributeStunBinary(Array data) {
        super(data.subArray(0,2));
        this.length = new Integer16Array(data.subArray(2, 2));
        this.binary = data.subArray(4,this.length.getValue());
        
    }

    public void setBinary(Array binary) {
        this.binary=binary;
        this.length.setValue(this.binary.length);
        
    }

    @Override
    public gp.utils.arrays.Array getValue() {
        return this.binary;
    }

    @Override
    public String getStringAttribute() {
        StringBuilder binaryString = new StringBuilder();
        binaryString.append("<binary ");
        binaryString.append("value=\"" + Array.toHexString(binary) + "\"/>");
        return binaryString.toString();
    }

    public Parameter getParameterAtt(String param) {
        Parameter var = new Parameter();
        if (param.equalsIgnoreCase("binary")) {
            var.add(Array.toHexString(binary));
            
        }
        return var;
    }
}
