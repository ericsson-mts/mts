/*
 * ListenpointMsrp.java
 *
 * Created on 22 octobre 2008, 11:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.msrp;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;

/**
 *
 * @author gpasquiers
 */
public class ListenpointMsrp extends Listenpoint
{   
    /** Creates a new instance of listenpoint */
    public ListenpointMsrp(Stack stack) throws Exception
    {
        super(stack);
    }
    
	/** Creates a Listenpoint specific from XML tree*/
	public ListenpointMsrp(Stack stack, Element root) throws Exception	
	{
		super(stack, root);
	}
    	
	/** Send a Msg to Channel*/
    @Override
    public synchronized boolean sendMessage(Msg msg, String remoteHost, int remotePort, String transport) throws Exception
    {
		if ((remoteHost == null) || (remotePort <= 0)) 
		{
            if((((MsgMsrp)msg).getMsgRemoteHost() != null) &&
                (((MsgMsrp)msg).getMsgRemotePort()) >= 0)
            {
                remoteHost = ((MsgMsrp)msg).getMsgRemoteHost();
                remotePort = ((MsgMsrp)msg).getMsgRemotePort();
            }
            else if(msg.getChannel() != null)
            {
                remoteHost = msg.getChannel().getRemoteHost();
                remotePort = msg.getChannel().getRemotePort();
            }
            else
            {
                throw new ExecutionException("Could not determine remote Host or remote Port");
            }
            msg.setRemoteHost(remoteHost);
            msg.setRemotePort(remotePort);
		}        

		return super.sendMessage(msg, remoteHost, remotePort, transport);
    }
        
}
