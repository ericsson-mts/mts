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

import java.util.concurrent.LinkedBlockingQueue;

import jpcap.packet.EthernetPacket;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;

import com.devoteam.srit.xmlloader.core.ThreadPool;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Probe;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.expireshashmap.ExpireHashMap;
import com.devoteam.srit.xmlloader.core.utils.expireshashmap.Removable;

import gp.utils.arrays.Array;
import gp.utils.arrays.ConstantArray;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.EmptyArray;
import gp.utils.arrays.Integer16Array;
import gp.utils.arrays.Integer32Array;
import gp.utils.arrays.OverlayArray;

/**
 * Reassemble fragmented packets for packet protocol like UDP if necessary or fill in the stack of message.
 * 
 * @author mbrakoto
 *
 */
public class PIPReassembler
{

    private static int MAX_PACKETS_HOLD = 100000;
    // Value recommended in RFC 791 (15s)
    private static long MAX_RESOURCE_LIFETIME = 15000;
    private static LinkedBlockingQueue<ProbeCallback> callbacks = new LinkedBlockingQueue(MAX_PACKETS_HOLD);
    private static ExpireHashMap<String, PacketReassembler> reassemblers = new ExpireHashMap("Reassembling resource", MAX_RESOURCE_LIFETIME);
    private static boolean running = false;


    public static void start() {
        if(!running){
            running = true;
            ThreadPool.reserve().start(new PacketReassemblerWorker());
        }
    }

    public static void stop() throws Exception {
        if(running){
            running = false;
            callbacks.put(null); // when the worker "takes" a "null", it stops
        }
    }

    public static void receiveNewPacket(Packet packet, Probe probe) throws Exception {
        if(running){
            ProbeCallback callback = new ProbeCallback();
            callback.packet = packet;
            callback.probe = probe;
            callbacks.put(callback);
        }
        else{
            //throw some error
        }
    }

    private static class PacketReassemblerWorker implements Runnable {
        @Override
        public void run(){
            GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, null, "PacketReassembler started");

            while (running){
                try{
                    // take a packet in the queue
                    // could have multiple thread doing this if we synchronize a bit on hashmap
                    ProbeCallback callback = callbacks.take();

                    if(null == callback) break;

                    Packet packet = callback.packet;                                        
                              
                    ConstantArray background = new ConstantArray((byte) 0x39, packet.header.length + packet.data.length);
                    OverlayArray data;
                    data = new OverlayArray(background, new DefaultArray(packet.header), 0);
                    data = new OverlayArray(data, new DefaultArray(packet.data), packet.header.length);

                    PIPPacket pipPacket = null;
                    EthernetPacket ethPacket = (EthernetPacket) packet.datalink;

                    if ((ethPacket.frametype & 0xffff) == 0x0800){
                        // Standard IP Packet
                    	pipPacket = new PIPPacket(data.subArray(14));
                    } 
                    else if ((ethPacket.frametype & 0xffff) == 0x8100){
                        // Ethernet VLAN tag = 0x8100
                    	pipPacket = new PIPPacket(data.subArray(18));

                        if(data.get(16) != 0x08 || data.get(17) != 0x00){
                            // not IP in VLAN
                            continue;
                        }
                    } 
                    else if ((ethPacket.frametype & 0xffff) == 0x9100){
                        // Ethernet VLAN double tag = 0x9100
                        if(data.get(20) != 0x08 || data.get(21) != 0x00){
                            // not IP in VLAN
                            continue;
                        }

                    	pipPacket = new PIPPacket(data.subArray(22));
                    }
                    else if (((ethPacket.frametype & 0xffff) == 0x0806))
                    {
                    	// ARP, Do nothing, just pass it to ethernet stack
                    	callback.probe.capturedETHPacket(packet);
                    	continue;
                    }
                    else{
                        // not IP
                       	continue;
                    }
                    long timestamp = packet.sec*1000000 + packet.usec;
                    pipPacket.getHeader().setTimestamp(timestamp);

                    if (callback.probe.getProtocol() == StackFactory.PROTOCOL_ETHERNET) {
                    	callback.probe.capturedETHPacket(packet);
                    }
                    else
                    {
	                    if (pipPacket.getHeader().isMore_flag() || pipPacket.getHeader().getOffset_fragment() != 0){
	                        // fragmented packet
	
	                        // Construct IP V4 packet identification
	                        StringBuilder builder = new StringBuilder(36);
	
	                        builder.append(Array.toHexString(new DefaultArray(((EthernetPacket) packet.datalink).src_mac)));
	                        builder.append(Array.toHexString(new DefaultArray(((EthernetPacket) packet.datalink).dst_mac)));
	                        builder.append(Array.toHexString(new Integer16Array(pipPacket.getHeader().getProtocol())));
	                        builder.append(Array.toHexString(new Integer32Array(pipPacket.getHeader().getIdent())));
	
	                        String packetId = builder.toString();
	
	                        PacketReassembler reassembler = reassemblers.get(packetId);
	
	                        if(null == reassembler){
	                            reassembler = new PacketReassembler();
	                            reassemblers.put(packetId, reassembler);
	                        }
	
	                        reassembler.addPacket(pipPacket);
	
	                        if(reassembler.isComplete()){
	                            reassemblers.remove(packetId);
	                            PIPHeader header = reassembler.getFirstPacket().getHeader();
	                            header.setTimestamp(timestamp);
	
	                            if(reassembler.getFirstPacket().getHeader().getProtocol() == IPPacket.IPPROTO_UDP){
	                                PUDPPacket udpPacket = new PUDPPacket(header, reassembler.getData());
	                                callback.probe.capturedUDPPacket(udpPacket);
	                            }
	                            else if(reassembler.getFirstPacket().getHeader().getProtocol() == IPPacket.IPPROTO_TCP){
	                                PTCPPacket tcpPacket = new PTCPPacket(header, reassembler.getData());
	                                callback.probe.capturedTCPPacket(tcpPacket);
	                            }
	                        }
	                    }
	                    else{
	                        if(pipPacket.getHeader().getProtocol() == IPPacket.IPPROTO_UDP){
	                            PUDPPacket udpPacket = new PUDPPacket(pipPacket.getHeader(), pipPacket.getData());
	                            callback.probe.capturedUDPPacket(udpPacket);
	                        }
	                        else if(pipPacket.getHeader().getProtocol() == IPPacket.IPPROTO_TCP){
	                            PTCPPacket tcpPacket = new PTCPPacket(pipPacket.getHeader(), pipPacket.getData());
	                            callback.probe.capturedTCPPacket(tcpPacket);
	                        }
	                    }
                    }
                }
                catch (Exception e){
                    GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Exception in PacketReassembler: ", e);
                }
            }
            GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, null, "PacketReassembler ended");
        }
    };

    private static class PacketReassembler implements Removable {
        boolean sizeIsFinal;
        int packetSize;
        int receivedSize;
        private PIPPacket firstPacket;
        private Array data;

        private PacketReassembler(){
            sizeIsFinal = false;
            packetSize = 0;
            receivedSize = 0;
            data = new EmptyArray();
        }

        private void addPacket(PIPPacket pipPacket){
        	int offset = pipPacket.getHeader().getOffset_fragment();
            int currentLen = pipPacket.getData().length + offset * 8;

            if(offset == 0){
                // first packet has a special header handling
                firstPacket = pipPacket;
                //int len = pipPacket.getHeader().getLength();
                //receivedSize += pipPacket.getHeader().getLength();
            }

            if(!sizeIsFinal && currentLen > packetSize){
                packetSize = currentLen;
                data = new OverlayArray(new ConstantArray((byte) 0, packetSize), data, 0) ;
                if(false == pipPacket.getHeader().isMore_flag()){
                    sizeIsFinal = true;
                }
            }

            data = new OverlayArray(data, pipPacket.getData(), offset * 8);
            receivedSize += pipPacket.getData().length;
        }

        private boolean isComplete(){
        	// return sizeIsFinal;
            return (packetSize == receivedSize) && sizeIsFinal;
        }

        private Array getData(){
            return data;
        }

        private PIPPacket getFirstPacket(){
            return firstPacket;
        }
        
        /**
         * interface Removable
         */
        public void onRemove() throws Exception
        {
        	// nothing to do
        }

    }

    private static class ProbeCallback{
        public Packet packet;
        public Probe probe;
    }

}
