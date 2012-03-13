/*
 * ListenpointRtsp.java
 *
 * Created on 22 octobre 2008, 11:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.rtsp;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;

/**
 *
 * @author gpasquiers
 */
public class ListenpointRtsp extends Listenpoint
{   
    /** Creates a new instance of listenpoint */
    public ListenpointRtsp(Stack stack) throws Exception
    {
        super(stack);
    }
    
	/** Creates a Listenpoint specific from XML tree*/
	public ListenpointRtsp(Stack stack, Element root) throws Exception	
	{
		super(stack, root);
	}
    	
	/** Send a Msg to Channel*/
    @Override
    public synchronized boolean sendMessage(Msg msg, String remoteHost, int remotePort, String transport) throws Exception
    {
		if ((remoteHost == null) || (remotePort <= 0)) 
		{
            if((((MsgRtsp)msg).getRemoteHost() != null) &&
                (((MsgRtsp)msg).getRemotePort()) >= 0)
            {
                remoteHost = ((MsgRtsp)msg).getRemoteHost();
                remotePort = ((MsgRtsp)msg).getRemotePort();
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
