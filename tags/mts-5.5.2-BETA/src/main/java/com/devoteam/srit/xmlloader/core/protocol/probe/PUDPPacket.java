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
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import gp.utils.arrays.Array;

/**
 *
 * @author gpasquiers
 */
public class PUDPPacket extends Channel {
    // header fields
    private PIPHeader ipHeader;
    private PUDPHeader udpHeader;

    private Array data;
    
    //private long timestamp;

    public PUDPPacket(PIPHeader ipHeader, Array array){
        super("Channel #" + Utils.newUID());
        this.ipHeader = ipHeader;
        this.udpHeader = new PUDPHeader(array);
        this.data = array.subArray(8); // udp header has a fixed length of "8" src port + dst port + len + checksum

        super.setLocalHost(ipHeader.getSrcIP().getValue());
        super.setLocalPort(udpHeader.getSrcPort().getValue());
        super.setRemoteHost(ipHeader.getDstIP().getValue());
        super.setRemotePort(udpHeader.getDstPort().getValue());
    }

    public PIPHeader getIPHeader(){
        return ipHeader;
    }

    public PUDPHeader getUDPHeader(){
        return udpHeader;
    }

    public Array getData(){
        return data;
    }
    
    // add for genscript
    /*public void setTimestamp(long time){
        timestamp = time;
    }
    
    public long getTimestamp(){
        return timestamp;
    }*/

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
        return StackFactory.PROTOCOL_UDP;
    }
}
