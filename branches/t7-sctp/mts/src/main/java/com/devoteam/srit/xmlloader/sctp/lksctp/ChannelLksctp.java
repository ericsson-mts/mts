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

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.tcp.bio.ChannelTcpBIO;
import com.devoteam.srit.xmlloader.tcp.nio.ChannelTcpNIO;

import com.devoteam.srit.xmlloader.sctp.ChannelSctp;
import com.devoteam.srit.xmlloader.sctp.ListenpointSctp;

import dk.i1.sctp.OneToManySCTPSocket;
import dk.i1.sctp.OneToOneSCTPSocket;
import dk.i1.sctp.SCTPSocket;
import dk.i1.sctp.sctp_initmsg;
import dk.i1.sctp.sctp_paddrparams;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.List;

import dk.i1.sctp.AssociationId;

import java.net.Socket;

import org.dom4j.Element;

/**
 *
 * @author nghezzaz
 */

//  channel is called association in SCTP 
public class ChannelLksctp extends ChannelSctp
{
    private SocketLksctp socket;
    
    private AssociationId aid;
    private sctp_initmsg initmsg;

    private long startTimestamp = 0;    
    
    /** Creates a new instance of Channel*/
    public ChannelLksctp(Stack stack)
    {
    	super(stack);
    }

    public ChannelLksctp(Listenpoint aListenpoint, String aLocalHost, int aLocalPort, String aRemoteHost, int aRemotePort, String aProtocol) throws Exception
    {
        super(aListenpoint, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        this.socket = null;
        this.initmsg = new sctp_initmsg();
    }

    public ChannelLksctp(String name, Listenpoint aListenpoint, Socket aSocket) throws Exception
    {
		super(  aListenpoint,
        		name,
        		((SCTPSocket)aSocket).getLocalAddress().getHostAddress(),
        		Integer.toString(((SCTPSocket)aSocket).getLocalInetPort()),
        		null,
        		null,
        		aListenpoint.getProtocol()
        );

		StatPool.beginStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_SCTP, getProtocol());
		this.startTimestamp = System.currentTimeMillis();

        this.socket = new SocketLksctp((SCTPSocket) aSocket);
        this.socket.setChannelSctp(this);
        this.initmsg = new sctp_initmsg();
    }
    
    public SocketLksctp getSocketLksctp()
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

    /** Open a Channel */
    @Override
    public boolean open() throws Exception
    {
    	if( !super.open() ){
    		return false;
    	}
    	
        if (socket == null)
        {
    		StatPool.beginStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_SCTP, getProtocol());
    		this.startTimestamp = System.currentTimeMillis();
            
			//InetAddress localAddr = InetAddress.getByName(getLocalHost());
			// TODO Take localAddr into account
            SCTPSocket sctpSocket = new OneToOneSCTPSocket();
            int intLocalPort = getLocalPort(); 
            sctpSocket.bind(intLocalPort);
            if (this.initmsg != null)
            {
            	sctpSocket.setInitMsg(this.initmsg);
            }
                        
            String strRemoteHost = getRemoteHost();
            if (strRemoteHost == null || "0.0.0.0".equals(strRemoteHost))
            {
            	strRemoteHost = InetAddress.getByName(strRemoteHost).getCanonicalHostName();
            }
            int intRemotePort = getRemotePort();            
            InetSocketAddress remoteSocketAddress = new InetSocketAddress(strRemoteHost, intRemotePort);
            sctpSocket.connect(remoteSocketAddress);
        
            this.localPort = sctpSocket.getLocalInetPort();
            // TODO Take socket LocalAddress into account
            // this.setLocalHost(socket.getLocalAddress().getHostAddress());
            
            /* for multihoming but not used : we keep it nethertheless
            if (localHost != null)
            {
	            for (int i = 0; i < this.localHost.length; i++)
	            {
	            	try
	            	{
	            		System.out.println("Bind to " + this.localHost[i] +  ":" + localPort);	        
		                sctp_paddrparams spp = new sctp_paddrparams();
		                spp.spp_assoc_id = new AssociationIdSctp(101);
		                System.out.println("spp.spp_assoc_id.hashCode()" + spp.spp_assoc_id.hashCode());
		                InetSocketAddress localSocketAddress = new InetSocketAddress(this.localHost[i], localPort);
		                spp.spp_address = localSocketAddress;
		                System.out.println("spp.spp_address:" + spp.spp_address);
		                spp.spp_hbinterval = 0;
		    			spp.spp_flags = sctp_paddrparams.SPP_HB_DISABLE | sctp_paddrparams.SPP_PMTUD_DISABLE | sctp_paddrparams.SPP_SACKDELAY_DISABLE;
		    			spp.spp_pathmaxrxt = 0;
		    			spp.spp_pathmtu = 0;
		    			spp.spp_sackdelay = 0;
		    			spp.spp_ipv6_flowlabel = 0;
		    			spp.spp_ipv4_tos = 0;
		    			//sctpSocket.setPeerParameters(spp);
		    			
		    			InetAddress inet = InetAddress.getByName(this.localHost[i]);
		            	sctpSocket.bind(inet, localPort);
		            	//sctpSocket.listen();
		            	break;
	            	}
	            	catch (Exception e)
	            	{
	            		System.out.println(e);
		            	GlobalLogger.instance().getApplicationLogger().warn(Topic.PROTOCOL, "Exception : so we try the next peer :", e);
	            	}
	            }
            }
            else
            {
            	sctpSocket.bind(getLocalPort());
            	//sctpSocket.listen();
            }   
            */       

            /* for multihoming but not used : we keep it nethertheless
            int remotePort = getRemotePort();
            if (remoteHost != null)
            {
            	boolean connected = false;
            	Exception lastException;
	            for (int i = 0; i < 1; i++)
	            {
	            	GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Connect to ", this.remoteHost, ":", remotePort);
	            	try
	            	{		            
		                InetSocketAddress remoteSocketAddress = new InetSocketAddress(this.remoteHost, remotePort);            
		                sctpSocket.connect(remoteSocketAddress);
		                connected = true;
		            	break;
	            	}
	            	catch (Exception e)
	            	{
	            		lastException = e;
		            	GlobalLogger.instance().getApplicationLogger().warn(Topic.PROTOCOL, "Exception : so we try the next peer :", e); 	            			            
	            	}
	            	if (!connected)
	            	{
	            		throw new ExecutionException("Can not connect to all remote destinations : last exception " + lastException);
	            	}
	            }
            }
			*/
            socket = new SocketLksctp(sctpSocket);
        }
        
        socket.setChannelSctp(this);
        socket.setDaemon(true);
        socket.start();        

        return true;
    }

    /** Close a Channel */
    @Override
    public boolean close()
    {	
    	if( !super.close() ){
    		return false;
    	}

    	if (socket != null)
    	{
    		StatPool.endStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_SCTP, getProtocol(), startTimestamp);
    		
    		socket.shutdown();
	        socket = null;
    	}
        return true;
    }

    
    /** Send a Msg to Channel */
    @Override
    public synchronized boolean sendMessage(Msg msg) throws Exception
    {
    	if( !super.sendMessage(msg) ){
    		return false;
    	}
    	
        if (socket == null)
        {
            throw new ExecutionException("SocketSctp is null, has the connection been opened ?");
        }
        msg.setChannel(this);
        socket.send(msg);
        return true;
    }
    
    //---------------------------------------------------------------------
    // methods for the XML display / parsing
    //---------------------------------------------------------------------
	
    /** 
     * Parse the message from XML element 
     */
    @Override
    public void parseFromXml(Element root, Runner runner, String protocol) throws Exception
    {
    	super.parseFromXml(root, runner, protocol);
    	
    	this.initmsg = new sctp_initmsg();
    	
		// initialize from the configuration file
    	this.initmsg.sinit_num_ostreams = (short) this.stack.getConfig().getInteger("connect.NUM_OSTREAMS");
    	this.initmsg.sinit_max_instreams = (short) this.stack.getConfig().getInteger("connect.MAX_INSTREAMS");
    	this.initmsg.sinit_max_attempts = (short)  this.stack.getConfig().getInteger("connect.MAX_ATTEMPTS");
    	this.initmsg.sinit_max_init_timeo= (short) this.stack.getConfig().getInteger("connect.MAX_INIT_TIMEO");
		
		// Parse the XML file
		List<Element> sctpElements = root.elements("sctp");
		if (sctpElements != null && sctpElements.size() > 0)
		{
			Element sctpElement = sctpElements.get(0);
	        
			String num_ostreams = sctpElement.attributeValue("num_ostreams");
			if (num_ostreams != null)
			{
				this.initmsg.sinit_num_ostreams = (short) Integer.parseInt(num_ostreams);	    	
			}
	
			String max_instreams = sctpElement.attributeValue("max_instreams");
	    	if (max_instreams != null)
	    	{
	    		this.initmsg.sinit_max_instreams = (short) Integer.parseInt(max_instreams);
	    	}
			
	    	String max_attempts = sctpElement.attributeValue("max_attempts");
			if (max_attempts != null)
			{
				this.initmsg.sinit_max_attempts = (short) Integer.parseInt(max_attempts);    			
			}
			
			String max_init_timeo = sctpElement.attributeValue("max_initTimeo");
			if (max_init_timeo != null)
			{
				this.initmsg.sinit_max_init_timeo= (short) Integer.parseInt(max_init_timeo);    			
			}
			
			// log datas
			GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "initmsg.sinit_num_ostreams=", this.initmsg.sinit_num_ostreams);			
    		GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "initmsg.sinit_max_instreams=", this.initmsg.sinit_max_instreams);
			GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "initmsg.sinit_max_attempts=", initmsg.sinit_max_attempts);
			GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "initmsg.sinit_max_init_timeo=", initmsg.sinit_max_init_timeo);
		}
    }
    
    //---------------------------------------------------------------------
    // methods for the XML display / parsing
    //---------------------------------------------------------------------

    /** 
     * Returns the string description of the message. Used for logging as DEBUG level 
     */
    @Override
    public String toString()
    {
        String ret = super.toString();
        /*
        if (this.socket != null && this.socket.getSctpSocket() != null)
        {
        	InetAddress inet = this.socket.getSctpSocket().getInetAddress();
        	if (inet != null)
        	{
		        ret += " inetAddress=\"" + inet.getHostAddress() + "\"";
        	}
    		InetAddress local = this.socket.getSctpSocket().getLocalAddress();
        	if (local != null)
        	{
	        	ret += " localAddress=\"" + local.getHostAddress() + "\"";
        	}
	        try 
	        {
	        	// crash de la jvm
	        	//String localAddresses = Utils.TableInetToString(this.socket.getSctpSocket().getLocalInetAddresses());
	        	//ret += " localAddresses=\"" + localAddresses + "\"";
	        }
	        catch (Exception e)
	        {
	        	// nothing to do
	        }
	        try 
	        {
	        	//int localInetPort = this.socket.getSctpSocket().getLocalInetPort();
		        //if (localInetPort > 0)
		        //{
		        	//ret += " localInetPort=\"" + localInetPort + "\"";
		        //}
	        }
	        catch (Exception e)
	        {
	        	// nothing to do
	        }
	        localPort = this.socket.getSctpSocket().getLocalPort();
	        if (localPort > 0)
	        {
	        	ret += " localPort=\"" + localPort + "\"";
	        }
	        int port = this.socket.getSctpSocket().getPort();
	        if (port > 0)
	        {
	        	ret += " port=\"" + port + "\"";
	        }
	        SocketAddress localSocket= this.socket.getSctpSocket().getLocalSocketAddress();
	        if (localSocket != null)
	        {
	        	ret += " localSocketAddress=\"" + localSocket + "\"";
	        }
	        SocketAddress remoteSocket = this.socket.getSctpSocket().getRemoteSocketAddress();
	        if (remoteSocket != null)
	        {
	        	ret += " remoteSocketAddress=\"" + remoteSocket + "\"";
	        }
	     }
	     */
        ret += ">\n";
        
		if (this.initmsg != null && (
			this.initmsg.sinit_num_ostreams != 0 ||
			this.initmsg.sinit_max_instreams != 0 ||
			this.initmsg.sinit_max_attempts != 0 ||
			this.initmsg.sinit_max_init_timeo != 0)
			)
		{
			ret += "<sctp";
			int numOutstreams = this.initmsg.sinit_num_ostreams & 0xffff;
			ret += " num_ostreams=\"" + numOutstreams + "\"";
			int maxInstreams = this.initmsg.sinit_max_instreams & 0xffff;
			ret += " max_instreams=\"" + maxInstreams + "\"";
			int maxAttempts = this.initmsg.sinit_max_attempts & 0xffff;
			ret += " max_attempts=\"" + maxAttempts+ "\"";
			int maxInitTimeo = this.initmsg.sinit_max_init_timeo & 0xffff;
			ret += " max_initTimeo=\"" + maxInitTimeo+ "\"";
			ret += "/>\n";
		}	
	    return ret;
    }
        
    //------------------------------------------------------
    // method for the "setFromMessage" <parameter> operation
    //------------------------------------------------------
    
    /** 
     * Get a parameter from the message 
     */
    @Override
    public Parameter getParameter(String path) throws Exception
    {
		Parameter var = super.getParameter(path);
		if (var != null)
		{
			return var;
		}

		var = new Parameter();
        path = path.trim();
        String[] params = Utils.splitPath(path);

        if(params[1].equalsIgnoreCase("peerHosts")) 
        {
        	SCTPSocket sctpSocket = this.getSocketLksctp().getSCTPSocket();
			if (sctpSocket != null)
			{						
				Collection<InetAddress> col = sctpSocket.getPeerInetAddresses(this.aid);
				for (InetAddress ia : col)
				{	
					var.add(ia.getHostAddress());						
				}
			}
        }
        else if(params[1].equalsIgnoreCase("peerPort")) 
        {
        	SCTPSocket sctpSocket = this.getSocketLksctp().getSCTPSocket();
			if (sctpSocket != null)
			{	
				int port = sctpSocket.getPeerInetPort(this.aid);
				var.add(Integer.toString(port));
			}
        }    
        else
        {
        	Parameter.throwBadPathKeywordException(path);
        }
        return var; 
    }

}



