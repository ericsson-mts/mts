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
