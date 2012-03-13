package com.devoteam.srit.xmlloader.udp.nio;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.udp.nio.DatagramReactor;
import java.net.InetSocketAddress;

public class ChannelUdpNIO extends Channel
{
    private SocketUdpNIO socketUdp;

    private long startTimestamp = 0;
    
    public ChannelUdpNIO(String name, String aLocalHost, String aLocalPort, String aRemoteHost, String aRemotePort, String aProtocol, boolean aConnected) throws Exception
    {
        super(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        socketUdp = null;
    }

    public ChannelUdpNIO(SocketUdpNIO socketUdp, String aLocalHost, int aLocalPort, String aRemoteHost, int aRemotePort, String aProtocol) throws Exception
    {
        super(aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        this.socketUdp = socketUdp;
    }

    @Override
    public boolean open() throws Exception
    {
        if (socketUdp != null)
        {
            return true;
        }
        try
        {
    		StatPool.beginStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_UDP, getProtocol());
    		this.startTimestamp = System.currentTimeMillis();
        	
            InetSocketAddress localDatagramSocketAddress;


            if (getLocalHost() != null)
            {
                localDatagramSocketAddress = new InetSocketAddress(getLocalHost(), getLocalPort());
            }
            else
            {
                localDatagramSocketAddress = new InetSocketAddress(getLocalPort());
            }

            //TODO!!!!!!!!!!!!!!!!!!!!!!!!
//            if (!datagramSocket.isBound())
//            {
//                GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "ChannelUdp: The datagramSocket is unbounded");
//            }

            this.setLocalPort(localDatagramSocketAddress.getPort());

            socketUdp = new SocketUdpNIO();
            socketUdp.setChannelUdp(this);

            DatagramReactor.instance().open(localDatagramSocketAddress, socketUdp);

            return true;
        }
        catch (Exception e)
        {
            throw new ExecutionException("ChannelUdp: Error occured while creating socket", e);
        }
    }

    public synchronized boolean sendMessage(Msg msg) throws Exception
    {
        if (socketUdp == null)
        {
            throw new ExecutionException("SocketUdp is null, has the connection been opened ?");
        }

        msg.setChannel(this);
        socketUdp.send(msg);
        return true;
    }

    public boolean close()
    {
        if (socketUdp != null)
        {
    		// StatPool.endStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_UDP, getProtocol(), startTimestamp);
    		
            socketUdp.close();
            socketUdp = null;        	
        }
        
        return true;
    }

    /** Get the transport protocol of this message */
    public String getTransport() 
    {
    	return StackFactory.PROTOCOL_UDP;
    }

}
