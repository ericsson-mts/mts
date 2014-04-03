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

package com.devoteam.srit.xmlloader.core.protocol.probe;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import jpcap.JpcapCaptor;
import jpcap.JpcapSender;
import jpcap.NetworkInterface;
import jpcap.PacketReceiver;
import jpcap.packet.EthernetPacket;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;


import com.devoteam.srit.xmlloader.core.ThreadPool;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Probe;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.ethernet.MsgEthernet;

import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.IPv4Array;
import java.util.concurrent.Semaphore;

import jpcap.NetworkInterfaceAddress;

/**
 * 
 * @author mbrakoto
 *
 */
public class PJpcapThread implements PacketReceiver, Runnable {
    // Max number of bytes to capture at once; default => 10 * MTU

    private static int DEFAULT_SNAPLENGHT = 15000;
    private Probe probe;
    private JpcapCaptor captor;
    private JpcapSender sendor;
    private Semaphore stopSemaphore;
    private Semaphore startSemaphore;
    private NetworkInterface networkInterface;
    private boolean stopped;
    private boolean stopPossible = false;

    public PJpcapThread(Probe probe) throws Exception {
        this.probe = probe;
        stopSemaphore = new Semaphore(0);
        startSemaphore = new Semaphore(0);
        stopped = false;

        if (probe.getNetworkInterface() != null) {
        	networkInterface = searchNetworkInterface(probe.getNetworkInterface());
            captor = JpcapCaptor.openDevice(networkInterface, DEFAULT_SNAPLENGHT, probe.getPromiscuousMode(), 10);
            sendor = JpcapSender.openDevice(networkInterface);
            captor.setFilter(probe.getCaptureFilter(), true);
        }
        else if (probe.getFilename() != null) {
            captor = JpcapCaptor.openFile(probe.getFilename());
            if (probe.getCaptureFilter() != null) {
                captor.setFilter(probe.getCaptureFilter(), true);
            }
        }

        captor.setPacketReadTimeout(500);

    }
    
    public boolean sendETHMessage(Msg msg) throws ExecutionException
    {		
		EthernetPacket etherPckt = new EthernetPacket(); //create Ethernet packet
		MsgEthernet msgEth = (MsgEthernet) msg; // casting abstract Msg class into MsgEthernet as this is the only  message sent here

		Packet p = new Packet();
		etherPckt.frametype = (short) msgEth.getETHType(); // type of packet embedded in Ethernet frame (0800 = IPv4)


	    etherPckt.src_mac = networkInterface.mac_address; //local MAC address
	    	    
        if (msgEth.getMac().length != 6)
        {
        	if (msgEth.getMac().length == 1 && msgEth.getMac()[0].length() == 12)
        	{
        		//MAC address is not in form AA:BB:CC:DD:EE:FF but in form AABBCCDDEEFF
        		String[] m = new String[6];
        		String src = msgEth.getMac()[0];
        		int a = 0;
        		for (int b = 0; b < src.length(); b++)
        		{
        			m[a] = "" + src.charAt(b) + src.charAt(++b);
        			a++;
        		}
        		msgEth.setMac(m);
        	}
        	else
        		throw new ExecutionException("Mac address is malformed, expected format is 	AA:BB:CC:DD:EE:FF");
        }
       	// Mac address are 6*8 bits long
       	byte[] mac = new byte[48];
       	for (int j = 0; j < 6; j++)
       		mac[j] = (byte)Integer.parseInt(msgEth.getMac()[j], 16); // Hex digit to be converted in one single byte
       	etherPckt.dst_mac = mac;
        
		p.data = msgEth.getData(); // filling raw packet with Hex datas provided in xml scenario
		p.datalink = etherPckt; // setting datalink of raw packet as Ethernet

		sendor.sendPacket(p); // send raw packet
		return true;
    }
    
    private List<IPPacket> fragmentData(byte[] data, EthernetPacket eth)
    {
    	List<IPPacket> ret = new ArrayList<IPPacket>();
    	
    	short proto = (short) (data[9]&0xff);
    	byte[] IPSRC = new byte[] {data[12], data[13], data[14], data[15]};
    	byte[] IPDST = new byte[] {data[16], data[17], data[18], data[19]};
    	    	
    	IPPacket p = new IPPacket();
    	IPPacket p2 = new IPPacket();
    	try {
    		p.setIPv4Parameter(0, false, false, false, 0, false, false, true, 0, 777, 128, proto, InetAddress.getByAddress(IPSRC), InetAddress.getByAddress(IPDST));
    		p2.setIPv4Parameter(0, false, false, false, 0, false, false, false, 0, 777, 128, proto, InetAddress.getByAddress(IPSRC), InetAddress.getByAddress(IPDST));
       	} catch (Exception e) {}
    	
    	p.ident = 777;
    	p2.ident = 777;
    	p2.offset = (short) 0xb900;
    	p.offset = 0;
    	p.datalink = eth;
    	p2.datalink = eth;
    	byte[] tmp = new byte[1480];
    	int j = 0;
    	for (int i = 20; i < 1500; i++)
    		tmp[j++] = data[i];
    	p.data = tmp;
    	tmp = null;
    	tmp = new byte[data.length - 1500];
    	j = 0;
    	for (int i = 1500; i < data.length; i++)
    		tmp[j++] = data[i];
    	p2.data = tmp;
    	
    	ret.add(p);
    	ret.add(p2);
    	return ret;
    }

    public void create() {
        try {
            ThreadPool.reserve().start(this);

            try {
                startSemaphore.acquire();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, null, "Probe created and started: ", probe.getName());
        }
        catch (Exception e) {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Probe creation error : ", probe.getName());
        }
    }

    @Override
    public void run() {
        captor.processPacket(1, this);

        startSemaphore.release();

        while (!stopped) {
            captor.processPacket(-1, this);
            synchronized (this) {
                try {
                    wait(1000);
                    stopPossible = true;
                }
                catch (InterruptedException ex) {
                    GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.CORE, ex, "Error Probe thread");
                }
            }
        }

        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, null, "Probe capture thread ended : ", probe.getName());

        stopSemaphore.release();
    }

    public void stop() {
        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, null, "CaptureProbeThread.stop() : ", probe.getName());
        if (null != captor) {
            stopped = true;
            if (captor != null) {
            	captor.breakLoop();
            	captor.close();
            }
            if (sendor != null)
            	sendor.close();
            captor = null;
            sendor = null;

            try {
                stopSemaphore.acquire();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized static NetworkInterface searchNetworkInterface(String networkName) throws Exception {
        // try to find a jpcap device having the same name
        for (NetworkInterface networkInterface : JpcapCaptor.getDeviceList()) {
        	GlobalLogger.instance().getSessionLogger().debug(TextEvent.Topic.PROTOCOL, "Get network interface : ", networkInterface.name);
            if (networkInterface.name.equalsIgnoreCase(networkName)) {
                return networkInterface;
            }
        }

        // try to find a java device having the same name
        java.net.NetworkInterface javaNetworkInterface = java.net.NetworkInterface.getByName(networkName);
        if (javaNetworkInterface != null) {
            // try to 	find a jpcap device having an ip address in common with the java device
        	Enumeration<java.net.InetAddress> addrs = javaNetworkInterface.getInetAddresses();
            if (addrs.hasMoreElements())
            {
	            // select the first of the InetAdresse of the java device
	            InetAddress inetAddress = javaNetworkInterface.getInetAddresses().nextElement();
	            for (NetworkInterface networkInterface : JpcapCaptor.getDeviceList()) {
	                // compare all of the adresses of the jpcap interface against the address of the java interface
	                for (NetworkInterfaceAddress networkInterfaceAddress : networkInterface.addresses) {
	                    byte[] jpcapAddress = networkInterfaceAddress.address.getAddress();
	                    byte[] javaAddress = inetAddress.getAddress();
	                    GlobalLogger.instance().getSessionLogger().debug(TextEvent.Topic.PROTOCOL, "Get network address : ", networkInterfaceAddress.address);
	                    if (new DefaultArray(javaAddress).equals(new DefaultArray(jpcapAddress))) {
	                        return networkInterface;
	                    }
	                }
	            }
            }
        }
        
        // for IPV6 address remove [] characters
        if (networkName.charAt(0) == '[')
        {
        	networkName = networkName.substring(1);
        }
        int len = networkName.length() - 1;
        if (networkName.charAt(len) == ']')
        {
        	networkName = networkName.substring(0, len);
        }
        
        networkName = "/" + networkName; 
        for (NetworkInterface networkInterface : JpcapCaptor.getDeviceList()) {
            // compare all of the adresses of the jpcap interface agains the address
            for (NetworkInterfaceAddress networkInterfaceAddress : networkInterface.addresses) {
            	String addr = networkInterfaceAddress.address.toString();
                GlobalLogger.instance().getSessionLogger().debug(TextEvent.Topic.PROTOCOL, "Get network address : ", addr);
                if (addr.equals(networkName)) {
                    return networkInterface;
                }
            }
        }

        throw new ExecutionException("Could not find any network interface matching the name " + networkName);
    }

    public void receivePacket(Packet packet) {
        try {
            if (packet == Packet.EOF) {
                stopPossible = true;
            }

            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, Stack.CAPTURE, " IP message :\n", packet, "\n", new String(packet.data));

            if (null != packet) {
                PIPReassembler.receiveNewPacket(packet, probe);
            }
        }
        catch (Exception e) {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Exception in ProbeJpcapThread : ", e);
        }
    }

    public boolean getStopPossible() {
        return stopPossible;
    }
}
