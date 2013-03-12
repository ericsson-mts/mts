package com.devoteam.srit.xmlloader.ethernet;

import java.util.LinkedList;
import java.util.List;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.core.utils.XMLElementTextMsgParser;
import com.devoteam.srit.xmlloader.udp.MsgUdp;

public class StackEthernet extends Stack
{
	
	private SocketEthernet sock = null;
	private String captureFilter = null;
	private boolean isEthernetProbeCreated = false;

	public boolean isEthernetProbeCreated() {
		return isEthernetProbeCreated;
	}

	public void setEthernetProbeCreated(boolean isEthernetProbeCreated) {
		this.isEthernetProbeCreated = isEthernetProbeCreated;
	}

	public String getCaptureFilter() {
		return captureFilter;
	}

	public void setCaptureFilter(String captureFilter) {
		this.captureFilter = captureFilter;
	}

	public SocketEthernet getSock() {
		return sock;
	}

	public StackEthernet() throws Exception {
		super();
        Listenpoint listenpoint = new ListenpointEthernet(this);
        createListenpoint(listenpoint, StackFactory.PROTOCOL_ETHERNET);
        sock = new SocketEthernet();
        sock.setListenpointEthernet((ListenpointEthernet) listenpoint);
        sock.setDaemon(true);
        captureFilter = "";
	}

	@Override
	public Config getConfig() throws Exception {
		return Config.getConfigByName("ethernet.properties");
	}

	@Override
	public XMLElementReplacer getElementReplacer() {
		return XMLElementTextMsgParser.instance();
	}

	@Override
	public Msg parseMsgFromXml(Boolean request, Element root, Runner runner) throws Exception {		
		//
        // Parse all <data ... /> tags
        //
		List<Element> elements = root.elements("data");
		Element header = root.element("ethernet");
        List<byte[]> datas = new LinkedList<byte[]>();        
        try
        {
            for (Element element : elements)
            {
                if (element.attributeValue("format").equalsIgnoreCase("text"))
                {
                    String text = element.getText();
                    // change the \n caractère to \r\n caracteres because the dom librairy return only \n.
                    // this could make some trouble when the length is calculated in the scenario
                    text = Utils.replaceNoRegex(text, "\r\n","\n");                    
                    text = Utils.replaceNoRegex(text, "\n","\r\n");                    
                    datas.add(text.getBytes("UTF8"));
                }
                else if (element.attributeValue("format").equalsIgnoreCase("binary"))
                {
                    String text = element.getTextTrim();
                    datas.add(Utils.parseBinaryString(text));
                }
            }
        }
        catch (Exception e)
        {
            throw new ExecutionException("StackIp: Error while parsing data", e);
        }
        
        //
        // Compute total length
        //
        int dataLength = 0;
        for (byte[] data : datas)
        {
            dataLength += data.length;
        }

        byte[] data = new byte[dataLength];
        int i = 0;
        for (byte[] aData : datas)
        {
            for (int j = 0; j < aData.length; j++)
            {
                data[i] = aData[j];
                i++;
            }
        }

        String length = root.attributeValue("length");
        if (length != null)
        {
        	dataLength = Integer.parseInt(length);
            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "fixed length of the datagramPacket to be sent:  ", dataLength);
            if (data.length != dataLength)
            {
                GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "data.length different from chosen fixed length");
            }
        }
                
        MsgEthernet msgIp = new MsgEthernet(data, dataLength);
        msgIp.setMac(header.attributeValue("remoteMac").split(":"));
        msgIp.setETHType(Integer.parseInt(header.attributeValue("type"), 16));
        if (root.attributeValue("nic") != null)
        	msgIp.setNic(Integer.parseInt(root.attributeValue("nic")));
		return msgIp;
	}

	@Override    
    public Msg readFromDatas(byte[] datas, int length) throws Exception
    {
    	MsgEthernet msg = new MsgEthernet(datas, length);    		
    	return msg;
    }
	
	public void startSocket()
	{
		this.sock.run();
	}
	
}
