/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.stun;

import com.devoteam.srit.xmlloader.core.Parameter;
import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.Integer16Array;

/**
 *
 * @author indiaye
 */
public class AttributeStunText extends AttributeStun {

    private Array text;

    public AttributeStunText(Array type, String text) {
        super(type);
        setText(text);
    }

    public AttributeStunText(Array data) {
        super((data.subArray(0,2)));
        this.length =new Integer16Array(data.subArray(2,2));
        this.text=data.subArray(4, this.length.getValue());
       
    }

    public void setText(String text) {
        this.text = new DefaultArray(text.getBytes());
        this.length.setValue(this.text.length);
    }

    @Override
    public Array getValue() {
        return text;
    }

    @Override
    public String getStringAttribute() {
          StringBuilder textString=new StringBuilder();
       textString.append("<text");
       textString.append("value=\""+new String(this.text.getBytes())+"\"/>");
       
       return textString.toString();
    }
     public Parameter getParameterAtt(String param) {
        Parameter var = new Parameter();
        if (param.equalsIgnoreCase("text")) {
            var.add(new String(this.text.getBytes()));
        }

        return var;
    }
}
