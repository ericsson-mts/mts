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

package com.devoteam.srit.xmlloader.rtp.flow;

import com.devoteam.srit.xmlloader.core.newstats.StatKey;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.operations.basic.operators.PluggableParameterOperatorBinary;
import com.devoteam.srit.xmlloader.core.protocol.*;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.XMLElementRTPFLOWParser;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.rtp.MsgRtp;
import com.devoteam.srit.xmlloader.rtp.StackRtp;
import gp.utils.arrays.Array;
import gp.utils.scheduler.Scheduler;
import gp.utils.scheduler.Task;

import java.util.HashMap;
import java.util.Map;

public class StackRtpFlow extends StackRtp 
{

    protected Scheduler scheduler;
    protected CodecDictionary dico = new CodecDictionary();//dictionary of all codec associated with their information (payloadType, frequence...)
    protected float endTimerNoPacket;
    protected float endTimerSilentFlow;
    protected float endTimerPeriodic;
    protected boolean qosMeasurment;
    protected String silentPattern;
    protected float silentFrequencyThreshold;
    boolean silentFiltering;
    private HashMap <String, ListenpointRtpFlow> ssrcAndListenpointName;

    // for capture reassembling RTP packet mechanism
    Map<String, ListenpointRtpFlow> capture_point = null;
    
    
    /** Creates a new instance */
    public StackRtpFlow() throws Exception
    {
        super();
        this.ssrcAndListenpointName = new HashMap<String, ListenpointRtpFlow>();
        int nbThreads = getConfig().getInteger("SCHEDULER_THREAD_NUMBER", 2);
        scheduler = new Scheduler(nbThreads);

        try {
            // deprecated
            endTimerNoPacket = (float) Config.getConfigByName("rtp.properties").getDouble("scheduler.WAIT_END_OF_FLOW");
        }
        catch (Exception e) {
            endTimerNoPacket = (float) getConfig().getDouble("endtimer.NO_PACKET", 5);
        }

        endTimerSilentFlow = (float) getConfig().getDouble("endtimer.SILENT_FLOW", 0);
        endTimerPeriodic = (float) getConfig().getDouble("endtimer.PERIODIC", 0);
        qosMeasurment = getConfig().getBoolean("QOS_MEASURMENT", true);
        ignoreReceivedMessages = getConfig().getBoolean("IGNORE_RECEIVED_MESSAGES", false);

        String defaultPattern = "00000000000000000000";
        String silentPatternHexa = getConfig().getString("qos.PATTERN");
        if (silentPatternHexa.length() == 0) {
            silentPatternHexa = getConfig().getString("silent.PATTERN_HEXA", defaultPattern);
        }
        if (silentPatternHexa.length() == 0) {
            silentPatternHexa = defaultPattern;
        }
        Array motif = Array.fromHexString(silentPatternHexa);
        silentPattern = motif.toString();
        silentFrequencyThreshold = (float) getConfig().getDouble("silent.FREQUENCY_THRESHOLD", 0.2);
        silentFiltering = getConfig().getBoolean("silent.FILTERING", true);
        
        capture_point = new HashMap<String, ListenpointRtpFlow>();
        
    }

    public void addSSRCAndListenpointName(String key, ListenpointRtpFlow value)
    {
    	this.ssrcAndListenpointName.put(key, value);
    }
    
    /** override method for the possibility to send a flow */
    @Override
    public boolean sendMessage(Msg msg) throws Exception {
        RtpFlowSendTask task = new RtpFlowSendTask((MsgRtpFlow) msg, scheduler);

        scheduler.scheduleIn(task, ((MsgRtpFlow) msg).getDeltaTime());

        ((MsgRtpFlow) msg).firstMsgSent.acquire();
        
        // increment counters in the transport section
        incrStatTransport(msg, StackFactory.PREFIX_OUTGOING, StackFactory.PREFIX_INCOMING);
        return true;
    }

    @Override
    public boolean receiveMessage(Msg msg) throws Exception {
        // dispatch the message to the right listenpoint
        ListenpointRtpFlow listenpoint = (ListenpointRtpFlow) msg.getListenpoint().getAttachment();
        listenpoint.receiveMessage((MsgRtp) msg);
        return true;
    }

    @Override
    public boolean captureMessage(Msg msg) throws Exception {
        // dispatch the message to the right listenpoint
    	String localHost = msg.getChannel().getLocalHost();
    	int localPort = msg.getChannel().getLocalPort();
    	String name = "Capture_point_" + localHost + "_" + localPort;
    	
    	ListenpointRtpFlow listenpoint = capture_point.get(name);    
    	if (listenpoint == null)
    	{
    		listenpoint = new ListenpointRtpFlow(this, name, localHost, localPort);
    		capture_point.put(name, listenpoint);
    	}
        listenpoint.receiveMessage((MsgRtp) msg);
        return true;
    }
    
    /** 
     * Returns the XML Element Replacer to replace the "[parameter]" string 
     * in the XML document by the parameter values.
     * By Default it is a generic replacer for text protocol : it duplicates 
     * the current line for each value of the parameter 
     */
    @Override
    public XMLElementReplacer getElementReplacer() 
    {
        return XMLElementRTPFLOWParser.instance();//do not parse text in sendMessageRTPFLOW tag
    }

    public void receiveMsgRtpFlow(MsgRtpFlow msgRtpFlow) throws Exception {
        ListenpointRtpFlow listenpoint = (ListenpointRtpFlow) msgRtpFlow.getListenpoint();

        if (msgRtpFlow != null) {
            if (this.silentFiltering) {
                // remove the silent packets at the end only
                boolean done = false;
                while (!msgRtpFlow.getPacketList().isEmpty() && !done){
                    // endTimerSilentFlow was not active, so the isSilent flag must be computed
                    if(listenpoint.endTimerSilentFlow <= 0){
                        MsgRtp packet = msgRtpFlow.getPacketList().getLast();
                        packet.setIsSilence(this.isSilentPacket(packet.getData()));
                    }

                    if(msgRtpFlow.getPacketList().getLast().isSilence()){
                        msgRtpFlow.getPacketList().removeLast();
                        msgRtpFlow.setPacketNumber(msgRtpFlow.getPacketNumber()-1);
                    }
                    else{
                        done = true;
                    }
                }
            }

            if (this.qosMeasurment) {
                msgRtpFlow.getQoSinfo().setPayloadType(msgRtpFlow.getPayloadType());
                msgRtpFlow.getQoSinfo().calculMOS();

                // increment the statistic counters for Rtpflow section
                StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_RTPFLOW, msgRtpFlow.getType() + StackFactory.PREFIX_INCOMING, "_nbRtpFlow"), 1);
                StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_RTPFLOW, msgRtpFlow.getType() + StackFactory.PREFIX_INCOMING, "_bitrate"), msgRtpFlow.getQoSinfo().getMeanBitRate());
                StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_RTPFLOW, msgRtpFlow.getType() + StackFactory.PREFIX_INCOMING, "_packetLost"), msgRtpFlow.getQoSinfo().getPacketLost());
                StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_RTPFLOW, msgRtpFlow.getType() + StackFactory.PREFIX_INCOMING, "_packetDuplicated"), msgRtpFlow.getQoSinfo().getDuplicated());
                StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_RTPFLOW, msgRtpFlow.getType() + StackFactory.PREFIX_INCOMING, "_packetMissSequence"), msgRtpFlow.getQoSinfo().getPacketMissSequence());
                StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_RTPFLOW, msgRtpFlow.getType() + StackFactory.PREFIX_INCOMING, "_deltaTime"), msgRtpFlow.getQoSinfo().getMeanDelta());
                StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_RTPFLOW, msgRtpFlow.getType() + StackFactory.PREFIX_INCOMING, "_packetSpacing"), msgRtpFlow.getQoSinfo().getMeanPacketSpacing());
                StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_RTPFLOW, msgRtpFlow.getType() + StackFactory.PREFIX_INCOMING, "_jitter"), msgRtpFlow.getQoSinfo().getMeanJitter());
                StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_RTPFLOW, msgRtpFlow.getType() + StackFactory.PREFIX_INCOMING, "_mos"), msgRtpFlow.getQoSinfo().getEModele().getMos());
            }

            if (msgRtpFlow.getProbe() != null)
            {
            	msgRtpFlow.setListenpoint(null);
            	super.captureMessage(msgRtpFlow);
            }
            else if (msgRtpFlow.getListenpoint() != null)
            {
            	super.receiveMessage(msgRtpFlow);
            }
        }
    }

    /*this function is just used by the rtpflow protocol*/
    protected boolean isSilentPacket(Array array) throws Exception {
        // search the silent pattern
        if (array.toString().contains(this.silentPattern)) {
            return true;
        }

        // calculate the most popular frequency
        float freqMax = PluggableParameterOperatorBinary.calculePopular(array.getBytes(), true);
        // compare the frequency to the threshold
        if (freqMax > array.length * this.silentFrequencyThreshold) {
            return true;
        }
        return false;
    }

    public void scheduleTask(Task task, long delay) {
        scheduler.scheduleIn(task, delay);
    }

    public void unscheduleTask(Task task) {
        scheduler.unschedule(task);
    }

    public CodecDictionary getCodecDict() {
        return dico;
    }
}
