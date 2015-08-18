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
import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import dk.i1.diameter.AVP;
import dk.i1.diameter.AVPInterface;
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
import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;


import org.dom4j.Element;
/**
 *
 * @author gpasquiers
 */
public class MsgDiamCommon extends Msg
{
    
    /** diameter message object */
    private Message message;
    
    /** Creates a new instance */
    public MsgDiamCommon(Stack stack)
    {
    	super(stack);
    }
    
    /** Creates a new instance */
    public MsgDiamCommon(Stack stack, Message aMessage)
    {
        this(stack);
        message = aMessage;
        setListenpoint(StackDiamCommon.listenpoint); 
    }
    
    /** Returns the diameter message of MsgDiameter */
    public Message getMessage()
    {
        return message ;
    }
    
	//-----------------------------------------------------------------------------------------
	// generic methods for protocol request type result retransmission, transaction and session
	//-----------------------------------------------------------------------------------------
                
    /** 
     * Return true if the message is a request else return false
     */
    @Override
    public boolean isRequest()
    {
        return message.hdr.isRequest();
    }
    
    /** 
     * Get the type of the message
     * Used for message filtering with "type" attribute and for statistic counters 
     */
    @Override
    public String getType()
    {
        return getCodeString() + ":" + message.hdr.command_code + "";
    }
    
    /** get the command as a label from the dictionary */
    private String getCodeString()
    {
        /** commandCode */
    	String codeString; 
        try
        {
            codeString = Dictionary.getInstance().getCommandDefByCode(message.hdr.command_code, Integer.toString(message.hdr.application_id)).get_name();
        }
        catch(Exception e)
        {
            codeString = "Unknown";
        }
	    return codeString;
    }
    
    /** Get the complete type of this message*/
    @Override
    public String getTypeComplete() throws Exception
    {
	    return getType();
    }

    /** 
     * Get the result of the message (null if request)
     * Used for message filtering with "result" attribute and for statistic counters 
     */
    @Override
    public String getResult() throws Exception
    {
        // get Result-Code value
        Parameter var = getParameter("avp.268.value");
        if (var.length() > 0) 
        {
            return (String) var.get(0);
        }
        
        // get Experimental-Result:Experimental-Result-Code
        var = getParameter("avp.297.298.value");
        //if (var != null) {
        if(var != null && var.length() > 0)
        {
            return (String) var.get(0);
        }
        
        return null ;
    }

    /** Get the complete result of this answer (null if request) */
    @Override
    public String getResultComplete() throws Exception
    {
        String applicationId = Integer.toString(message.hdr.application_id);
        // get Result-Code value
        Parameter var = getParameter("avp.268.value");
        if ((var != null) && (var.length() > 0)) {
        	String valueInt = (String) var.get(0);
            AvpDef avpDef = Dictionary.getInstance().getAvpDefByCodeVendorIdORCode(268, applicationId, null);
        	String valueName = avpDef.getEnumNameByCode(valueInt);
            return valueName + ":" + valueInt;
        }
        
        // get Experimental-Result:Experimental-Result-Code
        var = getParameter("avp.297.298.value");
        if ((var != null) && (var.length() > 0)) {
        	String valueInt = (String) var.get(0);
            AvpDef avpDef = Dictionary.getInstance().getAvpDefByCodeVendorIdORCode(298, applicationId, null);
        	String valueName = avpDef.getEnumNameByCode(valueInt);
            return valueName + ":" + valueInt;
        }
        
        return null ;
    }
     
    /**
     *  Tell whether the message shall begin a new session 
     * (= false by default) 
     */
    @Override
    public boolean beginSession() throws Exception
    {
    	String status = getResult();
    	if (status != null && (!status.equals("")))
    	{
    		int statusCode = new Integer(status).intValue();
            if (statusCode < 2000 && statusCode >= 3000)
            {
            	return false;
            } 
    	}
    	
        String type = getType();
        if ("Session-Termination:275".equalsIgnoreCase(type))
        {
            return false;
        }
    	
        // get Session-Id:263
        Parameter var = getParameter("avp.263.value");
        if ((var != null) && (var.length() > 0)) 
        {
            return true;
        }
        return false;
    }

    /**
     *  Tell whether the message shall end a session 
     * (= false by default) 
     */
    @Override
    public boolean endSession() throws Exception
    {
        String type = getType();
        if ("Session-Termination:275".equalsIgnoreCase(type))
        {
            return true;
        }
        if ("Accounting:271".equalsIgnoreCase(type))
        {
            // get Accounting-Record-Type:480 AVP
            Parameter var = getParameter("avp.480.value");
            if ((var != null) && (var.length() > 0)) 
            {
                String strvalue = (String) var.get(0);
                int intVal = new Integer(strvalue).intValue();
                // value STOP_RECORD:4
                if (intVal == 4)
                {
                    return true;
                }
            }
        }
        if ("Credit-Control:272".equalsIgnoreCase(type))
        {
            // get CC-Request-Type:416 AVP
            Parameter var = getParameter("avp.416.value");
            if ((var != null) && (var.length() > 0)) 
            {
                String strvalue = (String) var.get(0);
                int intVal = new Integer(strvalue).intValue();
                // value TERMINATION_REQUEST:3
                if (intVal == 3)
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    //-------------------------------------------------
    // methods for the encoding / decoding of the message
    //-------------------------------------------------
    
    /** 
     * encode the message to binary data 
     */
    @Override
    public byte[] encode()
    {
    	return message.encode();
    }
    
    /** decode the message from binary data */
    @Override
    public void decode(byte[] data) throws Exception
    {
        Message message = new Message();
        message.decode(data);        
        this.message = message;
    }

    
    //---------------------------------------------------------------------
    // methods for the XML display / parsing of the message
    //---------------------------------------------------------------------

    /** Returns a short description of the message. Used for logging as INFO level */
    /** This methods HAS TO be quick to execute for performance reason */
    @Override
    public String toShortString() throws Exception 
    {          
    	String ret = super.toShortString();
  		ret += "\n";
  		ret += "<HEADER "; 	
        String applicationId = Integer.toString(message.hdr.application_id);
    	ret += "applicationId=\"" + getApplicationIdString(applicationId) + ":" + message.hdr.application_id + "\" ";
    	ret += "hopByHop=\"" + message.hdr.hop_by_hop_identifier + "\" ";
    	ret += "endToEnd=\"" + message.hdr.end_to_end_identifier + "\"";	    	
    	ret += "/>";
        return ret;
    }
    
    /** get the applicationId as a label from the dictionary */
    private String getApplicationIdString(String applicationId)
    {
    	String applicationIdString;
	    try
	    {
	        applicationIdString = Dictionary.getInstance().getApplication(applicationId).get_name();
	    }
	    catch(Exception e)
	    {
	        applicationIdString = "Unknown";
	    }
	    return applicationIdString;
    }
        
    private static String avpToStringSafe(AVP avp, int indent)
    {
        String ret = "";
        ret += Utils.indent(indent);
        
        ret += "<avp code=\"" + avp.code + "\" type=\"OctetString\" ";
        ret += "value=\"";
    	Array array = new DefaultArray(AVPInterface.encode(avp));
		ret += Array.toHexString(array);
		ret += "\" />\n";
		
        return ret;
    }
    
    /**
     * prints as String an avp and it's sub-avps (recursive)
     */
    private String avpToXml(AVP avp, int indent, String applicationId) throws Exception
    {
        String ret = "" ;
        
        // retrieve the vendorId code
        String vendorIdCode = Integer.toString(avp.vendor_id);

        // retrieve the type of avp
        AvpDef avpDef = Dictionary.getInstance().getAvpDefByCodeVendorIdORCode(avp.code, applicationId, vendorIdCode);
                
        // retrieve the top-parent type
        String type = "OctetString";
        String typeBase = "OctetString";
        TypeDef typeDef = null;        
        if (avpDef != null)
        {
            typeDef = avpDef.get_type();
            if (typeDef != null)
            {
            	type = typeDef.get_type_name();
            }
            while (typeDef != null && typeDef.get_type_parent() != null)
            {
                typeDef = typeDef.get_type_parent();
            }
            if (typeDef != null)
            {
            	typeBase = typeDef.get_type_name();
            }
        }
        
        if(null != avpDef && avpDef.getGroupedAvpNameList().size()!=0)
        {
        	type = "Grouped";
        	typeBase = "Grouped";
        }
        
        ret += Utils.indent(indent) + "<avp";
        
        // retrieve the AVP label and code
        int code= avp.code;
        String label = "Unknown";
        if (avpDef != null)
        {
        	label = avpDef.get_name();
        	code = avpDef.get_code();
        }
        String name = label + ":" + code;
        ret += " code=\"" + name + "\"";

        // display the AVP value
        ret += " value=\"";
        try
        {
        	ret += getAVPValue(avp, avpDef, applicationId, type, typeBase); 
        }
        catch(Exception e)
        {
        	// case when there is an exception : we assume type=Binary
        	byte[] val = new AVP_OctetString(avp).queryValue();
        	Array array = new DefaultArray(val);
    		ret += Array.toHexString(array);
        	GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Error while trying to decode AVP : \"" + name + "\"");
        }
        ret += "\"";
                
        // display the AVP vendor Id
        if (avp.vendor_id != 0)
        {
        	ret += " vendorId=\"";
        	ret += getVendorIdString(avp.vendor_id, applicationId) + ":" + avp.vendor_id + "\"";
        }
        
        // display the AVP type and flags
        ret += " type=\"" + type + "\"";
        ret += " mandatory=\"" + avp.isMandatory() + "\"";
        ret += " private=\"" + avp.isPrivate() + "\"";
        
        // managed the "Grouped" AVP : display recursively the list of sub-AVPs
        if (typeBase.equalsIgnoreCase("grouped"))
        {
            ret += ">\n";
            AVP_Grouped gavp = new AVP_Grouped(avp);
            AVP[] tavp = gavp.queryAVPs();
            for(int i = 0; i < tavp.length; i++)
            {
                try
                {
                    ret += avpToXml(tavp[i], indent+1, applicationId);
                }
                catch(Exception e)
                {
                    ret += avpToStringSafe(tavp[i], indent+1);
                }
            }
            ret += Utils.indent(indent) + "</avp>\n";
        }
        else
        {
            ret += "/>\n";
        }
        
        return ret ;
    }

    /** get the enumeration value as a label from the dictionary */
    private String getEnumerationString(AvpDef avpDef, long code)
    {
    	String enumString;
	    try
	    {
	        enumString = avpDef.getEnumNameByCode(Long.toString(code));
	    }
	    catch(Exception e)
	    {
	        enumString = "Unknown";
	    }
	    return enumString;
    }

    /** get the vendor Id value as a label from the dictionary */
    private String getVendorIdString(int code, String applicationId)
    {
    	String vendorIdString;
	    try
	    {	    	
	    	vendorIdString = Dictionary.getInstance().getVendorDefByCode(code, applicationId).get_name();
	    }
	    catch(Exception e)
	    {
	    	vendorIdString = "Unknown";
	    }
	    return vendorIdString;
    }

    /** get the vendor Id value as a label from the dictionary */
    private String getAVPValue(AVP avp, AvpDef avpDef, String applicationId,
    						   String type, String typeBase) throws Exception
    {    
    	String label = null;
    	String value;
		if ("IPAddress".equalsIgnoreCase(type) || "IPAddress".equalsIgnoreCase(typeBase))
	    {
	    	byte[] val = new AVP_OctetString(avp).queryValue();
	    	if (val.length == 4 || val.length == 16)
	    	{
	    		value = Utils.toIPAddress(val);
	    	}
	    	else
	    	{
	    		Array array = new DefaultArray(val);
	    		value = Array.toHexString(array);
	    	}
	    }
		else if ("Address".equalsIgnoreCase(type) || "Address".equalsIgnoreCase(typeBase))
	    {
	    	byte[] val = new AVP_OctetString(avp).queryValue();
	    	if (val.length == 4 || val.length == 16)
	    	{
	    		value = Utils.toIPAddress(val);
	    	}
	    	else
	    	{
	    		Array array = new DefaultArray(val);
	    		value = Array.toHexString(array);
	    	}
	    }   
	    else if ("Time".equalsIgnoreCase(type) || "Time".equalsIgnoreCase(typeBase))
	    {
	    	// this method is buggous !
	    	//Date date = new AVP_Time(avp).queryDate();
	    	long secondSince1970 = new AVP_Time(avp).querySecondsSince1970() & 0xFFFFFFFFL;
	    	Date date = new Date(secondSince1970 * 1000);
	    	SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");
	    	value = format.format(date);
	    }        	
	    else if ("UTF8String".equalsIgnoreCase(type) || "UTF8String".equalsIgnoreCase(typeBase))
	    {
	    	byte[] val = (new AVP_OctetString(avp)).queryValue();
	    	value = new String(val);
	    }
		// base types
	    else if ("OctetString".equalsIgnoreCase(typeBase))
	    {
	    	byte[] val = new AVP_OctetString(avp).queryValue();
	    	Array array = new DefaultArray(val);
			value = Array.toHexString(array);
	    }
		else if ("Integer32".equalsIgnoreCase(typeBase))
	    {
	        int val = (new AVP_Integer32(avp)).queryValue();
	        label = getEnumerationString(avpDef, val);
	        value = Long.toString(val);
	    }
	    else if("Integer64".equalsIgnoreCase(typeBase))
	    {
	    	long val = (new AVP_Integer64(avp)).queryValue();
	    	label = getEnumerationString(avpDef, val);
	    	value = Long.toString(val);
	    }
	    else if("Unsigned32".equalsIgnoreCase(typeBase))
	    {
	    	long val = new AVP_Unsigned32(avp).queryValue() & 0xFFFFFFFFL;
	    	label = getEnumerationString(avpDef, val);            	
	    	value = Long.toString(val);
	    }
	    else if("Unsigned64".equalsIgnoreCase(typeBase))
	    {
	    	long val = new AVP_Unsigned64(avp).queryValue() & 0xFFFFFFFFFFFFFFFFL;
	    	label = getEnumerationString(avpDef, val);
	    	value = Long.toString(val);            
	    }
	    else if("Float32".equalsIgnoreCase(typeBase))
	    {
	    	float result = new AVP_Float32(avp).queryValue();
	    	value = Float.toString(result);
	    }
	    else if("Float64".equalsIgnoreCase(typeBase))
	    {
	    	double result = new AVP_Float64(avp).queryValue();
	    	value = Double.toString(result);
	    }
	    else
	    {
	    	// case when there is a decoding problem : we assume type=Binary
	    	byte[] val = new AVP_OctetString(avp).queryValue();
	    	Array array = new DefaultArray(val);
			value = Array.toHexString(array);
	    }
		// case of vendorId type : Auth-Application-Id: or Acct-Application-Id
		if (type.equalsIgnoreCase("appId"))
        {
        	label = this.getApplicationIdString(value);
        }
		// case of vendorId type : Vendor-Id: or Supported-Vendor-Id:
        if (type.equalsIgnoreCase("vendorId"))
        {
        	label = this.getVendorIdString(Integer.parseInt(value), applicationId);
        }

        // display the value the not "Grouped" AVP
        String ret = "";
        if (!typeBase.equalsIgnoreCase("grouped"))
        {
	        if (label != null)
	        {
	        	ret += label + ":";
	        }
	        ret += value;
        }

        return ret;
    }
	
	
    private String headerToXml() throws Exception 
    {         
    	
        String ret = "<header ";
        ret += "request=\"" + message.hdr.isRequest() + "\" ";
    	ret += "command=\""; 
    	ret += getCodeString() + ":";
    	ret += message.hdr.command_code + "\" ";
        ret += "applicationId=\"";
        String applicationId = Integer.toString(message.hdr.application_id);
        ret += getApplicationIdString(applicationId) + ":";
        ret += message.hdr.application_id + "\" ";
        ret += "hopByHop=\"" + message.hdr.hop_by_hop_identifier + "\" ";
        ret += "endToEnd=\"" + message.hdr.end_to_end_identifier + "\" ";
        ret += "proxiable=\"" + message.hdr.isProxiable() + "\" ";
        ret += "error=\"" + message.hdr.isError() + "\" ";
        ret += "retransmit=\"" + message.hdr.isRetransmit() + "\" ";
        ret += "/>\n";
        return ret;
	}
    
    /** 
     * Convert the message to XML document 
     */
    @Override
    public String toXml() throws Exception 
    {
        String xml = headerToXml();
        String applicationId = Integer.toString(message.hdr.application_id);
        Iterable<AVP> iterable = message.avps();
        Iterator<AVP> iterator = iterable.iterator();
        while(iterator.hasNext())
        {
            AVP avp = iterator.next();                
            xml += avpToXml(avp,0, applicationId);
        }
        return xml;
    }
    
    /** 
     * Parse the message from XML element 
     */
    @Override
    public void parseFromXml(Boolean request, Element root, Runner runner) throws Exception
    {
    	this.message = MsgDiameterParser.getInstance().parseMsgFromXml(request, root);
    	    	
        // DEPRECATED begin
        String server = root.attributeValue("server");
        if (server != null)
        {
       		GlobalLogger.instance().logDeprecatedMessage( root.getName() + " server=\"xxx\" .../", "sendMessageDiameter remoteUrl=\"xxx\" .../");
       		this.setRemoteUrl(server);
       	}
        // DEPRECATED end
    }

    //------------------------------------------------------
    // method for the "setFromMessage" <parameter> operation
    //------------------------------------------------------
    
    /** 
     * Get a parameter from the message 
     */
    @Override
    public Parameter getParameter(String path) throws Exception
    {
        Parameter var = super.getParameter(path);
        if (var != null) 
        {
            return var;
        }             
        
        var = new Parameter();
        path = path.trim();
        String[] params = Utils.splitPath(path);
        
        if(params.length>1 && params[0].equalsIgnoreCase("header"))
        {
            //----------------------------------------------------------- header:request -
            if(params[1].equalsIgnoreCase("request"))
            {
                var.add(Boolean.toString(message.hdr.isRequest()));
            }
            //----------------------------------------------------------- header:error -
            else if(params[1].equalsIgnoreCase("error"))
            {
                var.add(Boolean.toString(message.hdr.isError()));
            }
            //----------------------------------------------------------- header:proxiable -
            else if(params[1].equalsIgnoreCase("proxiable"))
            {
                var.add(Boolean.toString(message.hdr.isProxiable()));
            }
            //----------------------------------------------------------- header:retransmit -
            else if(params[1].equalsIgnoreCase("retransmit"))
            {
                var.add(Boolean.toString(message.hdr.isRetransmit()));
            }
            //----------------------------------------------------------------- header:command -
            else if(params[1].equalsIgnoreCase("command"))
            {
                var.add(Integer.toString(message.hdr.command_code));
            }
            //----------------------------------------------------------- header:applicationId -
            else if(params[1].equalsIgnoreCase("applicationId"))
            {
                var.add(Integer.toString(message.hdr.application_id));
            }
            //---------------------------------------------------------------- header:endToEnd -
            else if(params[1].equalsIgnoreCase("endToEnd"))
            {
                var.add(Integer.toString(message.hdr.end_to_end_identifier));
            }
            //---------------------------------------------------------------- header:hopByHop -
            else if(params[1].equalsIgnoreCase("hopByHop"))
            {
                var.add(Integer.toString(message.hdr.hop_by_hop_identifier));
            }
            else
            {
            	Parameter.throwBadPathKeywordException(path);
            }
        }
        // default case :
        //------------------------------------------------------------------- AVPs: xxx:yyy:value -
        else
        {
            int i = 1;
            if ((params.length > 0) && !("avp".equalsIgnoreCase(params[0])))
            {
            	i = 0;
            	Parameter.throwBadPathKeywordException(path);
            	GlobalLogger.instance().logDeprecatedMessage("setFromMessage value=xxx:yyy", "setFromMessage value=avp:xxx:yyy");
            	throw new Exception();
            }
            LinkedList<AVP> baseAvps = null ;
            LinkedList<AVP> tempAvps = null ;
            Iterator<AVP>   baseIterator = message.avps().iterator();
            LinkedList<AVP> validAvps = new LinkedList<AVP>();
            while(i<params.length-1)
            {
                if(null != baseAvps)
                {
                    baseAvps = new LinkedList<AVP>();
                    
                    Iterator<AVP> tmpIterator = tempAvps.iterator();
                    while(tmpIterator.hasNext())
                    {
                        AVP anAvp = tmpIterator.next();
                        if(getAvpStringValue(anAvp).equalsIgnoreCase("grouped"))
                        {
                            AVP[] avpTab = (new AVP_Grouped(anAvp)).queryAVPs();
                            for(int j=0; j<avpTab.length; j++)
                            {
                                baseAvps.add(avpTab[j]);
                            }
                        }
                    }
                    baseIterator = baseAvps.iterator();
                }
                
                
                validAvps.addAll(getAvps(baseIterator, params[i]));
                
                tempAvps = validAvps ;
                baseAvps = validAvps ;
                validAvps = new LinkedList<AVP>();
                
                
                i++ ;
            }
            
            if(params[params.length-1].equalsIgnoreCase("code"))
            {
                Iterator<AVP> iterator = baseAvps.iterator();
                while(iterator.hasNext())
                {             	
                	var.add(Integer.toString(iterator.next().code));
                }
            }
            else if(params[params.length-1].equalsIgnoreCase("value"))
            {
                Iterator<AVP> iterator = baseAvps.iterator();
                while(iterator.hasNext()) var.add(getAvpStringValue(iterator.next()));
            }
            else if(params[params.length-1].equalsIgnoreCase("binary"))
            {
                Iterator<AVP> iterator = baseAvps.iterator();
                while(iterator.hasNext())
                {             	
                	var.add(Array.toHexString(new DefaultArray(new AVP_OctetString(iterator.next()).queryValue())));
                }
            }
            else if(params[params.length-1].equalsIgnoreCase("vendorId"))
            {
                Iterator<AVP> iterator = baseAvps.iterator();
                while(iterator.hasNext()) var.add(Integer.toString(iterator.next().vendor_id));
            }
            else if(params[params.length-1].equalsIgnoreCase("vendorFlag"))
            {
                Iterator<AVP> iterator = baseAvps.iterator();
                while(iterator.hasNext())
                {             	
                	var.add(new AVP_OctetString(iterator.next()).isVendorSpecific());
                }
            }
            else if(params[params.length-1].equalsIgnoreCase("mandatory"))
            {
                Iterator<AVP> iterator = baseAvps.iterator();
                while(iterator.hasNext())
                {             	
                	var.add(new AVP_OctetString(iterator.next()).isMandatory());
                }
            }
            else if(params[params.length-1].equalsIgnoreCase("private"))
            {
                Iterator<AVP> iterator = baseAvps.iterator();
                while(iterator.hasNext())
                {             	
                	var.add(new AVP_OctetString(iterator.next()).isPrivate());
                }
            }
            else
            {
            	Parameter.throwBadPathKeywordException(path);
            }
            
        }
        
        return var;
    }
    
    private LinkedList<AVP> getAvps(Iterator<AVP> avps, String code) throws Exception
    {
        LinkedList<AVP> result = new LinkedList<AVP>();
        
        if(Utils.isInteger(code))
        {
            int avpCode = Integer.parseInt(code);
            while(avps.hasNext())
            {
                AVP avp = avps.next();
                if(avp.code == avpCode) result.add(avp);
            }
        }
        else
        {
        	String appliIDCode = Integer.toString(message.hdr.application_id);
            while(avps.hasNext())
            {
                AVP avp = avps.next();
                String vendorIDCode = Integer.toString(avp.vendor_id);
                AvpDef  avpDef = Dictionary.getInstance().getAvpDefByCodeVendorIdORCode(avp.code, appliIDCode, vendorIDCode);
                if (avpDef != null)
                {
		            String avpName = avpDef.get_name(); 
		            if(code.equals(avpName)) result.add(avp);
                }
            }
        }
        return result ;
    }
        
    /** returns the type of an AVP */
    private String getAvpStringValue(AVP avp) throws Exception
    {
        String applicationId = Integer.toString(message.hdr.application_id) ;
        
        // retrieve the vendorId code
        String vendorIdCode = Integer.toString(avp.vendor_id);

        // retrieve the type of avp
        AvpDef avpDef = Dictionary.getInstance().getAvpDefByCodeVendorIdORCode(avp.code, applicationId, vendorIdCode);
        
        // retrieve the top-parent type
        TypeDef typeDef = null;
        if(null != avpDef)
        {
            typeDef = avpDef.get_type();
            while(null != typeDef && null != typeDef.get_type_parent()) typeDef = typeDef.get_type_parent();
        }
        
        // determine the type name
        String type = "unknown" ;
        if(null != typeDef) type = typeDef.get_type_name();
        if(null != avpDef && avpDef.getGroupedAvpNameList().size()!=0) type = "grouped";
        
        // return the value
        if(type.equalsIgnoreCase("grouped"))
        {
        	return "grouped" ;
        }
        else if(type.equalsIgnoreCase("IPAddress") || type.equalsIgnoreCase("Address"))
        {
        	byte[] result = new AVP_OctetString(avp).queryValue();
        	String strRes = Utils.toIPAddress(result);
        	return strRes;
        }
        else if(type.equalsIgnoreCase("Time"))
        {
        	// this method is buggous !
        	//Date date = new AVP_Time(avp).queryDate();
        	long secondSince1970 = new AVP_Time(avp).querySecondsSince1970() & 0xFFFFFFFFL;
        	Date date = new Date(secondSince1970 * 1000);
        	SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");
        	return format.format(date);
        }
        else if(type.equalsIgnoreCase("UTF8String"))
        {
        	byte[] result = new AVP_OctetString(avp).queryValue();
        	return new String(result);
        }
        else if(type.equalsIgnoreCase("OctetString"))
        {
        	byte[] result = new AVP_OctetString(avp).queryValue();
        	return Utils.toBinaryString(result, false);
        }
        // base types
        else if(type.equalsIgnoreCase("Integer32"))
        {
        	long result = new AVP_Integer32(avp).queryValue();
        	return Long.toString(result);
        }
        else if(type.equalsIgnoreCase("Integer64"))
        {
        	long result = new AVP_Integer64(avp).queryValue();
        	return Long.toString(result);
        }
        else if(type.equalsIgnoreCase("Unsigned32"))
        {
        	long result = new AVP_Unsigned32(avp).queryValue() & 0xFFFFFFFFL;
        	return Long.toString(result);
        }
        else if(type.equalsIgnoreCase("Unsigned64"))
        {
        	long result = new AVP_Unsigned64(avp).queryValue()& 0xFFFFFFFFFFFFFFFFL;
        	return Long.toString(result);
        }
        else if(type.equalsIgnoreCase("Float32"))
        {
        	float result = new AVP_Float32(avp).queryValue();
        	return Float.toString(result);
        }
        else if(type.equalsIgnoreCase("Float64"))
        {
        	double result = new AVP_Float64(avp).queryValue();
        	return Double.toString(result);
        }
        else
        {
        	// case when there is a decoding problem : we assume type=OctetString
        	byte[] result = new AVP_OctetString(avp).queryValue();
        	return Utils.toBinaryString(result, false);
        }
    }


}
