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
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.sctp.ChannelSctp;
import com.devoteam.srit.xmlloader.tcp.ChannelTcp;
import com.devoteam.srit.xmlloader.tls.ChannelTls;
import com.devoteam.srit.xmlloader.udp.ChannelUdp;

import java.net.InetAddress;

import org.dom4j.Element;

/**
 * Interface générique servant a identifier un channel, au minimum, par son URL
 * @author gpasquiers
 */
public class Channel
{

    private String UID;
    
    protected String name;
    protected String localHost;
    protected int localPort = 0;
    protected String remoteHost;
    protected int remotePort = 0;

	protected String protocol;
	
	protected Stack stack;

    protected Channel channel = null;
    protected String transport = null;
	
    /** Creates a new instance of Channel*/
    public Channel(Stack stack)
    {
        this.stack = stack;
    }

    /** Creates a new instance of Channel*/
    public Channel(String name)
    {
        this.name = name;
    }

    /** Creates a new instance of Channel */
    public Channel(String localHost, int localPort, String remoteHost, int remotePort, String aProtocol)
    {
        this("Channel #" + Stack.nextTransactionId());
        this.localHost = Utils.formatIPAddress(localHost);
        this.localPort = localPort;

        this.remoteHost = Utils.formatIPAddress(remoteHost);
        this.remotePort = remotePort;
        
        this.protocol = aProtocol;
    }

    /** Creates a new instance of Channel */
    public Channel(String name, String localHost, String localPort, String remoteHost, String remotePort, String aProtocol) throws Exception
    {
        this(name);
        if (localHost != null)
        {
        	this.localHost = Utils.formatIPAddress(localHost);
        }
        
        if (localPort != null)
        {
            this.localPort = Integer.parseInt(localPort);
        }

        this.remoteHost = Utils.formatIPAddress(remoteHost);
        
        if (remotePort != null)
        {
            this.remotePort = Integer.parseInt(remotePort);
        }
       
        this.protocol = aProtocol.toUpperCase();
    }

    public String getName()
    {
        return name;
    }

    public String getUID()
    {
        return this.UID;
    }

    public String getLocalHost()
    {
        return localHost;
    }

    public int getLocalPort()
    {
        return localPort;
    }

    /**
     * This method has been added for protocol specific channels when we know
     * the local port only after a call to the open() method.
     * 
     * It should only be called in the open() method of the Channel sub-types.
     * @param port
     */
    public void setLocalPort(int port)
    {
        this.localPort = port;
    }

    /**
     * This method has been added for protocol specific channels when we know
     * the local host only after a call to the open() method.
     * 
     * It should only be called in the open() method of the Channel sub-types.
     * @param port
     */
    public void setLocalHost(String host)
    {
        this.localHost = Utils.formatIPAddress(host);
    }

    public String getRemoteHost()
    {
        return remoteHost;
    }

    public int getRemotePort()
    {
        return remotePort;
    }

    /**
     * This method has been added for protocol specific channels when we specify
     * the remote port in the xml syntax.
     * 
     * @param port
     */
    public void setRemotePort(int port)
    {
        this.remotePort = port;
    }

    /**
     * This method has been added for protocol specific channels when we specify
     * the remote host in the xml syntax.
     * 
     * @param host
     */
    public void setRemoteHost(String host)
    {
        this.remoteHost = Utils.formatIPAddress(host);
    }

    public String getProtocol() {
		return protocol;
	}

	/** Open a channel */
    public boolean open() throws Exception
    {
    	if (transport == null)
    	{
    		return true;
    	}
    	else if (transport.equalsIgnoreCase(StackFactory.PROTOCOL_TCP))
        {
        	channel = new ChannelTcp(this.name, this.localHost, new Integer(this.localPort).toString(), this.remoteHost, new Integer(this.remotePort).toString(), this.protocol);
        }
        else if (transport.equalsIgnoreCase(StackFactory.PROTOCOL_TLS))
        {
        	channel = new ChannelTls(name, this.localHost, new Integer(this.localPort).toString(), this.remoteHost, new Integer(this.remotePort).toString(), this.protocol);
        }
        else if (transport.equalsIgnoreCase(StackFactory.PROTOCOL_SCTP))
        {
        	channel = new ChannelSctp(stack, name, this.localHost, new Integer(this.localPort).toString(), this.remoteHost, new Integer(this.remotePort).toString(), this.protocol);
        }
        else if (transport.equalsIgnoreCase(StackFactory.PROTOCOL_UDP))
        {
        	channel = new ChannelUdp(name, this.localHost, new Integer(this.localPort).toString(), this.remoteHost, new Integer(this.remotePort).toString(), this.protocol, true);
        }
        else
        {
        	throw new Exception("openChannelSIGTRAN operation : Bad transport value for " + transport);
        }
    	return channel.open();
    }

    /** Close a channel */
    public boolean close(){
        try 
        {
        	channel.close();
        } 
        catch (Exception e) 
        {
            // nothing to do
        }
        channel = null;
        return true;
    }

    /** Send a Msg to SIP Stack */
    public boolean sendMessage(Msg msg) throws Exception
    {
        if (null == channel)
            throw new Exception("Channel is null, has one channel been opened ?");

        if (msg.getChannel() == null)
            msg.setChannel(this);

        channel.sendMessage(msg);

        return true;
    }


    /** Send a Msg to SIP Stack */

    public boolean receiveMessageNIO(Msg msg) throws Exception
    { 
    	return StackFactory.getStack(protocol).receiveMessageNIO(msg);
    }

    public boolean receiveMessage(Msg msg) throws Exception
    { 
    	return StackFactory.getStack(protocol).receiveMessage(msg);
    }

    /** Get the transport protocol of this message */
    public String getTransport()
    {
    	return this.transport;
    }

    
    //---------------------------------------------------------------------
    // methods for the XML display / parsing of the message
    //---------------------------------------------------------------------

    /** 
     * Returns the string description of the message. Used for logging as DEBUG level 
     */
    @Override
    public String toString()
    {
        String str = "name=\"" + name + "\" " +
                "localHost=\"" + localHost + "\" localPort=\"" + localPort + "\" " +
                "remoteHost=\"" + remoteHost + "\" remotePort=\"" + remotePort + "\" " + 
                "transport=\"" + getTransport() + "\"";
        return str;
    }

    /** 
     * Convert the channel to XML document 
     */
    public String toXml()
    {
        return "<CHANNEL " + this.toString() + "/>";
    }


    /** 
     * Parse the channel from XML element 
     */
    public void parseFromXml(Element root, String protocol) throws Exception
    {
    	this.protocol = protocol;
        this.name = root.attributeValue("name");
        String localHost  = root.attributeValue("localHost");
        if (localHost !=  null)
        {
        	localHost = InetAddress.getByName(localHost).getHostAddress();
        	this.localHost = Utils.formatIPAddress(localHost);
        }
        String localPort  = root.attributeValue("localPort");
        if (localPort != null)
        {
        	this.localPort = Integer.parseInt(localPort);
        }
        
        String remoteHost = root.attributeValue("remoteHost");
        if (remoteHost != null)
        {
        	remoteHost = InetAddress.getByName(remoteHost).getHostAddress();
        	this.remoteHost = Utils.formatIPAddress(remoteHost);
        }
        String remotePort = root.attributeValue("remotePort");
        if (remotePort != null)
        {
        	this.remotePort = Integer.parseInt(remotePort);
        }
        
        String transport = root.attributeValue("transport");
        if (transport == null)
        {
        	transport = stack.getConfig().getString("listenpoint.TRANSPORT");
        }
        this.transport = transport.toUpperCase(); 
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
        if(params.length <= 1)
        {
        	parameter.add(this);
        }
        else if(params[1].equalsIgnoreCase("name"))
        {
        	parameter.add(this.name);
        }
        else if(params[1].equalsIgnoreCase("UID"))
        {
        	parameter.add(this.UID);
        }
        else if(params[1].equalsIgnoreCase("localHost"))
        {
        	parameter.add(this.localHost);
        }
        else if(params[1].equalsIgnoreCase("localPort"))
        {
        	parameter.add(String.valueOf(this.localPort));
        }
        else if(params[1].equalsIgnoreCase("remoteHost"))
        {
        	parameter.add(this.remoteHost);
        }
        else if(params[1].equalsIgnoreCase("remotePort"))
        {
        	parameter.add(String.valueOf(this.remotePort));
        }
        else if(params[1].equalsIgnoreCase("protocol"))
        {
        	parameter.add(String.valueOf(this.protocol));
        }
        else if(params[1].equalsIgnoreCase("transport"))
        {
        	parameter.add(String.valueOf(getTransport()));
        }
        else if(params[1].equalsIgnoreCase("xml"))
        {
        	parameter.add(this.toXml());
        }
        else
        {
        	Parameter.throwBadPathKeywordException(path);
        }
            
        return parameter;
    }
    
    /** equals method */
    public boolean equals(Channel channel)
    {
        if (channel == null)
        {
            return false;
        }
        
        String name = channel.getName();
        if(null != this.name )
        {
            if(null != name)
            {
                if (!this.name.equals(name))
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
        
        String localHost = channel.getLocalHost();
        if(null != this.localHost )
        {
            if(null != localHost)
            {
                if (!this.localHost.equals(localHost))
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
        
        if (this.localPort != channel.getLocalPort())
        {
            return false;
        }
        
        String remoteHost = channel.getRemoteHost();
        if(null != this.remoteHost )
        {
            if(null != remoteHost)
            {
                if (!this.remoteHost.equals(remoteHost))
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
        
        if (this.remotePort != channel.getRemotePort())
        {
            return false;
        }
        
        return true;
    }
    
}
