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
public class OperationSemaphore extends Operation
{

    private int defaultTimeout;

    /**
     * Constructor
     *
     * @param name Name of the operation
     * @param action Type of cation to realize (create, delete, wait or notify)
     * @param timeout Timeout to wait
     */
    public OperationSemaphore(Element aRoot)
    {
        super(aRoot);

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
    public Operation execute(Runner runner) throws Exception
    {
        restore();

        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, this);

        // Replace elements in XMLTree
        replace(runner, new XMLElementDefaultParser(runner.getParameterPool()), TextEvent.Topic.CORE);

        String semaphoreName       = getAttribute("name");
        String semaphoreAction     = getAttribute("action");
        String semaphoreTimeoutStr = getAttribute("timeout");
        String semaphorePermitsStr = getAttribute("permits");

        GlobalLogger.instance().logDeprecatedMessage(
    			"semaphore name=\"" +
    			semaphoreName +
    			"\" action=\"" +
    			semaphoreAction +
    			"\"/",
    			"parameter name=\"[testcase:" +
    			semaphoreName +
    			"]\" operation=\"system.semaphore" +
    			semaphoreAction +
    			"\"/"
        		);

        //
        // Get the timeout value:
        //
        int semaphoreTimeout ;
        if(null == semaphoreTimeoutStr)
        {
            semaphoreTimeout = defaultTimeout;
        }
        else
        {
            semaphoreTimeout = (int) (Float.parseFloat(semaphoreTimeoutStr) * 1000);
        }

        //
        // Get the permits value:
        //
        int  semaphorePermits;
        if(null == semaphorePermitsStr)
        {
            semaphorePermits = 1;
        }
        else
        {
            semaphorePermits = (int) (Float.parseFloat(semaphorePermitsStr) * 1000);
        }

        //
        // Get the Semaphores pool
        //
        Semaphores semaphores = ((ScenarioRunner)runner).getParent().getSemaphores();
        if (semaphoreAction.equals("wait"))
        {
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "Wait semaphore ", semaphoreName , " for ", semaphorePermits, " permits");
            semaphores.tryAcquire(semaphoreName, semaphorePermits, semaphoreTimeout, TimeUnit.MILLISECONDS);
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "Semaphore ", semaphoreName, " unlocked");
        }
        else if (semaphoreAction.equals("notify"))
        {
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "Notify semaphore ", semaphoreName , " for ", semaphorePermits, " permits");
            semaphores.release(semaphoreName, semaphorePermits);
        }

        return null;
    }
}
