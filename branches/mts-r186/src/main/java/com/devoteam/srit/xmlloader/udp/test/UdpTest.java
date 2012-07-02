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

package com.devoteam.srit.xmlloader.udp.test;

/**
 *
 * @author nghezzaz
 */
public class UdpTest
{
    public static long   SERVER_PORT = 1111;
    public static String SERVER_HOST = "127.0.0.1";

    public static long   CLIENT_PORT = 2222;
    public static String CLIENT_HOST = "127.0.0.1";
    
    public static long   MSG_SIZE   = 1000;
    public static long   MSG_NUMBER = 2; //40000
    
    public static int total_sent = 0;
    public static int total_received = 0;
    public static long start = 0;
    public static long end = 0;
    
    public static void main(String[] args)
    {
        UdpServer udpServer = new UdpServer();
        udpServer.setDaemon(true);
        udpServer.start();
        System.out.println("UdpTest: server started");
        
        try
        {
            System.out.println("UdpTest: wait 1s");
            Thread.sleep(1000);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        UdpClient udpClient = new UdpClient();
        udpClient.start();
        
        System.out.println("UdpTest: client started");
		
		try {
			Thread.sleep(1000);	// 10000 wait the end of the threads
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		long duration = end-start;
		System.out.println("UdpTest: duration = " + duration +" ms");
		System.out.println("UdpTest: Nb requests = " + MSG_NUMBER);
		System.out.println( "UdpTest: total sent: " + total_sent);
		System.out.println( "UdpTest: total received: " + total_received);
		if (duration != 0)
		{	
			System.out.println("UdpTest: " + ((MSG_NUMBER*1000)/duration) + " requests per second");
			System.out.println("UdpTest: " + ((MSG_NUMBER * MSG_SIZE)*1000/1024/1024/duration) + " Mo per second");
		}
     
    }
}
