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

package com.devoteam.srit.xmlloader.sip.test;

/** mesure de perf de la pile SIP 
* sur ma machine : Pentium 4 hyperthreading 2,6 Gb 1 Gb RAM
* 10000 tests prennent 7.426s secondes soit environ 1346.62 msg /s   
*/


import com.devoteam.srit.xmlloader.core.utils.Utils;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
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
import com.devoteam.srit.xmlloader.sip.light.MsgSipLight;


public class SipManagerTest {
    
    private static GenericLogger logger;
          
    private static Tester tester;  
    
    private static String host;
    private static int port;
    private static String transport;
    
    // Jain private static MessageFactory msgFact;       
    
    
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
    	port = 6000; 
    	transport = Config.getConfigByName("sip.properties").getString("listenpoint.TRANSPORT");
    	
        Listenpoint listenpoint = StackFactory.getStack(StackFactory.PROTOCOL_SIP).getListenpoint(null);
        InetSocketAddress localDatagramSocketAddress = new InetSocketAddress(host, port);
     	DatagramSocket datagramSocket = new DatagramSocket(localDatagramSocketAddress);
        
        // Jain msgFact=SipFactory.getInstance().createMessageFactory();
        // String request = createRequestINVITE(0);
        String request = createRequestREGISTER(0);
        //Jain req = (SIPRequest) msgFact.createRequest(request);
        //Jain MsgSipJain msg = new MsgSipJain(request, false);
        MsgSipLight msg = new MsgSipLight(request, false, 0);
        msg.setListenpoint(listenpoint);
        System.out.println("length = " + request.length());
        
        int maxIter = Config.getConfigByName("sip.properties").getInteger("NB_ITERATION", 100000);
        System.out.println("maxIter : " + maxIter);

        long beginTT = new GregorianCalendar().getTimeInMillis();
        for (int i = 1; i <= maxIter; i++) {
            // Create a message object corresponding to the string message
            if (i % 1000 == 0) {
                // System.out.println("i=" + i);
            }
            
            // Jain req = (SIPRequest) msgFact.createRequest(request);
            // Jain MsgSipJain msg = new MsgSipJain(request, false);
            // String request = createRequestINVITE(i);
            // String request = createRequestREGISTER(i);            
            // MsgSipLight msg = new MsgSipLight(request, false, false);           
            // msg.setListenpoint(listenpoint);
            
            // StackFactory.getStack(StackFactory.PROTOCOL_SIP).sendMessage((Msg) msg);
            listenpoint.sendMessage((Msg) msg, host, port, transport);
            // StackFactory.getStack(StackFactory.PROTOCOL_SIP).sendMessageException((Msg) msg, null, null, null);
            
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
        System.out.println("nombre msg = " + maxIter + " msg.");
        System.out.println("duration = " + duration + " s.");
        System.out.println("msg flow = " + flow + " msg/s.");

        System.out.println("Hit enter to terminate server");
        System.in.read();

    }
    
    public static String createRequestINVITE(int i) throws Exception {        
        // Create a message object corresponding to the string message
        String request =
        	"INVITE sip:bob@devoteam.com SIP/2.0\r\n" +
        	"To: sip:bob@devoteam.com\r\n" +
        	"From: \"alice\" <sip:alice@devoteam.com>;tag=0123456789\r\n" +
        	"Via: SIP/2.0/" + transport + " " + host + ":" + port + ";branch=z9hG4bK" + i + "\r\n" +
	        "Call-ID: " + i + "\r\n" +
	        "CSeq: " + i + " INVITE\r\n" +
	        "Max-Forwards: 70\r\n" +
	        "Proxy-Authorization: IMS_GPRS_SCHEMA token=\"999\"\r\n" +
	        "Allow: INVITE, ACK, BYE, CANCEL, REFER, MESSAGE, SUBSCRIBE, NOTIFY, PUBLISH\r\n" +
	        "Contact: <sip:" + host + ":" + port + ">\r\n" +
	        "Supported: timer\r\n" +
	        "User-Agent: PoC-client/OMA1.0 XmlLoader/v0.0\r\n" +
	        "Session-Expires: 3600;refresher=uac\r\n" +
	        "Accept-Contact: +g.poc.talkburst;require;explicit\r\n" +
	        "P-Alerting-Mode: manual\r\n" +
	        "P-Asserted-Identity: \"Bruno\" <sip:alice@devoteam.com>,<tel:+3381164951574>\r\n" +
	        "P-Charging-Vector: icid-value=0.6645971655716005;ggsn=139.10.69.237;pdp-sig=yes;gcid=60ee19cf;auth-token=0.4849361942495226\r\n" +
	        "P-com.Siemens.MSISDN-ID: 3381164951574\r\n" +
	        "P-com.Siemens.IMSI-ID: 81164951574\r\n" +
	        "P-com.Siemens.SGSN-ID: " + host + "\r\n" +
	        "P-Charging-Function-Addresses: ccf=" + host + ";ccf_sec=" + host + "\r\n" +
	        "Route: <sip:alice@" + host + ":" + port + ";mode=originating;transport=" + transport + ";lr>\r\n" +
	        "Route: <sip:alice@" + host + ":" + port + ";mode=originating;transport=" + transport + ";lr>\r\n" +
	        "Content-Type: multipart/mixed;boundary=\"----=_Part_0\"\r\n" +
	        "Content-Length: x\r\n" +
	        "\r\n" +
	        "------=_Part_0\r\n" +
	        "Content-Type: application/sdp\r\n" +
	        "\r\n" +
	        "v=0\r\n" +
	        "o=alice 999 999 IN IP4 " + host + "\r\n" +
	        "s=-\r\n" +
	        "c=IN IP4 " + host + "\r\n" +
	        "t=0 0\r\n" +
	        "m=audio 45200 RTP/AVP 97\r\n" +
	        "a=rtpmap:97 amr/8000/1\r\n" +
	        "a=fmtp:97 mode-set=1;octet-align=1;crc=0\r\n" +
	        "a=+g.poc.talkburst\r\n" +
	        "a=maxptime:200\r\n" +
	        "a=ptime:200\r\n" +
	        "m=application 25463 udp TBCP\r\n" +
	        "a=fmtp:TBCP queuing=0;tb_priority=1;timestamp=0\r\n" +
	        "\r\n" +
	        " ------=_Part_0\r\n" +
	        "Content-Type: application/resource-lists+xml\r\n" +
	        "Content-Disposition: recipient-list\r\n" +
	        "\r\n" +
	        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
	        "<resource-lists xmlns=\"urn:ietf:params:xml:ns:resource-lists\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n" +
	        "<list>\r\n" +
	        "<entry uri=\"bob\"/>\r\n" +
	        "</list>\r\n" +
	        "</resource-lists>\r\n" +
	        "------=_Part_0--\r\n";
        return request;
    }

    public static String createRequestREGISTER(int i) throws Exception {
        // Create a SIP message object corresponding to the string message
        String request =
        	"REGISTER sip:alice@" + host + ":" + port + " SIP/2.0\r\n" +
        	"To: \"alice\" <sip:alice@devoteam.com>\r\n" +
        	"From: \"alice\" <sip:alice@devoteam.com>;tag=abcdefghij\r\n" +
        	"Call-ID: " + i + "\r\n" +
        	"CSeq: " + i + " REGISTER\r\n" +
        	"Via: SIP/2.0/" + transport.toUpperCase() + " " + host + ":" + port + ";branch=z9hG4bK" + i + "\r\n" +
        	"Max-Forwards: 70\r\n" +
        	"Contact: <sip:alice@" + host + ":" + port + ">\r\n" +
        	"Content-Length: 0\r\n";
        return request;
    }

}
