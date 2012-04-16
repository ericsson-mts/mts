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

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;

import java.util.Iterator;

import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.sun.media.rtp.util.RTPPacket;

/**
 *
 * @author fhenry
 */
public class ChannelRtp extends Channel
{  
    // RTP session manager
    private RtpManager rtpManager = null;
        
    /** Creates a new instance of Channel */
    public ChannelRtp(String name, String localHost, String localPort, String remoteHost, String remotePort, String aProtocol) throws Exception { 
        super(name, localHost, localPort, remoteHost, remotePort, aProtocol);  
    }

    /** Creates a new instance of Channel */
    public ChannelRtp(String localHost, int localPort, String remoteHost, int remotePort, String aProtocol)
    {
        super(localHost, localPort, remoteHost, remotePort, aProtocol);        
    }
    
    public boolean open() throws Exception {
        rtpManager = new RtpManager(this);        
        rtpManager.open(getLocalHost(), getLocalPort(), getRemoteHost(), getRemotePort());
        return true;
    }

    /** Close a channel to each Stack */
    public boolean close() {
        try {
            rtpManager.close();                    
        } catch (Exception e) {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Error while closing the RtpManager");
        }
        return true;
    }
    
    
    /** Send a Msg to RTP Stack */
    public synchronized boolean sendMessage(Msg msg) throws Exception
    {       
        MsgRtp msgRtp = (MsgRtp) msg; 
        Iterator<RTPPacket> iter = msgRtp.getRtpPackets(); 
        while (iter.hasNext()) {
            RTPPacket rtpPacket = iter.next();
            rtpManager.sendPacket(msgRtp.isControl(), rtpPacket);
        }                          
        return true;
    }
 
    /** Get the transport protocol of this message */
    public String getTransport() 
    {
    	return StackFactory.PROTOCOL_UDP;
    }    
    
}
