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
import com.devoteam.srit.xmlloader.core.exception.GotoExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.Operation;
import com.devoteam.srit.xmlloader.core.utils.XMLElementDefaultParser;

import org.dom4j.Element;

/**
 * OperationGoto operation
 */
public class OperationGoto extends Operation {

    /**
     * Constructor
     *
     *
     * @param name Ope name
     * @param label OperationGoto label
     */
    public OperationGoto(Element root) {
        super(root, XMLElementDefaultParser.instance());
    }

    /**
     * Execute operation
     *
     * @param session Current session
     * @return Next operation or null by default
     * @throws ExecutionException
     */
    public Operation execute(Runner runner) throws Exception {
        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, this);

        String label;

        try {
            lockAndReplace(runner);
            GlobalLogger.instance().getSessionLogger().debug(runner, TextEvent.Topic.CORE, "Operation after pre-parsing \n", this);
            label = getAttribute("label");
        }
        finally {
            unlockAndRestore();
        }
        
        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "Branching to label ", label);
        throw new GotoExecutionException(label);
    }
}
