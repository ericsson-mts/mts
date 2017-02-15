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

package com.devoteam.srit.xmlloader.core.protocol;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.net.AddressesList;
import com.devoteam.srit.xmlloader.sctp.ListenpointSctp;
import com.devoteam.srit.xmlloader.sctp.StackSctp;
import com.devoteam.srit.xmlloader.tcp.ListenpointTcp;
import com.devoteam.srit.xmlloader.tls.ListenpointTls;
import com.devoteam.srit.xmlloader.udp.ListenpointUdp;

import java.net.InetAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Element;

/**
 * Interface generique servant a identifier un listenpoint
 * @author fhenry
 */
public class Listenpoint
{

    private String UID;
    protected String name;
    
    /**
     * the local host(s)
     * in most cases, there is only one local address,
     * the one on which the communication object () is bound
     * in case of sctp transport, the communication object can be bound to many adresses (multihoming)
     */
    private AddressesList addresses = new AddressesList();
    
    private int port = 0;
    private int portTLS = 0;
    
    private boolean listenUDP = false;
    private boolean listenTCP = false;
    private boolean listenSCTP = false;
    private boolean listenTLS = false;
    
    protected String transport = null;
    protected String protocol;
    
    /**
     * Listenpoint
     */
    protected Listenpoint listenpointUdp = null;
    protected Listenpoint listenpointTcp = null;
    protected Listenpoint listenpointSctp = null;
    protected Listenpoint listenpointTls = null;
    
    private Object attachment;
    private HashMap<String, Channel> channels;
    
    protected Stack stack = null;

    /** Creates a new instance of Listenpoint with the config parameters */
    public Listenpoint(Stack stack) throws Exception
    {
        this.stack = stack;
        // Get the config parameters
        this.UID = Utils.newUID();
        
        String host = stack.getConfig().getString("listenpoint.LOCAL_HOST", "");
        if (host==null || host.isEmpty())
        {
            host = "0.0.0.0";
        }
        this.addresses.setFromAddressesStringWithSeparator(host);

        this.port = stack.getConfig().getInteger("listenpoint.LOCAL_PORT", 0);
        this.portTLS = this.stack.getConfig().getInteger("listenpoint.LOCAL_PORT_TLS", 0);
        this.transport = stack.getConfig().getString("listenpoint.TRANSPORT", StackFactory.PROTOCOL_UDP);
        this.listenUDP = this.stack.getConfig().getBoolean("listenpoint.LISTEN_UDP", false);
        this.listenTCP = this.stack.getConfig().getBoolean("listenpoint.LISTEN_TCP", false);
        this.listenSCTP = this.stack.getConfig().getBoolean("listenpoint.LISTEN_SCTP", false);
        this.listenTLS = this.stack.getConfig().getBoolean("listenpoint.LISTEN_TLS", false);
    }

    /** Creates a new instance of Listenpoint */
    // DON'T USE please 
    // Not used except for test unit
    // Used for capture only for RTPFLOW protocol
    public Listenpoint(Stack stack, String name, String host, int port) throws Exception
    {
        this(stack);
        this.name = name;
        
        if (host==null || host.isEmpty())
        {
            host = "0.0.0.0";
        }
        this.addresses.setFromAddressesStringWithSeparator(host);

        this.listenUDP = false;
        this.listenTCP = false;
        this.listenSCTP = false;
        this.listenTLS = false;

        this.port = port;
    }

    public String getName()
    {
        return name;
    }

    public String getUID()
    {
        return UID;
    }

    /**
     * get the default address in the legacy mts ip address string format
     * @return the first address
     */
    //@Nullable
    public String getHost()
    {
        InetAddress localAddress = this.addresses.getHeadImmutable();
        if( localAddress==null){
        	return null;
        }
        String hostAddress = localAddress.getHostAddress();
        String localHost = Utils.formatIPAddress(hostAddress);
        return localHost;
    }
        
    /**
     * @return the addresses
     */
    //@Immutable
    //@Nullable
    public InetAddress getAddress()
    {
    	InetAddress localAddress = this.addresses.getHeadImmutable();
    	return localAddress;
    }

    /**
     * @return the local addresses
     */
    //@Immutable
    public List<InetAddress> getAddresses()
    {
    	return this.addresses.getAllImmutable();
    }
    
    /**
     * 
     */
    public String getAddressesString()
    {
    	return this.addresses.toStringWithSeparator();
    }

    /**
     * @param localAdresses
     * @return
     */
    public boolean setAddresses( List<InetAddress> addresses ){
    	return this.addresses.set( addresses );
    }
    
    /**
     * This method has been added for protocol specific channels when we know
     * the local host only after a call to the open() method.
     * 
     * It should only be called in the open() method of the Channel sub-types.
     * 
     * @param addressesStringWithSeparator
     * @return status
     */
    public boolean setHost( String addressesStringWithSeparator ){
    	return this.addresses.setFromAddressesStringWithSeparator(addressesStringWithSeparator);
    }

    public int getPort()
    {
        return port;
    }

    /**
     * This method has been added for protocol specific channels when we know
     * the local port only after a call to the open() method.
     * 
     * It should only be called in the open() method of the Channel sub-types.
     * @param port
     */
    public void setPort(int port)
    {
        this.port = port;
    }

    public boolean getListenUDP()
    {
        return listenUDP;
    }

    public boolean getListenTCP()
    {
        return listenTCP;
    }

    public boolean getListenSCTP()
    {
        return listenSCTP;
    }

    public boolean getListenTLS()
    {
        return listenTLS;
    }

    public int getPortTLS()
    {
        return portTLS;
    }

    /**
     * This method has been added for protocol specific channels when we know
     * the local port only after a call to the open() method.
     * 
     * It should only be called in the open() method of the Channel sub-types.
     * @param port
     */
    public void setPortTLS(int portTLS)
    {
        this.portTLS = portTLS;
    }

    public Object getAttachment()
    {
        return attachment;
    }

    public void setAttachment(Object attachment)
    {
        this.attachment = attachment;
    }

    public Stack getStack() {
		return stack;
	}

	public void setStack(Stack stack) {
		this.stack = stack;
	}

	public String getProtocol()
    {
        return protocol;
    }

	
    //---------------------------------------------------------------------
    // methods for the transport
    //---------------------------------------------------------------------
		
    /** create a listenpoint  */
    public boolean create(String protocol) throws Exception
    {	    	
        this.channels = new HashMap<String, Channel>();
        this.protocol = protocol;
        
        if (this.listenUDP)
        {
            listenpointUdp = new ListenpointUdp(this.stack);
            listenpointUdp.clone(this);
            listenpointUdp.create(protocol);
        }

        if (this.listenTCP)
        {
            listenpointTcp = new ListenpointTcp(this.stack);
            listenpointTcp.clone(this);
            listenpointTcp.create(protocol);
        }

        if (this.listenSCTP)
        {
        	StackSctp stackSctp = (StackSctp) StackFactory.getStack(StackFactory.PROTOCOL_SCTP);
        	listenpointSctp = stackSctp.createListenpointSctp(this.stack);
            listenpointSctp.clone(this);
            try
            {
                listenpointSctp.create(protocol);
            }
            catch (UnsatisfiedLinkError e)
            {
                // nothing to do
                // we are on Windows and have not any SCTP library
            }
        }

        if (this.listenTLS)
        {
        	if (this.portTLS <= 0)
        	{
        		this.portTLS = this.port + 1;
        	}
            listenpointTls = new ListenpointTls(this.stack);
            listenpointTls.clone(this);
            listenpointTls.create(protocol);
        }
        return true;
    }

    /** Remove a listenpoint */
    public boolean remove()
    {
        if (this.channels != null)
        {
            for (Channel channel : channels.values())
            {
                channel.close();
            }
            this.channels.clear();
            this.channels = null;
        }

        if (listenpointUdp != null)
        {
        	try
        	{
        		listenpointUdp.remove();
        	}
        	catch (Exception e)
        	{
        	}
    		listenpointUdp = null;
        }
        if (listenpointTcp != null)
        {
        	try
        	{
        		listenpointTcp.remove();
	    	}
	    	catch (Exception e)
	    	{
	    	}
            listenpointTcp = null;
        }
        if (listenpointSctp != null)
        {
        	try
        	{
        		listenpointSctp.remove();
	    	}
	    	catch (Exception e)
	    	{
	    	}
            listenpointSctp = null;
        }
        if (listenpointTls != null)
        {
        	try
        	{
            	listenpointTls.remove();
	    	}
	    	catch (Exception e)
	    	{
	    	}
            listenpointTls = null;
        }

        return true;
    }

    /** Open the channel */
    public boolean openChannel(Channel channel) throws Exception
    {
        boolean opened = StackFactory.getStack(getProtocol()).openChannel(channel);
        if (opened)
        {
            String keySocket = channel.getRemoteHost() + ":" + channel.getRemotePort();
            synchronized (channels)
            {
                channels.put(keySocket, channel);
            }
            if (channels.size() % 1000 == 999)
            {
                GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "ListenPoint: List of channels : size = ", channels.size());
            }
            GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Stack: put in channels list : size = ", channels.size(), " the channel \n", channel);
        }

        return opened;
    }

    /** Remove the channel */
    public synchronized boolean removeChannel(Channel channel)
    {
        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "Listenpoint : remove a channel " , channel);

        String keySocket = channel.getRemoteHost() + ":" + channel.getRemotePort();
        this.channels.remove(keySocket);

        return true;
    }

    /** Get the channel */
    public Channel getChannel(String name) throws Exception
    {
        synchronized (channels)
        {
            return channels.get(name);
        }
    }

    /** Put the channel */
    public void putChannel(String name, Channel chan ) throws Exception
    {
        synchronized (channels)
        {
        	channels.put(name, chan);
        }
    }

    public boolean existsChannel(String name) throws Exception
    {
        synchronized (channels)
        {
            return channels.containsKey(name);
        }
    }
    
    /**
     * Prepare the channel
     */
    public Channel prepareChannel(Msg msg, String remoteHost, int remotePort, String transport) throws Exception
    {
        Channel res;

        if (transport == null)
        {
            transport = this.transport;
        }

        msg.setRemoteHost(remoteHost);
        msg.setRemotePort(remotePort);
        msg.setTransport(transport);

        transport = transport.toUpperCase();
        if (transport.equals(StackFactory.PROTOCOL_UDP) && listenpointUdp != null)
        {
            res = listenpointUdp.prepareChannel(msg, remoteHost, remotePort, transport);
        }
        else if (transport.equals(StackFactory.PROTOCOL_TCP) && listenpointTcp != null)
        {
            res = listenpointTcp.prepareChannel(msg, remoteHost, remotePort, transport);
        }
        else if (transport.equals(StackFactory.PROTOCOL_SCTP) && listenpointSctp != null)
        {
            res = listenpointSctp.prepareChannel(msg, remoteHost, remotePort, transport);
        }
        else if (transport.equals(StackFactory.PROTOCOL_TLS) && listenpointTls != null)
        {
            res = listenpointTls.prepareChannel(msg, remoteHost, remotePort, transport);
        }
        else
        {
            throw new ExecutionException("No listenpoint to prepare the channel of the message : bad transport value : " + transport);
        }
        
        return res;
    }

    /**
     * Send a Msg to a given destination with a given transport protocol
     */
    public boolean sendMessage(Msg msg, String remoteHost, int remotePort, String transport) throws Exception
    {
        boolean res = false;

        if (transport == null)
        {
            transport = this.transport;
        }
        
        msg.setRemoteHost(remoteHost);
        msg.setRemotePort(remotePort);
        msg.setTransport(transport);
        
        transport = transport.toUpperCase();
        
        if (transport.equals(StackFactory.PROTOCOL_UDP) && listenpointUdp != null)
        {
            res = listenpointUdp.sendMessage(msg, remoteHost, remotePort, transport);
        }
        else if (transport.equals(StackFactory.PROTOCOL_TCP) && listenpointTcp != null)
        {
            res = listenpointTcp.sendMessage(msg, remoteHost, remotePort, transport);
        }
        else if (transport.equals(StackFactory.PROTOCOL_SCTP) && listenpointSctp != null)
        {
            res = listenpointSctp.sendMessage(msg, remoteHost, remotePort, transport);
        }
        else if (transport.equals(StackFactory.PROTOCOL_TLS) && listenpointTls != null)
        {
            res = listenpointTls.sendMessage(msg, remoteHost, remotePort, transport);
        }
        else
        {
            throw new ExecutionException("No listenpoint to transport the message : bad transport value : " + transport);
        }
        return res;
    }
 
    
    //---------------------------------------------------------------------
    // methods for the XML display / parsing 
    //---------------------------------------------------------------------

    /** 
     * Returns the string description of the message. Used for logging as DEBUG level 
     */
    @Override
    public String toString()
    {
        String str = "";
        if (name != null)
        {
        	str += "name=\"" + name + "\"";
        }
        else
        {
        	str += "name=\"\"";
        }
        str += " localHost=\"" + this.getAddressesString() + "\"";
        str += " localPort=\"" + port + "\"";
        if (listenUDP)
        {
        	str += " listenUDP=\"true\"";
        }
        if (listenTCP)
        {
        	str += " listenTCP=\"true\"";
        }
        if (listenSCTP)
        {
        	str += " listenSCTP=\"true\"";
        }
        if (listenTLS)
        {
            str += " portTLS=\"" + portTLS + "\"";
        	str += " listenTLS=\"true\"";
        }
        if (transport != null && !transport.equals(""))
        {
            str += " transport=\"" + transport.toUpperCase() + "\"";
        }
        return str;
    }    

    /** 
     * Convert the listenpoint to XML document 
     */
    public String toXml()
    {
        return "<LISTENPOINT " + this.toString() + "/>";
    }
    
    /** 
     * Parse the listenpoint from XML element 
     */
    public void parseFromXml(Element root, Runner runner) throws Exception
    {
        this.name = root.attributeValue("name");

        String host = root.attributeValue("localHost");
        if (host == null || host.isEmpty())
        {
        	host = "0.0.0.0";
        }
        this.addresses.setFromAddressesStringWithSeparator(host);

        String portAttr = root.attributeValue("localPort");
        if (portAttr != null)
        {
            this.port = Integer.parseInt(portAttr);
        }
        else
        {
            this.port = 0;
        }
        
        String localURL = root.attributeValue("localURL");
        if (localURL != null)
        {
        	URI uri = new URI(localURL).normalize();
        	
        	String uriHost = uri.getHost();
        	this.addresses.setFromAddressString(uriHost);
        	
        	this.port = uri.getPort();
        }

        String listenUDPAttr = root.attributeValue("listenUDP");
        if (listenUDPAttr != null)
        {
            this.listenUDP = Utils.parseBoolean(listenUDPAttr, "listenUDP");
        }
        else
        {
        	this.listenUDP = this.stack.getConfig().getBoolean("listenpoint.LISTEN_UDP", false);
        }
        String listenTCPAttr = root.attributeValue("listenTCP");
        if (listenTCPAttr != null)
        {
            this.listenTCP = Utils.parseBoolean(listenTCPAttr, "listenTCP");
        }
        else
        {
        	this.listenTCP = this.stack.getConfig().getBoolean("listenpoint.LISTEN_TCP", false);
        }
        String listenSCTPAttr = root.attributeValue("listenSCTP");
        if (listenSCTPAttr != null)
        {
            this.listenSCTP = Utils.parseBoolean(listenSCTPAttr, "listenSCTP");
        }
        else
        {
        	this.listenSCTP = this.stack.getConfig().getBoolean("listenpoint.LISTEN_SCTP", false);
        }
        String listenTLSAttr = root.attributeValue("listenTLS");
        if (listenTLSAttr != null)
        {
            this.listenTLS = Utils.parseBoolean(listenTLSAttr, "listenTLS");
        }
        else
        {
        	this.listenTLS = this.stack.getConfig().getBoolean("listenpoint.LISTEN_TLS", false);
        }

        String localPortTLSAttr = root.attributeValue("localPortTLS");
        if ((this.listenTLS) && (localPortTLSAttr == null) && (this.portTLS > 0))
        {	
            throw new ExecutionException("The attribute \"localPortTLS\" is required because you have asked to listen on TLS; please set this attribute on the <createListenpointPPP> operation.");        	
        }

        if (localPortTLSAttr != null && Utils.isInteger(localPortTLSAttr))
        {
            this.portTLS = Integer.parseInt(localPortTLSAttr);
        }
        else
        {
        	this.portTLS = 0;
        }
        
        if (this.portTLS <= 0)
        {
            this.portTLS = this.port + 1;
        }
        String transportAttr = root.attributeValue("transport");
        if (transportAttr != null)
        {
            this.transport = transportAttr;
        }    	
    }


    //------------------------------------------------------
    // method for the "setFromMessage" <parameter> operation
    //------------------------------------------------------

    /** 
     * Get a parameter from the message 
     */
    public Parameter getParameter(String path) throws Exception
    {
        String[] params = Utils.splitPath(path);
    	Parameter parameter = new Parameter();
        if (params.length <= 1)
        {
        	parameter.add(this);
        }
        else if (params[1].equalsIgnoreCase("name"))
        {
            parameter.add(this.name);
        }
        else if (params[1].equalsIgnoreCase("UID"))
        {
        	parameter.add(this.UID);
        }
        else if (params[1].equalsIgnoreCase("host"))
        {
            GlobalLogger.instance().logDeprecatedMessage("setFromMessage value=\"listenpoint:host\"", "setFromMessage value=\"listenpoint:localHost\"");
            String host = this.getHost();
            parameter.add(host);
        }
        else if (params[1].equalsIgnoreCase("localHost"))
        {
            String host = this.getHost();
            parameter.add(host);
        } else if (params[1].equalsIgnoreCase("port"))
        {
            GlobalLogger.instance().logDeprecatedMessage("setFromMessage value=\"listenpoint:port\"", "setFromMessage value=\"listenpoint:localPort\"");
            parameter.add(String.valueOf(this.port));
        }
        else if (params[1].equalsIgnoreCase("localPort"))
        {
            parameter.add(String.valueOf(this.port));
        }
        else if (params[1].equalsIgnoreCase("localPortTLS"))
        {
            parameter.add(String.valueOf(this.portTLS));
        }
        else if (params[1].equalsIgnoreCase("protocol"))
        {
        	parameter.add(this.protocol);
        }
        else if (params[1].equalsIgnoreCase("xml"))
        {
            parameter.add(this.toXml());
        }
        else
        {
        	Parameter.throwBadPathKeywordException(path);
        }
        return parameter;
    }
    
    /** clone method */
    //@Override
    public void clone(Listenpoint listenpoint)
    {
    	if (listenpoint == null)
        {
            return;
        }
    	this.name = listenpoint.getName();
    	
    	this.addresses.set(listenpoint.addresses);
    	this.port = listenpoint.getPort();
    	this.portTLS = listenpoint.getPortTLS();
    	
    	this.protocol = listenpoint.getProtocol();
        this.listenUDP = false;
        this.listenTCP = false;
        this.listenSCTP = false;
        this.listenTLS = false;
    }
    
    /** equals method */
    //@Override
    public boolean equals(Listenpoint listenpoint)
    {
        if (listenpoint == null)
        {
            return false;
        }

        String name = listenpoint.getName();
        if (null != this.name)
        {
            if (!this.name.equals(name))
            {
                return false;
            }
        }

        if (!this.addresses.equals(listenpoint.addresses))
        {
            return false;
        }

        if (this.port != listenpoint.getPort())
        {
        	if (this.port != 0)
        	{
        		return false;
        	}
        }
        if (this.listenUDP != listenpoint.getListenUDP())
        {
            return false;
        }
        if (this.listenTCP != listenpoint.getListenTCP())
        {
            return false;
        }
        if (this.listenSCTP != listenpoint.getListenSCTP())
        {
            return false;
        }
        if (this.listenTLS != listenpoint.getListenTLS())
        {
            return false;
        }

        if (this.portTLS != listenpoint.getPortTLS())
        {
            return false;
        }

        return true;
    }

}
