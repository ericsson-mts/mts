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
import com.devoteam.srit.xmlloader.core.utils.XMLElementTextMsgParser;
import org.dom4j.Element;

/**
 *
 * @author gpasquiers
 */
public class OperationLog extends Operation {

    /**
     * Creates a new instance of OperationExit
     */
    public OperationLog(Element root) {
        super(root, XMLElementTextMsgParser.instance());
    }

    public Operation execute(Runner runner) throws Exception {
        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, this);


        String level;
        String type;
        String logStr;

        try {
            lockAndReplace(runner);
            GlobalLogger.instance().getSessionLogger().debug(runner, TextEvent.Topic.CORE, "Operation after pre-parsing \n", this);
            level = this.getAttribute("level");
            type = this.getAttribute("type");
            logStr = this.getRootElement().getText().trim();
        }
        finally {
            unlockAndRestore();
        }

        // Replace elements in XMLTree
        if (null == level) {
            level = "INFO";
        }
        int intLevel = -1;
        if (Utils.isInteger(level)) {
            intLevel = 3 - Integer.parseInt(level);
        }

        if (null == type) {
            type = "Scenario";
        }

        if ("Main".equalsIgnoreCase(type)) {
            if ((TextEvent.DEBUG_STRING.equalsIgnoreCase(level)) || (intLevel == TextEvent.DEBUG)) {
                GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.USER, logStr);
            }
            else if ((TextEvent.INFO_STRING.equalsIgnoreCase(level)) || (intLevel == TextEvent.INFO)) {
                GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.USER, logStr);
            }
            else if ((TextEvent.WARN_STRING.equalsIgnoreCase(level)) || (intLevel == TextEvent.WARN)) {
                GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.USER, logStr);
            }
            else if ((TextEvent.ERROR_STRING.equalsIgnoreCase(level)) || (intLevel == TextEvent.ERROR)) {
                GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.USER, logStr);
            }
            else {
                throw new ExecutionException("Level attribute should be an integer from [0-3] or a string from the list {DEBUG, INFO, WARN, ERROR}");
            }
        }
        else if ("Scenario".equalsIgnoreCase(type)) {
            if ((TextEvent.DEBUG_STRING.equalsIgnoreCase(level)) || (intLevel == TextEvent.DEBUG)) {
                GlobalLogger.instance().getSessionLogger().debug(runner, TextEvent.Topic.USER, logStr);
            }
            else if ((TextEvent.INFO_STRING.equalsIgnoreCase(level)) || (intLevel == TextEvent.INFO)) {
                GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.USER, logStr);
            }
            else if ((TextEvent.WARN_STRING.equalsIgnoreCase(level)) || (intLevel == TextEvent.WARN)) {
                GlobalLogger.instance().getSessionLogger().warn(runner, TextEvent.Topic.USER, logStr);
            }
            else if ((TextEvent.ERROR_STRING.equalsIgnoreCase(level)) || (intLevel == TextEvent.ERROR)) {
                GlobalLogger.instance().getSessionLogger().error(runner, TextEvent.Topic.USER, logStr);
            }
            else {
                throw new ExecutionException("Level attribute should be an integer from [0-3] or a string from the list {DEBUG, INFO, WARN, ERROR}");
            }
        }
        else {
            throw new ExecutionException("Type attribute should be a string from the list {Main, Scenario}");
        }

        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "Write in ", type, "log with the level = ", level, " the message  ", logStr);

        return null;
    }
}