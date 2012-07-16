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

package com.devoteam.srit.xmlloader.stun;

import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.core.utils.XMLElementTextMsgParser;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Properties;
import org.dom4j.Element;

/**
 *
 * @author indiaye
 */
public class StackStun extends Stack {

    public static Properties prop;

    public StackStun() throws Exception {
        super();
        int port = getConfig().getInteger("listenpoint.LOCAL_PORT", 0);
        if (port > 0) {
            Listenpoint listenpoint = new ListenpointStun(this);
            createListenpoint(listenpoint, StackFactory.PROTOCOL_STUN);
        }
        prop = new Properties();
        InputStream in = SingletonFSInterface.instance().getInputStream(new URI("../conf/stun/typeStun.properties"));
        prop.load(in);



    }

    @Override
    public Config getConfig() throws Exception {
        return Config.getConfigByName("stun.properties");
    }

    @Override
    public XMLElementReplacer getElementReplacer() {
        return XMLElementTextMsgParser.instance();
    }

    @Override
    public Msg parseMsgFromXml(Boolean request, Element root, Runner runner) throws Exception {

        MsgStun msgstun = new MsgStun(root);

        // OBSOLETE instanciates the listenpoint (compatibility with old grammar)
        String listenpointName = root.attributeValue("providerName");
        Listenpoint listenpoint = getListenpoint(listenpointName);
        if (listenpoint == null && listenpointName != null) {
            throw new ExecutionException("The listenpoint <name=" + listenpointName + "> does not exist");
        }
        msgstun.setListenpoint(listenpoint);

        if (request != null && request && !msgstun.isRequest()) {
            throw new ExecutionException("You specify to send a request using a <sendRequestXXX ...> tag, but the message you will send is not really a request.");
        }
        if (request != null && !request && msgstun.isRequest()) {
            throw new ExecutionException("You specify to send a response using a <sendResponseXXX ...> tag, but the message you will send is not really a response.");
        }

        return msgstun;
    }
    @Override
     public Msg readFromDatas(byte[] datas, int length) throws Exception
    {
    	return new MsgStun(new DefaultArray(datas,0,length));
    }
}
