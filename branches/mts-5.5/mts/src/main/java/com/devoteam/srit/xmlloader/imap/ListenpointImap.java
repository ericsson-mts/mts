/*
 * ListenpointImap.java
 *
 * Created on 22 octobre 2008, 11:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.imap;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;

/**
 *
 * @author gpasquiers
 */
public class ListenpointImap extends Listenpoint
{       
    /** Creates a new instance of listenpoint */
    public ListenpointImap(Stack stack) throws Exception
    {
        super(stack);
    }

	/** Creates a Listenpoint specific from XML tree*/
	public ListenpointImap(Stack stack, Element root) throws Exception	
	{
		super(stack, root);
	}
    	
    /** Send a Msg to Connection */
    @Override
    public synchronized boolean sendMessage(Msg msg, String remoteHost, int remotePort, String transport) throws Exception
    {
        throw new RuntimeException("Not supported in IMAP");
    }
}
