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

package com.devoteam.srit.xmlloader.udp;

import java.util.LinkedList;
import java.util.List;

import org.dom4j.Element;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.MessageId;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.TransactionId;
import com.devoteam.srit.xmlloader.core.utils.Utils;

/**
 *
 * @author fhenry
 */
public class MsgUdp extends Msg
{	    
    private byte[] data;
 
    /** Creates a new instance */
    public MsgUdp() throws Exception
    {
    	super();
    }
    
    /** Creates a new instance */
    public MsgUdp(byte[] datas, int length) throws Exception
    {
    	data = new byte [length];
    	for (int i=0; i<length; i++)
    		data[i]= datas[i];
    }

    /** Returns the UDP message without entity */
    public byte[] getData()
    {
        return data;
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
        return StackFactory.PROTOCOL_UDP;
    }
    
    /** Return true if the message is a request else return false*/
    public boolean isRequest()
    {
        return true;
    }
    
    /** Get the command code of this message */
    public String getType()
    {
        return "DATAGRAM";
    }
    
    /** Get the result of this answer (null if request) */
    public String getResult()
    {
        return null;
    }
        
    /** Return the transport of the message*/
    public String getTransport() {
    	return StackFactory.PROTOCOL_UDP;
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
    public String toShortString() throws Exception {
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
            throw new ExecutionException("StackUDP: Error while parsing data", e);
        }

        //
        // Compute total length
        //
        int dataLength = 0;
        for (byte[] data : datas)
        {
            dataLength += data.length;
        }

        this.data = new byte[dataLength];
        int i = 0;
        for (byte[] aData : datas)
        {
            for (int j = 0; j < aData.length; j++)
            {
                this.data[i] = aData[j];
                i++;
            }
        }

        String length = root.attributeValue("length");
        if (length != null)
        {
        	dataLength = Integer.parseInt(length);
            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "fixed length of the datagramPacket to be sent:  ", dataLength);
            if (this.data.length != dataLength)
            {
                GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "data.length different from chosen fixed length");
            }
        }
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
                var.add(new String(getData()));
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
