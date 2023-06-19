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

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Vector;

import com.sun.media.rtp.util.Packet;
import com.sun.media.rtp.util.UDPPacketSender;

/**
 * Adaptation of the JMF implementation to support RTCP APP events
 *  
 * @author JM. Auffret
 */ 
public class RTCPTransmitter
{
    RTCPRawSender sender;
    OverallStats stats;
    SSRCCache cache;
    int sdescounter;
    SSRCInfo ssrcInfo;
    private Method m[];
    private Class cl[];
    private Object args[][];

    public RTCPTransmitter(SSRCCache ssrccache)
    {
        stats = null;
        sdescounter = 0;
        ssrcInfo = null;
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        cache = ssrccache;
        stats = ssrccache.sm.defaultstats;
    }

    public RTCPTransmitter(SSRCCache ssrccache, int i, String s)
        throws UnknownHostException, IOException
    {
        this(ssrccache, new RTCPRawSender(i, s));
    }

    public RTCPTransmitter(SSRCCache ssrccache, int i, String s, UDPPacketSender udppacketsender)
        throws UnknownHostException, IOException
    {
        this(ssrccache, new RTCPRawSender(i, s, udppacketsender));
    }

    public RTCPTransmitter(SSRCCache ssrccache, RTCPRawSender rtcprawsender)
    {
        this(ssrccache);
        setSender(rtcprawsender);
        stats = ssrccache.sm.defaultstats;
    }

    public void bye(int i, byte abyte0[])
    {
        if(!cache.rtcpsent)
        {
            return;
        }
        cache.byestate = true;
        Vector vector = makereports();
        RTCPPacket artcppacket[] = new RTCPPacket[vector.size() + 1];
        vector.copyInto(artcppacket);
        int ai[] = new int[1];
        ai[0] = i;
        RTCPBYEPacket rtcpbyepacket = new RTCPBYEPacket(ai, abyte0);
        artcppacket[artcppacket.length - 1] = rtcpbyepacket;
        RTCPCompoundPacket rtcpcompoundpacket = new RTCPCompoundPacket(artcppacket);
        double d;
        if(cache.aliveCount() > 50)
        {
            cache.reset(((Packet) (rtcpbyepacket)).length);
            d = cache.calcReportInterval(ssrcInfo.sender, false);
        } else
        {
            d = 0.0D;
        }
        try
        {
            Thread.sleep((long)d);
        }
        catch(InterruptedException interruptedexception)
        {
            interruptedexception.printStackTrace();
        }
        transmit(rtcpcompoundpacket);
        sdescounter = 0;
    }

    public void bye(String s)
    {
        if(s != null)
        {
            bye(ssrcInfo.ssrc, s.getBytes());
        } else
        {
            bye(ssrcInfo.ssrc, null);
        }
    }

    public void close()
    {
        if(sender != null)
        {
            sender.closeConsumer();
        }
    }

    public RTCPRawSender getSender()
    {
        return sender;
    }

    protected RTCPReportBlock[] makerecreports(long l)
    {
        Vector vector = new Vector();
        for(Enumeration enumeration = cache.cache.elements(); enumeration.hasMoreElements();)
        {
            SSRCInfo ssrcinfo = (SSRCInfo)enumeration.nextElement();
            if(!ssrcinfo.ours && ssrcinfo.sender)
            {
                RTCPReportBlock rtcpreportblock = new RTCPReportBlock();
                rtcpreportblock.ssrc = ssrcinfo.ssrc;
                rtcpreportblock.lastseq = ssrcinfo.maxseq + ssrcinfo.cycles;
                rtcpreportblock.jitter = (int)ssrcinfo.jitter;
                rtcpreportblock.lsr = (int)(ssrcinfo.lastSRntptimestamp >> 32);
                rtcpreportblock.dlsr = (int)((double)(l - ssrcinfo.lastSRreceiptTime) * 65.536000000000001D);
                rtcpreportblock.packetslost = (int)(((rtcpreportblock.lastseq - (long)ssrcinfo.baseseq) + 1L) - (long)ssrcinfo.received);
                if(rtcpreportblock.packetslost < 0)
                {
                    rtcpreportblock.packetslost = 0;
                }
                double d = (double)(rtcpreportblock.packetslost - ssrcinfo.prevlost) / (double)(rtcpreportblock.lastseq - (long)ssrcinfo.prevmaxseq);
                if(d < 0.0D)
                {
                    d = 0.0D;
                }
                rtcpreportblock.fractionlost = (int)(d * 256D);
                ssrcinfo.prevmaxseq = (int)rtcpreportblock.lastseq;
                ssrcinfo.prevlost = rtcpreportblock.packetslost;
                vector.addElement(rtcpreportblock);
            }
        }

        RTCPReportBlock artcpreportblock[] = new RTCPReportBlock[vector.size()];
        vector.copyInto(artcpreportblock);
        return artcpreportblock;
    }

    protected Vector makereports()
    {
        Vector vector = new Vector();
        SSRCInfo ssrcinfo = ssrcInfo;
        boolean flag = false;
        if(ssrcinfo.sender)
        {
            flag = true;
        }
        long l = System.currentTimeMillis();
        RTCPReportBlock artcpreportblock[] = makerecreports(l);
        RTCPReportBlock artcpreportblock1[] = artcpreportblock;
        if(artcpreportblock.length > 31)
        {
            artcpreportblock1 = new RTCPReportBlock[31];
            System.arraycopy(artcpreportblock, 0, artcpreportblock1, 0, 31);
        }
        if(flag)
        {
            RTCPSRPacket rtcpsrpacket = new RTCPSRPacket(ssrcinfo.ssrc, artcpreportblock1);
            vector.addElement(rtcpsrpacket);
            long l1 = ssrcinfo.systime == 0L ? System.currentTimeMillis() : ssrcinfo.systime;
            long l2 = l1 / 1000L;
            double d = (double)(l1 - l2 * 1000L) / 1000D;
            rtcpsrpacket.ntptimestamplsw = (int)(d * 4294967296D);
            rtcpsrpacket.ntptimestampmsw = l2;
            rtcpsrpacket.rtptimestamp = (int)ssrcinfo.rtptime;
            rtcpsrpacket.packetcount = ssrcinfo.maxseq - ssrcinfo.baseseq;
            rtcpsrpacket.octetcount = ssrcinfo.bytesreceived;
        } else
        {
            RTCPRRPacket rtcprrpacket = new RTCPRRPacket(ssrcinfo.ssrc, artcpreportblock1);
            vector.addElement(rtcprrpacket);
        }
        if(artcpreportblock1 != artcpreportblock)
        {
            for(int i = 31; i < artcpreportblock.length; i += 31)
            {
                if(artcpreportblock.length - i < 31)
                {
                    artcpreportblock1 = new RTCPReportBlock[artcpreportblock.length - i];
                }
                System.arraycopy(artcpreportblock, i, artcpreportblock1, 0, artcpreportblock1.length);
                RTCPRRPacket rtcprrpacket1 = new RTCPRRPacket(ssrcinfo.ssrc, artcpreportblock1);
                vector.addElement(rtcprrpacket1);
            }

        }
        RTCPSDESPacket rtcpsdespacket = new RTCPSDESPacket(new RTCPSDES[1]);
        rtcpsdespacket.sdes[0] = new RTCPSDES();
        rtcpsdespacket.sdes[0].ssrc = ssrcInfo.ssrc;
        Vector vector1 = new Vector();
        vector1.addElement(new RTCPSDESItem(1, ssrcinfo.sourceInfo.getCNAME()));
        if(sdescounter % 3 == 0)
        {
            if(ssrcinfo.name != null && ssrcinfo.name.getDescription() != null)
            {
                vector1.addElement(new RTCPSDESItem(2, ssrcinfo.name.getDescription()));
            }
            if(ssrcinfo.email != null && ssrcinfo.email.getDescription() != null)
            {
                vector1.addElement(new RTCPSDESItem(3, ssrcinfo.email.getDescription()));
            }
            if(ssrcinfo.phone != null && ssrcinfo.phone.getDescription() != null)
            {
                vector1.addElement(new RTCPSDESItem(4, ssrcinfo.phone.getDescription()));
            }
            if(ssrcinfo.loc != null && ssrcinfo.loc.getDescription() != null)
            {
                vector1.addElement(new RTCPSDESItem(5, ssrcinfo.loc.getDescription()));
            }
            if(ssrcinfo.tool != null && ssrcinfo.tool.getDescription() != null)
            {
                vector1.addElement(new RTCPSDESItem(6, ssrcinfo.tool.getDescription()));
            }
            if(ssrcinfo.note != null && ssrcinfo.note.getDescription() != null)
            {
                vector1.addElement(new RTCPSDESItem(7, ssrcinfo.note.getDescription()));
            }
        }
        sdescounter++;
        rtcpsdespacket.sdes[0].items = new RTCPSDESItem[vector1.size()];
        vector1.copyInto(rtcpsdespacket.sdes[0].items);
        vector.addElement(rtcpsdespacket);

        return vector;
    }

    public void report()
    {
        Vector vector = makereports();
        RTCPPacket artcppacket[] = new RTCPPacket[vector.size()];
        vector.copyInto(artcppacket);
        RTCPCompoundPacket rtcpcompoundpacket = new RTCPCompoundPacket(artcppacket);
        transmit(rtcpcompoundpacket);
    }
    
    // PTT adaptation
    
    public RTCPCompoundPacket sendReport()
    {
        Vector vector = makereports();
        RTCPPacket artcppacket[] = new RTCPPacket[vector.size()];
        vector.copyInto(artcppacket);
        RTCPCompoundPacket rtcpcompoundpacket = new RTCPCompoundPacket(artcppacket);
        transmit(rtcpcompoundpacket);
        return rtcpcompoundpacket;
    }

    public void sendRtcpAppEvent(RTCPAPPPacket packet)
    {
        Vector vector = new Vector();
        vector.addElement(packet);
        RTCPPacket artcppacket[] = new RTCPPacket[vector.size()];
        vector.copyInto(artcppacket);
        RTCPCompoundPacket rtcpcompoundpacket = new RTCPCompoundPacket(artcppacket);
        transmit(rtcpcompoundpacket);
    }
    
    // End PTT adaptation

    public void setSSRCInfo(SSRCInfo ssrcinfo)
    {
        ssrcInfo = ssrcinfo;
    }

    public long getSSRC()
    {
        return ssrcInfo.ssrc;
    }

    public void setSender(RTCPRawSender rtcprawsender)
    {
        sender = rtcprawsender;
    }

    protected void transmit(RTCPCompoundPacket rtcpcompoundpacket)
    {
        try
        {
            sender.sendTo(rtcpcompoundpacket);
            if(ssrcInfo instanceof SendSSRCInfo)
            {
                ((SendSSRCInfo)ssrcInfo).stats.total_rtcp++;
                cache.sm.transstats.rtcp_sent++;
            }
            cache.updateavgrtcpsize(((Packet) (rtcpcompoundpacket)).length);
            if(cache.initial)
            {
                cache.initial = false;
            }
            if(!cache.rtcpsent)
            {
                cache.rtcpsent = true;
            }
        }
        catch(IOException _ex)
        {
            stats.update(6, 1);
            cache.sm.transstats.transmit_failed++;
        }
    }
}
