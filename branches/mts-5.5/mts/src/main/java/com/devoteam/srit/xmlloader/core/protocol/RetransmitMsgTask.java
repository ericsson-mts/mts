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
