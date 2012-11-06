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
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;

import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import dk.i1.sctp.OneToOneSCTPSocket;
import dk.i1.sctp.sctp_initmsg;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;

import dk.i1.sctp.AssociationId;
import java.net.Socket;

/**
 *
 * @author nghezzaz
 */

//  channel is called association in SCTP 
public class ChannelSctp extends Channel
{
    private SocketSctp socket;
    
    private Listenpoint listenpoint;

    private AssociationId aid;
    private Collection<InetAddress> localHostList;
    private Collection<InetAddress> remoteHostList;
    private sctp_initmsg initmsg;

    private long startTimestamp = 0;    
    
    /** Creates a new instance of ChannelSctp */
    public ChannelSctp(String name, String aLocalHost, String aLocalPort, String aRemoteHost, String aRemotePort, String aProtocol) throws Exception
    {
        super(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        this.socket = null;
        this.listenpoint = null;
        this.initmsg = new sctp_initmsg();
    }

    public ChannelSctp(String name, String aLocalHost, String aLocalPort, String aRemoteHost, String aRemotePort, String aProtocol, sctp_initmsg aIm) throws Exception
    {
        super(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        this.socket = null;
        this.listenpoint = null;
        this.initmsg = aIm;
    }

    public ChannelSctp(String name, String aLocalHost, String aLocalPort, String aRemoteHost, String aRemotePort, String aProtocol, SocketSctp aSocketSctp) throws Exception
    {
        super(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        this.socket = aSocketSctp;
        this.listenpoint = null;
        this.initmsg = new sctp_initmsg();
    }
    
    public ChannelSctp(ListenpointSctp aListenpoint, String aLocalHost, int aLocalPort, String aRemoteHost, int aRemotePort, String aProtocol) throws Exception
    {
        super(aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        this.socket = null;
        this.listenpoint = aListenpoint;
        this.initmsg = new sctp_initmsg();
    }

    public ChannelSctp(String name, Listenpoint aListenpoint, Socket aSocket) throws Exception
    {
		super(
        		name,
        		Utils.getLocalAddress().getHostAddress(),
        		Integer.toString(((OneToOneSCTPSocket)aSocket).getLocalInetPort()),
        		null,
        		null,
        		aListenpoint.getProtocol()
        );
		
		StatPool.beginStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_SCTP, getProtocol());
		this.startTimestamp = System.currentTimeMillis();

        this.listenpoint = aListenpoint;
        this.socket = new SocketSctp((OneToOneSCTPSocket) aSocket);
        this.socket.setChannelSctp(this);
        this.initmsg = new sctp_initmsg();
    }

    public SocketSctp getSocketSctp()
    {
        return socket;
    }

    public AssociationId getAssociationId()
    {
        return aid;
    }

    public void setAssociationId(AssociationId aid)
    {
        this.aid = aid;
    }

    /** Send a Msg to Channel */
    public synchronized boolean sendMessage(Msg msg) throws Exception
    {
        if (socket == null)
        {
            throw new ExecutionException("SocketSctp is null, has the connection been opened ?");
        }
        msg.setChannel(this);
        socket.send(msg);
        return true;
    }

    public boolean open() throws Exception
    {
        if (socket == null)
        {
    		StatPool.beginStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_SCTP, getProtocol());
    		this.startTimestamp = System.currentTimeMillis();
        	
			InetAddress localAddr = InetAddress.getByName(getLocalHost());
			// TODO Take localAddr into account
            OneToOneSCTPSocket sctpSocket = new OneToOneSCTPSocket();
            sctpSocket.bind(getLocalPort()); 
    		
    		Config config = StackFactory.getStack(StackFactory.PROTOCOL_SCTP).getConfig();
    		String ostreams = config.getString("connect.NUM_OSTREAMS");
    		if(ostreams != null)
    		{
    			this.initmsg.sinit_num_ostreams = (short) Integer.parseInt(ostreams);
    			GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "initmsg.sinit_num_ostreams=", initmsg.sinit_num_ostreams);
    		}
    		String instreams = config.getString("connect.MAX_INSTREAMS");
    		if(instreams != null)
    		{
    			this.initmsg.sinit_max_instreams = (short) Integer.parseInt(instreams);
    			GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "initmsg.sinit_max_instreams=", initmsg.sinit_max_instreams);
    		}
    		String attempts = config.getString("connect.MAX_ATTEMPS");
    		if(attempts != null)
    		{
    			this.initmsg.sinit_max_attempts = (short) Integer.parseInt(attempts);
    			GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "initmsg.sinit_max_attempts=", initmsg.sinit_max_attempts);    			
    		}
    		String timeo = config.getString("connect.MAX_INIT_TIMEO");
    		if(timeo != null)
    		{
    			this.initmsg.sinit_max_init_timeo= (short) Integer.parseInt(timeo);
    			GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "initmsg.sinit_max_init_timeo=", initmsg.sinit_max_init_timeo);    			
    		}
    		sctpSocket.setInitMsg(this.initmsg);
            
            InetSocketAddress remoteSocketAddress = new InetSocketAddress(getRemoteHost(), getRemotePort());
            sctpSocket.connect(remoteSocketAddress);
        
            this.setLocalPort(sctpSocket.getLocalInetPort());
            // TODO Take socket LocalAddress into account
            // this.setLocalHost(socket.getLocalAddress().getHostAddress());

            socket = new SocketSctp(sctpSocket);
        }
        
        socket.setChannelSctp(this);
        socket.setDaemon(true);
        socket.start();        

        return true;
    }

    public boolean close()
    {	
    	if (socket != null)
    	{
    		StatPool.endStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_SCTP, getProtocol(), startTimestamp);
    		
    		socket.shutdown();
	        socket = null;
    	}
        return true;
    }

    /** Get the transport protocol of this message */
    public String getTransport() 
    {
    	return StackFactory.PROTOCOL_SCTP;
    }

	public Listenpoint getListenpointSctp() {
		return listenpoint;
	}

}



