/*
 * Transaction.java
 *
 * Created on 6 avril 2007, 16:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.core.protocol;

import java.util.Iterator;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.newstats.StatKey;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.utils.Utils;

/**
 * Generic session manipulated by IMSLoader's core.
 * @author fhenry
 */
public class Sess extends GroupMsg<Msg>
{

    /** Creates a new instance of Session */
    public Sess(Stack stack, Msg beginMsg) throws Exception
    {
    	super(stack, beginMsg);
    	if (beginMsg.getTransaction() != null)
    	{
    		beginMsg.getTransaction().setSession(this);
    	}
    }

    /** Add a end message to the list of the transaction */
    public boolean addEndMessage(Msg msg) throws Exception
    {
    	boolean ret = super.addEndMessage(msg);
    	if (msg.endSession())
    	{
    		this.active = false;
    	}
    	return ret;
    }

    /** Get a parameter from the message */
    public Parameter getParameter(String path) throws Exception
    {
        String[] params = Utils.splitPath(path);
    	Parameter parameter = new Parameter();

        if (params.length == 1)
        {
        	parameter.add(this);
        }
        else if (params[1].equalsIgnoreCase("id"))
        {
            parameter.add(getBeginMsg().getSessionId().toString());
        }
        else if (params[1].equalsIgnoreCase("name"))
        {
            parameter.add(getSummary());
        }        
        else if (params[1].equalsIgnoreCase("initial"))
        {
            parameter.add(getBeginMsg());
        }
        else if (params[1].equalsIgnoreCase("subsequents"))
        {
        	Iterator<Msg> iter = endListMsg.values().iterator();
        	while (iter.hasNext())
        	{
        		MsgLight msg = iter.next();
        		parameter.add(msg);	
        	}        
        }
        else
        {
        	Parameter.throwBadPathKeywordException(path);
        }
        return parameter;
    }

    public String getSummary() throws Exception
    {    	
    	// FH BUG potentiel 
    	// String ret = beginMsg.getTransaction().getSummary(!beginMsg.isSend());
    	String ret = beginMsg.getSummary(beginMsg.isSend(), true);
    	ret += "<BR>";	
        Iterator<Msg> iter = endListMsg.values().iterator();
        while (iter.hasNext())
        {
            MsgLight msg = (MsgLight) iter.next(); 
            ret += msg.getSummary(msg.isSend(), true);
            if (iter.hasNext())
            {
            	ret += "<BR>";
            }
        }    
        return ret;
    }

    /** 
     * Interface Removable 
     * */
    public void onRemove() throws Exception
    {    
    	Iterator<Msg> iter = endListMsg.values().iterator();
    	if (!iter.hasNext())
    	{
    		return;
    	}
    	
    	String statsKey = getSummary();
	    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_SESSION, beginMsg.getProtocol(), statsKey, "_beginSession"), 1);
	    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_SESSION, beginMsg.getProtocol(), statsKey, "_activeSession"), 1);

        String key = beginMsg.getSummary(beginMsg.isSend(), true);
    	StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_SESSION, beginMsg.getProtocol(), statsKey, key, "_messageNumber"), 1);
    	StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_SESSION, beginMsg.getProtocol(), statsKey, key, "_messageTime"), 0);

        MsgLight msg = null;
        while (iter.hasNext())
        {
            msg = (MsgLight) iter.next();
            key = msg.getSummary(msg.isSend(), true);
    	    float respTime = Stack.getTimeDuration(msg, beginMsg.getTimestamp());
   	    	StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_SESSION, beginMsg.getProtocol(), statsKey, key, "_messageNumber"), 1);
   	    	StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_SESSION, beginMsg.getProtocol(), statsKey, key, "_messageTime"), respTime);
        }
        
        if (!this.active)
        {
    	    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_SESSION, beginMsg.getProtocol(), statsKey, "_endSession"), 1);
    	    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_SESSION, beginMsg.getProtocol(), statsKey, "_activeSession"), -1);
    	    float sessTime = Stack.getTimeDuration(msg, beginMsg.getTimestamp());
    	    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_SESSION, beginMsg.getProtocol(), statsKey, "_sessionTime"), sessTime);
            GlobalLogger.instance().getApplicationLogger().info(Topic.PROTOCOL, "Finish a outgoing session (time = ", sessTime, " s) : ", getSummary());
        }
    }

    /** Returns the string description of the message. Used for logging as DEBUG level */
    public String toString()
    {
   		return super.toString("SESSION:Initial=", "SESSION:Subsequents=");	}
}
