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

package com.devoteam.srit.xmlloader.core;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;

import java.util.HashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author gpasquiers
 */
public class Semaphores
{
    private HashMap<String, Semaphore> semaphores;
    
    /** Creates a new instance of Semaphores */
    public Semaphores()
    {
        semaphores = new HashMap<String, Semaphore>();
    }
    
    public void reset()
    {
        semaphores.clear();
    }
    
    public void tryAcquire(String name, int permits, long timeout, TimeUnit timeUnit) throws ExecutionException
    {
        //
        // Get or create the sempahore
        //
        Semaphore semaphore = semaphores.get(name);
        if(null == semaphore)
        {
            semaphore = new Semaphore(0);
            semaphores.put(name, semaphore);
        }
        
        //
        // tryAcquire (with timeout) "permits" permits
        //
        try
        {
            synchronized(semaphore)
            {
                boolean success ;
                
                if(0 >= timeout)
                {
                    semaphore.acquire(permits);
                    success = true;
                }
                else
                {
                    success = semaphore.tryAcquire(permits, timeout, timeUnit);
                }
            
                //
                // Throw an exception because we timeouted
                //
                if(false == success)
                {
                    throw new ExecutionException("Timeout in semaphore " + name);
                }
            }
        }
        catch(InterruptedException e)
        {
            //
            // ScenarioRunner has probably been interrupted (stop button)
            //
            throw new ExecutionException(e);
        }
    }
     
    public void release(String name, int permits)
    {
        //
        // Get or create the sempahore
        //
        Semaphore semaphore = semaphores.get(name);
        if(null == semaphore)
        {
            semaphore = new Semaphore(0);
            semaphores.put(name, semaphore);
        }

        //
        // Give "permits" permits to the semaphore
        //
        semaphore.release(permits);
    }
    
}
