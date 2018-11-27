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

package com.devoteam.srit.xmlloader.http2;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;

import org.dom4j.Element;


import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;



public class ListenpointHttp2 extends Listenpoint
{   
  

    /** Creates a new instance of Listenpoint */
    public ListenpointHttp2(Stack stack) throws Exception 
    {
    	super(stack);
    }

    
    //---------------------------------------------------------------------
    // methods for the transport
    //---------------------------------------------------------------------

    /** Create a listenpoint to each Stack */
    public boolean sendMessage(Msg msg, String remoteHost, int remotePort, String transport) throws Exception {
    	System.out.println("ListenpointHttp2.sendMessage()");
    	return false;
	}

    
    //---------------------------------------------------------------------
    // methods for the XML display / parsing 
    //---------------------------------------------------------------------

    /** 
     * Returns the string description of the message. Used for logging as DEBUG level 
     */
    public String toString()
    {
    	System.out.println("ListenpointHttp2.toString()");
    	return "ListenpointHttp2.toString";
    }

    /** 
     * Convert the channel to XML document 
     */
    public String toXml()
    {
    	System.out.println("ListenpointHttp2.toXml()");
    	return "ListenpointHttp2.toXml";
    }
    
    /** 
     * Parse the listenpoint from XML element 
     */
    public void parseFromXml(Element root, Runner runner) throws Exception
    {
    	System.out.println("ListenpointHttp2.parseFromXml()");
    }
    
    public void shutdown() {
    	System.out.println("ListenpointHttp2.shutdown()");
    }
    
    //------------------------------------------------------
    // method for the "setFromMessage" <parameter> operation
    //------------------------------------------------------

    /** 
     * Get a parameter from the message 
     */

    public Parameter getParameter(String path) throws Exception
    {
    	System.out.println("ListenpointHttp2.getParameter()");
        return null;
    }

    
    /** clone method */
    public void clone(Listenpoint listenpoint)
    {
    	System.out.println("ListenpointHttp2.clone()");
    }

    /** equals method */
    public boolean equals(Listenpoint listenpoint)
    {
    	System.out.println("ListenpointHttp2.equals()");
    	return true;
    }

}
