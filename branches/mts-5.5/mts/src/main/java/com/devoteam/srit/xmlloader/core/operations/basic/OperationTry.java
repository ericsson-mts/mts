/*
 * Created on 9 sept. 2005
 *
 */
package com.devoteam.srit.xmlloader.core.operations.basic;


import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.Scenario;
import com.devoteam.srit.xmlloader.core.ScenarioRunner;
import com.devoteam.srit.xmlloader.core.exception.AssertException;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.exception.ExitExecutionException;
import com.devoteam.srit.xmlloader.core.exception.GotoExecutionException;
import com.devoteam.srit.xmlloader.core.exception.InterruptedExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.Operation;

import org.dom4j.Element;

/**
 * enables your scenario to execute operations according to value of a boolean variable named condition
 *
 * @author ma007141
 *
 */
public class OperationTry extends Operation
{
    
    private OperationSequence operationsFinally;
    private OperationSequence operationsDo;
    
    private Scenario scenario;
    
    public OperationTry(Element root, Scenario scenario) throws Exception
    {
        super(root);
        this.scenario = scenario;
        Element element;
        
        element = root.element("do");
        if(null != element) this.operationsDo = new OperationSequence(element, this.scenario);
        else                this.operationsDo = null;

        element = root.element("finally");
        if(null != element) this.operationsFinally = new OperationSequence(element, this.scenario);
        else                this.operationsFinally = null;
    }
    
    /**
     * Execute operation
     * 
     * 
     * @param runner Current runner
     * @throws ExecutionException
     */
    public Operation execute(Runner runner) throws Exception
    {
        restore();

        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, this);
        
        try
        {
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "<do>");
            if(null != this.operationsDo) this.operationsDo.execute(runner);
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "</do> (OK)");
        }
        catch(GotoExecutionException e)
        {
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "</do> (expected exception)\n", e);
            throw e;
        }
        catch(ExitExecutionException e)
        {
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "</do> (expected exception)\n", e);
            throw e;
        }
        catch(AssertException e)
        {
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "</do> (expected exception)\n", e);
            throw e;
        }
        catch(InterruptedExecutionException e)
        {
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "</do> (expected exception)\n", e);
            throw e;
        }
        catch(Exception e)
        {
            GlobalLogger.instance().getSessionLogger().warn(runner, TextEvent.Topic.CORE, e, "</do> (KO)\n");
        }
        finally
        {
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "<finally>");
            if(null != this.operationsFinally){
                try{
                    ((ScenarioRunner) runner).finallyEnter();
                    this.operationsFinally.execute(runner);
                }
                finally{
                    ((ScenarioRunner) runner).finallyExit();
                }
            }
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "</finally>");
        }

        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "</try>");
        
        return null;
    }
}
