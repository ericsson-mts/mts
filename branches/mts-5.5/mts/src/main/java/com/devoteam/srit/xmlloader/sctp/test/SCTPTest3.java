//Simple test program for OneToMany-style sockets

package com.devoteam.srit.xmlloader.sctp.test;

import dk.i1.sctp.*;
import java.net.*;
import java.util.*;


class SCTPTest3 {
	public static void main(String[] argv) throws Exception {
		
		SctpServer server = new SctpServer();
		server.setDaemon(true);
		server.start();
		
		Thread.sleep(100);
		
		SctpClient client = new SctpClient();
		client.setDaemon(true);
		client.start();
		
		Thread.sleep(100);
		
		SCTPChunk chunk = null;

		
		System.out.println("Sending 100 messages Server->Client");
		server.s.setSctpNoDelay(true); //needed because we do not do request-reply
		for(int r=0; r<10; r++) {
			//send a chunk from server to client
			SCTPData data = new SCTPData(("Hello world"+r).getBytes());
			data.sndrcvinfo.sinfo_assoc_id = server.client_assoc_id;
			data.sndrcvinfo.sinfo_stream = 1;
			server.s.send(data);
			chunk = client.c.receive();
			assert ((SCTPData)chunk)!=null;
			assert (new String(((SCTPData)chunk).getData())).equals("Hello world"+r);
		}
		
		for(int r=0; r<10; r++) {
			//send a chunk from server to client
			SCTPData data = new SCTPData(("Hello world"+r).getBytes());
			data.sndrcvinfo.sinfo_assoc_id = server.client_assoc_id;
			data.sndrcvinfo.sinfo_stream = 2;
			server.s.send(data);
			chunk = client.c.receive();
			assert ((SCTPData)chunk)!=null;
			assert (new String(((SCTPData)chunk).getData())).equals("Hello world"+r);
		}
		
		
		Thread.sleep(100);
	/*	
		System.out.println("Sending 100 messages Client->Server");
		client.c.setSctpNoDelay(true); //needed because we do not do request-reply
		for(int r=0; r<100; r++) {
			SCTPData data = new SCTPData(("Hello to you"+r).getBytes());
			client.c.send(data);
			chunk = server.s.receive();
			assert ((SCTPData)chunk)!=null;
			assert (new String(((SCTPData)chunk).getData())).equals("Hello to you"+r);
			assert (((SCTPData)chunk).sndrcvinfo.sinfo_assoc_id.equals(server.client_assoc_id));
		}
		*/
		
		Thread.sleep(100);
		
		client.close();
		server.close();
		
	}
}
