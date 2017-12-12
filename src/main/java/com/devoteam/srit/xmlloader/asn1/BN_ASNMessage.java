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

package com.devoteam.srit.xmlloader.asn1;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.bn.CoderFactory;
import org.bn.IDecoder;
import org.bn.IEncoder;
import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.Component;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.Invoke;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.Reject;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.ReturnError;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.ReturnResult;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.TCMessage;


/**
 *
 * @author fhenry
 */

public class BN_ASNMessage extends ASNMessage 
{
	
	public BN_ASNMessage() throws Exception
    {
		super();
    }
	
	public BN_ASNMessage(String dictionaryFile) throws Exception
	{
		super(dictionaryFile);
	}
		
    public BN_ASNMessage(String dictionaryFile, Object asnObject) throws Exception
    {
    	super(dictionaryFile);
    	this.asnObject = asnObject;
    }
	
	public Object getAsnObject() 
	{
		return asnObject;
	}

    @Override
    public Array encode(String rule) throws Exception 
    {
    	// Library binarynotes
    	IEncoder<java.lang.Object> encoder = CoderFactory.getInstance().newEncoder(rule);
    	if (encoder == null)
    	{
    		throw new Exception("bad value for rule '" + rule + "'");
    	}
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    	try
    	{
    		encoder.encode(this.asnObject, outputStream);
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    		return null;
    	}
    	
        byte[] bytesMAP = outputStream.toByteArray();
        Array arrayMAP = new DefaultArray(bytesMAP);
        // String strMAP = Utils.toHexaString(bytesMAP, null);
		
        return arrayMAP;
    }
          
    @Override
    public void decode(Array array, String className, String rule) throws Exception 
    {
    	// Library binarynotes        
    	IDecoder decoder = CoderFactory.getInstance().newDecoder(rule);
    	if (decoder == null)
    	{
    		throw new ExecutionException("bad value for rule '" + rule +"'");
    	}
        InputStream inputStream = new ByteArrayInputStream(array.getBytes());
        Class cl = Class.forName(className);
        this.asnObject = cl.newInstance();
        if (array.length > 0)
        {
        	this.asnObject = decoder.decode(inputStream, cl);
        }
    }

    @Override
    public void parseFromXML(Element root, String className) throws Exception 
    {
    	String dictionaryFile = root.attributeValue("dictionary");
    	if (dictionaryFile != null)
    	{
    		initDictionary(dictionaryFile);
    	}
    	
        List<Element> children = root.elements();
        for (Element element : children) 
        {
            Class thisClass = Class.forName(className);
            int pos = className.lastIndexOf('.');
            String packageName = "";
            if (pos > 0)
            {
            	packageName = className.substring(0, pos + 1);
            }
            this.asnObject = thisClass.newInstance();
            String resultPath = "";
            XMLToASNParser.getInstance().parseFromXML(resultPath, this, this.asnObject, element, packageName);
        }
    }
    
    @Override
    public Parameter getParameter(String path) throws Exception
    {
    	Parameter var = super.getParameter(path);
        if (null != var) 
        {
            return var;
        }

        String resultPath = "";
        Parameter parameter = new Parameter();
        ASNGetParameter.getInstance().getParameter(parameter, path, resultPath, this, null, "value", this.asnObject);
        
    	return parameter;
    }
    
    @Override
    public boolean isRequest() throws Exception
    {
    	// TODO complete 
    	return true;
    }
    
    @Override
    public String getType() throws Exception
    {
    	// TODO complete 
    	return "TODO";
    }
    
    @Override
    public String getResult() throws Exception
    {
    	// TODO complete 
    	return "TODO";
    }

    public String toXML()
    {
        StringBuilder messageToString = new StringBuilder();
        messageToString.append("<ASN ");
        messageToString.append("layer=\"");
        messageToString.append(this.dictionary.getLayer());
        messageToString.append("\"");
        messageToString.append(">\n");

        messageToString.append(ASNToXMLConverter.indent(ASNToXMLConverter.NUMBER_SPACE_TABULATION));
        String resultPath = "";
        messageToString.append(ASNToXMLConverter.getInstance().toXML(resultPath, this, null, "value", this.asnObject, null, ASNToXMLConverter.NUMBER_SPACE_TABULATION * 2));

        messageToString.append("\n</ASN>");
    	return messageToString.toString();
    }
    
}
