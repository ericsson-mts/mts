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
    public final static String PROTOCOL_SIP = "SIP";
    public final static String PROTOCOL_HTTP = "HTTP";
    public final static String PROTOCOL_RTP = "RTP";
    public final static String PROTOCOL_RTPFLOW = "RTPFLOW";
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
    public static final String PROTOCOL_PCP = "PCP";		// experimental : not totally implemented
    public static final String PROTOCOL_MSRP = "MSRP";
    public static final String PROTOCOL_GTP = "GTP";
    public static final String PROTOCOL_SNMP = "SNMP";
    public static final String PROTOCOL_MGCP = "MGCP";
    public static final String PROTOCOL_STUN = "STUN";
    public static final String PROTOCOL_H225CS = "H225CS";
    public final static String PROTOCOL_ETHERNET = "ETHERNET";

    private static HashMap<String, Stack> listStack = new HashMap<String, Stack>();


    /** Creates or returns the instance of this stack */
    public synchronized static Stack getStack(String protocol) throws Exception
    {
    	// remove the subprotocol
    	int iPos = protocol.indexOf('.');
    	if (iPos >= 0)
    	{
    		protocol = protocol.substring(0, iPos);
    	}
        Stack stack = listStack.get(protocol);

        if (stack == null)
        {
	    	String propertyName = "protocol.STACK_CLASS_NAME_" + protocol.toUpperCase(); 
	    	String stackToLoad = Config.getConfigByName("tester.properties").getString(propertyName);
	    	if (stackToLoad == null)
	    	{
	    		stackToLoad = StackFactory.getClassFromProtocol(protocol, "Stack");
	    	}
	        Class<?> aClass = ClassLoader.getSystemClassLoader().loadClass(stackToLoad);
	        
	        Object anObject = aClass.newInstance();
	        stack = (Stack) anObject;
	        
	        listStack.put(protocol, stack);
        }
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
            stack = getStack(PROTOCOL_PCP);
            stack = getStack(PROTOCOL_MSRP);
            stack = getStack(PROTOCOL_SNMP);
            stack = getStack(PROTOCOL_MGCP);
            stack = getStack(PROTOCOL_STUN);            
            stack = getStack(PROTOCOL_H225CS);
            stack = getStack(PROTOCOL_ETHERNET);
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
    
    /** Get the class type (Stack, Msg, Listenpoint, Channel, Probe) object from protocol acronym */
    public static String getClassFromProtocol(String protocol, String type) throws Exception
    {
    	String packageName = "com.devoteam.srit.xmlloader.";
    	String acronymeLower = protocol.toLowerCase();
    	String acronymeFirstUpper = acronymeLower.substring(0, 1).toUpperCase() + acronymeLower.substring(1); 
    	String classname = packageName + acronymeLower + "." + type + acronymeFirstUpper;
    	return classname;
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
