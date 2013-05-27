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

package com.devoteam.srit.xmlloader.rtp;

import java.io.UnsupportedEncodingException;

import gp.utils.arrays.Array;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.srtp.RawPacket;
import com.devoteam.srit.xmlloader.srtp.SRTPCryptoContext;
import com.devoteam.srit.xmlloader.srtp.SRTPPolicy;
import com.devoteam.srit.xmlloader.srtp.SRTPTransformEngine;
import com.devoteam.srit.xmlloader.srtp.SRTPTransformer;

/**
 * @author bbouvier
 */
public class ListenpointRtp extends Listenpoint
{    
	private boolean isSecured = false;
	
	private SRTPTransformer cipherSender = null;
	private SRTPTransformer cipherReceiver = null;
	
	private SRTPCryptoContext srtpCryptoContext = null;
    public SRTPCryptoContext getSrtpCryptoContext() {
		return srtpCryptoContext;
	}

	/** Creates a new instance of Listenpoint */
    public ListenpointRtp(Stack stack) throws Exception
    {
        super(stack);
    }
    
	/** Creates a Listenpoint specific from XML tree*/
	public ListenpointRtp(Stack stack, Element root) throws Exception	
	{
		super(stack, root);
		if (root.element("srtpSender") != null)
			this.parseSRTPSender(root, 0);
		if (root.element("srtpReceiver") != null)
			this.parseSRTPSender(root, 1);
	}
	
	private void parseSRTPSender(Element root, int SR) throws UnsupportedEncodingException, Exception
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
		
    /** Creates a new instance of Listenpoint */
    public ListenpointRtp(Stack stack, String name, String host, int port) throws Exception
    {
    	super(stack, name, host, port);
    }

    /**
	 * @return the isSecured
	 */
	public boolean isSecured() {
		return isSecured;
	}

	/**
	 * @param isSecured the isSecured to set
	 */
	public void setSecured(boolean isSecured) {
		this.isSecured = isSecured;
	}

	/** Send a Msg to Listenpoint */
    @Override
    public synchronized boolean sendMessage(Msg msg, String remoteHost, int remotePort, String transport) throws Exception
    {
		if ((remoteHost == null) || (remotePort <= 0)) 
		{
            if(msg.getChannel() != null)
            {
                remoteHost = msg.getChannel().getRemoteHost();
                remotePort = msg.getChannel().getRemotePort();
            }
            else
            {
                throw new ExecutionException("Could not determine remote Host or remote Port");
            }
            msg.setRemoteHost(remoteHost);
            msg.setRemotePort(remotePort);
		}        

		if (isSecured && this.cipherSender != null)
		{
			// Compute RTP datas through Cryptographic context algorithm, add authentication tag at the end of datas and put them back into MsgRtp msg
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
    
    public RawPacket reverseTransformCipheredMessage(RawPacket rp)
    {
    	return this.cipherReceiver.reverseTransform(rp);
    }
    
    public int getCipheredAuthTagLength(int SR)
    {
    	if (SR == 0)
    		return this.cipherSender.getEngine().getSRTPPolicy().getAuthTagLength();
    	return this.cipherReceiver.getEngine().getSRTPPolicy().getAuthTagLength();
    }
    
    public boolean remove()
    {
    	this.cipherReceiver = null;
    	this.cipherSender = null;
    	this.isSecured = false;
    	return super.remove();
    }
}
