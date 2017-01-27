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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author emicpou
 *
 */
public class ServerSunNioSctp extends Thread
{
	public static class Config{
		protected int port = 4242;
		protected int msgSize = 42;
	}
	
	protected Config config;
	
	public ServerSunNioSctp(Config config)
	{
		this.config = config;
	}
	
    public void run()
    {
    	ServerSocket serverSocket = null;
        try
        {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(this.config.port);
            
            
            serverSocket = new ServerSocket();
            serverSocket.bind(inetSocketAddress);
        
            System.out.println("TcpServer: server waiting");
            while(true)
            {
                Socket socket = serverSocket.accept();
                SocketThread socketThread = new SocketThread(socket);
                socketThread.start();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        if( serverSocket!=null )
        {
        	try
            {
        		serverSocket.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    private class SocketThread extends Thread
    {
        private Socket socket;
        
        public SocketThread(Socket socket)
        {
            super();
            this.socket = socket;
        }
        
        public void run()
        {
            try
            {
                InputStream inputStream = this.socket.getInputStream();
                OutputStream outputStream = this.socket.getOutputStream();

                byte[] data = new byte[ServerSunNioSctp.this.config.msgSize];
                
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
