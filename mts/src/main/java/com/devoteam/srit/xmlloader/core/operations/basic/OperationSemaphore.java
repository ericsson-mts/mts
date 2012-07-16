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
package com.devoteam.srit.xmlloader.core.operations.basic;

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.ScenarioRunner;
import com.devoteam.srit.xmlloader.core.Semaphores;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.Operation;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.XMLElementDefaultParser;

import java.util.concurrent.TimeUnit;
import org.dom4j.Element;

/**
 * OperationSemaphore operation
 *
 *
 * @author JM. Auffret
 */
public class OperationSemaphore extends Operation {

    private int defaultTimeout;

    /**
     * Constructor
     *
     * @param name Name of the operation
     * @param action Type of cation to realize (create, delete, wait or notify)
     * @param timeout Timeout to wait
     */
    public OperationSemaphore(Element aRoot) {
        super(aRoot, XMLElementDefaultParser.instance());

        defaultTimeout = (int) (Config.getConfigByName("tester.properties").getDouble("operations.SEMAPHORE_TIMEOUT", 30) * 1000);
    }

    /**
     * Execute operation
     *
     *
     *
     * @param scenarioRunner Current scenarioRunner
     * @return Next operation or null by default
     * @throws ExecutionException
     */
    public Operation execute(Runner runner) throws Exception {
        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, this);

        String semaphoreName;
        String semaphoreAction;
        String semaphoreTimeoutStr;
        String semaphorePermitsStr;

        try {
            lockAndReplace(runner);
            GlobalLogger.instance().getSessionLogger().debug(runner, TextEvent.Topic.CORE, "Operation after pre-parsing \n", this);
            semaphoreName = getAttribute("name");
            semaphoreAction = getAttribute("action");
            semaphoreTimeoutStr = getAttribute("timeout");
            semaphorePermitsStr = getAttribute("permits");
        }
        finally {
            unlockAndRestore();
        }

        GlobalLogger.instance().logDeprecatedMessage(
                "semaphore name=\""
                + semaphoreName
                + "\" action=\""
                + semaphoreAction
                + "\"/",
                "parameter name=\"[testcase:"
                + semaphoreName
                + "]\" operation=\"system.semaphore"
                + semaphoreAction
                + "\"/");

        //
        // Get the timeout value:
        //
        int semaphoreTimeout;
        if (null == semaphoreTimeoutStr) {
            semaphoreTimeout = defaultTimeout;
        }
        else {
            semaphoreTimeout = (int) (Float.parseFloat(semaphoreTimeoutStr) * 1000);
        }

        //
        // Get the permits value:
        //
        int semaphorePermits;
        if (null == semaphorePermitsStr) {
            semaphorePermits = 1;
        }
        else {
            semaphorePermits = (int) (Float.parseFloat(semaphorePermitsStr) * 1000);
        }

        //
        // Get the Semaphores pool
        //
        Semaphores semaphores = ((ScenarioRunner) runner).getParent().getSemaphores();
        if (semaphoreAction.equals("wait")) {
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "Wait semaphore ", semaphoreName, " for ", semaphorePermits, " permits");
            semaphores.tryAcquire(semaphoreName, semaphorePermits, semaphoreTimeout, TimeUnit.MILLISECONDS);
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "Semaphore ", semaphoreName, " unlocked");
        }
        else if (semaphoreAction.equals("notify")) {
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "Notify semaphore ", semaphoreName, " for ", semaphorePermits, " permits");
            semaphores.release(semaphoreName, semaphorePermits);
        }

        return null;
    }
}
