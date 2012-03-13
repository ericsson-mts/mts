/*
 * ConnectionHttp.java
 *
 * Created on 26 juin 2007, 10:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.http.nio;

import com.devoteam.srit.xmlloader.core.hybridnio.HybridSocket;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.http.ChannelHttp;
import com.devoteam.srit.xmlloader.http.StackHttp;
import java.net.InetSocketAddress;
import java.net.Socket;
import org.apache.http.impl.DefaultHttpClientConnection;
import org.apache.http.params.BasicHttpParams;

/**
 *
 * @author gpasquiers
 */
public class NIOChannelHttp extends ChannelHttp
{
    
    public NIOChannelHttp(String name, String localHost, String localPort, String remoteHost, String remotePort, String aProtocol, boolean secure) throws Exception
    {
        super(name, localHost, localPort, remoteHost, remotePort, aProtocol, secure);
    }
    
    /** Open a connexion to each Stack */
    public synchronized boolean open() throws Exception
    {
    	if (this.secure)
    	{
    		StatPool.beginStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_TLS, StackFactory.PROTOCOL_HTTP);
    	}
    	else
    	{
    		StatPool.beginStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_TCP, StackFactory.PROTOCOL_HTTP);
    	}
    	
        if(null == this.socketServerHttp)
        {
            nbOpens++;

            if(nbOpens > 3)
            {
                throw new Exception("HTTP Channel already failed to open more than 3 times");
            }

            String host = this.getRemoteHost();
            int port = this.getRemotePort();
            
            DefaultHttpClientConnection defaultHttpClientConnection = new DefaultHttpClientConnection();

            //
            // Bind the socket to the local address
            //
            String localHost = this.getLocalHost();
            int localPort = initialLocalport;
            
            if(null == localHost) localHost = "0.0.0.0";

            InetSocketAddress localsocketAddress = new InetSocketAddress(localHost, localPort);
            InetSocketAddress remoteAddress = new InetSocketAddress(host, port);

            this.socketClientHttp = new NIOSocketClientHttp();
            Socket socket = new HybridSocket((NIOSocketClientHttp)this.socketClientHttp);

            if(secure) StackHttp.ioReactor.openTLS(localsocketAddress, remoteAddress, (HybridSocket) socket, StackHttp.context);
            else StackHttp.ioReactor.openTCP(localsocketAddress, remoteAddress, (HybridSocket) socket);

            defaultHttpClientConnection.bind(socket, new BasicHttpParams());
            ((NIOSocketClientHttp)this.socketClientHttp).init(defaultHttpClientConnection, this);
            
        }
        return true;
    }

    
    /** Close a connexion */
    @Override
    public boolean close()
    {
    	if (this.secure)
    	{
    		StatPool.endStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_TLS, StackFactory.PROTOCOL_HTTP, startTimestamp);
    	}
    	else
    	{
    		StatPool.endStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_TCP, StackFactory.PROTOCOL_HTTP, startTimestamp);
    	}
    	    	        	
    	return super.close();
    }
}
