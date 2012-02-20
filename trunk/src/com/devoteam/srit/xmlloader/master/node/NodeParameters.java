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
*//*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.master.node;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 *
 * @author gpasquiers
 */
public class NodeParameters implements Serializable
{
    private String host;
    private int    port;
    private String stub;
    
    private NodeIdentifier nodeIdentifier;
    
    public NodeParameters(String host, int port, String stub, NodeIdentifier nodeIdentifier) throws UnknownHostException
    {
        this.host = InetAddress.getByName(host).getHostAddress();
        this.port = port;
        this.stub = stub;
        this.nodeIdentifier = nodeIdentifier;
    }

    public NodeParameters(String path) throws Exception
    {
        this.load(path);
    }
    
    public void load(String path) throws Exception
    {
        Properties properties = new Properties();
        FileInputStream fileInputStream = new FileInputStream(path);
        properties.load(fileInputStream);
        this.setNodeIdentifier(new NodeIdentifier());
        this.setPort(Integer.parseInt(properties.getProperty("port")));
        this.setHost(properties.getProperty("host"));
        this.setStub(properties.getProperty("stub"));
        fileInputStream.close();
    }

    public void store(String path)
    {
        try
        {
            Properties properties = new Properties();
            properties.setProperty("host", this.getHost());
            properties.setProperty("port", Integer.toString(this.getPort()));
            properties.setProperty("stub", this.getStub());
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            properties.store(fileOutputStream, "");
            fileOutputStream.close();
        }
        catch(Exception e)
        {
            // not a fatal error
            e.printStackTrace();
        }
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public String getStub()
    {
        return stub;
    }

    public void setStub(String stub)
    {
        this.stub = stub;
    }

    public NodeIdentifier getNodeIdentifier()
    {
        return nodeIdentifier;
    }

    public NodeIdentifier setNodeIdentifier(NodeIdentifier nodeIdentifier)
    {
        this.nodeIdentifier = nodeIdentifier;
        return this.nodeIdentifier;
    }
    
    @Override
    public String toString()
    {
        return this.host + ":" + this.port + " (" + this.getNodeIdentifier() + ")";
    }
}
