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

package com.devoteam.srit.xmlloader.smpp;

import com.devoteam.srit.xmlloader.smpp.data.SmppAttribute;
import com.devoteam.srit.xmlloader.smpp.data.SmppMessage;
import com.devoteam.srit.xmlloader.smpp.data.SmppTLV;
import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.dictionaryElement.Attribute;
import gp.utils.arrays.Array;

/**
 *
 * @author
 */
public class MsgSmpp extends Msg
{
    private SmppMessage smppMessage = null;
    private String type = null;
    private String typeComplete = null;
    private String result = null;
    
    /**
     * Creates a new instance of MsgSmpp
     */
    public MsgSmpp(SmppMessage message) throws Exception
    {
        smppMessage = message;
    }

    protected SmppMessage getSmppMessage()
    {
        return this.smppMessage;
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
                if (params[1].equalsIgnoreCase("id"))
                {
                    var.add(smppMessage.getId());
                }
                else if (params[1].equalsIgnoreCase("name"))
                {
                    var.add(smppMessage.getName());
                }
                else if (params[1].equalsIgnoreCase("length"))
                {
                    var.add(smppMessage.getLength());
                }
                else if (params[1].equalsIgnoreCase("status"))
                {
                    var.add(smppMessage.getStatus());
                }
                else if (params[1].equalsIgnoreCase("sequence_number"))
                {
                    var.add(smppMessage.getSequenceNumber());
                }
                else
                {
                	Parameter.throwBadPathKeywordException(path);
                }
            }
        }
        else if (params[0].equalsIgnoreCase("attribute"))
        {
            if(params.length >= 2)
            {
                //get attribute given
                SmppAttribute att = smppMessage.getAttribut(params[1]);
                if(att != null)
                {
                    var.add(formatAttribute(att));
                }
                else
                {
                    throw new Exception("The attribute <" + att.getName() + "> is unknown in message <" + smppMessage.getName() + ">");
                }
            }
        }
        else if (params[0].equalsIgnoreCase("tlv"))
        {
            if(params.length >= 2)
            {
                //get attribute given
                SmppTLV tlv = smppMessage.getTLV(params[1]);
                if(tlv != null)
                {
                    var.add(formatAttribute(tlv));
                }
                else
                {
                    throw new Exception("The tlv <" + tlv.getName() + "> is unknown in message <" + smppMessage.getName() + ">");
                }
            }
        }
        else if (params[0].equalsIgnoreCase("content"))
        {
            var.add(smppMessage.getData());
        }
        else if (params[0].equalsIgnoreCase("malformed"))
        {
            if(smppMessage.getLogError().length() == 0)
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
        return StackFactory.PROTOCOL_SMPP;
    }

    /** Return true if the message is a request else return false*/
    public boolean isRequest()
    {
        return (smppMessage.getId() >= 0) ? true : false;
    }

    /** Get the command code of this message */
    public String getType()
    {
        if(type == null)
        {
            type = smppMessage.getName();
        }
        return type;
    }

    /** Get the complete type (with dictionary conversion) of this message */
    @Override
    public String getTypeComplete()
    {
        if(typeComplete == null)
        {
            typeComplete = smppMessage.getName().replace("_resp", "") + ":";
            typeComplete += Integer.toHexString(smppMessage.getId() & 0x7FFFFFFF);
        }
        return typeComplete;
    }

    /** Get the result of this answer (null if request) */
    public String getResult()
    {
        if(result == null)
        {
            result = Integer.toString(smppMessage.getStatus());
        }
        return result;
    }
    
    /** Return the length of the message*/
    @Override
    public int getLength() {
        try 
        {
        	return smppMessage.getArray().length;
        }
        catch (Exception ex)
        {
                GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, "Error while trying to write message SMPP on socket: ", ex);
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
            return smppMessage.getArray().getBytes();
        }
        catch (Exception ex)
        {
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, "Error while trying to write message SMPP on socket: ", ex);
        }
        return null;
    }

    /** Returns a short description of the message. Used for logging as INFO level */
    /** This methods HAS TO be quick to execute for performance reason */
    @Override
    public String toShortString() throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(super.toShortString());
        if(smppMessage.getLogError().length() != 0)
        {
            stringBuilder.append("<MESSAGE MALFORMED name= \"" + smppMessage.getName() + "\"");
        }
        else
        {
            stringBuilder.append("<MESSAGE name= \"" + smppMessage.getName() + "\"");
        }

        stringBuilder.append(" length=\"" + smppMessage.getLength() + "\"");
        stringBuilder.append(" id=\"" + Integer.toHexString(smppMessage.getId()) + "\"");
        stringBuilder.append(" status=\"" + smppMessage.getStatus() + "\"");
        stringBuilder.append(" sequence_number=\"" + smppMessage.getSequenceNumber() + "\"/>");
        return stringBuilder.toString();
    }

    /** Get the XML representation of the message; for the genscript module. */
    @Override
    public String toXml() throws Exception {
        return smppMessage.toString();
    }

}
