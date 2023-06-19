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
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.media.rtp.ReceiveStream;
import javax.media.rtp.event.ActiveReceiveStreamEvent;
import javax.media.rtp.event.ApplicationEvent;
import javax.media.rtp.event.ByeEvent;
import javax.media.rtp.event.NewParticipantEvent;
import javax.media.rtp.event.ReceiverReportEvent;
import javax.media.rtp.event.SenderReportEvent;
import javax.media.rtp.event.StreamMappedEvent;
import javax.media.rtp.rtcp.ReceiverReport;
import javax.media.rtp.rtcp.SenderReport;

import com.sun.media.rtp.util.Packet;
import com.sun.media.rtp.util.PacketConsumer;
import com.sun.media.rtp.util.PacketForwarder;
import com.sun.media.rtp.util.PacketSource;
import com.sun.media.rtp.util.UDPPacket;

/**
 * Adaptation of the JMF implementation to support RTCP APP events
 *  
 * @author JM. Auffret
 */ 
public class RTCPReceiver implements PacketConsumer
{   
    private static final int SR = 1;
    private static final int RR = 2;
    private boolean rtcpstarted;
    private boolean sentrecvstrmap;
    SSRCCache cache;
    private int type;

    public RTCPReceiver(SSRCCache ssrccache)
    {
        rtcpstarted = false;
        sentrecvstrmap = false;
        type = 0;
        cache = ssrccache;
        SSRCInfo ssrcinfo = ssrccache.lookup(ssrccache.ourssrc.ssrc);
    }

    public RTCPReceiver(SSRCCache ssrccache, int i, String s, StreamSynch streamsynch)
        throws UnknownHostException, IOException
    {
        this(ssrccache, ((PacketSource) (new RTCPRawReceiver(i | 1, s, ssrccache.sm.defaultstats, streamsynch))));
    }

    public RTCPReceiver(SSRCCache ssrccache, PacketSource packetsource)
    {
        this(ssrccache);
        PacketForwarder packetforwarder = new PacketForwarder(packetsource, this);
        packetforwarder.startPF();
    }

    public RTCPReceiver(SSRCCache ssrccache, DatagramSocket datagramsocket, StreamSynch streamsynch)
    {
        this(ssrccache, ((PacketSource) (new RTCPRawReceiver(datagramsocket, ssrccache.sm.defaultstats, streamsynch))));
    }

    public void closeConsumer()
    {
    }

    public String consumerString()
    {
        return "RTCP Packet Receiver/Collector";
    }

    public void sendTo(RTCPPacket rtcppacket)
    {
        SSRCInfo ssrcinfo = null;
        boolean flag = cache.sm.isUnicast();
        if(flag)
        {
            if(!rtcpstarted)
            {
                cache.sm.startRTCPReports(((UDPPacket)rtcppacket.base).remoteAddress);
                rtcpstarted = true;
                byte abyte0[] = cache.sm.controladdress.getAddress();
                int i = abyte0[3] & 0xff;
                if((i & 0xff) == 255)
                {
                    cache.sm.addUnicastAddr(cache.sm.controladdress);
                } else
                {
                    InetAddress inetaddress = null;
                    boolean flag1 = true;
                    try
                    {
                        inetaddress = InetAddress.getLocalHost();
                    }
                    catch(UnknownHostException _ex)
                    {
                        flag1 = false;
                    }
                    if(flag1)
                    {
                        cache.sm.addUnicastAddr(inetaddress);
                    }
                }
            } else
            if(!cache.sm.isSenderDefaultAddr(((UDPPacket)rtcppacket.base).remoteAddress))
            {
                cache.sm.addUnicastAddr(((UDPPacket)rtcppacket.base).remoteAddress);
            }
        }
        switch(rtcppacket.type)
        {
        default:
            break;

        case -1: 
            RTCPCompoundPacket rtcpcompoundpacket = (RTCPCompoundPacket)rtcppacket;
            cache.updateavgrtcpsize(((Packet) (rtcpcompoundpacket)).length);
            for(int j = 0; j < rtcpcompoundpacket.packets.length; j++)
            {
                sendTo(rtcpcompoundpacket.packets[j]);
            }

            if(cache.sm.cleaner != null)
            {
                cache.sm.cleaner.setClean();
            }
            break;

        case 200: 
            RTCPSRPacket rtcpsrpacket = (RTCPSRPacket)rtcppacket;
            type = 1;
            if(rtcppacket.base instanceof UDPPacket)
            {
                ssrcinfo = cache.get(rtcpsrpacket.ssrc, ((UDPPacket)rtcppacket.base).remoteAddress, ((UDPPacket)rtcppacket.base).remotePort, 1);
            } else
            {
                ssrcinfo = cache.get(rtcpsrpacket.ssrc, null, 0, 1);
            }
            if(ssrcinfo == null)
            {
                break;
            }
            ssrcinfo.setAlive(true);
            ssrcinfo.lastSRntptimestamp = (rtcpsrpacket.ntptimestampmsw << 32) + rtcpsrpacket.ntptimestamplsw;
            ssrcinfo.lastSRrtptimestamp = rtcpsrpacket.rtptimestamp;
            ssrcinfo.lastSRreceiptTime = ((Packet) (rtcpsrpacket)).receiptTime;
            ssrcinfo.lastRTCPreceiptTime = ((Packet) (rtcpsrpacket)).receiptTime;
            ssrcinfo.lastHeardFrom = ((Packet) (rtcpsrpacket)).receiptTime;
            if(ssrcinfo.quiet)
            {
                ssrcinfo.quiet = false;
                ActiveReceiveStreamEvent activereceivestreamevent = null;
                if(ssrcinfo instanceof ReceiveStream)
                {
                    activereceivestreamevent = new ActiveReceiveStreamEvent(cache.sm, ssrcinfo.sourceInfo, (ReceiveStream)ssrcinfo);
                } else
                {
                    activereceivestreamevent = new ActiveReceiveStreamEvent(cache.sm, ssrcinfo.sourceInfo, null);
                }
                cache.eventhandler.postEvent(activereceivestreamevent);
            }
            ssrcinfo.lastSRpacketcount = rtcpsrpacket.packetcount;
            ssrcinfo.lastSRoctetcount = rtcpsrpacket.octetcount;
            for(int k = 0; k < rtcpsrpacket.reports.length; k++)
            {
                rtcpsrpacket.reports[k].receiptTime = ((Packet) (rtcpsrpacket)).receiptTime;
                int l = rtcpsrpacket.reports[k].ssrc;
                RTCPReportBlock artcpreportblock[] = (RTCPReportBlock[])ssrcinfo.reports.get(l);
                if(artcpreportblock == null)
                {
                    artcpreportblock = new RTCPReportBlock[2];
                    artcpreportblock[0] = rtcpsrpacket.reports[k];
                    ssrcinfo.reports.put(l, artcpreportblock);
                } else
                {
                    artcpreportblock[1] = artcpreportblock[0];
                    artcpreportblock[0] = rtcpsrpacket.reports[k];
                }
            }

            if(ssrcinfo.probation > 0)
            {
                break;
            }
            if(!ssrcinfo.newpartsent && ssrcinfo.sourceInfo != null)
            {
                NewParticipantEvent newparticipantevent = new NewParticipantEvent(cache.sm, ssrcinfo.sourceInfo);
                cache.eventhandler.postEvent(newparticipantevent);
                ssrcinfo.newpartsent = true;
            }
            if(!ssrcinfo.recvstrmap && ssrcinfo.sourceInfo != null)
            {
                ssrcinfo.recvstrmap = true;
                StreamMappedEvent streammappedevent = new StreamMappedEvent(cache.sm, (ReceiveStream)ssrcinfo, ssrcinfo.sourceInfo);
                cache.eventhandler.postEvent(streammappedevent);
            }
            SenderReportEvent senderreportevent = new SenderReportEvent(cache.sm, (SenderReport)ssrcinfo);
            cache.eventhandler.postEvent(senderreportevent);
            break;

        case 201: 
            RTCPRRPacket rtcprrpacket = (RTCPRRPacket)rtcppacket;
            type = 2;
            if(rtcppacket.base instanceof UDPPacket)
            {
                ssrcinfo = cache.get(rtcprrpacket.ssrc, ((UDPPacket)rtcppacket.base).remoteAddress, ((UDPPacket)rtcppacket.base).remotePort, 2);
            } else
            {
                ssrcinfo = cache.get(rtcprrpacket.ssrc, null, 0, 2);
            }
            if(ssrcinfo == null)
            {
                break;
            }
            ssrcinfo.setAlive(true);
            ssrcinfo.lastRTCPreceiptTime = ((Packet) (rtcprrpacket)).receiptTime;
            ssrcinfo.lastHeardFrom = ((Packet) (rtcprrpacket)).receiptTime;
            if(ssrcinfo.quiet)
            {
                ssrcinfo.quiet = false;
                ActiveReceiveStreamEvent activereceivestreamevent1 = null;
                if(ssrcinfo instanceof ReceiveStream)
                {
                    activereceivestreamevent1 = new ActiveReceiveStreamEvent(cache.sm, ssrcinfo.sourceInfo, (ReceiveStream)ssrcinfo);
                } else
                {
                    activereceivestreamevent1 = new ActiveReceiveStreamEvent(cache.sm, ssrcinfo.sourceInfo, null);
                }
                cache.eventhandler.postEvent(activereceivestreamevent1);
            }
            for(int i1 = 0; i1 < rtcprrpacket.reports.length; i1++)
            {
                rtcprrpacket.reports[i1].receiptTime = ((Packet) (rtcprrpacket)).receiptTime;
                int j1 = rtcprrpacket.reports[i1].ssrc;
                RTCPReportBlock artcpreportblock1[] = (RTCPReportBlock[])ssrcinfo.reports.get(j1);
                if(artcpreportblock1 == null)
                {
                    artcpreportblock1 = new RTCPReportBlock[2];
                    artcpreportblock1[0] = rtcprrpacket.reports[i1];
                    ssrcinfo.reports.put(j1, artcpreportblock1);
                } else
                {
                    artcpreportblock1[1] = artcpreportblock1[0];
                    artcpreportblock1[0] = rtcprrpacket.reports[i1];
                }
            }

            if(!ssrcinfo.newpartsent && ssrcinfo.sourceInfo != null)
            {
                NewParticipantEvent newparticipantevent1 = new NewParticipantEvent(cache.sm, ssrcinfo.sourceInfo);
                cache.eventhandler.postEvent(newparticipantevent1);
                ssrcinfo.newpartsent = true;
            }
            ReceiverReportEvent receiverreportevent = new ReceiverReportEvent(cache.sm, (ReceiverReport)ssrcinfo);
            cache.eventhandler.postEvent(receiverreportevent);
            break;

        case 202: 
            RTCPSDESPacket rtcpsdespacket = (RTCPSDESPacket)rtcppacket;
            for(int k1 = 0; k1 < rtcpsdespacket.sdes.length; k1++)
            {
                RTCPSDES rtcpsdes = rtcpsdespacket.sdes[k1];
                if(type == 1)
                {
                    if(rtcppacket.base instanceof UDPPacket)
                    {
                        ssrcinfo = cache.get(rtcpsdes.ssrc, ((UDPPacket)rtcppacket.base).remoteAddress, ((UDPPacket)rtcppacket.base).remotePort, 1);
                    } else
                    {
                        ssrcinfo = cache.get(rtcpsdes.ssrc, null, 0, 1);
                    }
                }
                if(type == 2)
                {
                    if(rtcppacket.base instanceof UDPPacket)
                    {
                        ssrcinfo = cache.get(rtcpsdes.ssrc, ((UDPPacket)rtcppacket.base).remoteAddress, ((UDPPacket)rtcppacket.base).remotePort, 2);
                    } else
                    {
                        ssrcinfo = cache.get(rtcpsdes.ssrc, null, 0, 2);
                    }
                }
                if(ssrcinfo == null)
                {
                    break;
                }
                ssrcinfo.setAlive(true);
                ssrcinfo.lastHeardFrom = ((Packet) (rtcpsdespacket)).receiptTime;
                ssrcinfo.addSDESInfo(rtcpsdes);
            }

            if(ssrcinfo != null && !ssrcinfo.newpartsent && ssrcinfo.sourceInfo != null)
            {
                NewParticipantEvent newparticipantevent2 = new NewParticipantEvent(cache.sm, ssrcinfo.sourceInfo);
                cache.eventhandler.postEvent(newparticipantevent2);
                ssrcinfo.newpartsent = true;
            }
            if(ssrcinfo != null && !ssrcinfo.recvstrmap && ssrcinfo.sourceInfo != null && (ssrcinfo instanceof RecvSSRCInfo))
            {
                ssrcinfo.recvstrmap = true;
                StreamMappedEvent streammappedevent1 = new StreamMappedEvent(cache.sm, (ReceiveStream)ssrcinfo, ssrcinfo.sourceInfo);
                cache.eventhandler.postEvent(streammappedevent1);
            }
            type = 0;
            break;

        case 203: 
            RTCPBYEPacket rtcpbyepacket = (RTCPBYEPacket)rtcppacket;
            SSRCInfo ssrcinfo1;
            if(rtcppacket.base instanceof UDPPacket)
            {
                ssrcinfo1 = cache.get(rtcpbyepacket.ssrc[0], ((UDPPacket)rtcppacket.base).remoteAddress, ((UDPPacket)rtcppacket.base).remotePort);
            } else
            {
                ssrcinfo1 = cache.get(rtcpbyepacket.ssrc[0], null, 0);
            }
            for(int l1 = 0; l1 < rtcpbyepacket.ssrc.length; l1++)
            {
                if(rtcppacket.base instanceof UDPPacket)
                {
                    ssrcinfo1 = cache.get(rtcpbyepacket.ssrc[l1], ((UDPPacket)rtcppacket.base).remoteAddress, ((UDPPacket)rtcppacket.base).remotePort);
                } else
                {
                    ssrcinfo1 = cache.get(rtcpbyepacket.ssrc[l1], null, 0);
                }
                if(ssrcinfo1 == null)
                {
                    break;
                }
                if(!cache.byestate)
                {
                    ssrcinfo1.setAlive(false);
                    ssrcinfo1.byeReceived = true;
                    ssrcinfo1.byeTime = ((Packet) (rtcppacket)).receiptTime;
                    ssrcinfo1.lastHeardFrom = ((Packet) (rtcpbyepacket)).receiptTime;
                }
            }

            if(ssrcinfo1 == null)
            {
                break;
            }
            if(ssrcinfo1.quiet)
            {
                ssrcinfo1.quiet = false;
                ActiveReceiveStreamEvent activereceivestreamevent2 = null;
                if(ssrcinfo1 instanceof ReceiveStream)
                {
                    activereceivestreamevent2 = new ActiveReceiveStreamEvent(cache.sm, ssrcinfo1.sourceInfo, (ReceiveStream)ssrcinfo1);
                } else
                {
                    activereceivestreamevent2 = new ActiveReceiveStreamEvent(cache.sm, ssrcinfo1.sourceInfo, null);
                }
                cache.eventhandler.postEvent(activereceivestreamevent2);
            }
            ssrcinfo1.byereason = new String(rtcpbyepacket.reason);
            if(ssrcinfo1.byeReceived)
            {
                break;
            }
            boolean flag2 = false;
            RTPSourceInfo rtpsourceinfo = ssrcinfo1.sourceInfo;
            if(rtpsourceinfo != null && rtpsourceinfo.getStreamCount() == 0)
            {
                flag2 = true;
            }
            ByeEvent byeevent = null;
            if(ssrcinfo1 instanceof RecvSSRCInfo)
            {
                byeevent = new ByeEvent(cache.sm, ssrcinfo1.sourceInfo, (ReceiveStream)ssrcinfo1, new String(rtcpbyepacket.reason), flag2);
            }
            if(ssrcinfo1 instanceof PassiveSSRCInfo)
            {
                byeevent = new ByeEvent(cache.sm, ssrcinfo1.sourceInfo, null, new String(rtcpbyepacket.reason), flag2);
            }
            cache.eventhandler.postEvent(byeevent);
            break;

        case 204: 
            RTCPAPPPacket rtcpapppacket = (RTCPAPPPacket)rtcppacket;
            SSRCInfo ssrcinfo2;
            if(rtcppacket.base instanceof UDPPacket)
            {
                ssrcinfo2 = cache.get(rtcpapppacket.ssrc, ((UDPPacket)rtcppacket.base).remoteAddress, ((UDPPacket)rtcppacket.base).remotePort);
            } else
            {
                ssrcinfo2 = cache.get(rtcpapppacket.ssrc, null, 0);
            }
            if(ssrcinfo2 == null)
            {
            	ApplicationEvent applicationevent = new ApplicationEvent(
						cache.sm, null,
						null, rtcpapppacket.subtype, null,
						rtcpapppacket.data);
				cache.eventhandler.postEvent(applicationevent);
                break;
            }
            ssrcinfo2.lastHeardFrom = ((Packet) (rtcpapppacket)).receiptTime;
            if(ssrcinfo2.quiet)
            {
                ssrcinfo2.quiet = false;
                ActiveReceiveStreamEvent activereceivestreamevent3 = null;
                if(ssrcinfo2 instanceof ReceiveStream)
                {
                    activereceivestreamevent3 = new ActiveReceiveStreamEvent(cache.sm, ssrcinfo2.sourceInfo, (ReceiveStream)ssrcinfo2);
                } else
                {
                    activereceivestreamevent3 = new ActiveReceiveStreamEvent(cache.sm, ssrcinfo2.sourceInfo, null);
                }
                cache.eventhandler.postEvent(activereceivestreamevent3);
            }
            ApplicationEvent applicationevent = null;
            if(ssrcinfo2 instanceof RecvSSRCInfo)
            {
                applicationevent = new ApplicationEvent(cache.sm, ssrcinfo2.sourceInfo, (ReceiveStream)ssrcinfo2, rtcpapppacket.subtype, null, rtcpapppacket.data);
            }
            if(ssrcinfo2 instanceof PassiveSSRCInfo)
            {
                applicationevent = new ApplicationEvent(cache.sm, ssrcinfo2.sourceInfo, null, rtcpapppacket.subtype, null, rtcpapppacket.data);
            }
            cache.eventhandler.postEvent(applicationevent);
            break;
        }
    }

    public void sendTo(Packet packet)
    {
        sendTo((RTCPPacket)packet);
    }
}
