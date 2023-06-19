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
*/package javax.media.rtp;

/**
 * Adaptation of the JMF implementation to support new statistic counters
 *  
 * @author JM. Auffret
 */
public interface GlobalReceptionStats {

	public abstract int getBadRTCPPkts();

	public abstract int getBadRTPkts();

	public abstract int getBytesRecd();

	public abstract int getRtcpBytesRecd();

	public abstract int getLocalColls();

	public abstract int getMalformedBye();

	public abstract int getMalformedRR();

	public abstract int getMalformedSDES();

	public abstract int getMalformedSR();

	public abstract int getPacketsLooped();

	public abstract int getPacketsRecd();

	public abstract int getRTCPRecd();

	public abstract int getRemoteColls();

	public abstract int getSRRecd();

	public abstract int getTransmitFailed();

	public abstract int getUnknownTypes();
}
