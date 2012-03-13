/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.utils;

import java.util.concurrent.CountDownLatch;

/**
 *
 * @author gpasquiers
 */
public class CountingLatch
{
    private int counter;

    private CountDownLatch latch;

    public CountingLatch(int init)
    {
        counter = init;
        latch = null;
    }

    public synchronized int value()
    {
        return counter;
    }

    public synchronized void up(int value)
    {
        counter += value;
    }

    public synchronized void down(int value )
    {
        counter -= value;
        if(counter <= 0 && null != latch) latch.countDown();
    }

    public void waitforZero() throws InterruptedException
    {
        synchronized(this)
        {
            if(counter == 0)
            {
                return;
            }
            else
            {
                latch = new CountDownLatch(1);
            }
        }

        latch.await();
    }

}
