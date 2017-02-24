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

package com.devoteam.srit.xmlloader.rtp.flow;

import java.util.ArrayList;
import java.util.Iterator;

public class CodecDictionary {

    private static CodecDictionary instance = null;

    public static CodecDictionary instance() {
    	if (instance == null)
    	{
    		instance = new CodecDictionary();
    	}
    	return instance;
    }
    	
    private ArrayList<Protocol> listCodec = new ArrayList<Protocol>();

    public CodecDictionary() {
    	
    	/**
      	 * PT : Payload Type
      	 * NBChan : Number of Channel
      	 * AlgDelay : Algorithmic Delay
      	 * ie : Equiment Impairment
      	 * bpl : Robustness to random packet loss
      	 */
		//                                 PT  Code Name     Audio/Vid  Freq NBchan AlgDelay  Overhead  ie   bpl (all without PLC)
		listCodec.add(new Protocol(0,   7, "PCMU",      "A",    8000,   1,  0.125f,     0,      0,  4.8f));//4.8 for 20ms packet, if 10ms => 4.3    }
		listCodec.add(new Protocol(1,   0, "reserved",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(2,   0, "reserved",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(3,   49,"GSM",       "A",    8000,   1,  20,         0,      -1, -1));
		listCodec.add(new Protocol(4,   20,"G723",      "A",    8000,   1,  30,         7.5f,   -1, 16.1f));
		listCodec.add(new Protocol(5,   0, "DVI4",      "A",    8000,   1,  -1,         -1,     -1, -1));
		listCodec.add(new Protocol(6,   0, "DVI4",      "A",    16000,  1,  -1,         -1,     -1, -1));
		listCodec.add(new Protocol(7,   0, "LPC",       "A",    8000,   1,  -1,         -1,     -1, -1));
		listCodec.add(new Protocol(8,   6, "PCMA",      "A",    8000,   1,  0.125f,     0,      0,  4.8f));
		listCodec.add(new Protocol(9,   0, "G722",      "A",    8000,   1,  0.125f,     1.5f,   13, 4.3f));
		listCodec.add(new Protocol(10,  0, "L16",       "A",    44100,  2,  -1,         -1,     -1, -1));
		listCodec.add(new Protocol(11,  0, "L16",       "A",    44100,  1,  -1,         -1,     -1, -1));
		listCodec.add(new Protocol(12,  0, "QCELP",     "A",    8000,   1,  -1,         -1,     -1, -1));
		listCodec.add(new Protocol(13,  0, "CN",        "A",    8000,   1,  -1,         -1,     -1, -1));
		listCodec.add(new Protocol(14,  0, "MPA",       "A",    90000,  -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(15,  0, "G728",      "A",    8000,   1,  0.125f,     0,      -1, 1));
		listCodec.add(new Protocol(16,  0, "DVI4",      "A",    11025,  1,  -1,         -1,     -1, -1));
		listCodec.add(new Protocol(17,  0, "DVI4",      "A",    22050,  1,  -1,         -1,     -1, -1));
		listCodec.add(new Protocol(18,  0, "G729",      "A",    8000,   1,  10,         5,      -1, 17f));
		listCodec.add(new Protocol(19,  0, "reserved",  "A",    -1,     -1, -1,         -1,     -1, -1));		
		listCodec.add(new Protocol(20,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));		
		listCodec.add(new Protocol(21,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(22,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(23,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(24,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));		
		listCodec.add(new Protocol(25,  0, "CelB",      "V",    90000,  -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(26,  0, "JPEG",      "V",    90000,  -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(27,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(28,  0, "nv",        "V",    90000,  -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(29,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(30,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));		
		listCodec.add(new Protocol(31,  0, "H261",      "V",    90000,  -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(32,  0, "MPV",       "V",    90000,  -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(33,  0, "MP2T",      "AV",   90000,  -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(34,  0, "H263",      "V",    90000,  -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(35,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(36,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(37,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(38,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(39,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(40,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(41,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(42,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(43,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(44,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(45,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(46,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(47,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(48,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(49,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(50,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(51,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(52,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(53,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(54,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(55,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(56,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(57,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(58,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(59,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(60,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(61,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(62,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(63,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(64,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(65,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(66,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(67,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(68,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(69,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(70,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(71,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(72,  0, "reserved for RTCP conflict avoidance",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(73,  0, "reserved for RTCP conflict avoidance",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(74,  0, "reserved for RTCP conflict avoidance",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(75,  0, "reserved for RTCP conflict avoidance",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(76,  0, "reserved for RTCP conflict avoidance",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(77,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(78,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(79,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(80,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(81,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(82,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(83,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(84,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(85,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));		
		listCodec.add(new Protocol(86,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(87,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(88,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(89,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(90,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(91,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(92,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(93,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(94,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(95,  0, "unassigned",  "A",    -1,     -1, -1,         -1,     -1, -1));		
		listCodec.add(new Protocol(96,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(97,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(98,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(99,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(100,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));		
		listCodec.add(new Protocol(101,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(102,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(103,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(104,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(105,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));		
		listCodec.add(new Protocol(106,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(107,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(108,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(109,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(110,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));		
		listCodec.add(new Protocol(111,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(112,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(113,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(114,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(115,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));		
		listCodec.add(new Protocol(116,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(117,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(118,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(119,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(120,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));		
		listCodec.add(new Protocol(121,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(122,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(123,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(124,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(125,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));		
		listCodec.add(new Protocol(126,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(127,  0, "dynamic",  "?",    -1,     -1, -1,         -1,     -1, -1));
		listCodec.add(new Protocol(128,  0, "unknown",  "?",    -1,     -1, -1,         -1,     -1, -1));
    }
	
    public int getClockRate(int payloadType) {
	int result = -1;
        Protocol p = null;
        for(Iterator<Protocol> it = listCodec.iterator(); it.hasNext();)
        {
            p = it.next();
            if(p.getPayloadType() == payloadType)
            {
                result = p.getClockRate();
                break;
            }
        }
        return result;
    }

    public Protocol getProtocol(String protocol){
        Protocol p = null;
        for(Iterator<Protocol> it = listCodec.iterator(); it.hasNext();)
        {
            p = it.next();
            if (p.getName().equals(protocol)) {
                break;
            }
        }
        return p;
    }

    public Protocol getProtocol(int payloadType){
        Protocol p = null;
        for(Iterator<Protocol> it = listCodec.iterator(); it.hasNext();)
        {
            p = it.next();
            if (p.getPayloadType() == payloadType) {
                break;
            }
        }
        return p;
    }

    public Protocol getProtocolByName(String name){
        Protocol p = null;
        for(Iterator<Protocol> it = listCodec.iterator(); it.hasNext();)
        {
            p = it.next();
            if (p.getName().equals(name)) {
                break;
            }
        }
        return p;
    }

    public String getCodec(int payloadType){
        Protocol p = null;
        for(Iterator<Protocol> it = listCodec.iterator(); it.hasNext();)
        {
            p = it.next();
            if (p.getPayloadType() == payloadType) {
                break;
            }
        }
        return p.getName();
    }

    public void addCodec(int payloadType, int compressionCode, String name, String type, int clokcRate,
                         int nbChannel, float algorithmicDelay, float overhead, int ie, float bpl){

        Protocol p = new Protocol(payloadType, compressionCode, name, type, clokcRate,
                                  nbChannel, algorithmicDelay, overhead, ie, bpl);
        listCodec.add(p);
    }

    public void addCodec(Protocol p){
        listCodec.add(p);
    }

    public class Protocol {
	private int     payloadType;
        private int     compressionCode;
	private String  name;
	private String  type;
	private int     clockRate;
	private int     nbChannel;
        private float   algorithmicDelay;
        private float   overhead;
        private int     ie;                 // FACTEUR DE DEGRADATION DUE A L'EQUIPEMENT (Equiment Impairment)
        private float   bpl;                // FACTEUR DE ROBUSTESSE A LA PERTE DE PAQUET


	public Protocol(int payloadType, int compressionCode, String name, String type, int clockRate, int nbChannel,
                        float algorithmicDelay, float overhead, int ie, float bpl) {
		this.payloadType        = payloadType;
           	this.compressionCode    = compressionCode;
		this.name               = name;
		this.type               = type;
		this.clockRate          = clockRate;
		this.nbChannel          = nbChannel;
           	this.algorithmicDelay   = algorithmicDelay;
            	this.overhead           = overhead;
            	this.ie                 = ie;
            	this.bpl                = bpl;
	}

	public int getPayloadType() {
		return payloadType;
	}

        public int getCompressionCode() {
            return compressionCode;
        }

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public int getClockRate() {
		return clockRate;
	}

	public int getNbChannel() {
		return nbChannel;
	}

        public float getAlgorithmicDelay() {
		return algorithmicDelay;
	}

        public float getOverhead() {
		return overhead;
	}

        public int getIe() {
		return ie;
	}

        public float getBpl() {
		return bpl;
	}
    }
}