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


class SCTPTest2 {
	@SuppressWarnings("deprecation")
	public static final void main(String[] argv) throws Exception {
		//System.setProperty("java.library.path","/home/xmlloader/workspace/xmlSipLoader/lib/libdk_i1_sctp.so");
		//System.load("/home/xmlloader/workspace/xmlSipLoader/lib/libdk_i1_sctp.so");
		//System.loadLibrary("dk_i1_sctp");
		OneToOneSCTPSocket s = new OneToOneSCTPSocket();
		//s.bind(4000);
		s.bind(InetAddress.getByName("127.0.0.1"),4000);
		//s.bind(InetAddress.getByName("192.168.0.100"),4000);
		//SocketAddress sa=null;
		//s.bind(((InetSocketAddress)sa).getAddress(),0);	
		sctp_event_subscribe ses = new sctp_event_subscribe();
		ses.sctp_data_io_event = true;
		ses.sctp_association_event = true;
		s.subscribeEvents(ses);
		s.listen();
		Collection<InetAddress> col = s.getLocalInetAddresses();
		System.out.println("Server is listening on:");
		for(InetAddress ia : col)
			System.out.println(ia.toString());
				
		OneToOneSCTPSocket c1 = new OneToOneSCTPSocket();
		c1.bind();
		//c1.bind(4001);
		sctp_initmsg im = new sctp_initmsg();
		im.sinit_num_ostreams = 4;
		im.sinit_max_instreams = 7;
		c1.setInitMsg(im);
		c1.connect(InetAddress.getByName("localhost"),4000);
		//c.connect(InetAddress.getByName("192.168.0.100"),4000);
		System.out.println("Doing accept() on server socket");
		OneToOneSCTPSocket s1 = s.accept();
		im.sinit_num_ostreams = 2;
		im.sinit_max_instreams = 3;
		c1.setInitMsg(im);
		//c1.listen();
		
		/*
		OneToOneSCTPSocket c2 = new OneToOneSCTPSocket();
		c2.bind();
		c2.connect(InetAddress.getByName("localhost"),4000);
		System.out.println("Doing accept() on server socket");
		OneToOneSCTPSocket s2 = s.accept();
		
		
		OneToOneSCTPSocket c3 = new OneToOneSCTPSocket();
		c3.bind();
		c3.connect(InetAddress.getByName("localhost"),4001);
		System.out.println("Doing accept() on server socket");
		OneToOneSCTPSocket s3 = c1.accept();
		*/
		
		System.out.println("Sending 10 messages Server->Client");
		for(int r=0; r<10; r++) {
			//send a chunk from server to client
			SCTPData data = new SCTPData("Hello world".getBytes());
			s1.send(data);
			SCTPChunk chunk = c1.receive();
			assert ((SCTPData)chunk)!=null;
			assert (new String(((SCTPData)chunk).getData())).equals("Hello world");
			//s2.send(data);
			//SCTPChunk chunk4 = c2.receive();
			//assert ((SCTPData)chunk4)!=null;
			//assert (new String(((SCTPData)chunk4).getData())).equals("Hello world");
		}
		/*System.out.println("Sending 1000 messages Client->Server");
		for(int r=0; r<10; r++) {
			SCTPData data = new SCTPData("Hello to you".getBytes());
			c.send(data);
			SCTPChunk chunk = c2.receive();
			assert ((SCTPData)chunk)!=null;
			assert (new String(((SCTPData)chunk).getData())).equals("Hello to you");
		}*/
		System.out.println("Done. Closing client socket");
		
		c1.close();
		
		//c2.close();
		s1.close();
		
		//s2.close();
		s.close();
	}
}
