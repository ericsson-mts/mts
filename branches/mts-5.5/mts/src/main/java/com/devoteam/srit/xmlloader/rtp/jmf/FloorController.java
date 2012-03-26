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

import java.util.Iterator;

import javax.media.rtp.event.ApplicationEvent;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.sun.media.rtp.RTCPAPPPacket;
import com.sun.media.rtp.RTCPCompoundPacket;
import java.nio.ByteBuffer;

/**
 * Floor controller that has in charge the management of the floor
 * over RTCP:<br>
 *   - Floor request procedure,<br>
 *   - Floor release procedure,<br>
 *   - Floor events reception (Idle, Grant, Taken, Deny, Revoke),
 *   - Floor timers.<br>
 * 
 * @author JM. Auffret
 */
public class FloorController
	extends FloorObserver
	implements java.awt.event.ActionListener {

	/**
	 * Timer T0 (in ms)
	 */
	public static int T0 = 1500;

	/**
	 * Timer T1 (in ms)
	 */
	public static int T1 = 1500;

	/**
	 * PoC version constant
	 */
	private final static int PoC_VERSION =
		('P' << 24) + ('o' << 16) + ('C' << 8) + '1';

	/**
     * Max size of the floor request options
     */
    public final static int MAXIMUM_REQUEST_OPTION_ARRAY_SIZE = 64;
    
    /**
     * TBCP priority ID
     */
    public final static int TBCCP_REQUEST_OPTION_ID_PRIORITY = 1;
    
    /**
     * TBCP timestamp ID
     */
    public final static int TBCCP_REQUEST_OPTION_ID_TIMESTAMP = 2;

	/**
	 * RTP manager (parent)
	 */
	private RtpManager rtpMgr;

	/**
	 * Floor request retransmission timer (T0)
	 */
	protected FloorTimer timerT0 = null;

	/**
	 * Floor release retransmission timer (T1)
	 */
	protected FloorTimer timerT1 = null;

	/**
	 * Last received ack subtype
	 */
	private int acknoledgedType = 0;
	
	/**
	 * Constructor
	 * 
	 * @param rtpMgr RTP manager
	 * @param config Floor configuration
	 */
	public FloorController(RtpManager rtpMgr) {
		this.rtpMgr = rtpMgr;		
		
		// Create retransmission timers
		timerT0 = new FloorTimer(T0);
		timerT0.addActionListener(this);
		
		timerT1 = new FloorTimer(T1);
		timerT1.addActionListener(this);
	}

	/**
	 * Stop the controller
	 * 
	 * @throws RtpException
	 */
	public void stop() throws ExecutionException {		
		// Stop timers
		stopTimers();
	}
	
	/**
	 * Stop retransmission timers
	 */
	public void stopTimers() {
		timerT0.stop();
		timerT1.stop();
	}

	/**
	 * Receives a floor event
	 * 
	 * @param evt Floor event
	 */
	public synchronized void receiveFloorEvent(ApplicationEvent evt) {
		// Analyze APP event subtype
		switch (evt.getAppSubType()) {
			case FloorEvent.EVENT_FLOOR_DENY :
			    FloorDenyHandler.handleFloorDeny(this,evt);
				break;
			case FloorEvent.EVENT_FLOOR_GRANT :
				FloorGrantHandler.handleFloorGrant(this, evt);
				break;
			case FloorEvent.EVENT_FLOOR_IDLE :
			    FloorIdleHandler.handleFloorIdle(this,evt);
				break;
			case FloorEvent.EVENT_FLOOR_TAKEN :
			case FloorEvent.EVENT_FLOOR_TAKEN_ACK :
			    FloorTakenHandler.handleFloorTaken(this, evt);
				break;
			case FloorEvent.EVENT_FLOOR_REVOKE :
			    FloorRevokeHandler.handleFloorRevoke(this, evt);
				break;
			default :
				// Unknown event
				break;
		}
	}

	/**
	 * Send a floor request
	 * 
	 * @throws RtpException
	 */
	public synchronized void floorRequest() throws ExecutionException {
		// Send a floor request
		sendFloorRequest(rtpMgr.getTbcpParameters());

		// Stop timer T1
		timerT1.stop();

		// Start timer T0
		timerT0.start();
	}
	
	/**
     * Build byte array containing options passed in the floor request message.
     * Note that option array size is limited to MAXIMUM_REQUEST_OPTION_ARRAY_SIZE 
     * and can lead to exception if a too big option is sent. 
     * 
     * @param ol Option list
     * @return Array containg the data
     */
    private final static byte[] buildData(OptionList ol) {
        ByteBuffer buffer = ByteBuffer.allocate(MAXIMUM_REQUEST_OPTION_ARRAY_SIZE);
        Iterator i = ol.iterator();
        while(i.hasNext()) {
            Option o = (Option)i.next();
            buffer.put((byte)(o.getId()& 0xFF)); // Option id
            buffer.put((byte)(o.getLength()& 0xFF)); // Option length
            buffer.put(o.getValue());
            while(buffer.position()%4!=0) {
                // Padding
                buffer.put((byte)0x00);
            }
        }
        byte[] ret = new byte[buffer.position()];
        if(buffer.position()>0) {
            System.arraycopy(buffer.array(),0,ret,0,buffer.position());
        }
        return  ret;        
    }
	
    /**
	 * Send a floor request event
	 * 
	 * @throws RtpException
	 */
	private void sendFloorRequest(TbcpParameters tbcpParams) throws ExecutionException {
		try {
			// 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1  
			// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
			// |V=2|P|0 0 0 0 0|   PT=APP=204  |          length=2             |
			// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
			// |                          SSRC of UE                           |
			// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
			// |                          name=PoC1                            |
			// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

			// Create APP event
			byte appdata[] = new byte[0];
			RTCPAPPPacket evt =
				new RTCPAPPPacket(
					(int) rtpMgr.getSSRC(),
					PoC_VERSION,
					FloorEvent.EVENT_FLOOR_REQUEST,
					appdata);

			// Send the APP event
			rtpMgr.sender.getRtpSessionMgr().sendRtcpAppEvent(evt);

			// Update the floor controller state
		} catch (Exception e) {
			throw new ExecutionException("Can't send floor request: " + e.getMessage());
		}
	}
	
	
	/**
	 * Send a floor request event
	 * 
	 * @throws RtpException
	 */
	private void sendFloorRequest2(TbcpParameters tbcpParams) throws ExecutionException {
		
		// Build options
        OptionList options = new OptionList();
        if (tbcpParams.isQueuingEnabled()) {
        	// Queuing option
            byte [] data = new byte[1];
            data[0] = (byte)(tbcpParams.getPriority() & 0xFF);
            options.add(new Option(TBCCP_REQUEST_OPTION_ID_PRIORITY,data));
        }
        if(tbcpParams.isTimestampEnabled()) {
        	// Time stamp option
            byte [] data = new byte[8];
            options.add(new Option(TBCCP_REQUEST_OPTION_ID_TIMESTAMP,data));
        }
		byte[] data = buildData(options);
		
		
		try {
			// 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1  
			// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
			// |V=2|P|0 0 0 0 0|   PT=APP=204  |          length=2             |
			// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
			// |                          SSRC of UE                           |
			// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
			// |                          name=PoC1                            |
			// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

			// Create APP event
			byte appdata[] = new byte[0];
			RTCPAPPPacket evt =
				new RTCPAPPPacket(
					(int) rtpMgr.getSSRC(),
					PoC_VERSION,
					FloorEvent.EVENT_FLOOR_REQUEST,
					data);

			// Send the APP event
			rtpMgr.sender.getRtpSessionMgr().sendRtcpAppEvent(evt);

			// Update statistics
		} catch (Exception e) {
			throw new ExecutionException("Can't send floor request: " + e.getMessage());
		}
	}

	/**
	 * Send a floor release
	 * 
	 * @throws RtpException
	 */
	public synchronized void floorRelease() throws ExecutionException {
		// Send a floor release
		sendFloorRelease(false);

		// Stop timer T0
		timerT0.stop();

		// Start timer T1
		timerT1.start();
		
		// When the floor is released, UE should send a SR report
		sendSR();		
	}

	/**
	 * Send a floor release event
	 * 
	 * @throws RtpException
	 */
	private void sendFloorRelease(boolean ignore) throws ExecutionException {
		try {
			//  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1  
			// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
			// |V=2|P|0 0 1 0 0|   PT=APP=204  |          length=3             |
			// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
			// |                      SSRC of granted UE                       |
			// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
			// |                          name=PoC1                            |
			// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
			// |sequence number of last packet |						padding|
			// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 

			
			// Retrieve the last sequence number for RTP
			int num = rtpMgr.sender.getSeqNum();
			if (num == -1) {
            	ignore = true;
            }
			byte appdata[] = new byte[4];
			appdata[0] = (byte) ((num & 0xFF00) >> 8);
			appdata[1] = (byte) (num & 0xFF);
			if(ignore)
                appdata[2] = (byte)(0x01 << 7);
            else
                appdata[2] = 0x00;
			appdata[3] = 0x00;

			// Create APP event
			RTCPAPPPacket evt =
				new RTCPAPPPacket(
					(int) rtpMgr.getSSRC(),
					PoC_VERSION,
					FloorEvent.EVENT_FLOOR_RELEASE,
					appdata);

			// Send the APP event
			rtpMgr.sender.getRtpSessionMgr().sendRtcpAppEvent(evt);

			// Update statistics
		} catch (Exception e) {
			throw new ExecutionException("Can't send floor release: " + e.getMessage());
		}
	}

	
		
	/**
	 * Send a floor release
	 * 
	 * @throws RtpException
	 */
	public synchronized void floorAck() throws ExecutionException {
    	//  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
    	// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    	// |V=2|P|0 0 1 1 1| PT=APP=204 | length=3 |
    	// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    	// | SSRC of PoC Client sending the acknowledgement message |
    	// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    	// | name=PoC1 |
    	// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    	// | subtype | reason code ! padding |
    	// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
        
        try {
            // Set data
        	byte appdata[] = new byte[4];
        	appdata[0] = (byte)((acknoledgedType << 3) & 0xFF);
        	appdata[1] = 0x0;
        	appdata[2] = 0x0;
        	appdata[3] = 0x0;

            // Create APP event       	
        	RTCPAPPPacket evt =
				new RTCPAPPPacket(
					(int) rtpMgr.getSSRC(),
					PoC_VERSION,
					FloorEvent.EVENT_ACK,
					appdata);
        	
        	
        	// Send the APP event
			rtpMgr.sender.getRtpSessionMgr().sendRtcpAppEvent(evt);

        } catch (Exception e) {
        	throw new ExecutionException("Can't send floor Ack: " + e.getMessage());
        }
        
    }
	
	
	/**
	 * Periodic floor event retransmission
	 */
	public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
			if (evt.getSource() == timerT0) {
				// Floor request retransmission
				sendFloorRequest(rtpMgr.getTbcpParameters());
			} else
			if (evt.getSource() == timerT1) {
				// Floor release retransmission
				sendFloorRelease(false);
			}
		} catch(Exception e) {
			// to do
		}
	}
	
	/**
	 * Send a report
	 *
	 * @throws RtpException
	 */
	private void sendSR() throws ExecutionException {
		try {
			// Send the report
			RTCPCompoundPacket packets =
				rtpMgr.sender.getRtpSessionMgr().sendReport();
		} catch (Exception e) {
			throw new ExecutionException("Can't send SR event: " + e.getMessage());
		}
	}
	
	/**
	 * Convert a byte array to its hexadecimal representtaion
	 * 
	 * @param data Byte array
	 * @return String
	 */
	public static String byteToHexString(byte[] data) {
		String result = "";
    	if (data.length != 0) {
    		// Format byte array to string representation
    		for(int i=0; i < data.length; i++) {
    			int value = data[i];
				if (value < 0) {
					value = (value & 0xFF) | 128;
				}
				String car = Integer.toHexString(value);
				if (car.length() == 1) {
					car = "0" + car;
				}   			
    			result = result + "0x" + car + " ";
    		}
    	} else {
    		result = "none";
    	}
		return result;		
	}	
	
	/**
	 * @return Returns the acknoledgedType.
	 */
	public int getAcknoledgedType() {
		return acknoledgedType;
	}
	
	/**
	 * @param acknoledgedType The acknoledgedType to set.
	 */
	public synchronized void setAcknoledgedType(int acknoledgedType) {
		this.acknoledgedType = acknoledgedType;
	}
}
