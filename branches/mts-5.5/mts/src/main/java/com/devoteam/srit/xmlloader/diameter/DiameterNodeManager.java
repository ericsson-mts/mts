package com.devoteam.srit.xmlloader.diameter;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;

import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;


import dk.i1.diameter.Message;
import dk.i1.diameter.node.ConnectionKey;
import dk.i1.diameter.node.NodeManager;
import dk.i1.diameter.node.NodeSettings;
import dk.i1.diameter.node.Peer;

public class DiameterNodeManager extends NodeManager {
    
    
    private Listenpoint listenpoint = null;
    
    /**
     * init the diameter Client.
     * @param    peer : the peer to connect to
     * @return 
     */
    public DiameterNodeManager(NodeSettings node_settings, Listenpoint listenpoint) throws Exception {
        super(node_settings);       
        this.listenpoint  = listenpoint;
        // start the client             
         start();                
    }  

    /**
     * getConnectionKey(Peer peer)
     * @param    request 
     * @return The answer to the request. Null if there is no answer (all peers down, or other error)
     */
    public synchronized ConnectionKey getConnectionKey(Peer peer) throws Exception {        
	    
	    // find the connexion and establish it 
	    ConnectionKey connkey = node().findConnection(peer);
	    // establish the connexion if not already connected
	    if (connkey == null) 
	    {
	        node().initiateConnection(peer, true);
	        waitForConnection();
	        Thread.sleep(200);
	    }
	return connkey;
    }
    
    
    /**
     * Send a diameter request.
     * @param    request The request to send
     * @return The answer to the request. Null if there is no answer (all peers down, or other error)
     */
    public synchronized boolean sendRequest(MsgDiameter msg, Peer peer) throws Exception {        
                
        // send the DIAMETER request       	            	            	
        this.sendRequest(msg.getMessage(), new Peer[]{peer}, null);
        
        return true;
    }

    /**
     * Send a diameter answer.
     * @param    msg The request to send
     * @param    connKey The transport connection to use to send the message
     * @return The status of the operation. false means an error occured.
     */
    public synchronized boolean sendAnswer(Message message, ConnectionKey connKey) throws Exception {        
        this.answer(message, connKey);                
                
        return true;
    }

    /**
     * Method to close properly the ressources
     */    
    public synchronized boolean reset() {
        this.stop(0);        
        // noting to do
        return true;
    }

    /**
     * Handle an incoming message.
     * This implementation calls handleRequest(), or matches an answer to an outstanding request and calls handleAnswer().
     * Subclasses should not override this method.
     */     
    public synchronized final boolean handle(Message message, ConnectionKey connkey, Peer peer) {
        // appel de la methode de la pile pour libérer des listes internes
        super.handle(message, connkey, peer);

        // Call back vers la Stack generic
        MsgDiameter msg = null;
        try
        {
            msg = new MsgDiameter(message);
                        
            Channel channel = new ChannelDiameter(peer, connkey);
            msg.setChannel(channel);
            msg.setListenpoint(listenpoint);
                                                      
            StackFactory.getStack(StackFactory.PROTOCOL_DIAMETER).receiveMessage(msg);
        }
        catch(Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, e, "Error while receiving the DIAMETER message : ", msg, e);
            e.printStackTrace();
        }
        
        return true;
    }
       
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
    }

}
