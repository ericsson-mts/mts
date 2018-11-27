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

package com.devoteam.srit.xmlloader.http2;

import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;

import org.apache.hc.core5.http.HttpMessage;


/**
 *
 * @author gpasquiers
 */
public class MsgHttp2 extends Msg
{
    
    /** Creates a new instance */
    public MsgHttp2(Stack stack) 
    {
        super(stack);       
    }

    /** Creates a new instance */
    public MsgHttp2(Stack stack, HttpMessage aMessage) throws Exception
    {
        this(stack);
    }

	@Override
	public boolean isRequest() throws Exception {
		System.out.println("MsgHttp2.isRequest()");
		return true;
	}

	@Override
	public String getResult() throws Exception {
		System.out.println("MsgHttp2.getResult()");
		return "mon resultat";
	}

	@Override
	public void decode(byte[] data) throws Exception {
		System.out.println("MsgHttp2.decode()");
	}

	@Override
	public String toXml() throws Exception {
		System.out.println("MsgHttp2.toXml()");
		return "mon message";
	}

	@Override
	public byte[] encode() throws Exception {
		System.out.println("MsgHttp2.encode()");
		return null;
	}

}
