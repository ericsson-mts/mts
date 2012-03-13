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
