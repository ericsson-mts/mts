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

import com.devoteam.srit.xmlloader.diameter.dictionary.Application;
import com.devoteam.srit.xmlloader.diameter.dictionary.AvpDef;
import com.devoteam.srit.xmlloader.diameter.dictionary.CommandDef;
import com.devoteam.srit.xmlloader.diameter.dictionary.Dictionary;
import com.devoteam.srit.xmlloader.diameter.dictionary.TypeDef;
import com.devoteam.srit.xmlloader.diameter.dictionary.VendorDef;
import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.utils.DateUtils;
import com.devoteam.srit.xmlloader.core.utils.UnsignedInt32;
import com.devoteam.srit.xmlloader.core.utils.UnsignedInt64;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import dk.i1.diameter.AVP;
import dk.i1.diameter.AVP_Address;
import dk.i1.diameter.AVP_Float32;
import dk.i1.diameter.AVP_Float64;
import dk.i1.diameter.AVP_Grouped;
import dk.i1.diameter.AVP_Integer32;
import dk.i1.diameter.AVP_Integer64;
import dk.i1.diameter.AVP_OctetString;
import dk.i1.diameter.AVP_Time;
import dk.i1.diameter.AVP_Unsigned32;
import dk.i1.diameter.AVP_Unsigned64;
import dk.i1.diameter.Message;
import dk.i1.diameter.MessageHeader;
import gp.utils.arrays.Array;

import java.net.InetAddress;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Element;

/**
 *
 * @author gpasquiers
 */
public class MsgDiameterParser
{
    
    private static MsgDiameterParser  msgDiameterParser = null;
    
    /** Creates or returns the instance of this stack */
    public static MsgDiameterParser getInstance()
    {
        if (null == msgDiameterParser)
        {
            msgDiameterParser = new MsgDiameterParser();
        }
        return msgDiameterParser;
    }
    
    /** Creates a new Msg object from the root XML element */
    public Message parseMsgFromXml(Boolean request, Element root) throws Exception
    {
        Message msgDiameter = new Message();
        
        // parse the <header> XML tag
        parseMessageHeader(msgDiameter.hdr, request, root);
        
        // Parse recursively the <avp> XML tags
        parseAllAVPs(msgDiameter, root);
        
        return msgDiameter;
    }

    /** Parses then returns the header from the XML root element */
    private void parseMessageHeader(MessageHeader messageHeader, Boolean request, Element root) throws Exception
    {               
        // Parse the header tag
        Element element = root.element("header");
        
        // header tag is mandatory
        if(null == element)
        {
        	throw new ParsingException("There is no <header> XML tag in the DIAMETER message.");
        }
        
        // parse the Application id
        String applicationId = element.attributeValue("applicationId");
        // error if not specified
        if (applicationId == null)
        {
        	throw new ParsingException("There is no \"applicationId\" attribute in the <header> XML element :" + element);
        }                
        Application appli = parse_ApplicationId(applicationId, true);
        if (appli != null)
        {
        	applicationId = Integer.toString(appli.get_id());
        }
        else
        {
    	    int pos = applicationId.lastIndexOf(":");
    	    if (pos >= 0)
    	    {
    	    	applicationId = applicationId.substring(pos + 1);
    	    }
	
        }
        long appliIdCode = Long.parseLong(applicationId);
        messageHeader.application_id = (int) appliIdCode;
        
        // parse the Command code
        messageHeader.command_code = parse_CommandCode(element, applicationId);
        
        // Parse the Request : DEPRECATED
    	String strRequest = element.attributeValue("request");
        if (strRequest != null)
        {
        	request = Utils.parseBoolean(strRequest, "request");
        }
        if(request == null)
        {
        	request = false;
        }
        messageHeader.setRequest(request);
        
        // Parse the Proxyable
        String proxyable = element.attributeValue("proxiable");
        if (proxyable == null)
        {
        	proxyable = element.attributeValue("p");
        }
        if (proxyable != null)
        {
        	messageHeader.setProxiable(Utils.parseBoolean(proxyable, "proxiable"));
        }
        
        // Parse the Error
        String error = element.attributeValue("error");
        if (error == null)
        {
        	error = element.attributeValue("e");
        }
        if (error != null)
        {
        	messageHeader.setError(Utils.parseBoolean(error, "error"));
        }
        
        // Parse the Retransmit flag
        String retransmit = element.attributeValue("retransmit");
        if (retransmit == null)
        {
        	retransmit = element.attributeValue("r");
        }        
        if (retransmit != null)
        {
        	messageHeader.setRetransmit(Utils.parseBoolean(retransmit, "retransmit"));
        }
        
        // Parse the Flags : NOT USED
        if (element.attributeValue("flags") != null)
        {
        	throw new ParsingException("Flags attribute is currently not supported in <header> tag");
        }
        
        // Parse the Version : NOT USED        
        if(element.attributeValue("version") != null)
        {
        	throw new ParsingException("Version attribute is currently not supported in <header> tag");
        }
        
        // Parse the EndToEnd
        String endToEnd = element.attributeValue("endToEnd");
        if (endToEnd != null)
        {
        	messageHeader.end_to_end_identifier = Integer.parseInt(endToEnd);
        }
        else
        {
        	messageHeader.end_to_end_identifier = IDProvider.nextId();
        }

        // Parse the HopByHop
        String hopByHop = element.attributeValue("hopByHop");
        if (hopByHop != null)
        {
        	messageHeader.hop_by_hop_identifier = Integer.parseInt(hopByHop);
        }
        else
        {
        	messageHeader.hop_by_hop_identifier = IDProvider.nextId();
        }
    }
        

    /** Parses then returns all the AVPs from the XML root element */
    public void parseAllAVPs(Message message, Element root) throws Exception
    {    
    	List<Element> avpList = root.elements("avp");
	    for(Element avpElement:avpList)
	    {
	    	AVP avp = parseAVP(message, avpElement);
	    	if (avp != null)
	    	{
	    		message.add(avp);
	    	}
	    }
    }
    
    /** Parses an element <avp>; recursively if it contains other AVPs, used by parseMessage */
    private AVP parseAVP(Message message, Element element) throws Exception
    {
        AVP avp;

        String applicationId= Integer.toString(message.hdr.application_id);
                
        // Parse the AVP state flag
        String stateAttr = element.attributeValue("state");
        boolean stateBool = true;
        if (stateAttr != null)
        {
            stateBool = Utils.parseBoolean(stateAttr, "state");
        }
        if (!stateBool)
        {
        	return null;
        }

        // Parse the AVP Vendor ID
        String vendorIdAttr = element.attributeValue("vendorId");
        VendorDef vendorDef = parse_AVPVendorId(vendorIdAttr, applicationId);
        String vendorId = "0";				// use to make AVP search using vendorId
        if (vendorDef != null)
        {
        	vendorId = Integer.toString(vendorDef.get_code());
        }
        
        // Parse the AVP Code
        String codeAttr = element.attributeValue("code");
        // error if not specified
        if (codeAttr == null)
        {
        	throw new ParsingException("There is no \"code\" attribute for the AVP : " + " in the XML element : " + element);
        }
        AvpDef avpDef = parseAVP_Code(codeAttr, applicationId, vendorId);
        int code = -1;
        if (avpDef != null)
        {
        	code = avpDef.get_code();
        }
        else
        {
        	code = (int) (Long.parseLong(codeAttr) & 0xffffffffL);
        }
        
        // Parse the AVP type
        String type = element.attributeValue("type");
        TypeDef typeDef = parse_AVPType(type, applicationId);
        if (type == null && typeDef == null && avpDef != null)
        {
        	typeDef = avpDef.get_type();
        }
        String typeBase = type;        
        if (typeDef != null)
        {
        	type = typeDef.get_type_name();
            while (typeDef.get_type_parent() != null)
            {
                typeDef = typeDef.get_type_parent();
            }
            typeBase = typeDef.get_type_name();
        }

        List<Element> listSubAVPs = element.elements("avp");
        // Parse the Grouped AVP
        if (!listSubAVPs.isEmpty())
        {
        	// Parse the AVP value
        	String value = element.attributeValue("value");
            //error if specified 
            if (value != null)
            {
            	throw new ParsingException("You should not have a \"value\" attribute because the AVP is grouped for the AVP : " + code + " in the AVP : " + code + " in the XML element : " + element);
            }                

            // Parse child AVPs
            List<AVP> avpList = new LinkedList<AVP>();        
            for(Element e:listSubAVPs)
            {
    	    	AVP subAvp = parseAVP(message, e);
    	    	if (subAvp != null)
    	    	{
    	    		avpList.add(subAvp);
    	    	}
            }
            
            // Create the AVP and add parsed childs to grouped AVP
            AVP_Grouped gAvp = new AVP_Grouped((int) code, avpList.toArray(new AVP[0]));
            avp = gAvp ;
        }
        else
        {            
            // default value if the type is not specified and the AVP not known in the dictionary
            if (type == null) 
            {
            	throw new ParsingException("The AVP is not known in the dictionary and there is no \"type\" attribute for the AVP : " + code + " in the XML element : " + element);
            }

        	// Parse the AVP value
        	String value = element.attributeValue("value");
            //error if not specified 
            if (value == null)
            {
            	throw new ParsingException("The \"value\" attribute is mandatory because the AVP is not grouped for the AVP : " + code + " in the XML element : " + element);
            }                

            // Parse the value for the vendorId AVP
            if ("VendorId".equalsIgnoreCase(type) || code == 266 || code == 265)
            {
            	VendorDef vendorDefValue = parse_AVPVendorId(value, applicationId);
            	if (vendorDefValue != null)
            	{
            		value = Integer.toString(vendorDefValue.get_code());	
            	}
            }

            if ("AppId".equalsIgnoreCase(type) || code == 258 || code == 259 )
            {
            	Application applicationValue = parse_ApplicationId(value, false);
            	if (applicationValue != null)
            	{ 
            		value = Integer.toString(applicationValue.get_id());
            	}
            	
            }
            // replace escape XML character
            value = Utils.unescapeXMLEntities(value);
            
            // Create the AVP
            if ("IPAddress".equalsIgnoreCase(type) || "IPAddress".equalsIgnoreCase(typeBase))
            {
            	byte[] val = null;
            	if (value.contains(".") || value.contains(":"))
            	{
            		val = InetAddress.getByName(value).getAddress();
            	}
            	else
            	{
            		throw new ParsingException("The \"value\" attribute does not contain a valid IP address : " + value + " for the AVP : " + code + " in the element " + element);
            	}
                avp = new AVP_OctetString(code, val);
            }
            else if ("Address".equalsIgnoreCase(type) || "Address".equalsIgnoreCase(typeBase))
            {
            	byte[] val = null;
            	if (value.contains(".") || value.contains(":"))
            	{
            		val = InetAddress.getByName(value).getAddress();
            		avp = new AVP_OctetString(code, val);
            	}
            	else
            	{
            		throw new ParsingException("The \"value\" attribute does not contain a valid IP address : " + value + " for the AVP : " + code + " in the XML element " + element);
            	}
            }
            else if	("Time".equalsIgnoreCase(type) || "Time".equalsIgnoreCase(typeBase))
            {
            	long time = DateUtils.parseDate(value);
            	Date date = new Date (time);
                avp = new AVP_Time(code, date);
            }
            else if ("UTF8String".equalsIgnoreCase(type) || "UTF8String".equalsIgnoreCase(typeBase))
            {
                avp = new AVP_OctetString(code, value.getBytes("UTF-8"));
            }
            // base types
            else if ("OctetString".equalsIgnoreCase(typeBase))
            {
                avp = new AVP_OctetString(code, value.getBytes());
            }
            else if ("Integer32".equalsIgnoreCase(typeBase))
            {
            	value = parse_AVPEnumValue(value, avpDef);
                avp = new AVP_Integer32(code, (int) Long.parseLong(value));
            }
            else if ("Integer64".equalsIgnoreCase(typeBase))
            {
            	value = parse_AVPEnumValue(value, avpDef);
                avp = new AVP_Integer64(code,Long.parseLong(value));
            }
            else if ("Unsigned32".equalsIgnoreCase(typeBase))
            {
            	value = parse_AVPEnumValue(value, avpDef);
            	UnsignedInt32 unsignedInt32 = new UnsignedInt32(value);
                avp = new AVP_Unsigned32(code,unsignedInt32.intValue());
            }
            else if	("Unsigned64".equalsIgnoreCase(typeBase))
            {
            	value = parse_AVPEnumValue(value, avpDef);
                UnsignedInt64 unsignedInt64 = new UnsignedInt64(value);
                avp = new AVP_Unsigned64(code,unsignedInt64.longValue());
            }
            else if ("Float32".equalsIgnoreCase(typeBase))
            {
                Float float32 = Float.parseFloat(value);
                avp = new AVP_Float32(code, float32);
            }
            else if	("Float64".equalsIgnoreCase(typeBase))
            {
                double double64 = Double.parseDouble(value);
                avp = new AVP_Float64(code, double64);
            }
            else if ("Binary".equalsIgnoreCase(typeBase))
            {
            	byte[] bytes = Utils.parseBinaryString("h" + value);
            	avp = new AVP_OctetString(code, bytes);
            }
            else
            {
                throw new ParsingException("No matching AVP type " + type + " for the AVP : " + code + " in the XML element " + element);
            }
        }
        // if vendor Id is not specified then we take it from the avpDef (dictionary)
        if (vendorDef ==  null)
        {
        	if (avpDef !=  null)
        	{
        		vendorDef = avpDef.get_vendor_id();
        	}
        }
        // and set it in the avp object
        avp.vendor_id = 0;
    	if (vendorIdAttr != null && vendorDef == null)
    	{
    		String vendorIdCodeString = vendorIdAttr;
    		int pos = vendorIdCodeString.lastIndexOf(":");
    	    if (pos >= 0)
    	    {
    	    	vendorIdCodeString = vendorIdAttr.substring(pos + 1);
    	    }
    	    avp.vendor_id = (int) Long.parseLong(vendorIdCodeString);
    	}
    	else if (vendorDef !=  null)
        {
        	avp.vendor_id = vendorDef.get_code();
        }
        
        // Parse AVP flags
        String mandatoryAttr = element.attributeValue("mandatory");
        if (mandatoryAttr == null)
        {
        	mandatoryAttr = element.attributeValue("m");
        }
        boolean mandatoryBool = false;
        if (mandatoryAttr != null)
        {
        	mandatoryBool = Utils.parseBoolean(mandatoryAttr, "m[andatory]");
        }
        else
        {
        	if (avpDef != null && "mustnot".equals(avpDef.get_mandatory()))
        	{
        		mandatoryBool = false;
        	}
        	else
        	{
        		mandatoryBool = true;
        	}
        }
        avp.setMandatory(mandatoryBool);
        
        String privateAttr = element.attributeValue("private");
        if (privateAttr == null)
        {
        	privateAttr = element.attributeValue("p");
        }
        boolean privateBool = false;
        if (privateAttr != null)
        {
        	Utils.parseBoolean(privateAttr, "p[rivate]");
        }
        else
        {
        	if (avpDef != null && "mustnot".equals(avpDef.get_protected()))
        	{
        		privateBool = false;
        	}
        	else
        	{
        		privateBool = true;
        	}
        }
        avp.setPrivate(privateBool);        
        
        return avp ;
    }
        
    /** 
     * Parses the AVP code from XML element and perform dictionary change 
     */
    private AvpDef parseAVP_Code(String codeAttr, String applicationId, String vendorId) throws Exception
    {                    
	    int pos = codeAttr.lastIndexOf(":");
	    AvpDef avpDef = null;
	    if (pos >= 0)
	    {
	    	String codeLabel = codeAttr.substring(0, pos);
	    	String codeInt = codeAttr.substring(pos + 1);
	    	int code = Integer.parseInt(codeInt);
	        avpDef = Dictionary.getInstance().getAvpDefByCodeVendorIdORCode(code, applicationId, vendorId);
	        if (avpDef != null && !codeLabel.equals(avpDef.get_name()))
	        {
	        	GlobalLogger.instance().getApplicationLogger().warn(Topic.PROTOCOL, 
	        			"Parsing : For the AVP code, the label \"" + codeLabel + "\" does not match the code \"" + avpDef.get_code() + "\" in the dictionary; " +
	        			"we assume the code is \"" + avpDef.get_code() + " and we are waiting the label \"" + avpDef.get_name() + "\".");

	        }
	    }
	    else
	    {
	        if(!Utils.isInteger(codeAttr))
	        {
	            avpDef = Dictionary.getInstance().getAvpDefByNameVendorIdORName(codeAttr, applicationId, vendorId);
	            if (avpDef == null)
	            {
	            	throw new ParsingException("The avp \"" + codeAttr + "\" is not found in the dictionary.");        	
	            }
	        }
	        else
	        {
	        	int code = (int) (Long.parseLong(codeAttr) & 0xffffffffL);
	        	//int code = (int) Long.parseLong(codeAttr);
	        	avpDef = Dictionary.getInstance().getAvpDefByCodeVendorIdORCode(code, applicationId, vendorId);
	        }
	    }
	    return avpDef;
    }
 
    /** 
     * Parses the Command code from XML element and perform dictionary change 
     */
    private int parse_CommandCode(Element element, String applicationId) throws Exception
    {    
        String commandAttr = element.attributeValue("command");
        // error if not specified
        if (commandAttr == null)
        {
        	throw new ParsingException("There is no \"command\" attribute in the <header> XML element : " + element);
        }                

	    int pos = commandAttr.lastIndexOf(":");
	    int code = -1;
	    if (pos >= 0)
	    {
	    	String codeLabel = commandAttr.substring(0, pos);
	    	String codeInt = commandAttr.substring(pos + 1);
	    	code = Integer.parseInt(codeInt);
	    	CommandDef commandDef = Dictionary.getInstance().getCommandDefByCode(code, applicationId);
	        if (commandDef != null && !codeLabel.equals(commandDef.get_name()))
	        {
	        	GlobalLogger.instance().getApplicationLogger().warn(Topic.PROTOCOL, 
	        		"Parsing : For the header command code, the label \"" + codeLabel + "\" does not match the code \"" + commandDef.get_code() + "\" in the dictionary; " +
	        		"we assume the header command code is \"" + commandDef.get_code() + " and we are waiting the label \"" + commandDef.get_name() + "\".");	        	
	        }
	    }
	    else
	    {
	        if(!Utils.isInteger(commandAttr))
	        {
	        	CommandDef commandDef = Dictionary.getInstance().getCommandDefByName(commandAttr, applicationId);
	            if (commandDef == null)
	            {
	            	throw new ParsingException("The command code \"" + commandAttr + "\" is not found in the dictionary.");        	
	            }
	            code = commandDef.get_code(); 
	        }
	        else
	        {
	        	code = Integer.parseInt(commandAttr);
	        }
	    }
	    return code;
    }

    /** 
     * Parses the Application Id from XML element and perform dictionary change 
     */
    private Application parse_ApplicationId(String appliIdAttr, boolean checkExist) throws Exception
    {    
    	Application application = null;
	    int pos = appliIdAttr.lastIndexOf(":");
	    if (pos >= 0)
	    {
	    	String codeLabel = appliIdAttr.substring(0, pos);
	    	String codeInt = appliIdAttr.substring(pos + 1);
	    	long code = Long.parseLong(codeInt);
	    	application = Dictionary.getInstance().getApplicationById((int) code);
	    	if (application != null && !codeLabel.equals(application.get_name()))
	        {
	        	GlobalLogger.instance().getApplicationLogger().warn(Topic.PROTOCOL, 
	        		"Parsing : For the application id, the label \"" + codeLabel + "\" does not match the id \"" + application.get_id() + "\" in the dictionary; " +
	        		"we assume the application id is \"" + application.get_id() + " and we are waiting the label \"" + application.get_name() + "\".");
	        }
	    }
	    else
	    {
	        if(!Utils.isInteger(appliIdAttr))
	        {
	        	application = Dictionary.getInstance().getApplicationByName(appliIdAttr);
	            // not a problem because Application-Id is also an enumeration AVP
	        	if (application == null)
	            {
	        		if (checkExist)
	        		{
	        			throw new ParsingException("The application id \"" + appliIdAttr + "\" is not found in the dictionary.");
	        		}
	        		else
	        		{
	        			return null;
	        		}
	            } 
	        }
	        else
	        {
	        	long code = Long.parseLong(appliIdAttr);
	        	application = Dictionary.getInstance().getApplicationById((int) code);
	        }
	    }
	    return application;
    }

    /** 
     * Parses the AVP Vendor Id from XML element and perform dictionary change 
     */
    private VendorDef parse_AVPVendorId(String vendorIdAttr, String applicationId) throws Exception
    {    
    	if (vendorIdAttr == null)
    	{
    		return null;
    	}
    	
	    int pos = vendorIdAttr.lastIndexOf(":");
	    VendorDef vendorDef = null;
	    if (pos >= 0)
	    {
	    	String codeLabel = vendorIdAttr.substring(0, pos);
	    	String codeInt = vendorIdAttr.substring(pos + 1);
	    	long code = Long.parseLong(codeInt);
	    	vendorDef = Dictionary.getInstance().getVendorDefByCode((int) code, applicationId);
	        if (vendorDef != null && 
	        	!codeLabel.equals(vendorDef.get_vendor_id()) &&
	        	!codeLabel.equals(vendorDef.get_name()))
	        {
	        	GlobalLogger.instance().getApplicationLogger().warn(Topic.PROTOCOL, 
	        		"Parsing : For the vendor ID, the label \"" + codeLabel + "\" does not match the id \"" + vendorDef.get_code() + "\" in the dictionary; " +
	        		"we assume the header command code is \"" + vendorDef.get_code() + " and we are waiting the label \"" + vendorDef.get_name() + "\".");	        	
	        }
	    }
	    else
	    {
	        if(!Utils.isInteger(vendorIdAttr))
	        {
	        	vendorDef = Dictionary.getInstance().getVendorDefByName(vendorIdAttr, applicationId);
	            if (vendorDef == null)
	            {
	            	throw new ParsingException("The vendor id \"" + vendorIdAttr + "\" is not found in the dictionary.");        	
	            }
	        }
	        else
	        {
	        	long code = Long.parseLong(vendorIdAttr);
	        	vendorDef = Dictionary.getInstance().getVendorDefByCode((int) code, applicationId);
	        }
	    }
	    return vendorDef;
    }

    /** 
     * Parses the AVP Enum value from XML element and perform dictionary change 
     */
    private String parse_AVPEnumValue(String enumValue, AvpDef avpDef) throws Exception
    {    
	    int pos = enumValue.lastIndexOf(":");
	    long code;
	    if (pos >= 0)
	    {
	    	String codeLabel = enumValue.substring(0, pos);
	    	String codeLong = enumValue.substring(pos + 1);
	    	code = Long.parseLong(codeLong);
	    	String name = avpDef.getEnumNameByCode(codeLong);
	        if (!codeLabel.equals(name))
	        {
	        	GlobalLogger.instance().getApplicationLogger().warn(Topic.PROTOCOL, 
	        		"Parsing : For the AVP enumeration code, the label \"" + codeLabel + "\" does not match the code \"" + codeLong + "\" in the dictionary; " +
	        		"we assume the AVP enumeration code is \"" + code + " and we are waiting the label \"" + name + "\".");
	        }
	    }
	    else
	    {
	        if(!Utils.isInteger(enumValue) && avpDef.isEnumerated())
	        {
	        	code = avpDef.getEnumCodeByName(enumValue);
	            if (code < 0)
	            {	            	 
	            	throw new ParsingException("The AVP enum value \"" + enumValue + "\" is not found in the dictionary.");        	
	            } 
	        }
	        else
	        {
	        	code = Long.parseLong(enumValue);
	        }
	    }
	    return Long.toString(code);
    }

    /** 
     * Parses the AVP Vendor Id from XML element and perform dictionary change 
     */
    public TypeDef parse_AVPType(String typeAttr, String applicationId) throws Exception
    {    
    	if (typeAttr == null)
    	{
    		return  null;
    	}
    	TypeDef typeDef = Dictionary.getInstance().getTypeDefByName(typeAttr, applicationId);
        if (typeDef == null && !"grouped".equalsIgnoreCase(typeAttr.toLowerCase()) &&
        		!"binary".equalsIgnoreCase(typeAttr.toLowerCase()))
        {
        	throw new ParsingException("The type id \"" + typeAttr + "\" is not found in the dictionary.");        	
        }
	    return typeDef;
    }

    
}


