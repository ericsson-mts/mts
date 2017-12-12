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
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.RetransmissionId;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.TransactionId;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.rtp.MsgRtp;
import com.devoteam.srit.xmlloader.rtp.StackRtp;
import com.devoteam.srit.xmlloader.rtp.srtp.RawPacket;
import com.devoteam.srit.xmlloader.rtp.srtp.SRTPTransformer;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.ReadOnlyDefaultArray;
import gp.utils.arrays.SupArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

import org.dom4j.Element;

public class MsgRtpFlow extends Msg {
	
    final public Semaphore firstMsgSent = new Semaphore(0);
    
    // Information for received/send flow
    private float duration = 0;
    private int packetNumber = 0;
    private int packetNumberWithFilter = 0;
    private LinkedList<MsgRtp> packetsList = new LinkedList<MsgRtp>();
    private int ssrc;
    private int payloadType;
    // Information for flow to send
    private int indexPacketToSend = 0;
    private int maxNbPacketInList = 0;
    private float jitterDelay = 0;
    private float bitRate = 0;
    private int deltaTimestamp = 0;
    private float percentagePacketLost;
    private HashSet<Integer> listPacketLost;
    private int nbPacketSend = 0;   //just useful for packetLost list
    private boolean synchronous = true;
    private final Semaphore synchronousSemaphore = new Semaphore(0);
    // Variable concerning flow to send
    private MsgRtp msgRtp; //next message RTP to send
    private List<Long> timestampList; //timestamp list of messages to send
    private List<Integer> seqnumList; //sequence number list of messages to send
    private List<Array> payloadList; //payload list of messages to send
    private List<Integer> deltaTimeList;
    private List<Integer> markList;
    // Qos Information
    private QoSRtpFlow QoSinfo;
    private CodecDictionary dico = null;
    // specific variable for sequence number to have continuous sequence number
    // even if it loop from 65535 to 0
    private int coefMultSeqNum = 0;
    private int mostRecentReceivedSeqNum = 0;//to keep last seq num and know if coef should be increase even with packet late

    /** Creates a new instance */
    public MsgRtpFlow(Stack stack) throws Exception
    {
        super(stack);
        this.dico = ((StackRtpFlow)stack).dico;
        if (this.stack.getConfig().getBoolean("QOS_MEASURMENT", true)) 
        {
            QoSinfo = new QoSRtpFlow(this.dico);
        }
    }

    public QoSRtpFlow getQoSinfo() {
        return QoSinfo;
    }

    public float getBitRate() {
        return bitRate;
    }

    public void setBitRate(float bitRate) {
        this.bitRate = bitRate;
    }

    public int getDeltaTime() {
        if (deltaTimeList != null) 
        {

        	//calculate sum of all array elements
        	int sum = 0;
        	for (int i = 0; i < deltaTimeList.size() ; i++)
        	{
        	   sum = sum + deltaTimeList.get(i);
        	}
        	//calculate average value
        	return sum / deltaTimeList.size();
        }
        return 0;
    }

    /*this function is only called for a unique value of deltaTime*/
    public void setDeltaTime(int deltaTime) {
        deltaTimeList = new ArrayList<Integer>();
        deltaTimeList.add(deltaTime);
    }

    public void setDeltaTimeList(List deltaTimeList) {
        this.deltaTimeList = deltaTimeList;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public int getPacketNumber() {
        return packetNumber;
    }

    public void setPacketNumber(int packetNumber) {
        this.packetNumber = packetNumber;
    }

    public int getPacketNumberWithFilter() {
        return packetNumberWithFilter;
    }

    public void setPacketNumberWithFilter(int value) {
        packetNumberWithFilter = value;
    }

    public int getDeltaTimestamp() {
        return deltaTimestamp;
    }

    public void setDeltaTimestamp(int deltaTimestamp) {
        this.deltaTimestamp = deltaTimestamp;
    }

    public void addReceivedPacket(MsgRtp aPacket) throws Exception {
        /*set msgrtp for the first packet, so it will be displayed in toString
        and used by getParameter*/
        if (packetsList.isEmpty()) {
            msgRtp = aPacket;
            payloadType = aPacket.getPayloadType();
            ssrc = aPacket.getSsrc();
            mostRecentReceivedSeqNum = aPacket.getSequenceNumber();
        }

        // test useful to use old coef if packet arrive in late just after changing coef
        // this only applies for the last 100 packet after changing coef
        if (mostRecentReceivedSeqNum - aPacket.getSequenceNumber() + (MsgRtp.SEQ_NUM_MAX - 1) < 100)//this case is just for late packet
        {
            //apply old coef on seqNum
            aPacket.setSequenceNumber(aPacket.getSequenceNumber() + (coefMultSeqNum - 1) * MsgRtp.SEQ_NUM_MAX);
        }
        else {
            //increase coef if difference of seqnum between last mostrecent (65534 for example) and current(1 for example)
            //is greater than (65536 - 100), so this take care of case when there is late up to 100 packets
            if ((mostRecentReceivedSeqNum - aPacket.getSequenceNumber()) > (MsgRtp.SEQ_NUM_MAX - 100)) {
                coefMultSeqNum++;
            }

            mostRecentReceivedSeqNum = aPacket.getSequenceNumber();// get it before modifying the sequence number

            //apply coef on seqNum
            aPacket.setSequenceNumber(aPacket.getSequenceNumber() + coefMultSeqNum * MsgRtp.SEQ_NUM_MAX);
        }

        if (!((ListenpointRtpFlow) getListenpoint()).ignoreReceivedMessages) {
            packetsList.add(aPacket);
        }

        packetNumber++;

        if (((ListenpointRtpFlow) getListenpoint()).qosMeasurment) {
            QoSinfo.checkPacket(aPacket);
        }
    }

    public MsgRtp getPacket() {
        return msgRtp;
    }

    /** Get the transaction Identifier of this message
     * Transaction has no sense in RTP because there are no response (stream protocol) */
    @Override
    public TransactionId getTransactionId() {
        return null;
    }

    @Override
    public RetransmissionId getRetransmissionId() {
        return null;
    }

    /** 
     * Return true if the message is a request else return false
     */
    @Override
    public boolean isRequest() 
    {
        return true;
    }

    /** 
     * Get the type of the message
     * Used for message filtering with "type" attribute and for statistic counters 
     */
	@Override
    public String getType() 
    {
        return Integer.toString(payloadType);
    }

    @Override
    public String getTypeComplete() 
    {
        return CodecDictionary.instance().getCodec(payloadType) + ":" + Integer.toString(payloadType);    	
    }
    
    /** 
     * Get the result of the message (null if request)
     * Used for message filtering with "result" attribute and for statistic counters 
     */
	@Override
    public String getResult() 
    {
        return null;
    }

    public int getDataRTPLength() {
        return msgRtp.getData().length;
    }

    /** Return the transport of the message */
    @Override
    public String getTransport() {
        return StackFactory.PROTOCOL_UDP;
    }

    public void prepareNextMessage() throws Exception {
        indexPacketToSend++;
        nbPacketSend++;

        //indexPacketToSend++;
        int indexNextPacketToSend = indexPacketToSend % maxNbPacketInList;

        if (markList.size() > 1) {
            msgRtp.setMarker(markList.get(indexNextPacketToSend));
        }
        else if (nbPacketSend == 1)//if packet==1 change because the others mark can be 0
        {
            msgRtp.setMarker(markList.get(0));
        }

        //increment sequence_number
        if (seqnumList.size() > 1) {
            msgRtp.setSequenceNumber(seqnumList.get(indexNextPacketToSend) + (coefMultSeqNum * MsgRtp.SEQ_NUM_MAX));
        }
        else {
            msgRtp.setSequenceNumber(msgRtp.getInternalSequenceNumber() + 1);
        }

        //increment timestamp
        if (timestampList.size() > 1) {
            msgRtp.setTimestampRTP(timestampList.get(indexNextPacketToSend));
        }
        else {
            msgRtp.setTimestampRTP(msgRtp.getTimestampRTP() + getDeltaTimestamp());
        }

        msgRtp.setData(payloadList.get(indexNextPacketToSend));

        msgRtp.modifyMsgForFlow();
    }

    public void setJitterDelay(float parseInt) {
        jitterDelay = parseInt;
    }

    public float getJitterDelay() {
        return jitterDelay;
    }

    public void getPacketListTimestamp(Parameter var) {
        for (Iterator<MsgRtp> it = packetsList.iterator(); it.hasNext();) {
            var.add(it.next().getTimestamp());
        }
    }

    public boolean isPacketToBeSend() {
        if (listPacketLost != null) {
            return !listPacketLost.contains(nbPacketSend);
        }
        else {
            return true;
        }
    }

    public void setPacketToBeLost() {
        int nbPacketLost = (int) (packetNumber * percentagePacketLost / 100);
        int i = 0;
        Random rand = new Random();
        listPacketLost = new HashSet<Integer>();

        while (listPacketLost.size() < nbPacketLost) {
            i = rand.nextInt(packetNumber);
            listPacketLost.add(i);// add i if it is not already present into the list
        }
    }

    public int calculJitter() {
        int jitter = 0;
        if (jitterDelay != 0) {
            Random rand = new Random();
            jitter = rand.nextInt((int) (2 * jitterDelay));
            boolean sign = rand.nextBoolean();
            if (!sign)//true for +; false for -
            {
                jitter = -jitter;
            }
        }

        return jitter;
    }

    public void setPacketLost(float packetLostPercent) {
        percentagePacketLost = packetLostPercent;
    }

    public boolean isSynchronous() {
        return synchronous;
    }

    public void setSynchronous(boolean synchronous) {
        this.synchronous = synchronous;
    }

    public Semaphore getSynchronousSemaphore() {
        return synchronousSemaphore;
    }

    public void getPayload(Parameter var, String type) throws Exception {
        List<MsgRtp> reorderedList = (List) packetsList.clone();
        Collections.sort(reorderedList);

        for (Iterator<MsgRtp> it = reorderedList.iterator(); it.hasNext();) {
            if (type.equals("binary")) {
                var.add(Array.toHexString(it.next().getData()));
            }
            else if (type.equals("text")) {
                var.add(new String(it.next().getData().getBytes()));
            }
            else {
                throw new Exception("Format not supported, use text or binary");
            }
        }
    }

    public void getPayloadPlayer(Parameter var) throws Exception {
        List<MsgRtp> reorderedList = (List) packetsList.clone();
        Collections.sort(reorderedList);

        Array emptyPacket = getEmptyPacket(getPacket().getData().length);
        if (reorderedList.size() > 0)
        {
	        int i = 0;
	        int diff = 0;
	        var.add(Array.toHexString(reorderedList.get(i).getData()));
	        while (i < reorderedList.size() - 1) {
	            diff = reorderedList.get(i + 1).getInternalSequenceNumber() - reorderedList.get(i).getInternalSequenceNumber();
	            if (diff > 1) {/*some packet are missing and detected with sequence number*/
	                for (int k = 1; k < diff; k++) {
	                    var.add(Array.toHexString(emptyPacket));
	                }
	            }
	            var.add(Array.toHexString(reorderedList.get(i + 1).getData()));
	            i++;
	        }
        }
    }

    public static SupArray getEmptyPacket(int length) throws Exception {
        String defaultPattern = "00000000000000000000";
        String pattern = Config.getConfigByName("rtp.properties").getString("qos.PATTERN");
        if (pattern.length() == 0) {
            pattern = Config.getConfigByName("rtpflow.properties").getString("silent.PATTERN_HEXA", defaultPattern);
        }
        if (pattern.length() == 0) {
            pattern = defaultPattern;
        }

        SupArray emptyPacket = new SupArray();
        //recovery of the pattern
        Array motif = Array.fromHexString(pattern);
        int motifLength = pattern.length() / 2;
        int nbMotif = length / motifLength;
        int lengthOfLast = length - nbMotif * motifLength;
        for (int i = 0; i < nbMotif; i++) {
            emptyPacket.addLast(motif);
        }
        emptyPacket.addLast(motif.subArray(0, lengthOfLast));
        return emptyPacket;
    }

    public LinkedList<MsgRtp> getPacketList() {
        return this.packetsList;
    }

    public Integer getPayloadType() {
        return this.payloadType;
    }

    public int getSsrc() {
        return this.ssrc;
    }
    // temp modif gpasquiers; may become final
    private long lastPacketTimestamp = 0;
    private long lastNonSilencePacketTimestamp = 0;
    private long creationTimestamp = System.currentTimeMillis();

    public void updateLastPacketTimestamp() {
        lastPacketTimestamp = System.currentTimeMillis();
    }

    public long getLastPacketTimestamp() {
        return lastPacketTimestamp;
    }

    public void updateLastNonSilencePacketTimestamp() {
        lastNonSilencePacketTimestamp = System.currentTimeMillis();
    }

    public long getLastNonSilencePacketTimestamp() {
        return lastNonSilencePacketTimestamp;
    }

    public long getCreationTimestamp() {
        return creationTimestamp;
    }
 
    
    //-------------------------------------------------
    // methods for the encoding / decoding of the message
    //-------------------------------------------------

    /** 
     * encode the message to binary data 
     */
    @Override
    public byte[] encode() throws Exception 
    {
        return msgRtp.encode();
    }
    
    /** 
     * decode the message from binary data 
     */
    @Override
    public void decode(byte[] data) throws Exception
    {
    	// noting to do : never called
    } 
    
    
    //---------------------------------------------------------------------
    // methods for the XML display / parsing of the message
    //---------------------------------------------------------------------

    /** Returns a short description of the message. Used for logging as INFO level */
    /** This methods HAS TO be quick to execute for performance reason */
    @Override
    public String toShortString() throws Exception {
    	String ret = super.toShortString();
        ret += "\n";
        ret += "<flow ";
        ret += "packetNumber=\"" + getPacketNumber() + "\", ";
        ret += "duration=\"" + this.duration + "\", ";
        ret += "bitRate=\"" + getBitRate() + "\"";
        ret += "/> ";
        return ret;
    }

    /** 
     * Convert the message to XML document 
     */
    @Override
    public String toXml() throws Exception {
    	
        String xml = "<flow ";
        xml += "packetNumber=\"" + getPacketNumber() + "\", ";
        xml += "duration=\"" + this.duration + "\", ";
        xml += "bitRate=\"" + getBitRate() + "\", ";
        xml += "deltaTime=\"" + getDeltaTime() + "\", ";/*not needed in receipt mode*/
        xml += "deltaTimestamp=\"" + getDeltaTimestamp() + "\"/>\n";

        boolean qosEnable = ((StackRtpFlow) this.stack).qosMeasurment;
        if (qosEnable) {
            xml += "<qos ";
            xml += "jitterDelay=\"" + QoSinfo.getMeanJitter() + "\", ";
            xml += "packetLost=\"" + QoSinfo.getPacketLost() + "\", ";
            // xml += "mosMean=\"" + QoSinfo.getEModele().getMosMean() + "\"";
            xml += "mos=\"" + QoSinfo.getEModele().getMos() + "\"/>\n";
        }
        
        if (msgRtp != null) {
        	xml += msgRtp.toStringRTPFlow();
        }
        return xml;
    }
    
    /** 
     * Parse the message from XML element 
     */
    @Override
    public void parseFromXml(ParseFromXmlContext context, Element root, Runner runner) throws Exception
    {
    	super.parseFromXml(context,root,runner);

    	if (root.element("flow") != null) {
            parseFlow(root.element("flow"), runner);
        }
        else {
            throw new Exception("No flow tag in sendMessageRTPFLOW present");
        }
    }

    public void parseFlow(Element flow, Runner runner) throws Exception 
    {	
        StackRtp stackRtp = (StackRtp) StackFactory.getStack(StackFactory.PROTOCOL_RTP);
    	MsgRtp msg = new MsgRtp(stackRtp);
    	msg.parsePacketHeader(flow, runner);
    	this.msgRtp = msg;
    	
    	this.payloadList = parsePacketPayload(flow, runner);
    	this.packetNumber = payloadList.size();
    	//prepare first packet now =>
    	this.msgRtp.setData(payloadList.get(0));
    	    	
    	this.seqnumList = parsePacketAttribute(flow, runner, "seqnum");
        this.msgRtp.setSequenceNumber(this.seqnumList.get(0));
        
    	this.timestampList = parsePacketAttribute(flow, runner, "timestamp");
        this.msgRtp.setTimestampRTP(this.timestampList.get(0));

        this.markList = parsePacketAttribute(flow, runner, "mark");
        if (this.markList.size() == 0) 
        {
            this.msgRtp.setMarker(1);
        }
        else 
        {
            this.msgRtp.setMarker(this.markList.get(0));
        }
        if (this.markList.size() == 0)
        {
            this.markList = new ArrayList<Integer>();
            this.markList.add(0);
        }
        
        //construct it the first time
        this.msgRtp.encode();
        
        this.payloadType = this.msgRtp.getPayloadType();
        this.ssrc = this.msgRtp.getSsrc();

        this.packetsList = new LinkedList<MsgRtp>();
        if (this.payloadList.size() > 1) {
            this.maxNbPacketInList = this.payloadList.size();
        }
        else if (this.seqnumList.size() > 1) {
        	this.maxNbPacketInList = this.seqnumList.size();
        }
        else if (this.timestampList.size() > 1) {
        	this.maxNbPacketInList = this.timestampList.size();
        }
        else if (this.markList.size() > 1) {
        	this.maxNbPacketInList = this.markList.size();
        }
        else {
        	this.maxNbPacketInList = 1;
        }

        //WARNING: part above must be done before setting flow parameter

        String bitRate = flow.attributeValue("bitRate");
        String deltaTime = flow.attributeValue("deltaTime");
        String deltaTimestamp = flow.attributeValue("deltaTimestamp");
        if ((bitRate != null) && (deltaTime != null)) {
            throw new Exception("attribute <bitRate> and <deltaTime> of flow tag cannot be set in the same time");
        }

        if ((bitRate == null) && (deltaTime == null)) {
            throw new Exception("one of the attribute <bitRate> or <deltaTime> of flow tag must be set");
        }

        String duration = flow.attributeValue("duration");
        String packetNumber = flow.attributeValue("packetNumber");
        if ((packetNumber == null) && (duration == null)) {
            throw new Exception("one of the attribute <packetNumber> or <duration> of flow tag must be set");
        }

        if (packetNumber != null) {
            this.packetNumber = Integer.parseInt(packetNumber);
        }
        if (deltaTime != null) {
            LinkedList<String> listAttribute = runner.getParameterPool().parse(deltaTime);
            if (listAttribute.size() > 0)//if more than one element
            {
                ArrayList listAttributeData = new ArrayList<Integer>();

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
                this.deltaTimeList = listAttributeData;
            }
        }

        if (duration != null) {
            this.duration = Float.parseFloat(duration);
        }
        
        if (bitRate != null) {
            this.bitRate = Float.parseFloat(bitRate);
        }
        if (deltaTimestamp != null) {
            this.deltaTimestamp = Integer.parseInt(deltaTimestamp);
        }
        else {
            this.deltaTimestamp = (int) this.getDataRTPLength();
        }
        String jitterDelay = flow.attributeValue("jitterDelay");
        if (jitterDelay != null) {
            this.jitterDelay = Float.parseFloat(jitterDelay);
        }

        //now compute missing values with values which are given in operation
        if (packetNumber == null && bitRate != null)//compute packetNumber in function of the bitrate
        {
            this.packetNumber = (int) (this.bitRate * 1024 * this.duration / (this.getDataRTPLength() * 8));
        }
        else if (packetNumber == null && bitRate == null)//compute packetNumber in function of deltaTimestamp
        {
            int newPacketNumber = (int) (this.duration * 1000 / this.getDeltaTime());
            this.packetNumber = newPacketNumber;
        }

        if (duration == null) 
        {
        	float dur = (float) this.packetNumber * this.getDeltaTime() / 1000;
            this.duration = dur;
        }

        String packetLost = flow.attributeValue("packetLost");
        if (packetLost != null) {
            float packetLostPercent = Float.parseFloat(packetLost);
            if ((packetLostPercent < 0) || (packetLostPercent > 100)) {
                throw new Exception("packetLost attribute is a percentage (between 0 and 100)");
            }

            this.percentagePacketLost = packetLostPercent;
            this.setPacketToBeLost();
        }

        if (bitRate != null)//compute deltaTime in function of bitrate, it is also mandatory to set packetNumber in this case
        {
            long size = this.getDataRTPLength() * this.packetNumber * 8;
            float timeToSend = size / (this.bitRate * 1024);

            int newDeltaTimestamp = (int) Math.round(timeToSend * 1000 / this.packetNumber);
            if (newDeltaTimestamp == 0) {
                newDeltaTimestamp = 1;
            }
            this.setDeltaTime(newDeltaTimestamp);
        }

        String synchronous = flow.attributeValue("synchronous");
        if (synchronous != null) {
            this.synchronous = Utils.parseBoolean(synchronous, "synchronous");
        }
   }

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

    //------------------------------------------------------
    // method for the "setFromMessage" <parameter> operation
    //------------------------------------------------------

    /** 
     * Get a parameter from the message
     */
    @Override
    public Parameter getParameter(String path) throws Exception {
        Parameter var = super.getParameter(path);
        if (null != var) {
            return var;
        }

        var = new Parameter();
        path = path.trim();
        String[] params = Utils.splitPath(path);

        if (params.length >= 1 && params[0].equalsIgnoreCase("header")) {
            if (params[1].equalsIgnoreCase("ssrc")) {
                for (Iterator<MsgRtp> it = packetsList.iterator(); it.hasNext();) {
                    var.add(it.next().getSsrc());
                }
            }
            else if (params[1].equalsIgnoreCase("payloadType")) {
                for (Iterator<MsgRtp> it = packetsList.iterator(); it.hasNext();) {
                    var.add(it.next().getPayloadType().toString());
                }
            }
            else if (params[1].equalsIgnoreCase("seqnum")) {
                for (Iterator<MsgRtp> it = packetsList.iterator(); it.hasNext();) {
                    var.add(it.next().getSequenceNumber());
                }
            }
            else if (params[1].equalsIgnoreCase("timestamp")) {
                for (Iterator<MsgRtp> it = packetsList.iterator(); it.hasNext();) {
                    var.add(it.next().getTimestampRTP());
                }
            }
            else if (params[1].equalsIgnoreCase("mark")) {
                for (Iterator<MsgRtp> it = packetsList.iterator(); it.hasNext();) {
                    var.add(it.next().getMarker());
                }
            }
        }
        else if (params.length >= 1 && params[0].equalsIgnoreCase("payload"))//deprecated
        {
            GlobalLogger.instance().getSessionLogger().warn(Topic.PARAM, "path \"payload.xxx\" is deprecated in operation setFromMessage for RTPFLOW protocol, use \"flow.payload.xxx\" instead!");
            for (Iterator<MsgRtp> it = packetsList.iterator(); it.hasNext();) {
                Parameter param = it.next().getParameter(path);
                var.add(param.get(0));
            }
        }
        else if (params.length >= 1 && params[0].equalsIgnoreCase("qos")) {
            if (!this.stack.getConfig().getBoolean("QOS_MEASURMENT", true)) {
                Parameter.throwBadPathKeywordException(path + ": This cannot be used as QOS mesure calculation has been disable in configuration.");
            }
            else if (params[1].equalsIgnoreCase("bitRate")) {
                var.add(QoSinfo.getMeanBitRate());
            }
            else if (params[1].equalsIgnoreCase("packetLost")) {
                var.add(QoSinfo.getPacketLost());
            }
            else if (params[1].equalsIgnoreCase("packetDuplicated")) {
                var.add(QoSinfo.getDuplicated());
            }
            else if (params[1].equalsIgnoreCase("packetMissSequence")) {
                var.add(QoSinfo.getPacketMissSequence());
            }
            else if (params[1].equalsIgnoreCase("deltaTime")) {
                var.addAll(QoSinfo.getDelta());
            }
            else if (params[1].equalsIgnoreCase("deltaTimeMean")) {
                var.add(QoSinfo.getMeanDelta());
            }
            else if (params[1].equalsIgnoreCase("packetSpacing")) {
                var.addAll(QoSinfo.getPacketSpacing());
            }
            else if (params[1].equalsIgnoreCase("packetSpacingMean")) {
                var.add(QoSinfo.getMeanPacketSpacing());
            }
            else if (params[1].equalsIgnoreCase("jitterDelay")) {
                var.addAll(QoSinfo.getJitter());
            }
            else if (params[1].equalsIgnoreCase("jitterDelayMean")) {
                var.add(QoSinfo.getMeanJitter());
            }
            /**
             * TODO : This code has to be changed using a sliding window of 1 second for exemple
             * DO NOT ERASE IT
             */
            /*
            else if(params[1].equalsIgnoreCase("mosMean"))
            {
            var.add(QoSinfo.getEModele().getMosMean());
            }
             */
            else if (params[1].equalsIgnoreCase("mos")) {
                /**
                 * TODO : This code has to be changed using a sliding window of 1 second for exemple
                 * DO NOT ERASE IT
                 */
                /*
                var.addAll(QoSinfo.getEModele().getMosRT());
                 */
                var.add(QoSinfo.getEModele().getMos());
            }
            else {
                Parameter.throwBadPathKeywordException(path);
            }
        }
        else if (params.length >= 1 && params[0].equalsIgnoreCase("flow")) {
            if (params.length >= 2 && params[1].equalsIgnoreCase("packetNumber")) {
                var.add(getPacketNumber());
            }
            else if (params.length >= 2 && params[1].equalsIgnoreCase("packetTimestamp")) {
                getPacketListTimestamp(var);
            }
            else if (params.length >= 2 && params[1].equalsIgnoreCase("payload")) {
                if (params.length >= 3 && params[2].equalsIgnoreCase("binary")) {
                    getPayload(var, "binary");
                }
                else if (params.length >= 3 && params[2].equalsIgnoreCase("text")) {
                    getPayload(var, "text");
                }
                else if (params.length >= 3 && params[2].equalsIgnoreCase("player")) {
                    getPayloadPlayer(var);
                }
                else {
                    Parameter.throwBadPathKeywordException(path);
                }
            }
            else {
                Parameter.throwBadPathKeywordException(path);
            }
        }
        else {
            Parameter.throwBadPathKeywordException(path);
        }

        return var;
    }

    
    public void uncipherPayloadList(SRTPTransformer transformer, int authTagLength) throws Exception
    {
    	Collections.sort(packetsList);
    	for (int i = 0; i < packetsList.size(); i++)
    	{
    		byte[] data = packetsList.get(i).encode();
    		
    		RawPacket rp = new RawPacket(data, 0, data.length);
    		rp = transformer.reverseTransform(rp);
    		
    		byte[] uncipheredData = new byte[data.length - authTagLength];
        	System.arraycopy(rp.getBuffer(), 0, uncipheredData, 0, data.length - authTagLength);
        	
        	MsgRtp msg = new MsgRtp(stack);
        	msg.decode(uncipheredData);
        	packetsList.set(i, msg);        	
    		data = null;
    	}
    }
}