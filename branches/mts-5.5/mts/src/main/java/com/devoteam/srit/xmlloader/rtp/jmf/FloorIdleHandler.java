package com.devoteam.srit.xmlloader.rtp.jmf;

import javax.media.rtp.event.ApplicationEvent;

/**
 * A class to handle floor idle message
 */
class FloorIdleHandler {
    
    private FloorIdleHandler() {
        // Avoid instanciation
    }
    /**
     * handle floor idle message. <code>
     *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1  
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |V=2|P|0 0 1 0 1|   PT=APP=204  |          length=2             |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |                      SSRC of PoC server                       |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |                          name=PoC1                            |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * </code>
     * 
     * @param ctrl
     *            the controler
     * @param evt
     *            the event
     */
    protected final static void handleFloorIdle(FloorController ctrl,
            ApplicationEvent evt) {

        // Stop retransmission timer
        ctrl.timerT1.stop();

        // Notify listeners
        ctrl.notifyFloorListeners(new FloorEvent(FloorEvent.EVENT_FLOOR_IDLE));
    }
}
