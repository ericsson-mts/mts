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
import gp.utils.arrays.IPv4Array;
import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.Integer16Array;

/**
 *
 * @author gpasquiers
 */
public class PIPHeader {
    private Array array;
    private int length;
    private int ident;
    private boolean more_flag;
    private boolean dont_frag; 
    private boolean rsv_frag;
    private int offset_fragment; 
    private int protocol;
    private IPv4Array srcIP;
    private IPv4Array dstIP;
    private int totalLength;
    
    private Long timestamp;

    public PIPHeader(Array array){
        this.length = (array.get(0) & 0x0f) * 4;
        this.ident = new Integer16Array(array.subArray(4, 2)).getValue();
        this.totalLength = new Integer16Array(array.subArray(2, 2)).getValue();
       	Array flags = array.subArray(6, 1);
       	this.more_flag = flags.getBit(2) != 0;
       	this.dont_frag = flags.getBit(1) != 0;
       	this.rsv_frag = flags.getBit(0) != 0;
       	Integer16Array offset_frag = new Integer16Array(array.subArray(6, 2));
       	offset_fragment = offset_frag.getValue() & 0x1fff;
        this.protocol = new Integer08Array(array.subArray(9, 1)).getValue();
        this.srcIP = new IPv4Array(array.subArray(12, 4));
        this.dstIP = new IPv4Array(array.subArray(16, 4));

        /*if (this.protocol == IPPacket.IPPROTO_UDP)
        {
        	this.array = array.subArray(0, this.length + 8);        	
        }
        else if (this.protocol == IPPacket.IPPROTO_TCP)
        {
        	this.array = array.subArray(0, this.length + 20);
        }*/
    }

    public IPv4Array getSrcIP(){
        return srcIP;
    }

    public IPv4Array getDstIP(){
        return dstIP;
    }

    public int getProtocol(){
        return protocol;
    }

    public int getLength(){
        return length;
    }

    public int getTotalLength(){
        return totalLength;
    }

    public boolean isUDP(){
        return protocol == 17;
    }

    public boolean isTCP(){
        return protocol == 6;
    }

	public int getIdent() {
		return ident;
	}

	public boolean isMore_flag() {
		return more_flag;
	}

	public boolean isDont_frag() {
		return dont_frag;
	}

	public boolean isRsv_frag() {
		return rsv_frag;
	}

	public int getOffset_fragment() {
		return offset_fragment;
	}
        
    public void setTimestamp(long time){
        timestamp = time;
    }
    
    public long getTimestamp(){
        return timestamp;
    }
}