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
