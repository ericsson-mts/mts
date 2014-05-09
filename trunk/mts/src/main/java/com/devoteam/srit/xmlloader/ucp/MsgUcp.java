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

package com.devoteam.srit.xmlloader.ucp;

import com.devoteam.srit.xmlloader.ucp.data.*;
import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.gsm.GSMConversion;
import gp.utils.arrays.DefaultArray;
import java.util.Vector;

/**
 *
 * @author bbouvier
 */
public class MsgUcp extends Msg
{
    private UcpMessage ucpMessage = null;
    private String type = null;
    private String typeComplete = null;
    private String result = null;

    /**
     * Creates a new instance of MsgUcp
     */
    public MsgUcp(UcpMessage message) throws Exception
    {
        ucpMessage = message;
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
                if (params[1].equalsIgnoreCase("OT"))
                {
                    var.add(ucpMessage.getOperationType());
                }
                else if (params[1].equalsIgnoreCase("name"))
                {
                    var.add(ucpMessage.getName());
                }
                else if (params[1].equalsIgnoreCase("length"))
                {
                    var.add(ucpMessage.getLength());
                }
                else if (params[1].equalsIgnoreCase("TRN"))
                {
                    var.add(ucpMessage.getTransactionNumber());
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
                UcpAttribute att = ucpMessage.getAttribute(params[1]);

                if(att != null)
                {
                    if(att.getName().equalsIgnoreCase("XSer"))
                    {
                        var.add(att.getValue());
                    }
                    else if(att.getValue() instanceof Vector)
                    {
                        for(int i = 0; i < ((Vector)att.getValue()).size(); i++)
                        {
                            var.add((String)((UcpAttribute)((Vector)att.getValue()).get(i)).getValue());
                        }
                    }
                    else
                    {
                        if(att.getFormat().equals("encodedString"))
                        {
                            if((params.length == 3)
                               && (params[2].equalsIgnoreCase("binary")))//to return binary value if asked for an attribute with encodedString format
                            {
                                var.add(att.getValue());
                            }
                            else
                            {
                                String val = new String(DefaultArray.fromHexString((String)att.getValue()).getBytes());
                                var.add(new String(GSMConversion.fromGsmCharset(val)));
                            }
                        }
                        else
                        {
                            var.add(att.getValue());
                        }
                    }
                }
            }
        }
        else if (params[0].equalsIgnoreCase("content"))
        {
            var.add(ucpMessage.getData());
        }
        else if (params[0].equalsIgnoreCase("dataRaw"))
        {
            var.add(ucpMessage.getDataRaw());
        }
        else if (params[0].equalsIgnoreCase("malformed"))
        {           
            if(ucpMessage.getLogError().length() == 0)
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

    /** Get the protocol of this message */
    public String getProtocol()
    {
        return StackFactory.PROTOCOL_UCP;
    }

    /** Return true if the message is a request else return false*/
    public boolean isRequest()
    {
        return (ucpMessage.getMessageType().equals("O")) ? true : false;
    }

    /** Get the command code of this message */
    public String getType()
    {
        if(type == null)
        {
            type = ucpMessage.getName();
        }
        return type;
    }

    /** Get the complete type (with dictionary conversion) of this message */
    @Override
    public String getTypeComplete()
    {
        if(typeComplete == null)
        {
            typeComplete = ucpMessage.getName() + ":" + ucpMessage.getOperationType();
        }
        return typeComplete;
    }

    /** Get the result of this answer (null if request) */
    public String getResult()
    {
        if(result == null)
        {
            if(!isRequest())
            {
                if(ucpMessage.getAttribute("ACK") != null)
                {
                    result = (String)ucpMessage.getAttribute("ACK").getValue();
                }
                else if(ucpMessage.getAttribute("NACK") != null)
                {
                    result = (String)ucpMessage.getAttribute("NACK").getValue();
                }
            }
        }
        return result;
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
    	
    	/** On masque ici une exception NullPointerException lors de la reception d'un message UCP
    	/** A l'envoi du message il n'y a pas d'exception dans cette methode : le message UCP est sans 
    	 * doute mal construit à la réception (voir classe ChoiceUcp ligne #42
    	/** TODO : regarder pourquoi ? */
        try {
            return ucpMessage.getArray().getBytes();
        }
        catch (Exception ex)
        {
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, ex, "Error while encoding the message UCP : ");
        }
        return new byte[0];
    }

    /** Returns a short description of the message. Used for logging as INFO level */
    /** This methods HAS TO be quick to execute for performance reason */
    @Override
    public String toShortString() throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(super.toShortString());
        if(ucpMessage.getLogError().length() != 0)
        {
            stringBuilder.append("<MESSAGE MALFORMED name= \"" + ucpMessage.getName() + "\"");
        }
        else
        {
            stringBuilder.append("<MESSAGE name= \"" + ucpMessage.getName() + "\"");
        }
        stringBuilder.append(" OT=\"" + ucpMessage.getOperationType() + "\"");
        stringBuilder.append(" O/R=\"" + ucpMessage.getMessageType() + "\"");
        stringBuilder.append(" length=\"" + ucpMessage.getLength() + "\"");
        stringBuilder.append(" TRN=\"" + ucpMessage.getTransactionNumber() + "\"/>");

        return stringBuilder.toString();
    }

    /** Get the XML representation of the message; for the genscript module. */
    @Override
    public String toXml() throws Exception {
    	return ucpMessage.toString();
    }

}
