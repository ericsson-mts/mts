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

package com.devoteam.srit.xmlloader.core.coding.tpkt;

import gp.utils.arrays.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Exception;

/**
 *
 * @author indiaye
 */
public class TPKTPacket {

    Integer16Array _packetLength;
    Integer08Array _version;
    Array _reserved;

    public TPKTPacket(InputStream streamTPKT) throws IOException, Exception {
        byte[] tabversion = new byte[1];
        byte[] tablength = new byte[2];
        byte[] tabreserved = new byte[1];
        streamTPKT.read(tabversion);
        streamTPKT.read(tabreserved);
        streamTPKT.read(tablength);
        _version = new Integer08Array(new DefaultArray(tabversion));
        _packetLength = new Integer16Array(new DefaultArray(tablength));
        _reserved = new DefaultArray(tabreserved);
    }

    public TPKTPacket(int length) {

        _packetLength = new Integer16Array(length);

        _version = new Integer08Array(3);
        _reserved = new DefaultArray(1);
    }

    public int getPacketLength() {
        return _packetLength.getValue();
    }

    public void setPacketLength(int _packetLength) {
        this._packetLength.setValue(_packetLength);
    }

    public int getVersion() {
        return _version.getValue();
    }

    public void setVersion(int _version) {
        this._version.setValue(_version);
    }

    public Array getValue() {
        SupArray arr = new SupArray();
        arr.addLast(_version);
        arr.addLast(_reserved);
        arr.addLast(_packetLength);
        return arr;
    }
}
