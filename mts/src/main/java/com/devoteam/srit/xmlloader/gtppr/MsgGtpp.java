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

package com.devoteam.srit.xmlloader.gtppr;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.dictionaryElement.Attribute;
import com.devoteam.srit.xmlloader.gtppr.data.GtppMessage;
import com.devoteam.srit.xmlloader.gtppr.data.Tag;

import gp.utils.arrays.Array;

/**
 *
 * @author bbouvier
 */
public class MsgGtpp extends Msg
{
    private GtppMessage message = null;
    private String type = null;
//    private String typeComplete = null;
    private String result = null;
    
    /**
     * Creates a new instance of MsgGtpp
     */
    public MsgGtpp(GtppMessage message) throws Exception
    {
        this.message = message;
    }

    /** Get a parameter from the message */
    @Override
    public Parameter getParameter(String path) throws Exception
    {
        Parameter var = super.getParameter(path);
        if (null != var)
        {
            return var;
        }

        var = new Parameter();
        path = path.trim();
        String[] params = Utils.splitPath(path);

        if (params[0].equalsIgnoreCase("header"))
        {
            if(params.length == 2)
            {
                if (params[1].equalsIgnoreCase("type"))
                {
                    var.add(message.getHeader().getMessageType());
                }
                else if (params[1].equalsIgnoreCase("name"))
                {
                    var.add(message.getHeader().getName());
                }
                else if (params[1].equalsIgnoreCase("length"))
                {
                    var.add(message.getHeader().getLength());
                }
                else if (params[1].equalsIgnoreCase("sequenceNumber"))
                {
                    var.add(message.getHeader().getSequenceNumber());
                }
                else
                {
                	Parameter.throwBadPathKeywordException(path);
                }
            }
        }
        else if (params[0].equalsIgnoreCase("tlv"))
        {
            if(params.length >= 2)
            {
                //get attribute given
                Tag tlv = message.getTLV(params[1]);
                if(tlv != null)
                {
                    var.add(formatAttribute(tlv));
                }
                else
                {
                    throw new Exception("The tlv <" + tlv.getName() + "> is unknown in message <" + message.getHeader().getName() + ">");
                }
            }
        }
        else if (params[0].equalsIgnoreCase("content"))
        {
            var.add(message.getData());
        }
        else if (params[0].equalsIgnoreCase("malformed"))
        {
            if(message.getLogError().length() == 0)
            {
                var.add(false);
            }
            else
            {
                var.add(true);
            }
        }
        else
        {
        	Parameter.throwBadPathKeywordException(path);
        }

        return var;
    }

    private Object formatAttribute(Attribute att)
    {
        Object value = att.getValue();
        if(value instanceof String)
        {
        	value = ((String)value).trim();
        }
        else if(value instanceof Array)
        {
            value = Array.toHexString((Array)value);
        }
        return value;
    }

    /** Get the protocol of this message */
    public String getProtocol()
    {
        return StackFactory.PROTOCOL_GTP;
    }

    /** Return true if the message is a request else return false*/
    public boolean isRequest()
    {
        return message.getHeader().getName().contains("Request") ? true : false;
    }

    /** Get the command code of this message */
    public String getType()
    {
        if(type == null)
        {
            type = message.getHeader().getName();
        }
        return type;
    }

    /** Get the result of this answer (null if request) */
    public String getResult()
    {
        if(result == null)
        {
//            result = Integer.toString(message.getStatus());
        }
        return result;
    }
    
    /** Return the length of the message*/
    @Override
    public int getLength() {
        try 
        {
        	return message.getArray().length;
        }
        catch (Exception ex)
        {
                GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, "Error while trying to get length of GTPP message : " + ex);
        }
        return 0;

    }
    
    /** Return the transport of the message*/
    @Override
    public String getTransport()
    {
        return StackFactory.PROTOCOL_TCP;
    }
    
    /** Get the data (as binary) of this message */
    @Override    
    public byte[] getBytesData(){
        try 
        {
            return message.getArray().getBytes();
        }
        catch (Exception ex)
        {
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, "Error while trying to write message GTPP on socket: " + ex);
        }
        return null;
    }

    /** Returns a short description of the message. Used for logging as INFO level */
    /** This methods HAS TO be quick to execute for performance reason */
    @Override
    public String toShortString() throws Exception 
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(super.toShortString());
        if(message.getLogError().length() != 0)
        {
            stringBuilder.append("<MESSAGE MALFORMED name= \"" + message.getHeader().getName() + "\"");
        }
        else
        {
            stringBuilder.append("<MESSAGE name= \"" + message.getHeader().getName() + "\"");
        }

        stringBuilder.append(" length=\"" + message.getHeader().getLength() + "\"");
        stringBuilder.append(" type=\"" + Integer.toHexString(message.getHeader().getMessageType()) + "\"");
        stringBuilder.append(" sequenceNumber=\"" + message.getHeader().getSequenceNumber() + "\"/>");
        return stringBuilder.toString();
    }

    /** Get the XML representation of the message; for the genscript module. */
    @Override
    public String toXml() throws Exception {
        return message.toString();
    }

}
