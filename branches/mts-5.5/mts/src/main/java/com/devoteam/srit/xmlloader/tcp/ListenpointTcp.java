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

public class ListenpointTcp extends Listenpoint {

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
            StatPool.beginStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_UDP, protocol);
        }
        else {
            StatPool.beginStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_UDP, protocol);
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
            StatPool.endStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.NIO_KEY, StackFactory.PROTOCOL_UDP, getProtocol(), startTimestamp);
        }
        else {
            StatPool.endStatisticProtocol(StatPool.LISTENPOINT_KEY, StatPool.BIO_KEY, StackFactory.PROTOCOL_UDP, getProtocol(), startTimestamp);
        }
        return listenpoint.remove();
    }
}
