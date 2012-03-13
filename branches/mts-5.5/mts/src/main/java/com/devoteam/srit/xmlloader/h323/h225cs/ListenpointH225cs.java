/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.h323.h225cs;

import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import org.dom4j.Element;

/**
 *
 * @author indiaye
 */
public class ListenpointH225cs extends Listenpoint
{

    ListenpointH225cs(Stack stack) throws Exception 
    {
       super(stack);
    }
    
    public ListenpointH225cs(Stack stack, Element root) throws Exception
	{
		super(stack, root);
	}

}
