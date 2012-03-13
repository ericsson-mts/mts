/*
 * ChannelRtsp.java
 *
 * Created on 26 juin 2007, 10:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.rtsp;

import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;

import com.devoteam.srit.xmlloader.tcp.ChannelTcp;
import com.devoteam.srit.xmlloader.udp.ChannelUdp;

public class ChannelRtsp extends Channel
{
    private Channel channel = null;
    private String transport = null;
    
    // --- constructure --- //
    public ChannelRtsp(String name, String aLocalHost, String aLocalPort, String aRemoteHost, String aRemotePort, String aProtocol, String aTransport) throws Exception {
    	super(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);

        transport = aTransport;
        if(transport.equals(StackFactory.PROTOCOL_TCP))
        {
        	channel = new ChannelTcp(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        }
        else 
        {
        	channel = new ChannelUdp(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol, false);
        }
    }

    // --- basic methods --- //
    public boolean open() throws Exception {
        return channel.open();
    }
    
    /** Send a Msg to Channel */
    public boolean sendMessage(Msg msg) throws Exception{ 
        if (null == channel)
            throw new Exception("Channel is null, has one channel been opened ?");

        if (msg.getChannel() == null)
            msg.setChannel(this);

        channel.sendMessage((MsgRtsp) msg);

        return true;
    }
    
    public boolean close(){
        try {
        	channel.close();
        } catch (Exception e) {
            // nothing to do
        }
        channel = null;
        return true;
    }
    
    /** Get the transport protocol of this message */
    public String getTransport() 
    {
        return transport;
    }
    
}
