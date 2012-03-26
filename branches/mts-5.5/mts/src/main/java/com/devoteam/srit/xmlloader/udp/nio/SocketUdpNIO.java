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

package com.devoteam.srit.xmlloader.udp.nio;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.concurrent.Semaphore;

import java.net.InetSocketAddress;

public class SocketUdpNIO implements DatagramHandler
{
    private ChannelUdpNIO channelUdp;
    private ListenpointUdpNIO listenpointUdp;
    private int MTU = Config.getConfigByName("udp.properties").getInteger("DEFAULT_BUFFER_LENGHT", 1500);

    private ByteBuffer          currentSendBuffer = ByteBuffer.allocate(MTU);
    private InetSocketAddress   currentRemoteAddress;
    private Exception           currentSendException;
    private Semaphore           currentSendSemaphore = new Semaphore(0);



    public void setListenpointUdp(ListenpointUdpNIO listenpointUdp)
    {
        this.listenpointUdp = listenpointUdp;
    }

    public void setChannelUdp(ChannelUdpNIO channelUdp)
    {
        this.channelUdp = channelUdp;
    }

    public void close()
    {
        try
        {
        	dataChannel.socket().close();
            selectionKey.selector().wakeup();
        }
        catch (Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Error while closing UDP socket");
        }
    }

    private SelectionKey selectionKey;

    private ByteBuffer buffer = ByteBuffer.allocate(MTU);

    private DatagramChannel dataChannel ;
    
    private Stack stack;

    private InetSocketAddress localAddress;

    public void init(SelectionKey selectionKey)
    {
        this.selectionKey = selectionKey;
        this.dataChannel = (DatagramChannel) selectionKey.channel();
        this.localAddress = (InetSocketAddress) dataChannel.socket().getLocalSocketAddress();
        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "SocketUdp ready");
        this.stack = null;
    }

    // triggered when there is data to read
    public void inputReady()
    {
        try
        {
            try
            {
                if (null == stack && channelUdp != null)
                {
                    stack = StackFactory.getStack(channelUdp.getProtocol());
                }
                else if (null == stack && listenpointUdp != null)
                {
                    stack = StackFactory.getStack(listenpointUdp.getProtocol());
                }
            }
            catch (Exception e)
            {
                GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Exception in SocketUdp : unable to instatiate the stack");
                return;
            }

            while(true)
            {
                InetSocketAddress remoteAddress = (InetSocketAddress) dataChannel.receive(buffer);
                
                // null if there is no data to read, so we stop receiving
                if(null == remoteAddress) break;

                buffer.flip();

                Msg msg = stack.readFromDatas(buffer.array(), buffer.remaining());
                buffer.clear();

            	String remoteHost = remoteAddress.getAddress().getHostAddress();
            	int remotePort = remoteAddress.getPort();
                if (channelUdp != null)
                {
                    channelUdp.setRemoteHost(remoteHost);
                    channelUdp.setRemotePort(remotePort);
                    msg.setChannel(channelUdp);
                    channelUdp.receiveMessageNIO(msg);
                }
                else if (listenpointUdp != null)
                {
            		String nameChannel = remoteHost + ":" + remotePort;
            		Channel channel = listenpointUdp.getChannel(nameChannel);
            		if (channel == null)
            		{
                        channel = new ChannelUdpNIO(this, localAddress.getAddress().getHostAddress(), localAddress.getPort(), remoteHost, remotePort, listenpointUdp.getProtocol());
            			listenpointUdp.putChannel(nameChannel, channel);
            		}
                    msg.setChannel(channel);
                    msg.setListenpoint(listenpointUdp);
                    stack.receiveMessageNIO(msg);
                }
            }
        }
        catch(Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Exception in SocketUdp while receiving data");
        }
    }


    public synchronized void send(Msg msg) throws Exception
    {
        try
        {
            currentSendBuffer.clear();
            currentSendBuffer.put(msg.getBytesData());
            currentSendBuffer.flip();

            Channel channel = msg.getChannel();
            // init
            if (channel.getRemoteHost() != null && channel.getRemotePort() != 0)
            {
                currentRemoteAddress = new InetSocketAddress(channel.getRemoteHost(), channel.getRemotePort());
            }
            else if (channel.getRemoteHost() != null)
            {
                currentRemoteAddress = new InetSocketAddress(channel.getRemoteHost(), 0);
            }
            else if (channel.getRemotePort() != 0)
            {
                currentRemoteAddress = new InetSocketAddress(channel.getRemotePort());
            }

            boolean emptyDatagram = (0 == currentSendBuffer.remaining());

            int res = dataChannel.send(currentSendBuffer, currentRemoteAddress);

            if(res == 0 && !emptyDatagram)
            {
                currentSendSemaphore.drainPermits();
                selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
                selectionKey.selector().wakeup();

                currentSendSemaphore.acquire();
                if(null != currentSendException) throw currentSendException;
            }
        }
        catch (Exception e)
        {
            throw new ExecutionException("SocketUDP: Error while sending message", e);
        }
    }

    public void outputReady()
    {
        try
        {
            boolean emptyDatagram = (0 == currentSendBuffer.remaining());

            int res = dataChannel.send(currentSendBuffer, currentRemoteAddress);

            if(res != 0 || emptyDatagram)
            {
                currentSendBuffer.clear();
                
                selectionKey.interestOps(selectionKey.interestOps() & (0xff - SelectionKey.OP_WRITE));

                currentSendException = null;

                currentSendSemaphore.release();
            }
        }
        catch(Exception e)
        {
            currentSendException = e;
            
            currentSendSemaphore.release();
        }
    }
}
