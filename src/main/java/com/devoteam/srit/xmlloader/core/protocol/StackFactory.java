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
 *
 */

package com.devoteam.srit.xmlloader.core.protocol;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;

import com.devoteam.srit.xmlloader.core.coding.binary.Dictionary;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.system.OSValidator;

/**
 * @author fhenry
 */
public class StackFactory {

    // separator for sub information
    public final static String SEP_SUB_INFORMATION = ".";

    // separator for dictionary resolvation
    public final static String SEP_DICO_RESOLVATION = ":";

    // public final static String PREFIX_INCOMING = "&lt;&lt;in";
    public final static String PREFIX_INCOMING = "_in";
    // public final static String PREFIX_OUTGOING = "&gt;&gt;out";
    public final static String PREFIX_OUTGOING = "_out";
    // public final static String PREFIX_CAPTURING = "&lt;&lt;&gt;&gt;out";
    public final static String PREFIX_CAPTURING = "_cap";

    public final static String ROOT_PACKAGE = "com.devoteam.srit.xmlloader";

    public final static String PROTOCOL_DIAMETER = "DIAMETER";
    public final static String PROTOCOL_SIP = "SIP";
    public final static String PROTOCOL_HTTP = "HTTP";
    public final static String PROTOCOL_RTP = "RTP";
    public final static String PROTOCOL_RTPFLOW = "RTPFLOW";
    public final static String STACK_CLASS_RTPFLOW = "com.devoteam.srit.xmlloader.rtp.flow.StackRtpFlow";
    public final static String PROTOCOL_TCP = "TCP";
    public final static String PROTOCOL_UDP = "UDP";
    public final static String PROTOCOL_SCTP = "SCTP";
    public final static String PROTOCOL_RADIUS = "RADIUS";
    public final static String PROTOCOL_SMTP = "SMTP";
    public final static String PROTOCOL_RTSP = "RTSP";
    public final static String PROTOCOL_IMAP = "IMAP";
    public final static String PROTOCOL_POP = "POP";
    public final static String PROTOCOL_SMPP = "SMPP";
    public final static String PROTOCOL_UCP = "UCP";
    public final static String PROTOCOL_SIGTRAN = "SIGTRAN";
    public static final String PROTOCOL_TLS = "TLS";
    public static final String PROTOCOL_H248 = "H248";
    public static final String PROTOCOL_PCP = "PCP";        // experimental : not totally implemented
    public static final String PROTOCOL_MSRP = "MSRP";
    public static final String PROTOCOL_GTP = "GTP";
    public static final String PROTOCOL_SNMP = "SNMP";
    public static final String PROTOCOL_MGCP = "MGCP";
    public static final String PROTOCOL_STUN = "STUN";
    public static final String PROTOCOL_H225CS = "H225CS";
    public final static String STACK_CLASS_H225CS = "com.devoteam.srit.xmlloader.h323.h225cs.StackH225cs";
    public final static String PROTOCOL_ETHERNET = "ETHERNET";
    public final static String PROTOCOL_HTTP2 = "HTTP2";
    public final static String PROTOCOL_S1AP = "S1AP";
    public final static String PROTOCOL_NGAP = "NGAP";

    private static HashMap<String, Stack> listStack = new HashMap<String, Stack>();


    /**
     * Creates or returns the instance of this stack
     */
    public synchronized static Stack getStack(String protocol) throws Exception {
        // remove the subprotocol if any
        int iPos = protocol.indexOf('.');
        if (iPos >= 0) {
            protocol = protocol.substring(0, iPos);
        }
        Stack stack = listStack.get(protocol);


        if (stack == null) {
            String propertyName = "protocol.STACK_CLASS_NAME_" + protocol.toUpperCase();
            String stackClassname = Config.getConfigByName("tester.properties").getString(propertyName);
            if (stackClassname == null || stackClassname.length() == 0) {
                stackClassname = StackFactory.getClassFromProtocol(protocol, "Stack");
            }
            if (PROTOCOL_RTPFLOW.equalsIgnoreCase(protocol)) {
                stackClassname = STACK_CLASS_RTPFLOW;
            }
            if (PROTOCOL_H225CS.equalsIgnoreCase(protocol)) {
                stackClassname = STACK_CLASS_H225CS;
            }
            Class<?> aClass = ClassLoader.getSystemClassLoader().loadClass(stackClassname);

            Object anObject = aClass.newInstance();
            stack = (Stack) anObject;

            //the stack is registered BEFORE its initialization
            listStack.put(protocol, stack);

            try {
                stack.initialize();
            } catch (Exception exception) {
                //if the initialization fails, the stack MUST be unregistered
                listStack.remove(protocol);
                throw exception;
            }

        }
        return stack;
    }

    public static void getAllStacks() throws Exception {
        // use the class fields starting with "PROTOCOL_"
        Field[] fields = StackFactory.class.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            f.setAccessible(true);
            String protocolVarname = f.getName();
            if (protocolVarname.startsWith("PROTOCOL_")) {
                if (!(protocolVarname.equals("PROTOCOL_" + PROTOCOL_SCTP) && OSValidator.isWindows())) {
                    String protocol = (String) f.get(StackFactory.class);
                    getStack(protocol);
                }
            }

        }
    }

    /**
     * Get the class type (Stack, Msg, Listenpoint, Channel, Probe) object from protocol acronym
     */
    public static String getClassFromProtocol(String protocol, String type) throws Exception {
        String acronymeLower = protocol.toLowerCase();
        String acronymeFirstUpper = acronymeLower.substring(0, 1).toUpperCase() + acronymeLower.substring(1);
        String classname = ROOT_PACKAGE + "." + acronymeLower + "." + type + acronymeFirstUpper;
        return classname;
    }

    /**
     * reset the instance of all stacks
     */
    public synchronized static void reset() {
        Iterator<Stack> iter = listStack.values().iterator();
        while (iter.hasNext()) {
            Stack stack = (Stack) iter.next();
            stack.reset();
        }

        listStack.clear();
        Dictionary.reset();
    }

    /*
     * Remove eldest entry if instructed, else grow capacity if appropriate
     * for all the stacks
     */
    public static void cleanStackLists() {
        Iterator<Stack> iter = listStack.values().iterator();
        while (iter.hasNext()) {
            Stack stack = (Stack) iter.next();
            stack.cleanStackLists();
        }
    }
}
