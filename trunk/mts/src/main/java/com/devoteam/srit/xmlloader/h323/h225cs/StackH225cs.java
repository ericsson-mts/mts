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
public class StackH225cs extends Stack {

    private Dictionary dictionary;

    public StackH225cs() throws Exception {
        super();
        try {
            XMLDoc xml = new XMLDoc();
            xml.setXMLFile(new URI("../conf/sigtran/q931.xml"));
            xml.parse();
            Element root = xml.getDocument().getRootElement();
            dictionary = new Dictionary(root, "Q931");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        int port = getConfig().getInteger("listenpoint.LOCAL_PORT", 0);
        if (port > 0) {
            Listenpoint listenpoint = new ListenpointH225cs(this);
            createListenpoint(listenpoint, StackFactory.PROTOCOL_H225CS);
        }
    }

    @Override
    public Config getConfig() throws Exception {
        return Config.getConfigByName("h225cs.properties");
    }

    @Override
    public XMLElementReplacer getElementReplacer() {
        return XMLElementTextMsgParser.instance();
    }

    @Override
    public Msg parseMsgFromXml(Boolean request, Element root, Runner runner) throws Exception {

        MsgH225cs msgh225cs = new MsgH225cs(root);

        // OBSOLETE instanciates the listenpoint (compatibility with old grammar)
        String listenpointName = root.attributeValue("providerName");
        Listenpoint listenpoint = getListenpoint(listenpointName);
        if (listenpoint == null && listenpointName != null) {
            throw new ExecutionException("The listenpoint <name=" + listenpointName + "> does not exist");
        }
        msgh225cs.setListenpoint(listenpoint);

        if (request != null && request && !msgh225cs.isRequest()) {
            throw new ExecutionException("You specify to send a request using a <sendRequestXXX ...> tag, but the message you will send is not really a request.");
        }
        if (request != null && !request && msgh225cs.isRequest()) {
            throw new ExecutionException("You specify to send a response using a <sendResponseXXX ...> tag, but the message you will send is not really a response.");
        }

        return msgh225cs;
    }

    @Override
    public Listenpoint parseListenpointFromXml(Element root) throws Exception {
        return new ListenpointH225cs(this, root);
    }

    @Override
    public Msg readFromDatas(byte[] datas, int length) throws Exception {
        return new MsgH225cs(new DefaultArray(datas, 0, length));
    }

    @Override
    public Channel parseChannelFromXml(Element root, String protocol)
            throws Exception {
        String name = root.attributeValue("name");
        String localHost = root.attributeValue("localHost");
        String localPort = root.attributeValue("localPort");
        String remoteHost = root.attributeValue("remoteHost");
        String remotePort = root.attributeValue("remotePort");

        if (existsChannel(name)) {
            return getChannel(name);
        }
        else {
            if (null != localHost) {
                localHost = InetAddress.getByName(localHost).getHostAddress();
            }
            else {
                localHost = "0.0.0.0";
            }

            if (null != remoteHost) {
                remoteHost = InetAddress.getByName(remoteHost).getHostAddress();
            }

            return new ChannelH225cs(name, localHost, localPort, remoteHost, remotePort, protocol);
        }
    }

    @Override
    public Msg readFromStream(InputStream inputStream, Channel channel)
            throws Exception {
        TPKTPacket tpkt = new TPKTPacket(inputStream);
        int length = tpkt.getPacketLength();
        byte[] tabMsg = new byte[length - 4];
        int done = Utils.readFromSocketStream(inputStream, tabMsg);
        Array data = new DefaultArray(tabMsg);
        return new MsgH225cs(data);
    }

    public Dictionary getDictionary() {
        return dictionary;
    }
}
