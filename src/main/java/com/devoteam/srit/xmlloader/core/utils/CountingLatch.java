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
