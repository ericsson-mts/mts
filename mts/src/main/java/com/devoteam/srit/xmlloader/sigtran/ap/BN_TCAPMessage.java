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

package com.devoteam.srit.xmlloader.sigtran.ap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;

import org.bn.CoderFactory;
import org.bn.IDecoder;
import org.bn.IEncoder;
import org.dom4j.Element;

import com.devoteam.srit.xmlloader.asn1.BN_ASNMessage;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.Component;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.ComponentPortion;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.Invoke;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.Reject;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.ReturnError;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.ReturnResult;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.TCMessage;


/**
 *
 * @author fhenry
 */
public class BN_TCAPMessage extends BN_ASNMessage
{
	public BN_TCAPMessage()
	{
		super();
	}
	
	public BN_TCAPMessage(String className) throws Exception
	{
		super(className);
    }

    public String getClassName()
    {
    	return this.className;
    }

	public void setClassName(String className) 
	{
		this.className = className;
	}
    
    public boolean isRequest()
    {
        {
        	if (((TCMessage) asnObject).isEndSelected())
        	{
        		return false;
        	}
        	return true;
        }
    }
   
    public String getType()
    {
    	if (((TCMessage) asnObject).isUnidirectionalSelected())
    	{
    		return "Unidirectional:1";
    	}
    	if (((TCMessage) asnObject).isBeginSelected())
    	{
    		return "Begin:2";
    	}
    	else if (((TCMessage) asnObject).isEndSelected())
    	{
    		return "End:4";
    	}
    	else if (((TCMessage) asnObject).isContinue1Selected())
    	{
    		return "Continue:5";
    	}
    	else if (((TCMessage) asnObject).isAbortSelected())
    	{
    		return "Abort:7";
    	}
    	return null;
    }
    
    public String getResult()
    {
    	return "OK";
    }

    
    public String getTransactionId()
    {
    	TCMessage tcMessage = (TCMessage) asnObject;
    	byte[] bytes = null;
    	if (tcMessage.isBeginSelected())
    	{
    		bytes = tcMessage.getBegin().getOtid().getValue();
    	}
    	else if (tcMessage.isEndSelected())
        {
    		bytes = tcMessage.getEnd().getDtid().getValue();
    	}
    	else if (tcMessage.isContinue1Selected())
        {
    		if (tcMessage.getContinue1().getOtid() != null)
    		{
    			bytes = tcMessage.getContinue1().getOtid().getValue();
    		}
    		else if (tcMessage.getContinue1().getDtid() != null)
    		{
    			bytes = tcMessage.getContinue1().getDtid().getValue();
    		}
        }
    	else if (tcMessage.isAbortSelected())
        {
    		bytes = tcMessage.getAbort().getDtid().getValue();
    	}
    	if (bytes != null)
    	{
	    	Array array = new DefaultArray(bytes);
	    	return Array.toHexString(array);
    	}
    	return "";
    }

    public Array getTCAPComponents() throws Exception
    {
    	Collection<Component> comps = null;
    	if (((TCMessage) asnObject).isBeginSelected())
    	{
    		comps = ((TCMessage) asnObject).getBegin().getComponents().getValue();
    	}
    	else if (((TCMessage) asnObject).isEndSelected())
    	{
    		comps = ((TCMessage) asnObject).getEnd().getComponents().getValue();
    	}
    	else if (((TCMessage) asnObject).isContinue1Selected())
    	{
    		comps = ((TCMessage) asnObject).getContinue1().getComponents().getValue();
    	}
    	else if (((TCMessage) asnObject).isAbortSelected())
    	{
    		// nothing 
    	}
    	else if (((TCMessage) asnObject).isUnidirectionalSelected())
    	{
    		comps = ((TCMessage) asnObject).getUnidirectional().getComponents().getValue();
    	}
    	
    	Component component = null;
    	Iterator iter = comps.iterator();
    	while (iter.hasNext())
    	{
    		component = (Component) iter.next();
    	}
    	
    	Array arrayMAP = null;
    	if (component != null)
    	{
	    	// Library binarynotes
	    	IEncoder<java.lang.Object> encoderMAP = CoderFactory.getInstance().newEncoder("BER");
	    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	        encoderMAP.encode(component, outputStream);
	        byte[] bytesMAP = outputStream.toByteArray();
	        arrayMAP = new DefaultArray(bytesMAP);
	        // String strMAP = Utils.toHexaString(bytesMAP, null);
    	}
        return arrayMAP;
    } 

    public void setTCAPComponents(Array array) throws Exception
    {
    	Collection<Component> comps = new ArrayList<Component>();
    	Component component = new Component();
    	//component.setValue(array.getBytes());
    	
    	IDecoder decoder = CoderFactory.getInstance().newDecoder("BER");
        InputStream inputStream = new ByteArrayInputStream(array.getBytes());
        Class cl = Class.forName(Component.class.getCanonicalName());
        component = (Component) cl.newInstance();
        component = decoder.decode(inputStream, cl);
        comps.add(component);
        
        ComponentPortion compPortion = new ComponentPortion();
        compPortion.setValue(comps);
        if (((TCMessage) asnObject).isUnidirectionalSelected())
    	{
    		((TCMessage) asnObject).getUnidirectional().setComponents(compPortion);
    	} 
        else if (((TCMessage) asnObject).isBeginSelected())
    	{
    		((TCMessage) asnObject).getBegin().setComponents(compPortion);
    	}
    	else if (((TCMessage) asnObject).isEndSelected())
    	{
    		((TCMessage) asnObject).getEnd().setComponents(compPortion);
    	}
    	else if (((TCMessage) asnObject).isContinue1Selected())
    	{
    		((TCMessage) asnObject).getContinue1().setComponents(compPortion);
    	}
    	else if (((TCMessage) asnObject).isAbortSelected())
    	{
    		// nothing;
    	}
    } 
    
    
}