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

package com.devoteam.srit.xmlloader.h323.h225cs;

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.core.utils.XMLElementTextMsgParser;
import com.devoteam.srit.xmlloader.core.coding.binary.Dictionary;
import com.devoteam.srit.xmlloader.core.coding.binary.XMLDoc;
import com.devoteam.srit.xmlloader.core.coding.tpkt.TPKTPacket;
import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import org.dom4j.Element;

/**
 *
 * @author indiaye
 */
public class StackH225cs extends Stack 
{

    private Dictionary dictionary;
    

    /** Creates a new instance */
    public StackH225cs() throws Exception 
    {
        super();

        // initialise dictionary
        String file = "../conf/sigtran/q931.xml";
        this.dictionary = new Dictionary(file);
    }

    /** 
     * Read the message data from the stream
     * Use for TCP/TLS like protocol : to build incoming message  
     */
    @Override
    public byte[] readMessageFromStream(InputStream inputStream) throws Exception 
    {
        TPKTPacket tpkt = new TPKTPacket(inputStream);
        int length = tpkt.getPacketLength();
        byte[] bytes = new byte[length - 4];
        Utils.readFromSocketStream(inputStream, bytes);
        return bytes;
    }

    public Dictionary getDictionary() {
        return dictionary;
    }
}
