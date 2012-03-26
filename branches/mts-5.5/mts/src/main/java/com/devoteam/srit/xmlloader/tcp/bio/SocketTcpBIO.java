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

package com.devoteam.srit.xmlloader.tcp.bio;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.tcp.StackTcp;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 *
 * @author sngom
 */
public class SocketTcpBIO extends Thread
{
    private Socket socket;
    
    private InputStream  inputStream;

    private OutputStream outputStream;
    
    private ChannelTcpBIO channel;
    
    /** Creates a new instance of SocketClientReceiver */
    public SocketTcpBIO(Socket socket) throws Exception
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

        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "SocketTcp listener thread started : ", channel);
        
		boolean exception = false;    	
        while (!exception)
        {
            try
            {
		    	Msg msg = stack.readFromStream(inputStream, stack.getChannel(channel.getName()));
		    	if (msg != null) 
		    	{
                    if(msg.getChannel() == null)
                    {
                        msg.setChannel(channel);
                    }
                    if(msg.getListenpoint() == null)
                    {
                        msg.setListenpoint(channel.getListenpointTcp());
                    }
		    		stack.getChannel(channel.getName()).receiveMessage(msg);
		    	}
    		}
	        catch(SocketException e)
	        {
                exception = true;
	        }
            catch(Exception e)
            {
				if ((null != e.getMessage()) && (e.getMessage().equalsIgnoreCase("End of stream detected") || e.getMessage().equalsIgnoreCase("EOFException")))
				{
					exception = true;
					
					try
					{
						// Create an empty message for transport connection actions (open or close) 
						// and on server side and dispatch it to the generic stack
						((StackTcp) StackFactory.getStack(StackFactory.PROTOCOL_TCP)).receiveTransportMessage("FIN-ACK", channel, null);
					}
					catch (Exception ex)
					{
						GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, ex, "Exception : SocketTcp thread", channel);
					}
				}
				else
				{
					GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Exception : SocketTcp thread", channel);
				}
            }
    	}
        
        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "SocketTcp listener thread stopped : ", channel);

        try
        {
            synchronized (this)
            {
                if (null != socket)
                {
                    StackFactory.getStack(channel.getProtocol()).closeChannel(channel.getName());
                }
            }
        }
        catch (Exception e)
        {
        	// nothing to do
        }
    }
    
    public void setChannelTcp(ChannelTcpBIO channel)
    {
        this.channel = channel;
    }

    public synchronized void send(Msg msg) throws Exception
    {
        try
        {        
        	{
	            outputStream.write(msg.getBytesData());
	            boolean flushSocket = Config.getConfigByName("tcp.properties").getBoolean("FLUSH_AFTER_SENDING", false);
	            if (flushSocket)
	            {
	            	outputStream.flush();
	            }
	            double delay = Config.getConfigByName("tcp.properties").getDouble("DELAY_AFTER_SENDING", (double) 0.0);
	            int pause = (int) (delay * 1000);
	            if (delay != 0)
	            {
	            	Utils.pauseMilliseconds(pause);
	            }
        	}
        }
        catch(Exception e)
        {
            throw new ExecutionException("Exception : Send a message " + msg.toShortString(), e);
        }
    }
    
    public void close()
    {
        try
        {
            Socket tmp = socket;
            socket = null;
            if(null != tmp){
                tmp.close();
            }
        }
        catch(Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Error while closing TCP socket");
        }
    }

    public Socket getSocket() {
        return socket;
    }

}
