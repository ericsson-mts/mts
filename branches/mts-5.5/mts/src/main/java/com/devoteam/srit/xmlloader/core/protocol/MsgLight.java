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

package com.devoteam.srit.xmlloader.core.protocol;

import com.devoteam.srit.xmlloader.core.utils.expireshashmap.Removable;

/**
 * Generic message manipulated by XML Loader's core.<br/>
 * Should be inherited by protocol-specific messages.
 * @author fhenry
 */
public class MsgLight implements Removable
{
	
	protected String protocol;
    protected boolean isRequest;
    protected String type;
    protected String typeComplete;
    protected String result;
    protected String resultComplete;
    private long timestamp = 0;
    private int retransNumber = 0;
    private boolean send = false;
    
	public MsgLight()
	{
        timestamp = System.currentTimeMillis();
	}
	
	public MsgLight(Msg msg) throws Exception
	{
		this.protocol = msg.getProtocol();
		this.isRequest = msg.isRequest();
		this.type = msg.getType();
		this.typeComplete = msg.getTypeComplete();
		this.result = msg.getResult();
		this.resultComplete = msg.getResultComplete();
		this.timestamp = msg.getTimestamp();
		this.retransNumber = msg.getRetransNumber();
		this.send = msg.isSend();
	}
	
	public void onRemove() throws Exception
	{		  
	}

	public String getProtocol() {
		return protocol;
	}

	public boolean isRequest() throws Exception
	{
		return isRequest;
	}

	public String getType() throws Exception
	{
		return type;
	}

	public String getResult() throws Exception
	{
		return result;
	}

	public int getRetransNumber() throws Exception
	{
		return retransNumber;
	}

	public void setRetransNumber(int retransNumber) {
		this.retransNumber = retransNumber;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public boolean isSend() {
		return send;
	}
	
    public void setSend(boolean send) {
        this.send = send;
    }

	public String getSummary(boolean send, boolean prefix) throws Exception
	{
    	String ret = this.typeComplete;
    	if (isRequest)
    	{
			if (send)
			{
				ret += StackFactory.PREFIX_OUTGOING;
			}
			else
			{
				ret += StackFactory.PREFIX_INCOMING;
			}
    	}
    	else
    	{
			if (!send)
			{
				ret += StackFactory.PREFIX_OUTGOING;
			}
			else
			{
				ret += StackFactory.PREFIX_INCOMING;
			}
	    	ret += " / ";
	    	ret += this.resultComplete;
			if (send)
			{
				ret += StackFactory.PREFIX_OUTGOING;
			}
			else
			{
				ret += StackFactory.PREFIX_INCOMING;
			}
    	}
    	return ret;
	}

}

