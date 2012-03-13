/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.protocol.probe;

import com.devoteam.srit.xmlloader.core.protocol.StackFactory;

import gp.utils.arrays.Array;

/**
 *
 * @author gpasquiers
 */
public class PTCPPacket {
    // header fields
    private PIPHeader ipHeader;
    private PTCPHeader tcpHeader;
    private Array data;
    
    //private Long timestamp;

    public PTCPPacket(PIPHeader ipHeader, Array array){
        this.ipHeader = ipHeader;
        this.tcpHeader = new PTCPHeader(array);
        this.data = array.subArray(tcpHeader.getLength());
    }

    public PIPHeader getIPHeader(){
        return ipHeader;
    }

    public PTCPHeader getTCPHeader(){
        return tcpHeader;
    }

    public Array getData(){
        return data;
    }

    public void setData(Array data){
        this.data = data;
    }

}
