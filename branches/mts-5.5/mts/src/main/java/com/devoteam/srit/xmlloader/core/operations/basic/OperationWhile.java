/*
 * Created on 9 sept. 2005
 *
 */
package com.devoteam.srit.xmlloader.core.operations.basic;


import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.Scenario;
import com.devoteam.srit.xmlloader.core.exception.AssertException;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
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
public class OperationWhile extends Operation
{
    
    private OperationSequence operationsCondition;
    private OperationSequence operationsDo;
    
    private Scenario scenario;
    
    /**
     * Constructor
     *
     * @param condition 		boolean variable which represents a condition of the if statment
     * @param operationsThen 	List of operations executed if the value of condition is true
     * @param operationsElse	List of operations executed if the value of condition is false
     */
    public OperationWhile(Element root, Scenario scenario) throws Exception
    {
        super(root);
        this.scenario = scenario;
        Element element;
        
        element = root.element("condition");
        if(null != element) this.operationsCondition = new OperationSequence(element, this.scenario);
        else                this.operationsCondition = null;
        
        element = root.element("do");
        if(null != element) this.operationsDo = new OperationSequence(element, this.scenario);
        else                this.operationsDo = null;
    }
    
    /**
     * Execute operation
     * 
     * 
     * @param runner Current runner
     * @return Next operation or null by default
     * @throws ExecutionException
     */
    public Operation execute(Runner runner) throws Exception
    {
        restore();

        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, this);
        
        boolean condition = true;
        while(true)
        {
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "<condition>");
            GlobalLogger.instance().getSessionLogger().debug(runner, TextEvent.Topic.CORE, "Execute XML\n", this);
            try
            {
                if(null != operationsCondition) this.operationsCondition.execute(runner);
                GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "</condition> (OK)");
            }
            catch(AssertException e)
            {
                GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "</test> (KO)\n", e.getMessage());
                GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "</condition> (KO)");
                condition = false;
            }

            if(condition)
            {
                GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "<do>");
                if(null != operationsDo) this.operationsDo.execute(runner);
                GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "</do>");
            }
            else
            {
                break;
            }
        }

        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "</while>");
        
        return null;
    }
}
