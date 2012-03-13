package com.devoteam.srit.xmlloader.udp.bio;

import com.devoteam.srit.xmlloader.core.protocol.Channel;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.utils.Utils;

public class ListenpointUdpBIO extends Listenpoint {
	
	// --- attributs --- //
    private SocketUdpBIO socketUdp;
    
    /** Creates a new instance of Listenpoint */
    public ListenpointUdpBIO(Stack stack) throws Exception
    {
    	super(stack);
    }

	/** Creates a Listenpoint specific from XML tree*/
	public ListenpointUdpBIO(Stack stack, Element root) throws Exception	
	{
		super(stack, root);
	}

    /** Creates a new instance of Listenpoint */
    public ListenpointUdpBIO(Stack stack, String name, String host, int port) throws Exception
    {
    	super(stack, name, host, port);
    }
    
	public boolean create(String protocol) throws Exception {
		if (!super.create(protocol))
		{
			return false;
		}

        if (socketUdp != null)
        {
            return true;
        }

        InetSocketAddress localDatagramSocketAddress;

        DatagramSocket datagramSocket = null;

        if (getHost() != null)
        {
            localDatagramSocketAddress = new InetSocketAddress(getHost(), getPort());
            datagramSocket = new DatagramSocket(localDatagramSocketAddress);
        }
        else
        {
            datagramSocket = new DatagramSocket(getPort());
        }

        this.setPort(datagramSocket.getLocalPort());
        this.setHost(datagramSocket.getLocalAddress().getHostAddress());
        
        socketUdp = new SocketUdpBIO(datagramSocket);
        socketUdp.setListenpointUdp(this);
        socketUdp.setDaemon(true);
        socketUdp.start();
        
		return true;
	}

    @Override
    public synchronized Channel prepareChannel(Msg msg, String remoteHost, int remotePort, String transport) throws Exception
    {
		ChannelUdpBIO channel;

		String nameChannel = remoteHost + ":" + remotePort;
		if(!this.existsChannel(nameChannel))
		{
			String host = getHost();
			if ("0.0.0.0".equals(host))
			{
				host = Utils.getLocalAddress().getHostAddress();
			}
			channel = new ChannelUdpBIO(socketUdp, host, getPort(), remoteHost, remotePort, this.getProtocol());
			this.putChannel(nameChannel, channel);
		}
		else
		{
			channel = (ChannelUdpBIO) this.getChannel(nameChannel);
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
	
    	if(this.socketUdp!=null)
    	{
    		this.socketUdp.close();
    		this.socketUdp = null;
    	}

        return true;
    }

	
}
