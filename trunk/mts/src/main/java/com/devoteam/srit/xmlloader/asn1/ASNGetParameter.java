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

import com.devoteam.srit.xmlloader.asn1.data.ElementValue;
import com.devoteam.srit.xmlloader.asn1.dictionary.ASNDictionary;
import com.devoteam.srit.xmlloader.asn1.dictionary.Embedded;
import com.devoteam.srit.xmlloader.core.coding.binary.ElementAbstract;
import com.devoteam.srit.xmlloader.core.coding.binary.EnumLongField;
import com.devoteam.srit.xmlloader.core.coding.binary.EnumStringField;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.bn.CoderFactory;
import org.bn.IDecoder;
import org.bn.annotations.ASN1EnumItem;
import org.bn.coders.ASN1PreparedElementData;
import org.bn.coders.TagClass;
import org.bn.metadata.ASN1ElementMetadata;
import org.bn.metadata.ASN1Metadata;
import org.bn.types.BitString;
import org.bn.types.ObjectIdentifier;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

/**
 * 
 * @author fhenry
 */
public class ASNGetParameter 
{
	// separator for the XML tag between object attribute or type and the ASN1 tag and ASN1 object type
	public static char TAG_SEPARATOR = '.';
	
	// keyword to convert into XML the byte[] object
	//public static String LABEL_TABLE_BYTE = "Bytes";
	
	// number of space used to make the tabulation for XML readable presentation
	public static int NUMBER_SPACE_TABULATION = 3;
	
	private static ASNGetParameter _instance;

	public static ASNGetParameter getInstance() 
	{
		if (_instance == null) 
		{
			_instance = new ASNGetParameter();
		}
		return _instance;
	}

	public ASNGetParameter() 
	{
	}

	public static Document getDocumentXML(final String xmlFileName) 
	{
		Document document = null;
		SAXReader reader = new SAXReader();
		try 
		{
			document = reader.read(xmlFileName);
		} 
		catch (DocumentException ex) 
		{
			GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, ex, "Wrong ASN1 file : ");
		}
		return document;
	}

	public String getParameter(String resultPath, ASNMessage message, Object parentObj, String name, Object objClass, ASN1ElementMetadata objElementInfo, int indent) 
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

			// get the XML tag
	        String XMLTag = getSignificantXMLTag(objClass, name);
	        
        	// we add a embedded record in the list		
	        if (message !=null)
	        {
				// get the condition for embedded objects
		        String elementName = resultPath;
		        int iPos = resultPath.lastIndexOf(".");
		        if (iPos > 0)
		        {
		        	elementName = resultPath.substring(iPos + 1);
		        }
	        	String condition = elementName + "=" + objClass;
	        	List<Embedded> embeddedList = message.getEmbeddedByCondition(condition);
	        	if (embeddedList != null)
	        	{
	        		message.addConditionalEmbedded(embeddedList);
	        	}
	        }

	        // return the object as XML
			String retObject = processSimpleObject(resultPath, message, parentObj, objClass, name, objElementInfo, indent);			
			boolean complexObject = true;
			if (retObject != null || fieldsSize == 1)
			{
				complexObject = false;
			}
	        
			// calculate resultPath
	        resultPath = resultPath + "." + XMLTag; 
									
			if (retObject != null) 
			{
				return retObject;
			} 
			else 
			{
				// get whether the field is the first one which is not null
				boolean first = true;
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
						indent = indent + NUMBER_SPACE_TABULATION;
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
							k = k + 1;
							return getParameter(resultPath, message, objClass, f.getName(), subObj, subobjElementInfo, indent + NUMBER_SPACE_TABULATION);
						}
						indent = indent - NUMBER_SPACE_TABULATION;
						ret += "\n" + indent(indent);
						ret += "</Collection>";
						ret += "\n" + indent(indent - NUMBER_SPACE_TABULATION);
					} 
					else 
					{
						if (!first)
						{
							ret += "\n" + indent(indent);
						}

						return getParameter(resultPath, message, objClass, f.getName(), subObject, subobjElementInfo, indent + NUMBER_SPACE_TABULATION);
					}
					if (subObject != null)
					{
						first = false;
					}
				}
			}
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, e, "Exception in ASNToXMLConverter : ");
		}

		return ret;
	}
	
	private String getSignificantXMLTag(Object objClass, String name) throws Exception 
	{
		if (!"value".equals(name)) 
		{
			return name;
		}
		String XMLTag = objClass.getClass().getSimpleName();
		XMLTag = XMLTag.replace("byte[]", "Bytes");
		return XMLTag;
	}
		
	// get the XML data for simple object : null means a complex object
	private String processSimpleObject(String resultPath, ASNMessage message, Object parentObj, Object object, String name, ASN1ElementMetadata objElementInfo, int indent)
			throws Exception 
	{
		String type = object.getClass().getCanonicalName();
		
    	// get the embedded definition form the message (for conditional) and the dictionary
    	Embedded embedded =  null; 
    	if (message != null)
    	{
    		embedded =  message.getEmbeddedFromDico(name, type);
    	}
    	// process embedded object
		if (embedded != null) 
		{
			// calculate resultPath
		    String XMLTag = getSignificantXMLTag(object, name);
		    resultPath = resultPath + "." + XMLTag;
		    
		    Object obj = processEmbeddedObject(embedded, object);
		    
			String ret = "\n" + indent(indent);
			ret += getParameter(resultPath, message, object, "value", obj, objElementInfo, indent + NUMBER_SPACE_TABULATION);
			ret += "\n" + indent(indent - NUMBER_SPACE_TABULATION);
			return ret;
		}
		
		if (type.equals("java.lang.Boolean") || type.equals("boolean")) 
		{
			
			return object.toString();

		}  
		else if (type.equals("java.lang.Integer") || type.equals("int") || 
				 type.equals("java.lang.Long") || type.equals("long") ||
				 type.equals("java.lang.Short") || type.equals("short") || 
				 type.equals("java.lang.Byte") || type.equals("byte")) 
		{
			// get the element definition (enumeration binary data) from the dictionary
	    	ElementAbstract elementDico = null;
	    	if (message != null)
	    	{
		    	elementDico = message.getElementFromDico(parentObj, resultPath);
	    	}

        	if (elementDico != null)
        	{
	        	EnumLongField fld = (EnumLongField) elementDico.getField(0);
	        	return fld.getEnumValue((Long) object);
        	}
			return object.toString();
		}
        else if (type.equals("org.bn.types.BitString")) 
        {
        	// calculate resultPath
            resultPath = resultPath + "." + name;
            
        	BitString bitStr = (BitString) object;
            Array array = new DefaultArray(bitStr.getValue());
        	String strVal = Array.toHexString(array);

			// get the element definition (enumeration binary data) from the dictionary
	    	ElementAbstract elementDico = null;
	    	if (message != null)
	    	{
		    	elementDico = message.getElementFromDico(parentObj, resultPath);
	    	}

        	if (elementDico != null)
        	{
	        	EnumStringField fld = (EnumStringField) elementDico.getField(0);
	        	strVal = fld.getEnumValue(strVal);
        	}
        	return strVal; 
        }
		else if (type.equals("java.lang.String")) 
		{
			// get the element definition (enumeration binary data) from the dictionary
	    	ElementAbstract elementDico = null;
	    	if (message != null)
	    	{
		    	elementDico = message.getElementFromDico(parentObj, resultPath);
	    	}

        	if (elementDico != null)
        	{
	        	EnumStringField fld = (EnumStringField) elementDico.getField(0);
	        	return fld.getEnumValue((String) object);
        	}

			return object.toString();
		}
		else if (type.equals("org.bn.types.ObjectIdentifier")) 
		{
		    // calculate resultPath
            resultPath = resultPath + "." + name;

			// get the element definition (enumeration binary data) from the dictionary
	    	ElementAbstract elementDico = null;
	    	if (message != null)
	    	{
		    	elementDico = message.getElementFromDico(parentObj, resultPath);
	    	}
	    	
	    	String value = ((ObjectIdentifier) object).getValue();
        	if (elementDico != null)
        	{
	        	EnumStringField fld = (EnumStringField) elementDico.getField(0);
	        	value = fld.getEnumValue(value);
        	}
        	return value;
		}
		else if (type.equals("byte[]")) 
		{
			byte[] bytes = (byte[]) object;
			
			String ret = Utils.toHexaString(bytes, "");
			
			// get the element definition (enumeration binary data) from the dictionary
	    	ElementAbstract elementDico = null;
	    	if (message != null)
	    	{
		    	elementDico = message.getElementFromDico(parentObj, resultPath, bytes);
	    	}
        	if (elementDico != null)
        	{
        		// TODO bug dans la fonction copyToClone() : retourne toujours un IntegerField
        		// Est ce que c'est réellement un pb ? a voir à l'usage
        		//ElementSimple binary = new ElementSimple(); 
        		//binary.copyToClone(binaryDico);
        		// ElementV binary = (ElementV) elementDico;
        		Array array = new DefaultArray(bytes);
        		try
        		{
        			elementDico.decodeFromArray(array, message.dictionary);
            		ret += elementDico.fieldsElementsToXml(indent - NUMBER_SPACE_TABULATION);
            		ret += indent(indent - 2 * NUMBER_SPACE_TABULATION);
        		}
        		catch (Exception e)
        		{
        			// nothing to do
        		}
        	}
        	return ret;
		} 
		else if (type.endsWith(".EnumType")) 
		{
			ASN1EnumItem enumObj = null;
	        Class enumClass = object.getClass();
            for(Field enumItem: enumClass.getDeclaredFields()) 
            {
                if(enumItem.isAnnotationPresent(ASN1EnumItem.class)) 
                {
                    if(enumItem.getName().equals(object.toString())) 
                    {
                    	enumObj = enumItem.getAnnotation(ASN1EnumItem.class);
                        break;
                    }
                }
            }
			return object.toString() + TAG_SEPARATOR + enumObj.tag();
		}
		else if (type.equals("org.bn.types.NullObject")) 
		{
			return "";
		}
		else if (type.endsWith(".PcsExtensions")) 
		{
			return "";
		}
		return null;
	}

	/**
	 * generates a string of nb*"    " (four spaces nb times), used for indentation in printAvp
	 */
	public static String indent(int nb) {
		String str = "";
		for (int i = 0; i < nb; i++) {
			str += " ";
		}
		return str;
	}

public static Object processEmbeddedObject(Embedded embedded, Object object) throws Exception
	{
		String type = object.getClass().getCanonicalName();
		
		String replace = embedded.getReplace();
				
		byte[] bytesEmbedded = null;
	    if (!type.equals("byte[]"))
	    {
			Field[] fields = object.getClass().getDeclaredFields();
			fields[0].setAccessible(true);
			bytesEmbedded = (byte[]) fields[0].get(object);
			// Array arraybytesEmbedded = new DefaultArray(bytesEmbedded);
	    }
	    else
	    {
	    	bytesEmbedded = (byte[]) object;
	    }
	    
		IDecoder decoder = CoderFactory.getInstance().newDecoder("BER");
		InputStream inputStream = new ByteArrayInputStream(bytesEmbedded);
		Class<?> cl = Class.forName(replace);
		Object obj = cl.newInstance();
		try
		{
			obj = decoder.decode(inputStream, cl);
		}
		catch (Exception e)
		{
			return bytesEmbedded;
		}
		return obj;
	}
}
