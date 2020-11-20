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
*/package com.sun.media.rtp;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Adaptation of the JMF implementation to support RTCP APP events
 *  
 * @author JM. Auffret
 */ 
public class RTCPAPPPacket extends RTCPPacket
{

    int ssrc;
    int name;
    int subtype;
    byte data[];

    public RTCPAPPPacket(int i, int j, int k, byte abyte0[])
    {
        ssrc = i;
        name = j;
        subtype = k;
        data = abyte0;
        super.type = 204;
        super.received = false;
        if((abyte0.length & 3) != 0)
        {
            throw new IllegalArgumentException("Bad data length");
        }
        if(k < 0 || k > 31)
        {
            throw new IllegalArgumentException("Bad subtype");
        } else
        {
            return;
        }
    }

    public RTCPAPPPacket(RTCPPacket rtcppacket)
    {
        super(rtcppacket);
        super.type = 204;
    }

    void assemble(DataOutputStream dataoutputstream)
        throws IOException
    {
        dataoutputstream.writeByte(128 + subtype);
        dataoutputstream.writeByte(204);
        dataoutputstream.writeShort(2 + (data.length >> 2));
        dataoutputstream.writeInt(ssrc);
        dataoutputstream.writeInt(name);
        dataoutputstream.write(data);
    }

    public int calcLength()
    {
        return 12 + data.length;
    }

    public String nameString(int i)
    {
        return "" + (char)(i >>> 24) + (char)(i >>> 16 & 0xff) + (char)(i >>> 8 & 0xff) + (char)(i & 0xff);
    }

    public String decodeSubtype(int type)
    {
    	String result = "";
        switch(type) {
			case 0x00:
				result = "Floor request";
				break;
				
			case 0x01:
				result = "Floor grant";
				break;
				
			case 0x02:
				result = "Floor taken";
				break;

			case 0x12:
				result = "Floor taken (ack)";
				break;
				
			case 0x03:
				result = "Floor deny";
				break;
				
			case 0x04:
				result = "Floor release";
				break;
			
			case 0x05:
				result = "Floor idle";
				break;
			
			case 0x06:
				result = "Floor revoke";
				break;
				
			case 0x07:
				result = "Floor ack";
				break;

			default:
				result = result + type;
        }
        return result;
    }

	// PTT Adaptation

    public String toString()
    {
		return "RTCP APP Packet from SSRC "
			+ ssrc
			+ " with name "
			+ nameString(name)
			+ " and subtype '"
			+ decodeSubtype(subtype)
			+ "'\n\tData (length "
			+ data.length
			+ "): "
			+ new String(data)
			+ "\n";
    }
    
	/**
	 * @return Returns the data.
	 */
	public byte[] getData() {
		return data;
	}
	
	/**
	 * @return Returns the name.
	 */
	public int getName() {
		return name;
	}
	
	/**
	 * @return Returns the ssrc.
	 */
	public int getSsrc() {
		return ssrc;
	}
	
	/**
	 * @return Returns the subtype.
	 */
	public int getSubtype() {
		return subtype;
	}
	// End PTT Adaptation
}
