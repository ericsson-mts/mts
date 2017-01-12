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
import java.util.Map;

/**
 *
 * @author gpasquiers
 */
public class PluggableParameterOperatorListPool extends AbstractPluggableParameterOperator
{
    final private String NAME_SET = "set"; 
    final private String NAME_UNSET = "unset"; 
    final private String NAME_CREATE = "create"; 
    
    final private String NAME_L_SET    = "list.set";
    final private String NAME_L_DELETE = "list.remove";
    final private String NAME_L_CREATE = "list.create";

    public PluggableParameterOperatorListPool()
    {
        this.addPluggableName(new PluggableName(NAME_SET));
        this.addPluggableName(new PluggableName(NAME_UNSET, NAME_L_DELETE));
        this.addPluggableName(new PluggableName(NAME_CREATE, NAME_L_CREATE));

        this.addPluggableName(new PluggableName(NAME_L_SET));
        this.addPluggableName(new PluggableName(NAME_L_DELETE));
        this.addPluggableName(new PluggableName(NAME_L_CREATE));
    }
    
    @Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws ParameterException
    {
        if(name.equals(NAME_SET) || name.equals(NAME_L_SET))
        {
            PluggableParameterOperatorList.normalizeParameters(operands);
            Parameter value = operands.get("value");
            Parameter number = operands.get("value2");
            if (value == null)
            {
            	return new Parameter();
            }
            if (null == number)
            {
                return value;
            }
            Parameter result = new Parameter();
            for(int i=0; i<value.length(); i++)
            {
                int iNumber = Integer.valueOf(number.get(i).toString());
                String sValue = "";
                for(int j=0; j<iNumber; j++)
                {
                    sValue += value.get(i).toString();
                }
                result.add(sValue);
            }
            return result;
        }
        else if(name.equals(NAME_UNSET) || name.equals(NAME_L_DELETE))
        {
            runner.getParameterPool().delete(resultant);
            return null;
        }
        else if(name.equals(NAME_CREATE) || name.equals(NAME_L_CREATE))
        {
            return runner.getParameterPool().create(resultant);
        }
        else throw new RuntimeException("unsupported operation");
    }

}
