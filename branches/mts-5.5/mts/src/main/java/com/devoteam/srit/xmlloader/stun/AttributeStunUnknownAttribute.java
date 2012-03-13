/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.stun;

import com.devoteam.srit.xmlloader.core.Parameter;
import gp.utils.arrays.*;
import java.util.LinkedList;

/**
 *
 * @author indiaye
 */
public class AttributeStunUnknownAttribute extends AttributeStun {

    LinkedList<Array> listAttribute = new LinkedList<Array>();

    public AttributeStunUnknownAttribute(Array type, int[] tabType) {
        super(type);
        int i = 0;
        while (i < tabType.length) {
            Array array = new Integer16Array(tabType[i]);
            this.listAttribute.addLast(array);
            i++;
        }
        this.length.setValue(listAttribute.size());
    }

    AttributeStunUnknownAttribute(Array data) {
        super(data.subArray(0,2));
        this.length=new Integer16Array(data.subArray(2,2));
        Array attValue = data.subArray(4, this.length.getValue());
        for (int i = 0; i < attValue.length; i = i + 2) {
            listAttribute.addLast(attValue.subArray(i, 2));
        }
    }

    public Array getValue() {

        SupArray listResult = new SupArray();

        for (Array array : listAttribute) {
            listResult.addLast(array);
        }
        return listResult;
    }

    @Override
    public String getStringAttribute() {
        StringBuilder unknownString = new StringBuilder();
        unknownString.append("<unknownAttribute ");
        for (Array array : listAttribute) {
            unknownString.append("type=\"" + Integer.valueOf(Array.toHexString(array), 16).intValue() + "\",");
        }
        unknownString.append("/>");
        return unknownString.toString();
    }

    public Parameter getParameterAtt(String param) {
        Parameter var = new Parameter();

        var.add(this.listAttribute);

        return var;
    }
}
