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
import com.devoteam.srit.xmlloader.core.protocol.Msg.ParseFromXmlContext;
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
	
	/** Creates a new instance */
    public StackRtp() throws Exception 
    {
        super();
    }
    
    /** Creates a specific RTP Msg */
    public Msg parseMsgFromXml(ParseFromXmlContext context, Element root, Runner runner) throws Exception
    {                    
    	Msg msgRtp = super.parseMsgFromXml(context, root, runner);
    	
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
        
        return msgRtp;
    }
    
    /**
     * Creates a Msg specific to each Stack
     * Used for UDP like protocol : to build incoming message
     */
    @Override
    public Msg readFromDatas(byte[] datas, int length) throws Exception
    {
        MsgRtp msg = new MsgRtp(this);
        msg.add(extractDataFromMessage(datas, length));
    	return msg;
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
