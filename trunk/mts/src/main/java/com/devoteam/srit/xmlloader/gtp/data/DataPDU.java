package com.devoteam.srit.xmlloader.gtp.data;

import java.util.LinkedList;
import java.util.List;

import org.dom4j.Element;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.SupArray;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.coding.binary.Dictionary;
import com.devoteam.srit.xmlloader.core.coding.binary.ElementAbstract;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.utils.Utils;

public class DataPDU {

	private Array binaryData = null;
	private byte[] data = null;

	
	public DataPDU() {
		super();
		//this.binaryData = new SupArray();
	}	

	public int decodeFromArray(Array array, Dictionary dictionary)
			throws Exception {
		//this.binaryData = new SupArray();
		System.out.println("DECODE FROM ARRAY --> flags = " + String.format("%02x ", array.get(6), 16) + String.format("%02x ", array.get(7), 16) + "; length = " + array.length);
		this.data = new byte[array.length];
		for (int i = 0; i < array.length; i++)
	 		data[i] = array.get(i);
        this.binaryData = array;
		return 0;
	}
	

	public String toXml() 
    {
        return "<pdu type=\"binary\">\n" + Utils.toBinaryString(this.binaryData.getBytes(), 0, this.binaryData.getBytes().length, 0) + "\n</pdu>\n";
    }
	
	@Override
	public String toString() 
    {
    	return toXml();
    }
	
	public int getFieldLength()
	{
		return this.binaryData.length;
	}


	 public void getParameter(Parameter var, String[] params, String path, int offset, Dictionary dictionary) throws Exception 
	    {
		 	System.out.println("DATAPDU --> flags = " + String.format("%02x ", binaryData.get(6), 16) + String.format("%02x ", binaryData.get(7), 16) + "; length = " + binaryData.length);
		 	
			var.add(Array.toHexString(binaryData));
		 	//var.add(this.toHexString());
	    }
	

	public void parseFromXML(Element elementRoot, Dictionary dictionary, ElementAbstract elemDico) throws Exception 
    {
	    List<Element> data = elementRoot.elements("pdu");
	    if (data != null)
	    {
			List<byte[]> datas = new LinkedList<byte[]>();        
	        try
	        {
	            for (Element element : data)
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
	            throw new ExecutionException("StackGTPv1: Error while parsing data", e);
	        }
	        
	        int dataLength = 0;
	        for (byte[] data2 : datas)
	        {
	            dataLength += data2.length;
	        }
	
	        byte[] data2 = new byte[dataLength];
	        int i = 0;
	        for (byte[] aData : datas)
	        {
	            for (int j = 0; j < aData.length; j++)
	            {
	                data2[i] = aData[j];
	                i++;
	            }
	        }
	        this.binaryData = new DefaultArray(data2);
	        this.data = data2;
	    }
    }

	public Array encodeToArray()
    {
    	return this.binaryData;
    }
	
	private String toHexString()
	{
		String ret = "";
		for (int i = 0; i < data.length; i++)
			ret += String.format("%02x", data[i], 16);
		return ret;
	}
	
}
