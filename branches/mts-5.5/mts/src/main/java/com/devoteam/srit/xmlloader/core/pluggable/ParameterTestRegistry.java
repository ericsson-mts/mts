/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
