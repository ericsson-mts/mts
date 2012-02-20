package dk.i1.sctp;
import java.net.*;
import java.util.Collection;
import java.util.ArrayList;

/** An SCTP socket
An SCTP socket behaves in some ways like a TCP socket and in some ways like
a datagram socket.
<p>You cannot instantiate a SCTPSocket, but rather one of its two subclasses: {@link OneToOneSCTPSocket} and {@link OneToManySCTPSocket}.

 */
public class SCTPSocket extends Socket{/* extends Socket added by Devoteam */
	static {
		System.loadLibrary("dk_i1_sctp");
		init();
	}
	private static native void init();
	private long impl;
	
	public SCTPSocket(boolean one_to_many) throws SocketException {		 //Devoteam
		impl=0;
		open(one_to_many);
	}
	public SCTPSocket(boolean one_to_many, int port) throws SocketException { //Devoteam
		impl=0;
		open(one_to_many);
		bind(port);
	}
	protected void finalize() throws Throwable {
		try {
			close();
		} finally {
			super.finalize();
		}
	}
	private native void open(boolean one_to_many) throws SocketException;
	
	/** Bind to an unspecified address/port.
	 * Binds the socket to a system-decided port and set of addresses.
	 * A socket can only be bound once.
	 */
	public void bind() throws SocketException {
		bind(0);
	}
	/** Bind to a specific port.
	 * Binds the socket to the specified port, and system-decided set of addresses.
	 * A socket can only be bound once.
	 *@param port the SCTP port to bind to.
	 */
	public void bind(int port) throws SocketException {
		bind((byte[])null,port);
	}
	/** Bind to a specific socket address.
	 * A socket can only be bound once.
	* @deprecated An important feature of SCTP is multi-homing. This method binds the socket to a single interface and effectively disables multi-homing. This method is only here for completeness' sake.
	 */
	public void bind(InetSocketAddress bindaddr) throws SocketException {
		bind(bindaddr.getAddress(),bindaddr.getPort());
	}
	/** Bind the socket to a spcific port/address.
	 * A socket can only be bound once.
	* @deprecated An important feature of SCTP is multi-homing. This method binds the socket to a single interface and effectively disables multi-homing. This method is only here for completeness' sake.
	 */
	public void bind(InetAddress addr, int port) throws SocketException {
		bind(addr.getAddress(),port);
	}
	private void bind(byte[] addr, int port) throws SocketException {
		bind_native(addr,port);
	}
	private native void bind_native(byte[] addr, int port) throws SocketException;
	
	/**Close the socket.
	 * Closes the socket. It is better to close the socket explicitly than rely or Java's GC.
	 * note: This method waits for any outstanding receive() calls to finish.
	 */
	public native void close() throws SocketException;
	
	/**Returns the closed state of the socket.
	 *@since 0.5.3
	 */
	public native boolean isClosed();
	
	/** Subscribe to specific SCTP notifications.
	 *
	 */
	public void subscribeEvents(sctp_event_subscribe ses) throws SocketException {
		subscribeEvents(ses.sctp_data_io_event,
		                ses.sctp_association_event,
		                ses.sctp_address_event,
		                ses.sctp_send_failure_event,
		                ses.sctp_peer_error_event,
		                ses.sctp_shutdown_event,
		                ses.sctp_partial_delivery_event,
		                ses.sctp_adaptation_layer_event
			       );
	}
	private native void subscribeEvents(boolean sctp_data_io_event,
	                                    boolean sctp_association_event,
	                                    boolean sctp_address_event,
	                                    boolean sctp_send_failure_event,
	                                    boolean sctp_peer_error_event,
	                                    boolean sctp_shutdown_event,
	                                    boolean sctp_partial_delivery_event,
	                                    boolean sctp_adaptation_layer_event
					   ) throws SocketException;
	
	/**Configure the auto-close feature.
	 *Configures the auto-close timer, or disables it.
	 *@param seconds Number of seconds without traffic before automatically closing associations. 0 means disable.
	 */
	public native void configureAutoClose(int seconds) throws SocketException;
	
	/**Set the blocking mode.
	 *The blocking mode on SCTP sockets affects only two operations: connect() and send().
	 *When configured in non-blocking connect() will return before the association is established and you should subscribe to the sctp_association_event (see {@link #subscribeEvents}).
	 *When configured in non-blocking send() may throw the {@link WouldBlockException} exception if the send call would block (probably due to OS buffers being full).
	 *@param block If true then send() and connect() any block. If false then send() and connect() will not block
	 */
	public native void configureBlocking(boolean block) throws SocketException;
	
	/**Tells whether send() and connect() may block.
	 */
	public native boolean isBlocking() throws SocketException;
	
	/**Enable/disable SCTP_NODELAY (disable/enable Nagle's algorithm).
	 * @param on   true to enable TCP_NODELAY, false to disable.
	 * @since 0.5.4
	 */
	public native void setSctpNoDelay(boolean on) throws SocketException;
	/**Tests if SCTP_NODELAY is enabled.
	 * @return a boolean indicating whether or not SCTP_NODELAY is enabled.
	 * @since 0.5.4
	 */
	public native boolean getSctpNoDelay() throws SocketException;
	
	/**Get the SO_RCVBUF option for the socket.
	 * @since 0.5.7
	*/
	public native int getReceiveBufferSize() throws SocketException;
	/**Get the SO_SNDBUF option for the socket.
	 *@since 0.5.7
	 */
	public native int getSendBufferSize() throws SocketException;
	/**Sets the SO_RCVBUF option for this socket.
	 *@since 0.5.7
	 */
	public native void setReceiveBufferSize(int size) throws SocketException;
	/**Sets the SO_SNDBUF option for this socket.
	 *@since 0.5.7
	 */
	public native void setSendBufferSize(int size) throws SocketException;
	
	
	/** Set the parameters for a peer.
	 * Sets the parameters for a peer, heartbeat interval among others.
	 */
	public void setPeerParameters(sctp_paddrparams spp) throws SocketException {
		setPeerParameters_native(spp.spp_assoc_id.id,
		                         spp.spp_address==null?null:spp.spp_address.getAddress().getAddress(),
		                         spp.spp_address==null?0:spp.spp_address.getPort(),
		                         spp.spp_hbinterval,
		                         spp.spp_pathmaxrxt,
		                         spp.spp_pathmtu,
		                         spp.spp_sackdelay,
		                         spp.spp_flags,
		                         spp.spp_ipv6_flowlabel,
		                         spp.spp_ipv4_tos
					);
	}
	private native void setPeerParameters_native(long  spp_assoc_id,
	                                             byte[] spp_address_raw,
	                                             int spp_address_port,
	                                             int spp_hbinterval,
	                                             short spp_pathmaxrxt,
	                                             int spp_pathmtu,
	                                             int spp_sackdelay,
	                                             int spp_flags,
	                                             int spp_ipv6_flowlabel,
	                                             byte spp_ipv4_tos
	                                            ) throws SocketException;
	
	/**Set default association parameters.
	 *Setting initialization parameters is effective only on an unconnected
	 *socket (for one-to-many style sockets only future associations are
	 *effected by the change).  With one-to-one style sockets, this option
	 *is inherited by sockets derived from a listener socket.
	 *@since 0.5.6
	 */
	public void setInitMsg(sctp_initmsg im) throws SocketException {
		setInitMsg_native(im.sinit_num_ostreams, im.sinit_max_instreams,
		                  im.sinit_max_attempts, im.sinit_max_init_timeo);
	}
	private native void setInitMsg_native(short sinit_num_ostreams,
	                                      short sinit_max_instreams,
	                                      short sinit_max_attempts,
	                                      short sinit_max_init_timeo)
					     throws SocketException;
	
	/**Enable unbound connections.
	 *Until listen() has been called unbound associations are not accepted.
	 */
	public native void listen() throws SocketException;
	
	/**Create an association to a peer.
	 * Creates an association to a peer. Explicit association creation is
	 * not needed, but probably a good idea.
	 */
	public void connect(InetSocketAddress addr) throws SocketException {
		connect(addr.getAddress(),addr.getPort());
	}
	/**Create an association to a peer.
	 * Creates an association to a peer. Explicit association creation is
	 * not needed, but probably a good idea.
	 */
	public void connect(InetAddress addr, int port) throws SocketException {
		connect(addr.getAddress(),port);
	}
	private void connect(byte[] addr, int port) throws SocketException {
		connect_native(addr,port);
	}
	private native void connect_native(byte[] addr, int port) throws SocketException;
	
	/**Shut down an association.
	 */
	public void disconnect(AssociationId assoc_id) throws SocketException {
		disconnect_native(assoc_id.id,true);
	}
	/**Shut down an association.
	 *@param assoc_id The association to shut down
	 *@param gracefully Controls whether the association is shut down gracefully with SCTP_EOF, or with SCTP_ABORT.
	 */
	public void disconnect(AssociationId assoc_id, boolean gracefully) throws SocketException {
		disconnect_native(assoc_id.id,gracefully);
	}
	private native void disconnect_native(long assoc_id, boolean gracefully) throws SocketException;
	
	
	/**Get local addressed uses by the socket.
	 *Retrieves the list of local addresses the socket can be contacted on.
	 *The list usually includes loopback-addresses, public ip-address and link-local addresses.
	 */
	public Collection<InetAddress> getLocalInetAddresses() throws SocketException {
		return getLocalInetAddresses(AssociationId.default_);
	}
	/**Get local addresses used by the socket.
	 *Retrieves the list of local addresses the socket can be contacted on.
	 *The list usually includes loopback-addresses, public ip-address and link-local addresses.
	 */
	public Collection<InetAddress> getLocalInetAddresses(AssociationId assoc_id) throws SocketException {
		Collection<InetAddress> col = new ArrayList<InetAddress>();
		getLocalInetAddresses_native(col,assoc_id.id);
		return col;
	}
	private native void getLocalInetAddresses_native(Collection<InetAddress> col, long assoc_id);
	
	/**Get local port.
	 * Retrieves the local port the socket is bound to. This is useful if you
	 * did not specify a specific port to bind to in the call to bind().
	 */
 	public native int getLocalInetPort() throws SocketException;
	
	/**Get addresses of a peer.
	 *Retrieves a list of known addresses of a peer, including non-reachable addresses.
	*/
	public Collection<InetAddress> getPeerInetAddresses(AssociationId assoc_id) {
 		Collection<InetAddress> col = new ArrayList<InetAddress>();
 		getPeerInetAddresses_native(col,assoc_id.id);
 		return col;
 	}
 	private native void getPeerInetAddresses_native(Collection<InetAddress> col, long assoc_id);
	
	public int getPeerInetPort(AssociationId assoc_id) throws SocketException {
		return getPeerInetPort_native(assoc_id.id);
	}
	private native int getPeerInetPort_native(long aid) throws SocketException;
	
	/**Probe if any unread chunks are pending
	 *@return true if any chunks are pending reading
	 */
	public native boolean chunkAvailable() throws SocketException;
	
	/**Receive a data chunk or a notification. 
	 *Implemented as <tt>receive(0)</tt>
	 *@return a SCTPChunk, or, in some cases, null
	 */
	public SCTPChunk receive() throws SocketException {
		return receive_native(0);
	}
	/**Receive a data chunk or a notification.
	 *If no events are available the call returns immediately with null.
	 *@return a SCTPChunk, or, in some cases, null
	 */
	public SCTPChunk receiveNow() throws SocketException {
		return receive_native(-1);
	}
	/**Receive a data chunk or a notification.
	 *@param timeout Milliseconds to wait for an event. 0 means infinite. Must not be negative.
	 *@return a SCTPChunk, or, in some cases, null
	 */
	public SCTPChunk receive(long timeout) throws SocketException, IllegalArgumentException {
		if(timeout<0)
			throw new IllegalArgumentException();
		return receive_native(timeout);
	}
	private native SCTPChunk receive_native(long timeout) throws SocketException;
	
	/**Send a datagram to a peer.
	 * Sends the data chunk to a peer. The peer is specified in sctpdata.sndrcvinfo.sinfo_assoc_id.
	 * <p>Example:</p>
	 <pre>
	SCTPData data = new SCTPData(<i>raw_byte_array</i>);
	data.sndrcvinfo.sinfo_assoc_id = <i>destination_association_id</i>;
	sctp_socket.send(data);
	</pre>
	 *@param sctpdata the data chunk to send.
	 *@throws SocketException if a socket error occurs.
	 *@throws WouldBlockException if the socket is non-blocking and outgoing OS buffers are full.
	 */
	public void send(SCTPData sctpdata) throws SocketException, WouldBlockException {
		send_native(sctpdata.data,sctpdata.sndrcvinfo);
	}
	private native void send_native(byte[] data, sctp_sndrcvinfo sndrcvinfo) throws SocketException, WouldBlockException;
	
	/**Wake other threads from receive().
	 *Wakes 1 thread that is currently blocking in a receive() call.
	 *This method is only meant for clean shutdown, and is not as nice and the equivalent method in java.nio.Selector.
	 *The woken thread will return null.
	 */
	public native void wakeup() throws SocketException;
	
	
	
}
