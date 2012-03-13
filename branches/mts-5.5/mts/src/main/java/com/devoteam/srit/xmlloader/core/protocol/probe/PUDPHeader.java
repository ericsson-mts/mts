/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.protocol.probe;

import gp.utils.arrays.Array;
import gp.utils.arrays.Integer16Array;

/**
 *
 * @author gpasquiers
 */
public class PUDPHeader {
    private Integer16Array srcPort;
    private Integer16Array dstPort;

    public PUDPHeader(Array header){
        srcPort = new Integer16Array(header.subArray(0, 2));
        dstPort = new Integer16Array(header.subArray(2, 2));
    }

    public Integer16Array getSrcPort(){
        return srcPort;
    }

    public Integer16Array getDstPort(){
        return dstPort;
    }
}
