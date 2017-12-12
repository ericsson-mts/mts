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
