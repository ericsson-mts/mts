/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.operations.basic;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.Scenario;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.Operation;
import com.devoteam.srit.xmlloader.core.utils.XMLElementDefaultParser;

import org.dom4j.Element;

/**
 *
 * @author rbarbot
 */
public class OperationFor extends Operation
{
    private OperationSequence operationsSequence;
   
    private Scenario scenario;

    public OperationFor(Element root, Scenario scenario) throws Exception
    {
        super(root,false);
        this.scenario = scenario;        

        operationsSequence = new OperationSequence(root, this.scenario);
    }

    @Override
    public Operation execute(Runner runner) throws Exception
    {
        restore();

        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, this);

        // Replace elements in XMLTree
        replace(runner, new XMLElementDefaultParser(runner.getParameterPool()), TextEvent.Topic.CORE);

        // retrieve the xml attribute
        String index = getAttribute("index");
        int from = Integer.decode(getAttribute("from"));
        int to = Integer.decode(getAttribute("to"));
        int step = 1;
        String strStep = getAttribute("step");
        if (strStep != null)
        {
            step = Integer.decode(strStep);
        }
        
        Parameter parameterIndex = new Parameter();

        for (int i=from; i <= to; i+=step) {
            GlobalLogger.instance().getSessionLogger().debug(runner, TextEvent.Topic.CORE, "New iteration of "+index+"\nExecute XML\n", this);
            parameterIndex.add(i);
            runner.getParameterPool().set(index, parameterIndex);
            operationsSequence.execute(runner);
            parameterIndex.remove(0);
        }

        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "</for>");

        return null;
    }

}
