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
import java.util.Map;
import java.util.LinkedHashMap;

import com.devoteam.srit.xmlloader.core.ScenarioRunner;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.expireshashmap.Removable;

/**
 * Generic transaction manipulated by XML Loader's core.
 * @author fhenry
 */
public abstract class GroupMsg<V extends MsgLight> implements Removable
{	
	/** the active flag */
	protected boolean active = true;
    /** the message at the beginning of the transaction */
    protected V beginMsg;
    /** the list of end messages of the transaction */
    protected Map<String, V> endListMsg;
    /** Scenario runner sending the request (used for dispatching responses) */
    protected ScenarioRunner scRunner;
    /** Stack who is managing the transaction */
    protected Stack stack;

    /** Creates a new instance of Transaction */
    public GroupMsg(Stack stack, V beginMsg) throws Exception
    {
        this.stack = stack;
        this.beginMsg = beginMsg;
        this.endListMsg = new LinkedHashMap<String, V>();
    }

    /** Get message at the begining of the transaction */
    public V getBeginMsg()
    {
        return beginMsg;
    }

    /** Add a end message to the list of the transaction */
    public boolean addEndMessage(V msg) throws Exception
    {
        boolean res = false;
        String key = null;
        if (msg.isRequest())
        {
            key = msg.getType();
        }
        else
        {
            key = msg.getResult();
        }
        V endMsg = endListMsg.get(key);
        if (endMsg != null)
        {
            res = true;
        }
        else
        {
            msg.setRetransNumber(0);
            // put the message in the end list
            endListMsg.put(key, msg);
        }
        return res;
    }

    /** Returns the string description of the message. Used for logging as DEBUG level */
    public String toString(String beginLabel, String endLabel)
    {
    	
    	String ret = beginLabel;
    	ret += "****************************************" + "\n";
		try
		{
	    	ret += beginMsg.toString() + "\n";
	    	ret += endLabel;
	    	ret += "****************************************" + "\n";
	    	Iterator<V> iter = endListMsg.values().iterator();
	    	while (iter.hasNext())
	    	{
	    		Msg msg = (Msg) iter.next();
	    		ret += msg + "\n";
		    	ret += "****************************************" + "\n";
	    	}
		} 
		catch (Exception e) 
		{
	        GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "An error occured while logging the transaction");
	    	e.printStackTrace();
			return null;
		}
		return ret;
    }
    
    /** Get the scenario owning this message (used for dispatching) */
    public ScenarioRunner getScenarioRunner()
    {
        return scRunner;
    }

    /** Set the scenario owning this message (used for dispatching) */
    public void setScenarioRunner(ScenarioRunner scRunner)
    {
        this.scRunner = scRunner;
    }
    
}
