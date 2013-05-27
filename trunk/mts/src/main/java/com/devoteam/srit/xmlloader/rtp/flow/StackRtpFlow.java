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

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.newstats.StatKey;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.operations.basic.operators.PluggableParameterOperatorBinary;
import com.devoteam.srit.xmlloader.core.protocol.*;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.XMLElementRTPFLOWParser;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.rtp.ListenpointRtp;
import com.devoteam.srit.xmlloader.rtp.MsgRtp;
import com.devoteam.srit.xmlloader.rtp.StackRtp;
import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.scheduler.Scheduler;
import gp.utils.scheduler.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StackRtpFlow extends StackRtp {

    protected Scheduler scheduler;
    CodecDictionary dico = new CodecDictionary();//dictionary of all codec associated with their information (payloadType, frequence...)
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
    
    public StackRtpFlow() throws Exception {
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
    
    /** Creates a Listenpoint specific to each Stack */
    @Override
    public synchronized Listenpoint parseListenpointFromXml(Element root) throws Exception {
        return new ListenpointRtpFlow(this, root);
    }

    /** Creates a specific Msg */
    @Override
    public Msg parseMsgFromXml(Boolean request, Element root, Runner runner) throws Exception {
        MsgRtpFlow msg = null;

        if (root.element("flow") != null) {
            msg = parseFlow(root.element("flow"), runner);
        }
        else {
            throw new Exception("No flow tag in sendMessageRTPFLOW present");
        }

        return msg;
    }

    public MsgRtpFlow parseFlow(Element flow, Runner runner) throws Exception {
        MsgRtp msg = parsePacketHeader(flow, runner);

        List<Array> listPayload = parsePacketPayload(flow, runner);
        List<Integer> listSequm = parsePacketAttribute(flow, runner, "seqnum");
        List<Long> listTimestamp = parsePacketAttribute(flow, runner, "timestamp");
        List<Integer> listMarker = parsePacketAttribute(flow, runner, "mark");

        MsgRtpFlow flowRtp = new MsgRtpFlow(dico, listPayload, listSequm, listTimestamp, listMarker, msg);
        //WARNING: part above must be done before setting flow parameter

        String duration = flow.attributeValue("duration");
        String packetNumber = flow.attributeValue("packetNumber");
        String bitRate = flow.attributeValue("bitRate");
        String deltaTime = flow.attributeValue("deltaTime");
        String deltaTimestamp = flow.attributeValue("deltaTimestamp");
        String jitterDelay = flow.attributeValue("jitterDelay");
        String packetLost = flow.attributeValue("packetLost");
        String synchronous = flow.attributeValue("synchronous");

        if ((bitRate != null) && (deltaTime != null)) {
            throw new Exception("attribute <bitRate> and <deltaTime> of flow tag cannot be set in the same time");
        }

        if ((bitRate == null) && (deltaTime == null)) {
            throw new Exception("one of the attribute <bitRate> or <deltaTime> of flow tag must be set");
        }

        if ((packetNumber == null) && (duration == null)) {
            throw new Exception("one of the attribute <packetNumber> or <duration> of flow tag must be set");
        }

        if ((listPayload.size() > 1)
                && (packetNumber != null) && (Integer.parseInt(packetNumber) > listPayload.size())
                && ((listSequm.size() > 1) || (listTimestamp.size() > 1))) {
            throw new Exception("cannot use greater <packetNumber> than number of payload given when use in coordination with list of sequence number or timestamp");
        }

        if (packetNumber != null) {
            flowRtp.setPacketNumber(Integer.parseInt(packetNumber));
        }
        if (deltaTime != null) {
            LinkedList<String> listAttribute = runner.getParameterPool().parse(deltaTime);
            if (listAttribute.size() > 0)//if more than one element
            {
                ArrayList listAttributeData = new ArrayList();

                int delta;
                for (Iterator<String> it = listAttribute.iterator(); it.hasNext();) {
                    delta = Integer.parseInt(it.next());
                    if (delta < 0)//if delta is negatif, set 0 for the time to send
                    {
                        listAttributeData.add(0);
                    }
                    else {
                        listAttributeData.add(delta);
                    }

                }
                flowRtp.setDeltaTimeList(listAttributeData);
            }
        }

        if (duration != null) {
            flowRtp.setDuration(Float.parseFloat(duration));
        }
        if (bitRate != null) {
            flowRtp.setBitRate(Float.parseFloat(bitRate));
        }
        if (deltaTimestamp != null) {
            flowRtp.setDeltaTimestamp(Integer.parseInt(deltaTimestamp));
        }
        else {
            flowRtp.setDeltaTimestamp((int) flowRtp.getDataRTPLength());
        }
        if (jitterDelay != null) {
            flowRtp.setJitterDelay(Float.parseFloat(jitterDelay));
        }

        //now compute missing values with values which are given in operation
        if (packetNumber == null && bitRate != null)//compute packetNumber in function of the bitrate
        {
            flowRtp.setPacketNumber((int) (flowRtp.getBitRate() * 1024 * flowRtp.getDuration() / (flowRtp.getDataRTPLength() * 8)));
        }
        else if (packetNumber == null && bitRate == null)//compute packetNumber in function of deltaTimestamp
        {
            int newPacketNumber = (int) (flowRtp.getDuration() * 1000 / flowRtp.getDeltaTime());
            if ((packetNumber == null) || (newPacketNumber < flowRtp.getPacketNumber())) {
                flowRtp.setPacketNumber(newPacketNumber);
            }
        }

        if (packetLost != null) {
            float packetLostPercent = Float.parseFloat(packetLost);
            if ((packetLostPercent < 0) || (packetLostPercent > 100)) {
                throw new Exception("packetLost attribute is a percentage (between 0 and 100)");
            }

            flowRtp.setPacketLost(packetLostPercent);
            flowRtp.setPacketToBeLost();
        }

        if (bitRate != null)//compute deltaTime in function of bitrate, it is also mandatory to set packetNumber in this case
        {
            long size = flowRtp.getDataRTPLength() * flowRtp.getPacketNumber() * 8;
            float timeToSend = size / (flowRtp.getBitRate() * 1024);

            int newDeltaTimestamp = (int) Math.round(timeToSend * 1000 / flowRtp.getPacketNumber());
            if (newDeltaTimestamp == 0) {
                newDeltaTimestamp = 1;
            }
            flowRtp.setDeltaTime(newDeltaTimestamp);
        }

        if (synchronous != null) {
            flowRtp.setSynchronous(Boolean.parseBoolean(synchronous));
        }
        return flowRtp;
    }

    public ArrayList parsePacketAttribute(Element packet, Runner runner, String attName) throws Exception {
        Element header = packet.element("header");
        ArrayList listAttributeData = new ArrayList();
        String attribute = header.attributeValue(attName);

        if (attribute != null) {
            if(Parameter.matchesParameter(attribute)){
                // optimisation, use cache
                Parameter parameter = runner.getParameterPool().get(attribute);
                if (attName.equals("seqnum") || attName.equals("mark")) {
                    listAttributeData = (ArrayList) ParameterCache.getAsIntegerList(parameter);
                }
                else if (attName.equals("timestamp")) {
                    listAttributeData = (ArrayList) ParameterCache.getAsLongList(parameter);
                }
            }
            else{
                LinkedList<String> listAttribute = runner.getParameterPool().parse(attribute);
                if (attName.equals("seqnum") || attName.equals("mark")) {
                    for (Iterator<String> it = listAttribute.iterator(); it.hasNext();) {
                        listAttributeData.add(Integer.parseInt(it.next()));
                    }
                }
                else if (attName.equals("timestamp")) {
                    for (Iterator<String> it = listAttribute.iterator(); it.hasNext();) {
                        listAttributeData.add(Long.parseLong(it.next()));
                    }
                }
            }
        }
        return listAttributeData;
    }

    @Override
    public ArrayList<Array> parsePacketPayload(Element packet, Runner runner) throws Exception {
        List<Element> payloads = packet.elements("payload");
        ArrayList<Array> listPayloadData = new ArrayList<Array>();
        LinkedList<String> listPayload;
        String format = null;
        String text = null;

        for (Element element : payloads) {
            format = element.attributeValue("format");
            text = element.getTextTrim();

            if(Parameter.matchesParameter(text) && payloads.size() == 1){
                // optimisation, use cache
                Parameter parameter = runner.getParameterPool().get(text);
                if (format.equalsIgnoreCase("text")) {
                    listPayloadData = (ArrayList<Array>) ParameterCache.getAsAsciiArrayList(parameter);
                }
                else if (format.equalsIgnoreCase("binary")) {
                    listPayloadData = (ArrayList<Array>)  ParameterCache.getAsHexArrayList(parameter);
                }
                else {
                    throw new Exception("format of payload <" + format + "> is unknown");
                }
            }
            else{
                listPayload = runner.getParameterPool().parse(text);
                if (format.equalsIgnoreCase("text")) {
                    for (Iterator<String> it = listPayload.iterator(); it.hasNext();) {
                        listPayloadData.add(new DefaultArray(it.next().getBytes()));
                    }
                }
                else if (format.equalsIgnoreCase("binary")) {
                    for (Iterator<String> it = listPayload.iterator(); it.hasNext();) {
                        listPayloadData.add(Array.fromHexString(it.next()));
                    }
                }
                else {
                    throw new Exception("format of payload <" + format + "> is unknown");
                }
            }
        }
        return listPayloadData;
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
    
    @Override
    public XMLElementReplacer getElementReplacer() {
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

    public Config getConfig() throws Exception {
        return Config.getConfigByName("rtpflow.properties");
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
