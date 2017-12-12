package dk.i1.diameter.node;
import dk.i1.diameter.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * A Node manager.
 * The NodeManager class manages a Node instance and keeps track of connections
 * and in-/our-going messages and their end-to-end and hop-by-hop identifiers.
 * You can build proxies, redirect agents, servers and clients on top of it.
 * NodeManager is meant to be subclassed and subclasses should override
 * handleRequest() and handleAnswer()
 *
 * <p>If your needs are even simpler then have a look at {@link SimpleSyncClient} and {@link dk.i1.diameter.session.SessionManager}
 * <p>
 * NodeManager instances logs with the name "dk.i1.diameter.node", so you can
 * get detailed logging (including hex-dumps of incoming and outgoing packets)
 * by putting "dk.i1.diameter.node.level = ALL" into your log.properties
 * file (or equivalent)
 *
 * <p>What happens when acting as a server:
 * <ol>
 * <li>The Node instance receives a message (request)</li>
 * <li>The message is passed to handleRequest()</li>
 * <li>handleRequest() processes the message (this is where your code is meant to be)</lI>
 * <li>handleRequest() creates an answer message. Among other things it uses {@link Message#prepareAnswer(Message)}
 * <li>answer() is called</li>
 * <li>The answer is passed down to the Node instance which then sends or queues the message</li>
 * </ol>
 * <p>What happens when acting as a client:
 * <ol>
 * <li>sendRequest(Message,Peer[],Object) is called</li>
 * <li>The request is passed down to the Node instance which sends or queues it</li>
 * <li>The sendRequest() call returns</li>
 * <li>Some time passes</li<
 * <li>The Node instance receives the answer</li>
 * <li>The answer is passed to handleAnswer(). This is where your code is meant to be)
 * </ol>
 * <p>What happens when acting as a proxy or a relay:
 * <ol>
 * <li>The Node instance receives a message (request)</li>
 * <li>The message is passed to handleRequest()</li>
 * <li>Your implementation of handleRequest() decides to forward the request</li>
 * <li>forwardRequest() is called with a state object that among other things remembers the ConnectionKey and the hop-by-hop identifier</li>
 * <li>The request is passed down to the Node instance which sends or queues it</li>
 * <li>The forwardRequest() call returns</li>
 * <li>The handleRequest() returns</li>
 * <li>Some time passes</li>
 * <li>The Node instance receives the answer</li>
 * <li>The answer is passed to handleAnswer(). This is where your code is meant to be)
 * <li>Your code detects that the answer must be forward back to where the request came from.</lI>
 * <li>Your code restores the hop-by-hop identifier</lI>
 * <li>forwardAnswer() is called.</li>
 * <li>The answer is passed down to the Node instance which then sends or queues the message</li>
 * </ol>
 */
public class NodeManager implements MessageDispatcher, ConnectionListener {
	private Node node;
	private NodeSettings settings;
	private Map<ConnectionKey,Map<Integer,Object> > req_map;
	private Logger logger;
	
	/**
	 * Constructor for NodeManager.
	 * A Node instance is constructed using the specified settings, and
	 * the internal state is initialized.
	 */
	public NodeManager(NodeSettings settings) {
		this(settings,null);
	}
	/**
	 * Constructor for NodeManager.
	 * A Node instance is constructed using the specified settings, node
	 * validator (can be null) and the internal state is initialized.
	 * @since 0.9.4
	 */
	public NodeManager(NodeSettings settings, NodeValidator node_validator) {
		node = new Node(this,this,settings,node_validator);
		this.settings = settings;
		req_map = new HashMap<ConnectionKey,Map<Integer,Object> >();
		this.logger = Logger.getLogger("dk.i1.diameter.node");
	}
	
	/**
	 * Start the node manager.
	 * Starts the embedded Node.
	 * For details about {@link UnsupportedTransportProtocolException} see {@link Node#start}
	 */
	public void start() throws java.io.IOException, UnsupportedTransportProtocolException {
		node.start();
	}
	/**
	 * Stop the node manager immediately.
	 * Implemented as stop(0)
	 */
	public void stop() {
		stop(0);
	}
	/**
	 * Stop the node manager.
	 * Stops the embedded Node and call handleAnswer() with null messages for outstanding requests.
	 * @param grace_time Maximum time (milliseconds) to wait for connections to close gracefully.
	 * @since grace_time parameter introduced in 0.9.3
	 */
	public void stop(long grace_time) {
		node.stop(grace_time);
		synchronized(req_map) {
			for(Map.Entry<ConnectionKey,Map<Integer,Object>> e_c : req_map.entrySet()) {
				ConnectionKey connkey = e_c.getKey();
				for(Map.Entry<Integer,Object> e_s : e_c.getValue().entrySet()) {
					handleAnswer(null,connkey,e_s.getValue());
				}
			}
		}
		//Fastest way to clear it...
		req_map = new HashMap<ConnectionKey,Map<Integer,Object> >();
	}
	
	/**
	 * Wait until at least one connection has been established.
	 * Waits until at least one connection to a peer has been established
	 * and capability-exchange has finished.
	 * @since 0.9.1
	 */
	public void waitForConnection() throws InterruptedException {
		node.waitForConnection();
	}
	/**
	 * Wait until at least one connection has been established or until the timeout expires.
	 * Waits until at least one connection to a peer has been established
	 * and capability-exchange has finished, or the specified timeout has expired.
	 * @param timeout The maximum time to wait in milliseconds.
	 * @since 0.9.1
	 */
	public void waitForConnection(long timeout) throws InterruptedException {
		node.waitForConnection(timeout);
	}
	
	/**Returns the embedded node*/
	public Node node() { return node; }
	/**Returns the node settings*/
	public NodeSettings settings() { return settings; }
	
	
	//events:
	//    in request
	//        respond
	//        forward
	//    in answer
	//        consume (for locally generated request)
	//        forward answer
	
	/**
	 * Handle a request.
	 * This method is called when a request arrives. It is meant to be
	 * overridden by a subclass. This implementation rejects all requests.
	 * <p>
	 * Please note that the handleRequest() method is called by the
	 * networking thread and messages from other peers cannot be received
	 * until the method returns. If the handleRequest() method needs to do
	 * any lengthy processing then it should implement a message queue, put
	 * the message into the queue, and return. The requests can then be
	 * processed by a worker thread pool without stalling the networking layer.
	 * @param request The incoming request.
	 * @param connkey The connection from where the request came.
	 * @param peer The peer that sent the request. This is not the originating peer but the peer directly connected to us that sent us the request.
	 */
	protected void handleRequest(Message request, ConnectionKey connkey, Peer peer) {
		// incoming requests are not expected by this node
		Message answer = new Message();
		logger.log(Level.FINE,"Handling incoming request, command_code="+request.hdr.command_code+", peer="+peer.host()+", end2end="+request.hdr.end_to_end_identifier+", hopbyhop="+request.hdr.hop_by_hop_identifier);
		answer.prepareResponse(request);
		answer.hdr.setError(true);
		answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_UNABLE_TO_DELIVER));
		node.addOurHostAndRealm(answer);
		Utils.copyProxyInfo(request,answer);
		Utils.setMandatory_RFC3588(answer);
		try {
			answer(answer,connkey);
		} catch(NotAnAnswerException ex) {}
	}
	/**
	 * Handle an answer.
	 * This method is called when an answer arrives. It is meant to be overridden in a subclass.
	 * <p>
	 * Please note that the handleAnswer() method is called by the
	 * networking thread and messages from other peers cannot be received
	 * until the method returns. If the handleAnswer() method needs to do
	 * any lengthy processing then it should implement a message queue, put
	 * the message into the queue, and return. The answers can then be
	 * processed by a worker thread pool without stalling the networking layer.
	 * @param answer The answer message. Null if the connection broke.
	 * @param answer_connkey The connection from where the answer came.
	 * @param state The state object passed to sendRequest() or forwardRequest()
	 */
	protected void handleAnswer(Message answer, ConnectionKey answer_connkey, Object state) {
		//default implementation: silently discard
		logger.log(Level.FINE,"Handling incoming answer, command_code="+answer.hdr.command_code+", end2end="+answer.hdr.end_to_end_identifier+", hopbyhop="+answer.hdr.hop_by_hop_identifier);
	}
	
	/**
	 * Answer a request.
	 * The answer is sent to the connection. If the connection has been lost in the meantime it is ignored.
	 * @param answer The answer message.
	 * @param connkey The connection to send the answer to.
	 * @throws NotAnAnswerException If the answer has the R bit set in the header.
	 */
	protected final void answer(Message answer, ConnectionKey connkey) throws NotAnAnswerException {
		if(answer.hdr.isRequest())
			throw new NotAnAnswerException();
		try {
			node.sendMessage(answer,connkey);
		} catch(StaleConnectionException e) {}
	}
	/**
	 * Forward a request.
	 * Forward the request to the specified connection. The request will
	 * automatically get a route-record added if not already present.
	 * This method is meant to be called from handleRequest().
	 * @param request The request to forward
	 * @param connkey The connection to use
	 * @param state A state object that will be passed to handleAnswer() when the answer arrives. You should remember the ingoing connection and hop-by-hop identifier
	 * @throws NotARequestException If the request does not have the R bit set in the header.
	 * @throws NotProxiableException If the request does not have the P bit set in the header.
	 * @throws StaleConnectionException If the ConnectionKey refers to a lost connection.
	 */
	protected final void forwardRequest(Message request, ConnectionKey connkey, Object state) throws StaleConnectionException, NotARequestException, NotProxiableException {
		if(!request.hdr.isProxiable())
			throw new NotProxiableException();
		boolean our_route_record_found = false;
		String our_host_id = settings.hostId();
		for(AVP a:request.subset(ProtocolConstants.DI_ROUTE_RECORD)) {
			if(new AVP_UTF8String(a).queryValue().equals(our_host_id)) {
				our_route_record_found = true;
				break;
			}
		}
		if(!our_route_record_found) {
			//add a route-record
			request.add(new AVP_UTF8String(ProtocolConstants.DI_ROUTE_RECORD,settings.hostId()));
		}
		//send it
		sendRequest(request,connkey,state);
	}
	/**
	 * Forward an answer.
	 * Forward the answer to to the specified connection. The answer will automatically get a route-record added.
	 * This method is meant to be called from handleAnswer().
	 * Remember to restore the hop-by-hop-identifier on the message before calling this function.
	 * @param answer The answer to forward
	 * @param connkey The connection to use
	 * @throws NotAnAnswerException If the answer has the R bit set in the header.
	 * @throws NotProxiableException If the answer does not have the P bit set in the header. <strong>This indicates that there is something completely wrong with either the message, the peer or your application</strong>
	 * @throws StaleConnectionException If the ConnectionKey refers to a lost connection.
	 */
	protected final void forwardAnswer(Message answer, ConnectionKey connkey) throws StaleConnectionException, NotAnAnswerException, NotProxiableException {
		if(!answer.hdr.isProxiable())
			throw new NotProxiableException();
		if(answer.hdr.isRequest())
			throw new NotAnAnswerException();
		//add a route-record
		answer.add(new AVP_UTF8String(ProtocolConstants.DI_ROUTE_RECORD,settings.hostId()));
		//send it
		answer(answer,connkey);
	}
	/**
	 * Sends a request.
	 * A request initiated by this node is sent to the specified connection.
	 * The hop-by-hop identifier of the message is set. This is not symmetric with the other sendRequest method.
	 * @param request The request.
	 * @param connkey The connection to use.
	 * @param state A state object that will be passed to handleAnswer() when the answer arrives.
	 * @throws NotARequestException If the request does not have the R bit set in the header.
	 * @throws StaleConnectionException If the ConnectionKey refers to a lost connection.
	 */
	public final void sendRequest(Message request, ConnectionKey connkey, Object state) throws StaleConnectionException, NotARequestException {
		if(!request.hdr.isRequest())
			throw new NotARequestException();
		//request.hdr.hop_by_hop_identifier = node.nextHopByHopIdentifier(connkey);
		//remember state
		synchronized(req_map) {
			Map<Integer,Object> e_c = req_map.get(connkey);
			if(e_c==null) throw new StaleConnectionException();
			e_c.put(request.hdr.hop_by_hop_identifier,state);
		}
		try {
			node.sendMessage(request,connkey);
			logger.log(Level.FINER,"Request sent, command_code="+request.hdr.command_code+" hop_by_hop_identifier="+request.hdr.hop_by_hop_identifier);
		} catch(StaleConnectionException e) {
			synchronized(req_map) {
				req_map.remove(request.hdr.hop_by_hop_identifier);
			}
			throw e;
		}
	}
	/**
	 * Sends a request.
	 * The request is sent to one of the peers and an optional state object is remembered.
	 * Please note that handleAnswer() for this request may get called before this method returns. This can happen if the peer is very fast and the OS thread scheduler decides to schedule the networking thread.
	 * The end-to-end identifier of the message is set. This is not symmetric with the other sendRequest method.
	 * @param request The request to send.
	 * @param peers The candidate peers
	 * @param state A state object to be remembered. This will be passed to the handleAnswer() method when the answer arrives.
	 * @throws NotARequestException If the request does not have the R bit set in the header.
	 * @throws NotRoutableException If the message could not be sent to any of the peers.
	 */
	public final void sendRequest(Message request, Peer peers[], Object state) throws NotRoutableException, NotARequestException {
		logger.log(Level.FINER,"Sending request (command_code="+request.hdr.command_code+") to "+peers.length+" peers");
		//request.hdr.end_to_end_identifier = node.nextEndToEndIdentifier();
		boolean any_peers = false;
		boolean any_capable_peers=false;
		for(Peer p : peers) {
			any_peers = true;
			logger.log(Level.FINER,"Considering sending request to "+p.host());
			ConnectionKey connkey = node.findConnection(p);
			if(connkey==null) continue;
			Peer p2 = node.connectionKey2InetAddress(connkey).peer;
			if(p2==null) continue;
			if(!node.isAllowedApplication(request,p2)) {
				logger.log(Level.FINER,"peer "+p.host()+" cannot handle request");
				continue;
			}
			any_capable_peers=true;
			try {
				sendRequest(request,connkey,state);
				return;
			} catch (StaleConnectionException e) {
				//ok
			}
			logger.log(Level.FINE,"Setting retransmit bit");
			request.hdr.setRetransmit(true);
		}
		if(any_capable_peers)
			throw new NotRoutableException("All capable peer connections went stale");
		else if(any_peers)
			throw new NotRoutableException("No capable peers");
		else
			throw new NotRoutableException();
	}

	//messagedispatcher
	/**
	 * Handle an incoming message.
	 * This implementation calls handleRequest(), or matches an answer to an outstanding request and calls handleAnswer().
	 * Subclasses should not override this method.
	 */
	// Devoteam need to inherit this method in the DiameterNodeManger class
	// public final boolean handle(Message msg, ConnectionKey connkey, Peer peer) {
	public boolean handle(Message msg, ConnectionKey connkey, Peer peer) {
		if(msg.hdr.isRequest()) {
			logger.log(Level.FINER,"Handling request");
			handleRequest(msg,connkey,peer);
		} else {
			logger.log(Level.FINER,"Handling answer, hop_by_hop_identifier="+msg.hdr.hop_by_hop_identifier);
			//locate state
			Object state=null;
			synchronized(req_map) {
				Map<Integer,Object> e_c = req_map.get(connkey);
				if(e_c!=null) {
					state = e_c.get(msg.hdr.hop_by_hop_identifier);
					e_c.remove(msg.hdr.hop_by_hop_identifier);
				}
			}
			if(state!=null) {
				handleAnswer(msg,connkey,state);
			} else {
				logger.log(Level.FINER,"Answer did not match any outstanding request");
			}
		}
		return true;
	}
	//connectionlistener
	/**
	 * Handle a a connection state change.
	 * If the connection has been lost this implementation calls handleAnswer(null,...) for outstanding requests on the connection.
	 * Subclasses should not override this method.
	 */
	public final void handle(ConnectionKey connkey, Peer peer, boolean up) {
		synchronized(req_map) {
			if(up) {
				//register the new connection
				req_map.put(connkey, new HashMap<Integer,Object>());
			} else {
				//find outstanding requests, and call handleAnswer with NULL
				Map<Integer,Object> e_c = req_map.get(connkey);
				if(e_c==null) return;
				//forget the connection
				req_map.remove(connkey);
				//remove the entries
				for(Map.Entry<Integer,Object> e_s : e_c.entrySet()) {
					handleAnswer(null,connkey,e_s.getValue());
				}
			}
		}
	}
}
