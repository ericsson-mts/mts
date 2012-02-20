package dk.i1.diameter.node;
import java.util.logging.Logger;
import java.util.logging.Level;
//import java.util.Iterator;
import dk.i1.diameter.Message;
import dk.i1.sctp.*;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Collection;
import java.net.InetSocketAddress;
import java.net.InetAddress;

import com.devoteam.srit.xmlloader.core.utils.Config;

class SCTPNode extends NodeImplementation {
	static {
		//Create a socket and drop it. This makes the class loading in Node fail sooner
		try {
			Object o = new OneToManySCTPSocket();
		} catch(java.net.SocketException ex) {}
	}
	private Thread node_thread;
	SCTPSocket sctp_socket;
	private boolean please_stop;
	private long shutdown_deadline;
	private Map<AssociationId,SCTPConnection> map;//map from association-ids to connections
	//Current draft API cannot tell which connection attempt failed on
	//non-blocking sockets, so we allow only 1 outstanding outgoing
	//connection attempt and postpone the rest.
	private static class OutstandingConnection {
		SCTPConnection conn;
		Peer peer;
		InetAddress address;
		OutstandingConnection(SCTPConnection conn, Peer peer) {
			this.conn = conn;
			this.peer = peer;
		}
	}
	private LinkedList<OutstandingConnection> outstanding_connections;
	
	private boolean any_queued_messages;
	
	public SCTPNode(Node node, NodeSettings settings, Logger logger) {
		super(node,settings,logger);
		map = new HashMap<AssociationId,SCTPConnection>();
		outstanding_connections = new LinkedList<OutstandingConnection>();
		any_queued_messages = false;
	}
	
	void openIO() throws java.io.IOException {
		sctp_socket = new OneToManySCTPSocket();
		sctp_event_subscribe events = new sctp_event_subscribe();
		events.sctp_data_io_event  = true;
		events.sctp_association_event = true;
		sctp_socket.subscribeEvents(events);
		if(settings.port()!=0) {
			sctp_socket.bind(settings.port());
			sctp_socket.listen();
		} else {
			sctp_socket.bind();
		}
	}
	
	void start() {
		logger.log(Level.FINEST,"Starting SCTP node");
		please_stop = false;
		node_thread = new SelectThread();
		node_thread.setDaemon(true);
		node_thread.start();
		logger.log(Level.FINEST,"Started SCTP node");
	}
	
	void wakeup() {
		logger.log(Level.FINEST,"Waking up selector thread");
		try {
			sctp_socket.wakeup();
		} catch(java.net.SocketException ex) {
			logger.log(Level.WARNING,"Could not wake up SCTP service thread", ex);
		}
	}
	
	void initiateStop(long shutdown_deadline) {
		logger.log(Level.FINEST,"Initiating stop of SCTP node");
		please_stop = true;
		this.shutdown_deadline = shutdown_deadline;
		logger.log(Level.FINEST,"Initiated stop of SCTP node");
	}
	
	void join() {
		logger.log(Level.FINEST,"Joining node_thread thread");
		try {
			node_thread.join();
		} catch(java.lang.InterruptedException ex) {}
		node_thread = null;
		logger.log(Level.FINEST,"Selector thread joined");
	}
	
	void closeIO() {
		logger.log(Level.FINEST,"Closing SCTP socket");
		if(sctp_socket!=null) {
			try {
				sctp_socket.close();
			} catch(java.net.SocketException ex) {
				logger.log(Level.WARNING,"Error closing SCTP socket", ex);
			}
		}
		sctp_socket=null;
		logger.log(Level.FINEST,"Closed SCTP socket");
	}
	
	private class SelectThread extends Thread {
	    public SelectThread() {
			super("DiameterNode thread (SCTP)");
		}
	    public void run() {
			try {
				run_();
				sctp_socket.close();
			} catch(java.io.IOException ex) {}
		}
	    private void run_() throws java.io.IOException {
		// set non-blocking mode for the listening socket
		sctp_socket.configureBlocking(false);
		
		for(;;) {
			boolean tmp_any_queued_messages;
			synchronized(getLockObject()) {
				if(any_queued_messages) {
					tmp_any_queued_messages = trySendQueuedMessages();
				} else
					tmp_any_queued_messages = false;
			}
			
			if(please_stop) {
				if(System.currentTimeMillis()>=shutdown_deadline)
					break;
				if(!anyOpenConnections())
					break;
			}
			long timeout = calcNextTimeout();
			
			if(tmp_any_queued_messages) {
				//If there are any queued messages then we only wait for 200ms
				if(timeout==-1)
					timeout = 200;
				else
					timeout = Math.min(timeout,200);
			}
			
			SCTPChunk chunk;
			//System.out.println("selecting...");
			if(timeout!=-1) {
				long now=System.currentTimeMillis();
				if(timeout>now)
					chunk = sctp_socket.receive(timeout-now);
				else
					chunk = sctp_socket.receiveNow();
			} else
				chunk = sctp_socket.receive();
			//System.out.println("Woke up from select()");
			
			if(chunk!=null)
				processChunk(chunk);
		
/*
				if(key.isAcceptable()) {
					logger.log(Level.FINE,"Got an inbound connection (key is acceptable)");
					ServerSocketChannel server = (ServerSocketChannel)key.channel();
					SocketChannel channel = server.accept();
					InetSocketAddress address = (InetSocketAddress)channel.socket().getRemoteSocketAddress();
					logger.log(Level.INFO,"Got an inbound connection from " + address.toString());
					if(!please_stop) {
						TCPConnection conn = new TCPConnection(TCPNode.this,settings.watchdogInterval(),settings.idleTimeout());
						conn.host_id = address.getAddress().getHostAddress();
						conn.state = Connection.State.connected_in;
						conn.channel = channel;
						channel.configureBlocking(false);
						channel.register(selector, SelectionKey.OP_READ, conn);
						
						registerInboundConnection(conn);
					} else {
						//We don't want to add the connection if were are shutting down.
						channel.close();
					}
				} else if(key.isConnectable()) {
					logger.log(Level.FINE,"An outbound connection is ready (key is connectable)");
					SocketChannel channel = (SocketChannel)key.channel();
					TCPConnection conn = (TCPConnection)key.attachment();
					try {
						if(channel.finishConnect()) {
							logger.log(Level.FINEST,"Connected!");
							conn.state = Connection.State.connected_out;
							channel.register(selector, SelectionKey.OP_READ, conn);
							initiateCER(conn);
						}
					} catch(java.io.IOException ex) {
						logger.log(Level.WARNING,"Connection to '"+conn.host_id+"' failed", ex);
						try {
							channel.register(selector, 0);
							channel.close();
						} catch(java.io.IOException ex2) {}
						unregisterConnection(conn);
					}
				} else if(key.isReadable()) {
					logger.log(Level.FINEST,"Key is readable");
					//System.out.println("key is readable");
					SocketChannel channel = (SocketChannel)key.channel();
					TCPConnection conn = (TCPConnection)key.attachment();
					handleReadable(conn);
					if(conn.state!=Connection.State.closed &&
					   conn.hasNetOutput())
						channel.register(selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE, conn);
				} else if(key.isWritable()) {
					logger.log(Level.FINEST,"Key is writable");
					SocketChannel channel = (SocketChannel)key.channel();
					TCPConnection conn = (TCPConnection)key.attachment();
					synchronized(getLockObject()) {
						handleWritable(conn);
						if(conn.state!=Connection.State.closed &&
						   conn.hasNetOutput())
							channel.register(selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE, conn);
					}
				}
				
				// remove key from selected set, it's been handled
				it.remove();
			}
*/
			runTimers();
		}
		
		//Remaining connections are close by Node instance
	    }
	}
	
	private void processChunk(SCTPChunk chunk) {
		logger.log(Level.FINEST,"processChunk()...");
		
		if(chunk instanceof SCTPData) {
			logger.log(Level.FINEST,"Data chunk received");
			SCTPData data = (SCTPData)chunk;
			AssociationId assoc_id = data.sndrcvinfo.sinfo_assoc_id;
			SCTPConnection conn = map.get(assoc_id);
			processDataChunk(conn,data);
		} else if(chunk instanceof SCTPNotification) {
			SCTPNotification notification = (SCTPNotification)chunk;
			logger.log(Level.FINEST,"Notification chunk received");
			processNotificationChunk(notification);
		} else {
			logger.log(Level.WARNING,"Received unknown SCTP chunk from SCTP socket:"+chunk.toString());
		}
	}
	
	private void processDataChunk(SCTPConnection conn, SCTPData data) {
		int raw_bytes = data.getLength();
		byte[] raw = data.getData();
		
		if(raw_bytes<4) {
			logGarbagePacket(conn,raw,0,raw_bytes);
			closeConnection(conn,true);
			return;
		}
		int msg_size = Message.decodeSize(raw,0);
		if(raw_bytes<msg_size) {
			logGarbagePacket(conn,raw,0,raw_bytes);
			closeConnection(conn,true);
			return;
		}
		Message msg = new Message();
		Message.decode_status status = msg.decode(raw,0,msg_size);
		switch(status) {
			case decoded: {
				logRawDecodedPacket(raw,0,msg_size);
				boolean b = handleMessage(msg,conn);
				if(!b) {
					logger.log(Level.FINER,"handle error");
					closeConnection(conn);
					return;
				}
				break;
			}
			case not_enough:
				break;
			case garbage:
				logGarbagePacket(conn,raw,0,msg_size);
				closeConnection(conn,true);
				return;
		}
	}
	
	private void processNotificationChunk(SCTPNotification notification) {
		switch(notification.type) {
			//We don't subscribe to adaptation events so we won't receive them
			//case SCTP_ADAPTATION_INDICATION: {
			//	SCTPNotificationAdaptationIndication sai=(SCTPNotificationAdaptationIndication)notification;
			//	AssociationId assoc_id = sai.sai_assoc_id;
			//	SCTPConnection conn=map.get(assoc_id);
			//	logger.log(Level.INFO,"Got adaptation notification from "+(conn?conn.host_id:"unknown host")+" (association "+assoc_id.toString()+")");
			//	break;
			//}
			case SCTP_ASSOC_CHANGE: {
				logger.log(Level.FINEST,"Association-change notification received");
				SCTPNotificationAssociationChange sac=(SCTPNotificationAssociationChange)notification;
				AssociationId assoc_id = sac.sac_assoc_id;
				if(sac.sac_state==SCTPNotificationAssociationChange.State.SCTP_COMM_UP) {
					//Find the relevant connection
					//quite nasty
					logger.log(Level.FINE,"Got an association");
					InetAddress address = null;
					Collection<InetAddress> coll_address = null;
					int port;
					try {
						coll_address =  sctp_socket.getPeerInetAddresses(assoc_id);
						for(InetAddress a:coll_address) {
							address = a;
							break;
						}
						port = sctp_socket.getPeerInetPort(assoc_id);
					} catch(java.net.SocketException ex) {
						logger.log(Level.WARNING,"Caught SocketException while retrieving SCTP peer address",ex);
						try {
							sctp_socket.disconnect(assoc_id,true);
						} catch(java.net.SocketException ex2) {}
						return;
					}
					logger.log(Level.INFO,"Got an association connection from " + address.toString()+" port "+assoc_id);

					SCTPConnection conn = null;
					if(!outstanding_connections.isEmpty()) {
						//Maybe it was one we initiated
						OutstandingConnection oc=outstanding_connections.peek();
						for(InetAddress a:coll_address) {
							if(a.equals(oc.address) &&
							   port==oc.peer.port())
							{
								//Yes, it was our outstanding connection
								outstanding_connections.removeFirst();
								scheduleNextConnection();
								conn = oc.conn;
								logger.log(Level.FINE,"Outstading connection to "+conn.host_id+" completed");
							}
						}
					}
					
					if(!please_stop) {
						if(conn==null) {
							//Not one we initiated, so it is an inbound connection
							conn = new SCTPConnection(SCTPNode.this,settings.watchdogInterval(),settings.idleTimeout());
							conn.host_id = address.toString();
							conn.state = Connection.State.connected_in;
							conn.assoc_id = assoc_id;
							conn.sac_inbound_streams = sac.sac_inbound_streams;
							conn.sac_outbound_streams = sac.sac_outbound_streams;
							map.put(assoc_id,conn);
							registerInboundConnection(conn);
							
							// Devoteam Configure the CER/CEA sending
							boolean autoCERCEAEnable = Config.getConfigByName("diameter.properties").getBoolean("capability.AUTO_CER_CEA_ENABLE", true);
							if (!autoCERCEAEnable)
							{
								getNode().sendCER(conn);
							}		
						} else {
							conn.state = Connection.State.connected_out;
							conn.assoc_id = assoc_id;
							conn.sac_inbound_streams = sac.sac_inbound_streams;
							conn.sac_outbound_streams = sac.sac_outbound_streams;
							map.put(assoc_id,conn);
							initiateCER(conn);
						}
						try {
							//Set heartbeat to more that the device-watchdog interval
							sctp_paddrparams spp = new sctp_paddrparams();
							spp.spp_assoc_id = assoc_id;
							spp.spp_flags = sctp_paddrparams.SPP_HB_ENABLE;
							spp.spp_hbinterval = (int)conn.watchdogInterval()+1000;
							sctp_socket.setPeerParameters(spp);
						} catch(java.net.SocketException ex) {}
					} else {
						//We don't want to add the connection if were are shutting down.
						try {
							sctp_socket.disconnect(assoc_id);
						} catch(java.net.SocketException ex) {}
					}
				} else if(sac.sac_state==SCTPNotificationAssociationChange.State.SCTP_RESTART) {
					logger.log(Level.INFO,"Received sctp-restart notification on association "+assoc_id);
					SCTPConnection conn = map.get(assoc_id);
					if(conn!=null) {
						//The association is already gone
						conn.closed = true; //Make close() not doing the actual close because the assoc is already gone
						//Notify the Node isntance
						closeConnection(conn);
					}
					//Then open it again (maybe)
					if(please_stop) {
						//We don't want to add the connection if were are shutting down.
						try {
							sctp_socket.disconnect(assoc_id);
						} catch(java.net.SocketException ex) {}
						return;
					}
					InetAddress address = null;
					Collection<InetAddress> coll_address = null;
					int port;
					try {
						coll_address =  sctp_socket.getPeerInetAddresses(assoc_id);
						for(InetAddress a:coll_address) {
							address = a;
							break;
						}
						port = sctp_socket.getPeerInetPort(assoc_id);
					} catch(java.net.SocketException ex) {
						logger.log(Level.WARNING,"Caught SocketException while retrieving SCTP peer address",ex);
						try {
							sctp_socket.disconnect(assoc_id,true);
						} catch(java.net.SocketException ex2) {}
						return;
					}
					logger.log(Level.INFO,"Got an restarted connection from " + address.toString()+" port "+assoc_id);
					//Not one we initiated, so it is an inbound connection
					conn = new SCTPConnection(SCTPNode.this,settings.watchdogInterval(),settings.idleTimeout());
					conn.host_id = address.toString();
					conn.state = Connection.State.connected_in;
					conn.assoc_id = assoc_id;
					conn.sac_inbound_streams = sac.sac_inbound_streams;
					conn.sac_outbound_streams = sac.sac_outbound_streams;
					map.put(assoc_id,conn);
					registerInboundConnection(conn);
					try {
						//Set heartbeat to more that the device-watchdog interval
						sctp_paddrparams spp = new sctp_paddrparams();
						spp.spp_assoc_id = assoc_id;
						spp.spp_flags = sctp_paddrparams.SPP_HB_ENABLE;
						spp.spp_hbinterval = (int)conn.watchdogInterval()+1000;
						sctp_socket.setPeerParameters(spp);
					} catch(java.net.SocketException ex) {}
				} else {
					SCTPConnection conn = map.get(assoc_id);
					switch(sac.sac_state) {
						case SCTP_COMM_LOST:
							logger.log(Level.INFO,"Received sctp-comm-lost notification on association "+assoc_id);
							break;
						case SCTP_SHUTDOWN_COMP:
							logger.log(Level.INFO,"Received sctp-shutdown-comp notification on association "+assoc_id);
							break;
						case SCTP_CANT_STR_ASSOC: {
							//And outstanding connect operatio failed.
							OutstandingConnection oc = outstanding_connections.peek();
							if(oc!=null) {
								logger.log(Level.INFO,"Connection to "+oc.conn.host_id+" failed.");
								outstanding_connections.remove();
							} else {
								logger.log(Level.WARNING,"Got a cant-start-association association-change-event but no outstanding connect operation was found");
							}
							break;
						}
					}
					if(conn!=null) {
						//The association is already gone
						conn.closed = true; //Make close() not doing the actual close because the assoc is already gone
						//Notify the Node isntance
						closeConnection(conn);
					}
				}
				break;
			}
			//Not subscribed:
			//case SCTP_PARTIAL_DELIVERY_EVENT:

			//Not subscribed:
			//case SCTP_PEER_ADDR_CHANGE:

			//Not subscribed:
			//case SCTP_REMOTE_ERROR:

			//Not subscribed:
			//case SCTP_SEND_FAILED:

			case SCTP_SHUTDOWN_EVENT: {
				SCTPNotificationShutdownEvent sse = (SCTPNotificationShutdownEvent)notification;
				AssociationId assoc_id = sse.sse_assoc_id;
				SCTPConnection conn = map.get(assoc_id);
				logger.log(Level.INFO,"Received shutdown event from"+conn.host_id);
				closeConnection(conn);
				break;
			}
			default: {
				logger.log(Level.WARNING,"Received unknown SCTP event ("+notification.type+")"+notification.toString());
				break;
			}
		}
	}
	
	void sendMessage(SCTPConnection conn, byte[] raw) {
		logger.log(Level.FINEST,"sendMessage():");
		try {
			SCTPData data = new SCTPData(raw);
			data.sndrcvinfo.sinfo_assoc_id = conn.assoc_id;
			data.sndrcvinfo.sinfo_stream = conn.nextOutStream();
			sctp_socket.send(data);
		} catch(java.net.SocketException ex) {
			//TODO: re-throw
			//We don't close the association here. We 
			//will get the proper notification later on.
		} catch(WouldBlockException ex) {
			conn.queueMessage(raw);
			any_queued_messages = true;
			try {
				sctp_socket.wakeup();
			} catch(java.net.SocketException ex2) {}
		}
	}
	
	//Try sending the queued messages of all connections.
	//Return whether there are any queued messages left
	private boolean trySendQueuedMessages() {
		boolean any_left=false;
		for(Map.Entry<AssociationId,SCTPConnection> e : map.entrySet()) {
			SCTPConnection conn = e.getValue();
			byte[] raw;
			while((raw=conn.peekFirstQueuedMessage())!=null) {
				try {
					SCTPData data = new SCTPData(raw);
					data.sndrcvinfo.sinfo_assoc_id = conn.assoc_id;
					data.sndrcvinfo.sinfo_stream = conn.nextOutStream();
					sctp_socket.send(data);
					conn.removeFirstQueuedMessage();
				} catch(java.net.SocketException ex) {
					any_left = true;
					break;
				} catch(WouldBlockException ex) {
					any_left = true;
					break;
				}
			}
		}
		return any_left;
	}
	
	boolean initiateConnection(Connection conn_, Peer peer) {
		SCTPConnection conn = (SCTPConnection)conn_;
		OutstandingConnection oc = new OutstandingConnection(conn,peer);
		boolean was_empty = outstanding_connections.isEmpty();
		outstanding_connections.addLast(oc);
		if(was_empty) {
			return scheduleNextConnection();
		}
		return true;
	}
	
	//Schedules the first element in the outstanding_connections list
	//Continues until a connect() does fail or list is empty
	private boolean scheduleNextConnection() {
		while(!outstanding_connections.isEmpty()) {
			OutstandingConnection oc = outstanding_connections.peek();
			Peer peer=oc.peer;
			SCTPConnection conn = oc.conn;
			try {
				oc.address = InetAddress.getByName(peer.host());
				InetSocketAddress sock_addr = new InetSocketAddress(oc.address,peer.port());
				sctp_socket.connect(sock_addr);
				conn.state = Connection.State.connecting;
				return true;
			} catch(java.io.IOException ex) {
				logger.log(Level.WARNING,"java.io.IOException caught while initiating connection to '" + peer.host() +"'.", ex);
				outstanding_connections.removeFirst();
			}
		}
		return false;
	}
	
	void close(Connection conn_, boolean reset) {
		SCTPConnection conn = (SCTPConnection)conn_;
		if(!conn.closed) {
			logger.log(Level.FINEST,"Closing connection (SCTP) to "+conn.host_id);
			try {
				sctp_socket.disconnect(conn.assoc_id,reset);
			} catch(java.io.IOException ex) {
				logger.log(Level.WARNING,"Error closing SCTP connection to "+conn.host_id, ex);
			}
		}
		map.remove(conn.assoc_id);
	}
	
	Connection newConnection(long watchdog_interval, long idle_timeout) {
		return new SCTPConnection(this,watchdog_interval,idle_timeout);
	}

}
