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
import com.devoteam.srit.xmlloader.core.protocol.Probe;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.XMLElementDefaultParser;

import org.dom4j.Element;

/**
 * @author fhenry
 */
public class OperationCreateProbe extends Operation
{
    
    private String protocol;    
    
    /** Creates a new instance */
    public OperationCreateProbe(String aProtocol, Element rootNode) throws Exception
    {
        super(rootNode);
        protocol = aProtocol;
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
        // Instanciate the connection from Xml file
        //
        Probe probe = StackFactory.getStack(protocol).parseProbeFromXml(root);

        //
        // Get if she does not already exists and compare to the existing one
        //
        synchronized  (StackFactory.getStack(protocol)) {
        	
        	Probe oldProbe = StackFactory.getStack(protocol).getProbe(probe.getName());        
	        if ((oldProbe != null) && (!probe.equals(oldProbe))) {	        	
	            throw new ExecutionException("A probe <name=" + probe.getName() + "> already exists with other attributes.");
	        }

	        if (oldProbe == null) {
	        	GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CALLFLOW, ">>>CREATE ", protocol, " probe <", probe, ">");	        		        	
	            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CALLFLOW, ">>>CREATE ", protocol, " probe <", probe, ">");
	            
	            StackFactory.getStack(protocol).createProbe(probe,protocol);
	        }
        }        
        
        return null ;
    }
}
