package com.devoteam.srit.xmlloader.sniffer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Enumeration;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.PacketReceiver;
import jpcap.packet.Packet;

/**
	http://netresearch.ics.uci.edu/kfujii/jpcap/doc/download.html
*/


public class sniffer {
	
	public static void main(String[] args) 
	{
		//Obtain the list of network interfaces
		NetworkInterface[] devices = JpcapCaptor.getDeviceList();
		PacketPrinter printer = new PacketPrinter();
		int deviceNumber = trouve();
		String filter = "(src host 172.16.21.32) and (dst host 172.16.21.32)";
		
		try 
		{
			// JpcapCaptor captor = JpcapCaptor.openDevice(devices[deviceNumber], 65535, true, 0);
			JpcapCaptor captor = JpcapCaptor.openDevice(devices[deviceNumber], 65535, true, 0);
			captor.setFilter(filter, true);

			while (true) 
			{
				// Packet packet = captor.getPacket();
				//just print out a captured packet
				// System.out.println("********************************************************************************");
				// System.out.println(packet);
				// String strData = new String(packet.data);
				// System.out.println(strData);

				captor.processPacket(1,(printer));
				// Thread.yield();
				//System.out.println("woot");
			}
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static int trouve() 
	{
		//Obtain the list of network interfaces
		NetworkInterface[] devices = JpcapCaptor.getDeviceList();
		int i=0;
		InetAddress address = null;
		try 
		{
			for (Enumeration e = java.net.NetworkInterface.getNetworkInterfaces(); e.hasMoreElements();) 
			{
				java.net.NetworkInterface eth = (java.net.NetworkInterface) e.nextElement();
				if (null == "eth0" || eth.getName().equals("eth0")) 
				{
					for (Enumeration addr = ((java.net.NetworkInterface) eth).getInetAddresses(); addr.hasMoreElements();) 
					{
						address = (InetAddress) addr.nextElement();
					}
				}
			}
		} 
		catch (SocketException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int j=1; j<devices.length; j++) 
		{
			if (devices[j].addresses[0].address.equals(address)) 
			{
				i=j;
				j=devices.length;
			}
		}
		return i;
	}
}

class PacketPrinter implements PacketReceiver 
{	
	//this method is called every time Jpcap captures a packet
	public void receivePacket(Packet packet) 
	{
		//just print out a captured packet
		System.out.println("********************************************************************************");
		System.out.println(packet);
		String strData = new String(packet.data);
		System.out.println(strData);
	}
}

