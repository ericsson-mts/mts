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
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.Operation;
import com.devoteam.srit.xmlloader.core.operations.functions.Function;
import com.devoteam.srit.xmlloader.core.operations.functions.FunctionsCache;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import java.util.HashMap;
import java.util.LinkedList;
import org.dom4j.Element;

/**
 * OperationPause operation
 * 
 * 
 * @author JM. Auffret
 */
public class OperationCall extends Operation {

    /**
     * Constructor
     * 
     * 
     * @param name Name of the operation
     * @param pause OperationPause value
     */
    public OperationCall(Element root) {
        super(root, null);
    }

    /**
     * Execute operation
     *
     * @param session Current session
     * @return Next operation or null by default
     */
    @Override
    public Operation execute(Runner runner) throws Exception {
        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, this);

        // get the function
        String name = getRootElement().attributeValue("name");
        
        Function function = FunctionsCache.instance().getFunction(name, ((ScenarioRunner)runner).getScenarioReference());

        // prepare input arguments (copy parameters in hashmap)
        HashMap<String, Parameter> inputs  = new HashMap();
        for(Object object:getRootElement().selectNodes("./input/parameter")){
            Element element = (Element) object;
            String inputName = element.attributeValue("name");

            if(inputName.contains("function:")){
                inputName = Utils.replaceNoRegex(inputName, "function:", "");
            }

            String inputValue = element.attributeValue("value");

            if(!Parameter.matchesParameter(inputName)){
                throw new ExecutionException("invalid input name format in function call\r\n" +element.asXML() + "\r\nfrom\r\n" + getRootElement().asXML(), null);
            }

            if(Parameter.matchesParameter(inputValue)){
                LinkedList<String> inputValueParsed = runner.getParameterPool().parse(ParameterPool.unbracket(inputValue));
                if(inputValueParsed.size() != 1){
                    throw new ExecutionException("invalid parameter name size\r\n" +element.asXML() + "\r\nfrom\r\n" + getRootElement().asXML(), null);
                }


                Parameter inputValueParameter = runner.getParameterPool().get(ParameterPool.bracket(inputValueParsed.getFirst()));

                Parameter inputParameter = new Parameter();
                inputParameter.addAll(inputValueParameter.getArray());
                inputs.put(inputName, inputParameter);
            }
            else{
                LinkedList<String> inputValueParsed = runner.getParameterPool().parse(inputValue);
                Parameter inputParameter = new Parameter();
                inputParameter.addAll(inputValueParsed);
                inputs.put(inputName, inputParameter);
            }
        }

        if(null == function){
            throw new ExecutionException("could not find function " + name);
        }


        // execute the function
        HashMap<String, Parameter> outputs = function.execute(runner, inputs);

        // put back the function into the cache
        FunctionsCache.instance().freeFunction(function);

        // copy back output parameters to parameter pool
        for(Object object:getRootElement().selectNodes("./output/parameter")){
            Element element = (Element) object;
            String outputName = element.attributeValue("name");
            String outputValue = element.attributeValue("value");

            if(null != outputName && outputName.contains("function:")){
                outputName = Utils.replaceNoRegex(outputName, "function:", "");
            }

            if(null != outputValue && outputValue.contains("function:")){
                outputValue = Utils.replaceNoRegex(outputValue, "function:", "");
            }

            if(!Parameter.matchesParameter(outputName)){
                throw new ExecutionException("invalid output name format in function call\r\n" + element.asXML() + "\r\nfrom\r\n" + getRootElement().asXML(), null);
            }

            if(null != outputValue && !Parameter.matchesParameter(outputValue)){
                throw new ExecutionException("invalid output value format in function call\r\n" + element.asXML() + "\r\nfrom\r\n" + getRootElement().asXML(), null);
            }

            if(null == outputValue){
                if(!outputs.containsKey(outputName)){
                    throw new ExecutionException("invalid output name; not present in function output\r\n" + element.asXML() + "\r\nfrom\r\n" + getRootElement().asXML(), null);
                }

                runner.getParameterPool().set(outputName, outputs.get(outputName));
                GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "setted output ", outputValue," as ", outputName," in parameter pool");
            }
            else{
                if(!outputs.containsKey(outputValue)){
                    throw new ExecutionException("invalid output value; not present in function output\r\n" + element.asXML() + "\r\nfrom\r\n" + getRootElement().asXML(), null);
                }

                runner.getParameterPool().set(outputName, outputs.get(outputValue));
                GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "setted output ", outputName," as ", outputName," in parameter pool");
            }
            
        }

        // some logs
        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "<function> executed " + name);
        return null;
    }
}
