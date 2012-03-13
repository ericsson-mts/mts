/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.protocol.probe;

import gp.utils.arrays.Array;

/**
 *
 * @author fhenry
 */
public class PIPPacket {
    private PIPHeader header;
    private Array data;

    public PIPPacket(Array array){
        this.header = new PIPHeader(array);
        this.data = array.subArray(header.getLength(), header.getTotalLength() - header.getLength());
    }

    public PIPHeader getHeader(){
        return this.header;
    }

    public Array getData(){
        return this.data;
    }
}