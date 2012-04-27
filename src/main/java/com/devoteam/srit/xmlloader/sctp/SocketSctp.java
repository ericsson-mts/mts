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

package com.devoteam.srit.xmlloader.sctp;

import com.devoteam.srit.xmlloader.core.Parameter;
import java.net.SocketException;
import java.util.concurrent.Semaphore;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.tcp.StackTcp;

import dk.i1.sctp.AssociationId;
import dk.i1.sctp.OneToOneSCTPSocket;
import dk.i1.sctp.SCTPChunk;
import dk.i1.sctp.SCTPData;
import dk.i1.sctp.SCTPNotificationAssociationChangeCommLost;
import dk.i1.sctp.SCTPNotificationShutdownEvent;
import dk.i1.sctp.WouldBlockException;
import dk.i1.sctp.sctp_event_subscribe;
/**
 *
 * @author nghezzaz
 */

public class SocketSctp extends Thread {

	private OneToOneSCTPSocket sctpSocket;
	private ChannelSctp channelSctp;
	private Semaphore mutex = new Semaphore(1,true);

	public SocketSctp(OneToOneSCTPSocket aSctpSocket) throws Exception
	{
		this.sctpSocket = aSctpSocket;
		sctp_event_subscribe ses = new sctp_event_subscribe();
		ses.sctp_data_io_event = true;
		ses.sctp_association_event = true;
		ses.sctp_shutdown_event=true;
		try {				
			sctpSocket.subscribeEvents(ses);
		} catch (SocketException e1) {
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
        					((StackSctp) StackFactory.getStack(StackFactory.PROTOCOL_SCTP)).receiveTransportMessage("ABORT-ACK", channelSctp, null);

                            sctpSocket.close();
                        }
                        break;
                    }
                    /* SETTER L'AID
                    (SCTPData)chunk).sndrcvinfo.sinfo_assoc_id.hashCode()));
                    setAidFromMsg();
                    */
                    Msg msg = stack.readFromSCTPData((SCTPData)chunk);
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

		//
		// try to remove itself
		//
		try
		{	
			if(sctpSocket != null)
			{ 
				StackFactory.getStack(channelSctp.getProtocol()).closeChannel(channelSctp.getName());
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "SocketSctp receiver thread stopped");

	}

	public void setChannelSctp(ChannelSctp aChannelSctp)
	{
		channelSctp = aChannelSctp;
	}	

	public OneToOneSCTPSocket getSctpSocket(){
		return sctpSocket;	
	}

	public synchronized void send(Msg msg) throws Exception
	{
		try
		{	
			SCTPData data = new SCTPData();
			data.setData(msg.getBytesData());

            Config config = StackFactory.getStack(StackFactory.PROTOCOL_SCTP).getConfig();
            
            String logDataSndRcvInfo = ""; 
            int streamInt = config.getInteger("client.DEFAULT_STREAM", 0);
			GlobalLogger.instance().getSessionLogger().debug(TextEvent.Topic.PROTOCOL, "streamInt =" + streamInt);
			int streamUnint = Utils.convertLittleBigIndian(streamInt);        
			GlobalLogger.instance().getSessionLogger().debug(TextEvent.Topic.PROTOCOL, "streamUnint =" + streamUnint);			
            data.sndrcvinfo.sinfo_stream = (short) streamInt;
			logDataSndRcvInfo += "stream = " + data.sndrcvinfo.sinfo_stream;
			
            int ssnInt = config.getInteger("client.DEFAULT_SSN", 0);
            GlobalLogger.instance().getSessionLogger().debug(TextEvent.Topic.PROTOCOL, "ssnInt =" + ssnInt);
            int ssnUnint = Utils.convertLittleBigIndian(ssnInt);
            GlobalLogger.instance().getSessionLogger().debug(TextEvent.Topic.PROTOCOL, "ssnUnint =" + ssnUnint);
            data.sndrcvinfo.sinfo_ssn = (short) ssnInt;
			logDataSndRcvInfo += ", ssn = " + data.sndrcvinfo.sinfo_ssn;
			
            int ppidInt = config.getInteger("client.DEFAULT_PPID", 0);                
            GlobalLogger.instance().getSessionLogger().debug(TextEvent.Topic.PROTOCOL, "ppidInt =" + ppidInt);
            int ppidUnint = Utils.convertLittleBigIndian(ppidInt);            
            data.sndrcvinfo.sinfo_ppid = ppidUnint;
			logDataSndRcvInfo += ", ppid = " + data.sndrcvinfo.sinfo_ppid;

            int tsnInt = config.getInteger("client.DEFAULT_TSN", 0);                
            GlobalLogger.instance().getSessionLogger().debug(TextEvent.Topic.PROTOCOL, "tsnInt =" + tsnInt);
            int tsnUnint = Utils.convertLittleBigIndian(tsnInt);                        
            data.sndrcvinfo.sinfo_tsn = tsnUnint;
			logDataSndRcvInfo += ", tsn = " + data.sndrcvinfo.sinfo_tsn;

            int aidInt = config.getInteger("client.DEFAULT_AID", 0);
            GlobalLogger.instance().getSessionLogger().debug(TextEvent.Topic.PROTOCOL, "aidInt =" + aidInt);
            int aidUnint = Utils.convertLittleBigIndian(aidInt);            
            AssociationId assocId = new AssociationId(aidUnint);                 
            data.sndrcvinfo.sinfo_assoc_id = assocId;
            logDataSndRcvInfo += ", aid = " + data.sndrcvinfo.sinfo_assoc_id.hashCode();

            GlobalLogger.instance().getSessionLogger().debug(TextEvent.Topic.PROTOCOL, logDataSndRcvInfo);
            
			sctpSocket.send(data);
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
