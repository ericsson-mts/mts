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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dom4j.Element;
import org.mobicents.protocols.asn.AsnInputStream;
import org.mobicents.protocols.asn.AsnOutputStream;
import org.mobicents.protocols.ss7.tcap.asn.ApplicationContextName;
import org.mobicents.protocols.ss7.tcap.asn.ApplicationContextNameImpl;
import org.mobicents.protocols.ss7.tcap.asn.DialogPortion;
import org.mobicents.protocols.ss7.tcap.asn.DialogRequestAPDU;
import org.mobicents.protocols.ss7.tcap.asn.OperationCodeImpl;
import org.mobicents.protocols.ss7.tcap.asn.TCBeginMessageImpl;
import org.mobicents.protocols.ss7.tcap.asn.TcapFactory;
import org.mobicents.protocols.ss7.tcap.asn.comp.Component;
import org.mobicents.protocols.ss7.tcap.asn.comp.Invoke;
import org.mobicents.protocols.ss7.tcap.asn.comp.OperationCode;
import org.mobicents.protocols.ss7.tcap.asn.comp.Parameter;
import org.mobicents.protocols.ss7.tcap.asn.comp.TCBeginMessage;

import com.devoteam.srit.xmlloader.asn1.ASNMessage;
import com.devoteam.srit.xmlloader.asn1.ASNToXMLConverter;

/**
 *
 * @author fhenry
 */
public class MobicentTCAPMessage extends ASNMessage 
{

	// TCAP mobicent object
	TCBeginMessage tcbm;

    public MobicentTCAPMessage() 
    {
    	this.className =".tcap.";
    	// Mobicent TCAP message
    	this.tcbm = (TCBeginMessageImpl) TcapFactory.createTCBeginMessage();

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
        this.tcbm.setDialogPortion(dp);

        /*
            if (this.provider.getStack().getStatisticsEnabled()) {
                String acn = ((ApplicationContextNameImpl) event.getApplicationContextName()).getStringValue();
                this.provider.getStack().getCounterProviderImpl().updateOutgoingDialogsPerApplicatioContextName(acn);
            }
        } else {
            if (this.provider.getStack().getStatisticsEnabled()) {
                this.provider.getStack().getCounterProviderImpl().updateOutgoingDialogsPerApplicatioContextName("");
            }
        }
		*/
        
        // now comps
    	// transaction ID= 00000001	
    	byte[] transID = new byte[]{0,0,0,1};
        tcbm.setOriginatingTransactionId(transID);
        // if (this.scheduledComponentList.size() > 0) {
        org.mobicents.protocols.ss7.tcap.asn.comp.Component[] componentsToSend = new org.mobicents.protocols.ss7.tcap.asn.comp.Component[1];
        componentsToSend[0] = TcapFactory.createComponentInvoke();
        Invoke invoke = (Invoke) componentsToSend[0];
        componentsToSend[0].setInvokeId(1L);
        OperationCode opCode = new OperationCodeImpl();
        opCode.setLocalOperationCode(46L);
        invoke.setOperationCode(opCode);
        Parameter param = new org.mobicents.protocols.ss7.tcap.asn.ParameterImpl();
		param.setData(new byte[]{});
		invoke.setParameter(param);
        tcbm.setComponent(componentsToSend);
        //}
    }

    public Array encode() throws Exception 
    {
		AsnOutputStream aosTCAP = new AsnOutputStream();
		tcbm.encode(aosTCAP);
		Array arrayTCAP = new DefaultArray(aosTCAP.toByteArray());
		
		// Array supArray = arrayTCAP.subArray(1);
		// AsnInputStream aos = new AsnInputStream(supArray.getBytes());
    	// tcbm.decode(aos);
		
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
    	this.className =".tcap.";
    	/*
        if (root.element("ASN1") != null) 
        {
            List<Element> children = root.element("ASN1").elements();
            for (Element element : children) 
            {
            	FH ne compile pas
            	XmlToAsn1 xml_asn1 = new XmlToAsn1();
                String PackageName = "com.devoteam.srit.xmlloader.h323.h225v7.";
                asn1 = xml_asn1.instanceClass(element.getName(), PackageName);
                xml_asn1.initObject(asn1, element, PackageName);
               
            }
        }
        */
    }

    public String toXML()
    {
        String ret = "";
        ret += "<TCAP>";	
        ret += ASNToXMLConverter.getInstance().toXML("", this, null, this.tcbm, null, 0);
        ret += "\n";
        ret += "</TCAP>";
    	return ret;
    }

    public Component[] getTCAPComponents()
    {
    	Component[] comps = tcbm.getComponent();
    	/*
    	ArrayList<Component> compsList = new ArrayList<Component>();
    	for (int i = 0; i < comps.length; i++)
    	{
    		compsList.add(comps[i]);
    	}
    	*/
    	return comps;
    }
    
    public boolean isRequest()
    {
    	// TODO
    	return false;
    }
    
    public String getType()
    {
    	// TODO
    	return null;
    }

    
    public String getResult()
    {
    	// TODO
    	return null;
    }
    
    public String getTransactionId()
    {
    	// TODO
    	return null;
    }

}
