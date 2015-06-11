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
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.rtp.jmf.MsgRtp;
import com.sun.media.Log;
import com.sun.media.rtp.util.BadFormatException;
import com.sun.media.rtp.util.Packet;
import com.sun.media.rtp.util.PacketFilter;
import com.sun.media.rtp.util.RTPPacket;
import com.sun.media.rtp.util.RTPPacketReceiver;
import com.sun.media.rtp.util.UDPPacketReceiver;
import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import javax.media.rtp.*;

// Referenced classes of package com.sun.media.rtp:
//            OverallStats

public class RTPRawReceiver extends PacketFilter
{

    /**
     * Stack connection object
     */
    private Channel channel = null;    

    private OverallStats stats;
    private boolean recvBufSizeSet;
    public DatagramSocket socket;
    private RTPConnector rtpConnector;

    public RTPRawReceiver(Channel conn)
    {
        stats = null;
        recvBufSizeSet = false;
        rtpConnector = null;
        this.channel = conn;
    }

    public RTPRawReceiver(Channel conn, int i, String s, OverallStats overallstats)
        throws UnknownHostException, IOException, SocketException
    {
        this(conn);
        stats = null;
        recvBufSizeSet = false;
        rtpConnector = null;
        UDPPacketReceiver udppacketreceiver;
        setSource(udppacketreceiver = new UDPPacketReceiver(i & -2, s, -1, null, 2000, null));
        socket = udppacketreceiver.getSocket();
        stats = overallstats;
    }

    public RTPRawReceiver(Channel conn, DatagramSocket datagramsocket, OverallStats overallstats)
    {
        stats = null;
        recvBufSizeSet = false;
        rtpConnector = null;
        setSource(new UDPPacketReceiver(datagramsocket, 2000));
        stats = overallstats;
    }

    public RTPRawReceiver(Channel conn, RTPConnector rtpconnector, OverallStats overallstats)
    {
        this(conn);
        stats = null;
        recvBufSizeSet = false;
        rtpConnector = null;
        try
        {
            setSource(new RTPPacketReceiver(rtpconnector.getDataInputStream()));
        }
        catch(IOException ioexception)
        {
            ioexception.printStackTrace();
        }
        rtpConnector = rtpconnector;
        stats = overallstats;
    }

    public RTPRawReceiver(Channel conn, RTPPushDataSource rtppushdatasource, OverallStats overallstats)
    {
        this(conn);
        stats = null;
        recvBufSizeSet = false;
        rtpConnector = null;
        setSource(new RTPPacketReceiver(rtppushdatasource));
        stats = overallstats;
    }

    public RTPRawReceiver(Channel conn, SessionAddress sessionaddress, SessionAddress sessionaddress1, OverallStats overallstats, DatagramSocket datagramsocket)
        throws UnknownHostException, IOException, SocketException
    {
        this(conn);
        stats = null;
        recvBufSizeSet = false;
        rtpConnector = null;
        stats = overallstats;
        UDPPacketReceiver udppacketreceiver = new UDPPacketReceiver(sessionaddress.getDataPort(), sessionaddress.getDataHostAddress(), sessionaddress1.getDataPort(), sessionaddress1.getDataHostAddress(), 2000, datagramsocket);
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
        return "RTP Raw Packet Receiver";
    }

    public int getRecvBufSize()
    {
        try
        {
            if(socket != null)
            {
                Class class1 = socket.getClass();
                Method method = class1.getMethod("getReceiveBufferSize", (java.lang.Class[]) null);
                Integer integer = (Integer)method.invoke(socket, (java.lang.Object[]) null);
                return integer.intValue();
            }
            if(rtpConnector != null)
            {
                return rtpConnector.getReceiveBufferSize();
            }
        }
        catch(Exception _ex) { }
        return -1;
    }

    public Packet handlePacket(Packet packet)
    {
        stats.update(0, 1);
        stats.update(1, packet.length);
        RTPPacket rtppacket;
        try
        {
            rtppacket = parse(packet);
        }
        catch(BadFormatException _ex)
        {
            stats.update(2, 1);
            return null;
        }
        if(!recvBufSizeSet)
        {
            recvBufSizeSet = true;
            switch(rtppacket.payloadType)
            {
            case 14: // '\016'
            case 26: // '\032'
            case 34: // '"'
            case 42: // '*'
                setRecvBufSize(64000);
                break;

            case 31: // '\037'
                setRecvBufSize(0x1f400);
                break;

            case 32: // ' '
                setRecvBufSize(0x1f400);
                break;

            default:
                if(rtppacket.payloadType >= 96 && rtppacket.payloadType <= 127)
                {
                    setRecvBufSize(64000);
                }
                break;
            }
        }
        
        // FH provisoire
        // Call back vers la Stack generic
        try {
            MsgRtp msgRtp = new MsgRtp(null);
            msgRtp.add((RTPPacket) rtppacket);                                               
            
            // InetSocketAddress remoteAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
            // ConnRtp connRtp = new ConnRtp(socket.getLocalAddress().getHostAddress(), socket.getLocalPort(), remoteAddress.getHostName(), remoteAddress.getPort());
            msgRtp.setChannel(channel);
            
            StackFactory.getStack(StackFactory.PROTOCOL_RTP).receiveMessage(msgRtp);
        }
        catch (Exception e){
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Exception while receiving message");
        }
        
        return rtppacket;
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

    public RTPPacket parse(Packet packet)
        throws BadFormatException
    {
        RTPPacket rtppacket = new RTPPacket(packet);
        DataInputStream datainputstream = new DataInputStream(new ByteArrayInputStream(((Packet) (rtppacket)).data, ((Packet) (rtppacket)).offset, ((Packet) (rtppacket)).length));
        try
        {
            int i = datainputstream.readUnsignedByte();
            if((i & 0xc0) != 128)
            {
                throw new BadFormatException();
            }
            if((i & 0x10) != 0)
            {
                rtppacket.extensionPresent = true;
            }
            int j = 0;
            if((i & 0x20) != 0)
            {
                j = ((Packet) (rtppacket)).data[(((Packet) (rtppacket)).offset + ((Packet) (rtppacket)).length) - 1] & 0xff;
            }
            i &= 0xf;
            rtppacket.payloadType = datainputstream.readUnsignedByte();
            rtppacket.marker = rtppacket.payloadType >> 7;
            rtppacket.payloadType &= 0x7f;
            rtppacket.seqnum = datainputstream.readUnsignedShort();
            rtppacket.timestamp = (long)datainputstream.readInt() & 0xffffffffL;
            rtppacket.ssrc = datainputstream.readInt();
            int k = 0;
            if(rtppacket.extensionPresent)
            {
                rtppacket.extensionType = datainputstream.readUnsignedShort();
                int l = datainputstream.readUnsignedShort();
                l <<= 2;
                rtppacket.extension = new byte[l];
                datainputstream.readFully(rtppacket.extension);
                k += l + 4;
            }
            rtppacket.csrc = new int[i];
            for(int i1 = 0; i1 < rtppacket.csrc.length; i1++)
            {
                rtppacket.csrc[i1] = datainputstream.readInt();
            }

            k += 12 + (rtppacket.csrc.length << 2);
            rtppacket.payloadlength = ((Packet) (rtppacket)).length - (k + j);
            if(rtppacket.payloadlength < 1)
            {
                throw new BadFormatException();
            }
            rtppacket.payloadoffset = k + ((Packet) (rtppacket)).offset;
        }
        catch(EOFException _ex)
        {
            throw new BadFormatException("Unexpected end of RTP packet");
        }
        catch(IOException _ex)
        {
            throw new IllegalArgumentException("Impossible Exception");
        }
        return rtppacket;
    }

    public void setRecvBufSize(int i)
    {
        try
        {
            if(socket != null)
            {
                Class class1 = socket.getClass();
                Method method = class1.getMethod("setReceiveBufferSize", new Class[] {
                    Integer.TYPE
                });
                method.invoke(socket, new Object[] {
                    new Integer(i)
                });
            } else
            if(rtpConnector != null)
            {
                rtpConnector.setReceiveBufferSize(i);
            }
        }
        catch(Exception exception)
        {
            Log.comment("Cannot set receive buffer size: " + exception);
        }
    }
}
