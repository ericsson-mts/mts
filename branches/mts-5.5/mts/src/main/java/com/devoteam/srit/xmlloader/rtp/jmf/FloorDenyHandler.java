package com.devoteam.srit.xmlloader.rtp.jmf;

import javax.media.rtp.event.ApplicationEvent;


/**
 * A class to handle floor deny message
 */
class FloorDenyHandler {
    private FloorDenyHandler() {
        // Avoid instanciation
    }
    /**
     * handle floor deny message <code>
     *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1  
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |V=2|P|0 0 0 1 1|   PT=APP=204  |            length             |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |                      SSRC of PoC server                       |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |                          name=PoC1                            |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |  Reason code  |    Length     |         Reason Phrase         |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+                               :
     * :                                                               :
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * @param ctrl controler
     * @param evt the event
     */
    protected final static void handleFloorDeny(FloorController ctrl,
            ApplicationEvent evt) {
        // Floor request has been denied by the PoC server
        // -----------------------------------------------

        // Stop retransmission timer
        ctrl.timerT0.stop();

        // Notify listeners
        ctrl.notifyFloorListeners(new FloorEvent(FloorEvent.EVENT_FLOOR_DENY,
                extractDenyReason(evt.getAppData())));
    }
    
	/**
	 * Extract the reason phrase of the floor DENY event
	 * 
	 * @param data Application data
	 * @return Reason phrase
	 */
	private static String extractDenyReason(byte[] data) {
		//  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
		// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
		// |  Reason code  |    Length     |         Reason Phrase         |
		// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+                               :
		// :                                                               :
		// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
		String reason = "";
		try {
			int length = data[1];
			if (length != 0) {
				for(int i=2; i < (length + 2); i++) {
					String car = new Character((char)data[i]).toString();		
					reason = reason + car;
				}		
			}
		} catch(Exception e) {
			reason = "unknown";
		}
		return reason;
	}
}
