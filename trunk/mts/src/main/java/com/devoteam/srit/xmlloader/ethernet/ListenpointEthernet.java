package com.devoteam.srit.xmlloader.ethernet;

import java.io.IOException;
import java.net.InetAddress;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import jpcap.JpcapCaptor;
import jpcap.JpcapSender;
import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;
import jpcap.PacketReceiver;
import jpcap.packet.ARPPacket;
import jpcap.packet.EthernetPacket;
import jpcap.packet.Packet;

import java.net.Inet4Address;
import java.util.Arrays;

public class ListenpointEthernet extends Listenpoint {
	
	public ListenpointEthernet(Stack stack) throws Exception {
		super(stack);
	}
	
	public ListenpointEthernet(Stack stack, String name, String host, int port) throws Exception
    {
        super(stack, name, host, port);
    }
	
	public boolean sendMessage (Msg msg, String remoteHost) throws Exception
	{    	
		NetworkInterface[] devices = JpcapCaptor.getDeviceList();
		
		Packet p = new Packet(); // Raw packet to be embedded into Ethernet frame
		EthernetPacket etherPckt = new EthernetPacket(); //create Ethernet packet
		MsgEthernet msgEth = (MsgEthernet) msg; // casting abstract Msg class into MsgEthernet as this is the only  message sent here
		
		int NICIndex = msgEth.getNic(); // index of the NIC (0 = eth0, 1 = eth1, etc...)
		
		etherPckt.frametype = (short) msgEth.getETHType(); // type of packet embedded in Ethernet frame (0800 = IPv4)
	    etherPckt.src_mac = devices[NICIndex].mac_address; //local MAC address
	    
        if (msgEth.getMac().length != 6)
        {
        	throw new ExecutionException("Mac address is malformed, expected format is 	AA:BB:CC:DD:EE:FF");
        }
        else
        {
        	// Mac address are 6*8 bits long
        	byte[] mac = new byte[48];
        	for (int j = 0; j < 6; j++)
        		mac[j] = (byte)Integer.parseInt(msgEth.getMac()[j], 16); // Hex digit to be converted in one single byte
        	etherPckt.dst_mac = mac;
        }
		
        p.data = msgEth.getData(); // filling raw packet with Hex datas provided in xml scenario
        p.datalink = etherPckt; // setting datalink of raw packet as Ethernet
	    
		JpcapSender sender = JpcapSender.openDevice(JpcapCaptor.getDeviceList()[NICIndex]);
		sender.sendPacket(p); // send raw packet
		sender.close();
				
        // GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "Ethernet: Sending frame to " + etherPckt.dst_mac);
		return true;
	}
	
	public boolean create(String protocol) throws Exception
    {	
		if (!super.create(protocol))
			return false;		
		return true;
    }

	private byte[] arp(InetAddress ip) throws java.io.IOException{
		//find network interface
		NetworkInterface[] devices=JpcapCaptor.getDeviceList();
		NetworkInterface device=null;

loop:	for(NetworkInterface d:devices){
			for(NetworkInterfaceAddress addr:d.addresses){
				if(!(addr.address instanceof Inet4Address)) continue;
				byte[] bip=ip.getAddress();
				byte[] subnet=addr.subnet.getAddress();
				byte[] bif=addr.address.getAddress();
				for(int i=0;i<4;i++){
					bip[i]=(byte)(bip[i]&subnet[i]);
					bif[i]=(byte)(bif[i]&subnet[i]);
				}
				if(Arrays.equals(bip,bif)){
					device=d;
					break loop;
				}
			}
		}

		if(device==null)
			throw new IllegalArgumentException(ip+" is not a local address");

		//open Jpcap
		JpcapCaptor captor=JpcapCaptor.openDevice(device,2000,false,3000);
		captor.setFilter("arp",true);
		JpcapSender sender=captor.getJpcapSenderInstance();

		InetAddress srcip=null;
		for(NetworkInterfaceAddress addr:device.addresses)
			if(addr.address instanceof Inet4Address){
				srcip=addr.address;
				break;
			}

		byte[] broadcast=new byte[]{(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255};
		ARPPacket arp=new ARPPacket();
		arp.hardtype=ARPPacket.HARDTYPE_ETHER;
		arp.prototype=ARPPacket.PROTOTYPE_IP;
		arp.operation=ARPPacket.ARP_REQUEST;
		arp.hlen=6;
		arp.plen=4;
		arp.sender_hardaddr=device.mac_address;
		arp.sender_protoaddr=srcip.getAddress();
		arp.target_hardaddr=broadcast;
		arp.target_protoaddr=ip.getAddress();

		EthernetPacket ether=new EthernetPacket();
		ether.frametype=EthernetPacket.ETHERTYPE_ARP;
		ether.src_mac=device.mac_address;
		ether.dst_mac=broadcast;
		arp.datalink=ether;

		sender.sendPacket(arp);

		while(true){
			ARPPacket p=(ARPPacket)captor.getPacket();
			if(p==null){
				throw new IllegalArgumentException(ip+" is not a local address");
			}
			if(Arrays.equals(p.target_protoaddr,srcip.getAddress())){
				return p.sender_hardaddr;
			}
		}
	}
	
}
