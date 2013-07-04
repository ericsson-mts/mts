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

package com.devoteam.srit.xmlloader.tcp.bio;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.rtp.MsgRtp;
import com.devoteam.srit.xmlloader.tcp.StackTcp;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 *
 * @author sngom
 */
public class SocketTcpBIO extends Thread
{
    private Socket socket;
    private SSLSocket sslSocket;
    
    private InputStream  inputStream;

    private OutputStream outputStream;
    
    private ChannelTcpBIO channel;
    
    private boolean startTlsReceived = false;
    private boolean startTlsAnswerReceived = false;
    
    /** Creates a new instance of SocketClientReceiver */
    public SocketTcpBIO(Socket socket) throws Exception
    {
        this.socket = socket;
        this.sslSocket = null;
        this.inputStream = new BufferedInputStream(socket.getInputStream());
        this.outputStream = socket.getOutputStream();
    }    
    
    public void run() 
    {
		Stack stack = null; 
		try
		{
			stack = StackFactory.getStack(channel.getProtocol());
		}
		catch (Exception e)
		{
			return;
		}

        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "SocketTcp listener thread started : ", channel);
        
		boolean exception = false;    	
        while (!exception)
        {
            try
            {
            	if (!startTlsReceived && !startTlsAnswerReceived)
            	{
			    	Msg msg = stack.readFromStream(inputStream, stack.getChannel(channel.getName()));
			    	if (msg != null) 
			    	{
	                    if(msg.getChannel() == null)
	                    {
	                        msg.setChannel(channel);
	                    }
	                    if(msg.getListenpoint() == null)
	                    {
	                        msg.setListenpoint(channel.getListenpointTcp());
	                    }
	                    if (msg.isSTARTTLS_request())
	                    	startTlsReceived = true;
	                    if (msg.isSTARTTLS_answer())
	                    	startTlsAnswerReceived = true;
			    		stack.getChannel(channel.getName()).receiveMessage(msg);
			    	}
            	}
            	else
            	{
            		/**
            		 * STARTTLS management --> CLIENT MODE
            		 */
            		if (startTlsAnswerReceived)
            		{
            			setupSSLSocket(true);
	            		this.sslSocket.startHandshake();
	            		GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "TLS: Client handshake successfull for channel: ", channel);
	            		this.inputStream = new BufferedInputStream(sslSocket.getInputStream());
	            		this.outputStream = this.sslSocket.getOutputStream();
	            		startTlsAnswerReceived = false;
            		}
            	}
    		}
	        catch(SocketException e)
	        {
                exception = true;
	        }
            catch(Exception e)
            {
				if ((null != e.getMessage()) && (e.getMessage().equalsIgnoreCase("End of stream detected") || e.getMessage().equalsIgnoreCase("EOFException")))
				{
					exception = true;
					
					try
					{
						// Create an empty message for transport connection actions (open or close) 
						// and on server side and dispatch it to the generic stack
						((StackTcp) StackFactory.getStack(StackFactory.PROTOCOL_TCP)).receiveTransportMessage("FIN-ACK", channel, null);
					}
					catch (Exception ex)
					{
						GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, ex, "Exception : SocketTcp thread", channel);
					}
				}
				else
				{
					GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Exception : SocketTcp thread", channel);
				}
            }
    	}
        
        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "SocketTcp listener thread stopped : ", channel);

        try
        {
            synchronized (this)
            {
                if (null != socket)
                {
                    StackFactory.getStack(channel.getProtocol()).closeChannel(channel.getName());
                }
            }
        }
        catch (Exception e)
        {
        	// nothing to do
        }
    }
    
    public void setChannelTcp(ChannelTcpBIO channel)
    {
        this.channel = channel;
    }

    public synchronized void send(Msg msg) throws Exception
    {
        try
        {        
        	{
        		while (startTlsAnswerReceived)
        		{
        			//wait until handshake is done!
        		}
        		byte[] data = msg.getBytesData();
        		if (msg instanceof MsgRtp && ((MsgRtp) msg).isCipheredMessage())
                	data = ((MsgRtp) msg).getCipheredMessage();
	            outputStream.write(data);
	            boolean flushSocket = Config.getConfigByName("tcp.properties").getBoolean("FLUSH_AFTER_SENDING", false);
	            if (flushSocket)
	            {
	            	outputStream.flush();
	            }
	            double delay = Config.getConfigByName("tcp.properties").getDouble("DELAY_AFTER_SENDING", (double) 0.0);
	            int pause = (int) (delay * 1000);
	            if (delay != 0)
	            {
	            	Utils.pauseMilliseconds(pause);
	            }
	            /**
	             * STARTTLS management --> SERVEUR MODE
	             */
	            if (msg.getTransaction() != null)
	            {
	            	Msg begin = msg.getTransaction().getBeginMsg();
	            	if (begin != null && begin.isSTARTTLS_request() && msg.isSTARTTLS_answer())
	            	{
	            		setupSSLSocket(false);
	            		this.sslSocket.startHandshake();
	            		GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "TLS: Server handshake successfull for channel: ", channel);
	            		this.inputStream = new BufferedInputStream(sslSocket.getInputStream());
	            		this.outputStream = this.sslSocket.getOutputStream();
	            		startTlsReceived = false;
	            	}
	            }
        	}
        }
        catch(Exception e)
        {
            throw new ExecutionException("Exception : Send a message " + msg.toShortString(), e);
        }
    }
    
    public void close()
    {
        try
        {
            Socket tmp = socket;
            socket = null;
            if(null != tmp){
                tmp.close();
            }
        }
        catch(Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Error while closing TCP socket");
        }
    }

    public Socket getSocket() {
        return socket;
    }
    
    private void setupSSLSocket(boolean clientMode) throws Exception
    {
    	String certificateAlgorithm = Config.getConfigByName("tls.properties").getString("cert.ALGORITHM");
        String certificateSSLVersion = Config.getConfigByName("tls.properties").getString("cert.SSL_VERSION");
        String certificateServerPath = Config.getConfigByName("tls.properties").getString("cert.SERVER.DIRECTORY");
        String certificateServerKeystorePassword = Config.getConfigByName("tls.properties").getString("cert.SERVER.KEYSTORE_PASSWORD");
        String certificateServerKeyPassword = Config.getConfigByName("tls.properties").getString("cert.SERVER.KEY_PASSWORD");      
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
	    
	    SSLContext sslc = SSLContext.getInstance(certificateSSLVersion);
   	 	sslc.init(keyManagers, null, null);
   	 	
   	 	SSLSocketFactory sslSocketFactory = (SSLSocketFactory)sslc.getSocketFactory();
   	 	this.sslSocket = (SSLSocket)sslSocketFactory.createSocket(this.socket,
                        										  this.socket.getInetAddress().getHostAddress(),
                        										  this.socket.getPort(),
                        										  false);
   	 	this.sslSocket.setUseClientMode(clientMode);
    }
}
