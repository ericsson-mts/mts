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
