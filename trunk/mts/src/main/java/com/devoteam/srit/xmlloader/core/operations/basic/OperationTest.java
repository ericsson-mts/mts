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
import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.ScenarioRunner;
import com.devoteam.srit.xmlloader.core.exception.AssertException;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.Operation;
import com.devoteam.srit.xmlloader.core.pluggable.ParameterTestRegistry;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableParameterTest;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 *
 * @author gpasquiers
 */
public class OperationTest extends Operation {

    private PluggableParameterTest parameterTest;
    private PluggableName parameterTestName;
    private String testAttribute;
    private String strNot;

    /**
     * Creates a new instance of OperationParameter
     */
    public OperationTest(Element root) throws Exception {
        super(root, null);

        this.testAttribute = root.attributeValue("condition").toLowerCase();
        this._key[1] = this.testAttribute;
        this.strNot = root.attributeValue("not");
        this.parameterTest = ParameterTestRegistry.getPluggableComponent(this.testAttribute);
        this.parameterTestName = ParameterTestRegistry.getPluggableName(this.testAttribute);

        if (null == this.parameterTest) {
            throw new ParsingException("Could not find any <test> condition named " + this.testAttribute);
        }
    }

    /**
     * Executes the operation
     */
    public Operation execute(Runner runner) throws Exception {
        if (runner instanceof ScenarioRunner) {
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.PARAM, this);
        }
        else {
            GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PARAM, this);
        }

        if (this.parameterTestName.isDeprecated()) {
            GlobalLogger.instance().logDeprecatedMessage(
                    "test ... condition=\""
                    + this.parameterTestName.getName()
                    + "\" .../",
                    "test ... condition=\""
                    + this.parameterTestName.deprecatedBy()
                    + "\" .../");
        }

        /*
         * Replace parameters on the "not" attribute because there is not replacer for this operation
         */
        boolean not = false;
        if (strNot != null) {
            List<String> aRes = runner.getParameterPool().parse(strNot);
            if (aRes.size() != 1) {
                throw new ParameterException("error parsing a variable name or index in operands (" + strNot + "), final size is not 1");
            }
            strNot = aRes.get(0);
            not = Boolean.parseBoolean(strNot.toLowerCase());
        }

        /*
         * Parse the tested parameter parameter in case it is in format: "[myParam([indexParam])]" to get it in format: "[myParam(10)]
         */
        String parameterName = this.getRootElement().attributeValue("parameter");
        parameterName = ParameterPool.unbracket(parameterName);
        List<String> res = runner.getParameterPool().parse(parameterName);
        if (res.size() != 1) {
            throw new ParameterException("error parsing resultant, final size is not 1");
        }
        parameterName = res.get(0);
        parameterName = ParameterPool.bracket(parameterName);

        /*
         * Populate the HashMap of operands we will give to the ParameterOperator
         */
        HashMap<String, Parameter> operands = new HashMap<String, Parameter>();

        for (Object object : this.getRootElement().attributes()) {
            Attribute attribute = (Attribute) object;
            String attributeName = attribute.getName().toLowerCase();

            if (attributeName.equals("condition")
                    || attributeName.equals("state")
                    || attributeName.equals("description")) {
                continue;
            }

            String attributeValue = attribute.getValue();

            if (Parameter.matchesParameter(attributeValue)) {
                attributeValue = ParameterPool.unbracket(attributeValue);
                List<String> aRes = runner.getParameterPool().parse(attributeValue);
                if (aRes.size() != 1) {
                    throw new ParameterException("error parsing a variable name or index in operands (" + attribute.getValue() + "), final size is not 1");
                }
                attributeValue = aRes.get(0);
                attributeValue = ParameterPool.bracket(attributeValue);

                String myParameterName = ParameterPool.getName(attributeValue);
                int myParameterIndex = -1;

                if (ParameterPool.hasIndex(attributeValue)) {
                    myParameterIndex = ParameterPool.getIndex(attributeValue);
                }

                Parameter parameter;

                if (runner.getParameterPool().exists(myParameterName)) {
                    parameter = runner.getParameterPool().get(myParameterName);
                    if (-1 != myParameterIndex) {
                        Object myObject = parameter.get(myParameterIndex);
                        parameter = new Parameter();
                        parameter.add(myObject);
                    }
                }
                else {
                    parameter = new Parameter();
                    parameter.add(attributeValue);
                }
                operands.put(attributeName, parameter);
            }
            else {
                LinkedList<String> parsedValue = runner.getParameterPool().parse(attributeValue);
                Parameter parameter = new Parameter();
                for (String value : parsedValue) {
                    parameter.add(value);
                }
                operands.put(attributeName, parameter);
            }
        }

        boolean succeeded;
        try {
            /*
             * Execute the Parameter operator
             */
            this.parameterTest.test(runner, operands, testAttribute, parameterName);
            succeeded = true;
        }
        catch (AssertException e) {
            succeeded = false;
            if (!not) {
                throw e;
            }
        }

        if (succeeded && not) {
            throw new AssertException("expected a test failure (not=true)");
        }

        return null;
    }
}
