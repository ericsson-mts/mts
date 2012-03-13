/*
 * Created on Dec 2, 2004
 */
package com.devoteam.srit.xmlloader.rtp.flow;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import gp.utils.scheduler.Task;

/**
 * This task (one per listenpoint) sends the message the listenpoint is currently receiving
 * to the stack, when necessary
 * @author gpasquiers
 */
public class RtpFlowEndTask implements Task {

    private final ListenpointRtpFlow _listenpoint;
    private final StackRtpFlow _stack;
    private long _minimumInterval;

    public RtpFlowEndTask(ListenpointRtpFlow listenpoint) throws ExecutionException {
        _stack = (StackRtpFlow) StackFactory.getStack(StackFactory.PROTOCOL_RTPFLOW);
        _listenpoint = listenpoint;
        _minimumInterval = Long.MAX_VALUE;

        if (listenpoint.endTimerNoPacket > 0) {
            _minimumInterval = Math.min(_minimumInterval, (long) (listenpoint.endTimerNoPacket * 1000));
        }
        if (listenpoint.endTimerPeriodic > 0) {
            _minimumInterval = Math.min(_minimumInterval, (long) (listenpoint.endTimerPeriodic * 1000));
        }
        if (listenpoint.endTimerSilentFlow > 0) {
            _minimumInterval = Math.min(_minimumInterval, (long) (listenpoint.endTimerSilentFlow * 1000));
        }
    }

    public void execute() {
        long now = System.currentTimeMillis();

        long nextPossibleEnd = now + _minimumInterval;
        MsgRtpFlow nextEndingMessage = null;

        // for each message (now only one)
        if (null != _listenpoint._currentMessage) {
            if (_listenpoint.endTimerNoPacket > 0) {
                long timestamp = _listenpoint._currentMessage.getLastPacketTimestamp() + (long) (_listenpoint.endTimerNoPacket * 1000);
                if (timestamp < nextPossibleEnd) {
                    nextPossibleEnd = timestamp;
                    nextEndingMessage = _listenpoint._currentMessage;
                }
            }
        }

        // for each message (now only one)
        if (null != _listenpoint._currentMessage) {
            if (_listenpoint.endTimerSilentFlow > 0) {
                long timestamp = _listenpoint._currentMessage.getLastNonSilencePacketTimestamp() + (long) (_listenpoint.endTimerSilentFlow * 1000);
                if (timestamp < nextPossibleEnd) {
                    nextPossibleEnd = timestamp;
                    nextEndingMessage = _listenpoint._currentMessage;
                }
            }
        }

        // for each message (now only one)
        if (null != _listenpoint._currentMessage) {
            if (_listenpoint.endTimerPeriodic > 0) {
                long timestamp = _listenpoint._currentMessage.getCreationTimestamp() + (long) (_listenpoint.endTimerPeriodic * 1000);
                if (timestamp < nextPossibleEnd) {
                    nextPossibleEnd = timestamp;
                    nextEndingMessage = _listenpoint._currentMessage;
                }
            }
        }



        try {
            // will not schedule itself again if the listepoint is closed
            if (!_listenpoint.removed()) {
                if (null != nextEndingMessage) {
                    if (nextPossibleEnd <= now) {
                        synchronized (_listenpoint) {
                            try {
                                MsgRtpFlow msg = _listenpoint._currentMessage;
                                _listenpoint._currentMessage = null;
                                _stack.receiveMsgRtpFlow(msg);
                            }
                            catch (Exception ex) {
                                // should never happen
                                ex.printStackTrace();
                            }
                        }
                        this.execute();
                    }
                    else {
                        _stack.scheduler.scheduleAt(this, nextPossibleEnd);
                    }
                }
                else {
                    // will try again in _minimumInterval if there is currently no flow to handle
                    _stack.scheduler.scheduleIn(this, _minimumInterval);
                }
            }
        }
        catch (Exception ex) {
            // should never happen
            ex.printStackTrace();
        }
    }
}
