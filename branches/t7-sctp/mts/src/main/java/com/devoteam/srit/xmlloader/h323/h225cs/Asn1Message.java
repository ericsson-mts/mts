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

package com.devoteam.srit.xmlloader.h323.h225cs;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;

import java.util.List;

import org.dom4j.Element;
import org.mobicents.protocols.asn.AsnOutputStream;
import org.mobicents.protocols.ss7.tcap.asn.ApplicationContextName;
import org.mobicents.protocols.ss7.tcap.asn.ApplicationContextNameImpl;
import org.mobicents.protocols.ss7.tcap.asn.DialogPortion;
import org.mobicents.protocols.ss7.tcap.asn.DialogRequestAPDU;
import org.mobicents.protocols.ss7.tcap.asn.TCBeginMessageImpl;
import org.mobicents.protocols.ss7.tcap.asn.TcapFactory;
import org.mobicents.protocols.ss7.tcap.asn.comp.Component;
import org.mobicents.protocols.ss7.tcap.asn.comp.Invoke;
import org.mobicents.protocols.ss7.tcap.asn.comp.OperationCode;
import org.mobicents.protocols.ss7.tcap.asn.comp.Parameter;
import org.mobicents.protocols.ss7.tcap.asn.comp.TCBeginMessage;


/**
 *
 * @author gansquer
 */
public class Asn1Message {

	TCBeginMessage tcbm;
	
    Object asn1;

    public Asn1Message() {
    	tcbm = (TCBeginMessageImpl) TcapFactory.createTCBeginMessage();

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
            /*
            if (event.getUserInformation() != null) {
                apdu.setUserInformation(event.getUserInformation());
                this.lastUI = event.getUserInformation();
            }
            */
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

    }

    public Array encode() throws Exception 
    {
        AsnOutputStream aos = new AsnOutputStream();
		tcbm.encode(aos);
		Array array = new DefaultArray(aos.toByteArray());
	

    	/* FH ne compile pas 
        IEncoder<com.devoteam.srit.xmlloader.h323.h225v7.H323_UserInformation> encoder;
        IDecoder decoder;
        try {
            encoder = CoderFactory.getInstance().newEncoder("PER/U");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            encoder.encode((com.devoteam.srit.xmlloader.h323.h225v7.H323_UserInformation) asn1, outputStream);
            Array returnArray =Array.fromHexString(getHexString(outputStream.toByteArray()));

          	decoder = CoderFactory.getInstance().newDecoder("PER/U");
          	InputStream inputStream;
          	H323_UserInformation decodedUserInformation = decoder.decode(inputStream, H323_UserInformation.class);
            
            return returnArray;
        }
        catch (Exception ex) {
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, ex, "Error ASN1 : ");
        }
        */
        return array;
    }

    public void parseArray(Array array) throws Exception {
    }

    public void parseElement(Element root) throws Exception {
        if (root.element("ASN1") != null) {
            List<Element> children = root.element("ASN1").elements();
            for (Element element : children) {
            	/* FH ne compile pas */
            	XmlToAsn1 xml_asn1 = new XmlToAsn1();
                String PackageName = "com.devoteam.srit.xmlloader.h323.h225v7.";
                asn1 = xml_asn1.instanceClass(element.getName(), PackageName);
                xml_asn1.initObject(asn1, element, PackageName);
                /**/
            }
        }
    }

    public static String getHexString(byte[] b) throws Exception {
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result +=
                    Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }
    
}
