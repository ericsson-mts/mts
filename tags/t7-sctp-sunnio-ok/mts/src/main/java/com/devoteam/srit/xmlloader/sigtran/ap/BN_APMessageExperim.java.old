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
import org.mobicents.protocols.asn.AsnOutputStream;
import org.mobicents.protocols.ss7.tcap.asn.ApplicationContextName;
import org.mobicents.protocols.ss7.tcap.asn.ApplicationContextNameImpl;
// import org.mobicents.protocols.ss7.tcap.asn.DialogPortion;
import org.mobicents.protocols.ss7.tcap.asn.DialogRequestAPDU;
import org.mobicents.protocols.ss7.tcap.asn.TcapFactory;

import com.devoteam.srit.xmlloader.sigtran.ap.generated.map.Component;

import com.devoteam.srit.xmlloader.sigtran.ap.generated.map.ISDN_AddressString;
import com.devoteam.srit.xmlloader.sigtran.ap.generated.map.Invoke;
import com.devoteam.srit.xmlloader.sigtran.ap.generated.map.InvokeIdType;
import com.devoteam.srit.xmlloader.sigtran.ap.generated.map.InvokeParameter;
import com.devoteam.srit.xmlloader.sigtran.ap.generated.map.Mo_forwardSM_Arg;
import com.devoteam.srit.xmlloader.sigtran.ap.generated.map.Operation;
import com.devoteam.srit.xmlloader.sigtran.ap.generated.map.OperationLocalvalue;
import com.devoteam.srit.xmlloader.sigtran.ap.generated.map.ServiceCentreAddress;
import com.devoteam.srit.xmlloader.sigtran.ap.generated.map.Sm_RP_DA;
import com.devoteam.srit.xmlloader.sigtran.ap.generated.map.Sm_RP_OA;
import com.devoteam.srit.xmlloader.sigtran.ap.generated.map.Sm_RP_UI;
import com.devoteam.srit.xmlloader.sigtran.ap.generated.tcap.Begin;
import com.devoteam.srit.xmlloader.sigtran.ap.generated.tcap.ComponentPortion;
import com.devoteam.srit.xmlloader.sigtran.ap.generated.tcap.DialoguePortion;
import com.devoteam.srit.xmlloader.sigtran.ap.generated.tcap.OrigTransactionID;
import com.devoteam.srit.xmlloader.sigtran.ap.generated.tcap.TCMessage;


/**
 *
 * @author fhenry
 */
public class BN_APMessageExperim extends APMessage {

	// TCAP layer
	TCMessage tcMessage;
	// Begin tcapBegin;
	// DialoguePDU dialoguePdu;
	org.mobicents.protocols.ss7.tcap.asn.DialogPortion dp;
	// ArrayList<com.devoteam.srit.xmlloader.sigtran.ap.generated.tcap.Component> comps;
	
	//MAP layer
	Component mapComponent; 

    public BN_APMessageExperim() 
    {
    	
    	// Define TCMessages (TCAP.asn file)
    	Begin tcapBegin = new Begin();
    	OrigTransactionID otid = new OrigTransactionID();
    	byte[] transID = new byte[]{0,0,0,1};
    	otid.setValue(transID); 
    	tcapBegin.setOtid(otid);
    	ComponentPortion cp = new ComponentPortion();
    	com.devoteam.srit.xmlloader.sigtran.ap.generated.tcap.Component comp = new com.devoteam.srit.xmlloader.sigtran.ap.generated.tcap.Component();
    	ArrayList<com.devoteam.srit.xmlloader.sigtran.ap.generated.tcap.Component> comps = new ArrayList<com.devoteam.srit.xmlloader.sigtran.ap.generated.tcap.Component>();
    	comps.add(comp);
    	cp.setValue(comps);
    	tcapBegin.setComponents(cp);
    	this.tcMessage = new TCMessage();
    	this.tcMessage.selectBegin(tcapBegin); 
    	// this.dialoguePdu.initWithDefaults();
    	
    	// Define TCMessages (TCAP.asn file)
    	/* BUG en encodage avec BN : Remplacement par Mobicents
    	this.dialoguePdu = new DialoguePDU();
    	this.dialoguePdu.initWithDefaults();
    	AARQ_apdu aarq = new AARQ_apdu();
    	AARQ_apduSequenceType aarq_apduSequenceType = new AARQ_apduSequenceType();
    	BitString pv = new BitString();
    	pv.setValue(new byte[]{(byte) 0x80});
    	aarq_apduSequenceType.setProtocol_version(pv);
    	ObjectIdentifier oi = new ObjectIdentifier();
    	oi.setValue("0.4.0.0.1.0.21.2");
    	aarq_apduSequenceType.setApplication_context_name(oi);
    	//ArrayList<byte[]> userInformations = new ArrayList<byte[]>(); 
    	aarq_apduSequenceType.setUser_information(null);
    	aarq.setValue(aarq_apduSequenceType);
    	this.dialoguePdu.selectDialogueRequest(aarq);
    	*/
    	this.dp = TcapFactory.createDialogPortion();
        this.dp.setUnidirectional(false);
        DialogRequestAPDU apdu =  TcapFactory.createDialogAPDURequest();
        // Protocol-version = true
        apdu.setDoNotSendProtocolVersion(false);
        this.dp.setDialogAPDU(apdu);
        
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
        this.dp.setOidValue(new long[] {0,0,17,773,1,1,1});

    	// define MAP messages (MAP.asn file)
    	this.mapComponent = new Component();
    	Invoke invoke = new Invoke();
    	
    	Operation op = new Operation();
    	OperationLocalvalue opLocal = new OperationLocalvalue();
    	opLocal.setValue(46L);
    	op.selectLocalValue(opLocal);
    	invoke.setOpCode(op);
    	
    	InvokeIdType invType = new InvokeIdType(); 
    	invType.setValue(1);
    	invoke.setInvokeID(invType);
    	
    	InvokeParameter invokeParameter = new InvokeParameter();
    	invoke.setInvokeparameter(invokeParameter);
    	Mo_forwardSM_Arg moforwardSM_Arg = new Mo_forwardSM_Arg();
    	Sm_RP_DA sm_rp_da = new Sm_RP_DA();
    	ServiceCentreAddress serviceCentreAddress = new ServiceCentreAddress();
    	serviceCentreAddress.setValue(new byte[]{(byte)0x91,(byte)0x33,(byte)0x66,(byte)0x60,(byte)0x05,(byte)0x67,(byte)0xf9});
    	sm_rp_da.selectServiceCentreAddressDA(serviceCentreAddress);
    	moforwardSM_Arg.setSm_RP_DA(sm_rp_da);
    	
    	Sm_RP_OA sm_rp_oa = new Sm_RP_OA();
    	ISDN_AddressString isdn_AddressString = new ISDN_AddressString();
    	isdn_AddressString.setValue(new byte[]{(byte)0x91,(byte)0x33,(byte)0x66,(byte)0x31,(byte)0x70,(byte)0x71,(byte)0xf3});
    	sm_rp_oa.selectMsisdn(isdn_AddressString);
    	moforwardSM_Arg.setSm_RP_OA(sm_rp_oa);
    	
    	Sm_RP_UI sm_rp_ui = new Sm_RP_UI();
    	sm_rp_ui.setValue(new byte[]{(byte)0x11,(byte)0x08,(byte)0x0b,(byte)0x91,(byte)0x33,(byte)0x66,(byte)0x60,(byte)0x05,(byte)0x67,(byte)0xf7,(byte)0x00,(byte)0x00,(byte)0xa9,(byte)0x06,(byte)0xf3,(byte)0xf9,(byte)0x7c,(byte)0x3e,(byte)0x9f,(byte)0x03});
    	moforwardSM_Arg.setSm_RP_UI(sm_rp_ui);
    	
    	invokeParameter.setValue(moforwardSM_Arg);
    	this.mapComponent.selectInvoke(invoke);
    }

    public Array encode() throws Exception 
    {
    	/* BUG en encodage avec BN : Remplacement par Mobicents
        IEncoder<com.devoteam.srit.xmlloader.sigtran.ap.generated.tcap.DialoguePDU> encoderDialoguePDU = CoderFactory.getInstance().newEncoder("BER");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        encoderDialoguePDU.encode(this.dialoguePdu, outputStream);
        
        byte[] bytesPDU = outputStream.toByteArray();
        String strPDU = getHexString(bytesPDU);
        Array arrayDPDU = new DefaultArray(bytesPDU);
		SupArray sup = new SupArray();
		Array arraySep = new DefaultArray(new byte[]{(byte) 0x6b,(byte) 0x1e,(byte) 0x28,(byte) 0x1c,(byte) 0x06,(byte) 0x07,(byte) 0x00,(byte) 0x11,(byte) 0x86,(byte) 0x05,(byte) 0x01,(byte) 0x01,(byte) 0x01,(byte) 0xa0,(byte) 0x11});
		sup.addFirst(arraySep);
		sup.addLast(arrayDPDU);
		String str = getHexString(sup.getBytes());
    	*/
    	
    	// Library mobicent
        AsnOutputStream aos = new AsnOutputStream();
		this.dp.encode(aos);
		Array sup = new DefaultArray(aos.toByteArray());
		
        DialoguePortion dialogPortion = new DialoguePortion();
        dialogPortion.setValue(sup.getBytes());
    	//dp.setValue(new byte[]{(byte) 0x6b,(byte) 0x1e,(byte) 0x28,(byte) 0x1c,(byte) 0x06,(byte) 0x07,(byte) 0x00,(byte) 0x11,(byte) 0x86,(byte) 0x05,(byte) 0x01,(byte) 0x01,(byte) 0x01,(byte) 0xa0,(byte) 0x11,(byte) 0x60,(byte) 0x0f,(byte) 0x80, (byte) 0x02, (byte) 0x07, (byte) 0x80, (byte) 0xa1, (byte) 0x09, (byte) 0x06, (byte) 0x07, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x15, (byte) 0x02});
        this.tcMessage.getBegin().setDialoguePortion(dialogPortion);
    	
    	IEncoder<com.devoteam.srit.xmlloader.sigtran.ap.generated.map.Component> encoderMAP = CoderFactory.getInstance().newEncoder("BER");
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        encoderMAP.encode(this.mapComponent, outputStream);
        byte[] bytesMAP = outputStream.toByteArray();
        String strMAP = getHexString(bytesMAP);
        this.tcMessage.getBegin().getComponents().getValue().iterator().next().setValue(bytesMAP);
        // this.tcMessage.getBegin().getComponents().getValue().iterator().next().setValue(new byte[]{(byte) 0xa1,(byte) 0x30,(byte) 0x02,(byte) 0x01,(byte) 0x01,(byte) 0x02,(byte) 0x01,(byte) 0x2e,(byte) 0x30,(byte) 0x28, (byte) 0x84,(byte) 0x07,(byte) 0x91,(byte) 0x33,(byte) 0x66,(byte) 0x60,(byte) 0x05,(byte) 0x67,(byte) 0xf9,(byte) 0x82, (byte) 0x07,(byte) 0x91,(byte) 0x33,(byte) 0x66,(byte) 0x31,(byte) 0x70,(byte) 0x71,(byte) 0xf3,(byte) 0x04,(byte) 0x14,(byte) 0x11,(byte) 0x08,(byte) 0x0b,(byte) 0x91,(byte) 0x33,(byte) 0x66,(byte) 0x60,(byte) 0x05,(byte) 0x67,(byte) 0xf7,(byte) 0x00,(byte) 0x00,(byte) 0xa9,(byte) 0x06,(byte) 0xf3,(byte) 0xf9,(byte) 0x7c,(byte) 0x3e,(byte) 0x9f,(byte) 0x03});
    	
        //IDecoder decoder = CoderFactory.getInstance().newDecoder("BER");
        //InputStream inputStream = new ByteArrayInputStream(bytesMAP);
        //this.mapComponent = decoder.decode(inputStream, Component.class);
        
    	IEncoder<com.devoteam.srit.xmlloader.sigtran.ap.generated.tcap.TCMessage> encoderTCMessages = CoderFactory.getInstance().newEncoder("BER");
		outputStream = new ByteArrayOutputStream();
		encoderTCMessages.encode(this.tcMessage, outputStream);
		Array array =Array.fromHexString(getHexString(outputStream.toByteArray()));
		
		//this.decode(array);
        
		return array;
    } 

    public void decode(Array array) throws Exception
    {
    	IDecoder decoder = CoderFactory.getInstance().newDecoder("BER");
        InputStream inputStream = new ByteArrayInputStream(array.getBytes());
        this.tcMessage = decoder.decode(inputStream, TCMessage.class);
        byte[] otid = this.tcMessage.getBegin().getOtid().getValue();
        String strOtid = getHexString(otid);
        byte[] dialoguePortion = this.tcMessage.getBegin().getDialoguePortion().getValue();
        String strDP = getHexString(dialoguePortion);
        ComponentPortion componentPortion = this.tcMessage.getBegin().getComponents();
        // com.devoteam.srit.xmlloader.sigtran.ap.generated.tcap.Component comp = componentPortion.getValue().iterator().next();
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

    private static String getHexString(byte[] b) throws Exception 
    {
        String result = "";
        for (int i = 0; i < b.length; i++) 
        {
            result +=
                    Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }
    
}
