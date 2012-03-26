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

import com.devoteam.srit.xmlloader.core.protocol.StackFactory;

import gp.utils.arrays.Array;

/**
 *
 * @author gpasquiers
 */
public class PTCPPacket {
    // header fields
    private PIPHeader ipHeader;
    private PTCPHeader tcpHeader;
    private Array data;
    
    //private Long timestamp;

    public PTCPPacket(PIPHeader ipHeader, Array array){
        this.ipHeader = ipHeader;
        this.tcpHeader = new PTCPHeader(array);
        this.data = array.subArray(tcpHeader.getLength());
    }

    public PIPHeader getIPHeader(){
        return ipHeader;
    }

    public PTCPHeader getTCPHeader(){
        return tcpHeader;
    }

    public Array getData(){
        return data;
    }

    public void setData(Array data){
        this.data = data;
    }

}
