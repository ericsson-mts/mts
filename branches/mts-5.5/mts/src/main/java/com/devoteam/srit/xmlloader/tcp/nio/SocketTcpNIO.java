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

package com.devoteam.srit.xmlloader.tcp.nio;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.hybridnio.HybridSocket;
import com.devoteam.srit.xmlloader.core.hybridnio.HybridSocketInputHandler;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.tcp.StackTcp;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 *
 * @author sngom
 */
public class SocketTcpNIO implements HybridSocketInputHandler
{

    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private ChannelTcpNIO channel;
    private Stack stack;
    
    public void setChannelTcp(ChannelTcpNIO channel) throws Exception
    {
        this.channel = channel;
        this.stack = StackFactory.getStack(this.channel.getProtocol());
    }

    public synchronized void send(Msg msg) throws Exception
    {
        try
        {
            outputStream.write(msg.getBytesData());
            outputStream.flush();
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
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Error while closing TCP socket");
        }
    }

    public boolean handle(HybridSocket hybridSocket)
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
                        msg.setListenpoint(channel.getListenpointTcp());
                    }
                    stack.getChannel(channel.getName()).receiveMessage(msg);
                }
                return true;
            }
            catch (Exception e)
            {
                try
                {
                    synchronized(this)
                    {
                        if (null != socket)
                        {
                        	StackFactory.getStack(channel.getProtocol()).closeChannel(channel.getName());

        					// Create an empty message for transport connection actions (open or close) 
        					// and on server side and dispatch it to the generic stack 
                        	((StackTcp) StackFactory.getStack(StackFactory.PROTOCOL_TCP)).receiveTransportMessage("FIN-ACK", channel, null);
                        }
                    }
                }
                catch(Exception ee){ /* ignore */}

                if ((null != e.getMessage()) && e.getMessage().equalsIgnoreCase("End of stream detected"))
                {
                    //TODO
                }
                else
                {
                    GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Exception : SocketTcp thread", channel);
                }
                
                return false;
            }
    }

    public void init(HybridSocket hybridSocket)
    {
        try
        {
            this.socket = hybridSocket;
            this.inputStream = socket.getInputStream();
            this.outputStream = socket.getOutputStream();
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
   }
}
