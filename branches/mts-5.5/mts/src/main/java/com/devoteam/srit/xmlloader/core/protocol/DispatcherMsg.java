/*
 * Stack.java
 *
 * Created on 11 avril 2007, 16:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.core.protocol;

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.ScenarioRunner;


import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Dispatch the message from the Stack objects to the scenario according the scenarioName  
 *
 *
 * @author fhenry
 */
public class DispatcherMsg
{

    /** list of the running scenario runner of the test */
    private static Map<String, LinkedList<Runner>> scenarios = new HashMap<String, LinkedList<Runner>>();

    /** constructor */
    public DispatcherMsg() throws Exception
    {
    }

    /** Dispatch message to Msg.GetScenario() scenario */
    public static ScenarioRunner dispatchMsg(Msg msg) throws Exception
    {
        // get scenario's name computed into Stack because internal routing is protocol-specific
        LinkedList<String> scenarioName = msg.getScenarioName();
        LinkedList<Runner> runnerList = null;
        for(int i = 0; (runnerList == null) && (i < scenarioName.size()); i++)
        {
            runnerList = scenarios.get(scenarioName.get(i));
        }
        if (runnerList == null)
        {
            runnerList = scenarios.get("default");        	
        }
        if (runnerList != null)
        {
            GlobalLogger.instance().getApplicationLogger().info(Topic.PROTOCOL, "Routing: routing message by scenarioName (", scenarioName, ")");
            Runner runner;
            synchronized(runnerList)
            {
                if(runnerList.isEmpty())
                {
                    return null;
                }
                runner = runnerList.removeFirst();
                runnerList.addLast(runner);
            }
            return (ScenarioRunner) runner;
        }
        else
        {
            GlobalLogger.instance().getApplicationLogger().warn(Topic.PROTOCOL, "Routing: could not route message by scenarioName ", scenarioName, "\n", msg);
            return null;
        }
    }

    /** Add a scenario to the list of scenario we have to dispatch messages to */
    public static void registerScenario(Runner runner)
    {
        String scenarioName = runner.getName();
        GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Routing: register scenarioName ", scenarioName);
        synchronized(scenarios)
        {
        	String[] names = scenarioName.split(",");
        	for (int i =0; i < names.length; i++)
        	{
	            LinkedList<Runner> runnerList = scenarios.get(names[i]);
	            if (runnerList == null)
	            {
	                runnerList = new LinkedList<Runner>();
	                scenarios.put(names[i], runnerList);
	            }
	            runnerList.addLast(runner);
        	}
        }
    }

    /** Remove a scenario from the list of scenario we have to dispatch messages to */
    public static void unregisterScenario(Runner runner)
    {
        String scenarioName = runner.getName();
        GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Routing: unregister scenarioName ", scenarioName);
        synchronized(scenarios)
        {
        	String[] names = scenarioName.split(",");
        	for (int i =0; i < names.length; i++)
        	{
	            LinkedList<Runner> runnerList = scenarios.get(names[i]);
	            if (runnerList != null)
	            {
	                synchronized(runnerList)
	                {
	                    runnerList.remove(runner);
	                    
	                    if(runnerList.isEmpty())
	                    {
	                        scenarios.remove(names[i]);
	                    }
	                }
	            }
        	}
        }
    }
}