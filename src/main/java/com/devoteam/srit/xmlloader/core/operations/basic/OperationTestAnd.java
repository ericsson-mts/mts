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
import com.devoteam.srit.xmlloader.core.exception.AssertException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.Operation;

import java.util.ArrayList;
import org.dom4j.Element;

/**
 * enables your scenario to execute operations according to value of a boolean variable named condition
 *
 * @author ma007141
 *
 */
public class OperationTestAnd extends Operation {

    private ArrayList<Operation> operations;
    private Scenario scenario;

    /**
     * Constructor
     *
     * @param operationsTests list of tests
     */
    public OperationTestAnd(Element root, Scenario scenario) throws Exception {
        super(root, null);
        this.scenario = scenario;
        this.operations = new ArrayList();
        for (Object object : root.elements()) {
            Element element = (Element) object;
            this.operations.add(scenario.parseOperation(element));
        }
    }

    /**
     * Execute operation
     *
     *
     * @param runner Current runner
     * @return Next operation or null by default
     * @throws ExecutionException
     */
    public Operation execute(Runner runner) throws Exception {

        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, this);

        // Replace elements in XMLTree
        // No attribute to replace on <if> operation
        // replace(runner, new XMLElementDefaultParser(runner.getParameterPool()), TextEvent.Topic.CORE);        

        for (Operation operation : this.operations) {
            try {
                operation.executeAndStat(runner);
            }
            catch (AssertException e) {
                GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "</test> (KO)\n", e.getMessage());
                throw new AssertException("<and> (KO) (condition failed)");
            }
        }

        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "</and> (OK)");
        return null;
    }
}
