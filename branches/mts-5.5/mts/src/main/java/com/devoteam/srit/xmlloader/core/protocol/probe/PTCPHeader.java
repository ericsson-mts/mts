/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.protocol.probe;

import gp.utils.arrays.Array;
import gp.utils.arrays.Integer16Array;
import gp.utils.arrays.Integer32Array;

/**
 *
 * @author gpasquiers
 */
public class PTCPHeader {
    private Integer16Array srcPort;
    private Integer16Array dstPort;
    private Integer32Array ackNumber;
    private Integer32Array seqNumber;
    private Array flags;
    private Array dataOffset;
    private Integer16Array winSize;

    public PTCPHeader(Array header){
        // parse ip header
        srcPort = new Integer16Array(header.subArray(0, 2));
        dstPort = new Integer16Array(header.subArray(2, 2));
        seqNumber = new Integer32Array(header.subArray(4, 4));
        ackNumber = new Integer32Array(header.subArray(8, 4));
        flags = header.subArray(13, 1);
        dataOffset = header.subArray(12, 1);
        winSize = new Integer16Array(header.subArray(14, 2));
    }

    public Integer16Array getSrcPort(){
        return srcPort;
    }

    public Integer16Array getDstPort(){
        return dstPort;
    }

    public Integer32Array getAckNumber(){
        return ackNumber;
    }

    public Integer32Array getSeqNumber(){
        return seqNumber;
    }

    public boolean cwr(){
        return flags.getBit(0) == 1;
    }

    public boolean ece(){
        return flags.getBit(1) == 1;
    }

    public boolean urg(){
        return flags.getBit(2) == 1;
    }

    public boolean ack(){
        return flags.getBit(3) == 1;
    }

    public boolean psh(){
        return flags.getBit(4) == 1;
    }

    public boolean rst(){
        return flags.getBit(5) == 1;
    }

    public boolean syn(){
        return flags.getBit(6) == 1;
    }

    public boolean fin(){
        return flags.getBit(7) == 1;
    }

    public int getLength(){
        return dataOffset.getBits(0, 4) * 4;
    }

    public Integer16Array getWinSize(){
        return winSize;
    }

}
