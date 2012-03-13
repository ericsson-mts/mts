/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.mgcp;

import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import org.dom4j.Element;

/**
 *
 * @author indiaye
 */
public class ListenpointMgcp extends Listenpoint
{

	public ListenpointMgcp(Stack stack) throws Exception
	{
        super(stack);
    }

    /** Creates a Listenpoint specific from XML tree*/
	public ListenpointMgcp(Stack stack, Element root) throws Exception
	{
		super(stack, root);
	}

}
