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

package com.devoteam.srit.xmlloader.rtp.test;

/** Test fonctionnel de RTCP avec la pile Jain  
 */
import junit.framework.TestCase;

import com.devoteam.srit.xmlloader.core.Tester;
import com.devoteam.srit.xmlloader.core.log.GenericLogger;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.filesystem.LocalFSInterface;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import com.devoteam.srit.xmlloader.rtp.jmf.RtpManager;
import com.sun.media.rtp.util.Packet;
import com.sun.media.rtp.util.RTPPacket;

public class RtcpManagerTest extends TestCase {
     
    private static RtcpManagerTest test = new RtcpManagerTest();
    
    private static RtpManager rtpManager1 = null;
    private static RtpManager rtpManager2 = null;
    
    private static GenericLogger logger;
    
    private static Tester tester;    
                 
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        
        test.setUp();
        test.estRTCPFloor();
        test.testserver();
        test.tearDown();      
        
    }
    
    /**
     * junit setup method.
     * @throws Exception if any problem occurs.
     */
    protected void setUp() throws Exception {
        super.setUp();
              
        if (tester == null) {
            /*
             * Set the FSInterface to LocalFS.
             */
            SingletonFSInterface.setInstance(new LocalFSInterface());

            tester = Tester.buildInstance();   
            // RtpManager.getInstance().open("SBC.sip.france.fr", 10000, "172.16.21.23", 10000);
            rtpManager1 = new RtpManager(null);            
            rtpManager1.open("172.16.21.32", 10000, "172.16.21.32", 10000);            
            rtpManager1.setSDES("sip:fabien.henry@devoteam.com", "Henry Fabien");
            rtpManager2 = new RtpManager(null);            
            rtpManager2.open("172.16.21.32", 20000, "172.16.21.32", 20000);
            rtpManager2.setSDES("sip:pascale.henry@devoteam.com", "Henry Pascale");
            logger = GlobalLogger.instance().getApplicationLogger();
        }        
    }

    /**
     * junit setup method.
     * @throws Exception if any problem occurs.
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
       
    /**
     * testRTCPFloor() method.
     *
     */
    public void estRTCPFloor() throws Exception {                             
        
        int maxIter = Config.getConfigByName("rtp.properties").getInteger("NB_ITERATION");
        logger.debug(TextEvent.Topic.PROTOCOL, "maxIter : " + maxIter);
        for (int i = 0; i < 1; i++) {
            rtpManager1.requestFloorControl();
            rtpManager1.ackFloorControl();
            rtpManager1.releaseFloorControl();
            rtpManager2.requestFloorControl();
            rtpManager2.ackFloorControl();
            rtpManager2.releaseFloorControl();            
        }
        
        System.out.println("END");
    }

    /**
     * testRTPRawSender() method.
     *
     */
    public void testRTPRawSender() throws Exception {                             
        
        int maxIter = Config.getConfigByName("rtp.properties").getInteger("NB_ITERATION");
        logger.debug(TextEvent.Topic.PROTOCOL, "maxIter : " + maxIter);
        for (int i = 0; i < 2; i++) {
            int PACKET_SIZE = 1024;
            Packet packet = new Packet();            
            packet.data = new byte[PACKET_SIZE + 12];
            for (int j = 0; j < PACKET_SIZE; j++) {
                packet.data[12 + j] = (byte) j;
            }
            RTPPacket rtpPacket = new RTPPacket(packet);
            rtpPacket.ssrc = 0;
            rtpPacket.seqnum = i;
            rtpPacket.timestamp = i;
            rtpPacket.payloadType = 17;
            rtpPacket.payloadoffset = 12;
            rtpPacket.payloadlength = PACKET_SIZE;
            rtpPacket.calcLength();
            rtpPacket.assemble(1, false);
            rtpManager1.sendPacket(false, rtpPacket);  
            rtpManager2.sendPacket(false, rtpPacket);
    
            /* DON'T WORK ?
            RTPRawSender rawSender = new RTPRawSender(10000, "172.16.21.23");
            rawSender.sendTo(rtpPacket);            
            */
            
            /* DON'T WORK ?
            SSRCCache ssrcCache = RtpManager.getInstance().sender.getRtpSessionMgr().getSSRCCache();            
            RTPTransmitter rtpTransmitter = new RTPTransmitter(ssrcCache, 60000, "172.16.21.150");            
            */
            
            /*
            RTPRawSender rawSender = new RTPRawSender(10000, "172.16.21.23"); 
            SSRCCache ssrcCache = RtpManager.getInstance().sender.getRtpSessionMgr().getSSRCCache();
            RTPTransmitter rtpTransmitter = new RTPTransmitter(ssrcCache, rawSender);
            rtpTransmitter.getSender().sendTo(rtpPacket);
            */
            
            /* DON'T WORK ?
            rtpTransmitter.setSSRCInfo(ssrcCache.ourssrc);
            SessionAddress sessionaddress = new SessionAddress(InetAddress.getByName("172.16.21.244"), 60000); 
            Vector<SessionAddress> remoteAddresses = new Vector<SessionAddress>(); 
            remoteAddresses.addElement(sessionaddress);
            rtpTransmitter.getSender().setDestAddresses(remoteAddresses);
            */
            
            /* DON'T WORK ?
            // Define the remote address of the RTP session
            InetAddress ipAddr = InetAddress.getByName("172.16.21.244");
            SessionAddress destAddr = new SessionAddress(ipAddr, 20000);

            // Add the remote target to the RTP session
            RtpManager.getInstance().sender.getRtpSessionMgr().addTarget(destAddr);
            */ 
            
            // RTPTransmitter rtpTransmitter = RtpManager.getInstance().sender.getRtpSessionMgr().startDataTransmission(10000, "172.16.21.150");
            /*
            RTPTransmitter rtpTransmitter = RtpManager.getInstance().sender.getRtpSessionMgr().getRtpTransmitter();                       
            rtpTransmitter.getSender().sendTo(rtpPacket);
            */
        }
    
        System.out.println("Hit enter to terminate server");
        System.in.read();
        System.out.println("END");
    }

    
    /**
     * testServer() method.
     *
     */
    public void testserver() throws Exception {
        }

}
