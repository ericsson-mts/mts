/*
 * TcpClient.java
 *
 * Created on 17 septembre 2007, 17:33
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
