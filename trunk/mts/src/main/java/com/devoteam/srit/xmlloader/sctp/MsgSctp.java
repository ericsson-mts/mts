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

package com.devoteam.srit.xmlloader.sctp;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;

import java.net.InetAddress;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import dk.i1.sctp.AssociationId;
import dk.i1.sctp.SCTPData;


public class MsgSctp extends Msg{
	
	private enum DataType
	{
		text,
		binary
	}

	private SCTPData sctpData;

	/** Creates a new instance */
    public MsgSctp(Stack stack) throws Exception
    {
        super(stack);
    }
    
    /** Creates a new instance */
	public MsgSctp(Stack stack, byte[] bytes) throws Exception{
		this(stack);
		
		this.sctpData=new SCTPData(bytes);
	}

    /** Creates a new instance */
	public MsgSctp(SCTPData aSctpData) {
		this.sctpData=new SCTPData(aSctpData.sndrcvinfo,aSctpData.getData());
        this.type = "DATA";
	}

    /** Get the protocol of this message */
    @Override
	public String getProtocol() {
		return StackFactory.PROTOCOL_SCTP;  
	}

    /** Return true if the message is a request else return false*/
	@Override
	public boolean isRequest() {
		return true;
	}

    /** Get the type of this message */
	@Override
	public String getType() {
		return this.type;
	}
    /** Set the type of this message */
    public void setType(String type)
    {
        this.type = type;
    }    

    /** Get the result of this message */
	@Override
	public String getResult() throws Exception {
		return null;
	}

    /** Return the transport of the message*/
    @Override
    public String getTransport() {
    	return StackFactory.PROTOCOL_SCTP;
    }

	/// a utiliser
	public void setAidFromMsg(){
		((ChannelSctp) getChannel()).setAssociationId(sctpData.sndrcvinfo.sinfo_assoc_id);
	}

    /** Get the data (as binary) of this message */
    @Override
    public byte[] encode(){
        return this.sctpData.getData();
    }

    /** Returns a short description of the message. Used for logging as INFO level */
    /** This methods HAS TO be quick to execute for performance reason */
    @Override
	public String toShortString() throws Exception {
    	String ret = super.toShortString();
    	ret += "\n";
		try
		{
            ret += Utils.toStringBinary(sctpData.getData(), Math.min(sctpData.getData().length, 100));
		}
		catch(Exception e)
		{
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "An error occured while logging the SCTP message : ", ret);
        	e.printStackTrace();
		}
		return ret;
	}

    /** Get the XML representation of the message; for the genscript module. */
    @Override
    public String toXml() throws Exception {
		String xml = getTypeComplete();
		xml += "\n";
		if (sctpData.sndrcvinfo!=null){
			xml += "<header stream=\"" + sctpData.sndrcvinfo.sinfo_stream + "\", ";
			xml += "ssn=\"" + sctpData.sndrcvinfo.sinfo_ssn + "\", ";
			xml += "ppid=\"" + sctpData.sndrcvinfo.sinfo_ppid + "\", ";
			xml += "tsn=\"" + sctpData.sndrcvinfo.sinfo_tsn + "\", ";
			xml += "aid=\"" + sctpData.sndrcvinfo.sinfo_assoc_id + "\"/>\n";
			xml += "\n";
		}
		xml += Utils.byteTabToString(sctpData.getData());
        return xml;

    }

    /** 
     * Parse the message from XML element 
     */
    @Override
    public void parseFromXml(Boolean request, Element root, Runner runner) throws Exception
    {
		List<Element> elements = root.elements("data");
		List<byte[]> datas = new LinkedList<byte[]>();
		for(Element element:elements)
		{
			switch(DataType.valueOf(element.attributeValue("format")))
			{
			case text:
			{
				String text = element.getText();
                // change the \n caractère to \r\n caracteres because the dom librairy return only \n.
                // this could make some trouble when the length is calculated in the scenario
                text = text.replace("\r\n","\n");                    
                text = text.replace("\n","\r\n");                    					
				datas.add(text.getBytes("UTF8"));
				break;
			}
			case binary:
			{
				String text = element.getTextTrim();
				datas.add(Utils.parseBinaryString(text));
			}
			}
		}

		//
		// Compute total length
		//
		int length = 0;
		for(byte[] data:datas)
		{
			length += data.length;
		}

		byte[] data = new byte[length];

		int i=0;
		for(byte[] aData:datas)
		{
			for(int j=0; j<aData.length; j++)
			{
				data[i] = aData[j];
				i++;
			}
		}

		SCTPData sctpData = new SCTPData(data);		

		String stream = root.attributeValue("stream");
		if(stream !=null)
		{
			sctpData.sndrcvinfo.sinfo_stream = (short) Integer.parseInt(stream);
		}
		String ssn = root.attributeValue("ssn");
		if(ssn!=null)
		{
			sctpData.sndrcvinfo.sinfo_ssn = (short) Integer.parseInt(ssn);
		}
		String flags = root.attributeValue("flags");
		if(flags!=null)
		{
			sctpData.sndrcvinfo.sinfo_flags = (short) Integer.parseInt(flags);
		}
		String ppid = root.attributeValue("ppid");
		if(ppid!=null)
		{
			sctpData.sndrcvinfo.sinfo_ppid = (short) Integer.parseInt(ppid);
		}
		String context = root.attributeValue("context");
		if(context!=null)
		{
			sctpData.sndrcvinfo.sinfo_context = (short) Integer.parseInt(context);
		}
		String ttl = root.attributeValue("ttl");
		if(ttl!=null)
		{
			sctpData.sndrcvinfo.sinfo_timetolive = (short) Integer.parseInt(ttl);
		}
		String tsn = root.attributeValue("tsn");
		if(tsn!=null)
		{
			sctpData.sndrcvinfo.sinfo_tsn = (short) Integer.parseInt(tsn);
		}
		String cumtsn = root.attributeValue("cumtsn");
		if(cumtsn!=null)
		{
			sctpData.sndrcvinfo.sinfo_cumtsn = (short) Integer.parseInt(cumtsn);
		}
		String aid = root.attributeValue("aid");
		if(aid!=null)
		{
			sctpData.sndrcvinfo.sinfo_assoc_id = new AssociationId(Integer.parseInt(aid));
		}
				
		this.sctpData=new SCTPData(sctpData.sndrcvinfo,sctpData.getData());
        this.type = "DATA";
    }

    //------------------------------------------------------
    // method for the "setFromMessage" <parameter> operation
    //------------------------------------------------------

    /** 
     * Get a parameter from the message
     */
    @Override
    public Parameter getParameter(String path) throws Exception
	{
		Parameter var = super.getParameter(path);
		if (null != var)
		{
			return var;
		}

		var = new Parameter();
        path = path.trim();
        String[] params = Utils.splitPath(path);

        if(params[0].equalsIgnoreCase("header")) 
        {
        	if (this.sctpData != null && this.sctpData.sndrcvinfo != null)
        	{
	            if(params[1].equalsIgnoreCase("stream")) 
	            {
	            	var.add(Integer.toString(this.sctpData.sndrcvinfo.sinfo_stream));
	            }
	            else if(params[1].equalsIgnoreCase("ssn")) 
	            {
	            	var.add(Integer.toString(this.sctpData.sndrcvinfo.sinfo_ssn));
	            }
	            else if(params[1].equalsIgnoreCase("ppid")) 
	            {
	            	int ppid = Utils.convertLittleBigIndian(this.sctpData.sndrcvinfo.sinfo_ppid);
	            	var.add(Integer.toString(ppid));
	            }
	            else if(params[1].equalsIgnoreCase("tsn")) 
	            {
	            	int tsn = Utils.convertLittleBigIndian(this.sctpData.sndrcvinfo.sinfo_tsn);
	            	var.add(Long.toString(tsn));
	            }
	            else if(params[1].equalsIgnoreCase("aid")) 
	            {
	            	int aid = Utils.convertLittleBigIndian(this.sctpData.sndrcvinfo.sinfo_assoc_id.hashCode());
	            	var.add(Integer.toString(aid));
					setAidFromMsg();
	            }
	            else if(params[1].equalsIgnoreCase("sourceHost")) 
		        {
					SocketSctp socketSctp=((ChannelSctp) getChannel()).getSocketSctp();
					Collection<InetAddress> col = socketSctp.getSctpSocket().getPeerInetAddresses(sctpData.sndrcvinfo.sinfo_assoc_id);
					for(InetAddress ia : col)
					{	
						// support IPV6 address
						// if (ia instanceof Inet4Address)
						{
							var.add(ia.getHostAddress());
							break;
						}
					}
		        }
		        else if(params[1].equalsIgnoreCase("sourcePort")) 
		        {
					ChannelSctp connSctp =((ChannelSctp) getChannel());
					int port=connSctp.getSocketSctp().getSctpSocket().getPeerInetPort(sctpData.sndrcvinfo.sinfo_assoc_id);
					var.add(Integer.toString(port));
		        }
		        else 
		        {
		        	Parameter.throwBadPathKeywordException(path);
		        }
        	}
        }
        else if(params[0].equalsIgnoreCase("data")) 
        {
            if(params[1].equalsIgnoreCase("text")) 
            {
                var.add(new String(encode()));
            }
            else if(params[1].equalsIgnoreCase("binary")) 
            {
            	var.add(Array.toHexString(new DefaultArray(encode())));
            }
            else 
            {
            	Parameter.throwBadPathKeywordException(path);
            }
        }
        else if (params[0].equalsIgnoreCase("ua") && params[1].equalsIgnoreCase("ppid"))
        {
        	var.add("1111");
        }
        else 
        {
        	Parameter.throwBadPathKeywordException(path);
        }

		return var;
	}    

}
