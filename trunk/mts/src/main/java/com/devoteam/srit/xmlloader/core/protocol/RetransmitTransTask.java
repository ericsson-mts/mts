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

import com.devoteam.srit.xmlloader.core.ScenarioRunner;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;

import java.util.TimerTask;

import com.devoteam.srit.xmlloader.core.newstats.StatKey;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;


/**
 * Generic message manipulated by XML Loader's core.<br/>
 * Should be inherited by protocol-specific messages.
 * @author fhenry
 */
public class RetransmitTransTask extends TimerTask
{
    
    /** the transaction to retransmit */
    private Trans trans;
    
    /** Stack who is managing the transaction */
    private Stack stack;

    /** the different runner to log */
    private ScenarioRunner scRunner; 

    /** Creates a new instance of Transaction */
    public RetransmitTransTask(Stack stack, Trans trans, ScenarioRunner scRunner)
    {     
    	this.stack = stack;
    	this.trans = trans;       
    	this.scRunner = scRunner;    	
    }
       
    public void run()
    {
		Msg msg = trans.getBeginMsg();
    	try 
    	{
    		if (trans.shallRetransmit()) 
    		{
    			int retransNumber = msg.getRetransNumber() + 1;
    			msg.setRetransNumber(retransNumber);
    
        		if (stack.sendMessage(msg)) 
        		{
	    			float retransmitTime = ((float) (System.currentTimeMillis()- msg.getTimestamp())) /1000;
	    			
	                // logs in scenario and application logs as CALLFLOW topic
	            	stack.processLogsMsgSending(msg, scRunner, Stack.SEND);                			    		
	            	String logMsg = "Send an auto retransmission (index=" + msg.getRetransNumber() + ",time=" + retransmitTime + "s) for the message : "; 
                	GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, logMsg, msg.toShortString());
                	GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, logMsg, msg);
                	
	        		// update statistic counter
	        		if (msg.isRequest()) 
	        		{
			            StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_REQUEST, msg.getProtocol(), msg.getTypeComplete() + StackFactory.PREFIX_OUTGOING, "_retransmitNumber"), 1);			            			            
	        		} 
	        		else 
	        		{
			            StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_REQUEST, msg.getProtocol(), msg.getTypeComplete() + StackFactory.PREFIX_INCOMING, msg.getResultComplete() + StackFactory.PREFIX_OUTGOING, "_retransmitNumber"), 1);
			            StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_RESPONSE, msg.getProtocol(), msg.getResultComplete() + StackFactory.PREFIX_OUTGOING, msg.getTypeComplete() + StackFactory.PREFIX_INCOMING, "_retransmitNumber"), 1);			            
	        		}
	        		
	    			if (retransNumber >= stack.retransmitTimes.length)
	    			{
	    				String failureMsg = "Failure after auto retransmission (index=" + retransNumber + ",time=" + retransmitTime + "s) for the message : ";
	    				GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, failureMsg, msg.toString());
	    			}
        		}
	            trans.startAutomaticRetransmit();		
    		}
    	} 
    	catch (Exception e) {
    		GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Error while sending automatic retransmission of the request : ", e);
    	}
    }

}
