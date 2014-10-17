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

package com.devoteam.srit.xmlloader.asn1;

import com.devoteam.srit.xmlloader.asn1.dictionary.ASNDictionary;
import com.devoteam.srit.xmlloader.asn1.dictionary.Embedded;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;

import org.bn.CoderFactory;
import org.bn.IDecoder;
import org.bn.annotations.ASN1EnumItem;
import org.bn.coders.ASN1PreparedElementData;
import org.bn.coders.TagClass;
import org.bn.metadata.ASN1ElementMetadata;
import org.bn.metadata.ASN1Metadata;
import org.bn.types.ObjectIdentifier;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

/**
 * 
 * @author fhenry
 */
public class ASNToXMLConverter {

	public static char TAG_SEPARATOR = '.';
	
	public static String LABEL_TABLE_BYTE = "Bytes";
	
	private static ASNToXMLConverter _instance;

	public static ASNToXMLConverter getInstance() {
		if (_instance == null) {
			_instance = new ASNToXMLConverter();
		}
		return _instance;
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

	public String toXML(ASNMessage message, String name, Object objClass, ASN1ElementMetadata objElementInfo, int indent) 
	{
		String ret = "";
		try 
		{
			if (objClass == null) 
			{
				return ret;
			}

			Field[] fields = objClass.getClass().getDeclaredFields();
			int fieldsSize = fields.length;
			/*
			int j = 0;
			while (j < fields.length)
			{
				fields[j].setAccessible(true);
				Object subObject = fields[j].get(objClass);
				String typeField = fields[j].getType().getCanonicalName();
				j = j + 1;
				if (subObject == null) 
				{
					continue;
					// nothing to do
				} 
				else if (typeField != null && typeField.equals("org.bn.coders.IASN1PreparedElementData")) 
				{
					continue;
					// nothing to do
				} 
				else if (typeField != null && typeField.equals("java.util.Collection")) 
				{
					complexObject = true;
				} 
				String retObject = returnXMLObject(message, subObject, objElementInfo, indent);
				if (retObject == null)
				{
					complexObject = true;
					break;
				}
			}
			*/			
			//boolean complexObject = (retObject == null || fieldsSize == 1);
			// get the preparedData coming from annotations
			Field preparedDataField = null;
			try
			{
				preparedDataField = objClass.getClass().getDeclaredField("preparedData");
				fieldsSize = fieldsSize - 1; 
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
			ASN1Metadata objPreparedMetadata = null;
			if (objPreparedEltData != null)
			{
				objPreparedMetadata = objPreparedEltData.getTypeMetadata();
			}
			
			String retObject = returnXMLObject(message, objClass, objElementInfo, indent);
			
			boolean complexObject = true;
			if (retObject != null || fieldsSize == 1)
			{
				complexObject = false;
			}

			String fieldName = returnClassName(objClass, name, objElementInfo, objPreparedMetadata);			
			if (name != null) 
			{
				ret += "<" + fieldName + ">";
				if (complexObject) 
				{
					ret += "\n" + indent(indent);
				}
				else
				{
					int l = 0;
				}
			}
			
			if (retObject != null) 
			{
	        	// we add a embedded record in the list 
	        	String condition = fieldName + "=" + retObject;
	        	Embedded embedded = ASNDictionary.getInstance().getEmbeddedByCondition(condition);
	        	if (embedded != null)
	        	{
	        		message.addConditionalEmbedded(embedded);
	        	}

				ret += retObject;
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
						int k = 0;
						// nothing to do
					} 
					else if (typeField != null && typeField.equals("java.util.Collection")) 
					{
						Collection<?> coll = (Collection<?>) subObject;
						Iterator<?> iter = coll.iterator();
						ret += "\n" + indent(indent);
						indent = indent + 2;
						ret += "<Collection>";
						ret += "\n" + indent(indent);
						int k = 0;
						while (iter.hasNext()) 
						{
							Object subObj = iter.next();
							if (k > 0)
							{
								ret += "\n" + indent(indent);
							}
							ret += toXML(message, f.getName(), subObj, subobjElementInfo, indent + 2);
							k = k + 1;
						}
						indent = indent - 2;
						ret += "\n" + indent(indent);
						ret += "</Collection>";
						ret += "\n" + indent(indent - 2);
					} 
					else 
					{
						if (i > 0)
						{
							ret += "\n" + indent(indent);
						}

						ret += toXML(message, f.getName(), subObject, subobjElementInfo, indent + 2);
					}
				}
			}

			if (name != null) 
			{
				if (complexObject)
				{
					ret += "\n" + indent(indent - 2);
				}
				ret += "</" + fieldName + ">";
			}

		} catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ret;
	}

	private String returnClassName(Object objClass, String name, ASN1ElementMetadata objElementInfo, ASN1Metadata objPreparedMetadata1) throws Exception 
	{
		String ret;
		if ("value".equals(name)) 
		{
			ret = objClass.getClass().getSimpleName();
			ret = ret.replace("byte[]", "Bytes");		
		}
		else
		{
			ret = name;
		}
		
		// Add the tag information of the ASN1 object coming from the annotation (class tag imp^licit default)
		String tagAnnotation = null;
		int tagClass = -1;
		if (objElementInfo != null && objElementInfo.hasTag())
		{
			tagClass = objElementInfo.getTagClass();
		}
		String strTagClass = null;
		if (tagClass == TagClass.Application)
		{
			strTagClass = "A";
		}
		else if (tagClass == TagClass.ContextSpecific)
		{
			strTagClass = "C";
		}
		else if (tagClass == TagClass.Private)
		{
			strTagClass = "P";
		}
		else if (tagClass == TagClass.Universal)
		{
			strTagClass = "U";
		}
		if (strTagClass != null)
		{
			tagAnnotation = strTagClass;
		}
		
		int tag = -1; 
		if (objElementInfo != null && objElementInfo.hasTag())
		{
			tag = objElementInfo.getTag();
		}
		if (tag != -1)
		{
			tagAnnotation += tag;
		}
		
		boolean implicitTag = true;
		if (objElementInfo != null && objElementInfo.hasTag())
		{
			implicitTag = objElementInfo.isImplicitTag();
		}
		if (!implicitTag)
		{
			tagAnnotation += "e";
		}

		boolean defaultValue = false;
		if (objElementInfo != null && objElementInfo.hasDefaultValue())
		{
			defaultValue = true;
		}
		if (defaultValue)
		{
			tagAnnotation += "d";
		}
		
		if (tagAnnotation!= null)
		{
			ret += TAG_SEPARATOR;
			ret += tagAnnotation;
		}
		
		// Add the type of ASN1 object coming from the annotation (choice sequence boxedType ...)
		if (objPreparedMetadata1 != null)
		{
			String strDataType = objPreparedMetadata1.getClass().getSimpleName();
			if (strDataType != null)
			{
				if (strDataType.startsWith("ASN1"))
				{
					strDataType = strDataType.substring(4);
				}
				if (strDataType.endsWith("Metadata"))
				{
					strDataType = strDataType.substring(0, strDataType.length() - 8);
				}
				ret += TAG_SEPARATOR;
				ret += strDataType;
			}
			
		}
		return ret;
	}

	private String returnXMLObject(ASNMessage message, Object subObject, ASN1ElementMetadata objElementInfo, int indent)
			throws Exception {
		String ret = "";
		Class subClass = subObject.getClass();
		String type = subClass.getCanonicalName();
		if (type == null) 
		{
			return null;
		}
		// manage the embedded objects
		Embedded embedded = message.getEmbeddedByInitial(type);
		if (embedded != null) 
		{
			Field[] fields = subObject.getClass().getDeclaredFields();
			fields[0].setAccessible(true);
			byte[] bytesEmbedded = (byte[]) fields[0].get(subObject);
			// Array arraybytesEmbedded = new DefaultArray(bytesEmbedded);

			IDecoder decoder = CoderFactory.getInstance().newDecoder("BER");
			InputStream inputStream = new ByteArrayInputStream(bytesEmbedded);
			String replace = embedded.getReplace();
			Class<?> cl = Class.forName(replace);
			Object obj = cl.newInstance();
			obj = decoder.decode(inputStream, cl);
			ret += "\n" + indent(indent);
			ret += toXML(message, "value", obj, objElementInfo, indent + 2);
			ret += "\n" + indent(indent);
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
		}
		else if (type.endsWith(".NullObject")) {
			ret += "";
			return ret;
		}
		else if (type.endsWith(".ObjectIdentifier")) {
			ret += "<ObjectIdentifier>" + ((ObjectIdentifier) subObject).getValue() + "</ObjectIdentifier>";
			return ret;
		}
		else if (type.endsWith(".EnumType")) {
			ASN1EnumItem enumObj = null;
	        Class enumClass = subObject.getClass();
            for(Field enumItem: enumClass.getDeclaredFields()) {
                if(enumItem.isAnnotationPresent(ASN1EnumItem.class)) {
                    if(enumItem.getName().equals(subObject.toString())) {
                    	enumObj = enumItem.getAnnotation(ASN1EnumItem.class);
                        break;
                    }
                }
            }
			ret += subObject.toString() + TAG_SEPARATOR + enumObj.tag();
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
