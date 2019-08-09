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

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
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
             
    /** 
     * Creates a Msg specific to each Stack
     * Use for TCP/TLS like protocol : to build incoming message
     */
    @Override
    public byte[] readMessageFromStream(InputStream inputStream) throws Exception
    {
    	// read the header
        byte[] tab = new byte[20];
        int done = Utils.readFromSocketStream(inputStream, tab);

        // read the message payload       
        if (done >= 20)
        {

		    // get the length from the header
		    Array header = new DefaultArray(tab,0,done);
		    Array lengthArray = header.subArray(1,3);       
		    int length = 0;
		    length += (lengthArray.get(0) & 0xFF) << 16;
		    length += (lengthArray.get(1) & 0xFF) << 8;
		    length += lengthArray.get(2) & 0xFF;
		    length -= 20;
        
	        tab = new byte[length];
	        done = Utils.readFromSocketStream(inputStream, tab);
	        Array payload = new DefaultArray(tab,0,done);
	        
	        // concat the header and the payload
	        SupArray arrayMsg = new SupArray();
	        arrayMsg.addFirst(header);
	        arrayMsg.addLast(payload);
	               
	        return arrayMsg.getBytes();
        }
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
