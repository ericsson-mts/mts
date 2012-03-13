//Simple test program for OneToMany-style sockets

package com.devoteam.srit.xmlloader.sctp.test;

import dk.i1.sctp.*;
import java.net.*;
import java.util.*;


class SctpServer extends Thread{

	public OneToManySCTPSocket s =null;
	public AssociationId client_assoc_id;
	
	public void run() {
		try
		{
			s = new OneToManySCTPSocket();
			if(true) {
				sctp_initmsg im = new sctp_initmsg();
				im.sinit_num_ostreams = 5;
				im.sinit_max_instreams = 8;
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


			System.out.println("Doing receive() on server socket");
			SCTPChunk chunk = null;

			//grab notification
			while(chunk==null)
				chunk=s.receive();

			assert ((SCTPNotificationAssociationChangeCommUp)chunk)!=null;
			client_assoc_id = ((SCTPNotificationAssociationChangeCommUp)chunk).sac_assoc_id;
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


		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

	}

	public void close() {

		SCTPChunk chunk = null;
		try{
			//grab notification
			while(chunk==null){
				chunk=s.receive();
			}
			assert ((SCTPNotificationAssociationChangeCommLost)chunk)!=null;
			assert client_assoc_id == ((SCTPNotificationAssociationChangeCommLost)chunk).sac_assoc_id;
			System.out.println("Got SCTPNotificationAssociationChangeCommLost");
			System.out.println("Done. Closing server socket");
			s.close();	

		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
