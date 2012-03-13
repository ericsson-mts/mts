/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.operations.basic.operators;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;
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
                Pattern p = Pattern.compile(paramRegex.get(i).toString());
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
