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

package com.devoteam.srit.xmlloader.diameter;

import java.util.Iterator;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.Trans;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import dk.i1.diameter.AVP;
import dk.i1.diameter.AVP_Grouped;
import dk.i1.diameter.AVP_Integer32;
import dk.i1.diameter.AVP_OctetString;
import dk.i1.diameter.Message;
import dk.i1.diameter.node.Capability;
import dk.i1.diameter.node.ConnectionKey;
import dk.i1.diameter.node.NodeSettings;
import dk.i1.diameter.node.Peer;
import dk.i1.diameter.node.Peer.TransportProtocol;

/**
 *
 * @author fhenry
 */
public class ListenpointDiameter extends Listenpoint
{
    private long startTimestamp = 0;
    
	private NodeSettings node_settings;
	
    private DiameterNodeManager diameterNode = null;
	
    /** Creates a new instance of Listenpoint */
    public ListenpointDiameter(Stack stack) throws Exception
    {
        super(stack);
        Capability capability = createCapability(null);
        this.node_settings = createNodeSettings(capability, null, null);               
    }

	/** Creates a Listenpoint specific from XML tree*/
	public ListenpointDiameter(Stack stack, Element root) throws Exception	
	{
		super(stack, root);

		MsgDiameterParser.getInstance().doDictionnary(root, "0", true);
		
		Message capabilityMessage = new Message();
		MsgDiameterParser.getInstance().parseAllAVPs(capabilityMessage, root);
        
		Element element = root.element("nodeSettings");
    			
        Capability capability = createCapability(capabilityMessage);
        this.node_settings = createNodeSettings(capability, capabilityMessage, element);
	}

    /** create a listenpoint  */
    @Override
    public boolean create(String protocol) throws Exception    
    {
    	if (this.getListenTCP())
    	{
    		StatPool.beginStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_TCP, protocol);
    	}
    	else if (this.getListenSCTP())
    	{
    		StatPool.beginStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_SCTP, protocol);
    	}
    	this.protocol = protocol;
    	this.startTimestamp = System.currentTimeMillis();
    	this.diameterNode = new DiameterNodeManager(node_settings, this);
    	return true;
    }
	
    /** Remove a listenpoint */
    @Override
    public boolean remove()
    {
    	if (this.getListenTCP())
    	{
    		StatPool.endStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_TCP, getProtocol(), startTimestamp);
    	}
    	else if (this.getListenSCTP())
    	{
    		StatPool.endStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_SCTP, getProtocol(), startTimestamp);
    	}
    	
    	try 
    	{
    		if(null != diameterNode) 
    		{
    			diameterNode.reset();
            }
    	}
        catch (Exception e ) 
        {
        	// nothing to do
        }
    	diameterNode = null;
        return true;
    }

    
    /** Send a Msg to a given destination with a given transport protocol */
    @Override
    public synchronized boolean sendMessage(Msg msg, String remoteHost, int remotePort, String transport) throws Exception
    {
		if (remoteHost == null || !msg.isRequest())
		{
	    	// RFC 3588 : Hop-by-Hop Id is used instead of Destination-Host or Destination-Realm AVP
	    	Trans trans = msg.getTransaction();
	    	if (trans != null)
	    	{
	    		Msg request = trans.getBeginMsg();
	    		ChannelDiameter channel = (ChannelDiameter) request.getChannel();
	    		msg.setChannel(channel);

	    		// send the DIAMETER answer
	    		diameterNode.sendAnswer(((MsgDiameter) msg).getMessage(), channel.getConnectionKey());
	    		return true;
	    	}
		}
    	if (remoteHost == null)
		{        	
    		// RFC 3588 : If peer identity == Destination-Host AVP then send that peer, otherwise
    		Parameter destHost = msg.getParameter("avp.293.value");
    		if ((destHost != null) && (destHost.length() > 0)) 
    		{
    			remoteHost = destHost.get(0).toString();	    			
    		}
    		else 
    		{
	    		// RFC 3588 : Lookup realm table with Destination-Realm and AppId
	    		Parameter destRealm = msg.getParameter("avp.283.value");
	    		if ((destRealm != null) && (destRealm.length() > 0)) 
	    		{
	    			remoteHost = destRealm.get(0).toString();	    			
	    		}	    			
    		}	    		
		}
		if (remoteHost == null)
		{
			throw new ExecutionException("Could not determine the remote destination from the message : " + msg);
		}

		// default port is 3868 (RFC3588) 
		if (remotePort <=0)
		{
			remotePort = 3868; 
		}

		// default port is 3868 (RFC3588) 
		if (transport == null)
		{
			transport = this.transport; 
		}

		// default transport is TCP (RFC3588)	
        TransportProtocol protocol = Peer.TransportProtocol.tcp; 
        if (StackFactory.PROTOCOL_SCTP.equalsIgnoreCase(transport)) {
            protocol = Peer.TransportProtocol.sctp;
        }
       
        // set peers list
        Peer peer = new Peer(remoteHost, remotePort, protocol);
        peer.capabilities = node_settings.capabilities();

		diameterNode.sendRequest((MsgDiameter) msg, peer);
        return true;
    }	
 
    /**
     * Create the capability object.
     * @return
     */
    private Capability createCapability(Message capabilityMessage) throws Exception {
        // set capability
        Capability capability = new Capability();
        addSupportedVendor(capability, capabilityMessage);
        addAuthApp(capability, capabilityMessage);
        addAcctApp(capability, capabilityMessage);
        addVendorAuthApp(capability, capabilityMessage);
        // addVendorAuthApp(capability, null);
        addVendorAcctApp(capability, capabilityMessage);
        return capability;
    }
    
    /**
     * Create the node settingds object.
     * @return
     */
    protected NodeSettings createNodeSettings(Capability capability, Message capabilityMessage, Element nodeSettingsElement) throws Exception {
        // set node settings
        String nodeHostId = this.getHost();
        if (nodeHostId == null || nodeHostId.equalsIgnoreCase("0.0.0.0"))
        {
        	// The listenpoint host can not be 0.0.0.0 due to the CER/CEA exchange mechanism
            nodeHostId = Utils.getLocalAddress().getHostAddress();
        }
        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "nodeHostId : ", nodeHostId);
        int nodePort = this.getPort();
        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "nodePort : ", nodePort);

        String nodeRealm = stack.getConfig().getString("node.REALM");
        if (capabilityMessage != null)
        {
        	// AVP Origin-Realm = 296
        	AVP avp = capabilityMessage.find(296);
        	if (avp != null)
        	{
        		nodeRealm = new String(new AVP_OctetString(avp).queryValue());
        	}
        }
        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "nodeRealm : ", nodeRealm);
        int nodeVendorId = stack.getConfig().getInteger("node.VENDOR_ID");
        if (capabilityMessage != null)
        {
        	// AVP Vendor-Id = 266
        	AVP avp = capabilityMessage.find(266);
        	if (avp != null)
        	{
        		nodeVendorId = new AVP_Integer32(avp).queryValue();
        	}
        }        
        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "nodeVendorId : ", nodeVendorId);
        String nodeProductName = stack.getConfig().getString("node.PRODUCT_NAME");
        if (capabilityMessage != null)
        {
        	// AVP Product-Name = 269
        	AVP avp = capabilityMessage.find(269);
        	if (avp != null)
        	{
        		nodeProductName = new String(new AVP_OctetString(avp).queryValue());
        	}
        }
        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "nodeProductName : ", nodeProductName);
        int nodeFirmwareRevision = stack.getConfig().getInteger("node.FIRMWARE_REVISION");
        if (capabilityMessage != null)
        {
        	// AVP Firmware-Revision = 267
        	AVP avp = capabilityMessage.find(267);
        	if (avp != null)
        	{
        		nodeFirmwareRevision = new AVP_Integer32(avp).queryValue();
        	}
        }                
        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "nodeFirmwareRevision : ", nodeFirmwareRevision);
        
        // DNS resolver : not done by the diameter stack
    	NodeSettings node_settings  = new NodeSettings(
                nodeHostId,
                nodeRealm,
                nodeVendorId,
                capability,
                nodePort,
                nodeProductName, 
                nodeFirmwareRevision);
        
        boolean isNodeUseSCTP = this.getListenSCTP();
        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "isNodeUseSCTP : ", isNodeUseSCTP);
        node_settings.setUseSCTP(isNodeUseSCTP);
        boolean isNodeUseTCP = this.getListenTCP();
        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "isNodeUseTCP : ", isNodeUseTCP);
        node_settings.setUseTCP(isNodeUseTCP);
        
        long idleTimeout = stack.getConfig().getInteger("node.IDLE_TIMEOUT");
        if (nodeSettingsElement != null)
        {
        	idleTimeout = Long.parseLong(nodeSettingsElement.attributeValue("idleTimeout"));
        }        
        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "idleTimeout : ", idleTimeout);
        node_settings.setIdleTimeout(idleTimeout);
        long watchdogInterval = stack.getConfig().getInteger("node.WATCHDOG_INTERVAL");
        if (nodeSettingsElement != null)
        {
        	watchdogInterval = Long.parseLong(nodeSettingsElement.attributeValue("watchdogInterval"));
        }                
        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "watchdogInterval : ", watchdogInterval);
        node_settings.setWatchdogInterval(watchdogInterval);
        
        return node_settings;
    }
    
    /**
     * add the SupportedVendor AVPs to the capability from the diameter.properties config file
     * @throws Exception if any problem occurs.
     */
    private void addSupportedVendor(Capability capability, Message capabilityMessage) throws Exception {
    	if ((capabilityMessage != null) && (capabilityMessage.size() > 0))
    	{
    		// AVP Vendor-Id = 266
    		Iterator<AVP> iter = capabilityMessage.iterator(266);
            while(iter.hasNext())
            {
                AVP avp = iter.next();
            	int value = new AVP_Integer32(avp).queryValue();
                capability.addSupportedVendor(value);
            }
    	}
    	else
    	{    	
	        int i = 0;
	        boolean error = false;
	        while (!error) {
	            try {
	                int value = stack.getConfig().getInteger("capability.SUPPORTED_VENDORID." + i);
	                capability.addSupportedVendor(value );
	                i = i + 1;
	            } catch (Exception e) {
	                error = true;
	            }
	        }
    	}
    }
    
    /**
     * add the AuthApp AVPs to the capability from the diameter.properties config file
     * @throws Exception if any problem occurs.
     */
    private void addAuthApp(Capability capability, Message capabilityMessage) throws Exception {
    	if (capabilityMessage != null && capabilityMessage.size() > 0)
    	{
    		// AVP Auth-Application-Id = 258
    		Iterator<AVP> iter = capabilityMessage.iterator(258);
            while(iter.hasNext())
            {
                AVP avp = iter.next();
            	int value = new AVP_Integer32(avp).queryValue();
                capability.addAuthApp(value);
            }
    	}
    	else
    	{
    		int i = 0;
	        boolean error = false;
	        while (!error) {
	            try {
	                int value  = stack.getConfig().getInteger("capability.AUTH_APPLICATION." + i);
	                capability.addAuthApp(value);
	                i = i + 1;
	            } catch (Exception e) {
	                error = true;
	            }
	        }
    	}
    }
    
    /**
     * add the AcctApp AVPs to the capability from the diameter.properties config file
     * @throws Exception if any problem occurs.
     */
    private void addAcctApp(Capability capability, Message capabilityMessage) throws Exception {
    	if (capabilityMessage != null && capabilityMessage.size() > 0)
    	{
    		// AVP Acct-Application-Id = 259
    		Iterator<AVP> iter = capabilityMessage.iterator(259);
            while(iter.hasNext())
            {
                AVP avp = iter.next();
            	int value = new AVP_Integer32(avp).queryValue();
            	capability.addAcctApp(value);
            }
    	}
    	else
    	{
	        int i = 0;
	        boolean error = false;
	        while (!error) {
	            try {
	                int value = stack.getConfig().getInteger("capability.ACCT_APPLICATION." + i);
	                capability.addAcctApp(value);
	                i = i + 1;
	            } catch (Exception e) {
	                error = true;
	            }
	        }
    	}
    }
    
    /**
     * add the VendorAuthApp AVPs to the capability from the diameter.properties config file
     * @throws Exception if any problem occurs.
     */
    private void addVendorAuthApp(Capability capability, Message capabilityMessage) throws Exception {
    	if (capabilityMessage != null && capabilityMessage.size() > 0)
    	{
    		// AVP Vendor-Specific-Application-Id = 260
    		Iterator<AVP> iter = capabilityMessage.iterator(260);
            while(iter.hasNext())
            {
                AVP avp = iter.next();
                AVP[] avpTab = (new AVP_Grouped(avp)).queryAVPs();
                int vendor = -1;
                int appli = -1;
                if (avpTab[0].code == 266)
                {
                	vendor = new AVP_Integer32(avpTab[0]).queryValue();
                }
                if (avpTab[1].code == 266)
                {
                	vendor = new AVP_Integer32(avpTab[1]).queryValue();
                }
                if (avpTab[0].code == 258)
                {
                	appli = new AVP_Integer32(avpTab[0]).queryValue();
                }
                if (avpTab[1].code == 258)
                {
                	appli = new AVP_Integer32(avpTab[1]).queryValue();
                }
                if (vendor >= 0 && appli >= 0)
                {          
                	capability.addVendorAuthApp(vendor, appli);
                }
            }
    	}
    	else
    	{
	        int i = 0;
	        boolean error = false;
	        while (!error) {
	            try {
	                String value  = stack.getConfig().getString("capability.VENDOR_AUTH_APPLI." + i);
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
    }
    
    /**
     * add the VendorAcctApp AVPs to the capability from the diameter.properties config file
     * @throws Exception if any problem occurs.
     */
    private void addVendorAcctApp(Capability capability, Message capabilityMessage) throws Exception {
    	if (capabilityMessage != null && capabilityMessage.size() > 0)
    	{
    		// AVP Vendor-Specific-Application-Id = 260
    		Iterator<AVP> iter = capabilityMessage.iterator(260);
            while(iter.hasNext())
            {
                AVP avp = iter.next();
                AVP[] avpTab = (new AVP_Grouped(avp)).queryAVPs();
                int vendor = -1;
                int appli = -1;
                if (avpTab[0].code == 266)
                {
                	vendor = new AVP_Integer32(avpTab[0]).queryValue();
                }
                if (avpTab[1].code == 266)
                {
                	vendor = new AVP_Integer32(avpTab[1]).queryValue();
                }
                if (avpTab[0].code == 259)
                {
                	appli = new AVP_Integer32(avpTab[0]).queryValue();
                }
                if (avpTab[1].code == 259)
                {
                	appli = new AVP_Integer32(avpTab[1]).queryValue();
                }
                if (vendor >= 0 && appli >= 0)
                {          
                	capability.addVendorAcctApp(vendor, appli);
                }
            }
    	}
    	else
    	{    	
	        int i = 0;
	        boolean error = false;
	        while (!error) {
	            try {
	                String value  = stack.getConfig().getString("capability.VENDOR_ACCT_APPLI." + i);
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
    }
    
}