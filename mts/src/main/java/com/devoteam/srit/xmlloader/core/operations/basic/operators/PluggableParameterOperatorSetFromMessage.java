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
import com.devoteam.srit.xmlloader.core.ScenarioRunner;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import java.util.Map;

/**
 *
 * @author gpasquiers
 */
public class PluggableParameterOperatorSetFromMessage extends AbstractPluggableParameterOperator
{
    public PluggableParameterOperatorSetFromMessage()
    {

        this.addPluggableName(new PluggableName("setFromMessage", "protocol.setFromMessage"));
        this.addPluggableName(new PluggableName("protocol.setFromMessage"));
    }
    
    @Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws ParameterException
    {
    	String params = null;
        try
        {
            Parameter paramPath = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
            if(paramPath.length() != 1) throw new ParameterException("The parameter containg the path should have a size of 1");
            
            Msg msg;
            if(operands.containsKey("value2"))
            {
                Parameter paramMsg = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
                if(paramMsg.length() != 1) throw new ParameterException("The parameter containg the message should have a size of 1");
                msg = (Msg) paramMsg.get(0);    
            }
            else
            {
                msg = ((ScenarioRunner) runner).getCurrentMsg();
            }
            
            params = paramPath.get(0).toString();
            return msg.getParameter(params);            
        }
        catch(ParameterException e)
        {
            throw e;
        }
        catch(Exception e)
        {
            throw new ParameterException("Error in setFromMessage : " + params, e);
        }
    }

}
