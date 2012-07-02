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

package com.devoteam.srit.xmlloader.core.operations.basic.operators;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author gpasquiers
 */
public class PluggableParameterOperatorSetFromSystem extends AbstractPluggableParameterOperator
{

    public PluggableParameterOperatorSetFromSystem()
    {

        this.addPluggableName(new PluggableName("setFromSystem"));
    }

    @Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws ParameterException
    {
        this.normalizeParameters(operands);
        Parameter csvKey = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
        Parameter csvParam = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
        Parameter result = new Parameter();
        try
        {
            for (int i = 0; i < csvKey.length(); i++)
            {
                String var1 = csvKey.get(i).toString();
                String var2 = csvParam.get(i).toString();
                if(var1.equalsIgnoreCase("IPAddress"))
                {
                    GlobalLogger.instance().logDeprecatedMessage(
                			"parameter ... operation=\"setFromSystem\" value=\"" +
                    		var1 +
                			"\" value2=\"xxx\"/", 
                			"parameter ... operation=\"" +             		
                			"system.ipaddress" +
                 			"\" value=\"xxx\"/"            		
                    		);                	
                    InetAddress address = null;
                    for(Enumeration e = NetworkInterface.getNetworkInterfaces();e.hasMoreElements();)
                    {
                        NetworkInterface eth= (NetworkInterface)e.nextElement();
                        if(eth.getName().equals(var2))
                        {
                            for(Enumeration addr = eth.getInetAddresses();addr.hasMoreElements();)
                            {
                                address = (InetAddress)addr.nextElement();
                                if(address instanceof Inet4Address )
                                {
                                    result.add(address.getHostAddress()); // obtain the ip value from the specified interface
                                }
                            }
                        }
                    }

                    return result;
                }
                else if(var1.equalsIgnoreCase("Timestamp"))
                {
                    GlobalLogger.instance().logDeprecatedMessage(
                			"parameter ... operation=\"setFromSystem\" value=\"" +
                    		var1 +
                			"\" value2=\"xxx\"/", 
                			"parameter ... operation=\"" +             		
                			"system.timestamp" +
                 			"\" value=\"xxx\"/"            		
                    		);                	
                    if(var2.equals("1970"))
                    {
                        result.add(String.valueOf((System.currentTimeMillis())));
                        return result;
                    }
                    else if(var2.equals("1900"))
                    {
                        Calendar cal =Calendar.getInstance(Locale.US);
                        cal.set(Integer.parseInt(var2),00,01,00,00,00); // current time in millisecond since 1st january 1900
                        Long timeMillis1900 = cal.getTimeInMillis(); // obtain the time in milliseconds
                        result.add(String.valueOf((System.currentTimeMillis()) - timeMillis1900));
                        return result;
                    }
                }
                else if(var1.equalsIgnoreCase("CurrentDirectory"))
                {
                    GlobalLogger.instance().logDeprecatedMessage(
                			"parameter ... operation=\"setFromSystem\" value=\"" +
                    		var1 +
                			"\"/", 
                			"parameter ... operation=\"" +             		
                			"system.readproperty" +
                 			"\" value=\"user.dir\"/"            		
                    		);                	
                    String currentFolder = System.getProperty(var2.replace(":","."));
                    result.add(currentFolder);
                    return result;
                }
            }
        }
        catch(ParameterException e)
        {
            throw e;
        }
        catch(Exception e)
        {
            throw new ParameterException("Error in setFromSystem operator", e);
        }

        return result;
    }
}
