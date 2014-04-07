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
import com.devoteam.srit.xmlloader.core.utils.Utils;

import java.net.URI;

/**
 * Interface générique servant a identifier un channel, au minimum, par son URL
 * @author gpasquiers
 */
public abstract class Channel
{

    private String UID;
    
    private String name;
    private String localHost;
    private int localPort = 0;
    private String remoteHost;
    private int remotePort = 0;

	private String protocol;
        
    /** Creates a new instance of Channel*/
    public Channel(String name)
    {
        this.name = name;
        // this.UID = Utils.newUID();
    }

    /** Creates a new instance of Channel */
    public Channel(String name, String remoteUrl, String aProtocol) throws Exception
    {
        this(name);
        URI uri = null;
        try
        {
            uri = new URI(remoteUrl);
        }
        catch (Exception e)
        {
            throw new ExecutionException("Can't create URI from : " + remoteUrl, e);
        }
        this.remoteHost = Utils.formatIPAddress(uri.getHost());
        this.remotePort = uri.getPort();
        this.protocol = aProtocol;
    }

    /** Creates a new instance of Channel */
    public Channel(String name, String localUrl, String remoteUrl, String aProtocol) throws Exception
    {
        this(name, remoteUrl, aProtocol);
        URI uri = null;
        try
        {
            uri = new URI(localUrl);
        }
        catch (Exception e)
        {
            throw new ExecutionException("Can't create URI from : " + localUrl, e);
        }
        this.localHost = Utils.formatIPAddress(uri.getHost());
        this.localPort = uri.getPort();
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
    public abstract boolean open() throws Exception;

    /** Close a channel */
    public abstract boolean close();

    /** Send a Msg to SIP Stack */
    public abstract boolean sendMessage(Msg msg) throws Exception;

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
    public abstract String getTransport();

    /** display method */
    @Override
    public String toString()
    {
        String str = "name=\"" + name + "\" " +
                "localHost=\"" + localHost + "\" localPort=\"" + localPort + "\" " +
                "remoteHost=\"" + remoteHost + "\" remotePort=\"" + remotePort + "\" " + 
                "transport=\"" + getTransport() + "\"";
        return str;
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

    public Parameter getParameter(String path) throws Exception
    {       
        String[] params = Utils.splitPath(path);
        if(params.length < 2)
        {
        	return null;
        }
        
        Parameter parameter = new Parameter();
        if(params[1].equalsIgnoreCase("name"))
        {
        	parameter.add(getName());
        }
        else if(params[1].equalsIgnoreCase("UID"))
        {
        	parameter.add(getUID());
        }
        else if(params[1].equalsIgnoreCase("localHost"))
        {
        	parameter.add(getLocalHost());
        }
        else if(params[1].equalsIgnoreCase("localPort"))
        {
        	parameter.add(String.valueOf(getLocalPort()));
        }
        else if(params[1].equalsIgnoreCase("remoteHost"))
        {
        	parameter.add(getRemoteHost());
        }
        else if(params[1].equalsIgnoreCase("remotePort"))
        {
        	parameter.add(String.valueOf(getRemotePort()));
        }
        else if(params[1].equalsIgnoreCase("protocol"))
        {
        	parameter.add(String.valueOf(getProtocol()));
        }
        else if(params[1].equalsIgnoreCase("transport"))
        {
        	parameter.add(String.valueOf(getTransport()));
        }
        else
        {
        	Parameter.throwBadPathKeywordException(path);
        }
            
        return parameter;

    }
}
