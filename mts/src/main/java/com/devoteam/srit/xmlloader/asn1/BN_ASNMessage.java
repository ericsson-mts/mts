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

	// ASN1 binarynotes object
	protected Object asnObject;
	
	public BN_ASNMessage()
    {
		super();
    }
	
    public BN_ASNMessage(String className) throws Exception
    {
    	super(className);
        Class cl = Class.forName(className);
        this.asnObject = cl.newInstance();
    }

    public BN_ASNMessage(Object asnObject) throws Exception
    {
    	super(asnObject.getClass().getCanonicalName()); 
    	this.asnObject = asnObject;
    }

	public Object getAsnObject() 
	{
		return asnObject;
	}

    public Array encode() throws Exception 
    {
    	// Library binarynotes
    	IEncoder<java.lang.Object> encoderMAP = CoderFactory.getInstance().newEncoder("BER");
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        encoderMAP.encode(this.asnObject, outputStream);
        byte[] bytesMAP = outputStream.toByteArray();
        Array arrayMAP = new DefaultArray(bytesMAP);
        // String strMAP = Utils.toHexaString(bytesMAP, null);
		
        return arrayMAP;
    }
          
    
    public void decode(Array array) throws Exception 
    {
        
    	IDecoder decoder = CoderFactory.getInstance().newDecoder("BER");
        InputStream inputStream = new ByteArrayInputStream(array.getBytes());
        Class cl = Class.forName(className);
        this.asnObject = cl.newInstance();
        this.asnObject = decoder.decode(inputStream, cl);
        
    }

    public void parseFromXML(Element root) throws Exception 
    {
    	this.className = root.attributeValue("className");
    	
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

    public String toXML()
    {
        String ret = "";
        ret += "<AP className=\"" + className + "\">\n" + ASNToXMLConverter.indent(ASNToXMLConverter.NUMBER_SPACE_TABULATION);
        String resultPath = "";
        ret += ASNToXMLConverter.getInstance().toXML(resultPath, this, "value", this.asnObject, null, ASNToXMLConverter.NUMBER_SPACE_TABULATION * 2);
        ret += "\n";
        ret += "</AP>";
    	return ret;
    }
    
}
