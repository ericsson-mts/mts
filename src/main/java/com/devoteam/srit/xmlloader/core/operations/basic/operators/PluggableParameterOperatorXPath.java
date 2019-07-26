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
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;
import com.devoteam.srit.xmlloader.core.utils.XMLLoaderEntityResolver;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.dom4j.xpath.DefaultXPath;

/**
 *
 * @author gpasquiers
 */
public class PluggableParameterOperatorXPath extends AbstractPluggableParameterOperator
{

    final private String NAME_XPATH = "xpath";
    final private String NAME_S_XPATH = "string.xpath";

    public PluggableParameterOperatorXPath()
    {

        this.addPluggableName(new PluggableName(NAME_XPATH, NAME_S_XPATH));
        this.addPluggableName(new PluggableName(NAME_S_XPATH));
    }

    @Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws ParameterException
    {
        normalizeParameters(operands);

        Parameter param1 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
        Parameter param2 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
        Parameter result = new Parameter();
        try
        {
            for (int i = 0; i < param1.length(); i++)
            {
                String var1 = param1.get(i).toString();
                String var2 = param2.get(i).toString();
	    		result.applyXPath(var1, var2, false);
            }
        }
        catch (Exception e)
        {
            throw new ParameterException("error in XPath operator", e);
        }
        return result;
    }

}
