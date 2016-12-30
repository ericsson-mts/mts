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
import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.coding.binary.Dictionary;
import com.devoteam.srit.xmlloader.core.coding.binary.ElementAbstract;
import com.devoteam.srit.xmlloader.core.coding.binary.EnumLongField;
import com.devoteam.srit.xmlloader.core.coding.binary.EnumStringField;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.ObjectId;

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

	public void getParameter(Parameter parameter, String path, String resultPath, ASNMessage message, Object parentObj, String name, Object objClass) 
	{
		try 
		{
			if (objClass == null) 
			{
				return;
			}

			Field[] fields = objClass.getClass().getDeclaredFields();

			// get the XML tag
	        String XMLTag = getSignificantXMLTag(objClass, name);	     
	        
        	// we add a embedded record in the list		
	        if (message !=null)
	        {
				// get the condition for embedded objects	        	
	        	List<Embedded> embeddedList = message.getEmbeddedListWithCondition(resultPath, parentObj, name, objClass);
	        	if (embeddedList != null)
	        	{
	        		message.addConditionalEmbedded(embeddedList);
	        	}
	        }

	        // return the object as XML
			Object retObject = processSimpleObject(parameter, path, resultPath, message, parentObj, objClass, name);			
	        									
			if (retObject != null)
			{
				// remove the layer of the path (at the beginning)
				int pos = path.indexOf(TAG_SEPARATOR);
				String pathWithoutLayer = path; 
				if (pos >= 0)
				{
					pathWithoutLayer = path.substring(pos);
				}
				// remove the end of the path (after the ".field.")				
				int posField = pathWithoutLayer.indexOf(".field.");
				String pathWithoutLayerField = pathWithoutLayer;
				if (posField >= 0)
				{
					pathWithoutLayerField = pathWithoutLayer.substring(0, posField);
				}
				if (resultPath.endsWith(pathWithoutLayerField)) 
				{
					if (retObject != null)
					{
						if (retObject instanceof byte[])
						{
							byte[] bytes = (byte[]) retObject;
							getParameterElement(parameter, path, message, parentObj, name, bytes);
							return;
						}
						else
						{
							parameter.add(retObject);
						}
						return;
					}
				}
				pos = resultPath.lastIndexOf('.');
				String tempResultPath = resultPath; 
				if (pos >= 0)
				{
					tempResultPath = resultPath.substring(0, pos + 1);
					tempResultPath += name;
				}				
				if (tempResultPath.endsWith(pathWithoutLayerField)) 
				{
					if (retObject != null)
					{
						if (retObject != null)
						{
							if (retObject instanceof byte[])
							{
								byte[] bytes = (byte[]) retObject;
								getParameterElement(parameter, path, message, parentObj, name, bytes);
								return;
							}
							else
							{
								parameter.add(retObject);
							}
						}
						return;
					}
				}
				//return;
			} 
			//if (retObject == null)
			else
			{
				// calculate resultPath
		        resultPath = resultPath + "." + XMLTag; 

				// parsing object object fields
				for (int i = 0; i < fields.length; i++) 
				{
					Field f = fields[i];
					f.setAccessible(true);

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
						int k = 0;
						while (iter.hasNext()) 
						{
							Object subObj = iter.next();
							k = k + 1;
							getParameter(parameter, path, resultPath, message, objClass, f.getName(), subObj);
						}
					} 
					else 
					{
						getParameter(parameter, path, resultPath, message, objClass, f.getName(), subObject);
					}
				}
			}
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, e, "Exception in ASNToXMLConverter : ");
		}

		return;
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
	private Object processSimpleObject(Parameter parameter, String path, String resultPath, ASNMessage message, Object parentObj, Object object, String name)
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
			getParameter(parameter, path, resultPath, message, object, "value", obj);
			return null;
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
		    	elementDico = message.getElementFromDico(name, parentObj, resultPath);
	    	}

        	if (elementDico != null)
        	{
	        	EnumLongField fld = (EnumLongField) elementDico.getField(0);
	        	long longValue = ((Number) object).longValue();
	        	return fld.getEnumValue(longValue);
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
		    	elementDico = message.getElementFromDico(name, parentObj, resultPath);
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
		    	elementDico = message.getElementFromDico(name, parentObj, resultPath);
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
		    	elementDico = message.getElementFromDico(name, parentObj, resultPath);
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
			return object;
			
			/*
			String ret = Utils.toHexaString(bytes, "");
			
			// get the element definition (enumeration binary data) from the dictionary
	    	ElementAbstract elementDico = null;
	    	if (message != null)
	    	{
		    	elementDico = message.getElementFromDico(name, parentObj, resultPath, bytes);
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
            		ret += elementDico.fieldsElementsToXml(0);
        		}
        		catch (Exception e)
        		{
        			// nothing to do
        		}        
        	}
        	return ret;
        	*/
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

	
	public void getParameterElement(Parameter parameter, String path, ASNMessage message, Object parentObj, String name, byte[] bytes)
	{
		// get the element definition (enumeration binary data) from the dictionary
		ElementAbstract elementDico = null;
		if (message != null)
		{
	    	elementDico = message.getElementFromDico(name, parentObj, path, bytes);
		}
		if (elementDico != null)
		{
			Array array = new DefaultArray(bytes);
			try
			{
				elementDico.decodeFromArray(array, message.dictionary);
				String[] params = Utils.splitPath(path);
				if (path.contains(".field."))
				{
					elementDico.getParameter(parameter, params, path, params.length - 4, message.dictionary);
				}
				else
				{
					elementDico.getParameter(parameter, params, path, params.length - 2, message.dictionary);
				}
			}
			catch (Exception e)
			{
				// nothing to do
			}        
		}
		else
		{
			if (!(parentObj instanceof ObjectId))
			{
				String str = Utils.toHexaString(bytes, "");
				parameter.add(str);
			}
		}
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
