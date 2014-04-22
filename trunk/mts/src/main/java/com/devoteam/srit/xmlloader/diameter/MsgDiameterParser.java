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
import com.devoteam.srit.xmlloader.core.utils.UnsignedInt32;
import com.devoteam.srit.xmlloader.core.utils.UnsignedInt64;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import dk.i1.diameter.AVP;
import dk.i1.diameter.AVP_Address;
import dk.i1.diameter.AVP_Grouped;
import dk.i1.diameter.AVP_Integer32;
import dk.i1.diameter.AVP_Integer64;
import dk.i1.diameter.AVP_OctetString;
import dk.i1.diameter.AVP_Unsigned32;
import dk.i1.diameter.AVP_Unsigned64;
import dk.i1.diameter.Message;
import dk.i1.diameter.MessageHeader;

import java.net.InetAddress;
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
    public MsgDiameter parseMsgFromXml(Boolean request, Element root) throws Exception
    {
        MsgDiameter msgDiameter = new MsgDiameter(new Message());

        //!!deprecated part!!//
        String server = root.attributeValue("server");
        if (server != null)
        {
       		GlobalLogger.instance().logDeprecatedMessage( root.getName() + " server=\"xxx\" .../", "sendMessageDiameter remoteUrl=\"xxx\" .../");
       		msgDiameter.setRemoteUrl(server);
       	}
        //!!deprecated part!!//
        
        // parse the message after we set the channel (for hop-by-hop computing)
        parseMessage(msgDiameter, request, root);

        return msgDiameter;
    }
    

    /** Parses then returns an AVP Message from the XML root element */
    private Message parseMessage(MsgDiameter msgDiameter, Boolean request, Element root) throws Exception
    {
        
        Message message = msgDiameter.getMessage();
        
        // parse the <header> XML tag
        parseMessageHeader(message.hdr, request, root);
        
        // Parse recursively the <avp> XML tags
        parseAllAVPs(message, root);
        
        return message ;
    }
    

    /** Parses then returns the header from the XML root element */
    private void parseMessageHeader(MessageHeader messageHeader, Boolean request, Element root) throws Exception
    {               
        //
        // Parse header tag
        //
        Element element = root.element("header");
        
        //
        // header tag is mandatory
        //
        if(null == element) throw new ParsingException("can't get header tag");
        
        //
        // applicationId
        //
        String applicationId = element.attributeValue("applicationId");
        messageHeader.application_id = Integer.parseInt(applicationId);
        
        //
        // Command code
        //
        String commandCode = element.attributeValue("command");
        messageHeader.command_code = Integer.parseInt(commandCode) ;
        
        //
        // Request
        //
    	String strRequest = element.attributeValue("request");
        if (strRequest != null)
        {
        	request = Boolean.parseBoolean(strRequest);
        }
        if(request == null)
        {
        	request = false;
        }
        messageHeader.setRequest(request);
        
        //
        // Proxyable
        //
        String proxyable = element.attributeValue("proxiable");
        if (proxyable != null)
        {
        	messageHeader.setProxiable(Boolean.parseBoolean(proxyable));
        }
        
        //
        // Error
        //
        String error = element.attributeValue("error");
        if (error != null)
        {
        	messageHeader.setError(Boolean.parseBoolean(error));
        }
        
        //
        // Retransmit
        //
        String retransmit = element.attributeValue("retransmit");
        if (retransmit != null)
        {
        	messageHeader.setRetransmit(Boolean.parseBoolean(retransmit));
        }
        
        //
        // Flags attribute is not used
        //
        if(null != element.attributeValue("flags")) throw new ParsingException("Flags attribute is currently not supported in <header> tag");
        
        //
        // Version attribute is not used
        //
        if(null != element.attributeValue("version")) throw new ParsingException("Version attribute is currently not supported in <header> tag");
        
        //
        // EndToEnd
        //
        String endToEnd = element.attributeValue("endToEnd");
        if (endToEnd != null)
        {
        	messageHeader.end_to_end_identifier = Integer.parseInt(endToEnd);
        }
        else
        {
        	messageHeader.end_to_end_identifier = IDProvider.nextId();
        }

        //
        // HopByHop
        //
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
	    	AVP avp = parseAvp(avpElement);
	    	if (avp != null)
	    	{
	    		message.add(avp);
	    	}
	    }
    }
    
    /** Parses an element <avp>; recursively if it contains other AVPs, used by parseMessage */
    private AVP parseAvp(Element element) throws Exception
    {
        AVP avp ;
        
        // avp attributes
        String code_str      = element.attributeValue("code");
        String value         = element.attributeValue("value");
        String type          = element.attributeValue("type");
        String vendor_id_str = element.attributeValue("vendorId");
        String mandatory_str = element.attributeValue("mandatory");
        String private_str   = element.attributeValue("private");
        String state         = element.attributeValue("state");
        
        boolean bState = true;
        if (state != null && state.equalsIgnoreCase("false"))
        {
        	bState = false;
        }
        if (!bState)
        {
        	return null;
        }        
        
        int    vendor_id    = 0 ;
        int    code         = 0 ;
        
        boolean mandatory_bool = false ;
        boolean private_bool = false ;
        
        // error if no code is specified
        if(null == code_str) throw new ParsingException("In element :" + element + "\n" + "no avp code");
        
        // flags attribute is not used
        if(null != element.attributeValue("flags")) throw new ParsingException("In element :" + element + "\n" + "flags attribute is currently not supported in avp tag");
        
        // vendorSpecific attribute is not used
        if(null != element.attributeValue("vendorSpecific")) throw new ParsingException("In element :" + element + "\n" + "VendorSpecific attribute is currently not supported in avp tag");
        
        if(null == type) {
        	type="OctetString";
        }
        
        //
        // AVP Code
        //
        code = Integer.parseInt(code_str);
        
        //
        // AVP Mandatory Flag
        //
        if(null != mandatory_str)
        {
            mandatory_bool = Boolean.parseBoolean(mandatory_str);
        }
        
        //
        // AVP Private Flag
        //
        if(null != private_str)
        {
            private_bool = Boolean.parseBoolean(private_str);
        }
        
        //
        // AVP Vendor ID
        //
        if(null != vendor_id_str)
        {
            vendor_id = Integer.parseInt(vendor_id_str);
        }
        
        //
        // Grouped AVP
        //
        if(type.equalsIgnoreCase("grouped"))
        {
            //
            // Parse child avps
            //
            List<AVP> avpList = new LinkedList<AVP>();
            List<Element> list = element.elements("avp");
            for(Element e:list)
            {
    	    	AVP subAvp = parseAvp(e);
    	    	if (subAvp != null)
    	    	{
    	    		avpList.add(subAvp);
    	    	}
            }
            
            // create the AVP
            // add parsed childs to grouped AVP
            AVP_Grouped gAvp = new AVP_Grouped(code, avpList.toArray(new AVP[0]));
            avp = gAvp ;
        }
        else
        {
            //
            // Error if no value is specified
            //
            if(null == value) throw new ParsingException("In element :" + element + "\n" + "AVP of code " + code_str + " should have a value since it is not a grouped AVP");
            //
            // Create the AVP
            //
            if(type.equalsIgnoreCase("OctetString"))
            {
                try
                {
                    avp = new AVP_OctetString(code, Utils.parseBinaryString(value));
                }
                catch(Exception e)
                {
                    avp = new AVP_OctetString(code, value.getBytes());
                }
            }
            else if(type.equalsIgnoreCase("IPAddress"))
            {
                avp = new AVP_OctetString(code, InetAddress.getByName(value).getAddress());
            }
            else if(type.equalsIgnoreCase("UTF8String"))
            {
                avp = new AVP_OctetString(code, value.getBytes());
            }
            else if(type.equalsIgnoreCase("Integer32"))
            {
                avp = new AVP_Integer32(code,Integer.parseInt(value));
            }
            else if(type.equalsIgnoreCase("Integer64"))
            {
                avp = new AVP_Integer64(code,Long.parseLong(value));
            }
            else if(type.equalsIgnoreCase("Unsigned32"))
            {
            	UnsignedInt32 unsignedInt32 = new UnsignedInt32(value);
                avp = new AVP_Unsigned32(code,unsignedInt32.intValue());
            }
            else if(type.equalsIgnoreCase("Unsigned64"))
            {
                UnsignedInt64 unsignedInt64 = new UnsignedInt64(value);
                avp = new AVP_Unsigned64(code,unsignedInt64.longValue());
            }
            else
            {
                throw new ParsingException("no matching avp type in protocol stack for type " + type);
            }
        }
        
        avp.vendor_id = vendor_id ;
        avp.setMandatory(mandatory_bool);
        avp.setPrivate(private_bool);
        
        return avp ;
    }
    
    public void doDictionnary(Element root, String applicationId, boolean recurse) throws ParsingException
    {
        Application application = Dictionary.getInstance().getApplication(applicationId);
        
        if(null == application)
        {
           throw new ParsingException("Unknown \"applicationId\" attribute in header: " + applicationId) ;
        }
        
        Element unmodifiedRoot = root.createCopy();
        
        if(root.getName().equalsIgnoreCase("header"))
        {
            //
            // ApplicationId
            //
            String attributeValue;
            
            attributeValue = root.attributeValue("applicationId");
            if(!Utils.isInteger(attributeValue))
            {
                root.attribute("applicationId").setValue(Integer.toString(application.get_id()));
            }
            
            //
            // CommandCode
            //
            attributeValue = root.attributeValue("command");
            if(!Utils.isInteger(attributeValue))
            {
                CommandDef commandDef = Dictionary.getInstance().getCommandDefByName(attributeValue, applicationId);
                if (commandDef == null)
                {
                	throw (new ParsingException("Unknown \"command\" attribute in header: " + attributeValue + "skipp it"));
                }
                root.attribute("command").setValue(Integer.toString(commandDef.get_code()));
            }
            
        }
        else if(root.getName().equalsIgnoreCase("avp"))
        {
            boolean isTypeAppId    = false ;
            boolean isTypeVendorId = false ;
            String attributeValue;
            
            attributeValue = root.attributeValue("code");
            //
            // Set default values implied by code in XMLTree from dictionnary
            //
            if(null != attributeValue)
            {
                AvpDef avpDef ;
                if(!Utils.isInteger(attributeValue))
                {
                    avpDef = Dictionary.getInstance().getAvpDefByName(attributeValue, applicationId);
                }
                else
                {
                    avpDef = Dictionary.getInstance().getAvpDefByCode(Integer.parseInt(attributeValue), applicationId);
                }
                
                if(null == avpDef)
                {
                    //
                    // If the code value is an integer, we don't necessary have to know it in the dictionnary.
                    // However, if it isn't, we have to.
                    //
                }
                
                //
                // Handle the code attribute
                //
                if(null != avpDef)
                {
                    root.addAttribute("code", Integer.toString(avpDef.get_code()));
                }
                
                //
                // Handle the type attribute
                //
                if(null == root.attribute("type") && null != avpDef)
                {
                    TypeDef typeDef = avpDef.get_type();
                    if(null != typeDef)
                    {
                        while(null != typeDef.get_type_parent())
                        {
                            if(typeDef.get_type_name().equalsIgnoreCase("AppId"))     isTypeAppId = true;
                            if(typeDef.get_type_name().equalsIgnoreCase("VendorId"))  isTypeVendorId = true;
                            typeDef = typeDef.get_type_parent();
                        }
                        root.addAttribute("type", typeDef.get_type_name());
                    }
                }
                
                //
                // Handle the vendorId attribute
                //
                if(null == root.attribute("vendorId") && null != avpDef)
                {
                    VendorDef vendorDef = avpDef.get_vendor_id();
                    if(null != vendorDef)
                    {
                        root.addAttribute("vendorId", Integer.toString(vendorDef.get_code()));
                    }
                }
                
                //
                // Handle the mandatory attribute
                //
                if(null == root.attribute("mandatory"))
                {
                    if(null != avpDef && null != avpDef.get_mandatory() && avpDef.get_mandatory().equals("mustnot"))
                    {
                        root.addAttribute("mandatory", "false");
                    }
                    else
                    {
                        root.addAttribute("mandatory", "true");
                    }
                }
                
                //
                // Handle the private attribute
                //
                if(null == root.attribute("private") && null != avpDef)
                {
                    if(null != avpDef && null != avpDef.get_protected() && avpDef.get_protected().equals("mustnot"))
                    {
                        root.addAttribute("private", "false");
                    }
                    else
                    {
                        root.addAttribute("private", "true");
                    }
                }
                
                //
                // Parse the enumerated value that could be present in "value"
                //
                if(null != root.attribute("value") && null != avpDef)
                {
                    String enumName = root.attributeValue("value");
                    long enumValue = avpDef.getEnumCodeByName(enumName);
                    if(enumValue != -1)
                    {
                        root.attribute("value").setValue(Long.toString(enumValue));
                    }
                }
            }
            else
            {
                throw new ParsingException("in element: " + unmodifiedRoot + "\n" + "code is a mandatory attribute");
            }
            
            //
            // Set the vendorId code (in case it isn't referenced by the avp Code via dictionnary, or overwritten).
            //
            attributeValue = root.attributeValue("vendorId");
            if(null != attributeValue)
            {
                if(!Utils.isInteger(attributeValue))
                {
                    VendorDef vendorDef = Dictionary.getInstance().getVendorDefByName(attributeValue, applicationId);
                    if(null != vendorDef)
                    {
                        root.attribute("vendorId").setValue(Integer.toString(vendorDef.get_code()));
                    }
                    else
                    {
                        throw new ParsingException("in element: " + unmodifiedRoot + "\n" + attributeValue + " is not a valid vendor id in element");
                    }
                }
            }
            
            //
            // Set the top-parent type (in case it isn't referenced by the avp Code via dictionnary, or overwritten).
            //
            if(root.elements().size() > 0)
            {
                root.addAttribute("type", "grouped");
            }

            attributeValue = root.attributeValue("type");
            if(null != attributeValue)
            {
                if(!attributeValue.equalsIgnoreCase("grouped"))
                {
                    if(null != attributeValue)
                    {
                        TypeDef typeDef = Dictionary.getInstance().getTypeDefByName(attributeValue, applicationId);
                        if(null != typeDef)
                        {
                            while(null != typeDef && null != typeDef.get_type_parent())
                            {
                                if(typeDef.get_type_name().equalsIgnoreCase("AppId"))     isTypeAppId = true;
                                if(typeDef.get_type_name().equalsIgnoreCase("VendorId"))  isTypeVendorId = true;
                                typeDef = typeDef.get_type_parent();
                            }
                            root.attribute("type").setValue(typeDef.get_type_name());
                        }
                        else
                        {
                            throw new ParsingException("In element: " + unmodifiedRoot + "\n" + attributeValue + " is not a valid type");
                        }
                    }

                }
            }
            
            //
            // Handle the value in case it is an appId or vendorId avp, enum should have already been handled at this point
            //
            attributeValue = root.attributeValue("value");
            if(null != attributeValue)
            {
                if(isTypeAppId)
                {
                    Application anApplication = Dictionary.getInstance().getApplication(attributeValue);
                    if(null != anApplication)
                    {
                        root.attribute("value").setValue(Integer.toString(anApplication.get_id()));
                    }
                }
                if(isTypeVendorId)
                {
                    VendorDef vendorDef = Dictionary.getInstance().getVendorDefByName(attributeValue, applicationId);
                    if(null != vendorDef)
                    {
                        root.attribute("value").setValue(Integer.toString(vendorDef.get_code()));
                    }
                }
            }
            else
            {
                if(!root.attributeValue("type").equalsIgnoreCase("grouped"))
                {
                    throw new ParsingException("in element: " + unmodifiedRoot + "\n" + "value is a mandatory attribute for element <avp .../> if it is not a grouped avp");
                }
            }
        }
        
        if(recurse)
        {
            List<Element> list = root.elements();
            for(Element element:list)
            {
                doDictionnary(element, applicationId, recurse);
            }
        }
    }
    
}
