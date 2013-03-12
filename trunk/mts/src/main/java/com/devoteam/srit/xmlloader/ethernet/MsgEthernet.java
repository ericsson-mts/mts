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
	private String[] mac;
	private int type;
	public void setType(int type) {
		this.type = type;
	}

	private int nic;
	
	public MsgEthernet (byte[] datas, int length) throws Exception
	{
		data = new byte [length];
    	for (int i=0; i<length; i++)
    		data[i]= datas[i];
    	setMac(null);
    	setETHType(0);
    	setNic(0);
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
	public byte[] getBytesData() {
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

	/**
	 * @return the nic
	 */
	public int getNic() {
		return nic;
	}

	/**
	 * @param nic the nic to set
	 */
	public void setNic(int nic) {
		this.nic = nic;
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
        String[] params = Utils.splitPath(path);
        
        if(params[0].equalsIgnoreCase("data")) 
        {
            if(params[1].equalsIgnoreCase("text")) 
            {
                var.add(new String(getData()));
            }
            else if(params[1].equalsIgnoreCase("binary")) 
            {
            	var.add(Array.toHexString(new DefaultArray(getBytesData())));
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
    
    /** Get the XML representation of the message; for the genscript module. */
    @Override
    public String toXml() throws Exception {
    	return Utils.byteTabToString(data);
    }
}
