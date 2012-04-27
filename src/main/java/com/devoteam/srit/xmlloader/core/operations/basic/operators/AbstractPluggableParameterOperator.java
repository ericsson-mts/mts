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

import com.devoteam.srit.xmlloader.core.pluggable.PluggableParameterOperator;
import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.pluggable.AbstractPluggableComponent;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author gpasquiers
 */
abstract public class AbstractPluggableParameterOperator extends AbstractPluggableComponent implements PluggableParameterOperator
{
    public AbstractPluggableParameterOperator()
    {
        super();
    }
    
    static public void normalizeParameters(Map<String, Parameter> parameters) throws ParameterException
    {
        int finalSize = -1;
        boolean sizeException = false;
        for(Entry<String, Parameter> entry:parameters.entrySet())
        {
            int size = entry.getValue().length();
            if(-1 == finalSize) finalSize = size;
            else if(size == 1) continue;
            else if(size != finalSize)
            {
                if(1 == finalSize)
                {
                    finalSize = size;
                }
                else
                {
                    sizeException = true;
                    break;
                }
            }
        }

        if(sizeException)
        {
            String message = "invalid size of operands : ";
            
            for(Entry<String, Parameter> entry:parameters.entrySet())
            {
                message += entry.getKey() + ":" + entry.getValue().length() + " ";
            }
            throw new ParameterException(message);
        }
        
        for(Entry<String, Parameter> entry:parameters.entrySet())
        {
            Parameter parameter = entry.getValue();
            
            if(0 == finalSize)
            {
                entry.setValue(new Parameter());
            }
            else if(parameter.length() == 1)
            {
                // here, the parameter size should be either finalSize, either 0
                Parameter myParameter = new Parameter();
                entry.setValue(myParameter);
                while(myParameter.length() < finalSize) myParameter.add(parameter.get(0));
            }
        }
    }
        
    static public Parameter assertAndGetParameter(Map<String, Parameter> parameters, String name) throws ParameterException
    {
        return assertAndGetParameter(parameters, name, null);
    }

    static public Parameter assertAndGetParameter(Map<String, Parameter> parameters, String name, String otherName) throws ParameterException
    {
        Parameter parameter;
        parameter = parameters.get(name);
        if(null == parameter && null != otherName) parameter = parameters.get(otherName);
        if(null == parameter) throw new ParameterException("parameter with operand name " + name + " is not present");
        return parameter;
    }

    static public Parameter getParameter(Map<String, Parameter> parameters, String name) throws ParameterException {
        return parameters.get(name);
    }
    
    abstract public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws Exception;
    
    protected String formatDouble(Double res)
    {
        if(res.doubleValue() == res.longValue()) return String.valueOf(res.longValue());
        else {
        	DecimalFormat plop = new DecimalFormat("0.000000");
        	plop.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));
        	return plop.format(res.doubleValue());
        }
    }

}
