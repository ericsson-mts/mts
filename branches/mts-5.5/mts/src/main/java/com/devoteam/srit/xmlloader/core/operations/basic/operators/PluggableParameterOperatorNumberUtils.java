/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
                long number = (long) Math.floor(Math.random()*op1); // generates a new Integer between 0 and op1
                String res = String.valueOf(number);
                int length = param1.get(i).toString().length();

                result.add(Utils.padInteger(res, length));
            }
            else throw new RuntimeException("unsupported operation " + name);
        }
        
        return result;
    }

}
