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
import gp.utils.arrays.Array;
import java.util.Map;

/**
 *
 * @author gpasquiers
 */
public class PluggableParameterTestBinary extends AbstractPluggableParameterTest
{
    final private String NAME_EQUALS = "binary.equals";
    final private String NAME_CONTAINS = "binary.contains";

    public PluggableParameterTestBinary()
    {
        this.addPluggableName(new PluggableName(NAME_EQUALS));
        this.addPluggableName(new PluggableName(NAME_CONTAINS));
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
                 if (name.equals(NAME_EQUALS))
                {
                    Array array1 = Array.fromHexString(param.get(i).toString());
                    Array array2 = Array.fromHexString(testValue.get(i).toString());

                    if(!array1.equals(array2))
                    {
                        throw new AssertException("Error " + name + " test between \n" + param + "and\n" + testValue + "\n" + param.get(i) + " is not equal to " + testValue.get(i));
                    }
                }
                else if (name.equals(NAME_CONTAINS))
                {
                    Array array1 = Array.fromHexString(param.get(i).toString());
                    Array array2 = Array.fromHexString(testValue.get(i).toString());

                    if(array1.indexOf(array2) == -1)
                    {
                        throw new AssertException("Error " + name + " test between \n" + param + "and\n" + testValue + "\n" + param.get(i) + " does not contain " + testValue.get(i));
                    }
                }
               else throw new RuntimeException("unsupported test " + name);
        }
    }
}
