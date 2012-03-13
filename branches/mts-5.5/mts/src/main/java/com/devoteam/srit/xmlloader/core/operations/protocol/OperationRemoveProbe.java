/*
 * ReceiveMsgOperation.java
 *
 * Created on 12 avril 2007, 15:35
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.operations.protocol;


import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.Operation;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.XMLElementDefaultParser;
import org.dom4j.Element;

/**
 *
 * @author fhenry
 */
public class OperationRemoveProbe extends Operation
{      
    private String protocol;
    
    /** Creates a new instance of ReceiveMsgOperation */
    public OperationRemoveProbe(String protocol, Element rootElement) throws Exception
    {
        super(rootElement);
        this.protocol = protocol;        
    }
    
    /** Executes the operation (retrieve and check message) */
    public Operation execute(Runner runner) throws Exception
    {
        restore();

        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.PROTOCOL, this);

        // Replace elements in XMLTree
        replace(runner, new XMLElementDefaultParser(runner.getParameterPool()), TextEvent.Topic.PROTOCOL);

        Element root = getRootElement();        
        String probeName = root.attributeValue("name");

    	GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CALLFLOW, ">>>REMOVE ", protocol, " capture probe <name= \"", probeName, "\">");	        	
        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CALLFLOW, ">>>REMOVE ", protocol, " capture probe <name= \"", probeName, "\">");       
        
        // close the probe
        StackFactory.getStack(protocol).removeProbe(probeName);       
        
        return null;
    }
    
}
