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

import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Probe;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.probe.PInputStream.Element;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.expireshashmap.Removable;
import gp.utils.arrays.Array;

import gp.utils.scheduler.Scheduler;
import gp.utils.scheduler.Task;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author gpasquiers
 */
public class PTCPSocket extends Channel implements Task, Removable {
    private long nextSeq;
    private HashMap<Long, PTCPPacket> packets;
    private PInputStream inputStream;
    private Probe probe;
    private boolean taskRunning;
    private static final Scheduler parsingScheduler = new Scheduler(10);

    // optimization in order not to search in hashmap for each packet we receive
    // so we keep the latest packet there, because it will very often be the
    // right packet.
    private PTCPPacket latestPacket;

    public PTCPSocket(PTCPPacket packet, Probe probe){        
        super("TCP capture channel " + Utils.newUID());
        super.setLocalHost(packet.getIPHeader().getSrcIP().getValue());
        super.setLocalPort(packet.getTCPHeader().getSrcPort().getValue());
        super.setRemoteHost(packet.getIPHeader().getDstIP().getValue());
        super.setRemotePort(packet.getTCPHeader().getDstPort().getValue());
        
        
        this.probe = probe;
        taskRunning = false;
        packets = null;
        latestPacket = null;
        nextSeq = toUnsignedLong(packet.getTCPHeader().getSeqNumber().getValue());
        inputStream = new PInputStream();        
    }

    public PInputStream getInputStream(){
        return inputStream;
    }

    synchronized public boolean addPacket(PTCPPacket packet){
        long seqNumber = toUnsignedLong(packet.getTCPHeader().getSeqNumber().getValue());

        // RETR: cas particulier. une retransmission peut contenir plus de data que le message d'origine.
        //       mais le message d'origine peut avoir ete bien capture et avoir deja ete pris en compte.

        if(seqNumber >= nextSeq || seqNumber < nextSeq - 0x80000000L || seqNumber + packet.getData().length > nextSeq){
            if(null != latestPacket){
                // only instantiate hashmap when needed
                if(null == packets){
                    packets = new HashMap();
                }

                // RETR
                if(seqNumber < nextSeq && seqNumber + packet.getData().length > nextSeq){
                    packets.put(nextSeq, packet);
                }
                else{
                    packets.put(seqNumber, packet);
                }

            }
            else{
                latestPacket = packet;
            }

            long oldNextSeq = nextSeq;

            boolean newData = false;
            while(null != (packet = getValidPacket())){
                if(packet.getData().length > 0){
                    newData = true;
                    seqNumber = toUnsignedLong(packet.getTCPHeader().getSeqNumber().getValue());
                    // RETR
                    if(seqNumber < oldNextSeq && seqNumber + packet.getData().length > oldNextSeq){
                        int dataSup = (int) (seqNumber + packet.getData().length - oldNextSeq);
                        inputStream.feed(packet.getData().subArray(packet.getData().length - dataSup), packet.getIPHeader().getTimestamp());
                    }
                    else{
                        inputStream.feed(packet.getData(), packet.getIPHeader().getTimestamp());
                    }
                    oldNextSeq = nextSeq;
                }
            }
            
            if(newData && !taskRunning && inputStream.available()>0){                

                taskRunning = true;
                parsingScheduler.execute(this, false);
            }

            return newData;
        }

        return false;
    }

    private PTCPPacket getValidPacket(){
        PTCPPacket packet = null;
        if(null != latestPacket){
            long currentSeq = toUnsignedLong(latestPacket.getTCPHeader().getSeqNumber().getValue());
            if(nextSeq == currentSeq){
                packet = latestPacket;
                latestPacket = null;
            }
            // RETR
            else if(currentSeq < nextSeq && currentSeq + latestPacket.getData().length > nextSeq){
                packet = latestPacket;
                latestPacket = null;
            }
        }

        if(null == packet && null != packets){
            packet = packets.remove(nextSeq);

            // free hashmap if possible
            if(0 == packets.size()){
                packets = null;
            }
        }

        if(null != packet){
            nextSeq = toUnsignedLong(packet.getTCPHeader().getSeqNumber().getValue()) + packet.getData().length;
            if(packet.getTCPHeader().syn()){
                nextSeq ++;
            }
            nextSeq &= 0xFFFFFFFFL;
        }

        return packet;
    }

    private long toUnsignedLong(int integer){
        return 0xFFFFFFFFL & integer;
    }
    
    public long getLastTimestamp(){
        long timestamp;
        PInputStream stream = getInputStream();
        timestamp = stream.getTimestamp();

        return timestamp;
    }

    public void execute(){
        try{
            this.probe.handlePTCPSocket(this);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        synchronized(this){
            if(inputStream.available() > 0){
                parsingScheduler.execute(this, false);
            }
            else{
                taskRunning = false;
            }
        }
    }

    @Override
    public boolean open() throws Exception
    {
        throw new UnsupportedOperationException("Not supported, this is an informative capture channel");
    }

    @Override
    public boolean close()
    {
        throw new UnsupportedOperationException("Not supported, this is an informative capture channel");
    }

    @Override
    public boolean sendMessage(Msg msg) throws Exception
    {
        throw new UnsupportedOperationException("Not supported, this is an informative capture channel");
    }

    @Override
    public String getTransport()
    {
        return StackFactory.PROTOCOL_TCP;
    }

    /**
     * interface Removable
     */
    public void onRemove() throws Exception
    {
    	// nothing to do
    }

}
