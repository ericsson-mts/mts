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
import gp.utils.arrays.SupArray;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.dom4j.Element;
import org.mobicents.protocols.asn.AsnInputStream;
import org.mobicents.protocols.asn.AsnOutputStream;
import org.mobicents.protocols.ss7.map.api.MAPDialog;
import org.mobicents.protocols.ss7.map.api.MAPMessage;
import org.mobicents.protocols.ss7.map.api.primitives.AddressNature;
import org.mobicents.protocols.ss7.map.api.primitives.AddressString;
import org.mobicents.protocols.ss7.map.api.primitives.ISDNAddressString;
import org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan;
import org.mobicents.protocols.ss7.map.api.service.sms.MAPServiceSms;
import org.mobicents.protocols.ss7.map.api.service.sms.SM_RP_DA;
import org.mobicents.protocols.ss7.map.api.service.sms.SmsSignalInfo;
import org.mobicents.protocols.ss7.map.primitives.AddressStringImpl;
import org.mobicents.protocols.ss7.map.primitives.ISDNAddressStringImpl;
import org.mobicents.protocols.ss7.map.service.sms.MoForwardShortMessageRequestImpl;
import org.mobicents.protocols.ss7.map.service.sms.SM_RP_DAImpl;
import org.mobicents.protocols.ss7.map.service.sms.SM_RP_OAImpl;
import org.mobicents.protocols.ss7.map.service.sms.SmsSignalInfoImpl;
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
 * @author fhenry
 */
public class MB_MAPMessageExperim extends APMessage {

	// MAPServiceSms mapMessage;
	MoForwardShortMessageRequestImpl mapMessage;
	TCBeginMessage tcbm;
	

    public MB_MAPMessageExperim() 
    {
    	// mapMessage = new MAPServiceSms();
        AddressString sca = new AddressStringImpl(AddressNature.international_number, NumberingPlan.ISDN, "33660650769");
        SM_RP_DA sm_RP_DA = new SM_RP_DAImpl(sca);
        ISDNAddressString msisdn = new ISDNAddressStringImpl(AddressNature.international_number, NumberingPlan.ISDN,
                "33661307173");
        SM_RP_OAImpl sm_RP_OA = new SM_RP_OAImpl();
        sm_RP_OA.setMsisdn(msisdn);
        SmsSignalInfo sm_RP_UI = new SmsSignalInfoImpl(new byte[] { (byte)0x11, (byte)0x08, (byte)0x0b, (byte)0x91, (byte)0x33, (byte)0x66, (byte)0x60, (byte)0x05, (byte)0x67, (byte)0xf7, (byte)0x00, (byte)0x00, (byte)0xa9, (byte)0x06, (byte)0xf3, (byte)0xf9, (byte)0x7c, (byte)0x3e, (byte)0x9f, (byte)0x03},
                null);
        mapMessage = new MoForwardShortMessageRequestImpl(sm_RP_DA, sm_RP_OA, sm_RP_UI, null, null);
        mapMessage.setInvokeId(1);

    	tcbm = (TCBeginMessageImpl) TcapFactory.createTCBeginMessage();

        // build TCAP layer

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
            dp.setOidValue(new long[] {0,0,17,773,1,1,1});
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
            /*
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
            */
            tcbm.setComponent(componentsToSend);
        //}


    }

    public Array encode() throws Exception 
    {
    	// Library mobicents
        AsnOutputStream aosMAP = new AsnOutputStream();
		mapMessage.encodeAll(aosMAP);
		Array arrayMAP = new DefaultArray(aosMAP.toByteArray());

		SupArray sup = new SupArray();
		Array arraySep = new DefaultArray(new byte[]{(byte) 0xa1, (byte) 0x30, (byte) 0x02, (byte) 0x01, (byte) 0x01, (byte) 0x02, (byte) 0x01, (byte) 0x2e});
		sup.addFirst(arraySep);
		sup.addLast(arrayMAP);
		
		Array subArray = sup.subArray(1);
		AsnInputStream inputStream = new AsnInputStream(subArray.getBytes());
		tcbm.getComponent()[0].decode(inputStream);
		//((Component) tcbm.getComponent()[0]).
		
		AsnOutputStream aosTCAP = new AsnOutputStream();
		tcbm.encode(aosTCAP);
		Array arrayTCAP = new DefaultArray(aosTCAP.toByteArray());
		
		//SupArray sup = new SupArray();
		//sup.addFirst(arrayTCAP);
		//Array arraySep = new DefaultArray(new byte[]{(byte) 0xa1, (byte) 0x30, (byte) 0x02, (byte) 0x01, (byte) 0x01, (byte) 0x02, (byte) 0x01, (byte) 0x2e});
		//sup.addLast(arraySep);
		//sup.addLast(arrayMAP);
		
		Array supArray = arrayTCAP.subArray(1);
		AsnInputStream aos = new AsnInputStream(supArray.getBytes());
    	tcbm.decode(aos);
		
        return arrayTCAP;
    }
          
    
    public void decode(Array array) throws Exception 
    {
		Array supArray = array.subArray(1);
    	AsnInputStream aos = new AsnInputStream(supArray.getBytes());
    	tcbm.decode(aos);
    }

    public void parseFromXML(Element root) throws Exception 
    {
        if (root.element("ASN1") != null) 
        {
            List<Element> children = root.element("ASN1").elements();
            for (Element element : children) 
            {
            	/* FH ne compile pas
            	XmlToAsn1 xml_asn1 = new XmlToAsn1();
                String PackageName = "com.devoteam.srit.xmlloader.h323.h225v7.";
                asn1 = xml_asn1.instanceClass(element.getName(), PackageName);
                xml_asn1.initObject(asn1, element, PackageName);
                */
            }
        }
    }

    public String toXML()
    {
    	return null;
    }

}
