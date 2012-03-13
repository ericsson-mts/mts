/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.core.operations.basic.tests;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.AssertException;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;
import java.util.Map;

/**
 *
 * @author gpasquiers
 */
public class PluggableParameterTestListPool extends AbstractPluggableParameterTest
{

    final private String NAME = "exists";
    final private String L_NAME = "list.exists";

    public PluggableParameterTestListPool()
    {
        this.addPluggableName(new PluggableName(NAME, L_NAME));
        this.addPluggableName(new PluggableName(L_NAME));
    }

    @Override
    public void test(Runner runner, Map<String, Parameter> operands, String name, String parameter) throws Exception
    {
        boolean expected = true;
        Parameter param = operands.get("value");
        if(param != null)
        {
            if(param.length() != 1) throw new ParameterException("value should have a size of one in test " + name);
            expected = Boolean.parseBoolean(param.get(0).toString());
        }
        if (runner.getParameterPool().exists(parameter) != expected)
        {
            throw new AssertException("Error in basic test, parameter " + parameter + " hasn't the expected existing status: " + expected);
        }
    }
}
