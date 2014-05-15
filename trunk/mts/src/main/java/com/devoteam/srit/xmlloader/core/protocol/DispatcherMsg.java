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
package com.devoteam.srit.xmlloader.core.protocol;

import com.devoteam.srit.xmlloader.core.ScenarioRunner;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Dispatch the message from the Stack objects to the scenario according the scenarioName
 *
 *
 * @author fhenry
 */
public class DispatcherMsg {

    /**
     * list of the running scenario runner of the test
     */
    private static final Map<String, LinkedList<ScenarioRunner>> scenariosByRoutingName = new HashMap<String, LinkedList<ScenarioRunner>>();

    /**
     * constructor
     */
    public DispatcherMsg() throws Exception {
    }

    /**
     * Dispatch message to Msg.GetScenario() scenario
     */
    public static ScenarioRunner dispatchMsg(Msg msg) throws Exception {
        // get scenario's name computed into Stack because internal routing is protocol-specific
        LinkedList<String> scenarioName = msg.getScenarioName();
        LinkedList<ScenarioRunner> runnerList = null;
        for (int i = 0; (runnerList == null) && (i < scenarioName.size()); i++) {
            runnerList = scenariosByRoutingName.get(scenarioName.get(i));
        }
        if (runnerList == null) {
            runnerList = scenariosByRoutingName.get("default");
        }
        if (runnerList != null) {
            ScenarioRunner runner;
            synchronized (runnerList) {
                if (runnerList.isEmpty()) {
                    return null;
                }
                runner = runnerList.removeFirst();
                runnerList.addLast(runner);
            }
            GlobalLogger.instance().getApplicationLogger().info(Topic.PROTOCOL, "Routing: route the message by SCENARIO_ROUTING ", scenarioName, " to scenario : \"", runner.getName(), "\" (SCENARIO_ROUTING=", runner.getScenarioReference().getRoutingName(), ").");
            return (ScenarioRunner) runner;
        }
        else {
            GlobalLogger.instance().getApplicationLogger().warn(Topic.PROTOCOL, "Routing: could not route message by scenario routingName ", scenarioName, "\n", msg);
            return null;
        }
    }

    /**
     * Add a scenario to the list of scenario we have to dispatch messages to
     */
    public static void registerScenario(ScenarioRunner runner) {
    	if (runner.getScenarioState())
    	{
	        String scenarioRoutingName = runner.getScenarioReference().getRoutingName();
	        GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Routing: register scenario with routing=", scenarioRoutingName);
	        synchronized (scenariosByRoutingName) {
	            String[] names = scenarioRoutingName.split(",");
	            for (int i = 0; i < names.length; i++) {
	                LinkedList<ScenarioRunner> runnerList = scenariosByRoutingName.get(names[i]);
	                if (runnerList == null) {
	                    runnerList = new LinkedList<ScenarioRunner>();
	                    scenariosByRoutingName.put(names[i], runnerList);
	                }
	                runnerList.addLast(runner);
	            }
	        }
    	}
    }

    /**
     * Remove a scenario from the list of scenario we have to dispatch messages to
     */
    public static void unregisterScenario(ScenarioRunner runner) {
        String scenarioRoutingName = runner.getScenarioReference().getRoutingName();
        GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Routing: unregister scenario with routing=", scenarioRoutingName);
        synchronized (scenariosByRoutingName) {
            String[] names = scenarioRoutingName.split(",");
            for (int i = 0; i < names.length; i++) {
                LinkedList<ScenarioRunner> runnerList = scenariosByRoutingName.get(names[i]);
                if (runnerList != null) {
                    synchronized (runnerList) {
                        runnerList.remove(runner);

                        if (runnerList.isEmpty()) {
                            scenariosByRoutingName.remove(names[i]);
                        }
                    }
                }
            }
        }
    }
}
