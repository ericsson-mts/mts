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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author gpasquiers
 */
public class TcpServer extends Thread
{
    public void run()
    {
        try
        {
            InetSocketAddress inetSocketAddress = new InetSocketAddress((int)TcpTest.SERVER_PORT);
            
            
            ServerSocket serverSocket;
            serverSocket = new ServerSocket();
            serverSocket.bind(inetSocketAddress);
        
            System.out.println("TcpServer: server waiting");
            while(true)
            {
                Socket socket = serverSocket.accept();
                TcpServerThread tcpServerThread = new TcpServerThread(socket);
                tcpServerThread.start();
            }
        
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    // <editor-fold desc=" TcpServerThread inner Class ">
    private class TcpServerThread extends Thread
    {
        private Socket socket;
        
        private TcpServerThread(Socket aSocket)
        {
            super();
            socket = aSocket;
        }
        
        public void run()
        {
            try
            {
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();

                byte[] data = new byte[(int)TcpTest.MSG_SIZE];
                
                while(true)
                {
                    int len = inputStream.read(data);
                    
                    outputStream.write(data, 0, len);
                    outputStream.flush();
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    // </editor-fold>
    
}
