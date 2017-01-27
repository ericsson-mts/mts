/* 
 * Copyright 2017 Ericsson http://www.ericsson.com
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

package com.devoteam.srit.xmlloader.sctp.sunnio.test;

/**
 * @author emicpou
 *
 */
public class TestSunNioSctp
{
    public static String SERVER_HOST = "127.0.0.1";
    public static int SERVER_PORT = 12345;

    public static int   MSG_SIZE   = 10000;
    public static int   MSG_NUMBER = 100000;
    
    public static void main(String[] args)
    {
    	ServerSunNioSctp.Config serverConfig = new ServerSunNioSctp.Config();
    	serverConfig.port = SERVER_PORT;
    	serverConfig.msgSize = MSG_SIZE;
    	
        ServerSunNioSctp server = new ServerSunNioSctp( serverConfig );
        server.setDaemon(true);
        server.start();
        System.out.println("TestSunNioSctp: server started");
        
        try
        {
            System.out.println("TestSunNioSctp: wait 1s");
            Thread.sleep(1000);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        ClientSunNioSctp.Config clientConfig = new ClientSunNioSctp.Config();
        clientConfig.serverHost = SERVER_HOST;
        clientConfig.serverPort = SERVER_PORT;
        clientConfig.msgSize = MSG_SIZE;
        clientConfig.msgNumber = MSG_NUMBER;
        
    	ClientSunNioSctp client = new ClientSunNioSctp( clientConfig );
        client.start();
        
        System.out.println("TestSunNioSctp: client started");
    }
}
