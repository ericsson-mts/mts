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
