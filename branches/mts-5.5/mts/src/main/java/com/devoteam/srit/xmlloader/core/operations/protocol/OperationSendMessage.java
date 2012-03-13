/*
 * OperationSendMsgAAA.java
 *
 * Created on 6 avril 2007, 16:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.operations.protocol;


import com.devoteam.srit.xmlloader.diameter.MsgDiameterParser;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.ScenarioRunner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.Operation;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;


import com.devoteam.srit.xmlloader.core.protocol.TransactionId;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.utils.Config;

import org.dom4j.Element;

/**
 * TODO : statistics; request or answer ;
 * @author gpasquiers
 */
public class OperationSendMessage extends Operation
{
    
    private String protocol;           
    
    /** Creates a new instance */
    public OperationSendMessage(String aProtocol, Element rootNode) throws Exception
    {
        super(rootNode);
        protocol = aProtocol;
        
        if(protocol.equals(StackFactory.PROTOCOL_DIAMETER))
        {
            //
            // Read ApplicationId
            //
            String applicationId = rootNode.element("header").attributeValue("applicationId");
            MsgDiameterParser.getInstance().doDictionnary(this.getRootElement(), applicationId, true);
        }
    }
    
    /** Executes the operation */
    public Operation execute(Runner aRunner) throws Exception
    {
        restore();

    	Stack stack = StackFactory.getStack(protocol);
        ScenarioRunner runner = (ScenarioRunner) aRunner;
    	
        GlobalLogger.instance().getSessionLogger().info(aRunner, TextEvent.Topic.PROTOCOL, this);

        // Replace elements in XMLTree
        replace(runner, stack.getElementReplacer(runner.getParameterPool()), TextEvent.Topic.PROTOCOL);

        Element root = getRootElement();
        
        // deprecated 
        String request = getAttribute("request");
        Boolean req = null;
        if(null != request)
        {
            if (StackFactory.PROTOCOL_DIAMETER.equalsIgnoreCase(protocol))
            {
            	GlobalLogger.instance().logDeprecatedMessage( root.getName() + " request=\"xxx\" .../", "sendMessage" + protocol + " .../><header request=\"xxx\" .../");
            } 
            else if (StackFactory.PROTOCOL_RADIUS.equalsIgnoreCase(protocol))
            {
            	GlobalLogger.instance().logDeprecatedMessage( root.getName() + " request=\"xxx\" .../", "sendMessage" + protocol + " .../");
            }
             req = Boolean.valueOf(request);
        }

        // instanciates the msg
        Msg msg = stack.parseMsgFromXml(req, root, runner);
        msg.setSend(true);

        String listenpointName = getAttribute("listenpoint");
        String channelName = getAttribute("channel");
        String remoteHost = getAttribute("remoteHost");
        String remotePort = getAttribute("remotePort");
        String remoteUrl = getAttribute("remoteURL");
        String transport = getAttribute("transport");

        if((null != listenpointName) && (null != channelName))
        {
            throw new Exception("There must be just a listenpoint or a channel to send message, not both");
        }
        if(((null != remoteHost) || (null != remoteHost) || (null != remoteUrl)) && 
            (null != channelName))
        {
            throw new Exception("RemoteHost and remotePort cannot be set with the channel");
        }

        if(null != channelName)
        {
            Channel channel = stack.getChannel(channelName);
            if (channel == null)
            {
                throw new ExecutionException("The channel <name=" + channelName + "> does not exist");
            }
            msg.setChannel(channel);
        } 
        
        Listenpoint listenpoint = stack.getListenpoint(listenpointName);
        if (listenpointName != null && listenpoint == null)
        {
            throw new ExecutionException("The listenpoint <name=" + listenpointName + "> does not exist");
        }
        msg.setListenpoint(listenpoint);
        
        if(null != remoteHost)
        {
        	msg.setRemoteHost(remoteHost);
        }
        if(null != remotePort)
        {
        	msg.setRemotePort(Integer.parseInt(remotePort));
        }
        if(null != remoteUrl)
        {
        	msg.setRemoteUrl(remoteUrl);
        }        
        if(null != transport)
        {        
        	msg.setTransport(transport);
        }
               
        // Not a generic mechanism : USED only for HTTP and IMAP
        // Should be copy to StackHttp and StackImap in the parseMsgFromXml() method
        String transactionIdAttribute = getAttribute("transactionId");
        if(null != transactionIdAttribute)
        {
            msg.setTransactionId(new TransactionId(transactionIdAttribute));
        }
        
        String destScenarioAttribute = getAttribute("destScenario");
        Runner destRunner = null;
        if(null != destScenarioAttribute)
        {
            ScenarioRunner scenarioRunner = (ScenarioRunner) runner;
            
            for(ScenarioRunner aScenarioRunner:scenarioRunner.getParent().getChildren())
            {
                if(aScenarioRunner.getName().equals(destScenarioAttribute))
                {
                    destRunner = aScenarioRunner;
                }
            }
            
            if (null == destRunner)
            {
                GlobalLogger.instance().getSessionLogger().error(runner, TextEvent.Topic.PROTOCOL, "Unknown destination scenario ", destScenarioAttribute);
                throw new ExecutionException("The destination scenario '" + destScenarioAttribute + "' mentioned in the scenario is not defined");
            }
        }

        String strDelay = getAttribute("delay");
        if(null != strDelay)
        {
            long lDelay = (long) (Float.parseFloat(strDelay) *  1000);
            msg.setTimestamp(System.currentTimeMillis() + lDelay);
        }

        Runner answerHandler = null;
        String answerHandlerAttribute = getAttribute("answerHandler");
        if(null == answerHandlerAttribute)
        {
            answerHandler = runner;
        }
        else
        {
            ScenarioRunner scenarioRunner = (ScenarioRunner) runner;
            
            for(ScenarioRunner aScenarioRunner:scenarioRunner.getParent().getChildren())
            {
                if(aScenarioRunner.getName().equals(answerHandlerAttribute))
                {
                    answerHandler = aScenarioRunner;
                }
            }
            
            if (null == answerHandlerAttribute)
            {
                GlobalLogger.instance().getSessionLogger().error(runner, TextEvent.Topic.PROTOCOL, "Unknown answerHandler scenario ", destScenarioAttribute);
                throw new ExecutionException("The answerHandler scenario '" + answerHandlerAttribute + "' mentioned in the scenario is not defined");
            }
            

        }        
        // send the request via the stack
        StackFactory.getStack(protocol).sendMessage(msg, (ScenarioRunner) runner, (ScenarioRunner) destRunner, (ScenarioRunner) answerHandler);

        if(protocol.equals(StackFactory.PROTOCOL_RTPFLOW))
        {
            if(((com.devoteam.srit.xmlloader.rtp.flow.MsgRtpFlow)msg).isSynchronous())//wait for the end of the flow
            {
                //take a mutex which will be given by the end of the flow
                ((com.devoteam.srit.xmlloader.rtp.flow.MsgRtpFlow)msg).getSynchronousSemaphore().acquire();
            }
        }
        
        // set the implicit message into the runner (for setFromMessage operation)
        runner.setCurrentMsg(msg);
        
        return null ;
    }
}
