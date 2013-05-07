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

package com.devoteam.srit.xmlloader.tcp;

import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.tcp.bio.ListenpointTcpBIO;
import com.devoteam.srit.xmlloader.tcp.nio.ListenpointTcpNIO;

public class ListenpointTcp extends Listenpoint 
{

    private boolean nio = Config.getConfigByName("tcp.properties").getBoolean("USE_NIO", false);
    private Listenpoint listenpoint;
    private long startTimestamp = 0;

    /** Creates a new instance of Listenpoint */
    public ListenpointTcp(Stack stack) throws Exception {
        super(stack);
        if (nio) {
            listenpoint = new ListenpointTcpNIO(stack);
        }
        else {
            listenpoint = new ListenpointTcpBIO(stack);
        }
    }

    /** Creates a Listenpoint specific from XML tree*/
    public ListenpointTcp(Stack stack, Element root) throws Exception {
        super(stack, root);
        if (nio) {
            listenpoint = new ListenpointTcpNIO(stack, root);
        }
        else {
            listenpoint = new ListenpointTcpBIO(stack, root);
        }
    }

    /** Creates a new instance of Listenpoint */
    public ListenpointTcp(Stack stack, String name, String host, int port) throws Exception {
        super(stack, name, host, port);
        if (nio) {
            listenpoint = new ListenpointTcpNIO(stack, name, host, port);
        }
        else {
            listenpoint = new ListenpointTcpBIO(stack, name, host, port);
        }
    }

    /** Create a listenpoint to each Stack */
    @Override
    public boolean create(String protocol) throws Exception {
        if (nio) {
            StatPool.beginStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_TCP, protocol);
        }
        else {
            StatPool.beginStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_TCP, protocol);
        }
        this.startTimestamp = System.currentTimeMillis();
        return listenpoint.create(protocol);
    }

    @Override
    public synchronized Channel prepareChannel(Msg msg, String remoteHost, int remotePort, String transport) throws Exception {
        return listenpoint.prepareChannel(msg, remoteHost, remotePort, transport);
    }

    @Override
    public synchronized boolean sendMessage(Msg msg, String remoteHost, int remotePort, String transport) throws Exception {
        return prepareChannel(msg, remoteHost, remotePort, transport).sendMessage(msg);
    }

    @Override
    public String getProtocol() {
        return listenpoint.getProtocol();
    }

    public boolean remove() {
        if (nio) {
            StatPool.endStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_TCP, getProtocol(), startTimestamp);
        }
        else {
            StatPool.endStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_TCP, getProtocol(), startTimestamp);
        }
        return listenpoint.remove();
    }
}
