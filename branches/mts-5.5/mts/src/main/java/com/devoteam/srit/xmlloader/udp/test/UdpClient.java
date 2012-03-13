/*
 * UdpClient.java
 *
 * Created on 10 January 2008, 15:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.udp.test;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.Inet4Address;
/**
 *
 * @author nghezzaz
 */
public class UdpClient extends Thread
{

	public void run()
	{
		try
		{
			DatagramSocket datagramSocket = new DatagramSocket(0/*(int) UdpTest.SERVER_PORT + 1*/, Inet4Address.getByName(UdpTest.SERVER_HOST));
			//DatagramSocket datagramSocket = new DatagramSocket(null); // null->unbound
			System.out.println("UdpClient: client ready");

			boolean connect = false; // true ; "connecté" ou non 

			byte[] data = new byte[(int)UdpTest.MSG_SIZE];

			for(int i=0; i<data.length; i++)
			{
				data[i] = (byte)i;// 0;
			}

			DatagramPacket datagramPacket = new DatagramPacket(data,data.length,Inet4Address.getByName(UdpTest.SERVER_HOST),(int) UdpTest.SERVER_PORT); 
			//DatagramPacket datagramPacket = new DatagramPacket(data,data.length,Inet4Address.getByName(UdpTest.SERVER_HOST),3333);
			//DatagramPacket datagramPacket = new DatagramPacket(data,data.length);
			
			System.out.println("UdpClient: data initialized");
			
			if (connect){
				datagramSocket.connect(Inet4Address.getByName(UdpTest.SERVER_HOST),(int) UdpTest.SERVER_PORT);	
				if (datagramSocket.isConnected()) System.out.println("UdpClient: datagramSocket connected");
			}
			
			UdpTest.start = System.currentTimeMillis();
						
			//for(int i=0; i<UdpTest.MSG_NUMBER; i++)
			{

				//System.out.println( "sending: " + i);

				//for(int j=0; j<data.length; j++)
				//	System.out.print(datagramPacket.getData()[j]+", ");
				//System.out.println( "");

				System.out.println("client: localport :"+datagramSocket.getLocalPort());
				datagramSocket.send(datagramPacket);	// le send
				System.out.println("client: localport :"+datagramSocket.getLocalPort());

				if(datagramPacket.getLength() != UdpTest.MSG_SIZE)
					System.out.println(datagramPacket.getLength() + " != " +UdpTest.MSG_SIZE);

				UdpTest.total_sent ++;
			}
			
			datagramSocket.receive(datagramPacket);
			
			System.out.println("client: portsource paquet recu :"+ datagramPacket.getPort());

			UdpTest.end = System.currentTimeMillis();
	

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}


	}
}
