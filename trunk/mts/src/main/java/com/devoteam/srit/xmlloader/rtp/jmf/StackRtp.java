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

import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.Runner;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.core.utils.XMLElementTextMsgParser;
import com.devoteam.srit.xmlloader.rtp.ListenpointRtp;
import com.sun.media.rtp.util.Packet;
import com.sun.media.rtp.util.RTPPacket;
import gp.utils.arrays.DefaultArray;
import java.util.LinkedList;

/**
 *
 * @author gpasquiers
 */
public class StackRtp extends Stack
{        
    /** Creates or returns the instance of this stack */
    public StackRtp() throws Exception {
        super();
    }
    
	/** Creates a Listenpoint specific to each Stack */
    @Override
	public synchronized Listenpoint parseListenpointFromXml(Element root) throws Exception 
	{
        Listenpoint listenpoint = new ListenpointRtp(this, root);
        return listenpoint;        
	}

    /** Creates a Channel specific to each Stack */
    @Override
    public Channel parseChannelFromXml(Element root, String protocol) throws Exception {
        String name = root.attributeValue("sessionName");
        String localHost = root.attributeValue("localHost");
        String localPort = root.attributeValue("localPort");
        String remoteHost = root.attributeValue("remoteHost"); 
        String remotePort = root.attributeValue("remotePort");

        ChannelRtp channel = new ChannelRtp(name, localHost, localPort, remoteHost, remotePort, protocol); 
        return channel;
    }

    /** Creates a specific RTP Msg */
    public Msg parseMsgFromXml(Boolean request, Element root, Runner runner) throws Exception
    {                       
        MsgRtp msgRtp = new MsgRtp();
         
        // instanciates the channel        
        String channelName = root.attributeValue("sessionName");
        Channel channel = getChannel(channelName);
        if(channel != null)
        {
            msgRtp.setChannel(channel);
        }
        else if((channel == null) && (root.attribute("listenpoint") == null))
        {
            throw new ExecutionException("The channel <name=" + channelName + "> does not exist");
        }

        if(root.element("flow") != null)
        {
            throw new Exception("The JMF RTP stack does not support <flow> tag, use the light RTP stack instead");
        }
        
        List<Element> listPackets = root.elements("packet");
        boolean control = parseChannel(listPackets.get(0));
        msgRtp.setControl(control);
        
        Iterator<Element> iter = listPackets.iterator(); 
        while (iter.hasNext()) {
            Element packet = iter.next();
            RTPPacket rtpPacket = parsePacket(packet);
            msgRtp.add(rtpPacket);
        }
        return msgRtp;
    }
        
    /** Parses then returns an RTP Packet from the XML root element */
    private boolean parseChannel(Element root) throws Exception
    {    
        Element header = root.element("header");
        String channel = header.attributeValue("channel");
        boolean control = false;
        if (channel !=  null) {
            if (channel.equalsIgnoreCase("control")) {
                control = true;
            } else if (channel.equalsIgnoreCase("data")) {                
            } else {
                Exception e = new Exception();
                throw new ExecutionException("Bad channel attribute " + channel + " for the <header> tag : possible values are <data> or <control>", e);                                
            }
        }
        return control;
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
        String ssrc = header.attributeValue("ssrc");        
        rtpPacket.ssrc = Integer.parseInt(ssrc);
        String seqnum = header.attributeValue("seqnum");        
        rtpPacket.seqnum = Integer.parseInt(seqnum);
        String timestamp = header.attributeValue("timestamp");        
        rtpPacket.timestamp = Integer.parseInt(timestamp);
        String payloadType = header.attributeValue("payloadType");        
        rtpPacket.payloadType = Integer.parseInt(payloadType);
        rtpPacket.payloadoffset = 12;
        rtpPacket.payloadlength = data.length - rtpPacket.payloadoffset;
        rtpPacket.calcLength();
        rtpPacket.assemble(1, false);
        
        return rtpPacket; 
    }

    /**
     * Creates a Msg specific to each Stack
     * Use for UDP like protocol : to build incoming message
     * should become ABSTRACT later
     */
    @Override
    public Msg readFromDatas(byte[] datas, int length) throws Exception
    {
        MsgRtp msg = new MsgRtp();
        msg.add(extractDataFromMessage(datas, length));
    	return msg;
    }

    /** Returns the Config object to access the protocol config file*/
    public Config getConfig() throws Exception {
        return Config.getConfigByName("rtp.properties");
    }

    /** Returns the replacer used to parse sendMsg Operations */
    public XMLElementReplacer getElementReplacer()
    {
        return XMLElementTextMsgParser.instance();
    }

    private RTPPacket extractDataFromMessage(byte[] datas, int length)
    {
        RTPPacket packet = new RTPPacket();
        DefaultArray array = new DefaultArray(datas, 0, length);
//        packet.padding = array.getBit(2);
        packet.extensionPresent = (array.getBit(3) == 0) ? false : true;
//        packet.csrcCount = array.getBits(4, 4);
        packet.marker = array.getBit(8);
        packet.payloadType = array.getBits(9, 7);
        packet.seqnum = array.getBits(16, 16);
        packet.timestamp = array.getBits(32, 32);
        packet.ssrc = array.getBits(64, 32);

        //CSRC and extension are not managed in this decoding
//        int i = 0;
        int csrcCount = array.getBits(4, 4);
        if(csrcCount != 0)
        {
//            csrc = new Vector<Array>();
//            for(i = 0; i < csrcCount; i++)
//                csrc.add(array.subArray(12 + 4 * i , 4));
            //packet.csrc = array.subArray(12, 4 * csrcCount).getBytes();
        }
        int headerLength = 12 + 4 * csrcCount;

        //get extension if present
        if(packet.extensionPresent)
        {
//            packet.extensionType = array.getBits(headerLength, 16);
            headerLength += 2;
            int extensionLength = array.getBits(headerLength, 16);
            headerLength += 2;
//            extensionData = new Vector<Array>();
//            for(i = 0; i < extensionLength; i++)
//                csrc.add(array.subArray(headerLength + 4 * i , 4));
            headerLength += extensionLength * 4;
        }

        packet.data = array.subArray(headerLength, array.length - headerLength).getBytes();
        packet.payloadoffset = 0;
        packet.payloadlength = array.length - headerLength;
        return packet;
    }

}
