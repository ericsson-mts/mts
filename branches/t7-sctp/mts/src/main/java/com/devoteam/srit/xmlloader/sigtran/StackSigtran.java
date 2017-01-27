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

package com.devoteam.srit.xmlloader.sigtran;

import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.Integer32Array;
import gp.utils.arrays.SupArray;

import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;

import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.core.utils.XMLElementTextMsgParser;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import com.devoteam.srit.xmlloader.sigtran.fvo.FvoDictionary;
import com.devoteam.srit.xmlloader.sigtran.tlv.TlvDictionary;

import com.devoteam.srit.xmlloader.sctp.DataSctp;

public class StackSigtran extends Stack 
{

    private HashMap<String, TlvDictionary> tlvDictionaries;
    private HashMap<String, FvoDictionary> fvoDictionaries;

    private int defaultPayloadProtocolID = 0;
        
    
    public StackSigtran() throws Exception 
    {
        super();
        
        this.tlvDictionaries = new HashMap<String, TlvDictionary>();
        this.fvoDictionaries = new HashMap<String, FvoDictionary>();
        this.defaultPayloadProtocolID = getConfig().getInteger("server.DEFAULT_PPID", 3);
    }

    public TlvDictionary getTlvDictionnary(String name) throws Exception
    {
        if(!tlvDictionaries.containsKey(name))
        {
            tlvDictionaries.put(name, new TlvDictionary(SingletonFSInterface.instance().getInputStream(new URI("../conf/sigtran/"+name)), this));
        }
        return tlvDictionaries.get(name);
    }

    
    public FvoDictionary getFvoDictionnary(String name) throws Exception
    {
        if(!fvoDictionaries.containsKey(name))
        {
            fvoDictionaries.put(name, new FvoDictionary(SingletonFSInterface.instance().getInputStream(new URI("../conf/sigtran/"+name))));
        }
        return fvoDictionaries.get(name);
    }

    /** 
     * Creates a Msg specific to each Stack
     * Use for TCP like protocol : to build incoming message 
     */
    @Override
    public Msg readFromStream(InputStream inputStream, Channel channel) throws Exception 
    {
    	byte[] bytes = null;
    	synchronized (inputStream)
    	{
			bytes = this.readMessageFromStream(inputStream);
    	}

    	if (bytes != null)
    	{
	        //create the message
	        int ppidInt = this.defaultPayloadProtocolID;
	        MsgSigtran msg = new MsgSigtran(this, ppidInt);
	        msg.decode(bytes);
	        return msg;
    	}
    	return null;
    }
    
    /** 
     * Read the message data from the stream
     * Use for TCP/TLS like protocol : to build incoming message  
     */
    public byte[] readMessageFromStream(InputStream inputStream) throws Exception
    {
        byte[] header = new byte[4];
        byte[] lg = new byte[4];
        byte[] buf = null;
        int nbCharRead = 0;
        int msgLength = 0;
        Integer32Array headerArray = null;
        Integer32Array lgArray = null;

        synchronized (inputStream) {
            //read the header
            nbCharRead = inputStream.read(header, 0, 4);
            if (nbCharRead == -1) {
                throw new Exception("End of stream detected");
            }
            else if (nbCharRead < 4) {
                throw new Exception("Not enough char read");
            }
            headerArray = new Integer32Array(new DefaultArray(header));

            //read the length
            nbCharRead = inputStream.read(lg, 0, 4);
            if (nbCharRead == -1) {
                throw new Exception("End of stream detected");
            }
            else if (nbCharRead < 4) {
                throw new Exception("Not enough char read");
            }

            lgArray = new Integer32Array(new DefaultArray(lg));
            msgLength = lgArray.getValue();
            buf = new byte[msgLength - 8];
            //read the staying message's data
            nbCharRead = inputStream.read(buf, 0, msgLength - 8);
        }

        if (nbCharRead == -1) {
            throw new Exception("End of stream detected");
        }
        else if (nbCharRead < (msgLength - 8)) {
            throw new Exception("Not enough char read");
        }

        SupArray msgArray = new SupArray();
        msgArray.addFirst(headerArray);
        msgArray.addLast(lgArray);
        msgArray.addLast(new DefaultArray(buf));
    	return msgArray.getBytes();
    }

    /**
     * Creates a Msg specific to each Stack
     * Use for SCTP like protocol : to build incoming message
     */
    @Override
    public Msg readFromSCTPData(DataSctp chunk) throws Exception {
        DefaultArray array = new DefaultArray(chunk.getData());        
        int ppidIntLe = chunk.getInfo().getPpid();
        int ppidIntBe = Utils.convertLittleBigIndian( ppidIntLe );
        // when the PPID is not present into the sctp layer
        if (ppidIntBe == 0)
        {
        	ppidIntBe = this.defaultPayloadProtocolID;
        }
        MsgSigtran msg = new MsgSigtran(this, ppidIntBe);
        msg.decode(array.getBytes());
        return msg;
    }

}
