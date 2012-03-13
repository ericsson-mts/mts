/*
 * Msg.java
 *
 * Created on 6 avril 2007, 16:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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

