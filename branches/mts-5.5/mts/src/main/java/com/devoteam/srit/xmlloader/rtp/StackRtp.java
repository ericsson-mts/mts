package com.devoteam.srit.xmlloader.rtp;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.ReadOnlyDefaultArray;
import gp.utils.arrays.SupArray;

import java.util.List;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.core.utils.XMLElementTextMsgParser;
import java.util.ArrayList;

public class StackRtp extends Stack
{ 
	
    public boolean ignoreReceivedMessages;
    
    public StackRtp() throws Exception
    {
    	super();
    	
    	ignoreReceivedMessages = getConfig().getBoolean("IGNORE_RECEIVED_MESSAGES", false);
    }

	/** Creates a Listenpoint specific to each Stack */
    @Override
	public synchronized Listenpoint parseListenpointFromXml(Element root) throws Exception 
	{
        Listenpoint listenpoint = new ListenpointRtp(this, root);
        return listenpoint;        
	}

	/** Creates a specific Msg */
    @Override
    public Msg parseMsgFromXml(Boolean request, Element root, Runner runner) throws Exception
    {
        MsgRtp msg = null;

        if(root.element("packet") != null)
        {
        	root = root.element("packet");
        	GlobalLogger.instance().logDeprecatedMessage("sendMessageRTP ...><packet>...</packet></sendMessageRTP", "sendMessageRTP ...>...</sendMessageRTP");
        }
        msg = parsePacketHeader(root, runner);
        msg.setData(parsePacketPayload(root, runner).get(0));        

        return msg;
    }

    public MsgRtp parsePacketHeader(Element packet, Runner runner) throws Exception
    {
        MsgRtp packetRtp = new MsgRtp();
        Element header = packet.element("header");
        
        String ssrc = header.attributeValue("ssrc");
        if(ssrc != null){
            long ssrcLong = Long.parseLong(ssrc);
            packetRtp.setSsrc((int)ssrcLong);
        }                

        String payloadType = header.attributeValue("payloadType");
        if(payloadType != null)
            packetRtp.setPayloadType(Integer.parseInt(payloadType));

        String seqnum = header.attributeValue("seqnum");
        if((seqnum != null) && (Utils.isInteger(seqnum)))
            packetRtp.setSequenceNumber(Integer.parseInt(seqnum));

        String timestamp = header.attributeValue("timestamp");
        if((timestamp != null) && (Utils.isInteger(timestamp)))
            packetRtp.setTimestampRTP(Long.parseLong(timestamp));

        String mark = header.attributeValue("mark");
        if((mark != null) && (Utils.isInteger(mark)))
            packetRtp.setMarker(Integer.parseInt(mark));

        return packetRtp;
    }

    public ArrayList<Array> parsePacketPayload(Element packet, Runner runner) throws Exception
    {
        List<Element> payloads = packet.elements("payload");
        SupArray data = new SupArray();
        ArrayList<Array> listPayloadData = new ArrayList<Array>();
        String format = null;
        String text = null;

        for(Element element:payloads)
        {
            format = element.attributeValue("format");
            if (format == null)
            {
            	format = "binary";
            }
            text = element.getTextTrim();

            if(format.equalsIgnoreCase("text"))
            {
                data.addLast(new DefaultArray(text.getBytes()));
            }
            else if(format.equalsIgnoreCase("binary"))
            {
                data.addLast(new DefaultArray(Utils.parseBinaryString(text)));
            }
            else
            {
                throw new Exception("format of payload <" + format + "> is unknown");
            }
        }
        listPayloadData.add(data);
        return listPayloadData;
    }
    
     /**
     * Creates a Msg specific to each Stack
     * Use for UDP like protocol : to build incoming message
     * should become ABSTRACT later
     */
    @Override
    public Msg readFromDatas(byte[] datas, int length) throws Exception
    {
        byte[] copyData = new byte[length];
        System.arraycopy(datas, 0, copyData, 0, length);

        Array array = new ReadOnlyDefaultArray(copyData);
        MsgRtp msg = new MsgRtp(array);
        return msg;
    }

    public Config getConfig() throws Exception
    {
        return Config.getConfigByName("rtp.properties");
    }

    public XMLElementReplacer getElementReplacer(ParameterPool variables)
    {
        return new XMLElementTextMsgParser(variables);
    }
}