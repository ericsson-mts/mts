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
public class PluggableParameterOperatorStringRegex extends AbstractPluggableParameterOperator
{
    final private String NAME_REGEXREPLACES = "regexReplaces";
    final private String NAME_REGEXMATCHES = "regexMatches";
    final private String NAME_S_REGEXREPLACES = "string.regexReplaces";
    final private String NAME_S_REGEXMATCHES = "string.regexMatches";
    
    public PluggableParameterOperatorStringRegex()
    {
        this.addPluggableName(new PluggableName(NAME_REGEXREPLACES, NAME_S_REGEXREPLACES));
        this.addPluggableName(new PluggableName(NAME_REGEXMATCHES, NAME_S_REGEXMATCHES));
        this.addPluggableName(new PluggableName(NAME_S_REGEXREPLACES));
        this.addPluggableName(new PluggableName(NAME_S_REGEXMATCHES));
    }
    
    @Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws ParameterException
    {
        normalizeParameters(operands);
        Parameter paramData = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
        Parameter paramRegex = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
        Parameter result = new Parameter();
        
        if(name.equalsIgnoreCase(NAME_REGEXREPLACES) || name.equalsIgnoreCase(NAME_S_REGEXREPLACES))
        {
            Parameter paramReplace = PluggableParameterOperatorList.assertAndGetParameter(operands, "value3");
            for(int i=0; i<paramData.length(); i++)
            {
                result.add(paramData.get(i).toString().replaceAll(paramRegex.get(i).toString(), paramReplace.get(i).toString()));
            }
        }
        else if(name.equalsIgnoreCase(NAME_REGEXMATCHES) || name.equalsIgnoreCase(NAME_S_REGEXMATCHES))
        {
            for (int i = 0; i < paramData.length(); i++)
            {
                Pattern p = Utils.compilesRegex(paramRegex.get(i).toString());
                Matcher m = p.matcher(paramData.get(i).toString());
                
                while (m.find())
                {
                    if ((m.group() != null) && (m.group().length() > 0))
                    {
                        result.add(m.group());
                    }
                }
            }
        }
        else throw new RuntimeException("unsupported operator " + name);

        return result;
    }
}
