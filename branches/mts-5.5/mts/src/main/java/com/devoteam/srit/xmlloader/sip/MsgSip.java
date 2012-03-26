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

package com.devoteam.srit.xmlloader.sip;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.TransactionId;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.sip.MsgSip;

/**
 *
 * @author gpasquiers
 */
public abstract class MsgSip extends Msg
{
    private TransactionId responseTransactionId;
    private boolean isResponseTransactionIsSet;    
    
    /** Creates a new instance of MsgSip */
    public MsgSip() 
    {
        super();
    }
    
    /** Get the transaction Identifier of this message */
    public TransactionId getResponseTransactionId() throws Exception
    {
        if(!this.isResponseTransactionIsSet)
        {
            this.responseTransactionId = null;
            if(!this.isRequest())
            {
                if("INVITE".equals(this.getType()))
                {
                    int code = new Integer(getResult()).intValue();
                    if(code >= 200)
                    {
                    	Parameter cSeqParam = this.getParameter("header.CSeq.Number");
                        if (cSeqParam.length() > 0)
                        {
                            this.responseTransactionId = new TransactionId(this.getListenpoint().getName() + "|" + this.getDialogId() + "|" +  cSeqParam.get(0));
                        }
                    }
                    else if(code >= 180 && code < 190)
                    {
                    	Parameter cSeqNumberParam = this.getParameter("header.CSeq.Number");
                    	Parameter cSeqMethodParam = this.getParameter("header.CSeq.Method");
                        if (cSeqNumberParam.length() > 0 && cSeqMethodParam.length() > 0)
                        {
	                        String cSeqNumber = cSeqNumberParam.get(0).toString();
	                        String cSeqMethod = cSeqMethodParam.get(0).toString();
	                        Parameter rSeqParam = this.getParameter("header.RSeq");
	                        if (rSeqParam.length() > 0)
	                        {
	                            String rSeq = rSeqParam.get(0).toString();
	                            this.responseTransactionId = new TransactionId(this.getListenpoint().getName() + "|" + this.getDialogId() + "|" + rSeq + "|" + cSeqNumber + "|" + cSeqMethod);
	                        }
	                        else
	                        {
	                            this.responseTransactionId = new TransactionId(this.getListenpoint().getName() + "|" + this.getDialogId() + "|" + cSeqNumber);
	                        }
                        }
                    }
                }
            }
            else
            {
                if("ACK".equals(this.getType()) || "CANCEL".equals(this.getType()))
                {
                    Parameter cSeqParam = this.getParameter("header.CSeq.Number");
                    if (cSeqParam.length() > 0)
                    {
                        this.responseTransactionId = new TransactionId(this.getListenpoint().getName() + "|" + this.getDialogId() + "|" + cSeqParam.get(0));
                    }
                }
                else if ("PRACK".equals(this.getType()))
                {
                	Parameter rAckNumberParam = this.getParameter("header.RAck.Number");
                	Parameter rAckCSeqNumberParam = this.getParameter("header.RAck.CSeqNumber");
                	Parameter rAckMethodParam = this.getParameter("header.RAck.Method");

                    if (rAckNumberParam.length() > 0 && rAckCSeqNumberParam.length() > 0 && rAckMethodParam.length() > 0) 
                    {
                        String rAckNumber = rAckNumberParam.get(0).toString();
                        String rAckCSeqNumber = rAckCSeqNumberParam.get(0).toString();
                        String rAckMethod = rAckMethodParam.get(0).toString();
                        this.responseTransactionId = new TransactionId(this.getListenpoint().getName() + "|" + this.getDialogId() + "|" + rAckNumber + "|" + rAckCSeqNumber + "|" + rAckMethod);
                    }
                    else
                    {
                        // some error ? a PRACK SHOULD contain a RAck.
                    }
                }
            }
            
            this.isResponseTransactionIsSet = true;
        }

        return this.responseTransactionId;
    }

    /** Get the protocol of this message */
    @Override
    public String getProtocol()
    {
        return StackFactory.PROTOCOL_SIP;
    }

    /** Get the command code of this message */
    @Override
    public String getType() throws Exception
    {
    	if (isRequest())
    	{
	    	Parameter firstlineMethod = getParameter("firstline.Method");
	    	if (firstlineMethod.length() > 0)
	    	{
	    		return firstlineMethod.get(0).toString();
	    	}
    	}
	    else
	    {
	    	Parameter ceqMethod = getParameter("header.CSeq.Method");
	    	if (ceqMethod.length() > 0)
	    	{
	    		return ceqMethod.get(0).toString();
	    	}
	    }
	    	
        return null;
    }

    /** Get the dialog id of this message */
    public String getDialogId() throws Exception
    {
    	if (isRequest())
    	{
	    	if (this.isSend())
	    	{
		    	Parameter callID = getParameter("header.Call-ID");
		    	String res = "";
		    	if (callID.length() > 0)
		    	{
		    		res += callID.get(0);
		    	}
		        res += "|";
		        Parameter from = getParameter("header.From.Parameter.tag");
		    	if (from.length() > 0)
		    	{
		    		res += from.get(0);
		    	}
		        res += "|";
		        Parameter to = getParameter("header.To.Parameter.tag");
		    	if (to.length() > 0)
		    	{
		    		res += to.get(0);
		    	}
		        return res;
	    	}
	    	else
	    	{
		    	Parameter callID = getParameter("header.Call-ID");
		    	String res = "";
		    	if (callID.length() > 0)
		    	{
		    		res += callID.get(0);
		    	}
		        res += "|";
		        Parameter from = getParameter("header.To.Parameter.tag");
		    	if (from.length() > 0)
		    	{
		    		res += from.get(0);
		    	}
		        res += "|";
		        Parameter to = getParameter("header.From.Parameter.tag");
		    	if (to.length() > 0)
		    	{
		    		res += to.get(0);
		    	}	    	
		        return res;
	    	}
    	}
	    else
	    {		
	    	if (this.isSend())
	    	{
		    	Parameter callID = getParameter("header.Call-ID");
		    	String res = "";
		    	if (callID.length() > 0)
		    	{
		    		res += callID.get(0);
		    	}
		        res += "|";
		        Parameter from = getParameter("header.To.Parameter.tag");
		    	if (from.length() > 0)
		    	{
		    		res += from.get(0);
		    	}
		        res += "|";
		        Parameter to = getParameter("header.From.Parameter.tag");
		    	if (to.length() > 0)
		    	{
		    		res += to.get(0);
		    	}
		        return res;
	    	}
	    	else
	    	{
		    	Parameter callID = getParameter("header.Call-ID");
		    	String res = "";
		    	if (callID.length() > 0)
		    	{
		    		res += callID.get(0);
		    	}
		        res += "|";
		        Parameter from = getParameter("header.From.Parameter.tag");
		    	if (from.length() > 0)
		    	{
		    		res += from.get(0);
		    	}
		        res += "|";
		        Parameter to = getParameter("header.To.Parameter.tag");
		    	if (to.length() > 0)
		    	{
		    		res += to.get(0);
		    	}	    	
		        return res;		        
	    	}
	    }
    }

    /** Get the result of this answer (null if request) */
    @Override
    public String getResult() throws Exception
    {
        if (!isRequest())
        {
           Parameter status = getParameter("firstline.StatusCode");
           if (status.length()>0)
        	return status.get(0).toString();
        } 
        return null;
    }

    /**
     *  Tell whether the message shall be retransmitted or not 
     * (= true by default) 
     */
    @Override
    public boolean shallBeRetransmitted() throws Exception
    {
        String type = getType();
        if (isRequest())
        {
            if ("ACK".equals(type))
            {
                return false;
            }
            else
            {
                return true;    
            }
        }
        else
        {
            int result = new Integer(getResult()).intValue();
            if (("INVITE".equals(type)))
            {
                if(result >= 200)
                {
                    return true;
                }
                else if ((result >= 180) && (result < 190) && (getParameter("header.RSeq").length() > 0))
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
    }

    /**
     *  Tell whether the message shall be stop the automatic 
     *  retransmmission mechanism or not 
     * (= true by default) 
     */
    @Override
    public boolean shallStopRetransmit() throws Exception
    {
        String type = getType();
        if (isRequest())
        {
            if ("ACK".equals(type))
            {
                return true;
            }
            if ("PRACK".equals(type))
            {
                return true;
            }
            if ("CANCEL".equals(type))
            {
                return true;
            }
            return false;
        }
        else
        {
            return true;
        }
    }

    /**
     *  Tell whether the request begins the transaction or not 
     * (= true by default) 
     */
    public boolean beginTransaction() throws Exception
    {
		String status = getType();
		if ("ACK".equals(status))
		{
			return false;
		}
        return true;
    }

    /**
     *  Tell whether the response ends the transaction or not 
     * (= true by default) 
     */
    public boolean endTransaction() throws Exception
    {
    	String method = getType();
    	if ("ACK".equals(method))
    	{
    		return true;
    	}
    	if ("PRACK".equals(method))
    	{
    		return true;
    	}
		String status = getResult();
		if (Utils.isInteger(status))
		{
			int statusCode = new Integer(status).intValue();
			if (statusCode >= 200)
			{
				return true;
			}
		}		
        return false;
    }

    /**
     *  Tell whether the message shall begin a new session 
     * (= false by default) 
     */
    @Override
    public boolean beginSession() throws Exception
    {
    	String status = getResult();
    	if (status != null && (!status.equals("")))
    	{
    		int statusCode = new Integer(status).intValue();
            if (statusCode >= 200 && statusCode < 300)
            {
            	return true;
            }        
	       else if ((statusCode >= 180) && (statusCode < 190) && (getParameter("header.RSeq").length() > 0))
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

    /**
     *  Tell whether the message shall end a session 
     * (= false by default) 
     */
    @Override
    public boolean endSession() throws Exception
    {
		String type = getType();
		if ("BYE".equals(type))
		{
		   return true;
		}
		Parameter param = getParameter("header.Expires");
    	if (param.length() <= 0)
    	{
    		return false;
    	}
    	String value = (String) param.get(0);
        if ("0".equals(value))
        {
        	return true;
        }
        return false;    
    }

}