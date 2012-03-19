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
*//*
 * ListenpointRtpFlow.java
 *
 */
package com.devoteam.srit.xmlloader.rtp.flow;

import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.newstats.StatKey;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.rtp.MsgRtp;


/**
 * @author gpasquiers
 */
public class ListenpointRtpFlow extends Listenpoint {

    protected MsgRtpFlow _currentMessage = null;
    protected float endTimerNoPacket;
    protected float endTimerSilentFlow;
    protected float endTimerPeriodic;
    protected boolean qosMeasurment;
    protected boolean ignoreReceivedMessages;

    private RtpFlowEndTask _endOfFlowTask = null;

    private boolean _removed = false;

    /** Creates a Listenpoint specific from XML tree*/
    public ListenpointRtpFlow(Stack stack, Element root) throws Exception {
        super(stack, root);

        Element header = root.element("flow");

        this.endTimerNoPacket = ((StackRtpFlow) stack).endTimerNoPacket; // set the default value from config file
        if (header != null) {
            String endTimerNoPacketStr = header.attributeValue("endTimerNoPacket");
            //override value if given in listenpoint creation
            if (endTimerNoPacketStr != null) {
                this.endTimerNoPacket = Float.parseFloat(endTimerNoPacketStr); //set time given in attribute in seconds
            }
        }

        this.endTimerSilentFlow = ((StackRtpFlow) stack).endTimerSilentFlow; // set the default value from config file
        if (header != null) {

            String endTimerSilentFlowStr = header.attributeValue("endTimerSilentFlow");
            if (endTimerSilentFlowStr != null) {
                endTimerSilentFlow = Float.parseFloat(endTimerSilentFlowStr); //set time given in attribute in seconds
            }
        }

        this.endTimerPeriodic = ((StackRtpFlow) stack).endTimerPeriodic; // set the default value from config file
        if (header != null) {
            String endTimerPeriodicStr = header.attributeValue("endTimerPeriodic");
            if (endTimerPeriodicStr != null) {
                endTimerPeriodic = Float.parseFloat(endTimerPeriodicStr);//set time given in attribute in seconds
            }
        }

        this.qosMeasurment = ((StackRtpFlow) stack).qosMeasurment; // set the default value from config file
        if (header != null) {
            String qosMeasurmentStr = header.attributeValue("qosMeasurment");
            if (qosMeasurmentStr != null) {
                qosMeasurment = Boolean.parseBoolean(qosMeasurmentStr);
            }
        }

        this.ignoreReceivedMessages = ((StackRtpFlow) stack).ignoreReceivedMessages; // set the default value from config file
        if (header != null) {
            String ignoreReceivedMessagesStr = header.attributeValue("ignoreReceivedMessages");
            if (ignoreReceivedMessagesStr != null) {
                ignoreReceivedMessages = Boolean.parseBoolean(ignoreReceivedMessagesStr);
            }
        }
    }
    
    /** Creates a new instance of Listenpoint */
    public ListenpointRtpFlow(Stack stack, String name, String host, int port) throws Exception
    {
    	super(stack, name, host, port);
    	
        this.endTimerNoPacket = ((StackRtpFlow) stack).endTimerNoPacket; // set the default value from config file
        this.endTimerSilentFlow = ((StackRtpFlow) stack).endTimerSilentFlow; // set the default value from config file
        this.endTimerPeriodic = ((StackRtpFlow) stack).endTimerPeriodic; // set the default value from config file
        this.qosMeasurment = ((StackRtpFlow) stack).qosMeasurment; // set the default value from config file
        this.ignoreReceivedMessages = ((StackRtpFlow) stack).ignoreReceivedMessages; // set the default value from config file
    }
        
    /** Create a listenpoint to each Stack */
    @Override
    public boolean create(String protocol) throws Exception {
        boolean res = super.create(protocol);
        listenpointUdp.setAttachment(this);
        return res;
    }

    /** Create a listenpoint to each Stack */
    @Override
    public boolean remove() {
        _removed = true;
        return super.remove();
    }

    public boolean removed(){
        return _removed;
    }

    public String toString() {
        String ret = super.toString();
        ret += " endTimerNoPacket = " + endTimerNoPacket;
        if (endTimerSilentFlow != 0) {
            ret += " endTimerSilentFlow = " + endTimerSilentFlow;
        }
        if (endTimerPeriodic != 0) {
            ret += " endTimerPeriodic = " + endTimerPeriodic;
        }
        return ret;
    }



    protected synchronized void receiveMessage(MsgRtp message) throws Exception {
        // get the stack now (simpler)
        // StackRtpFlow stack = (StackRtpFlow) StackFactory.getStack(StackFactory.PROTOCOL_RTPFLOW);

        // create and start the endOfFlow task only when receiving the very first message
        if(null == _endOfFlowTask){
            _endOfFlowTask = new RtpFlowEndTask(this);
            // this task should remove itself once listenpoint is removed
            ((StackRtpFlow) this.stack).scheduler.execute(_endOfFlowTask, false);
        }

        // check if the packet is silent or not if necessary (endTimerSilentFlow "actif")
        if(endTimerSilentFlow >= 0){
            message.setIsSilence(((StackRtpFlow) this.stack).isSilentPacket(message.getData()));
        }

        // if we must handle the packet (received at least one packet, silent filtering disabled, or packet is not silence)
        if (_currentMessage != null || !((StackRtpFlow) this.stack).silentFiltering || !message.isSilence()) {

            // this is the first packet, create the RTPFlow message to receive the RTP packets
            if (null == _currentMessage) {
                _currentMessage = new MsgRtpFlow(((StackRtpFlow) this.stack).getCodecDict());
                _currentMessage.setChannel(message.getChannel());
                _currentMessage.setListenpoint(this);            
                _currentMessage.setProbe(message.getProbe());                
            }

            // update the timestamp for last received packet
            _currentMessage.updateLastPacketTimestamp();

            // add the rtp message to the rtpflow message
            _currentMessage.addReceivedPacket(message);

            // update the timestamp and counter for last non-silence received packet
            if(!message.isSilence()){
                _currentMessage.updateLastNonSilencePacketTimestamp();
                _currentMessage.setPacketNumberWithFilter(_currentMessage.getPacketNumber());
            }

            // update statistics
            StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TRANSPORT, _currentMessage.getTransport(), StackFactory.PROTOCOL_RTP, message.getTypeComplete() + StackFactory.PREFIX_INCOMING, "_transportNumber"), 1);
            StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TRANSPORT, _currentMessage.getTransport(), StackFactory.PROTOCOL_RTP, message.getTypeComplete() + StackFactory.PREFIX_INCOMING, "_transportBytes"), (float) message.getLength() / 1024 / 1024);
        }
    }
}
