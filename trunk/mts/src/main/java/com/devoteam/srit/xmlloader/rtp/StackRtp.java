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

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.ReadOnlyDefaultArray;
import gp.utils.arrays.SupArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.core.utils.XMLElementTextMsgParser;
import com.devoteam.srit.xmlloader.rtp.flow.ListenpointRtpFlow;
import com.devoteam.srit.xmlloader.rtp.flow.MsgRtpFlow;
import com.devoteam.srit.xmlloader.srtp.RawPacket;
import com.devoteam.srit.xmlloader.srtp.SRTPCryptoContext;

import java.util.ArrayList;

public class StackRtp extends Stack
{ 
	
    public boolean ignoreReceivedMessages;
    private HashMap <String, ListenpointRtp> ssrcAndListenpointName;
    
    public StackRtp() throws Exception
    {
    	super();
    	
    	ignoreReceivedMessages = getConfig().getBoolean("IGNORE_RECEIVED_MESSAGES", false);
    	this.ssrcAndListenpointName = new HashMap<String, ListenpointRtp>();
    }
    
    public void addSSRCAndListenpointName(String key, ListenpointRtp value)
    {
    	this.ssrcAndListenpointName.put(key, value);
    }
    
	/** Creates a Listenpoint specific to each Stack */
    @Override
	public synchronized Listenpoint parseListenpointFromXml(Element root) throws Exception 
	{
        Listenpoint listenpoint = new ListenpointRtp(this, root);
        return listenpoint;        
	}

	/** Creates a specific Msg */
    @Override
    public Msg parseMsgFromXml(Boolean request, Element root, Runner runner) throws Exception
    {
        MsgRtp msg = null;

        if(root.element("packet") != null)
        {
        	root = root.element("packet");
        	GlobalLogger.instance().logDeprecatedMessage("sendMessageRTP ...><packet>...</packet></sendMessageRTP", "sendMessageRTP ...>...</sendMessageRTP");
        }
        msg = parsePacketHeader(root, runner);
        msg.setData(parsePacketPayload(root, runner).get(0));        

        return msg;
    }

    public MsgRtp parsePacketHeader(Element packet, Runner runner) throws Exception
    {
        MsgRtp packetRtp = new MsgRtp();
        Element header = packet.element("header");
        
        String ssrc = header.attributeValue("ssrc");
        if(ssrc != null){
            long ssrcLong = Long.parseLong(ssrc);
            packetRtp.setSsrc((int)ssrcLong);
        }                

        String payloadType = header.attributeValue("payloadType");
        if(payloadType != null)
            packetRtp.setPayloadType(Integer.parseInt(payloadType));

        String seqnum = header.attributeValue("seqnum");
        if((seqnum != null) && (Utils.isInteger(seqnum)))
            packetRtp.setSequenceNumber(Integer.parseInt(seqnum));

        String timestamp = header.attributeValue("timestamp");
        if((timestamp != null) && (Utils.isInteger(timestamp)))
            packetRtp.setTimestampRTP(Long.parseLong(timestamp));

        String mark = header.attributeValue("mark");
        if((mark != null) && (Utils.isInteger(mark)))
            packetRtp.setMarker(Integer.parseInt(mark));

        return packetRtp;
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
    
     /**
     * Creates a Msg specific to each Stack
     * Use for UDP like protocol : to build incoming message
     * should become ABSTRACT later
     */
    @Override
    public Msg readFromDatas(byte[] datas, int length) throws Exception
    {
        byte[] copyData = new byte[length];
        System.arraycopy(datas, 0, copyData, 0, length);

        Array array = new ReadOnlyDefaultArray(copyData);
        MsgRtp msg = new MsgRtp(array);
        
        /*
        if (((ListenpointRtp) msg.getListenpoint()).isSecured())
        {
        	System.out.println("RECEIVING CIPHERED MESSAGE");
        	ListenpointRtp lrtp = (ListenpointRtp) msg.getListenpoint();
        	
        	RawPacket rp = new RawPacket(copyData, 0, length);
        	rp = lrtp.reverseTransformCipheredMessage(rp);
        	
        	byte[] uncipheredData = new byte[length - lrtp.getCipheredAuthTagLength(1)];
        	System.arraycopy(rp.getBuffer(), 0, uncipheredData, 0, length - lrtp.getCipheredAuthTagLength(1));
        	
        	Array uncipheredArray = new ReadOnlyDefaultArray(uncipheredData);
        	msg = null;
        	msg = new MsgRtp(uncipheredArray);
        }
        */
        
        return msg;
    }

    public Config getConfig() throws Exception
    {
        return Config.getConfigByName("rtp.properties");
    }

    public XMLElementReplacer getElementReplacer()
    {
        return XMLElementTextMsgParser.instance();
    }
    
    public boolean receiveMessage(Msg msg) throws Exception
    {
    	Listenpoint inc = msg.getListenpoint();
    	
    	if (inc.getProtocol().equalsIgnoreCase(StackFactory.PROTOCOL_RTPFLOW))
    	{
			ListenpointRtpFlow lstp = (ListenpointRtpFlow) inc;
			if (!lstp.isSecured())
				return super.receiveMessage(msg);
			
			MsgRtpFlow msgFlow = (MsgRtpFlow) msg;
			
			msgFlow.uncipherPayloadList(lstp.getSRTPTransformer(1), lstp.getCipheredAuthTagLength(1));
			
			msg = null;
			msg = msgFlow;
			
    		return super.receiveMessage(msg);
    	}
    	ArrayList<Listenpoint> lis = super.getAllListenpoint();
    	
    	//here we are getting a Msg with an UDPBIO listenpoint, or we want to get the RTP Listenpoint that match the incoming msg listenpoint port
    	for (int i = 0; i < lis.size(); i++)
    	{
    		if (lis.get(i).getPort() == inc.getPort())
    		{
    			//we found an RTP Listenpoint that match the incoming port
    			ListenpointRtp lrtp = (ListenpointRtp) lis.get(i);
    			if (!lrtp.isSecured())
    				break;
    			//Listenpoint is set as secured so we uncipher the msg and replace it 
            	RawPacket rp = new RawPacket(msg.getBytesData(), 0, msg.getBytesData().length);
            	rp = lrtp.reverseTransformCipheredMessage(rp);
            	
            	byte[] uncipheredData = new byte[msg.getBytesData().length - lrtp.getCipheredAuthTagLength(1)];
            	System.arraycopy(rp.getBuffer(), 0, uncipheredData, 0, msg.getBytesData().length - lrtp.getCipheredAuthTagLength(1));
            	
            	Array uncipheredArray = new ReadOnlyDefaultArray(uncipheredData);
            	msg = null;
            	msg = new MsgRtp(uncipheredArray);
    			break;
    		}
    	}
    	
    	return super.receiveMessage(msg);
    }
}