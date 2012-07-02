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

package com.devoteam.srit.xmlloader.core.operations.protocol;

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.Operation;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.XMLElementDefaultParser;

import org.dom4j.Element;

/**
 *
 * @author gpasquiers
 */
public class OperationCloseChannel extends Operation
{      
    private String protocol;
    
    /** Creates a new instance of ReceiveMsgOperation */
    public OperationCloseChannel(String protocol, Element rootElement) throws Exception
    {
        super(rootElement);

        this.protocol = protocol;
    }
    
    /** Executes the operation (retrieve and check message) */
    public Operation execute(Runner runner) throws Exception
    {
        restore();

        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.PROTOCOL, this);

        // Replace elements in XMLTree
        replace(runner, new XMLElementDefaultParser(runner.getParameterPool()), TextEvent.Topic.PROTOCOL);


        Element root = getRootElement();
        
        // instanciates the channel
        
        String name = root.attributeValue("connectionName");

        if(null == name)
        {
        	name = root.attributeValue("sessionName");
        }

        if(null == name)
        {
        	name = root.attributeValue("providerName");
        }
        
        if(null == name)
        {
        	name = root.attributeValue("socketName");
        }
                
        if(null == name)
        {
        	name = root.attributeValue("name");
        }

    	GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CALLFLOW, ">>>CLOSE ", protocol, " channel <name = \"", name, "\">");	        	
        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CALLFLOW, ">>>CLOSE ", protocol, " channel <name = \"", name, "\">");
        
        // close the channel
        StackFactory.getStack(protocol).closeChannel(name);       
        
        return null;
    }
    
}
