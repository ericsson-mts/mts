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
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;
import java.util.Map;

/**
 *
 * @author gpasquiers
 */
public class PluggableParameterTestListPool extends AbstractPluggableParameterTest
{

    final private String NAME = "exists";
    final private String L_NAME = "list.exists";

    public PluggableParameterTestListPool()
    {
        this.addPluggableName(new PluggableName(NAME, L_NAME));
        this.addPluggableName(new PluggableName(L_NAME));
    }

    @Override
    public void test(Runner runner, Map<String, Parameter> operands, String name, String parameter) throws Exception
    {
        boolean expected = true;
        Parameter param = operands.get("value");
        if(param != null)
        {
            if(param.length() != 1) throw new ParameterException("value should have a size of one in test " + name);
            expected = Boolean.parseBoolean(param.get(0).toString());
        }
        if (runner.getParameterPool().exists(parameter) != expected)
        {
            throw new AssertException("Error in basic test, parameter " + parameter + " hasn't the expected existing status: " + expected);
        }
    }
}
