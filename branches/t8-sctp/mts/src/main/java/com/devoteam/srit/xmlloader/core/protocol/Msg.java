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

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.ParameterKey;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.ScenarioRunner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.expireshashmap.Removable;
import com.devoteam.srit.xmlloader.sigtran.fvo.FvoMessage;
import com.devoteam.srit.xmlloader.sigtran.tlv.TlvMessage;

//TODO refactor
import com.devoteam.srit.xmlloader.sctp.MsgTransportInfosSctp;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.dom4j.Element;

/**
 * Generic message manipulated by XML Loader's core.<br/>
 * Should be inherited by protocol-specific messages.
 * @author gpasquiers
 */
public abstract class Msg extends MsgLight implements Removable
{
	/** Maximum number of characters to write into the log */
    protected static int MAX_STRING_LENGTH = Config.getConfigByName("tester.properties").getInteger("logs.MAX_STRING_LENGTH", 1000);

    // path information
    protected TransactionId transactionId;
    protected boolean isTransactionIdSet;
    private MessageId messageId;
    private boolean isMessageIdSet;
    private RetransmissionId retransmissionId;
    private boolean isRetransmissionIdSet;
    private Trans transaction;
    private boolean isSessionIdSet;
    private SessionId sessionId;
    private LinkedList<String> scenarioName = null; 
    private ScenarioRunner destScenario = null;
    
    protected Stack stack = null;
    
    // transport elements
    protected Channel channel = null;
    protected Listenpoint listenpoint = null;
    protected Probe probe = null;
    
	/*
	 * Transport layer informations
	 */
	public interface TransportInfos{
		/**
		 * @param transportInfosElements
		 * @throws Exception
		 */
		
		public void parseFromXml(Collection<Element> transportInfosElements)throws Exception;
		
		/**
		 * 
		 * @param parameterKey the parameter key
		 * @return the parameter value or an empty Parameter if the key is not  valid
		 */
		@Nonnull
		public Parameter getParameter( ParameterKey parameterKey )throws ParameterException;
	 }

    /*
     * transport layer informations
     */
    @Nullable
    protected TransportInfos transportInfos;
  
    private String remoteHost = null;
    private int remotePort =  -1;
    private String transport = null;
        
    private Long timestampCaptureFile = null;
    
    public Msg()
    {
        this.isMessageIdSet = false;
        this.isTransactionIdSet = false;
        this.isRetransmissionIdSet = false;
        this.transaction = null;
    }

    public Msg(Stack stack)
    {
        this.isMessageIdSet = false;
        this.isTransactionIdSet = false;
        this.isRetransmissionIdSet = false;
        this.transaction = null;
        this.stack = stack;
    }

    //--------------------------------------------------------------------------------
    // methods based on path keyword on message configured on the protocol config file
    //--------------------------------------------------------------------------------
    
    /** Get the message Identifier of the message */
    public MessageId getMessageId() throws Exception
    {
        if (!this.isMessageIdSet)
        {
            try
            {
                String paths = StackFactory.getStack(getProtocol()).getConfig().getString("route.MESSAGE_ID");
                if (paths.length() > 0)
                {
                    this.messageId = new MessageId(getFromMessage(paths));
                    this.isMessageIdSet = true;
                }
                else
                {
                    synchronized (this)
                    {
                        if (!this.isMessageIdSet)
                        {
                            this.messageId = new MessageId(Long.toString(Stack.nextTransactionId()));
                            this.isMessageIdSet = true;
                        }
                    }
                }
            }
            catch (Exception e)
            {
                this.isMessageIdSet = false;
                messageId = null;
                throw new ExecutionException("Can't get messageID information " + this, e);
            }
        }
        return this.messageId;
    }

    /** Get the scenario name of the message (used for message routing by scenarioName) */
    public LinkedList<String> getScenarioName() throws Exception
    {
        if (scenarioName == null)
        {            
            // read from config files
            scenarioName = new LinkedList<String>();
            String paths = StackFactory.getStack(getProtocol()).getConfig().getString("route.SCENARIO_NAME");
            if (paths.length() > 0)
            {
                String[] tabPaths = Utils.splitNoRegex(paths, "|");
                for (int i = 0; i < tabPaths.length; i++)
                {
                    try
                    {
                        scenarioName.add(getFromMessage(tabPaths[i]));
                    }
                    catch(Exception e)
                    {
                        throw new ExecutionException("Can't get scenarioName information " + this, e);
                    }
                }
            }
        }
        return scenarioName;
    }
    
    /** Get the destScenario of the message (used for message routing by destScenario) */
	public ScenarioRunner getDestScenario() {
		return destScenario;
	}

	/** Set the destScenario of the message (used for message routing by destScenario) */
	public void setDestScenario(ScenarioRunner destScenario) {
		if (destScenario != null && destScenario.getScenarioState())
		{
			this.destScenario = destScenario;
		}
	}

    /** Get the transaction Identifier of the message */
    public TransactionId getTransactionId() throws Exception
    {

        if (!this.isTransactionIdSet)
        {
            // read from config files
            try
            {
                Config conf = StackFactory.getStack(getProtocol()).getConfig();
                String paths = conf.getString("route.TRANSACTION_ID");
                if (paths.length() > 0)
                {
                    this.transactionId = new TransactionId(getFromMessage(paths));
                    this.isTransactionIdSet = true;
                }
                else
                {
                    synchronized(this)
                    {
                        if (!this.isTransactionIdSet)
                        {
                            this.transactionId = new TransactionId(Long.toString(Stack.nextTransactionId()));
                            this.isTransactionIdSet = true;
                        }
                    }
                }
            }
            catch (Exception e)
            {
                this.transactionId = null;
                this.isTransactionIdSet = false;
                throw new Exception("Can't get transactionID information " + this, e);
            }

        }

        return this.transactionId;
    }

    /** Get the transaction Identifier of the message */
    public void setTransactionId(TransactionId transactionId)
    {
        this.transactionId = transactionId;
        this.isTransactionIdSet = true;
    }

    /** Get the retyransmission Identifier of the message */
    public RetransmissionId getRetransmissionId() throws Exception
    {
        if (!this.isRetransmissionIdSet)
        {
            // read from config files
            try
            {
                Config conf = StackFactory.getStack(getProtocol()).getConfig();
                String paths = conf.getString("route.RETRANSMISSION_ID");
                if (paths.length() > 0)
                {
                    this.retransmissionId = new RetransmissionId(getFromMessage(paths));
                    this.isRetransmissionIdSet = true;
                }
                else
                {
                    synchronized(this)
                    {
                        if (!this.isRetransmissionIdSet)
                        {
                            this.retransmissionId = new RetransmissionId(Long.toString(Stack.nextTransactionId()));
                            this.isRetransmissionIdSet = true;
                        }
                    }
                }
            }
            catch (Exception e)
            {
                this.retransmissionId = null;
                this.isRetransmissionIdSet = false;
                throw new ExecutionException("Can't get retransmissionID information " + this, e);
            }
        }

        return this.retransmissionId;
    }

    /** Get the session Identifier of the message */
    public SessionId getSessionId() throws Exception
    {
        if (!this.isSessionIdSet)
        {
            // read from config files
            try
            {
                Config conf = StackFactory.getStack(getProtocol()).getConfig();
                String paths = conf.getString("route.SESSION_ID");
                if (paths != null && paths.length() > 0)
                {
                    this.sessionId = new SessionId(getFromMessage(paths));
                    this.isSessionIdSet = true;
                }
            }
            catch (Exception e)
            {
                this.sessionId = null;
                this.isSessionIdSet = false;
                throw new ExecutionException("Can't get sessionID information " + this, e);
            }
        }

        return this.sessionId;
    }

    //------------------------------------------------
    // methods concerning the transport of the message
    //------------------------------------------------
    
    /**
     * Set the channel used by the message
     */
    public void setChannel(Channel channel)
    {
        this.channel= channel;
    }

    /**
     * get the channel used by the message
     */
    public Channel getChannel()
    {
        return channel;
    }

    /**
     * Set the listenpoint used by the message
     */
    public void setListenpoint(Listenpoint listenpoint)
    {
        this.listenpoint = listenpoint;
    }

    /**
     * get the listenpoint used by the message
     */
    public Listenpoint getListenpoint()
    {
        return listenpoint;
    }

    /**
     * Set the probe used by the message
     */
    public void setProbe(Probe probe)
    {
        this.probe= probe;
    }

    /**
     * get the probe used by the message
     */
    public Probe getProbe()
    {
        return probe;
    }
    
	public String getRemoteHost() {
		return remoteHost;
	}

	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	public String getRemoteUrl() {
		return protocol + ":" + remotePort + ":" + remotePort;
	}
	
	public void setRemoteUrl(String remoteUrl) throws Exception {
	    URI uri = new URI(remoteUrl).normalize();
	    this.remoteHost = uri.getHost();
	    this.remotePort = uri.getPort();
	    String scheme = uri.getScheme();
	    if (scheme.toLowerCase().endsWith("s"))
	    {
	    	this.transport = StackFactory.PROTOCOL_TLS;
	    }
	}
	
    /** Return the transport of the message*/
	public String getTransport() {
		return transport;
	}

	public void setTransport(String transport) {
		this.transport = transport;
	}
	
	//-----------------------------------------------------------------------------------------
	// generic methods for protocol request type result retransmission, transaction and session
	//-----------------------------------------------------------------------------------------

    /** 
     * Get the protocol acronym of the message 
     */
    public String getProtocol()
    {
    	String msgClassname = this.getClass().getSimpleName();
    	return msgClassname.substring(3).toUpperCase();
    }

    /** 
     * Return true if the message is a request else return false
     */
    public abstract boolean isRequest() throws Exception;

    /** 
     * Get the type of the message
     * Used for message filtering with "type" attribute and for statistic counters 
     */
    public String getType() throws Exception
    {
    	String type = null;
        // for response message
        if (!isRequest())
        {
			Trans trans = getTransaction(); 
	    	if (trans != null)
	    	{
		    	Msg request = trans.getBeginMsg();
		    	if (request != null)
		    	{
		    		type = request.getType();
		    	}
	    	}
        }
        return type;
    }

    /** Get the type for comparison of the message */
    public String getTypeComparison() throws Exception
    {
    	String type = getTypeComplete();
    	if (type != null)
    	{
    		type = type.replace(StackFactory.SEP_SUB_INFORMATION, ":");
    	}
    	return ":" + type + ":";
    }

    /** Get the complete type (with dictionary conversion) of the message */
    public String getTypeComplete() throws Exception
    {
    	String type = getType();
    	type = Utils.getPrintableChar(type);
    	return type;
    }

    /** 
     * Get the result of the message (null if request)
     * Used for message filtering with "result" attribute and for statistic counters 
     */
    public abstract String getResult() throws Exception;

    /** Get the result for comparison of the message */
    public String getResultComparison() throws Exception
    {
    	String result = getResultComplete();
    	if (result != null)
    	{
    		result = result.replace(StackFactory.SEP_SUB_INFORMATION, ":");
    	}
    	return ":" + result + ":";
    }

    /** Get the complete result of the message (null if request) */
    public String getResultComplete() throws Exception
    {
    	//String result = getResult();
    	//result = Utils.getPrintableChar(result);
    	return getResult();
    }

    public void setTimestampCaptureFile(long time){
        timestampCaptureFile = time;
    }
    
    public Long getTimestampCaptureFile(){
        return timestampCaptureFile;
    }

    /**
     *  Tell whether the message shall be retransmitted or not 
     * (= true by default) 
     */
    public boolean shallBeRetransmitted() throws Exception
    {
        return true;
    }

    /**
     *  Tell whether the message shall be stop the automatic 
     *  retransmmission mechanism or not 
     * (= true by default) 
     */
    public boolean shallStopRetransmit() throws Exception
    {
        return true;
    }

    /** Get the transaction the message is belonging in */
    public Trans getTransaction()
    {
        return this.transaction;
    }

    /** Set the transaction the message is belonging in */
    public void setTransaction(Trans transaction)
    {
        this.transaction = transaction;
    }
    
    /**
     *  Tell whether the response begin the transaction or not 
     * (= true by default) 
     */
    public boolean beginTransaction() throws Exception
    {
        return true;
    }

    /**
     *  Tell whether the response end the transaction or not 
     * (= true by default) 
     */
    public boolean endTransaction() throws Exception
    {
        return true;
    }

    /**
     *  Tell whether the message shall begin a new session 
     * (= false by default) 
     */
    public boolean beginSession() throws Exception
    {
        return false;
    }

    /**
     *  Tell whether the message shall end a session 
     * (= false by default) 
     */
    public boolean endSession() throws Exception
    {
        return false;
    }

    public void onRemove() throws Exception
    {
    	// nothing to do
    }

    
    //-------------------------------------------------
    // methods for the encoding / decoding of the message
    //-------------------------------------------------
    
    /** 
     * encode the message to binary data 
     */
    public abstract byte[] encode() throws Exception;
       
    /** 
     * decode the message from binary data 
     */
    public abstract void decode(byte[] data) throws Exception; 
    
    /** 
     * Return the length of the message
     */
    public int getLength() throws Exception
    {
    	byte[] array = encode();
    	if (array != null)
    	{
    		return array.length;
    	}
    	return 0;
    }

    
    //---------------------------------------------------------------------
    // methods for the XML display / parsing of the message
    //---------------------------------------------------------------------
    
    /** 
     * Returns a short description of the message. Used for logging as INFO level
     * This methods HAS TO be quick to execute for performance reason 
     */
    public String toShortString() throws Exception {
        String ret = getProtocol() + " > " + getTypeComplete();
        if (!isRequest())
        {
        	ret += " > " + getResultComplete();
        }
        return ret;
    }
    
    /** 
     * Returns the string description of the message. Used for logging as DEBUG level 
     */
    public String toString()
    {
    	String ret = getProtocol() + " > ";
		// display the xml representation
		try
    	{
			ret += toXml();
        }
        catch (Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Error while performing toString on Msg : ");
        }

		// cut if message is too long
        if (ret.length() > MAX_STRING_LENGTH)
        {
        	ret = " {" + MAX_STRING_LENGTH + " of " + ret.length() + "} " + ret.substring(0, MAX_STRING_LENGTH);
        }

        ret += "\n";
        // display transport info
		if (channel != null)
		{
			ret += channel.toXml() + "\n";
		}
		if (listenpoint != null)
		{
			ret += listenpoint.toXml();
		}
		if (probe != null)
		{
			ret += probe.toXml();
		}
		
        return ret.trim();
    }

    /** 
     * Convert the message to XML document 
     */
    public abstract String toXml() throws Exception;

    
    /**
     * 
     */
    public static final class ParseFromXmlContext{
    	private Boolean request = null;
    	private String transport = null;
    	private Listenpoint listenpoint = null;
    	private Channel channel = null;
    	
    	
    	
    	public ParseFromXmlContext(){
    	}
    	
    	
    	
    	public ParseFromXmlContext setRequest(boolean value){
    		this.request = new Boolean(value);
    		return this;
    	}
    	
    	public boolean hasRequest(){
    		return this.request!=null;
    	}
    	
    	/** TODO return IMMUTABLE or boolean to avoid side-effects */
    	public Boolean getRequest(){
    		return this.request;
    	}
    	
    	
    	
    	public ParseFromXmlContext setTransport(String value){
    		if( value!=null ){
    			this.transport = value.toUpperCase();
    		}
    		else{
    			this.transport = null;
    		}
    		return this;
    	}
    	
    	public boolean hasTransport(){
    		return this.transport!=null;
    	}
    	
    	public String getTransport(){
    		return this.transport;
    	}
    	
    	
    	public ParseFromXmlContext setListenpoint(Listenpoint value){
    		this.listenpoint = value;
    		return this;
    	}
    	
    	public boolean hasListenpoint(){
    		return this.listenpoint!=null;
    	}
    	
    	/** TODO return IMMUTABLE */
    	public Listenpoint getListenpoint(){
    		return this.listenpoint;
    	}
    	
    	
    	public ParseFromXmlContext setChannel(Channel value){
    		this.channel = value;
    		return this;
    	}
    	
    	public boolean hasChannel(){
    		return this.channel!=null;
    	}
    	
    	/** TODO return IMMUTABLE */
    	public Channel getChannel(){
    		return this.channel;
    	}
    }
    
    /** 
     * Parse the message from XML element 
     */
    public void parseFromXml(ParseFromXmlContext context, Element root, Runner runner) throws Exception{
    	
		@SuppressWarnings("unchecked")
		List<Element> transportInfosElements = root.elements("transportInfos");
		
		if( !transportInfosElements.isEmpty() && context.hasTransport()){
			//TODO refactor (instanciation should not be hardcoded here)
	        switch (context.getTransport())
	        {
	        case StackFactory.PROTOCOL_SCTP:
	        	this.setTransportInfos( new MsgTransportInfosSctp() );	
	        	break;
	        }
		}

		if( this.transportInfos!=null ){
    		this.transportInfos.parseFromXml(transportInfosElements);
		}
    }

    /** summary of the message used for statistics counters */
    public String getSummary(boolean send, boolean prefix) throws Exception
    {
    	String ret = null;
    	if (isRequest())
    	{
    		ret = getTypeComplete();
    	}
    	else
    	{
    		ret = getResultComplete();
    	}
    	if (prefix)
    	{
			if (send)
			{
				ret += StackFactory.PREFIX_OUTGOING;
			}
			else
			{
				ret += StackFactory.PREFIX_INCOMING;
			}
    	}
    	return ret;
    }

    //------------------------------------------------------
    // method for the "setFromMessage" <parameter> operation
    //------------------------------------------------------
    
    /** 
     * Get a parameter from the message 
     */
    public Parameter getParameter(String path) throws Exception
    {
        ParameterKey key = new ParameterKey(path);
        String[] params = key.getSubkeys();

        if (params.length < 1)
        {
            return null;
        }
        
		Parameter var = new Parameter();
        // DEPRECATED value connection => channel        
        if (params[0].equalsIgnoreCase("connection"))
        {
        	GlobalLogger.instance().logDeprecatedMessage("parameter operation=\"protocol.setFromMessage\" value=\"connection:*\"/", "parameter operation=\"protocol.setFromMessage\" value=\"channel:*\"/");
        	if (this.channel != null)
        	{
        		var = this.channel.getParameter(path);
        	}
       		return var;
        }
        else if (params[0].equalsIgnoreCase("channel"))
        {
        	if (this.channel != null)
        	{
        		var = this.channel.getParameter(path);
        	}
       		return var;
        } 
        else if (params[0].equalsIgnoreCase("listenpoint"))
        {
        	if (this.listenpoint != null)
        	{
        		var = this.listenpoint.getParameter(path);
        	}
       		return var;
       	}
        else if (params[0].equalsIgnoreCase("probe"))
        {
        	if (this.probe != null)
        	{
        		var = this.probe.getParameter(path);
        	}
       		return var;
       	}        	
        else if (params[0].equalsIgnoreCase("message"))
        { 
            if (params.length == 1)
            {
            	var.add(this);
            }
            else if (params[1].equalsIgnoreCase("type"))
            {
                var.add(getType());
            }
            else if (params[1].equalsIgnoreCase("typeComparison"))
            {
                var.add(getTypeComparison());
            }
            else if (params[1].equalsIgnoreCase("request"))
            {
            	var.add(String.valueOf(isRequest()));
            }
            else if (params[1].equalsIgnoreCase("result"))
            {
            	var.add(getResult());
            }
            else if (params[1].equalsIgnoreCase("resultComparison"))
            {
            	var.add(getResultComparison());
            }            
            else if (params[1].equalsIgnoreCase("protocol"))
            {
            	var.add(getProtocol());
            }
            else if (params[1].equalsIgnoreCase("transactionId"))
            {
            	TransactionId trans = getTransactionId();
            	if (trans != null)
            	{
            		var.add(trans.toString());
            	}
            }
            else if (params[1].equalsIgnoreCase("messageId"))
            {
            	MessageId mess = getMessageId();
            	if (mess != null)
            	{
            		var.add(mess.toString());
            	}
            }
            else if (params[1].equalsIgnoreCase("scenarioName"))
            {
            	var.add(getScenarioName());
            }
            else if (params[1].equalsIgnoreCase("retransmissionId"))
            {
            	RetransmissionId retrans = getRetransmissionId();
            	if (retrans != null)
            	{
            		var.add(retrans.toString());
            	}
            }
            else if (params[1].equalsIgnoreCase("sessionId"))
            {
            	SessionId sess = getSessionId();
            	if (sess != null)
            	{
            		var.add(sess.toString());
            	}
            }
            else if (params[1].equalsIgnoreCase("timestamp"))
            {
            	var.add(new Long(getTimestamp()).toString());
            }
            else if (params[1].equalsIgnoreCase("timestampCaptureFile"))
            {
                if(getTimestampCaptureFile() != null)
                    var.add(new Long(getTimestampCaptureFile()).toString());
            }
            else if (params[1].equalsIgnoreCase("length"))
            {
            	var.add(new Long(getLength()).toString());
            }
            else if (params[1].equalsIgnoreCase("binary"))
            {
            	var.add(Array.toHexString(new DefaultArray(encode())));
            }
            else if (params[1].equalsIgnoreCase("text"))
            {
            	var.add(new String(encode()));
            }
            else if (params[1].equalsIgnoreCase("xml"))
            {
            	String xml = "<msg>" + toXml() + "</msg>";
            	var.add(xml);
            }
            else if (params[1].equalsIgnoreCase("transportInfos") )
            {
            	if( this.transportInfos!=null ){
            		ParameterKey transportInfosKey = key.shift(2);
            		var = this.transportInfos.getParameter( transportInfosKey );
            	}
            }
            else
            {
            	Parameter.throwBadPathKeywordException(path);
            }
            return var;
        }
        else if (params[0].equalsIgnoreCase("transaction"))
        {
        	if (this.transaction != null)
        	{
        		var = this.transaction.getParameter(path);
        	}
       		return var;
       	}
        else if (params[0].equalsIgnoreCase("session"))
        {
			SessionId sessId = getSessionId();
			Sess sess = StackFactory.getStack(getProtocol()).getSession(sessId);
			if (sess != null)
			{
				var = sess.getParameter(path);
			}
			else if (this.transaction != null)
        	{
        		Sess session = this.transaction.getSession();
        		if (session != null)
        		{
       				var = session.getParameter(path);
        		}
        		else
        		{
	        		Msg msg = this.transaction.getBeginMsg();
	        		if (msg != null)
	        		{
	        			SessionId sessTransId = msg.getSessionId();
	        			Sess sessTrans = StackFactory.getStack(getProtocol()).getSession(sessTransId);
	        			if (sessTrans != null)
	        			{
	        				var = sessTrans.getParameter(path);
	        			}
	        		}
        		}
        	}
       		return var;
       	}        
        return null;
    }

    /** Returns a value from the message */
    private String getFromMessage(String paths) throws Exception
    {
        String[] tabPaths = Utils.splitNoRegex(paths, ",");
        String result = "";
        for (int i = 0; i < tabPaths.length;)
        {
            Parameter var = new Parameter();
            var = getParameter(tabPaths[i]);
            if (var.length() > 0)
            {
            	result += var.get(0);
            }
            i++;
            if (i != tabPaths.length)
            {
                result += "|";
            }

        }
        return result;
    }
    
    /** check wether message is a command to initiate TLS handshake over TCP socket or not **/
    public boolean isSTARTTLS_request()
    {
    	return false;
    }
    
    public boolean isSTARTTLS_answer()
    {
    	return false;
    }
    
    public boolean isSTOPTLS_answer()
    {
    	return false;
    }
    
    public boolean isSTOPTLS_request()
    {
    	return false;
    }

    public int getFvoProtocol()
    {
    	return -1;
    }
    
    public void setFvoProtocol(int protocol) 
    {
    }

    public FvoMessage getFvoMessage() 
    {
        return null;
    }

    public void setFvoMessage(FvoMessage fvoMessage) 
    {
    }

    public int getTlvProtocol()
    {
    	return -1;
    }
    
    public void setTlvProtocol(int protocol) 
    {
    }

    public TlvMessage getTlvMessage() 
    {
        return null;
    }

    public void setTlvMessage(TlvMessage tlvMessage) 
    {
    }
    
    /**
	 * @return the transportInfos
	 */
	public TransportInfos getTransportInfos() {
		return transportInfos;
	}

	/**
	 * @param transportInfos the transportInfos to set
	 */
	public void setTransportInfos(TransportInfos transportInfos) {
    	assert this.transportInfos==null;
		this.transportInfos = transportInfos;
	}
    
}
