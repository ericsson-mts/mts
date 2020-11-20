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


import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Vector;

import javax.media.rtp.RTPConnector;
import javax.media.rtp.RTPPushDataSource;
import javax.media.rtp.SessionAddress;

import com.sun.media.rtp.util.BadFormatException;
import com.sun.media.rtp.util.Packet;
import com.sun.media.rtp.util.PacketFilter;
import com.sun.media.rtp.util.RTPPacketReceiver;
import com.sun.media.rtp.util.UDPPacketReceiver;

/**
 * Adaptation of the JMF implementation to support RTCP APP events
 *  
 * @author JM. Auffret
 */ 
public class RTCPRawReceiver extends PacketFilter
{
    public DatagramSocket socket;
    private StreamSynch streamSynch;
    private OverallStats stats;

    public RTCPRawReceiver()
    {
        stats = null;
    }

    public RTCPRawReceiver(int i, String s, OverallStats overallstats, StreamSynch streamsynch)
        throws UnknownHostException, IOException, SocketException
    {
        stats = null;
        streamSynch = streamsynch;
        stats = overallstats;
        UDPPacketReceiver udppacketreceiver = new UDPPacketReceiver(i, s, -1, null, 1000, null);
        setSource(udppacketreceiver);
        socket = udppacketreceiver.getSocket();
    }

    public RTCPRawReceiver(DatagramSocket datagramsocket, OverallStats overallstats, StreamSynch streamsynch)
    {
        stats = null;
        setSource(new UDPPacketReceiver(datagramsocket, 1000));
        stats = overallstats;
        streamSynch = streamsynch;
    }

    public RTCPRawReceiver(RTPConnector rtpconnector, OverallStats overallstats, StreamSynch streamsynch)
    {
        stats = null;
        streamSynch = streamsynch;
        try
        {
            setSource(new RTPPacketReceiver(rtpconnector.getControlInputStream()));
        }
        catch(IOException ioexception)
        {
            ioexception.printStackTrace();
        }
        stats = overallstats;
    }

    public RTCPRawReceiver(RTPPushDataSource rtppushdatasource, OverallStats overallstats, StreamSynch streamsynch)
    {
        stats = null;
        streamSynch = streamsynch;
        setSource(new RTPPacketReceiver(rtppushdatasource));
        stats = overallstats;
    }

    public RTCPRawReceiver(SessionAddress sessionaddress, SessionAddress sessionaddress1, OverallStats overallstats, StreamSynch streamsynch, DatagramSocket datagramsocket)
        throws UnknownHostException, IOException, SocketException
    {
        stats = null;
        streamSynch = streamsynch;
        stats = overallstats;
        UDPPacketReceiver udppacketreceiver = new UDPPacketReceiver(sessionaddress.getControlPort(), sessionaddress.getControlHostAddress(), sessionaddress1.getControlPort(), sessionaddress1.getControlHostAddress(), 1000, datagramsocket);
        setSource(udppacketreceiver);
        socket = udppacketreceiver.getSocket();
    }

    public void close()
    {
        if(socket != null)
        {
            socket.close();
        }
        if(getSource() instanceof RTPPacketReceiver)
        {
            getSource().closeSource();
        }
    }

    public String filtername()
    {
        return "RTCP Raw Receiver";
    }

    public Packet handlePacket(Packet packet)
    {
        stats.update(11, 1);
        stats.update(19, packet.length);
        RTCPPacket rtcppacket;
        try
        {
            rtcppacket = parse(packet);
        }
        catch(BadFormatException _ex)
        {
            stats.update(13, 1);
            return null;
        }
        return rtcppacket;
    }

    public Packet handlePacket(Packet packet, int i)
    {
        return null;
    }

    public Packet handlePacket(Packet packet, SessionAddress sessionaddress)
    {
        return null;
    }

    public Packet handlePacket(Packet packet, SessionAddress sessionaddress, boolean flag)
    {
        return null;
    }

    public RTCPPacket parse(Packet packet)
        throws BadFormatException
    {
        RTCPCompoundPacket rtcpcompoundpacket = new RTCPCompoundPacket(packet);
        Vector vector = new Vector(2);
        DataInputStream datainputstream = new DataInputStream(new ByteArrayInputStream(((Packet) (rtcpcompoundpacket)).data, ((Packet) (rtcpcompoundpacket)).offset, ((Packet) (rtcpcompoundpacket)).length));
        try
        {
            int j;
            for(int i = 0; i < ((Packet) (rtcpcompoundpacket)).length; i += j)
            {
                int k = datainputstream.readUnsignedByte();
                if((k & 0xc0) != 128)
                {
                	GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, "Bad RTCP packet format: bad version");
                    throw new BadFormatException();
                }
                int l = datainputstream.readUnsignedByte();
                j = datainputstream.readUnsignedShort();
                j = j + 1 << 2;
                int i1 = 0;
                if(i + j > ((Packet) (rtcpcompoundpacket)).length)
                {
	            	GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, "Bad RTCP packet format: bad length");
                    throw new BadFormatException();
                }
                if(i + j == ((Packet) (rtcpcompoundpacket)).length)
                {
                    if((k & 0x20) != 0)
                    {
                        i1 = ((Packet) (rtcpcompoundpacket)).data[(((Packet) (rtcpcompoundpacket)).offset + ((Packet) (rtcpcompoundpacket)).length) - 1] & 0xff;
                        if(i1 == 0)
                        {
		                	GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, "Bad RTCP packet format");
                            throw new BadFormatException();
                        }
                    }
                } else
                if((k & 0x20) != 0)
                {
                	GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, "Bad RTCP packet format: P != 0");
                    throw new BadFormatException();
                }
                int j1 = j - i1;
                k &= 0x1f;
                Packet obj=null;
                switch(l)
                {
                case 200: 
                    stats.update(12, 1);
                    if(j1 != 28 + 24 * k)
                    {
                        stats.update(18, 1);
	                    GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, "Bad format for SR event");
                        throw new BadFormatException();
                    }
                    RTCPSRPacket rtcpsrpacket = new RTCPSRPacket(rtcpcompoundpacket);
                    obj = rtcpsrpacket;
                    rtcpsrpacket.ssrc = datainputstream.readInt();
                    rtcpsrpacket.ntptimestampmsw = (long)datainputstream.readInt() & 0xffffffffL;
                    rtcpsrpacket.ntptimestamplsw = (long)datainputstream.readInt() & 0xffffffffL;
                    rtcpsrpacket.rtptimestamp = (long)datainputstream.readInt() & 0xffffffffL;
                    rtcpsrpacket.packetcount = (long)datainputstream.readInt() & 0xffffffffL;
                    rtcpsrpacket.octetcount = (long)datainputstream.readInt() & 0xffffffffL;
                    rtcpsrpacket.reports = new RTCPReportBlock[k];
                    streamSynch.update(rtcpsrpacket.ssrc, rtcpsrpacket.rtptimestamp, rtcpsrpacket.ntptimestampmsw, rtcpsrpacket.ntptimestamplsw);
                    for(int k1 = 0; k1 < rtcpsrpacket.reports.length; k1++)
                    {
                        RTCPReportBlock rtcpreportblock = new RTCPReportBlock();
                        rtcpsrpacket.reports[k1] = rtcpreportblock;
                        rtcpreportblock.ssrc = datainputstream.readInt();
                        long l2 = datainputstream.readInt();
                        l2 &= 0xffffffffL;
                        rtcpreportblock.fractionlost = (int)(l2 >> 24);
                        rtcpreportblock.packetslost = (int)(l2 & 0xffffffL);
                        rtcpreportblock.lastseq = (long)datainputstream.readInt() & 0xffffffffL;
                        rtcpreportblock.jitter = datainputstream.readInt();
                        rtcpreportblock.lsr = (long)datainputstream.readInt() & 0xffffffffL;
                        rtcpreportblock.dlsr = (long)datainputstream.readInt() & 0xffffffffL;
                    }
                    break;

                case 201: 
                    if(j1 != 8 + 24 * k)
                    {
                        stats.update(15, 1);
                        throw new BadFormatException();
                    }
                    RTCPRRPacket rtcprrpacket = new RTCPRRPacket(rtcpcompoundpacket);
                    obj = rtcprrpacket;
                    rtcprrpacket.ssrc = datainputstream.readInt();
                    rtcprrpacket.reports = new RTCPReportBlock[k];
                    for(int l1 = 0; l1 < rtcprrpacket.reports.length; l1++)
                    {
                        RTCPReportBlock rtcpreportblock1 = new RTCPReportBlock();
                        rtcprrpacket.reports[l1] = rtcpreportblock1;
                        rtcpreportblock1.ssrc = datainputstream.readInt();
                        long l3 = datainputstream.readInt();
                        l3 &= 0xffffffffL;
                        rtcpreportblock1.fractionlost = (int)(l3 >> 24);
                        rtcpreportblock1.packetslost = (int)(l3 & 0xffffffL);
                        rtcpreportblock1.lastseq = (long)datainputstream.readInt() & 0xffffffffL;
                        rtcpreportblock1.jitter = datainputstream.readInt();
                        rtcpreportblock1.lsr = (long)datainputstream.readInt() & 0xffffffffL;
                        rtcpreportblock1.dlsr = (long)datainputstream.readInt() & 0xffffffffL;
                    }
                    GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "RR RTCP packet received, packet=" + rtcprrpacket.toString());        
                    break;

                case 202: 
                    RTCPSDESPacket rtcpsdespacket = new RTCPSDESPacket(rtcpcompoundpacket);
                    obj = rtcpsdespacket;
                    rtcpsdespacket.sdes = new RTCPSDES[k];
                    int i2 = 4;
                    for(int j2 = 0; j2 < rtcpsdespacket.sdes.length; j2++)
                    {
                        RTCPSDES rtcpsdes = new RTCPSDES();
                        rtcpsdespacket.sdes[j2] = rtcpsdes;
                        rtcpsdes.ssrc = datainputstream.readInt();
                        i2 += 5;
                        Vector vector1 = new Vector();
                        boolean flag = false;
                        int j3;
                        while((j3 = datainputstream.readUnsignedByte()) != 0) 
                        {
                            if(j3 < 1 || j3 > 8)
                            {
                                stats.update(16, 1);
                                throw new BadFormatException();
                            }
                            if(j3 == 1)
                            {
                                flag = true;
                            }
                            RTCPSDESItem rtcpsdesitem = new RTCPSDESItem();
                            vector1.addElement(rtcpsdesitem);
                            rtcpsdesitem.type = j3;
                            int k3 = datainputstream.readUnsignedByte();
                            rtcpsdesitem.data = new byte[k3];
                            datainputstream.readFully(rtcpsdesitem.data);
                            i2 += 2 + k3;
                        }
                        if(!flag)
                        {
                            stats.update(16, 1);
		                    GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "Bad format exception (flag=false)");
                            throw new BadFormatException();
                        }
                        rtcpsdes.items = new RTCPSDESItem[vector1.size()];
                        vector1.copyInto(rtcpsdes.items);
                        if((i2 & 3) != 0)
                        {
                            datainputstream.skip(4 - (i2 & 3));
                            i2 = i2 + 3 & -4;
                        }
                    }
                    GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "SDES RTCP packet received, packet=", rtcpsdespacket.toString());        

                    if(j1 != i2)
                    {
                        stats.update(16, 1);
	                    GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, "Bad format exception (j1 != i2)");
                        throw new BadFormatException();
                    }
                    break;

                case 203: 
                    RTCPBYEPacket rtcpbyepacket = new RTCPBYEPacket(rtcpcompoundpacket);
                    obj = rtcpbyepacket;
                    rtcpbyepacket.ssrc = new int[k];
                    for(int k2 = 0; k2 < rtcpbyepacket.ssrc.length; k2++)
                    {
                        rtcpbyepacket.ssrc[k2] = datainputstream.readInt();
                    }

                    int i3;
                    if(j1 > 4 + 4 * k)
                    {
                        i3 = datainputstream.readUnsignedByte();
                        rtcpbyepacket.reason = new byte[i3];
                        i3++;
                    } else
                    {
                        i3 = 0;
                        rtcpbyepacket.reason = new byte[0];
                    }
                    i3 = i3 + 3 & -4;
                    if(j1 != 4 + 4 * k + i3)
                    {
                        stats.update(17, 1);
                        throw new BadFormatException();
                    }
                    datainputstream.readFully(rtcpbyepacket.reason);
                    datainputstream.skip(i3 - rtcpbyepacket.reason.length);
                    GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "BYE RTCP packet received, packet=", rtcpbyepacket.toString());        
                    break;

                case 204: 
                    if(j1 < 12)
                    {
                        throw new BadFormatException();
                    }
                    RTCPAPPPacket rtcpapppacket = new RTCPAPPPacket(rtcpcompoundpacket);
                    obj = rtcpapppacket;
                    rtcpapppacket.ssrc = datainputstream.readInt();
                    rtcpapppacket.name = datainputstream.readInt();
                    rtcpapppacket.subtype = k;
                    rtcpapppacket.data = new byte[j1 - 12];
                    datainputstream.readFully(rtcpapppacket.data);
                    datainputstream.skip(j1 - 12 - rtcpapppacket.data.length);
                    GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, rtcpapppacket.toString());        
                    break;

                default:
                    stats.update(14, 1);
                    throw new BadFormatException();
                }

                obj.offset = i;
                obj.length = j;
                vector.addElement(obj);
                datainputstream.skipBytes(i1);
            }

        }
        catch(EOFException _ex)
        {
            throw new BadFormatException("Unexpected end of RTCP packet");
        }
        catch(IOException _ex)
        {
            throw new IllegalArgumentException("Impossible Exception");
        }
        rtcpcompoundpacket.packets = new RTCPPacket[vector.size()];
        vector.copyInto(rtcpcompoundpacket.packets);
        return rtcpcompoundpacket;
    }
}
