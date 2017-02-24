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
import com.devoteam.srit.xmlloader.core.utils.Utils;
import java.util.Map;

/**
 *
 * @author gpasquiers
 */
public class PluggableParameterOperatorNumberUtils extends AbstractPluggableParameterOperator
{
    final private String NAME_ROUND = "math.round";
    final private String NAME_FLOOR = "math.floor";

    final private String NAME_N_ROUND = "number.round";
    final private String NAME_N_FLOOR = "number.floor";
    final private String NAME_N_RAND = "number.random";
    final private String NAME_N_UID = "number.uid";

    public PluggableParameterOperatorNumberUtils()
    {
        this.addPluggableName(new PluggableName(NAME_ROUND, NAME_N_ROUND));
        this.addPluggableName(new PluggableName(NAME_FLOOR, NAME_N_FLOOR));
        this.addPluggableName(new PluggableName(NAME_N_ROUND));
        this.addPluggableName(new PluggableName(NAME_N_FLOOR));
        this.addPluggableName(new PluggableName(NAME_N_RAND));
        this.addPluggableName(new PluggableName(NAME_N_UID));
    }
    
    @Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws ParameterException
    {
        normalizeParameters(operands);
        
        Parameter result = new Parameter();
        if(name.equalsIgnoreCase(NAME_N_UID))
        {
            String res = String.valueOf(PluggableParameterOperatorIdentifier.nextInteger()); // generates the next number.
            result.add(res);
            return result;
        }

        
        Parameter param1 = assertAndGetParameter(operands, "value");
        
        for(int i=0; i<param1.length(); i++)
        {
            double op1 = Double.parseDouble(param1.get(i).toString());
                        
            if(name.equalsIgnoreCase(NAME_ROUND) || name.equalsIgnoreCase(NAME_N_ROUND))       result.add(Math.round(op1));
            else if(name.equalsIgnoreCase(NAME_FLOOR) || name.equalsIgnoreCase(NAME_N_FLOOR))  result.add((long) Math.floor(op1));
            else if(name.equalsIgnoreCase(NAME_N_RAND))
            {
            	double op2 = 0;            
            	if(operands.containsKey("value2"))
                {
            		Parameter param2 = assertAndGetParameter(operands, "value2");
                	op2 = Double.parseDouble(param2.get(i).toString());
                }

                long number = (long) Math.floor(Math.random()*(op1 - op2) + op2); // generates a new Integer between 0 and op1
                String res = String.valueOf(number);
                int length = param1.get(i).toString().length();

                result.add(Utils.padInteger(res, length));
            }
            else throw new RuntimeException("unsupported operation " + name);
        }
        
        return result;
    }

}
