package com.devoteam.srit.xmlloader.ethernet;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Utils;

public class MsgEthernet extends Msg
{

	private byte[] data;
	private String[] mac; //dest MAC
	private String[] srcMac; //src MAC
	private int type;
	
	public void setType(int type) {
		this.type = type;
	}
	
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
	
	/** Get a parameter from the message */
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
}
