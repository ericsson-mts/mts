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

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.DialogueOC;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.DialogueServiceProvider;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.DialogueServiceUser;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.EmbeddedData;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.ObjectId;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.AssResult;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.AssSourceDiagnostic;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;

import org.bn.CoderFactory;
import org.bn.IDecoder;
import org.bn.coders.ASN1PreparedElementData;
import org.bn.metadata.ASN1ElementMetadata;
import org.bn.metadata.ASN1Metadata;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

/**
 * 
 * @author fhenry
 */
public class ASNToXMLConverter {

	static ASNToXMLConverter _instance;

	public static ASNToXMLConverter getInstance() {
		if (_instance != null) {
			return _instance;
		}
		return new ASNToXMLConverter();
	}

	public ASNToXMLConverter() {
	}

	public static Document getDocumentXML(final String xmlFileName) {
		Document document = null;
		SAXReader reader = new SAXReader();
		try {
			document = reader.read(xmlFileName);
		} catch (DocumentException ex) {
			GlobalLogger.instance().getApplicationLogger()
					.error(TextEvent.Topic.CORE, ex, "Wrong ASN1 file : ");
		}
		return document;
	}

	public String toXML(String name, Object objClass, ASN1ElementMetadata objElementInfo, int indent) 
	{
		String ret = "";
		try {
			if (objClass == null) {
				return ret;
			}

			// get the preparedData coming from annotations
			Field[] fields = objClass.getClass().getDeclaredFields();
			Field preparedDataField = null;
			try
			{
				preparedDataField = objClass.getClass().getDeclaredField("preparedData");
			}
			catch (Exception e)
			{
				// Nothing to do
			}
			ASN1PreparedElementData objPreparedEltData = null;
			if (preparedDataField != null)
			{
				preparedDataField.setAccessible(true);
				objPreparedEltData = (ASN1PreparedElementData) preparedDataField.get(objClass);
			}
			//ASN1ElementMetadata elementInfo = objPreparedEltData.getASN1ElementInfo();
			//ASN1Metadata objPreparedMetadata1 = objPreparedEltData.getTypeMetadata();

			int countFields = 0;
			boolean complex = true;

			if (name != null) 
			{
				if (!objClass.getClass().getCanonicalName()
						.startsWith("java.lang.")) 
				{
					ret += "\n" + indent(indent);
				}
				ret += "<" + returnClassName(objClass, name, objElementInfo) + ">";
			}
			

			String retObject = returnXMLObject(objClass, objElementInfo, indent);
			if (retObject != null) 
			{
				ret += retObject;
				complex = false;
			} 
			else 
			{
				// parsing object object fields
				for (int i = 0; i < fields.length; i++) 
				{
					Field f = fields[i];
					f.setAccessible(true);

					ASN1ElementMetadata subobjElementInfo = null;
					if (i < fields.length - 1)
					{
						ASN1PreparedElementData objPreparedEltData1 = null;
						if (objPreparedEltData != null)
						{
							objPreparedEltData1 = objPreparedEltData.getFieldMetadata(i);
						}
						if (objPreparedEltData1 != null)
						{
							subobjElementInfo = objPreparedEltData1.getASN1ElementInfo();
						}
					}
					Object subObject = f.get(objClass);

					String typeField = f.getType().getCanonicalName();
					if (subObject == null) 
					{
						// nothing to do
					} 
					else if (typeField != null && typeField.equals("org.bn.coders.IASN1PreparedElementData")) 
					{
						int j = 0;
						// nothing to do
					} 
					else if (typeField != null && typeField.equals("java.util.Collection")) 
					{
						Collection coll = (Collection) subObject;
						Iterator iter = coll.iterator();
						indent = indent + 2;
						ret += "\n" + indent(indent);
						ret += "<Collection>";
						while (iter.hasNext()) 
						{
							Object subObj = iter.next();
							ret += toXML(f.getName(), subObj, subobjElementInfo, indent + 2);
						}
						ret += "\n" + indent(indent);
						ret += "</Collection>";
					} 
					else 
					{
						ret += toXML(f.getName(), subObject, subobjElementInfo, indent + 2);
					}
					countFields++;
				}
			}

			if (name != null) {
				if (!objClass.getClass().getCanonicalName().startsWith("java.lang.") && fields.length > 2 && complex) 
				{
					ret += "\n" + indent(indent);
				}
				ret += "</" + returnClassName(objClass, name, objElementInfo) + ">";
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ret;
	}

	private String returnClassName(Object objClass, String name, ASN1ElementMetadata objElementInfo) throws Exception 
	{
		int tag = -1; 
		if (objElementInfo != null && objElementInfo.hasTag())
		{
			tag = objElementInfo.getTag();
		}

		if ("value".equals(name)) 
		{
			String ret = objClass.getClass().getSimpleName();
			ret = ret.replace("[]", "s");
			return ret;
		}
		else
		{
			return name;
		}
	}

	private String returnXMLObject(Object subObject, ASN1ElementMetadata objElementInfo, int indent)
			throws Exception {
		String ret = "";
		Class subClass = subObject.getClass();
		String type = subClass.getCanonicalName();
		if (type == null) {
			return null;
		} else if (type.endsWith(".DialogueOC")) {
			byte[] bytesEmbedded = ((DialogueOC) subObject).getValue();
			Array arraybytesEmbedded = new DefaultArray(bytesEmbedded);

			IDecoder decoder = CoderFactory.getInstance().newDecoder("BER");
			InputStream inputStream = new ByteArrayInputStream(bytesEmbedded);
			Class cl = Class
					.forName("com.devoteam.srit.xmlloader.sigtran.ap.tcap.ExternalPDU");
			Object obj = cl.newInstance();
			obj = decoder.decode(inputStream, cl);
			ret += toXML("ExternalPDU", obj, objElementInfo, indent + 2);
			return ret;
		} else if (type.endsWith(".EmbeddedData")) {
			byte[] bytesEmbedded = ((EmbeddedData) subObject).getValue();
			Array arraybytesEmbedded = new DefaultArray(bytesEmbedded);

			IDecoder decoder = CoderFactory.getInstance().newDecoder("BER");
			InputStream inputStream = new ByteArrayInputStream(bytesEmbedded);
			Class cl = Class
					.forName("com.devoteam.srit.xmlloader.sigtran.ap.tcap.DialoguePDU");
			Object obj = cl.newInstance();
			obj = decoder.decode(inputStream, cl);
			ret += toXML("DialoguePDU", obj, objElementInfo, indent + 2);
			return ret;
		} else if (type.endsWith(".ObjectId")) {
			byte[] bytesEmbedded = ((ObjectId) subObject).getValue();
			Array arraybytesEmbedded = new DefaultArray(bytesEmbedded);

			IDecoder decoder = CoderFactory.getInstance().newDecoder("BER");
			InputStream inputStream = new ByteArrayInputStream(bytesEmbedded);
			Class cl = Class.forName("org.bn.types.ObjectIdentifier");
			Object obj = cl.newInstance();
			obj = decoder.decode(inputStream, cl);

			ret += toXML("ObjectIdentifier", obj, objElementInfo, indent + 2);
			return ret;
		} else if (type.endsWith(".AssResult")) {
			byte[] bytesEmbedded = ((AssResult) subObject).getValue();
			Array arraybytesEmbedded = new DefaultArray(bytesEmbedded);

			IDecoder decoder = CoderFactory.getInstance().newDecoder("BER");
			InputStream inputStream = new ByteArrayInputStream(bytesEmbedded);
			Class cl = Class
					.forName("com.devoteam.srit.xmlloader.sigtran.ap.tcap.Associate_result");
			Object obj = cl.newInstance();
			obj = decoder.decode(inputStream, cl);

			ret += toXML("Associate_result", obj, objElementInfo, indent + 2);
			return ret;
		} else if (type.endsWith(".AssSourceDiagnostic")) {
			byte[] bytesEmbedded = ((AssSourceDiagnostic) subObject).getValue();
			Array arraybytesEmbedded = new DefaultArray(bytesEmbedded);

			IDecoder decoder = CoderFactory.getInstance().newDecoder("BER");
			InputStream inputStream = new ByteArrayInputStream(bytesEmbedded);
			Class cl = Class
					.forName("com.devoteam.srit.xmlloader.sigtran.ap.tcap.Associate_source_diagnostic");
			Object obj = cl.newInstance();
			obj = decoder.decode(inputStream, cl);

			ret += toXML("Associate_source_diagnostic", obj, objElementInfo, indent + 2);
			return ret;
		} else if (type.endsWith(".DialogueServiceUser")) {
			byte[] bytesEmbedded = ((DialogueServiceUser) subObject).getValue();
			Array arraybytesEmbedded = new DefaultArray(bytesEmbedded);

			IDecoder decoder = CoderFactory.getInstance().newDecoder("BER");
			InputStream inputStream = new ByteArrayInputStream(bytesEmbedded);
			Class cl = Class
					.forName("com.devoteam.srit.xmlloader.sigtran.ap.tcap.Dialogue_service_user");
			Object obj = cl.newInstance();
			obj = decoder.decode(inputStream, cl);

			ret += toXML("DialogueServiceUser", obj, objElementInfo, indent + 2);
			return ret;
		} 
		else if (type.endsWith(".DialogueServiceProvider")) {
			byte[] bytesEmbedded = ((DialogueServiceProvider) subObject)
					.getValue();
			Array arraybytesEmbedded = new DefaultArray(bytesEmbedded);

			IDecoder decoder = CoderFactory.getInstance().newDecoder("BER");
			InputStream inputStream = new ByteArrayInputStream(bytesEmbedded);
			Class cl = Class
					.forName("com.devoteam.srit.xmlloader.sigtran.ap.tcap.Dialogue_service_provider");
			Object obj = cl.newInstance();
			obj = decoder.decode(inputStream, cl);

			ret += toXML("DialogueServiceProvider", obj, objElementInfo, indent + 2);
			return ret;
		} 
		else if (type.equals("byte[]")) 
		{
			byte[] bytes = (byte[]) subObject;
			ret += Utils.toHexaString(bytes, "");
			return ret;

		} else if (type.equals("java.lang.Boolean") || type.equals("boolean")) {
			ret += subObject.toString();
			return ret;

		} else if (type.equals("java.lang.Long") || type.equals("long")) {
			ret += subObject.toString();
			return ret;
		} else if (type.equals("java.lang.Integer") || type.equals("int")) {
			ret += subObject.toString();
			return ret;
		} else if (type.equals("java.lang.String")) {
			ret += subObject.toString();
			return ret;
		} else if (type.endsWith(".EnumType")) {
			ret += subObject.toString();
			return ret;
		}
		return null;
	}

	/**
	 * generates a string of nb*"    " (four spaces nb times), used for
	 * intentation in printAvp
	 */
	private static String indent(int nb) {
		String str = "";
		for (int i = 0; i < nb; i++) {
			str += " ";
		}
		return str;
	}

}
