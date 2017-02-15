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

package com.devoteam.srit.xmlloader.sctp.lksctp;

import java.util.concurrent.Semaphore;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import com.devoteam.srit.xmlloader.sctp.*;

import dk.i1.sctp.*;

/**
 *
 * @author nghezzaz
 */

public class SocketLksctp extends Thread {

	private SCTPSocket sctpSocket;
	private ChannelLksctp channelSctp;
	private Semaphore mutex = new Semaphore(1,true);

	public SocketLksctp(SCTPSocket aSctpSocket) throws Exception
	{
		this.sctpSocket = aSctpSocket;
		sctp_event_subscribe ses = new sctp_event_subscribe();
		ses.sctp_data_io_event = true;
		ses.sctp_association_event = true;
		ses.sctp_shutdown_event=true;
		try 
		{				
			sctpSocket.subscribeEvents(ses);
		} 
		catch (Exception e1) 
		{
			e1.printStackTrace();
		}
	}

	public void run()
	{		
		Stack stack = null; 
		try
		{
			stack = StackFactory.getStack(channelSctp.getProtocol());
		}
		catch (Exception e)
		{
			return;
		}

		GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "SocketSctp receiver thread started");

		try
		{	
			while(!Thread.interrupted())
			{			
				mutex.acquire();
				if (sctpSocket.isClosed())
				{					
					// Create an empty message for transport connection actions (open or close) 
					// and on server side and dispatch it to the generic stack
					// ((StackSctp) StackFactory.getStack(StackFactory.PROTOCOL_SCTP)).receiveTransportMessage("ABORT-ACK", channelSctp, null);
					
					mutex.release();
					break;					
				}

                SCTPChunk chunk = sctpSocket.receive(1);

                if (chunk != null)
                {
                    if (!(chunk instanceof SCTPData))
                    {	
                        mutex.release();
                        if ((chunk instanceof SCTPNotificationAssociationChangeCommLost)
                           ||(chunk instanceof SCTPNotificationShutdownEvent))
                        {
        					// Create an empty message for transport connection actions (open or close) 
        					// and on server side and dispatch it to the generic stack
        					((StackLksctp) StackFactory.getStack(StackFactory.PROTOCOL_SCTP)).receiveTransportMessage("ABORT-ACK", channelSctp, null);

                            sctpSocket.close();
                        }
                        break;
                    }

                    DataSctp dataSctp = new DataLksctp( (SCTPData)chunk );

                    Msg msg = stack.readFromSCTPData(dataSctp);
    		    	if (msg != null) 
    		    	{
                        msg.setChannel(channelSctp);
                        msg.setListenpoint(channelSctp.getListenpointSctp());

                        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, ">>>RECEIVE the SCTP message :\n", msg);                       
                        stack.getChannel(channelSctp.getName()).receiveMessage(msg);
    		    	}
                }
				mutex.release();

			}
		}
		catch(Exception e)
		{	
			GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Exception in SocketSctp");
		}

		GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "SocketSctp receiver thread stopped");

        try
        {
            synchronized (this)
            {
                if (null != sctpSocket)
                {
                    StackFactory.getStack(channelSctp.getProtocol()).closeChannel(channelSctp.getName());
                }
            }
        }
        catch (Exception e)
        {
        	// nothing to do
        }
		
	}

	public void setChannelSctp(ChannelLksctp aChannelSctp)
	{
		channelSctp = aChannelSctp;
	}	

	public SCTPSocket getSCTPSocket(){
            //some native methods on a closed sctpSocket may SIGSEGV
	    if( sctpSocket!=null && !sctpSocket.isClosed() ){
		return sctpSocket;	
            }else
	    {
		return null;
            }
	}

	public synchronized void send(Msg msg) throws Exception
	{
		if( this.sctpSocket==null){
			throw new java.net.SocketException("SocketLksctp closed");
		}

		try
		{	
			if (msg.getProtocol().equalsIgnoreCase(StackFactory.PROTOCOL_SCTP))
			{
				MsgLksctp msgSctp = (MsgLksctp) msg;
				sctpSocket.send(msgSctp.getSCTPData());
			}
			else
			{
				SCTPData data = new SCTPData();
				//get the ppid from the config
				Config config = StackFactory.getStack(StackFactory.PROTOCOL_SCTP).getConfig();
	            int ppidInt = config.getInteger("client.DEFAULT_PPID", 0);                
	            GlobalLogger.instance().getSessionLogger().debug(TextEvent.Topic.PROTOCOL, "ppidInt =" + ppidInt);
	            data.sndrcvinfo.sinfo_ppid = Utils.convertLittleBigIndian(ppidInt);
	            // get the bytes from the msg
	            byte[] bytes = msg.encode();
				data.setData(bytes);
				sctpSocket.send(data);
			}
			GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "SEND>>> the SCTP message :\n", msg);
		}
		catch(WouldBlockException e1)
		{
			throw new ExecutionException("Error while sending message",e1);
		}
		catch(Exception e2)
		{
			throw new ExecutionException("Error while sending message",e2);
		}
	}

	public void shutdown()
	{
		try
		{			
			mutex.acquire();
			sctpSocket.close();
			mutex.release();
		}
		catch(Exception e)
		{
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Error while closing SCTP socket");
		}
	}	

}
