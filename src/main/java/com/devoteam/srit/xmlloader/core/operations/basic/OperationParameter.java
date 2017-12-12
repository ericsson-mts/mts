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
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.Operation;

import com.devoteam.srit.xmlloader.core.pluggable.PluggableParameterOperator;
import com.devoteam.srit.xmlloader.core.pluggable.ParameterOperatorRegistry;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;
import com.devoteam.srit.xmlloader.core.utils.XMLElementTextOnlyParser;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 *
 * @author gpasquiers
 */
public class OperationParameter extends Operation {

    private PluggableParameterOperator parameterOperator;
    private PluggableName parameterOperatorName;
    private String resultantAttribute;
    private String operatorAttribute;

    /**
     * Creates a new instance of OperationParameter
     */
    public OperationParameter(Element root) throws Exception {
        super(root, XMLElementTextOnlyParser.instance());

        this.resultantAttribute = root.attributeValue("name").trim();

        this.operatorAttribute = root.attributeValue("operation");
        if (null == this.operatorAttribute) {
            root.addAttribute("operation", "list.set");
            this.operatorAttribute = root.attributeValue("operation");
        }
        this.operatorAttribute = this.operatorAttribute.toLowerCase().trim();
        this._key[1] = this.operatorAttribute;

        this.parameterOperator = ParameterOperatorRegistry.getPluggableComponent(this.operatorAttribute);
        this.parameterOperatorName = ParameterOperatorRegistry.getPluggableName(this.operatorAttribute);

        if (null == this.parameterOperator) {
            throw new ParsingException("Could not find any <parameter> operation named " + this.operatorAttribute);
        }
    }

    /**
     * Executes the operation
     */
    @Override
    public Operation execute(Runner runner) throws Exception {
        try {
            if (runner instanceof ScenarioRunner) {
                GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.PARAM, this);
            }
            else {
                GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PARAM, this);
            }

            if (this.parameterOperatorName.isDeprecated()) {
                GlobalLogger.instance().logDeprecatedMessage("parameter ... operation=\"" + this.parameterOperatorName.getName() + "\" .../",
                        "parameter ... operation=\"" + this.parameterOperatorName.deprecatedBy() + "\" .../");
            }

            String text = "";
            if (null != this.getRootElement().getText() && this.getRootElement().getText().trim().length() != 0) {
                // Replace elements in XMLTree
                try {
                    lockAndReplace(runner);
                    GlobalLogger.instance().getSessionLogger().debug(runner, TextEvent.Topic.CORE, "Operation after pre-parsing \n", this);
                    text = this.getRootElement().getText();
                }
                finally {
                    unlockAndRestore();
                }
            }

            /*
             * Parse the resultant parameter in case it is in format: "[myParam([indexParam])]" to get it in format: "[myParam(10)]
             */
            String resultant = this.resultantAttribute;
            resultant = ParameterPool.unbracket(resultant);
            List<String> res = runner.getParameterPool().parse(resultant);
            if (res.size() != 1) {
                throw new ParameterException("error parsing resultant, final size is not 1");
            }
            resultant = res.get(0);
            resultant = ParameterPool.bracket(resultant);

            /*
             * Extract the namen level and index (if there is one) of the resultant param - resultantName - resultantIndex (-1 if no index present, will override parameter) - resultantLevel (null if
             * no level defined, will be current level)
             */
            String resultantName = ParameterPool.getName(resultant);
            int resultantIndex = -1;
            if (ParameterPool.hasIndex(resultant)) {
                resultantIndex = ParameterPool.getIndex(resultant);
            }


            /*
             * Populate the HashMap of operands we will give to the ParameterOperator
             */
            HashMap<String, Parameter> operands = new HashMap<String, Parameter>();

            for (Object object : this.getRootElement().attributes()) {
                Attribute attribute = (Attribute) object;
                String attributeName = attribute.getName().toLowerCase();

                if (attributeName.equals("operation")
                        || attributeName.equals("state")
                        || attributeName.equals("name")
                        || attributeName.equals("editable")
                        || attributeName.equals("description")) {
                    continue;
                }

                String attributeValue = attribute.getValue();

                /*
                 * The first case gets a parameter or a parameter item while trying to preserve the type of the object saved into the parameter.
                 */
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

            /*
             * If there is no "value" operand, try to get it from the tag "text()"
             */
            if (null == operands.get("value")) {
                Parameter parameter = new Parameter();
                if (text.length() > 0)
                {
                	parameter.add(text);
                	operands.put("value", parameter);
                }
            }

            /*
             * Execute the Parameter operator
             */
            Parameter result = this.parameterOperator.operate(runner, operands, this.operatorAttribute, resultant);

            if (null == result) {
                return null;
            }


            /*
             * Go write the result
             */
            if (-1 == resultantIndex) {
                runner.getParameterPool().set(resultantName, result);
            }
            else {
                if (1 != result.length()) {
                    throw new ParameterException("we should write the result in one cell, but the size of the result is greater than 1 (!)");
                }

                Parameter param;

                if (runner.getParameterPool().exists(resultantName)) {
                    param = runner.getParameterPool().get(resultantName);
                }
                else {
                	param = new Parameter();
                    runner.getParameterPool().set(resultantName, param);
                }

                param.set(resultantIndex, result.get(0));
            }

            return null;
        }
        catch (Exception e) {
            throw new ParameterException("Error in parameter operation : " + getRootElement().asXML(), e);
        }
    }
}
