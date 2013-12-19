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

package com.devoteam.srit.xmlloader.radius;

import com.devoteam.srit.xmlloader.core.ParameterPool;

import com.devoteam.srit.xmlloader.core.Runner;
import org.dom4j.Element;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.core.utils.XMLElementTextMsgParser;
import com.devoteam.srit.xmlloader.sip.ListenpointSip;

import gp.net.radius.data.AVPBytes;
import gp.net.radius.data.AVPInteger;
import gp.net.radius.data.RadiusMessage;
import gp.net.radius.data.AVPString;
import gp.net.radius.data.AVPVendorSpecific;
import gp.net.radius.dictionary.RadiusAttributes;
import gp.net.radius.dictionary.RadiusCodes;
import gp.net.radius.dictionary.RadiusDictionary;
import gp.net.radius.dictionary.RadiusValues;
import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.SupArray;
import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

/**
 *
 * @author 
 */
public class StackRadius extends Stack
{
    private RadiusDictionary radiusDictionary;
    
    /** Constructor */
    public StackRadius() throws Exception
    {
        super();
        this.radiusDictionary = new RadiusDictionary(new File("../conf/radius/dictionary"));
        
        // initiate a default listenpoint if port is not empty or null
        /*
        int port = getConfig().getInteger("listenpoint.LOCAL_PORT", 0);
        if (port > 0)
        {
        	Listenpoint listenpoint = new ListenpointRadius(this);
            createListenpoint(listenpoint, StackFactory.PROTOCOL_RADIUS);
        }
        */
    }

	/** Creates a Listenpoint specific to each Stack */
    @Override
	public synchronized Listenpoint parseListenpointFromXml(Element root) throws Exception 
	{
        String name = root.attributeValue("name");
        Listenpoint listenpoint = getListenpoint(name);
        if (listenpoint != null)
            return listenpoint;
        else
            return new ListenpointRadius(this, root);
	}

    /** Creates a Channel specific to each Stack */
    // deprecated part //
    @Override
    public Channel parseChannelFromXml(Element root, String protocol) throws Exception
    {
        String socketName = root.attributeValue("socketName");
        String localHost  = root.attributeValue("localHost");
        String localPort  = root.attributeValue("localPort");
        String remoteHost = root.attributeValue("remoteHost");
        String remotePort = root.attributeValue("remotePort");
        String secret     = root.attributeValue("secret");

        if (existsChannel(socketName))
        {
            return getChannel(socketName);
        }
        else
        {
            if(null != localHost) localHost = InetAddress.getByName(localHost).getHostAddress();
            else                  localHost = "0.0.0.0";

            if(null != remoteHost) remoteHost = InetAddress.getByName(remoteHost).getHostAddress();

            return new ChannelRadius(socketName, localHost, localPort, remoteHost, remotePort, protocol, secret);
        }
    }
    // deprecated part //

    private AVPBytes parseAVP(Integer vendorCode, Element avp) throws Exception
    {
        String avpCodeAttribute = avp.attributeValue("code");
        String avpTypeAttribute = avp.attributeValue("type");
        String avpValueAttribute = avp.attributeValue("value");

        RadiusAttributes radiusAttributes = this.radiusDictionary.getRadiusVendors().getRadiusAttributes(vendorCode);

        /*
         * Read this AVP code, if the code is not an integer, then try to decode
         * it using the dictionary for the given vendorCode.
         */ 
        Integer avpCode;
        if (!Utils.isInteger(avpCodeAttribute))
        {
            if(null == radiusAttributes) throw new Exception("Could not get attribute list from dictionary for vendor" + vendorCode);

            avpCode = radiusAttributes.getAttributeCode(avpCodeAttribute);
            
            if(null == avpCode) throw new Exception("Could not get code from dictionary for avp named" + avpCodeAttribute);
        }
        else
        {
            avpCode = Integer.valueOf(avpCodeAttribute);
        }
        
        /*
         * Override the AVP type to vendor-specific if the code is 26
         */
        if(26 == avpCode)
        {
            avpTypeAttribute = "vendor-specific";
        }
        
        /*
         * Read this AVP type. if the type is not set, try to read it from the
         * dictionary, based on the AVP code.
         */
        if(null == avpTypeAttribute)
        {
            if(null == radiusAttributes) throw new Exception("Could not get attribute list from dictionary for vendor" + vendorCode);

            avpTypeAttribute = radiusAttributes.getAttributeType(avpCode);

            if(null == avpTypeAttribute) throw new Exception("Could not get type from dictionary for avp " + avp.asXML() + " please extend the dictionary or set the type in XML");
        }
        
        /*
         * Safety check, for now only vendor-specific data can contain data
         * defined using <data> or <avp> elements.
         */
        if (26 != avpCode && (null != avp.element("avp") || null != avp.element("data")))
        {
            throw new ParsingException("A non vendor-specific AVP can't contain vendor-specific avp nor data");
        }

        /*
         * Now create the AVP object depending on type.
         */
        
        if (avpTypeAttribute.equalsIgnoreCase("vendor-specific"))
        {
             /*
              * Create a vendor-specific AVP.
              */
            String vendorDataVendorAttribute = avpValueAttribute;

            Integer vendorDataVendor;
            if(!Utils.isInteger(vendorDataVendorAttribute)) vendorDataVendor = this.radiusDictionary.getRadiusVendors().getVendorCode(vendorDataVendorAttribute);
            else                                            vendorDataVendor = Integer.parseInt(vendorDataVendorAttribute);

            if(null == vendorDataVendor) throw new Exception("Could not get vendor code for vendor named " + vendorDataVendorAttribute);
            
            SupArray vendorData = new SupArray();
            
            for(Object object:avp.elements())
            {
                Element element = (Element) object;
                if(element.getName().equals("data"))
                {
                    String vendorDataValueAttribute = element.attributeValue("value");
                    DefaultArray vendorDataValueArray = new DefaultArray(Utils.parseBinaryString(vendorDataValueAttribute));
                    vendorData.addLast(vendorDataValueArray);
                }
                else
                {
                    vendorData.addLast(this.parseAVP(vendorDataVendor, element).getArray());
                }
            }
            
            return new AVPVendorSpecific(26, vendorDataVendor, vendorData);
        }
        else if (avpTypeAttribute.equalsIgnoreCase("integer") || avpTypeAttribute.equalsIgnoreCase("date"))
        {
            Integer avpValue;
            
            /*
             * If the value is not an integer then try to decode it using the
             * dictionary (contains list of enumerated values).
             */
            if (!Utils.isInteger(avpValueAttribute))
            {
                if(null == radiusAttributes) throw new Exception("Could not get attribute list from dictionary for vendor" + vendorCode);
                RadiusValues radiusValues = radiusAttributes.getAttributeRadiusValues(avpCode);
                if (null == radiusValues) throw new Exception("There is no input in dictionary for value " + avpValueAttribute + " of avp " + avp.asXML());
                avpValue = radiusValues.getValueCode(avpValueAttribute);
                if (null == avpValue) throw new Exception("There is no input in dictionary for value " + avpValueAttribute + " of avp " + avp.asXML());
            }
            else
            {
                avpValue = (int)Long.parseLong(avpValueAttribute);
            }
            return new AVPInteger(avpCode, avpValue);
        }
        else if (avpTypeAttribute.equalsIgnoreCase("octets") || avpTypeAttribute.equalsIgnoreCase("octet"))
        {
            return new AVPBytes(avpCode, new DefaultArray(Utils.parseBinaryString(avpValueAttribute)));
        }
        else if (avpTypeAttribute.equalsIgnoreCase("ipaddr"))
        {
            return new AVPBytes(avpCode, new DefaultArray(InetAddress.getByName(avpValueAttribute).getAddress()));
        }
        else if (avpTypeAttribute.equalsIgnoreCase("text") || avpTypeAttribute.equalsIgnoreCase("string"))
        {
            return new AVPString(avpCode, avpValueAttribute, "UTF8");
        }
        else
        {
            throw new ParsingException("Unknown AVP type : " + avpTypeAttribute);
        }
    }
    
	/** Creates a specific Msg */
	@Override
    public Msg parseMsgFromXml(Boolean request, Element root, Runner runner) throws Exception
    {
        RadiusMessage radiusMessage = new RadiusMessage();

        // header
        Element header = root.element("header");
        String codeAttribute = header.attributeValue("code");
        String authenticatorAttribute = header.attributeValue("authenticator");
        String identifierAttribute = header.attributeValue("identifier");

        if(Utils.isInteger(codeAttribute))
        {
            radiusMessage.setCode(Integer.valueOf(codeAttribute));
        }
        else
        {
            RadiusCodes radiusCodes = this.radiusDictionary.getRadiusCodes();
            Integer code = radiusCodes.getCode(codeAttribute);
            if(null != code) radiusMessage.setCode(code);
            else throw new Exception("Unknown message code " + codeAttribute);
        }

        List<Element> avps = root.elements("avp");
        for(Element avp:avps)
        {
            radiusMessage.addAVP(this.parseAVP(-1, avp));
        }
        
        String name = root.attributeValue("listenpoint");
        boolean isListenpoint = true;
        // deprecated part //
        if(name == null)
        {
            name = root.attributeValue("socketName");
            isListenpoint = false;
        }
        // deprecated part //
        String remoteHost = root.attributeValue("remoteHost");
        String remotePort = root.attributeValue("remotePort");

        if((null != remoteHost) && (null != remotePort))
        {
            InetAddress inetAddress = InetAddress.getByName(remoteHost);
            InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, Integer.parseInt(remotePort));
            
            radiusMessage.setRemoteAddress(inetSocketAddress);
        }

        MsgRadius msgRadius = new MsgRadius(radiusMessage);
        ListenpointRadius listenpoint = null;
        ChannelRadius channel = null;
        
        if(null != identifierAttribute)
        {
            radiusMessage.setIdentifier(Integer.valueOf(identifierAttribute));
        }

        if(isListenpoint)
        {
            listenpoint = (ListenpointRadius) getListenpoint(name);
            if (listenpoint == null)
            {
                throw new ExecutionException("StackRadius: The listenpoint " + name + " does not exist");
            }
            if((null == remoteHost) || (null == remotePort))
            {
            	throw new ExecutionException("StackRadius: The \"remoteXXX\" attributes are mandatory; please define them when sending the message.");
            }
            if(radiusMessage.getIdentifier() == 0) radiusMessage.setIdentifier(listenpoint.getIdentifierHandler().getIdentifier());
            msgRadius.setListenpoint(listenpoint);
        }
        else// deprecated part //
        {
        	channel = (ChannelRadius) getChannel(name);
            if (channel == null)
            {
                throw new ExecutionException("StackRadius: The channel " + name + " does not exist");
            }
            if(radiusMessage.getIdentifier() == 0) radiusMessage.setIdentifier(channel.getIdentifierHandler().getIdentifier());
            msgRadius.setChannel(channel);
        }
        // deprecated part //
        

        if(isListenpoint)
        {
            radiusMessage.setSecret(listenpoint.getSecret());
            if(null == radiusMessage.getRemoteAddress() && null != listenpoint.getHost() && 0 != listenpoint.getPort())
            {
                InetAddress inetAddress = InetAddress.getByName(listenpoint.getHost());
                InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, listenpoint.getPort());
                radiusMessage.setRemoteAddress(inetSocketAddress);
            }
        }
        else// deprecated part //
        {
            radiusMessage.setSecret(channel.getSecret());
            if (radiusMessage.getRemoteAddress() == null)
            {
                InetAddress inetAddress = InetAddress.getByName(channel.getRemoteHost());
                InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, channel.getRemotePort());
                radiusMessage.setRemoteAddress(inetSocketAddress);
            }
            if ((channel.getRemoteHost() ==  null) || (channel.getRemotePort() == 0))
            {
            	channel.setRemoteHost(radiusMessage.getRemoteAddress().getAddress().getHostAddress());
            	channel.setRemotePort(radiusMessage.getRemoteAddress().getPort());
            }

        }// deprecated part //

        if(null != authenticatorAttribute)
        {
            radiusMessage.setAuthenticator(new DefaultArray(Utils.parseBinaryString(authenticatorAttribute)));
        }
        else
        {
            if(msgRadius.isRequest())
            {
                radiusMessage.computeRequestAuthenticator();
            }
            else
            {
                MsgRadius msgRadiusRequest = (MsgRadius) this.getInTransaction(msgRadius.getTransactionId()).getBeginMsg();
                
                radiusMessage.computeResponseAuthenticator(msgRadiusRequest.getRadiusMessage().getAuthenticator());
            }
        }
                
             
        // encode the User-Password AVPs if necessary
        if(this.getConfig().getBoolean("radius.ENCODE_USER_PASSWORD", true))
        {
            if(1 == radiusMessage.getCode())
            {
                GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL , "ChannelRadius, encode user-password avps");
                radiusMessage.encodeUserPasswordAvps();
            }
        }

        return msgRadius;
    }
    
    /** Returns the Config object to access the protocol config file*/
    public Config getConfig() throws Exception
    {
        return Config.getConfigByName("radius.properties");
    }

    /** Returns the replacer used to parse sendMsg Operations */
    public XMLElementReplacer getElementReplacer()
    {
        return XMLElementTextMsgParser.instance();
    }

    public RadiusDictionary getRadiusDictionary()
    {
        return radiusDictionary;
    }
}
