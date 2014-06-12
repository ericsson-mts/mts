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
import com.devoteam.srit.xmlloader.diameter.dictionary.Dictionary;
import com.devoteam.srit.xmlloader.diameter.dictionary.TypeDef;
import com.devoteam.srit.xmlloader.diameter.dictionary.VendorDef;
import com.devoteam.srit.xmlloader.tcp.ListenpointTcp;
import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.UnsignedInt32;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import dk.i1.diameter.AVP;
import dk.i1.diameter.AVPInterface;
import dk.i1.diameter.AVP_Grouped;
import dk.i1.diameter.AVP_Integer32;
import dk.i1.diameter.AVP_Integer64;
import dk.i1.diameter.AVP_OctetString;
import dk.i1.diameter.AVP_Unsigned32;
import dk.i1.diameter.AVP_Unsigned64;
import dk.i1.diameter.Message;
import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.LinkedList;
/**
 *
 * @author gpasquiers
 */
public class MsgDiameter extends Msg
{
    
    /** diameter message object */
    private Message message;
    
    /** transport protocol */
    private String transport = null;

    /** applicationId */
    String applicationIdString = null;

    /** Creates a new instance of MsgDiameter */
    public MsgDiameter()
    {
    }
    
    /** Creates a new instance of MsgDiameter */
    public MsgDiameter(Message aMessage)
    {
        super();
        message = aMessage;
        setListenpoint(StackDiameter.listenpoint); 
    }
    
    /** Get a parameter from the message */
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
            LinkedList validAvps = new LinkedList<AVP>();
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
            else
            {
            	Parameter.throwBadPathKeywordException(path);
            }
            
        }
        
        return var;
    }
    
    private LinkedList<AVP> getAvps(Iterator<AVP> avps, String code) throws ParsingException
    {
        LinkedList<AVP> result = new LinkedList<AVP>();
        int avpCode = 0 ;
        if(Utils.isInteger(code)) avpCode = Integer.parseInt(code);
        else                      avpCode = Dictionary.getInstance().getAvpDefByName(code, Integer.toString(message.hdr.application_id)).get_code();
        while(avps.hasNext())
        {
            AVP avp = avps.next();
            if(avp.code == avpCode) result.add(avp);
        }
        return result ;
    }
    
    /** Returns the diameter message of MsgDiameter */
    public Message getMessage()
    {
        return message ;
    }
    
    /** returns the type of an AVP */
    private String getAvpStringValue(AVP avp) throws Exception
    {
        
        String applicationId = Integer.toString(message.hdr.application_id) ;
        // retrieve the type of avp
        AvpDef avpDef = Dictionary.getInstance().getAvpDefByCode(avp.code, applicationId);
        
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
        
        // retrieve the AVP name
        String name = "unknown ";
        if(null != avpDef) name = avpDef.get_name();
        
        if(type.equalsIgnoreCase("grouped"))
        {
        	return "grouped" ;
        }
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
        	long result = new AVP_Unsigned32(avp).queryValue();
        	return Long.toString(result);
        }
        else if(type.equalsIgnoreCase("Unsigned64"))
        {
        	long result = new AVP_Unsigned64(avp).queryValue();
        	return Long.toString(result);
        }
        else if(type.equalsIgnoreCase("OctetString"))
        {
        	byte[] result = new AVP_OctetString(avp).queryValue();
        	return Utils.toBinaryString(result, false);
        }
        else if(type.equalsIgnoreCase("IPAddress"))
        {
        	byte[] result = new AVP_OctetString(avp).queryValue();
        	String strRes = InetAddress.getByAddress(result).getHostAddress();
        	return strRes;
        }
        else if(type.equalsIgnoreCase("UTF8String"))
        {
        	byte[] result = new AVP_OctetString(avp).queryValue();
        	return new String(result);
        }
        else
        {
        	return null ;
        }
    }
            
    /** Get the protocol of this message */
    @Override
    public String getProtocol()
    {
        return StackFactory.PROTOCOL_DIAMETER;
    }
    
    /** Return true if the message is a request else return false*/
    @Override
    public boolean isRequest()
    {
        return message.hdr.isRequest();
    }
    
    /** Get the type of this message*/
    @Override
    public String getType()
    {
        return getCodeString() + ":" + message.hdr.command_code + "";
    }
    
    /** Get the complete type of this message*/
    @Override
    public String getTypeComplete() throws Exception
    {
	    return getCodeString() + ":" + message.hdr.command_code + "";
    }

    /** Get the result of this answer (null if request) */
    @Override
    public String getResult() throws Exception
    {
        // get Result-Code value
        Parameter var = getParameter("avp.268.value");
        if (var.length() > 0) {
            return (String) var.get(0);
        }
        
        // get Experimental-Result:Experimental-Result-Code
        var = getParameter("avp.297.298.value");
        //if (var != null) {
        if(var != null && var.length()>0){
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
            AvpDef avpDef = Dictionary.getInstance().getAvpDefByCode(268, applicationId);
        	String valueName = avpDef.getEnumNameByCode(valueInt);
            return valueName + ":" + valueInt;
        }
        
        // get Experimental-Result:Experimental-Result-Code
        var = getParameter("avp.297.298.value");
        if ((var != null) && (var.length() > 0)) {
        	String valueInt = (String) var.get(0);
            AvpDef avpDef = Dictionary.getInstance().getAvpDefByCode(298, applicationId);
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
    
    /** Return the length of the message*/
    @Override
    public int getLength() {
    	return message.encodeSize();

    }

    /** Get the data (as binary) of this message */
    @Override
    public byte[] getBytesData()
    {
    	return message.encode();
    }

    private String headerToString() throws Exception 
    {          
        String ret = "<header command=\"" + message.hdr.command_code + "\"" +
        " applicationId=\"" + message.hdr.application_id + "\"" +
        " hopByHop=\"" + message.hdr.hop_by_hop_identifier + "\"" +
        " endToEnd=\"" + message.hdr.end_to_end_identifier + "\"" +
        " r=\"" + message.hdr.isRequest() + "\"" +
        " p=\"" + message.hdr.isProxiable() + "\"" +
        " e=\"" + message.hdr.isError() + "\"" +
        " t=\"" + message.hdr.isRetransmit() + "\"" + "/>\n";
        return ret;
    }

    /** Returns a short description of the message. Used for logging as INFO level */
    /** This methods HAS TO be quick to execute for performance reason */
    @Override
    public String toShortString() throws Exception {          
    	String ret = super.toShortString();
  		ret += "<header "; 	
    	ret += "applicationId=\"" + getApplicationIdString() + "(" + message.hdr.application_id + ")\" ";
    	ret += "hopByHop=\"" + message.hdr.hop_by_hop_identifier + "\" ";
    	ret += "endToEnd=\"" + message.hdr.end_to_end_identifier + "\">";	    	
        return ret;
    }

    /** Returns the string description of the message. Used for logging */
    @Override
    public String toString()
    {
    	String ret = " ";
        try
        {
        	ret += headerToString();
        }
        catch (Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "An error occured while logging the DIAMETER message : ", ret);
            e.printStackTrace();
        }
        
        String applicationId = Integer.toString(message.hdr.application_id);
        Iterable<AVP> iterable = message.avps();
        Iterator<AVP> iterator = iterable.iterator();
        while(iterator.hasNext())
        {
            AVP avp = iterator.next();
            try
            {
                ret += avpToString(avp,0, applicationId);
            }
            catch (Exception e)
            {
                GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "An error occured while logging the DIAMETER message : ", ret);
                e.printStackTrace();
            }
        }

		// cut if message is too long
        if (ret.length() > MAX_STRING_LENGTH)
        {
        	ret = " {" + MAX_STRING_LENGTH + " of " + ret.length() + "}" + ret.substring(0, MAX_STRING_LENGTH);
        }

        ret += "\n\n";
        // display transport info
		if (channel != null)
		{
			ret += "<CHANNEL " + channel + ">\n";
		}
		if (listenpoint != null)
		{
			ret += "<LISTENPOINT " + listenpoint + ">\n";
		}
		if (probe != null)
		{
			ret += "<PROBE " + probe + ">\n";
		}

        return ret;
    }

    /**
     * prints as String an avp and it's sub-avps (recursive)
     */
    private static String avpToString(AVP avp, int indent, String applicationId) throws Exception
    {
        String ret = "" ;
        // retrieve the type of avp
        AvpDef avpDef = Dictionary.getInstance().getAvpDefByCode(avp.code, applicationId);
        
        // retrieve the top-parent type
        TypeDef typeDef = null;
        
        boolean isTypeAppId = false;
        boolean isTypeVendorId = false;
        
        if(null != avpDef)
        {
            typeDef = avpDef.get_type();
            while(null != typeDef && null != typeDef.get_type_parent())
            {
                if(typeDef.get_type_name().equalsIgnoreCase("AppId"))     isTypeAppId = true;
                if(typeDef.get_type_name().equalsIgnoreCase("VendorId"))  isTypeVendorId = true;
                typeDef = typeDef.get_type_parent();
            }
            
        }
        
        // determine the type name
        String type = "unknown" ;
        if(null != typeDef) type = typeDef.get_type_name();
        if(null != avpDef && avpDef.getGroupedAvpNameList().size()!=0) type = "grouped";
        
        // retrieve the AVP name
        String name = "unknown ";
        if(null != avpDef) name = avpDef.get_name();
        
        ret += Utils.indent(indent) + "<avp";
        ret += " name=\"" + name + "\"";
        ret += " code=\"" + avp.code + "(0x" + Integer.toHexString(avp.code) +")\"";

        String value = "";
        try
        {
            if(type.equalsIgnoreCase("grouped"))          value = "grouped" ;
            else if(type.equalsIgnoreCase("Integer32"))   value = Long.toString((new AVP_Integer32(avp)).queryValue());
            else if(type.equalsIgnoreCase("Integer64"))   value = Long.toString((new AVP_Integer64(avp)).queryValue());
            else if(type.equalsIgnoreCase("Unsigned32"))  value = Long.toString((new AVP_Unsigned32(avp)).queryValue());
            else if(type.equalsIgnoreCase("Unsigned64"))  value = Long.toString((new AVP_Unsigned64(avp)).queryValue());
            else if(type.equalsIgnoreCase("OctetString")) value = Utils.toBinaryString(new AVP_OctetString(avp).queryValue(), false);
            else if(type.equalsIgnoreCase("IPAddress"))   value = InetAddress.getByAddress((new AVP_OctetString(avp)).queryValue()).getHostAddress();
            else if(type.equalsIgnoreCase("UTF8String"))  value = new String((new AVP_OctetString(avp)).queryValue());
        }
        catch(Exception e)
        {
        	// case when there is a decoding problem : 
        	// usually the dictionary is not wrong according to the received data
        	value = Utils.toBinaryString(new AVP_OctetString(avp).queryValue(), false);
        }

        try
        {
            if(null != avpDef)
            {
                String valueName = avpDef.getEnumNameByCode(value);
                if(null == valueName)
                {
                    if(isTypeAppId)
                    {
                        Application app = Dictionary.getInstance().getApplication(value);
                        if(null != app) valueName = app.get_name();
                    }
                    else if(isTypeVendorId && Utils.isInteger(value))
                    {
                        VendorDef vendorDef = Dictionary.getInstance().getVendorDefByCode(Integer.parseInt(value), applicationId);
                        if(null != vendorDef) valueName = vendorDef.get_vendor_id();
                    }
                }
                if(valueName != null) value = valueName + "(" + value + ")";
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        ret += " value=\"" + value + "\"";
        ret += " type=\"" + type + "\"";
        ret += " vendorId=\"" + avp.vendor_id + "\"";
        ret += " v=\"" + avp.isVendorSpecific() +"\"";
        ret += " m=\"" + avp.isMandatory() + "\"";
        ret += " p=\"" + avp.isPrivate() + "\"";
        
        if(type.equalsIgnoreCase("grouped"))
        {
            ret += ">\n";
            
            AVP_Grouped gavp = null;
            
            try
            {
            	gavp = new AVP_Grouped(avp);
            }
            catch (Exception e)
           
            {
            	throw e;
            }
            AVP[] tavp = gavp.queryAVPs();
            for(int i=0; i<tavp.length; i++)
            {
                try
                {
                    ret += avpToString(tavp[i], indent+1, applicationId);
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
        
    private static String avpToStringSafe(AVP avp, int indent)
    {
        String ret = "";
        ret += Utils.indent(indent);
        
        ret += "<avp code=\"" + avp.code + "\" type=\"OctetString\" ";
        ret += "value=\"" + Utils.toBinaryString(AVPInterface.encode(avp)) + "\" />\n";
        
        return ret;
    }

    /** codeString */
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
            codeString = null;
        }
	    return codeString;
    }

    /** applicationId */
    private String getApplicationIdString()
    {
    	if (applicationIdString != null) {
    		return applicationIdString;    		
    	}
	    try
	    {
	        applicationIdString = Dictionary.getInstance().getApplication(Integer.toString(message.hdr.application_id)).get_name();
	    }
	    catch(Exception e)
	    {
	        applicationIdString = null;
	    }
	    return applicationIdString;
    }
    
        /**
     * prints as String an avp and it's sub-avps (recursive)
     */
    private static String avpToXml(AVP avp, int indent, String applicationId) throws Exception
    {
        String ret = "" ;
        // retrieve the type of avp
        AvpDef avpDef = Dictionary.getInstance().getAvpDefByCode(avp.code, applicationId);
        
        
        // retrieve the top-parent type
        TypeDef typeDef = null;
        
        boolean isTypeAppId = false;
        boolean isTypeVendorId = false;
        
        if(null != avpDef)
        {
            typeDef = avpDef.get_type();
            while(null != typeDef && null != typeDef.get_type_parent())
            {
                if(typeDef.get_type_name().equalsIgnoreCase("AppId"))     isTypeAppId = true;
                if(typeDef.get_type_name().equalsIgnoreCase("VendorId"))  isTypeVendorId = true;
                typeDef = typeDef.get_type_parent();
            }
            
        }
        
        // determine the type name
        String type = "OctetString" ;
        if(null != typeDef) type = typeDef.get_type_name();
        if(null != avpDef && avpDef.getGroupedAvpNameList().size()!=0) type = "grouped";
        
        // retrieve the AVP name
        String name = "unknown ";
        if(null != avpDef) name = avpDef.get_name();

        // retrieve the AVP name
        int code= avp.code;
        if(null != avpDef) code = avpDef.get_code();
        
        ret += Utils.indent(indent) + "<avp";
        ret += " code=\"" + code + "\"";

        String value = "";
        try
        {
            if(type.equalsIgnoreCase("grouped"))          value = "grouped" ;
            else if(type.equalsIgnoreCase("Integer32")){
                int tmp = (new AVP_Integer32(avp)).queryValue();
                value = Long.toString(tmp);
            }
            else if(type.equalsIgnoreCase("Integer64")){
            	value = Long.toString((new AVP_Integer64(avp)).queryValue());
            }
            else if(type.equalsIgnoreCase("Unsigned32")){
            	Long l = new Long(new AVP_Unsigned32(avp).queryValue() & 0xffffffffl); 
            	value = Long.toString(l);
            }
            else if(type.equalsIgnoreCase("Unsigned64")){
            	value = Long.toString((new AVP_Unsigned64(avp)).queryValue() & 0xffffffffffffffffl);
            }
            else if(type.equalsIgnoreCase("OctetString")){
            	value = Utils.toBinaryString(new AVP_OctetString(avp).queryValue(), false);
            }
            else if(type.equalsIgnoreCase("IPAddress")){
            	value = InetAddress.getByAddress((new AVP_OctetString(avp)).queryValue()).getHostAddress();
            }
            else if(type.equalsIgnoreCase("UTF8String")){
            	value = new String((new AVP_OctetString(avp)).queryValue());
            }
        }
        catch(Exception e)
        {
            throw new Exception("Error while trying to decode AVP named " + name + " of code " + avp.code, e);
        }

        
        
        ret += " value=\"" + value + "\"";
        ret += " type=\"" + type + "\"";
        ret += " vendorId=\"" + avp.vendor_id + "\"";
        ret += " mandatory=\"" + avp.isMandatory() + "\"";
        ret += " private=\"" + avp.isPrivate() + "\"";
        
        if(type.equalsIgnoreCase("grouped"))
        {
            ret += ">\n";
            AVP_Grouped gavp = new AVP_Grouped(avp);
            AVP[] tavp = gavp.queryAVPs();
            for(int i=0; i<tavp.length; i++)
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

    private String headerToXml() throws Exception 
    {          
        String ret = "<header command=\"" + message.hdr.command_code + "\"" +
        " applicationId=\"" + message.hdr.application_id + "\"" +
        " hopByHop=\"" + message.hdr.hop_by_hop_identifier + "\"" +
        " endToEnd=\"" + message.hdr.end_to_end_identifier + "\"" +
        " request=\"" + message.hdr.isRequest() + "\"" +
        " proxiable=\"" + message.hdr.isProxiable() + "\"" +
        " error=\"" + message.hdr.isError() + "\"" +
        " retransmit=\"" + message.hdr.isRetransmit() + "\"" + "/>\n";
        return ret;
    }
    
    /** Get the XML representation of the message; for the genscript module. */
    @Override
    public String toXml() throws Exception {
        String xml = headerToXml();
        String applicationId = Integer.toString(message.hdr.application_id);
        Iterable<AVP> iterable = message.avps();
        Iterator<AVP> iterator = iterable.iterator();
        while(iterator.hasNext())
        {
            AVP avp = iterator.next();
            try
            {
                
                xml += avpToXml(avp,0, applicationId);
            }
            catch (Exception e)
            {
                GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "An error occured while logging the DIAMETER message : ", xml);
                e.printStackTrace();
            }
        }
        return xml;
    }

}
