/* 
 * Copyright 2017 Ericsson http://www.ericsson.com
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

package com.devoteam.srit.xmlloader.sctp.sunnio;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.Set;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.hybridnio.IOHandler;
import com.devoteam.srit.xmlloader.core.hybridnio.IOReactor;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.*;

import com.devoteam.srit.xmlloader.sctp.*;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;
import com.sun.nio.sctp.SctpStandardSocketOptions;

/**
 * @author emicpou
 *
 */
public class ListenpointSunNioSctp extends ListenpointSctp implements IOHandler
{
    
	/**
	 * implementation listen point
	 */
    private SctpServerChannel sctpServerChannel;
	
	/**
	 * 
	 */
	private SelectionKey selectionKey;

    /**
     * default constructor
     */
    public ListenpointSunNioSctp(Stack stack) throws Exception
    {
        super(stack);
    }

    
    //---------------------------------------------------------------------
    // methods for the transport
    //---------------------------------------------------------------------

    /**
     * Create a listenpoint to each Stack
     * open listen point
     */
    @Override
    public boolean create(String protocol) throws Exception
    {
		//this.startTimestamp = System.currentTimeMillis();
    	
        if (!super.create(protocol))
        {
            return false;
        }

        // Set up sctp server channel
        try
        {
            int port = this.getPort();

            InetAddress localInetAddr = null;
            if (null != this.getHost())
            {
                localInetAddr = InetAddress.getByName(this.getHost());
            }
            else
            {
                localInetAddr = InetAddress.getByName("0.0.0.0");
            }
            InetSocketAddress local = new InetSocketAddress(localInetAddr, port);
            
            this.sctpServerChannel = SctpServerChannel.open();
            
            //binds the channel's socket to a local address and configures the socket to listen for associations
            this.sctpServerChannel.bind(local);
            
            // @todo multihoming
            // Adds the given address to the bound addresses for the channel's socket. 
            // this.sctpServerChannel.bindAddress(otherLocalAddress);
            // Set<SocketAddress> allLocalAddresses = this.sctpServerChannel.getAllLocalAddresses();
            
            /// @todo set options
            // http://docs.oracle.com/javase/8/docs/jre/api/nio/sctp/spec/com/sun/nio/sctp/SctpSocketOption.html
            // this.sctpServerChannel.setOption( ... );
	        
            //max streams count allowed
            {
		        int maxInStreams = Short.toUnsignedInt( (short)-1 );
		        int maxOutStreams = Short.toUnsignedInt( (short)-1 );
		        SctpStandardSocketOptions.InitMaxStreams initMaxStreamsSctpSocketOption =  SctpStandardSocketOptions.InitMaxStreams.create(maxInStreams, maxOutStreams);
		        assert(initMaxStreamsSctpSocketOption!=null);	            
		      	this.sctpServerChannel.setOption(SctpStandardSocketOptions.SCTP_INIT_MAXSTREAMS,initMaxStreamsSctpSocketOption );
	        }
            
	        //register our sctpServerChannel to the IOReactor
	        //we want to be notified when a new association is available on the listen point
            this.selectionKey = IOReactor.instance().registerChannel(this.sctpServerChannel, SelectionKey.OP_ACCEPT, this);
        }
        catch (Exception exception)
        {
        	this.cleanup();
            throw exception;
        }

        return true;
    }

    /**
     * internal ressources cleanup
     */
    private void cleanup(){
    	if( this.sctpServerChannel!=null ){
	    	try{
				if( this.sctpServerChannel.isOpen() ){
					this.sctpServerChannel.close();
				}
	    	}catch(Exception exception){
	            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, exception, "Error while cleaning sctpServerChannel");
	    	}
	    	finally{
	    		this.sctpServerChannel = null;
	    	}
		}
    	if( this.selectionKey!=null ){
	    	try{
				this.selectionKey.cancel();
	    	}catch(Exception exception){
	            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, exception, "Error while cleaning selectionKey");
	    	}
	    	finally{
	    		this.selectionKey = null;
	    	}
    	}
    }

    @Override
    public boolean remove()
    {    		
        if (!super.remove())
        {
            return false;
        }
        
        try
        {
        	synchronized (this)
            {
        		this.cleanup();
            }
        }
        catch (Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Error while closing Sctp listener's socket");
        }


        return true;
    }
    
    /**
     * 
     */
    private void pollNewAssociation(){
        try
        {
	   		//normalement cette methode est appellee dans le handler acceptReady
	   		//il devrait donc y avoir une nouvelle association disponible
	   		if( !this.selectionKey.isAcceptable() ){
	   			return;
	   		}

	   		SctpChannel sctpChannel = this.sctpServerChannel.accept();
            if( sctpChannel==null ){
            	return;
            }

            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, ""+this.getName()+":ListenpointSunNioSctp#pollNewAssociation");

            //the Stack.buildChannelFromSocket API does not match the sun.nio.sctp.SctpChannel abstraction who has no socket accessor
            //
            //String listenpointProtocol = this.getProtocol();
            //Stack listenpointStack = StackFactory.getStack(listenpointProtocol);
            //Channel channelSctp = listenpointStack.buildChannelFromSocket(this.listenpoint, sctpChannel.socket());
        	ChannelSunNioSctp channelSctp = new ChannelSunNioSctp("Channel #" + Stack.nextTransactionId(),this,sctpChannel );

            this.openChannel(channelSctp);
            
			// Create an empty message for transport connection actions (open or close) 
			// and on server side and dispatch it to the generic stack
            StackSctp stackSctp = (StackSctp)StackFactory.getStack(StackFactory.PROTOCOL_SCTP);

            stackSctp.receiveTransportMessage("INIT-ACK", channelSctp, null);
        }
        catch (Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, e, "Exception in SocketServerSctpListener");
        }    
    }

    @Override
    public synchronized Channel prepareChannel(Msg msg, String remoteHost, int remotePort, String transport) throws Exception
    {
        Channel channel;

        String keySocket = remoteHost + ":" + remotePort;

        if (!this.existsChannel(keySocket))
        {
            channel = new ChannelSunNioSctp(this, getHost(), 0, remoteHost, remotePort, this.getProtocol());
            this.openChannel(channel);
        }
        else
        {
            channel = this.getChannel(keySocket);
        }

        return channel;
    }

    @Override
    public synchronized boolean sendMessage(Msg msg, String remoteHost, int remotePort, String transport) throws Exception
    {
        assert(false):"this code path is not tested";
		return prepareChannel(msg, remoteHost, remotePort, transport).sendMessage(msg);
    }

    @Override
    protected ChannelSctp createChannelSctp(String aLocalHost, int aLocalPort, String aRemoteHost, int aRemotePort, String aProtocol) throws Exception
    {
        assert(false):"this code path is not tested";
    	return new ChannelSunNioSctp(this,aLocalHost,aLocalPort,aRemoteHost,aRemotePort,aProtocol);
    }
 
    /**
	 * @param selectionKey
	 * @param selectableChannel (should be same as selectionKey.channel() ?)
	 */
    @Override
    public void onIorInit(SelectionKey selectionKey, SelectableChannel selectableChannel){
    	assert( this.selectionKey==null );
    	assert( this.sctpServerChannel==selectableChannel );
    }

	/**
     * triggered when readable (there is data to read)
	 */
    @Override
    public void onIorInputReady(){
        throw new UnsupportedOperationException("Not supported yet.");
    }

	/**
     * triggered when writable (it is possible to write data)
	 */
    @Override
    public void onIorOutputReady(){
        throw new UnsupportedOperationException("Not supported yet.");
    }

	/**
     * triggered on connect event (channel has either finished, or failed to finish, its socket-connection operation)
	 */
    @Override
    public void onIorConnectReady(){
     }

	/**
	 * triggered on accept event (channel is ready to accept a new socket connection)
	 */
    public void onIorAcceptReady(){
        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "ListenpointSunNioSctp acceptReady");
    	this.pollNewAssociation();
    }
    
}
