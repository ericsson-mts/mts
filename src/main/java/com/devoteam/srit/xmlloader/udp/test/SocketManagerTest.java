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

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.GregorianCalendar;

import com.devoteam.srit.xmlloader.core.utils.Utils;


public class SocketManagerTest {
        
    private static String host;
    private static int port;
    private static String transport;
            
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
    	
        host = Utils.getLocalAddress().getHostAddress();
        port = 10000;
    	transport = "udp";

    	InetSocketAddress localDatagramSocketAddress = new InetSocketAddress(host, port);
    	DatagramSocket datagramSocket = new DatagramSocket(localDatagramSocketAddress);
    	
    	String packet = createRequest(0);
    	byte[] data = Utils.parseBinaryString(packet);
        System.out.println("length = " + data.length);

		DatagramPacket dp = new DatagramPacket(data, data.length);
        InetSocketAddress remoteDatagramSocketAddress = new InetSocketAddress(host, port);
        dp.setSocketAddress(remoteDatagramSocketAddress);

        int maxIter = 100000;
        System.out.println("maxIter : " + maxIter);
        
        long beginTT = new GregorianCalendar().getTimeInMillis();
        for (int i = 1; i <= maxIter; i++) {
            // Create a message object corresponding to the string message
            if (i % 1000 == 0) {
                // System.out.println("i=" + i);
            }
            
            // MsgUdp msg = new MsgUdp(data, data.length);
            // msg.setListenpoint(listenpoint);
            
    		dp.setData(data);
            // remoteDatagramSocketAddress = new InetSocketAddress(host, port);
            dp.setSocketAddress(remoteDatagramSocketAddress);
    		
			datagramSocket.send(dp);            
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
