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

import com.sun.media.rtp.util.Packet;
import com.sun.media.rtp.util.PacketFilter;
import com.sun.media.rtp.util.RTPPacket;
import com.sun.media.rtp.util.UDPPacketSender;
import java.io.IOException;
import java.net.UnknownHostException;
import javax.media.Buffer;

/**
 * Adaptation of the JMF implementation to support RTCP APP events
 * 
 * @author JM. Auffret
 */ 
public class RTPTransmitter
{
    // PTT adaptation

	int lastSeqNumber = -1;

    // End PTT adaptation

	
    RTPRawSender sender;
    SSRCCache cache;

    public RTPTransmitter(SSRCCache ssrccache)
    {
        cache = ssrccache;
    }

    public RTPTransmitter(SSRCCache ssrccache, int i, String s)
        throws UnknownHostException, IOException
    {
        this(ssrccache, new RTPRawSender(i, s));
    }

    public RTPTransmitter(SSRCCache ssrccache, int i, String s, UDPPacketSender udppacketsender)
        throws UnknownHostException, IOException
    {
        this(ssrccache, new RTPRawSender(i, s, udppacketsender));
    }

    public RTPTransmitter(SSRCCache ssrccache, RTPRawSender rtprawsender)
    {
        this(ssrccache);
        setSender(rtprawsender);
    }

    protected RTPPacket MakeRTPPacket(Buffer buffer, SendSSRCInfo sendssrcinfo)
    {
        byte abyte0[] = (byte[])buffer.getData();
        if(abyte0 == null)
        {
            return null;
        }
        Packet packet = new Packet();
        packet.data = abyte0;
        packet.offset = 0;
        packet.length = buffer.getLength();
        packet.received = false;
        RTPPacket rtppacket = new RTPPacket(packet);
        if((buffer.getFlags() & 0x800) != 0)
        {
            rtppacket.marker = 1;
        } else
        {
            rtppacket.marker = 0;
        }
        sendssrcinfo.packetsize += buffer.getLength();
        rtppacket.payloadType = ((SSRCInfo) (sendssrcinfo)).payloadType;
        rtppacket.seqnum = (int)sendssrcinfo.getSequenceNumber(buffer);
        rtppacket.timestamp = ((SSRCInfo) (sendssrcinfo)).rtptime;
        rtppacket.ssrc = ((SSRCInfo) (sendssrcinfo)).ssrc;
        rtppacket.payloadoffset = buffer.getOffset();
        rtppacket.payloadlength = buffer.getLength();
        sendssrcinfo.bytesreceived += buffer.getLength();
        sendssrcinfo.maxseq++;
        sendssrcinfo.lasttimestamp = rtppacket.timestamp;
        
	    // PTT adaptation

        lastSeqNumber = rtppacket.seqnum;

	    // End PTT adaptation
        
        return rtppacket;
    }

    public void TransmitPacket(Buffer buffer, SendSSRCInfo sendssrcinfo)
    {
        sendssrcinfo.rtptime = sendssrcinfo.getTimeStamp(buffer);
        if(buffer.getHeader() instanceof Long)
        {
            sendssrcinfo.systime = ((Long)buffer.getHeader()).longValue();
        } else
        {
            sendssrcinfo.systime = System.currentTimeMillis();
        }
        RTPPacket rtppacket = MakeRTPPacket(buffer, sendssrcinfo);
        if(rtppacket == null)
        {
            return;
        } else
        {
            transmit(rtppacket);
            sendssrcinfo.stats.total_pdu++;
            sendssrcinfo.stats.total_bytes = sendssrcinfo.stats.total_bytes + buffer.getLength();
            cache.sm.transstats.rtp_sent++;
//            cache.sm.transstats.bytes_sent = cache.sm.transstats.bytes_sent + buffer.getLength();
            cache.sm.transstats.bytes_sent = cache.sm.transstats.bytes_sent + rtppacket.length;
            return;
        }
    }

    public void close()
    {
        if(sender != null)
        {
            sender.closeConsumer();
        }
    }

    public RTPRawSender getSender()
    {
        return sender;
    }

    public void setSender(RTPRawSender rtprawsender)
    {
        sender = rtprawsender;
    }

    protected void transmit(RTPPacket rtppacket)
    {
        try
        {
            sender.sendTo(rtppacket);
        }
        catch(IOException _ex)
        {
            cache.sm.transstats.transmit_failed++;
        }
    }
    
    // PTT adaptation

	public int getLastSeqNumber() {
		return lastSeqNumber;
	}

    // End PTT adaptation
    
}
