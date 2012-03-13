//Simple test program for OneToMany-style sockets

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
