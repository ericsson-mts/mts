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
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.rtp.flow.MsgRtpFlow;

import gp.utils.arrays.Array;
import gp.utils.arrays.SupArray;

import java.util.Map;

/**
 *
 * @author gpasquiers
 */
public class PluggableParameterTestList extends AbstractPluggableParameterTest
{

    final private String NAME_EMPTY = "isEmpty";
    final private String NAME_L_EMPTY = "list.isEmpty";
    final private String NAME_SIZE = "list.size";
    final private String NAME_CONTAINS = "list.contains";
    final private String NAME_FLOWCONTAINS = "list.flowcontains";

    public PluggableParameterTestList()
    {
        this.addPluggableName(new PluggableName(NAME_EMPTY, NAME_L_EMPTY));
        this.addPluggableName(new PluggableName(NAME_L_EMPTY));
        this.addPluggableName(new PluggableName(NAME_SIZE));
        this.addPluggableName(new PluggableName(NAME_CONTAINS));
        this.addPluggableName(new PluggableName(NAME_FLOWCONTAINS));
    }

    @Override
    public void test(Runner runner, Map<String, Parameter> operands, String name, String parameter) throws Exception
    {
        Parameter param1 = AbstractPluggableParameterOperator.assertAndGetParameter(operands, "parameter");

        if(name.equalsIgnoreCase(NAME_EMPTY) || name.equalsIgnoreCase(NAME_L_EMPTY))
        {
            if(param1.length() > 0)
            {
                throw new AssertException("<test operation=\"" + name + "\"/> KO : " + param1 + " is not empty");
            }
        }
        else if(name.equalsIgnoreCase(NAME_SIZE))
        {
            Parameter param2 = AbstractPluggableParameterOperator.assertAndGetParameter(operands, "value");
            if(param2.length() != 1)
            {
                throw new AssertException("attribute value of test " + name + " should have a size of 1");
            }

            int size = Integer.parseInt(param2.get(0).toString());

            if(param1.length() != size)
            {
                throw new AssertException("<test operation=\"" + name + "\"/> KO : size is " + param1.length() + " not " + size);
            }
        }
        else if(name.equalsIgnoreCase(NAME_CONTAINS))
        {
            Parameter param2 = AbstractPluggableParameterOperator.assertAndGetParameter(operands, "value"); 
            
            int i = 0;
            while (i < param1.length())
            {
            	int j= 0;
            	int k = i;
            	if (param1.get(k).equals(param2.get(j)))
            	{
                    while (k < param1.length() && j < param2.length())
                    {
                    	if (param1.get(k).equals(param2.get(j)))
                    	{            		
                    		k++;
                    		j++;
                    	}
                    	else
                    	{ 
                    		break;
                    	}
                    }
            	}
                if (j >= param2.length())
                {
                	break;
                }
        		i++;
                if (i >= param1.length())
                {
                	throw new AssertException("<test condition=\"" + name + "\"/> KO : " + param1 + " does not contain " + param2);
                }
            }                                
        }
        else if(name.equalsIgnoreCase(NAME_FLOWCONTAINS))
        {
            Parameter param2 = AbstractPluggableParameterOperator.assertAndGetParameter(operands, "value"); 
            
            int i = 0;
            while (i < param1.length())
            {
            	int j= 0;
            	int k = i;
            	if (flowEquals(param1.get(k), param2.get(j)))
            	{
                    while (k < param1.length() && j < param2.length())
                    {
                    	if (flowEquals(param1.get(k), param2.get(j)))
                    	{            		
                    		k++;
                    		j++;
                    	}
                    	else
                    	{ 
                    		break;
                    	}
                    }
            	}
                if (j >= param2.length())
                {
                	break;
                }
        		i++;
                if (i >= param1.length())
                {
                	throw new AssertException("<test condition=\"" + name + "\"/> KO : " + param1 + " does not contain " + param2);
                }
            }                                
        }
        else
        {
        	throw new RuntimeException("Unsupported <test> operation for condition = " + name);
        }
        
    }

    private boolean flowEquals(Object o1, Object o2) throws Exception
    {
    	SupArray supArray = MsgRtpFlow.getEmptyPacket(o2.toString().getBytes().length / 2);
        String pattern = Array.toHexString(supArray);       
    	if (o1.equals(pattern))
    	{
    		return true;
    	}
    	return o1.equals(o2);
    }
    
}
