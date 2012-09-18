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

import org.dom4j.Element;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.Integer16Array;
import gp.utils.arrays.Integer32Array;
import gp.utils.arrays.SupArray;

/**
*
* @author El Aly Mohamad Bilal 
*/
public class GtpHeader extends Header {
	
	//Header composers 
	private String name;
	private int version;
	private int protocolType;
	private int extensionHeaderFlag;
	private int sequenceNumberFlag;
	private int nPduNumberFlag;
	private int messageType;
	private int length;
	private int teid;
	private int sequenceNumber;
    private int nPduNumber;
    private int nextExtensionType;
    
    public GtpHeader()
    {}
    //getSize
    public int getSize()
    {
    	return 12; 
    }
    //Getters and setters
    public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public int getProtocolType() {
		return protocolType;
	}
	public void setProtocolType(int protocolType) {
		this.protocolType = protocolType;
	}
	public int getExtensionHeaderFlag() {
		return extensionHeaderFlag;
	}
	public void setExtensionHeaderFlag(int extensionHeaderFlag) {
		this.extensionHeaderFlag = extensionHeaderFlag;
	}
	public int getSequenceNumberFlag() {
		return sequenceNumberFlag;
	}
	public void setSequenceNumberFlag(int sequenceNumberFlag) {
		this.sequenceNumberFlag = sequenceNumberFlag;
	}
	public int getnPduNumberFlag() {
		return nPduNumberFlag;
	}
	public void setnPduNumberFlag(int nPduNumberFlag) {
		this.nPduNumberFlag = nPduNumberFlag;
	}
	public int getMessageType() {
		return messageType;
	}
	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public int getTeid() {
		return teid;
	}
	public void setTeid(int teid) {
		this.teid = teid;
	}
	public int getSequenceNumber() {
		return sequenceNumber;
	}
	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
	public int getnPduNumber() {
		return nPduNumber;
	}
	public void setnPduNumber(int nPduNumber) {
		this.nPduNumber = nPduNumber;
	}
	public int getNextExtensionType() {
		return nextExtensionType;
	}
	public void setNextExtensionType(int nextExtensionType) {
		this.nextExtensionType = nextExtensionType;
	}
	
	public Array getArray() throws Exception
    {
        //manage header data
        SupArray supArray = new SupArray();

        DefaultArray firstByte = new DefaultArray(1);//first byte data
        firstByte.setBits(0, 3, version);
        firstByte.setBits(3, 1, protocolType);
        firstByte.setBits(4, 1, 0);
        firstByte.setBits(5, 1, extensionHeaderFlag);
        firstByte.setBits(6, 1, sequenceNumberFlag);
        firstByte.setBits(7, 1, nPduNumberFlag);
        supArray.addFirst(firstByte);

        supArray.addLast(new Integer08Array(messageType));
        supArray.addLast(new Integer16Array(length));
        supArray.addLast(new Integer32Array(teid));
        supArray.addLast(new Integer16Array(sequenceNumber));
        supArray.addLast(new Integer08Array(nPduNumber));
        supArray.addLast(new Integer08Array(nextExtensionType));
        
        return supArray;
    }
	
	public void parseArray(Array array, GtppDictionary dictionary) throws Exception
    {
        version = (array.subArray(0, 1).getBits(0, 3));
        protocolType = (array.subArray(0, 1).getBits(3, 1));
        extensionHeaderFlag = (array.subArray(0, 1).getBits(5, 1));
        sequenceNumberFlag = (array.subArray(0, 1).getBits(6, 1));
        nPduNumberFlag = (array.subArray(0, 1).getBits(7, 1));
        
        messageType = (new Integer08Array(array.subArray(1, 1)).getValue());
        name = dictionary.getMessageNameFromType(messageType);
        
        length = (new Integer16Array(array.subArray(2, 2)).getValue());
        teid = (new Integer32Array(array.subArray(4, 4)).getValue());
        sequenceNumber = (new Integer16Array(array.subArray(8, 2)).getValue());
        nPduNumber = (new Integer08Array(array.subArray(10, 1)).getValue());
        nextExtensionType = (new Integer08Array(array.subArray(11, 1)).getValue());
    }
	
	@Override
    public GtpHeader clone()
    {
    	GtpHeader clone = new GtpHeader();

        clone.setName(getName());
        clone.setVersion(version);
        clone.setProtocolType(protocolType);
        clone.setExtensionHeaderFlag(extensionHeaderFlag);
        clone.setSequenceNumberFlag(sequenceNumberFlag); 
        clone.setnPduNumberFlag(nPduNumberFlag); 
        clone.setMessageType(messageType);
        clone.setLength(length);
        clone.setTeid(teid); 
        clone.setSequenceNumber(sequenceNumber);
        clone.setnPduNumber(nPduNumber); 
        clone.setNextExtensionType(nextExtensionType);

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

