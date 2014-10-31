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
import com.devoteam.srit.xmlloader.sigtran.ap.map.Component;
import com.devoteam.srit.xmlloader.sigtran.ap.map.Invoke;
import com.devoteam.srit.xmlloader.sigtran.ap.map.Reject;
import com.devoteam.srit.xmlloader.sigtran.ap.map.ReturnError;
import com.devoteam.srit.xmlloader.sigtran.ap.map.ReturnResult;

/**
 *
 * @author fhenry
 */
public class BN_APMessage extends BN_ASNMessage
{
	public BN_APMessage(ASNMessage tcapMessage)
	{
	}
	
	public BN_APMessage(String className) throws Exception
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
    	Component apMessage = (Component) asnObject;
    	if (apMessage.isInvokeSelected())
    	{
    		return true;
    	}
    	else if (apMessage.isReturnResultLastSelected())
        {
    		return false;
    	}
    	else if (apMessage.isRejectSelected())
        {
    		return false;
    	}
    	else if (apMessage.isReturnErrorSelected())
        {
    		return false;
        }
    	return false;

    }
   
    public String getType()
    {
    	Component apMessage = (Component) asnObject;
		if (apMessage.isInvokeSelected())
		{
			Invoke invoke = apMessage.getInvoke();
		 	return Long.toString(invoke.getOpCode().getLocalValue().getValue());
		}
		else if (apMessage.isReturnResultLastSelected())
    	{
			ReturnResult returnResult = apMessage.getReturnResultLast();
			if (returnResult != null && returnResult.getResultretres() != null)
			{	
				return Long.toString(returnResult.getResultretres().getOpCode().getLocalValue().getValue());
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
    
    public String getResult()
    {
    	Component apMessage = (Component) asnObject;
		if (apMessage.isInvokeSelected())
		{
			return null;
		}
		else if (apMessage.isReturnResultLastSelected())
		{
			ReturnResult returnResult = apMessage.getReturnResultLast();
			return "OK";
		}
		else if (apMessage.isReturnErrorSelected())
		{
			ReturnError returnError = apMessage.getReturnError();
			if (returnError.getErrorCode() != null)
			{
				if (returnError.getErrorCode().isGlobalValueSelected())
				{
					return returnError.getErrorCode().getGlobalValue().getValue();
				}
				if (returnError.getErrorCode().isLocalValueSelected())
				{
					return Long.toString(returnError.getErrorCode().getLocalValue().getValue());
				}
				else
				{
					return "KO";
				}
			}
		}
		else if (apMessage.isRejectSelected())
		{
			Reject reject = apMessage.getReject();
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
    	return "KO";    	
    }
   
    public String getTransactionId()
    {
    	Component apMessage = (Component) asnObject;
    	String transId = null;
    	if (apMessage.isInvokeSelected())
    	{
    		transId = String.valueOf(apMessage.getInvoke().getInvokeID().getValue());
    	}
    	else if (apMessage.isReturnResultLastSelected())
        {
    		transId = String.valueOf(apMessage.getReturnResultLast().getInvokeID().getValue());
    	}
    	else if (apMessage.isRejectSelected())
        {
    		transId = null;
    	}
    	else if (apMessage.isReturnErrorSelected())
        {
    		transId = String.valueOf(apMessage.getReturnError().getInvokeID().getValue());
        }
    	return transId;
    }

    /*
    public Collection<Component> getTCAPComponents()
    {
    	if (((TCMessage) asnObject).isBeginSelected())
    	{
    		return ((TCMessage) asnObject).getBegin().getComponents().getValue();
    	}
    	else if (((TCMessage) asnObject).isEndSelected())
    	{
    		return ((TCMessage) asnObject).getEnd().getComponents().getValue();
    	}
    	else if (((TCMessage) asnObject).isContinue1Selected())
    	{
    		return ((TCMessage) asnObject).getContinue1().getComponents().getValue();
    	}
    	else if (((TCMessage) asnObject).isAbortSelected())
    	{
    		return null;
    	}
    	else if (((TCMessage) asnObject).isUnidirectionalSelected())
    	{
    		return ((TCMessage) asnObject).getUnidirectional().getComponents().getValue();
    	}
    	return null;
    } 
    */
    
}