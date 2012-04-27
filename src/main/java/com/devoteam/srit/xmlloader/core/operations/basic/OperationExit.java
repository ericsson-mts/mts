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

package com.devoteam.srit.xmlloader.core.operations.basic;

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ExitExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.Operation;
import com.devoteam.srit.xmlloader.core.utils.XMLElementDefaultParser;

import org.dom4j.Element;

/**
 *
 * @author gpasquiers
 */
public class OperationExit extends Operation
{
    
    /**
     * Creates a new instance of OperationExit
     */
    public OperationExit(Element root)
    {
        super(root);
    }
    
    public Operation execute(Runner runner) throws Exception
    {
        restore();

        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, this);

        // Replace elements in XMLTree
        replace(runner, new XMLElementDefaultParser(runner.getParameterPool()), TextEvent.Topic.CORE);

        boolean failed = Boolean.parseBoolean(getAttribute("failed"));
        
        if (failed) 
        {
        	GlobalLogger.instance().getSessionLogger().error(runner, TextEvent.Topic.CORE, "Exit failed = ", failed);
        } else 
        {
        	GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "Exit failed = ", failed);
        }
        throw new ExitExecutionException(failed, "Exit Failed Exception");
    }
    
}