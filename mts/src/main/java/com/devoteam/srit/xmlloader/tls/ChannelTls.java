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
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.tls.ListenpointTls;
import com.devoteam.srit.xmlloader.tls.SocketTls;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyStore;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 *
 * @author fvandecasteele
 */
public class ChannelTls extends Channel {

	private SocketTls socketTls;

	private Listenpoint listenpoint;

    private long startTimestamp = 0;
	
	/** Creates a new instance of ChannelTls */
	public ChannelTls(String name, String aLocalHost, String aLocalPort, String aRemoteHost, String aRemotePort, String aProtocol) throws Exception
	{
		super(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
		socketTls = null;
		listenpoint = null;
	}

	/** Creates a new instance of ChannelTls */
	public ChannelTls(String name, Listenpoint listenpoint, Socket socket) throws Exception
	{
		super(
				name, 
				((InetSocketAddress)socket.getLocalSocketAddress()).getAddress().getHostAddress(), 
				Integer.toString(((InetSocketAddress)socket.getLocalSocketAddress()).getPort()), 
				((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress().getHostAddress(), 
				Integer.toString(((InetSocketAddress)socket.getRemoteSocketAddress()).getPort()),
				listenpoint.getProtocol()
		);
		
		StatPool.beginStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_TLS, getProtocol());
		this.startTimestamp = System.currentTimeMillis();
		
		socketTls = new SocketTls(socket);
		socketTls.setChannelTls(this);
		this.listenpoint = listenpoint;
	}

	/** Creates a new instance of ChannelTls */
	public ChannelTls(ListenpointTls listenpointTls, String localHost, int localPort, String remoteHost, int remotePort, String aProtocol)
	{
		super(localHost, localPort, remoteHost, remotePort, aProtocol);
		socketTls = null;    	    
		this.listenpoint = listenpointTls;        
	}

	/** Send a Msg to Channel */
	public synchronized boolean sendMessage(Msg msg) throws Exception
	{
		if(null == socketTls)
		{
			throw new ExecutionException("SocketTls is null, has the connection been opened ?");
		}

		msg.setChannel(this);
		socketTls.send(msg);
		return true;
	}

	public boolean open() throws Exception
	{
		if (socketTls == null) 
		{
			StatPool.beginStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_TLS, getProtocol());
			this.startTimestamp = System.currentTimeMillis();
	    				
			InetAddress localAddr = InetAddress.getByName(getLocalHost());

			TrustManager[] trustAllCerts = new TrustManager[]{
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
			
			//-------------------------------------------------------------------------//
            String certificateAlgorithm = Config.getConfigByName("tls.properties").getString("cert.ALGORITHM");
            String certificateClientPath = Config.getConfigByName("tls.properties").getString("cert.CLIENT.DIRECTORY");
            String certificateClientKeystorePassword = Config.getConfigByName("tls.properties").getString("cert.CLIENT.KEYSTORE_PASSWORD");
            String certificateClientKeyPassword = Config.getConfigByName("tls.properties").getString("cert.CLIENT.KEY_PASSWORD");
            String certificateSSLVersion = Config.getConfigByName("tls.properties").getString("cert.SSL_VERSION");
            //-------------------------------------------------------------------------//
            
            char[] certificateKeystorePasswordArray;
            char[] certificateKeyPasswordArray;
            
            if (null == certificateClientKeyPassword || certificateClientKeyPassword.length() == 0)
            	certificateKeyPasswordArray = null;
            else
            	certificateKeyPasswordArray = certificateClientKeyPassword.toCharArray();
            
            if (null == certificateClientKeystorePassword || certificateClientKeystorePassword.length() == 0)
            	certificateKeystorePasswordArray = null;
            else
            	certificateKeystorePasswordArray = certificateClientKeystorePassword.toCharArray();
            
            KeyStore keyStore = KeyStore.getInstance(certificateAlgorithm);
            keyStore.load(new FileInputStream(certificateClientPath), certificateKeystorePasswordArray);
            
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, certificateKeyPasswordArray);
            
            KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
            
            System.setProperty("javax.net.ssl.trustStore", certificateClientPath);
  	      	System.setProperty("javax.net.ssl.trustStorePassword", certificateClientKeystorePassword);
            
  	      	SSLContext sslContext = SSLContext.getInstance(certificateSSLVersion);
  	      	sslContext.init(keyManagers, null, null);
  	      	
  	      	SSLSocket socket = (SSLSocket) sslContext.getSocketFactory().createSocket(getRemoteHost(), getRemotePort(), localAddr, getLocalPort());
  	      	
  	      	Config.getConfigForTCPSocket(socket, true);
  	      	
            
			this.setLocalPort(socket.getLocalPort());
			this.setLocalHost(socket.getLocalAddress().getHostAddress());
			socketTls = new SocketTls(socket);
		}

		socketTls.setChannelTls(this);
		socketTls.setDaemon(true);
		socketTls.start();

		return true;
	}

	public boolean close()
	{   		
		if (socketTls != null)
		{
			StatPool.endStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_TLS, getProtocol(), startTimestamp);
			
			socketTls.close();
			socketTls = null;            
		}

		return true;
	}

	/** Get the transport protocol of this message */
	public String getTransport() 
	{
		return StackFactory.PROTOCOL_TLS;
	}

	public Listenpoint getListenpointTLS() {
		return listenpoint;
	}
	class MyHandshakeListener implements HandshakeCompletedListener {
		public void handshakeCompleted(HandshakeCompletedEvent e) {
            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "Handshake succesful for channel ", this);
		}
	}

}
