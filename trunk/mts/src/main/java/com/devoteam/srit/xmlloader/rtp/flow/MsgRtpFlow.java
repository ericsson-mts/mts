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
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.RetransmissionId;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.TransactionId;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.rtp.MsgRtp;
import com.devoteam.srit.xmlloader.srtp.RawPacket;
import com.devoteam.srit.xmlloader.srtp.SRTPTransformer;

import gp.utils.arrays.Array;
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

public class MsgRtpFlow extends Msg {
	
    final public Semaphore firstMsgSent = new Semaphore(0);
    
    // Information for received/send flow
    private float duration = 0;
    private int packetNumber = 0;
    private int packetNumberWithFilter = 0;
    private LinkedList<MsgRtp> packetsList;
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


    /*Constructor used for received flow*/
    public MsgRtpFlow(CodecDictionary dico) throws Exception {
        super();
        this.dico = dico;
        packetsList = new LinkedList<MsgRtp>();
        if (StackFactory.getStack(StackFactory.PROTOCOL_RTPFLOW).getConfig().getBoolean("QOS_MEASURMENT", true)) {
            QoSinfo = new QoSRtpFlow(this.dico);
        }
    }

    /*Constructor used for flow to send*/
    public MsgRtpFlow(CodecDictionary dico, List listPayload, List listSeqnum, List listTimestamp, List listMark, MsgRtp msg) throws Exception {
        this(dico);
        payloadList = listPayload;
        seqnumList = listSeqnum;
        timestampList = listTimestamp;

        if (listMark.size() == 0) {
            markList = new ArrayList<Integer>();
            markList.add(0);
        }
        else {
            markList = listMark;
        }

        msgRtp = msg;
        packetNumber = payloadList.size();
        if (StackFactory.getStack(StackFactory.PROTOCOL_RTPFLOW).getConfig().getBoolean("QOS_MEASURMENT", true)) {
            QoSinfo = new QoSRtpFlow(this.dico);
        }

        //prepare first packet now =>
        msgRtp.setData(payloadList.get(0));

        //in case no mark is specified, first packet is send with mark to 1 and all others with mark to 0
        if (listMark.size() == 0) {
            msgRtp.setMarker(1);
        }
        else {
            msgRtp.setMarker(markList.get(0));
        }

        msgRtp.setSequenceNumber(seqnumList.get(0));
        msgRtp.setTimestampRTP(timestampList.get(0));
        //construct it the first time
        msgRtp.getBytesData();

        if (payloadList.size() > 1) {
            maxNbPacketInList = payloadList.size();
        }
        else if (seqnumList.size() > 1) {
            maxNbPacketInList = seqnumList.size();
        }
        else if (timestampList.size() > 1) {
            maxNbPacketInList = timestampList.size();
        }
        else if (markList.size() > 1) {
            maxNbPacketInList = timestampList.size();
        }
        else {
            maxNbPacketInList = 1;
        }

        payloadType = msgRtp.getPayloadType();
        ssrc = msgRtp.getSsrc();
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
        if (deltaTimeList != null) {
            if (deltaTimeList.size() > 1) {
                return deltaTimeList.get(indexPacketToSend % maxNbPacketInList);
            }
            else {
                return deltaTimeList.get(0);
            }
        }
        else {
            return 0;
        }
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

    /*
     * Get parameters
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
            if (!StackFactory.getStack(StackFactory.PROTOCOL_RTPFLOW).getConfig().getBoolean("QOS_MEASURMENT", true)) {
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

    /** Get the protocol of this message */
    @Override
    public String getProtocol() {
        return StackFactory.PROTOCOL_RTPFLOW;
    }

    /** Return true if the message is a request else return false */
    @Override
    public boolean isRequest() {
        return true;
    }

    /** Get the command code of this message */
    //TODO: manage type for stat(packet, flow, rtcp) + getTypeComplete
    @Override
    public String getType() {
        return Integer.toString(payloadType);
    }

    @Override
    public String getTypeComplete() {
        return CodecDictionary.instance().getCodec(payloadType) + ":" + Integer.toString(payloadType);    	
    }
    /*
     * Get the result response
     */

    @Override
    public String getResult() {
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
 
    /** Get the data (as binary) of this message */
    @Override
    public byte[] getBytesData() {
        return msgRtp.getBytesData();
    }

    /** Returns a short description of the message. Used for logging as INFO level */
    /** This methods HAS TO be quick to execute for performance reason */
    @Override
    public String toShortString() throws Exception {
    	String ret = super.toShortString();
        ret += "<flow ";
        ret += "packetNumber=\"" + getPacketNumber() + "\", ";
        ret += "duration=\"" + getDuration() + "\", ";
        ret += "bitRate=\"" + getBitRate() + "\"/> ";
        return ret;
    }

    /** Get the XML representation of the message; for the genscript module. */
    @Override
    public String toXml() throws Exception {
    	
        String xml = "<flow ";
        xml += "packetNumber=\"" + getPacketNumber() + "\", ";
        xml += "duration=\"" + getDuration() + "\", ";
        xml += "bitRate=\"" + getBitRate() + "\", ";
        xml += "deltaTime=\"" + getDeltaTime() + "\", ";/*not needed in receipt mode*/
        xml += "deltaTimestamp=\"" + getDeltaTimestamp() + "\"/>\n";

        boolean qosEnable = ((StackRtpFlow) StackFactory.getStack(StackFactory.PROTOCOL_RTPFLOW)).qosMeasurment;
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
    
    public void uncipherPayloadList(SRTPTransformer transformer, int authTagLength) throws Exception
    {
    	Collections.sort(packetsList);
    	for (int i = 0; i < packetsList.size(); i++)
    	{
    		byte[] data = packetsList.get(i).getBytesData();
    		
    		RawPacket rp = new RawPacket(data, 0, data.length);
    		rp = transformer.reverseTransform(rp);
    		
    		byte[] uncipheredData = new byte[data.length - authTagLength];
        	System.arraycopy(rp.getBuffer(), 0, uncipheredData, 0, data.length - authTagLength);
        	
        	Array uncipheredArray = new ReadOnlyDefaultArray(uncipheredData);
        	
        	MsgRtp msg = new MsgRtp(uncipheredArray);
        	packetsList.set(i, msg);        	
    		data = null;
    	}
    }
}