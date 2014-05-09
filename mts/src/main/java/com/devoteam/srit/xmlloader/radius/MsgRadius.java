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

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.Trans;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import gp.net.radius.data.AVPBytes;
import gp.net.radius.data.AVPInteger;
import gp.net.radius.data.RadiusMessage;
import gp.net.radius.data.AVPString;
import gp.net.radius.data.AVPVendorSpecific;
import gp.net.radius.dictionary.RadiusAttributes;
import gp.net.radius.dictionary.RadiusDictionary;
import gp.utils.arrays.Array;
import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.Integer32Array;
import java.net.InetAddress;

/**
 *
 * @author 
 */
public class MsgRadius extends Msg
{

    private RadiusMessage radiusMessage;
    private RadiusDictionary radiusDictionary;

    /**
     * Creates a new instance of MsgUdp from a byte array
     */
    public MsgRadius(RadiusMessage radiusMessage) throws Exception
    {
        this.radiusMessage = radiusMessage;
        this.radiusDictionary = null;
    }

    protected boolean hasValidAuthenticator() throws Exception
    {
        if (StackFactory.getStack(StackFactory.PROTOCOL_RADIUS).getConfig().getBoolean("radius.CHECK_AUTHENTICATOR"))
        {
            if (this.isRequest())
            {
                GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "MsgRadius, checking request authenticator...");
                return this.radiusMessage.hasValidRequestAuthenticator();
            }
            else
            {
                GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "MsgRadius, checking response authenticator...");
                Array requestAuthenticator = this.getAssociatedRequest().getRadiusMessage().getAuthenticator();
                return this.radiusMessage.hasValidResponseAuthenticator(requestAuthenticator);
            }
        }
        else
        {
            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "MsgRadius, do not check radius authenticator");
            return true;
        }
    }
    
    protected MsgRadius getAssociatedRequest() throws Exception
    {
        Trans trans = StackFactory.getStack(StackFactory.PROTOCOL_RADIUS).getOutTransaction(this.getTransactionId());
        if(null == trans)
        {
            throw new Exception("Could not find response's associated request transactionId=" + this.getTransactionId());
        }
        return (MsgRadius) trans.getBeginMsg();
    }
    
    protected RadiusMessage getRadiusMessage()
    {
        return this.radiusMessage;
    }

    private RadiusDictionary getRadiusDictionary() throws Exception
    {
        if(null == this.radiusDictionary)
        {
            this.radiusDictionary = ((StackRadius)StackFactory.getStack(StackFactory.PROTOCOL_RADIUS)).getRadiusDictionary();
        }
        
        return this.radiusDictionary;
    }
    
    /** Get a parameter from the message */
    @Override
    public Parameter getParameter(String path) throws Exception
    {
        Parameter var = super.getParameter(path);
        if ((null != var) && (var.length() > 0))
        {
            return var;
        }

        var = new Parameter();
        path = path.trim();
        String[] params = Utils.splitPath(path);
        
        if (params[0].equalsIgnoreCase("header"))
        {
            if (params[1].equalsIgnoreCase("identifier"))
            {
                var.add(Integer.toString(this.radiusMessage.getIdentifier()));
            }
            else if (params[1].equalsIgnoreCase("authenticator"))
            {
                var.add(this.radiusMessage.getAuthenticator().toString().replace("\n", " ").replace(" ", ""));
            }
            else if (params[1].equalsIgnoreCase("code"))
            {               
            	var.add(String.valueOf(this.radiusMessage.getCode()));
            }
            else
            {
            	Parameter.throwBadPathKeywordException(path);
            }
        }
        else if (params[0].equalsIgnoreCase("channel"))
        {
            if (params[1].equalsIgnoreCase("remoteHost"))
            {
            	var.add(this.radiusMessage.getRemoteAddress().getAddress().getHostAddress());
            }
            else if (params[1].equalsIgnoreCase("remotePort"))
            {
            	var.add(Integer.toString(this.radiusMessage.getRemoteAddress().getPort()));
            }
            else if (params[1].equalsIgnoreCase("UID"))
            {
            	if (this.getChannel() != null)
            	{
            		var.add(this.getChannel().getUID());
            	}
            }
        }
	    else if (params[0].equalsIgnoreCase("listenpoint"))
	    {
		    if (params[1].equalsIgnoreCase("UID"))
		    {
            	if (this.getListenpoint() != null)
            	{
            		var.add(this.getListenpoint().getUID());
            	}
		    }
	    }
        else if(params.length == 2)
        {
            /*
             * Generic case. We assume the path has the following format:
             * [AVP# or AVPName]:[keyword]
             * keyword values: text, string, int, octet, ipaddr, length
             */
            String avpName = params[0];
            String keyword = params[1];
            Integer avpCode;
            if(Utils.isInteger(avpName))
            {
                avpCode = Integer.parseInt(avpName);
            }
            else
            {
                RadiusAttributes radiusAttributes = getRadiusDictionary().getRadiusAttributes(-1);
                avpCode = radiusAttributes.getAttributeCode(avpName);
                if(null == avpCode)
                {
                    throw new Exception("unknown avp name " + avpName);
                }
            }
            
            for(AVPBytes avp: this.radiusMessage.getAVPs())
            {
                if(avp.getType() == avpCode)
                {
                    if(keyword.equalsIgnoreCase("text") || keyword.equalsIgnoreCase("string"))
                    {
                    	var.add(new AVPString(avp, "UTF8").getValue());
                    }
                    else if(keyword.equalsIgnoreCase("octet") || keyword.equalsIgnoreCase("byte"))
                    {
                    	var.add(avp.getArray().toString().replace(" ", "").replace("\n", ""));
                    }
                    else if(keyword.equalsIgnoreCase("int"))
                    {
                    	var.add(String.valueOf(new AVPInteger(avp).getValue()));
                    }
                    else if(keyword.equalsIgnoreCase("ipaddr"))
                    {
                        Array array = avp.getData();

                        if(array.length != 4) throw new Exception("invalid length of data for ipaddr avp");
                        
                        StringBuilder builder = new StringBuilder();
                        
                        builder.append(new Integer08Array(array.subArray(0,1)).getValue());
                        builder.append('.');
                        builder.append(new Integer08Array(array.subArray(1,1)).getValue());
                        builder.append('.');
                        builder.append(new Integer08Array(array.subArray(2,1)).getValue());
                        builder.append('.');
                        builder.append(new Integer08Array(array.subArray(3,1)).getValue());
                        
                        var.add(builder.toString());
                    }
                    else if(keyword.equalsIgnoreCase("length"))
                    {
                    	var.add(String.valueOf(avp.getLength()));
                    }
                    else
                    {
                    	Parameter.throwBadPathKeywordException(path);
                    }
                }
            }
            
            
        }
        
        return var;
    }

    /** Get the protocol of this message */
    @Override
    public String getProtocol()
    {
        return StackFactory.PROTOCOL_RADIUS;
    }

    /** Return true if the message is a request else return false*/
    @Override
    public boolean isRequest()
    {
        int code = this.radiusMessage.getCode();

        switch (code)
        {
            case 1:
            case 4:
                return true;
            default:
                return false;
        }
    }

    /** Get the type of this message */
    @Override
    public String getType()
    {
        int code = this.radiusMessage.getCode();
        
        switch(code)
        {
            case 1:
            case 2:
            case 3:
            case 11:
                return "Access";
            case 4:
            case 5:
                return "Accounting";
            default:
                return Integer.toString(code);
        }
    }

    /** Get the complete type of this message */
    @Override
    public String getTypeComplete()
    {
        int code = this.radiusMessage.getCode();
        switch(code)
        {
            case 1:
            case 2:
            case 3:
            case 11:
                return "Access-Request:1";
            case 4:
            case 5:
                return "Accounting-Request:4";
            default:
                return Integer.toString(code);
        }
    }

    /** Get the result of this answer (null if request) */

    public String getResult()
    {
        int code = this.radiusMessage.getCode();
        
        switch(code)
        {
            case 2:
            case 3:
            case 11:
            case 5:
                return Integer.toString(code);               
            default:
                return Integer.toString(this.radiusMessage.getCode());
        }
    }

    /** Get the complete result of this answer (null if request) */
    @Override
    public String getResultComplete()
    {
        int code = this.radiusMessage.getCode();
        
        switch(code)
        {
            case 2:
            	return "Access-Accept:2";
            case 3:
            	return "Access-Reject:3";
            case 11:
            	return "Access-Challenge:11";
            case 5:
                return "Accounting-Response:5";
            default:
                return Integer.toString(this.radiusMessage.getCode());
        }
    }

    /** Return the length of the message*/
    @Override
    public int getLength()
    {
        return this.radiusMessage.getLength();
    }
 
    @Override
    public boolean shallStopRetransmit()
    {
        return !this.isRequest();
    }
    
    /** Get the data (as binary) of this message */    
    @Override
    public byte[] getBytesData(){    	
    	return radiusMessage.getArray().getBytes();
    }

    /** Returns a short description of the message. Used for logging as INFO level */
    /** This methods HAS TO be quick to execute for performance reason */
    @Override
    public String toShortString() throws Exception {
        StringBuilder StringBuilder = new StringBuilder();
        StringBuilder.append(super.toShortString());
        StringBuilder.append("<MESSAGE commandCode= \"" + this.radiusMessage.getCode() + "\"");
        StringBuilder.append(" identifier=\"" + this.radiusMessage.getIdentifier() + "\"");
        StringBuilder.append(" authenticator=\"" + this.radiusMessage.getAuthenticator() + "\"/>");
        return StringBuilder.toString();
    }
    
    /** Get the XML representation of the message; for the genscript module. */    
    @Override
    public String toXml() throws Exception {
        RadiusDictionary radiusDictionary = null;
        try
        {
            radiusDictionary = getRadiusDictionary();
        }
        catch(Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "An error occured while logging the HTTP message : ");
            e.printStackTrace();        	            
        }

        StringBuilder StringBuilder = new StringBuilder();
        StringBuilder.append("<Channel " + getChannel() + "/>\n");

        StringBuilder.append("<message remoteHost=\"" + this.radiusMessage.getRemoteAddress().getAddress().getHostAddress() + "\", ");
        StringBuilder.append("remotePort=\"" + this.radiusMessage.getRemoteAddress().getPort() + "\">\n");        
        StringBuilder.append("    <header ");
        
        String codeName = radiusDictionary.getRadiusCodes().getName(this.radiusMessage.getCode());
        StringBuilder.append(" code= \"" + codeName + " (" + this.radiusMessage.getCode() + ")\",");

        StringBuilder.append(" identifier=\"" + this.radiusMessage.getIdentifier() + "\",");
        StringBuilder.append(" authenticator=\"" + this.radiusMessage.getAuthenticator() + "\"/>");
        StringBuilder.append("\n");


        
        for(AVPBytes avpBytes:this.radiusMessage.getAVPs())
        {
            int code = avpBytes.getType();
            
            String attributeName = radiusDictionary.getRadiusVendors().getRadiusAttributes(-1).getAttributeName(code);
            if(null == attributeName) attributeName = Integer.toString(code);
            
            String attributeType = radiusDictionary.getRadiusVendors().getRadiusAttributes(-1).getAttributeType(code);
            if(null == attributeType) attributeType = "octet";
            
            String attributeValue;
            if(attributeType.equalsIgnoreCase("integer"))
            {
                AVPInteger AVPInteger = new AVPInteger(avpBytes);
                attributeValue = radiusDictionary.getRadiusVendors().getRadiusAttributes(-1).getAttributeRadiusValues(code).getValueName(AVPInteger.getValue());
            }
            else if(attributeType.equalsIgnoreCase("ipaddr"))
            {
                try
                {
                    attributeValue = InetAddress.getByAddress(avpBytes.getData().getBytes()).getHostAddress();
                }
                catch(Exception e)
                {
                    attributeValue = null;
                }
            }
            else if(attributeType.equalsIgnoreCase("octet"))
            {
                // no need to compute it here, octet is the default display
                attributeValue = null;
            }
            else // text, string
            {
                try
                {
                    AVPString AVPString = new AVPString(avpBytes, "UTF8");
                    attributeValue = AVPString.getValue();
                }
                catch(Exception e)
                {
                    attributeValue = null;
                }
            }
            

            
            if(avpBytes.getType() != 26)
            {
                if(null == attributeValue)
                {
                    attributeValue = avpBytes.getData().toString().replace('\n',' ');
                }

                StringBuilder.append("    <avp");
                StringBuilder.append(" code=\"" + attributeName + "(" + code + ")\"");
                StringBuilder.append(" type=\"" + attributeType + "\"");
                StringBuilder.append(" value=\"" + attributeValue + "\"");
                StringBuilder.append(" />\n");
            }
            else
            {
                StringBuilder.append("    <avp");
                StringBuilder.append(" code=\"" + attributeName + "\"");
                StringBuilder.append(" type=\"" + attributeType + "\"");
                try
                {
                    AVPVendorSpecific AVPVendorSpecific = new AVPVendorSpecific(avpBytes);

                    Integer32Array vendorIdArray = new Integer32Array(AVPVendorSpecific.getData().subArray(0, 4));
                    Integer vendorId = vendorIdArray.getValue();

                    attributeValue = radiusDictionary.getRadiusVendors().getVendorName(vendorId);
                    if(null == attributeValue) attributeValue = Integer.toString(vendorId);


                    StringBuilder.append(" value=\"" + attributeValue + "\"");
                    StringBuilder.append(">\n");

                    StringBuilder.append(AVPVendorSpecific.getData().subArray(4).toString());
                    StringBuilder.append("\n");

                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    StringBuilder.append(" error=\"" + e.toString() + "\"");
                }

                StringBuilder.append("    </avp>\n");
            }
        }
        StringBuilder.append("</message>");        
        return StringBuilder.toString();
    }
}
