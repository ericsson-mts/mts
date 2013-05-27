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

import gp.utils.arrays.Array;

import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.newstats.StatKey;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.rtp.MsgRtp;
import com.devoteam.srit.xmlloader.rtp.StackRtp;
import com.devoteam.srit.xmlloader.srtp.RawPacket;
import com.devoteam.srit.xmlloader.srtp.SRTPCryptoContext;
import com.devoteam.srit.xmlloader.srtp.SRTPPolicy;
import com.devoteam.srit.xmlloader.srtp.SRTPTransformEngine;
import com.devoteam.srit.xmlloader.srtp.SRTPTransformer;


/**
 * @author gpasquiers
 */
public class ListenpointRtpFlow extends Listenpoint {

    protected MsgRtpFlow _currentMessage = null;
    protected float endTimerNoPacket;
    protected float endTimerSilentFlow;
    protected float endTimerPeriodic;
    protected boolean qosMeasurment;
    protected boolean ignoreReceivedMessages;
    
    private boolean isSecured = false;
    public boolean isSecured() {
		return isSecured;
	}

	private SRTPTransformer cipherSender = null;
    private SRTPTransformer cipherReceiver = null;

    private RtpFlowEndTask _endOfFlowTask = null;

    private boolean _removed = false;

    /** Creates a Listenpoint specific from XML tree*/
    public ListenpointRtpFlow(Stack stack, Element root) throws Exception {
        super(stack, root);

        Element header = root.element("flow");
        if (root.element("srtpSender") != null)
        	this.parseSrtp(root, 0);
        if (root.element("srtpReceiver") != null)
        	this.parseSrtp(root, 1);

        this.endTimerNoPacket = ((StackRtpFlow) stack).endTimerNoPacket; // set the default value from config file
        if (header != null) {
            String endTimerNoPacketStr = header.attributeValue("endTimerNoPacket");
            //override value if given in listenpoint creation
            if (endTimerNoPacketStr != null) {
                this.endTimerNoPacket = Float.parseFloat(endTimerNoPacketStr); //set time given in attribute in seconds
            }
        }

        this.endTimerSilentFlow = ((StackRtpFlow) stack).endTimerSilentFlow; // set the default value from config file
        if (header != null) {

            String endTimerSilentFlowStr = header.attributeValue("endTimerSilentFlow");
            if (endTimerSilentFlowStr != null) {
                endTimerSilentFlow = Float.parseFloat(endTimerSilentFlowStr); //set time given in attribute in seconds
            }
        }

        this.endTimerPeriodic = ((StackRtpFlow) stack).endTimerPeriodic; // set the default value from config file
        if (header != null) {
            String endTimerPeriodicStr = header.attributeValue("endTimerPeriodic");
            if (endTimerPeriodicStr != null) {
                endTimerPeriodic = Float.parseFloat(endTimerPeriodicStr);//set time given in attribute in seconds
            }
        }

        this.qosMeasurment = ((StackRtpFlow) stack).qosMeasurment; // set the default value from config file
        if (header != null) {
            String qosMeasurmentStr = header.attributeValue("qosMeasurment");
            if (qosMeasurmentStr != null) {
                qosMeasurment = Boolean.parseBoolean(qosMeasurmentStr);
            }
        }

        this.ignoreReceivedMessages = ((StackRtpFlow) stack).ignoreReceivedMessages; // set the default value from config file
        if (header != null) {
            String ignoreReceivedMessagesStr = header.attributeValue("ignoreReceivedMessages");
            if (ignoreReceivedMessagesStr != null) {
                ignoreReceivedMessages = Boolean.parseBoolean(ignoreReceivedMessagesStr);
            }
        }
    }
    
    private void parseSrtp(Element root, int SR) throws Exception
    {
    	String algorithm = root.element(SR == 0 ? "srtpSender" : "srtpReceiver").attributeValue("algorithm");
		String[] algoExplode = algorithm.replace('_', ' ').split(" ");
		String masterKeyAndSalt = root.element(SR == 0 ? "srtpSender" : "srtpReceiver").attributeValue("masterKeyAndSalt");
		String keyDerivationRate = root.element(SR == 0 ? "srtpSender" : "srtpReceiver").attributeValue("keyDerivationRate");
		String mki = root.element(SR == 0 ? "srtpSender" : "srtpReceiver").attributeValue("mki");

		if (algoExplode.length != 6)
			throw new Exception("wrong cipher format : expected CIPHER_MODE_KEYLENGTH_AUTH_AUTHALGO_AUTHTAGLENGTH, got " + algorithm);
		
		byte[] masterKey = new byte[16];
		byte[] masterSalt = new byte[14];
		byte[] masterKeyAndSaltFromB64 = Array.fromBase64String(masterKeyAndSalt).getBytes();
		
		int KDR = 0;
		try {
			KDR = (int) (keyDerivationRate != null ? Math.pow(Integer.parseInt(keyDerivationRate.replace('^', ' ').split(" ")[0]), Integer.parseInt(keyDerivationRate.replace('^', ' ').split(" ")[1])) : 0);
		}
		catch (Exception e)
		{
			try {KDR = Integer.parseInt(keyDerivationRate);}
			catch (Exception e1) {}
		}
		
		if (masterKeyAndSaltFromB64.length != 30)
			throw new Exception("masterKeyAndSalt from Base64 has length not equals to 30 bytes : " + new String(masterKeyAndSaltFromB64, "UTF-8"));
		
		for (int i = 0; i < 16; i++)
    		masterKey[i] = masterKeyAndSaltFromB64[i];
    	for (int i = 0; i < 14; i++)
    		masterSalt[i] = masterKeyAndSaltFromB64[i + 16];
		
    	SRTPPolicy srtpPolicy = new SRTPPolicy(algoExplode);
		
		this.setSecured(true);
		
		SRTPTransformEngine engine = new SRTPTransformEngine(masterKey, masterSalt, srtpPolicy, srtpPolicy, null);
		
		if (SR == 0)
			this.cipherSender = new SRTPTransformer(engine);
		if (SR == 1)
			this.cipherReceiver = new SRTPTransformer(engine);
    }
    
    private void setSecured(boolean b) {
		// TODO Auto-generated method stub
		this.isSecured = b;
	}

	/** Creates a new instance of Listenpoint */
    public ListenpointRtpFlow(Stack stack, String name, String host, int port) throws Exception
    {
    	super(stack, name, host, port);
    	
        this.endTimerNoPacket = ((StackRtpFlow) stack).endTimerNoPacket; // set the default value from config file
        this.endTimerSilentFlow = ((StackRtpFlow) stack).endTimerSilentFlow; // set the default value from config file
        this.endTimerPeriodic = ((StackRtpFlow) stack).endTimerPeriodic; // set the default value from config file
        this.qosMeasurment = ((StackRtpFlow) stack).qosMeasurment; // set the default value from config file
        this.ignoreReceivedMessages = ((StackRtpFlow) stack).ignoreReceivedMessages; // set the default value from config file
    }
        
    /** Create a listenpoint to each Stack */
    @Override
    public boolean create(String protocol) throws Exception {
        boolean res = super.create(protocol);
        listenpointUdp.setAttachment(this);
        return res;
    }

    /** Create a listenpoint to each Stack */
    @Override
    public boolean remove() {
        _removed = true;
        this.cipherReceiver = null;
    	this.cipherSender = null;
    	this.isSecured = false;
        return super.remove();
    }

    public boolean removed(){
        return _removed;
    }

    public String toString() {
        String ret = super.toString();
        ret += " endTimerNoPacket = " + endTimerNoPacket;
        if (endTimerSilentFlow != 0) {
            ret += " endTimerSilentFlow = " + endTimerSilentFlow;
        }
        if (endTimerPeriodic != 0) {
            ret += " endTimerPeriodic = " + endTimerPeriodic;
        }
        return ret;
    }



    protected synchronized void receiveMessage(MsgRtp message) throws Exception {
        // get the stack now (simpler)
        // StackRtpFlow stack = (StackRtpFlow) StackFactory.getStack(StackFactory.PROTOCOL_RTPFLOW);

        // create and start the endOfFlow task only when receiving the very first message
        if(null == _endOfFlowTask){
            _endOfFlowTask = new RtpFlowEndTask(this);
            // this task should remove itself once listenpoint is removed
            ((StackRtpFlow) this.stack).scheduler.execute(_endOfFlowTask, false);
        }

        // check if the packet is silent or not if necessary (endTimerSilentFlow "actif")
        if(endTimerSilentFlow >= 0){
            message.setIsSilence(((StackRtpFlow) this.stack).isSilentPacket(message.getData()));
        }

        // if we must handle the packet (received at least one packet, silent filtering disabled, or packet is not silence)
        if (_currentMessage != null || !((StackRtpFlow) this.stack).silentFiltering || !message.isSilence()) {

            // this is the first packet, create the RTPFlow message to receive the RTP packets
            if (null == _currentMessage) {
                _currentMessage = new MsgRtpFlow(((StackRtpFlow) this.stack).getCodecDict());
                _currentMessage.setChannel(message.getChannel());
                _currentMessage.setListenpoint(this);            
                _currentMessage.setProbe(message.getProbe());                
            }

            // update the timestamp for last received packet
            _currentMessage.updateLastPacketTimestamp();

            // add the rtp message to the rtpflow message
            _currentMessage.addReceivedPacket(message);

            // update the timestamp and counter for last non-silence received packet
            if(!message.isSilence()){
                _currentMessage.updateLastNonSilencePacketTimestamp();
                _currentMessage.setPacketNumberWithFilter(_currentMessage.getPacketNumber());
            }

            // update statistics
            StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TRANSPORT, _currentMessage.getTransport(), StackFactory.PROTOCOL_RTP, message.getTypeComplete() + StackFactory.PREFIX_INCOMING, "_transportNumber"), 1);
            StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TRANSPORT, _currentMessage.getTransport(), StackFactory.PROTOCOL_RTP, message.getTypeComplete() + StackFactory.PREFIX_INCOMING, "_transportBytes"), (float) message.getLength() / 1024 / 1024);
        }
    }
    
    /** Send a Msg to Listenpoint */
    @Override
    public synchronized boolean sendMessage(Msg msg, String remoteHost, int remotePort, String transport) throws Exception
    {
    	if (this.isSecured && this.cipherSender != null)
    	{
    		byte[] msgData = msg.getBytesData();
			
			RawPacket rp = new RawPacket(msgData, 0, msgData.length);
			rp = this.cipherSender.transform(rp);
			
			byte[] cipheredMsgData = rp.getBuffer();
			
			MsgRtp tmpMsg = (MsgRtp) msg;
			tmpMsg.cipherThisMessage(cipheredMsgData);
			
			msg = tmpMsg;
    	}
    	return super.sendMessage(msg, remoteHost, remotePort, transport);
    }

	public RawPacket reverseTransformCipheredMessage(RawPacket rp) {
		// TODO Auto-generated method stub
		return this.cipherReceiver.reverseTransform(rp);
	}

	public int getCipheredAuthTagLength(int SR) {
		// TODO Auto-generated method stub
		if (SR == 0)
    		return this.cipherSender.getEngine().getSRTPPolicy().getAuthTagLength();
    	return this.cipherReceiver.getEngine().getSRTPPolicy().getAuthTagLength();
	}
	
	public SRTPTransformer getSRTPTransformer(int SR)
	{
		if (SR == 0)
			return this.cipherSender;
		return this.cipherReceiver;
	}
}
