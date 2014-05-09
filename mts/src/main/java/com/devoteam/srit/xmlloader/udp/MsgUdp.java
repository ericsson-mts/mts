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

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.MessageId;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.TransactionId;
import com.devoteam.srit.xmlloader.core.utils.Utils;

/**
 *
 * @author 
 */
public class MsgUdp extends Msg
{
	// private DatagramPacket datagramPacket;
	    
    private byte[] data;
 
    /**
     * Creates a new instance of MsgUdp from a byte array
     */
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
        
        if(params[0].equalsIgnoreCase("data")) 
        {
            if(params[1].equalsIgnoreCase("text")) 
            {
                var.add(new String(getData()));
            }
            else if(params[1].equalsIgnoreCase("binary")) 
            {
            	var.add(Array.toHexString(new DefaultArray(getBytesData())));
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
    
    // <editor-fold desc=" generic methods ">

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
    public byte[] getBytesData()
    {
        return data;
    }
   
    /** Returns a short description of the message. Used for logging as INFO level */
    /** This methods HAS TO be quick to execute for performance reason */
    @Override
    public String toShortString() throws Exception {
    	String ret = super.toShortString();
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
    
}
