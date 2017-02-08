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

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.hybridnio.HybridSocket;
import com.devoteam.srit.xmlloader.core.hybridnio.IOHandler;
import com.devoteam.srit.xmlloader.core.hybridnio.IOReactor;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.*;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.sctp.*;
import com.devoteam.srit.xmlloader.sctp.lksctp.DataLksctp;
import com.devoteam.srit.xmlloader.sctp.lksctp.MsgLksctp;
import com.devoteam.srit.xmlloader.sctp.lksctp.StackLksctp;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.*;

import com.sun.nio.sctp.*;

/**
 * @author emicpou
 *
 */
public class ChannelSunNioSctp extends ChannelSctp implements IOHandler
{
    /**
     * sctp msg receive max size
     */
    public static final int MTU = Config.getConfigByName("sctp.properties").getInteger("DEFAULT_BUFFER_LENGHT", 1500);
    public static final int MAX_RECEIVE_BUFFER_LENGTH = Config.getConfigByName("sctp.properties").getInteger("MAX_RECEIVE_BUFFER_LENGTH", 64*1024);

    /**
	 * 
	 */
	private SctpChannel sctpChannel;
	
	/**
	 * 
	 */
	private SelectionKey selectionKey;
	
	/**
	 * 
	 */
    private long startTimestamp = 0;    

	/**
	 * non-blocking send synchronisation
	 */
    private final Lock outputReadyLock = new ReentrantLock();

	/**
	 * non-blocking send synchronisation
	 * the condition is signaled by the IOReactor thread when it is possible to write to the sctpChannel
	 * the send blocking method thread should wait on the condition until writes to the sctpChannel succeed
	 */
    private final Condition outputReadyCondition = outputReadyLock.newCondition();
     
	/**
	 * Creates a new instance
	 */
    public ChannelSunNioSctp(Stack stack)
    {
    	super(stack);
		GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, ""+this.getName()+":ChannelSunNioSctp#ctor");
    }
    
	/**
	 * Creates a new instance
	 */
    public ChannelSunNioSctp(String name, String aLocalHost, String aLocalPort, String aRemoteHost, String aRemotePort, String aProtocol) throws Exception
    {
        super(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
		GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, ""+this.getName()+":ChannelSunNioSctp#ctor");
        assert(false):"this code path is not tested";
    }

    /**
	 * Creates a new instance
	 * 
     * @param name
     * @param sctpListenpoint
     * @param sctpChannel returned by the SctpServerChannel.accept() method (server side)
     * @throws Exception
     */
    public ChannelSunNioSctp(String name, ListenpointSctp listenpointSctp,SctpChannel sctpChannel) throws Exception
    {
		super( name,listenpointSctp );

		//initialize remote infos attributes
		{
			Set<SocketAddress> remoteAddresses = sctpChannel.getRemoteAddresses();
			assert(!remoteAddresses.isEmpty());
			SocketAddress firstSocketAddress = remoteAddresses.iterator().next();
			assert(firstSocketAddress instanceof InetSocketAddress);
			InetSocketAddress firstInetSocketAddress = (InetSocketAddress)firstSocketAddress;
			
			this.setRemoteHost(firstInetSocketAddress.getAddress().getHostAddress());
			this.setRemotePort(firstInetSocketAddress.getPort());
		}
		
		this.sctpChannel = sctpChannel;
		//some inits here

		GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, ""+this.getName()+":ChannelSunNioSctp#ctor");
    }

    /**
	 * Creates a new instance
     */
    public ChannelSunNioSctp(Listenpoint aListenpoint, String aLocalHost, int aLocalPort, String aRemoteHost, int aRemotePort, String aProtocol) throws Exception
    {
        super(aListenpoint, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        this.listenpointSctp = (ListenpointSctp)aListenpoint;
    }
    
    /**
     * 
     * @return the SctpChannel implementation member
     */
    //@Nullable
    public SctpChannel getSctpChannel(){
    	return this.sctpChannel;
    }

    /**
     *  
     */
    @Override
    public String toXml_PeerAddresses(AssociationSctp associationSctp) throws Exception{
    	// TODO implementer
    	return "<PeerAddresses />";
    }
    
    //---------------------------------------------------------------------
    // methods for the transport
    //---------------------------------------------------------------------

    /** Open a channel */
    @Override
    public boolean open() throws Exception
    {
		GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, ""+this.getName()+":ChannelSunNioSctp#open");

		if( !super.open() ){
    		return false;
    	}
        
    	assert(this.selectionKey==null):"channel should be closed";
    	
    	try{
        	//client side : we need to connect to the remote server
        	if(this.sctpChannel==null){
		
		    	//ensure a channel config is available
        		ChannelConfigSctp configSctp = this.configSctp;
				if (configSctp == null){
					Stack sctpStack = StackFactory.getStack(StackFactory.PROTOCOL_SCTP);
			        Config stackConfig = sctpStack.getConfig();
					configSctp = new ChannelConfigSctp();
					configSctp.setFromStackConfig(stackConfig);
				}
		    	
				//create
		        assert(this.sctpChannel==null);	            
		   		this.sctpChannel = SctpChannel.open();
		        assert(this.sctpChannel!=null);	            

		        //bind
		        {
		        	List<InetAddress> localAddresses = this.getLocalAddresses();
		        	Iterator<InetAddress> localAddressesIterator = localAddresses.iterator();
		        	
			        int localPort = this.getLocalPort();			        
		        	SocketAddress localSocketAddress = null;
		        	if( localAddressesIterator.hasNext() ){
		        		InetAddress localAddress = localAddressesIterator.next();
		        		localSocketAddress = new InetSocketAddress(localAddress,localPort);
		        	}else{
		        		localSocketAddress = new InetSocketAddress(localPort);		        		
		        	}
		            this.sctpChannel.bind(localSocketAddress);
		            
		            //multi-homing
		            while( localAddressesIterator.hasNext() ){
		            	InetAddress localAddress = localAddressesIterator.next();
		        		this.sctpChannel.bindAddress(localAddress); 
		        	}
		        }
		        
		        //options
		        if(configSctp!=null){
			        int maxInStreams = Short.toUnsignedInt(configSctp.max_instreams);
			        int maxOutStreams = Short.toUnsignedInt(configSctp.num_ostreams);
			        SctpStandardSocketOptions.InitMaxStreams initMaxStreamsSctpSocketOption =  SctpStandardSocketOptions.InitMaxStreams.create(maxInStreams, maxOutStreams);
			        assert(initMaxStreamsSctpSocketOption!=null);	            
			      	this.sctpChannel.setOption(SctpStandardSocketOptions.SCTP_INIT_MAXSTREAMS,initMaxStreamsSctpSocketOption );
		        }
		        
		        //connect
		        {
			        String strRemoteHost = this.getRemoteHost();
			        if (strRemoteHost == null || "0.0.0.0".equals(strRemoteHost))
			        {
			        	strRemoteHost = InetAddress.getByName(strRemoteHost).getCanonicalHostName();
			        }
			        int intRemotePort = getRemotePort();            
			        InetSocketAddress remoteSocketAddress = new InetSocketAddress(strRemoteHost, intRemotePort);
			        
			        boolean connected = this.sctpChannel.connect(remoteSocketAddress);
			            
			        if(!connected){
			        	this.sctpChannel.configureBlocking(true);
			        	connected = this.sctpChannel.finishConnect();
			        	this.sctpChannel.configureBlocking(false);
			        }
			        assert(connected);
		        }
        	}
	        assert(this.sctpChannel!=null);	            

	        //register our sctpChannel to the IOReactor
	        //differs the IOReactor notifications settings
	        assert(this.selectionKey==null);
			this.selectionKey = IOReactor.instance().registerChannel(this.sctpChannel,0,this);
			assert(this.selectionKey!=null);
	
	        // read all properties for the TCP socket 
			//Config.getConfigForSTCPSocket(hybridSocket, false);
	
	        //this.localPort = hybridSocket.getLocalPort();
	        //this.localHost = hybridSocket.getLocalAddress().getHostAddress();
	
			//begin statistics only when the channel has been sucessfully opened
			this.startTimestamp = System.currentTimeMillis();
			StatPool.beginStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_SCTP, getProtocol());
			
			//we are ready to be notified by the IOReactor thread when a read operation is possible
			{
				int interestOps = this.selectionKey.interestOps();
				interestOps |= SelectionKey.OP_READ;
				this.selectionKey.interestOps( interestOps );
			}			
	    }
	    catch(Exception exception){
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, exception, ""+this.getName()+" : error while opening sctp channel");
            this.cleanup();
            super.close();
			throw exception;
			//return false;
		}
        return true;
    }

    /** Close a channel */
    @Override
    public boolean close()
    {
		GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, ""+this.getName()+":ChannelSunNioSctp#close");

		if( !super.close() ){
    		return false;
    	}
		
		//ends statistics only if the channel has been opened successfully previously
		if( this.selectionKey!=null ){
			StatPool.endStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_SCTP, this.getProtocol(),this.startTimestamp);
		}
		
    	this.cleanup();
    	return true;
    }

    private void cleanup(){
		GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, ""+this.getName()+":ChannelSunNioSctp#cleanup");

		if( this.sctpChannel!=null ){
	    	try{
				if( this.sctpChannel.isOpen() ){
					this.sctpChannel.close();
				}
	    	}catch(Exception exception){
	            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, exception, ""+this.getName()+" : error while cleaning sctpChannel");
	    	}
	    	finally{
	    		this.sctpChannel = null;
	    	}
		}
    	if( this.selectionKey!=null ){
	    	try{
				this.selectionKey.cancel();
	    	}catch(Exception exception){
	            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, exception, ""+this.getName()+" : error while cleaning selectionKey");
	    	}
	    	finally{
	    		this.selectionKey = null;
	    	}
    	}
    }
    
    /**
     * poll the sctpChannel
     * if a datagram is available, unserialize and dispatch a message
     */
   	private void pollReceivedData(){
    	try{
        	assert(this.selectionKey!=null):"the channel must be opened";

        	//normalement cette methode est appellee dans le handler inputReady
	   		//il devrait donc y avoir des données a lire
	   		if( !this.selectionKey.isReadable() ){
	   			return;
	   		}
	   		
	    	/*
	    	 * allocation d'un ByteBuffer a chaque lecture
	    	 *   un buffer est theoriquement reutilisable apres consommation en faisant un buffer.clear()
	    	 *   
	    	 * taille du buffer basée sur le MTU
	    	 *   https://www.ietf.org/mail-archive/web/sigtran/current/msg08100.html
	    	 */
	    	ByteBuffer payloadByteBuffer = ByteBuffer.allocate(ChannelSunNioSctp.MAX_RECEIVE_BUFFER_LENGTH);
	    	
	    	MessageInfo messageInfo = this.sctpChannel.receive(payloadByteBuffer,null,null);
	    	if( messageInfo==null ){
	    		GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, ""+this.getName()+":ChannelSunNioSctp#pollReceivedData"+" readable, but receive failed");
	    		return;
	    	}
	    	
	    	if( messageInfo.bytes()==-1 ){
	    		GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, ""+this.getName()+":ChannelSunNioSctp#pollReceivedData"+" end of stream");

	    		//attempt to free ressources?
	    		try{
					this.sctpChannel.close();
					this.selectionKey.cancel();
		    	}catch(Exception exception){
		            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, exception, ""+this.getName()+" : error while cleaning sctpChannel");
		    	}

	    		// Create an empty message for transport connection actions (open or close) 
				// and on server side and dispatch it to the generic stack
	    		StackSctp stackSctp = ((StackSctp) StackFactory.getStack(StackFactory.PROTOCOL_SCTP));
				stackSctp.receiveTransportMessage("ABORT-ACK", this, null);
	    	}
	    	else{
		    		
		    	assert( messageInfo.bytes()>=0 );
		    	assert( messageInfo.isComplete() );
		    	payloadByteBuffer.flip();
		    	int payloadLength = payloadByteBuffer.limit()-payloadByteBuffer.position();
		        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, ""+this.getName()+":ChannelSunNioSctp#pollReceivedData"+" RECEIVE "+payloadLength+" bytes");                       
		    	//assert(payloadLength<=ChannelSunNioSctp.MTU);
		
		        DataSunNioSctp dataSctp = new DataSunNioSctp( payloadByteBuffer,messageInfo );
		        
		        //unserialize
		        Stack channelStack = StackFactory.getStack(this.getProtocol());
		        assert(channelStack!=null);
		        
		        Msg msg = channelStack.readFromSCTPData(dataSctp);
		    	if (msg==null){
		    		return;
		    	}
		
		    	msg.setChannel(this);
		        msg.setListenpoint(this.getListenpointSctp());
		
		        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, ""+this.getName()+":ChannelSunNioSctp#pollReceivedData"+" RECEIVE the SCTP message :\n", msg);                       
		        
		        //dispatch
		        this.receiveMessage(msg);
	    	}
	
	    }
	    catch(Exception exception){
	        GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, exception, ""+this.getName()+": error when processing data input");
	    }
   	}
   	
    /** Send a Msg to Channel */
    @Override
    public synchronized boolean sendMessage(Msg msg) throws Exception
    {
    	assert(this.selectionKey!=null):"the channel must be opened";
        
        msg.setChannel(this);
        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, ""+this.getName()+":ChannelSunNioSctp#sendMessage"+" SEND the SCTP message :\n", msg);                       
        
        Association association = this.sctpChannel.association();
		assert(association!=null);

		DataSunNioSctp dataSunNioSctp = null;
        if (msg.getProtocol().equalsIgnoreCase(StackFactory.PROTOCOL_SCTP)){
            assert( msg instanceof MsgSunNioSctp );
            MsgSunNioSctp msgSctp = (MsgSunNioSctp)msg;
            dataSunNioSctp = msgSctp.getDataSunNioSctp();
            
             if( !dataSunNioSctp.hasMessageInfo() ){
                //convert alternativeInfo to messageInfo
                InfoSctp infoSctp = dataSunNioSctp.getInfo();
                int streamNumber = Short.toUnsignedInt( infoSctp.getStreamId() );
  
    			MessageInfo messageInfo = MessageInfo.createOutgoing(association,null,streamNumber);			
    			InfoSunNioSctp infoSunNioSctp = new InfoSunNioSctp(messageInfo);
    			infoSunNioSctp.trySet(infoSctp);
    			
    			dataSunNioSctp.setInfo(infoSunNioSctp);
            }

        }
        else{
            // get the bytes from the msg
            byte[] bytes = msg.encode();
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        	
            //create a MessageInfo with default settings
        	BasicInfoSctp defaultInfoSctp = new BasicInfoSctp();
			Config sctpStackConfig = StackFactory.getStack(StackFactory.PROTOCOL_SCTP).getConfig();
			defaultInfoSctp.setFromStackConfig(sctpStackConfig);
			MessageInfo messageInfo = MessageInfo.createOutgoing(association,null,defaultInfoSctp.getStreamId());			
			InfoSunNioSctp infoSctp = new InfoSunNioSctp(messageInfo);
			infoSctp.trySet(defaultInfoSctp);
	
			dataSunNioSctp = new DataSunNioSctp( byteBuffer,messageInfo );    	
        }
        boolean status = this.send( dataSunNioSctp );
        
        return status;
    }

    /**
     * 
     * @param dataSctp
     * @return status
     * @throws Exception
     * @see outputReady
     */
    private boolean send( DataSunNioSctp dataSctp ) throws Exception {
        boolean status = false;
		assert(dataSctp.hasMessageInfo());
        try{
            if( this.selectionKey.isWritable() ){
            	if( !sendNonBlocking( dataSctp ) ){
            		status = this.sendBlocking( dataSctp );
            	}
            }else{
        		status = this.sendBlocking( dataSctp );
            }
        }catch (Exception exception) {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, exception, ""+this.getName()+": error while sending data on sctp channel");
			throw exception;
		}
        return status;
    }

	private boolean sendNonBlocking( DataSunNioSctp dataSctp ) throws Exception {
		boolean status = false;
		byte[] payloadByteArray = dataSctp.getData();
		//assert(payloadByteArray.length<=ChannelSunNioSctp.MTU);
		if( payloadByteArray.length>ChannelSunNioSctp.MTU){
          GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, ""+this.getName()+":ChannelSunNioSctp#sendNonBlocking"+" SEND "+payloadByteArray.length+" bytes"+" exceeds MTU size (datagrm will be fragmented)");                       
		}
    	ByteBuffer payloadByteBuffer = ByteBuffer.wrap( payloadByteArray );
        MessageInfo messageInfo = dataSctp.getMessageInfo();
		int sentCount = this.sctpChannel.send(payloadByteBuffer, messageInfo);
        if(sentCount<0) {
        	throw new Exception("sctpChannel.send failed with status code "+sentCount);
        }
        else if(sentCount==payloadByteArray.length){
          GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, ""+this.getName()+":ChannelSunNioSctp#sendNonBlocking"+" SEND "+payloadByteArray.length+" bytes");                       
         	status = true;
        }
        else{
        	throw new Exception("sctpChannel#send should be atomic");
        }
        return status;
	}

	private boolean sendBlocking( DataSunNioSctp dataSctp ) throws Exception {
		boolean status = false;
		
		this.outputReadyLock.lock();
		try{
			//we want to be notified when a write operation is possible
			{
				int interestOps = this.selectionKey.interestOps();
				interestOps |= SelectionKey.OP_WRITE;
				this.selectionKey.interestOps( interestOps );
				this.selectionKey.selector().wakeup();
			}
			

			//wait for output ready condition and try to send the data
			do{
			  this.outputReadyCondition.await();
			  status = this.sendNonBlocking(dataSctp);
			}while(!status);

			//we do not want to be notified anymore when a write operation is possible
			{
				int interestOps = this.selectionKey.interestOps();
				interestOps &= ~SelectionKey.OP_WRITE;
				this.selectionKey.interestOps( interestOps );
				this.selectionKey.selector().wakeup();
			}
			
        }
		catch (Exception exception) {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, exception, ""+this.getName()+": error while sending data on sctp channel");
			throw exception;
		}finally {
		  this.outputReadyLock.unlock();
		}
		    	
    	return status;
    }
    
	/**
	 * @param selectionKey
	 * @param selectableChannel
	 */
    @Override
    public void onIorInit(SelectionKey selectionKey, SelectableChannel selectableChannel){
    	assert( this.selectionKey==null );
    	assert( this.sctpChannel==selectableChannel );
		GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, ""+this.getName()+":ChannelSunNioSctp#onIorInit");
    }

	/**
     * triggered when readable (there is data to read)
	 */
    @Override
    public void onIorInputReady() {
		GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, ""+this.getName()+":ChannelSunNioSctp#onIorInputReady");
		
		//will occur if the channel has been closed and until the IOReactor has unbuffered internal queues
		if(this.selectionKey==null){
			return;
		}
		
        this.pollReceivedData();
    }

	/**
     * triggered when writable (it is possible to write data)
	 */
    @Override
    public void onIorOutputReady(){
		GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, ""+this.getName()+":ChannelSunNioSctp#onIorOutputReady");
				
    	this.outputReadyLock.lock();
		try{
			this.outputReadyCondition.signalAll();
		}finally {
			this.outputReadyLock.unlock();
		}
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
    @Override
    public void onIorAcceptReady(){
    }

}
