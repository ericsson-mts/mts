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

package com.devoteam.srit.xmlloader.udp.test;

/** mesure de perf de la pile UDP 
* sur ma machine : Pentium 4 hyperthreading 2,6 Gb 1 Gb RAM
* 10000 tests prennent 7.426s secondes soit environ 1346.62 msg /s   
*/

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.GregorianCalendar;

import com.devoteam.srit.xmlloader.core.Tester;
import com.devoteam.srit.xmlloader.core.log.GenericLogger;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.filesystem.LocalFSInterface;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import com.devoteam.srit.xmlloader.udp.MsgUdp;
import com.devoteam.srit.xmlloader.udp.bio.ChannelUdpBIO;


public class UdpManagerTest {
    
    private static GenericLogger logger;
          
    private static Tester tester;  
    
    private static String host;
    private static int port;
    private static String transport;
        
    
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        
        /*
         * Set the FSInterface to LocalFS.
         */
        SingletonFSInterface.setInstance(new LocalFSInterface());
    	    	
        if (tester == null) {
            tester = Tester.buildInstance();
        }
        logger = GlobalLogger.instance().getApplicationLogger();

        host = Utils.getLocalAddress().getHostAddress();
        port = 20000;
    	transport = Config.getConfigByName("udp.properties").getString("listenpoint.TRANSPORT");

        Listenpoint listenpoint = StackFactory.getStack(StackFactory.PROTOCOL_UDP).getListenpoint(null);
        InetSocketAddress localDatagramSocketAddress = new InetSocketAddress(host, port);
     	DatagramSocket datagramSocket = new DatagramSocket(localDatagramSocketAddress);
               
    	String packet = createRequest(0);
    	byte[] data = Utils.parseBinaryString(packet);
        System.out.println("length = " + data.length);
        
        MsgUdp msg = new MsgUdp(data, data.length);
        msg.setListenpoint(listenpoint);
        
        int maxIter = Config.getConfigByName("udp.properties").getInteger("NB_ITERATION", 100000);
        System.out.println("maxIter : " + maxIter);
        
        long beginTT = new GregorianCalendar().getTimeInMillis();
        for (int i = 1; i <= maxIter; i++) {
            // Create a message object corresponding to the string message
            if (i % 1000 == 0) {
                // System.out.println("i=" + i);
            }
            
            // MsgUdp msg = new MsgUdp(data, data.length);
            // msg.setListenpoint(listenpoint);
            
            // StackFactory.getStack(StackFactory.PROTOCOL_UDP).sendMessage((Msg) msg);
            // String address = "172.16.21.32" + ":" + "5060" + i;
            // Channel channel = new ChannelUdpBIO(null, "172.16.21.32", 7070 + i, "172.16.21.32", 7070 + i, "UDP");
            listenpoint.sendMessage((Msg) msg, host, port, transport);
            // StackFactory.getStack(StackFactory.PROTOCOL_UDP).sendMessage(msg);
            // StackFactory.getStack(StackFactory.PROTOCOL_UDP).sendMessageException((Msg) msg, null, null, null);
            
            /*
            for (int j = 1; j <= 0; j++) {
            	msg.getParameter("message.request");
            	msg.getParameter("message.type");
            	msg.getParameter("message.result");
            	msg.getParameter("message.length");
            	msg.getParameter("header.To.Parameter.tag");
            	msg.getParameter("header.From.Parameter.tag");
            	msg.getParameter("header.Call-ID");
            	msg.getParameter("header.Cseq");
            	msg.getParameter("header.Via");
            	msg.getParameter("header.Contact");
            }
            */
        }
        long endTT = new GregorianCalendar().getTimeInMillis();
        float duration = ((float)(endTT - beginTT)) / 1000;
        float flow = maxIter / duration;
        System.out.println("nombre trans = " + maxIter + " msg.");
        System.out.println("duration = " + duration + " s.");
        System.out.println("msg flow = " + flow + " msg/s.");

        System.out.println("Hit enter to terminate server");
        System.in.read();

    }
    
    public static String createRequest(int i) throws Exception {
        // Create a message object corresponding to the string message
        String packet ="h8011000000000000000621dde7e4e2e7e2dfe0dedddfe1e0dee0dfdedcdde0dfdedfe0e5e6dfdfe3eae4de" +
        				"dedfdddde0dfdfdde2e0dddee0e6e3dfe2e5e3dfdddddfdddedee0e0dfdfdcdcdedfdfdcdbdbdfdcdcdde4" +
        				"e2dddbdddfdcdcdededcdcdededcdadbdfdddadcdfdddcdde1e0dddcdddad8dadddcdbd9d8dbd9d9dadedb" +
        				"d8dbdfdbd9dbdcdad9dddfdcdededbd8dcdedad8dbded9d7dbdfddd9dde4dedbdededad7dadedcd9dadddd";
        return packet;
    }

}
