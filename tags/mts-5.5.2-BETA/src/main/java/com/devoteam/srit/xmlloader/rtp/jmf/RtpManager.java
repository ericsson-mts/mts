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

package com.devoteam.srit.xmlloader.rtp.jmf;

import java.net.InetAddress;

import javax.media.rtp.rtcp.SourceDescription;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.sun.media.rtp.RTCPTransmitter;
import com.sun.media.rtp.RTPTransmitter;
import com.sun.media.rtp.util.RTPPacket;

/**
 * Manager in charge of the RTP communications. The JMF (Java Media Framework)
 * API is used for VoIp over RTP/RTCP protocol.<br><br>
 * 
 * The RTP manager is mainly composed of:<br>
 *   - an RTP receiver: to manage incoming RTP/RTCP streams,<br>
 *   - an RTP sender: to manage outgoing RTP/RTCP streams,<br>
 *   - a Floor controller: to manage the PTT floor over RTCP.<br><br>
 * 
 * @author JM. Auffret
 */
public class RtpManager {

    /**
     * Stack connection object
     */
    private Channel channel = null;    
    
    /**
     * UDP port base for RTP communications
     */
    public static int UDP_RTP_PORT_BASE = 5000;

    /**
     * Local SSRC value (RTP session participant id)
     */
    private long SSRC = -1;

    /**
     * SDES value
     */
    private SourceDescription[] sdes = null;

    /**
     * RTP receiver
     */
    public RtpReceiver receiver;

    /**
     * RTP sender
     */
    public RtpSender sender;

    /**
     * Floor controller
     */
    public FloorController floor;

    
    public static  RtpManager rtpManager = null;    
    

    /**
     * Constructor
     * 
     * @param session PTT session
     */
    public RtpManager(Channel channel) {
        this.channel = channel;
    }
    
    /** Standard getter */
    public Channel getChannel() {
        return channel;
    }    

    /** Creates or returns the instance of this stack */
    public synchronized static RtpManager getInstance() throws ExecutionException
    {
        if (null == rtpManager)
        {
            rtpManager = new RtpManager(null);            
        }
        return rtpManager ;
    }

    /**
     * Return the SSRC identifier
     * 
     * @return SSRC identifier
     */
    public long getSSRC() {
        return SSRC;
    }

    /**
     * Set the SSRC identifier
     * 
     * @param value SSRC identifier
     */
    public void setSSRC(long value) {
        SSRC = value;
    }

    /**
     * Set the SDES value
     * 
     * @param cname User URI
     * @param name User display name
     */
    public void setSDES(String cname, String name) {
        // Create the SDES value
        sdes =
            new SourceDescription[] {
                new SourceDescription(
                    SourceDescription.SOURCE_DESC_CNAME,
                    cname,
                    1,
                    false),
                new SourceDescription(
                    SourceDescription.SOURCE_DESC_NAME,
                    name,
                    1,
                    false),
                new SourceDescription(
                    SourceDescription.SOURCE_DESC_EMAIL,
                    null,
                    1,
                    false),
                new SourceDescription(
                    SourceDescription.SOURCE_DESC_TOOL,
                    null,
                    1,
                    false)};
    }

    /**
     * Get the SDES value
     * 
     * @return Table of SDES items
     */
    public SourceDescription[] getSDES() {
        return sdes;
    }

    /**
     * Open the RTP session
     * 
     * @param localPort Local RTP port
     * @param remoteHost Remote host
     * @param remotePort Remote RTP port
     * @throws RTP exception
     */
    public void open(String localHost, int localPort, String remoteHost, int remotePort)
        throws Exception {

        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "Create the RTP managers");
            
        receiver = new RtpReceiver(this);
        sender = new RtpSender(this);
        floor = new FloorController(this);

        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "Start the RTP managers");
    	if (localHost == null) {
    		InetAddress addr = Utils.getLocalAddress();
    		localHost = addr.getHostAddress();
    	}
        // receiver.startSession(localPort);
        sender.startSession(localHost, localPort, remoteHost, remotePort);
    }

    /**
     * Close the RTP session
     * 
     * @throws RTP exception
     */
    public void close() throws ExecutionException {
        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "Close the RTP managers");

        floor.stop();
        sender.stopSession();
        receiver.stopSession();
    }

    /**
     * Request the floor control
     * 
     * @throws RtpException
     */
    public void requestFloorControl() throws ExecutionException {
        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "Request the floor control");
        floor.floorRequest();
    }

    /**
     * Release the floor control
     * 
     * @throws RtpException
     */
    public void releaseFloorControl() throws ExecutionException {
        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "Release the floor control");
        floor.floorRelease();
    }
    
    
    /**
     * Ack the floor control
     * 
     * @throws RtpException
     */
    public void ackFloorControl() throws ExecutionException {
        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "Acknowledge the floor control");
        floor.floorAck();
    }
        
    /**
     * Get the TBCP parameters
     * 
     * @return Returns the tbcp parameters.
     */
    public TbcpParameters getTbcpParameters() {
        return new TbcpParameters();
    }
    
    /**
     * Send a RTP flow
     * 
     * @throws RTP exception
     */
    public void sendPacket(boolean control, RTPPacket rtpPacket) throws Exception {
        if (control) {
            RTCPTransmitter rtcpTransmitter = sender.getRtpSessionMgr().getRtcpTransmitter();
            // RTCPAPPPacket rtcpPacket = new RTCPAPPPacket();
            rtcpTransmitter.getSender().sendTo(rtpPacket);
        } else {
            RTPTransmitter rtpTransmitter = sender.getRtpSessionMgr().getRtpTransmitter();                       
            rtpTransmitter.getSender().sendTo(rtpPacket);            
        }
    }    

}