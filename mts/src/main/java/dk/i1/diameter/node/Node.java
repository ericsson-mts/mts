package dk.i1.diameter.node;
import dk.i1.diameter.*;
import java.util.*;
import java.net.InetAddress;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.lang.reflect.Constructor;

import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.diameter.IDProvider;


/**
 * A Diameter node.
 * The Node class manages diameter transport connections and peers. It handles
 * the low-level messages itself (CER/CEA/DPR/DPA/DWR/DWA). The rest is sent to
 * the MessageDispatcher. When connections are established or closed the
 * ConnectionListener is notified. Message can be sent and received through the
 * node but no state is maintained per message.
 * <p>Node is quite low-level. You probably want to use {@link NodeManager} instead.
 * <p>Node instances logs with the name "dk.i1.diameter.node", so you can
 * get detailed logging (including hex-dumps of incoming and outgoing packets)
 * by putting "dk.i1.diameter.node.level = ALL" into your log.properties
 * file (or equivalent)
 * <p>The Node instance uses two properties when deciding which transport-protocols to support:
 * <ul>
 * <li><tt>dk.i1.diameter.node.use_tcp=</tt> [<tt><em>true</em></tt>|<tt><em>false</em></tt>|<tt><em>maybe</em></tt>] (default:true)</li>
 * <li><tt>dk.i1.diameter.node.use_sctp=</tt> [<tt><em>true</em></tt>|<tt><em>false</em></tt>|<tt><em>maybe</em></tt>] (default:maybe)</li>
 * </ul>
 * If a setting is set to true and the support class could not be loaded, then start operation fails.
 * If a setting is false, then no attempt will be made to use that transport-protocol.
 * If a setting is 'maybe' then the stack will try to initialize and use that trasnport-protocol, but failure to do so will not cause the stack initialization to fail.
 * You can override the properties by changing the setting with {@link NodeSettings#setUseTCP} and {@link NodeSettings#setUseSCTP}.
 * @see NodeManager
 */
public class Node {
	private MessageDispatcher message_dispatcher;
	private ConnectionListener connection_listener;
	private NodeSettings settings;
	private NodeValidator node_validator;
	private NodeState node_state;
	private Thread reconnect_thread;
	private boolean please_stop;
	private long shutdown_deadline;
	private Map<ConnectionKey,Connection> map_key_conn;
	private Set<Peer> persistent_peers;
	public static Logger logger = Logger.getLogger("dk.i1.diameter.node");
	private Object obj_conn_wait;
	private NodeImplementation tcp_node;
	private NodeImplementation sctp_node;
	
	/**
	 * Constructor for Node.
	 * Constructs a Node instance with the specified parameters.
	 * The node is not automatically started.
	 * Implemented as <tt>this(message_dispatcher,connection_listener,settings,null);</tt>
	 * @param message_dispatcher A message dispatcher. If null, a default dispatcher is used you. You probably dont want that one.
	 * @param connection_listener A connection observer. Can be null.
	 * @param settings The node settings.
	 */
	public Node(MessageDispatcher message_dispatcher,
	            ConnectionListener connection_listener,
	            NodeSettings settings
	           )
	{
		this(message_dispatcher,connection_listener,settings,null);
	}
	
	/**
	 * Constructor for Node.
	 * Constructs a Node instance with the specified parameters.
	 * The node is not automatically started.
	 * @param message_dispatcher A message dispatcher. If null, a default dispatcher is used you. You probably dont want that one.
	 * @param connection_listener A connection observer. Can be null.
	 * @param settings The node settings.
	 * @param node_validator a custom NodeValidator. If null then a {@link DefaultNodeValidator} is used.
	 * @since 0.9.4
	 */
	public Node(MessageDispatcher message_dispatcher,
	            ConnectionListener connection_listener,
	            NodeSettings settings,
	            NodeValidator node_validator
	           )
	{
		this.message_dispatcher = (message_dispatcher==null) ? new DefaultMessageDispatcher() : message_dispatcher;
		this.connection_listener = (connection_listener==null) ? new DefaultConnectionListener() : connection_listener;
		this.settings = settings;
		this.node_validator = (node_validator==null) ? new DefaultNodeValidator() : node_validator;
		this.node_state = new NodeState();
		this.obj_conn_wait = new Object();
		this.tcp_node = null;
		this.sctp_node = null;
	}
	
	/**
	 * Start the node.
	 * The node is started. If the port to listen on is already used by
	 * another application or some other initial network error occurs a java.io.IOException is thrown.
	 * @throws java.io.IOException Usually when a priviledge port is specified, system out of resoruces, etc.
	 * @throws UnsupportedTransportProtocolException If a transport-protocol has been specified as mandatory but could not be initialised.
	 */
	public void start() throws java.io.IOException, UnsupportedTransportProtocolException {
		logger.log(Level.INFO,"Starting Diameter node");
		please_stop = false;
		prepare();
		if(tcp_node!=null)
			tcp_node.start();
		if(sctp_node!=null)
			sctp_node.start();
		reconnect_thread = new ReconnectThread();
		reconnect_thread.setDaemon(true);
		reconnect_thread.start();
		logger.log(Level.INFO,"Diameter node started");
	}
	
	/**
	 * Stop the node.
	 * Implemented as stop(0)
	 */
	public void stop() {
		stop(0);
	}
	
	/**
	 * Stop the node.
	 * All connections are closed. A DPR is sent to the each connected peer
	 * unless the transport connection's buffers are full.
	 * Threads waiting in {@link #waitForConnection} are woken.
	 * Graceful connection close is not guaranteed in all cases.
	 * @param grace_time Maximum time (milliseconds) to wait for connections to close gracefully.
	 * @since grace_time parameter introduced in 0.9.3
	 */
	public void stop(long grace_time)  {
		logger.log(Level.INFO,"Stopping Diameter node");
		shutdown_deadline = System.currentTimeMillis() + grace_time;
		if(tcp_node!=null)
			tcp_node.initiateStop(shutdown_deadline);
		if(sctp_node!=null)
			sctp_node.initiateStop(shutdown_deadline);
		if(map_key_conn==null) {
			logger.log(Level.INFO,"Cannot stop node: It appears to not be running. (This is the fault of the caller)");
			return;
		}
		synchronized(map_key_conn) {
			please_stop = true;
			//Close all the non-ready connections, initiate close on ready ones.
			for(Iterator<Map.Entry<ConnectionKey,Connection>> it = map_key_conn.entrySet().iterator();
			    it.hasNext()
			   ;)
			{
				Map.Entry<ConnectionKey,Connection> e = it.next();
				Connection conn = e.getValue();
				switch(conn.state) {
					case connecting:
					case connected_in:
					case connected_out:
						logger.log(Level.FINE,"Closing connection to "+conn.host_id+" because we are shutting down");
						it.remove();
						conn.node_impl.closeConnection(conn);
						break;
					case tls:
						break; //don't know what to do here yet.
					case ready:
						initiateConnectionClose(conn,ProtocolConstants.DI_DISCONNECT_CAUSE_REBOOTING);
						break;
					case closing:
						break; //nothing to do
					case closed:
						break; //nothing to do
				}
			}
		}
		if(tcp_node!=null)
			tcp_node.wakeup();
		if(sctp_node!=null)
			sctp_node.wakeup();
		synchronized(map_key_conn) {
			map_key_conn.notify();
		}
		try {
			if(tcp_node!=null)
				tcp_node.join();
			if(sctp_node!=null)
				sctp_node.join();
			reconnect_thread.join();
		} catch(java.lang.InterruptedException ex) {}
		reconnect_thread = null;
		//close all connections not already closed
		//(todo) if a connection's out-buffer is non-empty we should wait for it to empty.
		synchronized(map_key_conn) {
			for(Map.Entry<ConnectionKey,Connection> e : map_key_conn.entrySet()) {
				Connection conn = e.getValue();
				closeConnection(conn);
			}
		}
		//other cleanup
		synchronized(obj_conn_wait) {
			obj_conn_wait.notifyAll();
		}
		map_key_conn = null;
		persistent_peers = null;
		if(tcp_node!=null)
			tcp_node.closeIO();
		if(sctp_node!=null)
			sctp_node.closeIO();
		logger.log(Level.INFO,"Diameter node stopped");
	}
	
	private boolean anyReadyConnection() {
		if(map_key_conn==null)
			return false;
		synchronized(map_key_conn) {
			for(Map.Entry<ConnectionKey,Connection> e : map_key_conn.entrySet()) {
				Connection conn = e.getValue();
				if(conn.state==Connection.State.ready)
					return true;
			}
		}
		return false;
	}
	
	/**
	 * Wait until at least one connection has been established to a peer
	 * and capability-exchange has finished.
	 * @since 0.9.1
	 */
	public void waitForConnection() throws InterruptedException {
		synchronized(obj_conn_wait) {
			while(!anyReadyConnection())
				obj_conn_wait.wait();
		}
	}
	/**
	 * Wait until at least one connection has been established or until the timeout expires.
	 * Waits until at least one connection to a peer has been established
	 * and capability-exchange has finished, or the specified timeout has expired.
	 * @param timeout The maximum time to wait in milliseconds.
	 * @since 0.9.1
	 */
	public void waitForConnection(long timeout) throws InterruptedException {
		long wait_end = System.currentTimeMillis()+timeout;
		synchronized(obj_conn_wait) {
			long now = System.currentTimeMillis();
			while(!anyReadyConnection() && now<wait_end) {
				long t = wait_end - now;
				obj_conn_wait.wait(t);
				now = System.currentTimeMillis();
			}
		}
	}
	
	/**
	 * Returns the connection key for a peer.
	 * Behaviour change since 0.9.6: Connections that are not in the "Open"
	 * state (rfc3588 section 5.6) will not be returned.
	 * @return The connection key. Null if there is no connection to the peer.
	 */
	public ConnectionKey findConnection(Peer peer)  {
		logger.log(Level.FINER,"Finding '" + peer.host() +"'");
		if(map_key_conn==null) {
			logger.log(Level.FINER,peer.host()+" NOT found (node is not ready)");
			return null;
		}
		synchronized(map_key_conn) {
			//System.out.println("Node.findConnection: size=" + map_key_conn.size());
			for(Map.Entry<ConnectionKey,Connection> e : map_key_conn.entrySet()) {
				Connection conn = e.getValue();
				//System.out.println("Node.findConnection(): examing " + ((conn.peer!=null)?conn.peer.host():"?"));
				if(conn.state!=Connection.State.ready)
					continue;
				if(conn.peer!=null
				&& conn.peer.equals(peer)) {
					//System.out.println("Node.findConnection(): found");
					return conn.key;
				}
			}
			logger.log(Level.FINER,peer.host()+" NOT found");
			return null;
		}
	}
	/**
	 * Returns if the connection is still valid.
	 * This method is usually only of interest to programs that do lengthy
	 * processing of requests nad are located in a poor network. It is
	 * usually much easier to just call sendMessage() and catch the
	 * exception if the connection has gone stale.
	 */
	public boolean isConnectionKeyValid(ConnectionKey connkey) {
		if(map_key_conn==null)
			return false;
		synchronized(map_key_conn) {
			return map_key_conn.get(connkey)!=null;
		}
	}

	/**
	 * Returns the IP-address of the remote end of a connection.
	 * Note: for connections using the SCTP transport protocol the returned
	 * IP-address will be one of the peer's IP-addresses but it is
	 * unspecified which one. In this case it is better to use
	 * connectionKey2Peer()
	 */
	public Connection connectionKey2InetAddress(ConnectionKey connkey) {
		if(map_key_conn==null)
			return null;
		synchronized(map_key_conn) {
			Connection conn = map_key_conn.get(connkey);
			if(conn!=null)
				return conn;
			else
				return null;
		}
	}
    
	/**
	 * Send a message.
	 * Send the specified message on the specified connection.
	 * @param msg The message to be sent
	 * @param connkey The connection to use. If the connection has been closed in the meantime StaleConnectionException is thrown.
	 */
	public void sendMessage(Message msg, ConnectionKey connkey) throws StaleConnectionException {
		if(map_key_conn==null)
			throw new StaleConnectionException();
		synchronized(map_key_conn) {
			Connection conn = map_key_conn.get(connkey);
			if(conn==null)
				throw new StaleConnectionException();
			if(conn.state!=Connection.State.ready)
				throw new StaleConnectionException();
			sendMessage(msg,conn);
		}
	}
    
	private void sendMessage(Message msg, Connection conn) {
		logger.log(Level.FINER,"command=" + msg.hdr.command_code +", to=" + (conn.peer!=null ? conn.peer.toString() : conn.host_id));
		byte[] raw = msg.encode();
		
		if(logger.isLoggable(Level.FINEST))
			hexDump(Level.FINEST,"Raw packet encoded",raw,0,raw.length);
		
		conn.sendMessage(raw);
	}
	
	/**
	 * Initiate a connection to a peer.
	 * A connection (if not already present) will be initiated to the peer.
	 * On return, the connection is probably not established and it may
	 * take a few seconds before it is. It is safe to call multiple times.
	 * If <code>persistent</code> true then the peer is added to a list of
	 * persistent peers and if the connection is lost it will automatically
	 * be re-established. There is no way to change a peer from persistent
	 * to non-persistent.
	 * <p>
	 * If/when the connection has been established and capability-exchange
	 * has finished threads waiting in {@link #waitForConnection} are woken.
	 * <p>
	 * You cannot initiate connections before the node has been started.
	 * Connection to peers specifying an unsupported transport-protocl are simply ignored.
	 * @param peer The peer that the node should try to establish a connection to.
	 * @param persistent If true the Node wil try to keep a connection open to the peer.
	 */
	public void initiateConnection(Peer peer, boolean persistent) {
		if(persistent) {
			synchronized(persistent_peers) {
				persistent_peers.add(new Peer(peer));
			}
		}
		synchronized(map_key_conn) {
			for(Map.Entry<ConnectionKey,Connection> e : map_key_conn.entrySet()) {
				Connection conn = e.getValue();
				if(conn.peer!=null &&
				   conn.peer.equals(peer))
					return; //already has a connection to that peer
				//what if we are connecting and the host_id matches?
			}
			logger.log(Level.INFO,"Initiating connection to '" + peer.host() +"' port "+peer.port());
			NodeImplementation node_impl=null;
			switch(peer.transportProtocol()) {
				case tcp:
					node_impl = tcp_node;
					break;
				case sctp:
					node_impl = sctp_node;
					break;
			}
			if(node_impl!=null) {
				Connection conn = node_impl.newConnection(settings.watchdogInterval(),settings.idleTimeout());
				conn.host_id = settings.hostId();
				// same for port but attribute does not exist in Connection class ?
				// conn.port = settings.port();
				conn.peer = peer;
				if(node_impl.initiateConnection(conn,peer))
					map_key_conn.put(conn.key,conn);
			}
		}
	}
	
	private class ReconnectThread extends Thread {
		public ReconnectThread() {
			super("Diameter node reconnect thread");
		}
		public void run() {
			for(;;) {
				synchronized(map_key_conn) {
					if(please_stop) return;
					try {
						map_key_conn.wait(30000);
					} catch(java.lang.InterruptedException ex) {}
					if(please_stop) return;
				}
				synchronized(persistent_peers) {
					for(Peer peer : persistent_peers)
						initiateConnection(peer,false);
				}
			}
		}
	}
	
	private static Boolean getUseOption(Boolean setting, String property_name, Boolean default_setting) {
		if(setting!=null)
			return setting;
		String v = System.getProperty(property_name);
		if(v!=null && v.equals("true"))
			return true;
		if(v!=null && v.equals("false"))
			return false;
		if(v!=null && v.equals("maybe"))
			return null;
		return default_setting;
	}
	
	@SuppressWarnings("unchecked")
	private NodeImplementation instantiateNodeImplementation(Level loglevel, String class_name) {
		Class our_cls = this.getClass();
		ClassLoader cls_ldr = our_cls.getClassLoader();
		if(cls_ldr==null) //according to sun it can be
			cls_ldr = ClassLoader.getSystemClassLoader();
		try {
			Class cls = cls_ldr.loadClass(class_name);
			Constructor<NodeImplementation> ctor;
			try {
				ctor = cls.getConstructor(this.getClass(),
			                        	  settings.getClass(),
			                        	  logger.getClass()
			                        	 );
			} catch(java.lang.NoSuchMethodException ex) {
				logger.log(loglevel,"Could not find constructor for "+class_name,ex);
				return null;
			} catch(java.lang.NoClassDefFoundError ex) {
				//This is the most common cause
				if(loglevel!=Level.FINE)
					logger.log(loglevel,"Could not find constructor for "+class_name,ex);
				else //tone down the log message. Drop the stack frame.
					logger.log(loglevel,"Could not find constructor for "+class_name);
				return null;
			} catch(java.lang.UnsatisfiedLinkError ex) {
				logger.log(loglevel,"Could not find constructor for "+class_name,ex);
				return null;
			}
			if(ctor==null) return null;
			try {
				NodeImplementation instance =  ctor.newInstance(this,settings,logger);
				return instance;
			} catch(java.lang.InstantiationException ex) {
				return null;
			} catch(java.lang.IllegalAccessException ex) {
				return null;
			} catch(java.lang.reflect.InvocationTargetException ex) {
				return null;
			} catch(java.lang.UnsatisfiedLinkError ex) {
				logger.log(loglevel,"Could not construct a "+class_name,ex);
				return null;
			}
		} catch(ClassNotFoundException ex) {
			logger.log(loglevel,"class "+class_name+" not found/loaded",ex);
			return null;
		}
	}
	
	private NodeImplementation loadTransportProtocol(Boolean setting, String setting_name, Boolean default_setting,
	                                                 String class_name, String short_name)
	                                                 throws java.io.IOException, UnsupportedTransportProtocolException
	{
		Boolean b;
		b = getUseOption(setting,setting_name,default_setting);
		NodeImplementation node_impl = null;
		if(b==null || b) {
			node_impl = instantiateNodeImplementation(b!=null?Level.INFO:Level.FINE,class_name);
			if(node_impl!=null)
				node_impl.openIO();
			else if(b!=null)
				throw new UnsupportedTransportProtocolException(short_name+" support could not be loaded");
		}
		logger.log(Level.INFO,short_name+" support was "+(node_impl!=null?"loaded":"not loaded"));
		return node_impl;
	}
	private void prepare() throws java.io.IOException, UnsupportedTransportProtocolException {
		tcp_node =  loadTransportProtocol(settings.useTCP(),"dk.i1.diameter.node.use_tcp",true,
		                                  "dk.i1.diameter.node.TCPNode", "TCP");
		sctp_node = loadTransportProtocol(settings.useSCTP(),"dk.i1.diameter.node.use_sctp",null,
		                                  "dk.i1.diameter.node.SCTPNode", "SCTP");
		if(tcp_node==null && sctp_node==null)
			logger.log(Level.WARNING,"No transport protocol classes could be loaded. The stack is running but without have any connectivity");
		
		map_key_conn = new HashMap<ConnectionKey,Connection>();
		persistent_peers = new HashSet<Peer>();
	}
	
	
	/**Calculate next timeout for a node implementation.
	 * Located here because the calculation involves examining each open connection.
	 */
	long calcNextTimeout(NodeImplementation node_impl) {
		long timeout = -1;
		synchronized(map_key_conn) {
			for(Map.Entry<ConnectionKey,Connection> e : map_key_conn.entrySet()) {
				Connection conn = e.getValue();
				if(conn.node_impl!=node_impl) continue;
				boolean ready = conn.state==Connection.State.ready;
				long conn_timeout = conn.timers.calcNextTimeout(ready);
				if(timeout==-1 || conn_timeout<timeout)
					timeout = conn_timeout;
			}
		}
		if(please_stop && shutdown_deadline<timeout)
			timeout=shutdown_deadline;
		return timeout;
	}
	
	/**Run timers on the connections for a node implementation.
	 * Located here because it involves examining each open connection.
	 */
	void runTimers(NodeImplementation node_impl) {
		synchronized(map_key_conn) {
			for(Iterator<Map.Entry<ConnectionKey,Connection>> it = map_key_conn.entrySet().iterator();
			    it.hasNext();
			   )
			{
				Map.Entry<ConnectionKey,Connection> e = it.next();
				Connection conn = e.getValue();
				if(conn.node_impl!=node_impl) continue;
				boolean ready = conn.state==Connection.State.ready;
				switch(conn.timers.calcAction(ready)) {
					case none:
						break;
					case disconnect_no_cer:
						logger.log(Level.WARNING,"Disconnecting due to no CER/CEA");
						it.remove();
						closeConnection(conn);
						break;
					case disconnect_idle:
						logger.log(Level.WARNING,"Disconnecting due to idle");
						//busy is the closest thing to "no traffic for a long time. No point in keeping the connection"
						it.remove();
						initiateConnectionClose(conn,ProtocolConstants.DI_DISCONNECT_CAUSE_BUSY);
						break;
					case disconnect_no_dw:
						logger.log(Level.WARNING,"Disconnecting due to no DWA");
						it.remove();
						closeConnection(conn);
						break;
					case dwr:
						sendDWR(conn);
						break;
				}
			}
		}
	}
	
	
	/**Logs a correctly decoded message*/
	void logRawDecodedPacket(byte[] raw, int offset, int msg_size) {
		hexDump(Level.FINEST,"Raw packet decoded",raw,offset,msg_size);
	}
	/**Logs an incorrectly decoded (non-diameter-)message.*/
	void logGarbagePacket(Connection conn, byte[] raw, int offset, int msg_size) {
		hexDump(Level.WARNING,"Garbage from "+conn.host_id,raw,offset,msg_size);
	}
	
	void hexDump(Level level, String msg, byte buf[], int offset, int bytes) {
		if(!logger.isLoggable(level))
			return;
		//For some reason this method is grotesquely slow, so we limit the raw dump to 1K
		if(bytes>1024) bytes=1024;
		StringBuffer sb = new StringBuffer(msg.length()+1+bytes*3+(bytes/16+1)*(6+3+5+1));
		sb.append(msg+"\n");
		for(int i=0; i<bytes; i+=16) {
			sb.append(String.format("%04X ", new Integer(i)));
			for(int j=i; j<i+16; j++) {
				if((j%4)==0)
					sb.append(' ');
				if(j<bytes) {
					byte b=buf[offset+j];
					sb.append(String.format("%02X",b));
				} else
					sb.append("  ");
			}
			sb.append("     ");
			for(int j=i; j<i+16 && j<bytes; j++) {
				byte b=buf[offset+j];
				if(b>=32 && b<127)
					sb.append((char)b);
				else
					sb.append('.');
			}
			sb.append('\n');
		}
		if(bytes>1024)
			sb.append("...\n"); //Maybe the string "(truncated)" would be a more direct hint
		logger.log(level,sb.toString());
	}
	
	
	void closeConnection(Connection conn)  {
		closeConnection(conn,false);
	}
	void closeConnection(Connection conn, boolean reset) {
		if(conn.state==Connection.State.closed) return;
		logger.log(Level.INFO,"Closing connection to " + (conn.peer!=null ? conn.peer.toString() : conn.host_id));
		synchronized(map_key_conn) {
			conn.node_impl.close(conn,reset);
			map_key_conn.remove(conn.key);
			conn.state = Connection.State.closed;
		}
		connection_listener.handle(conn.key, conn.peer, false);
	}
	
	//Send a DPR with the specified disconnect-cause, want change the state to 'closing'
	private void initiateConnectionClose(Connection conn, int why) {
		if(conn.state!=Connection.State.ready)
			return; //should probably never happen
		boolean autoDPRDPAEnable = Config.getConfigByName("diameter.properties").getBoolean("capability.AUTO_DPR_DPA_ENABLE", true);
		if (autoDPRDPAEnable)
		{
			sendDPR(conn,why);
		}
		conn.state = Connection.State.closing;
	}
	
	boolean handleMessage(Message msg, Connection conn) {
		if(logger.isLoggable(Level.FINE))
			logger.log(Level.FINE,"command_code=" + msg.hdr.command_code + " application_id=" + msg.hdr.application_id + " connection_state=" + conn.state);
		conn.timers.markActivity();
		if(conn.state==Connection.State.connected_in) {
			//only CER allowed
			if(!msg.hdr.isRequest() ||
			   msg.hdr.command_code!=ProtocolConstants.DIAMETER_COMMAND_CAPABILITIES_EXCHANGE ||
			   msg.hdr.application_id!=ProtocolConstants.DIAMETER_APPLICATION_COMMON)
			{
				logger.log(Level.WARNING,"Got something that wasn't a CER");
				return false;
			}
			conn.timers.markRealActivity();
			return handleCER(msg,conn);
		} else if(conn.state==Connection.State.connected_out) {
			//only CEA allowed
			if(msg.hdr.isRequest() ||
			   msg.hdr.command_code!=ProtocolConstants.DIAMETER_COMMAND_CAPABILITIES_EXCHANGE ||
			   msg.hdr.application_id!=ProtocolConstants.DIAMETER_APPLICATION_COMMON)
			{
				logger.log(Level.WARNING,"Got something that wasn't a CEA");
				return false;
			}
			conn.timers.markRealActivity();
			return handleCEA(msg,conn);
		} else {
			switch(msg.hdr.command_code) {
				case ProtocolConstants.DIAMETER_COMMAND_CAPABILITIES_EXCHANGE:
					logger.log(Level.WARNING,"Got CER from "+conn.host_id+" after initial capability-exchange");
					//not allowed in this state
					return false;
				case ProtocolConstants.DIAMETER_COMMAND_DEVICE_WATCHDOG:
					if(msg.hdr.isRequest())
						return handleDWR(msg,conn);
					else
						return handleDWA(msg,conn);
				case ProtocolConstants.DIAMETER_COMMAND_DISCONNECT_PEER:
					if(msg.hdr.isRequest())
						return handleDPR(msg,conn);
					else
						return handleDPA(msg,conn);
				default:
					conn.timers.markRealActivity();
					if(msg.hdr.isRequest()) {
						if(isLoopedMessage(msg)) {
							rejectLoopedRequest(msg,conn);
							return true;
						} 
						if(!isAllowedApplication(msg,conn.peer)) {
							rejectDisallowedRequest(msg,conn);
							return true;
						}
						//We could also reject requests if we ar shutting down, but there are no result-code for this.
					}
					if(!message_dispatcher.handle(msg,conn.key,conn.peer)) {
						if(msg.hdr.isRequest())
							return handleUnknownRequest(msg,conn);
						else
							return true; //unusual, but not impossible
					} else
						return true;
			}
		}
	}
	
	private boolean isLoopedMessage(Message msg) {
		//6.1.3
		for(AVP a : msg.subset(ProtocolConstants.DI_ROUTE_RECORD)) {
			AVP_UTF8String avp=new AVP_UTF8String(a);
			if(avp.queryValue().equals(settings.hostId()))
				return true;
		}
		return false;
	}
	private void rejectLoopedRequest(Message msg, Connection conn) {
		logger.log(Level.WARNING,"Rejecting looped request from " + conn.peer.host() + " (command=" + msg.hdr.command_code + ").");
		rejectRequest(msg,conn,ProtocolConstants.DIAMETER_RESULT_LOOP_DETECTED);
	}
	
	/**
	 * Determine if a message is supported by a peer.
	 * The auth-application-id, acct-application-id or
	 * vendor-specific-application AVP is extracted and tested against the
	 * peer's capabilities.
	 * @param msg The message
	 * @param peer The peer
	 * @return True if the peer should be able to handle the message.
	 */
	public boolean isAllowedApplication(Message msg, Peer peer) {			
		try {
			
			//SRIT add a configuration parameter to avoid the control capability before 
			//SRIT the message sending and receiving 
			boolean capabilityControl = Config.getConfigByName("diameter.properties").getBoolean("capability.CONTROL_VALIDITY", true);
			if (!capabilityControl)
			{
				return true;
			}
			
			AVP avp;
			avp = msg.find(ProtocolConstants.DI_AUTH_APPLICATION_ID);
			if(avp!=null) {
				int app = new AVP_Unsigned32(avp).queryValue();
				if(logger.isLoggable(Level.FINE))
					logger.log(Level.FINE,"auth-application-id="+app);
				return peer.capabilities.isAllowedAuthApp(app);
			}
			avp = msg.find(ProtocolConstants.DI_ACCT_APPLICATION_ID);
			if(avp!=null) {
				int app = new AVP_Unsigned32(avp).queryValue();
				if(logger.isLoggable(Level.FINE))
					logger.log(Level.FINE,"acct-application-id="+app);
				return peer.capabilities.isAllowedAcctApp(app);
			}
			avp = msg.find(ProtocolConstants.DI_VENDOR_SPECIFIC_APPLICATION_ID);
			if(avp!=null) {
				AVP g[] = new AVP_Grouped(avp).queryAVPs();
				if(g.length==2 &&
				   g[0].code==ProtocolConstants.DI_VENDOR_ID) {
					int vendor_id = new AVP_Unsigned32(g[0]).queryValue();
					int app = new AVP_Unsigned32(g[1]).queryValue();
					if(logger.isLoggable(Level.FINE))
						logger.log(Level.FINE,"vendor-id="+vendor_id+", app="+app);
					if(g[1].code==ProtocolConstants.DI_AUTH_APPLICATION_ID)
						return peer.capabilities.isAllowedAuthApp(vendor_id,app);
					if(g[1].code==ProtocolConstants.DI_ACCT_APPLICATION_ID)
						return peer.capabilities.isAllowedAcctApp(vendor_id,app);
				}
				return false;
			}
			logger.log(Level.WARNING,"No auth-app-id, acct-app-id nor vendor-app in packet");
		} catch(InvalidAVPLengthException ex) {
			logger.log(Level.INFO,"Encountered invalid AVP length while looking at application-id",ex);
		}
		return false;
	}
	private void rejectDisallowedRequest(Message msg, Connection conn) {
		logger.log(Level.WARNING,"Rejecting request  from " + conn.peer.host() + " (command=" + msg.hdr.command_code + ") because it is not allowed.");
		rejectRequest(msg,conn,ProtocolConstants.DIAMETER_RESULT_APPLICATION_UNSUPPORTED);
	}
	
	private void rejectRequest(Message msg, Connection conn, int result_code) {
		Message response = new Message();
		response.prepareResponse(msg);
		if(result_code>=3000 && result_code<=3999)
			response.hdr.setError(true);
		response.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, result_code));
		addOurHostAndRealm(response);
		Utils.copyProxyInfo(msg,response);
		Utils.setMandatory_RFC3588(response);
		sendMessage(response,conn);
	}
	
	
	/**
	 * Add origin-host and origin-realm to a message.
	 * The configured host and realm is added to the message as origin-host
	 * and origin-realm AVPs
	 */
	public void addOurHostAndRealm(Message msg) {
		msg.add(new AVP_UTF8String(ProtocolConstants.DI_ORIGIN_HOST,settings.hostId()));
		msg.add(new AVP_UTF8String(ProtocolConstants.DI_ORIGIN_REALM,settings.realm()));
	}
	
	/**
	 * Returns an end-to-end identifier that is unique.
	 * The initial value is generated as described in RFC 3588 section 3 page 34.
	 */
	public int nextEndToEndIdentifier() {
		return node_state.nextEndToEndIdentifier();
	}
	
	/**
	 * Generate a new session-id.
	 * Implemented as makeNewSessionId(null)
	 * @since 0.9.2
	 */
	public String makeNewSessionId() {
		return makeNewSessionId(null);
	}
	
	/**
	 * Generate a new session-id.
	 * A Session-Id consists of a mandatory part and an optional part.
	 * The mandatory part consists of the host-id and two sequencers.
	 * The optional part can be anything. The caller provide some
	 * information that will be helpful in debugging in production
	 * environments, such as user-name or calling-station-id.
	 * @since 0.9.2
	 */
	public String makeNewSessionId(String optional_part) {
		String mandatory_part = settings.hostId() + ";" + node_state.nextSessionId_second_part();
		if(optional_part==null)
			return mandatory_part;
		else
			return mandatory_part + ";" + optional_part;
	}
	
	/**
	 * Returns the node's state-id.
	 * @since 0.9.2
	 */
	public int stateId() {
		return node_state.stateId();
	}
	
	
	private boolean doElection(String cer_host_id) {
		int cmp = settings.hostId().compareTo(cer_host_id);
		//Devoteam if(cmp==0) {
		//Devoteam	logger.log(Level.WARNING,"Got CER with host-id="+cer_host_id+". Suspecting this is a connection from ourselves.");
		//Devoteam	//this is a misconfigured peer or ourselves.
		//Devoteam	return false;}
		boolean close_other_connection = cmp>0;
		synchronized(map_key_conn) {
			for(Map.Entry<ConnectionKey,Connection> e : map_key_conn.entrySet()) {
				Connection conn = e.getValue();
				if(conn.host_id!=null && conn.host_id.equals(cer_host_id) &&
				   conn.state==Connection.State.ready //TODO: what about TLS?
				) {
					logger.log(Level.INFO,"New connection to a peer we already have a connection to (" + cer_host_id + ")");
					if(close_other_connection) {
						closeConnection(conn);
						return true;
					} else
						return false; //close this one
				}
			}
		}
		return true;
	}
	
	private boolean handleCER(Message msg, Connection conn) {
		logger.log(Level.FINE,"CER received from " + conn.host_id);
		//Handle election
		String host_id;
		{
			AVP avp = msg.find(ProtocolConstants.DI_ORIGIN_HOST);
			if(avp==null) {
				//Origin-Host-Id is missing
				logger.log(Level.FINE,"CER from " + conn.host_id+" is missing the Origin-Host_id AVP. Rejecting.");
				Message error_response = new Message();
				error_response.prepareResponse(msg);
				error_response.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_MISSING_AVP));
				addOurHostAndRealm(error_response);
				error_response.add(new AVP_FailedAVP(new AVP_UTF8String(ProtocolConstants.DI_ORIGIN_HOST,"")));
				Utils.setMandatory_RFC3588(error_response);
				sendMessage(error_response,conn);
				return false;
			}
			host_id = new AVP_UTF8String(avp).queryValue();
			logger.log(Level.FINER,"Peer's origin-host-id is " + host_id);
			
			//We must authenticate the host before doing election.
			//Otherwise a rogue node could trick us into
			//disconnecting legitimate peers.
			NodeValidator.AuthenticationResult ar = node_validator.authenticateNode(host_id,conn.getRelevantNodeAuthInfo());
			if(ar==null || !ar.known) {
				logger.log(Level.FINE,"We do not know " + conn.host_id+" Rejecting.");
				Message error_response = new Message();
				error_response.prepareResponse(msg);
				if(ar!=null && ar.result_code!=null)
					error_response.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE,  ar.result_code));
				else
					error_response.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE,  ProtocolConstants.DIAMETER_RESULT_UNKNOWN_PEER));
				addOurHostAndRealm(error_response);
				if(ar!=null && ar.error_message!=null)
					error_response.add(new AVP_UTF8String(ProtocolConstants.DI_ERROR_MESSAGE,ar.error_message));
				Utils.setMandatory_RFC3588(error_response);
				sendMessage(error_response,conn);
				return false;
				
			}
			
			// devoteam : delete this mechanism because not understood ?
			/*
			if(!doElection(host_id)) {
				logger.log(Level.FINE,"CER from " + conn.host_id+" lost the election. Rejecting.");
				Message error_response = new Message();
				error_response.prepareResponse(msg);
				error_response.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_ELECTION_LOST));
				addOurHostAndRealm(error_response);
				Utils.setMandatory_RFC3588(error_response);
				sendMessage(error_response,conn);
				return false;
			}
			//*/
		}
		
		conn.peer = conn.toPeer();
		conn.peer.host(host_id);
		conn.host_id = host_id;
		
		
		
		if(handleCEx(msg,conn)) {
			//todo: check inband-security
			Message cea = new Message();
			cea.prepareResponse(msg);
			//Result-Code
			cea.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_SUCCESS));
			addCEStuff(cea,conn.peer.capabilities,conn);
			
			logger.log(Level.INFO,"Connection to " +conn.peer.toString() + " is now ready");
			Utils.setMandatory_RFC3588(cea);
			
			// Devoteam Configure the CER/CEA sending			
			boolean autoCERCEAEnable = Config.getConfigByName("diameter.properties").getBoolean("capability.AUTO_CER_CEA_ENABLE", true);
			if (autoCERCEAEnable)
			{
				sendMessage(cea,conn);
			}
			else
			{
				handleCEA(cea, conn);
			}
			
			conn.state=Connection.State.ready;
			connection_listener.handle(conn.key, conn.peer, true);
			synchronized(obj_conn_wait) {
				obj_conn_wait.notifyAll();
			}
			return true;
		} else
			return false;
	}
	private boolean handleCEA(Message msg, Connection conn) {
		logger.log(Level.FINE,"CEA received from "+conn.host_id);
		AVP avp = msg.find(ProtocolConstants.DI_RESULT_CODE);
		if(avp==null) {
			logger.log(Level.WARNING,"CEA from "+conn.host_id+" did not contain a Result-Code AVP. Dropping connection");
			return false;
		}
		int result_code;
		try {
			result_code = new AVP_Unsigned32(avp).queryValue();
		} catch(InvalidAVPLengthException ex) {
			logger.log(Level.INFO,"CEA from "+conn.host_id+" contained an ill-formed Result-Code. Dropping connection");
			return false;
		}
		if(result_code!=ProtocolConstants.DIAMETER_RESULT_SUCCESS) {
			logger.log(Level.INFO,"CEA from "+conn.host_id+" was rejected with Result-Code "+result_code+". Dropping connection");
			return false;
		}
		avp = msg.find(ProtocolConstants.DI_ORIGIN_HOST);
		if(avp==null) {
			logger.log(Level.WARNING,"Peer did not include origin-host-id in CEA");
			return false;
		}
		String host_id = new AVP_UTF8String(avp).queryValue();
		logger.log(Level.FINER,"Node:Peer's origin-host-id is '"+host_id+"'");
		
		conn.peer = conn.toPeer();
		conn.peer.host(host_id);
		conn.host_id = host_id;
		boolean rc = handleCEx(msg,conn);
		if(rc) {
			conn.state=Connection.State.ready;
			logger.log(Level.INFO,"Connection to " +conn.peer.toString() + " is now ready");
			connection_listener.handle(conn.key, conn.peer, true);
			synchronized(obj_conn_wait) {
				obj_conn_wait.notifyAll();
			}
			return true;
		} else {
			return false;
		}
	}
	private boolean handleCEx(Message msg, Connection conn) {
		logger.log(Level.FINER,"Processing CER/CEA");
		//calculate capabilities and allowed applications
		try {
			Capability reported_capabilities = new Capability();
			for(AVP a : msg.subset(ProtocolConstants.DI_SUPPORTED_VENDOR_ID)) {
				int vendor_id = new AVP_Unsigned32(a).queryValue();
				logger.log(Level.FINEST,"peer supports vendor "+vendor_id);
				reported_capabilities.addSupportedVendor(vendor_id);
			}
			for(AVP a : msg.subset(ProtocolConstants.DI_AUTH_APPLICATION_ID)) {
				int app = new AVP_Unsigned32(a).queryValue();
				logger.log(Level.FINEST,"peer supports auth-app "+app);
				if(app!=ProtocolConstants.DIAMETER_APPLICATION_COMMON)
					reported_capabilities.addAuthApp(app);
			}
			for(AVP a : msg.subset(ProtocolConstants.DI_ACCT_APPLICATION_ID)) {
				int app = new AVP_Unsigned32(a).queryValue();
				logger.log(Level.FINEST,"peer supports acct-app "+app);
				if(app!=ProtocolConstants.DIAMETER_APPLICATION_COMMON)
					reported_capabilities.addAcctApp(app);
			}
			for(AVP a : msg.subset(ProtocolConstants.DI_VENDOR_SPECIFIC_APPLICATION_ID)) {
				AVP_Grouped ag = new AVP_Grouped(a);
				AVP g[] = ag.queryAVPs();
				if(g.length>=2 && g[0].code==ProtocolConstants.DI_VENDOR_ID) {
					int vendor_id = new AVP_Unsigned32(g[0]).queryValue();
					int app = new AVP_Unsigned32(g[1]).queryValue();
					if(g[1].code==ProtocolConstants.DI_AUTH_APPLICATION_ID) {
						reported_capabilities.addVendorAuthApp(vendor_id,app);
					} else if(g[1].code==ProtocolConstants.DI_ACCT_APPLICATION_ID) {
						reported_capabilities.addVendorAcctApp(vendor_id,app);
					} else
						throw new InvalidAVPValueException(a);
				} else if(g.length>=2 && g[1].code==ProtocolConstants.DI_VENDOR_ID) {
					int vendor_id = new AVP_Unsigned32(g[1]).queryValue();
					int app = new AVP_Unsigned32(g[0]).queryValue();
					if(g[0].code==ProtocolConstants.DI_AUTH_APPLICATION_ID) {
						reported_capabilities.addVendorAuthApp(vendor_id,app);
					} else if(g[0].code==ProtocolConstants.DI_ACCT_APPLICATION_ID) {
						reported_capabilities.addVendorAcctApp(vendor_id,app);
					} else
						throw new InvalidAVPValueException(a);
				} else
					throw new InvalidAVPValueException(a);
			}

			Capability result_capabilities = node_validator.authorizeNode(conn.host_id, settings, reported_capabilities);
			if(logger.isLoggable(Level.FINEST)) {
				String s = "";
				for(Integer i:result_capabilities.supported_vendor)
					s = s + "  supported_vendor "+i+"\n";
				for(Integer i:result_capabilities.auth_app)
					s = s + "  auth_app "+i+"\n";
				for(Integer i:result_capabilities.acct_app)
					s = s + "  acct_app "+i+"\n";
				for(Capability.VendorApplication va:result_capabilities.auth_vendor)
					s = s + "  vendor_auth_app: vendor "+va.vendor_id+", application "+ va.application_id+"\n";
				for(Capability.VendorApplication va:result_capabilities.acct_vendor)
					s = s + "  vendor_acct_app: vendor "+va.vendor_id+", application "+ va.application_id+"\n";
				logger.log(Level.FINEST,"Resulting capabilities:\n"+s);
			}
			if(result_capabilities.isEmpty()) {
				logger.log(Level.WARNING,"No application in common with "+conn.host_id);
				if(msg.hdr.isRequest()) {
					Message error_response = new Message();
					error_response.prepareResponse(msg);
					error_response.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_NO_COMMON_APPLICATION));
					addOurHostAndRealm(error_response);
					Utils.setMandatory_RFC3588(error_response);
					sendMessage(error_response,conn);
				}
				return false;
			}
			
			conn.peer.capabilities = result_capabilities;
		} catch(InvalidAVPLengthException ex) {
			logger.log(Level.WARNING,"Invalid AVP in CER/CEA",ex);
			if(msg.hdr.isRequest()) {
				Message error_response = new Message();
				error_response.prepareResponse(msg);
				error_response.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_INVALID_AVP_LENGTH));
				addOurHostAndRealm(error_response);
				error_response.add(new AVP_FailedAVP(ex.avp));
				Utils.setMandatory_RFC3588(error_response);
				sendMessage(error_response,conn);
			}
			return false;
		} catch(InvalidAVPValueException ex) {
			logger.log(Level.WARNING,"Invalid AVP in CER/CEA",ex);
			if(msg.hdr.isRequest()) {
				Message error_response = new Message();
				error_response.prepareResponse(msg);
				error_response.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_INVALID_AVP_VALUE));
				addOurHostAndRealm(error_response);
				error_response.add(new AVP_FailedAVP(ex.avp));
				Utils.setMandatory_RFC3588(error_response);
				sendMessage(error_response,conn);
			}
			return false;
		}
		return true;
	}
	
	void initiateCER(Connection conn) {
		sendCER(conn);
	}
	public void sendCER(Connection conn)  {
		logger.log(Level.FINE,"Sending CER to "+conn.host_id);
		Message cer = new Message();
		cer.hdr.setRequest(true);
        // DEVOTEAM
		cer.hdr.command_code = ProtocolConstants.DIAMETER_COMMAND_CAPABILITIES_EXCHANGE;
		cer.hdr.application_id = ProtocolConstants.DIAMETER_APPLICATION_COMMON;
        cer.hdr.hop_by_hop_identifier = IDProvider.nextId();
        cer.hdr.end_to_end_identifier = IDProvider.nextId();
		addCEStuff(cer,settings.capabilities(),conn);
		Utils.setMandatory_RFC3588(cer);
	
		// Devoteam Configure the CER/CEA sending
		boolean autoCERCEAEnable = Config.getConfigByName("diameter.properties").getBoolean("capability.AUTO_CER_CEA_ENABLE", true);
		if (autoCERCEAEnable)
		{
			sendMessage(cer,conn);
		}
		else
		{
			handleCER(cer, conn);
		}
	}
	private void addCEStuff(Message msg, Capability capabilities, Connection conn) {
		//Origin-Host, Origin-Realm
		addOurHostAndRealm(msg);
		//Host-IP-Address
		Collection<InetAddress> local_addresses = conn.getLocalAddresses();
		for(InetAddress ia : local_addresses)
			msg.add(new AVP_Address(ProtocolConstants.DI_HOST_IP_ADDRESS, ia));
		//Vendor-Id
		msg.add(new AVP_Unsigned32(ProtocolConstants.DI_VENDOR_ID, settings.vendorId()));
		//Product-Name
		msg.add(new AVP_UTF8String(ProtocolConstants.DI_PRODUCT_NAME, settings.productName()));
		//Origin-State-Id
		msg.add(new AVP_Unsigned32(ProtocolConstants.DI_ORIGIN_STATE_ID,node_state.stateId()));
		//Error-Message, Failed-AVP: not in success
		//Supported-Vendor-Id
		for(Integer i : capabilities.supported_vendor) {
			msg.add(new AVP_Unsigned32(ProtocolConstants.DI_SUPPORTED_VENDOR_ID,i));
		}
		//Auth-Application-Id
		for(Integer i : capabilities.auth_app) {
			msg.add(new AVP_Unsigned32(ProtocolConstants.DI_AUTH_APPLICATION_ID,i));
		}
		//Inband-Security-Id
		//  todo
		//Acct-Application-Id
		for(Integer i : capabilities.acct_app) {
			msg.add(new AVP_Unsigned32(ProtocolConstants.DI_ACCT_APPLICATION_ID,i));
		}
		//Vendor-Specific-Application-Id
		for(Capability.VendorApplication va : capabilities.auth_vendor) {
			AVP g[] = new AVP[2];
			g[0] = new AVP_Unsigned32(ProtocolConstants.DI_VENDOR_ID,va.vendor_id);
			g[1] = new AVP_Unsigned32(ProtocolConstants.DI_AUTH_APPLICATION_ID,va.application_id);
			msg.add(new AVP_Grouped(ProtocolConstants.DI_VENDOR_SPECIFIC_APPLICATION_ID,g));
		}
		for(Capability.VendorApplication va : capabilities.acct_vendor) {
			AVP g[] = new AVP[2];
			g[0] = new AVP_Unsigned32(ProtocolConstants.DI_VENDOR_ID,va.vendor_id);
			g[1] = new AVP_Unsigned32(ProtocolConstants.DI_ACCT_APPLICATION_ID,va.application_id);
			msg.add(new AVP_Grouped(ProtocolConstants.DI_VENDOR_SPECIFIC_APPLICATION_ID,g));
		}
		//Firmware-Revision
		if(settings.firmwareRevision()!=0)
			msg.add(new AVP_Unsigned32(ProtocolConstants.DI_FIRMWARE_REVISION,settings.firmwareRevision()));
	}
	
	private boolean handleDWR(Message msg, Connection conn) {
		logger.log(Level.INFO,"DWR received from "+conn.host_id);
		conn.timers.markDWR();
		Message dwa = new Message();
		dwa.prepareResponse(msg);
		dwa.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_SUCCESS));
		addOurHostAndRealm(dwa);
		dwa.add(new AVP_Unsigned32(ProtocolConstants.DI_ORIGIN_STATE_ID,node_state.stateId()));
		Utils.setMandatory_RFC3588(dwa);
		
		sendMessage(dwa,conn);
		return true;
	}
	private boolean handleDWA(Message msg, Connection conn) {
		logger.log(Level.FINE,"DWA received from "+conn.host_id);
		conn.timers.markDWA();
		return true;
	}
	private boolean handleDPR(Message msg, Connection conn) {
		logger.log(Level.FINE,"DPR received from "+conn.host_id);
		Message dpa = new Message();
		dpa.prepareResponse(msg);
		dpa.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_SUCCESS));
		addOurHostAndRealm(dpa);
		Utils.setMandatory_RFC3588(dpa);
		
		sendMessage(dpa,conn);
		return false;
	}
	private boolean handleDPA(Message msg, Connection conn) {
		if(conn.state==Connection.State.closing)
			logger.log(Level.INFO,"Got a DPA from "+conn.host_id);
		else
			logger.log(Level.WARNING,"Got a DPA. This is not expected");
		return false; //in any case close the connection
	}
	private boolean handleUnknownRequest(Message msg, Connection conn) {
		logger.log(Level.INFO,"Unknown request received from "+conn.host_id);
		rejectRequest(msg,conn,ProtocolConstants.DIAMETER_RESULT_UNABLE_TO_DELIVER);
		return true;
	}
	
	private void sendDWR(Connection conn) {
		logger.log(Level.FINE,"Sending DWR to "+conn.host_id);
		Message dwr = new Message();
		dwr.hdr.setRequest(true);
		dwr.hdr.command_code = ProtocolConstants.DIAMETER_COMMAND_DEVICE_WATCHDOG;
		dwr.hdr.application_id = ProtocolConstants.DIAMETER_APPLICATION_COMMON;
		dwr.hdr.hop_by_hop_identifier = IDProvider.nextId();
		dwr.hdr.end_to_end_identifier = IDProvider.nextId();
		addOurHostAndRealm(dwr);
		dwr.add(new AVP_Unsigned32(ProtocolConstants.DI_ORIGIN_STATE_ID, node_state.stateId()));
		Utils.setMandatory_RFC3588(dwr);
		
		sendMessage(dwr,conn);
		
		conn.timers.markDWR_out();
	}
	
	private void sendDPR(Connection conn, int why) {
		logger.log(Level.FINE,"Sending DPR to "+conn.host_id);
		Message dpr = new Message();
		dpr.hdr.setRequest(true);
		dpr.hdr.command_code = ProtocolConstants.DIAMETER_COMMAND_DISCONNECT_PEER;
		dpr.hdr.application_id = ProtocolConstants.DIAMETER_APPLICATION_COMMON;
		dpr.hdr.hop_by_hop_identifier = IDProvider.nextId();
		dpr.hdr.end_to_end_identifier = IDProvider.nextId();
		addOurHostAndRealm(dpr);
		dpr.add(new AVP_Unsigned32(ProtocolConstants.DI_DISCONNECT_CAUSE, why));
		Utils.setMandatory_RFC3588(dpr);
		
		sendMessage(dpr,conn);
	}
	
	boolean anyOpenConnections(NodeImplementation node_impl) {
		synchronized(map_key_conn) {
			for(Map.Entry<ConnectionKey,Connection> e : map_key_conn.entrySet()) {
				Connection conn = e.getValue();
				if(conn.node_impl==node_impl)
					return true;
			}
		}
		return false;
	}
	void registerInboundConnection(Connection conn) {
		synchronized(map_key_conn) {
			map_key_conn.put(conn.key,conn);
		}
	}
	void unregisterConnection(Connection conn) {
		synchronized(map_key_conn) {
			map_key_conn.remove(conn.key);
		}
	}
	Object getLockObject() {
		return map_key_conn;
	}
}
