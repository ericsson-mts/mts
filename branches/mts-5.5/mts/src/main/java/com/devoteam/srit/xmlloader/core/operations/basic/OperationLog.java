/*
 * OperationExit.java
 *
 * Created on 15 mai 2007, 11:56
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.operations.basic;

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.Operation;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLElementTextMsgParser;

import org.dom4j.Element;

/**
 *
 * @author gpasquiers
 */
public class OperationLog extends Operation
{
    
    /**
     * Creates a new instance of OperationExit
     */
    public OperationLog(Element root)
    {
        super(root);
    }
    
    public Operation execute(Runner runner) throws Exception
    {
        restore();

        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, this);                

        // Replace elements in XMLTree
        replace(runner, new XMLElementTextMsgParser(runner.getParameterPool()), TextEvent.Topic.CORE);

        String level = this.getAttribute("level");        
        if(null == level)
        {
            level = "INFO";
        }
        int intLevel = -1;
        if(Utils.isInteger(level))
        {
        	intLevel = 3 - Integer.parseInt(level);
        }
        
        String type = this.getAttribute("type");        
        if(null == type)
        {
            type = "Scenario";
        }

        String logStr = this.getRootElement().getText().trim(); 
        if ("Main".equalsIgnoreCase(type))
        {
        	if ((TextEvent.DEBUG_STRING.equalsIgnoreCase(level)) || (intLevel == TextEvent.DEBUG)) {
        		GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.USER, logStr);
        	}        	
        	else if ((TextEvent.INFO_STRING.equalsIgnoreCase(level)) || (intLevel == TextEvent.INFO)) {
        		GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.USER, logStr);
        	}
        	else if ((TextEvent.WARN_STRING.equalsIgnoreCase(level)) || (intLevel == TextEvent.WARN)) {
        		GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.USER, logStr);
        	}        	
        	else if ((TextEvent.ERROR_STRING.equalsIgnoreCase(level)) || (intLevel == TextEvent.ERROR)) {
        		GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.USER, logStr);
        	} 
        	else
        	{
                throw new ExecutionException("Level attribute should be an integer from [0-3] or a string from the list {DEBUG, INFO, WARN, ERROR}");        		
        	}
        }
        else if ("Scenario".equalsIgnoreCase(type))
        {
        	if ((TextEvent.DEBUG_STRING.equalsIgnoreCase(level)) || (intLevel == TextEvent.DEBUG)) {
        		GlobalLogger.instance().getSessionLogger().debug(runner, TextEvent.Topic.USER, logStr);
        	} 
        	else if ((TextEvent.INFO_STRING.equalsIgnoreCase(level)) || (intLevel == TextEvent.INFO)) {
        		GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.USER, logStr);
        	}
        	else if ((TextEvent.WARN_STRING.equalsIgnoreCase(level)) || (intLevel == TextEvent.WARN)) {
        		GlobalLogger.instance().getSessionLogger().warn(runner, TextEvent.Topic.USER, logStr);
        	}        	
        	else if ((TextEvent.ERROR_STRING.equalsIgnoreCase(level)) || (intLevel == TextEvent.ERROR)) {
        		GlobalLogger.instance().getSessionLogger().error(runner, TextEvent.Topic.USER, logStr);
        	} 
        	else
        	{
                throw new ExecutionException("Level attribute should be an integer from [0-3] or a string from the list {DEBUG, INFO, WARN, ERROR}");        		
        	}
        }
    	else
    	{
            throw new ExecutionException("Type attribute should be a string from the list {Main, Scenario}");        		
    	}
        
        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "Write in ", type, "log with the level = ", level, " the message  ", logStr);

        return null;
    }
    
}