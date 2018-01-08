/* 
 * Copyright 2017 Orange http://www.orange.com
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
import com.devoteam.srit.xmlloader.core.groovy.TestParameter;
import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.ScenarioRunner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.exception.ParameterException
import com.devoteam.srit.xmlloader.core.log.GenericLogger
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.Operation;
import com.devoteam.srit.xmlloader.core.operations.functions.Function;
import com.devoteam.srit.xmlloader.core.operations.functions.FunctionsCache;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableParameterOperator;
import com.devoteam.srit.xmlloader.core.pluggable.ParameterOperatorRegistry;
import com.devoteam.srit.xmlloader.core.utils.URIRegistry;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List

import org.dom4j.Attribute

import groovy.io.FileType;
import groovy.lang.Script

/**
 * base class of the groovy classes used in MTS.
 * MTSScript manages the exchange of data between the groovy class 
 * and the MTS Runner
 * This base class can also be used to import common classes, to add common fields and methods,  
 * to execute any action before/after the groovy classes run method
 *
 * @author mickael.jezequel@orange.com
 *
 */
abstract class MTSScript extends Script {

    /**
     * this method is called when the groovy script is executed
     */
    def run(){
    }

    /**
     * this method add the editable attribute to the passed parameter
     * @param parameter
     * @return
     */
    def editable(parameter) {
        return new TestParameter(parameter,true);
    }
	
    /**
     * This method may be used to declare a MTS parameter in the classic way
     * note that editable, state and description are ignored
     * @see OperationParameter.execute
     * 
     * @param map
     * @return
     */
    def parameter(map) {
        String operation=map["operation"]
        String resultant=map["name"]
        //@TODO manage editable, state and description
        HashMap<String, Parameter> operands = new HashMap<String, Parameter>();

        map.each{attributeName,attributeValue->
            if (attributeName.equals("operation")
                || attributeName.equals("state")
                || attributeName.equals("name")
                || attributeName.equals("editable")
                || attributeName.equals("description"))
            return;


            /*
             * The first case gets a parameter or a parameter item while trying to preserve the type of the object saved into the parameter.
             */
            if (Parameter.matchesParameter(attributeValue)) {
                attributeValue = ParameterPool.unbracket(attributeValue);
                List<String> aRes = binding.runner.getParameterPool().parse(attributeValue);
                if (aRes.size() != 1) {
                    throw new ParameterException("error parsing a variable name or index in operands (" + attributeValue + "), final size is not 1");
                }
                attributeValue = aRes.get(0);
                attributeValue = ParameterPool.bracket(attributeValue);

                String myParameterName = ParameterPool.getName(attributeValue);
                int myParameterIndex = -1;

                if (ParameterPool.hasIndex(attributeValue)) {
                    myParameterIndex = ParameterPool.getIndex(attributeValue);
                }

                Parameter parameter;

                if (binding.runner.getParameterPool().exists(myParameterName)) {
                    parameter = binding.runner.getParameterPool().get(myParameterName);
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
                LinkedList<String> parsedValue = binding.runner.getParameterPool().parse(attributeValue);
                Parameter parameter = new Parameter();
                for (String value : parsedValue) {
                    parameter.add(value);
                }
                operands.put(attributeName, parameter);
            }
        }

        PluggableParameterOperator parameterOperator = ParameterOperatorRegistry.getPluggableComponent(operation);
        parameterOperator.operate(binding.runner, operands, operation, resultant);

    }
	
    /**
     * declare unknown properties in MTS ParameterPool
     * @param name
     * @param args
     * @return
     */
    def propertyMissing(String name, args) {
        //println "MTSBinder.propertyMissing($name)"
        if (args.length==0) {
            println "MTSBinder.methodMissing(name=${name})"
        } else if (args[0] instanceof Map) {
            boolean opt= args[0].find{ it.key=="optional" }? args[0].find{ it.key=="optional" }.value:false
            println "MTSBinder.methodMissing(name=${name},value=${args[0].find{it.key=="value"}.value},opt=${opt}) from Map"
            this.metaClass."$name"= args[0].find{ it.key=="value" }.value
            setMTSParam(name,args[0].find{ it.key=="value" }.value)
        } else {
            def opt=args.length>1 && args[1] instanceof Boolean && args[1]

            println "MTSBinder.methodMissing(name=${name},value=${args[0]},opt=${opt})"
            this.metaClass."$name"=args[0]
            setMTSParam(name,args[0])
        }
    }

    /**
     * retrieve parameter from ParameterPool
     * @param name
     * @return
     */
    def getMTSParam(String name) {
        //println "MTSBinder.getMTSParam($name)"
        def param= parseParameter(name)
        return param.length()>0?param.get(0):null;
    }
	
    def getMTSParam(String name, int i) {
        //println "MTSBinder.getMTSParam($name,$i)"
        def param= parseParameter(name)
        return param.length()>i?param.get(i):null;
    }
	
    def getMTSParamList(String name) {
        //println "MTSBinder.getMTSParamList($name)"
        def param= parseParameter(name)
        ArrayList result= new ArrayList();
        result.addAll(param.getArray());
        return result;
    }
	
    def getMTSParamListAsString(String name) {
        //println "MTSBinder.getMTSParamListAsString($name)"
        def param= parseParameter(name)
        ArrayList result= new ArrayList();
        result.addAll(param.getArray().toString());
        return result;
    }
	
    def getFromMessage(param,headerName) {
        def message=getMTSParamList(param)
        if (message instanceof java.util.ArrayList) {
            if (message[0].getParameter(headerName).length()==1) {
                return message[0].getParameter(headerName).get(0)
            }
            def result= []
            for (int i=0; i<message[0].getParameter(headerName).length(); i++) {
                result.add(message[0].getParameter(headerName).get(i))
            }
            return result
        }
    }
  
    /**
     * set a variable into MTS ParameterPool
     *
     */
    protected void setMTSParam(String name, Object value) {
        try {
            //println "MTSBinder.setMTSParam($name)"
            Parameter groovyParameter
            if (value != null && value instanceof List) {
                value.each{groovyParameter= parseParameter(it)}
            } else {
                groovyParameter= parseParameter(value);
            }
            binding.runner.getParameterPool().set(ParameterPool.bracket(name), groovyParameter);
        } catch (ParameterException e) {
            e;
            e.printStackTrace();
        }
    }
	
    /**
     * expand parameter in brackets such as [[foo]bar] by replacing the foo param by its value
     * @see OperationParameter.execute
     * 
     * @param attributeValue
     * @return
     */
    protected Parameter parseParameter (attrVal) {
        String attributeValue;
        if (!attrVal instanceof String) {
            attributeValue=attrVal.toString()
        } else {
            attributeValue=attrVal;
        }
        if (Parameter.matchesParameter(attributeValue)) {
            attributeValue = ParameterPool.unbracket(attributeValue);
            List<String> aRes = binding.runner.getParameterPool().parse(attributeValue);
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

            if (binding.runner.getParameterPool().exists(myParameterName)) {
                parameter = binding.runner.getParameterPool().get(myParameterName);
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
            return parameter;
        }
        else {
            LinkedList<String> parsedValue = binding.runner.getParameterPool().parse(attributeValue);
            Parameter parameter = new Parameter();
            for (String value : parsedValue) {
                parameter.add(value);
            }
            return parameter;
        }
    }
	
    /**
     * utility method for logging user info messages in session logger
     */
    def info = {message->
        GlobalLogger.instance().getSessionLogger().info(binding.runner, TextEvent.Topic.USER, ""+message);
    }
    /**
     * utility method for logging user error messages in session logger
     */
    def error = {message->
        GlobalLogger.instance().getSessionLogger().error(binding.runner, TextEvent.Topic.USER, ""+message);
    }
    /**
     * utility method for logging user warn messages in session logger
     */
    def warn = {message->
        GlobalLogger.instance().getSessionLogger().warn(binding.runner, TextEvent.Topic.USER, ""+message);
    }
    /**
     * utility method for logging user debug messages in session logger
     */
    def debug = {message->
        GlobalLogger.instance().getSessionLogger().debug(binding.runner, TextEvent.Topic.USER, ""+message);
    }
	
    /**
     * utility method for logging user messages in session or application logger
     * @see OperationLogger
     */
    def log(map) {
        if (map instanceof Map) {
            String level=map["level"]!=null?map["level"]:"INFO"
            int intLevel = -1;
            if (Utils.isInteger(level)) {
                intLevel = 3 - Integer.parseInt(level);
            }
            String type=map["type"]!=null?map["type"]:"Scenario"
            String logStr=map["message"]
            if ("Main".equalsIgnoreCase(type)) {
                if ((TextEvent.DEBUG_STRING.equalsIgnoreCase(level)) || (intLevel == TextEvent.DEBUG)) {
                    GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.USER, logStr);
                }
                else if ((TextEvent.INFO_STRING.equalsIgnoreCase(level)) || (intLevel == TextEvent.INFO)) {
                    GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.USER, logStr);
                }
                else if ((TextEvent.WARN_STRING.equalsIgnoreCase(level)) || (intLevel == TextEvent.WARN)) {
                    GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.USER, logStr);
                }
                else if ((TextEvent.ERROR_STRING.equalsIgnoreCase(level)) || (intLevel == TextEvent.ERROR)) {
                    GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.USER, logStr);
                }
                else {
                    throw new ExecutionException("Level attribute should be an integer from [0-3] or a string from the list {DEBUG, INFO, WARN, ERROR}");
                }
            }
            else if ("Scenario".equalsIgnoreCase(type)) {
                if ((TextEvent.DEBUG_STRING.equalsIgnoreCase(level)) || (intLevel == TextEvent.DEBUG)) {
                    GlobalLogger.instance().getSessionLogger().debug(binding.runner, TextEvent.Topic.USER, logStr);
                }
                else if ((TextEvent.INFO_STRING.equalsIgnoreCase(level)) || (intLevel == TextEvent.INFO)) {
                    GlobalLogger.instance().getSessionLogger().info(binding.runner, TextEvent.Topic.USER, logStr);
                }
                else if ((TextEvent.WARN_STRING.equalsIgnoreCase(level)) || (intLevel == TextEvent.WARN)) {
                    GlobalLogger.instance().getSessionLogger().warn(binding.runner, TextEvent.Topic.USER, logStr);
                }
                else if ((TextEvent.ERROR_STRING.equalsIgnoreCase(level)) || (intLevel == TextEvent.ERROR)) {
                    GlobalLogger.instance().getSessionLogger().error(binding.runner, TextEvent.Topic.USER, logStr);
                }
                else {
                    throw new ExecutionException("Level attribute should be an integer from [0-3] or a string from the list {DEBUG, INFO, WARN, ERROR}");
                }
            }
            else {
                throw new ExecutionException("Type attribute should be a string from the list {Main, Scenario}");
            }
	
        } else if (map !=null) {
            GlobalLogger.instance().getSessionLogger().info(binding.runner, TextEvent.Topic.USER, ""+map);
        }
    }
}
