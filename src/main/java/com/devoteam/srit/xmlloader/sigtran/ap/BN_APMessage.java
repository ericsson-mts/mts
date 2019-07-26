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
import com.devoteam.srit.xmlloader.core.Parameter;
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
		return StackFactory.PROTOCOL_SIGTRAN + StackFactory.SEP_SUB_INFORMATION + this.dictionary.getLayer();
    }
	
    @Override
    public boolean isRequest() throws Exception
    {
        Parameter param = getParameter("asn.Component.invoke.invokeID");
        if (param.length() > 0)
        {
        	return true;
        }
        else
       	{
       		return false;
       	}
    	// return true;
    }
   
    @Override
    public String getType() throws Exception
    {
        Parameter param = getParameter("asn.Component.invoke.opCode.localValue");
        if (param.length() > 0)
        {
        	return param.get(0).toString();
        }
        param = getParameter("asn.Component.invoke.opCode.localValue.CAMELOperationLocalvalue");
        if (param.length() > 0)
        {
        	return param.get(0).toString();
        }
        param = getParameter("asn.Component.returnResult.opCode.localValue");
        if (param.length() > 0)
        {
        	return param.get(0).toString();
        }
        param = getParameter("asn.Component.returnResult.opCode.localValue.CAMELOperationLocalvalue");
        if (param.length() > 0)
        {
        	return param.get(0).toString();
        }
       	return null;
    }
    
    @Override
    public String getResult() throws Exception
    {
        Parameter param = getParameter("asn.Component.invoke.invokeID");
        if (param.length() > 0)
        {
        	return null;
        }
        param = getParameter("asn.Component.returnResultLast.invokeID");
        if (param.length() > 0)
        {
        	return "RESULT";
        }
        param = getParameter("asn.Component.returnError.errorCode.localValue");
        if (param.length() > 0)
        {
        	return "ERROR:" + param.get(0).toString();
        }
        /*
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
    	*/
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