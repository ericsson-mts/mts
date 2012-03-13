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
import com.devoteam.srit.xmlloader.core.utils.XMLElementDefaultParser;

import org.dom4j.Element;

/**
 * enables your scenario to execute operations according to value of a boolean variable named condition
 *
 * @author ma007141
 *
 */
public class OperationIf extends Operation
{
    
    private OperationSequence operationsCondition;
    private OperationSequence operationsThen;
    private OperationSequence operationsElse;
    
    private Scenario scenario;
    
    /**
     * Constructor
     *
     * @param condition 		boolean variable which represents a condition of the if statment
     * @param operationsThen 	List of operations executed if the value of condition is true
     * @param operationsElse	List of operations executed if the value of condition is false
     */
    public OperationIf(Element root, Scenario scenario) throws Exception
    {
        super(root);
        this.scenario = scenario;
        Element element;
        
        element = root.element("condition");
        if(null != element) this.operationsCondition = new OperationSequence(element, this.scenario);
        else                this.operationsCondition = null;
        
        element = root.element("then");
        if(null != element) this.operationsThen = new OperationSequence(element, this.scenario);
        else                this.operationsThen = null;

        element = root.element("else");
        if(null != element) this.operationsElse = new OperationSequence(element, this.scenario);
        else                this.operationsElse = null;
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
        
        // Replace elements in XMLTree
        // No attribute to replace on <if> operation
        // replace(runner, new XMLElementDefaultParser(runner.getParameterPool()), TextEvent.Topic.CORE);        
        
        boolean condition = true;        
        try
        {
            if(null != operationsCondition) operationsCondition.execute(runner);
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
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "<then>");
            if(null != operationsThen) operationsThen.execute(runner);
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "</then>");
        }
        else
        {
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "<else>");
            if(null != operationsElse) operationsElse.execute(runner);
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "</else>");
        }
        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "</if>");

        return null;
    }
}
