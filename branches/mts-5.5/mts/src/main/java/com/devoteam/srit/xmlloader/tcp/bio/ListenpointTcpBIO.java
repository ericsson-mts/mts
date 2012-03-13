package com.devoteam.srit.xmlloader.tcp.bio;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;

public class ListenpointTcpBIO extends Listenpoint {
	
	// --- attributs --- //
	private SocketServerTcpListenerBIO  socketListenerTcp;

    private long startTimestamp = 0;
	
    /** Creates a new instance of Listenpoint */
    public ListenpointTcpBIO(Stack stack) throws Exception
    {
    	super(stack);
    }

	/** Creates a Listenpoint specific from XML tree*/
	public ListenpointTcpBIO(Stack stack, Element root) throws Exception
	{
		super(stack, root);
	}

    /** Creates a new instance of Listenpoint */
    public ListenpointTcpBIO(Stack stack, String name, String host, int port) throws Exception
    {
    	super(stack, name, host, port);
    }
    
    /** Create a listenpoint to each Stack */
    @Override
	public boolean create(String protocol) throws Exception {
		StatPool.beginStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_TCP, protocol);
		this.startTimestamp = System.currentTimeMillis();
    	
		if (!super.create(protocol)) 
		{
			return false;
		}
		
		socketListenerTcp = new SocketServerTcpListenerBIO(this);
		socketListenerTcp.setDaemon(true);
		socketListenerTcp.start();

		return true;
	}

    @Override
    public synchronized Channel prepareChannel(Msg msg, String remoteHost, int remotePort, String transport) throws Exception
    {
		ChannelTcpBIO channel;

		String keySocket = remoteHost + ":" + remotePort;

		if(!this.existsChannel(keySocket))
		{
			channel = new ChannelTcpBIO(this, getHost(), 0, remoteHost, remotePort, this.getProtocol());
			this.openChannel(channel);
		}
		else
		{
			channel = (ChannelTcpBIO) this.getChannel(keySocket);
		}
        
		return channel;
    }
	
    @Override
	public synchronized boolean sendMessage(Msg msg, String remoteHost, int remotePort, String transport) throws Exception
	{			
		return prepareChannel(msg, remoteHost, remotePort, transport).sendMessage(msg);
	}
		
	public boolean remove()
    {	
		super.remove();
	
    	if(this.socketListenerTcp!=null)
    	{
    		StatPool.endStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_TCP, getProtocol(), startTimestamp);
    		
    		this.socketListenerTcp.close();
    		this.socketListenerTcp = null;
    	}
    	
        return true;
    }
	
}
