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

import java.util.List;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.coding.binary.ElementAbstract;
import com.devoteam.srit.xmlloader.core.coding.binary.q931.MessageQ931;
import com.devoteam.srit.xmlloader.core.coding.tpkt.TPKTPacket;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.SupArray;

import org.dom4j.Element;

/**
 *
 * @author indiaye
 */
public class MsgH225cs extends Msg {

    MessageQ931 msgQ931;
    Asn1Message msgAsn1;

    /** Creates a new instance */
    public MsgH225cs(Stack stack) throws Exception
    {
        super(stack);
    }
    
    /** 
     * Return true if the message is a request else return false
     */
	@Override
    public boolean isRequest() throws Exception 
	{
    	return msgQ931.getHeader().isRequest();
    }

    /** 
     * Get the type of the message
     * Used for message filtering with "type" attribute and for statistic counters 
     */
	@Override
    public String getType() throws Exception 
    {
        return msgQ931.getHeader().getType();
    }

    /** 
     * Get the result of the message (null if request)
     * Used for message filtering with "result" attribute and for statistic counters 
     */
	@Override
    public String getResult() throws Exception 
    {
        return msgQ931.getHeader().getType();
    }
    

    //---------------------------------------------------------------------
    // methods for the XML display / parsing of the message
    //---------------------------------------------------------------------

    /** 
     * Convert the message to XML document 
     */
    @Override
    public byte[] encode() throws Exception
    {
       // get field and element for ASN1 and set value
    	SupArray arr = null;
       try
       {
    	   //List<ElementAbstract> elements_asn1 = msgQ931.getElementsFromTag("126");
	       arr = new SupArray();
	       TPKTPacket tpkt = new TPKTPacket(msgQ931.getLength() + 4);
	       arr.addLast(tpkt.getValue());
	       arr.addLast(msgQ931.getValue());
       }
       catch (Exception e)
       {
    	   // Nothing to do
       }

        return arr.getBytes();
    }
    
    /** 
     * decode the message from binary data 
     */
    public void decode(byte[] data) throws Exception
    {
    	Array array = new DefaultArray(data);
        this.msgQ931 = new MessageQ931(array, "../conf/sigtran/q931.xml");
    }


    /** Returns a short description of the message. Used for logging as INFO level */
    /** This methods HAS TO be quick to execute for performance reason */
    @Override
    public String toShortString() throws Exception {
    	String ret = super.toShortString();
        ret += msgQ931.toShortString();
        return ret; 
    }

    /** 
     * Convert the message to XML document 
     */
    @Override
    public String toXml() throws Exception {
        return msgQ931.toString();
    }

    /** 
     * Parse the message from XML element 
     */
    @Override
    public void parseFromXml(Boolean request, Element root, Runner runner) throws Exception
    {
    	super.parseFromXml(request,root,runner);

    	this.msgAsn1 = new Asn1Message();
    	this.msgAsn1.parseElement(root);

        Element ie = root.element("ISDN");
        this.msgQ931 = new MessageQ931(ie);
    }

    
    //------------------------------------------------------
    // method for the "setFromMessage" <parameter> operation
    //------------------------------------------------------

    /** 
     * Get a parameter from the message 
     */
    @Override
    public Parameter getParameter(String path) throws Exception {
        Parameter var = super.getParameter(path);
        if ((null != var) && (var.length() > 0)) {
            return var;
        }
        
        var = new Parameter();
        path = path.trim();
        String[] params = Utils.splitPath(path);
        
        if (params[0].equalsIgnoreCase("isdn")) {
        	this.msgQ931.getParameter(var, params, path);
        }
        else {
           	Parameter.throwBadPathKeywordException(path);
        }
        return var;
    }

}
