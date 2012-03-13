/*
 * TcpTest.java
 *
 * Created on 17 septembre 2007, 17:31
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
