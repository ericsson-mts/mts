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

import javax.media.rtp.GlobalReceptionStats;

/**
 * Adaptation of the JMF implementation to support new statistic counters
 *  
 * @author JM. Auffret
 */
public class OverallStats implements GlobalReceptionStats {

	public OverallStats() {
		numPackets = 0;
		numBytes = 0;
		numRtcpBytes = 0;
		numBadRTPPkts = 0;
		numLocalColl = 0;
		numRemoteColl = 0;
		numPktsLooped = 0;
		numTransmitFailed = 0;
		numRTCPRecd = 0;
		numSRRecd = 0;
		numBadRTCPPkts = 0;
		numUnknownTypes = 0;
		numMalformedRR = 0;
		numMalformedSDES = 0;
		numMalformedBye = 0;
		numMalformedSR = 0;
	}

	public int getBadRTCPPkts() {
		return numBadRTCPPkts;
	}

	public int getBadRTPkts() {
		return numBadRTPPkts;
	}

	public int getBytesRecd() {
		return numBytes;
	}

	public int getRtcpBytesRecd() {
		return numRtcpBytes;
	}

	public int getLocalColls() {
		return numLocalColl;
	}

	public int getMalformedBye() {
		return numMalformedBye;
	}

	public int getMalformedRR() {
		return numMalformedRR;
	}

	public int getMalformedSDES() {
		return numMalformedSDES;
	}

	public int getMalformedSR() {
		return numMalformedSR;
	}

	public int getPacketsLooped() {
		return numPktsLooped;
	}

	public int getPacketsRecd() {
		return numPackets;
	}

	public int getRtcpPacketsRecd() {
		return numPackets;
	}

	public int getRTCPRecd() {
		return numRTCPRecd;
	}

	public int getRemoteColls() {
		return numRemoteColl;
	}

	public int getSRRecd() {
		return numSRRecd;
	}

	public int getTransmitFailed() {
		return numTransmitFailed;
	}

	public int getUnknownTypes() {
		return numUnknownTypes;
	}

	public String toString() {
		String s =
			"Packets Recd "
				+ getPacketsRecd()
				+ "\nBytes Recd "
				+ getBytesRecd()
				+ "\nRTCP bytes Recd"
				+ getRtcpBytesRecd()
				+ "\nBadRTP "
				+ getBadRTPkts()
				+ "\nLocalColl "
				+ getLocalColls()
				+ "\nRemoteColl "
				+ getRemoteColls()
				+ "\nPacketsLooped "
				+ getPacketsLooped()
				+ "\ngetTransmitFailed "
				+ getTransmitFailed()
				+ "\nRTCPRecd "
				+ getTransmitFailed()
				+ "\nSRRecd "
				+ getSRRecd()
				+ "\nBadRTCPPkts "
				+ getBadRTCPPkts()
				+ "\nUnknown "
				+ getUnknownTypes()
				+ "\nMalformedRR "
				+ getMalformedRR()
				+ "\nMalformedSDES "
				+ getMalformedSDES()
				+ "\nMalformedBye "
				+ getMalformedBye()
				+ "\nMalformedSR "
				+ getMalformedSR();
		return s;
	}

	public synchronized void update(int i, int j) {
		switch (i) {
			case 0 : // '\0'
				numPackets += j;
				break;

			case 1 : // '\001'
				numBytes += j;
				break;

			case 2 : // '\002'
				numBadRTPPkts += j;
				break;

			case 3 : // '\003'
				numLocalColl += j;
				break;

			case 4 : // '\004'
				numRemoteColl += j;
				break;

			case 5 : // '\005'
				numPktsLooped += j;
				break;

			case 6 : // '\006'
				numTransmitFailed += j;
				break;

			case 11 : // '\013'
				numRTCPRecd += j;
				break;

			case 12 : // '\f'
				numSRRecd += j;
				break;

			case 13 : // '\r'
				numBadRTPPkts += j;
				break;

			case 14 : // '\016'
				numUnknownTypes += j;
				break;

			case 15 : // '\017'
				numMalformedRR += j;
				break;

			case 16 : // '\020'
				numMalformedSDES += j;
				break;

			case 17 : // '\021'
				numMalformedBye += j;
				break;

			case 18 : // '\022'
				numMalformedSR += j;
				break;

			case 19 :
				numRtcpBytes += j;
				break;
		}
	}

	public static final int PACKETRECD = 0;
	public static final int BYTESRECD = 1;
	public static final int BADRTPPACKET = 2;
	public static final int LOCALCOLL = 3;
	public static final int REMOTECOLL = 4;
	public static final int PACKETSLOOPED = 5;
	public static final int TRANSMITFAILED = 6;
	public static final int RTCPRECD = 11;
	public static final int SRRECD = 12;
	public static final int BADRTCPPACKET = 13;
	public static final int UNKNOWNTYPE = 14;
	public static final int MALFORMEDRR = 15;
	public static final int MALFORMEDSDES = 16;
	public static final int MALFORMEDBYE = 17;
	public static final int MALFORMEDSR = 18;
	public static final int RTCPBYTESRECD = 19;

	private int numPackets;
	private int numBytes;
	private int numRtcpBytes;
	private int numBadRTPPkts;
	private int numLocalColl;
	private int numRemoteColl;
	private int numPktsLooped;
	private int numTransmitFailed;
	private int numRTCPRecd;
	private int numSRRecd;
	private int numBadRTCPPkts;
	private int numUnknownTypes;
	private int numMalformedRR;
	private int numMalformedSDES;
	private int numMalformedBye;
	private int numMalformedSR;
}
