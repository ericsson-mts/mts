package com.devoteam.srit.xmlloader.gtp.data;

import java.util.ArrayList;
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

	
	public DataPDU() {
		super();
	}	

	public int decodeFromArray(Array array, Dictionary dictionary)
			throws Exception {
        this.binaryData = array.clone();
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
			var.add(Array.toHexString(binaryData));
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
	    }
    }

	public Array encodeToArray()
    {
    	return this.binaryData;
    }	
}
