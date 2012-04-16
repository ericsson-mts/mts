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

package com.devoteam.srit.xmlloader.master;

import com.devoteam.srit.xmlloader.core.Test;
import com.devoteam.srit.xmlloader.core.licence.Licence;
import com.devoteam.srit.xmlloader.core.licence.UtilsLicence;
import com.devoteam.srit.xmlloader.core.log.FileTextListenerProvider;
import com.devoteam.srit.xmlloader.core.log.TextListenerProviderRegistry;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.filesystem.LocalFSInterface;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import com.devoteam.srit.xmlloader.master.node.NodeIdentifier;
import com.devoteam.srit.xmlloader.master.node.NodeParameters;
import com.devoteam.srit.xmlloader.master.node.SlaveNode;
import com.devoteam.srit.xmlloader.master.testmanager.RemoteTesterImpl;
import com.devoteam.srit.xmlloader.core.PropertiesEnhanced;
import java.net.URI;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.Permission;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author gpasquiers
 */
public class SlaveImplementation implements SlaveInterface
{
    
    public static void main(String... args)
    {
        //
        // Temporarly init FSInterface to LocalFSInterface
        //
        SingletonFSInterface.setInstance(new LocalFSInterface());
        Config.reset();

        //
        // Register the File logger provider
        //
        TextListenerProviderRegistry.instance().register(new FileTextListenerProvider());


        /* Remove the licence control
        try{
            Licence.instance().isComplete();
        }
        catch(Exception e){
            displayErrorLicence();
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date currentDate = new Date();
            Date licenceDate = sdf.parse(Licence.instance().getDate());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(licenceDate);

            if((Licence.instance().getValidity().length() != 0)
               && (Integer.parseInt(Licence.instance().getValidity()) != 0))//for non infinite period
            {
                calendar.add(Calendar.DAY_OF_YEAR, Integer.parseInt(Licence.instance().getValidity()));
                Date licenceDateExpiration = calendar.getTime();

                if(currentDate.after(licenceDateExpiration))
                {
                    System.out.println(Licence.getExpiredLicenceMessage());
                    return;
                }
            }
        } catch (Exception ex) {
                displayErrorLicence();
        }

        if(Licence.instance().isTrial())
        {
            System.out.println(Licence.getTrialMessage());
        }

        if(!Licence.instance().versionMatches())
        {
            System.out.println(Licence.getBadVersionLicenceMessage());
            return;
        }
        */
        
        //
        // Read arguments
        //
        if(args.length == 0)
        {
            
        }
        else if(args.length == 1)
        {
            PropertiesEnhanced properties = new PropertiesEnhanced();
            properties.addPropertiesEnhancedComplete("slave.rmi.port", String.valueOf(Integer.parseInt(args[0])));
            Config.overrideProperties("master.properties", properties);
        }
        else
        {
            System.out.println( "Usage: startSlave <portNumber>\n");
            System.exit(1);
        }

        //
        // Init SecurityManager
        //
        SecurityManager securityManager = new SecurityManager(){
            @Override
            public void checkPermission(Permission permission){
            }
        };
        
        System.setSecurityManager(securityManager);



        // create and register the slave interface
        try
        {
            NodeIdentifier nodeIdentifier = new NodeIdentifier();
            int port = Config.getConfigByName("master.properties").getInteger("slave.rmi.port");
            String host = Config.getConfigByName("master.properties").getString("slave.rmi.host", "");
            if (host != null && host.equals(""))
            {
            	host = Utils.getLocalAddress().getHostAddress().toString();
            }            
            System.setProperty("java.rmi.server.hostname", host);

            NodeParameters nodeParameters;
            try
            {
                nodeParameters = new NodeParameters(SlaveNode.CONFIG_SLAVE_PATH);
                nodeParameters.setPort(port);
            }
            catch(Exception e)
            {
                nodeParameters = new NodeParameters("localhost", port, "imsloader.slave", nodeIdentifier);                
            }
            
            SlaveImplementation.buildInstance(nodeParameters);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void displayErrorLicence()
    {
        String computerID = "?";
        try{
            computerID = UtilsLicence.getNewComputeID();
        }
        catch(Exception ee){

        }

        System.out.println(Licence.getProcedure(computerID));
        return;
    }
    
    private static SlaveImplementation instance;
    
    public static SlaveImplementation instance()
    {
        return instance;
    }

    public static void buildInstance(NodeParameters nodeParameters) throws RemoteException
    {
        instance = new SlaveImplementation(nodeParameters);

        SlaveInterface stub = (SlaveInterface) UnicastRemoteObject.exportObject(instance, 0);

        // Bind the remote object's stub in the registry
        Registry registry = LocateRegistry.createRegistry(instance.getSlaveNode().getLocalNodeParameters().getPort());
        registry.rebind(instance.getSlaveNode().getLocalNodeParameters().getStub(), stub);
    }
    
    private SlaveImplementation(NodeParameters nodeParameters)
    {
        this.slaveNode = new SlaveNode(nodeParameters);
        this.testManagerImpl = new RemoteTesterImpl();
    }
    
    public SlaveNode getSlaveNode()
    {
        return this.slaveNode;
    }
    
    // <editor-fold desc="NodeInterface Implementation" defaultstate="collapsed">
    private SlaveNode slaveNode;

    public void initConnection(NodeParameters slaveNodeParameters, NodeParameters masterNodeParameters) throws RemoteException
    {
        this.slaveNode.initConnection(slaveNodeParameters, masterNodeParameters);
    }

    public void finalizeConnection(NodeParameters slaveNodeParameters) throws RemoteException
    {
        this.slaveNode.finalizeConnection(slaveNodeParameters);
    }

    public void closeConnection(NodeIdentifier sourceNodeIdentifier) throws RemoteException
    {
        this.slaveNode.closeConnection(sourceNodeIdentifier);
    }

    public boolean isAlive() throws RemoteException
    {
        return this.slaveNode.isAlive();
    }
    // </editor-fold>
    
    // <editor-fold desc="RemoteTester Implementation" defaultstate="collapsed">
    private RemoteTesterImpl testManagerImpl;
    
    public Test openTest(URI path, URI IMSLOADER_BIN, String name, String home, HashMap<String, String> initialParametersValues, boolean force) throws RemoteException
    {
        return this.testManagerImpl.openTest(path, IMSLOADER_BIN, name, home, initialParametersValues, force);
    }

    public void startTest(Class runnerClass) throws RemoteException
    {
        this.testManagerImpl.startTest(runnerClass);
    }

    public void stopTest() throws RemoteException
    {
        this.testManagerImpl.stopTest();
    }

    public void addMultiplexedListener(String channelUID, String testName, String testcaseName, String scenarioName) throws RemoteException
    {
        this.testManagerImpl.addMultiplexedListener(channelUID, testName, testcaseName, scenarioName);
    }
    
    public StatPool getStatPool() throws RemoteException
    {
        return this.testManagerImpl.getStatPool();
    }

    public void resetStatPool() throws RemoteException
    {
        this.testManagerImpl.resetStatPool();
    }

    public void removeMultiplexedListener(String channelUID) throws RemoteException
    {
        this.testManagerImpl.removeMultiplexedListener(channelUID);
    }
    // </editor-fold>
    
}
