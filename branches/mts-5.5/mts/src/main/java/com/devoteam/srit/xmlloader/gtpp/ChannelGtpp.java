/*
 * ChannelSmpp.java
 *
 */

package com.devoteam.srit.xmlloader.gtpp;

import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;

import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.tcp.ChannelTcp;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ChannelGtpp extends Channel
{
    private ChannelTcp channel = null;

    // --- constructeur --- //
    public ChannelGtpp(String name, String aLocalHost, String aLocalPort, String aRemoteHost, String aRemotePort, String aProtocol) throws Exception {
    	super(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        channel = new ChannelTcp(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
    }

    public ChannelGtpp(String name, Listenpoint listenpoint, Socket socket) throws Exception
    {
        super(
                name,
                ((InetSocketAddress)socket.getLocalSocketAddress()).getAddress().getHostAddress(),
                Integer.toString(((InetSocketAddress)socket.getLocalSocketAddress()).getPort()),
                ((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress().getHostAddress(),
                Integer.toString(((InetSocketAddress)socket.getRemoteSocketAddress()).getPort()),
                listenpoint.getProtocol()
        );
        channel = new ChannelTcp(name, listenpoint, socket);
    }

    // --- basic methods --- //
    public boolean open() throws Exception {
        boolean result = channel.open();

        return result;
    }

    /** Send a Msg to Channel */
    public boolean sendMessage(Msg msg) throws Exception{
        if (null == channel)
            throw new Exception("Channel is null, has one channel been opened ?");

        if (msg.getChannel() == null)
            msg.setChannel(this);

        channel.sendMessage((MsgGtpp) msg);
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

    @Override
    public String getTransport() {
        return StackFactory.PROTOCOL_TCP;
    }

    public boolean isServer(){
        return (channel.getListenpointTcp() != null);
    }

}
