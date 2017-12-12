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

import javax.media.protocol.DataSource;
import javax.media.protocol.PushSourceStream;
import javax.media.rtp.Participant;
import javax.media.rtp.RTPManager;
import javax.media.rtp.RTPPushDataSource;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.RemoteListener;
import javax.media.rtp.SendStreamListener;
import javax.media.rtp.SessionAddress;
import javax.media.rtp.SessionListener;
import javax.media.rtp.event.ActiveReceiveStreamEvent;
import javax.media.rtp.event.ApplicationEvent;
import javax.media.rtp.event.ByeEvent;
import javax.media.rtp.event.InactiveReceiveStreamEvent;
import javax.media.rtp.event.LocalCollisionEvent;
import javax.media.rtp.event.NewReceiveStreamEvent;
import javax.media.rtp.event.RTPEvent;
import javax.media.rtp.event.ReceiveStreamEvent;
import javax.media.rtp.event.RemoteEvent;
import javax.media.rtp.event.RemotePayloadChangeEvent;
import javax.media.rtp.event.SendStreamEvent;
import javax.media.rtp.event.SenderReportEvent;
import javax.media.rtp.event.SessionEvent;
import javax.media.rtp.event.StreamMappedEvent;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.sun.media.rtp.RTCPCompoundPacket;
import com.sun.media.rtp.RTPRawReceiver;
import com.sun.media.rtp.RTPSessionMgr;
import com.sun.media.rtp.RTPTransmitter;

/**
 * RTP sender that has in charge to open and to control outgoing RTP
 * streams.
 * 
 * @author JM. Auffret
 */
/**
 * @author fhenry
 *
 */
public class RtpSender implements ReceiveStreamListener, SessionListener, RemoteListener, SendStreamListener {

	/**
	 * RTP manager (parent)
	 */
	private RtpManager rtpMgr;

	/**
	 * RTP session manager from JMF
	 */
	private RTPSessionMgr sessionMgr = null;

	/**
	 * Constructor
	 * 
	 * @param rtpMgr RTP manager
	 */
	public RtpSender(RtpManager r) {
		this.rtpMgr = r;
	}

	/**
	 * Start the RTP session
	 * 
	 * @param remoteHost Remote RTP host
	 * @param remotePort Remote RTP port
	 * @throws RTP exception
	 */
	public void startSession(String localHost, int localPort, String remoteHost, int remotePort) throws ExecutionException {
		try {		
			// Create a JMF RTP manager in charge of the RTP session
			sessionMgr = (RTPSessionMgr) RTPManager.newInstance();
	
            // Create a JMF RTP manager in charge of the RTP session
            sessionMgr = (RTPSessionMgr) RTPManager.newInstance();
            sessionMgr.addSessionListener(this);
            sessionMgr.addReceiveStreamListener(this);
            sessionMgr.addRemoteListener(this);
            sessionMgr.addSendStreamListener(this);
            sessionMgr.setChannel(rtpMgr.getChannel());
            
			// Define the local address of the RTP session
            InetAddress ipLocalAddr = InetAddress.getByName(localHost);
            SessionAddress localAddr =  new SessionAddress( ipLocalAddr, localPort);
	
			// Initialize the RTP session
			sessionMgr.initialize(localAddr);

			// Define the remote address of the RTP session
			InetAddress ipRemoteAddr = InetAddress.getByName(remoteHost);
			SessionAddress destAddr = new SessionAddress(ipRemoteAddr, remotePort);

			// Add the remote target to the RTP session
			sessionMgr.addTarget(destAddr);

			// Set the SDES value
			sessionMgr.getLocalParticipant().setSourceDescription(rtpMgr.getSDES());

			// Save the SSRC value corresponding to the local UE
			rtpMgr.setSSRC(sessionMgr.getSSRC());
			
		} catch(Exception e) {
			throw new ExecutionException("Can't start the RTP sender: " + e.getMessage());
		}
	}

	/**
	 * Stop the RTP session
	 * 
	 * @throws RTP exception
	 */
	public void stopSession() throws ExecutionException {
		try {
			if (sessionMgr != null) {
				sessionMgr.removeTargets("Session ended");
				sessionMgr.dispose();
				sessionMgr = null;
			}
		} catch (Exception e) {
			throw new ExecutionException("Can't stop the RTP sender: " + e.getMessage());
		}
	}

	/**
	 * Return the last RTP sequence number
	 * 
	 * @return Sequence number
	 */
	public int getSeqNum() {
		try {
			RTPTransmitter trans = sessionMgr.getRtpTransmitter();
			return trans.getLastSeqNumber();
		} catch(Exception e) {
			return -1;
		}		
	}

	/**
	 * Return the RTP session manager from JMF
	 * 
	 * @return RTP session manager object
	 */
	public RTPSessionMgr getRtpSessionMgr() {
		return sessionMgr;
	}
    
    /**
     * SessionListener.
     * 
     * @param evt Session event
     */
    public synchronized void update(SessionEvent evt) {
        if (evt instanceof LocalCollisionEvent) {
            // A collision event has been detected: a new SSRC has been
            // affected to the RTP channel
            long SSRC = ((LocalCollisionEvent) evt).getNewSSRC();

            // Update the SSRC value
            rtpMgr.setSSRC(SSRC);
        }
    }

    /**
     * ReceiveStreamListener
     * 
     * @param evt Received stream event
     */
    public synchronized void update(ReceiveStreamEvent evt) {
        ReceiveStream stream = evt.getReceiveStream();
        Participant participant = evt.getParticipant();

        // Retrieve the remote party mane
        String name = "unknown";
        if (participant != null)
            name = participant.getCNAME();

        // Retrieve the remote party SSRC
        String ssrc = "unknown";
        if (stream != null)
            ssrc = "" + stream.getSSRC();
        String remoteParty = ssrc + " (" + name + ")";

        if (evt instanceof NewReceiveStreamEvent) {
            // A new stream is received. In our case it doesn't mean that a new
            // physical stream is opened, but that a new remote source (or speaker)
            // will start sending audio data. In fact, the PTT media server sends
            // differents SSRC into a single physical stream, but JMF detects the
            // differents SSRC values and generates severals "NewReceiveStreamEvent"
            // events.
            stream = ((NewReceiveStreamEvent) evt).getReceiveStream();
            DataSource ds = stream.getDataSource();
            /* FH
            RTPPushDataSource pushDS = (RTPPushDataSource) ds; 
            PushSourceStream pushSS = pushDS.getStreams()[0];
            byte b[] = new byte [100];
            try {
                pushSS.read(b, 0, 100);                
            } catch (Exception e) {
                
            }
            */
        } else
        if (evt instanceof StreamMappedEvent) {
            //
        } else
        if (evt instanceof InactiveReceiveStreamEvent) {
            //
        } else
        if (evt instanceof ActiveReceiveStreamEvent) {
            //
        } else
        if (evt instanceof RemotePayloadChangeEvent) {
            //
        } else
        if (evt instanceof ByeEvent) {
            // to do
            
        } else
        if (evt instanceof ApplicationEvent) {
            // Forward event to the floor controller            
        }
    }

    /**
     * RemoteListener.
     * 
     * @param evt Remote event
     */
    public void update(RemoteEvent evt)
    {
        // If SR report is received : UE should send a RR report
        if (evt instanceof SenderReportEvent) {
            RTCPCompoundPacket packets = sessionMgr.sendReport();
        }
    }

    /**
     * SendStreamListener.
     * 
     * @param evt SendStream event
     */
    public void update(SendStreamEvent evt)
    {
        // FH
        if (evt instanceof RTPEvent) {
            // to do            
        }
    }

}
