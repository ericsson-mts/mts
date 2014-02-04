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
public class OperationExit extends Operation {

	String name = null;
    /**
     * Creates a new instance of OperationExit
     */
    public OperationExit(Element root) {
        super(root, XMLElementDefaultParser.instance());
        this.name = getAttribute("name");
        this._key[1] = this.name;
    }

    @Override
    public Operation execute(Runner runner) throws Exception {
        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, this);

        boolean failed;
        String exception;
        try {
            lockAndReplace(runner);
            GlobalLogger.instance().getSessionLogger().debug(runner, TextEvent.Topic.CORE, "Operation after pre-parsing \n", this);
            this.name = getAttribute("name");
            this._key[1] = this.name;
            failed = Boolean.parseBoolean(getAttribute("failed"));
            exception = getAttribute("exception");
            
        }
        finally {
            unlockAndRestore();
        }

        String loggMsg = "<exit ";
        
        // Replace elements in XMLTree
        if (failed) {
        	loggMsg += "FAILED ";
        }
        if (this.name != null) {
        	loggMsg += " name=\"" + this.name + "\"";
        }
        if (exception != null) {
        	loggMsg += " exception=\"" + exception + "\"";
        }
        loggMsg += "\\>";
        
        if (failed) {
        	GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, loggMsg);
            GlobalLogger.instance().getSessionLogger().error(runner, TextEvent.Topic.CORE, loggMsg);
        }
        else {
        	GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, loggMsg);
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, loggMsg);
        }
        throw new ExitExecutionException(failed, "Exit Exception");	
    }
}