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

package com.devoteam.srit.xmlloader.snmp;

import java.io.IOException;
import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import org.snmp4j.Snmp;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;

/**
 *
 * @author bbouvier
 */
public class ListenpointSnmp extends Listenpoint
{

    Snmp snmp = null;
    ProcessMessage processMessage = null;

    Stack stack;

    /** Creates a new instance of Listenpoint */
    public ListenpointSnmp(Stack stack) throws Exception
    {
        super(stack);
        this.stack = stack;
    }

    /** Creates a Listenpoint specific from XML tree*/
    public ListenpointSnmp(Stack stack, Element root) throws Exception
    {
        super(stack, root);
        this.stack = stack;
    }

    @Override
    public boolean create(String protocol) throws Exception
    {
        processMessage = new ProcessMessage(this);
        snmp = new Snmp(new DefaultUdpTransportMapping(new UdpAddress(getHost() + "/" + getPort())));
        snmp.addCommandResponder(processMessage);

        // TEST CODE : initialize for receiving SNMPV3
        {
            String authPassword = stack.getConfig().getString("protocol.authPassword");
            String encryptPassword = stack.getConfig().getString("protocol.encryptPassword");

            snmp.getMessageDispatcher().removeMessageProcessingModel(new MPv3());
            snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3(MPv3.createLocalEngineID(new OctetString(this.getUID()))));

            USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(this.getUID().getBytes()), 0);
            SecurityModels.getInstance().addSecurityModel(usm);

            // need to get a list of users and their params from config
            snmp.getUSM().addUser(new OctetString("MD5DES"),
                    new UsmUser(new OctetString("MD5DES"),
                                AuthMD5.ID, new OctetString("protocol.authPassword"),
                                PrivDES.ID, new OctetString("protocol.encryptPassword")));
        }

        snmp.listen();

        return true;
    }

    /** Send a Msg to Channel */
    @Override
    public synchronized boolean sendMessage(Msg msg, String remoteHost, int remotePort, String transport) throws Exception
    {
        ((MsgSnmp) msg).getTarget().setAddress(new UdpAddress(remoteHost + "/" + remotePort));

        //asynchronous way, response processing is delayed to processResponse
        snmp.send(((MsgSnmp) msg).getPdu(), ((MsgSnmp) msg).getTarget(), null, processMessage);
        return true;
    }

    /** Remove a listenpoint */
    @Override
    public boolean remove()
    {
        try
        {
            snmp.close();
            snmp = null;
        }
        catch (IOException ex)
        {
        }
        return true;
    }
}
