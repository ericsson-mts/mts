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
public class AttributeStunChangeRequest extends AttributeStun {

    private Array head;
    private boolean changeIP;
    private boolean changePort;

    public AttributeStunChangeRequest(Array type, String changeIP, String changePort) {
        super(type);
        head = new DefaultArray(4);
        if (changeIP.equalsIgnoreCase("true")) {
            this.changeIP = true;
            head.setBit(29, 1);
        } else {
            this.changeIP = false;
            head.setBit(29, 0);
        }
        if (changePort.equalsIgnoreCase("true")) {
            this.changePort = true;
            head.setBit(30, 1);
        } else {
            this.changePort = false;
            head.setBit(30, 0);
        }

        this.length.setValue(head.length);

    }

    public AttributeStunChangeRequest(Array data) {
        super(data.subArray(0, 2));
        this.length = new Integer16Array(data.subArray(2, 2));
        this.head= data.subArray(4, this.length.getValue());
        if(head.getBit(29)==0)
            this.changeIP=false;
        else
            this.changeIP=true;
        if(head.getBit(30)==0)
            this.changePort=false;
        else
            this.changePort=true;
        
    }

    @Override
    public Array getValue() {
        return this.head;
    }

    @Override
    public String getStringAttribute() {
        StringBuilder changeString = new StringBuilder();
        changeString.append("<changeRequest ");
        changeString.append("changeIP=\"" + this.changeIP + "\" ,");
        changeString.append("changePort=\"" + this.changeIP + "\" ,");
        changeString.append("length=\"" + this.changePort + "\"/>");
        return changeString.toString();
    }

    public Parameter getParameterAtt(String param) {
        
        Parameter var = new Parameter();
        if (param.equalsIgnoreCase("changeIP")) {
            var.add(this.changeIP);
        }
        if (param.equalsIgnoreCase("changePort")) {
            var.add(this.changePort);
        }

        return var;
    }
}
