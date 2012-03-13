/*
 * Transaction.java
 *
 * Created on 6 avril 2007, 16:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.protocol;

import java.util.TimerTask;

import com.devoteam.srit.xmlloader.core.ScenarioRunner;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;

/**
 * Generic message manipulated by XML Loader's core.<br/>
 * Should be inherited by protocol-specific messages.
 * @author fhenry
 */
public class RetransmitMsgTask extends TimerTask
{
    
    /** the msg to differ */
    private Msg msg;
        
    /** the different runner to log */
    ScenarioRunner srcRunner; 
    ScenarioRunner destRunner; 
    ScenarioRunner answerHandler;
    /** Stack who is managing the transaction */
    private Stack stack;
    
    /** Creates a new instance of Transaction */
    public RetransmitMsgTask(Stack stack, Msg msg, ScenarioRunner srcRunner, ScenarioRunner destRunner, ScenarioRunner answerHandler)
    {     
    	this.stack = stack;
    	this.msg = msg;       
    	this.srcRunner = srcRunner; 
    	this.destRunner = destRunner; 
    	this.answerHandler = answerHandler;
            	
    }
       
    public void run()
    {    	
    	try 
    	{
    		stack.sendMessageException(msg, srcRunner, destRunner, answerHandler);    		
   		} 
    	catch (Exception e) {
    		GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "Error while sending the request : ", msg, e);
    	}
    }

}
