/*
 * UdpServer.java
 *
 * Created on 11 January 2008, 09:31
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
public class UdpServer extends Thread
{
	public void run()
	{  
		
		try
		{
			DatagramSocket datagramSocket = new DatagramSocket((int) UdpTest.SERVER_PORT, Inet4Address.getByName(UdpTest.SERVER_HOST));
			System.out.println("UdpServer: server waiting");
			boolean connect = false;
			if (connect){
				datagramSocket.connect(Inet4Address.getByName(UdpTest.SERVER_HOST),(int) UdpTest.SERVER_PORT + 1);	
				if (datagramSocket.isConnected()) System.out.println("UdpServer: datagramSocket connected");
			}
			
			//int i = 1;
			//while(true)
			
			int portEmission;
			{
	
				byte[] data = new byte[(int)UdpTest.MSG_SIZE]; 
				DatagramPacket datagramPacket = new DatagramPacket(data,(int)UdpTest.MSG_SIZE);
				datagramSocket.receive(datagramPacket);
				System.out.println("server: portsource paquet recu :"+ datagramPacket.getPort());
				portEmission = datagramPacket.getPort();
				UdpTest.total_received++;
				//System.out.println( "receiving: " + i++);
				//for(int j=0; j<data.length; j++)
				//	System.out.print(datagramPacket.getData()[j]+", ");
				//System.out.println( ""); 
				
			}

			byte[] data = new byte[(int)UdpTest.MSG_SIZE]; 
			DatagramPacket datagramPacket2 = new DatagramPacket(data,data.length,Inet4Address.getByName(UdpTest.SERVER_HOST),portEmission);
			datagramSocket.send(datagramPacket2);	// le send
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}
