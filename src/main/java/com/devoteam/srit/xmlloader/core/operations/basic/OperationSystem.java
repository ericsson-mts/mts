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
import java.io.IOException;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.Operation;
import com.devoteam.srit.xmlloader.core.utils.InputStreamConsumer;
import com.devoteam.srit.xmlloader.core.utils.XMLElementDefaultParser;
import java.io.InputStream;

import java.util.concurrent.Semaphore;
import org.dom4j.Element;

/**
 * @author ma007141
 */
public class OperationSystem extends Operation {

    public OperationSystem(Element root) {
        super(root, XMLElementDefaultParser.instance());
    }

    /**
     * Execute operation
     *
     * @param session Current session
     * @return Next operation or null by default
     * @throws IOException
     */
    public Operation execute(Runner runner) throws Exception {
        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, this);

        String command;


        try {
            lockAndReplace(runner);
            GlobalLogger.instance().getSessionLogger().debug(runner, TextEvent.Topic.CORE, "Operation after pre-parsing \n", this);
            command = this.getAttribute("command");
        }
        finally {
            unlockAndRestore();
        }

        // Replace elements in XMLTree
        GlobalLogger.instance().logDeprecatedMessage(
                "system command=\"xxx\" ... \"/",
                "parameter ... operation=\"" + "system.command" + "\" value=\"xxx\"/");

        try {
            Process p = Runtime.getRuntime().exec(command);

            InputStreamConsumer stdInputStreamConsumer = new InputStreamConsumer(p.getInputStream());
            InputStreamConsumer errInputStreamConsumer = new InputStreamConsumer(p.getErrorStream());

            stdInputStreamConsumer.acquire();
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, command, " (standard output):\n", stdInputStreamConsumer.getContents());
            errInputStreamConsumer.acquire();
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, command, " (error output):\n", errInputStreamConsumer.getContents());
            p.waitFor();
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, command, " system process ended");

            if (0 != p.exitValue()) {
                GlobalLogger.instance().getSessionLogger().error(runner, TextEvent.Topic.CORE, command, "Return value not null \n");
                throw new ExecutionException("Error , return value of System command : " + command + "=" + p.exitValue());
            }
        }
        catch (Exception e) {
            GlobalLogger.instance().getSessionLogger().error(runner, TextEvent.Topic.CORE, command, "Exception occured\n", e);
            throw new ExecutionException("Error executing system operation command", e);
        }

        return null;
    }
}
