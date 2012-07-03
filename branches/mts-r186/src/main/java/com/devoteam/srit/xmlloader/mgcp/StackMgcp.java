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

package com.devoteam.srit.xmlloader.mgcp;

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
import org.dom4j.Element;


/**
 *
 * @author indiaye
 */
public  class StackMgcp extends Stack {
      /** Constructor */
    public StackMgcp() throws Exception
    {
        super();
        
        int port = getConfig().getInteger("listenpoint.LOCAL_PORT", 0);
        if (port > 0)
        {
        	Listenpoint listenpoint = new ListenpointMgcp(this);
                createListenpoint(listenpoint, StackFactory.PROTOCOL_MGCP);
        }

        
    }

    @Override
    public Config getConfig() throws Exception {
      return Config.getConfigByName("mgcp.properties");
    }

    @Override
    public XMLElementReplacer getElementReplacer() {
         return XMLElementTextMsgParser.instance();
    }

    @Override
    public Msg parseMsgFromXml(Boolean request, org.dom4j.Element root, Runner runner) throws Exception {
      String text = root.getText();
        MsgMgcp msgmgcp = new MsgMgcp(text);

        // OBSOLETE instanciates the listenpoint (compatibility with old grammar)
        String listenpointName = root.attributeValue("providerName");
        Listenpoint listenpoint = getListenpoint(listenpointName);
        if (listenpoint == null && listenpointName != null)
        {
            throw new ExecutionException("The listenpoint <name=" + listenpointName + "> does not exist");
        }
        msgmgcp.setListenpoint(listenpoint);

        if (request != null && request && !msgmgcp.isRequest())
        {
            throw new ExecutionException("You specify to send a request using a <sendRequestXXX ...> tag, but the message you will send is not really a request.");
        }
        if (request != null && !request && msgmgcp.isRequest())
        {
            throw new ExecutionException("You specify to send a response using a <sendResponseXXX ...> tag, but the message you will send is not really a response.");
        }

        return msgmgcp;
    }
    /** Creates a Listenpoint specific to each Stack */
    @Override
    public Listenpoint parseListenpointFromXml(Element root) throws Exception
    {
        Listenpoint listenpoint = new ListenpointMgcp(this, root);
        return listenpoint;
    }

    @Override
    public Msg readFromDatas(byte[] datas, int length) throws Exception
    {
    	String str = new String(datas);
    	str = str.substring(0, length);
    	MsgMgcp msgMgcp = new MsgMgcp(str);
    	return msgMgcp;
    }


}
