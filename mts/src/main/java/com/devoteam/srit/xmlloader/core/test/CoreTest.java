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


import com.devoteam.srit.xmlloader.core.utils.Utils;
import java.net.InetAddress;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import gov.nist.javax.sip.message.SIPRequest;

import javax.sip.SipFactory;
import javax.sip.message.Message;
import javax.sip.message.MessageFactory;

import com.devoteam.srit.xmlloader.core.Tester;
import com.devoteam.srit.xmlloader.core.log.GenericLogger;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.filesystem.LocalFSInterface;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import com.devoteam.srit.xmlloader.udp.ListenpointUdp;
import com.devoteam.srit.xmlloader.udp.StackUdp;


public class CoreTest {
    
    private static GenericLogger logger;
          
    private static Tester tester;  
            
    /** list of listenpoint object**/
    private static Map<String, Listenpoint> listenpoints = Collections.synchronizedMap(new HashMap());
    
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        
        /*
         * Set the FSInterface to LocalFS.
         */
        SingletonFSInterface.setInstance(new LocalFSInterface());
    	    	
        if (tester == null) {
            tester = Tester.buildInstance();
        }
        logger = GlobalLogger.instance().getApplicationLogger();
    	               
        putHashmap();
        getHashmap();
    }
    

    /**
     * 
     */
    public static void getHashmap() throws Exception {
		long maxIter = 50000;
		logger.debug(TextEvent.Topic.PROTOCOL, "maxIter : " + maxIter);
		long beginTT = new GregorianCalendar().getTimeInMillis();
		
		for (int i = 1; i <= maxIter; i++) {
		    // Create a SIP message object corresponding to the string message
		    if (i % 1000 == 0) {
		        System.out.println("i=" + i);
		    }
		    Listenpoint lp = listenpoints.get("name" + i);
		}
		long endTT = new GregorianCalendar().getTimeInMillis();
		float duration = ((float)(endTT - beginTT)) / 1000;
		float flow = maxIter / duration;
		System.out.println("duration = " + duration + " s.");
		System.out.println("trans flow = " + flow + " trans/s.");

    }

    /**
     * 
     */
    public static void putHashmap() throws Exception {
        Stack stack = new StackUdp();
        
		long maxIter = 50000;
		logger.debug(TextEvent.Topic.PROTOCOL, "maxIter : " + maxIter);
		long beginTT = new GregorianCalendar().getTimeInMillis();
		
		// Listenpoint lp = new ListenpointUdp(stack);
		for (int i = 1; i <= maxIter; i++) {
		    // Create a SIP message object corresponding to the string message
		    if (i % 1000 == 0) {
		        System.out.println("i=" + i);
		    }
		    int port = 11000 + i;
	        Listenpoint lp = new ListenpointUdp(stack, "name" + i, "172.16.21.32", port);
		    listenpoints.put(lp.getName(), lp);
	        lp.create(StackFactory.PROTOCOL_SIP);
		}
		long endTT = new GregorianCalendar().getTimeInMillis();
		float duration = ((float)(endTT - beginTT)) / 1000;
		float flow = maxIter / duration;
		System.out.println("duration = " + duration + " s.");
		System.out.println("trans flow = " + flow + " trans/s.");

    }
    
   
}
