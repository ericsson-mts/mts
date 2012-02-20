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
*/package com.devoteam.srit.xmlloader.tls;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.tcp.StackTcp;

import javax.net.ssl.X509TrustManager;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyStore;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 *
 * @author fvandecasteele
 */
public class SocketServerTlsListener extends Thread
{

    private ServerSocket serverSocket;
    private ListenpointTls listenpoint;

    /**
     * Creates a new instance of SocketServerTlsListener
     */
    public SocketServerTlsListener(ListenpointTls listenpoint) throws ExecutionException
    {
        int port = 0;
        InetAddress localInetAddr = null;

        // Set up Tls connection
        try
        {
            port = listenpoint.getPort();
            localInetAddr = InetAddress.getByName(listenpoint.getHost());

            /*SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket plop = (SSLServerSocket) factory.createServerSocket(port, 0, localInetAddr);*/
//
//            String certificatePath       = Config.getConfigByName("tls.properties").getString("cert.DIRECTORY");
//            String certificateAlgorithm  = Config.getConfigByName("tls.properties").getString("cert.ALGORITHM");
//            String certificatePassword   = Config.getConfigByName("tls.properties").getString("cert.PASSWORD");
//            String certificateSSLVersion = Config.getConfigByName("tls.properties").getString("cert.SSL_VERSION");
//
//            char[] certificatePasswordArray ;
//
//            //
//            // If password is an empty string (allowed) or not defined (allowed), do not use a password
//            //
//            if(null == certificatePassword || certificatePassword.length() == 0)
//            {
//                certificatePasswordArray = null;
//            }
//            else
//            {
//                certificatePasswordArray = certificatePassword.toCharArray();
//            }
//
//            KeyStore keyStore = KeyStore.getInstance("JKS");
//            keyStore.load(new FileInputStream(certificatePath), certificatePasswordArray);
//
//            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(certificateAlgorithm);
//            keyManagerFactory.init(keyStore, certificatePasswordArray);
//
//            KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
//
//            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(certificateAlgorithm);
//            trustManagerFactory.init(keyStore);
//
//            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
//
//            SSLContext sslContext = SSLContext.getInstance(certificateSSLVersion);
//            sslContext.init(keyManagers, trustManagers, null);
//

            // Create a trust manager that validates all certificates
            TrustManager[] trustAllCerts = new TrustManager[]
            {
                new X509TrustManager()
                {

                    public java.security.cert.X509Certificate[] getAcceptedIssuers()
                    {
                        return null;
                    }

                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
                    {
                        //No need to implement.
                    }

                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
                    {
                        //No need to implement.
                    }
                }
            };

            String certificatePath = Config.getConfigByName("tls.properties").getString("cert.DIRECTORY");
            String certificateAlgorithm = Config.getConfigByName("tls.properties").getString("cert.ALGORITHM");
            String certificatePassword = Config.getConfigByName("tls.properties").getString("cert.PASSWORD");
            String certificateSSLVersion = Config.getConfigByName("tls.properties").getString("cert.SSL_VERSION");

            char[] certificatePasswordArray;

            //
            // If password is an empty string (allowed) or not defined (allowed), do not use a password
            //
            if (null == certificatePassword || certificatePassword.length() == 0)
            {
                certificatePasswordArray = null;
            }
            else
            {
                certificatePasswordArray = certificatePassword.toCharArray();
            }

            KeyStore keyStore = KeyStore.getInstance(certificateAlgorithm);
            keyStore.load(new FileInputStream(certificatePath), certificatePasswordArray);

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, certificatePasswordArray);

            KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);


            SSLContext sslContext = SSLContext.getInstance(certificateSSLVersion);
            sslContext.init(keyManagers, trustAllCerts, null);

            this.serverSocket = sslContext.getServerSocketFactory().createServerSocket(port, 0, localInetAddr);
            
            /*this.serverSocket = plop;*/
            this.listenpoint = listenpoint;
        }
        catch (Exception e)
        {
            throw new ExecutionException("Can't instantiate the Tls SocketServerTlsListener on " + localInetAddr + ":" + port, e);
        }
    }

    public void run()
    {

        Stack stack = null;
        try
        {
            stack = StackFactory.getStack(listenpoint.getProtocol());
        }
        catch (Exception e)
        {
            return;
        }

        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "SocketServerTlsListener started");

        boolean exception = false;
        while (!exception)
        {
            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "SocketServerTlsListener waiting for a connection on socket");

            try
            {
                Socket socket = serverSocket.accept();
                if (listenpoint != null)
                {
                    Channel channel = stack.buildChannelFromSocket(listenpoint, socket);
                    listenpoint.openChannel(channel);
                    
					// Create an empty message for transport connection actions (open or close) 
					// and on server side and dispatch it to the generic stack 
					((StackTls) StackFactory.getStack(StackFactory.PROTOCOL_TLS)).receiveTransportMessage("SYN-ACK", channel, listenpoint);
                }
            }
            catch (SocketException e)
            {
                if (e.getMessage().contains("closed"))
                {
                    exception = true;
                }
                else
                {
                    GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, e, "Exception in SocketServerTlsListener");
                }
            }
            catch (Exception e)
            {
                GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, e, "Exception in SocketServerTlsListener");
            }

            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "SocketServerTlsListener got a connection");
        }

        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "SocketServerTlsListener stopped");
    }

    public void close()
    {
        try
        {
            serverSocket.close();
        }
        catch (Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Error while closing Tls listener's socket");
        }
    }

}
