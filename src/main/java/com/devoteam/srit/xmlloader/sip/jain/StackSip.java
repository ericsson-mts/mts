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
package com.devoteam.srit.xmlloader.sip.jain;

import java.io.InputStream;

import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.sip.StackSipCommon;
import gov.nist.javax.sip.parser.StringMsgParser;

/**
 *
 * @author gpasquiers
 */
public class StackSip extends StackSipCommon {

    /**
     * Constructor
     */
    public StackSip() throws Exception {
        super();
        StringMsgParser.setComputeContentLengthFromMessage(true);
    }

    /**
     * Receive a message
     */
    @Override
    public boolean receiveMessage(Msg msg) throws Exception {
        ((MsgSip) msg).completeViaTopmostHeader();
        return super.receiveMessage(msg);
    }

    /**
     * Read the message data from the stream Use for TCP/TLS like protocol : to
     * build incoming message
     */
    @Override
    public byte[] readMessageFromStream(InputStream inputStream) throws Exception {
        String text = null;
        synchronized (inputStream) {
            text = this.reader(inputStream);
        }
        return text.getBytes();
    }
}
