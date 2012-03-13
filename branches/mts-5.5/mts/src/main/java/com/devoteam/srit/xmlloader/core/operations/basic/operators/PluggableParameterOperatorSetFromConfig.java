/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.core.operations.basic.operators;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;
import com.devoteam.srit.xmlloader.core.utils.Config;
import java.util.Map;

/**
 *
 * @author gpasquiers
 */
public class PluggableParameterOperatorSetFromConfig extends AbstractPluggableParameterOperator
{

    public PluggableParameterOperatorSetFromConfig()
    {
        this.addPluggableName(new PluggableName("setFromConfig", "file.readproperty"));
    }

    @Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws ParameterException
    {
        normalizeParameters(operands);
        Parameter config = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
        Parameter property = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
        Parameter result = new Parameter();
        
        String configName = "";
        String propertyName = "";
        try
        {
            for (int i = 0; i < config.length(); i++)
            {
                configName = config.get(i).toString();
                propertyName = property.get(i).toString();
                result.add(Config.getConfigByName(configName).getString(propertyName));
            }
        }
        catch(Exception e)
        {
            throw new ParameterException("Error in setFromConfig operator reading " + propertyName + " in " + configName, e);
        }

        return result;
    }
}
