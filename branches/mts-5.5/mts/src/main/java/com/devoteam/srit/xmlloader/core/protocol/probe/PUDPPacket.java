/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
