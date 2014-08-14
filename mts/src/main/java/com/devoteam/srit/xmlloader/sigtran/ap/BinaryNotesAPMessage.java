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

package com.devoteam.srit.xmlloader.sigtran.ap;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.bn.CoderFactory;
import org.bn.IDecoder;
import org.bn.IEncoder;
import org.dom4j.Element;

import com.devoteam.srit.xmlloader.sigtran.ap.generated.tcap.Begin;
import com.devoteam.srit.xmlloader.sigtran.ap.generated.tcap.Component;
import com.devoteam.srit.xmlloader.sigtran.ap.generated.tcap.ComponentPortion;
import com.devoteam.srit.xmlloader.sigtran.ap.generated.tcap.DialoguePortion;
import com.devoteam.srit.xmlloader.sigtran.ap.generated.tcap.OrigTransactionID;
import com.devoteam.srit.xmlloader.sigtran.ap.generated.tcap.TCMessage;


/**
 *
 * @author fhenry
 */
public class BinaryNotesAPMessage extends APMessage {

	TCMessage tcMessage;
	DialoguePortion dp;
	ArrayList<Component> comps;

    public BinaryNotesAPMessage() {    	
    	Begin begin = new Begin();
    	OrigTransactionID otid = new OrigTransactionID();
    	byte[] transID = new byte[]{0,0,0,1};
    	otid.setValue(transID);
    	begin.setOtid(otid);
    	
    	this.dp = new DialoguePortion();
    	dp.setValue(new byte[]{(byte) 0x6b,(byte) 0x1e,(byte) 0x28,(byte) 0x1c,(byte) 0x06,(byte) 0x07,(byte) 0x00,(byte) 0x11,(byte) 0x86,(byte) 0x05,(byte) 0x01,(byte) 0x01,(byte) 0x01,(byte) 0xa0,(byte) 0x11,(byte) 0x60,(byte) 0x0f,(byte) 0x80, (byte) 0x02, (byte) 0x07, (byte) 0x80, (byte) 0xa1, (byte) 0x09, (byte) 0x06, (byte) 0x07, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x15, (byte) 0x02});
    	begin.setDialoguePortion(dp);
   
    	ComponentPortion cp = new ComponentPortion();
    	Component comp = new Component();
    	comp.setValue(new byte[]{(byte) 0xa1,(byte) 0x30,(byte) 0x02,(byte) 0x01,(byte) 0x01,(byte) 0x02,(byte) 0x01,(byte) 0x2e,(byte) 0x30,(byte) 0x28, (byte) 0x84,(byte) 0x07,(byte) 0x91,(byte) 0x33,(byte) 0x66,(byte) 0x60,(byte) 0x05,(byte) 0x67,(byte) 0xf9,(byte) 0x82, (byte) 0x07,(byte) 0x91,(byte) 0x33,(byte) 0x66,(byte) 0x31,(byte) 0x70,(byte) 0x71,(byte) 0xf3,(byte) 0x04,(byte) 0x14,(byte) 0x11,(byte) 0x08,(byte) 0x0b,(byte) 0x91,(byte) 0x33,(byte) 0x66,(byte) 0x60,(byte) 0x05,(byte) 0x67,(byte) 0xf7,(byte) 0x00,(byte) 0x00,(byte) 0xa9,(byte) 0x06,(byte) 0xf3,(byte) 0xf9,(byte) 0x7c,(byte) 0x3e,(byte) 0x9f,(byte) 0x03});
    	this.comps = new ArrayList<Component>();
    	this.comps.add(comp);
    	cp.setValue(comps);
    	begin.setComponents(cp);

    	this.tcMessage = new TCMessage();
    	this.tcMessage.selectBegin(begin); 

    	
    	/*= (TCBeginMessageImpl) TcapFactory.createTCBeginMessage();

        // build DP

        //if (event.getApplicationContextName() != null) {
            //this.dpSentInBegin = true;
            DialogPortion dp = TcapFactory.createDialogPortion();
            dp.setUnidirectional(false);
            DialogRequestAPDU apdu =  TcapFactory.createDialogAPDURequest();
            // Protocol-version = true
            apdu.setDoNotSendProtocolVersion(false);
            dp.setDialogAPDU(apdu);
            
            // application-context-name=0.4.0.0.1.0.21.2
            ApplicationContextName acn = new ApplicationContextNameImpl();
            acn.setOid(new long[] {0,4,0,0,1,0,21,2});
            apdu.setApplicationContextName(acn);
            if (event.getUserInformation() != null) {
                apdu.setUserInformation(event.getUserInformation());
                this.lastUI = event.getUserInformation();
            }
            tcbm.setDialogPortion(dp);

        //    if (this.provider.getStack().getStatisticsEnabled()) {
        //        String acn = ((ApplicationContextNameImpl) event.getApplicationContextName()).getStringValue();
        //        this.provider.getStack().getCounterProviderImpl().updateOutgoingDialogsPerApplicatioContextName(acn);
        //    }
        //} else {
        //    if (this.provider.getStack().getStatisticsEnabled()) {
        //        this.provider.getStack().getCounterProviderImpl().updateOutgoingDialogsPerApplicatioContextName("");
        //    }
        //}

        // now comps
    	// transaction ID= 00000001	
    	byte[] transID = new byte[]{0,0,0,1};
        tcbm.setOriginatingTransactionId(transID);
        // if (this.scheduledComponentList.size() > 0) {
            Component[] componentsToSend = new Component[1];
            componentsToSend[0] = TcapFactory.createComponentInvoke();
            ((Invoke) componentsToSend[0]).setInvokeId((long) 1);
            // ((Invoke) componentsToSend[0]).setLinkedId((long) 1);
            OperationCode opCode = TcapFactory.createOperationCode();
            opCode.setLocalOperationCode((long) 0x2e);
            ((Invoke) componentsToSend[0]).setOperationCode(opCode);
            //this.prepareComponents(componentsToSend);
            Parameter[] params = new Parameter[3];
            params[0] = TcapFactory.createParameter();
            params[0].setTag(0x04);
            params[0].setTagClass(0x02);
            params[0].setPrimitive(true);
            params[0].setSingleParameterInAsn();
            params[0].setData(new byte[]{(byte) 0x91, (byte) 0x33, (byte) 0x66, (byte) 0x60, (byte) 0x05, (byte) 0x67, (byte) 0xf9});
            params[1] = TcapFactory.createParameter();
            params[1].setTag(0x02);
            params[1].setTagClass(0x02);
            params[1].setPrimitive(true);
            params[1].setSingleParameterInAsn();
            params[1].setData(new byte[]{(byte) 0x91, (byte) 0x33, (byte) 0x66, (byte) 0x31, (byte) 0x70, (byte) 0x71, (byte) 0xf3});
            params[2] = TcapFactory.createParameter();
            params[2].setTag(0x04);
            params[2].setData(new byte[]{(byte) 0x11, (byte) 0x08, (byte) 0x0b, (byte) 0x91, (byte) 0x33, (byte) 0x66, (byte) 0x60, (byte) 0x05, (byte) 0x67, (byte) 0xf7, (byte) 0x00,  (byte) 0x00,  (byte) 0xa9,  (byte) 0x06,  (byte) 0xf3,  (byte) 0xf9,  (byte) 0x7c,  (byte) 0x3e,  (byte) 0x9f,  (byte) 0x03});
            // Parameter param = new Parameter[1];
            Parameter param = TcapFactory.createParameter();
            param.setTag(0x10);
            param.setTagClass(0x00);
            param.setParameters(params);
            param.setPrimitive(false);
            ((Invoke) componentsToSend[0]).setParameter(param);
            tcbm.setComponent(componentsToSend);
        //}
         */

    }

    public Array encode() throws Exception 
    {
       IEncoder<com.devoteam.srit.xmlloader.sigtran.ap.generated.tcap.TCMessage> encoder;
       encoder = CoderFactory.getInstance().newEncoder("BER");
       ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
       encoder.encode((com.devoteam.srit.xmlloader.sigtran.ap.generated.tcap.TCMessage) tcMessage, outputStream);
       Array array =Array.fromHexString(getHexString(outputStream.toByteArray()));
       return array;
    } 

    public void decode(Array array) throws Exception
    {
    	IDecoder decoder = CoderFactory.getInstance().newDecoder("BER");
        InputStream inputStream = new ByteArrayInputStream(array.getBytes());
        tcMessage = decoder.decode(inputStream, TCMessage.class);
    }

    public void parseFromXML(Element root) throws Exception {
        if (root.element("ASN1") != null) {
            List<Element> children = root.element("ASN1").elements();
            for (Element element : children) {
            	/* FH ne compile pas
            	XmlToAsn1 xml_asn1 = new XmlToAsn1();
                String PackageName = "com.devoteam.srit.xmlloader.h323.h225v7.";
                asn1 = xml_asn1.instanceClass(element.getName(), PackageName);
                xml_asn1.initObject(asn1, element, PackageName);
                */
            }
        }
    }

    public String toXML(Element root) throws Exception {
    	return null;
    }

    private static String getHexString(byte[] b) throws Exception {
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result +=
                    Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }
    
}
