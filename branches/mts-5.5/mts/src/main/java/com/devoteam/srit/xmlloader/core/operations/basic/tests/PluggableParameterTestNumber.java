/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.core.operations.basic.tests;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.AssertException;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.operations.basic.operators.AbstractPluggableParameterOperator;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;
import java.util.Map;

/**
 *
 * @author gpasquiers
 */
public class PluggableParameterTestNumber extends AbstractPluggableParameterTest
{

    final private String NAME_LET = "lowerEqualThan";
    final private String NAME_LT = "lowerThan";
    final private String NAME_GET = "greaterEqualThan";
    final private String NAME_GT = "greaterThan";

    final private String NAME_N_LET = "number.lowerEqualThan";
    final private String NAME_N_LT = "number.lowerThan";
    final private String NAME_N_GET = "number.greaterEqualThan";
    final private String NAME_N_GT = "number.greaterThan";
    final private String NAME_N_EQ = "number.equals";

    public PluggableParameterTestNumber()
    {
        this.addPluggableName(new PluggableName(NAME_LET,NAME_N_LET));
        this.addPluggableName(new PluggableName(NAME_LT,NAME_N_LT));
        this.addPluggableName(new PluggableName(NAME_GET,NAME_N_GET));
        this.addPluggableName(new PluggableName(NAME_GT,NAME_N_GT));
        this.addPluggableName(new PluggableName(NAME_N_LET));
        this.addPluggableName(new PluggableName(NAME_N_LT));
        this.addPluggableName(new PluggableName(NAME_N_GET));
        this.addPluggableName(new PluggableName(NAME_N_GT));
        this.addPluggableName(new PluggableName(NAME_N_EQ));
    }

    @Override
    public void test(Runner runner, Map<String, Parameter> operands, String name, String parameter) throws Exception
    {
        try
        {
            AbstractPluggableParameterOperator.normalizeParameters(operands);
        }
        catch(ParameterException e)
        {
            throw new AssertException(e.getMessage(), e);
        }
        
        Parameter param1 = AbstractPluggableParameterOperator.assertAndGetParameter(operands, "parameter");
        Parameter param2 = AbstractPluggableParameterOperator.assertAndGetParameter(operands, "value");

        int len = param1.length();
        if(0 == param1.length()) throw new AssertException("A test between empty parameters is a KO");
        for (int i = 0; i < len; i++)
        {
            double op1 = Double.parseDouble(param1.get(i).toString());
            double op2 = Double.parseDouble(param2.get(i).toString());

            if (name.equalsIgnoreCase(NAME_LET) || name.equalsIgnoreCase(NAME_N_LET))
            {
                if(!(op1 <= op2))
                {
                    throw new AssertException("Error in math test " + name + " between \n" + param1 + "and\n" + param2 + "\n" + op1 + " is not <= to " + op2);
                }
            }
            else if (name.equalsIgnoreCase(NAME_LT) || name.equalsIgnoreCase(NAME_N_LT))
            {
                if(!(op1 < op2))
                {
                    throw new AssertException("Error in math test " + name + " between \n" + param1 + "and\n" + param2 + "\n" + op1 + " is not < to " + op2);
                }
            }
            else if (name.equalsIgnoreCase(NAME_GET) || name.equalsIgnoreCase(NAME_N_GET))
            {
                if(!(op1 >= op2))
                {
                    throw new AssertException("Error in math test " + name + " between \n" + param1 + "and\n" + param2 + "\n" + op1 + " is not >= to " + op2);
                }
            }
            else if (name.equalsIgnoreCase(NAME_GT) || name.equalsIgnoreCase(NAME_N_GT))
            {
                if(!(op1 > op2))
                {
                    throw new AssertException("Error in math test " + name + " between \n" + param1 + "and\n" + param2 + "\n" + op1 + " is not > to " + op2);
                }
            }
            else if (name.equalsIgnoreCase(NAME_N_EQ))
            {
                if(!(op1 == op2))
                {
                    throw new AssertException("Error in math test " + name + " between \n" + param1 + "and\n" + param2 + "\n" + op1 + " is not == to " + op2);
                }
            }
            else
            {
                throw new RuntimeException("unsupported math test " + name);
            }
        }
    }
}
