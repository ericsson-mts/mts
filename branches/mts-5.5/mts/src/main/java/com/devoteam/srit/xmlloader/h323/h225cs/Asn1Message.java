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

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.h323.h225cs.XmlToAsn1;
import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import org.bn.CoderFactory;
import org.bn.coders.per.*;
import org.bn.IEncoder;
import org.bn.IDecoder;
import org.dom4j.Element;

/**
 *
 * @author gansquer
 */
public class Asn1Message {

    Object asn1;

    public Asn1Message() {
    }

    public Array encode() throws Exception {

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
        return null;
    }

    public void parseArray(Array array) throws Exception {
    }

    public void parseElement(Element root) throws Exception {
        if (root.element("asn1") != null) {
            List<Element> children = root.element("asn1").elements();
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

    public static String getHexString(byte[] b) throws Exception {
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result +=
                    Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }
}
