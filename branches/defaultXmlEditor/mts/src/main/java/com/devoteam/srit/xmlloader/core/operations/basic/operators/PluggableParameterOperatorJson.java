/*
 * Copyright 2015 Orange http://www.orange.com
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
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.basic.operators.AbstractPluggableParameterOperator;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import java.util.Map;

import net.minidev.json.JSONArray;

/**
 *
 * @author glepessot
 */
public class PluggableParameterOperatorJson extends AbstractPluggableParameterOperator
{

    final private String NAME_JSON_JPATH = "string.jpath";
 
    public PluggableParameterOperatorJson()
    {
        this.addPluggableName(new PluggableName(NAME_JSON_JPATH));
    }

    @Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws Exception
    {
    	Parameter param1 = assertAndGetParameter(operands, "value");
        Parameter param2 = assertAndGetParameter(operands, "value2");
        
        Parameter result = new Parameter();
        
        try
        {        	        	
            for (int i = 0; i < param1.length(); i++)
            {
                String var1 = param1.get(i).toString();
                String var2 = param2.get(i).toString();
                
                // Retrieve JSON content
                GlobalLogger.instance().getSessionLogger().debug(TextEvent.Topic.USER, NAME_JSON_JPATH + ": JSON content is " + var1);
                GlobalLogger.instance().getSessionLogger().debug(TextEvent.Topic.USER, NAME_JSON_JPATH + ": JSON path is " + var2);
                
    			      Object document = Configuration.defaultConfiguration().jsonProvider().parse(var1);
                Object path_result = JsonPath.read(document, var2);
                
                // Prepare result
                if( path_result instanceof JSONArray && ((JSONArray) path_result).size() > 0)
                {
                	for( int cptR=0 ; cptR!=((JSONArray) path_result).size() ; cptR++ )
                		result.add(((JSONArray) path_result).get(cptR).toString());
                }
                else
                	result.add(path_result.toString());
                
                GlobalLogger.instance().getSessionLogger().debug(TextEvent.Topic.USER, NAME_JSON_JPATH + ": JSON result is " + result.toString());
            }
        }
        catch (Exception e)
        {
        	throw e;
        }
        return result;
    }
}
