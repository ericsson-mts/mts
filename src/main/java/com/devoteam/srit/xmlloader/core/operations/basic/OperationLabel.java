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
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.Operation;

import com.devoteam.srit.xmlloader.core.utils.XMLElementDefaultParser;
import org.dom4j.Element;

/**
 *
 * @author gpasquiers
 */
public class OperationLabel extends Operation {

    private String name;

    /**
     * Creates a new instance of OperationLabel
     */
    public OperationLabel(Element root) {
        super(root, XMLElementDefaultParser.instance());
        this.name = null;;
    }

    public Operation execute(Runner runner) throws Exception {
        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, this);

        try {
            lockAndReplace(runner);
            GlobalLogger.instance().getSessionLogger().debug(runner, TextEvent.Topic.CORE, "Operation after pre-parsing \n", this);
            this.name = getAttribute("name");
        }
        finally {
            unlockAndRestore();
        }

        return null;
    }

    public String getLabelName(Runner runner) throws Exception {
        if (null == this.name) {
            try {
                lockAndReplace(runner);
                return this.getAttribute("name");
            }
            finally {
                unlockAndRestore();
            }
        }
        return this.name;
    }
}
