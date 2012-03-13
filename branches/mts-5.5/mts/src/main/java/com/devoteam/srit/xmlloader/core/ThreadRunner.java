/*
 * ThreadRunner.java
 *
 * Created on 26 octobre 2007, 14:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core;

import java.util.concurrent.Semaphore;

/**
 * TODO: simply use LockSupport instead of Semaphore in Java 1.6
 * @author gpasquiers
 */
public class ThreadRunner extends Thread
{
    private Runnable runnable;
    private Semaphore semaphore = new Semaphore(0);

    /** Creates a new instance of ThreadRunner */
    public ThreadRunner()
    {
        super();
        this.setDaemon(true);
    }

    public ThreadRunner start(Runnable runnable)
    {
        this.runnable = runnable;
        this.semaphore.release();
        return this;
    }

    @Override
    public void run()
    {
        while(true)
        {
            try
            {
                this.semaphore.acquireUninterruptibly();
                this.runnable.run();
            }
            catch(Throwable t)
            {
                System.err.println(this);
                t.printStackTrace();
            }
            finally
            {
                this.runnable = null;
                ThreadPool.release(this);
            }
        }
    }
}
