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

package com.devoteam.srit.xmlloader.core.operations.protocol;


import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.Scenario;
import com.devoteam.srit.xmlloader.core.ScenarioRunner;
import com.devoteam.srit.xmlloader.core.exception.AssertException;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.Operation;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.XMLElementDefaultParser;

import java.util.LinkedList;
import java.util.List;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.DefaultElementInterface;

/**
 *
 * @author gpasquiers
 * @deprecated
 * 
 */
public class OperationReceiveMsg extends Operation
{
    
    /** Parent scenario */
    private Scenario scenario;
    
    private String protocol;

    private boolean failedOnTimeout;

    private boolean failedOnReceive;
    
    /** List of filters */
    private List<Operation> operations ;
    
    /** Creates a new instance of ReceiveMsgOperation */
    public OperationReceiveMsg(String protocol, boolean request, String channel, String listenpoint, String type, String result, Element root, Scenario aScenario) throws Exception
    {
        super(root, XMLElementDefaultParser.instance(), false);
        this.protocol = protocol;
        operations = new LinkedList<Operation>();
        scenario = aScenario ;

        // deprecated message
        GlobalLogger.instance().logDeprecatedMessage( root.getName() + " .../", "receiveMessage" + protocol + " .../");
        
        // add the corresponding <parameter> and <test> xml tag
        addParameterTestTag(root, "string.equals", "message.protocol", protocol);
        addParameterTestTag(root, "string.equals", "message.request", new Boolean(request).toString());

        if(null != channel)
        {
            addParameterTestTag(root, "string.equals", "channel.name", channel);
        }

        if(null != listenpoint)
        {
            addParameterTestTag(root, "string.equals", "listenpoint.name", listenpoint);
        }

        if(null != type)
        {
            addParameterTestTag(root, "string.contains", "message.typeComparison", ":" + type + ":");
        }
        
        if(null != result)
        {
            addParameterTestTag(root, "string.contains", "message.resultComparison", result);
        }
        
        parse(root);
    }
    
    public OperationReceiveMsg(Element root, Scenario aScenario) throws Exception
    {
        super(root, XMLElementDefaultParser.instance(), false);
        scenario = aScenario ;
        operations = new LinkedList<Operation>();
        parse(root);
    }
    
    /** Executes the operation (retrieve and check message) */
    public Operation execute(Runner aRunner) throws Exception
    {               
    	ScenarioRunner runner = (ScenarioRunner) aRunner;
    	
        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.PROTOCOL, this);
        
        String failedOnTimeoutStr;
        String failedOnReceiveStr;
        String timeoutStr;

        try {
            lockAndReplace(runner);
            GlobalLogger.instance().getSessionLogger().debug(runner, TextEvent.Topic.PROTOCOL, "Operation after pre-parsing \n", this);
            failedOnTimeoutStr = getAttribute("failedOnTimeout");
            failedOnReceiveStr = getAttribute("failedOnReceive");
            timeoutStr = getAttribute("timeout");
        }
        finally {
            unlockAndRestore();
        }
        
        // Replace elements in XMLTree
       
        if(null == failedOnTimeoutStr)
        {
            failedOnTimeoutStr = "true";
        }
        
        if(failedOnTimeoutStr.equalsIgnoreCase("false"))
        {
            this.failedOnTimeout = false;
        }
        else if(failedOnTimeoutStr.equalsIgnoreCase("true"))
        {
            this.failedOnTimeout = true;
        }
        else
        {
            throw new ExecutionException("failedOnTimeout (" + failedOnTimeoutStr + ") should be a boolean");
        }
         
        if(null == failedOnReceiveStr)
        {
            failedOnReceiveStr = "false";
        }
        
        if(failedOnReceiveStr.equalsIgnoreCase("false"))
        {
            this.failedOnReceive = false;
        }
        else if(failedOnReceiveStr.equalsIgnoreCase("true"))
        {
            this.failedOnReceive = true;
        }
        else
        {
            throw new ExecutionException("failedOnReceive (" + failedOnReceiveStr + ") should be a boolean");
        }        
        
                
        // instantiate the factory
        if(null != protocol)
        {
            StackFactory.getStack(protocol);
        }
        else
        {
            StackFactory.getAllStacks();
        }
        
        Msg msg ;
       
        long timeout;
        if (timeoutStr != null)
        {
            timeout = (long) (Float.parseFloat(timeoutStr) *  1000);
        }
        else if (protocol != null)
        {
            timeout = StackFactory.getStack(protocol).receiveTimeout;
        } else {
        	timeout = (long) (Config.getConfigByName("tester.properties").getDouble("operations.RECEIVE_TIMEOUT", 30) * 1000);
        }
        	
        if (timeout <=0)
        {
            timeout =  Long.MAX_VALUE;
        }
        
        long timestamp = System.currentTimeMillis();
        
        Exception error = null;
        try
        {
            while(null != (msg = runner.getBufferMsg().readMessageFromStack(timeout - (System.currentTimeMillis() - timestamp))))
            {
                GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.PROTOCOL, "Testing message\n",  msg);

                // set the implicit message into the runner (for setFromMessage operation)
                runner.setCurrentMsg(msg);
                
                boolean isValid = true;
                try
                {
                    for(Operation o:operations)
                    {
                        o.executeAndStat(runner);
                    }
                }
                catch(AssertException e)
                {
                    GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.PROTOCOL, "</test> (KO)\n", e.getMessage());
                    isValid = false ;
                }

                if(isValid)
                {
                    runner.getBufferMsg().removeMsgFromStack(msg);
                    break ;
                }
                else
                {
                    GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.PROTOCOL, "The message does not match the conditions");
                }
            }
        }
        catch(Exception e)
        {
            GlobalLogger.instance().getSessionLogger().error(runner, TextEvent.Topic.PROTOCOL, e, "Error in <", this.getName(), ">");
            throw new ExecutionException("Error in OperationReceiveMessage", e);
        }
        finally
        {
            runner.getBufferMsg().resetMsgStackFlag();
        }

        if(this.failedOnReceive)
        {
            if(null == msg)
            {
                GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.PROTOCOL, "<", this.getName(), "> OK (no message)");
            }
            else
            {
                GlobalLogger.instance().getSessionLogger().error(runner, TextEvent.Topic.PROTOCOL, "<", this.getName(), " KO : ", msg.getProtocol(), " message ", msg);
                throw new ExecutionException("Received a valid message (failedOnReceive=true) after " + (System.currentTimeMillis() - timestamp) + "" + this, error);
            }
        }
        else if(this.failedOnTimeout)
        {
            if(null == msg)
            {
                GlobalLogger.instance().getSessionLogger().error(runner, TextEvent.Topic.PROTOCOL, "<", this.getName(), "> TIMEOUT (no message)");                
                throw new ExecutionException("Error occured while waiting for a Msg (failedOnTimeout=true) after " + (System.currentTimeMillis() - timestamp) + "ms\n" + this, error);
            }
            else
            {
                GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.PROTOCOL, "<", this.getName(), "> OK : ", msg.getProtocol(), " message ", msg.toShortString());
                GlobalLogger.instance().getSessionLogger().debug(runner, TextEvent.Topic.PROTOCOL, "<", this.getName(), " OK : ", msg.getProtocol(), " message ", msg);
            }
        } else 
        {
            if(null == msg)
            {
                GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.PROTOCOL, "<", this.getName(), " TIMEOUT (no message)\n", this);
            }
            else
            {
                GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.PROTOCOL, "<", this.getName(), "> OK : ", msg.getProtocol(), " message ", msg);
            }        	
        }
        runner.getBufferMsg().resetMsgStackFlag();
        
        return null ;
    }
    
    /** Parses the operation from a root element */
    private void addParameterTestTag(Element root, String operation, String path, String value) throws Exception
    {
        DefaultElement filter ;
        
        filter = new DefaultElement("parameter");
        filter.addAttribute("name", "[reserved_param]");
        filter.addAttribute("operation", "protocol.setFromMessage");
        filter.addAttribute("value", path);
        
        DefaultElementInterface.addNode((DefaultElement) root, filter, 0);
        
        filter = new DefaultElement("test");
        filter.addAttribute("parameter", "[reserved_param]");
        filter.addAttribute("condition", operation);
        filter.addAttribute("value", value);

        DefaultElementInterface.addNode((DefaultElement) root, filter, 1);
    }
    
    /** Parses the operation from a root element */
    private void parse(Element root) throws Exception
    {
        List<Element> list = root.elements();
        
        for(Element element:list)
        {
            operations.add(scenario.parseOperation(element));
        }
    }
}
