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
            Pattern p = Pattern.compile(param2.get(0).toString());
            
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
