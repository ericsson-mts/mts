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

package com.devoteam.srit.xmlloader.core.test;

/** mesure de perf de la pile Diameter 
* sur ma machine : Pentium 4 hyperthreading 2,6 Gb 1 Gb RAM
* 10000 tests prennent 11 secondes soit environ 86 tests /s 
* chaque test corespond à 2 transactions
* => 173 transactions/s  
*/


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.devoteam.srit.xmlloader.core.Tester;
import com.devoteam.srit.xmlloader.core.log.GenericLogger;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.utils.filesystem.LocalFSInterface;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;

public class ThreadTest 
{
    
    private static GenericLogger logger;
          
    private static Tester tester;  
            
    /** list of listenpoint object**/
    private static Map<String, Listenpoint> listenpoints = Collections.synchronizedMap(new HashMap());
    
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception 
    {
    	int max_thread = 20000;
    	if (args.length > 0)
    	{
    		max_thread = Integer.parseInt(args[0]);
    	}
        
        for (int i = 0; i< max_thread; i++)
        {
        	PrimeThread p = new PrimeThread(10000);
        	p.start();
        }
    }
   
}

class PrimeThread extends Thread 
{
    static int number = 0;
    int duration;
    
    PrimeThread(int duration) 
    {
        this.duration = duration;
    }

    public void run() 
    {
    	this.number ++;
    	if ((number % 1000) == 0)
    	{
    		System.out.println(number);
    	}

    	try
    	{
        	Thread.sleep(duration); 	
    	}
    	catch (Exception e)
    	{
    		// nothnig to do
    	}
    	this.number --;
    	if ((number % 1000) == 0)
    	{
    		System.out.println(number);
    	}
    }
}
