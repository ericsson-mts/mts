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

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.tcp.StackTcp;

import dk.i1.sctp.OneToOneSCTPSocket;
import dk.i1.sctp.sctp_event_subscribe;
import java.net.Socket;
import java.net.SocketException;


/**
 *
 * @author nghezzaz
 */

public class SocketServerSctpListener extends Thread
{
	private OneToOneSCTPSocket sctpSocketserver;
    private ListenpointSctp listenpointSctp;

    /**
     * Creates a new instance of SocketServerSctpListener
     */
    public SocketServerSctpListener(ListenpointSctp listenpointSctp) throws ExecutionException
    {
    	int port = 0;

        // Set up Sctp connection
        try
        {
        	port = listenpointSctp.getPort();
            sctpSocketserver = new OneToOneSCTPSocket(port);
			sctp_event_subscribe ses = new sctp_event_subscribe();
			ses.sctp_data_io_event = true;
			ses.sctp_association_event = true;
			ses.sctp_shutdown_event=true;
//			sctpSocketserver.setInitMsg(im);
			sctpSocketserver.listen();
            this.listenpointSctp = listenpointSctp;
        }
        catch(Exception e)
        {
            throw new ExecutionException("Can't instantiate the SCTP SocketServerSctpListener on port " + port, e);
        }
    }

    @Override
	public void run()
	{
        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "SocketServerSctpListener started");
        
		try
		{
            boolean exception = false;
            while (!exception)
			{
                try
                {
                    GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "SocketServerSctpListener waiting for a connection on socket");
                    OneToOneSCTPSocket sctpSocket = sctpSocketserver.accept();
                    
                    GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "SocketServerSctpListener got a connection");

                    Channel channel = StackFactory.getStack(listenpointSctp.getProtocol()).buildChannelFromSocket(listenpointSctp, sctpSocket);
                    listenpointSctp.openChannel(channel);
                    
					// Create an empty message for transport connection actions (open or close) 
					// and on server side and dispatch it to the generic stack
					((StackSctp) StackFactory.getStack(StackFactory.PROTOCOL_SCTP)).receiveTransportMessage("INIT-ACK", channel, null);
                }
                catch(SocketException e)
                {
                    if (e.getMessage().contains("closed"))
                    {
                        exception = true;
                    }
                    else
                    {
                        GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, e, "Exception in SocketServerTcpListener");
                    }
                }
                catch(Exception e)
                {
                    GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, e, "Exception in SocketServerTcpListener");
                }
			}

		}
		catch(Exception e)
		{
			GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, e, "Exception in SocketServerSctpListener");
		}

		GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "SocketServerSctpListener stopped");	        
	}

	public void close()
	{
		//
		// Stop listening
		//
		try
		{
            //actually we are blocked here for an unknown reason
			sctpSocketserver.close();
		}
		catch(Exception e)
		{   
			GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Error while closing SocketServerSctpListener");
		}
	}
}
