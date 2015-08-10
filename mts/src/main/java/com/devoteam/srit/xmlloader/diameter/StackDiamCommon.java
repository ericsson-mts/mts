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

package com.devoteam.srit.xmlloader.diameter;

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLElementAVPParser;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;

import dk.i1.diameter.node.Node;
import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.SupArray;

import java.io.InputStream;
import java.util.logging.FileHandler;
import java.util.logging.Level;

import org.dom4j.Element;

/**
 *
 * @author gpasquiers
 */
public class StackDiamCommon extends Stack 
{
    
    public static Listenpoint listenpoint = null;
    
    
    /** Creates a new instance */
    public StackDiamCommon() throws Exception 
    {
        super();
        
        // configure stack trace parameters
        FileHandler fh = new FileHandler("../logs/diameterStack.log");
        // logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        Node.logger.addHandler(fh);
        String stringLevel = getConfig().getString("TRACE_LEVEL");
        Level traceLevel = Level.parse(stringLevel);
        Node.logger.setLevel(traceLevel);
        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "traceLevel : ", traceLevel);
        Node.logger.warning("traceLevel = " + traceLevel);
    }
        
    /** Creates a channel specific to each Stack */
    @Override
    public Channel parseChannelFromXml(Element root, Runner runner, String protocol) throws Exception
    {
        return null;
    }
    
    /** 
     * Returns the XML Element Replacer to replace the "[parameter]" string 
     * in the XML document by the parameter values.
     * By Default it is a generic replacer for text protocol : it duplicates 
     * the current line for each value of the parameter 
     */
    @Override
    public XMLElementReplacer getElementReplacer() 
    {
        return XMLElementAVPParser.instance();
    }
    
}
