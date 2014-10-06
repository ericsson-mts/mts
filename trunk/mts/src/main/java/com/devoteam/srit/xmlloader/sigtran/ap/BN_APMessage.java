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

import java.util.Collection;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.asn1.ASNMessage;
import com.devoteam.srit.xmlloader.asn1.BN_ASNMessage;
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
public class BN_APMessage extends BN_ASNMessage
{
	// TCAP layer
	private ASNMessage tcapMessage;
	
	public BN_APMessage(ASNMessage tcapMessage)
	{
		this.tcapMessage = tcapMessage;
	}
	
	public BN_APMessage(String className, ASNMessage tcapMessage) throws Exception
	{
		super(className);
		this.tcapMessage = tcapMessage;
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
        	Collection<Component> tcapComponents = getTCAPComponents();
        	Object[] tableComponents = (Object[])tcapComponents.toArray();
        	if (tableComponents.length >= 1)
        	{
        		Component component =  ((Component) tableComponents[0]);
        		if (component.isInvokeSelected())
        		{
        			return true;
        		}
        		else
        		{
        			return false;
        		}
        	}
        	return false;
        }
    }
   
    public String getType()
    {
    	Collection<Component> tcapComponents = getTCAPComponents();
    	Object[] tableComponents = (Object[])tcapComponents.toArray();
    	if (tableComponents.length >= 1)
    	{
    		Component component =  ((Component) tableComponents[0]);
    		if (component.isInvokeSelected())
    		{
    			Invoke invoke = component.getInvoke();
   			 	return Long.toString(invoke.getOpCode().getLocalValue());
    		}
    		else if (component.isReturnResultLastSelected())
        	{
    			ReturnResult returnResult = component.getReturnResultNotLast();
				if (returnResult != null && returnResult.getResultretres() != null)
				{	
					return Long.toString(returnResult.getResultretres().getOpCode().getLocalValue());
				}
				else
				{
					return null;
				}
        	}
    		else if (component.isReturnResultNotLastSelected())
        	{
    			ReturnResult returnResult = component.getReturnResultNotLast();
				if (returnResult != null && returnResult.getResultretres() != null)
				{	
					return Long.toString(returnResult.getResultretres().getOpCode().getLocalValue());
				}
				else
				{
					return null;
				}
        	}
    		else
    		{
    			// TO DO use the transaction
    			return null;
    		}
    	}
    	return null;
    }
    
    public String getResult()
    {
    	Collection<Component> tcapComponents = getTCAPComponents();
    	Object[] tableComponents = (Object[])tcapComponents.toArray();
    	if (tableComponents.length >= 1)
    	{
    		Component component =  ((Component) tableComponents[0]);
    		if (component.isInvokeSelected())
    		{
				return null;
			}
    		else if (component.isReturnResultLastSelected())
    		{
    			ReturnResult returnResult = component.getReturnResultLast();
				return "OK";
			}
    		else if (component.isReturnResultNotLastSelected())
    		{
    			Invoke invoke = component.getInvoke();
    			return "ok";
    		}
    		else if (component.isReturnErrorSelected())
    		{
    			ReturnError returnError = component.getReturnError();
    			if (returnError.getErrorCode() != null)
    			{
    				if (returnError.getErrorCode().isNationalerSelected())
    				{
    					return Long.toString(returnError.getErrorCode().getNationaler());
    				}
    				if (returnError.getErrorCode().isPrivateerSelected())
    				{
    					return Long.toString(returnError.getErrorCode().getPrivateer());
    				}
    				else
    				{
    					return "KO";
    				}
    			}
    		}
    		else if (component.isRejectSelected())
    		{
    			Reject reject = component.getReject();
    			if (reject.getProblem() != null)
    			{
    				if (reject.getProblem().isGeneralProblemSelected())
    				{
    					return Long.toString(reject.getProblem().getGeneralProblem().getValue());
    				}
    				else if (reject.getProblem().isInvokeProblemSelected())
    				{
    					return Long.toString(reject.getProblem().getInvokeProblem().getValue());
    				}
    				else if (reject.getProblem().isReturnErrorProblemSelected())
    				{
    					return Long.toString(reject.getProblem().getReturnErrorProblem().getValue());
    				}
    				else if (reject.getProblem().isReturnResultProblemSelected())
    				{
    					return Long.toString(reject.getProblem().getReturnResultProblem().getValue());
    				}
    			}
    		}
    	}
    	return "KO";    	
    }

    
    public String getTransactionId()
    {
    	TCMessage tcMessage = (TCMessage) apObject;
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
    	Array array = new DefaultArray(bytes);
    	return Array.toHexString(array);
    }

    public Collection<Component> getTCAPComponents()
    {
    	if (((TCMessage) apObject).isBeginSelected())
    	{
    		return ((TCMessage) apObject).getBegin().getComponents().getValue();
    	}
    	else if (((TCMessage) apObject).isEndSelected())
    	{
    		return ((TCMessage) apObject).getEnd().getComponents().getValue();
    	}
    	else if (((TCMessage) apObject).isContinue1Selected())
    	{
    		return ((TCMessage) apObject).getContinue1().getComponents().getValue();
    	}
    	else if (((TCMessage) apObject).isAbortSelected())
    	{
    		return null;
    	}
    	else if (((TCMessage) apObject).isUnidirectionalSelected())
    	{
    		return ((TCMessage) apObject).getUnidirectional().getComponents().getValue();
    	}
    	return null;
    } 
    
    
}