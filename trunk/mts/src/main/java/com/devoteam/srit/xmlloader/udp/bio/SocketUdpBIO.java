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

package com.devoteam.srit.xmlloader.udp.bio;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.rtp.MsgRtp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class SocketUdpBIO extends Thread {

    private DatagramSocket datagramSocket;
    private ChannelUdpBIO channelUdp;
    private ListenpointUdpBIO listenpointUdp;
    private DatagramPacket dp = null;
    private int MTU = 0;
    private boolean closed = false;

    public SocketUdpBIO(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
        this.MTU = Config.getConfigByName("udp.properties").getInteger("DEFAULT_BUFFER_LENGHT", 1500);
    }

    public void run() {
        Stack stack = null;
        try {
            if (channelUdp != null) {
                stack = StackFactory.getStack(channelUdp.getProtocol());
            }
            else if (listenpointUdp != null) {
                stack = StackFactory.getStack(listenpointUdp.getProtocol());
            }
        }
        catch (Exception e) {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Exception in SocketUdp : unable to instatiate the stack");
            return;
        }

        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "BIO SocketUdp started");

        byte[] buffer = new byte[MTU];
        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);

        while (!closed) {
            try {
                datagramSocket.receive(datagramPacket);

                Msg msg = stack.readFromDatas(datagramPacket.getData(), datagramPacket.getLength());
                String remoteHost = "";
                if (datagramPacket.getAddress() != null) {
                	remoteHost = datagramPacket.getAddress().getHostAddress();
                }
                int remotePort = datagramPacket.getPort();
                
                if (channelUdp != null) {
                    channelUdp.setRemoteHost(remoteHost);
                    channelUdp.setRemotePort(remotePort);
                    msg.setChannel(channelUdp);
                    // GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "RECEIVE UDP msg : ", msg);
                    channelUdp.receiveMessage(msg);
                }
                else if (listenpointUdp != null) {
                    String locHost = datagramSocket.getLocalAddress().getHostAddress();
            		String nameChannel = remoteHost + ":" + remotePort;
            		Channel channel = listenpointUdp.getChannel(nameChannel);
            		if (channel == null)
            		{
            			channel = new ChannelUdpBIO(this, locHost, datagramSocket.getLocalPort(), remoteHost, remotePort, listenpointUdp.getProtocol());
            			listenpointUdp.putChannel(nameChannel, channel);
            		}
                    msg.setChannel(channel);
                    msg.setListenpoint(listenpointUdp);
                    // GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "RECEIVE UDP msg : ", msg);
                    stack.receiveMessage(msg);
                }
            }
            catch (Exception e) {
                if(!closed){
                    GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, e, "Exception in SocketUdp", channelUdp);
                }
            }
        }
        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "BIO SocketUdp stopped");
    }

    public void setListenpointUdp(ListenpointUdpBIO listenpointUdp) {
        this.listenpointUdp = listenpointUdp;
    }

    public void setChannelUdp(ChannelUdpBIO channelUdp) {
        this.channelUdp = channelUdp;
    }

    public synchronized void send(Msg msg, InetSocketAddress remoteDatagramSocketAddress) throws Exception {
    	try {
            byte[] data = msg.getBytesData();
            if (msg instanceof MsgRtp && ((MsgRtp) msg).isCipheredMessage())
            	data = ((MsgRtp) msg).getCipheredMessage();
            	
            if (dp == null) {
                dp = new DatagramPacket(data, data.length);
            }
            else {
                dp.setData(data);
            }
            dp.setSocketAddress(remoteDatagramSocketAddress);
            datagramSocket.send(dp);
        }
        catch (Exception e) {
            throw new ExecutionException("SocketUDP: Error while sending message", e);
        }
    }

    public void close() {
        try {
            closed = true;
            this.datagramSocket.close();
        }
        catch (Exception e) {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Error while closing UDP socket");
        }
    }
}