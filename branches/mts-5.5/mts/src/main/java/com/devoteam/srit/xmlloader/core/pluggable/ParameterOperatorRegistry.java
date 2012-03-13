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
public class ParameterOperatorRegistry
{

    static private HashMap<String, PluggableName> pluggableNames;
    static private HashMap<String, PluggableParameterOperator> pluggableComponents;

    static public void initialize()
    {
        ParameterOperatorRegistry.pluggableNames = new HashMap<String, PluggableName>();
        ParameterOperatorRegistry.pluggableComponents = new HashMap<String, PluggableParameterOperator>();

        List<Class> classes = PluggableComponentRegistry.getPluggableComponents(PluggableParameterOperator.class);

        for (Class aClass : classes)
        {
            try
            {
                PluggableParameterOperator parameterOperator = (PluggableParameterOperator) aClass.newInstance();
                for (PluggableName pluggableName : parameterOperator.getPluggableNames())
                {
                    String name = pluggableName.getName().toLowerCase();
                    if (pluggableNames.containsKey(pluggableName.getName()))
                    {
                        if (pluggableName.hasPriority(pluggableNames.get(pluggableName.getName())))
                        {
                            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PARAM, "overriden operator ", pluggableName, " (", aClass.getCanonicalName(), ")");
                            pluggableNames.put(name, pluggableName);
                            pluggableComponents.put(name, parameterOperator);
                        }
                    }
                    else
                    {
                        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PARAM, "registered operator ", pluggableName, " (", aClass.getCanonicalName(), ")");
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

    static public PluggableParameterOperator getPluggableComponent(String name)
    {
        return ParameterOperatorRegistry.pluggableComponents.get(name);
    }

    static public PluggableName getPluggableName(String name)
    {
        return ParameterOperatorRegistry.pluggableNames.get(name);
    }
}
