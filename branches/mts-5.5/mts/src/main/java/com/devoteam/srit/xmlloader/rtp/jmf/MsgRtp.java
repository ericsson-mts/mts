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

package com.devoteam.srit.xmlloader.rtp.jmf;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;

import java.util.Iterator;
import java.util.Vector;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.TransactionId;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.sun.media.rtp.util.RTPPacket;

/**
 *
 * @author gpasquiers
 */
public class MsgRtp extends Msg {
       
    private boolean control;     
        
    private Vector<RTPPacket> rtpPackets = null;            
    
    /** Creates a new instance of MsgRtp */
    public MsgRtp() {
    	super();
        rtpPackets = new Vector<RTPPacket>();
    }

    /** Add a new RTP Packet to the message */
    public void add(RTPPacket rtpPacket) {
        this.rtpPackets.add(rtpPacket);
    }

    /** Get a parameter from the message */
    public Parameter getParameter(String path)throws Exception
    {
        Parameter var = super.getParameter(path);
        if (var != null) {
            return var;
        }
        
        var = new Parameter();
        String[] params = Utils.splitPath(path);
        
        if(params.length>1 && params[0].equalsIgnoreCase("header"))
        {
            if(params[1].equalsIgnoreCase("ssrc"))
            {
                for (int i = 0; i < rtpPackets.size(); i++) 
                {
                    RTPPacket rtpPacket = rtpPackets.get(i);
                    var.add(Integer.toString(rtpPacket.ssrc));
                }
            } 
            else if(params[1].equalsIgnoreCase("payloadType")) 
            {
                for (int i = 0; i < rtpPackets.size(); i++) 
                {
                    RTPPacket rtpPacket = rtpPackets.get(i);                 
                    var.add(Integer.toString(rtpPacket.payloadType));
                }
            } 
            else if(params[1].equalsIgnoreCase("seqnum")) 
            {
                for (int i = 0; i < rtpPackets.size(); i++) 
                {
                    RTPPacket rtpPacket = rtpPackets.get(i);                 
                    var.add(Integer.toString(rtpPacket.seqnum));
                }
            } 
            else if(params[1].equalsIgnoreCase("timestamp")) 
            {
                for (int i = 0; i < rtpPackets.size(); i++) 
                {
                    RTPPacket rtpPacket = rtpPackets.get(i);                 
                    var.add(Long.toString(rtpPacket.timestamp));
                }
            }
            else
            {
            	Parameter.throwBadPathKeywordException(path);
            }
        }        
        else if(params.length>1 && params[0].equalsIgnoreCase("payload"))
        {
            if(params[1].equalsIgnoreCase("text"))
            {
                for (int i = 0; i < rtpPackets.size(); i++) 
                {
                    RTPPacket rtpPacket = rtpPackets.get(i);
                    var.add(new String(rtpPacket.data, rtpPacket.payloadoffset, rtpPacket.payloadlength));
                }
            } 
            else if(params[1].equalsIgnoreCase("binary")) 
            {
                for (int i = 0; i < rtpPackets.size(); i++) 
                {
                    RTPPacket rtpPacket = rtpPackets.get(i);
                	var.add(Array.toHexString(new DefaultArray(rtpPacket.data, rtpPacket.payloadoffset, rtpPacket.payloadlength)));
                }
            }
            else
            {
            	Parameter.throwBadPathKeywordException(path);
            }
        }
        else
        {
        	Parameter.throwBadPathKeywordException(path);
        }
                
        return var;
    }            

    /** Get the transaction Identifier of this message 
     * Transaction has no sense in RTP because there are no response (stream protocol) */
    public TransactionId getTransactionId(){
        return null;
    }
    
    /** Get the protocol of this message */
    public String getProtocol(){
        return StackFactory.PROTOCOL_RTP;
    }
    
    /** Return true if the message is a request else return false*/
    public boolean isRequest(){
        return true;
    }
    
    /** Get the command code of this message */
    public String getType(){        
        return new Integer(rtpPackets.get(0).payloadType).toString();
    }
    
    /** Get the result of this answer (null if request) */
    public String getResult(){        
        return null;
    }

    /** Return the length of the message*/
    @Override
    public int getLength()
    {
    	// only the first element in the list is used
    	RTPPacket rtpPacket = ((RTPPacket) rtpPackets.get(0));
    	return rtpPacket.payloadoffset + rtpPacket.payloadlength; 
    }
    
    /** Return the transport of the message*/
    public String getTransport() {
    	return StackFactory.PROTOCOL_UDP;
    }
    
    /**
     * prints the packet header 
     */
    private static String headerToString(RTPPacket rtpPacket)
    {
        String ret = "";
        ret += "<header payloadType=\"" + rtpPacket.payloadType + "\", ";
        ret += "ssrc=\"" + rtpPacket.ssrc + "\", ";
        ret += "seqnum=\"" + rtpPacket.seqnum + "\", ";
        ret += "timestamp=\"" + rtpPacket.timestamp + "\"/>";
        return ret;
    }    	
    	
    /**
     * prints as String an avp and it's sub-avps (recursive)
     */
    private static String packetToString(RTPPacket rtpPacket) throws Exception
    {
        String ret = "<packet>\n";
        ret += Utils.indent(1) + headerToString(rtpPacket) + "\n";
        ret += Utils.indent(1) + "<payload format=\"binary\">\n";
        ret += Utils.toBinaryString(rtpPacket.data, rtpPacket.payloadoffset, rtpPacket.payloadlength, 0) + "\n";
        ret += Utils.indent(1) + "</payload>\n"; 
        ret += Utils.indent(1) + "<payload format=\"text\">\n";
        String text = new String(rtpPacket.data, rtpPacket.payloadoffset, rtpPacket.payloadlength);
        ret += text + "\n";
        ret += Utils.indent(1) + "</payload>";
        ret += "\n</packet>\n";        
        return ret;        
    }

    public Iterator getRtpPackets() {
        return rtpPackets.iterator();
    }

    public boolean isControl() {
        return control;
    }

    public void setControl(boolean control) {
        this.control = control;
    }

    /** Get the data (as binary) of this message */
    @Override
    public byte[] getBytesData(){
    	// only the first element in the list is used
    	RTPPacket rtpPacket = ((RTPPacket) rtpPackets.get(0));
        return rtpPacket.data;
    }

    /** Returns a short description of the message. Used for logging as INFO level */
    /** This methods HAS TO be quick to execute for performance reason */
    @Override
    public String toShortString() throws Exception {
    	String ret = super.toShortString();
        ret += headerToString(rtpPackets.get(0));
        return ret;
    }

    /** Get the XML representation of the message; for the genscript module. */
    @Override
    public String toXml() throws Exception {
		String xml = "";
        Iterator<RTPPacket> iter = rtpPackets.iterator(); 
        while (iter.hasNext()) {
            RTPPacket rtpPacket = iter.next();
            try
            {
            	xml += packetToString(rtpPacket);
            }
            catch (Exception e)
            {
                GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "An error occured while logging the diameter response : ", rtpPacket);
            }
        }
        return xml;
    }

}
