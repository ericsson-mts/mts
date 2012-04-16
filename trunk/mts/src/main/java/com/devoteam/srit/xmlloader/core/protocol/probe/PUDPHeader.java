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

package com.devoteam.srit.xmlloader.core.protocol.probe;

import gp.utils.arrays.Array;
import gp.utils.arrays.Integer16Array;

/**
 *
 * @author gpasquiers
 */
public class PUDPHeader {
    private Integer16Array srcPort;
    private Integer16Array dstPort;

    public PUDPHeader(Array header){
        srcPort = new Integer16Array(header.subArray(0, 2));
        dstPort = new Integer16Array(header.subArray(2, 2));
    }

    public Integer16Array getSrcPort(){
        return srcPort;
    }

    public Integer16Array getDstPort(){
        return dstPort;
    }
}
