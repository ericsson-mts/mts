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

/**
 *
 * @author gpasquiers
 */
public class PluggableParameterTestString extends AbstractPluggableParameterTest
{
    final private String NAME_EQUALS    = "equals";
    final private String NAME_CONTAINS  = "contains";
    final private String NAME_MATCHES   = "matches";
    final private String NAME_S_EQUALS  = "string.equals";
    final private String NAME_S_EQUALSI = "string.equalsignorecase";
    final private String NAME_S_CONTAINS= "string.contains";
    final private String NAME_S_MATCHES = "string.matches";
    final private String NAME_S_STARTSWITH= "string.startswith";
    final private String NAME_S_ENDSWITH = "string.endswith";


    public PluggableParameterTestString()
    {
        this.addPluggableName(new PluggableName(NAME_EQUALS, NAME_S_EQUALS));
        this.addPluggableName(new PluggableName(NAME_CONTAINS, NAME_S_CONTAINS));
        this.addPluggableName(new PluggableName(NAME_MATCHES, NAME_S_MATCHES));
        this.addPluggableName(new PluggableName(NAME_S_EQUALS));
        this.addPluggableName(new PluggableName(NAME_S_EQUALSI));
        this.addPluggableName(new PluggableName(NAME_S_CONTAINS));
        this.addPluggableName(new PluggableName(NAME_S_MATCHES));
        this.addPluggableName(new PluggableName(NAME_S_STARTSWITH));
        this.addPluggableName(new PluggableName(NAME_S_ENDSWITH));
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
            if(name.equalsIgnoreCase(NAME_EQUALS) || name.equalsIgnoreCase(NAME_S_EQUALS))
            {
                if(!param.get(i).toString().equals(testValue.get(i).toString()))
                {
                    throw new AssertException("Error in string test " + name + " between \n" + param + "and\n" + testValue + "\n" + param.get(i) + " is not equal to \n" + testValue.get(i));
                }
            }
            else if(name.equalsIgnoreCase(NAME_S_EQUALSI))
            {
                if(!param.get(i).toString().equalsIgnoreCase(testValue.get(i).toString()))
                {
                    throw new AssertException("Error in string test " + name + " between \n" + param + "and\n" + testValue + "\n" + param.get(i) + " is not equal to \n" + testValue.get(i));
                }
            }
            else if(name.equalsIgnoreCase(NAME_CONTAINS) || name.equalsIgnoreCase(NAME_S_CONTAINS))
            {
                if(!param.get(i).toString().contains(testValue.get(i).toString()))
                {
                    throw new AssertException("Error in string test " + name + " between \n" + param + "and\n" + testValue + "\n" + param.get(i) + " does not contain \n" + testValue.get(i));
                }
            }
            else if(name.equalsIgnoreCase(NAME_S_STARTSWITH))
            {
                if(!param.get(i).toString().startsWith(testValue.get(i).toString()))
                {
                    throw new AssertException("Error in string test " + name + " between \n" + param + "and\n" + testValue + "\n" + param.get(i) + " does not start with \n" + testValue.get(i));
                }
            }
            else if(name.equalsIgnoreCase(NAME_S_ENDSWITH))
            {
                if(!param.get(i).toString().endsWith(testValue.get(i).toString()))
                {
                    throw new AssertException("Error in string test " + name + " between \n" + param + "and\n" + testValue + "\n" + param.get(i) + " does not end with \n" + testValue.get(i));
                }
            }
            else if(name.equalsIgnoreCase(NAME_MATCHES) || name.equalsIgnoreCase(NAME_S_MATCHES))
            {
                Pattern p = Utils.compilesRegex(testValue.get(i).toString());
                Matcher m = p.matcher(param.get(i).toString());
                if(!m.find())
                {
                    throw new AssertException("Error in string test " + name + " between \n" + param + "and\n" + testValue + "\n" + param.get(i) + " does not match \n" + testValue.get(i));
                }
            }
            else 
            {
            	throw new RuntimeException("Unsupported <test> operation for condition = " + name);
            }

        }
    }
}
