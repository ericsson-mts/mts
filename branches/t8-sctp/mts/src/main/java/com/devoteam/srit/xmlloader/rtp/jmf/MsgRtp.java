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

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.TransactionId;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.rtp.flow.CodecDictionary;
import com.sun.media.rtp.util.Packet;
import com.sun.media.rtp.util.RTPPacket;

/**
 *
 * @author gpasquiers
 */
public class MsgRtp extends Msg {
       
    private boolean control;     
        
    private Vector<RTPPacket> rtpPackets = null;            
    
    /** Creates a new instance of MsgRtp */
    public MsgRtp(Stack stack) {
    	super(stack);
        rtpPackets = new Vector<RTPPacket>();
    }

    /** Add a new RTP Packet to the message */
    public void add(RTPPacket rtpPacket) {
        this.rtpPackets.add(rtpPacket);
    }
    
    /** Get the transaction Identifier of this message 
     * Transaction has no sense in RTP because there are no response (stream protocol) */
    public TransactionId getTransactionId(){
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
		int payloadType = rtpPackets.get(0).payloadType;
        return new Integer(payloadType).toString();
    }
    
    /** Get the command code of this message */
    @Override
    public String getTypeComplete() 
    {
    	int payloadType = rtpPackets.get(0).payloadType;
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

    /** Return the length of the message*/
    @Override
    public int getLength()
    {
    	// only the first element in the list is used
    	RTPPacket rtpPacket = ((RTPPacket) rtpPackets.get(0));
    	return rtpPacket.payloadoffset + rtpPacket.payloadlength; 
    }
    
    /** Return the transport of the message*/
    public String getTransport() {
    	return StackFactory.PROTOCOL_UDP;
    }
    
    /**
     * prints the packet header 
     */
    private static String headerToString(RTPPacket rtpPacket)
    {
        String ret = "";
        ret += "<header payloadType=\"" + rtpPacket.payloadType + "\" ";
        ret += "ssrc=\"" + rtpPacket.ssrc + "\" ";
        ret += "seqnum=\"" + rtpPacket.seqnum + "\" ";
        ret += "timestamp=\"" + rtpPacket.timestamp + "\" ";
        ret += "mark=\"" + rtpPacket.marker + "\"/>";
        return ret;
    }    	 
    	
    /**
     * prints as String an avp and it's sub-avps (recursive)
     */
    private static String packetToString(RTPPacket rtpPacket) throws Exception
    {
        String ret = "<packet>\n";
        ret += Utils.indent(1) + headerToString(rtpPacket) + "\n";
        ret += Utils.indent(1) + "<payload format=\"binary\">\n";
        ret += Utils.toBinaryString(rtpPacket.data, rtpPacket.payloadoffset, rtpPacket.payloadlength, 0) + "\n";
        ret += Utils.indent(1) + "</payload>\n"; 
        ret += Utils.indent(1) + "<payload format=\"text\">\n";
        String text = new String(rtpPacket.data, rtpPacket.payloadoffset, rtpPacket.payloadlength);
        ret += text + "\n";
        ret += Utils.indent(1) + "</payload>";
        ret += "\n</packet>\n";        
        return ret;        
    }

    public Iterator getRtpPackets() {
        return rtpPackets.iterator();
    }

    public boolean isControl() {
        return control;
    }

    public void setControl(boolean control) {
        this.control = control;
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
    	// only the first element in the list is used
    	RTPPacket rtpPacket = ((RTPPacket) rtpPackets.get(0));
        return rtpPacket.data;
    }

    /** 
     * decode the message from binary data 
     */
    @Override
    public void decode(byte[] data) throws Exception
    {
    	// nothing to do : we use external JMF RTP stack to transport messages
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
        ret += headerToString(rtpPackets.get(0));
        return ret;
    }

    /** 
     * Convert the message to XML document 
     */
    @Override
    public String toXml() throws Exception 
    {
		String xml = "";
        Iterator<RTPPacket> iter = rtpPackets.iterator(); 
        while (iter.hasNext()) {
            RTPPacket rtpPacket = iter.next();
            try
            {
            	xml += packetToString(rtpPacket);
            }
            catch (Exception e)
            {
                GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "An error occured while logging the diameter response : ", rtpPacket);
            }
        }
        return xml;
    }
    
    /** 
     * Parse the message from XML element 
     */
    @Override
    public void parseFromXml(Boolean request, Element root, Runner runner) throws Exception
    {
    	super.parseFromXml(request,root,runner);

    	RTPPacket rtpPacket = parsePacket(root);
        if (rtpPacket != null)
        {
        	add(rtpPacket);
        }
        boolean control = parseChannel(root);
        setControl(control);

        List<Element> listPackets = root.elements("packet");
        if (listPackets.size() > 0)
        {
	        control = parseChannel(listPackets.get(0));
	        setControl(control);
        }
        Iterator<Element> iter = listPackets.iterator(); 
        while (iter.hasNext()) {
            Element packet = iter.next();
            rtpPacket = parsePacket(packet);
            if (rtpPacket != null)
            {
            	add(rtpPacket);
            }
        }
    }
    
    /** Parses then returns the channel from the XML root element */
    private boolean parseChannel(Element root) throws Exception
    {    
        Element header = root.element("header");
        if (header !=  null)
        {
	        String channel = header.attributeValue("channel");
	        boolean control = false;
	        if (channel !=  null) 
	        {
	            if (channel.equalsIgnoreCase("control")) 
	            {
	                control = true;
	            } 
	            else if (channel.equalsIgnoreCase("data")) 
	            {                
	            } 
	            else 
	            {
	                Exception e = new Exception();
	                throw new ExecutionException("Bad channel attribute " + channel + " for the <header> tag : possible values are <data> or <control>", e);                                
	            }
	        }
	        return control;
        }
        return false;
    }

    /** Parses then returns an RTP Packet from the XML root element */
    private RTPPacket parsePacket(Element root) throws Exception
    {
        List<Element> elements = root.elements("payload");
        List<byte[]> datas = new LinkedList<byte[]>();
        
        for(Element element:elements)
        {
            if(element.attributeValue("format").equalsIgnoreCase("text")) {
                String text = element.getTextTrim();
                datas.add(text.getBytes("UTF8"));
            }
            else if(element.attributeValue("format").equalsIgnoreCase("binary")) {
                String text = element.getTextTrim();
                datas.add(Utils.parseBinaryString(text));
            }
        }
        
        //
        // Compute total length
        //
        int length = 0;
        for(byte[] data:datas)
        {
            length += data.length;
        }
        
        byte[] data = new byte[length + 12];
        
        int i=0;
        for(byte[] aData:datas)
        {
            for(int j=0; j<aData.length; j++)
            {
                data[i + 12] = aData[j];
                i++;
            }
        }

        Packet packet = new Packet();
        packet.data = data;
        
        RTPPacket rtpPacket = new RTPPacket(packet);

        //
        // Parse header tag
        //
        Element header = root.element("header");
        if (header != null)
        {
	        String ssrc = header.attributeValue("ssrc");        
	        rtpPacket.ssrc = Integer.parseInt(ssrc);
	        String payloadType = header.attributeValue("payloadType");        
	        rtpPacket.payloadType = Integer.parseInt(payloadType);
	        String seqnum = header.attributeValue("seqnum");        
	        rtpPacket.seqnum = Integer.parseInt(seqnum);
	        String timestamp = header.attributeValue("timestamp");        
	        rtpPacket.timestamp = Integer.parseInt(timestamp);
	        String marker = header.attributeValue("mark"); 
	        if (marker != null)
	        {
	        	rtpPacket.marker = Integer.parseInt(marker);
	        }
	        rtpPacket.payloadoffset = 12;
	        rtpPacket.payloadlength = data.length - rtpPacket.payloadoffset;
	        rtpPacket.calcLength();
	        rtpPacket.assemble(1, false);
	        
	        return rtpPacket;
        }
        return null;
    }

    //------------------------------------------------------
    // method for the "setFromMessage" <parameter> operation
    //------------------------------------------------------

    /** 
     * Get a parameter from the message
     */
    public Parameter getParameter(String path)throws Exception
    {
        Parameter var = super.getParameter(path);
        if (var != null) {
            return var;
        }
        
        var = new Parameter();
        String[] params = Utils.splitPath(path);
        
        if(params.length>1 && params[0].equalsIgnoreCase("header"))
        {
            if(params[1].equalsIgnoreCase("ssrc"))
            {
                for (int i = 0; i < rtpPackets.size(); i++) 
                {
                    RTPPacket rtpPacket = rtpPackets.get(i);
                    var.add(Integer.toString(rtpPacket.ssrc));
                }
            } 
            else if(params[1].equalsIgnoreCase("payloadType")) 
            {
                for (int i = 0; i < rtpPackets.size(); i++) 
                {
                    RTPPacket rtpPacket = rtpPackets.get(i);                 
                    var.add(Integer.toString(rtpPacket.payloadType));
                }
            } 
            else if(params[1].equalsIgnoreCase("seqnum")) 
            {
                for (int i = 0; i < rtpPackets.size(); i++) 
                {
                    RTPPacket rtpPacket = rtpPackets.get(i);                 
                    var.add(Integer.toString(rtpPacket.seqnum));
                }
            } 
            else if(params[1].equalsIgnoreCase("timestamp")) 
            {
                for (int i = 0; i < rtpPackets.size(); i++) 
                {
                    RTPPacket rtpPacket = rtpPackets.get(i);                 
                    var.add(Long.toString(rtpPacket.timestamp));
                }
            }
            else if(params[1].equalsIgnoreCase("mark")) 
            {
                for (int i = 0; i < rtpPackets.size(); i++) 
                {
                    RTPPacket rtpPacket = rtpPackets.get(i);                 
                    var.add(Long.toString(rtpPacket.marker));
                }
            }            
            else
            {
            	Parameter.throwBadPathKeywordException(path);
            }
        }        
        else if(params.length>1 && params[0].equalsIgnoreCase("payload"))
        {
            if(params[1].equalsIgnoreCase("text"))
            {
                for (int i = 0; i < rtpPackets.size(); i++) 
                {
                    RTPPacket rtpPacket = rtpPackets.get(i);
                    var.add(new String(rtpPacket.data, rtpPacket.payloadoffset, rtpPacket.payloadlength));
                }
            } 
            else if(params[1].equalsIgnoreCase("binary")) 
            {
                for (int i = 0; i < rtpPackets.size(); i++) 
                {
                    RTPPacket rtpPacket = rtpPackets.get(i);
                	var.add(Array.toHexString(new DefaultArray(rtpPacket.data, rtpPacket.payloadoffset, rtpPacket.payloadlength)));
                }
            }
            else
            {
            	Parameter.throwBadPathKeywordException(path);
            }
        }
        else
        {
        	Parameter.throwBadPathKeywordException(path);
        }
                
        return var;
    }            

}
