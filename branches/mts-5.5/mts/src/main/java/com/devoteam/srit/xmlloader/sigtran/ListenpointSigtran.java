package com.devoteam.srit.xmlloader.sigtran;


import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;

public class ListenpointSigtran extends Listenpoint {
	   
    public ListenpointSigtran(Stack stack) throws Exception
    {
        super(stack);
    }

	/** Creates a Listenpoint specific from XML tree*/
	public ListenpointSigtran(Stack stack, Element root) throws Exception	
	{
		super(stack, root);
	}
    	
    /** Send a Msg to Connection */
	@Override
	public boolean sendMessage(Msg msg, String remoteHost, int remotePort, String transport) throws Exception {
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
