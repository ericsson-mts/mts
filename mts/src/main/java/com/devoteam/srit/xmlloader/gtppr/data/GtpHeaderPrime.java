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

package com.devoteam.srit.xmlloader.gtppr.data;

import java.io.InputStream;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.coding.q931.HeaderAbstract;
import com.devoteam.srit.xmlloader.gtppr.GtppDictionary;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.Integer16Array;
import gp.utils.arrays.SupArray;

/**
 *
 * @author Fabien Henry
 */
public class GtpHeaderPrime extends Header {
	
	private String name;
    private int messageType;
    private int version;
    private String versionName;
    private int protocolType;
    private int length = 0;
    private int sequenceNumber;
    
    public GtpHeaderPrime() 
    {
    	this.protocolType = 0; 
    	this.version = 0; 
        this.versionName = "GTPPrime";
	}
    public GtpHeaderPrime(DefaultArray flagArray) 
    {
    	this();
        this.version = flagArray.getBits(0,3);
        this.protocolType = flagArray.getBits(3,1);
	}
    public int getSize()
    {
    	return 6; 
    }
    
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
	public String getVersionName() {
		return versionName;
	}
	public void setVersionName(String versionName) {
		this.versionName = versionName;
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
    
    public void parseArray(InputStream stream, GtppDictionary dictionary) throws Exception
    {	
    	byte[] header = new byte[1];
    	
        stream.read(header, 0, 1);
        Array array = new DefaultArray(header); 
        messageType = (new Integer08Array(array).getValue());
        name = dictionary.getMessageNameFromType(messageType);
        
        header = new byte[2];
        
        stream.read(header, 0, 2);
        array = new DefaultArray(header); 
        length = (new Integer16Array(array).getValue());
        
        stream.read(header, 0, 2);
		array = new DefaultArray(header); 
		sequenceNumber = (new Integer16Array(array).getValue()); 
    }
    
    @Override
    public GtpHeaderPrime clone()
    {
    	GtpHeaderPrime clone = new GtpHeaderPrime();

        clone.setName(getName());
        clone.setVersion(version);
        clone.setVersionName(versionName);
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
    
    public void parseXml(Element header, GtppDictionary dictionary) throws Exception
    {
        String msgName = header.attributeValue("name");
        String msgType = header.attributeValue("type");

        if((msgType != null) && (msgName != null))
            throw new Exception("type and name of the message " + msgName + " must not be set both");

        if((msgType == null) && (msgName == null))
            throw new Exception("One of the parameter type or name of the message header must be set");

        if(msgName != null)
        {
            this.name = msgName;
            this.messageType = dictionary.getMessageTypeFromName(msgName);
        }
        else if(msgType != null)
        {	
        	this.messageType = Integer.parseInt(msgType); 
        	this.name = dictionary.getMessageNameFromType(this.messageType);
        }
        String msgSeqNum = header.attributeValue("sequenceNumber");
        sequenceNumber = Integer.parseInt(msgSeqNum);

    }
}
