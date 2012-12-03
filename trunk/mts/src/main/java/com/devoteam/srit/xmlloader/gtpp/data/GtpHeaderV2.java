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

package com.devoteam.srit.xmlloader.gtpp.data;

import java.io.InputStream;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.gtpp.GtppDictionary;

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
public class GtpHeaderV2 extends Header {
	
	//Header composers 
	private String name;
	private int version;
	private int piggyFlag;
	private int teidFlag;
	private int messageType;
	private int length;
	private int tunnelEndpointId;
	private int sequenceNumber;
    
    public GtpHeaderV2(DefaultArray flagArray)
    {	
        this.version = flagArray.getBits(0,3);
        this.piggyFlag = flagArray.getBits(3,1);
        this.teidFlag = flagArray.getBits(4,1);
    }
    public GtpHeaderV2()
    {
    	this.version = 2;
    }
    //getSize
    public int getSize()
    {
    	int k = 0; 
    	if(teidFlag == 0)
    		k = 8; 
    	if(teidFlag == 1)
    		k =12; 
    	return k; 
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
	public int getPiggyFlag() {
		return piggyFlag;
	}
	public void setPiggyFlag(int piggyFlag) {
		this.piggyFlag = piggyFlag;
	}	
	public int getTeidFlag() {
		return teidFlag;
	}
	public void setTeidFlag(int teidFlag) {
		this.teidFlag = teidFlag;
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
	public int getTunnelEndpointId() {
		return tunnelEndpointId;
	}
	public void setTunnelEndpointId(int tunnelEndpointId) {
		this.tunnelEndpointId = tunnelEndpointId;
	}
	public int getSequenceNumber() {
		return sequenceNumber;
	}
	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
	
	public Array getArray() throws Exception
    {
        //manage header data
        SupArray supArray = new SupArray();

        DefaultArray firstByte = new DefaultArray(1);//first byte data
        firstByte.setBits(0, 3, version);
        firstByte.setBits(3, 1, piggyFlag);
        firstByte.setBits(4, 1, teidFlag);
        firstByte.setBits(5, 1, 0);
        firstByte.setBits(6, 1, 0);
        firstByte.setBits(7, 1, 0);
        supArray.addFirst(firstByte);

        supArray.addLast(new Integer08Array(messageType));
        supArray.addLast(new Integer16Array(length));
        if (teidFlag == 1)
        	supArray.addLast(new Integer32Array(tunnelEndpointId));
        
        Array sequenceNumberArray= new Integer32Array(sequenceNumber);
        supArray.addLast(sequenceNumberArray.subArray(1, 3));
        
        supArray.addLast(new Integer08Array(0));
        
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
        
        if(teidFlag != 0)
    	{
	        header = new byte[4];
	        stream.read(header, 0, 4);
	        array = new DefaultArray(header); 
	        tunnelEndpointId = (new Integer32Array(array).getValue());
    	}
        
    	header = new byte[4];
    	stream.read(header, 1, 3);
    	array = new DefaultArray(header); 
    	sequenceNumber = (new Integer32Array(array).getValue()); 

    	header = new byte[1];
    	stream.read(header, 0, 1);
    }
	
	@Override
    public GtpHeaderV2 clone()
    {
    	GtpHeaderV2 clone = new GtpHeaderV2();

        clone.setName(getName());
        clone.setVersion(version);
        clone.setPiggyFlag(piggyFlag);
        clone.setTeidFlag(teidFlag);
        clone.setMessageType(messageType);
        clone.setLength(length);
        clone.setTunnelEndpointId(tunnelEndpointId); 
        clone.setSequenceNumber(sequenceNumber);
        return clone;
    }
	
	@Override
    public String toString()
    {
        String str = "<headerV2 ";
        str += " piggyFlag=\"" + piggyFlag + "\""; 
        str += " teidFlag=" + teidFlag +  "\"";
        str += " messageType=\"" + name + ":" + messageType + "\"";
        str += " tunnelEndpointId=\"" + tunnelEndpointId + "\"";
        str += " sequenceNumber=\"" + sequenceNumber + "\"";
        str += "/>";
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
        
        String msgPiggyFlag = header.attributeValue("piggyFlag");
        if(msgPiggyFlag != null)
        {
        	piggyFlag = Integer.parseInt(msgPiggyFlag);
        }
        
        String msgTeidFlag = header.attributeValue("teidFlag");
        if(msgTeidFlag != null)
        {
        	teidFlag = Integer.parseInt(msgTeidFlag);
        }
        
        String msgSeqNum = header.attributeValue("sequenceNumber");
        if(msgSeqNum != null)
        {
        	sequenceNumber = Integer.parseInt(msgSeqNum);
        }
        
        String msgTunnelEndpointId = header.attributeValue("tunnelEndpointId");
        if(msgTunnelEndpointId != null)
        {
        	tunnelEndpointId = Integer.parseInt(msgTunnelEndpointId);
        }

    }

}


