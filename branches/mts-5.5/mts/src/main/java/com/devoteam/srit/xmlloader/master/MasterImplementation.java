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

import com.devoteam.srit.xmlloader.core.log.FileTextListenerProvider;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.log.TextListenerProviderRegistry;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.pluggable.ParameterOperatorRegistry;
import com.devoteam.srit.xmlloader.core.pluggable.ParameterTestRegistry;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.JDialogError;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.exceptionhandler.ExceptionHandlerSingleton;
import com.devoteam.srit.xmlloader.core.utils.exceptionhandler.SwingExceptionHandler;
import com.devoteam.srit.xmlloader.core.utils.filesystem.LocalFSInterface;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import com.devoteam.srit.xmlloader.gui.logs.GUITextListenerProvider;
import com.devoteam.srit.xmlloader.master.mastergui.JFrameMaster;
import com.devoteam.srit.xmlloader.master.node.MasterNode;
import com.devoteam.srit.xmlloader.master.node.NodeIdentifier;
import com.devoteam.srit.xmlloader.master.node.NodeParameters;
import com.devoteam.srit.xmlloader.master.testmanager.MultiplexedNotification;
import com.devoteam.srit.xmlloader.master.testmanager.NotificationDemultiplexer;
import com.devoteam.srit.xmlloader.master.testmanager.RemoteTester;
import com.devoteam.srit.xmlloader.core.PropertiesEnhanced;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

/**
 *
 * @author gpasquiers
 */
public class MasterImplementation implements MasterInterface
{
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        init();

        ExceptionHandlerSingleton.setInstance(new SwingExceptionHandler(JFrameMaster.instance()));
        JFrameMaster.instance().setVisible(true);
                
    }
    
    public static void init()
    {

    	// 
    	// redirect the output in a file
    	//
    	try
    	{ 
	        File file = new File("../logs/stdout.log");
	        PrintStream print = new PrintStream(file);
	    	System.setOut(print);
	    	System.setErr(print);
    	}
	    catch (Exception e)
	    {
	    	e.printStackTrace();
	    }

        // Init FSInterface
        //
        SingletonFSInterface.setInstance(new LocalFSInterface());

        //
        // sets the default font for all Swing components.
        //
        java.util.Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements())
        {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource)
            {
                UIManager.put(key, new javax.swing.plaf.FontUIResource("Sans", Font.PLAIN, 12));
            }
        }

        /* Remove the licence control
        try{
            Licence.instance().isComplete();
        }
        catch(Exception e){
            displayErrorLicencePanel();
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
                    JDialogError dialog = new JDialogError((JFrame)null, true);
                    dialog.setTitle("Licence error");
                    dialog.setMessage("Your licence has expired");
                    dialog.setDetails(Licence.getExpiredLicenceMessage());
                    dialog.setVisible(true);
                    return;
                }
            }
        } catch (Exception ex) {
            displayErrorLicencePanel();
        }

        if(Licence.instance().isTrial())
        {
            JOptionPane.showMessageDialog(null, Licence.getTrialMessage(),
            		"Trail version", JOptionPane.WARNING_MESSAGE);
        }

        if(!Licence.instance().versionMatches())
        {
            JDialogError dialog = new JDialogError((JFrame)null, true);
            dialog.setTitle("Licence version error");
            dialog.setMessage("Your Licence does not support the current version of IMSloader");
            dialog.setDetails(Licence.getBadVersionLicenceMessage());
            dialog.setVisible(true);
            return;
        }
		*/
        
        /*
         * Override tester.properties/logs.STORAGE_DIRECTORY for the master
         * filename = Config.getConfigByName("tester.properties").getString("logs.STORAGE_DIRECTORY","../logs") + "/application.log";
         */
        PropertiesEnhanced properties = new PropertiesEnhanced();
        properties.addPropertiesEnhancedComplete("logs.STORAGE_DIRECTORY", Config.getConfigByName("tester.properties").getString("logs.STORAGE_DIRECTORY","../logs") + "/master");
        Config.overrideProperties("tester.properties", properties);

        /*
         * Register the GUI logger provider
         */
        TextListenerProviderRegistry.instance().register(GUITextListenerProvider.instance());

        /*
         * Register the File logger provider
         */
        TextListenerProviderRegistry.instance().register(new FileTextListenerProvider());

        //
        // Some SWING ToolTip configuration
        //
        ToolTipManager.sharedInstance().setDismissDelay(60000);
        UIManager.put("ToolTip.background", new Color(184, 207, 229));
        UIManager.put("ToolTip.foreground", new Color(51, 51, 51));

        try
        {
            NodeIdentifier nodeIdentifier = new NodeIdentifier();
            int port = Config.getConfigByName("master.properties").getInteger("master.rmi.port");
            String host = Config.getConfigByName("master.properties").getString("master.rmi.host", "");
            if (host != null && host.equals(""))
            {
            	host = Utils.getLocalAddress().getHostAddress().toString();
            }
            System.setProperty("java.rmi.server.hostname", host);
            NodeParameters nodeParameters = new NodeParameters("127.0.0.1", port, "imsloader.master", nodeIdentifier);
            MasterImplementation.buildInstance(nodeParameters);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        //
        // Initialize the pluggable components
        //
        ParameterOperatorRegistry.initialize();
        ParameterTestRegistry.initialize();

        //
        // Initialize the Statistics to automatically generate periodically statistics report
        //
        StatPool.initialize("master");
    }

    private static MasterImplementation instance;
    
    public static MasterImplementation instance()
    {
        return instance;
    }

    public static void buildInstance(NodeParameters nodeParameters) throws Exception
    {
        instance = new MasterImplementation(nodeParameters);

        MasterInterface stub = (MasterInterface) UnicastRemoteObject.exportObject(MasterImplementation.instance(), 0);
        // Bind the remote object's stub in the registry
        Registry registry = LocateRegistry.createRegistry(MasterImplementation.instance().getMasterNode().getLocalNodeParameters().getPort());
        registry.rebind(MasterImplementation.instance().getMasterNode().getLocalNodeParameters().getStub(), stub);
    }
    
    private MasterImplementation(NodeParameters nodeParameters)
    {
        this.masterNode = new MasterNode(nodeParameters);
    }
    
    public MasterNode getMasterNode()
    {
        return masterNode;
    }
    
    // <editor-fold desc="NodeInterface Implementation" defaultstate="collapsed">
    private MasterNode masterNode;
    
    public void initConnection(NodeParameters slaveNodeParameters, NodeParameters masterNodeParameters) throws RemoteException
    {
        this.masterNode.initConnection(slaveNodeParameters, masterNodeParameters);
    }

    public void finalizeConnection(NodeParameters slaveNodeParameters) throws RemoteException
    {
        this.masterNode.finalizeConnection(slaveNodeParameters);
    }

    public void closeConnection(NodeIdentifier sourceNodeIdentifier) throws RemoteException
    {
        this.masterNode.closeConnection(sourceNodeIdentifier);
    }

    public boolean isAlive() throws RemoteException
    {
        return this.masterNode.isAlive();
    }

    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="FSInterface Implementation">
    public InputStream getInputStream(URI path) throws RemoteException
    {
        GlobalLogger.instance().getApplicationLogger().debug(Topic.CORE, "FSInterface: slave asked inputstream for ", path);
        return SingletonFSInterface.instance().getInputStream(path);
    }

    public boolean exists(URI path) throws RemoteException
    {
        GlobalLogger.instance().getApplicationLogger().debug(Topic.CORE, "FSInterface: slave asked if file ", path, " exists");
        return SingletonFSInterface.instance().exists(path);
    }

    public boolean isFile(URI path) throws RemoteException
    {
        GlobalLogger.instance().getApplicationLogger().debug(Topic.CORE, "FSInterface: slave asked if ", path, " is a file");
        return SingletonFSInterface.instance().isFile(path);
    }

    public boolean isDirectory(URI path) throws RemoteException
    {
        GlobalLogger.instance().getApplicationLogger().debug(Topic.CORE, "FSInterface: slave asked if ", path, " is a directory");
        return SingletonFSInterface.instance().isDirectory(path);
    }

    public byte[] getBytes(URI path) throws RemoteException
    {
        GlobalLogger.instance().getApplicationLogger().debug(Topic.CORE, "FSInterface: slave asked byte array for file ", path);
        return SingletonFSInterface.instance().getBytes(path);
    }

    public String[] list(URI path) throws RemoteException
    {
        GlobalLogger.instance().getApplicationLogger().debug(Topic.CORE, "FSInterface: slave asked list of files for ", path);
        return SingletonFSInterface.instance().list(path);
    }

    public OutputStream getOutputStream (URI path){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Notifications handling">
    private NotificationDemultiplexer notificationDemultiplexer = new NotificationDemultiplexer();;
    
    public void addMultiplexedListener(NotificationListener listener, RemoteTester testManager, String testName, String testcaseName, String scenarioName) throws Exception
    {
        this.notificationDemultiplexer.addMultiplexedListener(listener, testManager, testName, testcaseName, scenarioName);
    }
    
    public void removeMultiplexedListener(NotificationListener listener) throws Exception
    {
        this.notificationDemultiplexer.removeMultiplexedListener(listener);
    }
    
    public void notificationReceived(MultiplexedNotification notification) throws RemoteException
    {
        try
        {
            this.notificationDemultiplexer.notificationReceived(notification);
        }
        catch(Exception e)
        {
            // ignore exception
        }
    }
    // </editor-fold>
}
