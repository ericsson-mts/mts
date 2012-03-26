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
