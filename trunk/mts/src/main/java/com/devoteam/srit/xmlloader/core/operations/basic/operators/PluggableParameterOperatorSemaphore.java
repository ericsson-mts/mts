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
import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;
import com.devoteam.srit.xmlloader.core.utils.Config;

import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 *
 * @author gpasquiers
 */
public class PluggableParameterOperatorSemaphore extends AbstractPluggableParameterOperator
{

    final private String NAME_NOTIFY_OLD = "semaphore.notify";
    final private String NAME_WAIT_OLD = "semaphore.wait";

    final private String NAME_NOTIFY = "system.semaphorenotify";
    final private String NAME_WAIT = "system.semaphorewait";

    final private static Semaphore accessMutex = new Semaphore(1);

    public PluggableParameterOperatorSemaphore()
    {
        this.addPluggableName(new PluggableName(NAME_NOTIFY_OLD, NAME_NOTIFY));
        this.addPluggableName(new PluggableName(NAME_WAIT_OLD, NAME_WAIT));
        this.addPluggableName(new PluggableName(NAME_NOTIFY));
        this.addPluggableName(new PluggableName(NAME_WAIT));
    }

    @Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws ParameterException
    {
        try
        {
            Parameter param1 = operands.get("value");
            Parameter param2 = operands.get("value2");

            if (null != param1 && param1.length() != 1)
            {
                throw new ParameterException("all semaphore operands should be of size 1");
            }
            if (null != param2 && param2.length() != 1)
            {
                throw new ParameterException("all semaphore operands should be of size 1");
            }
            int permits = 1;
            long timeout = (long) Config.getConfigByName("tester.properties").getDouble("operations.SEMAPHORE_TIMEOUT", 30);

            if (null != param1)
            {
                permits = Integer.valueOf(param1.get(0).toString());
            }
            if (null != param2)
            {
                timeout = Long.valueOf(param2.get(0).toString());
            }
            Semaphore semaphore;
            
            if(ParameterPool.hasIndex(resultant))
            {
                throw new ParameterException("content of name is not allowed to have an index in semaphore operator");
            }
            
            /*
             * Here we go and get the semaphore. Only one thread can be in this zone
             * Creation AND registration of the semaphore might happen here too.
             */
            try
            {
                PluggableParameterOperatorSemaphore.accessMutex.acquire(1);
                if (runner.getParameterPool().exists(resultant))
                {
                    Parameter semaphoreParam = runner.getParameterPool().get(resultant);
                    if (null != semaphoreParam && semaphoreParam.length() != 1)
                    {
                    	//PluggableParameterOperatorSemaphore.accessMutex.release();
                        throw new ParameterException("all semaphore operands should be of size 1");
                    }

                    if (semaphoreParam.get(0) instanceof Semaphore)
                    {
                        semaphore = (Semaphore) semaphoreParam.get(0);
                        //GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PARAM, "name=" + name + " old Semaphore = " + semaphore + resultant);
                    }
                    else
                    {
                    	//PluggableParameterOperatorSemaphore.accessMutex.release();
                        throw new ParameterException("content of name is not a semaphore !");
                    }
                }
                else
                {
                    semaphore = new Semaphore(0);
                    //GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PARAM, "name=" + name + " new Semaphore = " + semaphore + resultant);
                    Parameter result = new Parameter();
                    result.add(semaphore);
                    runner.getParameterPool().set(resultant, result);
                    //PluggableParameterOperatorSemaphore.accessMutex.release();
                }
            }
            finally
            {
                PluggableParameterOperatorSemaphore.accessMutex.release();
            }


            if (name.equalsIgnoreCase(NAME_WAIT_OLD) || name.equalsIgnoreCase(NAME_WAIT))
            {
                if(0 == timeout)
                {
                	//GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PARAM, "name=" + name + " acquire BEGIN Semaphore = " + semaphore + resultant);
                    semaphore.acquire(permits);
                    //GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PARAM, "name=" + name + " acquire END Semaphore = " + semaphore + resultant);
                }
                else
                {
                	//GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PARAM, "name=" + name + " tryAcquire BEGIN Semaphore = " + semaphore + resultant);
                	boolean result = semaphore.tryAcquire(permits, timeout, TimeUnit.SECONDS);                	
                	//GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PARAM, "name=" + name + " tryAcquire END Semaphore = " + semaphore + resultant);
                	if (!result)
                	{
                        throw new ParameterException("Error timeout in semaphore parameter operation");                		
                	}                	
                }
            }
            else if (name.equalsIgnoreCase(NAME_NOTIFY_OLD) || name.equalsIgnoreCase(NAME_NOTIFY))
            {
                semaphore.release(permits);
                //GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PARAM, "name=" + name + " release Semaphore = " + semaphore + resultant);
            }
            else
            {
                throw new RuntimeException("unsupported operation " + name);
            }
            return null;
        }
        catch (ParameterException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ParameterException("Error in semaphore parameter operation", e);
        }
    }
}
