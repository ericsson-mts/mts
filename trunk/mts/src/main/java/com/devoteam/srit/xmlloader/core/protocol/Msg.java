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
import com.devoteam.srit.xmlloader.core.ScenarioReference;
import com.devoteam.srit.xmlloader.core.ScenarioRunner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.expireshashmap.Removable;
import com.devoteam.srit.xmlloader.sigtran.fvo.FvoMessage;
import com.devoteam.srit.xmlloader.sigtran.tlv.TlvMessage;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;

import java.net.URI;
import java.util.LinkedList;

/**
 * Generic message manipulated by XML Loader's core.<br/>
 * Should be inherited by protocol-specific messages.
 * @author gpasquiers
 */
public abstract class Msg extends MsgLight implements Removable
{
	/** Maximum number of characters to write into the log */
    protected static int MAX_STRING_LENGTH = Config.getConfigByName("tester.properties").getInteger("logs.MAX_STRING_LENGTH", 1000);

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
    
    protected Channel channel = null;
    protected Listenpoint listenpoint = null;
    protected Probe probe = null;
    
    private String remoteHost = null;
    private int remotePort =  -1;
    private String transport = null;
    
    private ScenarioRunner destScenario = null;
    
    private Long timestampCaptureFile = null;
    
    public Msg()
    {
        this.isMessageIdSet = false;
        this.isTransactionIdSet = false;
        this.isRetransmissionIdSet = false;
        this.transaction = null;
    }

    /** Get a parameter from the message */
    public Parameter getParameter(String path) throws Exception
    {
    	path = path.trim();
        String[] params = Utils.splitPath(path);
        if (params.length < 1)
        {
            return null;
        }
        
		Parameter var = new Parameter();
        // DEPRECATED value
        //---------------------------------------------------------------------- message:connection -        
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
            	var.add(Array.toHexString(new DefaultArray(getBytesData())));
            }
            else if (params[1].equalsIgnoreCase("text"))
            {
            	var.add(new String(getBytesData()));
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

    /** Returns a value from the diameter message */
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

    /** Get the message Identifier of this message */
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

    /** Get the scenario name owning this message (used for dispatching) */
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

    /** Get the transaction Identifier of this message */
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
                throw new ExecutionException("Can't get transactionID information " + this, e);
            }

        }

        return this.transactionId;
    }

    /** Get the transaction Identifier of this message */
    public void setTransactionId(TransactionId transactionId)
    {
        this.transactionId = transactionId;
        this.isTransactionIdSet = true;
    }

    public Trans getTransaction()
    {
        return this.transaction;
    }

    public void setTransaction(Trans transaction)
    {
        this.transaction = transaction;
    }

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

    /**
     * Set the channel used by this message
     */
    public void setChannel(Channel channel)
    {
        this.channel= channel;
    }

    /**
     * get the channel used by this message
     */
    public Channel getChannel()
    {
        return channel;
    }

    /**
     * Set the listenpoint used by this message
     */
    public void setListenpoint(Listenpoint listenpoint)
    {
        this.listenpoint = listenpoint;
    }

    /**
     * get the listenpoint used by this message
     */
    public Listenpoint getListenpoint()
    {
        return listenpoint;
    }

    /**
     * Set the probe used by this message
     */
    public void setProbe(Probe probe)
    {
        this.probe= probe;
    }

    /**
     * get the probe used by this message
     */
    public Probe getProbe()
    {
        return probe;
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
    
    /** Get the protocol of this message */
    public abstract String getProtocol();

    /** Get the type (eg the command code for aaa, the method for sip) of this message */
    public abstract String getType() throws Exception;

    /** Get the type for comparison (eg the command code for aaa, the method for sip) of this message */
    public String getTypeComparison() throws Exception
    {
    	return ":" + getTypeComplete() + ":";
    }

    /** Get the complete type (with dictionary conversion) of this message */
    public String getTypeComplete() throws Exception
    {
    	return getType();
    }

    /** Get the result of this answer (null if request) */
    public abstract String getResult() throws Exception;

    /** Get the result for comparison (eg the command code for aaa, the method for sip) of this message */
    public String getResultComparison() throws Exception
    {
    	return ":" + getResultComplete() + ":";
    }

    /** Get the complete result of this answer (null if request) */
    public String getResultComplete() throws Exception
    {
    	return getResult();
    }
    
    /** Return true if the message is a request else return false*/
    public abstract boolean isRequest() throws Exception;
    
    /** Get the data of this message */
    public abstract byte[] getBytesData();
       
    /** Return the length of the message*/
    public int getLength()
    {
    	return getBytesData().length;
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
	    URI uri = null;
	    try
	    {
	        uri = new URI(remoteUrl).normalize();
	    }
	    catch (Exception e)
	    {
	        throw new ExecutionException("Can't create URI from : " + remoteUrl, e);
	    }
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

	public ScenarioRunner getDestScenario() {
		return destScenario;
	}

	public void setDestScenario(ScenarioRunner destScenario) {
		if (destScenario != null && destScenario.getScenarioState())
		{
			this.destScenario = destScenario;
		}
	}

    public void setTimestampCaptureFile(long time){
        timestampCaptureFile = time;
    }
    
    public Long getTimestampCaptureFile(){
        return timestampCaptureFile;
    }
	
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

    /** Returns a short description of the message. Used for logging as INFO level */
    /** This methods HAS TO be quick to execute for performance reason */
    public String toShortString() throws Exception {
        String ret = getProtocol() + " > " + getTypeComplete();
        if (!isRequest())
        {
        	ret += " > " + getResultComplete();
        }
        ret += "\n";
        return ret;
    }
    
    /** Returns the string description of the message. Used for logging as DEBUG level */
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
			ret += "<CHANNEL " + channel + ">\n";
		}
		if (listenpoint != null)
		{
			ret += "<LISTENPOINT " + listenpoint + ">\n";
		}
		if (probe != null)
		{
			ret += "<PROBE " + probe + ">\n";
		}
		
        return ret.trim();
    }

    /** Get the XML representation of the message for the genscript module. */
    public abstract String toXml() throws Exception;


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
    
}
