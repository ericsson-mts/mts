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

package com.devoteam.srit.xmlloader.rtp;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.TransactionId;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.rtp.flow.CodecDictionary;

import gp.utils.arrays.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.dom4j.Element;

public class MsgRtp extends Msg implements Comparable<MsgRtp> {

    final static public int SEQ_NUM_MAX = 65536;// max value of sequence number in RTP message
    //message header parameter
    final static private int version = 2;//2 bits
    private int padding = 0; // 1 bit
    private int extension = 0; // 1 bit
    private int csrcCount = 0; // 4 bits
    private int marker = 0; // 1 bit
    private int payloadType = 0; // 7 bits
    private int sequenceNumber = 0; // 16 bits
    private long timestamp = 0; // 32 bits
    private int ssrc = 0; // 32 bits
    private Vector<Array> csrc = null; // array of size going from 0 to 15 with 32 bits value
    private int extensionProfile = 0; // 16 bits optional (present if extension = 1)
    private int extensionLength = 0; // 16 bits optional (present if extension = 1)
    private Vector<Array> extensionData = null; // extensionLength * 32 bits optional (present if extension = 1)
    private Array data = new DefaultArray(0);
    private SupArray msgArray = new SupArray();
    private SupArray headerArray = null;
    Integer32Array timestampArray = null;
    Integer16Array seqnumArray = null;
    Array markArray = null;
    private boolean _isSilence = false;
    
    private byte[] cipheredMessage = null;

    /** Creates a new instance */
    public MsgRtp(Stack stack) throws Exception 
    {
        super(stack);
    }

    public int getCsrcCount() {
        return csrcCount;
    }

    public void setCsrcCount(int CsrcCount) {
        this.csrcCount = CsrcCount;
    }

    public Vector<Array> getCsrc() {
        return csrc;
    }

    public void setCsrc(Vector<Array> csrc) {
        this.csrc = csrc;
    }

    public int getExtension() {
        return extension;
    }

    public void setExtension(int extension) {
        this.extension = extension;
    }

    public int getMarker() {
        return marker;
    }

    public void setMarker(int marker) {
        this.marker = marker;
    }

    public int getPadding() {
        return padding;
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

    public Integer getPayloadType() {
        return payloadType;
    }

    public void setPayloadType(int payloadType) {
        this.payloadType = payloadType;
    }

    // sequence number like in RTP message
    public int getSequenceNumber() {
        return sequenceNumber % SEQ_NUM_MAX;
    }

    // sequence number for internal use, so it can be greater then 65535 to have
    // continuity in sequence number
    public int getInternalSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public int getSsrc() {
        return ssrc;
    }

    public void setSsrc(int ssrc) {
        this.ssrc = ssrc;
    }

    public long getTimestampRTP() {
        return timestamp;
    }

    public void setTimestampRTP(long timestamp) {
        this.timestamp = timestamp;
    }

    public Array getData() {
        return data;
    }

    public void setData(Array data) {
        this.data = data;
    }

    public Vector<Array> getExtensionData() {
        return extensionData;
    }

    public void setExtensionData(Vector<Array> extensionData) {
        this.extensionData = extensionData;
    }

    public int getExtensionLength() {
        return extensionLength;
    }

    public void setExtensionLength(int extensionLength) {
        this.extensionLength = extensionLength;
    }

    public int getExtensionProfile() {
        return extensionProfile;
    }

    public void setExtensionProfile(int extensionProfile) {
        this.extensionProfile = extensionProfile;
    }

    /** Get the transaction Identifier of this message
     * Transaction has no sense in RTP because there are no response (stream protocol) */
    public TransactionId getTransactionId() {
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

    /** Get the command code of this message */
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
   
    @Override
    public int compareTo(MsgRtp msg) {
        // manage comparison with internal sequence number which allow the test
        // of continuous flow even if the sequence number loop from 65535 to 0
        return (int) (this.getInternalSequenceNumber() - msg.getInternalSequenceNumber());
    }

    /*this function is just used by the rtpflow protocol*/
    public void modifyMsgForFlow() {
        seqnumArray.setValue(sequenceNumber);
        timestampArray.setValue((int) timestamp);
        //markArray.setValue(marker);
        markArray.setBit(0, marker);

        //recreate new msg array based on changed header and new data
        msgArray = new SupArray();
        msgArray.addFirst(headerArray);
        msgArray.addLast(data);
    }

    public boolean isSilence() {
        return _isSilence;
    }

    public void setIsSilence(boolean value) {
        _isSilence = value;
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
        //construct message from header and content if not done
        //header
        if (headerArray == null) {
            DefaultArray rtpHeader = new DefaultArray(12);
            rtpHeader.setBits(0, 2, version);
            rtpHeader.setBit(2, padding);
            rtpHeader.setBit(3, extension);
            rtpHeader.setBits(4, 4, csrcCount);
            rtpHeader.setBit(8, marker);
            rtpHeader.setBits(9, 7, payloadType);

            markArray = rtpHeader.subArray(1, 1);

            seqnumArray = new Integer16Array(rtpHeader.subArray(2, 2));
            seqnumArray.setValue((int) sequenceNumber);

            timestampArray = new Integer32Array(rtpHeader.subArray(4, 4));
            timestampArray.setValue((int) timestamp);

            Integer32Array ssrcArray = new Integer32Array(rtpHeader.subArray(8, 4));
            ssrcArray.setValue(ssrc);

            headerArray = new SupArray();
            headerArray.addFirst(rtpHeader);

            if (csrc != null) {
                for (int i = 0; i < csrc.size(); i++) {
                    headerArray.addLast(csrc.elementAt(i));
                }
            }

            //if extension, added here at the end of the header
            if (extension != 0) {
                DefaultArray extensionArray = new DefaultArray(32 + extensionLength * 32);
                extensionArray.setBits(0, 16, extensionProfile);
                extensionArray.setBits(16, 16, extensionLength);
                if (extensionData != null) {
                    for (int i = 0; i < extensionData.size(); i++) {
                        headerArray.addLast(extensionData.elementAt(i));
                    }
                }
            }

            msgArray.addFirst(headerArray);
            //data
            msgArray.addLast(data);
        }
        return msgArray.getBytes();
    }

    /** 
     * decode the message from binary data 
     */
    @Override
    public void decode(byte[] data) throws Exception
    {
    	Array array = new DefaultArray(data);
        padding = array.getBit(2);
        extension = array.getBit(3);
        csrcCount = array.getBits(4, 4);
        marker = array.getBit(8);
        payloadType = array.getBits(9, 7);
        sequenceNumber = array.getBits(16, 16);
        timestamp = array.getBits(32, 32);
        ssrc = array.getBits(64, 32);

        int i = 0;
        if (csrcCount != 0) 
        {
            csrc = new Vector<Array>();
            for (i = 0; i < csrcCount; i++) 
            {
                csrc.add(array.subArray(12 + 4 * i, 4));
            }
        }

        int headerLength = 12 + 4 * i;

        //get extension if present
        if (extension != 0) 
        {
            extensionProfile = array.getBits(headerLength, 16);
            headerLength += 2;
            extensionLength = array.getBits(headerLength, 16);
            headerLength += 2;
            extensionData = new Vector<Array>();
            for (i = 0; i < extensionLength; i++) 
            {
                csrc.add(array.subArray(headerLength + 4 * i, 4));
            }
            headerLength += extensionLength * 4;
        }

        try 
        {
            // test to not always store data of RTP messages
            // actually useless because of gp-utils array
            // if a subArray is used, then the parent array
            // is kept in memory

            //if (((StackRtp) StackFactory.getStack(StackFactory.PROTOCOL_RTP)).ignoreReceivedMessages) 
        	//{
            //    data = new ConstantArray((byte) 0, array.length - headerLength);
            //}
            //else
        	//{
                this.data = array.subArray(headerLength, array.length - headerLength);
            //}
        }
        catch (Exception ex) 
        {
        }
    } 

    
    //---------------------------------------------------------------------
    // methods for the XML display / parsing of the message
    //---------------------------------------------------------------------

    /** Return the string from RTPFlow protocol */
    public String toStringRTPFlow() throws Exception {
        String ret = "";
       	ret += headerToString() + "\n";	
        ret += "<payload format=\"binary\" length=\"" + data.length + "\">\n";
        ret += Utils.toBinaryString(data.getBytes()).trim() + "\n";
        ret += "</payload>\n";
        return ret;
    }

    /** Returns a short description of the message. Used for logging as INFO level */
    /** This methods HAS TO be quick to execute for performance reason */
    @Override
    public String toShortString() throws Exception {
    	String ret = super.toShortString();    	
        ret += "\n";
        ret += headerToString();
        return ret;
    }
    
    private String headerToString() throws Exception {
    	String ret = "<header payloadType=\"" + payloadType + "\" ";
        ret += "ssrc=\"" + ssrc + "\" ";
        ret += "seqnum=\"" + getSequenceNumber() + "\" ";
        ret += "timestamp=\"" + timestamp + "\" ";
        ret += "mark=\"" + marker + "\"/>";
        return ret;
    }


    /** 
     * Convert the message to XML document 
     */
    @Override
    public String toXml() throws Exception 
    {
    	String xml = toStringRTPFlow();
        return xml;
    }
    
    /** 
     * Parse the message from XML element 
     */
    @Override
    public void parseFromXml(Boolean request, Element root, Runner runner) throws Exception
    {
    	super.parseFromXml(request,root,runner);

    	if(root.element("packet") != null)
        {
        	root = root.element("packet");
        	GlobalLogger.instance().logDeprecatedMessage("sendMessageRTP ...><packet>...</packet></sendMessageRTP", "sendMessageRTP ...>...</sendMessageRTP");
        }
        parsePacketHeader(root, runner);
        ArrayList<Array> payload = parsePacketPayload(root, runner); 
        if (payload.size() > 0)
        {
        	this.data = payload.get(0);
        }
    }

    public void parsePacketHeader(Element packet, Runner runner) throws Exception
    {
        Element header = packet.element("header");
        
        String ssrc = header.attributeValue("ssrc");
        if(ssrc != null)
        {
            long ssrcLong = Long.parseLong(ssrc);
            this.ssrc = (int)ssrcLong;
        }                
        String payloadType = header.attributeValue("payloadType");
        if(payloadType != null)
        {
            this.payloadType = Integer.parseInt(payloadType);
        }
        String seqnum = header.attributeValue("seqnum");
        if((seqnum != null) && (Utils.isInteger(seqnum)))
        {
            this.sequenceNumber = Integer.parseInt(seqnum);
        }
        String timestamp = header.attributeValue("timestamp");
        if((timestamp != null) && (Utils.isInteger(timestamp)))
        {
            this.timestamp = Long.parseLong(timestamp);
        }
        String mark = header.attributeValue("mark");
        if((mark != null) && (Utils.isInteger(mark)))
            this.marker = Integer.parseInt(mark);
    }

    public ArrayList<Array> parsePacketPayload(Element packet, Runner runner) throws Exception
    {
        List<Element> payloads = packet.elements("payload");
        SupArray data = new SupArray();
        ArrayList<Array> listPayloadData = new ArrayList<Array>();
        String format = null;
        String text = null;

        for(Element element:payloads)
        {
            format = element.attributeValue("format");
            if (format == null)
            {
            	format = "binary";
            }
            text = element.getTextTrim();

            if(format.equalsIgnoreCase("text"))
            {
                data.addLast(new DefaultArray(text.getBytes()));
            }
            else if(format.equalsIgnoreCase("binary"))
            {
                data.addLast(new DefaultArray(Utils.parseBinaryString(text)));
            }
            else
            {
                throw new Exception("format of payload <" + format + "> is unknown");
            }
        }
        listPayloadData.add(data);
        return listPayloadData;
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

        if (params.length > 1 && params[0].equalsIgnoreCase("header")) {
            if (params[1].equalsIgnoreCase("ssrc")) {
                var.add(getSsrc());
            }
            else if (params[1].equalsIgnoreCase("payloadType")) {
                var.add(getPayloadType());
            }
            else if (params[1].equalsIgnoreCase("seqnum")) {
                var.add(getSequenceNumber());
            }
            else if (params[1].equalsIgnoreCase("timestamp")) {
                var.add(getTimestampRTP());
            }
            else if (params[1].equalsIgnoreCase("mark")) {
                var.add(getMarker());
            }
            else {
                Parameter.throwBadPathKeywordException(path);
            }
        }
        else if (params.length > 1 && params[0].equalsIgnoreCase("payload")) {
            if (params[1].equalsIgnoreCase("text")) {
                var.add(new String(data.getBytes()));
            }
            else if (params[1].equalsIgnoreCase("binary")) {
                var.add(Array.toHexString(new DefaultArray(data.getBytes())));
            }
            else {
                Parameter.throwBadPathKeywordException(path);
            }
        }

        return var;
    }

    
    public boolean isCipheredMessage()
    {
    	return this.cipheredMessage != null;
    }
    
    public void cipherThisMessage(byte[] cipheredMessage)
    {
    	this.cipheredMessage = new byte[cipheredMessage.length];
    	System.arraycopy(cipheredMessage, 0, this.cipheredMessage, 0, cipheredMessage.length);
    }

    public byte[] getCipheredMessage()
    {
    	return this.cipheredMessage;
    }
}
