/*
 * ThreadPool.java
 *
 * Created on 26 octobre 2007, 14:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core;

import java.util.LinkedList;

/**
 *
 * @author gpasquiers
 */
public class ThreadPool
{
    private static LinkedList<ThreadRunner> threadsWaiting = new LinkedList<ThreadRunner>();
    
    public static void init(int size)
    {
        for(int i=0; i<size; i++)
        {
            ThreadRunner threadRunner = new ThreadRunner();
            threadRunner.start();
            threadsWaiting.addLast(threadRunner);
        }
    }
    
    public static synchronized ThreadRunner reserve()
    {
        if(!threadsWaiting.isEmpty())
        {
            return threadsWaiting.removeFirst();
        }
        else
        {
            ThreadRunner threadRunner = new ThreadRunner();
            threadRunner.start();
            return threadRunner;
        }
    }
    
    public static synchronized void release(ThreadRunner threadRunner)
    {
        threadsWaiting.addLast(threadRunner);
    }
}
