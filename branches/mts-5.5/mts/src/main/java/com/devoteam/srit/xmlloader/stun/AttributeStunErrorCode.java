/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.stun;

import com.devoteam.srit.xmlloader.core.Parameter;
import gp.utils.arrays.*;

/**
 *
 * @author indiaye
 */
public class AttributeStunErrorCode extends AttributeStun {

    private Integer08Array code;
    private Array reasonPhrase;
    private Array head;

    public AttributeStunErrorCode(Array type, int code, String reasonPhrase) {
        super(type);
        setErrorCode(code, reasonPhrase);
    }

    AttributeStunErrorCode(Array data) {
        super(data.subArray(0,2));
        this.length=new Integer16Array(data.subArray(2,2));
        Array attValue = data.subArray(4, this.length.getValue());
        this.head=attValue.subArray(0,4);
        //take the 3 bits for the class and the 8 last bits for tenths
        this.code=new Integer08Array(head.getBits(21,3)*100+head.getBits(24,8));
        reasonPhrase=attValue.subArray(4,this.length.getValue());
    }

    public void setErrorCode(int code, String reasonPhrase) {
        head=new DefaultArray(4);
        int hundreds=code/100;
        int rest=code%100;
        head.setBits(21,3,hundreds);
        //set of 3rd octet
        head.set(3,rest);
        this.code = new Integer08Array(code);
        this.reasonPhrase = new DefaultArray(reasonPhrase.getBytes());
        this.length.setValue(4 + this.reasonPhrase.length);
    }

    @Override
    public Array getValue() {
       SupArray array=new SupArray();
       array.addLast(head);
       array.addLast(this.reasonPhrase);
       return array;
    }

    @Override
    public String getStringAttribute() {

       StringBuilder errorString=new StringBuilder();
       errorString.append("<errorCode");
       errorString.append("code=\""+this.code.getValue()+"\",");
       errorString.append("reasonPhrase=\""+Integer.parseInt(Array.toHexString(this.reasonPhrase))+"\"/>");
       return errorString.toString();

    }
     public Parameter getParameterAtt(String param) {
        Parameter var = new Parameter();
        if (param.equalsIgnoreCase("code")) {
            var.add(this.code.getValue());
        }
        if (param.equalsIgnoreCase("reasonPhrase")) {
            var.add(Array.toHexString(this.reasonPhrase));
        }

        return var;
    }
}
