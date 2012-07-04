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
import com.devoteam.srit.xmlloader.core.ScenarioRunner;
import com.devoteam.srit.xmlloader.core.exception.GotoExecutionException;
import com.devoteam.srit.xmlloader.core.operations.Operation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.dom4j.Element;

/**
 * This operation handles a sequence of operations. It isnot a "true" operation.
 * That way it will not use a replacer, nor output logs.
 * @author gpasquiers
 */
public class OperationSequence extends Operation {

    private Scenario scenario;
    private ArrayList<Operation> operations;

    public OperationSequence(Element root, Scenario scenario) throws Exception {
        super(root, null);
        this.scenario = scenario;
        this.operations = new ArrayList<Operation>();
        HashSet<String> labelNames = new HashSet();
        if (null != root) {
            for (Element element : (List<Element>) root.elements()) {
                Operation operation = this.scenario.parseOperation(element);

                // check labels names unicity
                if (operation instanceof OperationLabel) {
                    // only check unicity for labels that do not contain parameters in their names
                    if (!Parameter.containsParameter(operation.getRootElement().attributeValue("name"))) {
                        if (labelNames.contains(operation.getRootElement().attributeValue("name"))) {
                            throw new Exception("Duplicated label name in operations sequence:\n" + this);
                        }
                        labelNames.add(operation.getRootElement().attributeValue("name"));
                    }
                }

                this.operations.add(operation);
            }
        }
    }

    /**
     * Execute operation
     * 
     * @param runner Current runner
     * @return Next operation or null by default
     * @throws Exception
     */
    @Override
    public Operation execute(Runner runner) throws Exception {
        ScenarioRunner scenarioRunner = (ScenarioRunner) runner;
        int index = 0;
        int size = this.operations.size();
        while (index < size) {
            scenarioRunner.assertIsNotInterrupting();

            try {
                this.operations.get(index).executeAndStat(runner);
                index++;
            }
            catch (GotoExecutionException e) {
                index = -1;
                String labelName = e.getLabel();
                for (int i = 0; i < size; i++) {
                    if ((operations.get(i) instanceof OperationLabel) && ((OperationLabel) operations.get(i)).getLabelName(runner).equals(labelName)) {
                        index = i;
                    }
                }

                if (-1 == index) {
                    throw e;
                }
            }
        }
        return null;
    }
}
