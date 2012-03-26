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

package com.devoteam.srit.xmlloader.core.pluggable;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.basic.OperationParameter;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableParameterOperator;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableComponentRegistry;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author gpasquiers
 */
public class ParameterTestRegistry
{

    static private HashMap<String, PluggableName> pluggableNames;
    static private HashMap<String, PluggableParameterTest> pluggableComponents;

    static public void initialize()
    {
        ParameterTestRegistry.pluggableNames = new HashMap<String, PluggableName>();
        ParameterTestRegistry.pluggableComponents = new HashMap<String, PluggableParameterTest>();

        List<Class> classes = PluggableComponentRegistry.getPluggableComponents(PluggableParameterTest.class);

        for (Class aClass : classes)
        {
            try
            {
                PluggableParameterTest parameterOperator = (PluggableParameterTest) aClass.newInstance();
                for (PluggableName pluggableName : parameterOperator.getPluggableNames())
                {
                    String name = pluggableName.getName().toLowerCase();
                    if (pluggableNames.containsKey(pluggableName.getName()))
                    {
                        if (pluggableName.hasPriority(pluggableNames.get(pluggableName.getName())))
                        {
                            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PARAM, "overriden test ", pluggableName, " (", aClass.getCanonicalName(), ")");
                            pluggableNames.put(name, pluggableName);
                            pluggableComponents.put(name, parameterOperator);
                        }
                    }
                    else
                    {
                        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PARAM, "registered test ", pluggableName, " (", aClass.getCanonicalName(), ")");
                        pluggableNames.put(name, pluggableName);
                        pluggableComponents.put(name, parameterOperator);
                    }
                }
            }
            catch (Exception e)
            {
                GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PARAM, e, "could not instanciate operator class ", aClass);
            }
        }
    }

    static public PluggableParameterTest getPluggableComponent(String name)
    {
        return ParameterTestRegistry.pluggableComponents.get(name);
    }

    static public PluggableName getPluggableName(String name)
    {
        return ParameterTestRegistry.pluggableNames.get(name);
    }
}
