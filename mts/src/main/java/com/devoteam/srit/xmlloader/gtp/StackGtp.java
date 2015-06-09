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

package com.devoteam.srit.xmlloader.gtp;

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.core.utils.XMLElementTextMsgParser;
import com.devoteam.srit.xmlloader.gtp.data.MessageGTP;

import java.io.InputStream;
import java.net.InetAddress;
import org.dom4j.Element;

/**
 *
 * @author bbouvier
 */
public class StackGtp extends Stack
{

    /** Constructor */
    public StackGtp() throws Exception
    {
        super();
        // initiate a default listenpoint if port is not empty or null
        int port = getConfig().getInteger("listenpoint.LOCAL_PORT", 0);
        if (port > 0)
        {
        	Listenpoint listenpoint = new Listenpoint(this);
            createListenpoint(listenpoint, StackFactory.PROTOCOL_GTP);
        }
    }

    @Override
    public Msg readFromStream(InputStream inputStream, Channel channel) throws Exception
    {
        MessageGTP message = new MessageGTP();
        message.decodeFromStream(inputStream);
        return new MsgGtp(message);
    }

    /** 
     * Creates a Msg specific to each Stack
     * Use for UDP like protocol : to build incoming message
     * should become ABSTRACT later  
     */
    public Msg readFromDatas(byte[] datas, int length) throws Exception
    {   
    	byte[] newDatas = datas.clone();
        MessageGTP message = new MessageGTP();
        message.decodeFromBytes(newDatas);
        return new MsgGtp(message);
    }
    
    /** Returns the Config object to access the protocol config file*/
    public Config getConfig() throws Exception
    {
        return Config.getConfigByName("gtp.properties");
    }

    /** Returns the replacer used to parse sendMsg Operations */
    public XMLElementReplacer getElementReplacer()
    {
        return XMLElementTextMsgParser.instance();
    }

}
