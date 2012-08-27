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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author gpasquiers
 */
public class PluggableParameterOperatorListFind extends AbstractPluggableParameterOperator
{
    final private String NAME_FIND = "find";
    final private String NAME_FINDMATCHES = "findMatches";
    final private String NAME_L_FIND = "list.find";
    final private String NAME_L_FINDMATCHES = "list.findMatches";
    
    public PluggableParameterOperatorListFind()
    {
        this.addPluggableName(new PluggableName(NAME_FIND));
        this.addPluggableName(new PluggableName(NAME_FINDMATCHES));
        this.addPluggableName(new PluggableName(NAME_L_FIND));
        this.addPluggableName(new PluggableName(NAME_L_FINDMATCHES));
    }
    
    @Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws ParameterException
    {
//        this.normalizeParameters(operands);
        
        Parameter param1 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
        Parameter param2 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");

        if(1 != param2.length())
        {
            throw new ParameterException("operant value2 of operator Find should be of size 1, not" + param2.length());
        }
        
        Parameter result = new Parameter();

        if (name.equalsIgnoreCase(NAME_FIND) || name.equalsIgnoreCase(NAME_L_FIND))
        {
            int len = param1.length();
            for (int i = 0; i < len; i++)
            {
                if(param1.get(i).equals(param2.get(0)))
                {
                    result.add(String.valueOf(i));
                }
            }
        }
        else if (name.equalsIgnoreCase(NAME_FINDMATCHES) || name.equalsIgnoreCase(NAME_L_FINDMATCHES))
        {
            Pattern p = Utils.compilesRegex(param2.get(0).toString());
            
            int len = param1.length();
            for (int i = 0; i < len; i++)
            {
            	Matcher matcher = p.matcher(param1.get(i).toString());

                if(matcher.matches())
                {
                    result.add(String.valueOf(i));
                }
            }
        }
        else throw new RuntimeException("unsupported operator " + name);
        
        return result;
    }
}
