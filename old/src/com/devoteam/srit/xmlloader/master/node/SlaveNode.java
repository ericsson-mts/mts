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

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import com.devoteam.srit.xmlloader.master.MasterInterface;
import com.devoteam.srit.xmlloader.master.SlaveInterface;
import com.devoteam.srit.xmlloader.master.filesystem.RemoteFSInterface;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author gpasquiers
 */
public class SlaveNode implements NodeInterface 
{
    public final static String CONFIG_SLAVE_PATH = "../conf/master.slave.properties";
    
    private NodeParameters localNodeParameters;
    private NodeParameters masterNodeParameters;
    
    private MasterInterface stub;

    private Timer timer;

    public SlaveNode(NodeParameters localNodeParameters)
    {
        this.localNodeParameters = localNodeParameters;
        this.masterNodeParameters = null;
        this.stub = null;

        this.timer = new Timer(true);
        this.timer.schedule(new TimerTask(){

            @Override
            public void run()
            {
                try
                {
                    if(null != stub)
                    {
                        try
                        {
                            stub.isAlive();
                        }
                        catch(Throwable t)
                        {
                            // there was an exception so we are not connected to the master anymore
                            closeConnection(null);
                        }
                    }
                }
                catch (Throwable t)
                {
                    System.out.println("timertask:" + t.toString());
                }
            }
        }, 1000, 10000);
    }
    
    private MasterInterface getMasterStub(NodeParameters masterNodeParameters) throws RemoteException, NotBoundException
    {
        Registry registry = LocateRegistry.getRegistry(masterNodeParameters.getHost(), masterNodeParameters.getPort());
        return (MasterInterface) registry.lookup(masterNodeParameters.getStub());
    }

    public void initConnection(NodeParameters slaveNodeParameters, NodeParameters masterNodeParameters) throws RemoteException
    {
        try
        {
            this.stub.isAlive();
        }
        catch (Throwable t)
        {
            this.masterNodeParameters = null;
            this.stub = null;
        }

        if(null != this.masterNodeParameters)
        {
            throw new RemoteException("This slave already has a master.");
        }
        
        try
        {
            masterNodeParameters.setHost(RemoteServer.getClientHost());
            this.stub = this.getMasterStub(masterNodeParameters);
            this.stub.finalizeConnection(slaveNodeParameters);
            this.masterNodeParameters = masterNodeParameters;
            this.localNodeParameters = slaveNodeParameters;
            this.localNodeParameters.setNodeIdentifier(slaveNodeParameters.getNodeIdentifier());
            this.localNodeParameters.store(CONFIG_SLAVE_PATH);

            //
            // Init FSInterface
            //
            SingletonFSInterface.setInstance(new RemoteFSInterface(this.stub));
            Config.reset();
            
        }
        catch(Exception e)
        {
            e.printStackTrace();
            this.masterNodeParameters = null;
            this.stub = null;
            throw new RemoteException("could not initialize connection", e);
        }       
    }

    public void finalizeConnection(NodeParameters slaveNodeParameters) throws RemoteException
    {
        throw new RemoteException("Not supported. The slave finalizes.");
    }

    public void closeConnection(NodeIdentifier sourceNodeIdentifier) throws RemoteException
    {
        this.masterNodeParameters = null;
        this.stub = null;
    }

    public Boolean isConnected()
    {
        return this.masterNodeParameters != null;
    }

    public boolean isAlive() throws RemoteException
    {
        return true;
    }
    
    public NodeParameters getLocalNodeParameters()
    {
        return localNodeParameters;
    }

    public NodeParameters getMasterNodeParameters()
    {
        return masterNodeParameters;
    }

    public MasterInterface getStub()
    {
        return this.stub;
    }
}
