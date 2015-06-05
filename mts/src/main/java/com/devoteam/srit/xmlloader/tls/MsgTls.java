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

package com.devoteam.srit.xmlloader.tls;

import java.util.LinkedList;
import java.util.List;

import org.dom4j.Element;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.protocol.MessageId;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.TransactionId;
import com.devoteam.srit.xmlloader.core.utils.Utils;

/**
 *
 * @author fvandecasteele
 */
public class MsgTls extends Msg
{
    private byte[] data;

    private String type;

    /** Creates a new instance */
    public MsgTls() throws Exception
    {
    	super();
    }
    
    public TransactionId getTransactionId() throws Exception
    {
        return null;
    }
    
    public MessageId getMessageId() throws Exception
    {
        return null;
    }

    /** Get the protocol of this message */
    public String getProtocol()
    {
        return StackFactory.PROTOCOL_TLS;
    }
    
    /** Return true if the message is a request else return false*/
    public boolean isRequest()
    {
        return true;
    }
        
    /** Get the type of this message */
    public String getType()
    {
        return type;
    }
    /** Set the type of this message */
    public void setType(String type)
    {
        this.type = type;
    }    
    
    /** Get the result of this message */
    public String getResult()
    {
        return null;
    }
    
    /** Return the transport of the message*/
    public String getTransport() {
    	return StackFactory.PROTOCOL_TLS;
    }

    /** Get the data (as binary) of this message */
    @Override
    public byte[] encode()
    {
        return data;
    }

    /** Returns a short description of the message. Used for logging as INFO level */
    /** This methods HAS TO be quick to execute for performance reason */
    @Override
    public String toShortString()throws Exception {
    	String ret = super.toShortString();
    	ret += "\n";
    	ret += Utils.toStringBinary(data, Math.min(data.length, 100));
        return ret;
    }

    /** Get the XML representation of the message; for the genscript module. */
    @Override
    public String toXml() throws Exception {
    	String ret = getTypeComplete();
    	ret += "\n" + Utils.byteTabToString(data);
    	return ret;
    }
    
    /** 
     * Parse the message from XML element 
     */
    @Override
    public void parseMsgFromXml(Boolean request, Element root, Runner runner) throws Exception
    {
        List<Element> elements = root.elements("data");
        List<byte[]> datas = new LinkedList<byte[]>();
        ;
        try
        {
            for (Element element : elements)
            {
                if (element.attributeValue("format").equalsIgnoreCase("text"))
                {
                    String text = element.getText();
                    // change the \n caractère to \r\n caracteres because the dom librairy return only \n.
                    // this could make some trouble when the length is calculated in the scenario
                    text = Utils.replaceNoRegex(text, "\r\n","\n");                    
                    text = Utils.replaceNoRegex(text, "\n","\r\n");                                        
                    datas.add(text.getBytes("UTF8"));
                }
                else if (element.attributeValue("format").equalsIgnoreCase("binary"))
                {
                    String text = element.getTextTrim();
                    datas.add(Utils.parseBinaryString(text));
                }
            }
        }
        catch (Exception e)
        {
            throw new ExecutionException(e);
        }

        //
        // Compute total length
        //
        int length = 0;
        for (byte[] data : datas)
        {
            length += data.length;
        }

        byte[] data = new byte[length];

        int i = 0;
        for (byte[] aData : datas)
        {
            for (int j = 0; j < aData.length; j++)
            {
                data[i] = aData[j];
                i++;
            }
        }
        setMessageBinary(data);
    }

    /** Get the message as binary */
    /*
    public String getMessageBinary() throws Exception
    {
    	return message.toString();
    }
    */
    
    /** Set the message from binary */
    public void setMessageBinary(byte[] binary) throws Exception {
    	this.data = binary;
        this.type = "SEQ-ACK";    
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
        if (null != var)
        {
            return var;
        }

    	var = new Parameter();
        path = path.trim();
        String[] params = Utils.splitPath(path);
        
        if(params[0].equalsIgnoreCase("data")) 
        {
            if(params[1].equalsIgnoreCase("text")) 
            {
            	var.add(new String(encode()));
            }
            else if(params[1].equalsIgnoreCase("binary")) 
            {
            	var.add(Array.toHexString(new DefaultArray(encode())));
            }
            else 
            {
            	Parameter.throwBadPathKeywordException(path);
            }
        }
        else 
        {
        	Parameter.throwBadPathKeywordException(path);
        }

        return var;
    } 
    
}
