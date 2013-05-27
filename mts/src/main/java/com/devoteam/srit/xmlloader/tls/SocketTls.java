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

package com.devoteam.srit.xmlloader.tls;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.rtp.MsgRtp;
import com.devoteam.srit.xmlloader.tcp.StackTcp;

import javax.net.ssl.SSLException;

/**
 *
 * @author fvandecasteele
 */
public class SocketTls extends Thread
{

    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private ChannelTls channel;

    /** Creates a new instance of SocketClientReceiver */
    public SocketTls(Socket socket) throws Exception
    {
        this.socket = socket;

        this.inputStream = new BufferedInputStream(socket.getInputStream());
        this.outputStream = socket.getOutputStream();

    }

    public void run()
    {
        Stack stack = null;
        try
        {
            stack = StackFactory.getStack(channel.getProtocol());
        }
        catch (Exception e)
        {
            return;
        }

        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "SocketTls listener thread started : ", channel);

        boolean exception = false;
        while (!exception)
        {
            try
            {
                Msg msg = stack.readFromStream(inputStream, stack.getChannel(channel.getName()));
                if (msg != null)
                {
                    if (msg.getChannel() == null)
                    {
                        msg.setChannel(channel);
                    }
                    if (msg.getListenpoint() == null)
                    {
                        msg.setListenpoint(channel.getListenpointTLS());
                    }
                    stack.getChannel(channel.getName()).receiveMessage(msg);
                }
            }
            catch (SocketException e)
            {
                exception = true;
                
				try
				{
					// Create an empty message for transport connection actions (open or close) 
					// and on server side and dispatch it to the generic stack
					((StackTls) StackFactory.getStack(StackFactory.PROTOCOL_TLS)).receiveTransportMessage("FIN-ACK", channel, null);
				}
				catch (Exception ex)
				{
					GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, ex, "Exception : SocketTcp thread", channel);
				}                
            }
            catch (SSLException e)
            {
                exception = true;
            }
            catch (Exception e)
            {
                if ((null != e.getMessage()) && e.getMessage().equalsIgnoreCase("End of stream detected"))
                {
                    exception = true;
                } else
                {
                    GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Exception : SocketTls thread", channel);
                }
            }
        }

        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "SocketTls listener thread stopped : ", channel);

        try
        {
            synchronized (this)
            {
                if (null != socket)
                {
                    // StackFactory.getStack(channel.getProtocol()).closeChannel(channel.getName());
                }
            }
        }
        catch (Exception e)
        {
            // nothing to do
        }
    }

    public void setChannelTls(ChannelTls channel)
    {
        this.channel = channel;
    }

    public synchronized void send(Msg msg) throws Exception
    {
        try
        {
            {
            	byte[] data = msg.getBytesData();
            	if (msg instanceof MsgRtp && ((MsgRtp) msg).isCipheredMessage())
                	data = ((MsgRtp) msg).getCipheredMessage();
                outputStream.write(data);
                // outputStream.flush();
            }
        }
        catch (Exception e)
        {
            throw new ExecutionException("Exception : Send a message " + msg.toShortString(), e);
        }
    }

    public void close()
    {
        try
        {
            synchronized (this)
            {
                socket.close();
                socket = null;
            }
        }
        catch (Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Error while closing TLS socket");
        }
    }
}
