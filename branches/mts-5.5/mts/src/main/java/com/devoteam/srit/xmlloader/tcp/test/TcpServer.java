/*
 * TcpServer.java
 *
 * Created on 17 septembre 2007, 17:33
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
