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

package com.devoteam.srit.xmlloader.tcp.test;

/**
 *
 * @author gpasquiers
 */
public class TcpTest
{
    public static long   SERVER_PORT = 12345;
    public static String SERVER_HOST = "127.0.0.1";

    public static long   MSG_SIZE   = 10000;
    public static long   MSG_NUMBER = 100000;
    
    public static void main(String[] args)
    {
        TcpServer tcpServer;
        tcpServer = new TcpServer();
        tcpServer.setDaemon(true);
        tcpServer.start();
        System.out.println("TcpTest: server started");
        
        try
        {
            System.out.println("TcpTest: wait 1s");
            Thread.sleep(1000);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        TcpClient tcpClient;
        tcpClient = new TcpClient();
        tcpClient.start();
        
        
        System.out.println("TcpTest: client started");
    }
}
