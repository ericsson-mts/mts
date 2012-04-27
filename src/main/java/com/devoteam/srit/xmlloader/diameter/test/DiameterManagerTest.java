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

package com.devoteam.srit.xmlloader.diameter.test;

/** mesure de perf de la pile Diameter 
 * sur ma machine : Pentium 4 dual core 2,6 Gb 1 Gb RAM
 * 10000 tests prennent 5,5 secondes soit environ 1800 tests /s 
 * chaque test corespond à 2 transactions
 * => 3636 transactions/s  
 */
import com.devoteam.srit.xmlloader.core.utils.Utils;
import java.net.URI;
import java.util.GregorianCalendar;
import java.util.logging.FileHandler;
import java.util.logging.Level;

import junit.framework.TestCase;

import com.devoteam.srit.xmlloader.diameter.MsgDiameter;
import com.devoteam.srit.xmlloader.diameter.MsgDiameterParser;
import com.devoteam.srit.xmlloader.core.Tester;
import com.devoteam.srit.xmlloader.core.log.GenericLogger;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.URIFactory;
import com.devoteam.srit.xmlloader.core.utils.XMLLoaderEntityResolver;

import dk.i1.diameter.AVP;
import dk.i1.diameter.AVP_Integer32;
import dk.i1.diameter.Message;
import dk.i1.diameter.ProtocolConstants;
import dk.i1.diameter.node.Capability;
import dk.i1.diameter.node.NodeSettings;
import dk.i1.diameter.node.Peer;
import dk.i1.diameter.node.Peer.TransportProtocol;
import java.io.File;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class DiameterManagerTest extends TestCase {
     
    private static DiameterManagerTest test = new DiameterManagerTest();
    
    private static GenericLogger logger;
    
    private static Tester tester;
    
    private DiameterManager diameterManager;
                 
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        
        test.setUp();
        test.testXMLParsedClient();
        test.testserver();
        test.tearDown();      
        
    }
    
    /**
     * junit setup method.
     * @throws Exception if any problem occurs.
     */
    protected void setUp() throws Exception {
        super.setUp();
                
        if (tester == null) {
            tester = Tester.buildInstance();
        }
        logger = GlobalLogger.instance().getApplicationLogger();
        
        // configure stack trace parameters
        FileHandler fh = new FileHandler("diameterStack.log");
        java.util.logging.Logger.getLogger("dk").addHandler(fh);        
        Level traceLevel = Level.parse(Config.getConfigByName("diameter.properties").getString("TRACE_LEVEL"));
        logger.debug(TextEvent.Topic.PROTOCOL, "traceLevel : ", traceLevel);        
        java.util.logging.Logger.getLogger("dk").setLevel(traceLevel);

        // set capability
        Capability capability = new Capability();
        addSupportedVendor(capability);                
        addAuthApp(capability);        
        addAcctApp(capability);        
        addVendorAuthApp(capability);        
        addVendorAcctApp(capability);

        // set node settings
        String nodeHostId = Config.getConfigByName("diameter.properties").getString("listenpoint.LOCAL_HOST");
        if ((nodeHostId == null) || (nodeHostId.length() <= 0)) {
            nodeHostId = Utils.getLocalAddress().getHostAddress();
            GregorianCalendar timeStamp = new GregorianCalendar();
            nodeHostId = nodeHostId + "_" + timeStamp.getTimeInMillis();
        }
        logger.debug(TextEvent.Topic.PROTOCOL, "nodeHostId : ", nodeHostId);
        String nodeRealm = Config.getConfigByName("diameter.properties").getString("node.REALM");
        logger.debug(TextEvent.Topic.PROTOCOL, "nodeRealm : ", nodeRealm);
        int nodeVendorId = Config.getConfigByName("diameter.properties").getInteger("node.VENDOR_ID");
        logger.debug(TextEvent.Topic.PROTOCOL, "nodeVendorId : ", nodeVendorId);
        int nodePort = Config.getConfigByName("diameter.properties").getInteger("listenpoint.LOCAL_PORT");
        logger.debug(TextEvent.Topic.PROTOCOL, "nodePort : ", nodePort);
        String nodeProductName = Config.getConfigByName("diameter.properties").getString("node.PRODUCT_NAME");
        logger.debug(TextEvent.Topic.PROTOCOL, "nodeProductName : ", nodeProductName);
        int nodeFirmwareRevision = Config.getConfigByName("diameter.properties").getInteger("node.FIRMWARE_REVISION");
        logger.debug(TextEvent.Topic.PROTOCOL, "nodeFirmwareRevision : ", nodeFirmwareRevision);

        NodeSettings node_settings;
        try {
            // DNS resolver : not done by the diameter stack
            node_settings  = new NodeSettings(
                nodeHostId,
                nodeRealm,
                nodeVendorId,
                capability,
                nodePort,
                "HSSDiameterNode", 0x01000000);
        } catch (Exception e) {
            Exception hsse = new Exception("ERROR : Reading the HSS server configuration : " + e.toString(), e);
            throw(hsse);
        }

        boolean isNodeUseSCTP = Config.getConfigByName("diameter.properties").getBoolean("listenpoint.LISTEN_SCTP");
        logger.debug(TextEvent.Topic.PROTOCOL, "isNodeUseSCTP : ", isNodeUseSCTP);
        node_settings.setUseSCTP(isNodeUseSCTP);
        boolean isNodeUseTCP = Config.getConfigByName("diameter.properties").getBoolean("listenpoint.LISTEN_TCP");
        logger.debug(TextEvent.Topic.PROTOCOL, "isNodeUseTCP : ", isNodeUseTCP);
        node_settings.setUseTCP(isNodeUseTCP);
        long idleTimeout = Config.getConfigByName("diameter.properties").getInteger("node.IDLE_TIMEOUT");
        logger.debug(TextEvent.Topic.PROTOCOL, "idleTimeout : ", idleTimeout);
        node_settings.setIdleTimeout(idleTimeout);
        long watchdogInterval = Config.getConfigByName("diameter.properties").getInteger("node.WATCHDOG_INTERVAL");
        logger.debug(TextEvent.Topic.PROTOCOL, "watchdogInterval : ", watchdogInterval);
        node_settings.setWatchdogInterval(watchdogInterval);
        
        // set peers list
        TransportProtocol protocol = Peer.TransportProtocol.tcp;
        String transport = Config.getConfigByName("diameter.properties").getString("DEFAULT_TRANSPORT");
        logger.debug(TextEvent.Topic.PROTOCOL, "diameterTransport : ", transport);
        if (StackFactory.PROTOCOL_SCTP.equals(transport)) {
            protocol = Peer.TransportProtocol.sctp;
        }
        String serverUrl = Config.getConfigByName("diameter.properties").getString("DEFAULT_SERVER_URL");
        URI diamUrl = URIFactory.newURI(serverUrl);
        Peer peer = new Peer(diamUrl.getHost(), diamUrl.getPort(), protocol);
        peer.capabilities = node_settings.capabilities();
        try {
            diameterManager = new DiameterManager(node_settings,  new Peer[]{peer});
        } catch (Exception e) {
            logger.error(TextEvent.Topic.PROTOCOL, e, "Enable to open TCP connexion for DIAMETER protocol to host : ", e);
            e.printStackTrace();
        } 
    }

    /**
     * junit setup method.
     * @throws Exception if any problem occurs.
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        
        diameterManager.stop();
    }
       
    /**
     * testXMLParsedMessage() method.
     *
     */
    public void testXMLParsedClient() throws Exception {       
        Element scElem = readMessageFromXml("../conf/diameter/diameterMessage.xml");
                      
        String applicationId = scElem.element("header").attributeValue("applicationId");
        MsgDiameterParser.getInstance().doDictionnary(scElem, applicationId, true);
        MsgDiameter request = MsgDiameterParser.getInstance().parseMsgFromXml(true, scElem);
        
        int maxIter = Config.getConfigByName("diameter.properties").getInteger("NB_ITERATION");
        logger.debug(TextEvent.Topic.PROTOCOL, "maxIter : ", maxIter);
        Message resp = null;
        for (int i = 0; i < maxIter; i++) {
            // request = MsgDiameterParser.getInstance().parseMsgFromXml(true, scElem);
            resp = diameterManager.sendDiameterRequest(request);
        }
        
        assertNotNull(resp);
        AVP avp = resp.find(ProtocolConstants.DI_RESULT_CODE);
        assertNotNull(avp);       
        
        assertEquals(((AVP_Integer32)new AVP_Integer32(avp)).queryValue(), ProtocolConstants.DIAMETER_RESULT_SUCCESS);

    }
    /**
     * test3gppSAR() method.
     *
     */
    public void testserver() throws Exception {
    
        System.out.println("Hit enter to terminate server");
        System.in.read();
    }

    /**
     * add the SupportedVendor AVPs to the capability from the diameter.properties config file
     * @throws Exception if any problem occurs.
     */
    private void addSupportedVendor(Capability capability) throws Exception {
        int i = 0;
        boolean error = false;
        while (!error) {
            try {
                int value = Config.getConfigByName("diameter.properties").getInteger("capability.SUPPORTED_VENDOR." + i);
                capability.addSupportedVendor(value );
                i = i + 1;
            } catch (Exception e) {
                error = true;
            }
        }   
    }
    
    /**
     * add the AuthApp AVPs to the capability from the diameter.properties config file
     * @throws Exception if any problem occurs.
     */
    private void addAuthApp(Capability capability) throws Exception {
        int i = 0;
        boolean error = false;
        while (!error) {
            try {
                int value  = Config.getConfigByName("diameter.properties").getInteger("capability.AUTH_APPLICATION." + i);
                capability.addAuthApp(value);
                i = i + 1;
            } catch (Exception e) {
                error = true;
            }
        }
    }

    /**
     * add the AcctApp AVPs to the capability from the diameter.properties config file
     * @throws Exception if any problem occurs.
     */
    private void addAcctApp(Capability capability) throws Exception {
        int i = 0;
        boolean error = false;
        while (!error) {
            try {
                int value = Config.getConfigByName("diameter.properties").getInteger("capability.ACCT_APPLICATION." + i);
                capability.addAcctApp(value);
                i = i + 1;
            } catch (Exception e) {
                error = true;
            }
        }
    }

    /**
     * add the VendorAuthApp AVPs to the capability from the diameter.properties config file
     * @throws Exception if any problem occurs.
     */
    private void addVendorAuthApp(Capability capability) throws Exception {
        int i = 0;
        boolean error = false;
        while (!error) {
            try {
                String value  = Config.getConfigByName("diameter.properties").getString("capability.VENDOR_AUTH_APPLI." + i);
                int pos = value.indexOf(",");
                if (pos < 0) {
                    error =true;
                }
                int vendor = Integer.parseInt(value.substring(0, pos));
                int appli = Integer.parseInt(value.substring(pos + 1));
                capability.addVendorAuthApp(vendor, appli);
                i = i + 1;
            } catch (Exception e) {
                error = true;
            }
        }
    }

    /**
     * add the VendorAcctApp AVPs to the capability from the diameter.properties config file
     * @throws Exception if any problem occurs.
     */
    private void addVendorAcctApp(Capability capability) throws Exception {
        int i = 0;
        boolean error = false;
        while (!error) {
            try {
                String value  = Config.getConfigByName("diameter.properties").getString("capability.VENDOR_ACCT_APPLI." + i);
                int pos = value.indexOf(",");
                if (pos < 0) {
                    error =true;
                }
                int vendor = Integer.parseInt(value.substring(0, pos));
                int appli = Integer.parseInt(value.substring(pos + 1));
                capability.addVendorAcctApp(vendor, appli);
                i = i + 1;
            } catch (Exception e) {
                error = true;
            }
        }
    }

    /**
     * read the message from XML scenario
     * @param    request The request to send
     * @return The answer to the request. Null if there is no answer (all peers down, or other error)
     */
    private Element readMessageFromXml(String fileName) throws Exception {
        String filename = "../conf/diameter/diameterMessage.xml" ;

        // Instanciate the XML parser
        //TODO
        SAXReader reader = new SAXReader(false);
        reader.setEntityResolver(new XMLLoaderEntityResolver());
        
        File file = new File(filename);
        org.dom4j.Document doc = reader.read(file);
    
        // scenario level
        return (Element) doc.getRootElement().elements().get(0);
    }

}
