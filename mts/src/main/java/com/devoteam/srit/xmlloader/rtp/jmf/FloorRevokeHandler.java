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

import javax.media.rtp.event.ApplicationEvent;

/**
 * A class to handle floor revoke.
 */
class FloorRevokeHandler {

    private FloorRevokeHandler() {
        // Avoid intanciation
    }
    
	//  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1  
	// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	// |V=2|P|0 0 1 1 0|   PT=APP=204  |          length               |
	// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	// |                      SSRC of PoC server                       |
	// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	// |                          name=PoC1                            |
	// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	// |           reason code         |    additional information     |
	// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    protected final static void handleFloorRevoke(FloorController ctrl, ApplicationEvent evt) {
		// Floor has been removed by the PoC server
		// ----------------------------------------

		// Extract data associated to the revoke event
		String reason = null;
		int code = extractRevokeReasonCode(evt.getAppData());
		if (code == 1) {
			reason = "only one user in the session";
		} else
		if (code == 2) {
			reason = "talk burst too long";
			int retryAfter = extractRevokeRetryValue(evt.getAppData());
			if (retryAfter > 0) {
				reason = reason + " (retry after " + retryAfter + " s)";
			}
		} else
		if (code == 3) {
			reason =
				"the server has closed the granted source for an internal reason";
		}	

		// Notify listeners
		ctrl.notifyFloorListeners(
			new FloorEvent(FloorEvent.EVENT_FLOOR_REVOKE, reason));
    }
    
	/**
	 * Extract the reason code of the floor REVOKE event
	 * 
	 * @param data Application data
	 * @return Code
	 */
	private static int extractRevokeReasonCode(byte[] data) {
		//  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
		// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
		// |           reason code         |    additional information     |
		// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
		int code = data[0] + (data[1] >> 8);
		return code;
	}
	
	/**
	 * Extract the retry after value of the floor REVOKE event
	 * 
	 * @param data Application data
	 * @return Retry after value in seconds
	 */
	private static int extractRevokeRetryValue(byte[] data) {
		//  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
		// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
		// |           reason code         |    additional information     |
		// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
		int retryAfter = data[2] + (data[3] >> 8);
		return retryAfter;
	}
}
