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
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.sctp.ListenpointSctp;
import com.devoteam.srit.xmlloader.tcp.ListenpointTcp;
import com.devoteam.srit.xmlloader.tls.ListenpointTls;
import com.devoteam.srit.xmlloader.udp.ListenpointUdp;

import java.util.HashMap;
import org.dom4j.Element;

/**
 * Interface generique servant a identifier un listenpoint
 * @author fhenry
 */
public abstract class Listenpoint
{

    private String UID;
    private String name;
    private String host;
    private int port = 0;
    private boolean listenUDP = false;
    private boolean listenTCP = false;
    private boolean listenSCTP = false;
    private boolean listenTLS = false;
    private boolean listenIP = false;
    
    private int portTLS = 0;
    protected String transport = null;
    protected String protocol;
    
    /**
     * Listenpoint
     */
    protected ListenpointUdp listenpointUdp = null;
    protected ListenpointTcp listenpointTcp = null;
    protected ListenpointSctp listenpointSctp = null;
    protected ListenpointTls listenpointTls = null;
    
    private Object attachment;
    private HashMap<String, Channel> channels;
    protected Stack stack = null;

    /** Creates a new instance of Listenpoint with the config parameters */
    public Listenpoint(Stack stack) throws Exception
    {
        this.stack = stack;
        // Get the config parameters
        this.host = stack.getConfig().getString("listenpoint.LOCAL_HOST", "");
        if (this.host.length() <= 0)
        {
            this.host = "0.0.0.0";
        }

        this.UID = Utils.newUID();
        this.port = stack.getConfig().getInteger("listenpoint.LOCAL_PORT", 0);
        this.listenUDP = this.stack.getConfig().getBoolean("listenpoint.LISTEN_UDP", false);
        this.listenTCP = this.stack.getConfig().getBoolean("listenpoint.LISTEN_TCP", false);
        this.listenSCTP = this.stack.getConfig().getBoolean("listenpoint.LISTEN_SCTP", false);
        this.listenTLS = this.stack.getConfig().getBoolean("listenpoint.LISTEN_TLS", false);
        this.listenIP = this.stack.getConfig().getBoolean("listenpoint.LISTEN_IP", false);
        this.portTLS = this.stack.getConfig().getInteger("listenpoint.LOCAL_PORT_TLS", 0);
        this.transport = stack.getConfig().getString("listenpoint.TRANSPORT", StackFactory.PROTOCOL_UDP);
    }

    /** Creates a Listenpoint specific from XML tree*/
    public Listenpoint(Stack stack, Element root) throws Exception
    {
        this(stack);
       
        // deprecated message
        this.name = root.attributeValue("providerName");
        if (this.name == null)
        {
        	this.name = root.attributeValue("name");
        }
        this.host = Utils.formatIPAddress(root.attributeValue("localHost"));
        if (null == this.host || this.host.length() <= 0)
        {
            this.host = "0.0.0.0";
        }        
        String portAttr = root.attributeValue("localPort");
        if (portAttr != null)
        {
            this.port = Integer.parseInt(portAttr);
        }
        else
        {
            this.port = 0;
        }

        String listenUDPAttr = root.attributeValue("listenUDP");
        if (listenUDPAttr != null)
        {
            this.listenUDP = Boolean.parseBoolean(listenUDPAttr);
        }
        String listenTCPAttr = root.attributeValue("listenTCP");
        if (listenTCPAttr != null)
        {
            this.listenTCP = Boolean.parseBoolean(listenTCPAttr);
        }
        String listenSCTPAttr = root.attributeValue("listenSCTP");
        if (listenSCTPAttr != null)
        {
            this.listenSCTP = Boolean.parseBoolean(listenSCTPAttr);
        }
        String listenTLSAttr = root.attributeValue("listenTLS");
        if (listenTLSAttr != null)
        {
            this.listenTLS = Boolean.parseBoolean(listenTLSAttr);
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

    /** Creates a new instance of Listenpoint */
    public Listenpoint(Stack stack, String name, String host, int port) throws Exception
    {
        this(stack);
        this.name = name;
        this.host = Utils.formatIPAddress(host);
        if (null == this.host || this.host.length() <= 0)
        {
            this.host = "0.0.0.0";
        }

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

    public String getHost()
    {
        return host;
    }

    /**
     * This method has been added for protocol specific channels when we know
     * the local host only after a call to the open() method.
     * 
     * It should only be called in the open() method of the Channel sub-types.
     * @param port
     */
    public void setHost(String host)
    {
        this.host = Utils.formatIPAddress(host);
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

    public String getProtocol()
    {
        return protocol;
    }

    /** create a listenpoint  */
    public boolean create(String protocol) throws Exception
    {	
        this.channels = new HashMap<String, Channel>();
        this.protocol = protocol;
        
        if (this.listenUDP)
        {
            listenpointUdp = new ListenpointUdp(this.stack, this.name, this.host, this.port);
            listenpointUdp.create(protocol);
        }

        if (this.listenTCP)
        {
            listenpointTcp = new ListenpointTcp(this.stack, this.name, this.host, this.port);
            listenpointTcp.create(protocol);
        }

        if (this.listenSCTP)
        {
            listenpointSctp = new ListenpointSctp(this.stack, this.name, this.host, this.port);
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
            listenpointTls = new ListenpointTls(this.stack, this.name, this.host, this.portTLS);
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

    /** Prepare the channel */
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

    /** Send a Msg to a given destination with a given transport protocol */
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

    /** display method */
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
        str += ", localHost = \"" + host + "\"";
        str += ", localPort = \"" + port + "\"";
        if (listenUDP)
        {
        	str += ", listenUDP = \"true\"";
        }
        if (listenTCP)
        {
        	str += ", listenTCP = \"true\"";
        }
        if (listenSCTP)
        {
        	str += ", listenSCTP = \"true\"";
        }
        if (listenTLS)
        {
            str += ", portTLS = \"" + portTLS + "\"";
        	str += ", listenTLS = \"true\"";
        }

        return str;
    }

    /** equals method */
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

        String listenHost = listenpoint.getHost();
        if (null != this.host)
        {
            if (!this.host.equals(listenHost))
            {
                return false;
            }
        }

        if (this.port != listenpoint.getPort())
        {
            return false;
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

    public Parameter getParameter(String path) throws Exception
    {
        String[] params = Utils.splitPath(path);
        if (params.length < 2)
        {
            return null;
        }

    	Parameter parameter = new Parameter();

        if (params[1].equalsIgnoreCase("name"))
        {
            parameter.add(getName());
        }
        else if (params[1].equalsIgnoreCase("UID"))
        {
        	parameter.add(UID);
        }
        else if (params[1].equalsIgnoreCase("host"))
        {
            GlobalLogger.instance().logDeprecatedMessage("setFromMessage value=\"listenpoint:host\"", "setFromMessage value=\"listenpoint:localHost\"");
            parameter.add(getHost());
        }
        else if (params[1].equalsIgnoreCase("localHost"))
        {
            parameter.add(getHost());
        } else if (params[1].equalsIgnoreCase("port"))
        {
            GlobalLogger.instance().logDeprecatedMessage("setFromMessage value=\"listenpoint:port\"", "setFromMessage value=\"listenpoint:localPort\"");
            parameter.add(String.valueOf(getPort()));
        }
        else if (params[1].equalsIgnoreCase("localPort"))
        {
            parameter.add(String.valueOf(getPort()));
        }
        else if (params[1].equalsIgnoreCase("localPortTLS"))
        {
            parameter.add(String.valueOf(getPort()));
        }
        else if (params[1].equalsIgnoreCase("protocol"))
        {
        	parameter.add(getProtocol());
        }
        else
        {
        	Parameter.throwBadPathKeywordException(path);
        }
        return parameter;
    }
}
