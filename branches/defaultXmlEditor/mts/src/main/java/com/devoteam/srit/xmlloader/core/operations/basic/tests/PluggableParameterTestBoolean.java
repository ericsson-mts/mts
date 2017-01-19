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

package com.devoteam.srit.xmlloader.core.operations.basic.tests;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.AssertException;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.operations.basic.operators.AbstractPluggableParameterOperator;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;
import com.devoteam.srit.xmlloader.core.utils.Utils;

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
        if (0 == len)
        {
        	throw new AssertException("A test between empty parameters is a KO");
        }
        
        for (int i=0; i<len; i++)
        {
            if (name.equalsIgnoreCase(NAME_EQUALS))
            {
            	boolean bool1 = Utils.parseBoolean(testValue.get(i).toString(), name);
            	boolean bool2 = Utils.parseBoolean(param.get(i).toString(), name);

                if(bool1 != bool2)
                {
                    throw new AssertException("Error " + name + " test between \n" + param + "and\n" + testValue + "\n" + param.get(i) + " is not equal to " + testValue.get(i));
                }
            }
            else 
            {
            	throw new RuntimeException("Unsupported <test> operation for condition = " + name);
            }
        }
    }
}
