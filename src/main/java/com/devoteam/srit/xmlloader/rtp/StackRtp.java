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
import gp.utils.arrays.ReadOnlyDefaultArray;

import java.util.HashMap;

import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.core.utils.XMLElementTextMsgParser;
import com.devoteam.srit.xmlloader.rtp.flow.ListenpointRtpFlow;
import com.devoteam.srit.xmlloader.rtp.flow.MsgRtpFlow;
import com.devoteam.srit.xmlloader.rtp.srtp.RawPacket;

import java.util.ArrayList;

public class StackRtp extends Stack
{ 
	
    public boolean ignoreReceivedMessages;
    
    private HashMap <String, ListenpointRtp> ssrcAndListenpointName;
    
    
    /** Creates a new instance */
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
        
	/**
	* Creates a Msg specific to each Stack
	* Used for UDP like protocol : to build incoming message
	*/
    @Override
    public Msg readFromDatas(byte[] datas, int length) throws Exception
    {
        byte[] copyData = new byte[length];
        System.arraycopy(datas, 0, copyData, 0, length);

        MsgRtp msg = new MsgRtp(this);
        msg.decode(copyData);
                
        return msg;
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
            	RawPacket rp = new RawPacket(msg.encode(), 0, msg.encode().length);
            	rp = lrtp.reverseTransformCipheredMessage(rp);
            	
            	byte[] uncipheredData = new byte[msg.encode().length - lrtp.getCipheredAuthTagLength(1)];
            	System.arraycopy(rp.getBuffer(), 0, uncipheredData, 0, msg.encode().length - lrtp.getCipheredAuthTagLength(1));
            	
            	msg = null;
            	msg = new MsgRtp(this);
            	msg.decode(uncipheredData);
    			break;
    		}
    	}
    	
    	return super.receiveMessage(msg);
    }
}