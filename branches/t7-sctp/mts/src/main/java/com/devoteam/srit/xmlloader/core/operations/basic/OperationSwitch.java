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


import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.Scenario;
import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.Operation;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.dom4j.Element;

/**
 * enables your scenario to execute operations according to value of a boolean variable named condition
 *
 * @author ma007141
 *
 */
public class OperationSwitch extends Operation
{
    private String parameter;
    private String[] equalsValues;
    private String[] matchesValues;
    private OperationSequence[] operationSequences;
    private OperationSequence defaultOperationSequence;

    private Scenario scenario;
    
    /**
     * Constructor
     *
     * @param condition 		boolean variable which represents a condition of the if statment
     * @param operationsThen 	List of operations executed if the value of condition is true
     * @param operationsElse	List of operations executed if the value of condition is false
     */
    public OperationSwitch(Element root, Scenario scenario) throws Exception
    {
        super(root, null);
        this.scenario = scenario;
        this.parameter = root.attributeValue("parameter");
        List<Element> list = root.elements("case");

        equalsValues = new String[list.size()];
        matchesValues = new String[list.size()];
        operationSequences = new OperationSequence[list.size()];
        
        int i = 0;
        for(Element element:list)
        {
            operationSequences[i] = new OperationSequence(element, scenario);
            equalsValues[i] = element.attributeValue("equals");
            matchesValues[i] = element.attributeValue("matches");
            
            boolean isEquals = null != equalsValues[i];
            boolean isMatches = null != matchesValues[i];

            if(!(isEquals ^isMatches)) throw new ParsingException("case element cannot contain both matches and equals attributes");
            
            i++;
        }
        
        Element defaultElement = root.element("default");
        if(null != defaultElement) defaultOperationSequence = new OperationSequence(defaultElement, scenario);
        else defaultOperationSequence = null;
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
        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, this);

        // Replace elements in XMLTree
        // No attribute to replace on <switch> operation        
        //replace(runner, new XMLElementTextMsgParser(runner.getParameterPool()), TextEvent.Topic.CORE);

        List switchList = runner.getParameterPool().parse(this.parameter);
        int i;
        for(i=0; i<equalsValues.length; i++)
        {
            boolean isEquals = null != equalsValues[i];
            boolean isMatches = null != matchesValues[i];

            List caseList = null;
            if(isEquals)
            {
                caseList = runner.getParameterPool().parse(equalsValues[i]);
                GlobalLogger.instance().getSessionLogger().debug(runner, TextEvent.Topic.CORE, "Testing if switch values ", switchList, " equals case values ", caseList);
            }
            if(isMatches)
            {
                caseList = runner.getParameterPool().parse(matchesValues[i]);
                GlobalLogger.instance().getSessionLogger().debug(runner, TextEvent.Topic.CORE, "Testing if switch values ", switchList, " matches case values ", caseList);
            }
            

            Iterator caseIterator = caseList.iterator();
            Iterator switchIterator = switchList.iterator();
            
            if(switchList.size() != caseList.size()) continue;
            
            boolean failed = false;
            while(switchIterator.hasNext() && caseIterator.hasNext())
            {
                if(isEquals)
                {
                    if(!caseIterator.next().equals(switchIterator.next()))
                    {
                        failed = true;
                        break;
                    }
                }
                else if(isMatches)
                {
                    if(!switchIterator.next().toString().matches(caseIterator.next().toString()))
                    {
                        failed = true;
                        break;
                    }
                }
            }
            
            if(failed) continue;
            else break;
        }

        if(i != equalsValues.length)
        {
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "<case>");
            operationSequences[i].execute(runner);
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "</case>");
        }
        else if(null != this.defaultOperationSequence)
        {
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "<default>");
            defaultOperationSequence.execute(runner);
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "</default>");
        }
        else
        {
            GlobalLogger.instance().getSessionLogger().warn(runner, TextEvent.Topic.CORE, "no case to execute");
        }
        
        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "</switch>");
        return null;
    }
}
