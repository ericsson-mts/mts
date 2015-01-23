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
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.sigtran.ap.map.Component;
import com.devoteam.srit.xmlloader.sigtran.ap.map.Invoke;
import com.devoteam.srit.xmlloader.sigtran.ap.map.Reject;
import com.devoteam.srit.xmlloader.sigtran.ap.map.ReturnError;
import com.devoteam.srit.xmlloader.sigtran.ap.map.ReturnResult;
import com.devoteam.srit.xmlloader.sigtran.ap.map.ReturnResult.ResultretresSequenceType;

/**
 *
 * @author fhenry
 */
public class BN_APMessage extends BN_ASNMessage
{
	public BN_APMessage() throws Exception
	{
		super();
	}
	
	public BN_APMessage(String dictionaryFile) throws Exception
	{
		super(dictionaryFile);
	}
	
	public String getProtocol()
    {
		return StackFactory.PROTOCOL_SIGTRAN + "." + this.dictionary.getLayer();
    }
	
    @Override
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
   
    @Override
    public String getType()
    {
    	Component apMessage = (Component) asnObject;
		if (apMessage.isInvokeSelected())
		{
			Invoke invoke = apMessage.getInvoke();
			if (invoke.getOpCode() != null && invoke.getOpCode().getLocalValue() != null)
			{
				return Long.toString(invoke.getOpCode().getLocalValue().getValue());
			}
		}
		else if (apMessage.isReturnResultLastSelected())
    	{
			ReturnResult returnResult = apMessage.getReturnResultLast();
			if (returnResult != null && returnResult.getResultretres() != null)
			{
				ResultretresSequenceType resultretresSequenceType = returnResult.getResultretres(); 
				if (resultretresSequenceType.getOpCode() != null && resultretresSequenceType.getOpCode().getLocalValue() != null)
				{
					return Long.toString(returnResult.getResultretres().getOpCode().getLocalValue().getValue());
				}
			}
    	}
		else if (apMessage.isReturnErrorSelected())
    	{
			
			return null;
    	}
		else if (apMessage.isRejectSelected())
    	{
			
			return null;
    	}
		return null;
    }
    
    @Override
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
			return "RESULT";
		}
		else if (apMessage.isReturnErrorSelected())
		{
			ReturnError returnError = apMessage.getReturnError();
			if (returnError.getErrorCode() != null)
			{
				if (returnError.getErrorCode().isGlobalValueSelected())
				{
					return "ERROR:" + returnError.getErrorCode().getGlobalValue().getValue();
				}
				if (returnError.getErrorCode().isLocalValueSelected())
				{
					return "ERROR:" + Long.toString(returnError.getErrorCode().getLocalValue().getValue());
				}
				else
				{
					return "ERROR";
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
					return "REJECT:General:" + Long.toString(reject.getProblem().getGeneralProblem().getValue());
				}
				else if (reject.getProblem().isInvokeProblemSelected())
				{
					return "REJECT:Invoke:" + Long.toString(reject.getProblem().getInvokeProblem().getValue());
				}
				else if (reject.getProblem().isReturnErrorProblemSelected())
				{
					return "REJECT:Error:" + Long.toString(reject.getProblem().getReturnErrorProblem().getValue());
				}
				else if (reject.getProblem().isReturnResultProblemSelected())
				{
					return "REJECT:Result:" + Long.toString(reject.getProblem().getReturnResultProblem().getValue());
				}
				else
				{
					return "REJECT";
				}
			}
    	}
    	return "KO";    	
    }
   
    public String getTransactionId()
    {
    	return "";
    	/*
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
    	*/
    }
   
}