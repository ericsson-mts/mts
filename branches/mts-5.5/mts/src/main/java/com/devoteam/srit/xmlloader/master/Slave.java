/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.master;

import com.devoteam.srit.xmlloader.core.PropertiesEnhanced;
import com.devoteam.srit.xmlloader.core.log.FileTextListenerProvider;
import com.devoteam.srit.xmlloader.core.log.TextListenerProviderRegistry;
import com.devoteam.srit.xmlloader.core.newstats.StatPoolReset;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.filesystem.LocalFSInterface;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import com.devoteam.srit.xmlloader.master.slave.SlaveImpl;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.Permission;
import java.util.concurrent.Semaphore;

/**
 *
 * @author gpasquiers
 */
public class Slave {

    public static void main(String... args) {
        // Temporarly init FSInterface to LocalFSInterface
        SingletonFSInterface.setInstance(new LocalFSInterface());
        Config.reset();

        // Disable the automatic reset of stats pool, it will be manually handled by the master
        StatPoolReset.instance().disable();
        
        // Register the File logger provider
        TextListenerProviderRegistry.instance().register(new FileTextListenerProvider());

        //
        // Read arguments
        //
        if (args.length == 0) {
        }
        else if (args.length == 1) {
            PropertiesEnhanced properties = new PropertiesEnhanced();
            properties.addPropertiesEnhancedComplete("slave.rmi.port", String.valueOf(Integer.parseInt(args[0])));
            Config.overrideProperties("master.properties", properties);
        }
        else {
            System.out.println("Usage: startSlave <portNumber>\n");
            System.exit(1);
        }

        //
        // Init SecurityManager
        //
        SecurityManager securityManager = new SecurityManager() {

            @Override
            public void checkPermission(Permission permission) {
            }
        };

        System.setSecurityManager(securityManager);



        // create and register the slave interface
        try {
            int port = Config.getConfigByName("master.properties").getInteger("slave.rmi.port");
            
            if(args.length >= 2){
                port = Integer.parseInt(args[1]);
            }
            
            String host = Config.getConfigByName("master.properties").getString("slave.rmi.host", "");
            if (host != null && host.equals("")) {
                host = Utils.getLocalAddress().getHostAddress().toString();
            }
            
            System.setProperty("java.rmi.server.hostname", host);

            // register a SlaveImp
            SlaveImpl slave = new SlaveImpl();

            // bind the stub into the registry
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("imsloader/slave", slave);
            new Semaphore(0).acquireUninterruptibly();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
