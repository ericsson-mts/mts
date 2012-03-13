/*
 * OperationSendMsgAAA.java
 *
 * Created on 6 avril 2007, 16:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.operations.protocol;

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.Operation;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.XMLElementDefaultParser;

import org.dom4j.Element;

/**
 * TODO : statistics; request or answer ;
 * @author gpasquiers
 */
public class OperationCreateListenpoint extends Operation
{
    
    private String protocol;    
    
    /** Creates a new instance */
    public OperationCreateListenpoint(String aProtocol, Element rootNode) throws Exception
    {
        super(rootNode);
        protocol = aProtocol;

        // deprecated message
        String rootName = rootNode.getName();
        if ("openProviderSIP".equals(rootName))
        {
        	GlobalLogger.instance().logDeprecatedMessage("openProviderSIP .../", "createListenpointSIP .../");        	
        }
    }
    
    /** Executes the operation */
    public Operation execute(Runner runner) throws Exception
    {
        restore();


        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.PROTOCOL, this);

        // Replace elements in XMLTree
        replace(runner, new XMLElementDefaultParser(runner.getParameterPool()), TextEvent.Topic.PROTOCOL);

        Element root = getRootElement();

        //
        // Instanciate the listenpoint from Xml file
        //
        Listenpoint listenpoint = StackFactory.getStack(protocol).parseListenpointFromXml(root);

        //
        // Get if she does not already exists and compare to the existing one
        //
        synchronized  (StackFactory.getStack(protocol)) {
        	
        	Listenpoint oldListenpoint = StackFactory.getStack(protocol).getListenpoint(listenpoint.getName());        
	        if ((oldListenpoint != null) && (!listenpoint.equals(oldListenpoint))) {	        	
	            throw new ExecutionException("A listenpoint <name=" + listenpoint.getName() + "> already exists with other attributes.");
	        }

	        if (oldListenpoint == null) {
	        	GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CALLFLOW, ">>>CREATE ", protocol, " listenpoint <", listenpoint, ">");	        		        	
	            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CALLFLOW, ">>>CREATE ", protocol, " listenpoint <", listenpoint, ">");
	            
	            StackFactory.getStack(protocol).createListenpoint(listenpoint,protocol);
	        }
        }        
        
        return null ;
    }
}
