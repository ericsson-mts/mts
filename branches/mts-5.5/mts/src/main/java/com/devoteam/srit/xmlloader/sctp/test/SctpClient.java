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

package com.devoteam.srit.xmlloader.sctp.test;

import dk.i1.sctp.*;

import java.net.*;
import java.util.*;


class SctpClient extends Thread{

	public OneToOneSCTPSocket c=null;

	public void run() {
		try
		{
			c = new OneToOneSCTPSocket();
			if(true) {
				sctp_initmsg im = new sctp_initmsg();
				im.sinit_num_ostreams = 4;
				im.sinit_max_instreams = 3;
				c.setInitMsg(im);
			}
			c.bind();
			c.connect(InetAddress.getByName("localhost"),4000);

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

	}
	public void close() {
		try{
			System.out.println("Done. Closing client socket");
			c.close();	

		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
