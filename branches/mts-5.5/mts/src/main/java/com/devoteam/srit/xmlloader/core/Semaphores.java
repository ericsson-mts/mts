/*
 * Semaphores.java
 *
 * Created on 8 juin 2007, 14:18
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
