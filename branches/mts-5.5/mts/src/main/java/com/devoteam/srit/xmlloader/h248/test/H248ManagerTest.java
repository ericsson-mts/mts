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

package com.devoteam.srit.xmlloader.h248.test;

/** mesure de perf de la pile H248 
* sur ma machine : Pentium 4 hyperthreading 2,6 Gb 1 Gb RAM
* 100000 tests prennent 36.361s secondes soit environ 2750 msg /s   
*/


import com.devoteam.srit.xmlloader.core.utils.Utils;
import java.util.GregorianCalendar;

import com.devoteam.srit.xmlloader.core.Tester;
import com.devoteam.srit.xmlloader.core.log.GenericLogger;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.filesystem.LocalFSInterface;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import com.devoteam.srit.xmlloader.h248.MsgH248;


public class H248ManagerTest {
    
    private static GenericLogger logger;
          
    private static Tester tester;  
    
    private static String host;
    private static String port;
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
    	transport = Config.getConfigByName("sip.properties").getString("listenpoint.TRANSPORT");
    	port = Config.getConfigByName("sip.properties").getString("listenpoint.LOCAL_PORT");
                 
        String request = createRequest(0);
        System.out.println("length = " + request.length());
        
        Listenpoint listenpoint = StackFactory.getStack(StackFactory.PROTOCOL_H248).getListenpoint(null);

        MsgH248 msg = new MsgH248(request);            
        msg.setListenpoint(listenpoint);

        int maxIter = Config.getConfigByName("h248.properties").getInteger("NB_ITERATION", 100000);
        System.out.println("maxIter : " + maxIter);
        
        long beginTT = new GregorianCalendar().getTimeInMillis();
        for (int i = 1; i <= maxIter; i++) {
            // Create a message object corresponding to the string message
            if (i % 1000 == 0) {
                // System.out.println("i=" + i);
            }
            
            // MsgH248 msg = new MsgH248(request);            
            // msg.setListenpoint(listenpoint);
            
            // StackFactory.getStack(StackFactory.PROTOCOL_H248).sendMessage((Msg) msg);
            listenpoint.sendMessage((Msg) msg, host, 60000, transport);
            // StackFactory.getStack(StackFactory.PROTOCOL_H248).sendMessageException((Msg) msg, null, null, null);
            
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
        String request =
        	"MEGACO/3 [[Host-Server]]:[Port-Server]\r\n" +
        	"Transaction=[transID]{\r\n" +
        	" Context=${\r\n" +
        	"  Priority = 1,\r\n" +                 
        	"  Add=ip/1/access/${\r\n" +
        	"   Media{\r\n" +
        	"    Stream=1{\r\n" +
        	"     LocalControl{\r\n" +
        	"      Mode=IN,\r\n" +
        	"      tman/sdr=800,\r\n" +
        	"      ds/dscp=0x00,\r\n" +
        	"     },\r\n" +
        	"     Local{\r\n" +
        	"v=0\r\n" +
        	"o=mhandley 2890844526 2890842807 IN IP4 [Host-Tester]\r\n" +
        	"s=SDP Seminar\r\n" +
        	"c=IN IP4 $\r\n" +
        	"t=3034423619 3042462419\r\n" +
        	"m=video $ RTP/AVP 98\r\n" +
        	"a=rtpmap:98 amr/8000/1\r\n" +
        	"a=fmtp:98 mode-set=1+octet-align=1+crc=0\r\n" +
        	"c=IN IP4 $\r\n" +
        	"m=audio $ RTP/AVP 96\r\n" +
        	"a=rtpmap:96 L8/8000\r\n" +
        	"c=IN IP4 $\r\n" +
        	"     },\r\n" +
        	"     Remote{\r\n" +
        	"v=0\r\n" +
        	"o=mhandley 2890844526 2890842807 IN IP4 [Host-Tester]\r\n" +
        	"s=SDP Seminar\r\n" +
        	"c=IN IP4 [Host-Tester]/127\r\n" +
        	"m=video 49232/2 RTP/AVP 98\r\n" +
        	"a=rtpmap:98 amr/8000/1\r\n" +
        	"a=fmtp:98 mode-set=1+octet-align=1+crc=0\r\n" +
        	"c=IN IP4 [Host-Tester]/127\r\n" +
        	"m=audio 49230/3 RTP/AVP 96\r\n" +
        	"a=rtpmap:96 L8/8000\r\n" +
        	"c=IN IP4 [Host-Tester]/127\r\n" +
        	"     }\r\n" +
        	"    },\r\n" +
        	"    TerminationState {\r\n" +
        	"     ServiceStates = {\r\n" +
        	"      InService\r\n" +
        	"     },\r\n" +
        	"     Buffer = ON\r\n" +
        	"    }\r\n" +
        	"   }\r\n" +
        	"  }\r\n" +
        	" }\r\n" +
        	"}";
        return request;
    }

}
