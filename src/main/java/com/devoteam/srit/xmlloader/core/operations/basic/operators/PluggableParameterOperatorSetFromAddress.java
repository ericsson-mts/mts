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
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;
import java.util.Map;
import javax.sip.SipFactory;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.URI;

/**
 *
 * @author gpasquiers
 */
public class PluggableParameterOperatorSetFromAddress extends AbstractPluggableParameterOperator
{

	private static String OPERATION_TYPE = "setFromAddress";
	
    public PluggableParameterOperatorSetFromAddress()
    {
        this.addPluggableName(new PluggableName(OPERATION_TYPE));
        this.addPluggableName(new PluggableName("protocol." + OPERATION_TYPE));
    }

    @Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws ParameterException
    {
        this.normalizeParameters(operands);
        Parameter addresses = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
        Parameter pathes = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
        Parameter result = new Parameter();
        try
        {
            for (int i = 0; i < addresses.length(); i++)
            {
                result.add(setFromAddress(addresses.get(i).toString(), pathes.get(i).toString()));
            }
        }
        catch (Exception e)
        {
            throw new ParameterException("Error in setFromURI operator", e);
        }

        return result;

    }

    public static String setFromAddress(String var1, String var2) throws Exception
    {	
        // process deprecated path keyword
        if(var2.indexOf(':') != -1){
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.CORE,
    			"Deprecated separator \":\" in path " + var2,
    			" please use \".\" instead.");
            var2 = var2.replace(':', '.');            
        }
    
        AddressFactory addressFactory = SipFactory.getInstance().createAddressFactory();
        var1 = var1.trim();
        Address addr = addressFactory.createAddress(var1);

        String varResult = null;
        if (var2.equalsIgnoreCase("displayName"))
        {
            // obtain the displayName
            varResult = addr.getDisplayName();
        }
        else if (var2.equalsIgnoreCase("uri"))
        {
            // obtain the uri
            URI someURI = addr.getURI();
            if (null != someURI)
            {
                varResult = someURI.toString();
            }
        }
        else if (var2.toLowerCase().startsWith("uri."))
        {
            // obtain the other path from an URI (see setFromUri operation)
            URI someURI = addr.getURI();
            if (null != someURI)
            {
                String operandeUri = var2.substring(4, var2.length());
                varResult = PluggableParameterOperatorSetFromURI.setFromUri(addr.getURI().toString(), operandeUri);
            }
        }
        else if (var2.toLowerCase().startsWith("parameter"))
        {
            // obtain a parameter
            String params = "";
            String paramValue = "";
            if (var1.contains("<"))
            {
                // uri between < and >
                params = var1.substring(var1.indexOf(">"), var1.length());
            }
            else
            {
                if (var1.contains(";"))
                {
                    params = var1.substring(var1.indexOf(";"), var1.length());
                }
            }
            if (!params.equals(""))
            {
                String[] parameters = params.split(";");
                for (int i = 0; i < parameters.length; i++)
                {
                    if (parameters[i].contains(var2.substring(var2.indexOf(".") + 1, var2.length())))
                    {
                        if (parameters[i].contains("="))
                        {
                            paramValue = parameters[i].substring(parameters[i].indexOf("=") + 1, parameters[i].length());
                            varResult = paramValue; // obtain the parameter value
                        }
                        else
                        {
                            varResult = var2; // obtain the parameter name
                        }
                    }
                }
                if (paramValue.equals(""))
                {
                    varResult = ""; // set empty
                }
            }
        }
        else
        {
        	Parameter.throwBadPathKeywordException(OPERATION_TYPE, var2);
        }
        return varResult;
    }
}
