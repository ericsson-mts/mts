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
import java.net.Socket;

/**
 *
 * @author gpasquiers
 */
public class TcpClient extends Thread
{
    
    public void run()
    {
        try
        {
            Socket socket = new Socket(TcpTest.SERVER_HOST,(int) TcpTest.SERVER_PORT);
            System.out.println("TcpClient: client connected");
            
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            
            byte[] data = new byte[(int)TcpTest.MSG_SIZE];
            
            for(int i=0; i<data.length; i++)
            {
                data[i] = 0;
            }
            
            System.out.println("TcpClient: data initialized");

            long start = System.currentTimeMillis();
            
            for(int i=0; i<TcpTest.MSG_NUMBER; i++)
            {
                outputStream.write(data);
                outputStream.flush();
                
                int len = inputStream.read(data);
                
                if(len != TcpTest.MSG_SIZE)
                {
                    System.out.println(len + "!=" + TcpTest.MSG_SIZE);
                }
                
            }
            
            long end = System.currentTimeMillis();
            
            System.out.println("TcpClient: duration=" + (end-start) +"ms");
            System.out.println("TcpClient: requests=" + TcpTest.MSG_NUMBER);
            System.out.println("TcpClient: " + ((TcpTest.MSG_NUMBER*1000)/(end-start)) + " requests per second");
            System.out.println("TcpClient: " + ((TcpTest.MSG_NUMBER * TcpTest.MSG_SIZE)*1000/1024/1024/(end-start)) + " mo per second");
            
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        
    }
}
