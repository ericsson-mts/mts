/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.core.operations.basic.operators;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import java.util.Map;

/**
 *
 * @author gpasquiers
 */
public class PluggableParameterOperatorIdentifier extends AbstractPluggableParameterOperator
{
    public PluggableParameterOperatorIdentifier()
    {

        this.addPluggableName(new PluggableName("identifier"));
    }

    @Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws ParameterException
    {
        normalizeParameters(operands);
        Parameter identifierAction = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
        Parameter identifierParam = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
        Parameter result = new Parameter();
        try
        {
            for (int i = 0; i < identifierAction.length(); i++)
            {
                String var1 = identifierAction.get(i).toString();
                String var2 = identifierParam.get(i).toString();
                if (var1.equalsIgnoreCase("newInteger"))
                {
                    GlobalLogger.instance().logDeprecatedMessage(
                			"parameter ... operation=\"identifier\" value=\"" +
                    		var1 +
                			"\" value2=\"xxx\"/", 
                			"parameter ... operation=\"" +             		
                			"number.random" +
                 			"\" value=\"xxx\"/"            		
                    		);
                    long number = (long) Math.floor(Math.random()*Integer.valueOf(var2)); // generates a new Integer between 0 and var2
                    String res = String.valueOf(number);
                    int length = var2.length();

                    res = Utils.padInteger(res, length);

                    result.add(res);
                }
                else if (var1.equalsIgnoreCase("nextInteger"))
                {
                    GlobalLogger.instance().logDeprecatedMessage(
                			"parameter ... operation=\"identifier\" value=\"" +
                    		var1 +
                			"\"/", 
                			"parameter ... operation=\"" +             		
                			"number.uid" +
                 			"\"/"            		
                    		);
                    long number = nextInteger(); // generates the next number.
                    result.add(String.valueOf(number));
                }
                else if (var1.equalsIgnoreCase("newString"))
                {
                    GlobalLogger.instance().logDeprecatedMessage(
                			"parameter ... operation=\"identifier\" value=\"" +
                    		var1 +
                			"\" value2=\"xxx\"/", 
                			"parameter ... operation=\"" +             		
                			"string.random" +
                 			"\" value=\"xxx\"/"            		
                    		);                	
                    StringBuilder s = new StringBuilder();
                    for (int j = 0; j < Integer.valueOf(var2); j++)
                    {
                        int nextChar = (int) (Math.random() * 62);
                        if (nextChar < 10) //0-9
                        {
                            s.append(nextChar);
                        }
                        else if (nextChar < 36) //a-z
                        {
                            s.append((char) (nextChar - 10 + 'a'));
                        }
                        else //A-Z
                        {
                            s.append((char) (nextChar - 36 + 'A'));
                        }
                    }
                    result.add(s.toString());
                }
                else
                {
                    throw new ParameterException("unknown parameter " + var1 + " should be either newInteger, nextInteger, or newString");
                }
            }
        }
        catch (ParameterException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ParameterException("Error in setFromSystem operator", e);
        }

        return result;
    }
    
    static private long nextInteger = 0;
    
    public static synchronized long nextInteger()
    {
        return nextInteger++;
    }
}
