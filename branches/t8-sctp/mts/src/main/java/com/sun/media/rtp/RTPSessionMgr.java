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
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.system.OSValidator;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.media.Format;
import javax.media.control.BufferControl;
import javax.media.format.AudioFormat;
import javax.media.format.UnsupportedFormatException;
import javax.media.format.VideoFormat;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import javax.media.rtp.EncryptionInfo;
import javax.media.rtp.GlobalReceptionStats;
import javax.media.rtp.GlobalTransmissionStats;
import javax.media.rtp.InvalidSessionAddressException;
import javax.media.rtp.LocalParticipant;
import javax.media.rtp.Participant;
import javax.media.rtp.RTPConnector;
import javax.media.rtp.RTPControl;
import javax.media.rtp.RTPManager;
import javax.media.rtp.RTPPushDataSource;
import javax.media.rtp.RTPSocket;
import javax.media.rtp.RTPStream;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.RemoteListener;
import javax.media.rtp.RemoteParticipant;
import javax.media.rtp.SSRCInUseException;
import javax.media.rtp.SendStream;
import javax.media.rtp.SendStreamListener;
import javax.media.rtp.SessionAddress;
import javax.media.rtp.SessionListener;
import javax.media.rtp.SessionManager;
import javax.media.rtp.SessionManagerException;
import javax.media.rtp.event.NewSendStreamEvent;
import javax.media.rtp.event.StreamClosedEvent;
import javax.media.rtp.rtcp.SourceDescription;

import com.sun.media.JMFSecurity;
import com.sun.media.JMFSecurityManager;
import com.sun.media.protocol.rtp.DataSource;
import com.sun.media.rtp.util.PacketFilter;
import com.sun.media.rtp.util.PacketForwarder;
import com.sun.media.rtp.util.RTPPacket;
import com.sun.media.rtp.util.RTPPacketSender;
import com.sun.media.rtp.util.SSRCTable;
import com.sun.media.rtp.util.UDPPacketSender;

/**
 * Adaptation of the JMF implementation to support RTCP APP events
 *
 * @author JM. Auffret
 */
public class RTPSessionMgr extends RTPManager
        implements SessionManager
{
    /**
     * Stack connection object
     */
    private Channel channel = null;    

    private static JMFSecurity jmfSecurity = null;
    private static boolean securityPrivelege = false;    boolean bindtome;
    private SSRCCache cache;
    int ttl;
    int sendercount;
    InetAddress localDataAddress;
    int localDataPort;
    InetAddress localControlAddress;
    int localControlPort;
    InetAddress dataaddress;
    InetAddress controladdress;
    int dataport;
    int controlport;
    RTPPushDataSource rtpsource;
    RTPPushDataSource rtcpsource;
    long defaultSSRC;
    SessionAddress localSenderAddress;
    private SessionAddress localReceiverAddress;
    UDPPacketSender udpsender;
    RTPPacketSender rtpsender;
    RTCPRawSender sender;
    SSRCCacheCleaner cleaner;
    private boolean unicast;
    private boolean startedparticipating;
    private boolean nonparticipating;
    private boolean nosockets;
    private boolean started;
    private boolean initialized;
    protected Vector sessionlistener;
    protected Vector remotelistener;
    protected Vector streamlistener;
    protected Vector sendstreamlistener;
    private static final int GET_ALL_PARTICIPANTS = -1;
    boolean encryption;
    SSRCTable dslist;
    StreamSynch streamSynch;
    FormatInfo formatinfo;
    DataSource defaultsource;
    PushBufferStream defaultstream;
    Format defaultformat;
    BufferControl buffercontrol;
    OverallStats defaultstats;
    OverallTransStats transstats;
    int defaultsourceid;
    Vector sendstreamlist;
    RTPTransmitter rtpTransmitter;
    boolean bds;
    private Method m[];
    private Class cl[];
    private Object args[][];
    Vector peerlist;
    boolean multi_unicast;
    Hashtable peerrtplist;
    Hashtable peerrtcplist;
    static FormatInfo supportedList = null;
    static Vector addedList = new Vector();
    private boolean newRtpInterface;
    private SessionAddress remoteAddress;
    private SessionAddress localAddress;
    private RTCPRawReceiver rtcpRawReceiver;
    private RTPRawReceiver rtpRawReceiver;
    private PacketForwarder rtpForwarder;
    private PacketForwarder rtcpForwarder;
    private RTPDemultiplexer rtpDemultiplexer;
    private OverallStats overallStats;
    private boolean participating;
    private UDPPacketSender udpPacketSender;
    private Vector remoteAddresses;
    private RTCPTransmitter rtcpTransmitter;
    private RTPConnector rtpConnector;
    private DatagramSocket dataSocket;
    private DatagramSocket controlSocket;
    private final int MAX_PORT = 65535;
    
    /** Standard getter */
    public Channel getChannel() {
        return channel;
    }

    /** Standard setter */
    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public RTPSessionMgr()
    {
        bindtome = false;
        cache = null;
        sendercount = 0;
        localDataAddress = null;
        localDataPort = 0;
        localControlAddress = null;
        localControlPort = 0;
        dataaddress = null;
        controladdress = null;
        dataport = 0;
        controlport = 0;
        rtpsource = null;
        rtcpsource = null;
        defaultSSRC = 0L;
        udpsender = null;
        rtpsender = null;
        sender = null;
        cleaner = null;
        unicast = false;
        startedparticipating = false;
        nonparticipating = false;
        nosockets = false;
        started = false;
        initialized = false;
        sessionlistener = new Vector();
        remotelistener = new Vector();
        streamlistener = new Vector();
        sendstreamlistener = new Vector();
        encryption = false;
        dslist = new SSRCTable();
        formatinfo = null;
        defaultsource = null;
        defaultstream = null;
        defaultformat = null;
        buffercontrol = null;
        defaultstats = null;
        transstats = null;
        defaultsourceid = 0;
        sendstreamlist = new Vector(1);
        rtpTransmitter = null;
        bds = false;
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        peerlist = new Vector();
        multi_unicast = false;
        peerrtplist = new Hashtable(5);
        peerrtcplist = new Hashtable(5);
        newRtpInterface = false;
        formatinfo = new FormatInfo();
        buffercontrol = new BufferControlImpl();
        defaultstats = new OverallStats();
        transstats = new OverallTransStats();
        streamSynch = new StreamSynch();
    }
    
    public RTPSessionMgr(DataSource datasource)
    throws IOException
    {
        bindtome = false;
        cache = null;
        sendercount = 0;
        localDataAddress = null;
        localDataPort = 0;
        localControlAddress = null;
        localControlPort = 0;
        dataaddress = null;
        controladdress = null;
        dataport = 0;
        controlport = 0;
        rtpsource = null;
        rtcpsource = null;
        defaultSSRC = 0L;
        udpsender = null;
        rtpsender = null;
        sender = null;
        cleaner = null;
        unicast = false;
        startedparticipating = false;
        nonparticipating = false;
        nosockets = false;
        started = false;
        initialized = false;
        sessionlistener = new Vector();
        remotelistener = new Vector();
        streamlistener = new Vector();
        sendstreamlistener = new Vector();
        encryption = false;
        dslist = new SSRCTable();
        formatinfo = null;
        defaultsource = null;
        defaultstream = null;
        defaultformat = null;
        buffercontrol = null;
        defaultstats = null;
        transstats = null;
        defaultsourceid = 0;
        sendstreamlist = new Vector(1);
        rtpTransmitter = null;
        bds = false;
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        peerlist = new Vector();
        multi_unicast = false;
        peerrtplist = new Hashtable(5);
        peerrtcplist = new Hashtable(5);
        newRtpInterface = false;
        formatinfo = new FormatInfo();
        buffercontrol = new BufferControlImpl();
        defaultstats = new OverallStats();
        transstats = new OverallTransStats();
        UpdateEncodings(datasource);
        RTPMediaLocator rtpmedialocator = null;
        try
        {
            rtpmedialocator = new RTPMediaLocator(datasource.getLocator().toString());
        }
        catch(MalformedURLException malformedurlexception)
        {
            throw new IOException("RTP URL is Malformed " + malformedurlexception.getMessage());
        }
        DataSource datasource1 = createNewDS(rtpmedialocator);
        RTPControl rtpcontrol = (RTPControl)datasource.getControl("javax.media.rtp.RTPControl");
        datasource1.setControl(rtpcontrol);
        String s = rtpmedialocator.getSessionAddress();
        dataport = rtpmedialocator.getSessionPort();
        controlport = dataport + 1;
        ttl = rtpmedialocator.getTTL();
        try
        {
            dataaddress = InetAddress.getByName(s);
        }
        catch(Throwable throwable1)
        {
            throwable1.printStackTrace();
        }
        dataaddress = InetAddress.getByName(s);
        controladdress = dataaddress;
        SessionAddress sessionaddress = new SessionAddress();
        try
        {
            initSession(sessionaddress, setSDES(), 0.050000000000000003D, 0.25D);
        }
        catch(SessionManagerException sessionmanagerexception)
        {
            throw new IOException("SessionManager exception " + sessionmanagerexception.getMessage());
        }
    }
    
    public RTPSessionMgr(RTPPushDataSource rtppushdatasource)
    {
        bindtome = false;
        cache = null;
        sendercount = 0;
        localDataAddress = null;
        localDataPort = 0;
        localControlAddress = null;
        localControlPort = 0;
        dataaddress = null;
        controladdress = null;
        dataport = 0;
        controlport = 0;
        rtpsource = null;
        rtcpsource = null;
        defaultSSRC = 0L;
        udpsender = null;
        rtpsender = null;
        sender = null;
        cleaner = null;
        unicast = false;
        startedparticipating = false;
        nonparticipating = false;
        nosockets = false;
        started = false;
        initialized = false;
        sessionlistener = new Vector();
        remotelistener = new Vector();
        streamlistener = new Vector();
        sendstreamlistener = new Vector();
        encryption = false;
        dslist = new SSRCTable();
        formatinfo = null;
        defaultsource = null;
        defaultstream = null;
        defaultformat = null;
        buffercontrol = null;
        defaultstats = null;
        transstats = null;
        defaultsourceid = 0;
        sendstreamlist = new Vector(1);
        rtpTransmitter = null;
        bds = false;
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        peerlist = new Vector();
        multi_unicast = false;
        peerrtplist = new Hashtable(5);
        peerrtcplist = new Hashtable(5);
        newRtpInterface = false;
        nosockets = true;
        rtpsource = rtppushdatasource;
        if(rtpsource instanceof RTPSocket)
        {
            rtcpsource = ((RTPSocket)rtpsource).getControlChannel();
        }
        formatinfo = new FormatInfo();
        buffercontrol = new BufferControlImpl();
        defaultstats = new OverallStats();
        transstats = new OverallTransStats();
        DataSource datasource = createNewDS(((RTPMediaLocator) (null)));
        UpdateEncodings(rtppushdatasource);
        RTPControl rtpcontrol = (RTPControl)rtppushdatasource.getControl("javax.media.rtp.RTPControl");
        datasource.setControl(rtpcontrol);
        initSession(setSDES(), 0.050000000000000003D, 0.25D);
        startSession(rtpsource, rtcpsource, null);
    }
    
    private void CheckRTPAddress(InetAddress inetaddress, InetAddress inetaddress1)
    throws InvalidSessionAddressException
    {
        if(inetaddress == null && inetaddress1 == null)
        {
            throw new InvalidSessionAddressException("Data and control addresses are null");
        }
        if(inetaddress1 == null && inetaddress != null)
        {
            inetaddress1 = inetaddress;
        }
        if(inetaddress == null && inetaddress1 != null)
        {
            inetaddress = inetaddress1;
        }
    }
    
    private void CheckRTPPorts(int i, int j)
    throws InvalidSessionAddressException
    {
        if(i == 0 || i == -1)
        {
            i = j - 1;
        }
        if(j == 0 || j == -1)
        {
            j = i + 1;
        }
        if(i != 0 && i % 2 != 0)
        {
            throw new InvalidSessionAddressException("Data Port must be valid and even");
        }
        if(j != 0 && j % 2 != 1)
        {
            throw new InvalidSessionAddressException("Control Port must be valid and odd");
        }
        if(j != i + 1)
        {
            throw new InvalidSessionAddressException("Control Port must be one higher than the Data Port");
        }
        else
        {
            return;
        }
    }
    
    public boolean IsNonParticipating()
    {
        return nonparticipating;
    }
    
    public void UpdateEncodings(javax.media.protocol.DataSource datasource)
    {
        RTPControlImpl rtpcontrolimpl = (RTPControlImpl)datasource.getControl("javax.media.rtp.RTPControl");
        if(rtpcontrolimpl != null && rtpcontrolimpl.codeclist != null)
        {
            Integer integer;
            for(Enumeration enumeration = rtpcontrolimpl.codeclist.keys(); enumeration.hasMoreElements(); formatinfo.add(integer.intValue(), (Format)rtpcontrolimpl.codeclist.get(integer)))
            {
                integer = (Integer)enumeration.nextElement();
            }
            
        }
    }
        
    public void addFormat(Format format, int i)
    {
        if(formatinfo != null)
        {
            formatinfo.add(i, format);
        }
        if(format != null)
        {
            addedList.addElement(format);
        }
    }
    
    public void addMRL(RTPMediaLocator rtpmedialocator)
    {
        int i = (int)rtpmedialocator.getSSRC();
        if(i == 0)
        {
            return;
        }
        DataSource datasource = (DataSource)dslist.get(i);
        if(datasource != null)
        {
            return;
        }
        else
        {
            DataSource datasource1 = createNewDS(rtpmedialocator);
            return;
        }
    }
    
    public void addPeer(SessionAddress sessionaddress)
    throws IOException, InvalidSessionAddressException
    {
        for(int i = 0; i < peerlist.size(); i++)
        {
            SessionAddress sessionaddress1 = (SessionAddress)peerlist.elementAt(i);
            if(sessionaddress1.equals(sessionaddress))
            {
                return;
            }
        }
        
        peerlist.addElement(sessionaddress);
        CheckRTPPorts(sessionaddress.getDataPort(), sessionaddress.getControlPort());
        RTCPRawReceiver rtcprawreceiver = null;
        RTPRawReceiver rtprawreceiver = null;
        InetAddress inetaddress = sessionaddress.getDataAddress();
        InetAddress inetaddress1 = sessionaddress.getControlAddress();
        int j = sessionaddress.getDataPort();
        int k = sessionaddress.getControlPort();
        CheckRTPAddress(inetaddress, inetaddress1);
        InetAddress inetaddress2 = InetAddress.getLocalHost();
        if(!inetaddress.isMulticastAddress() && !inetaddress.equals(inetaddress2))
        {
            if(isBroadcast(inetaddress) && !OSValidator.isWindows())
            {
                bindtome = false;
            }
            else
            {
                bindtome = true;
            }
        }
        if(!bindtome)
        {
            try
            {
                rtcprawreceiver = new RTCPRawReceiver(k, inetaddress1.getHostAddress(), defaultstats, streamSynch);
                if(inetaddress != null)
                {
                    rtprawreceiver = new RTPRawReceiver(channel, j, inetaddress.getHostAddress(), defaultstats);
                }
            }
            catch(SocketException socketexception)
            {
                throw new IOException(socketexception.getMessage());
            }
            finally
            {
                if(inetaddress != null && rtprawreceiver == null && rtcprawreceiver != null)
                {
                    System.err.println("could not create RTCP/RTP raw receivers");
                    rtcprawreceiver.closeSource();
                }
            }
        }
        else
        {
            try
            {
                rtcprawreceiver = new RTCPRawReceiver(k, inetaddress2.getHostAddress(), defaultstats, streamSynch);
                if(inetaddress != null)
                {
                    rtprawreceiver = new RTPRawReceiver(channel, j, inetaddress2.getHostAddress(), defaultstats);
                }
            }
            catch(SocketException socketexception1)
            {
                throw new IOException(socketexception1.getMessage());
            }
            finally
            {
                if(inetaddress != null && rtprawreceiver == null && rtcprawreceiver != null)
                {
                    System.err.println("could not create RTCP/RTP raw receivers");
                    rtcprawreceiver.closeSource();
                }
            }
        }
        PacketForwarder packetforwarder = new PacketForwarder(rtcprawreceiver, new RTCPReceiver(cache));
        PacketForwarder packetforwarder1 = null;
        if(rtprawreceiver != null)
        {
            packetforwarder1 = new PacketForwarder(rtprawreceiver, new RTPReceiver(cache, rtpDemultiplexer));
        }
        packetforwarder.startPF("RTCP Forwarder for address" + inetaddress1.toString() + "port " + k);
        if(packetforwarder1 != null)
        {
            packetforwarder1.startPF("RTP Forwarder for address " + inetaddress.toString() + "port " + j);
        }
        peerrtplist.put(sessionaddress, packetforwarder1);
        peerrtcplist.put(sessionaddress, packetforwarder);
        if(cache.ourssrc != null)
        {
            if(cache.ourssrc.reporter == null)
            {
                controladdress = inetaddress1;
                controlport = k;
                cache.ourssrc.reporter = startParticipating(k, inetaddress.getHostAddress(), cache.ourssrc);
            }
            if(((PacketFilter) (cache.ourssrc.reporter.transmit.sender)).peerlist == null)
            {
                cache.ourssrc.reporter.transmit.sender.peerlist = new Vector();
            }
        }
        ((PacketFilter) (cache.ourssrc.reporter.transmit.sender)).peerlist.addElement(sessionaddress);
        if(cache != null)
        {
            for(Enumeration enumeration = cache.cache.elements(); enumeration.hasMoreElements();)
            {
                SSRCInfo ssrcinfo = (SSRCInfo)enumeration.nextElement();
                if(ssrcinfo instanceof SendSSRCInfo)
                {
                    ssrcinfo.reporter.transmit.sender.control = true;
                    if(((PacketFilter) (ssrcinfo.reporter.transmit.sender)).peerlist == null)
                    {
                        ssrcinfo.reporter.transmit.sender.peerlist = new Vector();
                        ((PacketFilter) (ssrcinfo.reporter.transmit.sender)).peerlist.addElement(sessionaddress);
                    }
                }
            }
            
        }
        for(int l = 0; l < sendstreamlist.size(); l++)
        {
            SendSSRCInfo sendssrcinfo = (SendSSRCInfo)sendstreamlist.elementAt(l);
            if(((PacketFilter) (((SSRCInfo) (sendssrcinfo)).sinkstream.transmitter.sender)).peerlist == null)
            {
                ((SSRCInfo) (sendssrcinfo)).sinkstream.transmitter.sender.peerlist = new Vector();
                ((PacketFilter) (((SSRCInfo) (sendssrcinfo)).sinkstream.transmitter.sender)).peerlist.addElement(sessionaddress);
            }
        }
        
    }
    
    public void addReceiveStreamListener(ReceiveStreamListener receivestreamlistener)
    {
        if(!streamlistener.contains(receivestreamlistener))
        {
            streamlistener.addElement(receivestreamlistener);
        }
    }
    
    public void addRemoteListener(RemoteListener remotelistener1)
    {
        if(!remotelistener.contains(remotelistener1))
        {
            remotelistener.addElement(remotelistener1);
        }
    }
    
    void addSendStream(SendStream sendstream)
    {
        sendstreamlist.addElement(sendstream);
    }
    
    public void addSendStreamListener(SendStreamListener sendstreamlistener1)
    {
        if(!sendstreamlistener.contains(sendstreamlistener1))
        {
            sendstreamlistener.addElement(sendstreamlistener1);
        }
    }
    
    public void addSessionListener(SessionListener sessionlistener1)
    {
        if(!sessionlistener.contains(sessionlistener1))
        {
            sessionlistener.addElement(sessionlistener1);
        }
    }
    
    public void addTarget(SessionAddress sessionaddress)
    throws IOException
    {
        remoteAddresses.addElement(sessionaddress);
        if(remoteAddresses.size() > 1)
        {
            setRemoteAddresses();
            return;
        }
        remoteAddress = sessionaddress;
        try
        {
            rtcpRawReceiver = new RTCPRawReceiver(localAddress, sessionaddress, defaultstats, streamSynch, controlSocket);
            rtpRawReceiver = new RTPRawReceiver(channel, localAddress, sessionaddress, defaultstats, dataSocket);
        }
        catch(SocketException socketexception)
        {
            throw new IOException(socketexception.getMessage());
        }
        catch(UnknownHostException unknownhostexception)
        {
            throw new IOException(unknownhostexception.getMessage());
        }
        rtpDemultiplexer = new RTPDemultiplexer(cache, rtpRawReceiver, streamSynch);
        rtcpForwarder = new PacketForwarder(rtcpRawReceiver, new RTCPReceiver(cache));
        if(rtpRawReceiver != null)
        {
            rtpForwarder = new PacketForwarder(rtpRawReceiver, new RTPReceiver(cache, rtpDemultiplexer));
        }
        rtcpForwarder.startPF("RTCP Forwarder for address" + sessionaddress.getControlHostAddress() + " port " + sessionaddress.getControlPort());
        if(rtpForwarder != null)
        {
            rtpForwarder.startPF("RTP Forwarder for address " + sessionaddress.getDataHostAddress() + " port " + sessionaddress.getDataPort());
        }
        cleaner = new SSRCCacheCleaner(cache, streamSynch);
        if(cache.ourssrc != null && participating)
        {
            cache.ourssrc.reporter = startParticipating(rtpRawReceiver.socket);
        }
    }
    
    public void addUnicastAddr(InetAddress inetaddress)
    {
        if(sender != null)
        {
            sender.addDestAddr(inetaddress);
        }
    }
    
    public void closeSession()
    {
        if(dslist.isEmpty() || nosockets)
        {
            closeSession("DataSource disconnected");
        }
    }
    
    public void closeSession(String s)
    {
        stopParticipating(s, cache.ourssrc);
        if(defaultsource != null)
        {
            defaultsource.disconnect();
        }
        if(cache != null)
        {
            SSRCInfo ssrcinfo;
            for(Enumeration enumeration = cache.cache.elements(); enumeration.hasMoreElements(); stopParticipating(s, ssrcinfo))
            {
                ssrcinfo = (SSRCInfo)enumeration.nextElement();
                if(ssrcinfo.dstream != null)
                {
                    ssrcinfo.dstream.close();
                }
                if(ssrcinfo instanceof SendSSRCInfo)
                {
                    ((SendSSRCInfo)ssrcinfo).close();
                }
            }
            
        }
        for(int i = 0; i < sendstreamlist.size(); i++)
        {
            removeSendStream((SendStream)sendstreamlist.elementAt(i));
        }
        
        if(rtpTransmitter != null)
        {
            rtpTransmitter.close();
        }
        if(rtcpForwarder != null)
        {
            RTCPRawReceiver rtcprawreceiver = (RTCPRawReceiver)rtcpForwarder.getSource();
            rtcpForwarder.close();
            if(rtcprawreceiver != null)
            {
                rtcprawreceiver.close();
            }
        }
        if(cleaner != null)
        {
            cleaner.stop();
        }
        if(cache != null)
        {
            cache.destroy();
        }
        if(rtpForwarder != null)
        {
            RTPRawReceiver rtprawreceiver = (RTPRawReceiver)rtpForwarder.getSource();
            rtpForwarder.close();
            if(rtprawreceiver != null)
            {
                rtprawreceiver.close();
            }
        }
        if(multi_unicast)
        {
            removeAllPeers();
        }
    }
    
    public DataSource createNewDS(int i)
    {
        DataSource datasource = new DataSource();
        datasource.setContentType("raw");
        try
        {
            datasource.connect();
        }
        catch(IOException ioexception)
        {
            ioexception.printStackTrace();
        }
        RTPSourceStream rtpsourcestream = new RTPSourceStream(datasource);
        ((BufferControlImpl)buffercontrol).addSourceStream(rtpsourcestream);
        dslist.put(i, datasource);
        datasource.setSSRC(i);
        datasource.setMgr(this);
        return datasource;
    }
    
    public DataSource createNewDS(RTPMediaLocator rtpmedialocator)
    {
        DataSource datasource = new DataSource();
        datasource.setContentType("raw");
        try
        {
            datasource.connect();
        }
        catch(IOException ioexception)
        {
            System.err.println("IOException in createNewDS() " + ioexception.getMessage());
            ioexception.printStackTrace();
        }
        RTPSourceStream rtpsourcestream = new RTPSourceStream(datasource);
        ((BufferControlImpl)buffercontrol).addSourceStream(rtpsourcestream);
        if(rtpmedialocator != null && (int)rtpmedialocator.getSSRC() != 0)
        {
            dslist.put((int)rtpmedialocator.getSSRC(), datasource);
            datasource.setSSRC((int)rtpmedialocator.getSSRC());
            datasource.setMgr(this);
        }
        else
        {
            defaultsource = datasource;
            defaultstream = rtpsourcestream;
        }
        return datasource;
    }
    
    public SendStream createSendStream(int i, javax.media.protocol.DataSource datasource, int j)
    throws UnsupportedFormatException, IOException, SSRCInUseException
    {
        SSRCInfo ssrcinfo = cache.lookup(i);
        if(ssrcinfo != null)
        {
            throw new SSRCInUseException("SSRC supplied is already in use");
        }
        int k = i;
        if(cache.rtcp_bw_fraction == 0.0D)
        {
            throw new IOException("Initialized with zero RTP/RTCP outgoing bandwidth. Cannot create a sending stream");
        }
        PushBufferStream apushbufferstream[] = ((PushBufferDataSource)datasource).getStreams();
        PushBufferStream pushbufferstream = apushbufferstream[j];
        Format format = pushbufferstream.getFormat();
        int l = formatinfo.getPayload(format);
        if(l == -1)
        {
            throw new UnsupportedFormatException("Format of Stream not supported in RTP Session Manager", format);
        }
        SSRCInfo obj = null;
        if(sendercount == 0)
        {
            obj = new SendSSRCInfo(cache.ourssrc);
            obj.ours = true;
            cache.ourssrc = ((SSRCInfo) (obj));
            cache.getMainCache().put(((SSRCInfo) (obj)).ssrc, obj);
        }
        else
        {
            obj = cache.get(k, dataaddress, dataport, 3);
            obj.ours = true;
            if(!nosockets)
            {
                obj.reporter = startParticipating(controlport, controladdress.getHostAddress(), ((SSRCInfo) (obj)));
            }
            else
            {
                obj.reporter = startParticipating(rtcpsource, ((SSRCInfo) (obj)));
            }
        }
        obj.payloadType = l;
        ((SSRCInfo) (obj)).sinkstream.setSSRCInfo((SendSSRCInfo)obj);
        ((SendSSRCInfo)obj).setFormat(format);
        if(format instanceof VideoFormat)
        {
            obj.clockrate = 0x15f90;
        }
        else
            if(format instanceof AudioFormat)
            {
            obj.clockrate = (int)((AudioFormat)format).getSampleRate();
            }
            else
            {
            throw new UnsupportedFormatException("Format not supported", format);
            }
        obj.pds = datasource;
        pushbufferstream.setTransferHandler(((SSRCInfo) (obj)).sinkstream);
        if(multi_unicast)
        {
            if(peerlist.size() > 0)
            {
                SessionAddress sessionaddress = (SessionAddress)peerlist.firstElement();
                dataport = sessionaddress.getDataPort();
                dataaddress = sessionaddress.getDataAddress();
            }
            else
            {
                throw new IOException("At least one peer must be added");
            }
        }
        if(rtpTransmitter == null)
        {
            if(rtpConnector != null)
            {
                rtpTransmitter = startDataTransmission(rtpConnector);
            }
            else
                if(nosockets)
                {
                rtpTransmitter = startDataTransmission(rtpsource);
                }
                else
                {
                if(newRtpInterface)
                {
                    dataport = remoteAddress.getDataPort();
                    dataaddress = remoteAddress.getDataAddress();
                }
                rtpTransmitter = startDataTransmission(dataport, dataaddress.getHostAddress());
                }
            if(rtpTransmitter == null)
            {
                throw new IOException("Cannot create a transmitter");
            }
        }
        ((SSRCInfo) (obj)).sinkstream.setTransmitter(rtpTransmitter);
        addSendStream((SendStream)obj);
        if(multi_unicast)
        {
            for(int i1 = 0; i1 < peerlist.size(); i1++)
            {
                SessionAddress sessionaddress1 = (SessionAddress)peerlist.elementAt(i1);
                if(((PacketFilter) (((SSRCInfo) (obj)).sinkstream.transmitter.sender)).peerlist == null)
                {
                    ((SSRCInfo) (obj)).sinkstream.transmitter.sender.peerlist = new Vector();
                }
                ((PacketFilter) (((SSRCInfo) (obj)).sinkstream.transmitter.sender)).peerlist.addElement(sessionaddress1);
                if(cache != null)
                {
                    for(Enumeration enumeration = cache.cache.elements(); enumeration.hasMoreElements();)
                    {
                        SSRCInfo ssrcinfo1 = (SSRCInfo)enumeration.nextElement();
                        if(ssrcinfo1 instanceof SendSSRCInfo)
                        {
                            ssrcinfo1.reporter.transmit.sender.control = true;
                            if(((PacketFilter) (ssrcinfo1.reporter.transmit.sender)).peerlist == null)
                            {
                                ssrcinfo1.reporter.transmit.sender.peerlist = new Vector();
                            }
                            ((PacketFilter) (ssrcinfo1.reporter.transmit.sender)).peerlist.addElement(sessionaddress1);
                        }
                    }
                    
                }
            }
            
        }
        ((SSRCInfo) (obj)).sinkstream.startStream();
        NewSendStreamEvent newsendstreamevent = new NewSendStreamEvent(this, (SendStream)obj);
        cache.eventhandler.postEvent(newsendstreamevent);
        return (SendStream)obj;
    }
    
    public SendStream createSendStream(javax.media.protocol.DataSource datasource, int i)
    throws IOException, UnsupportedFormatException
    {
        int j = 0;
        do
        {
            j = (int)generateSSRC();
        } while(cache.lookup(j) != null);
        SendStream sendstream = null;
        try
        {
            sendstream = createSendStream(j, datasource, i);
            if(newRtpInterface)
            {
                setRemoteAddresses();
            }
        }
        catch(SSRCInUseException _ex)
        { }
        return sendstream;
    }
    
    public void dispose()
    {
        if(rtpConnector != null)
        {
            rtpConnector.close();
        }
        if(defaultsource != null)
        {
            defaultsource.disconnect();
        }
        if(cache != null)
        {
            SSRCInfo ssrcinfo;
            for(Enumeration enumeration = cache.cache.elements(); enumeration.hasMoreElements(); stopParticipating("dispose", ssrcinfo))
            {
                ssrcinfo = (SSRCInfo)enumeration.nextElement();
                if(ssrcinfo.dstream != null)
                {
                    ssrcinfo.dstream.close();
                }
                if(ssrcinfo instanceof SendSSRCInfo)
                {
                    ((SendSSRCInfo)ssrcinfo).close();
                }
            }
            
        }
        for(int i = 0; i < sendstreamlist.size(); i++)
        {
            removeSendStream((SendStream)sendstreamlist.elementAt(i));
        }
        
        if(rtpTransmitter != null)
        {
            rtpTransmitter.close();
        }
        if(rtcpTransmitter != null)
        {
            rtcpTransmitter.close();
        }
        if(rtcpForwarder != null)
        {
            RTCPRawReceiver rtcprawreceiver = (RTCPRawReceiver)rtcpForwarder.getSource();
            rtcpForwarder.close();
            if(rtcprawreceiver != null)
            {
                rtcprawreceiver.close();
            }
        }
        if(cleaner != null)
        {
            cleaner.stop();
        }
        if(cache != null)
        {
            cache.destroy();
        }
        if(rtpForwarder != null)
        {
            RTPRawReceiver rtprawreceiver = (RTPRawReceiver)rtpForwarder.getSource();
            rtpForwarder.close();
            if(rtprawreceiver != null)
            {
                rtprawreceiver.close();
            }
        }
    }
    
    private int findLocalPorts()
    {
        boolean flag = false;
        int i = -1;
        while(!flag)
        {
            do
            {
                double d = Math.random();
                i = (int)(d * 65535D);
                if(i % 2 != 0)
                {
                    i++;
                }
            } while(i < 1024 || i > 65534);
            try
            {
                DatagramSocket datagramsocket = new DatagramSocket(i);
                datagramsocket.close();
                datagramsocket = new DatagramSocket(i + 1);
                datagramsocket.close();
                flag = true;
            }
            catch(SocketException _ex)
            {
                flag = false;
            }
        }
        return i;
    }
    
    public static boolean formatSupported(Format format)
    {
        if(supportedList == null)
        {
            supportedList = new FormatInfo();
        }
        if(supportedList.getPayload(format) != -1)
        {
            return true;
        }
        for(int i = 0; i < addedList.size(); i++)
        {
            Format format1 = (Format)addedList.elementAt(i);
            if(format1.matches(format))
            {
                return true;
            }
        }
        
        return false;
    }
    
    public String generateCNAME()
    {
        return SourceDescription.generateCNAME();
    }
    
    public long generateSSRC()
    {
        long l = TrueRandom.rand();
        return l;
    }
    
    public Vector getActiveParticipants()
    {
        Vector vector1 = new Vector();
        RTPSourceInfoCache rtpsourceinfocache = cache.getRTPSICache();
        Hashtable hashtable = rtpsourceinfocache.getCacheTable();
        for(Enumeration enumeration = hashtable.elements(); enumeration.hasMoreElements();)
        {
            Participant participant = (Participant)enumeration.nextElement();
            if(participant == null || !(participant instanceof LocalParticipant) || !nonparticipating)
            {
                Vector vector = participant.getStreams();
                if(vector.size() > 0)
                {
                    vector1.addElement(participant);
                }
            }
        }
        
        return vector1;
    }
    
    public Vector getAllParticipants()
    {
        Vector vector = new Vector();
        RTPSourceInfoCache rtpsourceinfocache = cache.getRTPSICache();
        Hashtable hashtable = rtpsourceinfocache.getCacheTable();
        for(Enumeration enumeration = hashtable.elements(); enumeration.hasMoreElements();)
        {
            Participant participant = (Participant)enumeration.nextElement();
            if(participant != null && (!(participant instanceof LocalParticipant) || !nonparticipating))
            {
                vector.addElement(participant);
            }
        }
        
        return vector;
    }
    
    public Object getControl(String s)
    {
        if(s.equals("javax.media.control.BufferControl"))
        {
            return buffercontrol;
        }
        else
        {
            return null;
        }
    }
    
    public Object[] getControls()
    {
        Object aobj[] = new Object[1];
        aobj[0] = buffercontrol;
        return aobj;
    }
    
    public DataSource getDataSource(RTPMediaLocator rtpmedialocator)
    {
        if(rtpmedialocator == null)
        {
            return defaultsource;
        }
        int i = (int)rtpmedialocator.getSSRC();
        if(i == 0)
        {
            return defaultsource;
        }
        else
        {
            return (DataSource)dslist.get(i);
        }
    }
    
    public long getDefaultSSRC()
    {
        return defaultSSRC;
    }
    
    public Format getFormat(int i)
    {
        return formatinfo.get(i);
    }
    
    public GlobalReceptionStats getGlobalReceptionStats()
    {
        return defaultstats;
    }
    
    public GlobalTransmissionStats getGlobalTransmissionStats()
    {
        return transstats;
    }
    
    public LocalParticipant getLocalParticipant()
    {
        RTPSourceInfoCache rtpsourceinfocache = cache.getRTPSICache();
        Hashtable hashtable = rtpsourceinfocache.getCacheTable();
        for(Enumeration enumeration = hashtable.elements(); enumeration.hasMoreElements();)
        {
            Participant participant = (Participant)enumeration.nextElement();
            if(participant != null && !nonparticipating && (participant instanceof LocalParticipant))
            {
                return (LocalParticipant)participant;
            }
        }
        
        return null;
    }
    
    public SessionAddress getLocalReceiverAddress()
    {
        return localReceiverAddress;
    }
    
    public SessionAddress getLocalSessionAddress()
    {
        if(newRtpInterface)
        {
            return localAddress;
        }
        else
        {
            SessionAddress sessionaddress = new SessionAddress(localDataAddress, localDataPort, localControlAddress, localControlPort);
            return sessionaddress;
        }
    }
    
    public int getMulticastScope()
    {
        return ttl;
    }
    
    public Vector getPassiveParticipants()
    {
        Vector vector1 = new Vector();
        RTPSourceInfoCache rtpsourceinfocache = cache.getRTPSICache();
        Hashtable hashtable = rtpsourceinfocache.getCacheTable();
        for(Enumeration enumeration = hashtable.elements(); enumeration.hasMoreElements();)
        {
            Participant participant = (Participant)enumeration.nextElement();
            if(participant == null || !(participant instanceof LocalParticipant) || !nonparticipating)
            {
                Vector vector = participant.getStreams();
                if(vector.size() == 0)
                {
                    vector1.addElement(participant);
                }
            }
        }
        
        return vector1;
    }
    
    public Vector getPeers()
    {
        return peerlist;
    }
    
    private String getProperty(String s)
    {
        String s1 = null;
        try
        {
            s1 = System.getProperty(s);
        }
        catch(Throwable _ex)
        { }
        return s1;
    }
    
    public Vector getReceiveStreams()
    {
        Vector vector = new Vector();
        Vector vector1 = getAllParticipants();
        for(int i = 0; i < vector1.size(); i++)
        {
            Participant participant = (Participant)vector1.elementAt(i);
            Vector vector2 = participant.getStreams();
            for(int j = 0; j < vector2.size(); j++)
            {
                RTPStream rtpstream = (RTPStream)vector2.elementAt(j);
                if(rtpstream instanceof ReceiveStream)
                {
                    vector.addElement(rtpstream);
                }
            }
            
        }
        
        vector.trimToSize();
        return vector;
    }
    
    public Vector getRemoteParticipants()
    {
        Vector vector = new Vector();
        RTPSourceInfoCache rtpsourceinfocache = cache.getRTPSICache();
        Hashtable hashtable = rtpsourceinfocache.getCacheTable();
        for(Enumeration enumeration = hashtable.elements(); enumeration.hasMoreElements();)
        {
            Participant participant = (Participant)enumeration.nextElement();
            if(participant != null && (participant instanceof RemoteParticipant))
            {
                vector.addElement(participant);
            }
        }
        
        return vector;
    }
    
    public SessionAddress getRemoteSessionAddress()
    {
        return remoteAddress;
    }
    
    // SRIT 
    public SSRCCache getSSRCCache()
    {
        return cache;
    }
    
    public SSRCInfo getSSRCInfo(int i)
    {
        SSRCInfo ssrcinfo = cache.lookup(i);
        return ssrcinfo;
    }
    
    public Vector getSendStreams()
    {
        return null;
    }
    
    public SessionAddress getSessionAddress()
    {
        SessionAddress sessionaddress = new SessionAddress(dataaddress, dataport, controladdress, controlport);
        return sessionaddress;
    }
    
    public RTPStream getStream(long l)
    {
        Vector vector = null;
        vector = getAllParticipants();
        if(vector == null)
        {
            return null;
        }
        for(int i = 0; i < vector.size(); i++)
        {
            RTPSourceInfo rtpsourceinfo = (RTPSourceInfo)vector.elementAt(i);
            RTPStream rtpstream = rtpsourceinfo.getSSRCStream(l);
            if(rtpstream != null)
            {
                return rtpstream;
            }
        }
        
        return null;
    }
    
    public int initSession(SessionAddress sessionaddress, long l, SourceDescription asourcedescription[], double d, double d1)
    throws InvalidSessionAddressException
    {
        if(initialized)
        {
            return -1;
        }
        if(d == 0.0D)
        {
            nonparticipating = true;
        }
        defaultSSRC = l;
        localDataAddress = sessionaddress.getDataAddress();
        localControlAddress = sessionaddress.getControlAddress();
        localDataPort = sessionaddress.getDataPort();
        localControlPort = sessionaddress.getControlPort();
        InetAddress ainetaddress[] = null;
        InetAddress inetaddress;
        try
        {
            inetaddress = InetAddress.getLocalHost();
            String s1 = inetaddress.getHostName();
            ainetaddress = Utils.getAllLocalUsingNetworkInterface();
        }
        catch(Throwable throwable1)
        {
            System.err.println("InitSession : UnknownHostExcpetion " + throwable1.getMessage());
            throwable1.printStackTrace();
            return -1;
        }
        if(localDataAddress == null)
        {
            localDataAddress = inetaddress;
        }
        if(localControlAddress == null)
        {
            localControlAddress = inetaddress;
        }
        boolean flag = false;
        boolean flag1 = false;
        for(int i = 0; i < ainetaddress.length && (!flag || !flag1); i++)
        {
            if(ainetaddress[i].equals(localDataAddress))
            {
                flag = true;
            }
            if(ainetaddress[i].equals(localControlAddress))
            {
                flag1 = true;
            }
        }
        
        String s2 = "Does not belong to any of this hosts local interfaces";
        if(!flag)
        {
            throw new InvalidSessionAddressException("Local Data Address" + s2);
        }
        if(!flag1)
        {
            throw new InvalidSessionAddressException("Local Control Address" + s2);
        }
        cache = new SSRCCache(this);
        formatinfo.setCache(cache);
        cache.rtcp_bw_fraction = d;
        cache.rtcp_sender_bw_fraction = d1;
        cache.ourssrc = cache.get((int)l, inetaddress, 0, 2);
        cache.ourssrc.setAlive(true);
        if(!isCNAME(asourcedescription))
        {
            SourceDescription asourcedescription1[] = setCNAME(asourcedescription);
            cache.ourssrc.setSourceDescription(asourcedescription1);
        }
        else
        {
            cache.ourssrc.setSourceDescription(asourcedescription);
        }
        cache.ourssrc.ssrc = (int)l;
        cache.ourssrc.setOurs(true);
        initialized = true;
        return 0;
    }
    
    public int initSession(SessionAddress sessionaddress, SourceDescription asourcedescription[], double d, double d1)
    throws InvalidSessionAddressException
    {
        long l = generateSSRC();
        return initSession(sessionaddress, l, asourcedescription, d, d1);
    }
    
    private int initSession(SourceDescription asourcedescription[], double d, double d1)
    {
        if(initialized)
        {
            return -1;
        }
        InetAddress inetaddress = null;
        if(d == 0.0D)
        {
            nonparticipating = true;
        }
        defaultSSRC = generateSSRC();
        cache = new SSRCCache(this);
        formatinfo.setCache(cache);
        cache.rtcp_bw_fraction = d;
        cache.rtcp_sender_bw_fraction = d1;
        try
        {
            inetaddress = InetAddress.getLocalHost();
        }
        catch(Throwable throwable1)
        {
            System.err.println("InitSession : UnknownHostExcpetion " + throwable1.getMessage());
            throwable1.printStackTrace();
            return -1;
        }
        cache.ourssrc = cache.get((int)defaultSSRC, null, 0, 2);
        cache.ourssrc.setAlive(true);
        if(!isCNAME(asourcedescription))
        {
            SourceDescription asourcedescription1[] = setCNAME(asourcedescription);
            cache.ourssrc.setSourceDescription(asourcedescription1);
        }
        else
        {
            cache.ourssrc.setSourceDescription(asourcedescription);
        }
        cache.ourssrc.ssrc = (int)defaultSSRC;
        cache.ourssrc.setOurs(true);
        initialized = true;
        return 0;
    }
    
    public void initialize(RTPConnector rtpconnector)
    {
        rtpConnector = rtpconnector;
        newRtpInterface = true;
        String s = SourceDescription.generateCNAME();
        SourceDescription asourcedescription[] = {
            new SourceDescription(3, "jmf-user@sun.com", 1, false), new SourceDescription(1, s, 1, false), new SourceDescription(6, "JMF RTP Player v2.0", 1, false)
        };
        int i = (int)generateSSRC();
        ttl = 1;
        if(rtpConnector.getRTCPBandwidthFraction() == 0.0D)
        {
            participating = false;
        }
        else
        {
            participating = true;
        }
        cache = new SSRCCache(this);
        cache.sessionbandwidth = 0x5dc00;
        formatinfo.setCache(cache);
        if(rtpConnector.getRTCPBandwidthFraction() > 0.0D)
        {
            cache.rtcp_bw_fraction = rtpConnector.getRTCPBandwidthFraction();
        }
        else
        {
            cache.rtcp_bw_fraction = 0.050000000000000003D;
        }
        if(rtpConnector.getRTCPSenderBandwidthFraction() > 0.0D)
        {
            cache.rtcp_sender_bw_fraction = rtpConnector.getRTCPSenderBandwidthFraction();
        }
        else
        {
            cache.rtcp_sender_bw_fraction = 0.25D;
        }
        cache.ourssrc = cache.get(i, null, 0, 2);
        cache.ourssrc.setAlive(true);
        if(!isCNAME(asourcedescription))
        {
            SourceDescription asourcedescription1[] = setCNAME(asourcedescription);
            cache.ourssrc.setSourceDescription(asourcedescription1);
        }
        else
        {
            cache.ourssrc.setSourceDescription(asourcedescription);
        }
        cache.ourssrc.ssrc = i;
        cache.ourssrc.setOurs(true);
        initialized = true;
        rtpRawReceiver = new RTPRawReceiver(channel, rtpConnector, defaultstats);
        rtcpRawReceiver = new RTCPRawReceiver(rtpConnector, defaultstats, streamSynch);
        rtpDemultiplexer = new RTPDemultiplexer(cache, rtpRawReceiver, streamSynch);
        rtpForwarder = new PacketForwarder(rtpRawReceiver, new RTPReceiver(cache, rtpDemultiplexer));
        if(rtpForwarder != null)
        {
            rtpForwarder.startPF("RTP Forwarder: " + rtpConnector);
        }
        rtcpForwarder = new PacketForwarder(rtcpRawReceiver, new RTCPReceiver(cache));
        if(rtcpForwarder != null)
        {
            rtcpForwarder.startPF("RTCP Forwarder: " + rtpConnector);
        }
        cleaner = new SSRCCacheCleaner(cache, streamSynch);
        if(participating && cache.ourssrc != null)
        {
            cache.ourssrc.reporter = startParticipating(rtpConnector, cache.ourssrc);
        }
    }
    
    public void initialize(SessionAddress sessionaddress)
    throws InvalidSessionAddressException
    {
        String s = SourceDescription.generateCNAME();
        SourceDescription asourcedescription[] = {
            new SourceDescription(3, "jmf-user@sun.com", 1, false), new SourceDescription(1, s, 1, false), new SourceDescription(6, "JMF RTP Player v2.0", 1, false)
        };
        double d = 0.050000000000000003D;
        double d1 = 0.25D;
        SessionAddress asessionaddress[] = new SessionAddress[1];
        asessionaddress[0] = sessionaddress;
        initialize(asessionaddress, asourcedescription, d, d1, null);
    }
    
    public void initialize(SessionAddress asessionaddress[], SourceDescription asourcedescription[], double d, double d1, EncryptionInfo encryptioninfo)
    throws InvalidSessionAddressException
    {
        if(initialized)
        {
            return;
        }
        newRtpInterface = true;
        remoteAddresses = new Vector();
        int i = (int)generateSSRC();
        ttl = 1;
        if(d == 0.0D)
        {
            participating = false;
        }
        else
        {
            participating = true;
        }
        if(asessionaddress.length == 0)
        {
            throw new InvalidSessionAddressException("At least one local address is required!");
        }
        localAddress = asessionaddress[0];
        if(localAddress == null)
        {
            throw new InvalidSessionAddressException("Invalid local address: null");
        }
        InetAddress ainetaddress[] = null;
        InetAddress inetaddress;
        try
        {
            inetaddress = InetAddress.getLocalHost();
            String s1 = inetaddress.getHostName();
            ainetaddress = Utils.getAllLocalUsingNetworkInterface();
        }
        catch(Throwable throwable1)
        {
            System.err.println("Initialize : UnknownHostExcpetion " + throwable1.getMessage());
            throwable1.printStackTrace();
            return;
        }
        if(localAddress.getDataAddress() == null)
        {
            localAddress.setDataHostAddress(inetaddress);
        }
        if(localAddress.getControlAddress() == null)
        {
            localAddress.setControlHostAddress(inetaddress);
        }
        if(localAddress.getDataAddress().isMulticastAddress())
        {
            if(localAddress.getControlAddress().isMulticastAddress())
            {
                ttl = localAddress.getTimeToLive();
            }
            else
            {
                throw new InvalidSessionAddressException("Invalid multicast address");
            }
        }
        else
        {
            boolean flag = false;
            boolean flag1 = false;
            for(int j = 0; j < ainetaddress.length && (!flag || !flag1); j++)
            {
                if(ainetaddress[j].equals(localAddress.getDataAddress()))
                {
                    flag = true;
                }
                if(ainetaddress[j].equals(localAddress.getControlAddress()))
                {
                    flag1 = true;
                }
            }
            
            if(!flag)
            {
                String s2 = "Does not belong to any of this hosts local interfaces";
                throw new InvalidSessionAddressException("Local Data Address" + s2);
            }
            if(!flag1)
            {
                String s3 = "Does not belong to any of this hosts local interfaces";
                throw new InvalidSessionAddressException("Local Control Address" + s3);
            }
            if(localAddress.getDataPort() == -1)
            {
                int k = findLocalPorts();
                localAddress.setDataPort(k);
                localAddress.setControlPort(k + 1);
            }
            if(!localAddress.getDataAddress().isMulticastAddress())
            {
                try
                {
                    dataSocket = new DatagramSocket(localAddress.getDataPort(), localAddress.getDataAddress());
                }
                catch(SocketException _ex)
                {
                    throw new InvalidSessionAddressException("Can't open local data port: " + localAddress.getDataPort());
                }
            }
            if(!localAddress.getControlAddress().isMulticastAddress())
            {
                try
                {
                    controlSocket = new DatagramSocket(localAddress.getControlPort(), localAddress.getControlAddress());
                }
                catch(SocketException _ex)
                {
                    if(dataSocket != null)
                    {
                        dataSocket.close();
                    }
                    throw new InvalidSessionAddressException("Can't open local control port: " + localAddress.getControlPort());
                }
            }
        }
        cache = new SSRCCache(this);
        if(ttl <= 16)
        {
            cache.sessionbandwidth = 0x5dc00;
        }
        else
            if(ttl <= 64)
            {
            cache.sessionbandwidth = 0x1f400;
            }
            else
                if(ttl <= 128)
                {
            cache.sessionbandwidth = 16000;
                }
                else
                    if(ttl <= 192)
                    {
            cache.sessionbandwidth = 6625;
                    }
                    else
                    {
            cache.sessionbandwidth = 4000;
                    }
        formatinfo.setCache(cache);
        cache.rtcp_bw_fraction = d;
        cache.rtcp_sender_bw_fraction = d1;
        cache.ourssrc = cache.get(i, inetaddress, 0, 2);
        cache.ourssrc.setAlive(true);
        if(!isCNAME(asourcedescription))
        {
            SourceDescription asourcedescription1[] = setCNAME(asourcedescription);
            cache.ourssrc.setSourceDescription(asourcedescription1);
        }
        else
        {
            cache.ourssrc.setSourceDescription(asourcedescription);
        }
        cache.ourssrc.ssrc = i;
        cache.ourssrc.setOurs(true);
        initialized = true;
    }
    
    boolean isBroadcast(InetAddress inetaddress)
    {
        Object obj = null;
        try
        {
            InetAddress inetaddress1 = InetAddress.getLocalHost();
            byte abyte0[] = inetaddress1.getAddress();
            int i = abyte0[3] & 0xff;
            i |= abyte0[2] << 8 & 0xff00;
            i |= abyte0[1] << 16 & 0xff0000;
            i |= abyte0[0] << 24 & 0xff000000;
            byte abyte1[] = inetaddress.getAddress();
            int j = abyte1[3] & 0xff;
            j |= abyte1[2] << 8 & 0xff00;
            j |= abyte1[1] << 16 & 0xff0000;
            j |= abyte1[0] << 24 & 0xff000000;
            if((i | 0xff) == j)
            {
                return true;
            }
        }
        catch(UnknownHostException unknownhostexception)
        {
            System.err.println(unknownhostexception.getMessage());
        }
        return false;
    }
    
    private boolean isCNAME(SourceDescription asourcedescription[])
    {
        Object obj = null;
        boolean flag = false;
        if(asourcedescription == null)
        {
            return flag;
        }
        for(int j = 0; j < asourcedescription.length; j++)
        {
            try
            {
                int i = asourcedescription[j].getType();
                String s = asourcedescription[j].getDescription();
                if(i == 1 && s != null)
                {
                    flag = true;
                }
            }
            catch(Exception _ex)
            { }
        }
        
        return flag;
    }
    
    public boolean isDefaultDSassigned()
    {
        return bds;
    }
    
    public boolean isSenderDefaultAddr(InetAddress inetaddress)
    {
        if(sender == null)
        {
            return false;
        }
        InetAddress inetaddress1 = sender.getRemoteAddr();
        return inetaddress1.equals(inetaddress);
    }
    
    boolean isUnicast()
    {
        return unicast;
    }
    
    public void removeAllPeers()
    {
        for(int i = 0; i < peerlist.size(); i++)
        {
            removePeer((SessionAddress)peerlist.elementAt(i));
        }
        
    }
    
    public void removeDataSource(DataSource datasource)
    {
        if(datasource == defaultsource)
        {
            defaultsource = null;
            defaultstream = null;
            defaultsourceid = 0;
            bds = false;
        }
        dslist.removeObj(datasource);
    }
    
    public void removePeer(SessionAddress sessionaddress)
    {
        PacketForwarder packetforwarder = (PacketForwarder)peerrtplist.get(sessionaddress);
        PacketForwarder packetforwarder1 = (PacketForwarder)peerrtplist.get(sessionaddress);
        if(packetforwarder != null)
        {
            packetforwarder.close();
        }
        if(packetforwarder1 != null)
        {
            packetforwarder1.close();
        }
        for(int i = 0; i < peerlist.size(); i++)
        {
            SessionAddress sessionaddress1 = (SessionAddress)peerlist.elementAt(i);
            if(sessionaddress1.equals(sessionaddress))
            {
                peerlist.removeElementAt(i);
            }
        }
        
    }
    
    public void removeReceiveStreamListener(ReceiveStreamListener receivestreamlistener)
    {
        streamlistener.removeElement(receivestreamlistener);
    }
    
    public void removeRemoteListener(RemoteListener remotelistener1)
    {
        remotelistener.removeElement(remotelistener1);
    }
    
    void removeSendStream(SendStream sendstream)
    {
        sendstreamlist.removeElement(sendstream);
        if(((SSRCInfo) ((SendSSRCInfo)sendstream)).sinkstream != null)
        {
            ((SSRCInfo) ((SendSSRCInfo)sendstream)).sinkstream.close();
            StreamClosedEvent streamclosedevent = new StreamClosedEvent(this, sendstream);
            cache.eventhandler.postEvent(streamclosedevent);
            stopParticipating("Closed Stream", (SendSSRCInfo)sendstream);
        }
        if(sendstreamlist.size() == 0 && cache.ourssrc != null)
        {
            PassiveSSRCInfo passivessrcinfo = new PassiveSSRCInfo(getSSRCCache().ourssrc);
            cache.ourssrc = passivessrcinfo;
            cache.getMainCache().put(((SSRCInfo) (passivessrcinfo)).ssrc, passivessrcinfo);
        }
    }
    
    public void removeSendStreamListener(SendStreamListener sendstreamlistener1)
    {
    }
    
    public void removeSessionListener(SessionListener sessionlistener1)
    {
        sessionlistener.removeElement(sessionlistener1);
    }
    
    public void removeTarget(SessionAddress sessionaddress, String s)
    {
        remoteAddresses.removeElement(sessionaddress);
        setRemoteAddresses();
        if(remoteAddresses.size() == 0 && cache != null)
        {
            stopParticipating(s, cache.ourssrc);
        }
    }
    
    public void removeTargets(String s)
    {
        if(cache != null)
        {
            stopParticipating(s, cache.ourssrc);
        }
        if(remoteAddresses != null)
        {
            remoteAddresses.removeAllElements();
        }
        setRemoteAddresses();
    }
    
    private SourceDescription[] setCNAME(SourceDescription asourcedescription[])
    {
        Object obj = null;
        boolean flag = false;
        if(asourcedescription == null)
        {
            asourcedescription = new SourceDescription[1];
            String s = SourceDescription.generateCNAME();
            asourcedescription[0] = new SourceDescription(1, s, 1, false);
            return asourcedescription;
        }
        for(int j = 0; j < asourcedescription.length; j++)
        {
            int i = asourcedescription[j].getType();
            String s1 = asourcedescription[j].getDescription();
            if(i != 1 || s1 != null)
            {
                continue;
            }
            s1 = SourceDescription.generateCNAME();
            flag = true;
            break;
        }
        
        if(flag)
        {
            return asourcedescription;
        }
        SourceDescription asourcedescription1[] = new SourceDescription[asourcedescription.length + 1];
        asourcedescription1[0] = new SourceDescription(1, SourceDescription.generateCNAME(), 1, false);
        int k = 1;
        for(int l = 0; l < asourcedescription.length; l++)
        {
            asourcedescription1[k] = new SourceDescription(asourcedescription[l].getType(), asourcedescription[l].getDescription(), 1, false);
            k++;
        }
        
        return asourcedescription1;
    }
    
    public void setDefaultDSassigned(int i)
    {
        bds = true;
        defaultsourceid = i;
        dslist.put(i, defaultsource);
        defaultsource.setSSRC(i);
        defaultsource.setMgr(this);
    }
    
    public void setMulticastScope(int i)
    {
        if(i < 1)
        {
            i = 1;
        }
        ttl = i;
        if(ttl <= 16)
        {
            cache.sessionbandwidth = 0x5dc00;
        }
        else
            if(ttl <= 64)
            {
            cache.sessionbandwidth = 0x1f400;
            }
            else
                if(ttl <= 128)
                {
            cache.sessionbandwidth = 16000;
                }
                else
                    if(ttl <= 192)
                    {
            cache.sessionbandwidth = 6625;
                    }
                    else
                    {
            cache.sessionbandwidth = 4000;
                    }
        if(udpsender != null)
        {
            try
            {
                udpsender.setttl(ttl);
            }
            catch(IOException ioexception)
            {
                System.err.println("setMulticastScope Exception " + ioexception.getMessage());
                ioexception.printStackTrace();
            }
        }
    }
    
    private void setRemoteAddresses()
    {
        if(rtpTransmitter != null)
        {
            RTPRawSender rtprawsender = rtpTransmitter.getSender();
            rtprawsender.setDestAddresses(remoteAddresses);
        }
        if(rtcpTransmitter != null)
        {
            RTCPRawSender rtcprawsender = rtcpTransmitter.getSender();
            rtcprawsender.setDestAddresses(remoteAddresses);
        }
    }
    
    private SourceDescription[] setSDES()
    {
        SourceDescription asourcedescription[] = new SourceDescription[3];
        if(asourcedescription == null)
        {
            return null;
        }
        else
        {
            asourcedescription[0] = new SourceDescription(2, getProperty("user.name"), 1, false);
            asourcedescription[1] = new SourceDescription(1, SourceDescription.generateCNAME(), 1, false);
            asourcedescription[2] = new SourceDescription(6, "JMF RTP Player v1.0", 1, false);
            return asourcedescription;
        }
    }
    
    void setSessionBandwidth(int i)
    {
        cache.sessionbandwidth = i;
    }
    
    // SRIT
    public RTPTransmitter startDataTransmission(int i, String s)
    throws IOException
    {
        RTPTransmitter rtptransmitter = null;
        RTPRawSender rtprawsender = null;
        if(localDataPort == -1)
        {
            udpsender = new UDPPacketSender(dataaddress, dataport);
        }
        else
            if(newRtpInterface)
            {
            udpsender = new UDPPacketSender(rtpRawReceiver.socket);
            }
            else
            {
            int j = localSenderAddress.getDataPort();
            InetAddress inetaddress = localSenderAddress.getDataAddress();
            udpsender = new UDPPacketSender(j, inetaddress, dataaddress, dataport);
            }
        if(ttl != 1)
        {
            udpsender.setttl(ttl);
        }
        rtprawsender = new RTPRawSender(dataport, s, udpsender);
        rtptransmitter = new RTPTransmitter(cache, rtprawsender);
        return rtptransmitter;
    }
    
    private RTPTransmitter startDataTransmission(RTPConnector rtpconnector)
    {
        try
        {
            RTPRawSender rtprawsender = null;
            RTPTransmitter rtptransmitter = null;
            rtpsender = new RTPPacketSender(rtpconnector);
            rtprawsender = new RTPRawSender(rtpsender);
            rtptransmitter = new RTPTransmitter(cache, rtprawsender);
            return rtptransmitter;
        }
        catch(IOException _ex)
        {
            return null;
        }
    }
    
    private RTPTransmitter startDataTransmission(RTPPushDataSource rtppushdatasource)
    {
        RTPRawSender rtprawsender = null;
        RTPTransmitter rtptransmitter = null;
        rtpsender = new RTPPacketSender(rtppushdatasource);
        rtprawsender = new RTPRawSender(rtpsender);
        rtptransmitter = new RTPTransmitter(cache, rtprawsender);
        return rtptransmitter;
    }
    
    private synchronized RTCPReporter startParticipating(int i, String s, SSRCInfo ssrcinfo)
    throws IOException
    {
        startedparticipating = true;
        UDPPacketSender udppacketsender = null;
        if(localControlPort == -1)
        {
            udppacketsender = new UDPPacketSender(controladdress, controlport);
            localControlPort = udppacketsender.getLocalPort();
            localControlAddress = udppacketsender.getLocalAddress();
        }
        else
        {
            udppacketsender = new UDPPacketSender(localControlPort, localControlAddress, controladdress, controlport);
        }
        if(ttl != 1)
        {
            udppacketsender.setttl(ttl);
        }
        RTCPRawSender rtcprawsender = new RTCPRawSender(i, s, udppacketsender);
        RTCPTransmitter rtcptransmitter = new RTCPTransmitter(cache, rtcprawsender);
        rtcptransmitter.setSSRCInfo(ssrcinfo);
        RTCPReporter rtcpreporter = new RTCPReporter(cache, rtcptransmitter);
        return rtcpreporter;
    }
    
    private synchronized RTCPReporter startParticipating(DatagramSocket datagramsocket)
    throws IOException
    {
        UDPPacketSender udppacketsender = new UDPPacketSender(datagramsocket);
        udpPacketSender = udppacketsender;
        if(ttl != 1)
        {
            udppacketsender.setttl(ttl);
        }
        
        // SRIT add for RTP
        RTPRawSender rtprawsender = new RTPRawSender(remoteAddress.getDataPort(), remoteAddress.getControlAddress().getHostName(), udppacketsender);       
        rtpTransmitter = new RTPTransmitter(cache, rtprawsender);
        // rtpTransmitter.setSSRCInfo(cache.ourssrc);
        // RTCPReporter rtpreporter = new RTCPReporter(cache, rtcpTransmitter);

        RTCPRawSender rtcprawsender = new RTCPRawSender(remoteAddress.getControlPort(), remoteAddress.getControlAddress().getHostName(), udppacketsender);
        rtcpTransmitter = new RTCPTransmitter(cache, rtcprawsender);
        rtcpTransmitter.setSSRCInfo(cache.ourssrc);
        RTCPReporter rtcpreporter = new RTCPReporter(cache, rtcpTransmitter);
        
        startedparticipating = true;
        return rtcpreporter;
    }
    
    private synchronized RTCPReporter startParticipating(RTPConnector rtpconnector, SSRCInfo ssrcinfo)
    {
        startedparticipating = true;
        try
        {
            rtpsender = new RTPPacketSender(rtpconnector.getControlOutputStream());
        }
        catch(IOException ioexception)
        {
            ioexception.printStackTrace();
        }
        RTCPRawSender rtcprawsender = new RTCPRawSender(rtpsender);
        RTCPTransmitter rtcptransmitter = new RTCPTransmitter(cache, rtcprawsender);
        rtcptransmitter.setSSRCInfo(ssrcinfo);
        RTCPReporter rtcpreporter = new RTCPReporter(cache, rtcptransmitter);
        return rtcpreporter;
    }
    
    private synchronized RTCPReporter startParticipating(RTPPushDataSource rtppushdatasource, SSRCInfo ssrcinfo)
    {
        startedparticipating = true;
        rtpsender = new RTPPacketSender(rtppushdatasource);
        RTCPRawSender rtcprawsender = new RTCPRawSender(rtpsender);
        RTCPTransmitter rtcptransmitter = new RTCPTransmitter(cache, rtcprawsender);
        rtcptransmitter.setSSRCInfo(ssrcinfo);
        RTCPReporter rtcpreporter = new RTCPReporter(cache, rtcptransmitter);
        return rtcpreporter;
    }
    
    private synchronized RTCPReporter startParticipating(SessionAddress sessionaddress, SessionAddress sessionaddress1, SSRCInfo ssrcinfo, DatagramSocket datagramsocket)
    throws IOException
    {
        localReceiverAddress = sessionaddress;
        startedparticipating = true;
        UDPPacketSender udppacketsender = null;
        int i = sessionaddress1.getControlPort();
        InetAddress inetaddress = sessionaddress1.getControlAddress();
        int j = sessionaddress.getControlPort();
        InetAddress inetaddress1 = sessionaddress.getControlAddress();
        if(i == -1)
        {
            udppacketsender = new UDPPacketSender(inetaddress, i);
        }
        else
            if(i == j)
            {
            udppacketsender = new UDPPacketSender(datagramsocket);
            }
            else
            {
            udppacketsender = new UDPPacketSender(i, inetaddress, controladdress, controlport);
            }
        if(ttl != 1)
        {
            udppacketsender.setttl(ttl);
        }
        RTCPRawSender rtcprawsender = new RTCPRawSender(controlport, controladdress.getHostName(), udppacketsender);
        RTCPTransmitter rtcptransmitter = new RTCPTransmitter(cache, rtcprawsender);
        rtcptransmitter.setSSRCInfo(ssrcinfo);
        RTCPReporter rtcpreporter = new RTCPReporter(cache, rtcptransmitter);
        return rtcpreporter;
    }
    
    void startRTCPReports(InetAddress inetaddress)
    {
        if(!nonparticipating && !startedparticipating)
        {
            try
            {
                if(cache.ourssrc != null)
                {
                    cache.ourssrc.reporter = startParticipating(controlport, inetaddress.getHostAddress(), cache.ourssrc);
                }
            }
            catch(IOException ioexception)
            {
                System.err.println("startRTCPReports " + ioexception.getMessage());
                ioexception.printStackTrace();
            }
        }
    }
    
    public void startSession()
    throws IOException
    {
        SessionAddress sessionaddress = new SessionAddress(dataaddress, dataport, controladdress, controlport);
        try
        {
            startSession(sessionaddress, ttl, null);
        }
        catch(SessionManagerException sessionmanagerexception)
        {
            throw new IOException("SessionManager exception " + sessionmanagerexception.getMessage());
        }
    }
    
    public int startSession(int i, EncryptionInfo encryptioninfo)
    throws IOException
    {
        multi_unicast = true;
        if(i < 1)
        {
            i = 1;
        }
        ttl = i;
        if(ttl <= 16)
        {
            cache.sessionbandwidth = 0x5dc00;
        }
        else
            if(ttl <= 64)
            {
            cache.sessionbandwidth = 0x1f400;
            }
            else
                if(ttl <= 128)
                {
            cache.sessionbandwidth = 16000;
                }
                else
                    if(ttl <= 192)
                    {
            cache.sessionbandwidth = 6625;
                    }
                    else
                    {
            cache.sessionbandwidth = 4000;
                    }
        cleaner = new SSRCCacheCleaner(cache, streamSynch);
        return 0;
    }
    
    private int startSession(RTPPushDataSource rtppushdatasource, RTPPushDataSource rtppushdatasource1, EncryptionInfo encryptioninfo)
    {
        if(!initialized)
        {
            return -1;
        }
        if(started)
        {
            return -1;
        }
        cache.sessionbandwidth = 0x5dc00;
        RTPRawReceiver rtprawreceiver = new RTPRawReceiver(channel, rtppushdatasource, defaultstats);
        RTCPRawReceiver rtcprawreceiver = new RTCPRawReceiver(rtppushdatasource1, defaultstats, streamSynch);
        rtpDemultiplexer = new RTPDemultiplexer(cache, rtprawreceiver, streamSynch);
        rtpForwarder = new PacketForwarder(rtprawreceiver, new RTPReceiver(cache, rtpDemultiplexer));
        if(rtpForwarder != null)
        {
            rtpForwarder.startPF("RTP Forwarder " + rtppushdatasource);
        }
        rtcpForwarder = new PacketForwarder(rtcprawreceiver, new RTCPReceiver(cache));
        if(rtcpForwarder != null)
        {
            rtcpForwarder.startPF("RTCP Forwarder " + rtppushdatasource);
        }
        cleaner = new SSRCCacheCleaner(cache, streamSynch);
        if(!nonparticipating && cache.ourssrc != null)
        {
            cache.ourssrc.reporter = startParticipating(rtppushdatasource1, cache.ourssrc);
        }
        started = true;
        return 0;
    }
    
    public int startSession(SessionAddress sessionaddress, int i, EncryptionInfo encryptioninfo)
    throws IOException, InvalidSessionAddressException
    {
        if(started)
        {
            return -1;
        }
        if(i < 1)
        {
            i = 1;
        }
        ttl = i;
        if(ttl <= 16)
        {
            cache.sessionbandwidth = 0x5dc00;
        }
        else
            if(ttl <= 64)
            {
            cache.sessionbandwidth = 0x1f400;
            }
            else
                if(ttl <= 128)
                {
            cache.sessionbandwidth = 16000;
                }
                else
                    if(ttl <= 192)
                    {
            cache.sessionbandwidth = 6625;
                    }
                    else
                    {
            cache.sessionbandwidth = 4000;
                    }
        controlport = sessionaddress.getControlPort();
        dataport = sessionaddress.getDataPort();
        CheckRTPPorts(dataport, controlport);
        dataaddress = sessionaddress.getDataAddress();
        controladdress = sessionaddress.getControlAddress();
        CheckRTPAddress(dataaddress, controladdress);
        RTCPRawReceiver rtcprawreceiver = null;
        RTPRawReceiver rtprawreceiver = null;
        InetAddress inetaddress = null;
        try
        {
            inetaddress = InetAddress.getLocalHost();
        }
        catch(Throwable throwable1)
        {
            System.err.println("InitSession : UnknownHostExcpetion " + throwable1.getMessage());
            throwable1.printStackTrace();
            return -1;
        }
        if(dataaddress.equals(inetaddress))
        {
            unicast = true;
        }
        if(!dataaddress.isMulticastAddress() && !dataaddress.equals(inetaddress))
        {
            if(isBroadcast(dataaddress) && !OSValidator.isWindows() )
            {
                bindtome = false;
            }
            else
            {
                bindtome = true;
            }
        }
        if(!bindtome)
        {
            try
            {
                rtcprawreceiver = new RTCPRawReceiver(controlport, controladdress.getHostAddress(), defaultstats, streamSynch);
                if(dataaddress != null)
                {
                    rtprawreceiver = new RTPRawReceiver(channel, dataport, dataaddress.getHostAddress(), defaultstats);
                }
            }
            catch(SocketException socketexception)
            {
                throw new IOException(socketexception.getMessage());
            }
            finally
            {
                if(dataaddress != null && rtprawreceiver == null && rtcprawreceiver != null)
                {
                    System.err.println("could not create RTCP/RTP raw receivers");
                    rtcprawreceiver.closeSource();
                }
            }
        }
        else
        {
            try
            {
                rtcprawreceiver = new RTCPRawReceiver(controlport, inetaddress.getHostAddress(), defaultstats, streamSynch);
                if(dataaddress != null)
                {
                    rtprawreceiver = new RTPRawReceiver(channel, dataport, inetaddress.getHostAddress(), defaultstats);
                }
            }
            catch(SocketException socketexception1)
            {
                throw new IOException(socketexception1.getMessage());
            }
            finally
            {
                if(dataaddress != null && rtprawreceiver == null && rtcprawreceiver != null)
                {
                    System.err.println("could not create RTCP/RTP raw receivers");
                    rtcprawreceiver.closeSource();
                }
            }
        }
        rtpDemultiplexer = new RTPDemultiplexer(cache, rtprawreceiver, streamSynch);
        rtcpForwarder = new PacketForwarder(rtcprawreceiver, new RTCPReceiver(cache));
        if(rtprawreceiver != null)
        {
            rtpForwarder = new PacketForwarder(rtprawreceiver, new RTPReceiver(cache, rtpDemultiplexer));
        }
        rtcpForwarder.startPF("RTCP Forwarder for address" + controladdress.toString() + "port " + controlport);
        if(rtpForwarder != null)
        {
            rtpForwarder.startPF("RTP Forwarder for address " + dataaddress.toString() + "port " + dataport);
        }
        cleaner = new SSRCCacheCleaner(cache, streamSynch);
        if(!nonparticipating && !unicast && cache.ourssrc != null)
        {
            cache.ourssrc.reporter = startParticipating(controlport, dataaddress.getHostAddress(), cache.ourssrc);
        }
        started = true;
        return 0;
    }
    
    public int startSession(SessionAddress sessionaddress, SessionAddress sessionaddress1, SessionAddress sessionaddress2, EncryptionInfo encryptioninfo)
    throws IOException, InvalidSessionAddressException
    {
        if(started)
        {
            return -1;
        }
        localSenderAddress = sessionaddress1;
        cache.sessionbandwidth = 0x5dc00;
        controlport = sessionaddress.getControlPort();
        dataport = sessionaddress.getDataPort();
        CheckRTPPorts(dataport, controlport);
        dataaddress = sessionaddress.getDataAddress();
        controladdress = sessionaddress.getControlAddress();
        if(dataaddress.isMulticastAddress() || controladdress.isMulticastAddress() || isBroadcast(dataaddress) || isBroadcast(controladdress))
        {
            throw new InvalidSessionAddressException("Local Address must be UNICAST IP addresses");
        }
        CheckRTPAddress(dataaddress, controladdress);
        RTCPRawReceiver rtcprawreceiver = null;
        RTPRawReceiver rtprawreceiver = null;
        InetAddress inetaddress = null;
        try
        {
            inetaddress = InetAddress.getLocalHost();
        }
        catch(Throwable throwable1)
        {
            System.err.println("InitSession : UnknownHostExcpetion " + throwable1.getMessage());
            throwable1.printStackTrace();
            return -1;
        }
        try
        {
            rtcprawreceiver = new RTCPRawReceiver(controlport, controladdress.getHostAddress(), defaultstats, streamSynch);
            if(dataaddress != null)
            {
                rtprawreceiver = new RTPRawReceiver(channel, dataport, dataaddress.getHostAddress(), defaultstats);
            }
        }
        catch(SocketException socketexception)
        {
            throw new IOException(socketexception.getMessage());
        }
        finally
        {
            if(dataaddress != null && rtprawreceiver == null && rtcprawreceiver != null)
            {
                System.err.println("could not create RTCP/RTP raw receivers");
                rtcprawreceiver.closeSource();
            }
        }
        rtpDemultiplexer = new RTPDemultiplexer(cache, rtprawreceiver, streamSynch);
        rtcpForwarder = new PacketForwarder(rtcprawreceiver, new RTCPReceiver(cache));
        if(rtprawreceiver != null)
        {
            rtpForwarder = new PacketForwarder(rtprawreceiver, new RTPReceiver(cache, rtpDemultiplexer));
        }
        rtcpForwarder.startPF("RTCP Forwarder for address" + controladdress.toString() + "port " + controlport);
        if(rtpForwarder != null)
        {
            rtpForwarder.startPF("RTP Forwarder for address " + dataaddress.toString() + "port " + dataport);
        }
        controlport = sessionaddress2.getControlPort();
        dataport = sessionaddress2.getDataPort();
        CheckRTPPorts(dataport, controlport);
        dataaddress = sessionaddress2.getDataAddress();
        controladdress = sessionaddress2.getControlAddress();
        if(dataaddress.isMulticastAddress() || controladdress.isMulticastAddress() || isBroadcast(dataaddress) || isBroadcast(controladdress))
        {
            throw new InvalidSessionAddressException("Remote Address must be UNICAST IP addresses");
        }
        CheckRTPAddress(dataaddress, controladdress);
        cleaner = new SSRCCacheCleaner(cache, streamSynch);
        if(!nonparticipating && !unicast && cache.ourssrc != null)
        {
            cache.ourssrc.reporter = startParticipating(sessionaddress, sessionaddress1, cache.ourssrc, rtcprawreceiver.socket);
        }
        started = true;
        return 0;
    }
    
    private synchronized void stopParticipating(String s, SSRCInfo ssrcinfo)
    {
        if(ssrcinfo.reporter != null)
        {
            ssrcinfo.reporter.close(s);
            ssrcinfo.reporter = null;
        }
    }
    
    public String toString()
    {
        String s;
        if(newRtpInterface)
        {
            int i = 0;
            int j = 0;
            String s1 = "";
            if(localAddress != null)
            {
                i = localAddress.getControlPort();
                j = localAddress.getDataPort();
                s1 = localAddress.getDataHostAddress();
            }
            s = "RTPManager \n\tSSRCCache  " + cache + "\n\tDataport  " + j + "\n\tControlport  " + i + "\n\tAddress  " + s1 + "\n\tRTPForwarder  " + rtpForwarder + "\n\tRTPDemux  " + rtpDemultiplexer;
        }
        else
        {
            s = "RTPSession Manager  \n\tSSRCCache  " + cache + "\n\tDataport  " + dataport + "\n\tControlport  " + controlport + "\n\tAddress  " + dataaddress + "\n\tRTPForwarder  " + rtpForwarder + "\n\tRTPDEmux  " + rtpDemultiplexer;
        }
        return s;
    }
    
    // PTT adaptation
    
    public long getSSRC()
    {
        if (rtcpTransmitter != null)
        {
            return rtcpTransmitter.getSSRC();
        }
        else
        {
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, "No RTCP transmitter");
            return -1;
        }
    }
    
    public void sendRtcpAppEvent(RTCPAPPPacket evt)
    {
        if (rtcpTransmitter != null)
        {
            GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "Send RTCP APP event: ", evt.toString());
            rtcpTransmitter.sendRtcpAppEvent(evt);
        }
        else
        {
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, "No RTCP transmitter");
        }
    }
    
    public RTPTransmitter getRtpTransmitter()
    {
        return rtpTransmitter;
    }

    public RTCPTransmitter getRtcpTransmitter()
    {
        return rtcpTransmitter;
    }

    public RTCPCompoundPacket sendReport()
    {
        if (rtcpTransmitter != null)
        {
            return rtcpTransmitter.sendReport();
        }
        else
        {
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, "No RTCP transmitter");
            return null;
        }
    }
    
    /**
     * Send a RTP data packet
     * 
     * @throws RTP exception
     */
    public void sendPacket(RTPPacket rtpPacket) throws Exception {                             
        rtpTransmitter.getSender().sendTo(rtpPacket);        
    }    
        
    // End PTT adaptation
    
    static
    {
        try
        {
            jmfSecurity = JMFSecurityManager.getJMFSecurity();
            securityPrivelege = true;
        }
        catch(SecurityException _ex)
        { }
    }
}
