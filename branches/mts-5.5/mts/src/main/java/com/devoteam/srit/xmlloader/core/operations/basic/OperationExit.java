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
import com.devoteam.srit.xmlloader.core.exception.ExitExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.Operation;
import com.devoteam.srit.xmlloader.core.utils.XMLElementDefaultParser;

import org.dom4j.Element;

/**
 *
 * @author gpasquiers
 */
public class OperationExit extends Operation
{
    
    /**
     * Creates a new instance of OperationExit
     */
    public OperationExit(Element root)
    {
        super(root);
    }
    
    public Operation execute(Runner runner) throws Exception
    {
        restore();

        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, this);

        // Replace elements in XMLTree
        replace(runner, new XMLElementDefaultParser(runner.getParameterPool()), TextEvent.Topic.CORE);

        boolean failed = Boolean.parseBoolean(getAttribute("failed"));
        
        if (failed) 
        {
        	GlobalLogger.instance().getSessionLogger().error(runner, TextEvent.Topic.CORE, "Exit failed = ", failed);
        } else 
        {
        	GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "Exit failed = ", failed);
        }
        throw new ExitExecutionException(failed, "Exit Failed Exception");
    }
    
}