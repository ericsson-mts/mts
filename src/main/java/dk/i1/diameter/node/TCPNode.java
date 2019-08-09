package dk.i1.diameter.node;
import java.nio.channels.*;
import java.nio.ByteBuffer;
import java.net.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Iterator;
import dk.i1.diameter.Message;

class TCPNode extends NodeImplementation {
	private Thread node_thread;
	private Selector selector;
	private ServerSocketChannel serverChannel;
	private boolean please_stop;
	private long shutdown_deadline;
	public TCPNode(Node node, NodeSettings settings, Logger logger) {
		super(node,settings,logger);
	}
	
	void openIO() throws java.io.IOException {
		// create a new Selector for use below
		selector = Selector.open();
		if(settings.port()!=0) {
			// allocate an unbound server socket channel
			serverChannel = ServerSocketChannel.open();
			// Get the associated ServerSocket to bind it with
			ServerSocket serverSocket = serverChannel.socket();
			// set the port the server channel will listen to
			serverSocket.bind(new InetSocketAddress (settings.port()));
		}
	}
	
	void start() {
		logger.log(Level.FINEST,"Starting TCP node");
		please_stop = false;
		node_thread = new SelectThread();
		node_thread.setDaemon(true);
		node_thread.start();
		logger.log(Level.FINEST,"Started TCP node");
	}
	
	void wakeup() {
		logger.log(Level.FINEST,"Waking up selector thread");
		selector.wakeup();
	}
	
	void initiateStop(long shutdown_deadline) {
		logger.log(Level.FINEST,"Initiating stop of TCP node");
		please_stop = true;
		this.shutdown_deadline = shutdown_deadline;
		logger.log(Level.FINEST,"Initiated stop of TCP node");
	}
	
	void join() {
		logger.log(Level.FINEST,"Joining selector thread");
		try {
			node_thread.join();
		} catch(java.lang.InterruptedException ex) {}
		node_thread = null;
		logger.log(Level.FINEST,"Selector thread joined");
	}
	
	void closeIO() {
		logger.log(Level.FINEST,"Closing server channel, etc.");
		if(serverChannel!=null) {
			try {
				serverChannel.close();
			} catch(java.io.IOException ex) {}
		}
		serverChannel=null;
		try {
			selector.close();
		} catch(java.io.IOException ex) {}
		selector = null;
		logger.log(Level.FINEST,"Closed selector, etc.");
	}
	
	private class SelectThread extends Thread {
	    public SelectThread() {
			super("DiameterNode thread (TCP)");
		}
	    public void run() {
			try {
				run_();
				if(serverChannel!=null)
					serverChannel.close();
			} catch(java.io.IOException ex) {}
		}
	    private void run_() throws java.io.IOException {
		if(serverChannel!=null) {
			// set non-blocking mode for the listening socket
			serverChannel.configureBlocking(false);
			
			// register the ServerSocketChannel with the Selector
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		}
		
		for(;;) {
			if(please_stop) {
				if(System.currentTimeMillis()>=shutdown_deadline)
					break;
				if(!anyOpenConnections())
					break;
			}
			long timeout = calcNextTimeout();
			int n;
			//System.out.println("selecting...");
			if(timeout!=-1) {
				long now=System.currentTimeMillis();
				if(timeout>now)
					n = selector.select(timeout-now);
				else
					n = selector.selectNow();
			} else
				n = selector.select();
			//System.out.println("Woke up from select()");
			
			// get an iterator over the set of selected keys
			Iterator it = selector.selectedKeys().iterator();
			// look at each key in the selected set
			while(it.hasNext()) {
				SelectionKey key = (SelectionKey)it.next();
				
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
			
			runTimers();
		}
		
		//Remaining connections are close by Node instance
		
		//selector is closed in stop()
	    }
	}
	
	private void handleReadable(TCPConnection conn) {
		logger.log(Level.FINEST,"handlereadable()...");
		conn.makeSpaceInNetInBuffer();
		ConnectionBuffers connection_buffers = conn.connection_buffers;
		logger.log(Level.FINEST,"pre: conn.in_buffer.position=" + connection_buffers.netInBuffer().position());
 		int count;
		try {
			int loop_count=0;
	 		while((count=conn.channel.read(connection_buffers.netInBuffer()))>0 && loop_count++<3) {
				logger.log(Level.FINEST,"readloop: connection_buffers.netInBuffer().position=" + connection_buffers.netInBuffer().position());
				conn.makeSpaceInNetInBuffer();
			}
		} catch(java.io.IOException ex) {
			logger.log(Level.FINE,"got IOException",ex);
			closeConnection(conn);
			return;
		}
		conn.processNetInBuffer();
		processInBuffer(conn);
 		if(count<0 && conn.state!=Connection.State.closed) {
			logger.log(Level.FINE,"count<0");
			closeConnection(conn);
			return;
 		}
	}
	
	private void processInBuffer(TCPConnection conn) {
		ByteBuffer app_in_buffer = conn.connection_buffers.appInBuffer();
		logger.log(Level.FINEST,"pre: app_in_buffer.position=" + app_in_buffer.position());
		int raw_bytes=app_in_buffer.position();
		byte[] raw = new byte[raw_bytes];
		app_in_buffer.position(0);
		app_in_buffer.get(raw);
		app_in_buffer.position(raw_bytes);
		int offset=0;
		//System.out.println("processInBuffer():looping");
		while(offset<raw.length) {
			//System.out.println("processInBuffer(): inside loop offset=" + offset);
			int bytes_left = raw.length-offset;
			if(bytes_left<4) break;
			int msg_size = Message.decodeSize(raw,offset);
			if(bytes_left<msg_size) break;
			Message msg = new Message();
			Message.decode_status status = msg.decode(raw,offset,msg_size);
			//System.out.println("processInBuffer():decoded, status=" + status);
			switch(status) {
				case decoded: {
					logRawDecodedPacket(raw,offset,msg_size);
					offset += msg_size;
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
					logGarbagePacket(conn,raw,offset,msg_size);
					closeConnection(conn,true);
					return;
			}
			if(status==Message.decode_status.not_enough) break;
		}
		conn.consumeAppInBuffer(offset);
		//System.out.println("processInBuffer(): the end");
	}
	private void handleWritable(Connection conn_) {
		TCPConnection conn = (TCPConnection)conn_;
		logger.log(Level.FINEST,"handleWritable():");
		ByteBuffer net_out_buffer = conn.connection_buffers.netOutBuffer();
		//int bytes = net_out_buffer.position();
		//net_out_buffer.rewind();
		//net_out_buffer.limit(bytes);
		net_out_buffer.flip();
		//logger.log(Level.FINEST,"                :bytes= " + bytes);
		int count;
		try {
			count = conn.channel.write(net_out_buffer);
			if(count<0) {
				closeConnection(conn);
				return;
			}
			//conn.consumeNetOutBuffer(count);
			net_out_buffer.compact();
			conn.processAppOutBuffer();
			if(!conn.hasNetOutput())
				conn.channel.register(selector, SelectionKey.OP_READ, conn);
		} catch(java.io.IOException ex) {
			closeConnection(conn);
			return;
		}
	}
	
	void sendMessage(TCPConnection conn, byte[] raw) {
		boolean was_empty = !conn.hasNetOutput();
		conn.makeSpaceInAppOutBuffer(raw.length);
		//System.out.println("sendMessage: A: position=" + out_buffer.position() + " limit=" + conn.out_buffer.limit());
		conn.connection_buffers.appOutBuffer().put(raw);
		conn.connection_buffers.processAppOutBuffer();
		//System.out.println("sendMessage: B: position=" + out_buffer.position() + " limit=" + conn.out_buffer.limit());
		
		if(was_empty)
			outputBecameAvailable(conn);
	}
	
	private void outputBecameAvailable(Connection conn_) {
		TCPConnection conn = (TCPConnection)conn_;
		handleWritable(conn);
		if(conn.hasNetOutput()) {
			try {
				conn.channel.register(selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE, conn);
			} catch(java.nio.channels.ClosedChannelException ex) { }
		}
	}
	
	boolean initiateConnection(Connection conn_, Peer peer) {
		TCPConnection conn = (TCPConnection)conn_;
		try {
			SocketChannel channel = SocketChannel.open();
			channel.configureBlocking(false);
			InetSocketAddress address = new InetSocketAddress(peer.host(),peer.port());
			try {
				if(channel.connect(address)) {
					//This only happens on Solaris when connecting locally
					logger.log(Level.FINEST,"Connected!");
					conn.state = Connection.State.connected_out;
					conn.channel = channel;
					selector.wakeup();
					channel.register(selector, SelectionKey.OP_READ, conn);
					initiateCER(conn);
					return true;
				}
			} catch(java.nio.channels.UnresolvedAddressException ex) {
				channel.close();
				return false;
			}
			conn.state = Connection.State.connecting;
			conn.channel = channel;
			selector.wakeup();
			channel.register(selector, SelectionKey.OP_CONNECT, conn);
		} catch(java.io.IOException ex) {
			logger.log(Level.WARNING,"java.io.IOException caught while initiating connection to '" + peer.host() +"'.", ex);
		}
		return true;
	}
	
	void close(Connection conn_, boolean reset) {
		TCPConnection conn = (TCPConnection)conn_;
		try {
			conn.channel.register(selector, 0);
			if(reset) {
				//Set lingertime to zero to force a RST when closing the socket
				//rfc3588, section 2.1
				conn.channel.socket().setSoLinger(true,0);
			}
			conn.channel.close();
		} catch(java.io.IOException ex) {}
	}
	
	Connection newConnection(long watchdog_interval, long idle_timeout) {
		return new TCPConnection(this,watchdog_interval,idle_timeout);
	}

}
