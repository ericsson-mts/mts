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
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.PacketReceiver;
import jpcap.packet.EthernetPacket;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;


import com.devoteam.srit.xmlloader.core.ThreadPool;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Probe;
import com.devoteam.srit.xmlloader.core.protocol.Stack;

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
    private Semaphore stopSemaphore;
    private Semaphore startSemaphore;
    private boolean stopped;
    private boolean stopPossible = false;

    public PJpcapThread(Probe probe) throws Exception {
        this.probe = probe;
        stopSemaphore = new Semaphore(0);
        startSemaphore = new Semaphore(0);
        stopped = false;

        if (probe.getNetworkInterface() != null) {
            NetworkInterface networkInterface = searchNetworkInterface(probe.getNetworkInterface());
            captor = JpcapCaptor.openDevice(networkInterface, DEFAULT_SNAPLENGHT, probe.getPromiscuousMode(), 10);
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
            captor.breakLoop();
            captor.close();
            captor = null;

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
	                    if (new DefaultArray(javaAddress).equals(new DefaultArray(jpcapAddress))) {
	                        return networkInterface;
	                    }
	                }
	            }
            }
        }

        IPv4Array parsedIP = null;
        try {
            // try to parse the networkname as an IPv4 address        
            parsedIP = new IPv4Array(networkName);
        }
        catch (Exception e) {
            throw new ExecutionException("Could not find any network interface matching the name \"" + networkName + "\" : ", e);
        }

        for (NetworkInterface networkInterface : JpcapCaptor.getDeviceList()) {
            // compare all of the adresses of the jpcap interface agains the address
            for (NetworkInterfaceAddress networkInterfaceAddress : networkInterface.addresses) {
                byte[] jpcapAddress = networkInterfaceAddress.address.getAddress();
                if (new DefaultArray(jpcapAddress).equals(parsedIP)) {
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
