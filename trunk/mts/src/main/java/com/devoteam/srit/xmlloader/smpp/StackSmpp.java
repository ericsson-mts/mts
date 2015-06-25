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
import com.devoteam.srit.xmlloader.core.ParameterPool;

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.core.utils.XMLElementTextMsgParser;
import com.devoteam.srit.xmlloader.core.utils.dictionaryElement.Attribute;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import com.devoteam.srit.xmlloader.core.utils.gsm.GSMConversion;
import com.devoteam.srit.xmlloader.smpp.data.SmppChoice;
import com.devoteam.srit.xmlloader.smpp.data.SmppGroup;
import com.devoteam.srit.xmlloader.smpp.data.SmppTLV;

import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.Integer32Array;
import gp.utils.arrays.SupArray;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.util.List;
import java.util.Vector;
import org.dom4j.Element;

/**
 *
 * @author bbouvier
 */
public class StackSmpp extends Stack
{
    protected SmppDictionary smppDictionary;

    
    /** Creates a new instance */
    public StackSmpp() throws Exception
    {
        super();
        String dictionaryVersion = getConfig().getString("smpp.DICTIONARY_VERSION");

        if(dictionaryVersion.equalsIgnoreCase("3.4"))
        {
            dictionaryVersion = "../conf/smpp/dictionary_v3.4.xml";
        }
        else if(dictionaryVersion.equalsIgnoreCase("5.0"))
        {
            dictionaryVersion = "../conf/smpp/dictionary_v5.0.xml";
        }
        else
        {
            throw new Exception("SMPP dictionary version " + dictionaryVersion + " not supported");
        }
        
        this.smppDictionary = new SmppDictionary(SingletonFSInterface.instance().getInputStream(new URI(dictionaryVersion)));
    }

    /** 
     * Read the message data from the stream
     * Use for TCP/TLS like protocol : to build incoming message  
     */
    @Override
    public byte[] readMessageFromStream(InputStream inputStream) throws Exception
    {
        byte[] lg = new byte[4];
        byte[] buf = null;
        int nbCharRead = 0;
        int msgLength = 0;
        Integer32Array lgArray = null;
        SupArray msgArray = new SupArray();
        
        synchronized (inputStream)
        {
            //read the length
            nbCharRead = inputStream.read(lg, 0, 4);
            if(nbCharRead == -1)
                throw new Exception("End of stream detected");
            else if(nbCharRead < 4)
                throw new Exception("Not enough char read");

            lgArray = new Integer32Array(new DefaultArray(lg));
            msgLength = lgArray.getValue();
            buf = new byte[msgLength-4];
            //read the staying message's data
            nbCharRead = inputStream.read(buf, 0, msgLength-4);
        }

        if(nbCharRead == -1)
            throw new Exception("End of stream detected");
        else if(nbCharRead < (msgLength-4))
            throw new Exception("Not enough char read");

        msgArray.addFirst(lgArray);
        msgArray.addLast(new DefaultArray(buf));
        
        return msgArray.getBytes();
    }

}
