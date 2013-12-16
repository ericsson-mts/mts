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
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.Operation;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.XMLElementDefaultParser;

import org.dom4j.Element;

/**
 * TODO : statistics; request or answer ;
 *
 * @author gpasquiers
 */
public class OperationOpenChannel extends Operation {

    private String protocol;

    /**
     * Creates a new instance
     */
    public OperationOpenChannel(String aProtocol, Element rootNode) throws Exception {
        super(rootNode, XMLElementDefaultParser.instance());
        protocol = aProtocol;
    }

    /**
     * Executes the operation
     */
    public Operation execute(Runner runner) throws Exception {
        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.PROTOCOL, this);
        
        Channel channel;
        try {
            lockAndReplace(runner);
            GlobalLogger.instance().getSessionLogger().debug(runner, TextEvent.Topic.PROTOCOL, "Operation after pre-parsing \n", this);
            channel = StackFactory.getStack(protocol).parseChannelFromXml(getRootElement(), protocol);
        }
        finally {
            unlockAndRestore();
        }

        //
        // Get if she does not already exists and compare to the existing one
        //
        synchronized (StackFactory.getStack(protocol)) {

            Channel oldChannel = StackFactory.getStack(protocol).getChannel(channel.getName());
            if ((oldChannel != null) && (!channel.equals(oldChannel))) {
                throw new ExecutionException("A channel <name=" + channel.getName() + "> already exists with other attributes.");
            }

            if (oldChannel == null) {
                StackFactory.getStack(protocol).openChannel(channel);
            	GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CALLFLOW, ">>>OPEN ", protocol, " channel <", channel, ">");
            	GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CALLFLOW, ">>>OPEN ", protocol, " channel <", channel, ">");
            }
        }

        return null;
    }
}
