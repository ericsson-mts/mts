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

import com.devoteam.srit.xmlloader.core.protocol.*;
import com.devoteam.srit.xmlloader.diameter.dictionary.*;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.coding.binary.ElementAbstract;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.operations.basic.operators.PluggableParameterOperatorBinary;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import dk.i1.diameter.*;

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
     * Get the protocol acronym of the message 
     */
    @Override
    public String getProtocol()
    {
    	String applicationId = "";
        try
        {
        	if (this.message.hdr.application_id!= 0)
        	{
        		Application application = Dictionary.getInstance().getApplicationById(this.message.hdr.application_id);
        		if( application!=null )
        		{
        			applicationId = StackFactory.SEP_SUB_INFORMATION + application.get_name() + ":" + this.message.hdr.application_id;
        			applicationId = applicationId.replace(" ", "");
        		}
        	}
        }
        catch(Exception e)
        {
        	// nothing to do
        }
    	return StackFactory.PROTOCOL_DIAMETER + applicationId;    	
    }
        
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
    public String getType() throws Exception
    {    
    	String type = super.getType();
    	if (type != null)
    	{
    		return type;
    	}

    	// for request message
        type = getCodeString() + ":" + message.hdr.command_code;        

    	// for message with EAP data
        Parameter eapPayload = getParameter("avp.462.binary");    	
    	if (eapPayload != null && eapPayload.length() > 0) 
        {
    		String data = (String) eapPayload.get(0);
    		type += StackFactory.SEP_SUB_INFORMATION + getEAPType(data);    		
            return type;
        }
        return type;
    }
    
    /** 
     * Get the special type of the EAP message 
     */
    private String getEAPType(String data) throws Exception
    {
    	String eapType = null;
		// for message with EAP data
    	com.devoteam.srit.xmlloader.core.coding.binary.Dictionary dico = 
    	com.devoteam.srit.xmlloader.core.coding.binary.Dictionary.getInstance("binary/dictionary_EAP.xml");
    	
		ElementAbstract newElement = null;
		try 
		{
			newElement = PluggableParameterOperatorBinary.elementDecodeToXml(data, dico, null);
		}
		catch (Exception e)
		{
			//nothing to do
		}
		if (newElement == null)
		{
			return eapType;
		}
		ElementAbstract subElement = newElement.getElement(0);
		String typeValue = subElement.getFieldValue("Type");
		if (typeValue == null)
		{
			String codeValue = subElement.getFieldValue("Code");
			if (codeValue != null)
			{
				eapType = codeValue;
			}
			return eapType;
		}
		if (typeValue.endsWith(":23"))
		{
			subElement = newElement.getElement(1);
			String akaSubtypeValue = subElement.getFieldValue("EAP AKA Subtype");
			if (akaSubtypeValue != null)
			{
				eapType = akaSubtypeValue;
			}
			return eapType;
		}
		if (typeValue != null)
		{
			eapType = typeValue;
		}
        return eapType;
    }
    
    /** get the command as a label from the dictionary */
    private String getCodeString()
    {
        /** commandCode */
    	String codeString = "Unknown"; 
        try
        {
        	String applicationIdString = Integer.toString(message.hdr.application_id);
        	CommandDef commandDef = Dictionary.getInstance().getCommandDefByCode(message.hdr.command_code, applicationIdString);
        	if( commandDef!=null ){        	
        		codeString = commandDef.get_name();
        	}
        }
        catch(Exception e)
        {
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
    	String result = null;
    	    	
    	// get Result-Code value
        Parameter resultCode = getParameter("avp.268.value");
        if (resultCode != null && resultCode.length() > 0) 
        {
            result = (String) resultCode.get(0);
        }
                
        if (result == null)
        {
	        // get Experimental-Result:Experimental-Result-Code
	    	Parameter experimentalResult = getParameter("avp.297.298.value");
	        if (experimentalResult != null && experimentalResult.length() > 0)
	        {
	            result = (String) experimentalResult.get(0);
	        }
        }
        
    	// for message with EAP data
        Parameter eapPayload = getParameter("avp.462.binary");    	
    	if (eapPayload != null && eapPayload.length() > 0) 
        {
    		String data = (String) eapPayload.get(0);
    		result += StackFactory.SEP_SUB_INFORMATION + getEAPType(data);    		
            return result;
        }
        return result;
    }

    /** Get the complete result of this answer (null if request) */
    @Override
    public String getResultComplete() throws Exception
    {
    	return getResult();
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
    		int pos = status.lastIndexOf(":");
    		if (pos > 0)
    		{
    			status = status.substring(pos + 1);
    		}
    		if (Utils.isInteger(status))
    		{
    			int statusCode = new Integer(status).intValue();
    			if (statusCode < 2000 && statusCode >= 3000)
    			{
    				return false;
    			}
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
                if (strvalue != null)
                {	   
                	
                	int pos = strvalue.indexOf(":");
                	if (pos >=0)
                	{
                		strvalue = strvalue.substring(pos + 1);
                	}
                	int intVal = new Integer(strvalue).intValue();
                	// value STOP_RECORD:4
                	if (intVal == 4)
                	{
                		return true;
                	}
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
                if (strvalue != null)
                {
                	int pos = strvalue.indexOf(":");
                	if (pos >=0)
                	{
                		strvalue = strvalue.substring(pos + 1);
                	}
	                int intVal = new Integer(strvalue).intValue();
	                // value TERMINATION_REQUEST:3
	                if (intVal == 3)
	                {
	                    return true;
	                }
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
        long appliIdCode = message.hdr.application_id & 0xFFFFFFFFL;
    	ret += "applicationId=\"" + getApplicationIdString(applicationId) + ":" + appliIdCode + "\" ";
    	ret += "hopByHop=\"" + message.hdr.hop_by_hop_identifier + "\" ";
    	ret += "endToEnd=\"" + message.hdr.end_to_end_identifier + "\"";	    	
    	ret += "/>";
        return ret;
    }
    
    /** get the applicationId as a label from the dictionary */
    private String getApplicationIdString(String applicationId)
    {
    	String applicationIdString = "Unknown";
	    try
	    {
	    	Application application = Dictionary.getInstance().getApplication(applicationId);
	    	if( application!=null ){
		        applicationIdString = application.get_name();
	    	}
	    }
	    catch(Exception e)
	    {
	    }
	    return applicationIdString;
    }
            
    /**
     * prints as String an avp and it's sub-avps (recursive)
     */
    private String avpToXml(AVP avp, int indent, String applicationId) throws Exception
    {       
        // retrieve the vendorId code
        String vendorIdCode = Integer.toString(avp.vendor_id);

        // retrieve the AvpDef object from the dictionary
        AvpDef avpDef = Dictionary.getInstance().getAvpDefByCodeVendorIdORCode(avp.code, applicationId, vendorIdCode);
                
        // retrieve the dictionary type
        TypeDef typeDef = getAVPType(avpDef);
        String typeDico =  null;
        if (typeDef != null)
        {
        	typeDico = typeDef.get_type_name();
        }
        if (avpDef !=  null && avpDef.getGroupedAvpNameList().size() > 0)
        {
        	typeDico = "Grouped";
        }
        // retrieve the base type (using top-parent recursively)
        String typeBase = getAVPTypeBase(typeDico, applicationId);
        
        String ret = Utils.indent(indent) + "<avp";
        
        // display the AVP label and code
        long code = avp.code & 0xFFFFFFFFL;
        String label = "Unknown";
        if (avpDef != null)
        {
        	label = avpDef.get_name();
        }
        String name = label + ":" + Long.toString(code);
        ret += " code=\"" + name + "\"";
        
        AVP[] tavp = null;				// list of the sub-AVPs  
        // display the AVP value;
        try
        {
            // process the "Grouped" AVP : display recursively the list of sub-AVPs
	        if (typeDico != null && typeDico.equalsIgnoreCase("grouped"))
	        {
	            AVP_Grouped gavp = new AVP_Grouped(avp);
	            tavp = gavp.queryAVPs();
	        }
        	
        	String value = getAVPValue(avp, avpDef, applicationId, typeDico, typeBase);
        	// if there are non printable characters
        	if (!Utils.hasPrintableChar(value))
        	{
        		Array arrayVal = new DefaultArray(value.getBytes());
        		value = Array.toHexString(arrayVal);
        		typeDico = "Binary";
        	}
        	if (value != null)
        	{
        		// replace escape XML character
        		value = Utils.escapeXMLEntities(value);
        		ret += " value=\"" + value + "\"";
        	}
        }
        catch(Exception e)
        {
        	// case when there is an exception : we assume type=Binary
        	byte[] val = new AVP_OctetString(avp).queryValue();
        	Array array = new DefaultArray(val);
        	String value = Array.toHexString(array);
        	if (value != null)
        	{
        		ret += " value=\"" + value + "\"";
        	}
        	typeDico = "Binary";
        	GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Decoding : error while trying to decode AVP : \"" + code + "\" because the AVP type in the dictionary is not correct.");
        }
                
        // display the AVP vendor Id
        if (avp.vendor_id != 0)
        {
        	ret += " vendorId=\"";
        	ret += getVendorIdString(avp.vendor_id, applicationId) + ":" + avp.vendor_id + "\"";
        }
        
        // display the AVP type and flags
        if (typeDico != null)
        {
        	ret += " type=\"" + typeDico + "\"";
        }
        else
        {
        	ret += " type=\"" + typeBase + "\"";
        }
        ret += " m=\"" + avp.isMandatory() + "\"";
        ret += " p=\"" + avp.isPrivate() + "\"";

        // process the "Grouped" AVP : display recursively the list of sub-AVPs
        if (tavp != null)
        {
            ret += ">\n";
            for(int i = 0; i < tavp.length; i++)
            {
                ret += avpToXml(tavp[i], indent + 1, applicationId);
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
    //@Nullable
    private String getEnumerationString(AvpDef avpDef, long code)
    {
    	String enumString = null;
	    try
	    {
	    	if (avpDef != null)
	    	{
	    		enumString = avpDef.getEnumNameByCode(Long.toString(code));
	    	}
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
    	String vendorIdString = "Unknown";
	    try
	    {	    	
	    	VendorDef vendorDef = Dictionary.getInstance().getVendorDefByCode(code, applicationId);
	    	if( vendorDef!=null ){
	    		vendorIdString = vendorDef.get_name();
	    	}
	    }
	    catch(Exception e)
	    {
	    }
	    return vendorIdString;
    }

    /** get the type for an AVP according to the dictionary */
    private TypeDef getAVPType(AvpDef avpDef) throws Exception
    {    
        TypeDef typeDef = null;
        if (avpDef != null)
        {
        	typeDef = avpDef.get_type();
        }
        return typeDef;
    }
    
    /** get the type base for an AVP according to the dictionary */
    private String getAVPTypeBase(String typeDico, String applicationId) throws Exception
    {    
        TypeDef typeDef = MsgDiameterParser.getInstance().parse_AVPType(typeDico, applicationId);
        String typeBase = "Binary";
        while (typeDef != null && typeDef.get_type_parent() != null)
        {
            typeDef = typeDef.get_type_parent();
        }
        if (typeDef != null)
        {
        	typeBase = typeDef.get_type_name();
        }
        
        return typeBase;
    }
    
    /** get the value for an AVP according to the dictionary */    
    private String getAVPValue(AVP avp, AvpDef avpDef, String applicationId,
    						   String typeDico, String typeBase) throws Exception
    {    
    	String label = null;
    	String value = null;
        if ("Grouped".equalsIgnoreCase(typeDico) || "Grouped".equalsIgnoreCase(typeBase))
        {
        	return null;
        }
        else if ("IPAddress".equalsIgnoreCase(typeDico) || "IPAddress".equalsIgnoreCase(typeBase))
	    {
	    	byte[] val = new AVP_OctetString(avp).queryValue();
	    	value = Utils.toIPAddress(val);
	    }
		else if ("Address".equalsIgnoreCase(typeDico) || "Address".equalsIgnoreCase(typeBase))
	    {
	    	byte[] val = new AVP_OctetString(avp).queryValue();
	    	value = Utils.toIPAddress(val);
	    }   
	    else if ("Time".equalsIgnoreCase(typeDico) || "Time".equalsIgnoreCase(typeBase))
	    {
	    	// this method is buggous !
	    	//Date date = new AVP_Time(avp).queryDate();
	    	long secondSince1970 = new AVP_Time(avp).querySecondsSince1970() & 0xFFFFFFFFL;
	    	Date date = new Date(secondSince1970 * 1000);
	    	SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");
	    	value = format.format(date);
	    }        	
	    else if ("UTF8String".equalsIgnoreCase(typeDico) || "UTF8String".equalsIgnoreCase(typeBase))
	    {
	    	byte[] val = (new AVP_OctetString(avp)).queryValue();
	    	value = new String(val, "UTF-8");
	    }
		// base types
	    else if ("OctetString".equalsIgnoreCase(typeBase))
	    {
	    	byte[] val = (new AVP_OctetString(avp)).queryValue();
	    	value = new String(val);
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
	    else if("Binary".equalsIgnoreCase(typeBase))
	    {
	    	byte[] val = new AVP_OctetString(avp).queryValue();
	    	Array array = new DefaultArray(val);
			value = Array.toHexString(array);
	    }
	    else
	    {
	    	// case when there is a decoding problem : we assume type=Binary
	    	byte[] val = new AVP_OctetString(avp).queryValue();
	    	Array array = new DefaultArray(val);
			value = Array.toHexString(array);
	    }
        if (typeDico != null)
        {
			// case of vendorId type : Auth-Application-Id: or Acct-Application-Id
			if (typeDico.equalsIgnoreCase("appId"))
	        {
	        	label = this.getApplicationIdString(value);
	        }
			// case of vendorId type : Vendor-Id: or Supported-Vendor-Id:
	        if (typeDico.equalsIgnoreCase("vendorId"))
	        {
	        	label = this.getVendorIdString((int) Long.parseLong(value), applicationId);
	        }
        }
        
        // display the value the not "Grouped" AVP
        String ret = "";
	    if (label != null)
	    {
	    	ret += label + ":";
	    }
	    ret += value;
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
        long appliIdCode = message.hdr.application_id & 0xFFFFFFFFL;
        ret += appliIdCode + "\" ";
        ret += "hopByHop=\"" + message.hdr.hop_by_hop_identifier + "\" ";
        ret += "endToEnd=\"" + message.hdr.end_to_end_identifier + "\" ";
        ret += "p=\"" + message.hdr.isProxiable() + "\" ";
        ret += "e=\"" + message.hdr.isError() + "\" ";
        ret += "r=\"" + message.hdr.isRetransmit() + "\" ";
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
    public void parseFromXml(ParseFromXmlContext context, Element root, Runner runner) throws Exception
    {
    	super.parseFromXml(context,root,runner);

    	this.message = MsgDiameterParser.getInstance().parseMsgFromXml(context.getRequest(), root);
    	    	
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
            	String command = Integer.toString(message.hdr.command_code);
            	command = getCodeString() + ":" + command;
                var.add(command);
            }
            //----------------------------------------------------------- header:applicationId -
            else if(params[1].equalsIgnoreCase("applicationId"))
            {
            	long appliIdCode = message.hdr.application_id & 0xFFFFFFFFL;
            	String appliID = Long.toString(appliIdCode);
            	appliID = getApplicationIdString(appliID) + ":" + appliID;
                var.add(appliID);
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
            	GlobalLogger.instance().logDeprecatedMessage("setFromMessage value=xxx.yyy", "setFromMessage value=avp.xxx.yyy");
            	throw new Exception();
            }          
            
            LinkedList<AVP> baseAvps = null ;
            LinkedList<AVP> tempAvps = null ;
            Iterator<AVP>   baseIterator = message.avps().iterator();
            LinkedList<AVP> validAvps = new LinkedList<AVP>();
            while (i < params.length-1)
            {
                if (baseAvps != null)
                {
                    baseAvps = new LinkedList<AVP>();
                    
                    Iterator<AVP> tmpIterator = tempAvps.iterator();
                    while (tmpIterator.hasNext())
                    {
                        AVP anAvp = tmpIterator.next();
                        if (getAvpStringValue(anAvp, null) == null)
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
                
                while (baseIterator.hasNext())
                {
                	AVP avp = baseIterator.next();
                    String applicationId = Integer.toString(message.hdr.application_id);
                	if (matchAVP_Keyword(avp, params[i], applicationId))
                	{
                		validAvps.add(avp);
                	}
                	
                }
                tempAvps = validAvps ;
                baseAvps = validAvps ;
                validAvps = new LinkedList<AVP>();
                
                
                i++ ;
            }
            
            Iterator<AVP> iterator = baseAvps.iterator();
            while (iterator.hasNext())
            {             	
            	AVP avp = iterator.next();
                String applicationId = Integer.toString(message.hdr.application_id);
            	String value = getParameterForKeyword(avp, params[params.length-1], path, applicationId);
            	var.add(value);
            }
        }
        return var;
    }

    // get the value the parameter from keyword (setFromMessage)
    private String getParameterForKeyword(AVP avp, String keyword, String path, String applicationId) throws Exception
    {
    	String value = null;
	    if (keyword.equalsIgnoreCase("code"))
	    {
	    		long val = avp.code & 0xFFFFFFFFL;
	        	value = Long.toString(val);
	    }
	    else if (keyword.equalsIgnoreCase("value"))
	    {
	    	value = getAvpStringValue(avp, null);               
	    }
	    else if (keyword.equalsIgnoreCase("binary"))
	    {
        	byte[] binary = new AVP_OctetString(avp).queryValue();
        	Array array = new DefaultArray(binary);
        	value = Array.toHexString(array);
	    }
	    else if (keyword.equalsIgnoreCase("type"))
	    {
	        String vendorIdCode = Long.toString(avp.vendor_id & 0xFFFFFFFFL);
	        // retrieve the type of avp
	        AvpDef avpDef = Dictionary.getInstance().getAvpDefByCodeVendorIdORCode(avp.code, applicationId, vendorIdCode);
	     
	        // retrieve the dictionary type
	        TypeDef typeDef = getAVPType(avpDef);
	        if (typeDef != null)
	        {
	        	value = typeDef.get_type_name();
	        }
	        if (avpDef != null && avpDef.getGroupedAvpNameList().size() > 0)
	        {
	        	value = "Grouped";
	        }
	    }
	    else if (keyword.equalsIgnoreCase("vendorId"))
	    {
	    	if (avp.vendor_id != 0)
	    	{
	    		value = getVendorIdString(avp.vendor_id, applicationId) + ":";
		    	value += Integer.toString(avp.vendor_id);
	    	}
	    }
	    else if (keyword.equalsIgnoreCase("vendor"))
	    {
	    	value = Boolean.toString(avp.isVendorSpecific());
	    }
	    else if (keyword.equalsIgnoreCase("mandatory"))
	    {
	    	value = Boolean.toString(avp.isMandatory());
	    }
	    else if (keyword.equalsIgnoreCase("private"))
	    {
	    	value = Boolean.toString(avp.isPrivate());
	    }
	    else
	    {
	    	// case keyword is a type (integer32, unsigned64, octetstring, utf8string, ....)
	    	value = getAvpStringValue(avp, keyword);
	    }
	    return value;
    }
    
    // test whether a AVP matches a given keyword
    private boolean matchAVP_Keyword(AVP avp, String keyword, String applicationId) throws Exception
    {
    	String vendorId = Integer.toString(avp.vendor_id); 
	    int pos = keyword.lastIndexOf(":");
	    if (pos >= 0)
	    {
	    	String codeLabel = keyword.substring(0, pos);
	    	String codeInt = keyword.substring(pos + 1);
	    	int code = Integer.parseInt(codeInt);
	        AvpDef avpDef = Dictionary.getInstance().getAvpDefByCodeVendorIdORCode(code, applicationId, vendorId);
	        if (code == avp.code)
	        {
		        if (avpDef != null && !codeLabel.equalsIgnoreCase(avpDef.get_name()))
		        {
		        	GlobalLogger.instance().getApplicationLogger().warn(Topic.PROTOCOL, 
		        			"SetFromMessage : For the AVP code, the label \"" + codeLabel + "\" does not match the code \"" + codeInt + "\" in the dictionary; " +
		        			"we assume the code is \"" + code + " and we are waiting the label \"" + avpDef.get_name() + "\".");

		        }
	        	return true;
	        }
	    }
	    else
	    {
	        if(!Utils.isInteger(keyword))
	        {
	            AvpDef  avpDef = Dictionary.getInstance().getAvpDefByCodeVendorIdORCode(avp.code, applicationId, vendorId);	            
	            if (avpDef != null)
	            {
	            	String avpName = avpDef.get_name(); 
			        if (keyword.equals(avpName))
			        {
			        	return true;
	                }
	            }
	        }
	        else
	        {
	        	long code = Long.parseLong(keyword) & 0xffffffffL;
	        	long avpCode = ((long) avp.code) & 0xffffffffL;
		        if (code == avpCode)
		        {
		        	return true;
		        }	        	
	        }
	    }
	    return false;
    }
        
    /** returns the type of an AVP */
    private String getAvpStringValue(AVP avp, String typeDico) throws Exception
    {
        String applicationId = Integer.toString(message.hdr.application_id) ;
        
        // retrieve the vendorId code
        String vendorIdCode = Integer.toString(avp.vendor_id);

        // retrieve the type of avp
        AvpDef avpDef = Dictionary.getInstance().getAvpDefByCodeVendorIdORCode(avp.code, applicationId, vendorIdCode);
        
        // retrieve the dictionary type
        TypeDef typeDef = getAVPType(avpDef);
        if (typeDico == null)
        {
	        if (typeDef != null)
	        {
	        	typeDico = typeDef.get_type_name();
	        }
	        if (avpDef != null && avpDef.getGroupedAvpNameList().size() > 0)
	        {
	        	typeDico = "Grouped";
	        }
        }
        
        // retrieve the base type (using top-parent recursively)
        String typeBase = getAVPTypeBase(typeDico, applicationId);

        // retrieve the value
        String value;
        try
        {
        	value = getAVPValue(avp, avpDef, applicationId, typeDico, typeBase);
	    }
	    catch(Exception e)
	    {
	    	// case when there is an exception : we assume type=Binary
	    	byte[] val = new AVP_OctetString(avp).queryValue();
	    	Array array = new DefaultArray(val);
	    	value = Array.toHexString(array);
	    	GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Decoding : error for the AVP : \"" + avp.code + "\" because the AVP type in the dictionary is not compliant with the data : we display or return data as binary.");
	    }      
        return value;
    }


}
