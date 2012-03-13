package com.devoteam.srit.xmlloader.sctp;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;

import com.devoteam.srit.xmlloader.core.utils.Utils;
import dk.i1.sctp.OneToOneSCTPSocket;
import dk.i1.sctp.sctp_initmsg;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;

import dk.i1.sctp.AssociationId;
import java.net.Socket;

/**
 *
 * @author nghezzaz
 */

//  channel is called association in SCTP 
public class ChannelSctp extends Channel
{
    private SocketSctp socket;
    
    private Listenpoint listenpoint;

    private AssociationId aid;
    private Collection<InetAddress> localHostList;
    private Collection<InetAddress> remoteHostList;
    private sctp_initmsg im;

    private long startTimestamp = 0;    
    
    /** Creates a new instance of ChannelSctp */
    public ChannelSctp(String name, String aLocalHost, String aLocalPort, String aRemoteHost, String aRemotePort, String aProtocol) throws Exception
    {
        super(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        socket = null;
        listenpoint = null;
        im = new sctp_initmsg();
    }

    public ChannelSctp(String name, String aLocalHost, String aLocalPort, String aRemoteHost, String aRemotePort, String aProtocol, sctp_initmsg aIm) throws Exception
    {
        super(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        socket = null;
        listenpoint = null;
        im = aIm;
    }

    public ChannelSctp(String name, String aLocalHost, String aLocalPort, String aRemoteHost, String aRemotePort, String aProtocol, SocketSctp aSocketSctp) throws Exception
    {
        super(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        socket = aSocketSctp;
        listenpoint = null;
        im = new sctp_initmsg();
    }
    
    public ChannelSctp(ListenpointSctp aListenpoint, String aLocalHost, int aLocalPort, String aRemoteHost, int aRemotePort, String aProtocol) throws Exception
    {
        super(aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        socket = null;
        listenpoint = aListenpoint;
        im = new sctp_initmsg();
    }

    public ChannelSctp(String name, Listenpoint aListenpoint, Socket aSocket) throws Exception
    {
		super(
        		name,
        		Utils.getLocalAddress().getHostAddress(),
        		Integer.toString(((OneToOneSCTPSocket)aSocket).getLocalInetPort()),
        		null,
        		null,
        		aListenpoint.getProtocol()
        );
		
		StatPool.beginStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_SCTP, getProtocol());
		this.startTimestamp = System.currentTimeMillis();

        listenpoint = aListenpoint;
        socket = new SocketSctp((OneToOneSCTPSocket) aSocket);
        socket.setChannelSctp(this);
    }

    public SocketSctp getSocketSctp()
    {
        return socket;
    }

    public AssociationId getAssociationId()
    {
        return aid;
    }

    public void setAssociationId(AssociationId aid)
    {
        this.aid = aid;
    }

    /** Send a Msg to Channel */
    public synchronized boolean sendMessage(Msg msg) throws Exception
    {
        if (socket == null)
        {
            throw new ExecutionException("SocketSctp is null, has the connection been opened ?");
        }
        msg.setChannel(this);
        socket.send(msg);
        return true;
    }

    public boolean open() throws Exception
    {
        if (socket == null)
        {
    		StatPool.beginStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_SCTP, getProtocol());
    		this.startTimestamp = System.currentTimeMillis();
        	
			InetAddress localAddr = InetAddress.getByName(getLocalHost());
			// TODO Take localAddr into account
            OneToOneSCTPSocket sctpSocket = new OneToOneSCTPSocket();	
            sctpSocket.bind(getLocalPort());
            InetSocketAddress remoteSocketAddress = new InetSocketAddress(getRemoteHost(), getRemotePort());
            sctpSocket.connect(remoteSocketAddress);
            im = new sctp_initmsg();
            sctpSocket.setInitMsg(im);
        
            this.setLocalPort(sctpSocket.getLocalInetPort());
            // TODO Take socket LocalAddress into account
            // this.setLocalHost(socket.getLocalAddress().getHostAddress());

            socket = new SocketSctp(sctpSocket);
        }
        
        socket.setChannelSctp(this);
        socket.setDaemon(true);
        socket.start();        

        return true;
    }

    public boolean close()
    {	
    	if (socket != null)
    	{
    		StatPool.endStatisticProtocol(StatPool.CHANNEL_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_SCTP, getProtocol(), startTimestamp);
    		
    		socket.shutdown();
	        socket = null;
    	}
        return true;
    }

    /** Get the transport protocol of this message */
    public String getTransport() 
    {
    	return StackFactory.PROTOCOL_SCTP;
    }

	public Listenpoint getListenpointSctp() {
		return listenpoint;
	}

}



