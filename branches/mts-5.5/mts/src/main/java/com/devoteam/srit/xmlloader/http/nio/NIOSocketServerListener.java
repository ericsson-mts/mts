/*
 * SocketServerHttpListener.java
 *
 * Created on 26 juin 2007, 09:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.http.nio;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.hybridnio.HybridSocket;
import com.devoteam.srit.xmlloader.core.hybridnio.IOHandler;
import com.devoteam.srit.xmlloader.core.hybridnio.SSLSocketChannel;
import com.devoteam.srit.xmlloader.http.SocketServerListener;
import com.devoteam.srit.xmlloader.http.StackHttp;

import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.SelectionKey;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.impl.DefaultHttpServerConnection;
/**
 *
 * @author sngom
 */
public class NIOSocketServerListener extends SocketServerListener implements IOHandler
{
    private ServerSocketChannel channel;

    private long startTimestamp = 0;    
    
    /** Creates a new instance of SocketServerHttpListener */
    public NIOSocketServerListener(int port, boolean secure) throws ExecutionException
    {
        super(secure);

    	if (secure)
    	{
    		StatPool.beginStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_TLS, StackFactory.PROTOCOL_HTTP);
    	}
    	else
    	{
    		StatPool.beginStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_TCP, StackFactory.PROTOCOL_HTTP);
    	}
		this.startTimestamp = System.currentTimeMillis();

        try
        {
            if(secure) StackHttp.ioReactor.openTLSServer(new InetSocketAddress(port), this, StackHttp.context);
            else       StackHttp.ioReactor.openTCPServer(new InetSocketAddress(port), this);
        }
        catch(Exception e)
        {
            throw new ExecutionException("Can't instantiate the HTTP SocketServerListener secure="+secure, e);
        }
    }

    @Override
    public void shutdown()
    {
    	if (secure)
    	{
    		StatPool.endStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_TLS, StackFactory.PROTOCOL_HTTP, startTimestamp);
    	}
    	else
    	{
    		StatPool.endStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_TCP, StackFactory.PROTOCOL_HTTP, startTimestamp);
    	}
        	
        //
        // Stop listening
        //
        try
        {
            this.channel.close();
        }
        catch(Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Error while closing SocketServerListener's socket secure=", secure);
        }
    }

    
    public void init(SelectionKey selectionKey, SelectableChannel channel)
    {
        this.channel = (ServerSocketChannel) channel;
    }

    public void inputReady()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void outputReady()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void connectReady()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void acceptReady()
    {
        try
        {
            SocketChannel socketChannel = this.channel.accept();

            NIOSocketServerHttp socketServerHttp = new NIOSocketServerHttp();
            HybridSocket socket = new HybridSocket(socketServerHttp);

            InetSocketAddress remoteInetSocketAddress = (InetSocketAddress) socketChannel.socket().getRemoteSocketAddress();
            InetSocketAddress localInetSocketAddress = (InetSocketAddress) socketChannel.socket().getLocalSocketAddress();

            String connectionName = "HTTPServerConnection" + Stack.nextTransactionId();
            String remoteHost = remoteInetSocketAddress.getAddress().getHostAddress();
            String remotePort = Integer.toString(remoteInetSocketAddress.getPort());
            String localHost = localInetSocketAddress.getAddress().getHostAddress();
            String localPort = Integer.toString(localInetSocketAddress.getPort());

            NIOChannelHttp channelHTTP = new NIOChannelHttp(connectionName, localHost, localPort, remoteHost, remotePort, StackFactory.PROTOCOL_HTTP, secure);

            DefaultHttpServerConnection serverConnection = new DefaultHttpServerConnection();

            socketServerHttp.init(serverConnection, channelHTTP);

            channelHTTP.setSocketServerHttp(socketServerHttp);
            StackFactory.getStack(StackFactory.PROTOCOL_HTTP).openChannel(channelHTTP);
            if(socketChannel instanceof SSLSocketChannel) StackHttp.ioReactor.openTLS((SSLSocketChannel)socketChannel, socket);
            else                                          StackHttp.ioReactor.openTCP(socketChannel, socket);

            serverConnection.bind(socket, new BasicHttpParams());
        }
        catch(Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, e, "Exception in SocketServerListener secure=", secure);
            e.printStackTrace();
        }
    }
}
