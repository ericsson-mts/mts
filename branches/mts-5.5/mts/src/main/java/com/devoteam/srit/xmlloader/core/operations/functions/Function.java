/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.core.operations.functions;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.Scenario;
import com.devoteam.srit.xmlloader.core.ScenarioRunner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.operations.basic.OperationSequence;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import gp.utils.arrays.Array;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;
import org.dom4j.Element;

/**
 *
 * @author gpasquiers
 */
public class Function {

    private String _name;
    private long _version;
    private OperationSequence _do;
    private Element _root;

    public Function(Element root, Scenario scenario, long version) throws Exception {
        _name = root.attributeValue("name");
        _version = version;
        _root = root;

        // TODO : handle stopCount better (at executions and not parsing ?)
        _do = new OperationSequence(root.element("do"), scenario);
    }

    public long getVersion() {
        return _version;
    }

    public String getName() {
        return _name;
    }

    public HashMap<String, Parameter> execute(Runner runner, HashMap<String, Parameter> inputs) throws Exception {
        HashMap<String, Parameter> outputs = new HashMap();
        try {
            // check args (type, mandatory, create with default value if necessary ...)
            // also extract the parameters from the input hashmap and put them into the pool
            ((ScenarioRunner) runner).stackFunctionParameterPool();

            for (Object object : _root.selectNodes("./input/parameter")) {
                Element element = (Element) object;
                String inputName = element.attributeValue("name");
                String inputDefault = element.attributeValue("default");
                String inputType = element.attributeValue("type");

                if (!Parameter.matchesParameter(inputName)) {
                    throw new ExecutionException("invalid input argument name format in function call\r\n" + element.asXML(), null);
                }

                if(inputName.contains("function:")){
                    inputName = Utils.replaceNoRegex(inputName, "function:", "");
                }

                Parameter inputParameter = inputs.get(inputName);
                if (null == inputParameter && inputDefault != null) {
                    inputParameter = new Parameter();
                    if(!"EMPTY_LIST".equals(inputDefault)){
                        inputParameter.add(inputDefault);
                    }
                }
                else if (null == inputParameter && inputDefault == null) {
                    throw new ExecutionException("mandatory input argument (no default value) not passed from the function call\r\n" + element.asXML(), null);
                }

                // check the type
                checkType(inputParameter, inputType);

                runner.getParameterPool().set(inputName, inputParameter);
            }

            // put the input args them into the pool
            for (Entry<String, Parameter> entry : inputs.entrySet()) {
                runner.getParameterPool().set(entry.getKey(), entry.getValue());
            }

            // execute the function
            _do.execute(runner);

            // extract the parameters from the pool and put them into the output hashmap
            for (Object object : _root.selectNodes("./output/parameter")) {
                Element element = (Element) object;
                String outputName = element.attributeValue("name");
                
                if(outputName.contains("function:")){
                    outputName = Utils.replaceNoRegex(outputName, "function:", "");
                }

                if (!runner.getParameterPool().existsLocal(outputName)) {
                    throw new ExecutionException("invalid ouput arg value (does not exists) in function def\r\n" + element.asXML(), null);
                }

                outputs.put(outputName, runner.getParameterPool().get(outputName));
            }
        }
        finally {
            ((ScenarioRunner) runner).unstackFunctionParameterPool();
        }

        return outputs;
    }

    /* checks (asserts) the type of a parameter
     * throw an exception if it is not the expected type
     */
    private void checkType(Parameter parameter, String type) throws ExecutionException {
        for (Object o : parameter.getArray()) {
            if ("semaphore".equalsIgnoreCase(type)) {
                if (!(o instanceof Semaphore)) {
                    throw new ExecutionException("parameter content is not a semaphore\r\n" + parameter);
                }
            }
            else if ("message".equalsIgnoreCase(type)) {
                if (!(o instanceof Msg)) {
                    throw new ExecutionException("parameter content is not a message\r\n" + parameter);
                }
            }
            else {
                if (!(o instanceof String)) {
                    throw new ExecutionException("parameter content is not a " + type + "\r\n" + parameter);
                }

                String s = (String) o;
                if ("number".equalsIgnoreCase(type)) {
                    try {
                        Double.parseDouble(s);
                    }
                    catch (Exception e) {
                        throw new ExecutionException("parameter content is not a number\r\n" + parameter, e);
                    }
                }
                else if ("boolean".equalsIgnoreCase(type)) {
                    try {
                        Boolean.parseBoolean(s);
                    }
                    catch (Exception e) {
                        throw new ExecutionException("parameter content is not a boolean\r\n" + parameter, e);
                    }
                }
                else if ("binary".equalsIgnoreCase(type)) {
                    try {
                        // TODO: improve; it's simple but expensive as it is
                        Array.fromHexString(s);
                    }
                    catch (Exception e) {
                        throw new ExecutionException("parameter content is not a binary\r\n" + parameter, e);
                    }
                }
            }
        }
    }
}
