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
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import java.util.Map;

import org.dom4j.Element;
import org.dom4j.tree.BaseElement;

/**
 *
 * @author gpasquiers
 */
public class PluggableParameterOperatorProtocol extends AbstractPluggableParameterOperator
{

    final private String NAME_PROTO_PARSE_MESSAGE = "protocol.parseMessage";

    public PluggableParameterOperatorProtocol()
    {

        this.addPluggableName(new PluggableName(NAME_PROTO_PARSE_MESSAGE));
    }

    @Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws Exception
    {       
        normalizeParameters(operands);

        Parameter param1 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
        Parameter param2 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
        
        Parameter result = new Parameter();
        for(int i=0; i<param1.length(); i++)
        {

            if(name.equalsIgnoreCase(NAME_PROTO_PARSE_MESSAGE)) 
            {
                String text = param1.get(i).toString();
                String protocol = param2.get(i).toString();
                Stack stack = StackFactory.getStack(protocol); 
                Msg.ParseFromXmlContext context = new Msg.ParseFromXmlContext();   
                Element root = new BaseElement("root");
                root.addText(text);
                Msg msg = stack.parseMsgFromXml(context, root, runner);
                result.add(msg);	
                ((ScenarioRunner) runner).setCurrentMsg(msg);
            }
        }
        return result;
    }

}
