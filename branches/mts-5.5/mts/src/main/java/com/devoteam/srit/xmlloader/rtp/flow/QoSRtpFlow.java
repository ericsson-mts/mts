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

import java.util.TreeSet;

import com.devoteam.srit.xmlloader.rtp.MsgRtp;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.Collections;

public class QoSRtpFlow {

	//parameter calculate to the flow receipt
	private int lastSequenceNumber = -1;//sequence number of the last packet
	private SortedSet missingSequenceNumber;//list of the packets that have yet to arrive
	private SortedSet receivedSequenceNumber;//list of the arrived packets
	private int packetLost = 0;//number of lost packets
	private int duplicated = 0;//number of duplicated packets
	private boolean isDuplicated = false;
    private int packetMissSequence = 0;//number of unexpected arrived packets
	private SortedSet unexpectedPackets;//explicit
	private int topSequence = 0;//used for packetMissSequence computation

    private float currentDelta = 0;//time between the last packet arrival and this one
    private LinkedList<Float> deltaList;//list of delta for each packet received
    private float meanDelta = 0;//explicit

    private float currentPacketSpacing = 0;//instant spacing between the last packet and this one
    private LinkedList<Float> packetSpacingList;//list of packetSpacing for each packet received
    private float meanPacketSpacing = 0;//explicit

    private float currentJitter = 0;//instant jitter of the packet
    private LinkedList<Float> jitterList;//list of jitter for each packet received
    private float meanJitter = 0;//explicit

    private float currentBitRate = 0;//instant bitRate of the flow
	private float meanBitRate = 0;//explicit
    
	private int packetNumber = 0;//explicit
	private int oldPacketNumber = 0;//packet number of the last arrived packet
    private long lastTimestampRTP = 0;//explicit
	private long arrivedTime = 0;//arrived time of this packet
	private long lastArrivedTime = 0;//arrived time of the last packet
	private long data = 0;//accumulation of the arrived data since the last change of timestamp
	private CodecDictionary dico;//dictionary of all codec associated with their information (payloadType, frequence...)
    private EModele eModele;
    private int payloadType = 0;

	public QoSRtpFlow(CodecDictionary dico) {
        this.dico = dico;
		missingSequenceNumber = Collections.synchronizedSortedSet(new TreeSet<Integer>());//use this encapsulation to automatically synchronized the treeset access
		receivedSequenceNumber = Collections.synchronizedSortedSet(new TreeSet<Integer>());
		unexpectedPackets = Collections.synchronizedSortedSet(new TreeSet<Integer>());
        deltaList = new LinkedList<Float>();
        packetSpacingList = new LinkedList<Float>();
        jitterList = new LinkedList<Float>();
        eModele = new EModele();
	}

	public void checkPacket(MsgRtp msg)
	{
		//for all packets
		int seqnum = msg.getInternalSequenceNumber();
		arrivedTime = msg.getTimestamp();
		isDuplicated = false;

		if(lastSequenceNumber == -1) //for the first message
        {
			//initialisation
			lastSequenceNumber = seqnum;
			topSequence = seqnum;
			lastTimestampRTP = msg.getTimestampRTP();
			lastArrivedTime = arrivedTime;
		}
		else
        {
            //if the packet is contained in the list of the missing (or present) packets
            boolean found = false;
            //search in the missing list
            if (missingSequenceNumber.contains(seqnum))
            {
                missingSequenceNumber.remove(seqnum);
                found = true;
            }
            //search in the list of arrived packet to find duplicated packets
            if (receivedSequenceNumber.contains(seqnum) && !found)
            {
                duplicated++;
                found = true;
                isDuplicated = true;
            }

            //if the packet do not arrive in good order and was not found in the previous list research
			if((seqnum != (lastSequenceNumber + 1)) && !found) 
            {
                //add the interval of packet missing
                int seqNumToadd = lastSequenceNumber;
                int seqNumToTest = seqnum;

                if (seqnum < lastSequenceNumber)
                {
                    seqNumToadd = seqnum;
                    seqNumToTest = lastSequenceNumber;
                }

                seqNumToadd++;
                while (seqNumToadd != seqNumToTest)
                {
                    missingSequenceNumber.add(seqNumToadd++);
                }
			}
			//if packetMissSequence, possibility to fill some missing packets in the sequence
			if (!isDuplicated)
            {
				if (seqnum < topSequence)
                {
					packetMissSequence++;
				}
                else
                {
					topSequence = seqnum;
					if (!isDuplicated || !found)
                    {
                        unexpectedPackets.add(seqnum);
					}
				}
			}
		}
		
		//preparation for the next packet
		receivedSequenceNumber.add(seqnum);
		lastSequenceNumber = seqnum;

		packetLost = missingSequenceNumber.size();//update in real time
		packetNumber = receivedSequenceNumber.size();//update in real time

		/**time counters computing**/

		currentDelta = (arrivedTime - lastArrivedTime);
        deltaList.add(currentDelta);
		if (currentDelta != 0)
        {
			currentBitRate = ((float)data) / (currentDelta / 1000);
			currentBitRate = currentBitRate / 1024;
			data = msg.getData().getBytes().length * 8;
		}
		else
        {
			data = data + msg.getData().getBytes().length * 8;
		}

		meanBitRate = ((meanBitRate * oldPacketNumber) + currentBitRate) / packetNumber;

		if(dico.getClockRate(msg.getPayloadType()) != -1)//check if the payload is known to compute jitter and other Qos parameter
		{
			currentPacketSpacing = currentDelta - (((msg.getTimestampRTP() - lastTimestampRTP) * 1000) / dico.getClockRate(msg.getPayloadType()));
            packetSpacingList.add(currentPacketSpacing);

            currentJitter = currentJitter + (Math.abs(currentPacketSpacing) - currentJitter)/16;
            jitterList.add(currentJitter);

			meanJitter = ((meanJitter * oldPacketNumber) + currentJitter) / packetNumber;
			meanPacketSpacing = ((meanPacketSpacing * oldPacketNumber) + currentPacketSpacing) / packetNumber;
		}

		meanDelta = ((meanDelta * oldPacketNumber) + currentDelta) / packetNumber;

		oldPacketNumber = packetNumber;
		lastTimestampRTP = msg.getTimestampRTP();
		lastArrivedTime = arrivedTime;
    }

    public void calculMOS(){
        int nbPacketTotal = this.getPacketNumber() - this.getDuplicated() + this.getPacketLost();
        float ppl = (float) this.getPacketLost() / (float) nbPacketTotal * 100;

        /**
         * TODO : This code has to be changed using a sliding window of 1 second for exemple
         * DO NOT ERASE IT
         */
        /*
        this.eModele.calcul(0, ppl, msg, false);
        */
        this.eModele.calcul(0, ppl, payloadType, meanDelta);
    }
    /**
     * TODO : This code has to be changed using a sliding window of 1 second for exemple
     * DO NOT ERASE IT
     * @param msg
     * @param ppl
     */
    /*
    public void calculMOSRT(MsgRtp[] msg, float ppl){
        this.eModele.calcul(0, ppl, msg, true);
    }
    */

	public int getDuplicated() {
		return duplicated;
	}
	public int getPacketMissSequence() {
		return packetMissSequence;
	}
	public int getPacketLost() {
		return packetLost;
	}
    public LinkedList<Float> getDelta() {
		return deltaList;
	}
    public float getMeanDelta() {
		return meanDelta;
	}
	public LinkedList<Float> getPacketSpacing() {
		return packetSpacingList;
	}
    public float getMeanPacketSpacing() {
		return meanPacketSpacing;
	}
    public LinkedList<Float> getJitter() {
		return jitterList;
	}
	public float getMeanJitter() {
		return meanJitter;
	}
	public float getMeanBitRate() {
		return meanBitRate;
	}

    public int getPacketNumber(){
        return this.packetNumber;
    }

    public EModele getEModele(){
        return this.eModele;
    }

    public void setPayloadType(int payloadType) {
        this.payloadType = payloadType;
    }
}