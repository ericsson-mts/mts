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
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.Operation;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLElementDefaultParser;


import org.dom4j.Element;

/**
 * OperationPause operation
 * 
 * 
 * @author JM. Auffret
 */
public class OperationPause extends Operation
{
    /**
     * Constructor
     * 
     * 
     * @param name Name of the operation
     * @param pause OperationPause value
     */
    public OperationPause(Element root)
    {
        super(root, XMLElementDefaultParser.instance());
    }
    
    /**
     * Execute operation
     *
     * @param session Current session
     * @return Next operation or null by default
     */
    public Operation execute(Runner runner) throws Exception
    {
        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, this);                

        String seconds;
        String milliseconds;

        try {
            lockAndReplace(runner);
            GlobalLogger.instance().getSessionLogger().debug(runner, TextEvent.Topic.CORE, "Operation after pre-parsing \n", this);
            seconds = getAttribute("seconds");
            milliseconds= getAttribute("milliseconds");
        }
        finally {
            unlockAndRestore();
        }
        
        //
        // Read attribute
        //
        float pause ;

        if(null != seconds)
        {
            pause = Float.parseFloat(seconds);
            pause *= 1000;
            
        }
        else if(null != milliseconds)
        {
            pause = Float.parseFloat(milliseconds);
        }
        else
        {
            throw new ExecutionException("Duration not specified for operation "  + this);
        }

        //
        // Do pause
        //
        if (pause >= 0) 
        {
        	Utils.pauseMilliseconds((long) pause);
        }
        else
        {
            GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "Operation Pause : negative value. We ignore it.");        	
        }
        
        
        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "Pause of ", (float) pause / 1000, "sec");
        
        return null;
    }
}
