package com.devoteam.srit.xmlloader.core.operations.basic;


import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.exception.GotoExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.Operation;
import com.devoteam.srit.xmlloader.core.utils.XMLElementDefaultParser;

import org.dom4j.Element;

/**
 * OperationGoto operation
 */
public class OperationGoto extends Operation
{
    /**
     * Constructor
     * 
     * 
     * @param name Ope name
     * @param label OperationGoto label
     */
    public OperationGoto(Element root)
    {
        super(root);
    }
    
    /**
     * Execute operation
     *
     * @param session Current session
     * @return Next operation or null by default
     * @throws ExecutionException
     */
    public Operation execute(Runner runner) throws Exception
    {
        restore();

        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, this);

        // Replace elements in XMLTree
        replace(runner, new XMLElementDefaultParser(runner.getParameterPool()), TextEvent.Topic.CORE);

        String label = getAttribute("label");

        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "Branching to label ", label);
        throw new GotoExecutionException(label);
    }
}
