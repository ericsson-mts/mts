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
import javax.media.rtp.Participant;
import javax.media.rtp.RTPManager;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.RemoteListener;
import javax.media.rtp.SessionAddress;
import javax.media.rtp.SessionListener;
import javax.media.rtp.event.ActiveReceiveStreamEvent;
import javax.media.rtp.event.ApplicationEvent;
import javax.media.rtp.event.ByeEvent;
import javax.media.rtp.event.InactiveReceiveStreamEvent;
import javax.media.rtp.event.LocalCollisionEvent;
import javax.media.rtp.event.NewReceiveStreamEvent;
import javax.media.rtp.event.ReceiveStreamEvent;
import javax.media.rtp.event.RemoteEvent;
import javax.media.rtp.event.RemotePayloadChangeEvent;
import javax.media.rtp.event.SenderReportEvent;
import javax.media.rtp.event.SessionEvent;
import javax.media.rtp.event.StreamMappedEvent;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.sun.media.rtp.RTCPCompoundPacket;
import com.sun.media.rtp.RTPSessionMgr;

/**
 * RTP receiver that has in charge to open and to control incoming
 * RTP/RTCP streams.
 * 
 * @author JM. Auffret
 */
public class RtpReceiver
	implements
		ReceiveStreamListener,
		SessionListener,
		RemoteListener {
    
	/**
	 * RTP manager (parent)
	 */
	private RtpManager rtpMgr;

	/**
	 * Local port number (RTP listening port)
	 */
	private int localPort;

	/**
	 * RTP session manager from JMF
	 */
	private RTPSessionMgr sessionMgr = null;

	/**
	 * Constructor
	 * 
	 * @param rtpMgr RTP manager
	 */
	public RtpReceiver(RtpManager r) {
		this.rtpMgr = r;
	}

	/**
	 * Start the RTP session
	 * 
	 * @param localPort Local RTP port
	 * @throws RTP exception
	 */
	public void startSession(int lp) throws ExecutionException {
		this.localPort = lp;
		try {
			// Create a JMF RTP manager in charge of the RTP session
			sessionMgr = (RTPSessionMgr) RTPManager.newInstance();
			sessionMgr.addSessionListener(this);
			sessionMgr.addReceiveStreamListener(this);
	        sessionMgr.addRemoteListener(this);
            sessionMgr.setChannel(rtpMgr.getChannel());
            
			// Define the local address of the RTP session
			SessionAddress localAddr =
				new SessionAddress(InetAddress.getLocalHost(), localPort);

			// Initialize the RTP session
			sessionMgr.initialize(localAddr);

			// Set the SDES value
			sessionMgr.getLocalParticipant().setSourceDescription(rtpMgr.getSDES());

			// Define the remote address of the RTP session: it's
			// the local address because the remote address may be not
			// yet known by UE (200 OK not received). It's necessary to
			// open the RTP session just after the INVITE because some RTCP
			// events may sent by the PoC server before the 200 OK response.
			// Then for a receiver it's not necessary to specify a remote
			// address: why JMF needs it ??
			InetAddress ipAddr = InetAddress.getLocalHost();
			SessionAddress destAddr = new SessionAddress(ipAddr, localPort);

			// Add the remote target to the RTP session
			sessionMgr.addTarget(destAddr);

		} catch(Exception e) {
			throw new ExecutionException("Can't start the RTP receiver: " + e.getMessage());
		}
	}

	/**
	 * Stop the RTP session
	 * 
	 * @throws RTP exception
	 */
	public void stopSession() throws ExecutionException {
		try {
			// Close the RTP session
			if (sessionMgr != null) {
				sessionMgr.removeTargets("Closing session");
			}

			// Free ressources
			if (sessionMgr != null) {
				sessionMgr.dispose();
				sessionMgr = null;
			}
						
		} catch (Exception e) {
			throw new ExecutionException("Can't stop the RTP receiver: " + e.getMessage());
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
			// affected to the RTP connection
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
}
