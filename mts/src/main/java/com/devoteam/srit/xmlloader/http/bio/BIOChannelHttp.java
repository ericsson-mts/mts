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

package com.devoteam.srit.xmlloader.http.bio;

import com.devoteam.srit.xmlloader.core.ThreadPool;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.http.ChannelHttp;

import java.net.InetSocketAddress;
import java.net.Socket;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.impl.DefaultHttpClientConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

/**
 *
 * @author gpasquiers
 */
public class BIOChannelHttp extends ChannelHttp
{
    public BIOChannelHttp(String name, String localHost, String localPort, String remoteHost, String remotePort, String aProtocol, boolean secure) throws Exception
    {
        super(name, localHost, localPort, remoteHost, remotePort, aProtocol, secure);
    }
    
    /** Open a connexion to each Stack */
    public boolean open() throws Exception
    {
    	if (this.secure)
    	{
    		StatPool.beginStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_TLS, StackFactory.PROTOCOL_HTTP);
    	}
    	else
    	{
    		StatPool.beginStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_TCP, StackFactory.PROTOCOL_HTTP);
    	}
    	
    	this.startTimestamp = System.currentTimeMillis();
	
        if(null != this.socketServerHttp)
        {
            ThreadPool.reserve().start((BIOSocketServerHttp)socketServerHttp);
        }
        else
        {
            
            String host = this.getRemoteHost();
            int port = this.getRemotePort();
            
            DefaultHttpClientConnection defaultHttpClientConnection = new DefaultHttpClientConnection();
            
            Socket socket;
            
            if(this.secure)
            {
                // Create a trust manager that does not validate certificate chains like the default TrustManager
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
                
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, null);
                
                socket = sslContext.getSocketFactory().createSocket();
        		// read all properties for the TCP socket 
        		Config.getConfigForTCPSocket(socket, true);
            }
            else
            {
                //
                // Create a TCP non secure socket
                //                
                socket = new Socket();
        		// read all properties for the TCP socket 
        		Config.getConfigForTCPSocket(socket, false);
            }

            //
            // Bind the socket to the local address
            //
            String localHost = this.getLocalHost();
            int localPort = initialLocalport;
            
            if(null != localHost)
            {
                socket.bind(new InetSocketAddress(localHost, localPort));
            }
            else
            {
                socket.bind(new InetSocketAddress(localPort));
            }

            socket.setReceiveBufferSize(65536);
            socket.connect(new InetSocketAddress(host, port));
            
            this.setLocalPort(socket.getLocalPort());


            HttpParams params = new BasicHttpParams();
            defaultHttpClientConnection.bind(socket, params);
            
            this.socketClientHttp = new BIOSocketClientHttp(defaultHttpClientConnection, this);
            
            ThreadPool.reserve().start((BIOSocketClientHttp)socketClientHttp);
        }
        return true;
    }

    /** Close a connexion */
    @Override
    public boolean close()
    {
    	if (this.secure)
    	{
    		StatPool.endStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_TLS, StackFactory.PROTOCOL_HTTP, startTimestamp);
    	}
    	else
    	{
    		StatPool.endStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_TCP, StackFactory.PROTOCOL_HTTP, startTimestamp);
    	}
    	    	        	
    	return super.close();
    }

}
