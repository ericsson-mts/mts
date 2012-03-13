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
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;


import com.devoteam.srit.xmlloader.core.protocol.TransactionId;
import org.dom4j.Element;

/**
 * TODO : statistics; request or answer ;
 * @author gpasquiers
 * @deprecated
 */
public class OperationSendMsg extends Operation
{
    
    private String protocol;
    
    private Boolean request;
       
    
    /** Creates a new instance */
    public OperationSendMsg(String aProtocol, Boolean aRequest, Element rootNode) throws Exception
    {
        super(rootNode);
        protocol = aProtocol;
        request = aRequest;              
        
        // deprecated message
        GlobalLogger.instance().logDeprecatedMessage(rootNode.getName() + " .../", "sendMessage" + protocol + " .../");

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

    	ScenarioRunner runner = (ScenarioRunner) aRunner;
    	
        GlobalLogger.instance().getSessionLogger().info(aRunner, TextEvent.Topic.PROTOCOL, this);

        // Replace elements in XMLTree
        replace(runner, StackFactory.getStack(protocol).getElementReplacer(runner.getParameterPool()), TextEvent.Topic.PROTOCOL);

        Element root = getRootElement();
        
        // instanciates the msg
        Msg msg = StackFactory.getStack(protocol).parseMsgFromXml(request, root, runner);
        msg.setSend(true);

        String remoteHost = getAttribute("remoteHost");
        String remotePort = getAttribute("remotePort");
        String remoteUrl = getAttribute("remoteURL");        
        String transport = getAttribute("transport");
                
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

        Listenpoint listenpoint = StackFactory.getStack(protocol).getListenpoint(null);
        msg.setListenpoint(listenpoint);
        
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
        
        // set the implicit message into the runner (for setFromMessage operation)
        runner.setCurrentMsg(msg);
        
        return null ;
    }
}
