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
import com.devoteam.srit.xmlloader.core.utils.Config;
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

	public SCTPData getSctpData() {
		return sctpData;
	}

	/** Creates a new instance */
    public MsgSctp(Stack stack) throws Exception
    {
        super(stack);
    }
    
    /** Creates a new instance */
	public MsgSctp(Stack stack, SCTPData chunk) throws Exception{
		this(stack);
		
		this.setType("DATA");
		this.sctpData = chunk;
	}

    /** Get the protocol of this message */
    @Override
	public String getProtocol() {
		return StackFactory.PROTOCOL_SCTP;  
	}

    /** 
     * Return true if the message is a request else return false
     */
	@Override
	public boolean isRequest() 
	{
		return true;
	}

    /** 
     * Get the type of the message
     * Used for message filtering with "type" attribute and for statistic counters 
     */
	@Override
	public String getType() 
	{
		return this.type;
	}
    
	/** Set the type of this message */
    public void setType(String type)
    {
        this.type = type;
    }    

    /** 
     * Get the result of the message (null if request)
     * Used for message filtering with "result" attribute and for statistic counters 
     */
	@Override
	public String getResult() throws Exception 
	{
		return null;
	}

	/// a utiliser
	public void setAidFromMsg()
	{
		((ChannelSctp) getChannel()).setAssociationId(sctpData.sndrcvinfo.sinfo_assoc_id);
	}

	
    //-------------------------------------------------
    // methods for the encoding / decoding of the message
    //-------------------------------------------------

    /** 
     * encode the message to binary data 
     */
    @Override
    public byte[] encode() throws Exception
    {
        return this.sctpData.getData();
    }

    /** 
     * decode the message from binary data 
     */
    @Override
    public void decode(byte[] data) throws Exception
    {
    	this.sctpData = new SCTPData(data);
    }

    
    //---------------------------------------------------------------------
    // methods for the XML display / parsing of the message
    //---------------------------------------------------------------------

    /** Returns a short description of the message. Used for logging as INFO level */
    /** This methods HAS TO be quick to execute for performance reason */
    @Override
	public String toShortString() throws Exception 
    {
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

    /** 
     * Convert the message to XML document 
     */
    @Override
    public String toXml() throws Exception 
    {
		String xml = getTypeComplete();
		xml += "\n"; 
		if (sctpData.sndrcvinfo!=null)
		{
			xml += "<header ";
			int stream = sctpData.sndrcvinfo.sinfo_stream & 0xffff;
			xml += "stream=\"" + stream + "\", ";
			xml += "ssn=\"" + sctpData.sndrcvinfo.sinfo_ssn + "\", ";	
			long ppid = Utils.convertLittleBigIndian(this.sctpData.sndrcvinfo.sinfo_ppid) & 0xffffffffl;
			xml += "ppid=\"" + ppid + "\", ";
			xml += "flags=\"" + sctpData.sndrcvinfo.sinfo_flags + "\", ";
			long context = this.sctpData.sndrcvinfo.sinfo_context & 0xffffffffl;
			xml += "context=\"" + context + "\", ";
			long timetolive = this.sctpData.sndrcvinfo.sinfo_timetolive & 0xffffffffl;
			xml += "ttl=\"" + timetolive + "\", ";
			long tsn = this.sctpData.sndrcvinfo.sinfo_tsn & 0xffffffffl;
			xml += "tsn=\"" + tsn + "\", ";
			long cumtsn = this.sctpData.sndrcvinfo.sinfo_cumtsn & 0xffffffffl;
			xml += "cumtsn=\"" + cumtsn + "\", ";
			long aid = this.sctpData.sndrcvinfo.sinfo_assoc_id.hashCode() & 0xffffffffl;			
			xml += "aid=\"" + aid + "\"/>\n";
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
    	this.setType("DATA");
    	 
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

		this.sctpData=new SCTPData(data);

        Config config = StackFactory.getStack(StackFactory.PROTOCOL_SCTP).getConfig();
        
		String stream = root.attributeValue("stream");
		if (stream != null)
		{
			this.sctpData.sndrcvinfo.sinfo_stream = (short) Integer.parseInt(stream);
		}
		else
		{
			this.sctpData.sndrcvinfo.sinfo_stream = (short) config.getInteger("client.DEFAULT_STREAM", 1);

		}
		GlobalLogger.instance().getSessionLogger().debug(TextEvent.Topic.PROTOCOL, "stream =" + this.sctpData.sndrcvinfo.sinfo_stream);
		String ssn = root.attributeValue("ssn");
		if (ssn != null)
		{
			this.sctpData.sndrcvinfo.sinfo_ssn = (short) Integer.parseInt(ssn);
		}
		else
		{
			this.sctpData.sndrcvinfo.sinfo_ssn = (short) config.getInteger("client.DEFAULT_SSN", 0);
		}
		GlobalLogger.instance().getSessionLogger().debug(TextEvent.Topic.PROTOCOL, "ssn =" + this.sctpData.sndrcvinfo.sinfo_ssn);
		String ppidString = root.attributeValue("ppid");
		Integer ppid = null;
		if (ppidString != null)
		{			
			ppid = (int) Long.parseLong(ppidString);
		}
		else
		{
			ppid = config.getInteger("client.DEFAULT_PPID", 0);
		}
		this.sctpData.sndrcvinfo.sinfo_ppid = Utils.convertLittleBigIndian(ppid);
		GlobalLogger.instance().getSessionLogger().debug(TextEvent.Topic.PROTOCOL, "ppid =" + this.sctpData.sndrcvinfo.sinfo_ppid);
		String flags = root.attributeValue("flags");
		if (flags != null)
		{
			this.sctpData.sndrcvinfo.sinfo_flags = (short) Integer.parseInt(flags);
		}
		else
		{
			this.sctpData.sndrcvinfo.sinfo_flags = (short) config.getInteger("client.DEFAULT_FLAGS", 0);
		}
		GlobalLogger.instance().getSessionLogger().debug(TextEvent.Topic.PROTOCOL, "flags =" + this.sctpData.sndrcvinfo.sinfo_flags);
		String context = root.attributeValue("context");
		if (context != null)
		{
			this.sctpData.sndrcvinfo.sinfo_context = (int) Long.parseLong(context);
		}
		else
		{
			this.sctpData.sndrcvinfo.sinfo_context = config.getInteger("client.DEFAULT_CONTEXT", 0);
		}
		GlobalLogger.instance().getSessionLogger().debug(TextEvent.Topic.PROTOCOL, "context =" + this.sctpData.sndrcvinfo.sinfo_context);
		String ttl = root.attributeValue("ttl");
		if (ttl != null)
		{
			this.sctpData.sndrcvinfo.sinfo_timetolive = (int) Long.parseLong(ttl);
		}
		else
		{
			this.sctpData.sndrcvinfo.sinfo_timetolive = config.getInteger("client.DEFAULT_TTL", 0);
		}
		GlobalLogger.instance().getSessionLogger().debug(TextEvent.Topic.PROTOCOL, "ttl =" + this.sctpData.sndrcvinfo.sinfo_timetolive);
		String tsn = root.attributeValue("tsn");
		if (tsn != null)
		{
			this.sctpData.sndrcvinfo.sinfo_tsn = (int) Long.parseLong(tsn);
		}
		else
		{
			this.sctpData.sndrcvinfo.sinfo_tsn = config.getInteger("client.DEFAULT_TSN", 0);
		}
		GlobalLogger.instance().getSessionLogger().debug(TextEvent.Topic.PROTOCOL, "tsn =" + this.sctpData.sndrcvinfo.sinfo_tsn);
		String cumtsn = root.attributeValue("cumtsn");
		if (cumtsn != null)
		{
			this.sctpData.sndrcvinfo.sinfo_cumtsn = (int) Long.parseLong(cumtsn);
		}
		else
		{
			this.sctpData.sndrcvinfo.sinfo_cumtsn = config.getInteger("client.DEFAULT_CUMTSN", 0);
		}		
		GlobalLogger.instance().getSessionLogger().debug(TextEvent.Topic.PROTOCOL, "cumtsn =" + this.sctpData.sndrcvinfo.sinfo_cumtsn);
		String aid = root.attributeValue("aid");
		Long assocId = null; 
		if (aid != null)
		{
			assocId = Long.parseLong(aid);
		}
		else
		{
			assocId = (long) config.getInteger("client.DEFAULT_AID", 0);
			this.sctpData.sndrcvinfo.sinfo_assoc_id = new AssociationId(assocId);
		}
		GlobalLogger.instance().getSessionLogger().debug(TextEvent.Topic.PROTOCOL, "aid =" + assocId);
		this.sctpData.sndrcvinfo.sinfo_assoc_id = new AssociationId(assocId);
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
	            	int stream = this.sctpData.sndrcvinfo.sinfo_stream & 0xffff;
	            	var.add(Integer.toString(stream));
	            }
	            else if(params[1].equalsIgnoreCase("ssn")) 
	            {
	            	int ssn = this.sctpData.sndrcvinfo.sinfo_ssn & 0xffff;
	            	var.add(Integer.toString(ssn));
	            }
	            else if(params[1].equalsIgnoreCase("ppid")) 
	            {
	            	long ppid = Utils.convertLittleBigIndian(this.sctpData.sndrcvinfo.sinfo_ppid) & 0xffffffffl;
	            	var.add(Long.toString(ppid));
	            }
	            else if(params[1].equalsIgnoreCase("flags")) 
	            {
	            	short ppid = this.sctpData.sndrcvinfo.sinfo_flags;
	            	var.add(Short.toString(ppid));
	            }
	            else if(params[1].equalsIgnoreCase("context")) 
	            {
	            	int context = this.sctpData.sndrcvinfo.sinfo_context & 0xffffffff;
	            	var.add(Integer.toString(context));
	            }
	            else if(params[1].equalsIgnoreCase("ttl")) 
	            {
	            	int ttl = this.sctpData.sndrcvinfo.sinfo_timetolive & 0xffffffff;
	            	var.add(Integer.toString(ttl));
	            }
	            else if(params[1].equalsIgnoreCase("tsn")) 
	            {
	            	int tsn = this.sctpData.sndrcvinfo.sinfo_tsn & 0xffffffff;
	            	var.add(Integer.toString(tsn));
	            }
	            else if(params[1].equalsIgnoreCase("cumtsn")) 
	            {
	            	int cumtsn = this.sctpData.sndrcvinfo.sinfo_cumtsn & 0xffffffff;
	            	var.add(Integer.toString(cumtsn));
	            }
	            else if(params[1].equalsIgnoreCase("aid")) 
	            {
	            	AssociationId assocId = this.sctpData.sndrcvinfo.sinfo_assoc_id;
	            	int aid = assocId.hashCode()  & 0xffffffff;;
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
                var.add(new String(this.sctpData.getData()));
            }
            else if(params[1].equalsIgnoreCase("binary")) 
            {
            	var.add(Array.toHexString(new DefaultArray(this.sctpData.getData())));
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
