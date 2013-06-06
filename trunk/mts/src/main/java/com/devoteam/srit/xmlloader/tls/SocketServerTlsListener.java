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

package com.devoteam.srit.xmlloader.tls;

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
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
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

            String certificateAlgorithm = Config.getConfigByName("tls.properties").getString("cert.ALGORITHM");
            String certificateSSLVersion = Config.getConfigByName("tls.properties").getString("cert.SSL_VERSION");
            String certificateServerPath = Config.getConfigByName("tls.properties").getString("cert.SERVER.DIRECTORY");
            String certificateServerKeystorePassword = Config.getConfigByName("tls.properties").getString("cert.SERVER.KEYSTORE_PASSWORD");
            String certificateServerKeyPassword = Config.getConfigByName("tls.properties").getString("cert.SERVER.KEY_PASSWORD");
            String certificateTwoWay = Config.getConfigByName("tls.properties").getString("cert.TWO_WAY");
            
            char[] certificateKeystorePasswordArray;
            char[] certificateKeyPasswordArray;
            
            if (null == certificateServerKeyPassword || certificateServerKeyPassword.length() == 0)
            	certificateKeyPasswordArray = null;
            else
            	certificateKeyPasswordArray = certificateServerKeyPassword.toCharArray();
            
            if (null == certificateServerKeystorePassword || certificateServerKeystorePassword.length() == 0)
            	certificateKeystorePasswordArray = null;
            else
            	certificateKeystorePasswordArray = certificateServerKeystorePassword.toCharArray();

            KeyStore keyStore = KeyStore.getInstance(certificateAlgorithm);
            keyStore.load(new FileInputStream(certificateServerPath), certificateKeystorePasswordArray);
            
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, certificateKeyPasswordArray);
            
            KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
            
            System.setProperty("javax.net.ssl.trustStore", certificateServerPath);
    	    System.setProperty("javax.net.ssl.trustStorePassword", certificateServerKeystorePassword);
    	    
    	    SSLContext sslContext = SSLContext.getInstance(certificateSSLVersion);
    	    sslContext.init(keyManagers, null, null);
    	    
    	    SSLServerSocketFactory serverSocketFactory = sslContext.getServerSocketFactory();
    	    SSLServerSocket socket = (SSLServerSocket) serverSocketFactory.createServerSocket(port, 0, localInetAddr);
    	    
    	    socket.setNeedClientAuth(certificateTwoWay.equalsIgnoreCase("true"));
            
            
            this.serverSocket = socket;
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
