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


class SCTPTest {
	public static final void main(String[] argv) throws Exception {
		
		
		OneToManySCTPSocket s = new OneToManySCTPSocket();
		int NbMsg = 3;
		
		if(true) {
			sctp_initmsg im = new sctp_initmsg();
			im.sinit_num_ostreams = 4;
			im.sinit_max_instreams = 7;
			s.setInitMsg(im);
		}
		s.bind(4000);
		sctp_event_subscribe ses = new sctp_event_subscribe();
		ses.sctp_data_io_event = true;
		ses.sctp_association_event = true;
		s.subscribeEvents(ses);
		s.listen();
		Collection<InetAddress> col = s.getLocalInetAddresses();
		System.out.println("Server is listening on:");
		for(InetAddress ia : col)
			System.out.println(ia.toString());
		
		//OneToManySCTPSocket c = new OneToManySCTPSocket();
		OneToOneSCTPSocket c = new OneToOneSCTPSocket();
		if(true) {
			sctp_initmsg im = new sctp_initmsg();
			im.sinit_num_ostreams = 3;
			im.sinit_max_instreams = 2;
			c.setInitMsg(im);
		}
		c.bind();
		
		c.connect(InetAddress.getByName("localhost"),4000);
		//s.connect(InetAddress.getByName("localhost"),4000); // ne marche pas
		//c.listen();		// ne marche pas
		
		//grab notification
		System.out.println("Doing receive() on server socket");
		SCTPChunk chunk = s.receive();
		assert ((SCTPNotificationAssociationChangeCommUp)chunk)!=null;
		AssociationId client_assoc_id = ((SCTPNotificationAssociationChangeCommUp)chunk).sac_assoc_id;
		System.out.println("Got SCTPNotificationAssociationChangeCommUp");
		System.out.println("client_assoc_id = " + client_assoc_id.toString());
		System.out.println("sac_outbound_streams = " + ((SCTPNotificationAssociationChangeCommUp)chunk).sac_outbound_streams);
		System.out.println("sac_inbound_streams = " + ((SCTPNotificationAssociationChangeCommUp)chunk).sac_inbound_streams);

		col = s.getPeerInetAddresses(client_assoc_id);
		System.out.println("Client is reachable via on port "+s.getPeerInetPort(client_assoc_id)+":");
		for(InetAddress ia : col)
			System.out.println(ia.toString());
		
		col = s.getLocalInetAddresses(client_assoc_id);
		System.out.println("...which can reach us via:");
		for(InetAddress ia : col)
			System.out.println(ia.toString());
		
		/*
		OneToOneSCTPSocket c2 = new OneToOneSCTPSocket();
		if(true) {
			sctp_initmsg im = new sctp_initmsg();
			im.sinit_num_ostreams = 3;
			im.sinit_max_instreams = 2;
			c2.setInitMsg(im);
		}
		c2.bind();
		c2.connect(InetAddress.getByName("localhost"),4000);
		
		//grab notification
		System.out.println("Doing receive() on server socket");
		chunk = s.receive();
		assert ((SCTPNotificationAssociationChangeCommUp)chunk)!=null;
		AssociationId client_assoc_id2 = ((SCTPNotificationAssociationChangeCommUp)chunk).sac_assoc_id;
		System.out.println("Got SCTPNotificationAssociationChangeCommUp");
		System.out.println("client_assoc_id = " + client_assoc_id2.toString());
		System.out.println("sac_outbound_streams = " + ((SCTPNotificationAssociationChangeCommUp)chunk).sac_outbound_streams);
		System.out.println("sac_inbound_streams = " + ((SCTPNotificationAssociationChangeCommUp)chunk).sac_inbound_streams);
*/
		/*
		String tmp="Hello world";
		for(int r=0; r<9000; r++) 
			tmp=tmp+"Hello world";
		System.out.println("data length= "+tmp.getBytes().length+" oct");
		*/

		
		System.out.println("Sending "+NbMsg+" messages Server->Client");
		s.setSctpNoDelay(true); //needed because we do not do request-reply
		for(int r=0; r<NbMsg; r++) {
			//send a chunk from server to client
			SCTPData data = new SCTPData("Hello world".getBytes());
			//SCTPData data = new SCTPData(tmp.getBytes());
			data.sndrcvinfo.sinfo_assoc_id = client_assoc_id;
			s.send(data);
			chunk = c.receive();
			assert ((SCTPData)chunk)!=null;
			assert (new String(((SCTPData)chunk).getData())).equals("Hello world");	
			
			//data.sndrcvinfo.sinfo_assoc_id = client_assoc_id2;
			//s.send(data);
			//chunk = c2.receive();
			//assert ((SCTPData)chunk)!=null;
			//assert (new String(((SCTPData)chunk).getData())).equals("Hello world");
		}
		
		System.out.println("Sending "+NbMsg+" messages Client->Server");
		c.setSctpNoDelay(true); //needed because we do not do request-reply
		for(int r=0; r<NbMsg; r++) {
			SCTPData data = new SCTPData("Hello to you".getBytes());
			c.send(data);
			chunk = s.receive();
			assert ((SCTPData)chunk)!=null;
			assert (new String(((SCTPData)chunk).getData())).equals("Hello to you");
			assert (((SCTPData)chunk).sndrcvinfo.sinfo_assoc_id.equals(client_assoc_id));
		}
		System.out.println("Done. Closing client socket");
		
		
		c.close();
		
		
		//grab notification
		chunk = s.receive();
		assert ((SCTPNotificationAssociationChangeCommLost)chunk)!=null;
		assert client_assoc_id == ((SCTPNotificationAssociationChangeCommLost)chunk).sac_assoc_id;
		
		//c2.close();
		s.close();
	}
}
