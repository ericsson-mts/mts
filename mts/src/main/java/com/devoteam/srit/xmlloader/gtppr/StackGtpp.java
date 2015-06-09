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
import com.devoteam.srit.xmlloader.gtppr.data.GtpHeaderPrime;
import com.devoteam.srit.xmlloader.gtppr.data.GtpHeaderV1;
import com.devoteam.srit.xmlloader.gtppr.data.GtpHeaderV2;
import com.devoteam.srit.xmlloader.gtppr.data.GtppAttribute;
import com.devoteam.srit.xmlloader.gtppr.data.GtppMessage;
import com.devoteam.srit.xmlloader.gtppr.data.Tag;
import com.devoteam.srit.xmlloader.gtppr.data.Header;
import com.devoteam.srit.xmlloader.gtppr.data.TagTLV;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.Integer16Array;
import gp.utils.arrays.SupArray;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;

/**
 *
 * @author bbouvier
 */
public class StackGtpp extends Stack
{

    /** Constructor */
    public StackGtpp() throws Exception
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

    /** Creates a Channel specific to each Stack */
    @Override
    public Channel parseChannelFromXml(Element root, String protocol) throws Exception
    {
        String name       = root.attributeValue("name");
        String localHost  = root.attributeValue("localHost");
        String localPort  = root.attributeValue("localPort");
        String remoteHost = root.attributeValue("remoteHost");
        String remotePort = root.attributeValue("remotePort");

        if (existsChannel(name))
        {
            return getChannel(name);
        }
        else
        {
            if(null != localHost) localHost = InetAddress.getByName(localHost).getHostAddress();
            else                  localHost = "0.0.0.0";

            if(null != remoteHost) remoteHost = InetAddress.getByName(remoteHost).getHostAddress();

            return new ChannelGtpp(name, localHost, localPort, remoteHost, remotePort, protocol);
        }
    }

    @Override
    public Msg readFromStream(InputStream inputStream, Channel channel) throws Exception
    {
        GtppMessage msg = new GtppMessage();
        
        byte[] flag = new byte[1];
        //read the header
    	int nbCharRead= inputStream.read(flag, 0, 1);
    	if(nbCharRead == -1){
    		throw new Exception("End of stream detected");
    	}
    	else if (nbCharRead < 1) {
            throw new Exception("Not enough char read");
        }
        
        DefaultArray flagArray = new DefaultArray(flag);
        // int protocolType = flagArray.getBits(3, 1);
        int version = flagArray.getBits(0, 3);
        
        Header gtpHeader = null;
        if(version == 1)
        {
        	gtpHeader = new GtpHeaderV1(flagArray);
        }
        else if(version == 0)
        {
        	gtpHeader = new GtpHeaderPrime(flagArray); 
        }
        else if(version == 2)
        {
        	gtpHeader = new GtpHeaderV2(flagArray); 
        }
        
        // get the right dictionary in from of the version
        GtppDictionary dictionary = GtppDictionary.getDictionary(gtpHeader.getVersionName());

        gtpHeader.getSize(); 
        gtpHeader.parseArray(inputStream, dictionary); 
        msg.setHeader(gtpHeader);
        
        int msgLength = gtpHeader.getLength(); 
        // if(msgLength != 0)
        {
            byte[] buf = null;
            buf = new byte[msgLength];
            //read the staying message's data
            nbCharRead = inputStream.read(buf, 0, msgLength);
            if(nbCharRead == -1)
                throw new Exception("End of stream detected");
            else if(nbCharRead < msgLength)
                throw new Exception("Not enough char read");
			Array msgArrayTag = new DefaultArray(buf);
			msg.parseArray(msgArrayTag, dictionary);
        }
        
        return new MsgGtpp(msg);
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
