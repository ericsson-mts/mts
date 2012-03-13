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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.dom4j.Element;

/**
 *
 * @author gpasquiers
 */
public class PluggableParameterTestBoolean extends AbstractPluggableParameterTest
{
    final private String NAME_EQUALS = "boolean.equals";

    public PluggableParameterTestBoolean()
    {
        this.addPluggableName(new PluggableName(NAME_EQUALS));
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
        
        Parameter param = AbstractPluggableParameterOperator.assertAndGetParameter(operands, "parameter");
        Parameter testValue = AbstractPluggableParameterOperator.assertAndGetParameter(operands, "value");
        

        int len = param.length();
        if(0 == len) throw new AssertException("A test between empty parameters is a KO");
        for(int i=0; i<len; i++)
        {
            if(name.equalsIgnoreCase(NAME_EQUALS))
            {
                boolean bool1 = Boolean.parseBoolean(param.get(i).toString());
                boolean bool2 = Boolean.parseBoolean(testValue.get(i).toString());
                if(bool1 != bool2)
                {
                    throw new AssertException("Error " + name + " test between \n" + param + "and\n" + testValue + "\n" + param.get(i) + " is not equal to " + testValue.get(i));
                }
            }
            else throw new RuntimeException("unsupported test " + name);
        }
    }
}
