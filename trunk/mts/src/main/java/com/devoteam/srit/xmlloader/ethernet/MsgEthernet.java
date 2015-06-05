package com.devoteam.srit.xmlloader.ethernet;

import java.util.LinkedList;
import java.util.List;

import org.dom4j.Element;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Utils;

public class MsgEthernet extends Msg
{

	private byte[] data;
	private String[] mac; //dest MAC
	private String[] srcMac; //src MAC
	private int type;
		
	
    /** Creates a new instance */
    public MsgEthernet() throws Exception
    {
        super();
    }

    /** Creates a new instance */
	public MsgEthernet (byte[] datas, int length) throws Exception
	{
		data = new byte [length];
    	for (int i=0; i<length; i++)
    		data[i]= datas[i];
    	setMac(new String[6]);
    	srcMac = new String[6];
    	setETHType(0);
	}
	
	@Override
	public String getProtocol() {
		return StackFactory.PROTOCOL_ETHERNET;
	}

	@Override
	public String getType() throws Exception {
		// TODO Auto-generated method stub
		return "0x" + (String.format("%04X", type));
	}

	public void setType(int type) {
		this.type = type;
	}

	@Override
	public String getResult() throws Exception {
		// TODO Auto-generated method stub
		return "0x" + String.format("%04X", type);
	}

	@Override
	public boolean isRequest() throws Exception {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public byte[] encode() {
		// TODO Auto-generated method stub
		return data;
	}

	/**
	 * @return the data
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * @param mac the mac to set
	 */
	public void setMac(String[] mac) {
		this.mac = mac;
	}

	/**
	 * @param proto the proto to set
	 */
	public void setETHType(int type) {
		this.type = type;
	}
	
	public String[] getMac()
	{
		return mac;
	}

	public int getETHType()
	{
		return type;
	}
	    
    /** Get the XML representation of the message; for the genscript module. */
    @Override
    public String toXml() throws Exception {
    	return Utils.byteTabToString(data);
    }

	public void setdstMac(byte[] dstMac) {
		// TODO Auto-generated method stub
		for (int i = 0; i < dstMac.length; i++)
			this.mac[i] = String.format("%02X%s", dstMac[i], (i < dstMac.length - 1) ? ":" : "");
	}
	
	public void setSrcMac(byte[] tmpMac)
	{
		for (int i = 0; i < tmpMac.length; i++)
			this.srcMac[i] = String.format("%02X%s", tmpMac[i], (i < tmpMac.length - 1) ? ":" : "");
	}
	
	private String macToString(String[] mac)
	{
		String ret = "";
		for (int i = 0; i < mac.length; i++)
			ret += mac[i];
		return ret;
	}
	
    /** 
     * Parse the message from XML element 
     */
    @Override
    public void parseMsgFromXml(Boolean request, Element root, Runner runner) throws Exception
    {
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

        this.data = new byte[dataLength];
        int i = 0;
        for (byte[] aData : datas)
        {
            for (int j = 0; j < aData.length; j++)
            {
                this.data[i] = aData[j];
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
        this.mac = header.attributeValue("remoteMac").split(":");
        this.type = Integer.parseInt(header.attributeValue("type"), 16);    
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
        
        if(params[0].equalsIgnoreCase("data")) 
        {
            if(params[1].equalsIgnoreCase("text")) 
            {
                var.add(new String(getData()));
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
        else if (params[0].equalsIgnoreCase("ethernet"))
        {
        	if (params[1].equalsIgnoreCase("type"))
        		var.add(String.format("%04X", this.type));
        	else if (params[1].equalsIgnoreCase("dstMac"))
        		var.add(this.macToString(this.mac));
        	else if (params[1].equalsIgnoreCase("srcMac"))
        		var.add(this.macToString(this.srcMac));
        	else
        		Parameter.throwBadPathKeywordException(path);
        }
        else
        	Parameter.throwBadPathKeywordException(path);             

        return var;
    }

}
