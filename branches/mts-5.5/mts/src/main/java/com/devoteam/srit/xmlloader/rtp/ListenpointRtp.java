/*
 * ListenpointRtp.java
 *
 */

package com.devoteam.srit.xmlloader.rtp;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;

/**
 * @author bbouvier
 */
public class ListenpointRtp extends Listenpoint
{    
	
    /** Creates a new instance of Listenpoint */
    public ListenpointRtp(Stack stack) throws Exception
    {
        super(stack);
    }
    
	/** Creates a Listenpoint specific from XML tree*/
	public ListenpointRtp(Stack stack, Element root) throws Exception	
	{
		super(stack, root);
	}
	
    /** Creates a new instance of Listenpoint */
    public ListenpointRtp(Stack stack, String name, String host, int port) throws Exception
    {
    	super(stack, name, host, port);
    }

    /** Send a Msg to Listenpoint */
    @Override
    public synchronized boolean sendMessage(Msg msg, String remoteHost, int remotePort, String transport) throws Exception
    {
		if ((remoteHost == null) || (remotePort <= 0)) 
		{
            if(msg.getChannel() != null)
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
