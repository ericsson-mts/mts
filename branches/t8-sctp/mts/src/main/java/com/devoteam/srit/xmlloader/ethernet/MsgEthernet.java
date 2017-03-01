package com.devoteam.srit.xmlloader.ethernet;

import java.util.LinkedList;
import java.util.List;

import org.dom4j.Element;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Utils;

public class MsgEthernet extends Msg
{

	private byte[] data;
	private String[] dstMac; //dest MAC
	private String[] srcMac; //src MAC
	private int type;
		
	
    /** Creates a new instance */
    public MsgEthernet(Stack stack) throws Exception
    {
        super(stack);
        this.dstMac = new String[6];
        this.srcMac = new String[6];
        this.type = 0;
    }
	
    /** 
     * Return true if the message is a request else return false
     */
	@Override
	public boolean isRequest() throws Exception 
	{
		return true;
	}

    /** 
     * Get the type of the message
     * Used for message filtering with "type" attribute and for statistic counters 
     */
	@Override
	public String getType() throws Exception 
	{
		return "0x" + String.format("%04X", type);
	}

	public void setType(int type) 
	{
		this.type = type;
	}

	public int getTypeAsInteger()
	{
		return type;
	}

    /** 
     * Get the result of the message (null if request)
     * Used for message filtering with "result" attribute and for statistic counters 
     */
	@Override
	public String getResult() throws Exception 
	{
		return "0x" + String.format("%04X", type);
	}

	/**
	 * @param mac the mac to set
	 */
	public void setDstMac(String[] mac) 
	{
		this.dstMac = mac;
	}
	
	public String[] getDstMac()
	{
		return this.dstMac;
	}
	public void setDstMac(byte[] mac) 
	{
		for (int i = 0; i < mac.length; i++)
		{
			this.dstMac[i] = String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : "");
		}
	}
	
	public void setSrcMac(byte[] mac)
	{
		for (int i = 0; i < mac.length; i++)
		{
			this.srcMac[i] = String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : "");
		}
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
		return data;
	}

    /** 
     * decode the message from binary data 
     */
    public void decode(byte[] data) throws Exception
    {
		this.data = new byte [data.length];
    	for (int i = 0; i < data.length; i++)
    	{
    		this.data[i]= data[i];
    	}
    	setDstMac(new String[6]);
    	srcMac = new String[6];
    	setType(0);
	}
    
    
    //---------------------------------------------------------------------
    // methods for the XML display / parsing of the message
    //---------------------------------------------------------------------
    	    
    /** 
     * Convert the message to XML document 
     */
    @Override
    public String toXml() throws Exception 
    {
    	return Utils.byteTabToString(data);
    }
	
    /** 
     * Parse the message from XML element 
     */
    @Override
    public void parseFromXml(Boolean request, Element root, Runner runner) throws Exception
    {
    	super.parseFromXml(request,root,runner);

    	List<Element> elements = root.elements("data");
		Element header = root.element("ethernet");
        List<byte[]> datas = new LinkedList<byte[]>();        
        for (Element element : elements)
        {
            String text = element.getText();
            if (element.attributeValue("format").equalsIgnoreCase("text"))
            {
                // change the \n caractère to \r\n caracteres because the dom librairy return only \n.
                // this could make some trouble when the length is calculated in the scenario
                text = Utils.replaceNoRegex(text, "\r\n","\n");                    
                text = Utils.replaceNoRegex(text, "\n","\r\n");                    
                datas.add(text.getBytes("UTF8"));
            }
            else if (element.attributeValue("format").equalsIgnoreCase("binary"))
            {
                datas.add(Utils.parseBinaryString(text));
            }
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
                
        this.dstMac = header.attributeValue("remoteMac").split(":");
        this.type = Integer.parseInt(header.attributeValue("type"), 16);    
    }
	
	private String macToString(String[] mac)
	{
		String ret = "";
		for (int i = 0; i < mac.length; i++)
		{
			ret += mac[i];
		}
		return ret;
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
        else if (params[0].equalsIgnoreCase("ethernet"))
        {
        	if (params[1].equalsIgnoreCase("type"))
        		var.add(String.format("%04X", this.type));
        	else if (params[1].equalsIgnoreCase("dstMac"))
        		var.add(this.macToString(this.dstMac));
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
