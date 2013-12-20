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
import com.devoteam.srit.xmlloader.core.utils.Utils;

import java.net.URI;
import java.util.Map;
import javax.sip.SipFactory;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.address.TelURL;

/**
 *
 * @author gpasquiers
 */
public class PluggableParameterOperatorSetFromURI extends AbstractPluggableParameterOperator
{

	private static String OPERATION_TYPE = "setFromURI";
	
    public PluggableParameterOperatorSetFromURI()
    {
        this.addPluggableName(new PluggableName(OPERATION_TYPE));
        this.addPluggableName(new PluggableName("protocol." + OPERATION_TYPE));
    }

    @Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws ParameterException
    {
        this.normalizeParameters(operands);
        Parameter uris = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
        Parameter pathes = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
        Parameter result = new Parameter();
        try
        {
            for (int i = 0; i < uris.length(); i++)
            {
                result.add(setFromUri(uris.get(i).toString(),pathes.get(i).toString()));
            }
        }
        catch (Exception e)
        {
            throw new ParameterException("Error in setFromURI operator", e);
        }

        return result;
    }

    public static String setFromUri(String var1, String var2) throws Exception
    {
        // process deprecated path keyword
        if(var2.indexOf(':') != -1){
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.CORE,
    			"Deprecated separator \":\" in path " + var2,
    			" please use \".\" instead.");
            var2 = var2.replace(':', '.');            
        }

        String varResult = null;
        var1 = var1.trim();
        URI uri = new URI(var1); // create an uri

        if (!uri.isOpaque())
        {
		    if (var2.equalsIgnoreCase("authority"))
		    {
		        varResult = uri.getAuthority(); // obtain the authority
		    }
		    else if (var2.equalsIgnoreCase("fragment"))
		    {
		        varResult = uri.getFragment(); // obtain the host
		    }
		    else if ((!uri.isOpaque()) && (var2.equalsIgnoreCase("host")))
		    {
		        varResult = uri.getHost(); // obtain the host
		    }
		    else if (var2.equalsIgnoreCase("path"))
		    {
		        varResult = uri.getPath(); // obtain the subdirectories
		    }
		    else if (var2.equalsIgnoreCase("port"))
		    {
		        int port = uri.getPort();
		        if (port > 0)
		        {
		            varResult = String.valueOf(port); // obtain the port
		        }
		    }
		    else if (var2.equalsIgnoreCase("query"))
		    {
		        varResult = uri.getQuery(); // obtain the queries
		    }
		    else if (var2.equalsIgnoreCase("scheme"))
		    {
		        varResult = uri.getScheme(); // obtain the scheme
		    }
		    else if (var2.equalsIgnoreCase("schemeSpecificPart"))
		    {
		        varResult = uri.getSchemeSpecificPart(); // obtain the authority + subdirectories + parameters
		    }
		    else if (var2.equalsIgnoreCase("user"))
		    {
		        varResult = uri.getUserInfo(); // obtain the user information
		    }
		    else if (var2.equalsIgnoreCase("absolute"))
		    {
		    	if (uri.isAbsolute())
		    	{
		    		varResult = "absolute"; // obtain the absolute information
		    	}
		    }
		    else if (var2.equalsIgnoreCase("opaque"))
		    {
		    	if (uri.isOpaque())
		    	{
		    		varResult = "opaque"; // obtain the opaque information
		    	}
		    }
            else
            {
            	// Parameter.throwBadPathKeywordException(OPERATION_TYPE, var2);
            }
        }
        else
        { 	
            AddressFactory addressFactory = SipFactory.getInstance().createAddressFactory();
            javax.sip.address.URI uriGeneric = addressFactory.createURI(var1);

            // SipUri
            if (uriGeneric.isSipURI())
            {
            	if (var2.equalsIgnoreCase("host"))
                {
                    varResult = ((SipURI) uriGeneric).getHost(); // obtain the host
                }
                else if (var2.equalsIgnoreCase("port"))
                {
                    int port = ((SipURI) uriGeneric).getPort();
                    if (port > 0)
                    {
                        varResult = String.valueOf(port); // obtain the port
                    }
                }
            	else if (var2.equalsIgnoreCase("scheme"))
                {
                    varResult = ((SipURI) uriGeneric).getScheme(); // obtain the scheme
                }
                else if (var2.equalsIgnoreCase("user"))
                {
                    varResult = ((SipURI) uriGeneric).getUser(); // obtain the user
                }
                else if (var2.equalsIgnoreCase("phonenumber"))
                {
                    varResult = ((SipURI) uriGeneric).getUser(); // obtain the phonenumber : same as user
                }
                else if (var2.equalsIgnoreCase("password"))
                {
                    varResult = ((SipURI) uriGeneric).getUserPassword(); // obtain the user password
                }
                else if (var2.toLowerCase().startsWith("header"))
                {
                    String headerValue = ((SipURI) uriGeneric).getHeader(var2.substring(var2.indexOf(".") + 1, var2.length()));
                    if (headerValue != null)
                    {
                        varResult = headerValue; // obtain the header value
                    }
                }
                else if (var2.toLowerCase().startsWith("parameter"))
                {
                    String paramValue = ((SipURI) uriGeneric).getParameter(var2.substring(var2.indexOf(".") + 1, var2.length()));
                    if (paramValue != null)
                    {
                        varResult = paramValue; // obtain the parameter value
                    }
                }
                else if (var2.equalsIgnoreCase("lr"))
                {
                    if (((SipURI) uriGeneric).hasLrParam())
                    {
                    	varResult = "lr"; // verify if lr parameter is present
                    }
                }
                else if (var2.equalsIgnoreCase("secure"))
                {
                    if (((SipURI) uriGeneric).isSecure())
                    {
                        varResult = "secure"; // verify if scheme is secure (e.g sips)
                    }
                }
                else if (var2.equalsIgnoreCase("sipURI"))
                {
                    if (uriGeneric.isSipURI())
                    {
                        varResult = "sipURI"; // verify if scheme is sip
                    }
                }
                else
                {
                	// Parameter.throwBadPathKeywordException(OPERATION_TYPE, var2);
                }
            }
            // TelUrl
            else 
            {
                if (var2.equalsIgnoreCase("isdnSubAddress"))
                {
                    varResult = ((TelURL) uriGeneric).getIsdnSubAddress(); // Isdn sub address
                }
                else if (var2.equalsIgnoreCase("phoneContext"))
                {
                    varResult = ((TelURL) uriGeneric).getPhoneContext(); // obtain the phone context
                }
                else if (var2.equalsIgnoreCase("phoneNumber"))
                {
                    varResult = ((TelURL) uriGeneric).getPhoneNumber(); // obtain the phone number
                }
                else if (var2.equalsIgnoreCase("user"))
                {
                    String prefix = "";
                    if(((TelURL) uriGeneric).isGlobal())
                    {
                    	prefix = "+";
                    }
                    varResult = prefix + ((TelURL) uriGeneric).getPhoneNumber(); // obtain the user : same as phone number
                }
                else if (var2.equalsIgnoreCase("postDial"))
                {
                    varResult = ((TelURL) uriGeneric).getPostDial(); // obtain the post dial 
                }
                else if (var2.equalsIgnoreCase("scheme"))
                {
                    varResult = uriGeneric.getScheme(); // obtain the scheme
                }
                else if (var2.equalsIgnoreCase("global"))
                {
                	if (((TelURL) uriGeneric).isGlobal())
                	{
                		varResult = "global"; // verify if the uri is global 
                	}
                }
                else if (var2.equalsIgnoreCase("telURI"))
                {
                    if (!uriGeneric.isSipURI())
                    {
                        varResult = "telURI"; // verify if scheme is TEL
                    }
                }
                else if (var2.toLowerCase().startsWith("parameter"))
                {
                    String paramValue = ((TelURL) uriGeneric).getParameter(var2.substring(var2.indexOf(".") + 1, var2.length()));
                    if (paramValue != null)
                    {
                        varResult = paramValue; // obtain the parameter value
                    }
                }
                else
                {
                	// Parameter.throwBadPathKeywordException(OPERATION_TYPE, var2);
                }
            }

        }
        return varResult;
    }
}
