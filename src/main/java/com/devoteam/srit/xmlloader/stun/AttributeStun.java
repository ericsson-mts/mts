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
