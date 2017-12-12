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
public class OperationFor extends Operation {

    private OperationSequence operationsSequence;
    private Scenario scenario;

    public OperationFor(Element root, Scenario scenario) throws Exception {
        super(root, XMLElementDefaultParser.instance(), false);
        this.scenario = scenario;

        operationsSequence = new OperationSequence(root, this.scenario);
    }

    @Override
    public Operation execute(Runner runner) throws Exception 
    {
        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, this);

        String index;
        double from;
        double to;
        double step;
        String strStep;

        try 
        {
            lockAndReplace(runner);
            GlobalLogger.instance().getSessionLogger().debug(runner, TextEvent.Topic.CORE, "Operation after pre-parsing \n", this);

            index = getAttribute("index");
            
            String strFrom = getAttribute("from");
            from = Double.parseDouble(strFrom);
            String strTo = getAttribute("to");
            to = Double.parseDouble(strTo);
            step = 1;
            strStep = getAttribute("step");
            if (strStep != null) 
            {
                step = Double.parseDouble(strStep);
            }
        }
        finally 
        {
            unlockAndRestore();
        }

        Parameter parameterIndex = new Parameter();

        if (step > 0)
        {
	        for (double i = from; i <= to; i += step) 
	        {
	            // case when the index is an integer
	            String strI = ((Double) i).toString();
	            if (strI.endsWith(".0"))
	            {
	            	strI = strI.substring(0, strI.length() - 2);
	            }
	            parameterIndex.add(strI);
	            runner.getParameterPool().set(index, parameterIndex);
	            GlobalLogger.instance().getSessionLogger().debug(runner, TextEvent.Topic.CORE, "New iteration of " + index + "\nExecute XML\n", this);
	            
	            operationsSequence.execute(runner);

	            parameterIndex.remove(0);
	            runner.getParameterPool().delete(index);
	        }
        }
        
        if (step < 0)
        {
	        for (double i = from; i >= to; i += step) 
	        {
	        	// case when the index is an integer
	            String strI = ((Double) i).toString();
	            if (strI.endsWith(".0"))
	            {
	            	strI = strI.substring(0, strI.length() - 2);
	            }
	            parameterIndex.add(strI);
	            runner.getParameterPool().set(index, parameterIndex);
	            GlobalLogger.instance().getSessionLogger().debug(runner, TextEvent.Topic.CORE, "New iteration of " + index + "\nExecute XML\n", this);
	            
	            operationsSequence.execute(runner);
	            
	            parameterIndex.remove(0);
	            runner.getParameterPool().delete(index);
	        }
        }

        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "</for>");

        return null;
    }
}
