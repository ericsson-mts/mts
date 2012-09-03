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

package com.devoteam.srit.xmlloader.gtpp;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.Integer16Array;
import gp.utils.arrays.SupArray;

/**
 *
 * @author El Aly Mohamad Bilal 
 */
public class GtpHeader {

	private String name;
    private int messageType;
    private int version;
    private int protocolType;
    private int length = 0;
    private int sequenceNumber;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLength(int value)
    {
        this.length = value;
    }

    public int getLength()
    {
        return this.length;
    }
    
    public void setMessageType(int value)
    {
        this.messageType = value;
    }

    public int getMessageType()
    {
        return this.messageType;
    }

    public void setSequenceNumber(int value)
    {
        this.sequenceNumber = value;
    }

    public int getSequenceNumber()
    {
        return this.sequenceNumber;
    }

    public int isProtocolType() {
        return protocolType;
    }

    public void setProtocolType(int protocolType) {
        this.protocolType = protocolType;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
    
    public Array getArray() throws Exception
    {
        //manage header data
        SupArray supArray = new SupArray();

        DefaultArray firstByte = new DefaultArray(1);//first byte data
        firstByte.setBits(0, 3, version);
        firstByte.setBits(3, 1, protocolType);
        firstByte.setBits(4, 4, 15);//=> 1111 in bits
        supArray.addFirst(firstByte);

        supArray.addLast(new Integer08Array(messageType));
        supArray.addLast(new Integer16Array(length));
        supArray.addLast(new Integer16Array(sequenceNumber));

        return supArray;
    }
    
    public void parseArray(Array array) throws Exception
    {
        setVersion(array.subArray(0, 1).getBits(0, 3));
        setProtocolType(array.subArray(0, 1).getBits(3, 1));
        setMessageType(new Integer08Array(array.subArray(1, 1)).getValue());
        setLength(new Integer16Array(array.subArray(2, 2)).getValue());
        setSequenceNumber(new Integer16Array(array.subArray(4, 2)).getValue());
    }
    
    @Override
    public GtpHeader clone()
    {
    	GtpHeader clone = new GtpHeader();

        clone.setName(getName());
        clone.setVersion(version);
        clone.setProtocolType(protocolType);
        clone.setMessageType(messageType);
        clone.setLength(length);
        clone.setSequenceNumber(sequenceNumber);

        return clone;
    }

    @Override
    public String toString()
    {
        String str = name + ", length " + length + ", messageType " + messageType + ", version " + version + ", seqNum " + sequenceNumber + "\r\n";
        return str;
    }
}
