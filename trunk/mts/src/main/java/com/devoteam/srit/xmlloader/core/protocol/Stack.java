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

package com.devoteam.srit.xmlloader.core.protocol;

import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.ScenarioRunner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.log.TextListenerProviderRegistry;
import com.devoteam.srit.xmlloader.core.newstats.StatKey;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.expireshashmap.ExpireHashMap;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;

import dk.i1.sctp.SCTPData;
import org.dom4j.Element;

import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 *
 * @author gpasquiers
 */
public abstract class Stack
{
	public static String SEND = 	"SEND>>> msg";
	public static String RECEIVE = 	">>>RECEIVE msg";
	public static String CAPTURE = 	"==CAPTURE== msg";
	
    /** list of transactions objects **/
    private ExpireHashMap<TransactionId, Trans> outTransactions;
    private ExpireHashMap<TransactionId, Trans> inTransactions;
    private ExpireHashMap<TransactionId, Trans> capTransactions;

    /** list of retransmissions objects **/
    protected ExpireHashMap<RetransmissionId, Msg> outRetransRequests;
    protected ExpireHashMap<RetransmissionId, Msg> outRetransResponses;
    protected ExpireHashMap<RetransmissionId, Msg> inRetransRequests;
    protected ExpireHashMap<RetransmissionId, Msg> inRetransResponses;
    protected ExpireHashMap<RetransmissionId, Msg> capRetransRequests;
    protected ExpireHashMap<RetransmissionId, Msg> capRetransResponses;
    
    /** list of message objects **/
    private ExpireHashMap<MessageId, Msg> outMessageRequests;
    private ExpireHashMap<MessageId, Msg> outMessageResponses;
    private ExpireHashMap<MessageId, Msg> capMessageRequests;
    private ExpireHashMap<MessageId, Msg> capMessageResponses;

    /** list of sessions objects **/
    private ExpireHashMap<SessionId, Sess> outinSessions;
    private ExpireHashMap<SessionId, Sess> capSessions;

    /** list of channels object = channel transport**/
    private Map<String, Channel> channels = null;    
    /** list of listenpoint object**/
    private Map<String, Listenpoint> listenpoints = null;
    /** list of probe object**/
    private Map<String, Probe> probes = null;

    /** counter for the generation of the unique transaction ID */
    private static long transId = 0;

    /**
     * Config parameter
     */
    public long msgLifeTime = 30*1000;
    public long sessLifeTime = 600*1000;
    public long receiveTimeout = 30;
    public boolean retransmitManagement = false;
    public boolean retransmitFiltering = false;
    public boolean routeDefaultResponse = true;
    public boolean routeDefaultSubsequent = false;
    public float[] retransmitTimes = null;
    
    /** Timer to schedule the retransaction */
    public Timer retransmissionTimer = new Timer();
    
    /** constructor */
    public Stack() throws Exception
    {
        Config config = null;
        try
        {
            config = getConfig();
        }
        catch (Exception e)
        {
            // nothing to do because it happens when 
            // the Stack class is presnt and the config file not
            // example StackSoap 
        }

        retransmitManagement = config.getBoolean("retransmit.MANAGEMENT", retransmitManagement);

        retransmitFiltering = config.getBoolean("retransmit.FILTERING", retransmitFiltering);

        routeDefaultResponse = config.getBoolean("route.DEFAULT_RESPONSE", routeDefaultResponse);
        
        routeDefaultSubsequent = config.getBoolean("route.DEFAULT_SUBSEQUENT", routeDefaultSubsequent);
        
        String retransmitTimersParam = config.getString("retransmit.TIMERS");
        if (retransmitTimersParam != null && retransmitTimersParam.trim().length() > 0)
        {
            String[] retransmitTimersTab = retransmitTimersParam.split(",");

            retransmitTimes = new float[retransmitTimersTab.length];
            for (int i = 0; i < retransmitTimersTab.length; i++)
            {
                retransmitTimes[i] = Float.parseFloat(retransmitTimersTab[i]);
            }
        }
        else
        {
            retransmitTimes = new float[0];
        }

        this.receiveTimeout = (long) (config.getDouble("operations.RECEIVE_TIMEOUT", receiveTimeout) * 1000);

        this.msgLifeTime = (long) (config.getDouble("MESSAGE_TIME_LIFE", 30) * 1000);
        this.outRetransRequests = new ExpireHashMap<RetransmissionId, Msg>("outRetransRequests", msgLifeTime);
        this.outRetransResponses = new ExpireHashMap<RetransmissionId, Msg>("outRetransResponses", msgLifeTime);
        this.inRetransRequests = new ExpireHashMap<RetransmissionId, Msg>("inRetransRequests", msgLifeTime);
        this.inRetransResponses = new ExpireHashMap<RetransmissionId, Msg>("inRetransResponses", msgLifeTime);
        this.capRetransRequests = new ExpireHashMap<RetransmissionId, Msg>("capRetransRequests", msgLifeTime);
        this.capRetransResponses = new ExpireHashMap<RetransmissionId, Msg>("capRetransResponses", msgLifeTime);
        
        this.outMessageRequests   = new ExpireHashMap<MessageId, Msg>("outMessageRequests", msgLifeTime);
        this.outMessageResponses  = new ExpireHashMap<MessageId, Msg>("outMessageResponses", msgLifeTime);
        this.capMessageRequests   = new ExpireHashMap<MessageId, Msg>("capMessageRequests", msgLifeTime);
        this.capMessageResponses  = new ExpireHashMap<MessageId, Msg>("capMessageResponses", msgLifeTime);

        this.outTransactions = new ExpireHashMap<TransactionId, Trans>("outTransactions", msgLifeTime);
        this.inTransactions = new ExpireHashMap<TransactionId, Trans>("inTransactions", msgLifeTime);
        this.capTransactions = new ExpireHashMap<TransactionId, Trans>("capTransactions", msgLifeTime);

        this.sessLifeTime = (long) (config.getDouble("SESSION_TIME_LIFE", 0) * 1000);               
        this.outinSessions = new ExpireHashMap<SessionId, Sess>("outSessions", sessLifeTime);
        this.capSessions = new ExpireHashMap<SessionId, Sess>("capSessions", sessLifeTime);
        
        this.channels = Collections.synchronizedMap(new HashMap<String, Channel>());
        this.listenpoints = Collections.synchronizedMap(new HashMap<String, Listenpoint>());
        this.probes = Collections.synchronizedMap(new HashMap<String, Probe>());

        routingThread.start();
    }

    public static synchronized long nextTransactionId()
    {
        if (transId == Long.MAX_VALUE)
        {
            transId = Long.MIN_VALUE;
        }

        return transId++;
    }

    /** reset the instance of this stack */
    public void reset()
    {
        for (Channel channel : channels.values())
        {
        	channel.close();
        }

        for (Listenpoint listenpoint : listenpoints.values())
        {
        	listenpoint.remove();
        }
        for (Probe probe : probes.values())
        {
        	probe.remove();
        }

        channels.clear();
        listenpoints.clear();
        probes.clear();
        retransmissionTimer.cancel();
    }

    /** Returns the Config object to access the protocol config file */
    public abstract Config getConfig() throws Exception;

    public abstract XMLElementReplacer getElementReplacer();

    /** Open a channel */
    public boolean openChannel(Channel channel) throws Exception
    {
        synchronized (channels)
        {
        	channels.put(channel.getName(), channel);
	        if (channels.size() % 1000 == 999)
	        {
	            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "Stack : List of channels : size = ", channels.size());
	        }
        }
        GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Stack: put in channels list : size = ", channels.size(), " the channel \n", channel);

        try
        {
            boolean opened = channel.open();

            if (!opened)
            {
            	channels.remove(channel.getName());
                GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "Cannot open the channel : ", channel);
            }
            return opened;
        }
        catch(Exception e)
        {
        	channels.remove(channel.getName());
            throw e;
        }
    }

    /** Close a channel */
    public boolean closeChannel(String name) throws Exception
    {
        synchronized (channels)
        {
        	Channel channel = channels.get(name);
            if (channel != null)
            {
            	channel.close();
            	channels.remove(name);
            }
        }
        return true;
    }

    /** Get a channel */
    public Channel getChannel(String name) throws Exception
    {
        synchronized (channels)
        {
            return channels.get(name);
        }
    }

    public boolean existsChannel(String name) throws Exception
    {
        synchronized (channels)
        {
            return channels.containsKey(name);
        }
    }
    
    /** Create the listenpoint */
    public boolean createListenpoint(Listenpoint listenpoint, String protocol) throws Exception
    {
    	boolean result = true;
        synchronized (listenpoints)
        {
        	Listenpoint oldListenpoint = listenpoints.get(listenpoint.getName());
	        if ((oldListenpoint != null) && (!listenpoint.equals(oldListenpoint))) {
	            throw new ExecutionException("A listenpoint <name=" + listenpoint.getName() + "> already exists with other attributes.");
	        }
	        if (oldListenpoint != null)
	        {
	        	return false;
	        }
        	listenpoints.put(listenpoint.getName(), listenpoint);
            if (listenpoints.size() % 1000 == 999)
            {
                GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "Stack : List of listenpoints : size = ", listenpoints.size());
            }
            GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Stack: put in listenpoints list : size = ", listenpoints.size(), " the listenpoint \n", listenpoint);
        }
        
        result = listenpoint.create(protocol);
        
        return result;
    }

    /** Remove a listenpoint */
    public boolean removeListenpoint(String name) throws Exception
    {
    	Listenpoint listenpoint = null;
        synchronized (listenpoints)
        {
        	listenpoint = listenpoints.get(name);
        	if (listenpoint == null)
        	{
        		return false;
        	}
    		listenpoints.remove(name);
        }
        
		listenpoint.remove();
		
		return true;
    }

    /** Get a listenpoint from it's name */
    public Listenpoint getListenpoint(String name) throws Exception
    {
        synchronized (listenpoints)
        {
            return listenpoints.get(name);
        }
    }

    /** Create the probe */
    public boolean createProbe(Probe probe, String protocol) throws Exception
    {
    	boolean result = true;
        synchronized (probes)
        {
        	Probe oldProbe = probes.get(probe.getName());
	        if ((oldProbe != null) && (!probe.equals(oldProbe))) {	        	
	            throw new ExecutionException("A probe <name=" + probe.getName() + "> already exists with other attributes.");
	        }

	        if (oldProbe != null) {
	        	return false;
	        }
	        probes.put(probe.getName(), probe);
            if (probes.size() % 1000 == 999)
            {
                GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "Stack : List of probes : size = ", probes.size());
            }
            GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Stack: put in probes list : size = ", probes.size(), " the probe \n", probe);
        }
        
        result = probe.create(protocol);
        
        return result;
    }

    /** Remove a probe */
    public boolean removeProbe(String name) throws Exception
    {
        synchronized (probes)
        {
        	Probe probe = probes.get(name);
            if (probe == null)
            {
	            return false;
            }
            probe.remove();
        }

        probes.remove(name);
        
        return true;
    }

    /** Get a probe from it's name */
    public Probe getProbe(String name) throws Exception
    {
        synchronized (probes)
        {
            return probes.get(name);
        }
    }

    /** Creates a Channel specific to each Stack */
    public Channel parseChannelFromXml(Element root, String protocol) throws Exception
    {
    	throw new ExecutionException("Bug developpment : method not yet implemented", new Exception());    	
    }

    /** Creates a Listenpoint specific to each Stack */
    public Listenpoint parseListenpointFromXml(Element root) throws Exception 
    {
    	throw new ExecutionException("Bug developpment : method not yet implemented", new Exception());    	
    }

    /** Creates a probe specific to each Stack */
    public Probe parseProbeFromXml(Element root) throws Exception 
    {
        Probe probe = new Probe(this, root);
        return probe;        
    }

    /** Creates a Msg specific to each Stack */
    public abstract Msg parseMsgFromXml(Boolean request, Element root, Runner runner) throws Exception;

    /** 
     * Creates a Msg specific to each Stack
     * Use for TCP like protocol : to build incoming message
     * should become ABSTRACT later  
     */
    public Msg readFromStream(InputStream inputStream, Channel channel) throws Exception
    {
    	throw new ExecutionException("Bug developpment : method not yet implemented", new Exception());
    }

    /** 
     * Creates a Msg specific to each Stack
     * Use for UDP like protocol : to build incoming message
     * should become ABSTRACT later  
     */
    public Msg readFromDatas(byte[] datas, int length) throws Exception
    {
    	throw new ExecutionException("Bug developpment : method not yet implemented", new Exception());
    }

    /**
     * Creates a Msg specific to each Stack
     * Use for SCTP like protocol : to build incoming message
     * should become ABSTRACT later
     */
    public Msg readFromSCTPData(SCTPData chunk) throws Exception    
    {
    	byte[] bytes = chunk.getData();
    	return readFromDatas(bytes, bytes.length);
    }

    /** 
     * Creates a channel specific to each Stack
     * Use for TCP like protocol : to manage the incoming channel (server-side)
     * should become ABSTRACT later  
     */
    public Channel buildChannelFromSocket(Listenpoint listenpoint, Socket socket) throws Exception {
        // should remove || (socket.getClass().equals(Socket.class)) one hio are ok
    	if (socket.getClass().getName().equals("sun.nio.ch.SocketAdaptor") || (socket.getClass().equals(Socket.class))) {
    		com.devoteam.srit.xmlloader.tcp.ChannelTcp channelTcp = new com.devoteam.srit.xmlloader.tcp.ChannelTcp("Channel #" + Stack.nextTransactionId(), listenpoint, socket);
	        return channelTcp;
    	}
    	else if (socket.getClass().getName().equals("com.sun.net.ssl.internal.ssl.SSLSocketImpl") || socket.getClass().getName().equals("sun.security.ssl.SSLSocketImpl")) {
    		com.devoteam.srit.xmlloader.tls.ChannelTls channelTls = new com.devoteam.srit.xmlloader.tls.ChannelTls("Channel #" + Stack.nextTransactionId(), listenpoint, socket);
	        return channelTls;
    	}
    	else {
    		com.devoteam.srit.xmlloader.sctp.ChannelSctp channelSctp = new com.devoteam.srit.xmlloader.sctp.ChannelSctp("Channel #" + Stack.nextTransactionId(), listenpoint, socket);
	        return channelSctp;
    	} 
    }
        
    public synchronized boolean sendMessage(Msg msg) throws Exception
    {
        boolean ret = false;
        Channel channel = msg.getChannel();
        Listenpoint listenpoint = msg.getListenpoint();
        if (channel != null)
        {
            ret = channel.sendMessage(msg);
        }
        else if (listenpoint != null)
        {
            ret = listenpoint.sendMessage(msg, msg.getRemoteHost(), msg.getRemotePort(), msg.getTransport());

        }
        else
        {
            throw new ExecutionException("No listenpoint or channel to transport the message : \r\n" + msg.toString());
        }

        // increment counters in the transport section
        incrStatTransport(msg, StackFactory.PREFIX_OUTGOING, StackFactory.PREFIX_INCOMING);
        return ret;
    }
    
    public synchronized boolean sendMessage(Msg msg, ScenarioRunner srcRunner, ScenarioRunner destRunner, ScenarioRunner answerHandler) throws Exception
    {
        boolean ret = false;
        try {
            long delay = msg.getTimestamp() - System.currentTimeMillis();
            if (delay > 0) {
                RetransmitMsgTask retransmitTask = new RetransmitMsgTask(this, msg, srcRunner, destRunner, answerHandler);
                retransmissionTimer.schedule(retransmitTask, delay);
            }
            else
            {
                sendMessageException(msg, srcRunner, destRunner, answerHandler);
            }
        }
        catch (Exception e)
        {
            throw e;
        }
        return ret;
    }
    
    public synchronized boolean sendMessageException(Msg msg, ScenarioRunner srcRunner, ScenarioRunner destRunner, ScenarioRunner answerHandler) throws Exception {
        boolean isRetransmission = false;        	
        // we are sending a request
        if (msg.isRequest()) {
            // retransmissionid supported
            if (msg.getRetransmissionId() != null) {
                Msg retrans = outRetransRequests.get(msg.getRetransmissionId());
                // a message with the same retransmissionid has already been sent, update retrans number
                if (retrans != null) {
                    isRetransmission = true;
                    retrans.setRetransNumber(retrans.getRetransNumber() + 1);
                    msg.setRetransNumber(retrans.getRetransNumber());
                    
                    GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Retransmissions: sent request (RETRANSMISSION_ID=", msg.getRetransmissionId(), ") IS a retransmission");
                    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_REQUEST, msg.getProtocol(), msg.getTypeComplete() + StackFactory.PREFIX_OUTGOING, "_retransmitNumber"), 1);
                }
                // it is the first transmission of the message
                else {
                	outRetransRequests.put(msg.getRetransmissionId(), msg);
                }
            }

            // transactionid supported - create or update transaction if necessary
            if (msg.getTransactionId() != null){
                // the message is a restransmission, update the existing transaction
                if(isRetransmission) {
                    Trans trans = this.outTransactions.get(msg.getTransactionId());
                    if (trans != null) {
                        trans.setScenarioRunner(srcRunner);
                        GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Retransmissions: updated the scenario runner :", srcRunner, " associated to the transaction :", msg.getTransactionId());
                    }
                    else {
                        GlobalLogger.instance().getApplicationLogger().warn(Topic.PROTOCOL, "Retransmissions: could not update the scenario runner associated because this is an unknown transaction, transaction=", msg.getTransactionId(), ", message=", msg);
                    }
                }
                // the message is a new transaction, create the transaction if necessary
                else{
                    if (msg.beginTransaction()) {
                        Trans trans = new Trans(this, msg);
                        trans.setScenarioRunner(srcRunner);
                        msg.setTransaction(trans);
                        outTransactions.put(msg.getTransactionId(), trans);

                        if (srcRunner != null) {
                            GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Stack: create a new transaction (TRANSACTION_ID=", msg.getTransactionId(), ") for scenario ", srcRunner.getName());
                        }
                        else {
                            GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Stack: create a new transaction (TRANSACTION_ID=", msg.getTransactionId(), ") for no scenario");
                        }

                        if (msg.getRetransmissionId() != null && retransmitManagement) {
                            trans.startAutomaticRetransmit();
                            GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Retransmissions: start automatic retransmissions timer for request ", msg.getMessageId());
                        }
                    }
                }
            }

            // messageid supported - create the entry in map
            // we do it event if destScenario is not use because of statistics
            if (msg.getMessageId() != null && !isRetransmission) {
                msg.setDestScenario(destRunner);
                outMessageRequests.put(msg.getMessageId(), msg);

            	GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Stack: add entry to expected requests (MESSAGE_ID=", msg.getMessageId(), ") for destScenario = ", destRunner);
            }

            // logs and stats
            if (!isRetransmission) {
            	GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Retransmissions: sent request (RETRANSMISSION_ID=", msg.getRetransmissionId(), ") IS NOT a retransmission");
            	incrStatisticTransRequest(msg, StackFactory.PREFIX_OUTGOING);
            }
        }
        // we are sending a response
        else {
        	Trans trans = null;

            // retransmissionid supported
            if (msg.getRetransmissionId() != null) {
                Msg retrans = outRetransResponses.get(msg.getRetransmissionId());
                // a message with the same retransmissionid has already been sent, update retrans number
                if (retrans != null) {
                    isRetransmission = true;
                    retrans.setRetransNumber(retrans.getRetransNumber() + 1);
                    msg.setRetransNumber(retrans.getRetransNumber());
                    GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Retransmissions: sent response (RETRANSMISSION_ID=", msg.getRetransmissionId(), ") IS a retransmission");
                    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_REQUEST, msg.getProtocol(), msg.getTypeComplete() + StackFactory.PREFIX_INCOMING, msg.getResultComplete() + StackFactory.PREFIX_OUTGOING, "_retransmitNumber"), 1);
                    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_RESPONSE, msg.getProtocol(), msg.getResultComplete() + StackFactory.PREFIX_OUTGOING, msg.getTypeComplete() + StackFactory.PREFIX_INCOMING, "_retransmitNumber"), 1);                    
                }
                // it is the first transmission of the message
                else {
                	outRetransResponses.put(msg.getRetransmissionId(), msg);
                }
            }

            // transactionid supported - add the message to existing transaction
            if (msg.getTransactionId() != null && !isRetransmission) {
                trans = inTransactions.get(msg.getTransactionId());
                if (trans != null) {
                    trans.addEndMessage(msg);
                    msg.setTransaction(trans);
                    GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Stack: add the message to an existing transaction (TRANSACTION_ID=", msg.getTransactionId(), ")");
                }
                else {
                    GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "Stack: Could not find transaction for sent response (TRANSACTION_ID=", msg.getTransactionId(),")");
                }
            }
        
            // message id supported
            if (msg.getMessageId() != null && !isRetransmission) {
                GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Stack: add entry to expected responses (MESSAGE_ID=", msg.getMessageId(), ")");
                msg.setDestScenario(destRunner);
                outMessageResponses.put(msg.getMessageId(), msg);
            }

            // logs and stats
            if (!isRetransmission) {
                GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Retransmissions: sent response (RETRANSMISSION_ID=", msg.getRetransmissionId(), ") IS NOT a retransmission");
                incrStatisticTransResponse(trans, msg, StackFactory.PREFIX_INCOMING, StackFactory.PREFIX_OUTGOING);
            }
        }
        
        msg.setTimestamp(System.currentTimeMillis());
        sendMessage(msg);

        // stuff about sessions (see later)
        if (msg.getSessionId() != null && !isRetransmission) {
        	// check whether the message belongs to an existing session 
        	Sess sess = outinSessions.get(msg.getSessionId());
            if (sess != null) {
            	if (msg.isRequest()) {
		            sess.addEndMessage(msg);
                    // beginMsg.setRetransNumber(beginMsg.getRetransNumber() + 1);
	                float messageTime = Stack.getTimeDuration(msg, sess.getBeginMsg().getTimestamp());
	               	GlobalLogger.instance().getApplicationLogger().info(Topic.PROTOCOL, "Add a message (time = ", messageTime, " s) to an existing session : ", sess.getSummary(), " (SESSION_ID=", msg.getSessionId(), ")");
	                // if necessary, then remove an existing session
	            	if (msg.endSession()) {
	                    float sessionTime = Stack.getTimeDuration(msg, sess.getBeginMsg().getTimestamp());
			            GlobalLogger.instance().getApplicationLogger().info(Topic.PROTOCOL, "Finish a outgoing session (time = ", sessionTime, " s) : ", " (SESSION_ID=", msg.getSessionId(), ")");
			            
			    		sess.onRemove();
			    		outinSessions.remove(msg.getSessionId());
	            	}
            	}
            }
            else  {
	            // if necessary, then create a new session
	        	if (msg.beginSession()) {
		            sess = new Sess(this, msg);
		            sess.setScenarioRunner(srcRunner);
		            outinSessions.put(msg.getSessionId(), sess);
		            GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Create a new outgoing session : ", sess.getSummary(), " (SESSION_ID=", msg.getSessionId(), ")");
	        	}
            }
        }
        
        // logs in scenario and application logs as CALLFLOW topic
        processLogsMsgSending(msg, srcRunner, Stack.SEND);

        return true;
    }


    // TODO: set a max size to the queue
    private LinkedBlockingQueue<Msg> routingQueue = new LinkedBlockingQueue();

    private Thread routingThread = new Thread(){
        @Override
        public void run(){
            while(true){
                try{
                    Msg msg = routingQueue.take();
                    doReceiveMessage(msg);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    };

    public boolean receiveMessageNIO(final Msg msg) throws Exception{
        return receiveMessage(msg);
    }

    public boolean receiveMessage(Msg msg) throws Exception{
        routingQueue.offer(msg);
        return true;
    }

    public boolean doReceiveMessage(Msg msg) throws Exception{
        if (msg.getTimestamp() <= 0) {
            msg.setTimestamp(System.currentTimeMillis());
        }
     
        boolean isTransactionIdSupported = msg.getTransactionId() != null;
        boolean isMessageIdSupported = msg.getMessageId() != null;
        boolean isRestransmissionIdSupported = msg.getRetransmissionId() != null;

        boolean isRetransmission = false;
        ScenarioRunner destScenario = null;

        if (msg.isRequest())
        {
            Trans trans = null;
            if (isRestransmissionIdSupported)
            {
                Msg retrans = inRetransRequests.get(msg.getRetransmissionId());
                if (retrans != null)
                {
                    isRetransmission = true;
                    retrans.setRetransNumber(retrans.getRetransNumber() + 1);
                    trans = retrans.getTransaction();
                    msg.setTransaction(trans);
                    msg.setRetransNumber(retrans.getRetransNumber());
                    GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Retransmissions: received request (RETRANSMISSION_ID=", msg.getRetransmissionId(), ") IS a retransmission");
                    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_REQUEST, msg.getProtocol(), msg.getTypeComplete() + StackFactory.PREFIX_INCOMING, "_retransmitNumber"), 1);
                    if (retransmitManagement)
                    {
                    	if (retrans.getTransaction() != null)
                    	{
                    		retrans.getTransaction().retransmitAutomaticResponses();
                    	}
                    }
                }
                else
                {
                	inRetransRequests.put(msg.getRetransmissionId(), msg);
                }
            }
            
            if (isTransactionIdSupported && !isRetransmission)
            {
            	if (msg.beginTransaction())
            	{
	                trans = new Trans(this, msg);
	                trans.setScenarioRunner(destScenario);
	                msg.setTransaction(trans);
	                inTransactions.put(msg.getTransactionId(), trans);
                   	GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Stack: create a new transaction (TRANSACTION_ID=", msg.getTransactionId(), ")");
            	}
            }
            
            if (!isRetransmission)
            {
                GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Retransmissions: received request (RETRANSMISSION_ID=", msg.getRetransmissionId(), ") IS NOT a retransmission");
                incrStatisticTransRequest(msg, StackFactory.PREFIX_INCOMING);
            }
            
            if (!isRetransmission || !retransmitFiltering)
            {
                if (isMessageIdSupported)
                {
                    Msg message = outMessageRequests.get(msg.getMessageId());
                    if (message != null)
                    {
                        if(!isRetransmission)
                        {
                            float procTime = Stack.getTimeDuration(msg, message.getTimestamp());
                            StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_REQUEST, msg.getProtocol(), msg.getTypeComplete() + StackFactory.PREFIX_INCOMING, "_procTime"), procTime);
                            StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_REQUEST, msg.getProtocol(), msg.getTypeComplete() + StackFactory.PREFIX_INCOMING, "_msgNumber"), 1);		            	
                            GlobalLogger.instance().getApplicationLogger().info(Topic.PROTOCOL, "Stack: receive a request (processing time=", procTime, "s ) : ", msg.toShortString());
                        }

                        destScenario = message.getDestScenario();
                        if (destScenario != null)
                        {
                        	GlobalLogger.instance().getApplicationLogger().info(Topic.PROTOCOL, "Routing: route the received request by MESSAGE_ID to \"", destScenario.getName(), "\" because of destScenario attribute (MESSAGE_ID=", msg.getMessageId(), ").");
                        }
                    }
                }
            }
        }
        else
        {
            Trans trans = null;
            if (isRestransmissionIdSupported)
            {
                Msg retrans = inRetransResponses.get(msg.getRetransmissionId());
                if (retrans != null)
                {
                    isRetransmission = true;
                    retrans.setRetransNumber(retrans.getRetransNumber() + 1);
                    trans = retrans.getTransaction();
                    msg.setTransaction(trans);
                    msg.setRetransNumber(retrans.getRetransNumber());
                    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_REQUEST, msg.getProtocol(), msg.getTypeComplete() + StackFactory.PREFIX_OUTGOING, msg.getResultComplete() + StackFactory.PREFIX_INCOMING, "_retransmitNumber"), 1);                    
                    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_RESPONSE, msg.getProtocol(), msg.getResultComplete() + StackFactory.PREFIX_INCOMING, msg.getTypeComplete() + StackFactory.PREFIX_OUTGOING, "_retransmitNumber"), 1);                    
                }
                else
                {
                	inRetransResponses.put(msg.getRetransmissionId(), msg);
                }
            }            
            
            if (isTransactionIdSupported && !isRetransmission)
            {
                GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Stack: get transaction ", msg.getTransactionId(), " for received response ", msg.getMessageId());
                trans = outTransactions.get(msg.getTransactionId());
                if (trans != null)
                {
                    trans.addEndMessage(msg);
                    msg.setTransaction(trans);
                    GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Stack: add the message to an existing transaction (TRANSACTION_ID=", msg.getTransactionId(), ")");
                }
                else
                {
                	GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "Stack: Could not find transaction for received response (TRANSACTION_ID=", msg.getTransactionId(),")");
                }
            }

            if (!isRetransmission)
            {
                GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Retransmissions: received response (RETRANSMISSION_ID=", msg.getRetransmissionId(), ") IS NOT a retransmission");
                incrStatisticTransResponse(trans, msg, StackFactory.PREFIX_OUTGOING, StackFactory.PREFIX_INCOMING);
            }

            if (routeDefaultResponse && trans != null)
            {
                destScenario = trans.getScenarioRunner();
                if (destScenario != null)
                {
                    GlobalLogger.instance().getApplicationLogger().info(Topic.PROTOCOL, "Routing: route the received response by TRANSACTION_ID to \"", destScenario.getName(), "\" (TRANSACTION_ID=", msg.getTransactionId(),").");
                }
            }
                        
            if (!isRetransmission || !retransmitFiltering)
            {       
                if (isMessageIdSupported)
                {
                    Msg message = outMessageResponses.get(msg.getMessageId());
                    if (message != null)
                    {
                        if(!isRetransmission)
                        {
                            float procTime = Stack.getTimeDuration(msg, message.getTimestamp());
                            StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_REQUEST, msg.getProtocol(), msg.getTypeComplete() + StackFactory.PREFIX_OUTGOING, msg.getResultComplete() + StackFactory.PREFIX_INCOMING, "_procTime"), procTime);
                            StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_REQUEST, msg.getProtocol(), msg.getTypeComplete() + StackFactory.PREFIX_OUTGOING, msg.getResultComplete() + StackFactory.PREFIX_INCOMING, "_msgNumber"), 1);
                            StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_RESPONSE, msg.getProtocol(), msg.getResultComplete() + StackFactory.PREFIX_INCOMING, msg.getTypeComplete() + StackFactory.PREFIX_OUTGOING, "_procTime"), procTime);
                            StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_RESPONSE, msg.getProtocol(), msg.getResultComplete() + StackFactory.PREFIX_INCOMING, msg.getTypeComplete() + StackFactory.PREFIX_OUTGOING, "_msgNumber"), 1);                                                   
                            GlobalLogger.instance().getApplicationLogger().info(Topic.PROTOCOL, "Stack: receive a response (processing time=", procTime, "s) : ", msg.toShortString());
                        }
                        
                        if (destScenario ==  null)
                        {
	                        destScenario = message.getDestScenario();
	                        if (destScenario != null)
	                        {
	                        	GlobalLogger.instance().getApplicationLogger().info(Topic.PROTOCOL, "Routing: route the received response by MESSAGE_ID to \"", destScenario.getName(),"\" because of destScenario attribute (MESSAGE_ID=", msg.getMessageId(), ").");
	                        }
                        }
                    }
                }
            }
        }

        Sess sess = null; 
        if (msg.getSessionId() != null && !isRetransmission)
        {
        	// check whether the message belongs to an existing session 
        	sess = outinSessions.get(msg.getSessionId());
            if (sess != null)
            {
            	if (msg.isRequest())
            	{
                	
		            sess.addEndMessage(msg);
                    // beginMsg.setRetransNumber(beginMsg.getRetransNumber() + 1);
	                float messageTime = Stack.getTimeDuration(msg, sess.getBeginMsg().getTimestamp());
	               	GlobalLogger.instance().getApplicationLogger().info(Topic.PROTOCOL, "Add a message (time = ", messageTime, " s) to an existing session : ", sess.getSummary(), " (SESSION_ID=", msg.getSessionId(), ")");
	                // if necessary, then remove an existing session
	            	if (msg.endSession())
	            	{
	                    float sessionTime = Stack.getTimeDuration(msg, sess.getBeginMsg().getTimestamp());
			            GlobalLogger.instance().getApplicationLogger().info(Topic.PROTOCOL, "Finish a outgoing session (time = ", sessionTime, " s) : ", sess.getSummary(), " (SESSION_ID=", msg.getSessionId(), ")");
			            
			    		sess.onRemove();
			    		outinSessions.remove(msg.getSessionId());
	            	}
	            	
	                if (routeDefaultSubsequent && sess != null)
	                {
	                	if (destScenario == null)
	                	{
	                		destScenario = sess.getScenarioRunner();
	                		if (destScenario != null)
	                		{
	                			GlobalLogger.instance().getApplicationLogger().info(Topic.PROTOCOL, "Routing: route the message by SESSION_ID to \"", destScenario.getName(), "\" (SESSION_ID=", msg.getSessionId(), ").");
	                		}
	                	}
	                }
            	}
            	// case the message is not the initial message
                sess = null;
            }
            else
            {
	            // if necessary, then create a new session
	        	if (msg.beginSession())
	        	{
		            sess = new Sess(this, msg);
		            outinSessions.put(msg.getSessionId(), sess);
		            GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Create a new incoming session : ", sess.getSummary(), " sessionsId=", msg.getSessionId());
	        	}
            }
        }

        // dispatch the message to the right scenario
        if (!isRetransmission || !retransmitFiltering)
        {
        	
	        if (destScenario == null)
	        {
	            destScenario = DispatcherMsg.dispatchMsg(msg);
	        }
            // logs in scenario and application logs as CALLFLOW topic
	        processLogsMsgSending(msg, destScenario, Stack.RECEIVE);
	        if (destScenario != null)
	        {
	            destScenario.dispatchMessage(msg);
	            // case the message is the initial message
	            if (sess != null)
	            {
	            	sess.setScenarioRunner(destScenario);
	            }
	        }
        }
        
        // increment counters in the transport section
        incrStatTransport(msg, StackFactory.PREFIX_INCOMING, StackFactory.PREFIX_OUTGOING);
        return true;
    }

    public boolean captureMessage(Msg msg) throws Exception
    {
        boolean isTransactionIdSupported = msg.getTransactionId() != null;
        boolean isMessageIdSupported = msg.getMessageId() != null;
        boolean isRestransmissionIdSupported = msg.getRetransmissionId() != null;

        if (msg.getTimestamp() <= 0)
        {
            msg.setTimestamp(System.currentTimeMillis());
        }
       
        if (msg.isRequest())
        {
            boolean isRetransmission = false;
            Trans transaction = null;
            if (isRestransmissionIdSupported)
            {
                Msg knownRequest = capRetransRequests.get(msg.getRetransmissionId());

                if (null != knownRequest)
                {
                    GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Retransmissions: captured request (RETRANSMISSION_ID=", msg.getRetransmissionId(), ") IS a retransmission");
                    isRetransmission = true;
                    knownRequest.setRetransNumber(knownRequest.getRetransNumber() + 1);
                    transaction = knownRequest.getTransaction();
                    msg.setTransaction(transaction);
                    msg.setRetransNumber(knownRequest.getRetransNumber());
                    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_REQUEST, msg.getProtocol(), msg.getTypeComplete() + StackFactory.PREFIX_CAPTURING, "_retransmitNumber"), 1);                    
                }
                else
                {
                    capRetransRequests.put(msg.getRetransmissionId(), msg);
                }
            }

            if (!isRetransmission)
            {
                GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Retransmissions: captured request (RETRANSMISSION_ID=", msg.getRetransmissionId(), ") IS NOT a retransmission");
                incrStatisticTransRequest(msg, StackFactory.PREFIX_CAPTURING);
            }

            ScenarioRunner destScenario = null;
            if (!isRetransmission || !retransmitFiltering)
            {
                if (isMessageIdSupported)
                {
                    Msg message = capMessageRequests.get(msg.getMessageId());
                    if (message != null)
                    {
                        if(!isRetransmission)
                        {
                            float procTime = Stack.getTimeDuration(msg, message.getTimestamp());
                            StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_REQUEST + StackFactory.PREFIX_CAPTURING, msg.getProtocol(), msg.getTypeComplete() + StackFactory.PREFIX_CAPTURING, "_procTime"), procTime);
                            StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_REQUEST + StackFactory.PREFIX_CAPTURING, msg.getProtocol(), msg.getTypeComplete() + StackFactory.PREFIX_CAPTURING, "_msgNumber"), 1);		            	
                            GlobalLogger.instance().getApplicationLogger().info(Topic.PROTOCOL, "Stack: capture a request with processing time=", procTime, "s) : ", msg.toShortString());
                        }

                        destScenario = message.getDestScenario();
                        GlobalLogger.instance().getApplicationLogger().info(Topic.PROTOCOL, "Routing: route the captured request by MESSAGE_ID to \"", destScenario.getName(), "\" because of destScenario attribute (MESSAGE_ID=", msg.getMessageId(), ").");
                    }
                }

                if (null == destScenario)
                {
	                destScenario = DispatcherMsg.dispatchMsg(msg);
                }
            }
            
            if (isTransactionIdSupported && !isRetransmission)
            {
                // incrStatisticTransRequest(msg, "", StackFactory.PREFIX_CAPTURE);
            	if (msg.beginTransaction())
            	{
	                GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Stack: create new transaction ", msg.getTransactionId(), " for captured request ", msg.getMessageId());
	                transaction = new Trans(this, msg);
	                transaction.setScenarioRunner(destScenario);
	                msg.setTransaction(transaction);	                
	                capTransactions.put(msg.getTransactionId(), transaction);
	                if (destScenario != null)
	                {
	                	GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Stack: create a new transaction (TRANSACTION_ID=", msg.getTransactionId(), ") for scenario ", destScenario.getName());
	                }
	                else
	                {
	                	GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Stack: create a new transaction (TRANSACTION_ID=", msg.getTransactionId(), ") for no scenario");
	                }                
            	}
            }

            if (!isRetransmission || !retransmitFiltering)
            {
                if (null != destScenario)
                {
                    // logs in scenario and application logs as CALLFLOW topic
                    processLogsMsgSending(msg, destScenario, Stack.CAPTURE);
                    destScenario.dispatchMessage(msg);
                }
            }
        }
        else
        {
            boolean isRetransmission = false;
            Trans trans = null;
            if (isRestransmissionIdSupported)
            {
                Msg knownResponse = capRetransResponses.get(msg.getRetransmissionId());
                if (null != knownResponse)
                {
                    GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Retransmissions: captured response (RETRANSMISSION_ID=", msg.getRetransmissionId(), ") IS a retransmission");
                    isRetransmission = true;
                    knownResponse.setRetransNumber(knownResponse.getRetransNumber() + 1);
                    trans = knownResponse.getTransaction();
                    msg.setTransaction(trans);
                    msg.setRetransNumber(knownResponse.getRetransNumber());
                    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_REQUEST, msg.getProtocol(), msg.getTypeComplete() + StackFactory.PREFIX_CAPTURING, msg.getResultComplete() + StackFactory.PREFIX_CAPTURING, "_retransmitNumber"), 1);
                    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_RESPONSE, msg.getProtocol(), msg.getResultComplete() + StackFactory.PREFIX_CAPTURING, msg.getTypeComplete() + StackFactory.PREFIX_CAPTURING, "_retransmitNumber"), 1);                    
                }
                else
                {
                    capRetransResponses.put(msg.getRetransmissionId(), msg);
                }
            }
            if (isTransactionIdSupported && !isRetransmission)
            {
                GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Stack: get transaction ", msg.getTransactionId(), " for captured response ", msg.getMessageId());
                trans = capTransactions.get(msg.getTransactionId());

                if (null == trans)
                {
                    GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "Stack: Could not find transaction for captured response with TRANSACTION_ID=", msg.getTransactionId());
                }
                else
                {
                    trans.addEndMessage(msg);
                    msg.setTransaction(trans);
                }
            }

            if (!isRetransmission)
            {
                GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Retransmissions: captured response (RETRANSMISSION_ID=", msg.getRetransmissionId(), ") IS NOT a retransmission");
            	incrStatisticTransResponse(trans, msg, StackFactory.PREFIX_CAPTURING, StackFactory.PREFIX_CAPTURING);
            }
            	
            ScenarioRunner destScenario = null;
            if (!isRetransmission || !retransmitFiltering)
            {       
                if (isMessageIdSupported)
                {
                    Msg message = capMessageResponses.get(msg.getMessageId());
                    if (message != null)
                    {
                        if(!isRetransmission)
                        {
                            float procTime = Stack.getTimeDuration(msg, message.getTimestamp());
                            StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_REQUEST + StackFactory.PREFIX_CAPTURING, msg.getProtocol(), msg.getTypeComplete() + StackFactory.PREFIX_CAPTURING, msg.getResultComplete() + StackFactory.PREFIX_INCOMING, "_procTime"), procTime);
                            StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_REQUEST + StackFactory.PREFIX_CAPTURING, msg.getProtocol(), msg.getTypeComplete() + StackFactory.PREFIX_CAPTURING, msg.getResultComplete() + StackFactory.PREFIX_INCOMING, "_msgNumber"), 1);
                            StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_RESPONSE + StackFactory.PREFIX_CAPTURING, msg.getProtocol(), msg.getResultComplete() + StackFactory.PREFIX_CAPTURING, msg.getTypeComplete() + StackFactory.PREFIX_INCOMING, "_procTime"), procTime);
                            StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_RESPONSE + StackFactory.PREFIX_CAPTURING, msg.getProtocol(), msg.getResultComplete() + StackFactory.PREFIX_CAPTURING, msg.getTypeComplete() + StackFactory.PREFIX_INCOMING, "_msgNumber"), 1);                            
                            GlobalLogger.instance().getApplicationLogger().info(Topic.PROTOCOL, "Stack: capture a response with processing time=", procTime, "s) : ", msg.toShortString());
                        }
                        
                        destScenario = message.getDestScenario();
                        GlobalLogger.instance().getApplicationLogger().info(Topic.PROTOCOL, "Routing: route the captured response by MessageId to \"", destScenario.getName(), "\" because of destScenario attribute.");
                    }
                }
                if (null != destScenario)
                {
                	GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Stack: destScenario =", destScenario.getName(), "\n isTransactionIdSupported=", isTransactionIdSupported, "\n transaction");
                }

                if (null == destScenario && routeDefaultResponse && isTransactionIdSupported && null != trans)
                {
                    if(null != trans)
                    {
                        destScenario = trans.getScenarioRunner();

                        if (null != destScenario)
                        {
                            GlobalLogger.instance().getApplicationLogger().info(Topic.PROTOCOL, "Routing: route the captured response by TRANSACTION_ID to \"", destScenario.getName(), "\" (TRANSACTION_ID=", msg.getTransactionId(), ") (MESSAGE_ID=", msg.getMessageId(), ").");
                        }
                    }

                }

                if (null == destScenario)
                {
	                destScenario = DispatcherMsg.dispatchMsg(msg);
                }
                
                if (null != destScenario)
                {                	
                    // logs in scenario and application logs as CALLFLOW topic
                    processLogsMsgSending(msg, destScenario, Stack.CAPTURE);
                    destScenario.dispatchMessage(msg);
                }
            }
        }
        
        // increment counters in the transport section
        incrStatTransport(msg, StackFactory.PREFIX_CAPTURING, StackFactory.PREFIX_CAPTURING);
        return true;
    }

    public void processLogsMsgSending(Msg msg, ScenarioRunner srcRunner, String action) throws Exception
    {
        if (TextListenerProviderRegistry.instance().getTextListenerProviderCount() > 0 &&
            GlobalLogger.instance().getLogLevel() <= GlobalLogger.LOG_LEVEL_CONFIG_INFO)
        {
            action += " ";
            if (msg.getRetransNumber() > 0)
            {
                action += "(#" + msg.getRetransNumber() + ") ";
            }
            GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CALLFLOW, action, msg.toShortString());
            if (srcRunner != null) 
            {
            	GlobalLogger.instance().getSessionLogger().info(srcRunner, TextEvent.Topic.CALLFLOW, action, msg.toShortString());
            }

            if (GlobalLogger.instance().getLogLevel() <= GlobalLogger.LOG_LEVEL_CONFIG_DEBUG)
            {
                GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CALLFLOW, action, msg);
                if (srcRunner != null)
                {
                	GlobalLogger.instance().getSessionLogger().debug(srcRunner, TextEvent.Topic.CALLFLOW, action, msg);
                }
            }
        }
    }    

    /** Get the time of the duration between a begin msg and an end message (in s) 
     * used to calculate processing time, response time and session time */
    public static float getTimeDuration(MsgLight msg, long beginMsg)
    {
        float transTime = ((float) (msg.getTimestamp() - beginMsg)) / 1000;
        return transTime;
    }

    /**
     *  increment counters in the transport section
     * @param msg
     * @param action
     * @throws Exception
     */
    protected void incrStatTransport(Msg msg, String actionRequest, String actionResponse) throws Exception
    {
    	Channel channel = msg.getChannel();
    	if (channel != null)
    	{
    		if (channel.getTransport() != null)
    		{
    			if (msg.isRequest())
    			{
	  				StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TRANSPORT, channel.getTransport(), msg.getProtocol(), msg.getTypeComplete() + actionRequest, "_transportNumber"), 1);
	   				StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TRANSPORT, channel.getTransport(), msg.getProtocol(), msg.getTypeComplete() + actionRequest, "_transportBytes"), (float) msg.getLength() / 1024 / 1024);
    			}
    			else
    			{
	  				StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TRANSPORT, channel.getTransport(), msg.getProtocol(), msg.getResultComplete() + actionResponse, "_transportNumber"), 1);
	   				StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TRANSPORT, channel.getTransport(), msg.getProtocol(), msg.getResultComplete() + actionResponse, "_transportBytes"), (float) msg.getLength() / 1024 / 1024);
    			}
    		}	    		
    	}
    }
    
    private void incrStatisticTransRequest(Msg msg, String actionRequest) throws Exception
    {

        StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_REQUEST, msg.getProtocol(), msg.getTypeComplete() + actionRequest, "_transRequestNumber"), 1);
    }

    private void incrStatisticTransResponse(Trans trans, Msg msg, String actionRequest, String actionResponse) throws Exception
    {
        StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_REQUEST, msg.getProtocol(), msg.getTypeComplete() + actionRequest, msg.getResultComplete() + actionResponse, "_transResponseNumber"), 1);
        StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_RESPONSE, msg.getProtocol(), msg.getResultComplete() + actionResponse, msg.getTypeComplete() + actionRequest, "_transResponseNumber"), 1);
        if (trans != null)
        {
	        float responseTime = Stack.getTimeDuration(msg, trans.getBeginMsg().getTimestamp());
	        if (StackFactory.PREFIX_OUTGOING.equals(actionResponse)) {
	        	GlobalLogger.instance().getApplicationLogger().info(Topic.PROTOCOL, "Send a response (response time = ", responseTime, " s) for the transaction : ", trans.getSummary(true), " (TRANSACTION_ID=", msg.getTransactionId(), ")");
	        } else {
	        	GlobalLogger.instance().getApplicationLogger().info(Topic.PROTOCOL, "Receive a response (response time = ", responseTime, " s) for the transaction : ", trans.getSummary(false), " (TRANSACTION_ID=", msg.getTransactionId(), ")");
	        }
	        StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_REQUEST, msg.getProtocol(), msg.getTypeComplete() + actionRequest, msg.getResultComplete() + actionResponse, "_responseTime"), responseTime);
	        StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_RESPONSE, msg.getProtocol(), msg.getResultComplete() + actionResponse, msg.getTypeComplete() + actionRequest, "_responseTime"), responseTime);
        }
    }

	/**
	 * 
	 * @return true if this data should be captured 
	 */
	public boolean isValidCapture(byte[] data){
		boolean result = false;
		/** TODO add the search in the transaction and in the session lists */
		/*
		Pattern pattern;
		if(null != pattern){
			matcher = pattern.matcher(new String(data));
			
			result = matcher.find();
			this.matcher.reset();
		}else{
			result = true;
		}
		*/
		return result;
	}

    public synchronized Trans getOutTransaction(TransactionId transactionId)
    {
        return outTransactions.get(transactionId);
    }

    public synchronized Trans getInTransaction(TransactionId transactionId)
    {
        return inTransactions.get(transactionId);
    }
 
    public synchronized Sess getSession(SessionId sessionId)
    {    
    	return outinSessions.get(sessionId);
    }
    
    /*
     * Remove eldest entry if instructed, else grow capacity if appropriate
     * in all stack lists
     */
    public void cleanStackLists(){
    	this.outTransactions.cleanEldestEntries();
    	this.inTransactions.cleanEldestEntries();
    	this.capTransactions.cleanEldestEntries();
    	this.outinSessions.cleanEldestEntries();
    	this.capSessions.cleanEldestEntries();
    }
    
    public ArrayList<Listenpoint> getAllListenpoint()
    {
    	ArrayList<Listenpoint> ret = new ArrayList<Listenpoint>();
    	
    	Set<String> cles = this.listenpoints.keySet();
    	Iterator<String> it = cles.iterator();
    	while (it.hasNext()){
    	   Object cle = it.next();
    	   ret.add(this.listenpoints.get(cle));
    	}
    	
    	return ret;
    }

}