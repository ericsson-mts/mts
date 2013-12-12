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

package com.devoteam.srit.xmlloader.core.protocol;

import java.util.HashMap;
import java.util.Iterator;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.Config;

/**
 *
 * @author fhenry
 */
public class StackFactory
{

    // public final static String PREFIX_INCOMING = "&lt;&lt;in";
    public final static String PREFIX_INCOMING = "_in";
    // public final static String PREFIX_OUTGOING = "&gt;&gt;out";
    public final static String PREFIX_OUTGOING = "_out";
    // public final static String PREFIX_CAPTURING = "&lt;&lt;&gt;&gt;out";
    public final static String PREFIX_CAPTURING = "_cap";
    
    public final static String PROTOCOL_IP = "IP";
    
    public final static String PROTOCOL_DIAMETER = "DIAMETER";
    public static String PROTOCOL_DIAMETER_STACK_CLASS = "com.devoteam.srit.xmlloader.diameter.StackDiameter";

    public final static String PROTOCOL_SIP = "SIP";
    public static String PROTOCOL_SIP_STACK_CLASS = "com.devoteam.srit.xmlloader.sip.jain.StackSipJain";

    public final static String PROTOCOL_HTTP = "HTTP";
    public static String PROTOCOL_HTTP_STACK_CLASS = "com.devoteam.srit.xmlloader.http.StackHttp";

    public final static String PROTOCOL_RTP = "RTP";
    public static String PROTOCOL_RTP_STACK_CLASS = "com.devoteam.srit.xmlloader.rtp.StackRtp";

    public final static String PROTOCOL_RTPFLOW = "RTPFLOW";
    public static String PROTOCOL_RTPFLOW_STACK_CLASS = "com.devoteam.srit.xmlloader.rtp.flow.StackRtpFlow";

    public final static String PROTOCOL_TCP = "TCP";
    public static String PROTOCOL_TCP_STACK_CLASS = "com.devoteam.srit.xmlloader.tcp.StackTcp";

    public final static String PROTOCOL_UDP = "UDP";
    public static String PROTOCOL_UDP_STACK_CLASS = "com.devoteam.srit.xmlloader.udp.StackUdp";
    
    public final static String PROTOCOL_SCTP = "SCTP";
    public static String PROTOCOL_SCTP_STACK_CLASS = "com.devoteam.srit.xmlloader.sctp.StackSctp";

    public final static String PROTOCOL_RADIUS = "RADIUS";
    public static String PROTOCOL_RADIUS_STACK_CLASS = "com.devoteam.srit.xmlloader.radius.StackRadius";

    public final static String PROTOCOL_SMTP = "SMTP";
    public static String PROTOCOL_SMTP_STACK_CLASS = "com.devoteam.srit.xmlloader.smtp.StackSmtp";
    
    public final static String PROTOCOL_RTSP = "RTSP";
    public static String PROTOCOL_RTSP_STACK_CLASS = "com.devoteam.srit.xmlloader.rtsp.StackRtsp";
    
    public final static String PROTOCOL_IMAP = "IMAP";
    public static String PROTOCOL_IMAP_STACK_CLASS = "com.devoteam.srit.xmlloader.imap.StackImap";

    public final static String PROTOCOL_POP = "POP";
    public static String PROTOCOL_POP_STACK_CLASS = "com.devoteam.srit.xmlloader.pop.StackPop";

    public final static String PROTOCOL_SMPP = "SMPP";
    public static String PROTOCOL_SMPP_STACK_CLASS = "com.devoteam.srit.xmlloader.smpp.StackSmpp";

    public final static String PROTOCOL_UCP = "UCP";
    public static String PROTOCOL_UCP_STACK_CLASS = "com.devoteam.srit.xmlloader.ucp.StackUcp";

    public final static String PROTOCOL_SIGTRAN = "SIGTRAN";
    public static String PROTOCOL_SIGTRAN_STACK_CLASS = "com.devoteam.srit.xmlloader.sigtran.StackSigtran";
    
	public static final String PROTOCOL_TLS = "TLS";
    public static String PROTOCOL_TLS_STACK_CLASS = "com.devoteam.srit.xmlloader.tls.StackTls";

	public static final String PROTOCOL_H248 = "H248";
    public static String PROTOCOL_H248_STACK_CLASS = "com.devoteam.srit.xmlloader.h248.StackH248";

    public static final String PROTOCOL_PCP = "PCP";
    public static String PROTOCOL_PCP_STACK_CLASS = "com.devoteam.srit.xmlloader.pcp.StackPcp";

    public static final String PROTOCOL_MSRP = "MSRP";
    public static String PROTOCOL_MSRP_STACK_CLASS = "com.devoteam.srit.xmlloader.msrp.StackMsrp";

    public static final String PROTOCOL_GTP = "GTP";
    public static String PROTOCOL_GTPP_STACK_CLASS = "com.devoteam.srit.xmlloader.gtp.StackGtp";

    public static final String PROTOCOL_SNMP = "SNMP";
    public static String PROTOCOL_SNMP_STACK_CLASS = "com.devoteam.srit.xmlloader.snmp.StackSnmp";

    public static final String PROTOCOL_MGCP = "MGCP";
    public static String PROTOCOL_MGCP_STACK_CLASS = "com.devoteam.srit.xmlloader.mgcp.StackMgcp";

    public static final String PROTOCOL_STUN = "STUN";
    public static String PROTOCOL_STUN_STACK_CLASS = "com.devoteam.srit.xmlloader.stun.StackStun";

    public static final String PROTOCOL_H225CS = "H225CS";
    public static String PROTOCOL_H225CS_STACK_CLASS = "com.devoteam.srit.xmlloader.h323.h225cs.StackH225cs";
    
    public final static String PROTOCOL_ETHERNET = "ETHERNET";
    public static String PROTOCOL_ETHERNET_STACK_CLASS = "com.devoteam.srit.xmlloader.ethernet.StackEthernet";

    private static HashMap<String, Stack> listStack = new HashMap<String, Stack>();


    /** Creates or returns the instance of this stack */
    public synchronized static Stack getStack(String protocol) throws ExecutionException
    {
    	// remove the subprotocol
    	int iPos = protocol.indexOf('.');
    	if (iPos >= 0)
    	{
    		protocol = protocol.substring(0, iPos);
    	}
        Stack stack = listStack.get(protocol);

        if (null != stack)
        {
            return stack;
        }

        String stackToLoad = null;

        if (PROTOCOL_DIAMETER.equalsIgnoreCase(protocol))
        {
            stackToLoad = PROTOCOL_DIAMETER_STACK_CLASS;
        }
        else if (PROTOCOL_SIP.equalsIgnoreCase(protocol))
        {
        	stackToLoad = Config.getConfigByName("tester.properties").getString("protocol.STACK_CLASS_NAME_SIP", PROTOCOL_SIP_STACK_CLASS);
        }
        else if (PROTOCOL_HTTP.equalsIgnoreCase(protocol))
        {
            stackToLoad = PROTOCOL_HTTP_STACK_CLASS;
        }
        else if (PROTOCOL_RTP.equalsIgnoreCase(protocol))
        {
            stackToLoad = Config.getConfigByName("tester.properties").getString("protocol.STACK_CLASS_NAME_RTP", PROTOCOL_RTP_STACK_CLASS);
        }
        else if (PROTOCOL_TCP.equalsIgnoreCase(protocol))
        {
            stackToLoad = PROTOCOL_TCP_STACK_CLASS;
        }
        else if (PROTOCOL_SMTP.equalsIgnoreCase(protocol))
        {
            stackToLoad = PROTOCOL_SMTP_STACK_CLASS;
        }
        else if (PROTOCOL_UDP.equalsIgnoreCase(protocol))
        {
            stackToLoad = PROTOCOL_UDP_STACK_CLASS;
        }
        else if (PROTOCOL_SCTP.equalsIgnoreCase(protocol))
        {
            stackToLoad = PROTOCOL_SCTP_STACK_CLASS;
        }
        else if (PROTOCOL_RADIUS.equalsIgnoreCase(protocol))
        {
            stackToLoad = PROTOCOL_RADIUS_STACK_CLASS;
        }
        else if (PROTOCOL_RTSP.equalsIgnoreCase(protocol))
        {
            stackToLoad = PROTOCOL_RTSP_STACK_CLASS;
        }
        else if (PROTOCOL_IMAP.equalsIgnoreCase(protocol))
        {
            stackToLoad = PROTOCOL_IMAP_STACK_CLASS;
        }
        else if (PROTOCOL_POP.equalsIgnoreCase(protocol))
        {
            stackToLoad = PROTOCOL_POP_STACK_CLASS;
        }
        else if (PROTOCOL_SMPP.equalsIgnoreCase(protocol))
        {
            stackToLoad = PROTOCOL_SMPP_STACK_CLASS;
        }
        else if (PROTOCOL_UCP.equalsIgnoreCase(protocol))
        {
            stackToLoad = PROTOCOL_UCP_STACK_CLASS;
        }
        else if (PROTOCOL_SIGTRAN.equalsIgnoreCase(protocol))
        {
            stackToLoad = PROTOCOL_SIGTRAN_STACK_CLASS;
        }
        else if (PROTOCOL_RTPFLOW.equalsIgnoreCase(protocol))
        {
            stackToLoad = PROTOCOL_RTPFLOW_STACK_CLASS;
        }
        else if (PROTOCOL_TLS.equalsIgnoreCase(protocol))
        {
            stackToLoad = PROTOCOL_TLS_STACK_CLASS;
        }
        else if (PROTOCOL_H248.equalsIgnoreCase(protocol))
        {
            stackToLoad = PROTOCOL_H248_STACK_CLASS;
        }
        else if (PROTOCOL_PCP.equalsIgnoreCase(protocol))
        {
            stackToLoad = PROTOCOL_PCP_STACK_CLASS;
        }
        else if (PROTOCOL_MSRP.equalsIgnoreCase(protocol))
        {
            stackToLoad = PROTOCOL_MSRP_STACK_CLASS;
        }
        else if (PROTOCOL_GTP.equalsIgnoreCase(protocol))
        {
            stackToLoad = PROTOCOL_GTPP_STACK_CLASS;
        }
        else if (PROTOCOL_SNMP.equalsIgnoreCase(protocol))
        {
            stackToLoad = PROTOCOL_SNMP_STACK_CLASS;
        }
        else if (PROTOCOL_MGCP.equalsIgnoreCase(protocol))
        {
            stackToLoad = PROTOCOL_MGCP_STACK_CLASS;
        }
         else if (PROTOCOL_STUN.equalsIgnoreCase(protocol))
        {
            stackToLoad = PROTOCOL_STUN_STACK_CLASS;
        } 
        else if (PROTOCOL_H225CS.equalsIgnoreCase(protocol))
        {
            stackToLoad = PROTOCOL_H225CS_STACK_CLASS;
        }
        else if (PROTOCOL_ETHERNET.equalsIgnoreCase(protocol))
        {
            stackToLoad = PROTOCOL_ETHERNET_STACK_CLASS;
        }
        else
        {
            throw new ExecutionException("Unknown stack" + protocol);
        }
        
        try
        {
            Class aClass = ClassLoader.getSystemClassLoader().loadClass(stackToLoad);
            
            Object anObject = aClass.newInstance();
            stack = (Stack) anObject;
        }
        catch (Exception e)
        {
            throw new ExecutionException("Can't load class " + stackToLoad, e);
        }
        
        listStack.put(protocol, stack);

        return stack;
    }

    public static void getAllStacks()
    {    
    	Stack stack = null;
        try
        {
            stack = getStack(PROTOCOL_DIAMETER);
            stack = getStack(PROTOCOL_SIP);
            stack = getStack(PROTOCOL_HTTP);
            stack = getStack(PROTOCOL_RTP);
            stack = getStack(PROTOCOL_TCP);
            stack = getStack(PROTOCOL_SMTP);
            stack = getStack(PROTOCOL_UDP);
            stack = getStack(PROTOCOL_SCTP);
            stack = getStack(PROTOCOL_RADIUS);
            stack = getStack(PROTOCOL_RTSP);
            stack = getStack(PROTOCOL_IMAP);
            stack = getStack(PROTOCOL_POP);
            stack = getStack(PROTOCOL_SMPP);
            stack = getStack(PROTOCOL_UCP);
            stack = getStack(PROTOCOL_SIGTRAN);
            stack = getStack(PROTOCOL_TLS);
            stack = getStack(PROTOCOL_RTPFLOW);
            stack = getStack(PROTOCOL_TLS);
            stack = getStack(PROTOCOL_H248);
            // stack = getStack(PROTOCOL_PCP); // experimental : not yet integrated to MTS
            stack = getStack(PROTOCOL_MSRP);
            // stack = getStack(PROTOCOL_GTPP); // experimental : not yet integrated to MTS
            stack = getStack(PROTOCOL_SNMP);
            stack = getStack(PROTOCOL_MGCP);
            stack = getStack(PROTOCOL_STUN);            
            stack = getStack(PROTOCOL_H225CS);
            // stack = getStack(PROTOCOL_ETHERNET); // experimental : not yet integrated to MTS
        }
        catch (Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, e, "ERROR : loading the stack : ", stack);        	
        }

        // do not start SCTP stack if os is windows
        if (System.getProperty("os.name").toLowerCase().indexOf("windows") == -1)
        {
            try
            {
                getStack(PROTOCOL_SCTP);
            }
            catch (Throwable t)
            {
            }
        }
    }

    /** Creates or returns the instance of this stack */
    public static boolean isInstanciated(String protocol) throws ExecutionException
    {
        return listStack.get(protocol) != null;
    }

    /** Reset the instance of this stack */
    public static void reset(String protocol)
    {
        Stack stack = listStack.remove(protocol);
        if (null != stack)
        {
            stack.reset();
        }
    }

    /** reset the instance of all stacks */
    public synchronized static void reset()
    {
        Iterator<Stack> iter = listStack.values().iterator();
        while (iter.hasNext())
        {
            Stack stack = (Stack) iter.next();
            stack.reset();
        }

        listStack.clear();
    }
    
    /*
     * Remove eldest entry if instructed, else grow capacity if appropriate
     * for all the stacks
     */
    public static void cleanStackLists(){
        Iterator<Stack> iter = listStack.values().iterator();
        while (iter.hasNext())
        {
            Stack stack = (Stack) iter.next();
            stack.cleanStackLists();
        }
    }
}
