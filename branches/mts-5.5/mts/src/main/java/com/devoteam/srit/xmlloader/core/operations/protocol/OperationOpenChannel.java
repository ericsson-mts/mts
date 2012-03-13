/*
 * OperationOpenChannel.java
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
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.XMLElementDefaultParser;

import org.dom4j.Element;

/**
 * TODO : statistics; request or answer ;
 * @author gpasquiers
 */
public class OperationOpenChannel extends Operation
{
    
    private String protocol;    
    
    /** Creates a new instance */
    public OperationOpenChannel(String aProtocol, Element rootNode) throws Exception
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
        // Instanciate the channel from Xml file
        //
        Channel channel = StackFactory.getStack(protocol).parseChannelFromXml(root, protocol);

        //
        // Get if she does not already exists and compare to the existing one
        //
        synchronized  (StackFactory.getStack(protocol)) {
        	
        	Channel oldChannel = StackFactory.getStack(protocol).getChannel(channel.getName());        
	        if ((oldChannel != null) && (!channel.equals(oldChannel))) {	        	
	            throw new ExecutionException("A channel <name=" + channel.getName() + "> already exists with other attributes.");
	        }

	        if (oldChannel == null) {
	        	GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CALLFLOW, ">>>OPEN ", protocol, " channel <", channel, ">");	        	
	            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CALLFLOW, ">>>OPEN ", protocol, " channel <", channel, ">");
	            
	            StackFactory.getStack(protocol).openChannel(channel);
	        }
        }        
        
        return null ;
    }
}
