/* 
 * Copyright 2012 Devoteam http://www.devoteam.com
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * 
 * This file is part of Multi-Protocol Test Suite (MTS).
 * 
 * Multi-Protocol Test Suite (MTS) is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License.
 * 
 * Multi-Protocol Test Suite (MTS) is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Multi-Protocol Test Suite (MTS).
 * If not, see <http://www.gnu.org/licenses/>.
 * 
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
